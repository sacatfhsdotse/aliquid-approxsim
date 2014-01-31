#ifndef _STRATMASMAP_H
#define _STRATMASMAP_H

// System                                                                                           

// Own                                                                                              

// Forward Declarations                                                                             
class DataObject;
class ProjCoord;
class Projection;
class Shape;


/**
 * \brief Class representing the map the simulation concerns.
 * 
 * \author Per Alexius
 * \date   $Date: 2006/02/28 17:48:19 $                                                           
 */
class Map
{
private:
     Shape*    mBorders;  ///< The Shape constituting the map.

     double    mCenLat;   ///< Latitude of the center of the Map's bounding box                     
     double    mCenLng;   ///< Longitude of the center of the Map's bounding box                    
     double    mMinX;     ///< Leftmost coordinate of the Map
     double    mMaxX;     ///< Rightmost coordinate of the Map
     double    mMinY;     ///< Minimum y-coordinate of the Map
     double    mMaxY;     ///< Maximum y-coordinate of the Map

     Projection   *mProj;      ///< Pointer to the projection used for this Map

public:
     Map(const Shape& s);
     ~Map();
     
     /**
      *	\brief Access the longitude of the center coordinate
      *	
      *	\return Longitude of the center coordinate
      */
     inline Shape& borders() const { return *mBorders; }

     /**
      *	\brief Access the longitude of the center coordinate
      *	
      *	\return Longitude of the center coordinate
      */
     inline double cenLng() const { return mCenLng; }

     /**
      * \brief Access the latitude of the center coordinate
      *
      * \return Latitude of the center coordinate
      */
     inline double cenLat() const { return mCenLat; }

     /**
      *	\brief Access the minimum x-value for the Map
      *	
      *	\return Minimum x-value for the Map
      */
     inline double minX() const { return mMinX; }

     /**
      *	\brief Access the maximum x-value for the Map
      *	
      *	\return Maximum x-value for the Map
      */
     inline double maxX() const { return mMaxX; }

     /**
      *	\brief Access the minimum y-value for the Map
      *	
      *	\return Minimum y-value for the Map
      */
     inline double minY() const { return mMinY; }

     /**
      *	\brief Access the maximum y-value for the Map
      *	
      *	\return Maximum y-value for the Map
      */
     inline double maxY() const { return mMaxY; }

     /**
      *	\brief Access the width of the Map
      *	
      *	\return Width of the Map
      */
     inline double width()  const { return mMaxX - mMinX; }

     /**
      *	\brief Access the height of the Map
      *	
      *	\return Height of the Map
      */
     inline double height() const { return mMaxY - mMinY; }

     /**
      *	\brief Access the current Projection
      *	
      *	\return Current Projection
      */
     inline const Projection &proj() const { return *mProj; }

     const Shape *getRegionForPoint(const ProjCoord &p) const;
};

#endif   // _STRATMASMAP_H
