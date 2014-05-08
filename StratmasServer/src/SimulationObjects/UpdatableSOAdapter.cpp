// Own
#include "DataObject.h"
#include "Error.h"
#include "Reference.h"
#include "Update.h"
#include "UpdatableSOAdapter.h"


/**
 * \brief Update dispatcher function.
 *
 * \param u The Update to update this object with.
 */
void UpdatableSOAdapter::update(const Update& u)
{
     switch (u.getType()) {
     case Update::eAdd:
          approxsimDebug("Adding object " << u.getReference());
          addObject(*u.getObject(), u.getInitiator());
          break;
     case Update::eRemove:
          approxsimDebug("Removing object " << u.getReference());
          removeObject(u.getReference(), u.getInitiator());
          break;
     case Update::eReplace:
          approxsimDebug("Replacing object " << u.getReference());
          replaceObject(*u.getObject(), u.getInitiator());
          break;
     case Update::eModify:
          approxsimDebug("Modifying object " << u.getReference());
          modify(*u.getObject());
          break;
     default:
          Error e;
          e << "Unknown update type " << u.getType() << " for update with target ";
          throw e;
     }
}

/**
 * \brief Adds the SimulationObject created from the provided
 * DataObject to this object.
 *
 * \param toAdd The DataObject to create the new SimulationObject from.
 * \param initiator The id of the initiator of the update.
 */
void UpdatableSOAdapter::addObject(DataObject& toAdd, int64_t initiator)
{
     Error e;
     e << "Can't add '" << toAdd.identifier() << "' to object '" << ref() << "'";
     throw e;
}

/**
 * \brief Removes the SimulationObject referenced by the provided
 * Reference from this object.
 *
 * \param toRemove The Reference to the object to remove.
 * \param initiator The id of the initiator of the update.
 */
void UpdatableSOAdapter::removeObject(const Reference& toRemove, int64_t initiator)
{
     Error e;
     e << "Can't remove '" << toRemove.name() << "' from object '" << ref() << "'";
     throw e;
}

/**
 * \brief Replaces the SimulationObject with the same reference as the
 * provided DataObject with a new SimulationObject created from the
 * provided DataObject.
 *
 * \param newObject The DataObject to create the replacing object from.
 * \param initiator The id of the initiator of the update.
 */
void UpdatableSOAdapter::replaceObject(DataObject& newObject, int64_t initiator)
{
     Error e;
     e << "Can't replace '" << newObject.identifier() << "' in object '" << ref() << "'";
     throw e;
}

/**
 * \brief Modifies this object with data from the provided DataObject.
 *
 * \param d The DataObject containing the new value.
 */
void UpdatableSOAdapter::modify(const DataObject& d)
{
     Error e;
     e << "modify() not supported for object '" << ref() << "'";
     throw e;
}

