#ifndef APPROXSIM_UPDATABLESOADAPTER
#define APPROXSIM_UPDATABLESOADAPTER

// Own
#include "SimulationObject.h"
#include "stdint.h"


/**
 * \brief Conveniance class that provides some default update behavior
 * for SimulationObjects.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/03 14:18:23 $
 */
class UpdatableSOAdapter : public SimulationObject {
protected:
     /**
      * \brief Creates a UpdatableSOAdapter with the specified
      * Reference.
      *
      * \param ref The Reference for the object to be created.
      */
     UpdatableSOAdapter(const Reference& ref) : SimulationObject(ref) {}

     /**
      * \brief Creates a UpdatableSOAdapter from the specified
      * Referencable.
      *
      * \param ref A Referencable for the object to be created.
      */
     UpdatableSOAdapter(const Referencable& ref) : SimulationObject(ref) {}

     /**
      * \brief Creates a UpdatableSOAdapter from the specified
      * DataObject.
      *
      * \param d The DataObject to create this object from.
      */
     UpdatableSOAdapter(const DataObject& d) : SimulationObject(d) {}

public:
     virtual ~UpdatableSOAdapter() {}
     virtual void update(const Update& u);
     virtual void addObject(DataObject& toAdd, int64_t initiator);
     virtual void removeObject(const Reference& toRemove, int64_t initiator);
     virtual void replaceObject(DataObject& newObject, int64_t initiator);
     virtual void modify(const DataObject& d);
};

#endif   // APPROXSIM_UPDATABLESOADAPTER
