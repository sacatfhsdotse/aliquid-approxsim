#ifndef _PROJECTION_H
#define _PROJECTION_H

// Own
#include "gpc.h"

// Forward Declarations
class LatLng;
class ProjCoord;


/**
 * \brief This class holds data related to the projection used when
 * partitioning the Grid.
 *
 * \author   Per Alexius
 * \date     $Date: 2005/06/13 11:19:06 $
 */
class Projection
{
private:
     double        mPCenX;     ///<   x-coordinate of the center of the projection
     double        mPCenY;     ///<   y-coordinate of the center of the projection

     double        mCenLng;    ///<   Longitude of center of area to be projected
     double        mCenLat;    ///<   Latitude of center of area to be projected
     double        mCosPhi0;   ///<   Projection parameter
     double        mSinPhi0;   ///<   Projection parameter

     // Azimuthal
     double        mR;         ///<   Radius of sphere to be projected upon
     double        mPhi0;      ///<   Projection parameter
     double        mLam0;      ///<   Projection parameter
     
     /// Private default constructor.
     Projection() {}
     void setCoordCenter(double inLat, double inLng);
     
public:
     /// Holds a pointer to the current projection used by the simulation.
     static Projection *mCurrent;

     /**
      * \brief Returns the current projection.
      *
      * \return The current projection.
      */
     inline static const Projection *currentProjection() { return mCurrent; }

     Projection(double R, double cenLat, double cenLng);

     /// Destructor.
     virtual ~Projection() {}
     
     /**
      * \brief Sets the center of the projection.
      *
      * \param inX The center x coordinate.
      * \param inY The center y coordinate.
      */
     void         setProjCenter(double inX, double inY) { mPCenX = inX; mPCenY = inY; }

     void         coordToProj(const double inLng, const double inLat, double &outX, double &outY) const;
     void         projToCoord(const double inX, const double inY, double &outLng, double &outLat) const;
     
     ProjCoord     coordToProj(const LatLng    &l) const;
     LatLng        projToCoord(const ProjCoord &p) const;

     void         coordToProj(gpc_vertex &dst,      const gpc_vertex &src)      const;
     void         coordToProj(gpc_vertex_list &dst, const gpc_vertex_list &src) const;
     void         coordToProj(gpc_polygon &dst,     const gpc_polygon &src)     const;

     void         projToCoord(gpc_vertex &dst,      const gpc_vertex &src)      const;
     void         projToCoord(gpc_vertex_list &dst, const gpc_vertex_list &src) const;
     void         projToCoord(gpc_polygon &dst,     const gpc_polygon &src)     const;
};

#endif        // _PROJECTION_H
