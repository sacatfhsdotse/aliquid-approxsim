// System
#include <iostream>

// Own
#include "Error.h"
#include "ProcessVariables.h"

// Static Definitions
std::map<std::string, eAllPV> PVHelper::sNameToOverAllOrder;
std::map<std::string, eAllPV> PVHelper::sDisplayNameToOverAllOrder;


/**
 * \brief The schema name of the PV:s in the same order as the eAllPV
 * enumeration.
 */
static const char* allPVSchemaName[] = {
     "population"          ,
     "displaced"           ,
     "sheltered"           ,
     "protected"           ,
     "violence"            ,
     "perceivedThreat"     ,
     "insurgents"          ,
     "fractionNoMedical"   ,
     "fractionNoWork"      ,
     "ethnicTension"       ,
     "fractionCrimeVictims",
     "housingUnits"        ,
     "storedFood"          ,
     "foodConsumption"     ,
     "farmStoredFood"      ,
     "marketedFood"        ,
     "foodDays"            ,
     "fractionNoFood"      ,
     "waterConsumption"    ,
     "waterDays"           ,
     "suppliedWater"       ,
     "fractionNoWater"     ,
     "susceptible"         ,
     "infected"            ,
     "recovered"           ,
     "deadDueToDisease"    ,
     "fractionInfected"    ,
     "fractionRecovered"   ,
     "infrastructure"      ,
     "dailyDead"           ,
     "totalDead"           ,
     "smoothedDead"        ,
     "TEST"                ,
     "disaffection"
};

/**
 * \brief The display name of the PV:s in the same order as the eAllPV
 * enumeration.
 */
static const char* allPVDisplayName[] = {
     "Population"      ,
     "Displaced"       ,
     "Sheltered"       ,
     "Protected"       ,
     "Violence"        ,
     "Perceived Threat",
     "Insurgents"      ,
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
     "TEST"                  ,
     "Disaffection"
};


const char* PVHelper::allPVName(eAllPV pv)
{
     return allPVSchemaName[pv];
}

/**
 * \brief Maps an attribute name to its order in the attribute array.
 *
 * \param name The attribute name.
 * \return The value for the specified attribute.
 */
eAllPV PVHelper::nameToOverAllOrder(const std::string& name) {
     static bool firstTime = true;
	  
     if (firstTime) {
	  firstTime = false;
	  for (int i = 0; i < eNumWithFac + eNumNoFac + eDNumDerivedF; ++i) {
	       sNameToOverAllOrder[allPVSchemaName[i]] = static_cast<eAllPV>(i);
	  }
     }
     std::map<std::string, eAllPV>::iterator it = sNameToOverAllOrder.find(name);
     if (it == sNameToOverAllOrder.end()) {
	  Error e;
	  e << "The process variable '" << name << "' is not a valid Stratmas process variable.";
	  throw e;
     }

     return it->second;
}

/**
 * \brief Maps an attribute's display name to its order in the
 * attribute array.
 *
 * \param name The attribute name.
 * \return The value for the specified attribute.
 */
eAllPV PVHelper::displayNameToOverAllOrder(const std::string& name) {
     static bool firstTime = true;
	  
     if (firstTime) {
	  firstTime = false;
	  for (int i = 0; i < eNumWithFac + eNumNoFac + eDNumDerivedF; ++i) {
	       sDisplayNameToOverAllOrder[allPVDisplayName[i]] = static_cast<eAllPV>(i);
	  }
     }
     std::map<std::string, eAllPV>::iterator it = sDisplayNameToOverAllOrder.find(name);
     if (it == sDisplayNameToOverAllOrder.end()) {
	  Error e;
	  e << "The process variable '" << name << "' is not a valid Stratmas process variable.";
	  throw e;
     }

     return it->second;
}
