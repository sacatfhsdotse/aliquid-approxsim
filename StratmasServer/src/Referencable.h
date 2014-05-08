#ifndef APPROXSIM_REFERENCABLE_H
#define APPROXSIM_REFERENCABLE_H

// Xerces
#include <xercesc/util/XercesDefs.hpp>

// Forward Declarations
class Reference;

namespace XERCES_CPP_NAMESPACE {
     class DOMElement;
}


XERCES_CPP_NAMESPACE_USE


/**
 * \brief Superclass for all objects that could be pointed out by a
 * Reference.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/05/24 12:32:10 $
 */
class Referencable {
protected:
     /// Pointer to the Reference to this Object.
     const Reference* mReference;

     Referencable();
     Referencable(const Reference& scope, const DOMElement* n);

     /**
      * \brief Creates a Referencable with Reference ref.
      *
      * \param ref The Reference to this Referenceable.
      */
     Referencable(const Reference& ref) : mReference(&ref) {}

     /**
      * \brief Copy constructor.
      *
      * \param refable The Referencable to copy.
      */
     Referencable(const Referencable& refable) : mReference(&refable.ref()) {}


public:
     /// Destructor
     virtual ~Referencable() {}

     /**
      * \brief Returns the Reference to this Referenceable.
      *
      * \return ref The Reference to this Referenceable.
      */
     const Reference& ref() const { return *mReference; }
};

#endif   // APPROXSIM_REFERENCABLE_H
