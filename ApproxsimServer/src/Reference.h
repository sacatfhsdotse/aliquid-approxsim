#ifndef _APPROXSIM_REFERENCE_H
#define _APPROXSIM_REFERENCE_H

// System
#include <iosfwd>
#include <map>
#include <string>
#include <cstdlib>

// Xerces-c
#include <xercesc/util/XercesDefs.hpp>

// Own
#include "debugheader.h"

// Forward Declarations
namespace XERCES_CPP_NAMESPACE {
     class DOMElement;
}


XERCES_CPP_NAMESPACE_USE


/**
 * \brief A Reference is used to point out an object somewhere in the
 * Approxsim object hierarchy.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/05/24 12:32:11 $
 */
class Reference {
private:
     /// The global root Reference.
     static const Reference sRoot;

     /// The global root Reference.
     static const Reference sNull;

     /// The name of this Reference.
     const std::string mName;

     /// The Reference making up the scope of this Reference.
     const Reference* mScope;

     /**
      * \brief Maps the name of a child of this Reference to the
      * actual Reference of that child.
      */
     mutable std::map<const std::string, const Reference*> mChildren;

     /**
      * \brief Creates a reference with the specified name. Only used
      * when creating the root Reference.
      *
      * \param name The name of the Reference to be created.
      */
     Reference(const std::string &name) : mName(name), mScope(0) {}

     /**
      * \brief Creates a reference with the specified name in the
      * specified scope
      *
      * \param scope The scope in which to create the Reference.
      * \param name The name of the Reference to be created.
      */
     Reference(const Reference& scope, const std::string& name)
          : mName(name), mScope(&scope) {}
     Reference(const Reference& ref);
     ~Reference();
     
public:
     /**
      * \brief Accessor for the name of this Reference.
      *
      * \return The name of this Reference.
      */
     const std::string& name() const { return mName; }

     /**
      * \brief Accessor for the scope of this Reference.
      *
      * \return The scope of this Reference.
      */
     const Reference* scope() const { return mScope; }

     /**
      * \brief Gets the root Reference.
      *
      * \return The root Reference.
      */
     static const Reference& root() { return sRoot; }

     /**
      * \brief Gets the null Reference.
      *
      * \return The null Reference.
      */
     static const Reference& nullRef() { return sNull; }

     static void cleanUp();

     static const Reference& get(const DOMElement* n);
     static const Reference& get(const Reference& scope, const std::string& name);

     /**
      * \brief Less than operator.
      *
      * A Reference r1 is less than another reference r2 if:
      *    <p>
      *    r1's scope is less than r2's scope. A null scope is less
      *    than any other scope.
      *    <p>
      *    or
      *    <p>
      *    r1 and r2 have the same scope and r1's name is
      *    lexicographically less than r2's
      *
      * \param r The Reference to compare to.
      * \return True if this reference is less than r.
      */
     bool operator < (const Reference& r) const {
          // Equal scopes => return true of this name is less than r's name.
          if (mScope == r.mScope) {
               return (mName < r.mName);
          }
          // Basis case - one of the scopes is null, e.g. either this
          // or r is the root Reference and thus the 'smallest' Reference.
          else if (mScope == 0 || r.mScope == 0) {
               return (mScope == 0);
          }
          // Else just compare this Reference to r's scope Reference.
          else {
               return (*mScope < *r.mScope);
          }
     }

     /**
      * \brief Equality operator.
      *
      * Two References are equal if they are located on the same
      * memory address i.e. they are the same object.
      *
      * \param r The Reference to compare to.
      * \return True if this reference is equal to r.
      */
     bool operator == (const Reference& r) const { return (this == &r); }

     /**
      * \brief Inequality operator.
      *
      * \param r The Reference to compare to.
      * \return True if this reference is not equal to r.
      */
     bool operator != (const Reference& r) const { return !(*this == r); }

     /**
      * \brief Produces an XML representation of this Reference
      * according to the xml schemas.
      *
      * \param o The ostream to print to.
      * \return The ostream with the XML representation written to it.
      */
     std::ostream& toXML(std::ostream& o) const { return toXML(o, ""); }
     std::ostream& toXML(std::ostream& o, std::string indent) const;
     
     /// For debugging purposes.
     friend std::ostream& operator << (std::ostream& o, const Reference& r);
};

/**
 * \brief Function object used to compare const Reference
 * pointers. Needed by std::map.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/05/24 12:32:11 $
 */
struct lessReferenceP {
     /**
      * \brief Compares the References pointed to by r1 and r2.
      *
      * \param r1 A pointer to a Reference.
      * \param r2 A pointer to a Reference.
      * \return true if the Reference pointed to by r1 is less than
      * the Reference pointed to by r2.
      */
     bool operator()(const Reference *const r1, const Reference *const r2) const {
          if (!r1 || !r2) {
               exit(1);
          }
          else {
               return (*r1 < *r2);
          }
     }
};

/**
 * \brief Function object used to create a hashcode for a
 * Reference. Needed by hash_map.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/05/24 12:32:11 $
 */
struct hashReferenceP {
     /**
      * \brief Produces a hashcode for the Reference pointed to by
      * key. Since there may onbly be one Reference object for each
      * reference the address will do.
      *
      * \param key Pointer to the Reference for which to create a
      * hashcode.
      * \return A hashcode for the specified Reference.
      */
     size_t operator()(const Reference *const key) const {
          return reinterpret_cast<size_t>(key);
     }
};

#endif   // _APPROXSIM_REFERENCE_H
