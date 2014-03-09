#ifndef STRATMAS_AGENCYTEAM_H
#define STRATMAS_AGENCYTEAM_H

//System
#include <iosfwd>
#include <vector>

// Own
#include "Element.h"
#include "GridEffect.h"
#include "LatLng.h"
#include "Time2.h"

// Forward Declarations
class Agency;
class Buffer;
class Camp;
class Grid;
class GridDataHandler;


/**
 * \brief Abstract class that is inherited by all AgencyTeams.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/10/10 09:35:59 $
 */
class AgencyTeam : public Element {
protected:
     Agency* mAgency;   ///< The Agency this team belongs to.

     /// If goal is a camp this pointer points at that camp.
     Camp *mCamp;

     /// Goal that team moves towards.
     LatLng mGoal;

     /// Day when team starts operating.
     Time mStartTime;

     /// Team's capacity in persons per day.
     double mCapacityPPD;

     /// Response time in seconds.
     double mResponseTimeSecs;

     /// A reference to the Grid.
     Grid* mGrid;     

     /// A reference to the GridDataHandler.
     const GridDataHandler* mGridDataHandler;     

     /// True if the team has been given a start time
     bool mHasStartTime;
     
     /// The team will not go where violence > mViolenceThreshold.
     double mViolenceThreshold;

     /// Flag indicating whether or not this team is deployed.
     bool mDeployed;

     /// Flag indicating whether or not this team has departed.
     bool mDeparted;

     /// The deploy time.
     Time mDeployTime;

     /// The depart time.
     Time mDepartTime;

     /**
      * \brief Flag indicating the behavior of the team.
      *
      * A 'true' value means that the team behaves as in old Stratmas,
      * i.e jumps around to the places where the clustering algorithm
      * detects clusters of 'problem cells'. A 'false' value means
      * that the team will stand still on the initial location given
      * to it.
      */
     bool mOwnInitiative;

protected:
     bool canWorkAt(Shape& loc) const;

public:
     AgencyTeam(const DataObject& d);

     /// Destructor
     virtual ~AgencyTeam() {};

     void deploy();

     /**
      * \brief Makes the team depart.
      */
     void depart() { mDeparted = true; }

     /**
      * \brief Accessor for the deployed flag.
      *
      * \return The state of the deployed flag..
      */
     bool deployed() const { return mDeployed; }

     /**
      * \brief Accessor for the departed flag.
      *
      * \return The state of the departed flag..
      */
     bool departed() const { return mDeparted; }

     /**
      * \brief Accessor for the own initiative flag.
      *
      * \return The state of the own initiative flag..
      */
     bool ownInitiative() const { return mOwnInitiative; }

     /**
      * \brief Sets the Agency this team belons to.
      *
      * \param agency The Agency this team shoul belong to.
      */
     void setAgency(Agency* agency) { mAgency = agency; }

     /**
      * \brief Prepares this SimulationObject for simulation.
      *
      * Should be called after creation and reset and before the
      * simulation starts.
      *
      * \param grid The Grid.
      */
     void prepareForSimulation(Grid& grid, const GridDataHandler& gdh) { mGrid = &grid; mGridDataHandler = &gdh; }

     virtual void extract(Buffer &buf) const;
     void addObject(DataObject& toAdd, int64_t initiator);
     void removeObject(const Reference& toRemove, int64_t initiator);
     void modify(const DataObject& d);
     void reset(const DataObject& d);

     /**
      * \brief Returns true if this team is operational, i.e. has been
      * given a start time and a goal.
      *
      * \return True if this team is operational, i.e. has been given
      * a start time and a goal, false otherwise.
      */
     bool operational() const { return present() && hasStartTime() && !mGoal.nowhere(); }

     /**
      * \brief Checks if this AgencyTeam is currently present in the
      * simulation, i.e. if it is operational.
      *
      * \return True if this AgencyTeam is present, false otherwise.
      */
     bool present() const { return mDeployed && !mDeparted; }

     /**
      * \brief Returns true if this team has been given a start time.
      *
      * \return True if this team has been given a start time.
      */
     bool hasStartTime() const { return mHasStartTime; }

     /**
      * \brief Accessor for the start time.
      *
      * \return The start time.
      */
     Time startTime() const { return mStartTime; }

     /**
      * \brief Mutator for the start time.
      *
      * \param t The start time.
      */
     void setStartTime(Time t) { mStartTime = t; mHasStartTime = true; }

     void setGoal(LatLng goal);
     void setGoal(Camp *camp);

     /**
      * \brief Mutator for the team's capacity.
      *
      * \param cap The team's capacity
      */
     void setCapacity(double cap) { mCapacityPPD = cap; }

     /**
      * \brief Accessor for the team's capacity.
      *
      * \return cap The team's capacity
      */
     double getCapacity() const { return mCapacityPPD; }

     /**
      * \brief Accessor for the team's responseTime.
      *
      * \return cap The team's responseTime
      */
     double getResponseTime() const { return mResponseTimeSecs; }

     Time getDeployTime() const { return mDeployTime; }
     Time getDepartTime() const { return mDepartTime; }

     /**
      * \brief Calculates the current need of this team. Overridden by
      * teams that have need-based behaviour.
      *
      * \return The current need of this team.
      */
     virtual double calculateNeed() { return 0; }

     /**
      * \brief Performs the actions of this team. Must be implemented
      * by all subclasses.
      *
      * \param now The current simulation time.
      */
     virtual void act(Time now) = 0;

     friend std::ostream& operator << (std::ostream& o, const AgencyTeam& a);
};

/**
 * \brief Class representing a FoodAgencyTeam.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/10/10 09:35:59 $
 */
class FoodAgencyTeam : public AgencyTeam {
public:
     /**
      * \brief Constructor that creates an AgencyTeam from the provided
      * DataObject.
      *
      * \param d The DataObject to create this object from.
      */
     FoodAgencyTeam(const DataObject& d) : AgencyTeam(d) {}
     double calculateNeed();
     void act(Time now);
};



/**
 * \brief Class representing a WaterAgencyTeam.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/10/10 09:35:59 $
 */
class WaterAgencyTeam : public AgencyTeam {
private:
     /// Fraction of the agency's total capacity that this team has
     double mRepairCapacity;

public:
     /**
      * \brief Constructor that creates an AgencyTeam from the provided
      * DataObject.
      *
      * \param d The DataObject to create this object from.
      */
     WaterAgencyTeam(const DataObject& d) : AgencyTeam(d), mRepairCapacity(0) {}
     double calculateNeed();
     void act(Time now);
};



/**
 * \brief Class representing a ShelterAgencyTeam.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/10/10 09:35:59 $
 */
class ShelterAgencyTeam : public AgencyTeam {
public:
     /**
      * \brief Constructor that creates an AgencyTeam from the provided
      * DataObject.
      *
      * \param d The DataObject to create this object from.
      */
     ShelterAgencyTeam(const DataObject& d) : AgencyTeam(d) {}
     void act(Time now);
};



/**
 * \brief Class representing a HealthAgencyTeam.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/10/10 09:35:59 $
 */
class HealthAgencyTeam : public AgencyTeam {
public:
     /**
      * \brief Constructor that creates an AgencyTeam from the provided
      * DataObject.
      *
      * \param d The DataObject to create this object from.
      */
     HealthAgencyTeam(const DataObject& d) : AgencyTeam(d) {}
     double calculateNeed();
     void act(Time now);
};



/**
 * \brief Class representing a PoliceAgencyTeam.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/10/10 09:35:59 $
 */
class PoliceAgencyTeam : public AgencyTeam {
public:
     /**
      * \brief Constructor that creates an AgencyTeam from the provided
      * DataObject.
      *
      * \param d The DataObject to create this object from.
      */
     PoliceAgencyTeam(const DataObject& d) : AgencyTeam(d) {}
     void act(Time now);
};



/**
 * \brief Class representing a CustomAgencyTeam.
 *
 * A CustomAgenyTeam may be set to affect any combination of
 * modifiable process variables in the area it occupies. The
 * functionality is very similar to the CustomPVModification.
 *
 * This type of team was not present in old Stratmas. It was
 * introduced to meet requirements that arose before the Demo06
 * exercise during the autumn 2006.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/10/10 09:35:59 $
 */
class CustomAgencyTeam : public AgencyTeam {
private:
     const Reference*        mTarget;   ///< Reference to the target Faction.
     std::vector<GridEffect> mEffects;  ///< Vector of effects.

     /// Map that maps the PV name to the severity of the impact.
     std::map<std::string, double> mSeverities;

     void createEffects();

public:
     /**
      * \brief Constructor that creates an AgencyTeam from the provided
      * DataObject.
      *
      * \param d The DataObject to create this object from.
      */
     CustomAgencyTeam(const DataObject& d);

     /**
      * \brief Adds an effect to this activity.
      *
      * \param a The effect to add.
      * \param si The magnitude of the effect to add.
      * \param f The affected faction.
      */
     void addEffect(eAllPV a, double si, EthnicFaction* f) { mEffects.push_back(GridEffect(a, si, f)); }

     virtual void extract(Buffer& b) const;
     virtual void addObject(DataObject& toAdd, int64_t initiator);
     virtual void removeObject(const Reference& toRemove, int64_t initiator);
     virtual void modify(const DataObject& d);
     virtual void reset(const DataObject& d);

     void act(Time now);
};

#endif   // STRATMAS_AGENCYTEAM_H
