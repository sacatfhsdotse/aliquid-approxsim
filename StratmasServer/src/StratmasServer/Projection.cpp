// System
#include <cmath>

// Own
#include "debugheader.h"
#include "GoodStuff.h"
#include "LatLng.h"
#include "ProjCoord.h"
#include "Projection.h"


Projection *Projection::mCurrent = 0;


/**
 * \brief Constructor
 *
 * \param R Radius of sphere to be used as projection surface. The
 * simulation sets it to the earth radius in order to get projection
 * units in meters.
 * \param cenLat Latitude of center of area to be projected.
 * \param cenLng Longitude of center of area to be projected.
 */
Projection::Projection(double R, double cenLat, double cenLng)
     : mPCenX(0), mPCenY(0), mR(R)
{
     setCoordCenter(cenLat, cenLng);
}

/**
 * \brief Sets the center coordinate and the projection parameters
 * related to it.
 *
 * \param inLat Latitude of center of area to be projected.
 * \param inLng Longitude of center of area to be projected.
 */
void Projection::setCoordCenter(double inLat, double inLng)
{
    mCenLng = inLng;
    mCenLat = inLat;
    mPhi0 = mCenLat * kDeg2Rad;
    mLam0 = mCenLng * kDeg2Rad;
    mCosPhi0 = cos(mPhi0);
    mSinPhi0 = sin(mPhi0);
}

/**
 * \brief Projects a lat, lng coordinate on the projection surface.
 *
 * \param inLng   Longitude of in coordinate
 * \param inLat   Latitude of in coordinate
 * \param outX   x-coordinate of projected point
 * \param outY   y-coordinate of projected point
 */
void Projection::coordToProj(const double inLng, const double inLat, double &outX, double &outY) const
{
    double lam = inLng * kDeg2Rad;
    double phi = inLat * kDeg2Rad;
    double sin_phi = sin( phi );
    double cos_phi = cos( phi );
    double cos_lam = cos( lam - mLam0 );
    double t = 1.0 + mSinPhi0 * sin_phi + mCosPhi0 * cos_phi * cos_lam;
    double k1 = sqrt( 2.0 / t );
    double Rk1 = mR * k1;
    double dh = +( Rk1 * cos_phi * sin(lam-mLam0) );
    double dv = -( Rk1 * (mCosPhi0 * sin_phi - mSinPhi0 * cos_phi * cos_lam) );
    outX = mPCenX + dh;
    outY = mPCenY - dv;
//    outY = mPCenY + dv;   // For y increasing towards south
}

/**
 * \brief Converets a projected point back to lat, lng.
 *
 * \param inX   x-coordinate of point to be converted
 * \param inY   y-coordinate of point to be converted
 * \param outLng   Resulting longitude
 * \param outLat   Resulting latitude
 */
void Projection::projToCoord(const double inX, const double inY, double &outLng, double &outLat) const
{
    double phi;
    double lam;
    double x = double( inX - mPCenX);
    double y = double( inY - mPCenY);
//    double y = double(-inY + mPCenY);   // For y increasing towards south
    double rho = sqrt( x*x + y*y );
    double z = 0.5 * rho / mR;

    if ( z > 1.0 ) {
//	debug("z > 1.0 in Proj2Coord for coord x: " << inX << ", y: " << inY);
	return;
    }

    double t = 2.0 * asin(z);
    if ( inX == mPCenX && inY == mPCenY) {
	phi = mPhi0;
	lam = mLam0;
    }
    else {
	phi = asin(cos(t) * mSinPhi0 + y * sin(t) * mCosPhi0 / rho);
	if (mCenLat == 90.0) {
	    lam = mLam0 + atan2(x, -y);
	}
	else if (mCenLat == -90.0) {
	    lam = mLam0 + atan2(x, y);
	}
	else {
	    lam = mLam0 + atan2(x * sin(t), rho * mCosPhi0 * cos(t) - y * mSinPhi0 * sin(t));
	}
    }
    outLng = lam * kRad2Deg;
    outLat = phi * kRad2Deg;

    if (outLat > 90.0) {
	outLat = 90.0;
    }
    else if (outLat < -90.0) {
	outLat = -90.0;
    }

    while (outLng > 180.0) {
//	outLng -= 180.0;
	outLng -= 360.0;
    }

    while (outLng < -180.0) {
//	outLng += 180.0;
	outLng += 360.0;
    }
}

/**
 * \brief Projects a LatLng coordinate on the projection surface.
 *
 * \param l   Coordinate to be projected
 * \return   The projected coordinate.
 */
ProjCoord Projection::coordToProj(const LatLng &l) const
{
     double x, y;
     coordToProj(l.lng(), l.lat(), x, y);
     return ProjCoord(x,y);
}

/**
 * \brief Converts a projected coordinate back to lat, lng
 *
 * \param p   Coordinate to be converted
 * \return   LatLng coordinate.
 */
LatLng Projection::projToCoord(const ProjCoord &p) const
{
     double lat, lng;
     projToCoord(p.x(), p.y(), lng, lat);
     return LatLng(lat, lng);
}

/**
 * \brief Helper for gpc-related projecting.
 *
 * \param dst The destination gpc_vertex
 * \param src The source gpc_vertex
 */
void Projection::coordToProj(gpc_vertex &dst, const gpc_vertex &src) const
{
     coordToProj(src.x, src.y, dst.x, dst.y); 
}

/**
 * \brief Helper for gpc-related projecting.
 *
 * \param dst The destination gpc_vertex_list
 * \param src The source gpc_vertex_list
 */
void Projection::coordToProj(gpc_vertex_list &dst, const gpc_vertex_list &src) const
{
     for (int i = 0; i < src.num_vertices; i++) {
	  coordToProj(dst.vertex[i], src.vertex[i]);
     }
}

/**
 *   \brief Helper for gpc-related projecting.
 */
void Projection::coordToProj(gpc_polygon &dst,  const gpc_polygon &src) const
{
     for (int i = 0; i < src.num_contours; i++) {
	  coordToProj(dst.contour[i], src.contour[i]);
     }
}

/**
 *   \brief Helper for gpc-related projecting.
 */
void Projection::projToCoord(gpc_vertex &dst,  const gpc_vertex &src) const
{
     projToCoord(src.x, src.y, dst.x, dst.y); 
}

/**
 *   \brief Helper for gpc-related projecting.
 */
void Projection::projToCoord(gpc_vertex_list &dst,  const gpc_vertex_list &src) const
{
     for (int i = 0; i < src.num_vertices; i++) {
	  projToCoord(dst.vertex[i], src.vertex[i]);
     }
}

/**
 *   \brief Helper for gpc-related projecting.
 */
void Projection::projToCoord(gpc_polygon &dst,  const gpc_polygon &src) const
{
     for (int i = 0; i < src.num_contours; i++) {
	  projToCoord(dst.contour[i], src.contour[i]);
     }
}
