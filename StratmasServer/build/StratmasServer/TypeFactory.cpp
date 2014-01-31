// System

// Xerces
#include <xercesc/framework/MemBufInputSource.hpp>

// Own
#include "Environment.h"
#include "TypeFactory.h"
#include "XSDContent.h"
#include "MemEntityResolver.h"

extern const char* file2h_lookup[];

// Static Definitions
XSDContent* TypeFactory::sXSDContent = 0;


/**
 * \brief Creates a Type object that corresponds to the xml schema
 * type with the specified name.
 *
 * \param typeName The name of the Type.
 * \return The Type with the specified name.
 */
const Type& TypeFactory::getType(const std::string& typeName)
{
     return getType(typeName, Environment::DEFAULT_SCHEMA_NAMESPACE);
}


/**
 * \brief Creates a Type object that corresponds to the xml schema
 * type with the specified name and namespace.
 *
 * \param typeName The name of the Type.
 * \param nameSpace The namespace of the Type.
 * \return The Type with the specified name and namespace.
 */
const Type& TypeFactory::getType(const std::string& typeName, const std::string& nameSpace)
{
     if (sXSDContent == 0) {
	  MemEntityResolver r;
	  InputSource* inp = r.resolve(Environment::STRATMAS_PROTOCOL_SCHEMA);
	  sXSDContent = XSDContent::create(inp);
	  delete inp;
     }
     return sXSDContent->getType(typeName, nameSpace);
}
