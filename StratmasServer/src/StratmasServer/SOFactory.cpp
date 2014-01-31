// System

// Own
#include "Activity.h"
#include "AgencyTeam.h"
#include "DataObjectImpl.h"
#include "City.h"
#include "Disease.h"
#include "Distribution.h"
#include "Error.h"
#include "Faction.h"
//#include "FactionRelation.h"
#include "GridPartitioner.h"
#include "Mapper.h"
#include "ModelParameters.h"
#include "ValidParameterGroups.h"
#include "Region.h"
#include "Scenario.h"
#include "Simulation.h"
#include "SOFactory.h"
#include "SOFactoryListener.h"
#include "SOMapper.h"
#include "TimeStepper.h"
#include "Type.h"
#include "Unit.h"


using namespace std;

// Static Deinitions
std::map<std::string, SimulationObject*(*)(const DataObject&)> SOFactory::sCreatorMap;
std::map<const Reference*, std::set<SOFactoryListener*> > SOFactory::mListeners;


/**
 * \brief Fires an objectAdded event.
 *
 * Both the SimulationObject and the corresponding DataObject exists
 * and are registered when this call occurs.
 *
 * \param ref The Reference to the added object.
 * \param initiator The id of the initiator of the event.
 */
void SOFactory::fireObjectAdded(const Reference& ref, int64_t initiator)
{
     set<SOFactoryListener*>& listeners = mListeners[ref.scope()];
     for (set<SOFactoryListener*>::iterator it = listeners.begin(); it != listeners.end(); it++) {
	  (*it)->objectAdded(ref, initiator);
     }
}

/**
 * \brief Fires an objectRemoved event.
 *
 * When this function is called the SimulationObject is already
 * deleted and deregistered. The corresponding DataObject does still
 * exist and is still registered.
 *
 * \param ref The Reference to the removed object.
 * \param initiator The id of the initiator of the event.
 */
void SOFactory::fireObjectRemoved(const Reference& ref, int64_t initiator)
{
     set<SOFactoryListener*>& listeners = mListeners[ref.scope()];
     for (set<SOFactoryListener*>::iterator it = listeners.begin(); it != listeners.end(); it++) {
	  (*it)->objectRemoved(ref, initiator);
     }
}

/**
 * \brief Creates a SimulationObject from the provided DataObject.
 *
 * This function should only be called for DataObjects which type is a
 * non ValueType descendant.
 *
 * \param d The DataObject to create a SimulationObject from.
 * \param initiator The id of the initiator of the creation.
 * \return The newly created SimulationObject.
 */
SimulationObject* SOFactory::createSimulationObject(const DataObject& d, int64_t initiator)
{
     static bool firstTime = true;
     if (firstTime) {
	  firstTime = false;
 	  sCreatorMap["AmbushOrder"                  ] = createAmbushOrder;
 	  sCreatorMap["AttackOrder"                  ] = createAttackOrder;
	  sCreatorMap["Population"                   ] = createCity;
 	  sCreatorMap["StratmasCityDistribution"     ] = createCityDistribution;
 	  sCreatorMap["CommonScenario"               ] = createCommonScenario;
 	  sCreatorMap["CommonSimulation"             ] = createCommonSimulation;
 	  sCreatorMap["ConstantStepper"              ] = createConstantStepper;
 	  sCreatorMap["ControlOrder"                 ] = createCustomPVModification; 
 	  sCreatorMap["CustomAgencyTeam"             ] = createCustomAgencyTeam;
	  sCreatorMap["CustomAreaOrder"              ] = createCustomPVModification;
 	  sCreatorMap["CustomPVModification"         ] = createCustomPVModification;
 	  sCreatorMap["DefendOrder"                  ] = createDefendOrder;
 	  sCreatorMap["Disease"                      ] = createDisease;
	  sCreatorMap["EthnicFaction"                ] = createEthnicFaction;
// 	  sCreatorMap["FactionRelation"              ] = createFactionRelation;
 	  sCreatorMap["FoodAgencyTeam"               ] = createFoodAgencyTeam;
 	  sCreatorMap["FreedomOfMovementOrder"       ] = createCustomPVModification;
 	  sCreatorMap["GoToOrder"                    ] = createGoToOrder;
 	  sCreatorMap["HealthAgencyTeam"             ] = createHealthAgencyTeam;
	  sCreatorMap["MilitaryFaction"              ] = createMilitaryFaction;
 	  sCreatorMap["ModelParameters"              ] = createModelParameters;
 	  sCreatorMap["NormalDistribution"           ] = createNormalDistribution;
 	  sCreatorMap["ParameterGroup"               ] = createParameterGroup;
 	  sCreatorMap["PoliceAgencyTeam"             ] = createPoliceAgencyTeam;
 	  sCreatorMap["PresenceOrder"                ] = createCustomPVModification;
 	  sCreatorMap["ProvideCivilianFunctionsOrder"] = createCustomPVModification;
 	  sCreatorMap["RandomUniformDistribution"    ] = createRandomUniformDistribution;
 	  sCreatorMap["Region"                       ] = createRegion;
 	  sCreatorMap["SecureOrder"                  ] = createCustomPVModification;
 	  sCreatorMap["ShelterAgencyTeam"            ] = createShelterAgencyTeam;
 	  sCreatorMap["SquarePartitioner"            ] = createSquarePartitioner;
 	  sCreatorMap["TerroristAttackOrder"         ] = createTerroristAttackOrder;
 	  sCreatorMap["UniformDistribution"          ] = createUniformDistribution;
 	  sCreatorMap["MilitaryUnit"                 ] = createUnit;
 	  sCreatorMap["WaterAgencyTeam"              ] = createWaterAgencyTeam;
     }
     std::map<std::string, SimulationObject*(*)(const DataObject&)>::iterator it = sCreatorMap.find(d.getType().getName());
     if (it == sCreatorMap.end()) {
	  Error e;
	  e << "Unknown object type '" << d.getType().getName() << "' in SOFactory.";
	  throw e;
     }
     SimulationObject* so = (*it->second)(d);
     d.reg();
     fireObjectAdded(so->ref(), initiator);

     return so;
}

/**
 * \brief Creates a SimulationObject of the provided Type with the
 * provided Reference by first creating the corresponing DataObject
 * and then calling createSimulationObject() with the DataObject as
 * parameter.
 *
 * This function should only be called when the server creates
 * optional objects or objects in lists on its own initaitve.
 *
 * \param ref The Reference to create a SimulationObject for.
 * \param type The Type of SimulationObject to create.
 * \return The newly created SimulationObject.
 */
SimulationObject* SOFactory::createSimulationObject(const Reference& ref, const Type& type)
{
     DataObject* created = 0;
     DataObject* parent = Mapper::map(*ref.scope());
     if (dynamic_cast<ComplexDataObject*>(parent)) {
	  // Parent is Complex so the object to be created must be optional.
	  created = DataObjectFactory::addOptional(*parent, ref.name());
     }
     else {
	  created = DataObjectFactory::createDataObject(ref, type);
	  if (ContainerDataObject* c = dynamic_cast<ContainerDataObject*>(parent)) {
	       // Parent is not Complex so the order does not matter.
	       c->add(created);
	  }
     }
     return createSimulationObject(*created);
}

/**
 * \brief Since ValueType descendants have no corresponding
 * SimulationObject we can not create an actual SimulationObject so we
 * create the corresponding DataObject and register it.
 *
 * This function should only be called when the server creates
 * ValueType descendants that are optional.
 *
 * \param d The DataObject to create a SimulationObject in.
 * \param idToAdd The identifier of the object to add.
 * \param initiator The id of the initiator of the creation.
 */
void SOFactory::createOptionalSimpleIn(DataObject& d, const std::string& idToAdd, int64_t initiator)
{
     DataObject* added = DataObjectFactory::addOptional(d, idToAdd);
     added->reg();
     fireObjectAdded(added->ref(), initiator);
}

/**
 * \brief Since ValueType descendants have no corresponding
 * SimulationObject we can not create an actual SimulationObject so we
 * create a DataObject with the provided reference and type and calls
 * createSimple().
 *
 * \param ref The Reference to the object to be created.
 * \param type The type of the object to be created.
 */
void SOFactory::createSimpleInList(const Reference& ref, const Type& type)
{
     createSimple(*DataObjectFactory::createDataObject(ref, type));
}

/**
 * \brief Since ValueType descendants have no corresponding
 * SimulationObject we can not create an actual SimulationObject so we
 * simply add the provided DataObject to the DataObject referenced by
 * its scope and registers it.
 *
 * This function should only be called when the server creates
 * ValueType descendants on its own initiative.
 *
 * \param d The DataObject representing the ValueType desendant.
 * \param initiator The id of the initiator of the creation.
 */
void SOFactory::createSimple(DataObject& d, int64_t initiator)
{
     const Reference& refToParent = *d.ref().scope();
     if (ContainerDataObject* cdo = dynamic_cast<ContainerDataObject*>(Mapper::map(refToParent))) {
	  cdo->add(&d);
	  d.reg();
	  fireObjectAdded(d.ref(), initiator);
     }
     else {
	  Error e;
	  e << "No ContainerDataObject with Reference '" << refToParent << "'";
	  throw e;
     }
}

/**
 * \brief Removes a SimulationObject.
 *
 * \param o The SimulationObject to remove.
 * \param initiator The id of the initiator of the removal.
 */
void SOFactory::removeSimulationObject(SimulationObject *o, int64_t initiator)
{
     if (o) {
	  const Reference& toRemove = o->ref();
	  delete o;
	  if (SOMapper::map(toRemove)) {
	       SOMapper::dereg(toRemove);
	       simulationObjectRemoved(toRemove, initiator);
	  }
     }
}

/**
 * \brief Removes a listener.
 *
 * \param ref Reference to the container that has been listened to.
 * \param listener The listener to remove.
 */
void SOFactory::removeListener(const Reference& ref, SOFactoryListener& listener)
{
     set<SOFactoryListener*>& listeners = mListeners[&ref];
     listeners.erase(&listener);
     if (listeners.empty()) {
	  mListeners.erase(&ref);
     }
}

/**
 * \brief This function should be called when the server has removed a
 * ValueType descendant on its own initiaive. Since there is no actual
 * SimulationObject to remove we deregister and remove the
 * corresponding DataObject.
 *
 * \param ref The Reference to the object to remove.
 * \param initiator The id of the initiator of the removal.
 */
void SOFactory::simulationObjectRemoved(const Reference& ref, int64_t initiator)
{
     DataObject* d = Mapper::map(ref);
     if (d) {
	  fireObjectRemoved(ref, initiator);
	  d->dereg();
	  ContainerDataObject* parent = d->getParent();
	  if (parent) {
	       parent->remove(d->identifier());
	  }
     }
     else {
	  Error e;
	  e << "SOFactory tried to delete SimulationObject '" << ref << "'without registered DataObject.";
	  throw e;
     }
}

/**
 * \brief This function should be called when the server has replaced
 * an object on its own initiaive.
 *
 * If the removed object is a ValueType descendant no new
 * SimulationObject is created. Otherwise the SimulationObject created
 * from the replacing DataObject is returned.
 *
 * \param newObj The DataObject to replace the old object with.
 * \param initiator The id of the initiator of the removal.
 * \return The new SimulationObject or null if newObject is a
 * ValueType descendant.
 */
SimulationObject* SOFactory::simulationObjectReplaced(DataObject& newObj, int64_t initiator)
{
     SimulationObject* ret = 0;
     DataObject* old = Mapper::map(newObj.ref());
     if (old) {
	  if (newObj.getType().canSubstitute("ValueType")) {
	       fireObjectRemoved(old->ref(), initiator);
	       old->dereg();
	       ContainerDataObject* parent = old->getParent();
	       
	       if (parent) {
		    parent->replace(&newObj);
	       }
	       else {
		    Error e;
		    e << "Replacing DataObject " << old->ref() << " without parent";
		    throw e;
	       }
	       newObj.reg();
	       fireObjectAdded(newObj.ref(), initiator);
	  }
	  else {
	       removeSimulationObject(SOMapper::map(newObj.ref()), initiator);
	       ret = createSimulationObject(newObj, initiator);
	  }
     }
     else {
	  Error e;
	  e << "SOFactory tried to replace non registered DataObject '" << newObj.ref() << "'.";
	  throw e;
     }
     return ret;
}

/**
 * \brief Creates a AmbushOrder object from the specified DataObject.
 *
 * \param d The DataObject to create this AmbushOrder object from.
 * \return The newly created AmbushOrder object.
 */
SimulationObject* SOFactory::createAmbushOrder(const DataObject& d)
{
     return new AmbushOrder(d);
}
 
/**
 * \brief Creates a AttackOrder object from the specified DataObject.
 *
 * \param d The DataObject to create this AttackOrder object from.
 * \return The newly created AttackOrder object.
 */
SimulationObject* SOFactory::createAttackOrder(const DataObject& d)
{
     return new AttackOrder(d);
}
 
/**
 * \brief Creates a City object from the specified DataObject.
 *
 * \param d The DataObject to create this City object from.
 * \return The newly created City object.
 */
SimulationObject* SOFactory::createCity(const DataObject& d)
{
     return new City(d);
}
 
/**
 * \brief Creates a CityDistribution object from the specified DataObject.
 *
 * \param d The DataObject to create this CityDistribution object from.
 * \return The newly created CityDistribution object.
 */
SimulationObject* SOFactory::createCityDistribution(const DataObject& d)
{
     return new CityDistribution(d);
}

/**
 * \brief Creates a CommonScenario object from the specified DataObject.
 *
 * \param d The DataObject to create this CommonScenario object from.
 * \return The newly created CommonScenario object.
 */
SimulationObject* SOFactory::createCommonScenario(const DataObject& d)
{
     return new Scenario(d);
}
 
/**
 * \brief Creates a CommonSimulation object from the specified
 * DataObject.
 *
 * \param d The DataObject to create this CommonSimulation object from.
 * \return The newly created CommonSimulation object.
 */
SimulationObject* SOFactory::createCommonSimulation(const DataObject& d)
{
     return new CommonSimulation(d);
}
 
/**
 * \brief Creates a ConstantStepper object from the specified DataObject.
 *
 * \param d The DataObject to create this ConstantStepper object from.
 * \return The newly created ConstantStepper object.
 */
SimulationObject* SOFactory::createConstantStepper(const DataObject& d)
{
     return new ConstantStepper(d);
}
 
/**
 * \brief Creates a CustomAgencyTeam object from the specified DataObject.
 *
 * \param d The DataObject to create this CustomAgencyTeam object from.
 * \return The newly created CustomAgencyTeam object.
 */
SimulationObject* SOFactory::createCustomAgencyTeam(const DataObject& d)
{
     return new CustomAgencyTeam(d);
}
 
/**
 * \brief Creates a CustomPVModification object from the specified DataObject.
 *
 * \param d The DataObject to create this CustomPVModification object from.
 * \return The newly created CustomPVModification object.
 */
SimulationObject* SOFactory::createCustomPVModification(const DataObject& d)
{
     return new CustomPVModification(d);
}
 
/**
 * \brief Creates a DefendOrder object from the specified DataObject.
 *
 * \param d The DataObject to create this DefendOrder object from.
 * \return The newly created DefendOrder object.
 */
SimulationObject* SOFactory::createDefendOrder(const DataObject& d)
{
     return new DefendOrder(d);
}
 
/**
 * \brief Creates a Disease object from the specified DataObject.
 *
 * \param d The DataObject to create this Disease object from.
 * \return The newly created Disease object.
 */
SimulationObject* SOFactory::createDisease(const DataObject& d)
{
     return new Disease(d);
}
 
/**
 * \brief Creates a EthnicFaction object from the specified DataObject.
 *
 * \param d The DataObject to create this EthnicFaction object from.
 * \return The newly created EthnicFaction object.
 */
SimulationObject* SOFactory::createEthnicFaction(const DataObject& d)
{
     return new EthnicFaction(d);
}
 
/**
 * \brief Creates a FactionRelation object from the specified DataObject.
 *
 * \param d The DataObject to create this FactionRelation object from.
 * \return The newly created FactionRelation object.
 */
// SimulationObject* SOFactory::createFactionRelation(const DataObject& d)
// {
//      return new FactionRelation(d);
// }
 
/**
 * \brief Creates a FoodAgencyTeam object from the specified DataObject.
 *
 * \param d The DataObject to create this FoodAgencyTeam object from.
 * \return The newly created FoodAgencyTeam object.
 */
SimulationObject* SOFactory::createFoodAgencyTeam(const DataObject& d)
{
     return new FoodAgencyTeam(d);
}
 
/**
 * \brief Creates a GoToOrder object from the specified DataObject.
 *
 * \param d The DataObject to create this GoToOrder object from.
 * \return The newly created GoToOrder object.
 */
SimulationObject* SOFactory::createGoToOrder(const DataObject& d)
{
     return new GoToOrder(d);
}
 
/**
 * \brief Creates a HealthAgencyTeam object from the specified DataObject.
 *
 * \param d The DataObject to create this HealthAgencyTeam object from.
 * \return The newly created HealthAgencyTeam object.
 */
SimulationObject* SOFactory::createHealthAgencyTeam(const DataObject& d)
{
     return new HealthAgencyTeam(d);
}
 
/**
 * \brief Creates a MilitaryFaction object from the specified DataObject.
 *
 * \param d The DataObject to create this MilitaryFaction object from.
 * \return The newly created MilitaryFaction object.
 */
SimulationObject* SOFactory::createMilitaryFaction(const DataObject& d)
{
     return new MilitaryFaction(d);
}
 
/**
 * \brief Creates a ModelParameters object from the specified DataObject.
 *
 * \param d The DataObject to create this ModelParameters object from.
 * \return The newly created ModelParameters object.
 */
SimulationObject* SOFactory::createModelParameters(const DataObject& d)
{
     return new ModelParameters(d);
}
 
/**
 * \brief Creates a NormalDistribution object from the specified DataObject.
 *
 * \param d The DataObject to create this NormalDistribution object from.
 * \return The newly created NormalDistribution object.
 */
SimulationObject* SOFactory::createNormalDistribution(const DataObject& d)
{
     return new NormalDistribution(d);
}

/**
 * \brief Creates a ParameterGroup object from the specified DataObject.
 *
 * \param d The DataObject to create this ParameterGroup object from.
 * \return The newly created ParameterGroup object.
 */
SimulationObject* SOFactory::createParameterGroup(const DataObject& d)
{
     if (d.identifier() == "Food Model") {
	  return new FoodModelParameters(d);
     }
     else if (d.identifier() == "Insurgent Model") {
	  return new InsurgentModelParameters(d);
     }
     else if (d.identifier() == "parameters") {
	  return new DefaultParameterGroup(d);
     }
     else {
	  Error e;
	  e << "'" << d.identifier() << "' is not a valid ParameterGroup.";
	  throw e;
     }
}
 
/**
 * \brief Creates a PoliceAgencyTeam object from the specified DataObject.
 *
 * \param d The DataObject to create this PoliceAgencyTeam object from.
 * \return The newly created PoliceAgencyTeam object.
 */
SimulationObject* SOFactory::createPoliceAgencyTeam(const DataObject& d)
{
     return new PoliceAgencyTeam(d);
}
 
/**
 * \brief Creates a RandomUniformDistribution object from the specified DataObject.
 *
 * \param d The DataObject to create this RandomUniformDistribution object from.
 * \return The newly created RandomUniformDistribution object.
 */
SimulationObject* SOFactory::createRandomUniformDistribution(const DataObject& d)
{
     return new RandomUniformDistribution(d);
}

/**
 * \brief Creates a Region object from the specified DataObject.
 *
 * \param d The DataObject to create this Region object from.
 * \return The newly created Region object.
 */
SimulationObject* SOFactory::createRegion(const DataObject& d)
{
     return new Region(d);
}

/**
 * \brief Creates a ShelterAgencyTeam object from the specified DataObject.
 *
 * \param d The DataObject to create this ShelterAgencyTeam object from.
 * \return The newly created ShelterAgencyTeam object.
 */
SimulationObject* SOFactory::createShelterAgencyTeam(const DataObject& d)
{
     return new ShelterAgencyTeam(d);
}
 
/**
 * \brief Creates a SquarePartitioner object from the specified DataObject.
 *
 * \param d The DataObject to create this SquarePartitioner object from.
 * \return The newly created SquarePartitioner object.
 */
SimulationObject* SOFactory::createSquarePartitioner(const DataObject& d)
{
     return new SquarePartitioner(d);
}
 
/**
 * \brief Creates a TerroristAttackOrder object from the specified DataObject.
 *
 * \param d The DataObject to create this TerroristAttackOrder object from.
 * \return The newly created TerroristAttackOrder object.
 */
SimulationObject* SOFactory::createTerroristAttackOrder(const DataObject& d)
{
     return new TerroristAttackOrder(d);
}
 
/**
 * \brief Creates a UniformDistribution object from the specified DataObject.
 *
 * \param d The DataObject to create this UniformDistribution object from.
 * \return The newly created UniformDistribution object.
 */
SimulationObject* SOFactory::createUniformDistribution(const DataObject& d)
{
     return new UniformDistribution(d);
}

/**
 * \brief Creates a Unit object from the specified DataObject.
 *
 * \param d The DataObject to create this Unit object from.
 * \return The newly created Unit object.
 */
SimulationObject* SOFactory::createUnit(const DataObject& d)
{
     return new Unit(d);
}

/**
 * \brief Creates a WaterAgencyTeam object from the specified DataObject.
 *
 * \param d The DataObject to create this WaterAgencyTeam object from.
 * \return The newly created WaterAgencyTeam object.
 */
SimulationObject* SOFactory::createWaterAgencyTeam(const DataObject& d)
{
     return new WaterAgencyTeam(d);
}
 
