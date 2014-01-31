#ifndef STRATMAS_COMBAT_GRID_H
#define STRATMAS_COMBAT_GRID_H


// System
#include <iosfwd>
#include <list>
#include <map>
#include <set>
#include <string>
#include <vector>

// Own
#include "BasicGrid.h"
#include "GridPos.h"
#include "PresenceObject.h"
#include "PresenceObjectAllocator.h"
#include "SOFactoryListener.h"

// Forward Declarations
class Faction;
class Grid;
class Shape;
class Unit;


/**
 * \brief This class controlls most of the grid related combat
 * activities such as finding out which units that overlaps etc.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/09/04 14:34:39 $
 */
class CombatGrid : public BasicGrid, SOFactoryListener {
private:
     /// Maps the name of a layer to its index.
     std::map<std::string, int> mNameToIndex;

     /// Maps the index of a layer to its name.
     std::vector<std::string> mIndexToName;

     /**
      * \brief An array that contains a bool value for each layer
      * indicating whether or not the layer should be reset between
      * two consecutive timesteps.
      */
     bool *mResetableLayer;

     /// Reference to the Grid.
     Grid& mGrid;

     /// The number of layers in the CombatGrid.
     int mNumLayers;
     
     /// Index of the layer storing the sum of all units' personnel.
     int mSumLayerIndex;

     /// Index of the layer storing the sum of all units' personnel.
     int mCasualtySumLayerIndex;

     /**
      * \brief The index of the layer storing the sum of all blue
      * forces' personnel. A blue force is a force that has 'F' as the
      * second letter in its symbol id code.
      */
     int mBlueLayerIndex;

     /// Index of the layer storing the number of insurgent casualties.
     int mInsurgentLayerIndex;

     /// A reference to the scenario's force vector.
     std::vector<Unit*>& mForces;

     /// Two dimensional array storing the actual values in the grid.
     double** mGridData;

     /**
      * \brief The set of PresenceObjects marking the presence of all
      * units during each timestep.
      */
     std::set<PresenceObject*, lessPresenceObjectPointer> mPresence;

     /// Memory handler for PresenceObjects.
     PresenceObjectAllocator mPOA;

     void reset(bool includeNonResetables = false);
     void unitToGrid(Unit& u);
     void markPresence(Unit& u);
     void markPresence();
     
public:
     static const char* kCasualtyStr;

     CombatGrid(Map& amap, Grid& grid, std::vector<Unit*>& forceVec, const std::vector<Faction*>& facVec);
     ~CombatGrid();

     void objectAdded(const Reference& ref, int64_t initiator);
     void objectRemoved(const Reference& ref, int64_t initiator);

     /**
      * \brief Accessor for the number of layers.
      *
      * \return The number of layers.
      */
     int layers() const { return mNumLayers; }

     /**
      * \brief Accessor for the blue layer index.
      *
      * \return The blue layer index.
      */
     int blueLayer() const { return mBlueLayerIndex; }

     /**
      * \brief Accessor for the casualty sum layer index.
      *
      * \return The casualty sum layer index.
      */
     int casualtySumLayer() const { return mCasualtySumLayerIndex; }

     /**
      * \brief Gets the value for the specified cell and layer.
      *
      * \param actInd The index of the cell in the active array.
      * \param layer The index of the layer.
      * \return The value for the specified cell and layer.
      */
     double value(int actInd, int layer) const { return mGridData[layer][actInd]; }

     /**
      * \brief Gets the value for the specified cell and layer.
      *
      * \param actInd The index of the cell in the active array.
      * \param layer The name of the layer.
      * \return The value for the specified cell and layer.
      */
     double value(int actInd, const std::string& layer) const { return mGridData[nameToIndex(layer)][actInd]; }

     /**
      * \brief Gets the value for the specified cell and layer.
      *
      * \param gp The GridPos marking the cell.
      * \param layer The index of the layer.
      * \return The value for the specified cell and layer.
      */
     double value(GridPos gp, int layer) const { return mGridData[layer][posToActive(gp.r, gp.c)]; }

     /**
      * \brief Adds a value to the specified cell and layer.
      *
      * \param actInd The index of the cell in the active array.
      * \param layer The index of the layer.
      * \param val The value to add.
      */
     void add(int actInd, int layer, double val) const { mGridData[layer][actInd] += val; }

     double* aggregate(const std::list<GridPos>& pos, double*& outAgg) const;
     double* aggregate(const Shape& region, double*& outAgg) const;

     void reset(std::vector<Unit*>& forceVec);
     void unitsToGrid();
     void setUpBattleField();
     void registerCombat();
     
     /**
      * \brief Accessor for the mNameToIndex map.
      *
      * \return The mNameToIndex map.
      */
     const std::map<std::string, int>& nameToIndexMap() const { return mNameToIndex; }

     /**
      * \brief Maps the name of a layer to its index.
      *
      * \param i The layer index.
      * \return The name of the specified layer.
      */
     std::string indexToName(int i) const { return mIndexToName[i]; }

     /**
      * \brief Maps the index of a layer to its name.
      *
      * \param name The layer name.
      * \return The index of the specified layer.
      */
     int nameToIndex(const std::string& name) const {
	  std::map<std::string, int>::const_iterator it = mNameToIndex.find(name);
	  return (it == mNameToIndex.end() ? -1 : it->second);
     }

     friend std::ostream& operator << (std::ostream& o, const CombatGrid& c);
};


#endif   // STRATMAS_COMBAT_GRID_H
