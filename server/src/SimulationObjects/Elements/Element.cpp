// System
#include <string>

// Own
#include "Buffer.h"
#include "DataObject.h"
#include "Distribution.h"
#include "Element.h"
#include "Error.h"
#include "SOFactory.h"
#include "Shape.h"


using namespace std;


/**
 * \brief Constructor that performs a deep copy of the location. - For
 * camps.
 *
 * \param ref The reference to this Element.
 * \param location The location of this Element.
 */
Element::Element(const Reference &ref, const Shape &location)
     : UpdatableSOAdapter(ref), mLocation(location.clone()), mDeployment(0)
{
}

/**
 * \brief Constructor that creates an Element from a DataObject
 *
 * \param d The DataObject to create this object from.
 */
Element::Element(const DataObject& d)
     : UpdatableSOAdapter(d),
       mLocation(d.getChild("location")->getShape()),
       mDeployment(dynamic_cast<Distribution*>(SOFactory::createSimulationObject(*d.getChild("deployment"))))
{
}

/// Destructor.
Element::~Element()
{
     if (mLocation) { delete mLocation; }
     SOFactory::removeSimulationObject(mDeployment);
}

/**
 * \brief Accessor for the center coordinate.
 *
 * \return The center coordinate.
 */
LatLng Element::center() const
{
     return mLocation->cenCoord();
}

/**
 * \brief Extracts data from this object to the Buffer.
 *
 * \param b The Buffer to extract data to.
 */
void Element::extract(Buffer &b) const
{
     DataObject& me = *b.map(ref());
     me.getChild("present")->setBool(present());
     me.getChild("location")->setShape(&location());
}

/**
 * \brief Replaces the SimulationObject with the same reference as the
 * provided DataObject with a new SimulationObject created from the
 * provided DataObject.
 *
 * \param newObject The DataObject to create the replacing object from.
 * \param initiator The id of the initiator of the update.
 */
void Element::replaceObject(DataObject& newObject, int64_t initiator)
{
     const string& attr = newObject.identifier();
     if (attr == "location") {
          delete mLocation;
          mLocation = newObject.getShape();
          SOFactory::simulationObjectReplaced(newObject, initiator);
     }
     else if (attr == "deployment") {
          mDeployment = dynamic_cast<Distribution*>(SOFactory::simulationObjectReplaced(newObject, initiator));
     }
     else {
          UpdatableSOAdapter::replaceObject(newObject, initiator);
     }
}

/**
 * \brief Modifies this object with data from the provided DataObject.
 *
 * \param d The DataObject containing the new value.
 */
void Element::modify(const DataObject& d)
{
     const string& attr = d.ref().name();
     if (attr == "present") {
          Error e("It is not allowed to update the 'present' attribute in Elements", Error::eWarning);
          throw e;
     }
     else if (attr == "location") {
          delete mLocation;
          mLocation = d.getShape();
     }
     else {
          Error e;
          e << "No updatable attribute '" << attr << "' in '" << ref() << "'";
          throw e;
     }
}

/**
 * \brief Resets this object to the state it would have had if it was
 * created from the provided DataObject.
 *
 * \param d The DataObject to use as source for the reset.
 */
void Element::reset(const DataObject& d)
{
     delete mLocation;
     mLocation = d.getChild("location")->getShape();
     mDeployment->reset(*d.getChild("deployment"));
}

