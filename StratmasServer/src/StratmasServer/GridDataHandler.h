#ifndef STRATMAS_GRIDDATAHANDLER_H
#define STRATMAS_GRIDDATAHANDLER_H


// System
#include <map>
#include <string>
#include <vector>

// Own
#include "stdint.h"

// Static Definitions
class CombatGrid;
class Faction;
class Grid;
class Reference;


/**
 * \brief Helper object that provides an interface for accessing data
 * from the grid based on layer name, i.e. the name of the process
 * variable.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/10/10 09:36:51 $
 */
class GridDataHandler {
private:
     /// Maps layer name to its index in the mGridData
     std::map<std::string, int> mLayerNameToIndex;

     /// Reference to the Scenario's faction vector.
     const std::vector<Faction*>& mFactions;

     /**
      * \brief Array for the values of all process variables in all cells.
      *
      * The structure is as follows:
      * <p>     mGridData[number of active cells][number of layers] <p>
      * where cells are ordered from top left to bottom right.
      */
     double** mGridData;

     /// Reference to the Grid to handle data from.
     Grid& mGrid;

     /// Reference to the CombatGrid to handle data from.
     CombatGrid& mCG;

     int mNumActive;    ///< Number of active cells.
     int mNumLayers;    ///< Number of layers.

     /// The index in the mGridData[][] for the first stance layer.
     int mStanceStartIndex;

     // Names of the stance layers.
     std::vector<std::string> mStanceLayerName;

     void layer(int lay, int fac, int size, int32_t* index, double*& outLayer);

public:
     GridDataHandler(Grid& grid, CombatGrid& cg, const std::vector<Faction*>& facVec);
     ~GridDataHandler();

     /**
      * \brief Accessor for the Grid.
      *
      * \return A Reference to the Grid.
      */
     const Grid& grid() const { return mGrid; }

     /**
      * \brief Accessor for the CombatGrid.
      *
      * \return A Reference to the CombatGrid.
      */
     const CombatGrid& combatGrid() const { return mCG; }

     /**
      * \brief Maps the stance layer index to the layer name.
      *
      * \param i The index among the stance layers
      * \return The name of the layer.
      */
     const std::string& stanceLayerName(int i) const { return mStanceLayerName[i]; }

     /**
      * \brief Returns the number of stance layers.
      *
      * \return The number of stance layers.
      */
     int stanceLayers() const { return mNumLayers - mStanceStartIndex; }

     /**
      * \brief Gets the value of a stance variable for the specified
      * cell.
      *
      * \param cellIndex The index (in the active array) of the cell.
      * \param stanceIndex The index among the stance layers.
      * \return The value of a stance variable for the specified cell.
      */
     double ps(int cellIndex, int stanceIndex) const { return mGridData[cellIndex][mStanceStartIndex + stanceIndex]; }

     void layer(const std::string& lay, const Reference& fac, int size, int32_t* index, double*& outData);
     void extractGridData();
};

#endif   // STRATMAS_GRIDDATAHANDLER_H
