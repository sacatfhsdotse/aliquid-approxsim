#ifndef _LATLNG_H
#define _LATLNG_H

// System
#include <cmath>
#include <ostream>

// Own
#include "GoodStuff.h"
#include "ProjCoord.h"
#include "Projection.h"

// Forward Declarations
class ProjCoord;


/**
 * \brief The LatLng class represents a geografic location indicated
 * by degrees latitude and longitude.
 *
 * \author   Per Alexius
 * \date     $Date: 2005/06/13 11:19:06 $
 */
class LatLng
{
protected:
     /// The latitude of this point
     double mLat;
     /// The longitude of this point
     double mLng;

public:
     /// \brief Constructs a point representing nowhere.
     inline LatLng() : mLat(0), mLng(0) {}

     /**
      * \brief Constructs a point.
      *
      * \param lat The latitude.
      * \param lng The longitude.
      */
     inline LatLng(double lat, double lng) : mLat(lat), mLng(lng) {}

     /// Destructor.
     inline virtual ~LatLng() {}

     /**
      * \brief Checks if this point is nowhere.
      *
      * \return True if this point is nowhere, false otherwise.
      */
     inline bool nowhere() const { return (mLat == 0 && mLng == 0); }

     /**
      * \brief Sets the position of this LatLng.
      *
      * \param lat The latitude.
      * \param lng The longitude.
      */
     inline void setPos(double lat, double lng) { mLat = lat; mLng = lng; }

     /**
      * \brief Accessor for this point's latitude.
      *
      * \return This point's latitude.
      */
     inline double lat() const { return mLat; }

     /**
      * \brief Accessor for this point's longitude.
      *
      * \return This point's longitude.
      */
     inline double lng() const { return mLng; }

     /**
      * \brief Returns the square of the distance between this point and
      * the point p.
      *
      * \param p The point to measure the distance to.
      * \return The square of the distance between this point and the point
      * p in meters.
      */
     inline double squDistanceTo(const LatLng &p) const;

     /**
      * \brief Returns the distance between this point and the point p.
      */
     inline double distanceTo(const LatLng &p) const { return sqrt(squDistanceTo(p)); }

     /**
      * \brief Returns the projection of this point using the current
      * Projection.
      *
      * \return The projection of this point using the current Projection.
      */
     inline ProjCoord toCoord() const;

     /**
      * \brief Equality operator.
      *
      * \param p The point to compare with.
      * \return True if the points are equal, false otherwise.
      */
     inline bool operator == (const LatLng &p) const { return (mLat == p.mLat && mLng == p.mLng); }

     /**
      * \brief Not-equal-to operator.
      *
      * \param p The point to compare with.
      * \return True if the points are not equal, false otherwise.
      */
     inline bool operator != (const LatLng &p) const { return (mLat != p.mLat || mLng != p.mLng); }

     // Friends
     /// For debugging purposes.
     friend inline std::ostream &operator << (std::ostream &o, const LatLng &p);
};

inline double LatLng::squDistanceTo(const LatLng &p) const
{
     double dx = (mLng - p.mLng) * cos(mLat * kDeg2Rad );
     double dy = (mLat - p.mLat);
     return ((dx * dx) + (dy * dy)) * kMetersPerDegreeLat2;
}

inline ProjCoord LatLng::toCoord() const
{
     return Projection::mCurrent->coordToProj(*this); 
}

// Friends
inline std::ostream &operator << (std::ostream &o, const LatLng &p)
{
     return o << "(lat, lng): " << p.mLat << ", " << p.mLng;
}

#endif   // _LATLNG_H
