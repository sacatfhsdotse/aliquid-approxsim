// Own
#include "Buffer.h"
#include "CellGroup.h"
#include "DataObject.h"
#include "Error.h"
#include "Grid.h"
#include "GridCell.h"
#include "Map.h"
#include "ModelParameters.h"
#include "Reference.h"
#include "Region.h"
#include "Shape.h"
#include "StratmasConstants.h"
#include "Update.h"

using namespace std;


// Static Definitions
std::map<std::string, eRegionParameter> Region::sNameToIndex;


/**
 * \brief Creates a Region from the provided DataObject.
 *
 * \param d The DataObject to use for construction.
 */
Region::Region(const DataObject& d)
     : SimulationObject(d),
       mChanged(true),
       mPVR(new double[eRNumRegionPV]),
       mParam(new double[eRNumRegionParameters]),
       mAreaRef(&d.getChild("area")->getReference()),
       mArea(0),
       mCellGroup(0),
       mGrid(0)
{     
     static bool firstTime = true;
     if (firstTime) {
          firstTime = false;
          for (int i = 0; i < eRNumRegionParameters; ++i) {
               eRegionParameter mp = static_cast<eRegionParameter>(i);
               sNameToIndex[paramName(mp)] = mp;
          }
     }

     setDefault();
     getDataFromDataObject(d);
     memset(mPVR, 0, eRNumRegionPV * sizeof(double));
}

/**
 * \brief Destructor
 */
Region::~Region()
{
     if (mCellGroup) { delete mCellGroup; }
     if (mParam) { delete [] mParam; }
     if (mPVR) { delete [] mPVR; }
}

void Region::getDataFromDataObject(const DataObject& d)
{
     for (int i = 0; i < eRNumRegionParameters; ++i) {
          eRegionParameter rp = static_cast<eRegionParameter>(i);
          const DataObject* child = d.getChild(paramName(rp));
          if (child) {
               mParam[rp] = child->getDouble();
          }
     }
     mChanged = true;
}

/**
 * \brief Prepares this SimulationObject for simulation.
 *
 * Should be called after creation and reset and before the simulation
 * starts.
 *
 * \param map The map of the simulation.
 */
void Region::prepareForSimulation(const Map& map, Grid& grid, const GridDataHandler& gdh)
{
     mGrid = &grid;

     if (*mAreaRef == map.borders().ref()) {
          mArea = &map.borders();
     }
     else {
          CompositeShape* comp = dynamic_cast<CompositeShape*>(&map.borders());
          if (comp) {
               mArea = comp->getPart(*mAreaRef);
          }
     }
     if (!mArea) {
          Error e;
          e << "Couldn't map the reference '" << *mAreaRef << "' to any region in the map";
          throw e;
     }

     if (mCellGroup) {
          delete mCellGroup;
     }
     mCellGroup = new CellGroup(gdh);

     list<GridPos> cellList;
     mArea->cells(grid, cellList);
     for (list<GridPos>::iterator it = cellList.begin(); it != cellList.end(); ++it) {
          GridCell* c = grid.cell(*it);
          mCellGroup->addMember(c);
          c->addOverlappingRegion(*this);
     }
}

/**
 * \brief 
 */
void Region::update()
{
     if (mChanged) {
          mCellGroup->updateWeights();
          mChanged = false;
          double numCells = 0;
          for (std::vector<std::pair<double, const GridCell*> >::const_iterator it = mCellGroup->members().begin();
               it != mCellGroup->members().end(); ++it) {
               numCells += it->first;
          }
          double cellProd = GridCell::areaKm2() * rp(eRFractionArableLand) * mGrid->mp().mp(eFoodProductionPerKm2);
          pvrSet(eRFoodProduction, numCells * cellProd);
     }
     mCellGroup->update();

     double myShare = mCellGroup->pvfGet(ePopulation) / mGrid->totalPopulation();
     double importFromAbroad = mGrid->mp().mp(eFoodImportFromAbroad) * myShare;
     double consumption = mCellGroup->pvfGet(ePopulation) * kFoodPPPDKg / 1000.0;
     pvrSet(eRFoodSurplusDeficit, pvrGet(eRFoodProduction) - consumption + importFromAbroad);
}

/**
 * \brief Sets values to default.
 */
void Region::setDefault()
{
     mParam[eRDailyRainfallMeters] = 0.35 / 365.0;
     mParam[eRFractionArableLand ] = 0.10;
     mChanged = true;
}

/**
 * \brief Updates this object.
 *
 * \param u The Update to update this object with.
 */
void Region::update(const Update& u)
{
     const string& attr = u.getReference().name();
     if (u.getType() == Update::eModify) {
          std::map<string, eRegionParameter>::iterator it = sNameToIndex.find(attr);
          if (it != sNameToIndex.end()) {
               mParam[it->second] = u.getObject()->getDouble();
          }
          else {
               Error e;
               e << "No updatable attribute '" << attr << "' in '" << ref() << "'";
               throw e;
          }
     }
     else {
          Error e;
          e << "Invalid Region Update (type:" << u.getTypeAsString();
          e << ", object: " << attr << ").";
          throw e;
     }
     mChanged = true;
}

/**
 * \brief Extracts data from this object to the Buffer.
 *
 * \param b The Buffer to extract data to.
 */
void Region::extract(Buffer &b) const
{
     DataObject& me = *b.map(ref());
     for (int i = 0; i < eRNumRegionParameters; ++i) {
          eRegionParameter rp = static_cast<eRegionParameter>(i);
          DataObject* c = me.getChild(paramName(rp));
          if (c) {
               c->setDouble(mParam[rp]);
          }
//          me.getChild(paramName(mp))->setDouble(mParam[mp]);
     }
}

/**
 * \brief Resets this object to the state it would have had if it was
 * created from the provided DataObject.
 *
 * \param d The DataObject to use as source for the reset.
 */
void Region::reset(const DataObject& d)
{
     getDataFromDataObject(d);
}

