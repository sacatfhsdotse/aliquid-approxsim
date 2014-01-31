#ifndef STRATMAS_GRIDEFFECT_H
#define STRATMAS_GRIDEFFECT_H


// Own
#include "ProcessVariables.h"

// Forward Declarations
class EthnicFaction;

/**
 * \brief GridEffect represents an effect on a process variable.
 *
 * GridEffects are used in Activities that effect the Grid directly.
 */
class GridEffect {
public:
     eAllPV           mPV;        ///< The attribute the effect effects.
     double           mSeverity;  ///< The severity of the effect - a value between [-10, 10]
     EthnicFaction   *mFaction;   ///< Pointer to the EthnicFaction the effect effects.

     /**
       * \brief Constructor
       *
       * \param pv The attribute number as defined in eAllPV.
       * \param s The severity of the effect.
       * \param f A pointer to the Faction the effect referse to.
       */
     GridEffect(eAllPV pv, double s, EthnicFaction* f) : mPV(pv), mSeverity(s), mFaction(f) {}
};

#endif   // STRATMAS_GRIDEFFECT_H
