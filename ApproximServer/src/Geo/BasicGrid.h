#ifndef _APPROXSIMBASICGRID_H
#define _APPROXSIMBASICGRID_H

// System
#include <iosfwd>
#include <list>

// Own

// Forward Declarations
class AreaHandler;
class Circle;
class GridPos;
class LatLng;
class Map;
class Polygon;
class ProjCoord;

/**
 * \brief This class represents basic characteristics for a grid
 * overlayed by a map.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/19 07:04:27 $
 */
class BasicGrid {
private:
     /**
      * \brief Inactive cells should be marked with kInactive in the
      * mIndexToActive array.
      */
     static const int kInactive = -1;

     /**
      * \brief All cells in the mIndexToActive array are marked as
      * kUndefinedActive to start with. The reason for this is that we
      * must be able to determine which cells the map covers and since
      * it is the map that determines which cells that are active all
      * cells should be active (i.e. kUndefinedActive) when calling
      * cells() for the map. Cells not covered by the map may then be
      * considered inactive.
      */
     static const int kUndefinedActive = -2;

     /**
      * \brief Maps a cell's position in the grid (r * mCols + c) to
      * its position in the active array. Inactive cells are marked
      * with kInactive. The size of this array is thus mCells.
      */
     int* mIndexToActive;

     /**
      * \brief Maps a cell's position in the active array to its
      * position in the grid (r * mCols + c). The size of this array
      * is thus mActive.
      */
     int* mActiveToIndex;

     void sizeGrid(const Map &m);
     void setCellPositions();

protected:
     int mRows;                ///< Number of rows
     int mCols;                ///< Number of columns
     int mCells;               ///< Number of cells
     int mActive;              ///< Number of active cells
     double mCellSideMeters;   ///< Cell side in meters.
     double mCellArea;         ///< Area of each cell.
     double mTop;      ///< Top coordinate.
     double mBottom;   ///< Bottom coordinate.
     double mLeft;     ///< Leftmost coordinate.
     double mRight;    ///< Rightmost coordinate.
     double mDx;       ///< The width of a cell.
     double mDy;       ///< The height of a cell.          

     AreaHandler* mAH; ///< The AreaHandler for this Grid.

     /// The Map used when creating this Grid.
     const Map& mMap;

     /**
      * \brief An array for storing cell positions in.
      *
      * The array contains lat and lng values for cell 'intersection
      * points' ordered row-wise from top left to bottom right
      * i.e. [lat0 lng0 lat1 lng1...].
      */
     double* mCellPosLatLng;

     /**
      * \brief An array for storing cell positions in.
      *
      * The array contains projected x and y values for cell
      * 'intersection points' ordered row-wise from top left to bottom
      * right i.e. [x0 y0 x1 y1...].
      */
     double* mCellPosProj;

public:
     BasicGrid(const Map &amap, double cellSizeMeters);
     virtual ~BasicGrid();

     /**
      * \brief Accessor for the number of rows.
      *
      * \return The number of rows.
      */
     int rows() const { return mRows; }

     /**
      * \brief Accessor for the number of columns.
      *
      * \return The number of columns.
      */
     int cols() const { return mCols; }

     /**
      * \brief Accessor for the number of cells.
      *
      * \return The number of cells.
      */
     int cells() const { return mCells; }

     /**
      * \brief Accessor for the number of active cells.
      *
      * \return The number of active cells.
      */
     int active() const { return mActive; }

     /**
      * \brief Checks if the specified position is inside active grid.
      *
      * \param r The row.
      * \param c The column.
      * \return True if the specified position is inside active grid,
      * false otherwise.
      */
     bool isActive(int r, int c) const { return mIndexToActive[r * mCols + c] != kInactive; }

     /**
      * \brief Gets the index in the active array from a position in
      * the grid.
      *
      * \param r The row.
      * \param c The column.
      * \return The index in the active array.
      */
     int posToActive(int r, int c) const { return mIndexToActive[r * mCols + c]; }

     /**
      * \brief Gets the position (r * mCols + c) from an index in the
      * active array.
      *
      * \return The position.
      */
     int activeToIndex(int activeIndex) const { return mActiveToIndex[activeIndex]; }

     /**
      * \brief Accessor for the cell side.
      *
      * \return The cell side in meters.
      */
     double cellSideMeters() const { return mCellSideMeters; }

     /**
      * \brief Accessor for the cell area.
      *
      * \return The cell area in square meters.
      */
     double cellAreaKm2() const { return mCellArea / 1000000.0; }

     /**
      * \brief Accessor for the map.
      *
      * \return The map.
      */
     const Map& map() const { return mMap; }

     /**
      * \brief Accessor for the cell positions array.
      *
      * \return The cell positions array.
      */
     const double* cellPosLatLng() const { return mCellPosLatLng; }

     /**
      * \brief Accessor for the cell positions array.
      *
      * \return The cell positions array.
      */
     const double* cellPosProj() const { return mCellPosProj; }


     GridPos cell(const ProjCoord p) const;
     GridPos cell(const LatLng p) const;
     void cells(const Polygon &inP, std::list<GridPos> &outCells) const;
     void cells(const Circle &inC, std::list<GridPos> &outCells) const;

     LatLng center(int r, int c) const;

     // Friends
     friend std::ostream &operator << (std::ostream& o, const BasicGrid& g);
};

#endif    // _APPROXSIMBASICGRID_H
