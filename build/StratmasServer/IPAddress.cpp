// System
#include <ostream>
#include <sstream>
#include <cstring>

// Own
#include "Error.h"
#include "IPAddress.h"


using namespace std;


/**
 * \brief Creates an IPAddress from a string.
 *
 * Ignores leading whitespace and all characters after the last
 * integer part.
 *
 * \param ip The string to create the IPAddress from.
 */
IPAddress::IPAddress(const std::string& ip)
{
     if (ip.length() < 7) {
	  Error e;
	  e << "'" << ip << "' is not a valid IP address.";
	  throw e;
     }

     char dot;
     istringstream ist(ip);
     for (int i = 0; i < 4; i++) {
	  ist >> mParts[i] >> dot;
	  if (mParts[i] < 0 || mParts[i] > 255 || (dot != '.' && i < 3)) {
	       Error e;
	       e << "'" << ip << "' is not a valid IP address.";
	       throw e;
	  }
     }
}

/**
 * \brief Copy constructor.
 *
 * \param ip The IPAddress to copy.
 */
IPAddress::IPAddress(const IPAddress& ip)
{
     memcpy(mParts, ip.mParts, 4 * sizeof(int));
}

/**
 * \brief Produces a string representation of this IPAddress.
 *
 * \return A string representation of this IPAddress.
 */
string IPAddress::toString() const
{
     ostringstream o;
     o << mParts[0] << '.' << mParts[1] << '.' << mParts[2] << '.' << mParts[3];
     return o.str();
}

/**
 * \brief Equality operator.
 *
 * \param ip The other IPAddress.
 * \return True if the IPAddresses are equal.
 */
bool IPAddress::operator == (const IPAddress& ip) const
{
     for (int i = 0;  i < 4; i++) {
	  if (mParts[i] != ip.mParts[i]) {
	       return false;
	  }
     }
     return true;
}

/**
 * \brief Equality operator for strings.
 *
 * \param ip A string representation of the other IPAddress.
 * \return True if the IPAddresses are equal.
 */
bool IPAddress::operator == (const std::string& ip) const
{
     try {
	  return IPAddress(ip) == *this;
     }
     catch (Error e) {
	  return false;
     }
}

/**
 * \brief For printing to ostreams.
 *
 * \param o The stream to write to.
 * \param ip The IPAddress to write.
 * \return The provided stream with the IPAddress written to it.
 */
ostream& operator << (ostream& o, const IPAddress& ip)
{
     return o << ip.toString();
}
