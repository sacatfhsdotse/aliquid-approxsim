// System
#include <iostream>
#include <cstdlib>

// Own
#include "Declaration.h"
#include "Environment.h"
#include "Type.h"
#include "TypeAttribute.h"
#include "XSDContent.h"

using namespace std;


/// Destructor
Type::~Type()
{
     for(vector<const Declaration*>::iterator it = mSubElements.begin(); it != mSubElements.end(); it++) {
	  delete *it;
     }
     for(std::map<string, const Declaration*>::iterator it = mSubElementsMap.begin(); it != mSubElementsMap.end(); it++) {
	  delete it->second;
     }
     for(vector<const TypeAttribute*>::iterator it = mAttributes.begin(); it != mAttributes.end(); it++) {
	  delete *it;
     }
     for(std::map<string, const TypeAttribute*>::iterator it = mAttributesMap.begin(); it != mAttributesMap.end(); it++) {
	  delete it->second;
     }
}


/**
 * \brief Gets a sub element of this Type
 *
 * \param name The name of the subelement to get.
 * \return The sub element with the specified name or null of no such
 * element was found.
 */
const Declaration* Type::getSubElement(const std::string& name) const
{
     std::map<std::string, const Declaration*>::const_iterator it = mSubElementsMap.find(name);
     return (it == mSubElementsMap.end() ? 0 : it->second);
}


/**
 * \brief Appends a subelement to this Type.
 *
 * \param dec The Declaration to append.
 */
void Type::appendSubElement(Declaration& dec)
{
     mSubElements.push_back(&dec);
     mSubElementsMap[dec.getName()] = &dec;
}

/**
 * \brief Appends an attribute to this Type.
 *
 * \param ta The attirbute to append.
 */
void Type::appendAttribute(TypeAttribute& ta)
{
     mAttributes.push_back(&ta);
     mAttributesMap[ta.getName()] = &ta;
}

/**
 * \brief Checks if this Type inherits from the Type with the
 * specified name in the default namespace.
 *
 * \param typeName The name of the Type to check inheritance from.
 * \return True if this Type inherits from the provided Type.
 */
bool Type::canSubstitute(const std::string& typeName) const
{
     return canSubstitute(typeName, Environment::DEFAULT_SCHEMA_NAMESPACE);
}

/**
 * \brief Checks if this Type inherits from the Type with the
 * specified name and namespace.
 *
 * \param typeName The name of the Type to check inheritance from.
 * \param nameSpace The namespace of the Type to check inheritance
 * from.
 * \return True if this Type inherits from the provided Type.
 */
bool Type::canSubstitute(const std::string& typeName, const std::string& nameSpace) const
{
     return canSubstitute(mXSDContent.getType(typeName, nameSpace));
}


/**
 * \brief For debugging purposes.
 *
 * \param o The stream to write to.
 * \param t The Type to print.
 * \return The provided ostream with the Type written to it.
 */
std::ostream& operator << (std::ostream& o, const Type& t)
{
     o << "=== " << t.getName() << (t.abstract() ? " (abstract)" : "") << endl;
     if (!t.mAttributes.empty()) {
	  o << "Attributes:" << endl;
	  for(vector<const TypeAttribute*>::const_iterator it = t.mAttributes.begin(); it != t.mAttributes.end(); it++) {
	       o << **it;
	  }
     }
     if (!t.mSubElements.empty()) {
	  o << "Elements:" << endl;
	  for(vector<const Declaration*>::const_iterator it = t.mSubElements.begin(); it != t.mSubElements.end(); it++) {
	       o << **it;
	  }
     }
     return o;
}
