#ifndef STRATMAS_PVAREA_H
#define STRATMAS_PVAREA_H


// System
#include <map>

// Own
#include "ProcessVariables.h"

// Forward Declarations
class Distribution;
class EthnicFaction;
class Shape;


/**
 * \brief This class represents a modification to a process variable.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/19 07:04:34 $
 */
class PVModification {
private:
     /**
      * \brief Contains a mapping between pv index (as in the
      * eAllPV enumeration) to the type of aggregation that should
      * be performed if several modifications are made to the same
      * cell.
      */
     static std::map<int, int> sPVTypeMap;

     /// The pv index (as in the eAllPV enumeration).
     int mPV;

     /// The faction to modify.
     EthnicFaction* mFaction;

     /// The pv value to use.
     double mValue;

public:
     /// Enumeration for aggregation types.
     enum ePVType {eSum, eMean, eUnknown};

     /**
      * \brief Constructor.
      *
      * \param pv The pv index (as in the eAllPV enumeration).
      * \param faction The faction to modify.
      * \param value The pv value.
      */
     PVModification(int pv, EthnicFaction& faction, double value) : mPV(pv), mFaction(&faction), mValue(value) {}

     /**
      * \brief Accessor for the pv index.
      *
      * \return The pv index.
      */
     int pv() const { return mPV; }

     /**
      * \brief Accessor for the faction.
      *
      * \return The faction.
      */
     EthnicFaction& faction() const { return *mFaction; }

     /**
      * \brief Accessor for the value.
      *
      * \return The value.
      */
     double value() const { return mValue; }

     /**
      * \brief Gets the aggregation type for the pv this modification
      * refers to.
      *
      * \return The aggregation type.
      */
     int type() const { return type(mPV); }

     static int type(int t);     
};

/**
 * \brief Gets the aggregation type for the provided pv index.
 *
 * \param t The pv index as in the eAllPV enumeration.
 * \return The aggregation type.
 */
inline int PVModification::type(int t)
{
     static bool firstTime = true;
     if (firstTime) {
	  sPVTypeMap[eAllPopulation          ] = eSum;
	  sPVTypeMap[eAllDisplaced           ] = eSum;
	  sPVTypeMap[eAllSheltered           ] = eSum;
	  sPVTypeMap[eAllProtected           ] = eSum;
	  sPVTypeMap[eAllInsurgents          ] = eSum;
	  sPVTypeMap[eAllSuppliedWater       ] = eSum;
	  sPVTypeMap[eAllDailyDead           ] = eSum;
	  sPVTypeMap[eAllTotalDead           ] = eSum;
	  sPVTypeMap[eAllViolence            ] = eMean;
	  sPVTypeMap[eAllPerceivedThreat     ] = eMean;
	  sPVTypeMap[eAllFractionNoMedical   ] = eMean;
	  sPVTypeMap[eAllFractionNoWork      ] = eMean;
	  sPVTypeMap[eAllEthnicTension       ] = eMean;
	  sPVTypeMap[eAllFractionCrimeVictims] = eMean;
	  sPVTypeMap[eAllFoodDays            ] = eMean;
	  sPVTypeMap[eAllFractionNoFood      ] = eMean;
	  sPVTypeMap[eAllWaterDays           ] = eMean;
	  sPVTypeMap[eAllFractionNoWater     ] = eMean;
	  sPVTypeMap[eAllFractionInfected    ] = eMean;
	  sPVTypeMap[eAllFractionRecovered   ] = eMean;
	  sPVTypeMap[eAllInfrastructure      ] = eMean;
     }
     std::map<int, int>::iterator it = sPVTypeMap.find(t);
     return (it == sPVTypeMap.end() ? eUnknown : it->second);
}

/**
 * \brief This class represents the interface for modification of pv
 * variables in an area.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/19 07:04:34 $
 */
class PVArea {
protected:
     /// The modifications.
     std::vector<PVModification> mPVs;

public:
     /// Destructor.
     virtual ~PVArea() {}
     
     /**
      * \brief Accessor for the Shape the modifications refer to.
      *
      * \return The Shape.
      */
     virtual const Shape& area() const = 0;

     /**
      * \brief Accessor for the Distribution of the modifications over
      * the area.
      *
      * \return The Distribution.
      */
     virtual const Distribution& distribution() const = 0;

     /**
      * \brief Accessor for the modifications.
      *
      * \return The modifications.
      */
     const std::vector<PVModification>& pvs() const { return mPVs; }
};

#endif   // STRATMAS_PVAREA_H
