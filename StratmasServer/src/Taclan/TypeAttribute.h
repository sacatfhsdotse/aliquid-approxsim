#ifndef APPROXSIM_TYPEATTRIBUTE
#define APPROXSIM_TYPEATTRIBUTE

// System
#include <iosfwd>
#include <string>

// Own

// Xerces
#include <xercesc/util/XercesDefs.hpp>


// Forward Declarations
namespace XERCES_CPP_NAMESPACE {
     class XSAttributeUse;
}


XERCES_CPP_NAMESPACE_USE


/**
 * \brief The TypeAttribute class represents an attribute of a type in
 * the Approxsim xml schema.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/05/24 12:32:11 $
 */
class TypeAttribute {
private:
     /// The name of the attribute.
     std::string mName;

     /// The constraint type as specified by Xerces.
     int mConstraintType;

     /// Required flag.
     bool mRequired;

     /// Fixed or default value if any.
     std::string mConstraintValue;
public:
     TypeAttribute(XSAttributeUse& xsAttrUse);

     /**
      * \brief Accessor for the name.
      *
      * \return The name.
      */
     const std::string& getName() const { return mName; }

     /**
      * \brief Accessor for the required flag.
      *
      * \return The state of the required flag.
      */
     bool required() const { return mRequired; }

     /**
      * \brief Accessor for the constraint type.
      *
      * \return The  constraint type.
      */
     int constraintType() const { return mConstraintType; }

     /**
      * \brief Accessor for the constraint value.
      *
      * \return The constraint value.
      */
     const std::string& constraintValue() const { return mConstraintValue; }

     friend std::ostream& operator << (std::ostream& o, const TypeAttribute& t);
};

#endif   // APPROXSIM_TYPEATTRIBUTE
