// System
#include <iostream>
#include <cstdlib>

// Own
#include "debugheader.h"
#include "Declaration.h"
#include "TypeAttribute.h"
#include "TypeDefinition.h"
#include "StrX.h"

// Xerces
#include <xercesc/framework/psvi/XSAttributeUse.hpp>
#include <xercesc/framework/psvi/XSComplexTypeDefinition.hpp>
#include <xercesc/framework/psvi/XSElementDeclaration.hpp>
#include <xercesc/framework/psvi/XSModelGroup.hpp>
#include <xercesc/framework/psvi/XSParticle.hpp>
#include <xercesc/framework/psvi/XSSimpleTypeDefinition.hpp>


using namespace std;


/**
 * \brief Creates a TypeDefinition.
 *
 * \param content The XSDContent to create this TypeDefinition from.
 * \param xsTypeDef The XSTypeDefinition to create this TypeDefinition
 * from.
 */
TypeDefinition::TypeDefinition(XSDContent& content, XSTypeDefinition& xsTypeDef)
     : Type(content), mXSTypeDefinition(xsTypeDef), mAbstract(false)
{
     mName = StrX(mXSTypeDefinition.getName()).str();
     mNamespace = StrX(mXSTypeDefinition.getNamespace()).str();
     if (mXSTypeDefinition.getTypeCategory() == XSTypeDefinition::SIMPLE_TYPE) {
          processSimpleTypeDefinition(*reinterpret_cast<XSSimpleTypeDefinition*>(&mXSTypeDefinition));
     }
     else {
          processComplexTypeDefinition(*reinterpret_cast<XSComplexTypeDefinition*>(&mXSTypeDefinition));
     }
}

/**
 * \brief Handles simple type definitions.
 *
 * \param xsSimpleTypeDef The XSSimpleTypeDefinition to handle.
 */
void TypeDefinition::processSimpleTypeDefinition(XSSimpleTypeDefinition& xsSimpleTypeDef)
{
     int facets = xsSimpleTypeDef.getDefinedFacets();
     if (facets) {
          //cout << "Facets:\n";
          
          if (facets & XSSimpleTypeDefinition::FACET_LENGTH) {
               //cout << "\tLength:\t\t" << StrX(xsSimpleTypeDef.getLexicalFacetValue(XSSimpleTypeDefinition::FACET_LENGTH)) << endl;
          }
          if (facets & XSSimpleTypeDefinition::FACET_MINLENGTH) {
               //cout << "\tMinLength:\t" << StrX(xsSimpleTypeDef.getLexicalFacetValue(XSSimpleTypeDefinition::FACET_MINLENGTH)) << endl;
          }
          if (facets & XSSimpleTypeDefinition::FACET_MAXLENGTH) {
               //cout << "\tMaxLength:\t" << StrX(xsSimpleTypeDef.getLexicalFacetValue(XSSimpleTypeDefinition::FACET_MAXLENGTH)) << endl;
          }
          if (facets & XSSimpleTypeDefinition::FACET_PATTERN) {
               StringList *lexicalPatterns = xsSimpleTypeDef.getLexicalPattern();
               if (lexicalPatterns && lexicalPatterns->size()) {
                    //cout << "\tPattern:\t\t";
                    for (unsigned i = 0; i < lexicalPatterns->size(); i++) {                    
                         //cout << StrX(lexicalPatterns->elementAt(i));
                    }
                    //cout << endl;
               }
          }
          if (facets & XSSimpleTypeDefinition::FACET_WHITESPACE) {
               //cout << "\tWhitespace:\t\t" << StrX(xsSimpleTypeDef.getLexicalFacetValue(XSSimpleTypeDefinition::FACET_WHITESPACE)) << endl;
          }
          if (facets & XSSimpleTypeDefinition::FACET_MAXINCLUSIVE) {
               //cout << "\tMaxInclusive:\t" << StrX(xsSimpleTypeDef.getLexicalFacetValue(XSSimpleTypeDefinition::FACET_MAXINCLUSIVE)) << endl;
          }
          if (facets & XSSimpleTypeDefinition::FACET_MAXEXCLUSIVE) {
               //cout << "\tMaxExclusive:\t" << StrX(xsSimpleTypeDef.getLexicalFacetValue(XSSimpleTypeDefinition::FACET_MAXEXCLUSIVE)) << endl;      
          }
          if (facets & XSSimpleTypeDefinition::FACET_MINEXCLUSIVE) {
               //cout << "\tMinExclusive:\t" << StrX(xsSimpleTypeDef.getLexicalFacetValue(XSSimpleTypeDefinition::FACET_MINEXCLUSIVE)) << endl;
          }
          if (facets & XSSimpleTypeDefinition::FACET_MININCLUSIVE) {
               //cout << "\tMinInclusive:\t" << StrX(xsSimpleTypeDef.getLexicalFacetValue(XSSimpleTypeDefinition::FACET_MININCLUSIVE)) << endl;
          }
          if (facets & XSSimpleTypeDefinition::FACET_TOTALDIGITS) {
               //cout << "\tTotalDigits:\t" << StrX(xsSimpleTypeDef.getLexicalFacetValue(XSSimpleTypeDefinition::FACET_TOTALDIGITS)) << endl;
          }
          if (facets & XSSimpleTypeDefinition::FACET_FRACTIONDIGITS) {
               //cout << "\tFractionDigits:\t" << StrX(xsSimpleTypeDef.getLexicalFacetValue(XSSimpleTypeDefinition::FACET_FRACTIONDIGITS)) << endl;
          }
          if (facets & XSSimpleTypeDefinition::FACET_ENUMERATION) {
               StringList *lexicalEnums = xsSimpleTypeDef.getLexicalEnumeration();
               if (lexicalEnums && lexicalEnums->size()) {
                    //cout << "\tEnumeration:\n";
                    for (unsigned i = 0; i < lexicalEnums->size(); i++) {
                         //cout << "\t\t\t" << StrX(lexicalEnums->elementAt(i)) << "\n";
                    }
                    //cout << endl;
               }
          }
     }
}

/**
 * \brief Handles complex type definitions.
 *
 * \param xsComplexTypeDef The XSComplexTypeDefinition to handle.
 */
void TypeDefinition::processComplexTypeDefinition(XSComplexTypeDefinition& xsComplexTypeDef)
{
     mAbstract = xsComplexTypeDef.getAbstract();

     XSComplexTypeDefinition::CONTENT_TYPE contentType = xsComplexTypeDef.getContentType();

     if (contentType == XSComplexTypeDefinition::CONTENTTYPE_ELEMENT ||
         contentType == XSComplexTypeDefinition::CONTENTTYPE_MIXED) {
          processParticle(*xsComplexTypeDef.getParticle());
     }
     XSAttributeUseList* attrList = xsComplexTypeDef.getAttributeUses();
     if (attrList) {
          for (unsigned int i = 0; i < attrList->size(); i++) {
               processAttributeUse(*attrList->elementAt(i));
          }
     }
}


/**
 * \brief Handles a single XSParticle.
 *
 * \param xsParticle The XSParticle to handle.
 */
void TypeDefinition::processParticle(XSParticle& xsParticle)
{
     XSParticle::TERM_TYPE termType = xsParticle.getTermType();
     if (termType == XSParticle::TERM_ELEMENT) {
          XSElementDeclaration* dec = xsParticle.getElementTerm();
          string nameSpace = StrX(mXSTypeDefinition.getNamespace()).str();
          if (getName() == StrX(dec->getTypeDefinition()->getName()     ).str() &&
              nameSpace == StrX(dec->getTypeDefinition()->getNamespace()).str()) {
               Declaration* dec = new Declaration(xsParticle, this);
               appendSubElement(*dec);
          }
          else {
               Declaration *dec = new Declaration(xsParticle, mXSDContent);
               appendSubElement(*dec);
          }
     }
     else if (termType == XSParticle::TERM_MODELGROUP) {
          processParticles(*xsParticle.getModelGroupTerm());
     }
     else if (termType == XSParticle::TERM_WILDCARD) {
          approxsimDebug("* (wildcard) NOT IMPLEMENTED");
     }
}

/**
 * \brief Handles a group of XSParticles.
 *
 * \param xsModelGroup The XSModelGroup to handle.
 */
void TypeDefinition::processParticles(XSModelGroup& xsModelGroup)
{
     // Should we care about gropus contra sequences?
//      switch (xsModelGroup.getCompositor()) {
//      case XSModelGroup::COMPOSITOR_SEQUENCE:
//           break;
//      case XSModelGroup::COMPOSITOR_CHOICE:
//           break;
//      case XSModelGroup::COMPOSITOR_ALL:
//           break;
//      }    

     XSParticleList* xsParticleList = xsModelGroup.getParticles();
     for (unsigned i = 0; i < xsParticleList->size(); i++) {
          processParticle(*xsParticleList->elementAt(i));
     }
}

/**
 * \brief Handles an XSAttributeuse.
 *
 * \param xsAttributeUse The XSAttributeuse. to handle.
 */
void TypeDefinition::processAttributeUse(XSAttributeUse& xsAttributeUse)
{
     TypeAttribute* ta = new TypeAttribute(xsAttributeUse);
     appendAttribute(*ta);
}

bool TypeDefinition::canSubstitute(const Type& type) const
{
     // Have to do this the hard way since the cygwin xerces 2.5
     // library has a bug. The bug is that anyType is ancestor of
     // itself and thus causes the derivedFromType function to hang.
     if (type.getName() == "anyType") {
          return true;
     }
     else {
          XSTypeDefinition* td = &mXSTypeDefinition;
          while (!(StrX(td->getName()) == "anyType")) {
               if (type.getName() == StrX(td->getName()).str()) {
                    return true;
               }
               td = td->getBaseType();
          }
     }
     return false;

     // The following line is sufficient with a non-buggy xerces.
//     return mXSTypeDefinition.derivedFromType(&dynamic_cast<const TypeDefinition*>(&type)->mXSTypeDefinition);
}
