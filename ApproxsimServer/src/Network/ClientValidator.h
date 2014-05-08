// $Id: ClientValidator.h,v 1.1 2006/07/21 13:35:29 dah Exp $
#ifndef APPROXSIM_CLIENTVALIDATOR_H
#define APPROXSIM_CLIENTVALIDATOR_H

// Own
#include "Socket.h"

/**
 * \brief Class that validetes if a client may connect.
 *
 * \author   Daniel Ahlin
 * \date     $Date: 2006/07/21 13:35:29 $
 */
class ClientValidator {
public:
     virtual ~ClientValidator() {}
     virtual bool isValidClient(const Socket* socket) = 0;
};

/**
 * \brief Class that allows any client to connect.
 *
 * \author   Daniel Ahlin
 * \date     $Date: 2006/07/21 13:35:29 $
 */
class PassClientValidator : public ClientValidator{
public:
     virtual ~PassClientValidator() {}
     virtual bool isValidClient(const Socket* socket) {return true;}
};

#endif   // APPROXSIM_CLIENTVALIDATOR_H
