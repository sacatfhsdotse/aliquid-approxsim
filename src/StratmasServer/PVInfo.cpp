// System
#include <ostream>

// Own
#include "debugheader.h"
#include "ProcessVariables.h"
#include "PVInfo.h"


using namespace std;

// Static Definitions
std::vector<PVDescription> PVInfo::sStaticPV;
std::vector<PVDescription> PVInfo::sSimulationDependentPV;


/**
 * \brief Produces an XML representation of this PVDescription
 * according to the xml schemas.
 *
 * \param o The ostream to print to.
 * \return The ostream with the XML representation written to it.
 */
ostream& PVDescription::toXML(std::ostream& o) const
{
     o << "<pv>" << endl;
     o << "  <name>" << mName << "</name>" << endl;
//     o << "  <type>" << mType << "</type>" << endl;
     o << "  <category>" << mCategory << "</category>" << endl;
     o << "  <factions>" << (mFactions ? "true" : "false") << "</factions>" << endl;
     o << "  <range xsi:type=\"sp:DoubleRange\">" << endl;
     o << "    <min>" << mMin << "</min>" << endl;
     o << "    <max>" << mMax << "</max>" << endl;
     o << "  </range>" << endl;
     o << "</pv>" << endl;
     return o;
}

/**
 * \brief Adds a PVDescription with the provided values.
 *
 * \param name The name of the PV.		  
 * \param type The type of the PV.	  
 * \param cat The category of the PV.	  
 * \param fac Flag for factions or not.	  
 * \param min The minimum value.		  
 * \param max The maximum value.		  
 * \param visible Flag indicating visibility to client.
 */
void PVInfo::addStaticPV(string name, string type, string cat, bool fac, string min, string max, bool visible)
{
     sStaticPV.push_back(PVDescription(name, type, cat, fac, min, max, visible));
}

/**
 * \brief Initializes this PVInfo object.
 */
void PVInfo::init()
{
     addStaticPV(PVHelper::pvfName(ePopulation          ), "original", "Environmental"  , true , "0"   , "INF");
     addStaticPV(PVHelper::pvName (eTEST                ), "test"    , "Unknown"        , false, "-INF", "10000", false);
     addStaticPV(PVHelper::pvfName(eDisplaced           ), "original", "Social"         , true , "0"   , "INF");
     addStaticPV(PVHelper::pvfName(eSheltered           ), "original", "Environmental"  , true , "0"   , "INF");
     addStaticPV(PVHelper::pvfName(eProtected           ), "original", "Social"         , true , "0"   , "INF");
     addStaticPV(PVHelper::pvfName(eViolence            ), "original", "Social"         , true , "0"   , "100");
     addStaticPV(PVHelper::pvfName(ePerceivedThreat     ), "original", "Quality of Life", true , "0"   , "100");
     addStaticPV(PVHelper::pdfName(eDDisaffection       ), "derived" , "Political"      , true , "0"   , "100");
     addStaticPV(PVHelper::pvfName(eInsurgents          ), "original", "Environmental"  , true , "0"   , "INF");

     addStaticPV(PVHelper::pvName (eFractionNoMedical   ), "original", "Quality of Life", false, "0"   , "1"  );
     addStaticPV(PVHelper::pvName (eFractionNoWork      ), "original", "Economical"     , false, "0"   , "1"  );
     addStaticPV(PVHelper::pvName (eEthnicTension       ), "original", "Social"         , false, "0"   , "1"  );
     addStaticPV(PVHelper::pvName (eFractionCrimeVictims), "original", "Social"         , false, "0"   , "1"  );
     addStaticPV(PVHelper::pvName (eHousingUnits        ), "not used", "UNKNOWN"        , false, "0"   , "INF", false);
     addStaticPV(PVHelper::pvName (eStoredFood          ), "not used", "UNKNOWN"        , false, "0"   , "INF", false);
     addStaticPV(PVHelper::pvName (eFoodConsumption     ), "not used", "UNKNOWN"        , false, "0"   , "1"  , false);
     addStaticPV(PVHelper::pvName (eFarmStoredFood      ), "not used", "UNKNOWN"        , false, "0"   , "INF", false);
     addStaticPV(PVHelper::pvName (eMarketedFood        ), "not used", "UNKNOWN"        , false, "0"   , "INF", false);
     addStaticPV(PVHelper::pvName (eFoodDays            ), "original", "Economical"     , false, "0"   , "18" );
     addStaticPV(PVHelper::pvName (eFractionNoFood      ), "derived" , "Environmental"  , false, "0"   , "1"  );
     addStaticPV(PVHelper::pvName (eWaterConsumption    ), "not used", "UNKNOWN"        , false, "0"   , "20" , false);
     addStaticPV(PVHelper::pvName (eWaterDays           ), "original", "Social"         , false, "0"   , "5"  );
     addStaticPV(PVHelper::pvName (eSuppliedWater       ), "other"   , "Economical"     , false, "0"   , "INF");
     addStaticPV(PVHelper::pvName (eFractionNoWater     ), "derived" , "Environmental"  , false, "0"   , "1"  );
     addStaticPV(PVHelper::pvName (eSusceptible         ), "not used", "UNKNOWN"        , false, "0"   , "INF", false);
     addStaticPV(PVHelper::pvName (eInfected            ), "not used", "UNKNOWN"        , false, "0"   , "INF", false);
     addStaticPV(PVHelper::pvName (eRecovered           ), "not used", "UNKNOWN"        , false, "0"   , "INF", false);
     addStaticPV(PVHelper::pvName (eDeadDueToDisease    ), "not used", "UNKNOWN"        , false, "0"   , "INF", false);
     addStaticPV(PVHelper::pvName (eFractionInfected    ), "original", "Social"         , false, "0"   , "1"  );
     addStaticPV(PVHelper::pvName (eFractionRecovered   ), "original", "Social"         , false, "0"   , "1"  );
     addStaticPV(PVHelper::pvName (eInfrastructure      ), "original", "Governance"     , false, "0"   , "1"  );
     addStaticPV(PVHelper::pvName (eDailyDead           ), "other"   , "Environmental"  , false, "0"   , "INF");
     addStaticPV(PVHelper::pvName (eTotalDead           ), "derived" , "Environmental"  , false, "0"   , "INF");
     addStaticPV(PVHelper::pvName (eSmoothedDead        ), "internal", "Unknown"        , false, "0"   , "INF", false);

     addStaticPV(PVHelper::pdName (eDPolarization       ), "derived" , "Political"      , false, "0"   , "100");
     addStaticPV(PVHelper::pdName (eDHoused             ), "not used", "UNKNOWN"        , false, "0"   , "INF", false);
     addStaticPV(PVHelper::pdName (eDAvailableFood      ), "not used", "UNKNOWN"        , false, "0"   , "INF", false);
     addStaticPV(PVHelper::pdName (eDFoodDeprivation    ), "not used", "UNKNOWN"        , false, "0"   , "1"  , false);
     addStaticPV(PVHelper::pdName (eDWaterSurplusDeficit), "not used", "UNKNOWN"        , false, "-INF", "INF", false);
     addStaticPV(PVHelper::pdName (eDWaterDeprivation   ), "not used", "UNKNOWN"        , false, "0"   , "1"  , false);
     
     if (kShowPrecalculated) {
	  addStaticPV(PVHelper::pcfName(ePAtHome                 ), "precalc" , "UNKNOWN"  , true , "0"   , "INF", true);
	  addStaticPV(PVHelper::pcfName(ePSheltered              ), "precalc" , "UNKNOWN"  , true , "0"   , "INF", true);
	  addStaticPV(PVHelper::pcfName(ePUnsheltered            ), "precalc" , "UNKNOWN"  , true , "0"   , "INF", true);
	  addStaticPV(PVHelper::pcfName(ePHomeIll                ), "precalc" , "UNKNOWN"  , true , "0"   , "INF", true);
	  addStaticPV(PVHelper::pcfName(ePShelteredIll           ), "precalc" , "UNKNOWN"  , true , "0"   , "INF", true);
	  addStaticPV(PVHelper::pcfName(ePUnshelteredIll         ), "precalc" , "UNKNOWN"  , true , "0"   , "INF", true);
	  addStaticPV(PVHelper::pcfName(ePHomeImmune             ), "precalc" , "UNKNOWN"  , true , "0"   , "INF", true);
	  addStaticPV(PVHelper::pcfName(ePShelteredImmune        ), "precalc" , "UNKNOWN"  , true , "0"   , "INF", true);
	  addStaticPV(PVHelper::pcfName(ePUnshelteredImmune      ), "precalc" , "UNKNOWN"  , true , "0"   , "INF", true);
	  addStaticPV(PVHelper::pcfName(ePHomeDead               ), "precalc" , "UNKNOWN"  , true , "0"   , "INF", true);
	  addStaticPV(PVHelper::pcfName(ePShelteredDead          ), "precalc" , "UNKNOWN"  , true , "0"   , "INF", true);
	  addStaticPV(PVHelper::pcfName(ePUnshelteredDead        ), "precalc" , "UNKNOWN"  , true , "0"   , "INF", true);
	  addStaticPV(PVHelper::pcfName(ePIDPDeadDueToViolence   ), "precalc" , "UNKNOWN"  , true , "0"   , "INF", true);
	  addStaticPV(PVHelper::pcfName(ePHousedDeadDueToViolence), "precalc" , "UNKNOWN"  , true , "0"   , "INF", true);
	  addStaticPV(PVHelper::pcfName(ePNewIDPDueToViolence    ), "precalc" , "UNKNOWN"  , true , "0"   , "INF", true);
	  addStaticPV(PVHelper::pcfName(ePTowardsCampDelta       ), "precalc" , "UNKNOWN"  , true , "0"   , "INF", true);
	  addStaticPV(PVHelper::pcfName(ePDiffusionDelta         ), "precalc" , "UNKNOWN"  , true , "0"   , "INF", true);
	  addStaticPV(PVHelper::pcfName(ePDiffusionDisplacedDelta), "precalc" , "UNKNOWN"  , true , "0"   , "INF", true);

	  addStaticPV(PVHelper::pcName (ePFoodProduction         ), "not used", "UNKNOWN"  , false, "0"   , "INF", true);
	  addStaticPV(PVHelper::pcName (ePWaterProduction        ), "not used", "UNKNOWN"  , false, "0"   , "INF", true);
     }
}

/**
 * \brief Adds a PVDescription with the provided values.
 *
 * \param n   The name of the PV.		  
 * \param t   The type of the PV.	  
 * \param c   The category of the PV.	  
 * \param f   Flag for factions or not.	  
 * \param min The minimum value.		  
 * \param max The maximum value.		  
 */
void PVInfo::addPV(string n, string t, std::string c, bool f, string min, string max)
{
     sSimulationDependentPV.push_back(PVDescription(n, t, c, f, min, max));
}

/**
 * \brief Produces an XML representation of this PVInfo according to
 * the xml schemas.
 *
 * \param o The ostream to print to.
 * \return The ostream with the XML representation written to it.
 */
ostream& PVInfo::toXML(std::ostream& o)
{
     o << "<processVariables>" << endl;
     for (vector<PVDescription>::iterator it = sStaticPV.begin(); it != sStaticPV.end(); it++) {
	  if (it->visible()) {
	       it->toXML(o);
	  }
     }
     for (vector<PVDescription>::iterator it = sSimulationDependentPV.begin();
	  it != sSimulationDependentPV.end(); it++) {
	  if (it->visible()) {
	       it->toXML(o);
	  }
     }
     o << "</processVariables>" << endl;
     return o;
}
