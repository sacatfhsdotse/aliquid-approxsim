#ifndef _SERVER_H
#define _SERVER_H

#define BACKLOG                (100)        // max request backlog

// System
#include <map>
#include <string>

// Own
#include "stdint.h"
#include "Buffer.h"
#include "Engine.h"
#include "ClientValidator.h"
#include "TSQueue.h"


class Session;
class ApproxsimServerSocket;
class ApproxsimSocket;


/**
 * \brief This class represents the server.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/24 10:14:35 $
 */
class Server {
private:
     /// Set to true if the current architecture is big endian.
     static bool sBigEndian;

     /// The socket used to receive connections .
     ApproxsimServerSocket* mSocket;

     /// Current number of sessions.
     int mNumSessions;

     /// Keeps track of which id to give to the next Session.
     int64_t mIdCount;

     /// Mapping session id to Session object.
     std::map<int64_t, Session*> mSessions;

     /// Pointer to the Buffer object.
     Buffer* mBuf;

     /// Pointer to the Engine object.
     Engine* mEng;

     /// Id of the avtive Session.
     int64_t mActiveId;

     /// Queue used to communicate with the dispatcher thread.
     TSQueue<ApproxsimSocket*>      mConQ;

     /// Used to validate incomming connections.
     ClientValidator* mClientValidator;

public:
     Server(int port, const std::string& host,
            ClientValidator* clientValidator);
     ~Server();

     void start();
     void notifyClosure(int64_t id);

     /**
      * \brief Checks if the Server currently has an active Session,
      * i.e. if there is a client connected that is active.
      *
      * \return True if there is an active client.
      */
     bool hasActiveClient() const { return mActiveId != -1; }

     static void sigpipe_handle(int sig);
     static void *dispatcherThreadMain(Server* data);

     /**
      * \brief Returns endian of current architecture.
      *
      * \return Endian of current architecture.
      */
     static bool bigEndian() { return sBigEndian; }

     /**
      * \brief Accessor for the map of Sessions.
      *
      * \return The map of Sessions.
      */
     const std::map<int64_t, Session*>& sessions() const { return mSessions; }

     static void handleTemporarySession(Server& server, ApproxsimSocket& sock);
};

#endif   // _SERVER_H
