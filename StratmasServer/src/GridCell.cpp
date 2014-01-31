// System
#include <iostream>
#include <sstream>
#include <iomanip>
#include <cstring>

// Own
#include "debugheader.h"
#include "Error.h"
#include "gpc.h"
#include "GridCell.h"
#include "Grid.h"
#include "ProjCoord.h"
#include "StratmasConstants.h"
#include "LogStream.h"
#include "ModelParameters.h"
#include "random.h"

// The process variable update code.
//#include "GridCellPV.cpp"


using namespace std;


// Static Definitions
int    GridCell::sFactions = 1;
double GridCell::sCellAreaKm2 = 0;


/**
 * \brief Creates a GridCell.
 *
 * \param g The Grid to which this cell belongs.
 * \param activeIndex The index of this cell in the active array.
 * \param corners The corners of this cell in lat lng.
 * \param nGrp The number of factions excluding the all faction.
 */
GridCell::GridCell(Grid& g, int activeIndex, const double* corners, int nGrp)
     : mGrid(g),
       mIndex(activeIndex),
       mPos(g.activeToIndex(activeIndex)),
       mNeighbor(new GridCell*[eNumNeighbors])
{
     sCellAreaKm2 = mGrid.cellAreaKm2();
     mRow = mPos / g.cols();
     mCol = mPos % g.cols();

     position(corners);

     for (int i = 0; i < 2; ++i) {
	  mPVF[i] = new double*[eNumWithFac];
	  mPV[i] = new double[eNumNoFac];
	  for (int j = 0; j < eNumWithFac; j++) {
	       mPVF[i][j] = new double[sFactions + 1];
	       memset(mPVF[i][j], 0, (sFactions + 1) * sizeof(double));
	  }
	  memset(mPV[i], 0, eNumNoFac * sizeof(double));
     }
     mPreCalcF = new double[ePNumPreCalcF * (sFactions + 1)];
     mPreCalc = new double[ePNumPreCalc];
     mDerivedF = new double[eDNumDerivedF * (sFactions + 1)];
     mDerived = new double[eDNumDerived];
}

/**
 * \brief Destructor.
 */
GridCell::~GridCell()
{
     delete [] mNeighbor;
     for (int i = 0; i < 2; ++i) {
	  for (int j = 0; j < eNumWithFac; j++) {
	       delete [] mPVF[i][j];
	  }
	  delete [] mPVF[i];
	  delete [] mPV[i];
     }
     delete [] mPreCalcF;
     delete [] mPreCalc;
     delete [] mDerivedF;
     delete [] mDerived;
}

/**
 * \brief Sets the coordinates for the corner points of this cell.
 *
 * \param corner An array of size 8 containing the four corner points
 * of this cell (x0 y0 x1 y1...). The corners are ordered clockwise
 * from the bottom left corner.
 */
void GridCell::position(const double* corner)
{
     mCenter = ProjCoord(corner[0] + (corner[6] - corner[0]) / 2,
			 corner[1] + (corner[3] - corner[1]) / 2).toLatLng();
}

/**
 * \brief Initializes the attributes for this cell.
 */
void GridCell::init()
{
     mReadInd = 0;
     mWriteInd = 1;

     // Population
     for (int i = 0; i < sFactions + 1; i++) {
	  pvfSetR(ePopulation, i, mGrid.initialPopulation(mIndex, i));
     }
     // Displaced
     for (int i = 0; i < sFactions + 1; i++) {
	  pvfSetR(eDisplaced, i, 0);
     }
     // Sheltered
     for (int i = 0; i < sFactions + 1; i++) {
	  pvfSetR(eSheltered, i, 0);
     }
     // Protected
     for (int i = 0; i < sFactions + 1; i++) {
	  pvfSetR(eProtected, i, 0);
     }
     // Violence
     for (int i = 0; i < sFactions + 1; i++) {
	  pvfSetR(eViolence, i, 0);
     }
     // PerceivedThreat
     for (int i = 0; i < sFactions + 1; i++) {
	  pvfSetR(ePerceivedThreat, i, 0);
     }
     // Insurgents
     for (int i = 0; i < sFactions + 1; i++) {
	  pvfSetR(eInsurgents, i, 0);
     }

     double densityFactor = min(0.01 * popDensity(), 1.0 ); // km to m
     pvSetR(eFractionNoMedical, (1.0 - mGrid.HDI()) * sqrt(1.0 - densityFactor));
//     pvSetR(eInfrastructure, 0.2 + densityFactor * mGrid.HDI());
     pvSetR(eInfrastructure, mGrid.HDI());

//     pvSetR(eFractionNoWork, mGrid.unemployment());
     pvSetR(eFractionNoWork, mGrid.HDI());
     pvSetR(eEthnicTension, mGrid.HDI() / 3.0);  // expectedTension() with some 0 values
     pvSetR(eFractionCrimeVictims, 0);
     pvSetR(eHousingUnits, mGrid.HDI() * mGrid.initialPopulation(index()) / mGrid.mp().mp(eMeanFamilySize));
     pvSetR(eStoredFood, 11.0 / 1000.0 * mGrid.initialPopulation(index()));
     pvSetR(eFoodConsumption, 1);
     pvSetR(eFarmStoredFood, 0);
     pvSetR(eMarketedFood, 0);
     pvSetR(eFarmStoredFood, 0);
     pvSetR(eFoodDays, bestFoodStorage());
     pvSetR(eFractionNoFood, 0);
     pvSetR(eWaterConsumption, 20);
     pvSetR(eWaterDays, bestWaterCapacity());
     pvSetR(eSuppliedWater, 0);
     pvSetR(eFractionNoWater, 0);
     pvSetR(eSusceptible, mGrid.mp().mp(eFractionSusceptible) * mGrid.initialPopulation(index()));
     pvSetR(eInfected, (mGrid.initialPopulation(index()) > 0 ? 1 : 0));
     pvSetR(eRecovered, 0);
     pvSetR(eDeadDueToDisease, 0);
     pvSetR(eFractionInfected, 0);
     pvSetR(eFractionRecovered, 0);
     pvSetR(eDailyDead, 0);
     pvSetR(eTotalDead, 0);
     pvSetR(eSmoothedDead, 0);

     pvSetR(eTEST, pvrGet(eRFoodSurplusDeficit));

     mWellWaterFraction = RandomUniform(0.5, 1.0);

     pdfReset();
     pdReset();
}

/**
 * \brief Updates the attributes for this cell, i.e. calculates the
 * next timestep.
 */
void GridCell::update()
{
     if (pvfGet(ePopulation) + pcfGet(ePTowardsCampDelta) + pcfGet(ePDiffusionDelta) < kMinPopulation) {
	  init();
	  makeCalculatedTSCurrentTS();
	  return;
     }

     doPopulation          (mPVF[mWriteInd][ePopulation     ]);
     doDisplaced           (mPVF[mWriteInd][eDisplaced      ]);
     doSheltered           (mPVF[mWriteInd][eSheltered      ]);
     doProtected           (mPVF[mWriteInd][eProtected      ]);
     doViolence            (mPVF[mWriteInd][eViolence       ]);
     doPerceivedThreat     (mPVF[mWriteInd][ePerceivedThreat]);
     doInsurgents          (mPVF[mWriteInd][eInsurgents     ]);

     doEthnicTension       (&mPV[mWriteInd][eEthnicTension       ]);
     doFractionCrimeVictims(&mPV[mWriteInd][eFractionCrimeVictims]);
//      doHousingUnits        (&mPV[mWriteInd][eHousingUnits        ]);
//      doStoredFood          (&mPV[mWriteInd][eStoredFood          ]);
//      doFoodConsumption     (&mPV[mWriteInd][eFoodConsumption     ]);
//      doFarmStoredFood      (&mPV[mWriteInd][eFarmStoredFood      ]);
//      doMarketedFood        (&mPV[mWriteInd][eMarketedFood        ]);
     doFoodDays            (&mPV[mWriteInd][eFoodDays            ]);
//      doWaterConsumption    (&mPV[mWriteInd][eWaterConsumption    ]);
     doWaterDays           (&mPV[mWriteInd][eWaterDays           ]);
//      doSusceptible         (&mPV[mWriteInd][eSusceptible         ]);
//      doInfected            (&mPV[mWriteInd][eInfected            ]);
//      doRecovered           (&mPV[mWriteInd][eRecovered           ]);
//      doDeadDueToDisease    (&mPV[mWriteInd][eDeadDueToDisease    ]);
     doFractionInfected    (&mPV[mWriteInd][eFractionInfected    ]);
     doFractionRecovered   (&mPV[mWriteInd][eFractionRecovered   ]);
     doFractionNoWork      (&mPV[mWriteInd][eFractionNoWork      ]);
     doInfrastructure      (&mPV[mWriteInd][eInfrastructure      ]);

     // Derived?
     doFractionNoMedical   (&mPV[mWriteInd][eFractionNoMedical]);
     doFractionNoFood      (&mPV[mWriteInd][eFractionNoFood   ]);
     doSuppliedWater       (&mPV[mWriteInd][eSuppliedWater    ]);
     doFractionNoWater     (&mPV[mWriteInd][eFractionNoWater  ]);

     doTEST                (&mPV[mWriteInd][eTEST]);

     handleRoundOffErrorsPositive(&mPV[mWriteInd][eTotalDead   ]);
     handleRoundOffErrorsPositive(&mPV[mWriteInd][eDailyDead   ]);
     handleRoundOffErrorsPositive(&mPV[mWriteInd][eSmoothedDead]);
}

/**
 * \brief Sets a PV:s value (write index) from the index in the eAllPV
 * enumeration.
 *
 * \param pv The index in the eAllPV enumeration.
 * \param f The faction index.
 * \param value The value.
 */
void GridCell::pvAllSet(eAllPV pv, int f, double value)
{
     int ipv = pv;
     if (ipv < eNumWithFac) {
	  pvfSet(static_cast<ePVF>(pv), f, value);
     }
     else if (ipv < eNumWithFac + eNumNoFac) {
	  pvSet(static_cast<ePV>(pv), value);
     }
     else {
	  // Silently ignore invalid pv:s
// 	  Error e;
// 	  e << "Invalid pv " << pv << " (" << PVHelper::allPVName(pv)<< ") in GridCell::pvAllSet()";
// 	  throw e;
     }
}

/**
 * \brief Sets a PV:s value (read index) from the index in the eAllPV
 * enumeration.
 *
 * \param pv The index in the eAllPV enumeration.
 * \param f The faction index.
 * \param value The value.
 */
void GridCell::pvAllSetR(eAllPV pv, int f, double value)
{
     mWriteInd = mReadInd;
     pvAllSet(pv, f, value);
     mWriteInd = 1 - mWriteInd;     
}

/**
 * \brief Calculates the sum or mean for the all faction for all non
 * derived pv:s with factions.
 */
void GridCell::recalculateAllFaction()
{
     setSumR(ePopulation);
     setSumR(eDisplaced);
     setSumR(eSheltered);
     setSumR(eProtected);
     setSumR(eInsurgents);
     setPopulationWeightedAverageR(eViolence);
     setPopulationWeightedAverageR(ePerceivedThreat);
}

/**
 * \brief Calculates the sum over the factions for a PV and stores it
 * as the value of the all faction.
 *
 * \param pv The index in the ePVF enumeration.
 */
void GridCell::setSumR(ePVF pv)
{
     double sum = 0;
     for (int f = 1; f < sFactions + 1; ++f) {
	  sum += pvfGet(pv, f);
     }
     pvfSetR(pv, 0, sum);
}

/**
 * \brief Calculates the population weighted average over the factions
 * for a PV and stores it as the value of the all faction.
 *
 * \param pv The index in the ePVF enumeration.
 */
void GridCell::setPopulationWeightedAverageR(ePVF pv)
{
     double sum = 0;
     for (int f = 1; f < sFactions + 1; ++f) {
	  sum += pvfGet(pv, f) * pvfGet(ePopulation, f);
     }
     pvfSetR(pv, 0, sum / pvfGet(ePopulation));
}

void GridCell::doDerived()
{
     doDDisaffection       (&mDerivedF[eDDisaffection * (sFactions + 1)]);

     doDPolarization       (&mDerived[eDPolarization       ]);
//      doDHoused             (&mDerived[eDHoused             ]);
//      doDAvailableFood      (&mDerived[eDAvailableFood      ]);
//      doDFoodDeprivation    (&mDerived[eDFoodDeprivation    ]);
//      doDWaterSurplusDeficit(&mDerived[eDWaterSurplusDeficit]);
//      doDWaterDeprivation   (&mDerived[eDWaterDeprivation   ]);
}


/**
 * \brief Updates the derived attributes for this cell.
 *
 * \param attr The attribute to expose.
 * \param faction The faction to expose.
 * \param size The magnitude of the effect.
 */
void GridCell::expose(eAllPV pv, const EthnicFaction& faction, double size)
{
     if (size == 0) {
	  return;
     }

     static void (GridCell::*exposeFaction[eNumWithFac])(double*, const EthnicFaction&, double);
     static void (GridCell::*exposeNoFaction[eNumNoFac])(double*, double);
     static void (GridCell::*exposeDFaction[eNumWithFac])(double*, const EthnicFaction&, double);

     static bool firstTime = true;
     if (firstTime) {
	  firstTime = false;
	  exposeFaction[ePopulation     ] = 0;
	  exposeFaction[eDisplaced      ] = &GridCell::exposeDisplaced;
	  exposeFaction[eSheltered      ] = &GridCell::exposeSheltered;
	  exposeFaction[eProtected      ] = &GridCell::exposeProtected;
	  exposeFaction[eViolence       ] = &GridCell::exposePercent;
	  exposeFaction[ePerceivedThreat] = &GridCell::exposePercent;
	  exposeFaction[eInsurgents     ] = &GridCell::exposeInsurgents;
	  
	  exposeNoFaction[eFractionNoMedical   ] = &GridCell::exposeFraction;
	  exposeNoFaction[eFractionNoWork      ] = &GridCell::exposeFraction;
	  exposeNoFaction[eEthnicTension       ] = &GridCell::exposeFraction;
	  exposeNoFaction[eFractionCrimeVictims] = &GridCell::exposeFraction;
	  exposeNoFaction[eStoredFood          ] = 0;
	  exposeNoFaction[eFoodConsumption     ] = 0;
	  exposeNoFaction[eFarmStoredFood      ] = 0;
	  exposeNoFaction[eMarketedFood        ] = 0;
	  exposeNoFaction[eFoodDays            ] = &GridCell::exposeFoodDays;
	  exposeNoFaction[eFractionNoFood      ] = 0;
	  exposeNoFaction[eWaterConsumption    ] = 0;
	  exposeNoFaction[eWaterDays           ] = &GridCell::exposeWaterDays;
	  exposeNoFaction[eSuppliedWater       ] = 0;
	  exposeNoFaction[eFractionNoWater     ] = 0;
	  exposeNoFaction[eSusceptible         ] = 0;
	  exposeNoFaction[eInfected            ] = 0;
	  exposeNoFaction[eRecovered           ] = 0;
	  exposeNoFaction[eDeadDueToDisease    ] = 0;
	  exposeNoFaction[eFractionInfected    ] = &GridCell::exposeFractionInfected;
	  exposeNoFaction[eFractionRecovered   ] = &GridCell::exposeFractionRecovered;
	  exposeNoFaction[eInfrastructure      ] = &GridCell::exposeFraction;
	  exposeNoFaction[eDailyDead           ] = 0;
	  exposeNoFaction[eTotalDead           ] = 0;
	  exposeNoFaction[eSmoothedDead        ] = 0;
	  exposeNoFaction[eTEST                ] = 0;

	  exposeDFaction[eDDisaffection] = &GridCell::exposePercent;
     }
     
     mWriteInd = mReadInd;
     if (pv < static_cast<int>(eNumWithFac)) {
	  if (exposeFaction[pv]) {
	       (this->*exposeFaction[pv])(mPVF[mWriteInd][pv], faction, size);
//	       debug("exposing ePVF " << pv << " = "<< PVHelper::pvfName(static_cast<ePVF>(pv)));
	  }
	  else {
	       slog << "Shouldn't expose faction pv " << pv << logEnd;
	  }
     }
     else if (pv < static_cast<int>(eNumWithFac + eNumNoFac)) {
	  int i = pv - eNumWithFac;
	  if (exposeNoFaction[i]) {
	       (this->*exposeNoFaction[i])(&mPV[mWriteInd][i], size);
//	       debug("exposing ePV " << i << " = "<< PVHelper::pvName(static_cast<ePV>(i)));
	  }
	  else {
	       slog << "Shouldn't expose pv " << pv << logEnd;
	  }
     }
     else {
	  int i = pv - eNumWithFac - eNumNoFac;
	  if (exposeDFaction[i]) {
	       (this->*exposeDFaction[i])(&mDerivedF[i * (sFactions + 1)], faction, size);
//	       debug("exposing eDerivedF " << i << " = "<< PVHelper::pdfName(static_cast<eDerivedF>(i)));
	  }
	  else {
	       slog << "Shouldn't expose derived faction pv " << pv << logEnd;
	  }
     }
     mWriteInd = !mReadInd;
}

/**
 * \brief Checks so that the limits of displaced, sheltered, protected
 * etc doesn't exceed the population number and recalculates sums and
 * averages. Implemented in order to handle PVRegion initialization.
 */
void GridCell::adjustValues()
{
     for (int i = 1; i < sFactions + 1; i++) {
	  pvfSetR(eDisplaced , i, min(pvfGet(eDisplaced , i), pvfGet(ePopulation, i)));	  
	  pvfSetR(eSheltered , i, min(pvfGet(eSheltered , i), pvfGet(eDisplaced , i)));
	  pvfSetR(eProtected , i, min(pvfGet(eProtected , i), pvfGet(ePopulation , i) - pvfGet(eDisplaced, i)));
	  pvfSetR(eInsurgents, i, min(pvfGet(eInsurgents, i), pvfGet(ePopulation, i)));
     }   
     recalculateAllFaction();
}

/**
 * \brief Checks for round off errors.
 */
void GridCell::handleRoundOffErrors()
{
     double totPop   = 0;
     double totDisp  = 0;
     double totShelt = 0;
     double totProt  = 0;
     for (int i = 1; i < sFactions + 1; i++) {
	  totPop   += pvfGet(ePopulation, i);
 	  totDisp  += pvfGet(eDisplaced, i);
 	  totShelt += pvfGet(eSheltered, i);
 	  totProt  += pvfGet(eProtected, i);
     }
     ostringstream o;
     // Sum mismatches
     double epsilon = 1e-6;
     if (totPop != pvfGet(ePopulation)) {
	  if (totPop - pvfGet(ePopulation) < epsilon) {
	       pvfSetR(ePopulation, 0, totPop);
	  }
	  else {
	       o << "Population sum mismatch: " << totPop << " != " << pvfGet(ePopulation) << endl;
	  }
     }
     if (totDisp != pvfGet(eDisplaced)) {
	  if (totDisp - pvfGet(eDisplaced) < epsilon) {
	       pvfSetR(eDisplaced, 0, totDisp);
	  }
	  else {
	       o << "Displaced sum mismatch: " << totDisp << " != " << pvfGet(eDisplaced) << endl;
	  }
     }
     if (totShelt != pvfGet(eSheltered)) {
	  if (totShelt - pvfGet(eSheltered) < epsilon) {
	       pvfSetR(eSheltered, 0, totShelt);
	  }
	  else {
	       o << "Sheltered sum mismatch: " << totShelt << " != " << pvfGet(eSheltered) << endl;
	  }
     }
     if (totProt != pvfGet(eProtected)) {
	  if (totProt - pvfGet(eProtected) < epsilon) {
	       pvfSetR(eProtected, 0, totProt);
	  }
	  else {
	       o << "Protected sum mismatch: " << totProt << " != " << pvfGet(eProtected) << endl;
	  }
     }
     // Attribute combination errors
     if (totDisp > totPop) {
	  o << "Displaced > Population: " << totDisp << " > " << totPop << endl;
     }
     if (totDisp + totProt > totPop) {
	  o << "Displaced + Protected > Population: " << totDisp + totProt << " > " << totPop << endl;
     }
     if (totShelt > totDisp) {
	  o << "Sheltered > Displaced: " << totShelt << " > " << totDisp << endl;
     }
     if (pvGet(eFractionInfected) + pvGet(eFractionRecovered) > 1) {
	  // This sum will exceed 1 frequently. According to old Stratmas source
	  // code this seems to be part of the model. The following comment can
	  // be found in DGridCell::DoEpidemics() in the old source code.

			//	Make sure that ill+immune do not exceed the total
			//	(this is absolutely NECESSARY for severe diseases!

	  // So let's just modify the numbers as was done in the old version
	  // and keep quiet...
	  double sum = pvGet(eFractionInfected) + pvGet(eFractionRecovered);
	  pvSetR(eFractionInfected, pvGet(eFractionInfected) / sum);
	  pvSetR(eFractionRecovered, 1 - pvGet(eFractionInfected));
     }
     if (o.str() != "") {
	  debug("Cell: " << mRow << ", " << mCol << endl << o.str());
     }
}

/**
 * \brief For debugging purposes.
 *
 * \param o The stream to write to.
 * \param c The cell to print.
 */
ostream &operator<<(ostream& o, const GridCell& c)
{
     o << "Cell " << c.mRow << ", " << c.mCol << endl;
     for (int i = 0; i < eNumWithFac; i++) {
	  ePVF pv = static_cast<ePVF>(i);
	  o << PVHelper::pvfName(pv) << "(type = " << PVHelper::pvfType(pv) << "):" << endl;
	  for (int f = 0; f < c.sFactions + 1; f++) {
	       o << "   " << c.pvfGet(pv, f) << endl;
	  }
     }
     for (int i = 0; i <  eNumNoFac; i++) {
	  ePV pv = static_cast<ePV>(i);
	  o << left << setw(22) << PVHelper::pvName(pv);
	  o << right << " = " << c.pvGet(pv) << endl;
     }
     return o;
}

