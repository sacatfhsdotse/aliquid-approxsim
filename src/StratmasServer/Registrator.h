// $Id: Registrator.h,v 1.8 2006/07/21 13:35:29 dah Exp $
#ifndef STRATMAS_REGISTRATOR_H
#define STRATMAS_REGISTRATOR_H

// System
#include <string>

// Own
#include "Socket.h"

class StratmasServerSocket;

/**
 * \brief This class represents a registration to the dispatcher.
 *
 * \author   Daniel Ahlin
 * \date     $Date: 2006/07/21 13:35:29 $
 */
class Registrator {
private:
     /// The hostname of the dispatcher.
     std::string dispatcherHost;
     /// The port of the dispatcher
     int dispatcherPort;

     /// The hostname of the server.
     std::string host;
     /// The port of the server
     int port;
     bool registerServerOnce();
public:
     Registrator(std::string dHost, int dPort, std::string sHost, int sPort);
     
     /**
      * \brief This destructor will leak the memory for
      * dispatcherHost, and, possibly host.
      */
     ~Registrator() {}
     
     bool registerServer(int retries = 4);
};

/**
 * \brief This class represents a connection to a dispatcher.
 *
 * \author   Daniel Ahlin
 * \date     $Date: 2006/07/21 13:35:29 $
 */
class DispatcherSocket : public Socket {
public:
     DispatcherSocket(std::string host, int port);
     bool sendDispatcherMessage(const std::string msg);
};

#endif   // STRATMAS_REGISTRATOR_H
