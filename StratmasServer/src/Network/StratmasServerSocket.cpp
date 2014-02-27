// Own
#include "StratmasServerSocket.h"
#include "SocketException.h"


/**
 * \brief Creates a socket that will listen to connections on the
 * specified port and hostname.
 */
StratmasServerSocket::StratmasServerSocket(const char *host, int port) : Socket()
{
     if (!Socket::create()) {
          throw SocketException("Could not create server socket.");
     }     

     if (!Socket::bind(host, port)) {
          throw SocketException ("Could not bind to specified host and port.");
     }

     if (!Socket::listen()) {
          throw SocketException ("Could not listen to socket.");
     }
}
