#ifndef _STRATMASSERVERSOCKET_H
#define _STRATMASSERVERSOCKET_H


// Own
#include "Socket.h"


/**
 * \brief ServerSocket user for listening to incoming stratmas
 * messages.
 *
 * \author Per Alexius
 * \date   $Date: 2005/06/13 13:41:15 $
 */
class StratmasServerSocket : public Socket {
public:
     StratmasServerSocket(const char *host, int port);
};


#endif   // _STRATMASSERVERSOCKET_H
