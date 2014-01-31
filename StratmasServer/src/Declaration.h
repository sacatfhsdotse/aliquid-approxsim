#ifndef STRATMAS_DECLARATION
#define STRATMAS_DECLARATION

// System
#include <iosfwd>
#include <string>

// Xerces
#include <xercesc/util/XercesDefs.hpp>

// Forward Declarations
class Type;
class XSDContent;

namespace XERCES_CPP_NAMESPACE {
     class XSParticle;
}


XERCES_CPP_NAMESPACE_USE


/**
 * \brief This class represents an element declaration in the xml
 * schema.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/05/24 12:32:10 $
 */
class Declaration {
private:
     int mMinOccurs;      ///< The minOccurs attribute.
     int mMaxOccurs;      ///< The maxOccurs attribute.
     bool mUnbounded;     ///< Flag indicating unbounded or not.
     std::string mName;   ///< The name of the Declaration.
     const Type* mType;   ///< The Type of the Declaration.

     void init(XSParticle& particle, XSDContent* xsdcontent, const Type* type);

public:
     Declaration(XSParticle& particle, XSDContent& xsdcontent);
     Declaration(XSParticle& particle, const Type* type);

     /**
      * \brief Accessor for the minOccurs attribute.
      *
      * \return The minOccurs attribute.
      */
     int minOccurs() const { return mMinOccurs; }

     /**
      * \brief Accessor for the axOccurs attribute.
      *
      * \return The maxOccurs attribute.
      */
     int maxOccurs() const { return mMaxOccurs; }

     /**
      * \brief Accessor for the unbounded flag.
      *
      * \return The state of the unbounded flag.
      */
     int unbounded() const { return mUnbounded; }

     /**
      * \brief Checks if this Declaration refers to a list i.e. if
      * minOccurs and macOccurs differ more than 1.
      *
      * \return True if this Delcaration refers to a list, false
      * otherwise.
      */
     bool isList() const { return (unbounded() || maxOccurs() - minOccurs() > 1); }

     /**
      * \brief Checks if this Declaration refers to an optional
      * element, i.e. if minOccurs = 0 and maxOccurs = 1.
      *
      * \return True if this Delcaration refers to an optional
      * element, false otherwise.
      */
     bool isOptional() const { return (minOccurs() == 0 && maxOccurs() == 1); }

     /**
      * \brief Checks if this Declaration refers to a singular
      * element, i.e. if minOccurs = 1 and maxOccurs = 1.
      *
      * \return True if this Delcaration refers to a singular element,
      * false otherwise.
      */
     bool isSingular() const { return (minOccurs() == 1 && maxOccurs() == 1); }

     /**
      * \brief Accessor for the name.
      *
      * \return The name of this Declaration.
      */
     const std::string& getName() const { return mName; }

     /**
      * \brief Accessor for the Type.
      *
      * \return The Type of this Declaration.
      */
     const Type& getType() const { return *mType; }
     friend std::ostream& operator << (std::ostream& o, const Declaration& d);
};

#endif   // STRATMAS_DECLARATION
