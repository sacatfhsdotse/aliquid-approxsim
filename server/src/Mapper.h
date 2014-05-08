#ifndef APPROXSIM_MAPPER_H
#define APPROXSIM_MAPPER_H

// System
#include <map>
#include <unordered_map>

// Own
#include "DataObject.h"
#include "debugheader.h"
#include "Error.h"
#include "Reference.h"

typedef std::unordered_map<const Reference*, DataObject*> MapType;

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

#endif   // APPROXSIM_MAPPER_H
