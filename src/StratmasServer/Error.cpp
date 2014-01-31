// System
#include <string>

// Own
#include "Error.h"
#include "Reference.h"
#include "XMLHelper.h"


using namespace std;


/**
 * \brief Gets the type of this error as a string
 *
 * \return The type of this error as a string.
 */
string Error::typeStr() const
{
     switch (mType) {
     case eWarning:
	  return "warning";
	  break;
     case eGeneral:
	  return "general";
	  break;
     case eFatal:
	  return "fatal";
	  break;
     default:
	  // Unknown error type must be fatal...
	  return "fatal";
	  break;
     }
}

/**
 * \brief Writes the provided Reference to this Error's message
 * stream.
 *
 * \param ref The Reference to write.
 * \return A reference to this Error.
 */
Error& Error::operator << (const Reference& ref) {
     mMsg << ref;
     return *this;
}

/**
 * \brief Produces the XML representation of this object
 *
 * \param o The stream to write the XML representation to.
 */
void Error::toXML(std::ostream& o) const {
     o << "<error>" << endl;
     o << "<type>"<< typeStr() << "</type>" << endl;
     o << "<description>" << XMLHelper::encodeSpecialCharacters(mMsg.str()) << "</description>" << endl;
     o << "</error>";
}


/**
 * \brief For debugging purposes.
 *
 * \param o The stream to write to.
 * \param e The Error to print.
 * \return The provided ostream with the Error written to it.
 */
std::ostream& operator << (std::ostream& o, const Error& e) {
     o << e.mMsg.str();
     return o;
}

