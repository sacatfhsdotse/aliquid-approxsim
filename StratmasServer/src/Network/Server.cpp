// System
#include <cstdlib>
#include <sstream>
#include <boost/thread.hpp>
#include <boost/bind.hpp>

// Own
#include "debugheader.h"
#include "Environment.h"
#include "Server.h"
#include "Session.h"
#include "SocketException.h"
#include "StratmasSocket.h"
#include "StratmasServerSocket.h"
#include "LogStream.h"
#include "Log4C.h"

// Temporary
#include "StratmasMessage.h"
#include "XMLHandler.h"

//void *keyboardListener(void *data);


using namespace std;


// Static Definitions
static const int one = 1;
bool Server::sBigEndian = (!(*(char*)&one));
static const bool detachThreads = true;


/**
 * \brief Creates a server object.
 *
 * \param port The port to listen to connections on.
 * \param host The interface to listen to connections on.
 * \param client Explicitly defined ip from which to allow
 * connections or NULL if no such client was specified.
 * \param validIPFile Name of explicitly specified validIPFile or NULL
 * if no such file was specified.
 * \param ipValidation Set to true if ip validation should be applied,
 * false otherwise.
 */
Server::Server(int port, const std::string &host, 
               ClientValidator* clientValidator)
     : mNumSessions(0), mIdCount(0), mBuf(new Buffer()), 
       mEng(new Engine(*mBuf)), mActiveId(-1), 
       mClientValidator(clientValidator)
{ 
     LOG_FATAL(networkLog, "host = " << (host.empty() ? "any" : host) << ", port = " << port );

     try {
          mSocket = new StratmasServerSocket(host.empty() ? 0 : host.c_str(), 
                                             port);
     } catch (SocketException e) {
          slog << "Error when creating StratmasServerSocket - " 
               << e.description() << logEnd;
          exit(1);
     } catch (...) {
          slog << "Error when creating StratmasServerSocket - " << logEnd;
          exit(1);
     }     
}

/**
 * \brief Destructor.
 */
Server::~Server()
{
     if (mSocket) { delete mSocket; }
     delete mEng;
     delete mBuf;
}

/**
 * \brief Starts the server.
 */
void Server::start()
{
     // Start Engine (get mEng to thread (void*)mEng)
     boost::thread tmpObj(boost::bind(&Engine::start, (void*)mEng));

     // Start Dispatcher (get this to thread (void*)this)
     boost::thread tmpObj2(boost::bind(&Server::dispatcherThreadMain, (void*)this));

     // Loop through requests
     while (true) {
          StratmasSocket *sock = 0;

          // Accept request
          try {
               sock = new StratmasSocket();
               if (!mSocket->accept(*sock)) {
                    slog << "Error while accepting connection. Ignoring..." 
                         << logEnd;
                    delete sock;
               }
          } catch (...) {
               slog << "Error occured" << logEnd;
               delete sock;
               continue;
          }

          if (mClientValidator != 0 && 
              !mClientValidator->isValidClient(sock)) {
               cout << "Connection from invalid ip address " 
                    << sock->address() << " rejected" << endl;
               delete sock;
               continue;
          }

          mConQ.enqueue(sock);
     }
}

/**
 * \brief Notifies the server about a Session that was ended.
 *
 * \param id The id of the Session that was ended.
 */
void Server::notifyClosure(int64_t id)
{
     mSessions.erase(id);
     if (id == mActiveId) {
          mActiveId = -1;
     }
     mNumSessions--;
}

/**
 * \brief Main function of the dispatcher thread.
 *
 * The dispatcher waits for a StratmasSocket (containing data about
 * a connection) to be enqueued by the Server main thread. When this
 * happens, the dispatcher creates a thread for handling the
 * connection and gives that thread access to the correct Session
 * object. The dispatcher keeps doing this until it dequeues a NULL
 * pointer that indicates that it is ok to quit.
 *
 * \param data A Server object.
 *    
 * \return NULL if everything is ok.
 */
void *Server::dispatcherThreadMain(void *data) {
     Server &server = *static_cast<Server*>(data);

     int64_t          id;
     Session          *sess;
     StratmasSocket   *sock;

     while (true) {
          sock = server.mConQ.dequeue();
          if (!sock) {
               stratmasDebug("Dispatcher thread dequeued a null socket. This should not happen!!!");
               continue;
          }
          try {
               id = sock->recvStratmasHeader();
               if (id != 0) {
                    cout << "Connection accepted from " << sock->address() << endl;
               }
          } catch (SocketException e) {
               slog << "Error when receiving StratmasHeader: '" << e.description() << "'" << logEnd;
               delete sock;
               continue;
          } catch (...) {
               slog << "Error when receiving StratmasHeader: " << logEnd;
               delete sock;
               continue;
          }
          if (id == -1) {   // If this is a new client calling...
               sock->id(++server.mIdCount);
               if (server.mActiveId == -1) {
                    server.mActiveId = sock->id();
                    sess = new Session(server, *server.mEng, *server.mBuf, true, sock);
               }
               else {
                    sess = new Session(server, *server.mEng, *server.mBuf, false, sock);
               }
               server.mSessions[sock->id()] = sess;
               server.mNumSessions++;
          }   
          else if (id == 0) {
               handleTemporarySession(server, *sock);
               delete sock;
               continue;
          }
          // Else if this client already has a Session
          else if (server.mSessions.find(id) != server.mSessions.end()) {
               sess = server.mSessions[id];
               if (!sess->setSocket(sock)) {
                    slog << "Tried to connect to a Session that is already connected." << logEnd;
               }
          }
          else {   // This client has no Session but has still given an id...
               sock->id(++server.mIdCount);
               if (server.mActiveId == -1) {
                    server.mActiveId = sock->id();
                    sess = new Session(server, *server.mEng, *server.mBuf, true, sock);
               }
               else {
                    sess = new Session(server, *server.mEng, *server.mBuf, false, sock);
               }
               server.mSessions[sock->id()] = sess;
               server.mNumSessions++;
               slog << "Id given but there was no matching Session" << logEnd;
               // Somehow produce a warning...
          }

          // Spawn session (get sess to thread (void*)sess) fix thread
          // detach.
          boost::thread tmpObj(boost::bind(&Session::staticStart, (void*) sess));
     }
     
     stratmasDebug("Congratulations! You have just done the impossible...");

     return 0;
}


/**
 * \brief Handles temporary Sessions.
 *
 * Used for LoadQueryMessages.
 *
 * \param server The server object.
 * \param sock The StratmasSocket that the temporary session uses.
 */
void Server::handleTemporarySession(Server& server, StratmasSocket& sock)
{
     std::string xml;
     sock.recvStratmasMessage(xml);
     XMLHandler xmlh(*server.mBuf,
                     Environment::DEFAULT_SCHEMA_NAMESPACE,
                     Environment::STRATMAS_PROTOCOL_SCHEMA,
                     -1);
     ostringstream ost;
     ost.precision(24);
     try {
          switch (xmlh.handle(xml)) {
          case eLoadQuery: {
               LoadQueryResponseMessage msg(server);
               msg.toXML(ost);
               break;
          }
          default:
               break;
          }
     }
     catch (Error& e) {
          slog << "Server caught Error - " << e << logEnd;
          ost.str("");
          StatusMessage msg(xmlh.lastType());
          msg.addError(e);
          msg.toXML(ost);
     }
     catch (vector<Error>& e) {
          slog << "Server caught Errors:" << logEnd;
          ost.str("");
          StatusMessage msg(xmlh.lastType());
          for(vector<Error>::iterator it = e.begin(); it != e.end(); it++) {
               slog << *it << logEnd;
               msg.addError(*it);
          }
          msg.toXML(ost);
     }
     sock.sendStratmasMessage(ost.str());
}

// void *keyboardListener(void *data) {
//      Server* server = static_cast<Server*>(data);
//      string line;
//      while (line != "q") {
//           cin >> line;
//      }
//      stratmasDebug("Cleaning up server and shutting down...");
//      delete server;
//      exit(0);
//      return 0;
// }

