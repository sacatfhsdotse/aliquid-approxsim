#ifndef STRATMAS_CITY_H
#define STRATMAS_CITY_H

// System
#include <iosfwd>
#include <map>

// Own
#include "Distribution.h"
#include "Element.h"
#include "PVArea.h"
#include "Shape.h"

// Forward Declarations
class Buffer;
class DataObject;
class Faction;
class Reference;


/**
 * \brief City is the class containing Stratmas' representation of a
 * City, or more general - a population instance.
 *
 * A City has an area, a population from one or more Factions and a
 * population Distribution. Currently all cities have the same kind of
 * Distribution - the StratmasCityDistribution that represents the way
 * older versions of Stratmas distributed population from cities.
 */
class City : public Element, public PVArea {
private:
     /// The total number of Factions in the simulation.
     int mFactions;

     /**
      * \brief Maps a faction Reference to the number of inhabitants
      * for that faction.
      */
     std::map<const Reference*, double> mPop;

public:
     City(const DataObject& d);

     /**
      * \brief A City is always present.
      *
      * \return Always true.
      */
     bool present() const { return true; }

     virtual void extract(Buffer &b) const {}

     double population(int f = 0) const;
     double population(const Faction& f) const;

     // Friends
     friend std::ostream &operator << (std::ostream &o, const City &c);

     const Shape& area() const { return location(); }
     const Distribution& distribution() const { return deployment(); }
};


#endif   // STRATMAS_CITY_H
