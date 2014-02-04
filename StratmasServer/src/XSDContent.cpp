// System
#include <iostream>

// Xerces
#include <xercesc/framework/LocalFileInputSource.hpp>
#include <xercesc/framework/MemBufInputSource.hpp>
#include <xercesc/framework/psvi/XSModel.hpp>
#include <xercesc/framework/XMLGrammarPoolImpl.hpp>
#include <xercesc/sax2/SAX2XMLReader.hpp>
#include <xercesc/parsers/SAX2XMLReaderImpl.hpp>
#include <xercesc/sax2/XMLReaderFactory.hpp>
#include <xercesc/util/OutOfMemoryException.hpp>
#include <xercesc/util/PlatformUtils.hpp>

// Own
#include "Declaration.h"
#include "Environment.h"
#include "Error.h"
#include "ParserErrorReporter.h"
#include "StrX.h"
#include "TypeDefinition.h"
#include "TypeFactory.h"
#include "XSDContent.h"
#include "MemEntityResolver.h"
#include "LogStream.h"


using namespace std;


/**
 * \brief Default constructor.
 */
XSDContent::XSDContent() : mGrammarPool(0), mXSModel(0), mNamespace(Environment::DEFAULT_SCHEMA_NAMESPACE)
{
     try {
          XMLPlatformUtils::Initialize();
     }
     catch (const XMLException& toCatch) {
          slog << "Error during initialization! Message:" 
               << StrX(toCatch.getMessage()) << logEnd;
          XMLPlatformUtils::Terminate();
          return;
     }
}

/**
 * \brief Destructor.
 */
XSDContent::~XSDContent()
{
     if (mXSModel)     { delete mXSModel    ; }
     if (mGrammarPool) { delete mGrammarPool; }

     for (std::map<std::string, Type*>::iterator it = mResolvedTypes.begin(); it != mResolvedTypes.end(); it++) {
          delete it->second;
     }

     XMLPlatformUtils::Terminate();
}

/**
 * \brief Reads an xml schema from a file.
 *
 * \param filename The name of the file.
 * \return The newly created XSDContent.
 */
XSDContent* XSDContent::createFromFile(const string& filename)
{
     XSDContent* ret = new XSDContent();
     LocalFileInputSource lf(XStr(filename).str());
     ret->parseSchema(lf);
     return ret;
}

/**
 * \brief Reads an xml schema from a string.
 *
 * \param schemastring The string containing the schema.
 * \return The newly created XSDContent.
 */
XSDContent* XSDContent::createFromString(const string& schemastring)
{
     XSDContent* ret = new XSDContent();
     const char* xmlChar = schemastring.c_str();
     MemBufInputSource memBuf((XMLByte*)xmlChar, schemastring.size(), "Schema", false);
     memBuf.setCopyBufToStream(false);
     ret->parseSchema(memBuf);
     return ret;
}

/**
 * \brief Reads an xml schema from an InputSource.
 *
 * \param schemaSource The input source containing the schema.
 * \return The newly created XSDContent.
 */
XSDContent* XSDContent::create(InputSource* schemaSource)
{
     XSDContent* ret = new XSDContent();
     ret->parseSchema(*schemaSource);
     return ret;
}


/**
 * \brief Parses the schema.
 *
 * \param source The input source to create the XSDContent from.
 */
void XSDContent::parseSchema(const InputSource& source)
{
    SAX2XMLReaderImpl* parser = 0;

    Error e;
    bool errOcc = false;
    try {        
         mGrammarPool = new XMLGrammarPoolImpl(XMLPlatformUtils::fgMemoryManager);

         parser = new SAX2XMLReaderImpl(XMLPlatformUtils::fgMemoryManager, mGrammarPool);
         parser->setFeature(XMLUni::fgSAX2CoreNameSpaces       , true);
         parser->setFeature(XMLUni::fgXercesSchema             , true);
         parser->setFeature(XMLUni::fgXercesSchemaFullChecking , true);
         parser->setFeature(XMLUni::fgSAX2CoreNameSpacePrefixes, false);
         parser->setFeature(XMLUni::fgSAX2CoreValidation       , true);
         parser->setFeature(XMLUni::fgXercesDynamic            , true);
         parser->setProperty(XMLUni::fgXercesScannerName, (void *)XMLUni::fgSGXMLScanner);
         
         ParserErrorReporter handler;    
         parser->setErrorHandler(&handler);

         MemEntityResolver entityResolver(0, parser->getEntityResolver());
         parser->setEntityResolver(&entityResolver);

         parser->loadGrammar(source, Grammar::SchemaGrammarType, true);

          if (handler.errorsOccurred()) {
              delete parser;
              parser = 0;
              throw handler.errors();
          }
   bool lax;
   bool& fisk = lax;
         mXSModel = mGrammarPool->getXSModel(fisk);
         if (!mXSModel) {    
              e << "No XSModel in XSDContent::parseSchmea()";
              errOcc = true;
         }
    }
    catch (const OutOfMemoryException&) {
         e << "OutOfMemoryException when parsing schema";
         errOcc = true;
    }
    catch (const XMLException& exc) {
         e << "Error when parsing schema. Exception message is: '" << StrX(exc.getMessage()).str() << "'";
         errOcc = true;
    }
    catch (vector<Error>& es) {
         throw es;
    }
    catch (...) {
         e << "Unexpected exception when parsing schema";
         errOcc = true;
    }
    if (parser) {
         delete parser;
    }

    if (errOcc) {
         throw e;
    }
}


/**
 * \brief Creates a Type object that corresponds to the xml schema
 * type with the specified name.
 *
 * \param typeName The name of the Type.
 * \return The Type with the specified name.
 */
const Type& XSDContent::getType(const std::string& typeName)
{
     return getType(typeName, mNamespace);
}

/**
 * \brief Creates a Type object that corresponds to the xml schema
 * type with the specified name and namespace.
 *
 * \param typeName The name of the Type.
 * \param nameSpace The namespace of the Type.
 * \return The Type with the specified name and namespace.
 */
const Type& XSDContent::getType(const std::string& typeName, const std::string& nameSpace)
{
     Type* ret = 0;
     std::map<std::string, Type*>::iterator it = mResolvedTypes.find(nameSpace + ":" + typeName);
     if (it == mResolvedTypes.end()) {
          XStr t(typeName);
          XStr ns(nameSpace);
          XSTypeDefinition* td = mXSModel->getTypeDefinition(t.str(), ns.str());
          if (td) {
               ret = new TypeDefinition(*this, *td);
               mResolvedTypes[nameSpace + ":" + ret->getName()] = ret;
          }
          else {
               Error e;
               e << "Can't find type '" << typeName << "' in namespace '" << nameSpace << "'";
               throw e;
          }
     }
     else {
          ret = it->second;
     }
     return *ret;
}
