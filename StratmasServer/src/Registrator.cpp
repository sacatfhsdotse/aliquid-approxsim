// $Id: Registrator.cpp,v 1.3 2006/08/29 17:18:42 dah Exp $
// System
#include <sstream>
#include <iostream>
#include <string>
#include <cstdio>
#include <cstdlib>

// Own
#include "debugheader.h"
#include "StratmasSocket.h"
#include "SocketException.h"
#include "Registrator.h"
#include "Environment.h"
#include "GoodStuff.h"
#include "stdint.h"

/**
 * \brief Creates a new registrator.
 * 
 * \param dHost the name of the dispatcher.
 * \param dPort the port of the dispatcher.
 * \param sHost the name of the server.
 * \param sPort the port of the server.
 */
Registrator::Registrator(std::string dHost, int dPort, 
                         std::string sHost, int sPort) :
          dispatcherHost(dHost), dispatcherPort(dPort), port(sPort) 
{
     // Special handling if listening to the any-address:
     if (sHost.empty()) {
          char self[40];
          if (gethostname(self, 40) < 0) {
               perror(Environment::getProgramName().c_str());
               exit(1);
          } else {
               host = std::string(self);
          }
     } else {
          host = sHost;
     }
}

/**
 * /brief Tries to register the server with the dispatcher.
 */
bool Registrator::registerServerOnce() {
     std::ostringstream ost;
     ost << "<?xml version=\"1.0\"?>"
          "<dispatcherRequest " 
          "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
          "xsi:type=\"RegistrationRequest\">"
          "<stratmasServer>"
          "<host>" << host << "</host>"
          "<port>" << port << "</port>" 
          "<hasActiveClient>false</hasActiveClient>"
          "<isPending>true</isPending>"
          "</stratmasServer>"
          "</dispatcherRequest>";
     try {
          DispatcherSocket socket(dispatcherHost, dispatcherPort);
          socket.sendDispatcherMessage(ost.str());
          return true;
     } catch (SocketException e) {
          return false;
     }
}

/**
 * /brief Registers the server with the dispatcher.
 */
bool Registrator::registerServer(int tries)
{
     for (int i = 0; i < tries; i++) {
          if (registerServerOnce()) {
               return true;
          } else {
               Environment::milliSleep(1000);
          }
     }

     return false;
}

/**
 * \brief Sends a stratmas message.
 *
 * \param msg The message to send.
 * \return True if all is ok.
 */
bool DispatcherSocket::sendDispatcherMessage(const std::string msg) {
     int32_t len = htonl(msg.size());

     if (!send(&len, 4)) {
          return false; 
     }
     
     return send(msg.c_str(), msg.size());
}

/**
 * \brief Creates a socket that connects to the specified host and
 * port.
 *
 * \param host The host to connect to.
 * \param port The port to connect to.
 */
DispatcherSocket::DispatcherSocket(std::string host, int port)
     : Socket()
{
     if (!Socket::create()) {
          throw SocketException("Could not create client socket.");
     }
     
     if (!Socket::connect(host, port)) {
          throw SocketException("Could not bind to port.");
     }
}
