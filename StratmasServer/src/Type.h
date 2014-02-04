#ifndef STRATMAS_TYPE
#define STRATMAS_TYPE

// System
#include <iosfwd>
#include <map>
#include <string>
#include <vector>

// Xerces
#include <xercesc/util/XercesDefs.hpp>

// Forward Declarations
class Declaration;
class TypeAttribute;
class XSDContent;


XERCES_CPP_NAMESPACE_USE


/**
 * \brief The Type class represents a Type in the Stratmas xml schema.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/02 17:06:56 $
 */
class Type {
private:
     /**
      * \brief The children of this type. The order of the elements in
      * this vector is the same as in the sequence in the Stratmas XML
      * schema.
      */
     std::vector<const Declaration*> mSubElements;

     /// Map that maps child name to the child itself.
     std::map<std::string, const Declaration*> mSubElementsMap;

     /// Vector containing the Attirbutes of this Type.
     std::vector<const TypeAttribute*> mAttributes;

     /// Map that maps attribute name to the attribute itself.
     std::map<std::string, const TypeAttribute*> mAttributesMap;
protected:
     /// The XSDContent that this Type was created by.
     XSDContent& mXSDContent;

     void appendSubElement(Declaration& dec);
     void appendAttribute(TypeAttribute& ta);
public:
     /**
      * \brief Constructor that sets the creator XSDContent.
      */
     Type(XSDContent& content) : mXSDContent(content) {}
     virtual ~Type();

     /**
      * \brief Accessor for the name of this Type.
      *
      * \return The name of this Type.
      */
     virtual const std::string& getName() const = 0;

     /**
      * \brief Accessor for the namespace of this Type.
      *
      * \return The namespace of this Type.
      */
     virtual const std::string& getNamespace() const = 0;

     /**
      * \brief Accessor for the abstract flag.
      *
      * \return True if this Type is abstract, false otherwise.
      */
     virtual bool abstract() const = 0;

     const Declaration* getSubElement(const std::string& name) const;

     /**
      * \brief Accessor for the subelements vector.
      *
      * \return The subelements vector.
      */
     const std::vector<const Declaration*>& subElements() const { return mSubElements; }

     /**
      * \brief Checks if this Type inherits from the provided Type.
      *
      * \param type The Type to check inheritance from.
      * \return True if this Type inherits from the provided Type.
      */
     virtual bool canSubstitute(const Type& type) const = 0;
     bool canSubstitute(const std::string& typeName) const;
     bool canSubstitute(const std::string& typeName, const std::string& nameSpace) const;

     /**
      * \brief Equality operator. Two types are equal if they have
      * identical name and namespace.
      *
      * \param t The Type to check for equality.
      * \return True if this Type and the provided Type are equal.
      */
     bool operator == (const Type& t) const { return getName() == t.getName() && getNamespace() == t.getNamespace(); }

     friend std::ostream& operator << (std::ostream& o, const Type& t);
};


/**
 * \brief Function object used to compare const Type pointers. Needed
 * by std::map.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/02 17:06:56 $
 */
struct lessTypeP {
     /**
      * \brief Compares the Types pointed to by t1 and t2.
      *
      * \param t1 A pointer to a Type.
      * \param t2 A pointer to a Type.
      * \return true if the Type pointed to by t1 is less than
      * the Type pointed to by t2.
      */
     bool operator()(const Type *const t1, const Type *const t2) const {
          if (!t1 || !t2) {
               exit(1);
          }
          else {
               return (t1 < t2);
          }
     }
};


#endif   // STRATMAS_TYPE
