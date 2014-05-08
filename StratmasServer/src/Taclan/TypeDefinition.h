#ifndef APPROXSIM_TYPEDEFINITION
#define APPROXSIM_TYPEDEFINITION

// Own
#include "Type.h"

// Forward Declarations
namespace XERCES_CPP_NAMESPACE {
     class XSAttributeUse;
     class XSComplexTypeDefinition;
     class XSModelGroup;
     class XSParticle;
     class XSSimpleTypeDefinition;
     class XSTypeDefinition;
}

/**
 * \brief The TypeDefinition class is the implementation of the Type
 * interface.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/05/24 12:32:11 $
 */
class TypeDefinition : public Type {
private:
     /// The XSTypeDefinition that this TypeDefinition was creted from.
     XSTypeDefinition& mXSTypeDefinition;

     /// The name of this Type.
     std::string mName;

     /// The namespace of this Type.
     std::string mNamespace;

     /// Indicates if this Type is abstract.
     bool mAbstract;

     void processSimpleTypeDefinition(XSSimpleTypeDefinition& xsSimpleTypeDef);
     void processComplexTypeDefinition(XSComplexTypeDefinition& xsComplexTypeDef);
     void processParticle(XSParticle& xsParticle);
     void processParticles(XSModelGroup& xsModelGroup);
     void processAttributeUse(XSAttributeUse& xsAttributeUse);
public:
     TypeDefinition(XSDContent& content, XSTypeDefinition& xsTypeDef);
     inline ~TypeDefinition() {}
     inline const std::string& getName() const { return mName; }
     inline const std::string& getNamespace() const { return mNamespace; }
     inline bool abstract() const { return mAbstract; }
     bool canSubstitute(const Type& type) const;
};

#endif   // APPROXSIM_TYPEDEFINITION
