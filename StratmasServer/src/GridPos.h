#ifndef STRATMAS_GRIDPOS_H
#define STRATMAS_GRIDPOS_H

#include <iostream>

/**
 * \brief A GridPos represents a position in the Grid.
 */
class GridPos {
public:
     /// The column.
     int c;
     /// The row.
     int r;
     /// \brief Default constructor
     GridPos() : c(0), r(0) {}
     /// \brief Constructs a GridPos (ir, ic)
     GridPos(int ir, int ic) : c(ic), r(ir) {}
     /// \brief Compares two GridPos
     inline bool operator == (const GridPos &p) const { return ( (p.r == r) && (p.c == c) ); }
     /// \brief The order of GridPos is as follows. The top left (0, 0) is the
     /// smallest, the top second left (0, 1) is the second smallest etc.
     inline bool operator <  (const GridPos &p) const { return ((r < p.r) || ( (r == p.r) && (c < p.c) )); }
     /// \brief For debugging purposes.
     friend std::ostream &operator<<(std::ostream &os, const GridPos &p) {
          return os << "r: " << p.r << ", c: " << p.c;
     }
};

#endif   // STRATMAS_GRIDPOS_H
