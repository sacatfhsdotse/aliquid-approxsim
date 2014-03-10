// System
#include <algorithm>
#include <functional>

// Own
#include "Agency.h"
#include "AgencyFactory.h"
#include "AgencyTeam.h"
#include "debugheader.h"
#include "Error.h"


using namespace std;


/**
 * \brief Unary predicate for controlling if an object of type Base
 * may be dynamic_cast to type Sub. Only works for polymorphic classes
 * i.e. classes with at least one virtual function.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/10/02 16:03:46 $
 */
template<class Base, class Sub> class castPredicate : public unary_function<Sub, bool> {
public:
     /**
      * \brief Returns true if b may be dynamic_cast to type Sub.
      *
      * \param b Object of type Base
      * \return True if b may be dynamic_cast to type Sub.
      */
     bool operator ()(const Base b) { return (dynamic_cast<const Sub>(b) != 0); }
};


/**
 * \brief Creates Agencies based on a vector of already created
 * AgencyTeams.
 *
 * Notice that the teams may be of any type. The AgencyFactory sorts
 * out the different types and creates the corresponding Agencies.
 *
 * \param grid A reference to the Grid.
 * \param teams A vector containing the teams to create Agencies for.
 * \param ioAgencies On return this vector contains the newly created
 * Agencies.
 */
void AgencyFactory::createAgencies(Grid& grid, const vector<AgencyTeam*>& teams, std::vector<Agency*>& ioAgencies)
{
     enum eAgencies {eFood, eHealth, ePolice, eShelter, eWater, eCustom, eNumAgencyTypes};
     vector<vector<AgencyTeam*> > teamVecs(eNumAgencyTypes);
     for (vector<AgencyTeam*>::const_iterator it = teams.begin(); it != teams.end(); it++) {
          if (dynamic_cast<FoodAgencyTeam*>(*it)) {
               teamVecs[eFood].push_back(*it);
          }
          else if (dynamic_cast<HealthAgencyTeam*>(*it)) {
               teamVecs[eHealth].push_back(*it);
          }
          else if (dynamic_cast<PoliceAgencyTeam*>(*it)) {
               teamVecs[ePolice].push_back(*it);
          }
          else if (dynamic_cast<CustomAgencyTeam*>(*it)) {
               teamVecs[eCustom].push_back(*it);
          }
          else if (dynamic_cast<ShelterAgencyTeam*>(*it)) {
               teamVecs[eShelter].push_back(*it);
          }
          else if (dynamic_cast<WaterAgencyTeam*>(*it)) {
               teamVecs[eWater].push_back(*it);
          }
          else {
               Error e;
               e << "Unknown Agency type in AgencyFactory() for object with reference: " << (*it)->ref();
               throw e;
          }
     }
     
     if (!teamVecs[eFood].empty()) {
          ioAgencies.push_back(new FoodAgency(teamVecs[eFood], grid));
//          stratmasDebug("Created FoodAgency with " << teamVecs[eFood].size() << " teams");
     }
     if (!teamVecs[eHealth].empty()) {
          ioAgencies.push_back(new HealthAgency(teamVecs[eHealth], grid));
//          stratmasDebug("Created HealthAgency with " << teamVecs[eHealth].size() << " teams");
     }
     if (!teamVecs[ePolice].empty()) {
          ioAgencies.push_back(new PoliceAgency(teamVecs[ePolice], grid));
//          stratmasDebug("Created PoliceAgency with " << teamVecs[ePolice].size() << " teams");
     }
     if (!teamVecs[eCustom].empty()) {
          ioAgencies.push_back(new CustomAgency(teamVecs[eCustom], grid));
//          stratmasDebug("Created CustomAgency with " << teamVecs[eCustom].size() << " teams");
     }
     if (!teamVecs[eShelter].empty()) {
          ioAgencies.push_back(new ShelterAgency(teamVecs[eShelter], grid));
//          stratmasDebug("Created ShelterAgency with " << teamVecs[eShelter].size() << " teams");
     }
     if (!teamVecs[eWater].empty()) {
          ioAgencies.push_back(new WaterAgency(teamVecs[eWater], grid));
//          stratmasDebug("Created WaterAgency with " << teamVecs[eWater].size() << " teams");
     }
}

/**
 * \brief Adds a team to an Agency. If the correct type of Agency does
 * not yet exist it will be created.
 *
 * Notice that the team may be of any type. The AgencyFactory adds the
 * team to the correct Agency.
 *
 * \param grid A reference to the Grid.
 * \param team The AgencyTeam to add.
 * \param ioAgencies On return this vector contains the same Agencies
 * as before either with the provided team added to the correct Agency
 * or a new Agency created with the provided team in it (if the
 * correct type of Agency did not exist).
 */
void AgencyFactory::addTeam(Grid& grid, AgencyTeam& team, vector<Agency*>& ioAgencies)
{
     vector<Agency*>::iterator it = ioAgencies.end();
     if (dynamic_cast<FoodAgencyTeam*>(&team)) {
          it = find_if(ioAgencies.begin(), ioAgencies.end(), castPredicate<Agency*, FoodAgency*>());
     }
     else if (dynamic_cast<HealthAgencyTeam*>(&team)) {
          it = find_if(ioAgencies.begin(), ioAgencies.end(), castPredicate<Agency*, HealthAgency*>());
     }
     else if (dynamic_cast<PoliceAgencyTeam*>(&team)) {
          it = find_if(ioAgencies.begin(), ioAgencies.end(), castPredicate<Agency*, PoliceAgency*>());
     }
     else if (dynamic_cast<CustomAgencyTeam*>(&team)) {
          it = find_if(ioAgencies.begin(), ioAgencies.end(), castPredicate<Agency*, CustomAgency*>());
     }
     else if (dynamic_cast<ShelterAgencyTeam*>(&team)) {
          it = find_if(ioAgencies.begin(), ioAgencies.end(), castPredicate<Agency*, ShelterAgency*>());
     }
     else if (dynamic_cast<WaterAgencyTeam*>(&team)) {
          it = find_if(ioAgencies.begin(), ioAgencies.end(), castPredicate<Agency*, WaterAgency*>());
     }
     else {
          Error e;
          e << "Unknown Agency type in AgencyFactory() for object with reference: " << team.ref();
          throw e;
     }

     if (it != ioAgencies.end()) {
          (*it)->addTeam(team);
     }
     else {
          vector<AgencyTeam*> v;
          v.push_back(&team);
          createAgencies(grid, v, ioAgencies);
     }
}

/**
 * \brief Removes a team from an Agency.
 *
 * Notice that the team may be of any type. The AgencyFactory removes
 * the team from the correct Agency.
 *
 * \param team The AgencyTeam to remove.
 * \param ioAgencies On return this vector contains the same Agencies
 * as before but with the provided team removed from the correct
 * Agency.
 */
void AgencyFactory::removeTeam(AgencyTeam& team, vector<Agency*>& ioAgencies)
{
     vector<Agency*>::iterator it = ioAgencies.end();
     if (dynamic_cast<FoodAgencyTeam*>(&team)) {
          it = find_if(ioAgencies.begin(), ioAgencies.end(), castPredicate<Agency*, FoodAgency*>());
     }
     else if (dynamic_cast<HealthAgencyTeam*>(&team)) {
          it = find_if(ioAgencies.begin(), ioAgencies.end(), castPredicate<Agency*, HealthAgency*>());
     }
     else if (dynamic_cast<PoliceAgencyTeam*>(&team)) {
          it = find_if(ioAgencies.begin(), ioAgencies.end(), castPredicate<Agency*, PoliceAgency*>());
     }
     else if (dynamic_cast<CustomAgencyTeam*>(&team)) {
          it = find_if(ioAgencies.begin(), ioAgencies.end(), castPredicate<Agency*, CustomAgency*>());
     }
     else if (dynamic_cast<ShelterAgencyTeam*>(&team)) {
          it = find_if(ioAgencies.begin(), ioAgencies.end(), castPredicate<Agency*, ShelterAgency*>());
     }
     else if (dynamic_cast<WaterAgencyTeam*>(&team)) {
          it = find_if(ioAgencies.begin(), ioAgencies.end(), castPredicate<Agency*, WaterAgency*>());
     }
     else {
          Error e;
          e << "Unknown Agency type in AgencyFactory() for object with reference: " << team.ref();
          throw e;
     }

     if (it != ioAgencies.end()) {
          (*it)->removeTeam(team);
     }
     else {
          Error e;
          e << "Tried to remove the team '" << team.ref() << "'from a non existing Agency";
          throw e;
     }
}
