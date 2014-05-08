#ifndef APPROXSIM_IPVALIDATOR_H
#define APPROXSIM_IPVALIDATOR_H


// System
#include <set>
#include <string>

// Own
#include "Socket.h"
#include "ClientValidator.h"

/**
 * \brief Class that stores ip numbers that the server should allow
 * connections from.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/21 13:35:29 $
 */
class IPValidator : public ClientValidator {
private:
     /**
      * \brief A set with ip numbers that the server should allow
      * connections from.
      */
     std::set<std::string> mIPSet;

     static bool isStringIP(const std::string& str);
     
public:
     virtual ~IPValidator() {}
     bool getValidIPsFromFile(const std::string& filename);
     bool addValidIP(const std::string& ipToAdd);
     bool isValidIP(const std::string& ipToValidate);
     virtual bool isValidClient(const Socket* socket) { 
          return isValidIP(socket->address());
     }
     int numValidIPs() const { return mIPSet.size(); }
};

#endif   // APPROXSIM_IPVALIDATOR_H
