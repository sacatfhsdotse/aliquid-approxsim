#include <cstring>
#include "CombatGrid.h"
#include "debugheader.h"
#include "Distribution.h"
#include "Faction.h"
#include "Grid.h"
#include "GridCell.h"
#include "ModelParameters.h"
#include "PresenceObject.h"
#include "PVInfo.h"
#include "random2.h"
#include "Reference.h"
#include "Shape.h"
#include "Simulation.h"  // For fractionOfDay()
#include "Unit.h"


using namespace std;


const char* CombatGrid::kCasualtyStr = " casualties";
const char* kForceSumStr    = "Force sum";
const char* kCasualtySumStr = "Casualty sum";
const char* kBlueSumStr     = "Blue sum";

const double kSmallNumber = 0.00001;


/**
 * \brief Creats a CombatGrid for the specified map and forces.
 *
 * \param amap The map.
 * \param grid The Grid.
 * \param forceVec A vector with the top units in all forces.
 */
CombatGrid::CombatGrid(Map& amap, Grid& grid, vector<Unit*>& forceVec, const std::vector<Faction*>& facVec)
     : BasicGrid(amap, grid.cellSideMeters()),
       mGrid(grid),
       mForces(forceVec),
       mGridData(0)
{
     mGrid.setCombatGrid(this);

     // Two layers for each faction, one for the sum, one for the
     // casualties sum, one for insurgent casualties and one for the
     // sum of 'blue' forces :P
     mNumLayers = facVec.size() * 2 + 4;
     mResetableLayer = new bool[mNumLayers];

     for (int i = 0; i < mNumLayers - 4; i+=2) {
          mResetableLayer[i] = true;
          mResetableLayer[i+1] = false;
     }
     // Force sum layer
     mResetableLayer[mNumLayers - 4] = true;
     // Casualty sum layer
     mResetableLayer[mNumLayers - 3] = false;
     // Insurgent casualty layer
     mResetableLayer[mNumLayers - 2] = false;
     // Blue sum layer
     mResetableLayer[mNumLayers - 1] = true;
     

     // Allocate grid.
     mGridData = new double*[mNumLayers];
     for (int i = 0; i < mNumLayers; i++) {
          mGridData[i] = new double[mActive];
     }

     // Remove old simulation dependent PVDescriptions.
     PVInfo::reset();

     // Initialize name to index map
     int count = 0;
     for (vector<Faction*>::const_iterator it = facVec.begin(); it != facVec.end(); it++) {
          string facName = (*it)->ref().name();
          string casualtyStr = (*it)->ref().name() + kCasualtyStr;
          mIndexToName.push_back(facName);
          mNameToIndex[facName] = count++;
          PVInfo::addPV(facName, "combat", "Forces", false, "0", "INF");
          mIndexToName.push_back(casualtyStr);
          mNameToIndex[casualtyStr] = count++;
          PVInfo::addPV(casualtyStr, "combat", "Forces", false, "0", "INF");
     }

     // The force sum layer
     mIndexToName.push_back(kForceSumStr);
     mSumLayerIndex = count++;
     mNameToIndex[kForceSumStr] = mSumLayerIndex;
//     PVInfo::addPV(kForceSumStr, "combat", false, "0", "INF");

     // The casualty sum layer
     mIndexToName.push_back(kCasualtySumStr);
     mCasualtySumLayerIndex = count++;
     mNameToIndex[kCasualtySumStr] = mCasualtySumLayerIndex;
     PVInfo::addPV(kCasualtySumStr, "combat", "Forces", false, "0", "INF");

     // The insurgent casualty layer.
     string casualtyStr = string("Insurgents") + kCasualtyStr;
     mIndexToName.push_back(casualtyStr);
     mInsurgentLayerIndex = count++;
     mNameToIndex[casualtyStr] = mInsurgentLayerIndex;
     PVInfo::addPV(casualtyStr, "combat", "Environmental", false, "0", "INF");

     // The blue sum layer     
     mIndexToName.push_back(kBlueSumStr);
     mBlueLayerIndex = count++;
     mNameToIndex[kBlueSumStr] = mBlueLayerIndex;
//     PVInfo::addPV(kBlueSumStr, "combat", false, "0", "INF");

     // There will be a double reset for the resetable layers but this
     // is acceptable since this only happens at creation.
     reset(true);

     // Place units on the grid.
     unitsToGrid();
}

/**
 * \brief Destructor
 */
CombatGrid::~CombatGrid()
{
     if (mGridData) {
          for (int i = 0; i < mNumLayers; i++) {
               if (mGridData[i]) {
                    delete [] mGridData[i];
               }
          }
          delete [] mGridData;
     }
     if (mResetableLayer) { delete [] mResetableLayer; }
}

void CombatGrid::objectAdded(const Reference& ref, int64_t initiator)
{
}

void CombatGrid::objectRemoved(const Reference& ref, int64_t initiator)
{
}

/**
 * \brief Resets grid layers.
 *
 * May reset all layers (as when reseting the whole CombatGrid) or
 * only the resetable layers (as in each timestep).
 *
 * \param includeNonResetables Indicates whether the 'non resetable'
 * layers should be reset.
 */
void CombatGrid::reset(bool includeNonResetables)
{
     for (int i = 0; i < mNumLayers; i++) {
          if (mResetableLayer[i] || includeNonResetables) {
               for (int j = 0; j < mActive; j++) {
                    mGridData[i][j] = 0;
               }
          }
     }
}

/**
 * \brief Adds the personnel of the provided unit to the cells it
 * covers using the deployment Distribution.
 *
 * \param u The unit which personnel should be added to the grid.
 */
void CombatGrid::unitToGrid(Unit& u)
{
     vector<double> a;
     list<GridPos> c;

     // Find the cells that this unit covers.
     u.location().cells(*this, c);

     // Store the percentage of precence in each covered cell in the
     // list 'a' (sorted in the same order as the cells in 'c').
     u.deployment().amount(u.center(), c, *this, a);

     // Add presence to the grid.
     int layer = nameToIndex(u.faction().ref().name());
     if (layer == -1) {
          Error e;
          e << "Unit '" << u.ref() << "' with affiliation '" << u.faction().ref().name();
          e << "' could not be mapped to any layer in the CombatGrid.";
          throw e;
     }
     vector<double>::iterator ait = a.begin();
     for(list<GridPos>::iterator it = c.begin(); it != c.end(); it++) {
          int cellIndex = posToActive(it->r, it->c);
          mGridData[layer][cellIndex]          +=  *ait * u.personnel();
          mGridData[mSumLayerIndex][cellIndex] +=  *ait * u.personnel();
          if (u.color() == eBlue) {
               mGridData[mBlueLayerIndex][cellIndex] +=  *ait * u.personnel();
          }
          ait++;
     }

     // Handle subunits.
     for (vector<Unit*>::const_iterator it = u.subunits().begin(); it != u.subunits().end(); it++) {
          unitToGrid(**it);
     }
}

/**
 * \brief Aggregates (by summing) values for all layers for the
 * specified cells.
 *
 * \param pos A list of GridPos specifying the cells to aggregate.
 * \param outAgg An array that must be of at least mNumLayers lenght
 * in which the aggregated values will be placed on return.
 * \return The outAgg array.
 */
double* CombatGrid::aggregate(const std::list<GridPos>& pos, double*& outAgg) const
{
     // Reset
     memset(outAgg, 0, mNumLayers * sizeof(double));

     // Sum up values for the different forces.
     for (list<GridPos>::const_iterator it = pos.begin(); it != pos.end(); it++) {
          for (int i = 0; i < mNumLayers; i++) {
               outAgg[i] += value(*it, i);
          }
     }

     return outAgg;
}

/**
 * \brief Aggregates (by summing) values for all layers for the cells
 * covered by the provided Shape.
 *
 * \param region A Shape specifying the region over which to
 * aggregate.
 * \param outAgg An array that must be of at least mNumLayers lenght
 * in which the aggregated values will be placed on return.
 * \return The outAgg array.
 */
double* CombatGrid::aggregate(const Shape& region, double*& outAgg) const
{
     list<GridPos> pos;
     region.cells(*this, pos);
     return aggregate(pos, outAgg);
}

/**
 * \brief Resets the CombatGrid.
 *
 * Called only when resetting a Scenario.
 *
 * \param forceVec A vector with all top units in the scenario.
 */
void CombatGrid::reset(std::vector<Unit*>& forceVec)
{
     mForces = forceVec;
     // There will be a double reset for the resetable layers but this
     // is acceptable since this function is called only when
     // resetting a scernario, which should be seldom.
     reset(true);
     mPOA.reset();
     unitsToGrid();
}

/**
 * \brief Calculates the personnel in each cell for all units.
 */
void CombatGrid::unitsToGrid()
{
     reset();
     for (vector<Unit*>::iterator it = mForces.begin(); it != mForces.end(); it++) {
          unitToGrid(**it);
     }
}

/**
 * \brief Marks the presence of the provided Unit by creating a
 * PresenceObject for every cell the Unit overlaps.
 *
 * \param u The unit to mark presence for.
 */
void CombatGrid::markPresence(Unit& u)
{
     if (u.present()) {
          vector<double> a;
          list<GridPos> c;

          // Find the cells that this unit covers.
          u.location().cells(*this, c);

          // Store the percentage of precence in each covered cell in the
          // list 'a' (sorted in the same order as the cells in 'c').
          u.deployment().amount(u.center(), c, *this, a);

          // Mark presence
          vector<double>::iterator ait = a.begin();
          for(list<GridPos>::iterator it = c.begin(); it != c.end(); it++) {
               PresenceObject* po = u.getPresence(mPOA, posToActive(it->r, it->c), (*ait));
               if (po) {
                    mPresence.insert(po);
               }
               ait++;
          }
     }

     // Handle subunits.
     for (vector<Unit*>::const_iterator it = u.subunits().begin(); it != u.subunits().end(); it++) {
          markPresence(**it);
     }
}

/**
 * \brief Create PresenceObjects for all cells overlapped by any
 * unit.
 *
 * For each cell overlapped by a unit we will also create a
 * PresenceObject for the insurgents in that cell.
 */
void CombatGrid::markPresence()
{
     for (vector<Unit*>::iterator it = mForces.begin(); it != mForces.end(); it++) {
          markPresence(**it);
     }
}

/**
 * \brief Setup function that should be called at the beginning of
 * each timestep, before the units act.
 *
 * Marks the presence of all units and registers which units that will
 * fight each other etc.
 */
void CombatGrid::setUpBattleField()
{
     Simulation::fractionOfDay();
          
     // Mark all cells overlapped by any unit
     markPresence();
     
     vector<PresenceObject*> combatantsInCurrentCell;
     set<PresenceObject*, lessPresenceObjectPointer>::iterator it;
     for (it = mPresence.begin(); it != mPresence.end(); ) {
          // Get cell index for this PresenceObject,
          int index = (*it)->cell();

          // Pick out all PresenceObjects referring to this cell index
          // (the set is sorted according to cell index).
          for (; it != mPresence.end() && (*it)->cell() == index; it++) {
               combatantsInCurrentCell.push_back(*it);
          }

          // Takes two to fight...
          if (combatantsInCurrentCell.size() > 1) {
               // Every combatant could battle every other combatant.
               for (vector<PresenceObject*>::iterator attacker = combatantsInCurrentCell.begin();
                    attacker != combatantsInCurrentCell.end(); attacker++) {
                    (*attacker)->affect(combatantsInCurrentCell);
               }
          }

          combatantsInCurrentCell.clear();
     }
}

/**
 * \brief Registration function that should be called at the end of
 * each timestep, after the units have acted.
 *
 * Handles insurgent combat and registers casualties etc.
 */
void CombatGrid::registerCombat()
{
     // Insurgent combat
     vector<PresenceObject*> unitsInCurrentCell;
     set<PresenceObject*, lessPresenceObjectPointer>::iterator it;
     double areakm2 = mGrid.cellAreaKm2();
     for (it = mPresence.begin(); it != mPresence.end(); ) {
          // Get cell index for this PresenceObject,
          int index = (*it)->cell();
          GridCell& c = *mGrid.cell(index);
          
          // Pick out all PresenceObjects referring to this cell index
          // (the set is sorted according to cell index).
          unitsInCurrentCell.clear();
          for (; it != mPresence.end() && (*it)->cell() == index; it++) {
               unitsInCurrentCell.push_back(*it);
          }
          
          // Don't care if the total amount of insurgents is to small.
          if (c.pvfGet(eInsurgents) / areakm2 < 0.3) {
               continue;
          }

          // Hostility vectors - example: If faction 1 and 3 will
          // fight unit 0, faction 2 unit 1 and faction 2 and 3 will
          // fight unit 2, the vectors will look like:
          // Hostility[0] (Unit 0) =  [1 3]
          // Hostility[1] (Unit 1) =  [2]
          // Hostility[2] (Unit 2) =  [2 3]
          vector<vector<int> > hostility(unitsInCurrentCell.size());
          
          // Maps faction number to the sum of the number of personnel
          // for the units that faction fights.
          // Allocate array
          double* totEnemyCount = new double[mGrid.factions()];
          memset(totEnemyCount, 0, sizeof(double) * mGrid.factions());
          
          for (unsigned int i = 0; i < unitsInCurrentCell.size(); i++) {
               Unit& u = unitsInCurrentCell[i]->unit();
               for (int j = 1; j < mGrid.factions() + 1; j++) {
                    if (u.faction().isHostileTowards(*EthnicFaction::faction(j))) {
                         totEnemyCount[j - 1] += value(index, u.faction().ref().name());
                    }
               }
          }

          // Set up hostility vectors.
          for (unsigned int i = 0; i < unitsInCurrentCell.size(); i++) {
               Unit& u = unitsInCurrentCell[i]->unit();
               for (int j = 1; j < mGrid.factions() + 1; j++) {
                    double ins = c.pvfGet(eInsurgents, j);
                    // See ***PAPERREF*** for a description of when insurgents attack.
                    if (ins / areakm2 > 10.0 && 
                        c.pvfGet(ePopulation, j) / c.pvfGet(ePopulation) > 0.35 &&
                        ins / totEnemyCount[j - 1] > 0.1 &&
                        u.faction().isHostileTowards(*EthnicFaction::faction(j))) {
//                          stratmasDebug(EthnicFaction::faction(j)->ref().name() << " is hostile towards "
//                                << u.ref().name() << " since " << ins /areakm2 << " > 10 and "
//                                << c.pvfGet(ePopulation, j) / c.pvfGet(ePopulation) << " > 0.35 and "
//                                << ins / max(kSmallNumber, totEnemyCount[j - 1]) << " > 0.1");
                         hostility[i].push_back(j);
                    }
                    else if (u.color() == eBlue &&
                             ins / areakm2 > 0.3   &&
                             ins / max(kSmallNumber, value(index, mBlueLayerIndex)) > 0.03 && 
//                               ins / value(index, u.faction().ref().name()) > 0.1 && 
                             u.faction().isHostileTowards(*EthnicFaction::faction(j))) {
                         
//                          stratmasDebug(EthnicFaction::faction(j)->ref().name() << " is hostile towards "
//                                << u.ref().name() << " since " << ins /areakm2 << " > 1 and "
//                                << ins / value(index, u.faction().ref().name()) << " > 0.1");

                          hostility[i].push_back(j);
                    } 
               }
          }
          // Deallocate array
          delete [] totEnemyCount;
          totEnemyCount = 0;
          // Note that it is probably possible to reuse totEnemyCount
          // for totEnemyStrength below;

          // Maps faction number to the sum of the modified strength
          // for the units that faction fights.
          // Allocate array
          double* totEnemyStrength = new double[mGrid.factions()];
          memset(totEnemyStrength, 0, sizeof(double) * mGrid.factions());

          for (unsigned int i = 0; i < unitsInCurrentCell.size(); i++) {
               for (unsigned int j = 0; j < hostility[i].size(); j++) {
                    totEnemyStrength[hostility[i][j] - 1] +=
                         unitsInCurrentCell[i]->unit().modifiedStrength();
               }
          }

          // Ok, lets fight
//          GridCell& c = *mGrid.cell(index);
          // Allocate array
          double* facDamage = new double[mGrid.factions()];
          memset(facDamage, 0, sizeof(double) * mGrid.factions());
          
          for (unsigned int i = 0; i < unitsInCurrentCell.size(); i++) {
               unsigned int numHostileFacs = hostility[i].size();
               if (numHostileFacs > 0) {
                    Unit &u = unitsInCurrentCell[i]->unit();
                    double facImpact = 0;

                    // Sum up the number of hostile insurgents so we can calculate the
                    // fraction of the unit's offensive capacity that should be allocated to
                    // respective faction.
                    double numHostileInsurgents = 0;
                    for (unsigned int j = 0; j < numHostileFacs; j++) {
                         numHostileInsurgents += c.pvfGet(eInsurgents, hostility[i][j]);
                    }

                    // Calculate the impact of units on factions and vice verca.
                    for (unsigned int j = 0; j < numHostileFacs; j++) {
                         int faction = hostility[i][j];

                         // Units divide their offensive capacity between the factions
                         // proportionally to the size of the faction.
                         double unitImpact = u.modifiedStrength() * unitsInCurrentCell[i]->fraction() * 
                              c.pvfGet(eInsurgents, faction) / numHostileInsurgents * Simulation::fractionOfDay();

                         // Factions divide their offensive capacity between the units
                         // proportionally to the size of the unit.
                          double facFactor = Simulation::fractionOfDay() * mGrid.mp().mp(eInsurgentStrengthFactor) *
                              u.modifiedStrength() / totEnemyStrength[faction - 1];
                         
                         // Don't fight insurgents if we're fighting other units.
                         if (!u.combatSituation() && !u.searching() && unitImpact > 0) {
                              facDamage[faction - 1] += unitImpact;
//                              stratmasDebug(u.ref().name() << " impacts faction " << faction << " with " << unitImpact);
                         }
                         if (c.pvfGet(eInsurgents, faction) * facFactor > 0) {
                              facImpact += c.pvfGet(eInsurgents, faction) * facFactor;
//                                stratmasDebug("### Faction " << faction << " impacts " << u.ref().name() <<
//                                      " with " << c.pvfGet(eInsurgents, faction) * facFactor);
                         }
                    }
                    // Register unit casualties to unit and CombatGrid
                    double casualties = u.registerInsurgentImpact(index, facImpact);
                     mGridData[nameToIndex(u.faction().ref().name() + kCasualtyStr)][index] += casualties;
                     mGridData[nameToIndex(kCasualtySumStr)][index] += casualties;
               }
          }
          // Deallocate array
          delete [] totEnemyStrength;
          totEnemyStrength = 0;
          
          
          // Register insurgent delta to Grid and CombatGrid.
          for (int i = 0; i < mGrid.factions(); i++) {
               double damage = min(facDamage[i], c.pvfGet(eInsurgents, i + 1));
               c.pvfAddR(eInsurgents, i + 1, -damage);
               c.pvfAddR(eInsurgents, 0, -damage);
               mGridData[mInsurgentLayerIndex][index] += damage;
          }

          // Deallocate array
          delete [] facDamage;
          facDamage = 0;
     }
     
     // Force on force combat + register insurgent damage
     for (vector<Unit*>::iterator uit = mForces.begin(); uit != mForces.end(); uit++) {
          (*uit)->registerCombat(*this);
     }

     // Deallocate memory for PresenceObjects.
     for (it = mPresence.begin(); it != mPresence.end(); it++) {
          mPOA.dismiss(*it);
     }

     mPresence.clear();
     unitsToGrid();
}

/**
 * \brief For debugging purposes.
 *
 * \param o The stream to write to.
 * \param c The CombatGrid to print.
 * \return The stream with the CombatGrid written to it.
 */
ostream& operator << (ostream& o, const CombatGrid& c)
{
     for (int j = 0; j < c.mNumLayers; ++j) {
          o << c.indexToName(j) << endl;
          for (int i = 0; i < c.mActive; ++i) {
               o << c.value(i, j) << " ";
          }
     }
     return o;
}
