// Definition of the Socket class

#ifndef Socket_class
#define Socket_class


/**
 * \brief C++ wrapper around a C socket.
 *
 * \author Per Alexius
 * \date   $Date: 2006/07/03 14:18:23 $
 */
#ifdef __win__
#include "WinSocket.h"
class Socket : public WinSocket {
#else 
#include "PosixSocket.h"
class Socket : public PosixSocket {
#endif
protected:

public:
     Socket();
     virtual ~Socket();
};

#endif
