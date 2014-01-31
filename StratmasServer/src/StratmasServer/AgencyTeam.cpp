// System
#include <list>
#include <ostream>

// Own
#include "Action.h"
#include "AgencyTeam.h"
#include "Agency.h"
#include "Buffer.h"
#include "Camp.h"
#include "CellGroup.h"
#include "DataObject.h"
#include "debugheader.h"
#include "Faction.h"
#include "GridCell.h"
#include "Grid.h"
#include "LogStream.h"
#include "ProcessVariables.h"
#include "random.h"
#include "Reference.h"
#include "Shape.h"
#include "SOFactory.h"
#include "StratmasConstants.h"
#include "Type.h"


/**
 * \brief Constructor that creates an AgencyTeam from the provided
 * DataObject.
 *
 * \param d The DataObject to create this object from.
 */
AgencyTeam::AgencyTeam(const DataObject&d)
     : Element(d),
       mCamp(0),
       mGoal(location().cenCoord()),
       mCapacityPPD(d.getChild("capacityPPD")->getDouble()),
       mResponseTimeSecs(d.getChild("responseTimeSecs")->getDouble()),
       mHasStartTime(false),
       mDeployed(false),
       mDeparted(false)
{
     DataObject* child;
     child = d.getChild("violenceThreshold");
     mViolenceThreshold = (child ? child->getDouble() : 40);
     child = d.getChild("deployTime");
     mDeployTime = (child ? child->getTime() : Time::minTime());
     child = d.getChild("departTime");
     mDepartTime = (child ? child->getTime() : Time::maxTime());
     child = d.getChild("ownInitiative");
     mOwnInitiative = (child ? child->getBool() : true);
}

/**
 * \brief Checks if the team can go to the given location, e.g. if the
 * violence level is low enough.
 *
 * \param goal The goal.
 * \return True if the team can go, false otherwise.
 */
bool AgencyTeam::canWorkAt(Shape& loc) const
{
     CellGroup grp(*mGridDataHandler);
     list<GridPos> l;
     loc.toProj(*Projection::mCurrent);
     loc.cells(*mGrid, l);
     for (list<GridPos>::iterator it = l.begin(); it != l.end(); ++it) {
          grp.addMember(mGrid->cell(*it));
     }
     grp.update();
     return (grp.pvfGet(eViolence) <= mViolenceThreshold);
}

/**
 * \brief Deploys the team.
 */
void AgencyTeam::deploy()
{
     mDeployed = true;
     setStartTime(mDeployTime + Time(0, 0, 0, Poisson(mResponseTimeSecs)));
}

/**
 * \brief Extracts data from this object to the Buffer.
 *
 * \param b The Buffer to extract data to.
 */
void AgencyTeam::extract(Buffer &b) const
{
     Element::extract(b);
     DataObject& me = *b.map(ref());
     me.getChild("capacityPPD")->setDouble(getCapacity());
     me.getChild("responseTimeSecs")->setDouble(getResponseTime());

     DataObject* child;
     child = me.getChild("violenceThreshold");
     if (child) {
	  child->setDouble(mViolenceThreshold);
     }
     child = me.getChild("deployTime");
     if (child) {
	  child->setTime(mDeployTime);
     }
     child = me.getChild("departTime");
     if (child) {
	  child->setTime(mDepartTime);
     }
     child = me.getChild("ownInitiative");
     if (child) {
	  child->setBool(mOwnInitiative);
     }
}

/**
 * \brief Adds the SimulationObject created from the provided
 * DataObject to this object.
 *
 * \param toAdd The DataObject to create the new SimulationObject from.
 * \param initiator The id of the initiator of the update.
 */
void AgencyTeam::addObject(DataObject& toAdd, int64_t initiator)
{
     string attr = toAdd.identifier();
     if (attr == "violenceThreshold") {
	  SOFactory::createSimple(toAdd, initiator);
	  mViolenceThreshold = toAdd.getDouble();
     }
     else if (attr == "deployTime") {
	  SOFactory::createSimple(toAdd, initiator);
	  mDeployTime = toAdd.getTime();
     }
     else if (attr == "departTime") {
	  SOFactory::createSimple(toAdd, initiator);
	  mDepartTime = toAdd.getTime();
     }
     else if (attr == "ownInitiative") {
	  SOFactory::createSimple(toAdd, initiator);
	  mOwnInitiative = toAdd.getBool();
     }
     else {
	  Element::addObject(toAdd, initiator);
     }
}

/**
 * \brief Removes the SimulationObject referenced by the provided
 * Reference from this object.
 *
 * \param toRemove The Reference to the object to remove.
 * \param initiator The id of the initiator of the update.
 */
void AgencyTeam::removeObject(const Reference& toRemove, int64_t initiator)
{
     DataObject* d = Mapper::map(toRemove);
     if (!d) {
	  Error e;
	  e << "Tried to remove non existing DataObject '" << toRemove << "' from '" << ref() << "'";
	  throw e;
     }
     string attr = d->identifier();
     if (attr == "violenceThreshold" || attr == "deployTime" ||
	 attr == "departTime" || attr == "ownInitiative") {
	  SOFactory::simulationObjectRemoved(toRemove, initiator);
     }
     else {
	  Element::removeObject(toRemove, initiator);
     }
}

/**
 * \brief Modifies this object with data from the provided DataObject.
 *
 * \param d The DataObject containing the new value.
 */
void AgencyTeam::modify(const DataObject& d)
{
     const string& attr = d.ref().name();
     if (attr == "capacityPPD") {
	  mCapacityPPD = d.getDouble();
	  mAgency->aggregateCapacity();
     }
     else if (attr == "responseTimeSecs") {
	  mResponseTimeSecs = d.getDouble();
	  mAgency->aggregateCapacity();
     }
     else if (attr == "violenceThreshold") {
	  mViolenceThreshold = d.getDouble();
     }
     else if (attr == "deployTime") {
	  mDeployTime = d.getTime();
     }
     else if (attr == "departTime") {
	  mDepartTime = d.getTime();
     }
     else if (attr == "ownInitiative") {
	  mOwnInitiative = d.getBool();
     }
     else {
	  Element::modify(d);
     }
}

/**
 * \brief Resets this object to the state it would have had if it was
 * created from the provided DataObject.
 *
 * \param d The DataObject to use as source for the reset.
 */
void AgencyTeam::reset(const DataObject& d)
{
     mCamp = 0;
     mHasStartTime = false;
     mDeployed = false;
     mDeparted = false;

     Element::reset(d);
     mCapacityPPD = d.getChild("capacityPPD")->getDouble();
     mResponseTimeSecs = d.getChild("responseTimeSecs")->getDouble();

     DataObject* child;
     child = d.getChild("violenceThreshold");
     mViolenceThreshold = (child ? child->getDouble() : 40);
     child = d.getChild("deployTime");
     mDeployTime = (child ? child->getTime() : Time::minTime());
     child = d.getChild("departTime");
     mDepartTime = (child ? child->getTime() : Time::maxTime());
     child = d.getChild("ownInitiative");
     mOwnInitiative = (child ? child->getBool() : true);
}

/**
 * \brief Mutator for the team's goal.
 *
 * \param goal The team's goal.
 */
void AgencyTeam::setGoal(LatLng goal)
{
     mGoal = goal;
     mCamp = 0;

     if (!mOwnInitiative || !present()) {
	  // No teleport :)
	  mGoal = location().cenCoord();
     }

     // Temporary Teleport     
     mLocation->move(mGoal);
}

/**
 * \brief Mutator for the team's goal. Sets this teams goal to
 * the camp pointed to by the pointer camp
 *
 * \param camp The Camp.
 */
void AgencyTeam::setGoal(Camp *camp)
{
     if (camp) {
	  mCamp = camp;
	  mGoal = camp->center();
     }
}

/**
 * \brief For debugging purposes.
 *
 * \param o The ostream to write to.
 * \param a The AgencyTeam to write.
 * \return The provided ostream with the AgencyTeam written to it.
 */
ostream& operator << (std::ostream& o, const AgencyTeam& a)
{
     o << "capacityPPD: " << a.getCapacity() << endl;
     o << "responseTimeSecs: " << a.getResponseTime() << endl;
     return o;
}



/**
 * \brief Calculates food need among the population in this team's
 * area of influence.
 *
 * Food need is calculated as follows: For all cells in this team's
 * area of influence with population p > 0.5 persons where the amount
 * of stored food f is less than one day's consumption - add (1 - f) *
 * p to the total need.
 *
 * \return The need for food in this team's area of influence
 * represented as person-days.
 */
double FoodAgencyTeam::calculateNeed()
{
     double need = 0;
     list<GridPos> cells;
     list<GridPos>::iterator it;
     location().cells(*mGrid, cells);
     
     //	Iterate through nearby cells, adding up the need:
     for (it = cells.begin(); it != cells.end(); it++) {
	  GridCell* c = mGrid->cell(*it);
	  if (!c || c->pvfGet(ePopulation) < kMinPopulation || c->pvGet(eFoodDays) > 1.0) {
	       continue;
	  }
	       
	  // Accumulate the person-days of food needed in ppd:
	  need += (1.0 - c->pvGet(eFoodDays)) * c->pvfGet(ePopulation);
     }
     return need;
}

/**
 * \brief Distribute food in this team's area of influence according
 * to its capacity.
 *
 * \param now The current simulation time.
 */
void FoodAgencyTeam::act(Time now)
{
     if (!canWorkAt(*mLocation)) {
	  return;
     }
     // Move - Teleport for now...
     mLocation->move(mGoal);
//     mLat = mGoal.lat();
//     mLng = mGoal.lng();
//     debug("FoodTeam has location (lat, lng): " << location().center().lat() << ", " << location().center().lng());

     // Do our thing...
     list<GridPos> cells;
     list<GridPos>::iterator it;
     location().cells(*mGrid, cells);
     
     double sumServed = 0;
     for (it = cells.begin(); it != cells.end(); it++) {
	  GridCell *c = mGrid->cell(*it);
	  if (!c || c->pvfGet(ePopulation) < kMinPopulation || c->pvGet(eFoodDays) > 1.0) {
	       continue;
	  }
	  // Calculate the food needed in this cell:
	  double cellNeed = (1.0 - c->pvGet(eFoodDays)) * c->pvfGet(ePopulation);   // Calculated twice!
	  
	  // Supply food until it is exhausted:
	  if (sumServed < mCapacityPPD) {
	       double personsSupplied = min(cellNeed, mCapacityPPD - sumServed);
	       double daysSupplied = personsSupplied / c->pvfGet(ePopulation);
	       c->pvAddR(eFoodDays, daysSupplied);

	       sumServed += personsSupplied;
	  }
     }
}



/**
 * \brief Calculates water need among the population in this team's
 * area of influence.
 *
 * Water need is calculated as follows: For all cells in this team's
 * area of influence with population p > 0.5 persons - add f * p
 * (where f is the fraction of the population that does not have
 * water) to the total need.
 *
 * \return The need for water in this team's area of influence
 * represented as persons without water.
 */
double WaterAgencyTeam::calculateNeed()
{
     double need = 0;
     list<GridPos> cells;
     list<GridPos>::iterator it;
     location().cells(*mGrid, cells);
     
     //	Iterate through nearby cells, adding up the need:
     for (it = cells.begin(); it != cells.end(); it++) {
	  GridCell *c = mGrid->cell(*it);
	  if (!c || c->pvfGet(ePopulation) < kMinPopulation) {
	       continue;
	  }
	       
	  // Accumulate the number of people without water
	  need += c->pvGet(eFractionNoWater) * c->pvfGet(ePopulation);
     }
     return need;
}

/**
 * \brief Distribute water in this team's area of influence according
 * to its capacity.
 *
 * \param now The current simulation time.
 */
void WaterAgencyTeam::act(Time now)
{
     if (!canWorkAt(*mLocation)) {
	  return;
     }
     // Move - Teleport for now...
     mLocation->move(mGoal);
//     debug("WaterTeam has location (lat, lng): " << location().center().lat() << ", " << location().center().lng());

     // Do our thing...
     list<GridPos> cells;
     list<GridPos>::iterator it;
     location().cells(*mGrid, cells);
     
     double sumServed = 0;
     for (it = cells.begin(); it != cells.end(); it++) {
	  GridCell *c = mGrid->cell(*it);
	  if (!c || c->pvfGet(ePopulation) < kMinPopulation) {
	       continue;
	  }
	  //	Repair water infrastructure:
	  double bestCap = c->bestWaterCapacity();
	  double delta = bestCap - c->pvGet(eWaterDays);
	  if (delta > 0) {
	       //	10% capacity is repaired each day
	       c->pvAddR(eWaterDays, 0.10 * delta * mRepairCapacity);
	  }

	  //	Supply water:
	  if (sumServed < mCapacityPPD) {
	       double thirsty = c->pvGet(eFractionNoWater) * c->pvfGet(ePopulation);
	       if (thirsty > 1.0) {
		    double popServed = min(thirsty, mCapacityPPD);
		    c->pvSetR(eSuppliedWater, popServed);
		    sumServed += popServed;
	       }
	  }
     }
}



/**
 * \brief Performs this team's actions.
 *
 * A ShelterAgencyTeam's action pattern is as follows: When it becomes
 * operational, it builds a Camp at its current location. For all
 * following days the team moves all of the displaced, unsheltered
 * people in the cell where the camp is located into the camp untilthe
 * team's - and thereby the camp's - capcaity limit is met.
 *
 * \param now The current simulation time.
 */
void ShelterAgencyTeam::act(Time now)
{
     if (!canWorkAt(*mLocation)) {
	  return;
     }
     if (!operational()) {
	  return;   // We're not operational yet so let's return
     }
     else if (!mCamp) {
	  // Move - Teleport for now...
	  mLocation->move(mGoal);
//	  debug("ShelterTeam has location (lat, lng): " << location().center().lat() << ", " << location().center().lng());

	  // We've been ordered to build a camp but we haven't started yet so
	  // let's start building.
	  mCamp = new Camp(Reference::get(ref(), "camp"), location(), mGrid->factions());
	  debug("%%%%% Building Camp at location: " << mCamp->center().lat() << ", " << mCamp->center().lng());
	  GridCell *ccc = mGrid->cell(mCamp->center());
	  if (ccc) {
	       mGrid->notifyAboutCamp(mCamp);
	       debug("Row, col: " << ccc->row() << ", " << ccc->col());
	  }
	  else {
	       slog << "NULL cell for Camp in ShelterAgencyTeam::act()" << logEnd;
	  }
     }
     else if (now >= mStartTime) {
	  GridCell *c = mGrid->cell(center());
	  if (!c || c->pvfGet(ePopulation) < kMinPopulation) {
	       return;
	  }
	  
	  double noShelter = c->pvfGet(eDisplaced) - c->pvfGet(eSheltered);
	  if (noShelter > kMinPopulation && mCapacityPPD > mCamp->population()) {
	       //	Move some people into this camp:
	       double toBeSheltered = min(mCapacityPPD - mCamp->population(), noShelter);
	       double pSheltered = toBeSheltered / noShelter;
	       double newSheltered = 0;
	       for (int i = 1; i < mGrid->factions() + 1; i++) {
		    double nSheltered = pSheltered * (c->pvfGet(eDisplaced, i) - c->pvfGet(eSheltered, i));
		    c->pvfAddR(eSheltered, i, nSheltered);

		    newSheltered += nSheltered;
		    mCamp->addPopulation(nSheltered, i);
	       }
	       // Index 0 is the total
	       c->pvfAddR(eSheltered, 0, newSheltered);
	       mCamp->addPopulation(newSheltered, 0);
	  }
	  else if (mCamp->population() > mCapacityPPD) {
	       //	Just let it be overcrowded.
	  }
     }
}



/**
 * \brief Calculates the need for medical support among the population
 * in this team's area of influence.
 *
 * Need for medical support is calculated as follows: For all cells in
 * this team's area of influence with population p > 0.5 persons - add
 * f * p (where f is the fraction of the population that is infected)
 * to the total need.
 *
 * \return The need for medical support in this team's area of
 * influence represented as the number of infected persons.
 */
double HealthAgencyTeam::calculateNeed()
{
     double need = 0;
     list<GridPos> cells;
     list<GridPos>::iterator it;
     location().cells(*mGrid, cells);
     
     //	Iterate through nearby cells, adding up the need:
     for (it = cells.begin(); it != cells.end(); it++) {
	  GridCell *c = mGrid->cell(*it);
	  if (!c || c->pvfGet(ePopulation) < kMinPopulation) {
	       continue;
	  }

	  // Accumulate the number of sick people
	  need += c->pvGet(eFractionInfected) * c->pvfGet(ePopulation);
     }
     return need;
}

/**
 * \brief Performs this team's actions.
 *
 * Reduces the proportion infected people by an average of 5% in all
 * cells in this team's area of influence until the team's capacity
 * limit is met.
 *
 * \param now The current simulation time.
 */
void HealthAgencyTeam::act(Time now)
{
     if (!canWorkAt(*mLocation)) {
	  return;
     }
     // Move - Teleport for now...
     mLocation->move(mGoal);
//     debug("HealthTeam has location (lat, lng): " << location().center().lat() << ", " << location().center().lng());

     // Do our thing...
     list<GridPos> cells;
     list<GridPos>::iterator it;
     location().cells(*mGrid, cells);
     
     double sumServed = 0;
     for (it = cells.begin(); it != cells.end(); it++) {
	  GridCell *c = mGrid->cell(*it);
	  if (!c || c->pvfGet(ePopulation) < kMinPopulation) {
	       continue;
	  }

	  // Reduce the proportion infected by an average of 5%
	  if(sumServed < mCapacityPPD) {
	       double reduction = max(0.0, 1.0 - Exponential(0.05));
	       c->pvSetR(eFractionInfected, c->pvGet(eFractionInfected) * reduction);
	       sumServed += c->pvfGet(ePopulation);
	  }
     }
//     FIXME... probably... debug("Should increase recovered here!!!");
}


/**
 * \brief Performs this teams actions.
 *
 * Reduce violence in all cells in this team's area of influence until
 * the team's capacity limit is met.
 *
 * \param now The current simulation time.
 */
void PoliceAgencyTeam::act(Time now)
{
     if (!canWorkAt(*mLocation)) {
	  return;
     }
     // Move - Teleport for now...
     mLocation->move(mGoal);
//     debug("PoliceTeam has location (lat, lng): " << location().center().lat() << ", " << location().center().lng());

     // Do our thing...
     list<GridPos> cells;
     list<GridPos>::iterator it;
     location().cells(*mGrid, cells);
     
     double sumServed = 0;
     const double kBaseViolence = 10.0;
     for (it = cells.begin(); it != cells.end(); it++) {
	  GridCell *c = mGrid->cell(*it);
	  if (!c || c->pvfGet(ePopulation) < kMinPopulation) {
	       continue;
	  }

	  if (sumServed < mCapacityPPD) {
	       for (int i = 1; i < mGrid->factions() + 1; i++) {
		    double violence = c->pvfGet(eViolence, i);
		    if (violence > kBaseViolence) {
			 c->pvfAddR(eViolence, i, -0.2 * (violence - kBaseViolence));
		    }
	       }
	       sumServed += c->pvfGet(ePopulation);
	  }
     }
}



CustomAgencyTeam::CustomAgencyTeam(const DataObject& d)
     : AgencyTeam(d),
       mTarget(d.getChild("target") ? &d.getChild("target")->getReference() : 0)
{
     for (int i = 0; i < kNumModifiable; i++) {
	  DataObject* elem = d.getChild(PVHelper::modifiablePVName(i));
	  if (elem) {
	       mSeverities[PVHelper::modifiablePVName(i)] = elem->getDouble();
	  }
     }

     createEffects();
}

/**
 * \brief Creates the effects of this AgencyTeam from the mSeverities
 * map.
 */
void CustomAgencyTeam::createEffects() {
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
	  e << "Tried to create effects in CustomAgencyTeam but couldn't map the Reference '" << *mTarget;
	  e << "' to any ethnic faction.\nStratmas does not yet support other targets for Activities";
	  throw e;
     }

     mEffects.clear();
     for (std::map<string, double>::iterator it = mSeverities.begin(); it != mSeverities.end(); it++) {
//	  debug("  " << it->first << " - " << it->second);
	  addEffect(PVHelper::nameToOverAllOrder(it->first), it->second, targetFaction);
     }
}

/**
 * \brief Extracts data from this object to the Buffer.
 *
 * \param b The Buffer to extract data to.
 */
void CustomAgencyTeam::extract(Buffer& b) const
{
     AgencyTeam::extract(b);
     DataObject& me = *b.map(ref());
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
void CustomAgencyTeam::addObject(DataObject& toAdd, int64_t initiator)
{
     const Type& type = toAdd.getType();
     if (type.canSubstitute("EthnicFactionReference")) {
	  SOFactory::createSimple(toAdd, initiator);
	  mTarget = &toAdd.getReference();
     }
     else if (type.canSubstitute("Double") && toAdd.identifier() != "violenceThreshold") {
	  SOFactory::createSimple(toAdd, initiator);
	  mSeverities[toAdd.identifier()] = toAdd.getDouble();
     }
     else {
	  AgencyTeam::addObject(toAdd, initiator);
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
void CustomAgencyTeam::removeObject(const Reference& toRemove, int64_t initiator)
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
	  AgencyTeam::removeObject(toRemove, initiator);
     }
     createEffects();
}

/**
 * \brief Modifies this object with data from the provided DataObject.
 *
 * \param d The DataObject containing the new value.
 */
void CustomAgencyTeam::modify(const DataObject& d)
{
     const string& attr = d.identifier();
     if (attr == "target") {
	  mTarget = &d.getReference();
     }
     else if (mSeverities.find(attr) != mSeverities.end()) {
	  mSeverities[attr] = d.getDouble();
     }
     else {
	  AgencyTeam::modify(d);
     }
     createEffects();
}

/**
 * \brief Resets this object to the state it would have had if it was
 * created from the provided DataObject.
 *
 * \param d The DataObject to use as source for the reset.
 */
void CustomAgencyTeam::reset(const DataObject& d)
{
     AgencyTeam::reset(d);
     mTarget = (d.getChild("target") ? &d.getChild("target")->getReference() : 0);

     // Copy severities map
     std::map<std::string, double> sev(mSeverities);
     // Update the ones that already exist, add new ones.
     for (int i = 0; i < kNumModifiable; ++i) {
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
 * \brief Performs this teams actions.
 *
 * \param now The current simulation time.
 */
void CustomAgencyTeam::act(Time now)
{
     if (!canWorkAt(*mLocation)) {
	  return;
     }
     GridAction ga(*mGrid, location(), this, mEffects, 1.0);
     ga.carryOut();
}

