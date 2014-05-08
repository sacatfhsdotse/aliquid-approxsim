#ifndef _APPROXSIMSERVERSOCKET_H
#define _APPROXSIMSERVERSOCKET_H


// Own
#include "Socket.h"


/**
 * \brief ServerSocket user for listening to incoming approxsim
 * messages.
 *
 * \author Per Alexius
 * \date   $Date: 2005/06/13 13:41:15 $
 */
class ApproxsimServerSocket : public Socket {
public:
     ApproxsimServerSocket(const char *host, int port);
};


#endif   // _APPROXSIMSERVERSOCKET_H
