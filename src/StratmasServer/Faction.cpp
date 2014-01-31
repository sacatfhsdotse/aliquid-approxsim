// System
#include <iostream>
#include <vector>

// Own
#include "Buffer.h"
#include "DataObject.h"
#include "Faction.h"
#include "Reference.h"
#include "SOFactory.h"
#include "Type.h"


using namespace std;


// Static Definitions
RefFactionMap Faction::mFactionRefMap;
EthnicFaction* EthnicFaction::sAllFaction(0);
int EthnicFaction::sCurrentIndex(1);
std::map<int, EthnicFaction*> EthnicFaction::mFactionIndexMap;


/**
 * \brief Protected constructor for the All faction.
 */
Faction::Faction()
     : UpdatableSOAdapter(Reference::get(Reference::root(), "Faction ALL")),
       mIndex(EthnicFaction::ALL)
{
     mFactionRefMap[&ref()] = this;
}

/**
 * Constructor that creates a Faction from a DataObject.
 *
 * \param d The DataObject to create this object from.
 */
Faction::Faction(const DataObject& d) : UpdatableSOAdapter(d), mIndex(-1)
{
     // Remove later
     mFactionRefMap[&ref()] = this;

     const vector<DataObject*>& v = d.getChild("enemies")->objects();
     for (vector<DataObject*>::const_iterator it = v.begin(); it != v.end(); it++) {
	  mEnemyList[&(*it)->ref()] = &(*it)->getReference();
     }
     createEnemySet();
}

/**
 * Destructor.
 */
Faction::~Faction()
{
     mFactionRefMap.erase(&ref());
}

/**
 * \brief Stores the enemies of this faction in the
 * mEnemySet. Necessary since two different entries in the enemy list
 * may refer to the same faction.
 */
void Faction::createEnemySet()
{
     mEnemy.clear();
     for (std::map<const Reference*, const Reference*>::iterator it = mEnemyList.begin(); it != mEnemyList.end(); it++) {
	  mEnemy.insert(it->second);
     }
}

/**
 * \brief Extracts data from this object to the Buffer.
 *
 * \param b The Buffer to extract data to.
 */
void Faction::extract(Buffer& b) const 
{
     DataObject& enemyList = *b.map(Reference::get(ref(), "enemies"));
     for (std::map<const Reference*, const Reference*>::const_iterator it = mEnemyList.begin(); it != mEnemyList.end(); it++) {	  
	  if (DataObject* d = enemyList.getChild(it->first->name())) {
	       d->setReference(*it->second);
	  }
	  else {
	       Error e;
	       e << "Enemy '" << it->first->name() << "' in faction '" << ref() << "'' lacks DataObject";
	       throw e;
	  }
     }
}

/**
 * \brief Adds the SimulationObject created from the provided
 * DataObject to this object.
 *
 * \param toAdd The DataObject to create the new SimulationObject from.
 * \param initiator The id of the initiator of the update.
 */
void Faction::addObject(DataObject& toAdd, int64_t initiator)
{
     const Type& type = toAdd.getType();
     if (type.canSubstitute("FactionReference")) {
	  mEnemyList[&toAdd.ref()] = &toAdd.getReference();
	  createEnemySet();
	  SOFactory::createSimple(toAdd, initiator);
     }
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
void Faction::removeObject(const Reference& toRemove, int64_t initiator)
{
     DataObject* d = Mapper::map(toRemove);
     if (!d) {
	  Error e;
	  e << "Tried to remove non existing DataObject '" << toRemove << "' from '" << ref() << "'";
	  throw e;
     }
     const Type& type = d->getType();
     if (type.canSubstitute("FactionReference")) {
	  mEnemyList.erase(&toRemove);
	  createEnemySet();
	  SOFactory::simulationObjectRemoved(toRemove, initiator);
     }
     else {
	  UpdatableSOAdapter::removeObject(toRemove, initiator);
     }
}

/**
 * \brief Replaces the SimulationObject with the same reference as the
 * provided DataObject with a new SimulationObject created from the
 * provided DataObject.
 *
 * \param newObject The DataObject to create the replacing object from.
 * \param initiator The id of the initiator of the update.
 */
void Faction::replaceObject(DataObject& newObject, int64_t initiator)
{
     if (newObject.getType().canSubstitute("FactionReference")) {
	  if (mEnemyList.find(&newObject.ref()) != mEnemyList.end()) {
	       mEnemyList[&newObject.ref()] = &newObject.getReference();
	       SOFactory::simulationObjectReplaced(newObject, initiator);
	       createEnemySet();
	  }
	  else {
	       Error e;
	       e << "Can't replace non existing object '" << newObject.ref() << "'";
	       throw e;
	  }
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
void Faction::modify(const DataObject& d)
{
     if (d.getType().canSubstitute("FactionReference")) {
	  std::map<const Reference*, const Reference*>::iterator it = mEnemyList.find(&d.ref());
	  if (it != mEnemyList.end()) {
	       it->second = &d.getReference();
	       createEnemySet();
	  }
	  else {
	       Error e;
	       e << "Tried to modify non existing element '" << d.identifier() << "' in enemy list.";
	       throw e;
	  }
     }
     else {
	  UpdatableSOAdapter::modify(d);
     }
}

/**
 * \brief Resets this object to the state it would have had if it was
 * created from the provided DataObject.
 *
 * \param d The DataObject to use as source for the reset.
 */
void Faction::reset(const DataObject& d)
{
     // Copy enemy map
     std::map<const Reference*, const Reference*> enems(mEnemyList);
     // Update the ones that already exist, add new ones.
     const vector<DataObject*>& enemData = d.getChild("enemies")->objects();
     for (vector<DataObject*>::const_iterator it = enemData.begin(); it != enemData.end(); it++) {
	  std::map<const Reference*, const Reference*>::iterator it2 = enems.find(&(*it)->ref());
	  if (it2 != enems.end()) {
	       mEnemyList[it2->first] = &(*it)->getReference();
	       enems.erase(it2);
	  }
	  else {
	       addObject(*(*it)->clone(), -1);
	  }
     }
     // Remove the ones that exist in the simulation but not in the reset DataObject.
     for (std::map<const Reference*, const Reference*>::iterator it = enems.begin(); it != enems.end(); it++) {	  
	  removeObject(*it->first, -1);
     }
     createEnemySet();
}



/**
 * \brief Constructor that creates an EthnicFaction from a DataObject.
 *
 * \param d The DataObject to create this object from.
 */
EthnicFaction::EthnicFaction(const DataObject& d) : Faction(d)
{
     mIndex = sCurrentIndex++;
     mFactionIndexMap[mIndex] = this;
}

/**
 * \brief Destructor.
 */
EthnicFaction::~EthnicFaction()
{
     sCurrentIndex--;
     if (index() != ALL) {
	  mFactionIndexMap.erase(index());
     }
}

/**
 * \brief Gets the 'all' faction. If it does not already exist it is
 * created.
 *
 * \return The 'all' faction.
 */
EthnicFaction& EthnicFaction::all()
{
     if (!sAllFaction) {
	  sAllFaction = new EthnicFaction();
     }
     return *sAllFaction;
}
