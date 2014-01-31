#ifndef STRATMAS_RESETTER
#define STRATMAS_RESETTER

// System
#include <map>
#include <vector>

// Own
#include "DataObject.h"
#include "Resetter.h"
#include "SimulationObject.h"
#include "SOFactory.h"


/**
 * \brief Convenience class that is used to handle resetting of
 * vectors of SimulationObjects.
 *
 * This class is used when the client resets a Simulation in order to
 * restore the original set of SimulationObjects in a vector based on
 * the original corresponding vector of DataObjects.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/10 08:58:35 $
 */
template<class T> class Resetter {
public:
     static void reset(std::vector<T*>& simObjs, const std::vector<DataObject*>& dataObjs);
};


/**
 * \brief Adds, removes and modifies the SimulationObjects in the
 * simObjs vector based on the DataObjects in the dataObjs vector so
 * that the SimulationObjects looks just like they would have if they
 * were created from the provided DataObjects. 
 *
 * \param simObjs The vector containing the SimulationObjects to be
 * reset. On exit this vector contains the SimulationObjects as they
 * would have looked like if they were created from the DataObjects in
 * the dataObjs vector.
 *
 * \param dataObjs The vector of DataObjects to reset the
 * SimulationObjects with.
 */
template<class T> void Resetter<T>::reset(std::vector<T*>& simObjs, const std::vector<DataObject*>& dataObjs)
{
     std::map<const Reference*, SimulationObject*> objs;

     // Store SimulationObjects in map. 
     for (unsigned int i = 0; i < simObjs.size(); i++) {
	  objs[&simObjs[i]->ref()] = simObjs[i];
     }

     // Clear original vector
     simObjs.clear();

     // 
     for (std::vector<DataObject*>::const_iterator it = dataObjs.begin(); it != dataObjs.end(); it++) {
	  std::map<const Reference*, SimulationObject*>::iterator it2 = objs.find(&(*it)->ref());
	  if (it2 != objs.end()) {
	       // Object exists both in current and original state so let's reset it.
	       it2->second->reset(**it);
	       simObjs.push_back(dynamic_cast<T*>(it2->second));
	       objs.erase(it2);
	  }
	  else {
	       // Object existed in original state but not in current so let's create it.
	       simObjs.push_back(dynamic_cast<T*>(SOFactory::createSimulationObject(*(*it)->clone())));
	  }
     }
     for (std::map<const Reference*, SimulationObject*>::iterator it = objs.begin(); it != objs.end(); it++) {
	  // Object exists in current state but not in the original so let's remove it.
	  SOFactory::removeSimulationObject(it->second);
     }
}

#endif   // STRATMAS_RESETTER
