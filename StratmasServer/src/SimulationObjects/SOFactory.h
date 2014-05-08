#ifndef APPROXSIM_SOFACTORY
#define APPROXSIM_SOFACTORY

// System
#include <map>
#include <set>
#include <string>

// Own

// Forward Declarations
class DataObject;
class Reference;
class SimulationObject;
class SOFactoryListener;
class Type;


/**
 * \brief Factory for SimulationObjects.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/10/02 16:01:49 $
 */
class SOFactory {
private:
     /**
      * \brief Maps the name of a type to the function used to create
      * a SimulationObject of that type from a DataObject.
      */
     static std::map<std::string, SimulationObject*(*)(const DataObject&)> sCreatorMap;

     /// The listeners listening to object creation and removal events.
     static std::map<const Reference*, std::set<SOFactoryListener*> > mListeners;

     static void fireObjectAdded(const Reference& ref, int64_t initiator); 
     static void fireObjectRemoved(const Reference& ref, int64_t initiator);

public:
     static SimulationObject* createSimulationObject(const DataObject& d, int64_t initiator = -1);
     static SimulationObject* createSimulationObject(const Reference& ref, const Type& type);
     static void createOptionalSimpleIn(DataObject& d, const std::string& idToAdd, int64_t initiator = -1);
     static void createSimpleInList(const Reference& ref, const Type& type);
     static void createSimple(DataObject& d, int64_t initiator = -1);
     static void removeSimulationObject(SimulationObject *o, int64_t initiator = -1);

     /**
      * \brief Adds a listener.
      *
      * \param ref Reference to the container to listen to.
      * \param listener The listener to add.
      */
     inline static void addListener(const Reference& ref, SOFactoryListener& listener) { mListeners[&ref].insert(&listener); }
     static void removeListener(const Reference& ref, SOFactoryListener& listener);
     static void simulationObjectRemoved(const Reference& ref, int64_t initiator);
     static SimulationObject* simulationObjectReplaced(DataObject& newObj, int64_t initiator);

     static SimulationObject* createAmbushOrder(const DataObject& d);
     static SimulationObject* createAttackOrder(const DataObject& d);
     static SimulationObject* createCity(const DataObject& d);
     static SimulationObject* createCityDistribution(const DataObject& d);
     static SimulationObject* createCommonScenario(const DataObject& d);
     static SimulationObject* createCommonSimulation(const DataObject& d);
     static SimulationObject* createConstantStepper(const DataObject& d);
     static SimulationObject* createCustomAgencyTeam(const DataObject& d);
     static SimulationObject* createCustomPVModification(const DataObject& d);
     static SimulationObject* createDefendOrder(const DataObject& d);
     static SimulationObject* createDisease(const DataObject& d);
     static SimulationObject* createEthnicFaction(const DataObject& d);
//     static SimulationObject* createFactionRelation(const DataObject& d);
     static SimulationObject* createFoodAgencyTeam(const DataObject& d);
     static SimulationObject* createGoToOrder(const DataObject& d);
     static SimulationObject* createHealthAgencyTeam(const DataObject& d);
     static SimulationObject* createMilitaryFaction(const DataObject& d);
     static SimulationObject* createModelParameters(const DataObject& d);
     static SimulationObject* createNormalDistribution(const DataObject& d);
     static SimulationObject* createParameterGroup(const DataObject& d);
     static SimulationObject* createPoliceAgencyTeam(const DataObject& d);
     static SimulationObject* createRandomUniformDistribution(const DataObject& d);
     static SimulationObject* createRegion(const DataObject& d);
     static SimulationObject* createShelterAgencyTeam(const DataObject& d);
     static SimulationObject* createSquarePartitioner(const DataObject& d);
     static SimulationObject* createTerroristAttackOrder(const DataObject& d);
     static SimulationObject* createUniformDistribution(const DataObject& d);
     static SimulationObject* createUnit(const DataObject& d);
     static SimulationObject* createWaterAgencyTeam(const DataObject& d);
};

#endif   // APPROXSIM_SOFACTORY

// vim: ts=5 sw=5 expandtab:
