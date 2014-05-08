#ifndef APPROXSIM_TYPEFACTORY
#define APPROXSIM_TYPEFACTORY

// System
#include <string>

// Forward Declarations
class Type;
class XSDContent;


/**
 * \brief Factory for creating Types. The XSDContent Ccaches already
 * created Types.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/02 17:06:57 $
 */
class TypeFactory {
private:
     /// The XSDContent to use for creating Types.
     static XSDContent* sXSDContent;
public:
     static const Type& getType(const std::string& typeName);
     static const Type& getType(const std::string& typeName, const std::string& nameSpace);
};

#endif   // APPROXSIM_TYPEFACTORY
