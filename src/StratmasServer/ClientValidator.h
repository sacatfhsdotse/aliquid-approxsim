// $Id: ClientValidator.h,v 1.1 2006/07/21 13:35:29 dah Exp $
#ifndef STRATMAS_CLIENTVALIDATOR_H
#define STRATMAS_CLIENTVALIDATOR_H

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

#endif   // STRATMAS_CLIENTVALIDATOR_H
