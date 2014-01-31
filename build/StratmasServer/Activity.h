#ifndef STRATMAS_ACTIVITY_H
#define STRATMAS_ACTIVITY_H


// System
#include <iosfwd>
#include <map>
#include <vector>

// Own
#include "GridEffect.h"
#include "ProcessVariables.h"
#include "Time.h"
#include "UpdatableSOAdapter.h"

// Forward Declarations
class Buffer;
class DataObject;
class Element;
class Faction;
class Grid;
class Shape;
class Unit;
class Update;


/**
 * \brief Abstract super class for all activities.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/19 07:04:26 $
 */
class Activity : public UpdatableSOAdapter {
protected:
     bool mActive;  ///< Indicates if this activity is currently executed.
     Time mStart;   ///< The start time.

public:
     Activity();
     Activity(const DataObject& d);

     /**
      * \brief Destructor.
      */
     virtual ~Activity() {}

     virtual void prepareForSimulation(Grid& g, Time currentTime);
     
     virtual void extract(Buffer& b) const;
     virtual void modify(const DataObject& d);
     virtual void reset(const DataObject& d);

     /**
      * \brief Accessor for the start time.
      *
      * \return The start time.
      */
     Time startTime() const { return mStart; }

     /**
      * \brief Accessor for the area.
      *
      * \return The area or null if this activity does not have an
      * area.
      */
     virtual Shape* location() const = 0;

     /**
      * \brief Checks if this activity is active at time t.
      *
      * \param t The time for which to check.
      * \return True if this activity is active at the specified time.
      */
     virtual bool isActive(Time t) = 0;

     /**
      * \brief Performs this Activity.
      *
      * \param e The Element that should perform this Activity.
      * \param fraction The fraction of the performers total capacity
      * that this activity is performed with.
      */
     virtual void perform(Element* e, double fraction = 1.0) = 0;
};

/**
 * \brief Function object for less-than operator for pointer to
 * Activities
 *
 * An Activity is less than another Activity if its start time is
 * earlier than the other Activitiy's.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/19 07:04:26 $
 */
struct lessActivityPointer {
     /**
      * \brief Less-than operator for pointers to Activities.
      *
      * \param a1 The first Activity.
      * \param a2 The second Activity.
      * \return True if the first Activity is less than the other
      * Activity, false otherwise.
      */
     bool operator()(const Activity* const a1, const Activity* const a2) const {
	  if (!a1 || !a2) {
	       return false;
	  }
	  else {
	       return (a1->startTime() < a2->startTime());
	  }
     }
};


/**
 * \brief Abstract super class for all orders.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/19 07:04:26 $
 */
class Order : public Activity {
protected:
     bool mCarriedOut;  ///< Indicates if this order has been executed.
     Shape* mLocation;  ///< The location of this order (nullable).

public:
     /// Default constructor.
     Order() : mCarriedOut(false), mLocation(0) {}

     /**
      * \brief Creates an Order from the provided DataObject.
      *
      * \param d The DataObject to use for construction.
      */
     Order(const DataObject& d);
     virtual ~Order();

     /**
      * \brief Checks if this activity is active at time t.
      *
      * \param t The time for which to check.
      * \return True if this activity is active at the specified time.
      */
     virtual bool isActive(Time t) { return mActive = (t > mStart && !isCarriedOut()); }

     /**
      * \brief Accessor for the area.
      *
      * \return The area or null if this activity does not have an
      * area.
      */
     Shape* location() const { return mLocation; }

     /**
      * \brief Checks if this order is carried out
      *
      * \return True if this order is carried out, false otherwise.
      */
     virtual bool isCarriedOut() { return mCarriedOut; }

     virtual void extract(Buffer& b) const;
     virtual void addObject(DataObject& toAdd, int64_t initiator);
     virtual void removeObject(const Reference& toRemove, int64_t initiator);
     virtual void replaceObject(DataObject& newObject, int64_t initiator);
     virtual void modify(const DataObject& d);
     virtual void reset(const DataObject& d);

     /**
      * \brief Accessor for the combat factor.
      *
      * \return The combat factor.
      */
     virtual double combatFactor() const = 0;
};


/**
 * \brief The CustomPVModification activity.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/19 07:04:26 $
 */
class CustomPVModification : public Order {
protected:
     Grid*                   mGrid;     ///< The grid.
     Time                    mEnd;      ///< The end time.
     const Reference*        mTarget;   ///< Reference to the target Faction.
     std::vector<GridEffect> mEffects;  ///< Vector of effects.

     /// String representation of the type of this Activity.
     std::string mType;

     /// The combat factor of this Activity.
     double mCombatFactor;

     /// Map that maps the PV name to the severity of the impact.
     std::map<std::string, double> mSeverities;
public:
     CustomPVModification(const DataObject& d);
     virtual ~CustomPVModification() {}

     void prepareForSimulation(Grid& g, Time currentTime);
     void createEffects();

     virtual void extract(Buffer& b) const;
     virtual void addObject(DataObject& toAdd, int64_t initiator);
     virtual void removeObject(const Reference& toRemove, int64_t initiator);
     virtual void modify(const DataObject& d);
     virtual void reset(const DataObject& d);

     /**
      * \brief Accessor for the end time.
      *
      * \return The emd time.
      */
     Time endTime() const { return mEnd; }

     /**
      * \brief Checks if this activity is active at time t.
      *
      * \param t The time for which to check.
      * \return True if this activity is active at the specified time.
      */
     virtual bool isActive(Time t) { mCarriedOut = (t > mEnd); return mActive = (t > mStart && t <= mEnd); }

     /**
      * \brief Adds an effect to this activity.
      *
      * \param a The effect to add.
      * \param si The magnitude of the effect to add.
      * \param f The affected faction.
      */
     void addEffect(eAllPV a, double si, EthnicFaction* f) { mEffects.push_back(GridEffect(a, si, f)); }

     /**
      * \brief Performs this Activity.
      *
      * \param e The Element that should perform this Activity.
      * \param fraction The fraction of the performers total capacity
      * that this activity is performed with.
      */
     virtual void perform(Element* e, double fraction = 1.0);

     virtual double combatFactor() const { return mCombatFactor; }

     // Friends
     friend std::ostream &operator << (std::ostream& o, const CustomPVModification& a);
};


/**
 * \brief The TerroristAttackOrder
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/19 07:04:26 $
 */
class TerroristAttackOrder : public CustomPVModification {
private:
     /// The time for the attack
     Time mActionTime;

     /// True if the simulation time has passed the action time.
     bool mTimeToPerform;

     /// True if the attack has been performed.
     bool mAttackPerformed;
public:
     TerroristAttackOrder(const DataObject& d);

     /**
      * \brief Accessor for the action time
      *
      * \return The action time.
      */
     Time actionTime() const { return mActionTime; }

     /**
      * \brief Checks if this activity is active at time t.
      *
      * \param t The time for which to check.
      * \return True if this activity is active at the specified time.
      */
     bool isActive(Time t) { mTimeToPerform = (t > actionTime()); return CustomPVModification::isActive(t); }

     void extract(Buffer &b) const;
     void modify(const DataObject& d);
     void reset(const DataObject& d);
     void perform(Element* e, double fraction = 1.0);
};



/**
 * \brief The AttackOrder
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/19 07:04:26 $
 */
class AttackOrder : public Order {
public:
     /// Default constructor.
     AttackOrder() {}

     /**
      * \brief Creates an AttackOrder from the provided DataObject.
      *
      * \param d The DataObject to create this order from.
      */
     AttackOrder(const DataObject& d) : Order(d) {}

     void perform(Element* e, double fraction = 1.0);
     double combatFactor() const { return 1; }
};


/**
 * \brief The DefendOrder
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/19 07:04:26 $
 */
class DefendOrder : public Order {
protected:
     Time mEnd;   ///< The end time.

public:
     DefendOrder() {}
     DefendOrder(const DataObject& d);

     void extract(Buffer& b) const;
     void modify(const DataObject& d);
     void reset(const DataObject& d);

     /**
      * \brief Checks if this activity is active at time t.
      *
      * \param t The time for which to check.
      * \return True if this activity is active at the specified time.
      */
     bool isActive(Time t) { mCarriedOut = (t > mEnd); return mActive = (t > mStart && t <= mEnd); }
     void perform(Element* e, double fraction = 1.0);
     double combatFactor() const { return 1; }
};


/**
 * \brief The AmbushOrder
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/19 07:04:26 $
 */
class AmbushOrder : public Order {
protected:
     int mState;          ///< The state of this AmbushOrder.

     /**
      * \brief True when time is in ambush intervall or when time is
      * after mStartTime and oneAmbush() == true and
      * mOneAmbushPerformed == false.
      */
     bool mTimeForAmbush; 

     /**
      * \brief If this is a 'oneAmbush' order e.g. if mStartAmbush and
      * mEndAmbush are equal, then this flag is set to true when one
      * ambush has been performed.
      */
     bool mOneAmbushPerformed;

     Time mEnd;           ///< The end time of the order (the 'hide' state)
     Time mStartAmbush;   ///< The start time for the ambush activity.
     Time mEndAmbush;     ///< The end time for the ambush activity.

public:
     /**
      * \brief Enumeration for the state of an AmbushOrder.
      */
     enum eAmbushState {eHide, eAmbush};

     AmbushOrder(const DataObject& d);

     void extract(Buffer& b) const;
     void modify(const DataObject& d);
     void reset(const DataObject& d);

     bool isActive(Time t);

     /**
      * \brief Checks if the startAmbush and endAmbush times are equal.
      *
      * \return True if the startAmbush and endAmbush times are equal.
      */
     bool oneAmbush() const { return (mStartAmbush == mEndAmbush); }

     void perform(Element* e, double fraction = 1.0);

     /**
      * \brief Accessor for the combat factor.
      *
      * \return The combat factor.
      */
     double combatFactor() const { return 1; }

     /**
      * \brief Accessor for the state.
      *
      * \return The state.
      */
     int state() const { return (mTimeForAmbush ? eAmbush : eHide); }
};



/**
 * \brief The GoToOrder.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/19 07:04:26 $
 */
class GoToOrder : public Order {
public:
     GoToOrder(const Shape& location);
     /**
      * \brief Creates a GoToOrder from the provided DataObject.
      *
      * \param d The DataObject to use for construction.
      */
     GoToOrder(const DataObject& d) : Order(d) {}

     virtual void perform(Element* e, double fraction = 1.0);
     double combatFactor() const { return 0.1; }
};


/**
 * \brief The RetreatOrder. This order only exists on the server side.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/19 07:04:26 $
 */
class RetreatOrder : public GoToOrder {
private:
     int mState;   ///< The state of the RetreatOrder.

public:
     /**
      * \brief Enumeration for RetreatOrder state. The eRetreat state
      * means that the unit is retreating x km in a random
      * direction. The eReturn state means that the unit has retreated
      * x km in a random direction and is now on its way towards its
      * closest superior (untouchable and with half ordinary velocity).
      */
     enum eRetreatState {eRetreat, eReturn};

     /**
      * \brief Constructor used internally by the server
      *
      * \param location The location to retreat to.
      */
     RetreatOrder(const Shape& location) : GoToOrder(location), mState(eRetreat) {}
     void perform(Element* e, double fraction = 1.0);

     /**
      * \brief Accessor for the state.
      *
      * \return The state.
      */
     int state() const { return mState; }
     Unit* findClosestParentWithSameFaction(Unit& u);
};


/**
 * \brief The SearchOrder for searching for TerroristAttacking
 * units. This order only exists on the server side.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/19 07:04:26 $
 */
class SearchOrder : public Order {
protected:
     Time mEnd;   ///< The end time.
public:
     /**
      * \brief Constructor.
      *
      * \param end The end time of the order.
      */
     SearchOrder(Time end) : mEnd(end) {}

     /**
      * \brief Checks if this activity is active at time t.
      *
      * \param t The time for which to check.
      * \return True if this activity is active at the specified time.
      */
     bool isActive(Time t) { mCarriedOut = (t > mEnd); return mActive = (t > mStart && t <= mEnd); }

     void perform(Element* e, double fraction = 1.0) {}
     double combatFactor() const { return 1.0; }
};

#endif  // STRATMAS_ACTIVITY_H
