// System
#include <iostream>
#include <cmath>
#include <list>

// Own
#include "AreaHandler.h"
#include "LogStream.h"
#include "debugheader.h"
#include "GoodStuff.h"
#include "gpc.h"
#include "Shape.h"

#define SWAP(a,b) {temp = a; a = b; b = temp;}

using namespace std;

/* Pointers to global edge table (GET) and active edge table (AET) */
static EdgeState *GETPtr;
static EdgeState *AETPtr;

// Private

/**
 * \brief Creates a Global Edge Table (GET) in the buffer pointed to
 * by NextFreeEdgeStruc from the vertex list.
 *
 * Edge endpoints are flipped, if necessary, to guarantee all edges go
 * top to bottom. The GET is sorted primarily by ascending Y start
 * coordinate, and secondarily by ascending X start coordinate within
 * edges with common Y coordinates
 *
 * \param VertexList A list of vertices.
 * \param NextFreeEdgeStruc Allocated memory for all EdgeState structs.
 * \param XOffset Horizontal offset.
 * \param YOffset Verical offset.
 */
void AreaHandler::BuildGET(list<GridPos>& VertexList, EdgeState *NextFreeEdgeStruc, int XOffset, int YOffset) const
{
     int StartX, StartY, EndX, EndY, DeltaY, DeltaX, Width, temp;
     struct EdgeState *NewEdgePtr;
     struct EdgeState *FollowingEdge, **FollowingEdgeLink;

     /* Scan through the vertex list and put all non-0-height edges into
        the GET, sorted by increasing Y start coordinate */
     GETPtr = NULL;    /* initialize the global edge table to empty */
     for (list<GridPos>::iterator i = VertexList.begin(); i != VertexList.end(); i++) {
          /* Calculate the edge height and width */
          StartX = i->c + XOffset;
          StartY = i->r + YOffset;
          /* The edge runs from the current point to the previous one */
          if (i == VertexList.begin()) {
               /* Wrap back around to the end of the list */
               EndX = VertexList.back().c + XOffset;
               EndY = VertexList.back().r + YOffset;
          } else {
               list<GridPos>::iterator ii = i;
               ii--;
               EndX = ii->c + XOffset;
               EndY = ii->r + YOffset;
          }
          /* Make sure the edge runs top to bottom */
          if (StartY > EndY) {
               SWAP(StartX, EndX);
               SWAP(StartY, EndY);
          }
          /* Skip if this can't ever be an active edge (has 0 height) */
          if ((DeltaY = EndY - StartY) != 0) {
               /* Allocate space for this edge's info, and fill in the
                  structure */
               NewEdgePtr = NextFreeEdgeStruc++;
               NewEdgePtr->XDirection =   /* direction in which X moves */
                    ((DeltaX = EndX - StartX) > 0) ? 1 : -1;
               Width = abs(DeltaX);
               NewEdgePtr->X = StartX;
               NewEdgePtr->EndX = EndX;   //P
               NewEdgePtr->EndY = EndY;   //P
               NewEdgePtr->StartY = StartY;
               NewEdgePtr->Count = DeltaY;
               NewEdgePtr->ErrorTermAdjDown = DeltaY;
               if (DeltaX >= 0)  /* initial error term going L->R */
                    NewEdgePtr->ErrorTerm = 0;
               else              /* initial error term going R->L */
                    NewEdgePtr->ErrorTerm = -DeltaY + 1;
               if (DeltaY >= Width) {     /* Y-major edge */
                    NewEdgePtr->WholePixelXMove = 0;
                    NewEdgePtr->ErrorTermAdjUp = Width;
               } else {                   /* X-major edge */
                    NewEdgePtr->WholePixelXMove =
                         (Width / DeltaY) * NewEdgePtr->XDirection;
                    NewEdgePtr->ErrorTermAdjUp = Width % DeltaY;
               }
               /* Link the new edge into the GET so that the edge list is
                  still sorted by Y coordinate, and by X coordinate for all
                  edges with the same Y coordinate */
               FollowingEdgeLink = &GETPtr;
               for (;;) {
                    FollowingEdge = *FollowingEdgeLink;
                    if ((FollowingEdge == NULL) ||
                        (FollowingEdge->StartY > StartY) ||
                        ((FollowingEdge->StartY == StartY) &&
                         (FollowingEdge->X >= StartX))) {
                         NewEdgePtr->NextEdge = FollowingEdge;
                         *FollowingEdgeLink = NewEdgePtr;
                         break;
                    }
                    FollowingEdgeLink = &FollowingEdge->NextEdge;
               }
          }
     }
}

/**
 * \brief Sorts all edges currently in the active edge table into
 * ascending order of current X coordinates
 */
void AreaHandler::XSortAET() const
{
     struct EdgeState *CurrentEdge, **CurrentEdgePtr, *TempEdge;
     int SwapOccurred;

     /* Scan through the AET and swap any adjacent edges for which the
        second edge is at a lower current X coord than the first edge.
        Repeat until no further swapping is needed */
     if (AETPtr != NULL) {
          do {
               SwapOccurred = 0;
               CurrentEdgePtr = &AETPtr;
               while ((CurrentEdge = *CurrentEdgePtr)->NextEdge != NULL) {
                    if (CurrentEdge->X > CurrentEdge->NextEdge->X) {
                         /* The second edge has a lower X than the first;
                            swap them in the AET */
                         TempEdge = CurrentEdge->NextEdge->NextEdge;
                         *CurrentEdgePtr = CurrentEdge->NextEdge;
                         CurrentEdge->NextEdge->NextEdge = CurrentEdge;
                         CurrentEdge->NextEdge = TempEdge;
                         SwapOccurred = 1;
                    }
                    CurrentEdgePtr = &(*CurrentEdgePtr)->NextEdge;
               }
          } while (SwapOccurred != 0);
     }
}

/**
 * \brief Advances each edge in the AET by one scan line. Removes
 * edges that have been fully scanned.
 */
void AreaHandler::AdvanceAET() const
{
     struct EdgeState *CurrentEdge, **CurrentEdgePtr;

     /* Count down and remove or advance each edge in the AET */
     CurrentEdgePtr = &AETPtr;
     while ((CurrentEdge = *CurrentEdgePtr) != NULL) {
          /* Count off one scan line for this edge */
          if ((--(CurrentEdge->Count)) == 0) {
//        if ((--(CurrentEdge->Count)) == -1) {  //P Draw bottom line too
               /* This edge is finished, so remove it from the AET */
               *CurrentEdgePtr = CurrentEdge->NextEdge;
          } else {
               /* Advance the edge's X coordinate by minimum move */
               CurrentEdge->X += CurrentEdge->WholePixelXMove;
               /* Determine whether it's time for X to advance one extra */
               if ((CurrentEdge->ErrorTerm +=
                    CurrentEdge->ErrorTermAdjUp) > 0) {
                    CurrentEdge->X += CurrentEdge->XDirection;
                    CurrentEdge->ErrorTerm -= CurrentEdge->ErrorTermAdjDown;
               }
               CurrentEdgePtr = &CurrentEdge->NextEdge;
          }
     }
}

/**
 * \brief Moves all edges that start at the specified Y coordinate
 * from the GET to the AET, maintaining the X sorting of the AET.
 *
 * \param YToMove The y-coordinate for which to move edges.
 */
void AreaHandler::MoveXSortedToAET(int YToMove) const
{
     struct EdgeState *AETEdge, **AETEdgePtr, *TempEdge;
     int CurrentX;

     /* The GET is Y sorted. Any edges that start at the desired Y
        coordinate will be first in the GET, so we'll move edges from
        the GET to AET until the first edge left in the GET is no longer
        at the desired Y coordinate. Also, the GET is X sorted within
        each Y coordinate, so each successive edge we add to the AET is
        guaranteed to belong later in the AET than the one just added */
     AETEdgePtr = &AETPtr;
     while ((GETPtr != NULL) && (GETPtr->StartY == YToMove)) {
          CurrentX = GETPtr->X;
          /* Link the new edge into the AET so that the AET is still
             sorted by X coordinate */
          for (;;) {
               AETEdge = *AETEdgePtr;
               if ((AETEdge == NULL) || (AETEdge->X >= CurrentX)) {
                    TempEdge = GETPtr->NextEdge;
                    *AETEdgePtr = GETPtr;  /* link the edge into the AET */
                    GETPtr->NextEdge = AETEdge;
                    AETEdgePtr = &GETPtr->NextEdge;
                    GETPtr = TempEdge;   /* unlink the edge from the GET */
                    break;
               } else {
                    AETEdgePtr = &AETEdge->NextEdge;
               }
          }
     }
}

/**
 * \brief Fills the scan line described by the current AET at the
 * specified Y coordinate.
 *
 * \param YToScan The y-coordinate for which to fill the scan line.
 * \param outInterior A list that on return contains the same values
 * as on entry plus the cells on the line just scanned.
 */
void AreaHandler::ScanOutAET(int YToScan, list<GridPos> &outInterior) const
{
     int LeftX;
     struct EdgeState *CurrentEdge;

     /* Scan through the AET, drawing line segments as each pair of edge
        crossings is encountered. The nearest pixel on or to the right
        of left edges is drawn, and the nearest pixel to the left of but
        not on right edges is drawn */
     CurrentEdge = AETPtr;
     while (CurrentEdge != NULL) {
          LeftX = CurrentEdge->X;
          CurrentEdge = CurrentEdge->NextEdge;

          for (int col = LeftX + 1; col < CurrentEdge->X; col++) {
               outInterior.push_back(GridPos(YToScan, col));
          }
          CurrentEdge = CurrentEdge->NextEdge;
     }
}

/**
 * \brief Gets the position in the grid for the cell that contains the
 * specified point.
 *
 * \param x The x coordinate of the point.
 * \param y The y coordinate of the point.
 * \return The position in the grid for the cell that contains the
 * specified point.
 */
inline GridPos AreaHandler::cellPos(double x, double y) const
{
     GridPos po(mRows - static_cast<int>((y - mBottom) / mDy) - 1, static_cast<int>((x - mLeft) / mDx));

//      if (po.c < 0 || po.c >= mCols || po.r < 0 || po.r >= mRows) {
//           approxsimDebug("point " << x << ", " << y << " is outside grid - e.g. " << po);
//      }

     return po;
}

/**
 * \brief Gets the position in the grid for the cell that contains the
 * specified point.
 *
 * \param p The point.
 * \return The position in the grid for the cell that contains the
 * specified point.
 */
inline GridPos AreaHandler::cellPos(const gpc_vertex &p) const
{
     return cellPos(p.x, p.y);
}

/**
 * \brief Gets the y-coordinate of the line separating the two
 * provided rows.
 *
 * \param r1 The first row.
 * \param r2 The second row.
 * \return The y-coordinate of the line separating the two provided
 * rows.
 */
inline double AreaHandler::borderBetweenRows(int r1, int r2) const
{
     return mTop - mDy * static_cast<double>(max(r1, r2));
}

/**
 * \brief Adds specified number of cells in the current row.
 *
 * The current row is the row of the last element in the list.
 *
 * \param l The list which last element indicates the current row and
 * to which to add cells.
 * \param dc The number of cells to add - positive for right, negative
 * for left.
 */
inline void AreaHandler::addCellsInCurrentRow(list<GridPos> &l, int dc) const
{
     GridPos &c1 = l.back();

     if (dc == 0) {
          return;
     }
     else if (dc > 0) {
          for (int c = c1.c + 1; c <= c1.c + dc; c++) {
               l.push_back(GridPos(c1.r, c));
          }
     }
     else {
          for (int c = c1.c - 1; c >= c1.c + dc; c--) {
               l.push_back(GridPos(c1.r, c));
          }
     }
}

/**
 * \brief Adds specified number of cells in the current column.
 *
 * The current column is the column of the last element in the list.
 *
 * \param l The list which last element indicates the current column
 * and to which to add cells.
 * \param dr The number of cells to add - positive for down, negative for up.
 */
inline void AreaHandler::addCellsInCurrentCol(list<GridPos> &l, int dr) const
{
     GridPos &c1 = l.back();

     if (dr == 0) {
          return;
     }
     else if (dr > 0) {
          for (int r = c1.r + 1; r <= c1.r + dr; r++) {
               l.push_back(GridPos(r, c1.c));
          }
     }
     else {
          for (int r = c1.r - 1; r >= c1.r + dr; r--) {
               l.push_back(GridPos(r, c1.c));
          }
     }
}


/**
 * \brief Gets the cells that a polygon boundary overlaps (not the interior).
 *
 *   Does: For each edge in the polygon - check which cells it crosses and
 *      store those cells in a linked list. At exit the linked list contains
 *      all cells in the boundary in order such that there is an edge between
 *      outB[i] and outB[i+1].
 * 
 *   Idea: For each line (p1, p2) between the points p1 and p2 do:              <p>
 *      Find the cells c1 and c2 that contains the points p1 and p2.            <p>
 *      If c1 == c2                                                             <p>
 *         Head on to the next point                                                <p>
 *      Else                                                                        <p>
 *         Calculate number of rows (dr) and cols (dc) the line passes.                <p>
 *         If dr or dc == 0                                                        <p>
 *            We have a trivial case so add edges between cells in the                <p>
 *            current row or column between c1 and c2                                <p>
 *         Else                                                                        <p>
 *            For each row that (p1, p2) passes find out in which column c        <p>
 *            the intersection is and add edges between all cells on that        <p>
 *            row between the current column oldc and c. Also add an edge        <p>
 *            between the cells on current row and the next row, in column c.   <p>
 *         End                                                                        <p>
 *      End
 *
 * \param inP The vertices of the polygon.
 * \param inNumPoints The number of vertices in the polygon.
 * \param outB A list that on return contains the positions of the
 * cells that the provided polygon boundary overlaps.
 */
void AreaHandler::polygonBoundaryToCellBoundary(gpc_vertex *inP, int inNumPoints, list<GridPos> &outB) const
{
//     doublePoint *p1;
//     doublePoint *p2;
     gpc_vertex *p1;
     gpc_vertex *p2;
     GridPos c1;
     GridPos c2;
     GridPos ctmp;
     
     int dr;
     int dc;

     if (inNumPoints < 3) {
          return;
     }

     // Add first cell to boundary cell list
     outB.push_back(cellPos(inP[0]));

     for (int i = 0; i < inNumPoints; i++) {
          p1 = &inP[i];
          p2 = (i == inNumPoints - 1 ? &inP[0] : &inP[i + 1]);   // Wrap at end of poly
          c1 = cellPos(*p1);
          c2 = cellPos(*p2);

          if (!(c1 == c2)) {       // Don't add edge between a cell and itself
               dr = c2.r - c1.r;
               dc = c2.c - c1.c;
               if (dr == 0) {      // Only moving horizontally
                    // Add edges between consecutive cells in row c1.r from c1.c to c2.c
                    // Separate cases for moving right resp. left e.g delta column <> 0
                    addCellsInCurrentRow(outB, dc);
               }
               else if (dc == 0) {   // Only moving vertically
                    // Add edges between consecutive cells in column c1.c from c1.r to c2.r
                    // Separate cases for moving down resp. up e.g delta row <> 0
                    addCellsInCurrentCol(outB, dr);
               }
               else {   // Moving both horizontally and vertically
                    // For each row between the current and the next point's row - check
                    // at which x-coord. the line between p1 and p2 intersects the line
                    // between row and row +- 1. Add cell above and below this intersection
                    // and move on to the next row.
                    double x, y;
                    double oneOverk = (p2->x - p1->x) / (p2->y - p1->y);
                    int lastc = c1.c;   // Keeps track of column for last cross between rows
                    int count = 0;      // Counts the number of rows passed
                    if (dr > 0) {
                         for (int row = c1.r; row < c1.r + dr; row++) {
                              y = borderBetweenRows(row, row + 1);
                              x = (y - p1->y) * oneOverk + p1->x;
                              if (x < 0) {
//                                   approxsimDebug("STOP");
                              }
                              ctmp = cellPos(x, p1->y - count * mDy);
                              addCellsInCurrentRow(outB, ctmp.c - lastc);
                              outB.push_back(GridPos(row + 1, ctmp.c));
                              lastc = ctmp.c;
                              count++;
                         }
                         addCellsInCurrentRow(outB, c2.c - lastc);
                    }
                    else {
                         for (int row = c1.r; row > c1.r + dr; row--) {
                              y = borderBetweenRows(row, row - 1);
                              x = (y - p1->y) * oneOverk + p1->x;
                              if (x < 0) {
//                                   approxsimDebug("STOP");
                              }
                              ctmp = cellPos(x, p1->y + count * mDy);
                              addCellsInCurrentRow(outB, ctmp.c - lastc);
                              outB.push_back(GridPos(row - 1, ctmp.c));
                              lastc = ctmp.c;
                              count++;
                         }
                         addCellsInCurrentRow(outB, c2.c - lastc);
                    }
               }
          }
     }
}

/**
 * \brief Splits the provided boundary so that all loops are stored as
 * separate boundaries.
 *
 *   Does: Splits inB so that all loops are stored as a separate list of cells      <p>
 *      in outB. If inB contains no loops the first element in outB will contain    <p>
 *      a list identical to inB                                                            <p>
 *   Idea: For each cell c in l (l is a copy of inB)                                    <p>
 *      If c was visited before e.g if we have encountered a loop                    <p>
 *         Store cells that are part of the loop in a new list                            <p>
 *         Remove all cells that are part of the loop except c from l                    <p>
 *         Unmark all cells that are part of the loop except c
 *
 * \param inB A list of grid positions constituting a contour of a polygon.
 * \param outB A list of lists that on return contains one list of
 * grid positions for each loop found.
 */
void AreaHandler::splitBoundary(const list<GridPos> &inB, list<list<GridPos> > &outB) const
{
     if (!(inB.back() == inB.front())) {
          slog << "splitBoundary() wants closed polygons and this one wasn't. Aborting!" << logEnd;
          return;
     }

     list<GridPos> l(inB);   // Copy list so we can remove elements as we wish


     int minr = l.front().r;
     int maxr = minr;
     int minc = l.front().c;
     int maxc = minc;

     // Find out max and min row and col that this shape
     // overlaps. Notice that overlapped cells may fall outside
     // grid. Such cells are removed after finding the interior of
     // this shape.
     for (list<GridPos>::iterator it = l.begin(); it != l.end(); it++) {
          minr = min(minr, it->r);
          maxr = max(maxr, it->r);
          minc = min(minc, it->c);
          maxc = max(maxc, it->c);
     }

     int overlappedRows = maxr - minr + 1;
     int overlappedCols = maxc - minc + 1;
     list<GridPos>::iterator nullIt = l.end();

     list<GridPos>::iterator** marked = new list<GridPos>::iterator*[overlappedRows];
     for (int i = 0; i < overlappedRows; i++) {
          marked[i] = new list<GridPos>::iterator[overlappedCols];
     }
     // Bad, bad, broken, lousy intel compiler doesn't allow the next row...
//     list<GridPos>::iterator marked[overlappedRows][overlappedCols];

     for (int r = 0; r < overlappedRows; r++) {
          for (int c = 0; c < overlappedCols; c++) {
               marked[r][c] = nullIt;
          }
     }

     for (list<GridPos>::iterator i = l.begin(); i != l.end(); i++) {
          int rind = i->r - minr;
          int cind = i->c - minc;
          if(marked[rind][cind] != nullIt) {
               // Create new list containing loop
               outB.push_back(list<GridPos>(marked[rind][cind], i));

               // Erase cells in loop from l
               l.erase(marked[rind][cind], i);

               // Unmark cells in loop
               for (list<GridPos>::iterator j = outB.back().begin(); j != outB.back().end(); j++) {
                    marked[j->r - minr][j->c - minc] = nullIt;
               }     
          }
          marked[rind][cind] = i;   // Mark cell as visited and copy the iterator pointing at it
     }
     for (int i = 0; i < overlappedRows; i++) {
          delete [] marked[i];
     }
     delete [] marked;
}

/**
 * \brief Gets the interior grid positions of the provided exterior.
 *
 * \param VertexList A list of grid positions constituting the
 * boundary of the polygon to get the interior for.
 * \param outInterior A list of grid positions that on return contains
 * the same values as on entry plus the interior grid positions.
 */
int AreaHandler::getInterior(list<GridPos> &VertexList, list<GridPos> &outInterior) const
{
     int XOffset = 0;   // Could be used as inparameter
     int YOffset = 0;   // Could be used as inparameter

     struct EdgeState *EdgeTableBuffer;
     int CurrentY;

     /* It takes a minimum of 3 vertices to cause any pixels to be
        drawn; reject polygons that are guaranteed to be invisible */
     if (VertexList.size() < 3)
          return(1);
     /* Get enough memory to store the entire edge table */
     EdgeTableBuffer = new EdgeState[VertexList.size()];

     /* Build the global edge table */
     BuildGET(VertexList, EdgeTableBuffer, XOffset, YOffset);

     /* Scan down through the polygon edges, one scan line at a time,
        so long as at least one edge remains in either the GET or AET */
     AETPtr = NULL;    /* initialize the active edge table to empty */
     CurrentY = GETPtr->StartY; /* start at the top polygon vertex */

     int count = 0; //P
     while ((GETPtr != NULL) || (AETPtr != NULL)) {
          MoveXSortedToAET(CurrentY);  /* update AET for this scan line */
          if (count != 0) {            //P Leave out the top row
               ScanOutAET(CurrentY, outInterior);   /* draw this scan line from AET */
          }
          AdvanceAET();                /* advance AET edges 1 scan line */
          XSortAET();                  /* resort on X */
          CurrentY++;                  /* advance to the next scan line */
          count++;                     //P Only to keep track of the first iteration
     }
     /* Release the memory we've allocated and we're done */
     delete [] EdgeTableBuffer;
     return(1);
}

/**
 * \brief Gets the position in the grid for the cell that contains the
 * specified point.
 *
 * \param p The point.
 * \return The position in the grid for the cell that contains the
 * specified point.
 */
GridPos AreaHandler::cell(const ProjCoord p) const
{
     return cellPos(p.x(), p.y()); 
}

/**
 * \brief Returns a list containing the grid positions of all cells
 * covered by the provided Polygon.
 *
 * \param inP The Polygon to get the grid positions for.
 * \param outCells A list that on return contains the grid positions
 * of all cells covered by the provided Polygon.
 */
void AreaHandler::cells(const Polygon &inP, std::list<GridPos> &outCells) const
{
     list<GridPos> interior;
     list<list<GridPos> > loops;
     
     outCells.sort();

     // Make sure we use projected coordinates for the Polygon.
     const gpc_polygon* pp;
     gpc_polygon tmpPoly = {0, 0, 0};
     if (inP.projected()) {
          pp = &inP.boundary();
     }
     else {
          Polygon::deepCopyGpcPolygon(tmpPoly, inP.boundary());
          Projection::currentProjection()->coordToProj(tmpPoly, tmpPoly);
          pp = &tmpPoly;
     }
     const gpc_polygon &p = *pp;

     // Do our thing...
     for (int i = 0; i < p.num_contours; i++) {
          list<GridPos> tmp;
          polygonBoundaryToCellBoundary(p.contour[i].vertex, p.contour[i].num_vertices, tmp);

          splitBoundary(tmp, loops);

          for (list<list<GridPos> >::iterator it = loops.begin(); it != loops.end(); it++) {
               if (it->size() > 2) {
                    getInterior(*it, interior);
               }
          }

          tmp.sort();
          tmp.unique();
          interior.sort();
          tmp.merge(interior);
          outCells.merge(tmp);
     }
     outCells.unique();   // Must be called on a sorted list

     // Remove positions representing cells outside the grid.
     vector<list<GridPos>::iterator> toRemove;
     for (list<GridPos>::iterator it = outCells.begin(); it != outCells.end(); it++) {
          if (it->r < 0 || it->r >= mRows || it->c < 0 || it->c >= mCols) {
               toRemove.push_back(it);
          }
     }
     for (vector<list<GridPos>::iterator>::iterator it = toRemove.begin(); it != toRemove.end(); ++it) {
          outCells.erase(*it);
     }

     // Dealloc potentially allocated gpc_polygon
     Polygon::deallocGpcPolygon(tmpPoly);
}

/**
 * \brief Returns a list containing the grid positions of all cells
 * covered by the provided Circle.
 *
 * \param inC The Circle to get the grid positions for.
 * \param outCells A list that on return contains pointers to all
 * cells covered by the provided Circle.
 */
void AreaHandler::cells(const Circle &inC, std::list<GridPos> &outCells) const
{
     int    d  = Round(inC.radius() / mCellSideMeters);
//     double r2 = inC.radius() * inC.radius();
     double d2 = d * d;

     ProjCoord p = inC.cenProj();
     GridPos cen = cellPos(p.x(), p.y());
     
     double h, h2;
     double w, w2;
     for (int r = max(cen.r - d, 0); r <= min(cen.r + d, mRows - 1); r++) {
          h  = r - cen.r;
          h2 = h * h;
          for (int c = max(cen.c - d, 0); c <= min(cen.c + d, mCols - 1); c++) {
               w  = c - cen.c;      // Could be precalculated outside loop
               w2 = w * w;          // Could be precalculated outside loop
//               if (h2 + w2 <= r2) {
               if (h2 + w2 <= d2) {
                    outCells.push_back(GridPos(r, c));
               }
          }
     }
}

/*
     list<GridPos>::iterator marked[mRows][mCols];

     for (int r = 0; r < mRows; r++) {
          for (int c = 0; c < mCols; c++) {
               marked[r][c] = 0;
          }
     }

     for (list<GridPos>::iterator i = l.begin(); i != l.end(); i++) {
          if(marked[i->r][i->c] != 0) {
               // Create new list containing loop
               outB.push_back(list<GridPos>(marked[i->r][i->c], i));

               // Erase cells in loop from l
               l.erase(marked[i->r][i->c], i);

               // Unmark cells in loop
               for (list<GridPos>::iterator j = outB.back().begin(); j != outB.back().end(); j++) {
                    marked[j->r][j->c] = 0;
               }     
          }
          marked[i->r][i->c] = i;   // Mark cell as visited and copy the iterator pointing at it
     }
 */
