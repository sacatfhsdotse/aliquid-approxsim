// System
#include <vector>

// Own
#include "Buffer.h"
#include "DataObject.h"
#include "debugheader.h"
#include "GridDataHandler.h"
#include "Simulation.h"
#include "SimulationObject.h"
#include "SOMapper.h"
#include "Update.h"


using namespace std;


// Initialization of statics
MapType Mapper::mMap;


/**
 * \brief Constructor.
 */
Buffer::Buffer() : mGridDataHandler(0), mSimulation(0), mSimClone(0), mResetCount(0)
{
}

/**
 * \brief Destructor.
 */
Buffer::~Buffer()
{
     if (mSimClone) { delete mSimClone; }
     if (mGridDataHandler) { delete mGridDataHandler; }
     for (vector<Update*>::iterator it = mUpdates.begin(); it != mUpdates.end(); it++) {
	  delete *it;
     }
}

/**
 * \brief Accessor for the Grid.
 *
 * \return A Reference to the Grid.
 */
const Grid& Buffer::grid() const
{
     return mGridDataHandler->grid();
}

/**
 * \brief Accessor for the CombatGrid.
 *
 * \return A Reference to the CombatGrid.
 */
const CombatGrid& Buffer::combatGrid() const
{
     return mGridDataHandler->combatGrid();
}


/**
 * \brief Fetches process variable values for the specified cells,
 * process variable and faction.
 *
 * \param lay The name of the process variable.
 * \param fac A Reference to the faction.
 * \param size The number of cells to fetch values for.
 * \param index An array of size elements containing the indices in
 * the active cells array of the cells for which to fetch the values.
 * \param outData An array of size elements that on return will
 * contain the values for the specified cells.
 */
void Buffer::layer(const std::string& lay,
		   const Reference& fac,
		   int size,
		   int32_t* index,
		   double*& outData)
{
     mGridDataHandler->layer(lay, fac, size, index, outData);
}

/**
 * \brief Stores data about a simulation in the Buffer.
 *
 * Called by a Session after receiving an InitializationMessage from
 * an active client.
 *
 * \param d The DataObject for the simulation.
 */
void Buffer::put(DataObject* d)
{
     if (mSimulation) {
	  delete mSimClone;            // Delete clone of old simulation.
	  delete mSimulation;
	  mResetCount++;
     }
     mSimulation = d;
     mSimClone = mSimulation->clone();
     mSimulationName = mSimulation->ref().name();
     if (mGridDataHandler) {
	  delete mGridDataHandler;
	  mGridDataHandler = 0;
     }
}

/**
 * \brief Stores data about updates in the Buffer.
 *
 * Called by a Session after receiving an UpdateMessage from an active
 * client. From this point, the Buffer takes over responsibility for
 * deallocating memory used by the updates.
 *
 * \param updates A vector containing the updates.
 */
void Buffer::put(vector<Update*> updates)
{
     Lock lock(mutex());
     mUpdates.insert(mUpdates.begin(), updates.begin(), updates.end());
     lock.unlock();
}

/**
 * \brief Resets the buffer to the same state as directly after an
 * initialization.
 *
 * Called by the Engine when told to reset the simulation.
 */
void Buffer::reset() {
     if (mGridDataHandler) {
	  delete mGridDataHandler;
	  mGridDataHandler = 0;
     }
     mResetCount++;
}

/**
 * \brief Copies data from the simulation Grid to the Buffer so that
 * it will be accessible for clients.
 *
 * Called by the Engine when simulation data should be transfered from
 * the simulation to the Buffer, for example after each timestep and
 * after initialization.
 *
 * Notice that the call to this function means that the Buffer takes
 * over the responsibility to deallocate the GridDataHandler.
 *
 * \param gdh The GridDataHandler created by the Scenario.
 */
void Buffer::extractGridData(GridDataHandler* gdh)
{
     if (!mGridDataHandler) {
	  mGridDataHandler = gdh;
     }
     mGridDataHandler->extractGridData();
}

/**
 * \brief Transfers the updates from the Buffer to the simulation.
 *
 * Called by the Engine when told to update the simulation.
 */
void Buffer::transferUpdatesToSimulation()
{
     Lock lock(mutex());
     
     std::vector<Error> errors;     
     for (vector<Update*>::iterator it = mUpdates.begin(); it != mUpdates.end(); it++) {
	  try {
	       Update& u = **it;
	       SimulationObject* target = SOMapper::map(u.getTargetRef());
	       if (target) {
		    debug("Trying to update target " << target->ref());
		    target->update(u);
	       }
	       else {
		    Error e;
		    e << "Update failed. Couldn't find target SimulationObject '" << u.getTargetRef();
		    e << "' for update of child '" << u.getReference().name() << "'";
		    throw e;
	       }
	  } catch (Error& e) {
	       errors.push_back(e);
	  }
     }

     for (vector<Update*>::iterator it = mUpdates.begin(); it != mUpdates.end(); it++) {
	  delete *it;
     }

     mUpdates.clear();
     lock.unlock();

     if (!errors.empty()) {
	  throw errors;
     }
}

/**
 * \brief Returns the name of the currently initialized simulation.
 *
 * Used in order to produce LoadQuery messages.
 *
 * \return The name of the currently initialized simulation.
 */
string Buffer::simulationName() const
{
     return mSimulationName;
}
