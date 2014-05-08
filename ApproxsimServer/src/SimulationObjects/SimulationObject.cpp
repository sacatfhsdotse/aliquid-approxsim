// Own
#include "DataObject.h"
#include "SimulationObject.h"
#include "SOMapper.h"

/**
 * \brief Creates a SimulationObject with the specified
 * Reference.
 *
 * \param ref The Reference for the object to be created.
 */
SimulationObject::SimulationObject(const Reference& ref) : Referencable(ref)
{
}

/**
 * \brief Creates a SimulationObject from the specified
 * Referencable.
 *
 * \param ref A Referencable for the object to be created.
 */
SimulationObject::SimulationObject(const Referencable& ref) : Referencable(ref)
{
}

/**
 * \brief Creates a SimulationObject from the specified
 * DataObject.
 *
 * \param d The DataObject to create this object from.
 */
SimulationObject::SimulationObject(const DataObject& d) : Referencable(d.ref())
{
     SOMapper::reg(this);
}


/**
 * \brief Destructor
 */
SimulationObject::~SimulationObject()
{
}
