#ifndef APPROXSIM_IPADDRESS_H
#define APPROXSIM_IPADDRESS_H


// System
#include <iosfwd>
#include <string>


/**
 * \brief Class representing an IP address.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/05/23 09:57:34 $
 */
class IPAddress {
private:
     // The integer parts of the IP address (0 = the leftmost etc.)
     int mParts[4];

public:
     IPAddress(const std::string& ip);
     IPAddress(const IPAddress& ip);

     std::string toString() const;
     bool operator == (const IPAddress& ip) const;
     bool operator == (const std::string& ip) const;
     friend std::ostream& operator << (std::ostream& o, const IPAddress& ip);
};

#endif   // APPROXSIM_IPADDRESS_H
