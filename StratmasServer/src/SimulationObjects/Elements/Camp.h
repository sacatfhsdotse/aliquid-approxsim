#ifndef STRATMAS_CAMP_H
#define STRATMAS_CAMP_H

// Own
#include "Element.h"

// Forward Declarations
class Shape;

/**
 * \brief Class representing a refugee camp.
 *
 * \author Per Alexius
 * \date     $Date: 2006/02/28 17:48:10 $
 */
class Camp : public Element {
private:
     double *mPopulation;   ///< Population of each Faction in this Camp

public:
     /**
      * \brief Constructor
      *
      * \param ref The Reference to this Camp.
      * \param location A Shape defining the area of this Camp.
      * \param nFactions The total number of Factions excluding the 'all' Faction.
      */
     inline Camp(const Reference &ref, const Shape &location, int nFactions) : Element(ref, location) {
          mPopulation = new double[nFactions + 1];
          for (int i = 0; i < nFactions + 1; i++) {
               mPopulation[i] = 0;
          }
     }
     
     /// Destructor
     inline ~Camp() { if (mPopulation) { delete [] mPopulation; } }

     /**
      * \brief A Camp is always present once it is created.
      *
      * \return Always true.
      */
     inline bool present() const { return true; }

     /**
      * \brief Access the population of a specified Faction.
      *
      * \param f Index of the Faction to get the population value for.
      * \return The population of Faction f in this Camp
      */
     inline double population(int f = 0) { return mPopulation[f]; }

     /**
      * \brief Add population of a specified Faction to this Camp.
      *
      * \param pop The population to add.
      * \param f Index of the Faction to get the population value for.
      */
     inline void addPopulation(double pop, int f = 0) { mPopulation[f] += pop; }
};

#endif   // STRATMAS_CAMP_H
