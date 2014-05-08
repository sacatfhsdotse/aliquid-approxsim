#ifndef APPROXSIM_PVREGION_H
#define APPROXSIM_PVREGION_H


// System
#include <vector>

// Own
#include "ProcessVariables.h"

// Forward Declarations
class Reference;
class Shape;

namespace XERCES_CPP_NAMESPACE {
     class DOMElement;
}


/**
 * \brief This class represents a region and a pv value to set in that
 * region.
 *
 * \author   Per Alexius
 * \date     $Date: 2007/01/24 17:03:11 $
 */
class PVRegion {
private:
     /// The pv value.
     double mValue;
     /// Reference to a shape or null if it's a CreatedRegion.
     const Reference* mShapeRef;
     /// The shape defining this region.
     mutable const Shape* mArea;

public:
     PVRegion(const DOMElement* n);

     ~PVRegion();
     
     /**
      * \brief Accessor for the value.
      *
      * \return The value.
      */
     double value() const { return mValue; }

     const Shape& area() const;
};



/**
 * \brief This class represents a ProcessVariableInitialValues xml
 * object.
 *
 * \author   Per Alexius
 * \date     $Date: 2007/01/24 17:03:11 $
 */
class PVInitValue {
private:
     /// The pv index (as in the eAllPV enumeration).
     eAllPV mPV;

     /// References to the factions that the initialization should affect.
     std::vector<const Reference*> mFactions;

     /// The regions that the initialization should affect.
     std::vector<PVRegion*> mRegions;

public:
     PVInitValue(const DOMElement* n);
     ~PVInitValue();

     /**
      * \brief Accessor for the pv index.
      *
      * \return The pv index.
      */
     eAllPV pv() const { return mPV; }

     /**
      * \brief Accessor for the factions vector.
      *
      * \return The factions vector.
      */
     const std::vector<const Reference*>& factions() const { return mFactions; }

     /**
      * \brief Accessor for region vector.
      *
      * \return The region vector.
      */
     const std::vector<PVRegion*>& regions() const { return mRegions; }
};



/**
 * \brief This class represents a set of ProcessVariableInitialValues
 * xml objects.
 *
 * \author   Per Alexius
 * \date     $Date: 2007/01/24 17:03:11 $
 */
class PVInitValueSet {
private:
     /**
      * \brief The current set used by the simulation.
      */
     static PVInitValueSet* sCurrentSet;

     /// The vector containing the PVInitValues.
     std::vector<PVInitValue*> mInitValues;

public:
     PVInitValueSet(const DOMElement* n);
     ~PVInitValueSet();

     /**
      * \brief Accessor for the current set.
      *
      * \return The current set used by the simulation.
      */
     static const PVInitValueSet* currentSet() { return sCurrentSet; }

     /**
      * \brief Accessor for initial values vector.
      *
      * \return The initial values vector.
      */
     const std::vector<PVInitValue*>& initValues() const { return mInitValues; }
};

#endif   // APPROXSIM_PVREGION_H
