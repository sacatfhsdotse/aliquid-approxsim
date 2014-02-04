/**
 * \brief Static class for precalculating expensive exp() and sqrt()
 * functions used in the 'AttrFractionInfected' attribute.
 *
 * The 'setEpidemicsWeights()' function is called from the Grid's
 * constructor which guarantees that the static members are
 * initialized with reasonable values when the simulation starts.
 */

#ifndef STRATMAS_EPIDEMICSWEIGHTS_H
#define STRATMAS_EPIDEMICSWEIGHTS_H

// System
#include <cmath>

// Own
#include "GoodStuff.h"


class EpidemicsWeights {
private:
     /// Number of rows in the Grid.
     static int     mRows;
     /// Number of colums in the Grid.
     static int     mCols;
     /// Radius of the epidemic's area of influence measured in in cells.
     static int     mHalfNumberOfCells;
     /// Size of the cells.
     static double mCellSideMeters;
     /// Vector containing the weights
     static double *mEpidemicsWeight;

public:
     /// \brief Initialize the weights. Called only by Grid constructor.
     inline static void setEpidemicsWeights(double meanContact, double maxContact, double cellSideMeters,
                                            int nrows, int ncols);

     /// \brief Returns top left and bottom right row and column for the area of
     /// influence measured from 'row' and 'col'.
     inline static void   limits(int row, int col, int &top, int &left, int &bottom, int &right);

     ///  \brief Returns weight 'i' where 'i' is the number of the cell in the epidemic's
     /// area of influence counted from top left to bottom right.
     inline static double weight(int i) { return mEpidemicsWeight[i]; }

     /// \brief Free memory. Called only by Grid destructor.
     inline static void   clear() { if (mEpidemicsWeight) { delete [] mEpidemicsWeight; mEpidemicsWeight = 0; } }
};

inline void EpidemicsWeights::setEpidemicsWeights(double meanContact, double maxContact, double cellSideMeters, int nrows, int ncols)
{
     mRows = nrows;
     mCols = ncols;
     mHalfNumberOfCells = Round(maxContact / cellSideMeters);
     mCellSideMeters = cellSideMeters;

     int dr2, dc, midRow, midCol;
     int top, left, bottom, right;
     double lambdaInv;
     
     midRow        = mRows / 2;
     midCol        = mCols / 2;
     lambdaInv        = 1 / meanContact;
     
     limits(midRow, midCol, top, left, bottom, right);
     
     if (mEpidemicsWeight) {
          delete [] mEpidemicsWeight;
     }

     mEpidemicsWeight = new double[(bottom - top + 1) * (right - left + 1)];
     
     for (int r = top; r <= bottom; r++ ) {
          dr2 = (midRow - r) * (midRow - r);
          for (int c = left; c <= right; c++ ) {
               dc = midCol - c;
               if ( dr2 || dc ) {                        
                    mEpidemicsWeight[c - left + (r - top) * (right - left + 1)] = 
                         exp( -(sqrt(static_cast<double>(dr2+dc*dc)) * mCellSideMeters) * lambdaInv ) * lambdaInv;
               }
               else {
                    mEpidemicsWeight[c - left + (r - top) * (right - left + 1)] = 0;
               }
          }
     }
}

inline void EpidemicsWeights::limits(int row, int col, int &top, int &left, int &bottom, int &right ) {
     top    = std::max(         0, row - mHalfNumberOfCells );
     left   = std::max(         0, col - mHalfNumberOfCells );
     bottom = std::min( mRows - 1, row + mHalfNumberOfCells );
     right  = std::min( mCols - 1, col + mHalfNumberOfCells );
}

#endif   // STRATMAS_EPIDEMICSWEIGHTS_H
