// Own
#include "CombatGrid.h"
#include "debugheader.h"
#include "Error.h"
#include "Faction.h"
#include "Grid.h"
#include "GridCell.h"
#include "GridDataHandler.h"
#include "ProcessVariables.h"
// New
//#include "ProcessVariables.h"
#include "PVInfo.h"
#include "Reference.h"

using namespace std;


/**
 * \brief Creates a GridDataHandler for the provided grid and combat
 * grid.
 *
 * \param grid The Grid.
 * \param cg The CombatGrid.
 */
GridDataHandler::GridDataHandler(Grid& grid, CombatGrid& cg, const vector<Faction*>& facVec)
     : mFactions(facVec), mGridData(0), mGrid(grid), mCG(cg)
{
     mNumActive = mGrid.active();

     // Calculate the number of layers
     mNumLayers = (eNumWithFac + eDNumDerivedF) * (mGrid.factions() + 1) + eNumNoFac + eDNumDerived + mCG.layers();
     if (kShowPrecalculated) {
	  mNumLayers += ePNumPreCalcF * (mGrid.factions() + 1) + ePNumPreCalc;
     }
     mStanceStartIndex = mNumLayers;
//     int numStanceLayers = mGrid.factions() * (mFactions.size() - 1);
//     mNumLayers += numStanceLayers;

     // PV:s with factions.
     for (int i = 0; i < eNumWithFac; ++i) {
 	  mLayerNameToIndex[PVHelper::pvfName(static_cast<ePVF>(i))] = i * (mGrid.factions() + 1);
     }
     int base = (mGrid.factions() + 1) * eNumWithFac;
     // PV:s without factions.
     for (int i = 0; i < eNumNoFac; ++i) {
 	  mLayerNameToIndex[PVHelper::pvName(static_cast<ePV>(i))] = base + i;
     }
     base += eNumNoFac;

     // Derived PV:s with factions.
     for (int i = 0; i < eDNumDerivedF; ++i) {
 	  mLayerNameToIndex[PVHelper::pdfName(static_cast<eDerivedF>(i))] = base + i * (mGrid.factions() + 1);
     }
     base += (mGrid.factions() + 1) * eDNumDerivedF;
     // Derived PV:s without factions.
     for (int i = 0; i < eDNumDerived; ++i) {
 	  mLayerNameToIndex[PVHelper::pdName(static_cast<eDerived>(i))] = base + i;
     }
     base += eDNumDerived;

     if (kShowPrecalculated) {
	  // Precalculated PV:s with factions.
	  for (int i = 0; i < ePNumPreCalcF; ++i) {
	       mLayerNameToIndex[PVHelper::pcfName(static_cast<ePreCalcF>(i))] = base + i * (mGrid.factions() + 1);
	  }
	  base += (mGrid.factions() + 1) * ePNumPreCalcF;
	  // Precalculated PV:s without factions.
	  for (int i = 0; i < ePNumPreCalc; ++i) {
	       mLayerNameToIndex[PVHelper::pcName(static_cast<ePreCalc>(i))] = base + i;
	  }
	  base += ePNumPreCalc;
     }

     // CombatGrid.
     for (std::map<string, int>::const_iterator it = mCG.nameToIndexMap().begin();
	  it != mCG.nameToIndexMap().end(); it++) {
  	  mLayerNameToIndex[it->first] = base + it->second;
     }
     base += mCG.layers();

     // Stance layers
//      for (int i = 1; i < mGrid.factions() + 1; ++i) {
// 	  int numMilFac = 0;
// 	  int count = 0;
// 	  for (vector<Faction*>::const_iterator it = mFactions.begin(); it != mFactions.end(); ++it) {
// 	       EthnicFaction* fac = dynamic_cast<EthnicFaction*>(*it);
// 	       int j = (fac ? fac->index() : mGrid.factions() + 1 + numMilFac++);
// 	       if (j != i ) {
// 		    string layerName = EthnicFaction::faction(i)->ref().name()
// 			 + " hostility toward " + (*it)->ref().name();
// 		    mStanceLayerName.push_back(layerName);
// 		    mLayerNameToIndex[layerName] = base + (i - 1) * (mFactions.size() - 1) + count;
// 		    PVInfo::addPV(layerName, "Stance", false, "0", "100");
// 		    ++count;
// 	       }
// 	  }
//      }

//       for (std::map<std::string, int>::iterator it = mLayerNameToIndex.begin(); it != mLayerNameToIndex.end(); it++) {
//        	  debug(it->first << " - " << it->second);
//       }

     // Allocate memory for new grid
     // Cell after cell
     mGridData = new double*[mNumActive];
     for (int i = 0; i < mNumActive; i++) {
	  mGridData[i] = new double[mNumLayers];
	  memset(mGridData[i], 0, mNumLayers * sizeof(double));
     }
}


/**
 * \brief Destructor
 */
GridDataHandler::~GridDataHandler()
{
     if (mGridData) {
	  for (int i = 0; i < mNumActive; ++i) {
	       if (mGridData[i]) {
		    delete [] mGridData[i];
	       }
	  }
	  delete [] mGridData;
     }
     mStanceLayerName.clear();
}

/**
 * \brief Fetches process variable values for the specified cells,
 * process variable and faction.
 *
 * \param lay The process variable number according to the eAttribute
 * enumeration.
 * \param fac The faction index.
 * \param size The number of cells to fetch values for.
 * \param index An array of size elements containing the indices in
 * the active cells array of the cells for which to fetch the values.
 * \param outData An array of size elements that on return will
 * contain the values for the specified cells.
 */
void GridDataHandler::layer(int lay, int fac, int size, int32_t* index, double*& outData)
{
     for (int i = 0; i < size; ++i) {
	  outData[i] = (index ? mGridData[index[i]][lay + fac] : mGridData[i][lay + fac]);
     }
}

/**
 * \brief Fetches process variable values for the specified cells,
 * process variable and faction.
 *
 * \param lay The name of the process variable.
 * \param fac A Reference to the faction.
 * \param size The number of cells to fetch values for.
 * \param index An array of size elements containing the indices in
 * the active cells array of the cells for which to fetch the values.
 * \param outData An array of size elements that on return will
 * contain the values for the specified cells.
 */
void GridDataHandler::layer(const std::string& lay,
			    const Reference& fac,
			    int size,
			    int32_t* index,
			    double*& outData)
{
     EthnicFaction* facp = EthnicFaction::faction(fac);
     std::map<std::string, int>::iterator it = mLayerNameToIndex.find(lay);
     
     // Check if the layer and faction asked for are is valid
     if(it == mLayerNameToIndex.end()) {
	  Error e(Error::eWarning);
	  e << "In GridDataHandler::layer() - Tried to extract data for unknown layer '" << lay << "'";
	  throw e;
     }
     else if (!facp) {
	  Error e(Error::eWarning);
	  e << "In GridDataHandler::layer() - Tried to extract data for unknown faction '" << fac << "'";
	  throw e;
     }
     else {
	  layer(it->second, facp->index(), size, index, outData);
     }
}

/**
 * \brief Copies data from the simulation Grid to this object so that
 * it will be accessible for clients.
 *
 * Called by the Engine via Buffer when simulation data should be
 * transfered from the simulation to the Buffer, for example after
 * each timestep and after initialization.
 */
void GridDataHandler::extractGridData() {
     int nFac = mGrid.factions();
     int base;
     int tmpbase;

     // PV values
     // Parallell - All these for loops should be parallellizable.
     for (int i = 0; i < mGrid.active(); ++i) {
	  GridCell &c = *mGrid.cell(i);
	  // PV:s with factions.
	  for (int j = 0; j < eNumWithFac; ++j) {
	       tmpbase = j * (nFac + 1);
	       for (int k = 0; k < nFac + 1; ++k) {
		    mGridData[i][tmpbase + k] = c.pvfGet(static_cast<ePVF>(j), k);
	       }
	  }
	  base = eNumWithFac * (nFac + 1);
	  // PV:s without factions.
	  for (int j = 0; j < eNumNoFac; ++j) {
	       mGridData[i][base + j] = c.pvGet(static_cast<ePV>(j));
	  }
	  base += eNumNoFac;

	  // Derived PV:s with factions.
	  for (int j = 0; j < eDNumDerivedF; ++j) {
	       tmpbase = base + j * (nFac + 1);
	       for (int k = 0; k < nFac + 1; ++k) {
		    mGridData[i][tmpbase + k] = c.pdfGet(static_cast<eDerivedF>(j), k);
	       }
	  }
	  base += eDNumDerivedF * (nFac + 1);
	  // Derived PV:s without factions.
	  for (int j = 0; j < eDNumDerived; ++j) {
	       mGridData[i][base + j] = c.pdGet(static_cast<eDerived>(j));
	  }
	  base += eDNumDerived;

	  if (kShowPrecalculated) {
	       // Precalculated PV:s with factions.
	       for (int j = 0; j < ePNumPreCalcF; ++j) {
		    tmpbase = base + j * (nFac + 1);
		    for (int k = 0; k < nFac + 1; ++k) {
			 mGridData[i][tmpbase + k] = c.pcfGet(static_cast<ePreCalcF>(j), k);
		    }
	       }
	       base += ePNumPreCalcF * (nFac + 1);
	       // PreCalculated PV:s without factions.
	       for (int j = 0; j < ePNumPreCalc; ++j) {
		    mGridData[i][base + j] = c.pcGet(static_cast<ePreCalc>(j));
	       }
	       base += ePNumPreCalc;
	  }

	  // CombatGrid.
	  for (int j = 0; j < mCG.layers(); ++j) {
	       mGridData[i][base + j] = mCG.value(i, j);
	  }
	  base += mCG.layers();
     }

     // Stance layers
//      for (int i = 1; i < mGrid.factions() + 1; ++i) {
// 	  int numMilFac = 0;
// 	  int count = 0;
// 	  for (vector<Faction*>::const_iterator it = mFactions.begin(); it != mFactions.end(); ++it) {
// 	       EthnicFaction* fac = dynamic_cast<EthnicFaction*>(*it);
// 	       int j = (fac ? fac->index() : mGrid.factions() + 1 + numMilFac++);
// 	       if (j != i ) {
// 		    int index = base + (i - 1) * (mFactions.size() - 1) + count;
// 		    double stance = EthnicFaction::faction(i)->stance(**it);
// 		    for (int k = 0; k < mGrid.active(); ++k) {
// 			 mGridData[k][index] = mGrid.cell(k)->pdfGet(eDDisaffection, i) * stance;
// 		    }
// 		    ++count;
// 	       }
// 	  }
//      }
}
