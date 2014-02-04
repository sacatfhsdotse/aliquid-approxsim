// System

// Own
#include "Buffer.h"
#include "DataObject.h"
#include "Declaration.h"
#include "GridPartitioner.h"
#include "Mapper.h"
#include "ModelParameters.h"
#include "random.h"
#include "Reference.h"
#include "ParameterGroup.h"
#include "Scenario.h"
#include "Simulation.h"
#include "SOFactory.h"
#include "TimeStepper.h"
#include "Type.h"
#include "Update.h"

// Static Definitions
Time Simulation::sTimestep;
Time Simulation::sSimTime;


/**
 * \brief Creates a Simulation from the provided data object.
 *
 * \param d The data object to create this object from.
 */
Simulation::Simulation(const DataObject& d)
     : UpdatableSOAdapter(d),
       mTimeStepper(dynamic_cast<TimeStepper*>(SOFactory::createSimulationObject(*d.getChild("timeStepper")))),
       mGridPartitioner(dynamic_cast<GridPartitioner*>(SOFactory::createSimulationObject(*d.getChild("gridPartitioner")))),
       mScenario(dynamic_cast<Scenario*>(SOFactory::createSimulationObject(*d.getChild("scenario")))),
       mStartTime(d.getChild("startTime")->getTime()),
       mRandomSeed(d.getChild("randomSeed") ? d.getChild("randomSeed")->getInt64_t() : createRandomSeed())
{
     sSimTime = mStartTime;
     sTimestep = mTimeStepper->dt();
     DataObject* modParm = d.getChild("modelParameters");
     mModelParameters = (modParm ? dynamic_cast<ModelParameters*>(SOFactory::createSimulationObject(*modParm)) : 0);

//     DataObject* pg = d.getChild("parameters");
//     mParameters = (pg ? dynamic_cast<ParameterGroup*>(SOFactory::createSimulationObject(*pg)) : 0);
}


/**
 * \brief Destructor
 */
Simulation::~Simulation()
{
     SOFactory::removeSimulationObject(mTimeStepper);
     SOFactory::removeSimulationObject(mGridPartitioner);
     SOFactory::removeSimulationObject(mScenario);
     SOFactory::removeSimulationObject(mModelParameters);
//     SOFactory::removeSimulationObject(mParameters);
}

GridDataHandler* Simulation::takeOverGridDataHandler() const
{
     return mScenario->takeOverGridDataHandler();
}

/**
 * \brief Prepares this SimulationObject for simulation.
 *
 * Should be called after creation and reset and before the simulation
 * starts.
 */
void Simulation::prepareForSimulation()
{
     DataObject& myself = *Mapper::map(ref());

     // Generate random seed if we didn't get any from the client.
     DataObject* randomSeed = myself.getChild("randomSeed");
     if (!randomSeed) {
          SOFactory::createOptionalSimpleIn(myself, "randomSeed");
          randomSeed = myself.getChild("randomSeed");
          randomSeed->setInt64_t(mRandomSeed);
     }
     setRandomSeed(mRandomSeed);

     // Generate ModelParameters if we didn't get any from the client.
     if (!mModelParameters) {
          const Type& modParmType = myself.getType().getSubElement("modelParameters")->getType();
          const Reference& refToModParm = Reference::get(ref(),"modelParameters");
          mModelParameters = dynamic_cast<ModelParameters*>(SOFactory::createSimulationObject(refToModParm, modParmType));
          mModelParameters->setDefault();
     }
     mScenario->prepareForSimulation(*mGridPartitioner, *mModelParameters, mStartTime);

//      if (!mParameters) {
//           const Type& pgType = myself.getType().getSubElement("parameters")->getType();
//           const Reference& refToPgToBeCreated = Reference::get(ref(), "parameters");
//           mParameters = dynamic_cast<ParameterGroup*>(SOFactory::createSimulationObject(refToPgToBeCreated, pgType));
//      }
//      mParameters->prepareForSimulation();
}

/**
 * \brief Advances the simulation one timestep
 *
 * \return The simulation time after the step was taken.
 */
Time Simulation::step()
{
     sTimestep = mTimeStepper->dt();
     sSimTime += sTimestep;
     mScenario->step(sSimTime);
     return sSimTime;
}

/**
 * \brief Extracts data from this object to the Buffer.
 *
 * \param b The Buffer to extract data to.
 */
void Simulation::extract(Buffer& b) const
{
     DataObject& me = *b.map(ref());
     me.getChild("randomSeed")->setInt64_t(mRandomSeed);

     // The simulation time
     b.simTime(sSimTime);
}

/**
 * \brief Adds the SimulationObject created from the provided
 * DataObject to this object.
 *
 * \param toAdd The DataObject to create the new SimulationObject from.
 * \param initiator The id of the initiator of the update.
 */
void Simulation::addObject(DataObject& toAdd, int64_t initiator)
{
     const Type& type = toAdd.getType();
     if (type.canSubstitute("NonNegativeInteger")) {
          SOFactory::createSimple(toAdd, initiator);
          mRandomSeed = toAdd.getInt64_t();
          setRandomSeed(mRandomSeed);
     }
     else if (type.canSubstitute("ModelParameters")) {
          mModelParameters = dynamic_cast<ModelParameters*>(SOFactory::createSimulationObject(toAdd, initiator));
     }
//      else if (type.canSubstitute("ParameterGroup")) {
//           mParameters = dynamic_cast<ParameterGroup*>(SOFactory::createSimulationObject(toAdd, initiator));
//      }
     else {
          UpdatableSOAdapter::addObject(toAdd, initiator);
     }
}

/**
 * \brief Removes the SimulationObject referenced by the provided
 * Reference from this object.
 *
 * \param toRemove The Reference to the object to remove.
 * \param initiator The id of the initiator of the update.
 */
void Simulation::removeObject(const Reference& toRemove, int64_t initiator)
{
     DataObject* d = Mapper::map(toRemove);
     if (!d) {
          Error e;
          e << "Tried to remove non existing DataObject '" << toRemove << "' from '" << ref() << "'";
          throw e;
     }
     const Type& type = d->getType();
     if (type.canSubstitute("NonNegativeInteger")) {
          // Shouldn't be able to remove randomSeed so let's add it again.
          DataObject* addAgain = d->clone();
          SOFactory::simulationObjectRemoved(toRemove, initiator);
          SOFactory::createSimple(*addAgain);
     }
     else if (type.canSubstitute("ModelParameters")) {
          // Shouldn't be able to remove modelParameters so let's add it again.
          DataObject* addAgain = d->clone();
          SOFactory::removeSimulationObject(mModelParameters, initiator);
          addObject(*addAgain, -1);
     }
//      else if (type.canSubstitute("ParameterGroup")) {
//           // Shouldn't be able to remove ParameterGroup so let's add it again.
//           DataObject* addAgain = d->clone();
//           SOFactory::removeSimulationObject(mParameters, initiator);
//           addObject(*addAgain, -1);
//           mParameters->prepareForSimulation();
//      }
     else {
          UpdatableSOAdapter::removeObject(toRemove, initiator);
     }
}

/**
 * \brief Modifies this object with data from the provided DataObject.
 *
 * \param d The DataObject containing the new value.
 */
void Simulation::modify(const DataObject& d)
{
     const string& attr = d.identifier();
     if (attr == "startTime") {
          mStartTime = d.getTime();
     }
     else if (attr == "randomSeed") {
          mRandomSeed = d.getInt64_t();
          setRandomSeed(mRandomSeed);
     }
}

/**
 * \brief Resets this object to the state it would have had if it was
 * created from the provided DataObject.
 *
 * \param d The DataObject to use as source for the reset.
 */
void Simulation::reset(const DataObject& d)
{
     mTimeStepper->reset(*d.getChild("timeStepper"));
     mGridPartitioner->reset(*d.getChild("gridPartitioner"));
     mScenario->reset(*d.getChild("scenario"));
     mStartTime = d.getChild("startTime")->getTime();
     mRandomSeed = (d.getChild("randomSeed") ? d.getChild("randomSeed")->getInt64_t() : mRandomSeed);
     if (DataObject* o = d.getChild("modelParameters")) {
          mModelParameters->reset(*o);
     }
     else {
          SOFactory::removeSimulationObject(mModelParameters);
          const Type& modParmType = d.getType().getSubElement("modelParameters")->getType();
          mModelParameters = 
               dynamic_cast<ModelParameters*>(SOFactory::createSimulationObject(Reference::get(ref(),"modelParameters"),
                                                                                modParmType));
          mModelParameters->setDefault();
     }

     sSimTime = mStartTime;
     sTimestep = mTimeStepper->dt();
}
