// Definition of the Socket class

#ifndef PosixSocket_class
#define PosixSocket_class


// System
#include <arpa/inet.h>
#include <netdb.h>
#include <netinet/in.h>
#include <string>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>

// Own
#include "SocketImpl.h"

/**
 * \brief C++ wrapper around a posix socket.
 *
 * \author Per Alexius
 * \date   $Date: 2006/07/03 14:18:23 $
 */
class PosixSocket : public SocketImpl {
protected:
     /// The file descriptor for the socket.
     int mSock;

     /// The name assigned to this socket.
     sockaddr_in   mAddr;

public:
     PosixSocket();
     virtual ~PosixSocket();

     // Server initialization
     virtual bool create();
     virtual bool bind(const char *host, int port);
     virtual bool listen() const;
     virtual bool accept(Socket &newSock) const;

     // Client initialization
     virtual bool connect(const std::string host, const int port);

     // Termination
     virtual bool close();

     // Data Transimission Primitives
     virtual bool send(const void *msg, unsigned int len) const;
     virtual int recv(void *msg, unsigned int len) const;
     virtual int recvf(void *msg, int len) const;

     // Socket settings.
     virtual void set_non_blocking(const bool b);

     // Socket Status
     virtual bool valid() const;
     virtual std::string address() const;
};


#endif
