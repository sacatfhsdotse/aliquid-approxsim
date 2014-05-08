#ifndef APPROXSIM_MODELPARAMETERS_H
#define APPROXSIM_MODELPARAMETERS_H


// System
#include <map>
#include <string>

// Own
#include "SimulationObject.h"

// Forward declarations
class DataObject;


enum eModelParameter {
     // Military
     eCivilianToMilitaryCasualtyRate  ,
     // Insurgents
     eFractionPotentialInsurgents     ,
     eInsurgentDisaffectionThreshold  ,
     eInsurgentGenerationCoefficient  ,
     eInsurgentStrengthFactor         ,
    // Employment and Infrastructure model
     eWorkforceFraction               ,
     eFractionNewHired                ,
     eEmployerRiskTakingFactor        ,
     eInfrastructureDecay             ,
     eFractionInfrastructureWorkers   ,
     // Potable water model
     eFractionRainfallCollected       ,
     eFractionCollectedPurified       ,
     // Food Model
     eFoodProductionPerKm2            ,
     eFoodImportFromAbroad            ,
     // Housing Model
     eMeanFamilySize                  ,
     eConstructionWorkforceFraction   ,
     eHousesDestroyedPerCasualty      ,
     // Disease and Medical Care
     eFractionSusceptible             ,
     eFractionCivCasualtiesSeekingCare,
     eNumModelParameters              
};

static const char* modelParameterName[] = {
     // Military
     "CivilianToMilitaryCasualtyRate"  ,
     // Insurgents
     "fractionPotentialInsurgents"     ,
     "insurgentDisaffectionThreshold"  ,
     "insurgentGenerationCoefficient"  ,
     "insurgentStrengthFactor"         ,
     // Employment and Infrastructure model
     "workforceFraction"               ,
     "fractionNewHired"                ,
     "employerRiskTakingFactor"        ,
     "infrastructureDecay"             ,
     "fractionInfrastructureWorkers"   ,
     // Potable water model
     "fractionRainfallCollected"       ,
     "fractionCollectedPurified"       ,
     // Food Model
     "foodProductionPerKm2"            ,
     "foodImportFromAbroad"            ,
     // Housing Model
     "meanFamilySize"                  ,
     "constructionWorkforceFraction"   ,
     "housesDestroyedPerCasualty"      ,
     // Disease and Medical Care
     "fractionSusceptible"             ,
     "fractionCivCasualtiesSeekingCare"
};

/**
 * \brief The SimulationObject that corresponds to the ModelParameters
 * type in the Approxsim xml schemas.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/09/04 14:34:42 $
 */
class ModelParameters : public SimulationObject {
private:
     static std::map<std::string, eModelParameter> sNameToIndex;
     double* mParam;

     void getDataFromDataObject(const DataObject& d);

public:
     ModelParameters(const DataObject& d);
     ~ModelParameters();

     void setDefault();
     
     void update(const Update& u);
     void extract(Buffer &b) const;
     void reset(const DataObject& d);

     double mp(eModelParameter param) const { return mParam[param]; }
     static const char* paramName(eModelParameter param) { return modelParameterName[param]; }
};

#endif   // APPROXSIM_MODELPARAMETERS_H
