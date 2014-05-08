#ifndef APPROXSIM_DISEASE_H
#define APPROXSIM_DISEASE_H


// System
#include <string>

// Own
#include "SimulationObject.h"

// Forward declarations
class DataObject;


/**
 * \brief This is the SimulationObject that corresponds to the Disease
 * type in the Approxsim xml schema.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/06 09:18:08 $
 */
class Disease : public SimulationObject {
private:
     std::string mDescription; ///< Description of the disease.
     double mInfectionRate;    ///< The infection rate of this disease.
     double mRecoveryRate;     ///< The recovery rate parameter of this disease.
     double mMortalityRate;    ///< The mortality rate of this disease.
public:
     Disease(const DataObject& d);

     /**
      * \brief Accessor for the infection rate.
      *
      * \return The infection rate.
      */
     double infectionRate() const { return mInfectionRate; }

     /**
      * \brief Accessor for the recovery rate.
      *
      * \return The recovery rate.
      */
     double recoveryRate()  const { return mRecoveryRate ; }

     /**
      * \brief Accessor for the mortality rate.
      *
      * \return The mortality rate.
      */
     double mortalityRate() const { return mMortalityRate; }

     void update(const Update& u);
     void extract(Buffer &b) const;
     void reset(const DataObject& d);
};

#endif   // APPROXSIM_DISEASE_H
