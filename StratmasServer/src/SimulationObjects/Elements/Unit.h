#ifndef _UNIT_H
#define _UNIT_H


// System
#include <iosfwd>
#include <map>
#include <set>
#include <string>
#include <vector>

// Own
#include "Element.h"
#include "Time2.h"


// Forward Declarations
class Activity;
class Buffer;
class CombatGrid;
class Faction;
class Grid;
class Order;
class PresenceObject;
class PresenceObjectAllocator;
class Reference;
class Shape;
class NavigationPlan;


/**
 * \brief Record for holding information of damage in different cells.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/04/21 15:54:52 $
 */
class EnemyRecord {
private:
     /// The set of cells the damage is inflicted in.
     std::set<int> mCells;

     /// The total damage in the cells this record refers to.
     double mDamage;
public:
     /// Default constructor.
     EnemyRecord() : mDamage(0) {}

     /**
      * \brief Copy constructor.
      *
      * \param er The record to copy.
      */
     EnemyRecord(const EnemyRecord& er) : mCells(er.cells()), mDamage(er.damage()) {}

     /**
      * \brief Accessor for the damage.
      *
      * \return The damage.
      */
     double damage() const { return mDamage; }

     /**
      * \brief Accessor for the set of cells this record refers to.
      *
      * \return The set of cells this record refers to.
      */
     const std::set<int>& cells() const { return mCells; }

     /**
      * \brief Adds a cell to this record.
      *
      * \param cell The index (in the active array) of the cell to add.
      */
     void addCell(int cell) { mCells.insert(cell); }

     /**
      * \brief Adds some damage to this record.
      *
      * \param damage The damage to add.
      */
     void addDamage(double damage) { mDamage += damage; }
};



/**
 * \brief Record for holding information of ambushs.
 *
 * A unit has an AmbushRecord for each cell it should try to perform
 * an ambush in.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/04/21 15:54:52 $
 */
class AmbushRecord {
private:
     /**
      * \brief The set of units that may be affected by an ambush in
      * the cell this AmbushRecord refers to.
      */
     std::set<Unit*> mUnits;

     /**
      * \brief The damage this unit will cause in this cell if an
      * ambush occurs.
      */
     double mDamage;
public:
     /// Default constructor.
     AmbushRecord() : mDamage(0) {}

     /**
      * \brief Copy constructor.
      *
      * \param ar The record to copy.
      */
     AmbushRecord(const AmbushRecord& ar) : mUnits(ar.units()), mDamage(ar.damage()) {}

     /**
      * \brief Checks if this record is active, i.e. if there are any
      * potential victims.
      *
      * \return True if the record is active, false otherwise.
      */
     bool active() const { return !mUnits.empty(); }

     /**
      * \brief Adds a potential victim.
      *
      * \param u The Unit to add.
      */
     void addUnit(Unit* u) { mUnits.insert(u); }

     /**
      * \brief Accessor for the vector containing the potential
      * victims.
      *
      * \return The vector containing the potential victims.
      */
     const std::set<Unit*>& units() const { return mUnits; }

     /**
      * \brief Accessor for the damage.
      *
      * \return The damage.
      */
     double damage() const { return mDamage; }

     /**
      * \brief Adds some damage to this record.
      *
      * \param damage The damage to add.
      */
     void addDamage(double damage) { mDamage += damage; }
     void selectTarget();
};


/**
 * \brief Enumeration for force 'colors'.
 */
enum eForce {
     eBlue,
     eRed,
     eGreen,
     eYellow
};

static const double kMinAmbushPerKm2 = 0.5;


/**
 * \brief This is the class that represents the simulation instance of
 * a military unit.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/04/21 15:54:52 $
 */
class Unit : public Element {
private:
     Unit             *mParent;      ///< The parent of this unit.
     std::string      mSymbolIDCode; ///< The symbol id code.
     std::vector<Unit*> mSubunits;     ///< A vector with this unit's subunits.
     const Reference* mAffiliation;  ///< The affiliation of this unit.
     int              mPersonnel;    ///< The personnel of this unit.
     double           mPersonnelRest;///< Decimal accumulative personnel rest (recovered).
     int              mCasualties;   ///< The current number of casualties.
     double           mCasualtyRest; ///< Decimal accumulative casualty rest.

     double mInitialPersonnel;       ///< The initial personnel of this unit.
     double mStrengthFactor;         ///< The strength factor.
     double mAttackFactor;           ///< The attack factor.
     double mDefenseFactor;          ///< The defense factor.
     double mAttritionCoefficient;   ///< The attrition coefficient.
     double mWithdrawThreshold;      ///< The withdraw threshold.

     double           mVelocity;     ///< The current velocity of this unit.
     double           mMaxVelocity;  ///< The maximum velocity of this unit.
     double           mSqueeze;      ///< Used to compensate for current latitude.
     Time             mDeployTime;   ///< This unit's deploy time.
     Time             mDepartTime;   ///< This unit's depart time.
     bool             mDeployed;     ///< Indicates if this unit has been deployed.
     bool             mDeparted;     ///< Indicates if this unit has departed.
     bool             mMoving;       ///< Indicates if this unit is currently moving.
     Shape*           mGoal;         ///< The current goal of this unit.

     /// Maps a unit to the EnemyRecord for that unit.
     std::map<Unit*, EnemyRecord> mEnemyRecords;

     /// Maps a cell index to the AmbudhRecord for that cell.
     std::map<int, AmbushRecord>  mAmbushRecords;
     
     /**
      * \brief Maps a unit to the EnemyRecord holding the damage
      * caused by ambushs by that unit.
      */
     std::map<Unit*, EnemyRecord> mAmbushExposure;

     std::set<int> mInsurgentCells;       ///< Cells where we were damaged by insurgents during the last time step.
     std::map<Unit*, double> mSpotters;   ///< Stores potential spotters and their 'presence'.
     bool mSearchWithoutFind;             ///< True if we were ambushed during the last time step.
     double mCurrentEnemyStrengthSum;     ///< The sum of the strenth of enemies overlapping our area.
     double mInsurgentRelatedCasualties;  ///< Damage caused by insurgents during the last time step.

     double mModifiedStrength;            ///< The modified strength.
     
     std::vector<Activity*> mActivities;  ///< A vector containing this unit's activities.
     Order* mCurrentOrder;                ///< The order currently executed by this unit.
     Order* mAllocatedOrder;              ///< Holds the address to the latest created order so it can be freed.

     double mAngle;   ///< This unit's current face angle.
     Grid* mGrid;     ///< The Grid in which this unit lives.
     int mColor;      ///< The color of this unit (blue, red etc)
     
     Order* setAllocatedOrder(Order* order);
     Order* setOrder(Time simTime);
     void setModifiedStrength();
     void setUpSearchAndDestroy(Time simTime);
//     void myMove();
     Order* createRetreatOrder();
     void exposeForAttack(double modifiedStrength, Unit& attacker);
     void exposeForAmbush(Unit& u, int cell, double damage);
     bool attacker() const;
     bool defender() const;
     double attackDefendFactor() const;
     void recover();

public:
     Unit(const DataObject& d);
     virtual ~Unit();

     void prepareForSimulation(Grid& grid, Time currentTime);
     void extract(Buffer& b) const;
     void addObject(DataObject& toAdd, int64_t initiator);
     void removeObject(const Reference& toRemove, int64_t initiator);
     void modify(const DataObject& d);
     void reset(const DataObject& d);


     /**
      * \brief Adds a subunit to this unit.
      *
      * \param sub The subunit to add.
      */
     void addSubunit(Unit *sub) { mSubunits.push_back(sub); sub->mParent = this; }
     
     /**
      * \brief Returns the parent of this unit or null if it has no
      * parent.
      *
      * \return The parent of this unit or null if it has no parent.
      */
     Unit* parent() const { return mParent; }

     /**
      * \brief Accessor for the subunit vector.
      *
      * \return The subunit vector.
      */
     const std::vector<Unit*>& subunits() const { return mSubunits; }

     /**
      * \brief Accessor for the personnel.
      *
      * \return The personnel.
      */
     int personnel() const { return mPersonnel; }

     Faction& faction() const;

     /**
      * \brief Accessor for the casualties.
      *
      * \return The casualties.
      */
     int casualties() const { return mCasualties; }

     /**
      * \brief Accessor for the strength.
      *
      * \return The strength.
      */
     double strength() const { return mPersonnel * mStrengthFactor; }

     /**
      * \brief Accessor for the initial strength.
      *
      * \return The initial strength.
      */
     double initialStrength() const { return mInitialPersonnel * mStrengthFactor; }

     /**
      * \brief Accessor for the modified strength.
      *
      * \return The modified strength.
      */
     double modifiedStrength() const { return mModifiedStrength; }

     /**
      * \brief Accessor for the deploy time.
      *
      * \return The deploy time.
      */
     Time deployTime() const { return mDeployTime; }

     /**
      * \brief Accessor for the depart time.
      *
      * \return The depart time.
      */
     Time departTime() const { return mDepartTime; }

     /**
      * \brief Accessor for the deployed flag.
      *
      * \return The status of the deployed flag.
      */
     bool deployed() const { return mDeployed; }

     /**
      * \brief Accessor for the departed flag.
      *
      * \return The status of the departed flag.
      */
     bool departed() const { return mDeparted; }

     /**
      * \brief Accessor for the color.
      *
      * \return The color of this unit.
      */
     int color() const { return mColor; }

     /**
      * \brief Checks if this unit is capable of performing its
      * activities.
      *
      * A unit that has lost more that mWithdrawThreshold percent of
      * its total force strength is no longer considered capable.
      *
      * \return True if this unit is capable, fals otherwise.
      */
     bool capable() const {
          return (initialStrength() > 0 && strength() / initialStrength() * 100 > mWithdrawThreshold);
     }

     /**
      * \brief Accessor for the goal.
      *
      * \return The goal.
      */
     const Shape* goal() const { return mGoal; }

     /**
      * \brief Checks if this unit is currently present in the
      * simulation, i.e. deployed and not departed.
      *
      * \return True if this unit is present, false otherwise.
      */
     bool present() const { return (deployed() && !departed()); }

     /**
      * \brief Deploy this unit.
      */
     void deploy() { mDeployed = true; }

     /**
      * \brief Depart this unit.
      */
     void depart() { mDeparted = true; }

     bool untouchable() const;

     void setup(Time simTime);
     void act(Time simTime);

     void setGoal(const Shape& goal);
     void setVelocity(double frac = 0.5);
     void move();
     bool combat();

     bool combatSituation();
     bool criticalInsurgentSituation();
     bool searching();
     PresenceObject* getPresence(PresenceObjectAllocator& poa, int cellIndex, double fraction = 1.0);
     bool isHostileTowards(const Unit& u) const;
     void registerCombat(CombatGrid& cg);
     double registerInsurgentImpact(int cell, double impact);
     void registerEnemy(const PresenceObject& p);
     void registerPotentialAmbush(PresenceObject& victim, double myFraction);
     void setLocation(const Shape& area);

     void registerSpotter(const PresenceObject& p);
     bool isSpotted();
     void kill(Unit& killer);
          
     // Friends
     friend std::ostream &operator << (std::ostream& o, const Unit& u);
};

#endif  // _UNIT_H

// vim: ts=5 sw=5 expandtab:
