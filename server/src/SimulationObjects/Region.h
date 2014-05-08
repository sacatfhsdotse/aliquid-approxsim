#ifndef APPROXSIM_REGION_H
#define APPROXSIM_REGION_H


// System
#include <map>
#include <string>

// Own
#include "SimulationObject.h"

// Forward declarations
class CellGroup;
class DataObject;
class Grid;
class GridDataHandler;
class Map;
class Shape;


enum eRegionParameter {
     eRDailyRainfallMeters,
     eRFractionArableLand ,
     eRNumRegionParameters
};

static const char* regionParameterName[] = {
     "dailyRainfallMeters",
     "fractionArableLand" ,
};

enum eRegionPV {
     eRFoodProduction    ,
     eRFoodSurplusDeficit,
     eRNumRegionPV
};


/**
 * \brief This is the SimulationObject that corresponds to the Region
 * type in the Approxsim xml schema.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/10/02 16:05:14 $
 */
class Region : public SimulationObject {
private:
     static std::map<std::string, eRegionParameter> sNameToIndex;

     bool mChanged;
     double* mPVR;
     double* mParam;
     const Reference* mAreaRef;
     const Shape* mArea;

     CellGroup* mCellGroup;
     Grid* mGrid;

     void setDefault();
     void getDataFromDataObject(const DataObject& d);

public:
     Region(const DataObject& d);
     virtual ~Region();

     /**
      * \brief Accessor for the area.
      *
      * \return The area.
      */
     const Shape& area() const { return *mArea; }

     const CellGroup& cellGroup() const { return *mCellGroup; }

     void prepareForSimulation(const Map& map, Grid& grid, const GridDataHandler& gdh);
     void update();

     void update(const Update& u);
     void extract(Buffer &b) const;
     void reset(const DataObject& d);

     double pvrGet(eRegionPV pvr) const { return mPVR[pvr]; }
     void pvrSet(eRegionPV pvr, double value) const { mPVR[pvr] = value; }

     double rp(eRegionParameter param) const { return mParam[param]; }
     static const char* paramName(eRegionParameter param) { return regionParameterName[param]; }
};

#endif   // APPROXSIM_REGION_H
