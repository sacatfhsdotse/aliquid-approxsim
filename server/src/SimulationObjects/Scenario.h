#ifndef APPROXSIM_SCENARIO_H
#define APPROXSIM_SCENARIO_H

// System
#include <map>
#include <vector>

// Own
#include "Time2.h"
#include "UpdatableSOAdapter.h"


// Forward Declarations
class Activity;
class Agency;
class AgencyFactory;
class AgencyTeam;
class Buffer;
class City;
class CombatGrid;
class Disease;
class Faction;
class Grid;
class GridDataHandler;
class GridPartitioner;
class Map;
class ModelParameters;
class Reference;
class Region;
class TimeStepper;
class Unit;


/**
 * \brief This class represents the simulation instance of a Scenario.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/10/02 16:05:15 $
 */
class Scenario : public UpdatableSOAdapter {
private:
     /// The TimeStepper.
     TimeStepper* mTimeStepper;

     /// The Map of the scenario.
     Map* mMap;

     /// The simulation grid.
     Grid* mGrid;

     /// The combat grid
     CombatGrid* mCombatGrid;

     /// The number of ethnic factions in the scenario.
     int mNumEthnicFactions;

     /// A vector with all Factions.
     std::vector<Faction*> mFactions;

     /// A vector with all Cities.
     std::vector<City*> mCities;

     /// A vector with the roots of the force hierarchies.
     std::vector<Unit*> mForces;

     /// A vector with all agency teams.
     std::vector<AgencyTeam*> mAgencyTeams;

     /// A vector with all agencies.
     std::vector<Agency*> mAgencies;

     /// A vector with all Activities.
     std::vector<Activity*> mActivities;

     /// A vector with all Regions.
     std::vector<Region*> mRegions;

     /// An pointer to the disease object.
     Disease* mDisease;

     /// An object containing various model parameters.
     ModelParameters* mModelParameters;

     /// Time when the Grid should be updated the next time.
     Time mNextGridUpdate;

     /// The time for the last call to step().
     Time mCurrentTime;

     /// The HDI parameter
     double mHDI;
     /// The unemployment parameter
     double mUnemployment;

     GridDataHandler* mGridDataHandler;

public:
     Scenario(const DataObject& d);
     ~Scenario();
     
     GridDataHandler* takeOverGridDataHandler() const { return mGridDataHandler; }
     void prepareForSimulation(const GridPartitioner& g, const ModelParameters& m, Time startTime);

     void step(Time currentTime);

     void extract(Buffer& b) const;
     void addObject(DataObject& toAdd, int64_t initiator);
     void removeObject(const Reference& toRemove, int64_t initiator);
     void modify(const DataObject& d);
     void reset(const DataObject& d);

     void createCityRegionMapping();
};

#endif   // APPROXSIM_SCENARIO_H
