// System
#include <ostream>

// Own
#include "LatLng.h"
#include "ProjCoord.h"

LatLng ProjCoord::toLatLng() const
{
     return Projection::mCurrent->projToCoord(*this);
}

// Friends
std::ostream &operator << (std::ostream &o, const ProjCoord &p)
{
     return o << p.mX << ", " << p.mY;
}
