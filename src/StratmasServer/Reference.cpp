// System
#include <ostream>

// Xerces-c
#include <xercesc/dom/DOMElement.hpp>

// Own
#include "debugheader.h"
#include "Error.h"
#include "Reference.h"
#include "XMLHelper.h"

// Static Definitions
const Reference Reference::sRoot("root");
const Reference Reference::sNull("null");


/**
 * \brief Copy constructor. Mustn't be used.
 *
 * \param ref The Reference that we aren't allowed to copy.
 */
Reference::Reference(const Reference& ref) {
     Error e;
     e << "Tried to use copyconstructor for Reference " << *this;
     throw e;
}
     
/**
 * \brief Destructor.
 */
Reference::~Reference() {
     std::map<const std::string, const Reference*>::iterator it;
     for (it = mChildren.begin(); it != mChildren.end(); it++) {
	  delete it->second;
     }
}

/**
 * \brief Deletes and frees memory for all existing References except
 * the root and null references that are statically allocated.
 */
void Reference::cleanUp() {
     std::map<const std::string, const Reference*>::iterator it;
     for (it = root().mChildren.begin(); it != root().mChildren.end(); it++) {
	  delete it->second;
     }
     root().mChildren.clear();

     for (it = nullRef().mChildren.begin(); it != nullRef().mChildren.end(); it++) {
	  delete it->second;
     }
     nullRef().mChildren.clear();
}

/**
 * \brief Gets the Reference with the specified scope and name.
 *
 * \param scope The scope of the Reference to get.
 * \param name The name of the Reference to get.
 * \return The Reference with the specified scope and name.
 */
const Reference& Reference::get(const Reference& scope, const std::string& name)
{
     std::map<const std::string, const Reference*>::const_iterator it;
     it = scope.mChildren.find(name);
     if (it == scope.mChildren.end()) {
	  const Reference *r = new Reference(scope, name);
	  scope.mChildren[r->mName] = r;
	  return *r;
     }
     else {
	  return *it->second;
     }
}

/**
 * \brief Gets the Reference specified in the provided DOMElement.
 *
 * \param n Pointer to the DOMElement to get the Reference from.
 * \return The Reference extracted from the provided DOMElement.
 */
const Reference &Reference::get(const DOMElement* n) {
     if (!n) {
	  Error e;
	  e << "Invalid Stratmas message. Null element node in Reference::Reference()";
	  throw e;
     }
     
     std::string name;
     XMLHelper::getString(*n, "name", name);
     DOMElement *scopeElem = XMLHelper::getFirstChildByTag(*n, "scope");

     // Recursion - We have a scope, so call get(DOMElement*) for
     // the DOMElement representing that scope.
     if (scopeElem) {
	  return get(get(scopeElem), name);
     }
     // Base case - no scope => Reference is a child of the root
     // Reference.
     else {
	  return get(root(), name);
     }
}

/**
 * \brief Produces the XML representation of this object.
 *
 * \param o The stream to which the object is written.
 * \param indent Intention string for readable output.
 * \return The ostream with the XML representation written to it.
 */
ostream& Reference::toXML(std::ostream& o, std::string indent) const {
     if(mScope) {
	  o << indent << "<name>" << XMLHelper::encodeSpecialCharacters(mName) << "</name>";
	  if (mScope->mScope) {
	       o << std::endl << indent << "<scope>" << std::endl;
	       mScope->toXML(o, indent + INDENT) << std::endl;
	       o << indent << "</scope>";
	  }
     }
     return o;
}

/**
 * \brief For debugging purpooses.
 *
 * \param o The ostream to write to.
 * \param r The Reference to write.
 * \return The ostream with the Reference written to it.
 */
std::ostream &operator << (std::ostream& o, const Reference& r) {
     if (r.mScope) {
	  o << *r.mScope << ":";
     }
     return o << r.mName;
}

