#ifndef _AREAHANDLER_H
#define _AREAHANDLER_H

// System
#include <list>
//#include <iostream>   // Only for friend ostream << operator of EdgeState

// Own
#include "GridPos.h"
#include "gpc.h"
#include "ProjCoord.h"

// Forward Declarations
class Circle;
class Polygon;


/**                                                                                                 
 * \brief Helper struct for the interior finding algorithm.
 *
 * Represents a linked list of edges.
 *
 * \author Per Alexius (after Michel Abrash's algorithm).
 * \date   $Date: 2005/06/15 09:28:18 $
 */
struct EdgeState {
     /// The next edge.
     struct EdgeState *NextEdge;
     int X;                 ///< The x-coordinate.
     int StartY;            ///< The start y-coordinate.
     /**
      * \brief The number of whole pixels to move horizontally for each
      * pixel moved vertically.
      */
     int WholePixelXMove;
     int XDirection;        ///< The direction to move in (-1 for left, 1 for right).
     int ErrorTerm;         ///< Current error term.
     int ErrorTermAdjUp;    ///< Error term adjustment when moving up.
     int ErrorTermAdjDown;  ///< Error term adjustment when moving down.
     int Count;             ///< Keeps the count.
     int EndX;              ///< For debugging purposes.
     int EndY;              ///< For debugging purposes.
/*      friend std::ostream &operator<<(std::ostream &os, const EdgeState &e) { */
/*           return os << e.X << ", " << e.StartY << " to " << e.EndX << ", " << e.EndY; */
/*      } */
};


/**                                                                                                 
 * \brief Class that finds out which cells a certain shape covers on a
 * certain grid.
 *
 * The algorithm for finding the interior of a polygon is taken from
 * Michel Abrash's Graphics Programming Black Book Special Edition
 * (ISBN 1-57610-174-6).
 *
 * \author Per Alexius
 * \date   $Date: 2005/06/15 09:28:18 $
 */
class AreaHandler {
private:
     int mRows;        ///< Number of rows in the grid.
     int mCols;        ///< Number of columns in the grid.
     double mTop;      ///< Top coordinate.
     double mBottom;   ///< Bottom coordinate.
     double mLeft;     ///< Leftmost coordinate.
     double mRight;    ///< Rightmost coordinate.
     double mDx;       ///< The width of a cell.
     double mDy;       ///< The height of a cell.          
     double mCellSideMeters;   ///< The cells side in meters.

     void BuildGET(std::list<GridPos>& VertexList, EdgeState* NextFreeEdgeStruc, int XOffset, int YOffset) const;
     void MoveXSortedToAET(int YToMove) const;
     void ScanOutAET(int YToScan, std::list<GridPos> &outInterior) const;
     void AdvanceAET() const;
     void XSortAET() const;

     inline GridPos cellPos(double x, double y) const;
     inline GridPos cellPos(const gpc_vertex &p) const;

     inline double borderBetweenRows(int r1, int r2) const;
     inline void addCellsInCurrentRow(std::list<GridPos> &l, int dc) const;
     inline void addCellsInCurrentCol(std::list<GridPos> &l, int dr) const;

     void polygonBoundaryToCellBoundary(gpc_vertex *inP, int inNumPoints, std::list<GridPos> &outB) const;
     void splitBoundary(const std::list<GridPos> &inB, std::list<std::list<GridPos> > &outB) const;
     int getInterior(std::list<GridPos> &VertexList, std::list<GridPos> &outInterior) const;

public:
     /**
      * \brief Creates an areahandler for a grid with the specified parameters.
      *
      * \param rows Number of rows in the grid.           
      * \param cols Number of columns in the grid. 
      * \param t    Top coordinate.                   
      * \param b    Bottom coordinate.                   
      * \param l    Leftmost coordinate.           
      * \param r    Rightmost coordinate.           
      * \param dx   The width of a cell.           
      * \param dy   The height of a cell.          
      * \param cs   The cells side in meters.
      */
     AreaHandler(int rows, int cols, double t, double b, double l, double r, double dx, double dy, double cs) : mRows(rows), mCols(cols), mTop(t), mBottom(b), mLeft(l), mRight(r), mDx(dx), mDy(dy), mCellSideMeters(cs) {}
     GridPos cell(const ProjCoord p) const;
     void cells(const Polygon &inP, std::list<GridPos> &outCells) const;
     void cells(const Circle &inC, std::list<GridPos> &outCells) const;
};

#endif   // _AREAHANDLER_H
