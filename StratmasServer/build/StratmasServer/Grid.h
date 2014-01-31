#ifndef STRATMAS_GRID_H
#define STRATMAS_GRID_H

// System
#include <list>
#include <vector>

// Own
#include "BasicGrid.h"
#include "GridPos.h"
#include "LatLng.h"

// Forward Declarations
class Action;
class Camp;
class Circle;
class City;
class CombatGrid;
class Disease;
class Faction;
class GridCell;
class Map;
class ModelParameters;
class Polygon;
class PresenceObject;
class PresenceObjectAllocator;
class PVArea;
class Region;
class Unit;


/**
 * \brief This class represents the simulation grid.
 *
 * \author   Per Alexius
 * \date     $Date: 2007/01/28 17:07:48 $
 */
class Grid : public BasicGrid {
private:
     int mFactions;            ///< Number of ethnic groups
     double mHDI;              ///< The HDI parameter.
     double mUnemployment;     ///< The HDI parameter.
     double mRegionFoodSurplus;
     double mRegionFoodDeficit;
     double mTotalPopulation;

     /// Initial population of each group in the entire grid
     double* mTotalInitialPopulation;

     /**
      * \brief An array that contains the initial population of each
      * ethnic group in each cell where mInitialPopulation[i][j] means
      * the population of faction j in cell i - i refering to the
      * active array.
      */
     double** mInitialPopulation;

     double* mResettlers;

     GridCell** mCell;    ///< Array of pointers to active cells - size = mActive.
     GridCell** mCellP;   ///< Array of pointers to cells - size = mCells.

     /// A vector containing all camps.
     std::vector<Camp*> mCamps;

     int*    mVindex;   ///< Indices of cells when clustering.
     double* mVx;       ///< X-coordinates of cell centers when clustering.
     double* mVy;       ///< Y-coordinates of cell centers when clustering.
     double* mVw;       ///< Weights of cells when clustering.

     /// An object containing various model parameters.
     const ModelParameters* mModelParameters;

     /// An object containing disease parameters.
     const Disease* mDisease;

     const CombatGrid* mCG; ///< Pointer to the combat grid.

     void setCellNeighbors();

public:
     Grid(const Map &amap, double cellSizeMeters, int nEthnic);
     virtual ~Grid();

     /**
      * \brief Accessor for the number of factions, excluding the
      * 'all' faction.
      *
      * \return The number of factions, excluding the 'all' faction.
      */
     int factions() const { return mFactions; }

     /**
      * \brief Accessor for the number of camps.
      *
      * \return The number of camps.
      */
     int camps() const { return mCamps.size(); }

     /**
      * \brief Accessor for camps
      *
      * \param i The index in the mCamps vector.
      * \return The specified camp or null if no such camp exists..
      */
     Camp* camp(unsigned int i) const { return (i < mCamps.size() ? mCamps[i] : 0); }

     /**
      * \brief Accessor for the HDI parameter.
      *
      * \return The HDI parameter.
      */
     double HDI() const { return mHDI; }

     /**
      * \brief Accessor for the unemployment parameter.
      *
      * \return The unemployment parameter.
      */
     double unemployment()  const { return mUnemployment; }

     double regionFoodSurplus() const { return mRegionFoodSurplus; }
     double regionFoodDeficit() const { return mRegionFoodDeficit; }
     void updateRegions(const std::vector<Region*>& regions);

     /**
      * \brief Accessor for the total population in the entire grid.
      *
      * \return The total population.
      */
     double totalPopulation() const { return mTotalPopulation; }

     /**
      * \brief Accessor for the initial population of a faction in the
      * entire grid.
      * 
      * \param f The faction index.
      * \return The initial population for the specified faction.
      */
     double totalInitialPopulation(int f = 0)  const { return mTotalInitialPopulation[f]; }

     /**
      * \brief Accessor for the initial population of a faction in a
      * cell.
      *
      * \param i The cell index (in the active array).
      * \param f The faction index.
      * \return The initial population for the specified cell and faction.
      */
     double initialPopulation(int i, int f = 0)  const { return mInitialPopulation[i][f]; }

     double resettlers(int f) const { return mResettlers[f]; }

     /**
      * \brief Accessor for the cell center x coordinate array used
      * for clustering.
      *
      * \return The cell center x coordinate array.
      */
     const double* const cellCenterCoordsX() const { return mVx; }

     /**
      * \brief Accessor for the cell center y coordinate array used
      * for clustering.
      *
      * \return The cell center y coordinate array.
      */
     const double* const cellCenterCoordsY() const { return mVy; }

     /**
      * \brief Accessor for the cell center y coordinate array used
      * for clustering.
      *
      * \return The cell center y coordinate array.
      */
     const CombatGrid* cg() const { return mCG; }

     /**
      * \brief Gets the cell with the specified index in the active
      * cells array.
      *
      * \return The cell with the specified index in the active cells
      * array or null if no such cell exists
      */
     GridCell* cell(int ind) const { return (ind >= 0 && ind < mActive ? mCell[ind] : 0); }

     /**
      * \brief Gets the cell with the specified row and column number.
      *
      * \return The cell with the specified row and column number or
      * null if no such cell exists.
      */
     GridCell* cell(int r, int c) {
	  int pos = r * mCols + c; return (pos >= 0 && pos < mCells ? mCellP[pos] : 0);
     }

     /**
      * \brief Gets the cell at the specified grid position.
      *
      * \return The cell at the specified grid position or null if no
      * such cell exists.
      */
     GridCell* cell(const GridPos& p) { return cell(p.r, p.c); };

     /**
      * \brief Gets the cell with the specified row and column number.
      *
      * \return The cell with the specified row and column number or
      * null if no such cell exists.
      */
     const GridCell* cell(int r, int c) const {
	  int pos = r * mCols + c; return (pos >= 0 && pos < mCells ? mCellP[pos] : 0);
     }

     /**
      * \brief Gets the cell at the specified grid position.
      *
      * \return The cell at the specified grid position or null if no
      * such cell exists.
      */
     const GridCell *cell(const GridPos &p) const { return cell(p.r, p.c); };

     GridCell* cell(const LatLng& p);
     void cells(const Polygon& p, std::list<GridCell*>& outCells);
     void cells(const Polygon& p, std::list<const GridCell*>& outCells) const;
     void cells(const Circle& inC, std::list<GridCell*>& outCells);
     void cells(const Circle& inC, std::list<const GridCell*>& outCells) const;

     void setParameters(const Disease& d, const ModelParameters& mp, double HDI, double unemployment);
     void updateParameters(double HDI, double unemployment);
     void notifyAboutCamp(Camp* c);

     /**
      * \brief Accessor for the ModelParameters.
      *
      * \return The ModelParameters.
      */
     const ModelParameters& mp() const { return *mModelParameters; }

     /**
      * \brief Accessor for the Disease.
      *
      * \return The Disease.
      */
     const Disease& disease() const { return *mDisease; }

     void populate(const std::vector<City*>& cities);
     void init(const std::vector<Region*>& regions);
     void initializeGrid(const std::vector<PVArea*>& v);

     void step(const std::vector<Region*>& regions);
     void expose(const Action& inA);

     /**
      * \brief Mutator for the CombatGrid
      *
      * \param cg The CombatGrid.
      */
     void setCombatGrid(const CombatGrid* cg) { mCG = cg; }

     // Friends
     friend std::ostream &operator << (std::ostream& o, const Grid& g);

     GridCell *getCellForNearestCamp(LatLng p, double& dist);
};

#endif    // STRATMAS_GRID_H
