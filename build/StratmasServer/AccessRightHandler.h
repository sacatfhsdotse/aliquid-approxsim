#ifndef STRATMAS_ACCESSRIGHTHANDLER
#define STRATMAS_ACCESSRIGHTHANDLER


// System
#include <set>

// Own
#include "Type.h"

// Forward Declarations
class Reference;


/**
 * \brief This class keeps track of which types of objects that the
 * server may not change during a simulation.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/02 17:06:50 $
 */
class AccessRightHandler {
private:
     /// The set of Types that may not change during a simulation.
     static std::set<const Type*, lessTypeP> sUnchangableTypes;

public:
     static bool changeable(const Reference& ref);
};

#endif   // STRATMAS_ACCESSRIGHTHANDLER
