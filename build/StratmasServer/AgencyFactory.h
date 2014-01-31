#ifndef STRATMAS_AGENCYFACTORY_H
#define STRATMAS_AGENCYFACTORY_H


// System
#include <vector>

// Forward Declarations
class Agency;
class AgencyTeam;
class Grid;


/**
 * \brief Factory for creating Agencies.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/06 09:18:04 $
 */
class AgencyFactory {
private:
     /// Reference to the Grid.
     Grid& mGrid;

public:
     static void createAgencies(Grid& grid, const std::vector<AgencyTeam*>& teams, std::vector<Agency*>& ioAgencies);
     static void addTeam(Grid& grid, AgencyTeam& team, std::vector<Agency*>& ioAgencies);
     static void removeTeam(AgencyTeam& team, std::vector<Agency*>& ioAgencies);
};


#endif   // STRATMAS_AGENCYFACTORY_H
