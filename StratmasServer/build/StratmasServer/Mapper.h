#ifndef STRATMAS_MAPPER_H
#define STRATMAS_MAPPER_H

// System
#include <map>
#ifndef __win__
#include <ext/hash_map>
#endif


// Own
#include "DataObject.h"
#include "debugheader.h"
#include "Error.h"
#include "Reference.h"

// Forward Declarations

#ifndef __win__
namespace stdext = ::__gnu_cxx; 
#endif


#ifdef __win__
typedef std::map<const Reference*, DataObject*, lessReferenceP> MapType;
#else
typedef stdext::hash_map<const Reference*, DataObject*, hashReferenceP> MapType;
#endif


/**
 * \brief This class is used to map References to their corresponding
 * DataObject.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/03 14:18:23 $
 */
class Mapper {
private:
     /// Contains the mappings between Reference and DataObject.
     static MapType mMap;

public:
     /**
      * \brief Registers the provided DataObject with this Mapper.
      *
      * \param c The DataObject to register.
      */
     static void reg(const DataObject* c) {
	  if (mMap.find(&c->ref()) != mMap.end()) {
	       Error e;
	       e << "Tried to register Reference '" << c->ref() << "' twice";
	       throw e;
	  }
	  mMap[&c->ref()] = const_cast<DataObject*>(c);
     }

     /**
      * \brief Deregisters the provided DataObject from this mapper.
      *
      * \param d The DataObject to deregister.
      */
     static void dereg(const DataObject& d) {
	  mMap.erase(&d.ref());
     }

     /**
      * \brief Maps the provided Reference to its corresponding
      * DataObject.
      *
      * \param ref The Reference to find a DataObject for.
      * \return The DataObject for the provided Reference of null if no
      * such DataObject was found..
      */
     static DataObject* map(const Reference& ref) {
	  DataObject *ret = 0;
	  MapType::const_iterator it = mMap.find(&ref);
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

     /**
      * \brief For debugging purposes.
      *
      * \param o The stream to write to.
      * \param m The Mapper to print.
      */
     friend std::ostream& operator << (std::ostream& o, const Mapper& m) {
	  MapType::const_iterator it2;
	  for (it2 = m.mMap.begin(); it2 != m.mMap.end(); it2++) {
	       o << *it2->first << std::endl;
	  }
	  return o;
     }
};

#endif   // STRATMAS_MAPPER_H
