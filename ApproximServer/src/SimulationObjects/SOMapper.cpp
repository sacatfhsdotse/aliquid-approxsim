// System
#include <ostream>

// Own
#include "Error.h"
#include "SimulationObject.h"
#include "SOMapper.h"

// Static definitions
SOMap SOMapper::mMap;


/**
 * \brief Registers the provided SimulationObject with this SOMapper.
 *
 * \param c The SimulationObject to register.
 */
void SOMapper::reg(SimulationObject* c) {
     if (mMap.find(&c->ref()) != mMap.end()) {
          Error e;
          e << "Tried to register SimulationObject with Reference '" << c->ref() << "' twice";
          throw e;
     }
     mMap[&c->ref()] = c;
}

/**
 * \brief Extracts data from all mapped objects to the provided
 * Buffer.
 *
 * \param b The Buffer to extract data to.
 */
void SOMapper::extract(Buffer& b)
{
     for (SOMap::iterator it = mMap.begin(); it != mMap.end(); it++) {
          it->second->extract(b);
     }
}

/**
 * \brief For debugging purposes.
 *
 * \param o The stream to write to.
 * \param m The SOMapper to print.
 */
std::ostream& operator << (std::ostream& o, const SOMapper& m) {
     for (SOMap::const_iterator it = m.mMap.begin(); it != m.mMap.end(); it++) {
          o << *it->first << std::endl;
     }
     return o;
}
