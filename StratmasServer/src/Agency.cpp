// System
#include <ostream>
#include <cstdlib>
#include <cstring>

// Own
#include "AgencyTeam.h"
#include "Agency.h"
#include "Camp.h"
#include "ClusterSet.h"
#include "debugheader.h"
#include "Ellipse.h"
#include "GridCell.h"
#include "Grid.h"
#include "Mapper.h"  // For operator << 
#include "ProcessVariables.h"
#include "random.h"
#include "SOFactory.h"
#include "StratmasConstants.h"
#include "Type.h"
#include "LogStream.h"

// Temporary
#include "Shape.h"

using namespace std;


/**
 * \brief Constructor.
 *
 * \param teams A vector containing this Agency's teams.
 * \param g A reference to the Grid.
 */
Agency::Agency(const std::vector<AgencyTeam*>& teams, Grid& g)
     : mIntervallDays(1), mTeams(teams), mNumTeams(teams.size()), mCapacityPPD(0), mResponseDays(0), mGrid(g),
       mVindex(new int[mGrid.active()]), mVw(new double [mGrid.active()])
{
     aggregateCapacity();
     for(vector<AgencyTeam*>::const_iterator it = mTeams.begin(); it != mTeams.end(); it++) {
          (*it)->setAgency(this);
     }
}

/**
 * \brief Destructor.
 */
Agency::~Agency()
{
     if (mVindex) { delete [] mVindex; }
     if (mVw    ) { delete [] mVw    ; }
}

/**
 * \brief Adds a team to this Agency.
 *
 * \param team The team to add.
 */
void Agency::addTeam(AgencyTeam& team)
{
     mTeams.push_back(&team);
     mNumTeams++;
     mCapacityPPD += team.getCapacity();
     aggregateCapacity();
}

/**
 * \brief Removes a team from this Agency.
 *
 * Notice that the team itself is not deleted.
 *
 * \param team The team to remove.
 */
void Agency::removeTeam(AgencyTeam& team)
{
     mTeams.erase(find(mTeams.begin(), mTeams.end(), &team));
     mNumTeams--;
     aggregateCapacity();
}

/**
 * \brief Updates the total capacity and response time for this Agency.
 */
void Agency::aggregateCapacity()
{
     mCapacityPPD = 0;
     double resp = 0;
     for(vector<AgencyTeam*>::const_iterator it = mTeams.begin(); it != mTeams.end(); it++) {
          mCapacityPPD  += (*it)->getCapacity();
          resp += (*it)->getResponseTime();
     }
     
     // Convert responseTimeSecs to a mean responseDays value for the entire agency
     mResponseDays = static_cast<int>(resp / static_cast<double>(mTeams.size()) / (24.0 * 3600.0));
}

/**
 * \brief Finds centers of clusters of the specified type.
 *
 * This function requires that mVindex and mVw is set, which is done
 * by the severeProblem() function in the respective subclass.
 *
 * \param inNumClusters The maximum number of clusters to be sought for.
 * \param outCenters An array of positions indicating where the
 * centers of the found clusters are located.
 * \return The number of clusters found. Not necessarily the same as
 * inNumClusters.
 */
int Agency::cluster(int inNumClusters, std::vector<LatLng>& outCenters)
{
     // Perform the clustering:
//     debug("Clustering for " << type << "Agency");
     ClusterSet zones(inNumClusters);
     int outNumClusters = zones.FitToData(mGrid.active(), mVindex, mGrid.cellCenterCoordsX(),
                                          mGrid.cellCenterCoordsY(), mVw);
     //        Pass back the centers of the clusters:
     for (int i = 0; i < outNumClusters; i++) {
          Ellipse &e = zones.mCluster[i];
          outCenters[i] = LatLng(e.my, e.mx);
     }
/*
     debug("got " << outNumClusters << " clusters";
     if (outNumClusters > 0) {
          debug(" with centerpoints at:");
          for (int i = 0; i < outNumClusters; i++) {
               debug(outCenters[i].lat() << ", " << outCenters[i].lng());
          }
     }
     else {
          debug(endl;
     }
*/     
     return outNumClusters;
}

/**
 * \brief Assign teams to clusters.
 *
 * If there are more teams than clusters - assign more than one team
 * to each cluster.
 *
 * \param nTeams Numer of teams to assign.
 * \param firstTeam Index in the mTeams vector of the first team to
 * assign.
 */
void Agency::orderTeamsToClusters(int nTeams, int firstTeam)
{
     if (firstTeam + nTeams > mNumTeams) {
          Error e;
          e << "Tried to order more teams than it exists to clusters in Agency::orderTeamsToClusters() "
            << "Number of teams: " << mNumTeams << ", ordered teams: " << firstTeam + nTeams;
          throw e;
     }

     vector<LatLng> clusters(nTeams);
     int nClusters = cluster(nTeams, clusters);
     if (nClusters > 0) {
          for (int i = 0; i < nTeams; i++) {
               AgencyTeam &team = *mTeams[firstTeam + i];
               //P Added this in order to avoid null cell for food team when
               //  there are more teams than clusters, e.g. assign more than
               //  one team to each cluster.
               int j = i % nClusters;
               if (!mGrid.cell(clusters[j])) {
                    slog << "Null cell for " << mType << "AgencyTeam " << i << " at position lat "
                         << clusters[j].lat() << ", lng " <<  clusters[j].lng()
                         << " in orderTeamsToClusters(), e.g. cluster center outside active grid" << logEnd;
               }
               else {
//                    if (team.canGoTo(clusters[j])) {
                         team.setGoal(clusters[j]);   // Set team goal to cluster center
//                     }
//                     else {
//                          debug("team " << team.ref().name() << " can't go to " << clusters[j]);
//                     }
               }
          }
     }
}

/**
 * \brief Agency default behaviour for positioning teams.
 *
 * The default behaviour is as follows: Assign one team to each
 * camp. After that, if we have a severe problem - assign excess teams
 * to clusters. If we have more excess teams than clusters - assign
 * more than one team to each cluster. Both food, water, health and
 * shelter agencies follow this behaviour.
 */
void Agency::setTeamsGoals()
{
     int nCamps = mGrid.camps();

     // First assign one team to each camp
     for (int i = 0; i < nCamps && i < mNumTeams; i++) {
          AgencyTeam &team = *mTeams[i];
          Camp *thisCamp = mGrid.camp(i);
          team.setGoal(thisCamp->center());
     }

     // If there are more teams than camps
     if (mNumTeams > nCamps) {
          int excessTeams = mNumTeams - nCamps;

          // If we have a severe problem, then assign excess teams to clusters
          if (severeProblem()) {
               orderTeamsToClusters(excessTeams, nCamps);
          }
          // ...else assign excess teams to camps too, unless there are no camps
          // at all, in which case we do nothing.
          else if (nCamps > 0) {
               for (int i = 0; i < excessTeams; i++) {
                    AgencyTeam &team = *mTeams[nCamps + i];
                    Camp *thisCamp = mGrid.camp(i % nCamps);
                    team.setGoal(thisCamp->center());
               }
          }
     }
}

/**
 * \brief Agency over all default behaviour
 *
 * The default behaviour is as follows:
 * <p>
 * For all teams that are not operational - set their start time.
 * <p>
 * For operational teams - Check if current time is later than their
 * start time and if it is - consider them active teams.
 * <p>
 * For all active teams - calculate their need, divide the resources
 * evenly among all teams and let each team supply their resources.
 *
 * \param now The current simulation time.
 */
void Agency::act(Time now)
{
     for (int i = 0; i < mNumTeams; i++) {
          AgencyTeam& team = *mTeams[i];
          if (!team.deployed() && now >= team.getDeployTime()) {
               team.deploy();
          }
          if (!team.departed() && now >= team.getDepartTime()) {
               team.depart();
          }
     }

     Time intervall = now - mLastUpdate;
     if (static_cast<int>(intervall.days()) % mIntervallDays == 0) {
          mLastUpdate = now;
          setTeamsGoals();
     }

     //        Estimate needs:
     int    activeTeams = 0;
     double totalNeed = 0.0;   // Total need in all team areas

     // Needs in person-days in each team area
     double* need = new double[mNumTeams];
     memset(need, 0, mNumTeams * sizeof(double));
        
     for (int i = 0; i < mNumTeams; i++) {
          AgencyTeam &team = *mTeams[i];
          if (team.present() && now >= team.startTime()) {
               activeTeams++;
               
               double teamNeed = team.calculateNeed();
               need[i]   += teamNeed;
               totalNeed += teamNeed;
          }
     }
        
     if (activeTeams == 0) {
          // Deallocate array
          delete [] need;
          return;
     }
        
     //        Supply resources within each team's operating radius
     double teamResourceSupply;
     for (int i = 0; i < mNumTeams; i++) {
          AgencyTeam &team = *mTeams[i];
          if (team.present() && now >= team.startTime()) {
               if (totalNeed > 0.0) {
                    // Allocate resources according to local need:
                    teamResourceSupply = (need[i] / totalNeed) * mCapacityPPD;
               }
               else {
                    // Allocate resources equally among teams:
                    teamResourceSupply = mCapacityPPD / activeTeams;
               }
               team.setCapacity(teamResourceSupply);
               team.act(now);
          }
     }

     // Deallocate array
     delete [] need;
}

/**
 * \brief For debugging purposes.
 *
 * \param o The ostream to write to.
 * \param a The Agency to write.
 * \return The provided ostream with the Agency written to it.
 */
ostream& operator << (std::ostream& o, const Agency& a)
{
     o << Mapper::map(a.mTeams[0]->ref())->getType().getName() << endl;
     o << "Teams: " << endl;
     o << "capacityPPD: " << a.mCapacityPPD << endl;
     o << "responseTime (days): " << a.mResponseDays << endl;
     o << "#Teams: " << a.mTeams.size() << endl;
     for (vector<AgencyTeam*>::const_iterator it = a.mTeams.begin(); it != a.mTeams.end(); it++) {
          o << "=======" << endl;
          o << **it << endl;
     }
     o << "=======" << endl;
     return o;
}

/**
 * \brief Determines if we have a severe resource problem. If we do -
 * then the weights for the clustering algorithm is set.
 *
 * \return True if we have a severe food problem, false otherwise.
 */
bool FoodAgency::severeProblem()
{
     double total = 0.0;
     for (int i = 0; i < mGrid.active(); i++) {
          GridCell *c        = mGrid.cell(i);
          mVindex[i]         = 0;
           double population = c->pvfGet(ePopulation);
           double food       = c->pvGet(eFoodDays);
          if (food < 1.0  &&  population > kMinPopulation) {
               if ( food < 0.1 ) {
                    food = 0.1;
               }
               mVw[i] = population / food;
          }
          else {
               mVw[i] = 0.0;
          }
          total += mVw[i];
     }
     //        Food is a severe problem if total exceeds 1000:
     return (total > 1000);
}

/**
 * \brief Determines if we have a severe resource problem. If we do -
 * then the weights for the clustering algorithm is set.
 *
 * \return True if we have a severe water problem, false otherwise.
 */
bool WaterAgency::severeProblem()
{
     double total = 0.0;
     for (int i = 0; i < mGrid.active(); i++) {
          GridCell *c   = mGrid.cell(i);
          mVindex[i]    = 0;
          double shortfall = c->pvGet(eFractionNoWater) * c->pvfGet(ePopulation);
          
          if (mGrid.camps() > 0) {
               // Adjust for water already suppied within camps:
               shortfall -= c->pvGet(eSuppliedWater);
          }
          if ( shortfall > 0.0 ) {
               mVw[i]  = shortfall;
               total  += shortfall;
          }
          else {
               mVw[i] = 0.0;
          }
     }
     //        Lack of water is "severe" if more than 1000 people are affected.
     return (total > 1000);
}

/**
 * \brief Determines if we have a severe problem. If we do - then the
 * weights for the clustering algorithm is set.
 *
 * \return True if we have a severe displaced unsheltered people
 * problem, false otherwise.
 */
bool ShelterAgency::severeProblem()
{
     //        Find clusters of unsheltered displaced persons:
     double R2 = kNoCampZone * kNoCampZone;
     double total = 0.0;
     for (int i = 0; i < mGrid.active(); i++) {
          GridCell *c   = mGrid.cell(i);
          mVindex[i]    = 0;

          // Set the number of displaced to zero within a radius of
          // kNoCampZone around each camp. This will prevent camps
          //  from being built too close to one another.

          int tooClose = 0;
          for (int j = 0; j < mGrid.camps(); j++) {
               double D2 = c->center().squDistanceTo(mGrid.camp(j)->center());
               tooClose += (D2 < R2 ? 1 : 0);
          }
          double displaced = (tooClose > 0 ? 0 : c->pvfGet(eDisplaced) - c->pvfGet(eSheltered));

          mVw[i] = displaced;
          total += displaced;
     }
     return (total > 5000.0);
}

/**
 * \brief Determine actions of the ShelterAgencyTeams.
 *
 * Find out if we have severe problems with displaced unsheltered
 * people. If so - order teams to start building camps at the
 * locations for the problems.
 *
 * \param now The current simulation time.
 */
void ShelterAgency::act(Time now)
{
     Time intervall = now - mLastUpdate;
     if (static_cast<int>(intervall.days()) % mIntervallDays == 0) {
          mLastUpdate = now;

          // For all teams that haven't already been ordered to build a camp: if we
          // have a severe IDP problem - order team to build camp at problem location
          if (mNumTeams > mOperationalTeams) {
               // How severe is the problem?
               bool severe = severeProblem();
               vector<LatLng> clusters(mNumTeams);
               int newCamps = cluster(mNumTeams - mOperationalTeams, clusters);
               int clusterCount = 0;

               for (int i = 0; i < mNumTeams; ++i) {
                    AgencyTeam& team = *mTeams[i];
                    if (!team.deployed()) {
                         if (team.ownInitiative()) {
                              if (severe && clusterCount < newCamps && now >= team.getDeployTime()) {
                                   team.deploy();
                                   team.setGoal(clusters[clusterCount++]);
                                   team.setCapacity(mCapacityPPD / mNumTeams);
                                   mOperationalTeams++;
                              }
                         }
                         else if (now >= team.getDeployTime()) {
                              team.deploy();
                              team.setCapacity(mCapacityPPD / mNumTeams);
                              mOperationalTeams++;
                         }
                    }
                    else if (now >= team.getDepartTime()) {
                         team.depart();
                    }
               }
//                if (severeProblem()) {
//                     // The problem is severe, so start building new camps:
//                     vector<LatLng> clusters(mNumTeams);
//                     int newCamps = cluster(mNumTeams - mOperationalTeams, clusters);
                    
//                     for (int i = mOperationalTeams; i < mOperationalTeams + newCamps; i++) {
//                          AgencyTeam &team = *mTeams[i];
//                          team.setGoal(clusters[i - mOperationalTeams]);
//                          team.setStartTime(now + Time(Poisson(static_cast<double>(mResponseDays))));
// //                    team.setStartTime(now);
// //                     debug("=============== Start time for " << mType << "Agency team " << i
// //                          << " is " << team.startTime());
//                          team.setCapacity(mCapacityPPD / mNumTeams);
//                     }
//                     mOperationalTeams += newCamps;
//                }
          }

//           if (mNumTeams > mOperationalTeams) {
//                for (int i = mOperationalTeams; i < mNumTeams; ++i) {
//                     AgencyTeam &team = *mTeams[i];
//                     team.setGoal(team.location().cenCoord());
//                     team.setStartTime(now + Time(Poisson(static_cast<double>(mResponseDays))));
//                     team.setStartTime(now);
//                      debug("=============== Start time for " << mType << "Agency team " << i
//                           << " is " << team.startTime());
//                     team.setCapacity(mCapacityPPD / mNumTeams);
//                     mOperationalTeams++;
//                }
//           }
     }

     for (int i = 0; i < mOperationalTeams; i++) {
          AgencyTeam& team = *mTeams[i];
          if (team.present() && team.hasStartTime() && now >= team.startTime()) {
               team.act(now);
          }
     }
}



/**
 * \brief Determines if we have a severe resource problem. If we do -
 * then the weights for the clustering algorithm is set.
 *
 * \return True if we have a severe disease problem, false otherwise.
 */
bool HealthAgency::severeProblem()
{
     double total = 0.0;
     for (int i = 0; i < mGrid.active(); i++) {
          GridCell *c        = mGrid.cell(i);
          mVindex[i]         = 0;

          // Find clusters of disease:
          double sick = c->pvGet(eFractionInfected) * c->pvfGet(ePopulation);
          mVw[i]     = sick;
          total     += sick;
     }
     //        Public health is a severe problem if more than 20 people have cholera:
     return (total > 20.0);
}



/**
 * \brief Determines if we have a severe problem. If we do - then the
 * weights for the clustering algorithm is set.
 *
 * \return True if we have a severe violence problem, false otherwise.
 */
bool PoliceAgency::severeProblem()
{
     //        Find clusters of violence, weighted by log10( population ).
     double maximum = 0.0;
     for (int i = 0; i < mGrid.active(); i++) {
          GridCell *c        = mGrid.cell(i);
          mVindex[i]         = 0;

          double violence   = 0.0;
          for (int j = 1; j < mGrid.factions() + 1; j++) {
               double pop = c->pvfGet(ePopulation, j);
               if (pop > 1.0) {
                    violence += 0.01 * c->pvfGet(eViolence, j) * log10(pop);
               }
          }
          mVw[i] = violence;
          maximum = max(maximum, violence);
     }
     //        Overall violence is "severe" if any cell > 1.0.
     return (maximum > 1.0);
}

/**
 * \brief Determine positions of the teams.
 *
 * Assigns one team to each cluster found. If there are more teams
 * than clusters - assign more than one team to each cluster.
 */
void PoliceAgency::setTeamsGoals()
{
     if (severeProblem()) {
          orderTeamsToClusters(mNumTeams, 0);
     }
}



/**
 * \brief Determines if we have a severe problem. If we do - then the
 * weights for the clustering algorithm is set.
 *
 * \return True if we have a severe violence problem, false otherwise.
 */
bool CustomAgency::severeProblem()
{
     return true;
}

/**
 * \brief Determine positions of the teams.
 *
 * Assigns one team to each cluster found. If there are more teams
 * than clusters - assign more than one team to each cluster.
 */
void CustomAgency::setTeamsGoals()
{
     if (severeProblem()) {
          orderTeamsToClusters(mNumTeams, 0);
     }
}

void CustomAgency::act(Time now)
{
     for (int i = 0; i < mNumTeams; i++) {
          AgencyTeam& team = *mTeams[i];
          if (!team.deployed() && now >= team.getDeployTime()) {
               team.deploy();
          }
          if (!team.departed() && now >= team.getDepartTime()) {
               team.depart();
          }
     }

     for (int i = 0; i < mNumTeams; i++) {
          AgencyTeam &team = *mTeams[i];
          if (team.present() && now >= team.startTime()) {
               team.act(now);
          }
     }
}
