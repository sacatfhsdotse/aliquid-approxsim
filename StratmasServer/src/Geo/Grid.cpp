
// System
#include <iostream>
#include <limits>   // For numeric_limits<double> (and nextafter()?)

// Own
#include "Action.h"
#include "Camp.h"
#include "CellGroup.h"
#include "City.h"
#include "ClusterSet.h"
#include "debugheader.h"
#include "Distribution.h"
#include "Ellipse.h"
#include "EpidemicsWeights.h"
#include "Error.h"
#include "Faction.h"
#include "GoodStuff.h"
#include "GridCell.h"
#include "Grid.h"
#include "Map.h"
#include "ModelParameters.h"
#include "PresenceObject.h"
#include "PresenceObjectAllocator.h"
#include "Projection.h"
#include "PVArea.h"
#include "PVRegion.h"
#include "random2.h"
#include "Region.h"
#include "ApproxsimConstants.h"
#include "Unit.h"
#include "LogStream.h"

// Temporary
#include "stopwatch.h"
#include "Reference.h"

using namespace std;

// Static Definitions
std::map<int, int> PVModification::sPVTypeMap;


/**
 * \brief Creates a Grid for the provided Map with the specified cell
 * size.
 *
 * \param amap The map to create the Grid for.
 * \param cellSizeMeters The side of the cells in meters.
 * \param factions The number of factions excluding the all faction.
 */
Grid::Grid(const Map& amap, double cellSizeMeters, int factions)
     : BasicGrid(amap, cellSizeMeters),
       mFactions(factions),
       mHDI(0.5),
       mUnemployment(0.5),
       mTotalInitialPopulation(new double[factions + 1]),
       mResettlers(new double[factions + 1]),
       mModelParameters(0),
       mCG(0)
{
     GridCell::setNumberOfFactions(factions);

     mInitialPopulation = new double*[active()];
     for (int i = 0; i < active(); ++i) {
          mInitialPopulation[i] = new double[factions + 1];
     }

     // Allocate memory
     mCell  = new GridCell*[mActive];
     mCellP = new GridCell*[mCells];

     // Create cells
     double dmin = numeric_limits<double>::min();
     double corner[8];
     for (int r = 0; r < mRows; r++) {
          for (int c = 0; c < mCols; c++) {
               if (isActive(r, c)) {
                    // Find corner points of this cell.
                    corner[0] = mCellPosProj[(r + 1) * (mCols + 1) * 2  +  2 *  c         ];                  // x
                    corner[1] = mCellPosProj[(r + 1) * (mCols + 1) * 2  +  2 *  c      + 1];                  // y
                    corner[2] = mCellPosProj[ r      * (mCols + 1) * 2  +  2 *  c         ];                  // x
#ifdef OS_WIN32
                    corner[3] = _nextafter(mCellPosProj[ r      * (mCols + 1) * 2  +  2 *  c      + 1], dmin); // y
                    corner[4] = _nextafter(mCellPosProj[ r      * (mCols + 1) * 2  +  2 * (c + 1)    ], dmin); // x
                    corner[5] = _nextafter(mCellPosProj[ r      * (mCols + 1) * 2  +  2 * (c + 1) + 1], dmin); // y
                    corner[6] = _nextafter(mCellPosProj[(r + 1) * (mCols + 1) * 2  +  2 * (c + 1)    ], dmin); // x
#else
                    corner[3] = nextafter(mCellPosProj[ r      * (mCols + 1) * 2  +  2 *  c      + 1], dmin); // y
                    corner[4] = nextafter(mCellPosProj[ r      * (mCols + 1) * 2  +  2 * (c + 1)    ], dmin); // x
                    corner[5] = nextafter(mCellPosProj[ r      * (mCols + 1) * 2  +  2 * (c + 1) + 1], dmin); // y
                    corner[6] = nextafter(mCellPosProj[(r + 1) * (mCols + 1) * 2  +  2 * (c + 1)    ], dmin); // x
#endif
                    corner[7] = mCellPosProj[(r + 1) * (mCols + 1) * 2  +  2 * (c + 1) + 1];                  // y

                    int activeIndex = posToActive(r, c);
                    mCell[activeIndex] = new GridCell(*this, activeIndex, corner, factions);
                    mCellP[r * mCols + c] = mCell[activeIndex];
               }
               else {
                    mCellP[r * mCols + c] = 0;
               }
          }
     }

     // Set neighbors
     setCellNeighbors();

     // Set up vectors for clustering
     mVindex = new int[mActive];
     mVx     = new double[mActive];
     mVy     = new double[mActive];
     mVw     = new double[mActive];
     for (int i = 0; i < mActive; ++i) {
          mVindex[i] = 0;
          mVx[i] = mCell[i]->center().lng();
          mVy[i] = mCell[i]->center().lat();
          mVw[i] = 0;
     }

     // Hack for precalculating expensive exp and sqrt operations in the epidemics model
     EpidemicsWeights::setEpidemicsWeights(10000.0, 50000.0, mCellSideMeters, mRows, mCols);

     approxsimDebug("units per cell x: " << mDx);
     approxsimDebug("units per cell y: " << mDy);
     approxsimDebug("rows: " << mRows << ", cols: " << mCols << ", cells: " << mCells);
     approxsimDebug("Number of active cells: " << mActive);
}

/**
 * \brief Destructor.
 */
Grid::~Grid()
{
     if (mCell) {
          for (int i = 0; i < active(); ++i) {
               delete mCell[i];
          }
          delete [] mCell;
     }
     if (mInitialPopulation) {
          for (int i = 0; i < active(); ++i) {
               delete [] mInitialPopulation[i];
          }
          delete [] mInitialPopulation;
     }
     if (mCellP)             { delete [] mCellP; }
     if (mTotalInitialPopulation) { delete [] mTotalInitialPopulation; }
     if (mResettlers)        { delete [] mResettlers; }
     if (mVindex)            { delete [] mVindex; }
     if (mVx)                { delete [] mVx; }
     if (mVy)                { delete [] mVy; }
     if (mVw)                { delete [] mVw; }

     for (vector<Camp*>::iterator it = mCamps.begin(); it != mCamps.end(); ++it) {
          delete *it;
     }
     
     EpidemicsWeights::clear();
}

/**
 * \brief Sets the neighbors of all cells.
 */
void Grid::setCellNeighbors()
{
     for (int r = 0; r < mRows; r++) {
          for (int c = 0; c < mCols; c++) {
               GridCell *thisCell = mCellP[r * mCols + c];
               if (thisCell) {
                    thisCell->setNeighbor(cell(r - 1, c    ), eN );
                    thisCell->setNeighbor(cell(r - 1, c + 1), eNE);
                    thisCell->setNeighbor(cell(r    , c + 1), eE );
                    thisCell->setNeighbor(cell(r + 1, c + 1), eSE);
                    thisCell->setNeighbor(cell(r + 1, c    ), eS );
                    thisCell->setNeighbor(cell(r + 1, c - 1), eSW);
                    thisCell->setNeighbor(cell(r    , c - 1), eW );
                    thisCell->setNeighbor(cell(r - 1, c - 1), eNW);
               }
          }
     }
}

/**
 * \brief Sets some simulation parameters.
 *
 * \param d The disease from the scenario object.
 * \param mp The model parameters from the simulation object.
 * \param HDI The HDI parameter.
 * \param unemployment The unemployment parameter.
 */
void Grid::setParameters(const Disease& d, const ModelParameters& mp, double HDI, double unemployment)
{
     // LISTEN
     mModelParameters = &mp;
     mDisease = &d;

     updateParameters(HDI, unemployment);
}

/**
 * \brief Updates some simulation parameters.
 *
 * \param HDI The HDI parameter.
 * \param unemployment The unemployment parameter.
 */
void Grid::updateParameters(double HDI, double unemployment)
{
     mHDI           = HDI;
     mUnemployment  = unemployment;
}

/**
 * \brief Notifies the Grid about the creation of a new Camp.
 *
 * \param c The newly created Camp.
 */
void Grid::notifyAboutCamp(Camp* c)
{
     if (c) {
          const GridCell* gc = cell(c->location().cenCoord());
          approxsimDebug("Grid added camp in cell " << (gc ? gc->row() : -1) << ", " << (gc ? gc->col() : -1));
          mCamps.push_back(c);
     }
}

/**
 * \brief Spreads the population in 'cities' to the cells in the grid
 * based on the city's distribution with its area as cutoff.
 *
 * \param cities A vector of Cities to spread the population from.
 */
void Grid::populate(const vector<City*>& cities)
{
     GridCell *c;
     vector<City*>::const_iterator mit;

     // Reset population counter
     memset(mTotalInitialPopulation, 0, sizeof(double) * (factions() + 1));
     for (int i = 0; i < active(); ++i) {
          memset(mInitialPopulation[i], 0, sizeof(double) * (factions() + 1));
     }

     for (mit = cities.begin(); mit != cities.end(); mit++) {
          list<GridPos> v;
          list<GridPos>::iterator vit;
          City &city = **mit;       // Create reference for simplicity
          city.location().cells(*this, v);

          vector<double> oa;
          city.deployment().amount(city.center(), v, *this, oa);
          vit = v.begin();
          for (vector<double>::iterator it = oa.begin(); it != oa.end(); ++it) {
               c = cell(*vit);
               if (c) {
                    for (int i = 1; i < mFactions + 1; ++i) {
                         double toAdd = *it * city.population(i);
                         mInitialPopulation[c->index()][i] += toAdd;
                    }
               }
               else {
                    slog << "City covers NULL cell in Grid::populate()" 
                         << logEnd;
               }
               vit++;
          }
     }
     
     const vector<PVInitValue*>& initVals = PVInitValueSet::currentSet()->initValues();
     for (vector<PVInitValue*>::const_iterator it = initVals.begin(); it != initVals.end(); ++it) {
          PVInitValue& pvi = **it;
          if (pvi.pv() == eAllPopulation) {
               for(vector<PVRegion*>::const_iterator it2 = pvi.regions().begin(); it2 != pvi.regions().end(); ++it2) {
                    list<GridPos> v;
                    list<GridPos>::iterator vit;
                    (*it2)->area().cells(*this, v);
                    for (list<GridPos>::iterator it3 = v.begin(); it3 != v.end(); ++it3) {
                         c = cell(*it3);
                         if (c) {
                              if (pvi.factions().empty()) {
                                   for (int i = 1; i < mFactions + 1; ++i) {
                                        mInitialPopulation[c->index()][i] += (*it2)->value();
                                   }
                              }
                              else {
                                   for (vector<const Reference*>::const_iterator it4 = pvi.factions().begin();
                                        it4 != pvi.factions().end(); ++it4) {
                                        EthnicFaction* efac = EthnicFaction::faction(**it4);
                                        mInitialPopulation[c->index()][efac->index()] += (*it2)->value();
                                   }
                              }
                         }
                         else {
                              slog << "PVRegion covers NULL cell in Grid::populate()" << logEnd;
                         }
                    }
               }
          }
     }

     // Zero out all groups in cells where that group's population is less than
     // kMinPopulation. Calculate each cell's total population and the grid's
     // total group and total population.
     for (int i = 0; i < mActive; ++i) {
          c = mCell[i];
          for (int j = 1; j < mFactions + 1; j++) {
               if (mInitialPopulation[i][j] < kMinPopulation) {
                    mInitialPopulation[i][j] = 0;
               }
               else {
                    mTotalInitialPopulation[j] += mInitialPopulation[i][j];
                    mInitialPopulation[i][0] += mInitialPopulation[i][j];
               }
          }
          mTotalInitialPopulation[0] += mInitialPopulation[i][0];

     }

     // Check for large diffs between the total population and the sum of the groups.
     double sum = 0;
     for (int i = 1; i < mFactions + 1; ++i) {
          sum += mTotalInitialPopulation[i];
     }
     if (fabs(sum - mTotalInitialPopulation[0]) > 10) {
          Error e;
          e << "Diff between total population and sum of group population is " << sum - mTotalInitialPopulation[0];
          throw e;
     }
     else {
          mTotalInitialPopulation[0] = sum;
     }
     mTotalPopulation = mTotalInitialPopulation[0];
     approxsimDebug("Total pop: " << sum);
}

void Grid::initializeGrid(const vector<PVArea*>& v)
{
     // Reset population counter
     memset(mTotalInitialPopulation, 0, sizeof(double) * (factions() + 1));
     for (int i = 0; i < active(); ++i) {
          memset(mInitialPopulation[i], 0, sizeof(double) * (factions() + 1));
     }

     int numLayers = eNumWithFac * (factions() + 1) + eNumNoFac;

     std::map<int, int*> areaCount;
     list<GridPos> overlappedCells;
     vector<double> frac;
     vector<double> amount;

     double** gd = new double*[active()];
     for (int i = 0; i < active(); ++i) {
          gd[i] = new double[numLayers];
          memset(gd[i], 0, sizeof(double) * numLayers);
     }

     for (vector<PVArea*>::const_iterator it = v.begin(); it != v.end(); ++it) {
          const PVArea& pva = **it;
          pva.area().cells(*this, overlappedCells);

          int count = 0;
          for (list<GridPos>::iterator it2 = overlappedCells.begin(); it2 != overlappedCells.end(); it2++) {
               GridCell& c = *cell(*it2);
               for (vector<PVModification>::const_iterator it3 = pva.pvs().begin(); it3 != pva.pvs().end(); it3++) {
                    const PVModification& pvm = *it3;
                    double factor;
                    if (pvm.type() == PVModification::eSum) {
                         if (frac.empty()) {
                              pva.distribution().amount(pva.area().cenCoord(), overlappedCells, *this, frac);
                         }
                         factor = frac[count];
                    }
                    else if (pvm.type() == PVModification::eMean) {
                         if (amount.empty()) {
                              pva.distribution().amountMean1(pva.area().cenCoord(), overlappedCells, *this, amount);
                         }
                         factor = amount[count];
                    }
                    else {
                         Error e;
                         e << "Unknown type " << pvm.type() << " for process variable with index " << pvm.pv();
                         throw e;
                    }
                    int layerIndex = (pvm.pv() < eNumWithFac ?
                                      pvm.pv() * (factions() + 1) + pvm.faction().index() :
                                      eNumWithFac * factions() + pvm.pv());
                    gd[c.index()][layerIndex] += pvm.value() * factor;
                    
                    std::map<int, int*>::iterator mit = areaCount.find(c.index());
                    if (mit == areaCount.end()) {
                         int* arr = new int[numLayers];
                         memset(arr, 0, sizeof(int) * numLayers);
                         areaCount[c.index()] = arr;
                         arr[layerIndex]++;
                    }
                    else {
                         mit->second[layerIndex]++;
                    }
               }
               count++;
          }
          frac.clear();
          amount.clear();
          overlappedCells.clear();
     }

     // Calculate mean where needed.
     for (int i = 0; i < active(); ++i) {
          GridCell& c = *cell(i);
          std::map<int, int*>::iterator mit = areaCount.find(i);
          if (mit != areaCount.end()) {
               int* arr = mit->second;
               int pv = 0;
                for (int j = 0; j < numLayers; ++j) {
                    if (PVModification::type(pv) == PVModification::eMean && arr[j] != 0) {
                         gd[c.index()][j] /= static_cast<double>(arr[j]);
                    }
                    if (pv >= eNumWithFac || j % (factions() + 1) == factions()) {
                         ++pv;
                    }
               }
          }
          for (int j = 0; j < factions() + 1; ++j) {
               mInitialPopulation[i][j] = c.pvfGet(ePopulation, j);
               mTotalInitialPopulation[j] += c.pvfGet(ePopulation, j);
          }
     }

     for (std::map<int, int*>::iterator mit = areaCount.begin(); mit != areaCount.end(); mit++) {
          delete [] mit->second;
     }
     for (int i = 0; i < active(); ++i) {
          delete [] gd[i];
     }
     delete [] gd;


     // Check for large diffs between the total population and the sum of the groups.
     double sum = 0;
     for (int i = 1; i < factions() + 1; ++i) {
          sum += mTotalInitialPopulation[i];
     }
     if (fabs(sum - mTotalInitialPopulation[0]) > 10) {
          Error e;
          e << "Diff between total population and sum of group population is " << sum - mTotalInitialPopulation[0];
          throw e;
     }
     else {
          mTotalInitialPopulation[0] = sum;
     }
     approxsimDebug("Total pop: " << sum);
}

/**
 * \brief Initializes the Grid.
 */
void Grid::init(const vector<Region*>& regions)
{
     // Remove camps
     for (vector<Camp*>::iterator it = mCamps.begin(); it != mCamps.end(); ++it) {
          delete *it;
     }
     mCamps.clear();
     
     // Precalculated attributes - notice that all must be reset
     // before any can be updated.
     for (int i = 0; i < mActive; ++i) {
          mCell[i]->pcfReset();
          mCell[i]->pcReset();
     }

     // Initialize the attributes - requires that the following values are set:
     // - Initial population in cells e.g. mTotalInitialPopulation[0 .. mFactions]
     // - HDI
     // - Unemployment
     // Also requires that AttrPopulation is initialized before the other
     // attributes
     for (int i = 0; i < mActive; ++i) {
          mCell[i]->init();
     }

     // Handle pv initializating regions.
     const vector<PVInitValue*>& initVals = PVInitValueSet::currentSet()->initValues();
     for (vector<PVInitValue*>::const_iterator it = initVals.begin(); it != initVals.end(); ++it) {
          PVInitValue& pvi = **it;
          if (pvi.pv() != eAllPopulation) {
               for(vector<PVRegion*>::const_iterator it2 = pvi.regions().begin(); it2 != pvi.regions().end(); ++it2) {
                    list<GridPos> v;
                    list<GridPos>::iterator vit;
                    (*it2)->area().cells(*this, v);
                    for (list<GridPos>::iterator it3 = v.begin(); it3 != v.end(); ++it3) {
                         GridCell* c = cell(*it3);
                         if (c) {
                              if (pvi.factions().empty()) {
                                   for (int i = 1; i < mFactions + 1; ++i) {
                                        c->pvAllSetR(pvi.pv(), i, (*it2)->value());
                                   }
                              }
                              else {
                                   for (vector<const Reference*>::const_iterator it4 = pvi.factions().begin();
                                        it4 != pvi.factions().end(); ++it4) {
                                        EthnicFaction* efac = EthnicFaction::faction(**it4);
                                        c->pvAllSetR(pvi.pv(), efac->index(), (*it2)->value());
                                   }
                              }
                         }
                         else {
                              slog << "PVRegion covers NULL cell in Grid::populate()" 
                                   << logEnd;
                         }
                    }
               }
          }
     }
     
     // Check limits and recalculate sums and averages for pvs with
     // factions after getting values from regions.
     for (int i = 0; i < active(); ++i) {
          cell(i)->adjustValues();
     }

     // Sum up region population - needed in GridCell::doPrecalculated()
     updateRegions(regions);

     for (int i = 0; i < mActive; ++i) {
          mCell[i]->doPrecalculated();
     }
     // Sum up food import need after population changes - needed in
     // GridCell::doDerived()
     updateRegions(regions);
     // Derived PV:s
     for (int i = 0; i < mActive; ++i) {
          mCell[i]->doDerived();
     }     
}

/**
 * \brief Advances the Grid one timestep.
 */
void Grid::step(const vector<Region*>& regions)
{
     if (!mCG) {
          Error e("No CombatGrid in Grid::step()");
          throw e;
     }

     // Precalculated
     for (int i = 0; i < mActive; ++i) {
          // Zero out the precalculated from the previous timestep. Notice
          // that all must be reset before any can be updated.
          mCell[i]->pcfReset();
          mCell[i]->pcReset();
     }
     for (int i = 0; i < mActive; ++i) {
          mCell[i]->doPrecalculated();
     }     

     // Sum up the number of protected people that will resettle this timestep.
     memset(mResettlers, 0, (mFactions + 1) * sizeof(double));
     for (int i = 0; i < mActive; ++i) {
          for (int j = 1; j < mFactions + 1; ++j) {
               mResettlers[j] += mCell[i]->pvfGet(eProtected, j) * kFractionProtectedResettling;
          }
     }
     for (int j = 1; j < mFactions + 1; ++j) {
          mResettlers[0] += mResettlers[j];
     }


     // Calculate next timestep for all cells
     for (int i = 0; i < mActive; ++i) {
          mCell[i]->update();
     }

     // Move data for next timestep to current timestep-buffer
     for (int i = 0; i < mActive; ++i) {
          mCell[i]-> makeCalculatedTSCurrentTS();
     }

     // Sum up food import need after population changes - needed in
     // GridCell::doDerived()
     updateRegions(regions);

     // Calculate derived PV:s
     for (int i = 0; i < mActive; ++i) {
          mCell[i]->doDerived();
     }     

//      static int foo = 0;
//      if (foo % 1 == 0) {
//           ofstream ofs("new.tmp");
//           approxsimDebug("--- DUMPING ---");
//           for (int i = 0; i < mActive; ++i) {
//                ofs << *mCell[i];
//           }
//      }
//      foo++;

     // Handle round off errors (duh!)
     for (int i = 0; i < mActive; ++i) {
          mCell[i]->handleRoundOffErrors();
     }
}

void Grid::updateRegions(const vector<Region*>& regions)
{
     vector<Region*>::const_iterator it;
     for (it = regions.begin(); it != regions.end(); ++it) {
          (*it)->update();
     }
     mRegionFoodSurplus = 0;
     mRegionFoodDeficit = 0;
     mTotalPopulation = 0;
     for (it = regions.begin(); it != regions.end(); ++it) {
          double val = (*it)->pvrGet(eRFoodSurplusDeficit);
          if (val > 0) {
               mRegionFoodSurplus += val;
          }
          else {
               mRegionFoodDeficit += val;
          }
          mTotalPopulation += (*it)->cellGroup().pvfGet(ePopulation);
     }
}

/**
 * \brief Gets the cell that covers the specified location.
 *
 * \return The cell that covers the specified location or null if no
 * such cell exists.
 */
GridCell* Grid::cell(const LatLng& p)
{
     return cell(BasicGrid::cell(p.toCoord()));
}

/**
 * \brief Returns a list containing the grid cells covered by the
 * provided Polygon.
 *
 * \param p The Polygon to get the grid cells for.
 * \param outCells A list that on return contains the grid cells
 * covered by the provided Polygon.
 */
void Grid::cells(const Polygon& p, std::list<GridCell*>& outCells)
{
     list<GridPos> l;
     BasicGrid::cells(p, l);
     outCells.clear();
     for (list<GridPos>::iterator it = l.begin(); it != l.end(); ++it) {
          GridCell *gp = cell(*it);
          if (gp) {
               outCells.push_back(cell(*it));
          }
     }
}

/**
 * \brief Returns a list containing the grid cells covered by the
 * provided Polygon.
 *
 * \param p The Polygon to get the grid cells for.
 * \param outCells A list that on return contains the grid cells
 * covered by the provided Polygon.
 */
void Grid::cells(const Polygon& p, std::list<const GridCell*>& outCells) const
{
     list<GridPos> l;
     BasicGrid::cells(p, l);
     outCells.clear();
     for (list<GridPos>::iterator it = l.begin(); it != l.end(); ++it) {
          const GridCell *gp = cell(*it);
          if (gp) {
               outCells.push_back(cell(*it));
          }
     }
}

/**
 * \brief Returns a list containing the grid cells covered by the
 * provided Circle.
 *
 * \param inC The Circle to get the grid cells for.
 * \param outCells A list that on return contains the grid cells
 * covered by the provided Circle.
 */
void Grid::cells(const Circle& inC, std::list<GridCell*>& outCells)
{
     list<GridPos> l;
     BasicGrid::cells(inC, l);
     outCells.clear();
     for (list<GridPos>::iterator it = l.begin(); it != l.end(); ++it) {
          GridCell *gp = cell(*it);
          if (gp) {
               outCells.push_back(cell(*it));
          }
     }
}

/**
 * \brief Returns a list containing the grid cells covered by the
 * provided Circle.
 *
 * \param inC The Circle to get the grid cells for.
 * \param outCells A list that on return contains the grid cells
 * covered by the provided Circle.
 */
void Grid::cells(const Circle& inC, std::list<const GridCell*>& outCells) const
{
     list<GridPos> l;
     BasicGrid::cells(inC, l);
     outCells.clear();
     for (list<GridPos>::iterator it = l.begin(); it != l.end(); ++it) {
          const GridCell *gp = cell(*it);
          if (gp) {
               outCells.push_back(cell(*it));
          }
     }
}

/**
 * \brief Expose the Grid to an Action.
 *
 * \param inA The Action to expose the Grid to.
 */
void Grid::expose(const Action &inA)
{
     const GridAction &a = dynamic_cast<const GridAction&>(inA);

     if (!&a) {
          approxsimDebug("Tried to expose Grid to non GridAction. Ignoring...");
          return;
     }

     // Currently we let the activity last the whole day
     double duration = 1;

     // Get covered cells 
     list<GridPos> l;
     a.location().cells(*this, l);

     // Get the fraction of the capacity that goes to each cell.
     vector<double> frac;
     if (a.performer()) {
          a.performer()->deployment().amount(a.location().cenCoord(), l, *this, frac);
     }
     else {
          // Activities without performer is assumed to be normal
          // distributed with sigma that is the approximate radius of
          // a circle approximately enclosing an approximate square
          // area with as many cells as the given area. Right...
          NormalDistribution nd(sqrt(static_cast<double>(l.size())) * cellSideMeters() / 4);
          nd.amount(a.location().cenCoord(), l, *this, frac);
     }
     
     for (int i = 0; i < a.effects(); ++i) {
          GridEffect e = a.effect(i);
          double severity = e.mSeverity;

          //P Go through these cells and 'hit' them.
          vector<double>::iterator dit = frac.begin();
          for (list<GridPos>::iterator it = l.begin(); it != l.end(); ++it) {
               GridCell& c = *cell(*it);
               if (c.pvfGet(ePopulation) > kMinPopulation) {
                    const Unit* u = dynamic_cast<const Unit*>(a.performer());
                    if (u) {
                         switch (e.mPV) {
                         case eAllDisplaced:
                         case eAllProtected:
                         case eAllViolence:
                         case eAllPerceivedThreat:
                         case eAllFractionCrimeVictims:
                         {
                              // Make sure we don't care about the faction for SingelAttributes
                              int faction = (e.mPV == eAllFractionCrimeVictims ?
                                             0 : e.mFaction->index());
                              
                              // Calculate the new severity based on the performing unit's
                              // strength and the target attribute's magnitude in the
                              // target cell.
                              double popToUse = (e.mPV == eAllProtected ?
                                                 c.pvfGet(eProtected, faction) + 
                                                 c.pvfGet(eDisplaced, faction) :
                                                 c.pvfGet(ePopulation, faction));
                              
                               if (popToUse != 0) {
                                    severity = e.mSeverity * 10.0 * u->personnel() / popToUse;
                               }
                              break;
                         }
                         default:
                              break;
                         }
                    }

                    double size = between(0.05 * (*dit) * severity * duration, -0.5, 0.5);
                    c.expose(e.mPV, *e.mFaction, size);
//                        approxsimDebug("Exposing " << PVHelper::allPVName(e.mPV) << ", faction " << e.mFaction->ref().name()
//                              << ", magnitude " << size);
               }
               dit++;
          }
     }
}


/**
 * \brief For debugging purposes.
 *
 * \param o The stream to write to.
 * \param g The Grid to print.
 * \return The provided ostream with the Grid written to it.
 */
std::ostream &operator << (std::ostream& o, const Grid& g)
{
     ePVF attr = ePopulation;
     double limit[4] = {100, 1000, 10000, 100000};
//     double limit[4] = {0.1, 0.2, 0.3, 0.5};
//     double limit[4] = {10, 20, 30, 50};
     for (int r = 0; r < g.mRows; r++) {
          for (int c = 0; c < g.mCols; c++) {
               char ch;
               const GridCell *cell = g.cell(r, c);
               if (cell) {
                    double val = cell->pvfGet(attr);
                    if (val >= 0 && val < limit[0]) {
                         ch = '.';
                    }
                    else if (val >= limit[0] && val < limit[1]) {
                         ch = 'o';
                    }
                    else if (val >= limit[1] && val < limit[2]) {
                         ch = 'O';
                    }
                    else if (val >= limit[2] && val < limit[3]) {
                         ch = '0';
                    }
                    else {
                         ch = '#';
                    }
               }
               else {
                    ch = ' ';
               }
               o << ch;
          }
          o << endl;
     }
     return o;
}


/**
 * \brief Gets the cell in which the camp that is closest to the
 * provided point is located.
 *
 * \param p The point to measure from.
 * \param dist Contains the distance in meters to the nearest Camp on
 * successful return, undefined otherwise.
 * \return The cell in which the nearest camp is located or null if
 * there are no camps.
 */
GridCell *Grid::getCellForNearestCamp(LatLng p, double &dist)
{
     Camp* resCamp = 0;
     if (!mCamps.empty()) {
          resCamp = mCamps.front();
          dist = p.squDistanceTo(resCamp->center());
          
          for (std::vector<Camp*>::iterator it = mCamps.begin(); it != mCamps.end(); ++it) {
               double newDist = p.squDistanceTo((*it)->center());
               if (newDist < dist) {
                    dist = newDist;
                    resCamp = *it;
               }
          }
          dist = sqrt(dist);
     }
     return (resCamp ? cell(resCamp->center()) : 0);
}
