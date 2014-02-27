// $Id: SocketImpl.h,v 1.1 2006/07/03 14:18:23 dah Exp $
// Definition of the SocketImpl class

#ifndef SocketImpl_class
#define SocketImpl_class

// System
#include <string>

// Own
const int MAXCONNECTIONS = 100;
const int MAXRECV        = 5000;

class Socket;

/**
 * \brief C++ socket implementation interface.
 *
 * \author Daniel Ahlin
 * \date   $Date: 2006/07/03 14:18:23 $
 */
class SocketImpl {
protected:
     virtual ~SocketImpl() {};
               
public:
     // Server initialization
     virtual bool create() = 0;
     virtual bool bind(const char *host, int port) = 0;
     virtual bool listen() const = 0;
     virtual bool accept(Socket &newSock) const = 0;

     // Client initialization
     virtual bool connect(const std::string host, const int port) = 0;

     // Termination
     virtual bool close() = 0;

     // Data Transimission Primitives
     virtual bool send(const void *msg, unsigned int len) const = 0;
     virtual int recv(void *msg, unsigned int len) const = 0;
     virtual int recvf(void *msg, int len) const = 0;

     // Socket settings.
     virtual void set_non_blocking(const bool b) = 0;

     // Socket Status
     virtual bool valid() const = 0;
     virtual std::string address() const = 0;
};

#endif // SocketImpl
