#ifndef STRATMAS_GRIDCELL_H
#define STRATMAS_GRIDCELL_H

// System
#include <vector>
#include <cstring>

// Own
#include "debugheader.h"
#include "GoodStuff.h"
#include "LatLng.h"
#include "ProcessVariables.h"
#include "Region.h"


// Forward Declarations
class City;
class EthnicFaction;
class Grid;
class Region;


/// Enumeration for facilitating neighbor indexing.
enum eNeighbor {
     eN,
     eNE,
     eE,
     eSE,
     eS,
     eSW,
     eW,
     eNW,
     eNumNeighbors
};



/**
 * \brief This class represents a cell in the Grid.
 *
 * \author   Per Alexius
 * \date     $Date: 2007/01/28 17:07:49 $
 */
class GridCell {
private:
     static int sFactions;
     static double sCellAreaKm2;

     Grid&       mGrid;       ///< Reference to the grid this cell is a part of.
     int         mIndex;      ///< This cell's index in the active cells array.
     int         mPos;        ///< This cell's position in the grid i.e. r * nCol + c.
     int         mRow;        ///< The row of this cell.
     int         mCol;        ///< The column of this cell.
     GridCell**  mNeighbor;   ///< An array of pointers to this cells neighboring cells.
     LatLng      mCenter;     ///< The center coordinate of this cell.

     /// Contains all regions overlapping this cell.
     std::vector<const Region*> mRegions;

     int mReadInd;
     int mWriteInd;

     double** mPVF[2];
     double* mPV[2];
     double* mPreCalcF;
     double* mPreCalc;
     double* mDerivedF;
     double* mDerived;

     double mWellWaterFraction;

     inline void position(const double* corner);

     inline double pvrGet(eRegionPV pvr);
     inline double regionParam(eRegionParameter param);
     inline double regionPVFGet(ePVF pvf, int f = 0);

     inline void doPopulation(double* data);
     inline void doDisplaced(double* data);
     inline void doSheltered(double* data);
     inline void doViolence(double* data);
     inline void doPerceivedThreat(double* data);
     inline void doInsurgents(double* data);
     inline void doEthnicTension(double* data);
     inline void doFractionCrimeVictims(double* data);
     inline void doHousingUnits(double* data);
     inline void doStoredFood(double* data);
     inline void doFoodConsumption(double* data);
     inline void doFarmStoredFood(double* data);
     inline void doMarketedFood(double* data);
     inline void doFoodDays(double* data);
     inline void doWaterConsumption(double* data);
     inline void doWaterDays(double* data);
     inline void doSusceptible(double* data);
     inline void doInfected(double* data);
     inline void doRecovered(double* data);
     inline void doDeadDueToDisease(double* data);
     inline void doFractionInfected(double* data);
     inline void doFractionRecovered(double* data);
     inline void doFractionNoWork(double* data);

     inline void doProtected(double* data);
     inline void doFractionNoMedical(double* data) { doNothing(eFractionNoMedical); }
     inline void doFractionNoFood(double* data);
     inline void doSuppliedWater(double* data) { doNothing(eSuppliedWater); }
     inline void doFractionNoWater(double* data);
     inline void doInfrastructure(double* data);

     inline void doTEST(double* data);

     inline void doDDisaffection(double* data);
     inline void doDPolarization(double* data);
     inline void doDHoused(double* data);
     inline void doDAvailableFood(double* data);
     inline void doDFoodDeprivation(double* data);
     inline void doDWaterSurplusDeficit(double* data);
     inline void doDWaterDeprivation(double* data);

     inline void doNothingF(ePVF pv) { memcpy(mPVF[mWriteInd][pv], mPVF[mReadInd][pv], (sFactions + 1) * sizeof(double)); }
     inline void doNothing(ePV pv) { mPV[mWriteInd][pv] = mPV[mReadInd][pv]; }

     void exposePercent(double* data, const EthnicFaction& fac, double size);
     void exposeDisplaced(double* data, const EthnicFaction& fac, double size);
     void exposeSheltered(double* data, const EthnicFaction& fac, double size);
     void exposeProtected(double* data, const EthnicFaction& fac, double size);
     void exposeInsurgents(double* data, const EthnicFaction& fac, double size);

     void exposeFraction(double* data, double size);
     void exposeFoodDays(double* data, double size);
     void exposeWaterDays(double* data, double size);
     void exposeFractionInfected(double* data, double size);
     void exposeFractionRecovered(double* data, double size);

public:
     GridCell(Grid& g, int activeIndex, const double* corners, int nGrp);
     ~GridCell();

     /**
      * \brief Mutator for the faction count.
      *
      * \param num The number of factions.
      */
     static void setNumberOfFactions(int num) { sFactions = num; }

     /**
      * \brief Accessor for the index.
      *
      * \return The index.
      */
     int index() const { return mIndex; }

     /**
      * \brief Accessor for the position.
      *
      * \return The position.
      */
     int pos() const { return mPos; }

     /**
      * \brief Accessor for the row.
      *
      * \return The row.
      */
     int row() const { return mRow; }

     /**
      * \brief Accessor for the column.
      *
      * \return The column.
      */
     int col() const { return mCol; }

     static double areaKm2() { return sCellAreaKm2; }

     /**
      * \brief Accessor for the center coordinate.
      *
      * \return The center coordinate.
      */
     LatLng center() const { return mCenter; }

     /**
      * \brief Gets the square of the distance in meters between this
      * cell and the provided cell.
      *
      * \param g The cell to measure the distance to.
      * \return The square of the distance in meters between this cell
      * and the provided cell.
      */
     double squDistanceTo(const GridCell& g) const   { return mCenter.squDistanceTo(g.mCenter); }

     /**
      * \brief Accessor for the neighbors.
      *
      * \param i The neighbor as defined in eNeighbor.
      * \return The neighbor in the specified direction or null if no
      * such neighbor exists.
      */
     GridCell* neighbor(int i) { return mNeighbor[i]; };

     /**
      * \brief Mutator for the neighbors.
      *
      * \param cell The neighboring cell.
      * \param dir The direction this neighbor are located in.
      */
     void setNeighbor(GridCell* cell, eNeighbor dir) { mNeighbor[dir] = cell; }
     /**
      * \brief Swaps the attribute buffers.
      */
     void makeCalculatedTSCurrentTS() { mReadInd = !mReadInd; mWriteInd = !mWriteInd; }

     void addOverlappingRegion(const Region& r) { mRegions.push_back(&r); }
     int numOverlappingRegions() const { return mRegions.size(); }

     void init();
     void update();
     void expose(eAllPV pv, const EthnicFaction& faction, double size);
     void adjustValues();
     void handleRoundOffErrors();

     friend std::ostream& operator << (std::ostream& o, const GridCell& c);

     /**
      * \brief Not applicable since there is no fighting.
      *
      * \return 0.
      */
     int dailyShots() const { return 0; }

     /**
      * \brief Not applicable since there is no fighting.
      *
      * \return 0.
      */
     double smoothedShots() const { return 0; }

     double weight() const { return 1.0 / static_cast<double>(mRegions.size()); }

     double pvfGet(ePVF pv, int f = 0) const { return mPVF[mReadInd][pv][f]; }
     void pvfSet(ePVF pv, int f, double value) { mPVF[mWriteInd][pv][f] = value; }
     void pvfSetR(ePVF pv, int f, double value) { mPVF[mReadInd][pv][f] = value; }
     void pvfAdd(ePVF pv, int f, double value) { mPVF[mWriteInd][pv][f] += value; }
     void pvfAddR(ePVF pv, int f, double value) { mPVF[mReadInd][pv][f] += value; }

     double pvGet(ePV pv) const { return mPV[mReadInd][pv]; }
     void pvSet(ePV pv, double value) { mPV[mWriteInd][pv] = value; }
     void pvSetR(ePV pv, double value) { mPV[mReadInd][pv] = value; }
     void pvAdd(ePV pv, double value) { mPV[mWriteInd][pv] += value; }
     void pvAddR(ePV pv, double value) { mPV[mReadInd][pv] += value; }


     double pdfGet(eDerivedF pv, int f = 0) const { return mDerivedF[pv * (sFactions + 1)  + f]; }
     void pdfSet(eDerivedF pv, int f, double value) { mDerivedF[pv * (sFactions + 1)  + f] = value; }
     void pdfAdd(eDerivedF pv, int f, double value) { mDerivedF[pv * (sFactions + 1)  + f] += value; }
     void pdfReset() { memset(mDerivedF, 0, eDNumDerivedF * (sFactions + 1) * sizeof(double)); }

     double pdGet(eDerived pv) const { return mDerived[pv]; }
     void pdSet(eDerived pv, double value) { mDerived[pv] = value; }
     void pdAdd(eDerived pv, double value) { mDerived[pv] += value; }
     void pdReset() { memset(mDerived, 0, eDNumDerived * sizeof(double)); }


     double pcfGet(ePreCalcF pv, int f = 0) const { return mPreCalcF[pv * (sFactions + 1)  + f]; }
     void pcfSet(ePreCalcF pv, int f, double value) { mPreCalcF[pv * (sFactions + 1)  + f] = value; }
     void pcfAdd(ePreCalcF pv, int f, double value) { mPreCalcF[pv * (sFactions + 1)  + f] += value; }
     void pcfReset() { memset(mPreCalcF, 0, ePNumPreCalcF * (sFactions + 1) * sizeof(double)); }

     double pcGet(ePreCalc pv) const { return mPreCalc[pv]; }
     void pcSet(ePreCalc pv, double value) { mPreCalc[pv] = value; }
     void pcAdd(ePreCalc pv, double value) { mPreCalc[pv] += value; }
     void pcReset() { memset(mPreCalc, 0, ePNumPreCalc * sizeof(double)); }

     void pvAllSet(eAllPV pv, int f, double value);
     void pvAllSetR(eAllPV pv, int f, double value);

     void recalculateAllFaction();
     void setSumR(ePVF pv);
     void setPopulationWeightedAverageR(ePVF pv);

     void doDerived();
     void doPrecalculated();

     static inline void handleRoundOffErrorsPositive(double* data, int size = 1);
     static inline void handleRoundOffErrorsPercent(double* data, int size = 1);
     static inline void handleRoundOffErrorsFraction(double* data, int size = 1);

     double popDensity() const { return pvfGet(ePopulation, 0) / sCellAreaKm2; }
     double bestFoodStorage() const;
     double bestWaterCapacity() const;
     double expectedTension() const;
     std::ostream& print2(std::ostream& o);
     std::vector<const Region*> regions() const { return mRegions; }
};

/**
 * \brief Function object for less-than operator for pointer to
 * GridCells.
 *
 * A cell is less than another cell if it has a smaller row number -
 * or if the row numbers are equal - has a smaller column number.
 *
 * \author   Per Alexius
 * \date     $Date: 2007/01/28 17:07:49 $
 */
struct lessGridCellPtr {
     /**
      * \brief Less-than operator for pointers to GridCells.
      *
      * \param c1 The first cell.
      * \param c2 The second cell.
      * \return True if the first cell is less than the other cell,
      * false otherwise.
      */
     bool operator()(const GridCell* c1, const GridCell* c2) {
          return (c1->row() < c2->row() || c1->row() == c2->row() && c1->col() < c2->col());
     }
};

/**
 * \brief Function object for equality operator for pointer to
 * GridCells.
 *
 * Two cells are equal if their row and column number matches.
 *
 * \author   Per Alexius
 * \date     $Date: 2007/01/28 17:07:49 $
 */
struct equalGridCellPtr {
     /**
      * \brief Equality operator for pointers to GridCells.
      *
      * \param c1 The first cell.
      * \param c2 The second cell.
      * \return True if the two cells are equal.
      */
     bool operator()(const GridCell* c1, const GridCell* c2) {
          return (c1->row() == c2->row() && c1->col() == c2->col());
     }
};


void GridCell::handleRoundOffErrorsPositive(double* data, int size)
{
     for (int i = 0; i < size; i++) {
          data[i] = std::max(0.0, data[i]);
     }
}

void GridCell::handleRoundOffErrorsPercent(double* data, int size)
{
     for (int i = 0; i < size; i++) {
          data[i] = between(data[i], 0.0, 100.0);
     }
}

void GridCell::handleRoundOffErrorsFraction(double* data, int size)
{
     for (int i = 0; i < size; i++) {
          data[i] = between(data[i], 0.0, 1.0);
     }
}


#endif   // STRATMAS_GRIDCELL_H

