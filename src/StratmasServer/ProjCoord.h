#ifndef _PROJCOORD_H
#define _PROJCOORD_H

// System
#include <cmath>
#include <iosfwd>

// Own
#include "GoodStuff.h"
#include "Projection.h"

// Forward Declarations
class LatLng;

/**
 *   \brief A class representing a coordinate in projection space.
 */
class ProjCoord
{
protected:
     double mX;   ///<   X-value of coordinate.
     double mY;   ///<   Y-value of coordinate.

public:
     /// Obvious constructor
     inline ProjCoord(double x, double y) : mX(x), mY(y) {}
     inline virtual ~ProjCoord() {}

     /// Sets the coordinate to (inx, iny)
     inline void set(double inx, double iny) { mX = inx; mY = iny; }
     /// Returns the x value of this coordinate.
     inline double x() const { return mX; }
     /// Returns the y value of this coordinate.
     inline double y() const { return mY; }
     /// Returns the square of the distance (in meters) between this point and p.
     inline double squDistanceTo(const ProjCoord &p) const;

     /// Convert this projected point back to lat, lng.
     LatLng toLatLng() const;
     
     // Friends
     /// For debugging purposes.
     friend std::ostream &operator << (std::ostream &o, const ProjCoord &p);
};

inline double ProjCoord::squDistanceTo(const ProjCoord &p) const {
     double dx = (mX - p.mX) * cos(mY * kDeg2Rad );
     double dy = (mY - p.mY);
     return ((dx * dx) + (dy * dy)) * kMetersPerDegreeLat2;
}


#endif   // _PROJCOORD_H
