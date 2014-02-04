// System

// Own
#include "DataObject.h"
#include "debugheader.h"
#include "GoodStuff.h"
#include "Map.h"
#include "Projection.h"
#include "Shape.h"

// Temporary
#include <fstream>

using namespace std;


/**
 * \brief Creates a Map.
 *
 * \param s The Shape for this map.
 */
Map::Map(const Shape &s)
{
     mBorders = s.clone();

     // Neccessary for creating Projection
     mBorders->boundingBox(mMaxY, mMinX, mMinY, mMaxX);   // top, left, bottom, right...
     debug("mMinX: " << mMinX << ", mMinY: " << mMinY);
     debug("mMaxX: " << mMaxX << ", mMaxY: " << mMaxY);

     mCenLng = mBorders->cenCoord().lng();
     mCenLat = mBorders->cenCoord().lat();
     debug("mCenLat, mCenLng: " << mCenLat << ", " << mCenLng);

     // Project lat, lng coordinates
     mProj = new Projection(6371000, mMinY + height() / 2.0, mMinX + width() / 2.0);
     Projection::mCurrent = mProj;

     mBorders->toProj(*mProj);

     // Find out the new boundaries
     mBorders->boundingBox(mMaxY, mMinX, mMinY, mMaxX);   // top, left, bottom, right...
     debug("mMinX: " << mMinX << ", mMinY: " << mMinY);
     debug("mMaxX: " << mMaxX << ", mMaxY: " << mMaxY);
     debug("mCenLat, mCenLng: " << mCenLat << ", " << mCenLng);

//     ofstream ofs("map.tmp", ios_base::trunc);
//     mBorders->toXML(ofs);
}

/**
 * \brief Destructor.
 */
Map::~Map()
{
     if (mProj   ) { delete mProj; Projection::mCurrent = 0; } 
     if (mBorders) { delete mBorders; }
}

/**
 * \brief Finds out in which subshape the specified point is located.
 *
 * \param p The point.
 * \return The first found subschape that contains the point, or null
 * if no such subshape could be found.
 */
const Shape *Map::getRegionForPoint(const ProjCoord &p) const
{
     CompositeShape *cs = dynamic_cast<CompositeShape*>(mBorders);
     if (cs) {
          return cs->getRegionForPoint(p);
     }
     else {
          debug("Not a Composite map!");
     }
     return 0;
}
