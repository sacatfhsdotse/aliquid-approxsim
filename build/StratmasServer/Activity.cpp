// System
#include <ostream>

// Own
#include "Action.h"
#include "Activity.h"
#include "Buffer.h"
#include "DataObject.h"
#include "debugheader.h"
#include "Element.h"
#include "Error.h"
#include "Faction.h"
#include "Grid.h"
#include "ProcessVariables.h"
#include "random.h"
#include "Shape.h"
#include "Simulation.h"
#include "SOFactory.h"
#include "Type.h"   // For canSubstitute() in Order::extract()
#include "Unit.h"
#include "Update.h"


/**
 * \brief Default Constructor.
 */
Activity::Activity()
     : UpdatableSOAdapter(Reference::nullRef()), mActive(false)
{
}

/**
 * \brief Creates an Activity from the provided DataObject.
 *
 * \param d The DataObject to use for construction.
 */
Activity::Activity(const DataObject& d)
     : UpdatableSOAdapter(d),
       mActive(d.getChild("active")->getBool()),
       mStart(d.getChild("start")->getTime())
{
}

/**
 * \brief Prepares this SimulationObject for simulation.
 *
 * Should be called after creation and reset and before the simulation
 * starts.
 *
 * \param g The Grid.
 * \param currentTime The current simulation time.
 */
void Activity::prepareForSimulation(Grid& g, Time currentTime)
{
     isActive(currentTime);
}

/**
 * \brief Extracts data from this object to the Buffer.
 *
 * \param b The Buffer to extract data to.
 */
void Activity::extract(Buffer &b) const
{
     DataObject& me = *b.map(ref());
     me.getChild("active")->setBool(mActive);
     me.getChild("start")->setTime(mStart);
}

/**
 * \brief Modifies this object with data from the provided DataObject.
 *
 * \param d The DataObject containing the new value.
 */
void Activity::modify(const DataObject& d)
{
     const string& attr = d.ref().name();
     if (attr == "active") {
	  mActive = d.getBool();
     }
     else if (attr == "start") {
	  mStart = d.getTime();
     }
     else {
	  Error e;
	  e << "No updatable attribute '" << attr << "' in '" << ref() << "'";
	  throw e;
     }
}

/**
 * \brief Resets this object to the state it would have had if it was
 * created from the provided DataObject.
 *
 * \param d The DataObject to use as source for the reset.
 */
void Activity::reset(const DataObject& d)
{
     mActive = d.getChild("active")->getBool();
     mStart = d.getChild("start")->getTime();
}


Order::Order(const DataObject& d)
     : Activity(d),
       mCarriedOut(false),
       mLocation(d.getChild("location") ? d.getChild("location")->getShape() : 0)
{
}

/**
 * \brief Destructor
 */
Order::~Order()
{
     if (mLocation) { delete mLocation; }
}

/**
 * \brief Extracts data from this object to the Buffer.
 *
 * \param b The Buffer to extract data to.
 */
void Order::extract(Buffer &b) const
{
     Activity::extract(b);
     DataObject& me = *b.map(ref());
     // Have to do this hack if we want to represent both
     // CustomPVModification (that has no carriedOut) and area orders
     // (that has carriedOut) with the same class.
     if (me.getType().canSubstitute("Order")) {
	  me.getChild("carriedOut")->setBool(mCarriedOut);
     }
     if (mLocation) {
	  DataObject* d = me.getChild("location");
	  if (d) {
	       d->setShape(mLocation);
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
void Order::addObject(DataObject& toAdd, int64_t initiator)
{
     const Type& type = toAdd.getType();
     if (type.canSubstitute("Shape")) {
	  SOFactory::createSimple(toAdd, initiator);
	  mLocation = toAdd.getShape();
     }
     else {
	  Activity::addObject(toAdd, initiator);
     }
}

/**
 * \brief Removes the SimulationObject referenced by the provided
 * Reference from this object.
 *
 * \param toRemove The Reference to the object to remove.
 * \param initiator The id of the initiator of the update.
 */
void Order::removeObject(const Reference& toRemove, int64_t initiator)
{
     DataObject* d = Mapper::map(toRemove);
     if (!d) {
	  Error e;
	  e << "Tried to remove non existing DataObject '" << toRemove << "' from '" << ref() << "'";
	  throw e;
     }
     const Type& type = d->getType();
     if (type.canSubstitute("Shape")) {
	  delete mLocation;
	  mLocation = 0;
	  SOFactory::simulationObjectRemoved(toRemove, initiator);
     }
     else {
	  Activity::removeObject(toRemove, initiator);
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
void Order::replaceObject(DataObject& newObject, int64_t initiator)
{
     const string& attr = newObject.identifier();
     if (attr == "location") {
	  delete mLocation;
	  mLocation = newObject.getShape();
	  SOFactory::simulationObjectReplaced(newObject, initiator);
     }
     else {
	  Activity::replaceObject(newObject, initiator);
     }
}

/**
 * \brief Modifies this object with data from the provided DataObject.
 *
 * \param d The DataObject containing the new value.
 */
void Order::modify(const DataObject& d)
{
     const string& attr = d.ref().name();
     if (attr == "carriedOut") {
	  mCarriedOut = d.getBool();
     }
     else if (attr == "location") {
	  delete mLocation;
	  mLocation = d.getShape();
     }
     else {
	  Activity::modify(d);
     }
}

/**
 * \brief Resets this object to the state it would have had if it was
 * created from the provided DataObject.
 *
 * \param d The DataObject to use as source for the reset.
 */
void Order::reset(const DataObject& d)
{
     Activity::reset(d);
     // Hack in order to be able to use CustomPV for both ActorBased
     // and ActorLess activities.
     if (DataObject* o = d.getChild("carriedOut")) {
	  mCarriedOut = o->getBool();
     }
     if (DataObject* o = d.getChild("location")) {
	  if (mLocation) {
	       delete mLocation;
	       mLocation = o->getShape();
//	       replaceObject(*o, -1);
	  }
	  else {
	       addObject(*o->clone(), -1);
	  }
     }
     else if (mLocation) {
	  removeObject(Reference::get(ref(), "location"), -1);
     }
}


/**
 * \brief Creates a CustomPVModification object from the provided
 * DataObject.
 *
 * \param d The DataObject to use for construction.
 */
CustomPVModification::CustomPVModification(const DataObject& d)
     : Order(d),
       mGrid(0),
       mEnd(d.getChild("end")->getTime()),
       mTarget(d.getChild("target") ? &d.getChild("target")->getReference() : 0),
       mType(d.getType().getName())
{
     if (mType == "PresenceOrder") {
	  mCombatFactor = 1;
     }
     else if (mType == "ControlOrder") {
	  mCombatFactor = 0.8;
     }
     else if (mType == "SecureOrder") {
	  mCombatFactor = 0.6;
     }
     else if (mType == "FreedomOfMovementOrder") {
	  mCombatFactor = 0.4;
     }
     else if (mType == "ProvideCivilianFunctionsOrder") {
	  mCombatFactor = 0.2;
     }
     else if (mType == "CustomAreaOrder") {
	  mCombatFactor = 0.2;
     }
     else if (mType == "CustomPVModification") {
	  mCombatFactor = 0;
     }
     else if (mType == "TerroristAttackOrder") {
	  mCombatFactor = 0;
     }
     else {
	  Error e;
	  e << "Unknown type of order: " << mType;
	  throw e;
     }
     for (int i = 0; i < kNumModifiable; i++) {
	  DataObject* elem = d.getChild(PVHelper::modifiablePVName(i));
	  if (elem) {
	       mSeverities[PVHelper::modifiablePVName(i)] = elem->getDouble();
	  }
     }
}

void CustomPVModification::prepareForSimulation(Grid& g, Time currentTime)
{
     Activity::prepareForSimulation(g, currentTime);
     mGrid = &g;
     createEffects();
}

/**
 * \brief Creates the effects of this Activity from the mSeverities
 * map.
 */
void CustomPVModification::createEffects() {
     // Get the modifiers. Test for all modifiable PV:s and create a modifier
     // for the ones that are found
     EthnicFaction *targetFaction;
     if (mTarget) {
	  targetFaction = EthnicFaction::faction(*mTarget);
     }
     else {
	  targetFaction = &EthnicFaction::all();
     }
     
     if (!targetFaction) {
	  Error e;
	  e << "Tried to create effects in Activity but couldn't map the Reference '" << *mTarget;
	  e << "' to any ethnic faction.\nStratmas does not yet support other targets for Activities";
	  throw e;
     }

     mEffects.clear();
     for (std::map<string, double>::iterator it = mSeverities.begin(); it != mSeverities.end(); it++) {
	  addEffect(PVHelper::nameToOverAllOrder(it->first), it->second, targetFaction);
     }
}

/**
 * \brief Performs this Activity.
 *
 * \param e The Element that should perform this Activity.
 */
void CustomPVModification::perform(Element *e, double fraction)
{
     const Shape* areaToUse = 0;
     if (e) {
	  Unit* u = dynamic_cast<Unit*>(e);
	  if (u && location() && u->location().cenCoord() != location()->cenCoord() && u->capable()) {
	       if (u->goal()->cenCoord() != location()->cenCoord()) {
		    u->setGoal(*location());
	       }
	       u->setVelocity();
	       u->move();
	       return;
	  }
	  else if (u && u->criticalInsurgentSituation()) {
	       // Don't modify any PV if we're fighting insurgents.
	       return;
	  }
	  areaToUse = &e->location();
     }
     else if (location()) {
	  areaToUse = location();
     }
     else {
	  Error e;
	  e << "No area or performer specified for CustomPVModification " << ref();
	  throw e;
     }
     GridAction ga(*mGrid, *areaToUse, e, mEffects, fraction * (1 - combatFactor()));
     ga.carryOut();
}

/**
 * \brief Extracts data from this object to the Buffer.
 *
 * \param b The Buffer to extract data to.
 */
void CustomPVModification::extract(Buffer& b) const
{
     Order::extract(b);
     DataObject& me = *b.map(ref());
     me.getChild("end")->setTime(mEnd);
     if (mTarget) {
	  me.getChild("target")->setReference(*mTarget);
     }
     for (std::map<string, double>::const_iterator it =  mSeverities.begin(); it != mSeverities.end(); it++) {
	  me.getChild(it->first)->setDouble(it->second);
     }
}

/**
 * \brief Adds the SimulationObject created from the provided
 * DataObject to this object.
 *
 * \param toAdd The DataObject to create the new SimulationObject from.
 * \param initiator The id of the initiator of the update.
 */
void CustomPVModification::addObject(DataObject& toAdd, int64_t initiator)
{
     const Type& type = toAdd.getType();
     if (type.canSubstitute("EthnicFactionReference")) {
	  SOFactory::createSimple(toAdd, initiator);
	  mTarget = &toAdd.getReference();
     }
     else if (type.canSubstitute("Double")) {
	  SOFactory::createSimple(toAdd, initiator);
	  mSeverities[toAdd.identifier()] = toAdd.getDouble();
     }
     else {
	  Order::addObject(toAdd, initiator);
     }
     createEffects();
}

/**
 * \brief Removes the SimulationObject referenced by the provided
 * Reference from this object.
 *
 * \param toRemove The Reference to the object to remove.
 * \param initiator The id of the initiator of the update.
 */
void CustomPVModification::removeObject(const Reference& toRemove, int64_t initiator)
{
     DataObject* d = Mapper::map(toRemove);
     if (!d) {
	  Error e;
	  e << "Tried to remove non existing DataObject '" << toRemove << "' from '" << ref() << "'";
	  throw e;
     }
     const Type& type = d->getType();
     if (type.canSubstitute("EthnicFactionReference")) {
	  mTarget = 0;
	  SOFactory::simulationObjectRemoved(toRemove, initiator);
     }
     else if (type.canSubstitute("Double")) {
	  mSeverities.erase(toRemove.name());
	  SOFactory::simulationObjectRemoved(toRemove, initiator);
     }
     else {
	  Order::removeObject(toRemove, initiator);
     }
     createEffects();
}

/**
 * \brief Modifies this object with data from the provided DataObject.
 *
 * \param d The DataObject containing the new value.
 */
void CustomPVModification::modify(const DataObject& d)
{
     const string& attr = d.identifier();
     if (attr == "end") {
	  mEnd = d.getTime();
     }
     else if (attr == "target") {
	  mTarget = &d.getReference();
     }
     else if (mSeverities.find(attr) != mSeverities.end()) {
	  mSeverities[attr] = d.getDouble();
     }
     else {
	  Order::modify(d);
     }
     createEffects();
}

/**
 * \brief Resets this object to the state it would have had if it was
 * created from the provided DataObject.
 *
 * \param d The DataObject to use as source for the reset.
 */
void CustomPVModification::reset(const DataObject& d)
{
     Order::reset(d);
     mEnd = d.getChild("end")->getTime();
     mTarget = (d.getChild("target") ? &d.getChild("target")->getReference() : 0);

     // Copy severities map
     std::map<std::string, double> sev(mSeverities);
     // Update the ones that already exist, add new ones.
     for (int i = 0; i < kNumModifiable; i++) {
	  DataObject* elem = d.getChild(PVHelper::modifiablePVName(i));
	  if (elem) {
	       if (sev.find(elem->identifier()) != sev.end()) {
		    mSeverities[elem->identifier()] = elem->getDouble();
		    sev.erase(elem->identifier());
	       }
	       else {
		    SOFactory::createSimple(*elem->clone());
	       }
	  }
     }
     // Remove the ones that exist in the simulation but not in the reset DataObject.
     for (std::map<std::string, double>::iterator it = sev.begin(); it != sev.end(); it++) {
	  mSeverities.erase(it->first);
	  SOFactory::simulationObjectRemoved(Reference::get(ref(), it->first), -1);
     }
}

/**
 * \brief For debugging purposes.
 *
 * \param o The stream to write to.
 * \param a The activity to print.
 * \return The provided ostream with the Activity written to it.
 */
std::ostream &operator << (std::ostream& o, const CustomPVModification& a)
{
     o << "Start:  " << a.startTime() << std::endl;
     o << "End:    " << a.endTime() << std::endl;
     if (a.mTarget) {
	  o << "Target: " << *a.mTarget << endl;
     }
     if (a.location()) {
	  o << "Location:" << endl;
	  a.location()->toXML(o) << endl;
     }
     o << "Effects:";
     for(std::vector<GridEffect>::const_iterator it = a.mEffects.begin(); it != a.mEffects.end(); it++) {
// 	  o << std::endl << "Attribute: " << PVHelper::attrName(it->mAttr);
// 	  o << " -- type: " << PVHelper::attrType(it->mAttr) << " -- severity: " << it->mSeverity;
// 	  o << ", faction: " << it->mFaction->ref();
     }
     return o;
}



/**
 * \brief Creates a TerroristAttackOrder object from the provided
 * DataObject.
 *
 * \param d The DataObject from which to create this object.
 */
TerroristAttackOrder::TerroristAttackOrder(const DataObject& d)
     : CustomPVModification(d), mActionTime(d.getChild("actionTime")->getTime()),
       mTimeToPerform(false), mAttackPerformed(false)
{
}

/**
 * \brief Extracts data from this object to the Buffer.
 *
 * \param b The Buffer to extract data to.
 */
void TerroristAttackOrder::extract(Buffer &b) const
{
     CustomPVModification::extract(b);
     DataObject& me = *b.map(ref());
     me.getChild("actionTime")->setTime(mActionTime);
}

/**
 * \brief Modifies this object with data from the provided DataObject.
 *
 * \param d The DataObject containing the new value.
 */
void TerroristAttackOrder::modify(const DataObject& d)
{
     const string& attr = d.identifier();
     if (attr == "actionTime") {
	  mActionTime = d.getTime();
     }
     else {
	  CustomPVModification::modify(d);
     }
}

/**
 * \brief Resets this object to the state it would have had if it was
 * created from the provided DataObject.
 *
 * \param d The DataObject to use as source for the reset.
 */
void TerroristAttackOrder::reset(const DataObject& d)
{
     mTimeToPerform = false;
     mAttackPerformed = false;

     CustomPVModification::reset(d);
     mActionTime = d.getChild("actionTime")->getTime();
}

/**
 * \brief Performs this Activity.
 *
 * \param e The Element that should perform this Activity.
 * \param fraction The fraction of the performers total capacity that
 * this activity is performed with.
 */
void TerroristAttackOrder::perform(Element *e, double fraction)
{
     if (Unit* u = dynamic_cast<Unit*>(e)) {
	  if (location() && u->location().cenCoord() != location()->cenCoord() && u->capable()) {
	       if (u->goal()->cenCoord() != location()->cenCoord()) {
		    u->setGoal(*location());
	       }
	       u->setVelocity();
	       u->move();
	       return;
	  }
	  else if (mTimeToPerform && !mAttackPerformed && !u->isSpotted()) {
	       GridAction ga(*mGrid, (location() ? *location() : u->location()), e, mEffects, fraction);
	       ga.carryOut();
	       mAttackPerformed = true;
	  }
     }
     else {
	  Error e;
	  e << "No performer specified for TerroristAttackOrder " << ref();
	  throw e;
     }
}



/**
 * \brief Performs this Activity.
 *
 * \param e The Element that should perform this Activity.
 * \param fraction The fraction of the performers total capacity that
 * this activity is performed with.
 */
void AttackOrder::perform(Element *e, double fraction)
{
     Unit* u = dynamic_cast<Unit*>(e);
     if (u && u->capable()) {
	  if (u->combatSituation()) {
	       u->combat();
	  }
	  else if (location() && u->center() != location()->cenCoord()) {
	       mCarriedOut = false;
	       if (u->goal()->cenCoord() != location()->cenCoord()) {
		    u->setGoal(*location());
	       }
	       u->setVelocity();
	       u->move();
	  }
	  else {
	       mCarriedOut = !u->combat();
	  }
     }
}



/**
 * \brief Creates a DefendOrder object from the provided DataObject.
 *
 * \param d The DataObject from which to create this object.
 */
DefendOrder::DefendOrder(const DataObject& d)
     : Order(d), mEnd(d.getChild("end")->getTime())
{
}



/**
 * \brief Performs this Activity.
 *
 * \param e The Element that should perform this Activity.
 * \param fraction The fraction of the performers total capacity that
 * this activity is performed with.
 */
void DefendOrder::perform(Element *e, double fraction)
{
     Unit* u = dynamic_cast<Unit*>(e);
     if (u && u->capable()) {
	  if (location() && u->center() != location()->cenCoord()) {
	       mCarriedOut = false;
	       if (u->goal()->cenCoord() != location()->cenCoord()) {
		    u->setGoal(*location());
	       }
	       u->setVelocity();
	       u->move();
	  }
	  else {
	       if (u->combatSituation()) {
		    u->combat();
	       }
	  }
     }
}

/**
 * \brief Extracts data from this object to the Buffer.
 *
 * \param b The Buffer to extract data to.
 */
void DefendOrder::extract(Buffer &b) const
{
     Order::extract(b);
     DataObject& me = *b.map(ref());
     me.getChild("end")->setTime(mEnd);
}

/**
 * \brief Modifies this object with data from the provided DataObject.
 *
 * \param d The DataObject containing the new value.
 */
void DefendOrder::modify(const DataObject& d)
{
     const string& attr = d.identifier();
     if (attr == "end") {
	  mEnd = d.getTime();
     }
     else {
	  Order::modify(d);
     }
}

/**
 * \brief Resets this object to the state it would have had if it was
 * created from the provided DataObject.
 *
 * \param d The DataObject to use as source for the reset.
 */
void DefendOrder::reset(const DataObject& d)
{
     Order::reset(d);
     mEnd = d.getChild("end")->getTime();
}



/**
 * \brief Creates an AmbushOrder object from the provided DataObject.
 *
 * \param d The DataObject from which to create this object.
 */
AmbushOrder::AmbushOrder(const DataObject& d)
     : Order(d), mState(eHide), mTimeForAmbush(false), mOneAmbushPerformed(false),
       mEnd(d.getChild("end")->getTime()),
       mStartAmbush(d.getChild("startAmbush")->getTime()),
       mEndAmbush(d.getChild("endAmbush")->getTime())
{
}

/**
 * \brief Performs this Activity.
 *
 * \param e The Element that should perform this Activity.
 * \param fraction The fraction of the performers total capacity that
 * this activity is performed with.
 */
void AmbushOrder::perform(Element *e, double fraction)
{
     Unit* u = dynamic_cast<Unit*>(e);
     if (u && u->capable()) {
	  // Move to the correct position.
	  if (location() && u->center() != location()->cenCoord()) {
	       mState = eHide;
	       if (u->goal()->cenCoord() != location()->cenCoord()) {
		    u->setGoal(*location());
	       }
	       u->setVelocity();
	       u->move();
	  }
	  else if (state() == eAmbush) {
	       mOneAmbushPerformed = true;
	  }
	  // The actual ambush is handled in Unit.cpp...
     }
}

/**
 * \brief Extracts data from this object to the Buffer.
 *
 * \param b The Buffer to extract data to.
 */
void AmbushOrder::extract(Buffer &b) const
{
     Order::extract(b);
     DataObject& me = *b.map(ref());
     me.getChild("end")->setTime(mEnd);
     me.getChild("startAmbush")->setTime(mStartAmbush);
     me.getChild("endAmbush")->setTime(mEndAmbush);
}

/**
 * \brief Modifies this object with data from the provided DataObject.
 *
 * \param d The DataObject containing the new value.
 */
void AmbushOrder::modify(const DataObject& d)
{
     const string& attr = d.identifier();
     if (attr == "end") {
	  mEnd = d.getTime();
     }
     else if (attr == "startAmbush") {
	  mStartAmbush = d.getTime();
     }
     else if (attr == "endAmbush") {
	  mEndAmbush = d.getTime();
     }
     else {
	  Order::modify(d);
     }
}

/**
 * \brief Resets this object to the state it would have had if it was
 * created from the provided DataObject.
 *
 * \param d The DataObject to use as source for the reset.
 */
void AmbushOrder::reset(const DataObject& d)
{
     mState = eHide;
     mTimeForAmbush = false;
     mOneAmbushPerformed = false;

     Order::reset(d);
     mEnd = d.getChild("end")->getTime();
     mStartAmbush = d.getChild("startAmbush")->getTime();
     mEndAmbush = d.getChild("endAmbush")->getTime();
}

/**
 * \brief Checks if this activity is active at time t.
 *
 * \param t The time for which to check.
 * \return True if this activity is active at the specified time.
 */
bool AmbushOrder::isActive(Time t) {
     // Since the decision whether to do ambush or not is taken before
     // isActive is called for a time step we have to calculate with a
     // simulation time that is one step ahead in order to get the
     // order to start and stop at the correct time.
     Time actualTime = t + Simulation::timestep();
     mCarriedOut = (actualTime > mEnd);
     if (oneAmbush() && !mOneAmbushPerformed) {
	  mTimeForAmbush = actualTime > mStartAmbush;
     } 
     else {
	  mTimeForAmbush = (actualTime > mStartAmbush && actualTime <= mEndAmbush);
     }
     return mActive = (actualTime > mStart && actualTime <= mEnd);
}



/**
 * \brief Constructor used internally by the server
 *
 * \param location The location to go to.
 */
GoToOrder::GoToOrder(const Shape& location)
{
     mLocation = location.clone();
}

/**
 * \brief Performs this Activity.
 *
 * \param e The Element that should perform this Activity.
 * \param fraction The fraction of the performers total capacity that
 * this activity is performed with.
 */
void GoToOrder::perform(Element *e, double fraction)
{
     Unit* u = dynamic_cast<Unit*>(e);
     if (u && u->capable()) {
	  if (u->center() != location()->cenCoord()) {
	       mCarriedOut = false;
	       if (u->goal()->cenCoord() != location()->cenCoord()) {
		   u->setGoal(*location());
	       }
	       u->setVelocity();
	       u->move();
	  }
	  else {
	       mCarriedOut = true;
	  }
     }
}



/**
 * \brief Performs this Activity.
 *
 * \param e The Element that should perform this Activity.
 * \param fraction The fraction of the performers total capacity that
 * this activity is performed with.
 */
void RetreatOrder::perform(Element *e, double fraction)
{
     Unit* u = dynamic_cast<Unit*>(e);
     if (u) {
	  if (mState == eRetreat) {
	       if (u->location().cenCoord() != location()->cenCoord()) {
		    mCarriedOut = false;
		    if (u->goal()->cenCoord() != location()->cenCoord()) {
			 u->setGoal(*location());
		    }
		    u->setVelocity();
		    u->move();
	       }
	       else {
		    mState = eReturn;
	       }
	  }
	  else if (mState == eReturn) {
	       Unit *parent = findClosestParentWithSameFaction(*u);
	       if (parent &&
		   parent->faction().ref() == u->faction().ref() &&
		   u->location().cenCoord() != parent->location().cenCoord()) {
		    if (u->goal()->cenCoord() != parent->location().cenCoord()) {
			 u->setGoal(parent->location());
		    }
		    u->setVelocity(0.25);
		    u->move();
	       }
	       else {
		    mCarriedOut = true;
	       }
	  }
     }
}


Unit* RetreatOrder::findClosestParentWithSameFaction(Unit& u)
{
     Unit* parent = u.parent();

     while (parent != 0) {
	  if (parent->faction().ref() == u.faction().ref()) {
	       break;
	  }
	  parent = parent->parent();
     }
     return parent;
}
