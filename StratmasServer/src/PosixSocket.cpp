#ifdef OS_LINUX
// System
#include <errno.h>
#include <fcntl.h>
#include <iostream>
#include <sstream>
#include "string.h"

// Own
#include "debugheader.h"
#include "PosixSocket.h"
#include "Socket.h"
#include "LogStream.h"

using namespace std;


// For build on Powerbook G4
// #ifdef __APPLE__
// typedef int socklen_t;
// #endif 


/**
 * \brief Constructor.
 */
PosixSocket::PosixSocket() : mSock (-1)
{
     memset(&mAddr, 0, sizeof(mAddr));
}

/**
 * \brief Destructor.
 */
PosixSocket::~PosixSocket()
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
bool PosixSocket::valid() const
{ 
  return mSock != -1; 
}

/**
 * \brief Closes the socket
 *
 * \return True on success.
 */
bool PosixSocket::close() 
{
  int status = ::close(mSock);
  if (status < 0) {
    slog << "Error closing socket: " << errno << " " << strerror(errno) 
	 << logEnd;
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
bool PosixSocket::create()
{
     bool ret = false;

     mSock = socket(AF_INET, SOCK_STREAM, 0);

     //debug("mSock == " << mSock << std::endl)

     if (valid()) {
	  const int on = 1;
	  if (setsockopt(mSock, SOL_SOCKET, SO_REUSEADDR, (const char*)&on, sizeof(on)) != -1) {
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
bool PosixSocket::bind(const char *host, int port)
{
     if (!valid()) {
	  return false;
     }

     struct hostent *tmp = 0;
     if (host) {
	  // tmp Is statically allocated (at least on Mac) so there is no need
	  // to deallocate it.
	  tmp = gethostbyname(host);
	  if (!tmp) {
	       slog << "Invalid interface '" << host << "'. Setting interface to localhost." << logEnd;
	       tmp = gethostbyname("localhost");
	  }
     }
     
     mAddr.sin_family      = AF_INET;
     mAddr.sin_addr.s_addr = (host ? *(unsigned int*)*tmp->h_addr_list : INADDR_ANY);
     mAddr.sin_port        = htons(port);

     debug("Server is binding to " << address() << ":" << port);

     int bindRet = ::bind(mSock, (struct sockaddr*)(&mAddr), sizeof(mAddr));

     if (bindRet == -1) {
	  slog << "errno: " << errno << " - " << strerror(errno) << logEnd;
	  return false;
     }

     return true;
}


/**
 * \brief Listens to connections.
 *
 * \return True if all is ok, false otherwise.
 */
bool PosixSocket::listen() const
{
     if (!valid()) {
	  return false;
     }

     int listen_return = ::listen(mSock, MAXCONNECTIONS);

     if (listen_return == -1) {
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
bool PosixSocket::accept(Socket& newSock) const
{
  PosixSocket* newSockP = dynamic_cast<PosixSocket*>(&newSock);

  int addr_length = sizeof(newSockP->mAddr);

  int socket = ::accept(mSock, (sockaddr*) &(newSockP->mAddr), 
			(socklen_t*) &addr_length);
  
  newSockP->mSock = socket;
    
  if (socket < 0 ) {
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
bool PosixSocket::send(const void *msg, unsigned int len) const
{
     int sent = ::send(mSock, msg, len, 0);
     if (sent == -1) {
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
int PosixSocket::recv(void *msg, unsigned int len) const
{
     int status = ::recv(mSock, msg, len, 0);

     if (status == -1) {
	  slog << "status = -1   errno = " << errno << " - " 
	       << strerror(errno) << "  in PosixSocket::recv()" << logEnd;
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
int PosixSocket::recvf(void *msg, int len) const
{
     char *buf    = static_cast<char*>(msg);
     unsigned int totRead  = 0;
     int lastRead = 0;

     for (int pos = 0; pos < len; pos += lastRead) {
	  lastRead = ::recv(mSock, buf + pos, len - pos, 0);
	  if (lastRead < 0) {
	       slog << "status == " << lastRead << " errno = " << errno 
		    << " - " << strerror(errno) 
		    << "  in PosixSocket::recvf()" << logEnd;
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
bool PosixSocket::connect(const std::string host, const int port)
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

     if ( status == 0 )
	  return true;
     else {
	  slog << "errno: " << errno << " - " << strerror(errno) << logEnd;
	  return false;
     }
}

/**
 * \brief Set the O_NONBLOCK flag.
 *
 * \param b New value of the O_NONBLOCK flag.
 */
void PosixSocket::set_non_blocking(const bool b)
{
     int opts = fcntl(mSock, F_GETFL);

     if (opts < 0) {
	  return;
     }

     if (b) {
	  opts = ( opts | O_NONBLOCK );
     }
     else {
	  opts = ( opts & ~O_NONBLOCK );
     }
     fcntl(mSock, F_SETFL, opts);
}

/**
 * \brief Returns a string representation of the address of this
 * socket.
 *
 * \return A string representation of the address of this socket.
 */
std::string PosixSocket::address() const
{
     ostringstream ost;
     uint32_t tmp = ntohl(mAddr.sin_addr.s_addr);
     ost << ((int)(tmp>>24)&0xFF) << "." << ((int)(tmp>>16)&0xFF) << "."
	 << ((int)(tmp>>8)&0xFF) << "." << ((int)tmp&0xFF);
     return ost.str();
}

#endif /* OS_LINUX */
