// $Id: WinSocket.cpp,v 1.3 2006/09/05 14:18:21 dah Exp $

// Windows adaptation of socket.

// System
#include <winsock2.h>
#include <iostream>
#include <sstream>
#include <string.h>
#include <stdlib.h>

// Own
#include "debugheader.h"
#include "WinSocket.h"
#include "Socket.h"
#include "LogStream.h"


using namespace std;

typedef int socklen_t;

// Init the socket library, or fail
bool WinSocket::sWSADataInitialized = WinSocket::initWinSocketLibrary();
WSADATA WinSocket::sWSAData;

bool WinSocket::initWinSocketLibrary()
{
  int status = ::WSAStartup(MAKEWORD(2,2), &WinSocket::sWSAData);
  if (status != 0) {
    slog << "Failed to initialize Windows Socket library: error " << status
	 << logEnd;
    abort();
    // Never happens.
    return false;
  }

  return true;
}

/**
 * \brief Constructor.
 */
WinSocket::WinSocket() : mSock (INVALID_SOCKET)
{
  memset(&mAddr, 0, sizeof(mAddr));
}

/**
 * \brief Destructor.
 */
WinSocket::~WinSocket()
{
     if (valid()) {
       close();
     }
}

/**
 * \brief Checks if this socket is valid.
 *
 * \return True if this socket is valid, false otherwise.
 */
bool WinSocket::valid() const
{ 
  return mSock != INVALID_SOCKET; 
}

/**
 * \brief Closes the socket
 *
 * \return True on success.
 */
bool WinSocket::close() 
{
  if (::closesocket(mSock) == SOCKET_ERROR) {
    slog << "Error closing socket: " << ::WSAGetLastError() << logEnd;
    return false;
  } else {
    return true;
  }  
}

/**
 * \brief Creates the underlying socket.
 *
 * \return True if the socket was successfully created, false
 * otherwise.
 */
bool WinSocket::create()
{
     bool ret = false;

     mSock = ::socket(AF_INET, SOCK_STREAM, 0);

     if (valid()) {
	  const int on = 1;
	  if (::setsockopt(mSock, SOL_SOCKET, SO_REUSEADDR, 
+			   (const char*) &on,
			   sizeof(on)) != SOCKET_ERROR) {
	       ret = true;
	  }
     }
     return ret;
}

/**
 * \brief Binds this socket to the provided address and port.
 *
 * \param host The name of the host or null if INADDR_ANY should be
 * used.
 * \param port The port.
 * \return True if the socket was successfully bound, false otherwise.
 */
bool WinSocket::bind(const char *host, int port)
{
     if (!valid()) {
	  return false;
     }

     struct hostent *tmp = 0;
     if (host) {
	  // tmp Is statically allocated (at least on Mac) so there is
	  // no need to deallocate it.
	  tmp = ::gethostbyname(host);
	  if (!tmp) {
	       slog << "Invaid interface '" << host << 
		 "'. Setting interface to localhost." << logEnd;
	       tmp = ::gethostbyname("localhost");
	  }
     }
     
     mAddr.sin_family      = AF_INET;
     mAddr.sin_addr.s_addr = (host ? *(unsigned int*)*tmp->h_addr_list : INADDR_ANY);
     mAddr.sin_port        = htons(port);

     debug("Server is binding to " << address() << ":" << port);

     int bindRet = ::bind(mSock, (struct sockaddr*)(&mAddr), sizeof(mAddr));

     if (bindRet == SOCKET_ERROR) {
	  slog << "WSAError: " << ::WSAGetLastError() << " - "  << logEnd;
	  return false;
     }

     return true;
}


/**
 * \brief Listens to connections.
 *
 * \return True if all is ok, false otherwise.
 */
bool WinSocket::listen() const
{
     if (!valid()) {
	  return false;
     }

     int listen_return = ::listen(mSock, MAXCONNECTIONS);

     if (listen_return == SOCKET_ERROR) {
	  return false;
     }

     return true;
}


/**
 * \brief Accepts a connection.
 *
 * \param newSock On return - the socket from which the connection was
 * accepted.
 * \return True if all is ok, false otherwise.
 */
bool WinSocket::accept(Socket& newSock) const
{
  WinSocket* newSockP = dynamic_cast<WinSocket*>(&newSock);

  int addr_length = sizeof(newSockP->mAddr);

  SOCKET socket = ::accept(mSock, (sockaddr*) &(newSockP->mAddr), 
			   (socklen_t*) &addr_length);
  
  newSockP->mSock = socket;
    
  if (socket == SOCKET_ERROR) {
    return false;
  }
  else {
    return true;
  }
}


/**
 * \brief Sends data over the socket.
 *
 * \param msg The data to send.
 * \param len The length in bytes of the data to be sent.
 * \return True if all is ok, false otherwise.
 */
bool WinSocket::send(const void *msg, unsigned int len) const
{
     const char *buf    = static_cast<const char*>(msg);
     int status = ::send(mSock, buf, len, 0);
     if (status == SOCKET_ERROR) {
	  return false;
     }
     else {
	  return true;
     }
}


/**
 * \brief Receives data from the socket.
 *
 * \param msg On return - the data received.
 * \param len The maximum length in bytes of the data to receive.
 * \return True if all is ok, false otherwise.
 */
int WinSocket::recv(void *msg, unsigned int len) const
{
     char *buf    = static_cast<char*>(msg);
     int status = ::recv(mSock, buf, len, 0);

     if (status == SOCKET_ERROR) {
	  slog << "status = SOCKET_ERROR ::WSAGetLastError = " << 
	    ::WSAGetLastError() << "  in WinSocket::recv()" << logEnd;
	  return 0;
     }
     else if (status == 0) {
	  return 0;
     }
     else {
	  return status;
     }
}

/**
 * \brief Receives an exact amount of data from the socket.
 *
 * \param msg On return - the data received.
 * \param len The number of bytes to receive.
 * \return The total number of bytes read.
 */
int WinSocket::recvf(void *msg, int len) const
{
     char *buf    = static_cast<char*>(msg);
     unsigned int totRead  = 0;
     int lastRead = 0;

     for (int pos = 0; pos < len; pos += lastRead) {
	  lastRead = ::recv(mSock, buf + pos, len - pos, 0);
	  if (lastRead < 0) {
	    slog << "status == " << lastRead << " ::WSAGetLastError = " 
		 << ::WSAGetLastError() << " in WinSocket::recvf()" 
		 << logEnd;
	    return totRead;
	  }
	  else if (lastRead == 0) {
	       return totRead;
	  }
	  totRead += lastRead;
     }
     return totRead;
}

/**
 * \brief Connects to the specified port on the specified host.
 *
 * \param host The host to connect to.
 * \param port The port to connect to.
 * \return True if all is ok, false otherwise.
 */
bool WinSocket::connect(const std::string host, const int port)
{
     if (!valid() || host == "") {
	  return false;
     }

     // tmp Is statically allocated (at least on Mac) so there is no need
     // to deallocate it.
     struct hostent *tmp = gethostbyname(host.c_str());
     
     mAddr.sin_family      = AF_INET;
     mAddr.sin_addr.s_addr = *(unsigned int*)*tmp->h_addr_list;
     mAddr.sin_port        = htons(port);

     int address = htonl(*(unsigned int*)*tmp->h_addr_list);
     ostringstream ost;     
     ost << ((int)(address>>24)&0xFF) << "." 
	 << ((int)(address>>16)&0xFF) << "."
	 << ((int)(address>>8)&0xFF) << "." 
	 << ((int)address&0xFF) << ":"
	 << port;
     debug("Server is connecting to " << ost.str());

     int status = ::connect(mSock, (sockaddr*) &mAddr, sizeof(mAddr));

     if ( status != SOCKET_ERROR )
       return true;
     else {
       slog << "::WSAGetLastError(): " << ::WSAGetLastError() << logEnd;
       return false;
     }
}

/**
 * \brief Set the O_NONBLOCK flag.
 *
 * \param b New value of the O_NONBLOCK flag.
 */
void WinSocket::set_non_blocking(const bool b)
{
  u_long opts;
  int status;
    
  if (b) {
    opts = 1;
  } else {
    opts = 0;
  }

  if ( ::ioctlsocket(mSock, FIONBIO, &opts) == SOCKET_ERROR ) {
    slog << "set_non_blocking failed with code: "  << ::WSAGetLastError() 
	 << logEnd; 
  }
}

/**
 * \brief Returns a string representation of the address of this
 * socket.
 *
 * \return A string representation of the address of this socket.
 */
std::string WinSocket::address() const
{
     ostringstream ost;
     u_long tmp = ::ntohl(mAddr.sin_addr.s_addr);
     ost << ((int)(tmp>>24)&0xFF) << "." 
	 << ((int)(tmp>>16)&0xFF) << "."
	 << ((int)(tmp>>8)&0xFF) << "." 
	 << ((int)tmp&0xFF);
     return ost.str();
}
