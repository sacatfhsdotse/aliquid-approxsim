// $Id: MemEntityResolver.h,v 1.1 2006/07/21 13:35:29 dah Exp $
#ifndef STRATMAS_MEMENTITYRESOLVER_H
#define STRATMAS_MEMENTITYRESOLVER_H

// System
#include <string>

// Xerces
#include <xercesc/util/XMLEntityResolver.hpp>
#include <xercesc/sax/EntityResolver.hpp>

namespace XERCES_CPP_NAMESPACE {
     class InputSource;
}

XERCES_CPP_NAMESPACE_USE

/**
 * \brief This class provides schemas to the xml parser used in
 * XMLHandler.
 *
 * \author   Daniel Ahlin
 * \date     $Date: 2006/07/21 13:35:29 $
 */
class MemEntityResolver : public XMLEntityResolver, public EntityResolver {
 private:
     XMLEntityResolver* mpXMLEntityResolverFallback;
     EntityResolver* mpEntityResolverFallback;
     
     virtual InputSource* resolve(const XMLCh *const publicId);
 public:
     MemEntityResolver(XMLEntityResolver* xmlEntityResolverFallback = 0,
                       EntityResolver* entityResolverFallback = 0);
     virtual ~MemEntityResolver();

     virtual InputSource* resolveEntity(const XMLCh *const publicId, 
                                        const XMLCh *const systemId);
     virtual InputSource* resolveEntity(XMLResourceIdentifier 
                                        *resourceIdentifier);
     virtual InputSource* resolve(const std::string& publicId);
};

#endif   // STRATMAS_MEMENTITYRESOLVER_H
