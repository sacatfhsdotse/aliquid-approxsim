// Own
#include "Buffer.h"
#include "DataObject.h"
#include "Error.h"
#include "TimeStepper.h"
#include "Update.h"


/**
 * \brief Creates a ConstantStepper from the provided DataObject.
 *
 * \param d The DataObject to create this ConstantStepper from.
 */
ConstantStepper::ConstantStepper(const DataObject &d)
     : TimeStepper(d), mDt(d.getChild("dt")->getTime())
{
}

/**
 * \brief Updates this object.
 *
 * \param u The Update to update this object with.
 */
void ConstantStepper::update(const Update& u)
{
     if (u.getType() == Update::eModify && u.getReference().name() == "dt") {
	  mDt = u.getObject()->getTime();
     }
     else {
	  Error e;
	  e << "No updatable attribute '" << u.getReference().name() << "' in '" << ref() << "'";
	  throw e;
     }
}

/**
 * \brief Extracts data from this object to the Buffer.
 *
 * \param b The Buffer to extract data to.
 */
void ConstantStepper::extract(Buffer &b) const
{
     DataObject& me = *b.map(ref());
     me.getChild("dt")->setTime(mDt);
}

/**
 * \brief Resets this object to the state it would have had if it was
 * created from the provided DataObject.
 *
 * \param d The DataObject to use as source for the reset.
 */
void ConstantStepper::reset(const DataObject& d)
{
     mDt = d.getChild("dt")->getTime();
}
