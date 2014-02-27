// System
#include <iostream>

// Own
#include "StrX.h"
#include "TypeAttribute.h"

// Xerces
#include <xercesc/framework/psvi/XSAttributeDeclaration.hpp>
#include <xercesc/framework/psvi/XSAttributeUse.hpp>


using namespace std;


/**
 * \brief Constructs a TypeAttribute from the provided XSAttributeUse.
 *
 * \param xsAttrUse The XSAttributeUse to use for construction.
 */
TypeAttribute::TypeAttribute(XSAttributeUse& xsAttrUse)
{
     XSAttributeDeclaration& xsAttrDecl = *xsAttrUse.getAttrDeclaration();

     mName            = StrX(xsAttrDecl.getName()).str();
     mConstraintType  = xsAttrUse.getConstraintType();
     mRequired        = xsAttrUse.getRequired();
     const XMLCh* cValue = xsAttrUse.getConstraintValue();
     mConstraintValue = (cValue ? StrX(cValue).str() : "");
}

/**
 * \brief For debugging purposes.
 *
 * \param o The stream to write to.
 * \param t The TypeAttribute to print.
 * \return The provided ostream with the TypeAttribute written to it.
 */
ostream& operator << (ostream& o, const TypeAttribute& t)
{
     o << t.getName() << (t.required() ? " (required)" : "") << endl;
     
//      switch (t.constraintType()) {
//      case XSConstants::VALUE_CONSTRAINT_NONE:
//           break;
//      case XSConstants::VALUE_CONSTRAINT_DEFAULT:
//           o << "  Default constraint - '" << t.constraintValue() << "'" << endl;
//           break;
//      case XSConstants::VALUE_CONSTRAINT_FIXED:
//           o << "  Fixed constraint - '" << t.constraintValue() << "'" << endl;
//           break;
//      }
     return o;
}
