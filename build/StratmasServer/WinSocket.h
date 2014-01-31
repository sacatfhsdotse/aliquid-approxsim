// $Id: WinSocket.h,v 1.1 2006/07/03 14:18:24 dah Exp $
// Definition of the WinSocket class

#ifndef WinSocket_class
#define WinSocket_class


// System
#include <winsock2.h>

// Own
#include "SocketImpl.h"

/**
 * \brief C++ wrapper around a Windows socket.
 *
 * \author Daniel Ahlin
 * \date   $Date: 2006/07/03 14:18:24 $
 */
class WinSocket : public SocketImpl {
protected:
     /// Library init data:
     static WSADATA sWSAData;

     /// Library init success
     static bool sWSADataInitialized;

     /// The Windows socket object.
     SOCKET mSock;
     
     /// The name assigned to this socket.
     sockaddr_in   mAddr;

public:
     WinSocket();
     virtual ~WinSocket();

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
     
     // Socket implementation init
     static bool WinSocket::initWinSocketLibrary();
};

#endif
