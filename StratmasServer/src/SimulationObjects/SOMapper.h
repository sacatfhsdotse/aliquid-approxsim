#ifndef _STRATMAS_SOMAPPER_H
#define _STRATMAS_SOMAPPER_H

// System
#ifdef OS_WIN32
#include <map>
#else
#include <ext/hash_map>
#endif
#include <iosfwd>

// Own
#include "Reference.h"

// Forward Declarations
class Buffer;
class SimulationObject;

#ifndef OS_WIN32
namespace stdext = ::__gnu_cxx; 
#endif


#ifdef OS_WIN32
typedef std::map<const Reference*, SimulationObject*, lessReferenceP> SOMap;
#else
 typedef stdext::hash_map<const Reference*, SimulationObject*, hashReferenceP> SOMap;
#endif


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

#endif   // _STRATMAS_SOMAPPER_H
