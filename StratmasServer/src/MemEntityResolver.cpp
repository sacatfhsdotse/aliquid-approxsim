// $Id: MemEntityResolver.cpp,v 1.1 2006/07/21 13:35:29 dah Exp $
// System
#include <iostream>

// Xerces
#include <xercesc/framework/MemBufInputSource.hpp>

// Own
#include "MemEntityResolver.h"
#include "schemas.h"
#include "StrX.h"

/**
 * \brief Creates a new MemEntitiyResolver that tries to deliver
 * entitys from an internal lookup table, if unable to fullfill the
 * request it will consult provided fallback resolver, if any.
 */
MemEntityResolver::MemEntityResolver(XMLEntityResolver 
				     *xmlEntityResolverFallback,
				     EntityResolver *entityResolverFallback) : 
     mpXMLEntityResolverFallback(xmlEntityResolverFallback),
     mpEntityResolverFallback(entityResolverFallback)
{
}

/**
 * \brief Destroys this resolver.
 */
MemEntityResolver::~MemEntityResolver()
{
}

/**
 * \brief Returns an InputSource for the provided resource
 * idententifier, or null if unable to fullfill the request.
 *
 * \returns an InputSource for the resourceidentifier, or null if
 * unable to provide one. The returned InputSource is owned by the
 * caller which is responsible to clean up the memory.
 */
InputSource* MemEntityResolver::resolve(const XMLCh *const systemId)
{
     return resolve(std::string(StrX(systemId).str()));
}

/**
 * \brief Returns an InputSource for the provided resource
 * idententifier, or null if unable to fullfill the request.
 *
 * \returns an InputSource for the resourceidentifier, or null if
 * unable to provide one. The returned InputSource is owned by the
 * caller which is responsible to clean up the memory.
 */
InputSource* MemEntityResolver::resolve(const std::string& systemId)
{
     InputSource* pRes = 0;

     // Look for schemas included during compilation
     const char* const compiled = ::file2h_lookup(systemId.c_str());
     if (compiled != 0) {
	  MemBufInputSource *memBuf = 
	       new MemBufInputSource((XMLByte*) compiled, strlen(compiled),
				     XStr(systemId.c_str()).str());
	  memBuf->setCopyBufToStream(false);
	  pRes = memBuf;	       
     }

     return pRes;
}

/**
 * \brief Returns an InputSource for the provided resource
 * idententifier. If unable to fullfill the request and a fallback
 * were provided in the constructor, the function will return the
 * answer of the fallback.
 *
 * \returns An input source for the entity, or null if unable to
 * provide one, the returned InputSource is owned by the parser which
 * is responsible to clean up the memory.
 */
InputSource* MemEntityResolver::resolveEntity(XMLResourceIdentifier 
					      *resourceIdentifier)
{
     // Try ourselves
     InputSource *pRes = resolve(resourceIdentifier->getSystemId());

     if (pRes == 0 && mpXMLEntityResolverFallback != 0) {
	  // On failure try fallback if available.
	  pRes = 
	       mpXMLEntityResolverFallback->resolveEntity(resourceIdentifier);
     }

     return pRes;
}

InputSource* MemEntityResolver::resolveEntity(const XMLCh *const publicId,
					      const XMLCh *const systemId)
{
     // Try ourselves
     InputSource *pRes = resolve(systemId);;

     if (pRes == 0 && mpEntityResolverFallback != 0) {
	  // On failure try fallback if available.
	  pRes = mpEntityResolverFallback->resolveEntity(publicId, systemId);
     }
     
     return pRes;
}
