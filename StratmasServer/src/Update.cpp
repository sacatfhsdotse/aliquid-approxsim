// Xerces
#include <xercesc/dom/DOMElement.hpp>

// Own
#include "DataObjectImpl.h"
#include "Mapper.h"
#include "Update.h"
#include "XMLHelper.h"


/**
 * \brief The type name of the updates in the same order as the eType
 * enumeration.
 */
static const char* types[] = {
     "ServerUpdateAdd",
     "ServerUpdateRemove",
     "ServerUpdateReplace",
     "ServerUpdateModify"
};


/**
 * \brief Creates an Update from the provided DOMElement.
 *
 * \param n The DOMElement to create this Update from.
 * \param initiator The id of the initiator of the Update.
 */
Update::Update(const DOMElement& n, int64_t initiator) : mInitiator(initiator), mObject(0)
{
     mReference = &Reference::get(XMLHelper::getFirstChildByTag(n, "reference"));

     string type = XMLHelper::getStringAttribute(n, "xsi:type");
     if (type == "sp:ServerUpdateAdd") {
          mType = eAdd;
          mTarget = findReferenceToClosestComplexParent(*mReference);
          mObject = DataObjectFactory::createDataObject(*mReference->scope(), XMLHelper::getFirstChildByTag(n, "identifiable"));
     }
     else if (type == "sp:ServerUpdateRemove") {
          mType = eRemove;
          mTarget = findReferenceToClosestComplexParent(*mReference);
     }
     else if (type == "sp:ServerUpdateReplace") {
          mType = eReplace;
          mTarget = findReferenceToClosestComplexParent(*mReference);
          mObject = DataObjectFactory::createDataObject(*mReference->scope(), XMLHelper::getFirstChildByTag(n, "newObject"));
     }
     else if (type == "sp:ServerUpdateModify") {
          mType = eModify;
          mTarget = findReferenceToClosestComplexParent(*mReference);
          mObject = DataObjectFactory::createDataObject(*mReference->scope(), XMLHelper::getFirstChildByTag(n, "newValue"));
     }
     else {
          Error e;
          e << "Unknown update type '" << type << "'";
          throw e;
     }
}

/**
 * \brief The Update is responsible for deallocation of the DataObject
 * if and only if it is of type 'eModify'.
 */
Update::~Update()
{
     if (mType == eModify && mObject) {
          delete mObject;
     }
}

/**
 * \brief Gets the type of this Update as a string.
 *
 * \return The type of this Update as a string.
 */
const char* Update::getTypeAsString() const
{
     if (getType() > 3 || getType() < 0) {
          Error e;
          e << "Invalid update type: " << getType();
          throw e;
     }
     return types[getType()];
}

/**
 * \brief Finds the closest parent to the DataObject pointed out by
 * the provided Reference that is a ComplexDataObject.
 *
 * \param ref The Reference to the object to find the closest complex
 * parent for.
 * \return A Reference to the closest complex parent or null if no
 * such parent was found.
 */
const Reference* Update::findReferenceToClosestComplexParent(const Reference& ref)
{
     const Reference* ret;
     for (ret = ref.scope();
          ret && !dynamic_cast<ComplexDataObject*>(Mapper::map(*ret));
          ret = ret->scope()) {
     }
     if (!ret) {
          Error e;
          e << "Couldn't find target ComplexDataObject for update of object '" << ref;
          throw e;
     }
     return ret;
}
