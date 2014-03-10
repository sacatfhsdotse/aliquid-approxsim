// System
#include <algorithm>
#include <iostream>

// Own
#include "Activity.h"
#include "Buffer.h"
#include "CombatGrid.h"
#include "DataObject.h"
#include "debugheader.h"
#include "Faction.h"
#include "GoodStuff.h"
#include "Grid.h"
#include "GridCell.h"
#include "LatLng.h"
#include "PresenceObject.h"
#include "PresenceObjectAllocator.h"
#include "PropertyHandler.h"
#include "random.h"
#include "Resetter.h"
#include "Shape.h"
#include "Simulation.h"       // For timestep()
#include "SOFactory.h"
#include "SOMapper.h"
#include "Type.h"
#include "Unit.h"


using namespace std;


//Tmp
//#include <typeinfo>

/**
 * \brief Selects a target unit among the registered potential target
 * units.
 *
 * The probability for a unit to be chosen as target is proportional
 * to the number of personnel for that unit. All units but the
 * selected unit are removed from the record.
 */
void AmbushRecord::selectTarget()
{
     if (active()) {
          // Allocate array
          Unit** candidates = new Unit*[mUnits.size()];
          // Allocate array
          double* sizes = new double[mUnits.size()];
          int count = 0;
          for (set<Unit*>::iterator it = mUnits.begin(); it != mUnits.end(); it++) {
               candidates[count] = *it;
               sizes[count] = (*it)->personnel();
               count++;
          }
          Unit* u = candidates[probBySize(sizes, mUnits.size())];
          // Deallocate array
          delete [] candidates;
          // Deallocate array
          delete [] sizes;
          mUnits.clear();
          addUnit(u);
     }
}

/**
 * \brief Creates a Unit object from the provided DataObject.
 *
 * \param d The DataObject to create the unit from.
 */
Unit::Unit(const DataObject& d)
     : Element(d), mParent(0),
       mSymbolIDCode(d.getChild("symbolIDCode")->getString()),
       mAffiliation(&d.getChild("affiliation")->getReference()),
       mPersonnel(d.getChild("personnel")->getInt64_t()), mPersonnelRest(0),
       mCasualties(d.getChild("casualties")->getInt64_t()), mCasualtyRest(0),
       mInitialPersonnel(mPersonnel),
       mStrengthFactor(d.getChild("strengthFactor")->getDouble()),
       mAttackFactor(d.getChild("attackFactor")->getDouble()),
       mDefenseFactor(d.getChild("defenseFactor")->getDouble()), 
       mWithdrawThreshold(d.getChild("WithdrawThreshold")->getDouble()), mVelocity(0),
       mMaxVelocity(d.getChild("maxVelocity")->getDouble()),
       mDeployTime(d.getChild("deployTime")->getTime()),
       mDepartTime(d.getChild("departTime")->getTime()),
       mDeployed(false), mDeparted(false), mMoving(false),
       mGoal(location().clone()), mSearchWithoutFind(false), mInsurgentRelatedCasualties(0),
       mCurrentOrder(0), mAllocatedOrder(0),
       mAngle(0), mGrid(0)
{
     // Check that the faction this unit affiliates with exists.
     if (!SOMapper::map(*mAffiliation)) {
          Error e;
          e << "The unit " << ref() << " affiliates with the non existing faction " << *mAffiliation;
          e << " and can thus not be simulated. Please remove it.";
          throw e;
     }

     switch (mSymbolIDCode[1]) {
     case 'F':
          mColor = eBlue;
          break;
     case 'H':
          mColor = eRed;
          break;
     case 'N':
          mColor = eGreen;
          break;
     case 'U':
          mColor = eYellow;
          break;
     default:
          mColor = eYellow;
          break;
     }

     // Create this unit's activities.
     const vector<DataObject*>& acts = d.getChild("activities")->objects();
     for(vector<DataObject*>::const_iterator it = acts.begin(); it != acts.end(); it++) {
          Activity* a = dynamic_cast<Activity*>(SOFactory::createSimulationObject(**it));
          mActivities.push_back(a);
     }
     
     // Latitude compensation
     mSqueeze = cos(center().lat() * kDeg2Rad);

     // Create subunits
     const vector<DataObject*>& subs = d.getChild("subunits")->objects();
     for(vector<DataObject*>::const_iterator it = subs.begin(); it != subs.end(); it++) {
          addSubunit(dynamic_cast<Unit*>(SOFactory::createSimulationObject(**it)));
     }
}

/**
 * \brief Destructor.
 */
Unit::~Unit()
{
     if (mGoal) { delete mGoal; }

     delete mAllocatedOrder;

     vector<Activity*>::iterator it;
     for (it = mActivities.begin(); it != mActivities.end(); it++) {
          SOFactory::removeSimulationObject(*it);
     }
     for (vector<Unit*>::iterator it = mSubunits.begin(); it != mSubunits.end(); it++) {
          SOFactory::removeSimulationObject(*it);
     }
}

/**
 * \brief Prepares this SimulationObject for simulation.
 *
 * Should be called after creation and reset and before the simulation
 * starts.
 *
 * \param grid The Grid.
 * \param currentTime The current simulation time.
 */
void Unit::prepareForSimulation(Grid& grid, Time currentTime)
{
     for (vector<Unit*>::iterator it = mSubunits.begin(); it != mSubunits.end(); it++) {
          (*it)->prepareForSimulation(grid, currentTime);
     }
     for (vector<Activity*>::iterator it = mActivities.begin(); it != mActivities.end(); it++) {
          (*it)->prepareForSimulation(grid, currentTime);
     }
     mGrid = &grid;
     mDeployed = (currentTime >= mDeployTime);
     mDeparted = (currentTime >= mDepartTime);
     setOrder(currentTime);
}

/**
 * \brief Extracts data from this object to the Buffer.
 *
 * \param b The Buffer to extract data to.
 */
void Unit::extract(Buffer& b) const
{
     Element::extract(b);
     DataObject& me = *b.map(ref());
     me.getChild("symbolIDCode")->setString(mSymbolIDCode);
     me.getChild("affiliation")->setReference(*mAffiliation);
     me.getChild("personnel")->setInt64_t(mPersonnel);
     me.getChild("casualties")->setInt64_t(mCasualties);
     me.getChild("strengthFactor")->setDouble(mStrengthFactor);
     me.getChild("attackFactor")->setDouble(mAttackFactor);
     me.getChild("defenseFactor")->setDouble(mDefenseFactor);
     me.getChild("maxVelocity")->setDouble(mMaxVelocity);
     me.getChild("deployTime")->setTime(mDeployTime);
     me.getChild("departTime")->setTime(mDepartTime);
     me.getChild("WithdrawThreshold")->setDouble(mWithdrawThreshold);
}

/**
 * \brief Adds the SimulationObject created from the provided
 * DataObject to this object.
 *
 * \param toAdd The DataObject to create the new SimulationObject from.
 * \param initiator The id of the initiator of the update.
 */
void Unit::addObject(DataObject& toAdd, int64_t initiator)
{
     const Type& type = toAdd.getType();
     if (type.canSubstitute("MilitaryUnit")) {
          Unit* u = dynamic_cast<Unit*>(SOFactory::createSimulationObject(toAdd, initiator));
          u->prepareForSimulation(*mGrid, Simulation::simulationTime());
          addSubunit(u);
     }
     else if (type.canSubstitute("Activity")) {
          Activity* a = dynamic_cast<Activity*>(SOFactory::createSimulationObject(toAdd, initiator));
          a->prepareForSimulation(*mGrid, Simulation::simulationTime());
          mActivities.push_back(a);
          sort(mActivities.begin(), mActivities.end(), lessActivityPointer());
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
void Unit::removeObject(const Reference& toRemove, int64_t initiator)
{
     SimulationObject* o = SOMapper::map(toRemove);

     if (Unit* a = dynamic_cast<Unit*>(o)) {
          mSubunits.erase(find(mSubunits.begin(), mSubunits.end(), a));
          SOFactory::removeSimulationObject(a, initiator);
     }
     else if (Activity* a = dynamic_cast<Activity*>(o)) {
          mActivities.erase(find(mActivities.begin(), mActivities.end(), a));
          if (mCurrentOrder == a) {
               mCurrentOrder = 0;
          }
          SOFactory::removeSimulationObject(a, initiator);
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
void Unit::modify(const DataObject& d)
{
     const string& attr = d.identifier();
     if (attr == "symbolIDCode") {
          mSymbolIDCode = d.getString();
     }
     else if (attr == "affiliation") {
          mAffiliation = &d.getReference();
     }
     else if (attr == "personnel") {
          mPersonnel = d.getInt64_t();
          if (mPersonnel > mInitialPersonnel) {
               mInitialPersonnel = mPersonnel;
          }
     }
     else if (attr == "casualties") {
          mCasualties = d.getInt64_t();
     }
     else if (attr == "strengthFactor") {
          mStrengthFactor = d.getDouble();
     }
     else if (attr == "attackFactor") {
          mAttackFactor = d.getDouble();
     }
     else if (attr == "defenseFactor") {
          mDefenseFactor = d.getDouble();
     }
     else if (attr == "maxVelocity") {
          mMaxVelocity = d.getDouble();
     }
     else if (attr == "deployTime") {
          mDeployTime = d.getTime();
          mDeployed = (Simulation::simulationTime() >= mDeployTime);
     }
     else if (attr == "departTime") {
          mDepartTime = d.getTime();
          mDeparted = (Simulation::simulationTime() >= mDepartTime);
     }
     else if (attr == "WithdrawThreshold") {
          mWithdrawThreshold = d.getDouble();
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
void Unit::reset(const DataObject& d)
{
     Element::reset(d);
     mSymbolIDCode = d.getChild("symbolIDCode")->getString();
     Resetter<Unit>::reset(mSubunits, d.getChild("subunits")->objects());
     mAffiliation = &d.getChild("affiliation")->getReference();
     mPersonnel = d.getChild("personnel")->getInt64_t();
     mPersonnelRest = 0;
     mCasualties = d.getChild("casualties")->getInt64_t();
     mCasualtyRest = 0;
     mInitialPersonnel = mPersonnel;
     mStrengthFactor = d.getChild("strengthFactor")->getDouble();
     mAttackFactor = d.getChild("attackFactor")->getDouble();
     mDefenseFactor = d.getChild("defenseFactor")->getDouble();
     mWithdrawThreshold = d.getChild("WithdrawThreshold")->getDouble();
     mVelocity = 0;
     mMaxVelocity = d.getChild("maxVelocity")->getDouble();
     mDeployTime = d.getChild("deployTime")->getTime();
     mDepartTime = d.getChild("departTime")->getTime();
     mDeployed = false;
     mDeparted = false;
     mMoving = false;
     setGoal(location());
     mEnemyRecords.clear(); 
     mAmbushRecords.clear();
     mAmbushExposure.clear();
     mInsurgentCells.clear();
     mSpotters.clear();
     mSearchWithoutFind = false;
     mInsurgentRelatedCasualties = 0;
     Resetter<Activity>::reset(mActivities, d.getChild("activities")->objects());
     mCurrentOrder = 0;
     delete mAllocatedOrder;
     mAllocatedOrder = 0;
     mGrid = 0;
}


/**
 * \brief Returns the faction that this unit belongs to.
 *
 * \return The faction that this unit belongs to.
 */
Faction& Unit::faction() const
{
     return *Faction::faction(*mAffiliation);
}

/**
 * \brief Checks if this unit is in a combat situation.
 *
 * \return True if this unit will have to fight (or retreat).
 */
bool Unit::combatSituation()
{
     return (!mEnemyRecords.empty());
}

/**
 * \brief Checks if this unit is in a critical insurgent situation.
 *
 * Critical means that the unit has lost more than one per thousand of
 * its personnel during the last day due to insurgent activities.
 *
 * \return True if the insurgent situation is critical.
 */
bool Unit::criticalInsurgentSituation()
{
     return (mInsurgentRelatedCasualties / Simulation::fractionOfDay() / personnel() > 0.001);
}

/**
 * \brief Checks if this unit is searching for an ambushing unit.
 *
 * \return True if this unit is searcing for an ambushing unit.
 */
bool Unit::searching()
{
     return (dynamic_cast<SearchOrder*>(mCurrentOrder) != 0);
}

/**
 * \brief Helper for handling memory allocation for temporarily
 * created orders that must survive beyond the scope of a single
 * function.
 *
 * The only purpose is to deallocate the last allocated order and
 * store a pointer to the newly allocated order so that it may be
 * freed at the next invocation of this function.
 *
 * \param order The newly allocated order.
 * \return The same order as the input order.
 */
Order* Unit::setAllocatedOrder(Order* order)
{
     if (mAllocatedOrder) {
          SOFactory::removeSimulationObject(mAllocatedOrder);
     }
     mAllocatedOrder = order;
     return mAllocatedOrder;
}


/**
 * \brief Sets the order of this unit.
 *
 * \param simTime The current simulation time.
 */
Order* Unit::setOrder(Time simTime)
{
     if (combatSituation()) {
          // Our strength this time step is based on the previous time
          // step's order because it should be reduced if we're in
          // combat this time step but wasn't in the previous.
          setModifiedStrength();
          if (capable()) {
               if (!dynamic_cast<AttackOrder*>(mCurrentOrder) &&
                   !dynamic_cast<DefendOrder*>(mCurrentOrder)) {
                    if (mMoving) {
                         mCurrentOrder = setAllocatedOrder(new AttackOrder());
                    }
                    else {
                         mCurrentOrder = setAllocatedOrder(new DefendOrder());
                    }
                     //stratmasDebug("== Setting order for unit " << ref().name() << " to " << (mMoving ? "attack" : "defend"));
               }
          }
          else {
               if (!untouchable()) {
                    mCurrentOrder = setAllocatedOrder(createRetreatOrder());
               }
               //stratmasDebug("== Setting order for unit " << ref().name() << " to retreat");
          }
     }
     else if (mSearchWithoutFind) {
          mCurrentOrder = setAllocatedOrder(new SearchOrder(simTime));
          setModifiedStrength();
          mSearchWithoutFind = false;
     }
     else if (!mCurrentOrder || !mCurrentOrder->isActive(simTime)) {
          mCurrentOrder = 0;
          for (vector<Activity*>::iterator it = mActivities.begin(); it != mActivities.end(); it++) {
               if ((*it)->isActive(simTime)) {
                    mCurrentOrder = dynamic_cast<Order*>(*it);
                    if (!mCurrentOrder) {
                         Error e;
                         e << "Unit " << ref() << " is given the activity ";
                         e << (*it)->ref() << " that is not an order";
                         throw e;
                    }
                    //stratmasDebug("== Setting order for unit " << ref().name() << " to " << mCurrentOrder->ref().name());
                    break;
               }
          }
           if (!mCurrentOrder) {
                //stratmasDebug("== Setting order for unit " << ref().name() << " to NULL");
           }
          setModifiedStrength();
     }
     else {
          setModifiedStrength();
     }
     
//     stratmasDebug("mCurrentOrder for " << ref().name() << " is " << (mCurrentOrder ? typeid(*mCurrentOrder).name() : "null"));
     return mCurrentOrder;
}

/**
 * \brief Calculates and sets the modified strength for this unit
 * based on a time step of one day (24 h).
 */
void Unit::setModifiedStrength()
{
     double factor;
     // Combat situation implicity leads to attack/defend order.
     if (combatSituation()) {
          // Calculate the mean combat factor during the timestep.
          //  cv ^                           0     cv ^                                 
          //     |            k (slope)      0        |                                 
          // trg |          /---------       0    trg |          k (slope)                      
          //     |        / :    :           0    cv2 |        /-----------
          //     |      /   :    :           0        |      / :
          // cv1 |----/     :    :           0    cv1 |----/   :                      
          //     |    :     :    :           0        |    :   :                     
          //     +---------------------> t   0              +---------------------> t 
          //          0    tf   dt           0             0  dt
          const double k = 0.2; // readiness increases 20 points of percentage per hour.
          double dt = Simulation::timestep().hoursd();   // Timestep in hours
          // Combat factor before timestep starts
          double cv1 = (mCurrentOrder ? mCurrentOrder->combatFactor() : 1);
          // Combat factor at end of timestep if we continue to increase above 1.
          double cv2 = cv1 + k * dt;
          double trg = 1;
//          stratmasDebug(ref().name() << ": cv1 " << cv1 << ", cv2 " << cv2);
          if (cv2 > trg) {
               // The unit will reach target combat factor before the timestep is over.
               double tf = (trg - cv1) / k;  // Number of hours to reach target combat factor.
//               stratmasDebug("tf: " << tf);
               factor = (trg * dt   -   tf * (trg - cv1) / 2) / dt;
          }
          else {
               // The unit will not reach target combat factor before the timestep is over.
               factor = cv1 + k * dt / 2;
          }
//           const double deltaCVPerHour = 0.2;
//           const double orderFactor = (mCurrentOrder ? mCurrentOrder->combatFactor() : 0.5);
//           const double timeToGainCV = (1 - orderFactor) / deltaCVPerHour;
          
//           // Reduce combat value based on how 'prepared' we are.
//           factor = (timeToGainCV * (1 - orderFactor) / 2 + 24 - timeToGainCV) / 24;
//          stratmasDebug("factor: " << factor);
     }
     else if (criticalInsurgentSituation()) {
          factor = 1;
     }
     else {
          factor = (mCurrentOrder ? mCurrentOrder->combatFactor() : 1);
     }
     mModifiedStrength = strength() * factor;
}

/**
 * \brief Finds out which of an ambushed unit's ambushers it will find the next timestep.
 *
 * If it finds any of them - create an AttackOrder for itself and a
 * defend order for each of the found units. Then it will be regular
 * combat during the next timestep.
 */
void Unit::setUpSearchAndDestroy(Time simTime) 
{
     // The probability p for a unit with personnel 1000 to find a
     // unit with 1 person per square kilometer should be 0.4. The
     // proposed model says p = 1-exp(-a*b*gamma) where a is the
     // number of personnel for the searching unit and b is the number
     // of ambushers per km2 and thus gamma = -log(1-0.4)/(1000)
     const double kGamma = -log(1 - 0.4) * 0.001;
     double km2PerCell = mGrid->cellAreaKm2();
//     stratmasDebug("--------------------" << ref().name() << " searches...");
     bool foundAnyone = false;
     for (std::map<Unit*, EnemyRecord>::iterator it = mAmbushExposure.begin(); it != mAmbushExposure.end(); it++) {
          list<GridPos> l;
          Unit& ambusher = *it->first;
          ambusher.location().cells(*mGrid, l);
          double ambushersPerKm2 = ambusher.personnel() / (static_cast<double>(l.size()) * km2PerCell);
          double prob = 1 - exp(-personnel() * ambushersPerKm2 * kGamma);
          double r = RandomUniform();
//            stratmasDebug("   #ambushers per km2: " << ambushersPerKm2);
//            stratmasDebug("   Is " << r << " less than " << prob);
          if (r < prob) {
//               stratmasDebug("   " << ref().name() << " finds " << it->first->ref().name());
               it->first->mCurrentOrder = it->first->setAllocatedOrder(new DefendOrder());
               foundAnyone = true;
          }
     }
     if (foundAnyone) {
          mCurrentOrder = setAllocatedOrder(new AttackOrder());
     }
     else {
          // We didn't find anyone so next time step we should be
          // 'searching' e.g. we are not able to fight insurgents.
          mSearchWithoutFind = true;
     }
}

/**
 * \brief Creates a RetreatOrder.
 *
 * \return The newly created RetreatOrder.
 */
Order* Unit::createRetreatOrder()
{
     // Find the angle in which to retreat. The algorithm will
     // calculate the angles of all current foes and retreat in
     // the direction where the angular distance between any two
     // enemies are greatest.
     set<double> ang;

     // Store the angles of our foes.
     for (std::map<Unit*, EnemyRecord>::iterator it = mEnemyRecords.begin(); it != mEnemyRecords.end(); it++) {
          // Don't count units that has the exact same location as this unit.
          if (it->first->center() != center()) {
               ang.insert(atan2(it->first->center().lat() - center().lat(),
                                it->first->center().lng() - center().lng()));
          }
     }

     double angle;
     if (ang.empty()) {
          // If all our enemies have the exact same location as we do
          // choose an angle at random.
          angle = RandomUniform(0, k2Pi);
     }
     else {
          // Insert the first element + 2*pi so we don't have to treat
          // the first and last value special.
          ang.insert(*ang.begin() + k2Pi);
          
          double maxDist = 0;            // Current max angular distance
          double last    = *ang.begin(); // Angle of last foe
          angle = 0;                     // The currently best retreat angle
          for (set<double>::iterator it = ++ang.begin(); it != ang.end(); it++) {
               double dist = *it - last;
               if (dist > maxDist) {
                    maxDist = dist;
                    angle = last + dist / 2;
               }
               last = *it;
          }
     }

     // Retreat a predefined distance in the found direction.
     LatLng newGoal(center().lat() + 50 / kKmPerDegreeLat * sin(angle),
                    center().lng() + 50 / kKmPerDegreeLat * cos(angle) * mSqueeze);

     Order* o = new RetreatOrder(Circle(newGoal, 10000));
     stratmasDebug(ref().name() << " retreating towards " << o->location()->cenCoord());
     return o;
}

/**
 * \brief Exposes this unit for an attack by another unit.
 *
 * This function implements the damage model proposed by Thorssell,
 * where the magnitude of the damage is highly dependent of the quote
 * between the attacker's and the defender's combat values. Notice
 * that - despite the name of the function - the unit for which this
 * function is called is not necessarily a defender.
 *
 * \param modStr The modified strength (the part that should be used
 * to combat this unit) of the attacker..
 * \param attacker The attacking unit.
 */
void Unit::exposeForAttack(double modStr, Unit& attacker)
{
     double frac = attacker.strength() / mCurrentEnemyStrengthSum;
     double myStr = modifiedStrength() * attackDefendFactor() * frac;
     double damage;
//      stratmasDebug(ref().name() << ", str: " << myStr << " exposed by " << attacker.ref().name()
//             << ", str: " << modStr << " (" << frac*100 << "% of tot. enemy)");

     if (defender()) {
          double quote = (myStr != 0 ? modStr / myStr : 6.0);
          double fx = 0.025 * quote + 0.025;
//           double percent = fx * 100;
//             stratmasDebug("  " << ref().name() << " = defender,  quote: " << quote << " gives loss of "
//                   << percent << "% (" << percent * frac << "% of total)");
          damage = fx * static_cast<double>(mPersonnel) * frac;
     }
     else {
          double quote = (modStr != 0 ? myStr / modStr : 6.0);
          double fx = -0.025 * quote + 0.15;
//           double percent = fx * 100;
//             stratmasDebug("  " << ref().name() << " = attacker,  quote: " << quote << " gives loss of "
//                  << percent << "% (" << percent * frac << "% of total)");
          damage = max(0.0, fx * static_cast<double>(mPersonnel) * frac);
     }
     mEnemyRecords[&attacker].addDamage(damage * Simulation::fractionOfDay());
//     stratmasDebug("   " << attacker.ref().name() << " exposes " << ref().name() << " for " << damage << " damage");
}

/**
 * \brief Exposes this unit for an ambush. Also registers the damage
 * suffered by the ambushing unit.
 *
 * \param u The ambushing unit.
 * \param cell The cell in which this ambush is performed.
 * \param damage The damage caused to the <b>ambushing</b> unit,
 * e.g. 5% of its personnel in this cell. The damage suffered by the
 * ambushed unit is 4 times that number.
 */
void Unit::exposeForAmbush(Unit& u, int cell, double damage)
{
     // Register the damage that this unit suffers.
     EnemyRecord& er = mAmbushExposure[&u];
     er.addCell(cell);
     er.addDamage(damage * 4.0);

     // Register the damage that the ambushing unit suffers.
     EnemyRecord& er2 = u.mAmbushExposure[this];
     er2.addCell(cell);
     er2.addDamage(damage);
}

/**
 * \brief Checks if this unit is an attacker.
 *
 * \return True if this unit is an attacker, false otherwise.
 */
bool Unit::attacker() const
{
     return (dynamic_cast<AttackOrder*>(mCurrentOrder) != 0);
}

/**
 * \brief Checks if this unit is an defender.
 *
 * \return True if this unit is an defender, false otherwise.
 */
bool Unit::defender() const
{
     return (dynamic_cast<DefendOrder*>(mCurrentOrder) != 0);
}

/**
 * \brief Gets the combat value modifier based on if we're attacker,
 * defender or neither.
 *
 * \return The attack defend combat value modifier.
 */
double Unit::attackDefendFactor() const 
{
     double ret = 1;
     if (attacker()) {
          ret = mAttackFactor;
     }
     else if (defender()) {
          ret = mDefenseFactor;
     }
     return ret;
}


/**
 * \brief Increases the personnel of the unit due to
 * recovering. According to Thorssell a unit recovers personnel
 * corresponding to one unit of strength per day.
 */
void Unit::recover()
{
     // Introduced 2006-10-03 on demand by Patrik for Demo06.
     const double recoveryReductionFactor = 0.1;
     if (mPersonnel < mInitialPersonnel) {
          double recovered = 1.0 / mStrengthFactor * Simulation::fractionOfDay() * recoveryReductionFactor;
          mPersonnelRest += modf(recovered, &recovered);
          if (mPersonnelRest > 1) {
               recovered++;
               mPersonnelRest--;
          }
          if (recovered > (mInitialPersonnel - static_cast<double>(mPersonnel))) {
               mPersonnel = Round(mInitialPersonnel);
               mPersonnelRest = 0;
          }
          else {
               mPersonnel += Round(recovered);
          }
//          stratmasDebug(ref().name() << " recovers " << recovered << " persons");
     }
}


/**
 * \brief Moves this unit with its current velocity straight towards its goal.
 */
void Unit::move()
{
     double distanceKm;
     double speedKm;
     double portion;

     LatLng goal = mGoal->cenCoord();
        
     if (mVelocity > 0.0) {
          double vx = (goal.lng() - center().lng()) * mSqueeze;
          double vy =  goal.lat() - center().lat();
          if ((vx != 0.0) || (vy != 0.0)) {
               // We're moving
               mMoving = true;

               // Calculate distance to goal:
               distanceKm = sqrt( vx*vx + vy*vy ) * kKmPerDegreeLat;
                        
               // Calculate speed in kilometers per timestep
               speedKm = mVelocity * Simulation::timestep().hoursd();
                        
//               debug ("distance to goal: " << distanceKm << ", current speed: " << mVelocity << " km/h");
               if (distanceKm > speedKm) {
                    portion = speedKm / distanceKm;
                    vx *= portion;
                    vy *= portion;
                    mLocation->move(vx / mSqueeze, vy);
               }
               else {
                    setLocation(*mGoal);
                    mVelocity = 0;
                    vx = 0;
                    vy = 0;
               }
               mSqueeze = cos(center().lat() * kDeg2Rad);
          }
     }
}


/**
 * \brief Performs combat actions for this unit.
 *
 * Exposes each recorded enemy for an attack of size that is
 * proportional to the strength of that unit compared to other
 * ememies.
 *
 * \return True if we're stll in a combat situation, false otherwise.
 */
bool Unit::combat()
{
     // According to Thorssell we can't fight if we're retreating.
     if (mCurrentOrder && dynamic_cast<RetreatOrder*>(mCurrentOrder)) {
          return false;
     }
     for (std::map<Unit*, EnemyRecord>::iterator it = mEnemyRecords.begin(); it != mEnemyRecords.end(); it++) {
          double frac = it->first->strength() / mCurrentEnemyStrengthSum;
          it->first->exposeForAttack(modifiedStrength() * frac * attackDefendFactor(), *this);
     }
     return combatSituation();
}


/**
 * \brief Returns true if this unit is untouchable.
 *
 * An untouchable state allows the unit to move without beeing
 * attacked by enemies. This is currently used when a unit has
 * retreated and then should go to the location of its closest
 * superior and for the Ambush order.
 *
 * \return True if this unit is untouchable.
 */
bool Unit::untouchable() const
{     
     if (RetreatOrder* o = dynamic_cast<RetreatOrder*>(mCurrentOrder)) {
          return (o->state() == RetreatOrder::eReturn && !o->isCarriedOut());
     }
     else if (dynamic_cast<AmbushOrder*>(mCurrentOrder)) {
          return true;
     }
     else if (dynamic_cast<TerroristAttackOrder*>(mCurrentOrder)) {
          return true;
     }
     else {
          return false;
     }
}

/**
 * \brief Sets up this unit before ti can act.
 *
 * Must set up all units before any of them can act since we have to
 * know for example which order each unit has.
 *
 * \param simTime The current simulation time.
 */
void Unit::setup(Time simTime)
{
     // Subunits first
     for (vector<Unit*>::iterator it = mSubunits.begin(); it != mSubunits.end(); it++) {
          (*it)->setup(simTime);
     }


     // Deploy or depart
     if (!deployed() && simTime > deployTime()) {
          deploy();
     }
     if (!departed() && simTime > departTime()) {
          depart();
     }

     if (present() && personnel() > 0) {
          // Now we can set the order for this time step.
          setOrder(simTime);
     }     

     // Are we an ambush unit ready to ambush?
     if (!mAmbushRecords.empty()) {
          for (std::map<int, AmbushRecord>::iterator it = mAmbushRecords.begin(); it != mAmbushRecords.end(); it++) {
               AmbushRecord& ar = it->second;
               if (ar.active()) {
                    ar.selectTarget();
                    (*ar.units().begin())->exposeForAmbush(*this, it->first, ar.damage());
               }
          }
     }

     // Sum up the total strength of our current enemies so we can
     // determine how to divide our offensive capacities. This could
     // perhaps be done in the registerEnemy function at the cost of
     // some extra lookups in the mEnemyRecord map
     mCurrentEnemyStrengthSum = 0;
     for (std::map<Unit*, EnemyRecord>::iterator it = mEnemyRecords.begin(); it != mEnemyRecords.end(); it++) {
          mCurrentEnemyStrengthSum += it->first->strength();
     }
//     stratmasDebug(ref().name() << "'s tot enemy  " << mCurrentEnemyStrengthSum);
}


/**
 * \brief Perform this units tasks.
 *
 * \param simTime The current simulation time.
 */
void Unit::act(Time simTime)
{
     // Subunits act first
     for (vector<Unit*>::iterator it = mSubunits.begin(); it != mSubunits.end(); it++) {
          (*it)->act(simTime);
     }

     // The mMoving flag will be set in the move() function if the
     // order we're currently executing involves moving.
     mMoving = false;

     // Do our things if we're here, we're alive and we have an order.
     if (present() && personnel() > 0) {
          if (mCurrentOrder) {
               mCurrentOrder->perform(this, Simulation::fractionOfDay());
          }
          if (!combatSituation() && !criticalInsurgentSituation()) {
               recover();
          }
          if (!combatSituation() && !dynamic_cast<AmbushOrder*>(mCurrentOrder) && !mAmbushExposure.empty()) {
               setUpSearchAndDestroy(simTime);
          }
     }

     // Will be set by CombatGrid::registerCombat()
     mInsurgentCells.clear();
     mInsurgentRelatedCasualties = 0;
}

/**
 * \brief Sets the goal for this unit.
 *
 * \param goal The new goal
 */
void Unit::setGoal(const Shape& goal)
{
     if (mGoal) {
          delete mGoal;
     }
     mGoal = goal.clone();
}

/**
 * \brief Sets the velocity of this unit.
 */
void Unit::setVelocity(double frac)
{
     mVelocity = mMaxVelocity * frac;
}


/**
 * \brief Gets a PresenceObject for this unit for the given cell.
 *
 * \param poa The PresenceObjectAllocator to use for allocation.
 * \param cellIndex The index of the cell the PresenceObject should
 * refer to.
 * \param fraction The fraction of this unit that should be placed in
 * this cell according to the deployment distribution of the
 * unit. Notice that this is not necessarily the actual fraction since
 * it may differ if this unit is in a critical insurgent situation and
 * should thus focus its strength to the cells where insurgent related
 * damage occured during the previous time step.
 * \return A PresenceObject or null if this unit does not have any
 * personnel.
 */
PresenceObject* Unit::getPresence(PresenceObjectAllocator& poa, int cellIndex,  double fraction)
{
     if (personnel() > 0) {
          double fracToUse;
          if (criticalInsurgentSituation()) {
               double focusFraction = (mCurrentOrder ? max(0.95, mCurrentOrder->combatFactor()) : 1);
               fracToUse = focusFraction / static_cast<double>(mInsurgentCells.size());
          }
          else {
               fracToUse = fraction;
          }
          return poa.create(cellIndex ,*this, fracToUse);
     }
     else {
          return 0;
     }
}

/**
 * \brief Returns true if this unit is hostile towards the unit c.
 *
 * \param u The unit to check stance against.
 * \return True if this unit is hostile towards the unit c, false
 * otherwise.
 */
bool Unit::isHostileTowards(const Unit& u) const
{
     if (mAffiliation == u.mAffiliation) {
          return false;
     }
     return faction().isHostileTowards(u.faction());
}

/**
 * \brief Registers the effect of force on force combat, insurgent
 * combat and ambush during this time step, e.g. modifies the
 * personnel and casualties if necessary.
 *
 * \param cg The CombatGrid to register casualties to.
 */
void Unit::registerCombat(CombatGrid& cg)
{
     for (vector<Unit*>::iterator lit = mSubunits.begin(); lit != mSubunits.end(); lit++) {
           (*lit)->registerCombat(cg);
     }

     int unitLayer = cg.nameToIndex(faction().ref().name() + CombatGrid::kCasualtyStr);
     double dead = mInsurgentRelatedCasualties;
     double factor = 1;
     std::map<Unit*, EnemyRecord>::iterator it;
     set<int>::const_iterator sit;

     for (it = mEnemyRecords.begin(); it != mEnemyRecords.end(); it++) {          
          dead += it->second.damage();
     }

     for (it = mAmbushExposure.begin(); it != mAmbushExposure.end(); it++) {          
          dead += it->second.damage();
     }

     // Notice that the damage caused to a unit may be larger than the
     // total personnel but that personnel mustn't be less than zero.
     if (dead > static_cast<double>(mPersonnel)) {
          // The average damage should be reduced by this factor.
          factor = static_cast<double>(mPersonnel) / dead;
          dead = mPersonnel;   // Everybody dies...
          mCasualtyRest = 0;
     }
     for (it = mEnemyRecords.begin(); it != mEnemyRecords.end(); it++) {
          EnemyRecord& er = it->second;
          double frac = er.damage() * factor / static_cast<double>(er.cells().size());
          for (sit = er.cells().begin(); sit != er.cells().end(); sit++) {
               cg.add(*sit, unitLayer, frac);
               cg.add(*sit, cg.casualtySumLayer(), frac);
//               stratmasDebug(ref().name() << " damage " << frac << " in cell " << *sit);
          }
     }

     for (it = mAmbushExposure.begin(); it != mAmbushExposure.end(); it++) {
          EnemyRecord& er = it->second;
          double frac = er.damage() * factor / static_cast<double>(er.cells().size());
          for (sit = er.cells().begin(); sit != er.cells().end(); sit++) {
               cg.add(*sit, unitLayer, frac);
               cg.add(*sit, cg.casualtySumLayer(), frac);
//               stratmasDebug(ref().name() << " ambush " << frac << " in cell " << *sit);
          }
     }

     mCasualtyRest += modf(dead, &dead);
     if (mCasualtyRest > 1) {
          dead++;
          mCasualtyRest--;
     }

     int deadi = Round(dead);
     mCasualties += deadi;
     mPersonnel  -= deadi;

     mEnemyRecords.clear();
     mAmbushRecords.clear();
     mAmbushExposure.clear();
     mSpotters.clear();
}

/**
 * \brief registers the impact of insurgents in the specified
 * cell. Does not change the personnel and casualties number since
 * this is done in registerCombat()
 *
 * \param cell The index of the cell.
 * \param impact The magnitude of * the impact in number of
 * casualties.
 * \return The number of casualties.
 */
double Unit::registerInsurgentImpact(int cell, double impact)
{
     mInsurgentCells.insert(cell);
     double newDead = min(static_cast<double>(personnel()), impact);
     mInsurgentRelatedCasualties += newDead;
     return newDead;
}

/**
 * \brief Registers a unit as an enemy of this unit.
 *
 * Enemies will get attacked when act() is called the next time
 * provided that this unit is capable.
 *
 * \param p The PresenceObject for the victim.
 */
void Unit::registerEnemy(const PresenceObject& p)
{
//      if (mEnemyRecords.find(&p.unit()) == mEnemyRecords.end()) {
//            stratmasDebug("======== Registering " << p.unit().ref().name() << " as enemy of " << ref().name());
//      }
     EnemyRecord& er = mEnemyRecords[&p.unit()];
     er.addCell(p.cell());
}

/**
 * \brief Registers a unit as an ambush victim of this unit if the
 * ambush conditions are met.
 *
 * The ambush conditions are: If there are more than kMinAmbushPerKm2
 * personnel per square kilometer in this cell the probability for an
 * ambush is p = #personnel in this cell / 1000.
 *
 * \param victim The PresenceObject for the potential victim.
 * \param myFraction The fraction of this unit that is located in the
 * cell refered to by the PresenceObject.
 */
void Unit::registerPotentialAmbush(PresenceObject& victim, double myFraction)
{
     AmbushOrder* o = dynamic_cast<AmbushOrder*>(mCurrentOrder);
     if (o && o->state() == AmbushOrder::eAmbush) {
          std::map<int, AmbushRecord>::iterator it = mAmbushRecords.find(victim.cell());
          // If there should be an ambush in this cell then add an
          // active AmbushRecord and add all subsequent units to that
          // record. If there shouldn't be an Ambush then add a
          // non-active AmbushRecord.
          if (it == mAmbushRecords.end()) {
               double persons = myFraction * personnel();
               // If the startAmbush and endAmbush times are equal
               // there will be guaranteed ambush in all cells with
               // enough personnel.
               double r = (o->oneAmbush() ? -1 : RandomUniform());
//               stratmasDebug("cell: " << victim.cell() << ", random: " << r << ", persons: " << persons);
               double km2PerCell = mGrid->cellAreaKm2();
               // The probability for 1000 persons to perform an
               // ambush in a cell with area 100 km2 should be 1.
               if (persons > kMinAmbushPerKm2 * km2PerCell && r < persons / 10 / km2PerCell) {
//                     stratmasDebug("Registering " << victim.unit().ref().name() <<
//                           " as ambushee of " << ref().name() << " in cell " << victim.cell());
                    // Insert new ambush record and add the victim to it.
                    AmbushRecord ar;
                    ar.addDamage(persons * 0.05);
                    ar.addUnit(&victim.unit());
                    mAmbushRecords.insert(pair<int, AmbushRecord>(victim.cell(), ar));
               }
               else {
                    // No ambush so mark cell with a non-active AmbushRecord.
                    mAmbushRecords[victim.cell()] = AmbushRecord();
               }
          }
          else if (it->second.active()) {  
               // Add victim to AmbushRecord for this cell.
               it->second.addUnit(&victim.unit());
          }
     }
}

/**
 * \brief Sets the location of this unit.
 *
 * \param newLocation The new location.
 */
void Unit::setLocation(const Shape& newLocation)
{
     if (mLocation) { delete mLocation; }
     mLocation = newLocation.clone();
     mLocation->touch();
}

/**
 * \brief Registers the unit in the given PresenceObject as a
 * potential 'spotter' of this unit.
 *
 * Used in order to fulfill the TerroristAttack order behavior. Notice
 * that a unit may only be spotted during the same timestep as the
 * attack is (or isn't) performed.
 *
 * \param p The PresenceObject containing the potential spotting unit
 * and how large part of that unit that is located in the cell the
 * PresenceObject refers to.
 */
void Unit::registerSpotter(const PresenceObject& p)
{
     // According to the model we can only be spotted in our center cell.
     const GridCell* c = mGrid->cell(center());
     int midCell = (c ? c->index() : -1);
     if (dynamic_cast<TerroristAttackOrder*>(mCurrentOrder) && p.cell() == midCell) {
          mSpotters[&p.unit()] = p.fraction();
     }
}

/**
 * \brief Determines if this unit is spotted by any of its potential
 * spotters.
 *
 * A spotted unit is killed immediately, according to the model.
 *
 * \return True if we're spotted, false otherwise.
 */
bool Unit::isSpotted()
{
     // The probability for 1 soldier to reveal a terrorist attack in
     // a population of 10 persons in a square kilometer should be
     // 0.5. The formula is p = exp(-#soliders / #inhabitants * gamma)
     // where gamma is approximately 6.93.
     const double kGamma = -10 * log(0.5);
     const GridCell* c = mGrid->cell(center());
     bool spotted = false;
     for (std::map<Unit*, double>::iterator it = mSpotters.begin(); it != mSpotters.end(); it++) {
          stratmasDebug("blue: " << it->first->personnel() << ", fraction: " << it->second << ", pop: " << c->pvfGet(ePopulation));
          stratmasDebug("   p = " << (1 - exp(-it->first->personnel() * it->second  / c->pvfGet(ePopulation) * kGamma)));
          if (c && RandomUniform() < (1 - exp(-it->first->personnel() * it->second / c->pvfGet(ePopulation) * kGamma))) {
               stratmasDebug("====== " << ref().name() << " spotted by " << it->first->ref().name());
               kill(*it->first);
               spotted = true;
               break;
          }
     }
     return spotted;
}

/**
 * \brief Kills this unit and spreads damage evenly across all
 * overlapped cells.
 */
void Unit::kill(Unit& killer)
{
     EnemyRecord er;
     er.addDamage(static_cast<double>(mPersonnel));
     list<GridPos> c;
     location().cells(*mGrid, c);
     for (list<GridPos>::iterator it = c.begin(); it != c.end(); it++) {
          er.addCell(mGrid->posToActive(it->r, it->c));
     }
     mEnemyRecords[&killer] = er;
}

/**
 * \brief For debugging purposes.
 *
 * \param o The stream to write to.
 * \param u The Unit to print.
 */
ostream &operator << (ostream& o, const Unit& u)
{
     o << "Reference:  " << u.ref() << endl;
     o << "Parent:     " << (u.mParent ? u.mParent->ref().name() : "No Parent") << endl;
     o << "Personnel:  " << u.mPersonnel << endl;
     return o;
}

