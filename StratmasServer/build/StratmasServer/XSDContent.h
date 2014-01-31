#ifndef STRATMAS_XSDCONTENT_H
#define STRATMAS_XSDCONTENT_H

// System
#include <map>
#include <string>

// Xerces
#include <xercesc/util/XercesDefs.hpp>

XERCES_CPP_NAMESPACE_USE

// Forward Declarations
class Type;

namespace XERCES_CPP_NAMESPACE {
     class InputSource;
     class XMLGrammarPool;
     class XSModel;
}


/**
 * \brief This class represents the contents of an xml schema
 * document.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/21 13:35:30 $
 */
class XSDContent {
private:
     /// The grammar pool to use.
     XMLGrammarPool* mGrammarPool;
     
     /// The XSModel created from the xml schema.
     XSModel* mXSModel;

     /// The namespace.
     const std::string mNamespace;

     /// Map containing the types already resolved.
     std::map<std::string, Type*> mResolvedTypes;

     void parseSchema(const InputSource& source);

public:
     XSDContent();
     ~XSDContent();     
     static XSDContent* createFromFile(const std::string& filename);
     static XSDContent* createFromString(const std::string& schemastring);
     static XSDContent* create(InputSource* schemaSource);
     const Type& getType(const std::string& typeName);
     const Type& getType(const std::string& typeName, const std::string& nameSpace);
};

#endif   // STRATMAS_XSDCONTENT_H
