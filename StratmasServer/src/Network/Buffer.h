#ifndef STRATMAS_BUFFER_H
#define STRATMAS_BUFFER_H

// System
#include <string>
#include <vector>

// Own
#include "Lockable.h"
#include "Mapper.h"
#include "Time2.h"

// Forward declarations
class CombatGrid;
class DataObject;
class Faction;
class Grid;
class GridDataHandler;
class Simulation;
class Update;


/**
 * \brief This class is used to store data that should be transfered
 * between the simulation and the clients.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/10/10 09:36:51 $
 */
class Buffer : public Lockable {
private:
     /// Handles grid data.
     GridDataHandler* mGridDataHandler;

     DataObject* mSimulation;   ///< The simulation DataObject.
     DataObject* mSimClone;     ///< The original simulation DataObject.

     Time        mSimTime;      ///< Data in the buffer is valid for this time.
     Time        mCurrentTime;  ///< Current Engine timestep (data not necessarily copied to Buffer).
                
     bool        mEngineIdle;   ///< True if the Engine isn't working on a timestep.
     int         mResetCount;   ///< Counts the number of resets.
     
     std::vector<Update*> mUpdates;   ///< A vector containing all updates.
     std::string mSimulationName;     ///< The name of the currently initialized simulation.

public:
     Buffer();
     ~Buffer();
     
     /**
      * \brief Checks if there is data stored in the buffer.
      *
      * \return True if there is data stored in the Buffer.
      */
     bool hasData() const { return (mSimulation && mGridDataHandler); }

     const Grid& grid() const;
     const CombatGrid& combatGrid() const;

     /**
      * \brief Accessor for the GridDatahandler.
      *
      * \return The GridDatahandler.
      */
     const GridDataHandler& gridDataHandler() const { return *mGridDataHandler; }

     /**
      * \brief Accessor for the reset count.
      *
      * \return The reset count.
      */
     int resetCount() const { return mResetCount; }

     void layer(const std::string& lay, const Reference& fac, int size, int32_t* index, double*& outData);

     /**
      * \brief Accessor for the simulation DataObject.
      *
      * \return The simulation DataObject.
      */
     const DataObject* simulation()  const { return mSimulation; }

     /**
      * \brief Accessor for the original simulation DataObject,
      * i.e. the one that the server got initialized with.
      *
      * \return The original simulation DataObject.
      */
     const DataObject* originalSimulation()  const { return mSimClone; }
     std::string simulationName() const;

     void extractGridData(GridDataHandler* gdh);
     void transferUpdatesToSimulation();

     /**
      * \brief Accessor for the current simulation time.
      *
      * \return The current simulation time.
      */
     Time currentTime() const { return mCurrentTime; }

     /**
      * \brief Mutator for the current simulation time.
      *
      * \param t The current simulation time.
      */
     void currentTime(const Time t) { mCurrentTime = t; }

     /**
      * \brief Checks if the engin is idle.
      *
      * \return True if the Engine is idle, false otherwise.
      */
     bool engineIdle() const { return mEngineIdle; }

     /**
      * \brief Mutator for the mEngineIdle flag.
      *
      * \param b The new state of the mEngineIdle flag.
      */
     void engineIdle(bool b) { mEngineIdle = b; }

     /**
      * \brief Accessor for the time for which the data in the Buffer
      * is valid.
      *
      * \return The time for which the data in the Buffer is valid.
      */
     Time simTime() const { return mSimTime; }

     /**
      * \brief Mutator for the time for which the data in the Buffer
      * is valid.
      *
      * \param simTime The time for which the data in the Buffer is
      * valid.
      */
     void simTime(Time simTime) { mSimTime = simTime; }


     // Initialization functions
     void put(DataObject* d);
     void put(std::vector<Update*> updates);
     void reset();

     /**
      * \brief Maps the provided Reference to its corresponding
      * DataObject.
      *
      * \param ref The Reference to find a DataObject for.
      * \return The DataObject for the provided Reference of null if no
      * such DataObject was found..
      */
     DataObject* map(const Reference& ref) const { return Mapper::map(ref); }
};

#endif   // STRATMAS_BUFFER_H
