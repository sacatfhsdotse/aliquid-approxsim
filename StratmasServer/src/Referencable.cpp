// Xerces-c
#include <xercesc/dom/DOMElement.hpp>

// Own
#include "Referencable.h"
#include "Reference.h"
#include "StrX.h"
#include "XMLHelper.h"

/**
 * \brief Creates a Referencable refering to the null reference.
 *
 */
Referencable::Referencable() : mReference(&Reference::nullRef())
{
}

/**
 * \brief Creates a Referencable in the specified scope, extracting
 * the name from the provided DOMElement.
 *
 * \param scope Reference to the scope of this Referenceable.
 * \param n Pointer to the DOMElement from which to extract the name
 * of this Referencable.
 */
Referencable::Referencable(const Reference &scope, const DOMElement *n)
{
     std::string id = XMLHelper::getStringAttribute(*n, "identifier");
     if (id == "") {
          id = StrX(n->getTagName()).str();
     }
     mReference = &Reference::get(scope, id);
}
