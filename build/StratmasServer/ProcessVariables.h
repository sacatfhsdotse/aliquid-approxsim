#ifndef STRATMAS_PROCESSVARIABLES_H
#define STRATMAS_PROCESSVARIABLES_H


// System
#include <map>
#include <string>


const bool kShowPrecalculated = false;

/**
 * \brief Enumeration for process variables with factions.
 */
enum ePVF {
     ePopulation     ,
     eDisplaced      ,
     eSheltered      ,
     eProtected      ,
     eViolence       ,
     ePerceivedThreat,
     eInsurgents     ,
     eNumWithFac
};

/**
 * \brief Process variable name array sorted in the same order as the
 * ePVF enumeration.
 */
static const char* pvfName[] = {
     "Population"      ,
     "Displaced"       ,
     "Sheltered"       ,
     "Protected"       ,
     "Violence"        ,
     "Perceived Threat",
     "Insurgents"
};

/**
 * \brief Process variable type array sorted in the same order as the
 * ePVF enumeration.
 */
static const char* pvfType[] = {
     "sp:Positive",     // population
     "sp:Positive",     // displaced
     "sp:Positive",     // sheltered
     "sp:Positive",     // protected
     "sp:Percent" ,     // violence
     "sp:Percent" ,     // perceivedThreat
     "sp:Positive"      // insurgents
};

// ----------------------------------------------------------------------------

/**
 * \brief Enumeration for process variables without factions.
 */
enum ePV {
     eFractionNoMedical   ,
     eFractionNoWork      ,
     eEthnicTension       ,
     eFractionCrimeVictims,
     eHousingUnits        ,
     eStoredFood          ,
     eFoodConsumption     ,
     eFarmStoredFood      ,
     eMarketedFood        ,
     eFoodDays            ,
     eFractionNoFood      ,
     eWaterConsumption    ,
     eWaterDays           ,
     eSuppliedWater       ,
     eFractionNoWater     ,
     eSusceptible         ,
     eInfected            ,
     eRecovered           ,
     eDeadDueToDisease    ,
     eFractionInfected    ,
     eFractionRecovered   ,
     eInfrastructure      ,
     eDailyDead           ,     
     eTotalDead           ,     
     eSmoothedDead        ,
     eTEST                ,
     eNumNoFac
};

/**
 * \brief Process variable name array sorted in the same order as the
 * ePV enumeration.
 */
static const char* pvName[] = {
     "Fraction No Medical"   ,
     "Fraction No Work"      ,
     "Ethnic Tension"        ,
     "Fraction Crime Victims",
     "Housing Units"         ,
     "Stored Food"           ,
     "Food Consumption"      ,
     "Farm Stored Food"      ,
     "Marketed Food"         ,
     "Food Days"             ,
     "Fraction No Food"      ,
     "Water Consumption"     ,
     "Water Days"            ,
     "Supplied Water"        ,
     "Fraction No Water"     ,
     "Susceptible"           ,
     "Infected"              ,
     "Recovered"             ,
     "Dead Due To Disease"   ,
     "Fraction Infected"     ,
     "Fraction Recovered"    ,
     "Infrastructure"        ,
     "Daily Dead"            ,
     "Total Dead"            ,
     "Smoothed Dead"         ,
     "TEST"             
};

/**
 * \brief Process variable type array sorted in the same order as the
 * ePV enumeration.
 */
static const char* pvType[] = {
     "sp:Fraction",     // fractionNoMedical
     "sp:Fraction",     // fractionNoWork
     "sp:Fraction",     // ethnicTension
     "sp:Fraction",     // fractionCrimeVictims
     "sp:Positive",     // housingUnits
     "sp:Positive",     // storedFood
     "sp:Positive",     // foodConsumption
     "sp:Positive",     // farmStoredFood
     "sp:Positive",     // marketedFood
     "sp:Positive",     // foodDays
     "sp:Fraction",     // fractionNoFood
     "sp:Positive",     // waterConsumption
     "sp:Positive",     // waterDays
     "sp:Positive",     // suppliedWater
     "sp:Fraction",     // fractionNoWater
     "sp:Positive",     // susceptible     
     "sp:Positive",     // infected        
     "sp:Positive",     // recovered       
     "sp:Positive",     // deadDueToDisease
     "sp:Fraction",     // fractionInfected
     "sp:Fraction",     // fractionRecovered
     "sp:Fraction",     // infrastructure
     "sp:Positive",     // dailyDead
     "sp:Positive",     // totalDead
     "sp:Positive",     // smoothedDead
     "xsd:double"       // TEST
};

//=============================================================================

/**
 * \brief Enumeration for derived process variables with factions.
 */
enum eDerivedF {
     eDDisaffection,
     eDNumDerivedF
};


/**
 * \brief Derived process variable name array sorted in the same order
 * as the eDerivedF enumeration.
 */
static const char* pdfName[] = {
     "Disaffection"
};

/**
 * \brief Process variable type array sorted in the same order as the
 * eDerivedF enumeration.
 */
static const char* pdfType[] = {
     "sp:Percent"       // disaffection
};

// ----------------------------------------------------------------------------

/**
 * \brief Enumeration for derived process variables without factions.
 */
enum eDerived {
     eDPolarization       ,
     eDHoused             ,
     eDAvailableFood      ,
     eDFoodDeprivation    ,
     eDWaterSurplusDeficit,
     eDWaterDeprivation   ,
     eDNumDerived
};

/**
 * \brief Derived process variable name array sorted in the same order
 * as the eDerived enumeration.
 */
static const char* pdName[] = {
     "Polarization"         ,
     "Housed"               ,
     "Available Food"       ,
     "Food Deprivation"     ,
     "Water Surplus/Deficit",
     "Water Deprivation"
};

/**
 * \brief Process variable type array sorted in the same order as the
 * ePVF enumeration.
 */
static const char* pdType[] = {
     "sp:Percent" ,     // polarization
     "sp:Positive",     // housed
     "sp:Positive",     // availableFood
     "sp:Fraction",     // foodDeprivation
     "xsd:double" ,     // waterSurplusDeficit
     "sp:Fraction"      // waterDeprivation
};

//=============================================================================

/**
 * \brief Enumeration for precalculated process variables with
 * factions.
 */
enum ePreCalcF {
     ePAtHome                 ,
     ePSheltered              ,
     ePUnsheltered            ,
     // Epidemics
     ePHomeIll                ,
     ePShelteredIll           ,
     ePUnshelteredIll         ,	                  
     ePHomeImmune             ,
     ePShelteredImmune        ,
     ePUnshelteredImmune      ,	                  
     ePHomeDead               ,
     ePShelteredDead          ,
     ePUnshelteredDead        ,
     // Violence related deaths and displaced
     ePIDPDeadDueToViolence   ,
     ePHousedDeadDueToViolence,
     ePNewIDPDueToViolence    ,
     // Refugee migration towards camps
     ePTowardsCampDelta       ,
     // Diffusion
     ePDiffusionDelta         ,
     ePDiffusionDisplacedDelta,
     ePNumPreCalcF
};

/**
 * \brief Process variable name array sorted in the same order as the
 * ePreCalcF enumeration.
 */
static const char* pcfName[] = {
     "At Home"                    ,
     "Sheltered"                  ,
     "Unsheltered"                ,
     "Home Ill"                   ,
     "Sheltered Ill"              ,
     "Unsheltered Ill"            ,	                  
     "Home Immune"                ,
     "Sheltered Immune"           ,
     "Unsheltered Immune"         ,	                  
     "Home Dead"                  ,
     "Sheltered Dead"             ,
     "Unsheltered Dead"           ,
     "IDP Dead Due To Violence"   ,
     "Housed Dead Due To Violence",
     "New IDP Due To Violence"    ,
     "Towards Camp Delta"         ,
     "Diffusion Delta"            ,
     "Diffusion Displaced Delta"
};

/**
 * \brief Process variable type array sorted in the same order as the
 * ePreCalcF enumeration.
 */
static const char* pcfType[] = {
     "sp:Positive",     // AtHome                 
     "sp:Positive",     // Sheltered              
     "sp:Positive",     // Unsheltered            
     "sp:Positive",     // HomeIll                
     "sp:Positive",     // ShelteredIll           
     "sp:Positive",     // UnshelteredIll         
     "sp:Positive",     // HomeImmune             
     "sp:Positive",     // ShelteredImmune        
     "sp:Positive",     // UnshelteredImmune      
     "sp:Positive",     // HomeDead               
     "sp:Positive",     // ShelteredDead          
     "sp:Positive",     // UnshelteredDead        
     "sp:Positive",     // IDPDeadDueToViolence   
     "sp:Positive",     // HousedDeadDueToViolence
     "sp:Positive",     // NewIDPDueToViolence    
     "sp:Positive",     // TowardsCampDelta       
     "sp:Positive",     // DiffusionDelta         
     "sp:Positive"	// DiffusionDisplacedDelta
};

// ----------------------------------------------------------------------------

/**
 * \brief Enumeration for precalculated process variables without
 * factions.
 */
enum ePreCalc {
     ePFoodProduction ,
     ePWaterProduction,
     ePNumPreCalc
};

/**
 * \brief Process variable name array sorted in the same order as the
 * ePreCalc enumeration.
 */
static const char* pcName[] = {
     "Food Production" ,
     "Water Production"
};

/**
 * \brief Process variable type array sorted in the same order as the
 * ePreCalc enumeration.
 */
static const char* pcType[] = {
     "sp:Positive" ,
     "sp:Positive"
};

//=============================================================================

/**
 * \brief Enumeration for all process variables.
 */
enum eAllPV {
     eAllPopulation          ,   // Factions
     eAllDisplaced           ,   // Factions
     eAllSheltered           ,   // Factions
     eAllProtected           ,   // Factions
     eAllViolence            ,   // Factions
     eAllPerceivedThreat     ,	 // Factions
     eAllInsurgents          ,   // Factions
     eAllFractionNoMedical   ,
     eAllFractionNoWork      ,
     eAllEthnicTension       ,
     eAllFractionCrimeVictims,
     eAllHousingUnits        ,
     eAllStoredFood          ,
     eAllFoodConsumption     ,
     eAllFarmStoredFood      ,
     eAllMarketedFood        ,
     eAllFoodDays            ,
     eAllFractionNoFood      ,
     eAllWaterConsumption    ,
     eAllWaterDays           ,
     eAllSuppliedWater       ,
     eAllFractionNoWater     ,
     eAllSusceptible         ,
     eAllInfected            ,
     eAllRecovered           ,
     eAllDeadDueToDisease    ,
     eAllFractionInfected    ,
     eAllFractionRecovered   ,
     eAllInfrastructure      ,
     eAllDailyDead           ,
     eAllTotalDead           ,
     eAllSmoothedDead        ,
     eAllTEST                ,
     eAllDDisaffection       ,   // Derived
     eAllNumAttr
};

/// The number of modifiable process variables.
static const int kNumModifiable = 16;

/// Mapping between modifiable pv's index and name.
static const char *modifiablePVName[] = {
     "foodDays",
     "waterDays", 
     "fractionNoMedical", 
     "fractionInfected", 
     "fractionRecovered", 
     "displaced", 
     "protected", 
     "sheltered", 
     "perceivedThreat", 
     "ethnicTension", 
     "violence", 
     "fractionCrimeVictims", 
     "disaffection", 
     "fractionNoWork",  
     "infrastructure",
     "insurgents"
};


/**
 * \brief Helper class for handling mapping between pv indices, names
 * and types etc.
 *
 * \author   Per Alexius
 * \date     $Date: 2007/01/24 13:13:25 $
 */
class PVHelper {
private:
     /// Maps a PV name to its order in the eAllPV enumeration.
     static std::map<std::string, eAllPV> sNameToOverAllOrder;
     
     /// Maps a PV display name to its order in the eAllPV enumeration.
     static std::map<std::string, eAllPV> sDisplayNameToOverAllOrder;
     
public:
     static const char* pvfName(ePVF pv) { return ::pvfName[pv]; }
     static const char* pvfType(ePVF pv) { return ::pvfType[pv]; }
     static const char* pvName(ePV pv) { return ::pvName[pv]; }
     static const char* pvType(ePV pv) { return ::pvType[pv]; }

     static const char* pdfName(eDerivedF pv) { return ::pdfName[pv]; }
     static const char* pdfType(eDerivedF pv) { return ::pdfType[pv]; }
     static const char* pdName(eDerived pv) { return ::pdName[pv]; }
     static const char* pdType(eDerived pv) { return ::pdType[pv]; }

     static const char* pcfName(ePreCalcF pv) { return ::pcfName[pv]; }
     static const char* pcfType(ePreCalcF pv) { return ::pcfType[pv]; }
     static const char* pcName(ePreCalc pv) { return ::pcName[pv]; }
     static const char* pcType(ePreCalc pv) { return ::pcType[pv]; }

     static const char* modifiablePVName(int pv) { return ::modifiablePVName[pv]; }
     static const char* allPVName(eAllPV pv);

     static eAllPV nameToOverAllOrder(const std::string& name);
     static eAllPV displayNameToOverAllOrder(const std::string& name);
};

#endif   // STRATMAS_PROCESSVARIABLES_H
