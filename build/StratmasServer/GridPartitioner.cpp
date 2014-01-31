// Own
#include "DataObject.h"
#include "Error.h"
#include "Grid.h"
#include "GridPartitioner.h"


/**
 * \brief Creates a ConstantStepper from the provided DataObject.
 *
 * \param d The Databject to create this ConstantStepper from.
 */
SquarePartitioner::SquarePartitioner(const DataObject& d)
     : GridPartitioner(d), mCellSideMeters(d.getChild("cellSizeMeters")->getDouble())
{
}

/**
 * \brief Creates a Grid.
 *
 * \param m The Map to create the Grid for.
 * \param numEthnicFactions The number of ethnic factions.
 * \return The newly created Grid.
 */
Grid* SquarePartitioner::createGrid(const Map& m, int numEthnicFactions) const
{
     return new Grid(m, mCellSideMeters, numEthnicFactions);
}

/**
 * \brief Square partitioners can not be updated so calling this
 * function is an erroneous behavior.
 *
 * \param u The Update to update this object with.
 */
void SquarePartitioner::update(const Update& u)
{
     Error e("Should not call update for SquarePartitioner");
     throw e;
}

/**
 * \brief Resets this object to the state it would have had if it was
 * created from the provided DataObject.
 *
 * \param d The DataObject to use as source for the reset.
 */
void SquarePartitioner::reset(const DataObject& d)
{
     mCellSideMeters = d.getChild("cellSizeMeters")->getDouble();
}
