#ifndef STRATMAS_SIMULATION_H
#define STRATMAS_SIMULATION_H

// Own
#include "Time.h"
#include "UpdatableSOAdapter.h"

// Forward Declarations
class GridPartitioner;
class ModelParameters;
class ParameterGroup;
class Scenario;
class TimeStepper;


/**
 * \brief A base class for all Simulations.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/10/02 16:05:16 $
 */
class Simulation : public UpdatableSOAdapter {
private:
     static Time sTimestep;   ///< The current timestep.
     static Time sSimTime;    ///< The current simulation time.

     /// The TimeStepper to use when stepping this Simulation.
     TimeStepper* mTimeStepper;

     /// The GridPartitioner to uses when partitioning the Grid.
     GridPartitioner* mGridPartitioner;

     /// The Scenario object.
     Scenario* mScenario;

     /// The start time of the Simulation.
     Time mStartTime;

     /// The random seed.
     unsigned long mRandomSeed;

     /// The ModelParameters object.
     ModelParameters* mModelParameters;

     /// The ParameterGroup object.
//     ParameterGroup* mParameters;

protected:
     Simulation(const DataObject& d);

public:
     /// Destructor
     virtual ~Simulation();

     /**
      * \brief Accessor for the size of the current time step.
      *
      * \return The size of the current time step.
      */
     static Time timestep() { return sTimestep; }

     /**
      * \brief Accessor for the current simultion time.
      *
      * \return The current simultion time.
      */
     static Time simulationTime() { return sSimTime; }

     /**
      * \brief Returns the fraction of a day that the current time
      * step constitutes.
      *
      * \return The fraction of a day that the current time step
      * constitutes.
      */
     static double fractionOfDay() { return timestep().hoursd() / 24.0; }

     GridDataHandler* takeOverGridDataHandler() const;
     void prepareForSimulation();

     Time step();

     void extract(Buffer& b) const;
     void addObject(DataObject& toAdd, int64_t initiator);
     void removeObject(const Reference& toRemove, int64_t initiator);
     void modify(const DataObject& d);
     void reset(const DataObject& d);
};


/**
 * \brief Represents the common simulation.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/10/02 16:05:16 $
 */
class CommonSimulation : public Simulation {
public:
     /**
      * \brief Creates a CommonSimulation from the provided data object.
      *
      * \param d The data object to create this object from.
      */
     CommonSimulation(const DataObject& d) : Simulation(d) {}
};

#endif   // STRATMAS_SIMULATION_H
