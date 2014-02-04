// System
#include <iomanip>
#include <iostream>

// Own
#include "Declaration.h"
#include "StrX.h"
#include "Type.h"
#include "XSDContent.h"

// Xerces
#include <xercesc/framework/psvi/XSElementDeclaration.hpp>
#include <xercesc/framework/psvi/XSParticle.hpp>
#include <xercesc/framework/psvi/XSTypeDefinition.hpp>


using namespace std;


/**
 * \brief Creates a Declaration.
 *
 * \param particle The XSParticle to create the Declaration from.
 * \param xsdcontent The XSDContent to create the Declaration from.
 */
Declaration::Declaration(XSParticle& particle, XSDContent& xsdcontent)
{
     init(particle, &xsdcontent, 0);
}

/**
 * \brief Creates a Declaration of the specified Type. eccesary in
 * order to avoid infinite recursion for recursively defined types.
 *
 * \param particle The XSParticle to create the Declaration from.
 * \param type The Type of the Declaration.
 */
Declaration::Declaration(XSParticle& particle, const Type* type)
{
     init(particle, 0, type);
}

/**
 * \brief Helper function for initializing the Declaration.
 *
 * \param particle The XSParticle to create the Declaration from.
 * \param xsdcontent The XSDContent to create the Declaration from.
 * \param type The Type of the Declaration.
 */
void Declaration::init(XSParticle& particle, XSDContent* xsdcontent, const Type* type)
{
     mMinOccurs = particle.getMinOccurs(); 
     mMaxOccurs = particle.getMaxOccurs(); 
     mUnbounded = particle.getMaxOccursUnbounded();
     XSElementDeclaration* dec = particle.getElementTerm();
     if (dec) {
          StrX name(dec->getName());
          mName = name.str();
          if (type) {
               mType = type;
          }
          else if (xsdcontent) {
               mType = &xsdcontent->getType(StrX(dec->getTypeDefinition()->getName()).str(),
                                            StrX(dec->getTypeDefinition()->getNamespace()).str());
          }
          else {
               Error e("Neither Type nor XSDContent in Declaration constructor.");
               throw e;
          }
     }
     else {
          Error e("No XSElementDeclaration in Declaration constructor.");
          throw e;
     }
}

/**
 * \brief For debugging purposes.
 *
 * \param o The stream to write to.
 * \param d The Declaration to print.
 * \return The provided ostream with the Decaration written to it.
 */
ostream& operator << (std::ostream& o, const Declaration& d)
{
     o << "  " << left << setw(25) << d.getName() << setw(20) << d.getType().getName() << "\t";
     o << "minOccurs: " << d.minOccurs() << "\t";
     o << "maxOccurs: " << d.maxOccurs() << "\t";
     o << "unbounded: " << (d.unbounded() ? "true" : "false") << endl;
     return o;
}
