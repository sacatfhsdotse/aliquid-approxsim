#ifndef APPROXSIM_PARSERERRORREPORTER_H
#define APPROXSIM_PARSERERRORREPORTER_H

// System
#include <vector>

// Xerces-c
#include <xercesc/sax/ErrorHandler.hpp>
#include <xercesc/sax/SAXParseException.hpp>
#include <xercesc/util/XercesDefs.hpp>

// Own
#include "Error.h"
#include "StrX.h"


using namespace XERCES_CPP_NAMESPACE;


/**
 * \brief Error reporter for the DOMParser.
 *
 * \author   Per Alexius
 * \date     $Date: 2005/12/08 11:26:52 $
 */
class ParserErrorReporter : public ErrorHandler {
private:
     /// A vector containing the errors.
     std::vector<Error> mErrors;

public:
     /**
      * \brief Checks if any errors occurred.
      *
      * \return True if any errors occurred, false otherwise.
      */
     inline bool errorsOccurred() const { return !mErrors.empty(); }

     /**
      * \brief Handles warning exceptions.
      *
      * \param toCatch The exception to handle.
      */
     inline void warning(const SAXParseException& toCatch) {}
     inline void error(const SAXParseException& toCatch);
     inline void fatalError(const SAXParseException& toCatch);

     /**
      * \brief Accessor for the vector containing the errors.
      *
      * \return The vector containing the errors.
      */
     inline std::vector<Error> errors() const { return mErrors; }

     /**
      * \brief Resets the mErrorsOccurred flag.
      */
     inline void resetErrors() { mErrors.clear(); }
};

/**
 * \brief Handles error exceptions.
 *
 * \param toCatch The exception to handle.
 */
inline void ParserErrorReporter::error(const SAXParseException& toCatch)
{
     Error e;
     e << "Error at file \"" << StrX(toCatch.getSystemId()) << "\", line " << toCatch.getLineNumber()
       << ", column " << toCatch.getColumnNumber() << "\n   Message: " << StrX(toCatch.getMessage());
     mErrors.push_back(e);
}

/**
 * \brief Handles fatalError exceptions.
 *
 * \param toCatch The exception to handle.
 */
inline void ParserErrorReporter::fatalError(const SAXParseException& toCatch)
{
     Error e;
     e << "Fatal Error at file \"" << StrX(toCatch.getSystemId()) << "\", line " << toCatch.getLineNumber()
       << ", column " << toCatch.getColumnNumber() << "\n   Message: " << StrX(toCatch.getMessage());
     mErrors.push_back(e);
}


#endif   // APPROXSIM_PARSERERRORREPORTER_H
