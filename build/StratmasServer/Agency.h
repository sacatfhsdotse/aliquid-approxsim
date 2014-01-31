#ifndef STRATMAS_AGENCY_H
#define STRATMAS_AGENCY_H


// System
#include <iosfwd>
#include <string>
#include <vector>

// Own
#include "Time.h"

// Forward Declarations
class AgencyTeam;
class Buffer;
class Grid;
class LatLng;
class Reference;


/**
 * \brief Abstract class containing the basic functionality for a
 * Stratmas Agency.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/10/02 16:01:25 $
 */
class Agency {
protected:
     /// Simulation time when last updated.
     Time mLastUpdate;

     /**
      * \brief The number of days between rechecking of the need,
      * i.e. applying the cluster algorithm to the grid
      */
     int mIntervallDays;

     /// The type of this agency as a string.
     std::string mType;

     /// Vector containing this Agency's teams.
     std::vector<AgencyTeam*> mTeams;

     /// The number of teams in this Agency.
     int mNumTeams;

     /// The total capacity of this Agency in persons per day.
     double mCapacityPPD;

     /// The response time in days.
     int mResponseDays;

     /// A reference to the Grid.
     Grid& mGrid;

     /// Array used for clustering. Keeps track of which cell that belongs to which cluster.
     int* mVindex;

     /// Array used for clustering. Keeps track of the weights for different cells.
     double* mVw;

     /**
      * \brief Should return true if there is a severe problem of the
      * type a certain Agency is interesed in.
      *
      * \return True if there is a severe problem of the type this
      * Agency is interested in, false otherwise.
      */
     virtual bool severeProblem() = 0;
     int cluster(int inNumClusters, std::vector<LatLng>& outCenters);
     void orderTeamsToClusters(int nTeams, int firstTeam);
     virtual void setTeamsGoals();

public:
     Agency(const std::vector<AgencyTeam*>& teams, Grid& g);
     virtual ~Agency();
     void addTeam(AgencyTeam& team);
     void removeTeam(AgencyTeam& team);
     void aggregateCapacity();
     virtual void act(Time now);
     friend std::ostream& operator << (std::ostream& o, const Agency& a);
};



/**
 * \brief Class containing functionality for controlling FoodAgencyTeams.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/10/02 16:01:25 $
 */
class FoodAgency : public Agency {
private:
     bool severeProblem();
    
public:
     /**
      * \brief Constructor.
      *
      * \param teams A vector containing this Agency's teams.
      * \param g A reference to the Grid.
      */
     FoodAgency(const std::vector<AgencyTeam*>& teams, Grid& g) : Agency(teams, g) {}
};



/**
 * \brief Class containing functionality for controlling WaterAgencyTeams.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/10/02 16:01:25 $
 */
class WaterAgency : public Agency {
private:
     bool severeProblem();
    
public:
     /**
      * \brief Constructor.
      *
      * \param teams A vector containing this Agency's teams.
      * \param g A reference to the Grid.
      */
     WaterAgency(const std::vector<AgencyTeam*>& teams, Grid& g) : Agency(teams, g) {}
};



/**
 * \brief Class containing functionality for controlling ShelterAgencyTeams.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/10/02 16:01:25 $
 */
class ShelterAgency : public Agency {
private:
     /// The number of operational teams.
     int  mOperationalTeams;
     bool severeProblem();
    
public:
     /**
      * \brief Constructor.
      *
      * \param teams A vector containing this Agency's teams.
      * \param g A reference to the Grid.
      */
     ShelterAgency(const std::vector<AgencyTeam*>& teams, Grid& g) : Agency(teams, g), mOperationalTeams(0) {}
     void act(Time now);
};



/**
 * \brief Class containing functionality for controlling HealthAgencyTeams.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/10/02 16:01:25 $
 */
class HealthAgency : public Agency {
private:
     bool severeProblem();
    
public:
     /**
      * \brief Constructor.
      *
      * \param teams A vector containing this Agency's teams.
      * \param g A reference to the Grid.
      */
     HealthAgency(const std::vector<AgencyTeam*>& teams, Grid& g) : Agency(teams, g) {}
};



/**
 * \brief Class containing functionality for controlling PoliceAgencyTeams.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/10/02 16:01:25 $
 */
class PoliceAgency : public Agency {
private:
     bool severeProblem();
    
public:
     /**
      * \brief Constructor.
      *
      * \param teams A vector containing this Agency's teams.
      * \param g A reference to the Grid.
      */
     PoliceAgency(const std::vector<AgencyTeam*>& teams, Grid& g) : Agency(teams, g) {}
     void setTeamsGoals();
};



/**
 * \brief Class containing functionality for controlling CustomAgencyTeams.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/10/02 16:01:25 $
 */
class CustomAgency : public Agency {
private:
     bool severeProblem();
    
public:
     /**
      * \brief Constructor.
      *
      * \param teams A vector containing this Agency's teams.
      * \param g A reference to the Grid.
      */
     CustomAgency(const std::vector<AgencyTeam*>& teams, Grid& g) : Agency(teams, g) {}
     void setTeamsGoals();
     void act(Time now);
};


#endif   // STRATMAS_AGENCY_H
