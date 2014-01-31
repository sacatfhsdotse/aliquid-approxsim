// System
#include <limits>   // For numeric_limits<double> (and nextafter()?)
#include <ostream>

// Own
#include "AreaHandler.h"
#include "BasicGrid.h"
#include "debugheader.h"
#include "Error.h"
#include "GoodStuff.h"
#include "Map.h"
#include "ProjCoord.h"
#include "Projection.h"
#include "Shape.h"

using namespace std;


/**
 * \brief Creates a BasicGrid for the provided Map with the specified
 * cell size.
 *
 * \param amap The map to create the Grid for.
 * \param cellSizeMeters The side of the cells in meters.
 */
BasicGrid::BasicGrid(const Map& amap, double cellSizeMeters)
     : mIndexToActive(0), mActiveToIndex(0),
       mActive(0), mCellSideMeters(cellSizeMeters),
       mCellArea(cellSizeMeters * cellSizeMeters), mMap(amap),
       mCellPosLatLng(0), mCellPosProj(0)
{
     // Determine the boundaries of the grid and the cell size. Values refer to
     // the current projection of the map.
     sizeGrid(mMap);

     // Create AreaHandler
     mAH = new AreaHandler(mRows, mCols, mTop, mBottom, mLeft, mRight, mDx, mDy, mCellSideMeters);

     // Initialize mCellPosLatLng array. Should be done after sizeGrid()
     setCellPositions();

     // Allocate memory and consider all cells active to begin
     // with. This is important for the functionality of the cells()
     // functions.
     mIndexToActive = new int[mCells];
     for (int i = 0; i < mCells; i++) {
	  mIndexToActive[i] = kUndefinedActive;
     }

     // Fetch cells covered by the map
     list<GridPos> activeCells;
     mMap.borders().cells(*this, activeCells);

     // Now only the cells covered by the map should be active so
     // start with considering all cells inactive.
     for (int i = 0; i < mCells; i++) {
	  mIndexToActive[i] = kInactive;
     }
     

     // Mapping between index and position in active array.
     for(list<GridPos>::iterator it = activeCells.begin(); it != activeCells.end(); it++) {
	  mIndexToActive[it->r * mCols + it->c] = mActive;
	  mActive++;
     }
     
     // Mapping between position in active array and index.
     mActiveToIndex = new int[mActive];
     for (int i = 0; i < mCells; i++) {
	  if (mIndexToActive[i] != kInactive) {
	       mActiveToIndex[mIndexToActive[i]] = i;
	  }
     }
}

/**
 * \brief Destructor.
 */
BasicGrid::~BasicGrid()
{
     if (mAH)              { delete    mAH; }
     if (mCellPosLatLng)   { delete [] mCellPosLatLng; }
     if (mCellPosProj)     { delete [] mCellPosProj; }
     if (mIndexToActive)   { delete [] mIndexToActive; }
     if (mActiveToIndex)   { delete [] mActiveToIndex; }
}

/**
 * \brief Sets the grid size parameters based on the provided Map.
 *
 * \param m The Map.
 */
void BasicGrid::sizeGrid(const Map& m)
{
//     const double degWidth  = mCellSideKm * 360 / 40075;   // Earth circumference by the equator
//     const double degHeight = mCellSideKm * 360 / 40008;   // Earth circumference by the poles
     const double degWidth  = mCellSideMeters * 360 / 40030000 / cos(m.cenLat() * kDeg2Rad);
     const double degHeight = mCellSideMeters * 360 / 40030000;
     double aX, aY, bX, bY;

     mTop    = m.maxY();
     mBottom = m.minY();
     mLeft   = m.minX();
     mRight  = m.maxX();

     // Calculate the height of a cell in the middle of the grid
     m.proj().coordToProj(m.cenLng(), m.cenLat(), aX, aY);
     m.proj().coordToProj(m.cenLng(), m.cenLat() + degHeight, bX, bY);
     mDy = fabs(bY - aY);
     
     // Calculate the width of a cell in the middle of the grid
     m.proj().coordToProj(m.cenLng(), m.cenLat(), aX, aY);
     m.proj().coordToProj(m.cenLng() + degWidth, m.cenLat(), bX, bY);
     mDx = fabs(bX - aX);
     
     // Calculate the number of rows and columns
     mRows  = Round( (mTop - mBottom) / mDy ) + 2;
     mCols  = Round( (mRight - mLeft) / mDx ) + 2;
     mCells = mRows * mCols;

     // Add one row and col on each side of the grid
     // instead of two rows and cols at the right and top.
     mBottom -= mDy;
     mLeft   -= mDx;

     mTop   = mBottom + mRows * mDy;
     mRight = mLeft   + mCols * mDx;
}

/**
 * \brief Initializes the array that holds the coordinates of the
 * cornerpoints of all cells.
 */
void BasicGrid::setCellPositions()
{
     // Initialize array of cell positions in lat lon.
     if (mCellPosLatLng) { delete [] mCellPosLatLng; }
     if (mCellPosProj  ) { delete [] mCellPosProj  ; }
     mCellPosLatLng = new double[(mRows + 1) * (mCols + 1) * 2];
     mCellPosProj   = new double[(mRows + 1) * (mCols + 1) * 2];

     // Calculate the position of each cell cornerpoint. The array
     // contains x and y values for cell 'intersection points' ordered
     // row-wise from top left to bottom right i.e. [x0 y0 x1 y1...]
     float *tmpPos = new float[(mRows + 1) * (mCols + 1) * 2];
     for (int r = 0; r < mRows + 1; r++) {
	  for (int c = 0; c < mCols + 1; c++) {
	       tmpPos[r * (mCols + 1) * 2 + c * 2]     = (c == mCols ? mRight  : mLeft + static_cast<double>(c) * mDx);
	       tmpPos[r * (mCols + 1) * 2 + c * 2 + 1] = (r == mRows ? mBottom : mTop  - static_cast<double>(r) * mDy);
	       mCellPosProj[r * (mCols + 1) * 2 + c * 2]     = tmpPos[r * (mCols + 1) * 2 + c * 2];
	       mCellPosProj[r * (mCols + 1) * 2 + c * 2 + 1] = tmpPos[r * (mCols + 1) * 2 + c * 2 + 1];
	  }
     }
     
     // Notice x, y to lat, lon i.e x is lon and y is lat.
     for (int i = 0; i < (mRows + 1) * (mCols + 1) * 2; i += 2) {
	  Projection::currentProjection()->projToCoord(mCellPosProj[i]    , mCellPosProj[i+1],
						       mCellPosLatLng[i+1], mCellPosLatLng[i]);
     }

     delete [] tmpPos;
}

/**
 * \brief Gets the position in the grid for the cell that contains the
 * specified point.
 *
 * \param p The point.
 * \return The position in the grid for the cell that contains the
 * specified point.
 */
GridPos BasicGrid::cell(const ProjCoord p) const
{
     return mAH->cell(p);
}

/**
 * \brief Gets the position in the grid for the cell that contains the
 * specified point.
 *
 * \param p The point.
 * \return The position in the grid for the cell that contains the
 * specified point.
 */
GridPos BasicGrid::cell(const LatLng p) const
{
     return mAH->cell(p.toCoord());
}

/**
 * \brief Returns a list containing the grid positions of all cells
 * covered by the provided Polygon.
 *
 * \param inP The Polygon to get the grid positions for.
 * \param outCells A list that on return contains the grid positions
 * of all cells covered by the provided Polygon.
 */
void BasicGrid::cells(const Polygon& inP, std::list<GridPos>& outCells) const
{
     list<GridPos> tmp;
     mAH->cells(inP, tmp);
     for (list<GridPos>::iterator it = tmp.begin(); it != tmp.end(); it++) {
	  if (isActive(it->r, it->c)) {
	       outCells.push_back(*it);
	  }
     }
}

/**
 * \brief Returns a list containing the grid positions of all cells
 * covered by the provided Circle.
 *
 * \param inC The Circle to get the grid positions for.
 * \param outCells A list that on return contains pointers to all
 * cells covered by the provided Circle.
 */
void BasicGrid::cells(const Circle& inC, std::list<GridPos>& outCells) const
{
     list<GridPos> tmp;
     mAH->cells(inC, tmp);
     for (list<GridPos>::iterator it = tmp.begin(); it != tmp.end(); it++) {
	  if (isActive(it->r, it->c)) {
	       outCells.push_back(*it);
	  }
     }
}

/**
 * \brief Gets the center coordinate (lat, lag) for the specified
 * cell.
 *
 * \param r The row of the cell.
 * \param c The column of the cell.
 * \return The coordinate of the center of the specified cell.
 */
LatLng BasicGrid::center(int r, int c) const
{
     double dmin = numeric_limits<double>::min();
     double ll =           mCellPosProj[(r + 1) * (mCols + 1) * 2  +  2 *  c         ];        
     double bb =           mCellPosProj[(r + 1) * (mCols + 1) * 2  +  2 *  c      + 1];
#ifdef __win__
     double rr = _nextafter(mCellPosProj[ r      * (mCols + 1) * 2  +  2 * (c + 1)    ], dmin);
     double tt = _nextafter(mCellPosProj[ r      * (mCols + 1) * 2  +  2 * (c + 1) + 1], dmin);
#else
     double rr = nextafter(mCellPosProj[ r      * (mCols + 1) * 2  +  2 * (c + 1)    ], dmin);
     double tt = nextafter(mCellPosProj[ r      * (mCols + 1) * 2  +  2 * (c + 1) + 1], dmin);
#endif

     return ProjCoord(ll+(rr-ll)/2, bb+(tt-bb)/2).toLatLng();
}

/**
 * \brief For debugging purposes.
 *
 * \param o The stream to write to.
 * \param g The BasicGrid to print.
 */
std::ostream &operator << (std::ostream& o, const BasicGrid& g)
{
     o << "BasicGrid" << endl;
     return o;
}

