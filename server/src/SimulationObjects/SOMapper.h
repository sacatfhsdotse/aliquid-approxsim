#ifndef _APPROXSIM_SOMAPPER_H
#define _APPROXSIM_SOMAPPER_H

// System
#include <unordered_map>
#include <iosfwd>

// Own
#include "Reference.h"

// Forward Declarations
class Buffer;
class SimulationObject;

typedef std::unordered_map<const Reference*, SimulationObject*> SOMap;

/**
 * \brief This class is used to map References to their corresponding
 * SimulationObject.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/03 14:18:23 $
 */
class SOMapper {
private:
     /// Contains the mappings between Reference and SimulationObject.
     static SOMap mMap;

public:
     static void reg(SimulationObject* c);

     /**
      * \brief Deregisters the provided Reference from this SOMapper.
      *
      * \param ref The Reference to deregister.
      */
     inline static void dereg(const Reference& ref) {
          mMap.erase(&ref);
     }

     /**
      * \brief Maps the provided Reference to its corresponding
      * SimulationObject.
      *
      * \param ref The Reference to find a SimulationObject for.
      * \return The SimulationObject for the provided Reference or
      * null if no such SimulationObject was found..
      */
     inline static SimulationObject* map(const Reference& ref) {
          SimulationObject *ret = 0;
          SOMap::const_iterator it = mMap.find(&ref);
          if (it != mMap.end()) {
               ret = it->second;
          }
          return ret;
     }

     /**
      * \brief Erases all mappings.
      */
     inline static void clear() {
          mMap.clear();
     }

     static void extract(Buffer& b);

     friend std::ostream& operator << (std::ostream& o, const SOMapper& m);
};

#endif   // _APPROXSIM_SOMAPPER_H
