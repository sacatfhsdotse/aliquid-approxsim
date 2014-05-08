#ifndef APPROXSIM_SIMULATIONOBJECT
#define APPROXSIM_SIMULATIONOBJECT

// Own
#include "Referencable.h"

// Forward Declarations
class Buffer;
class DataObject;
class Update;


/**
 * \brief An abstract base class for all SimulationObjects
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/06 09:18:12 $
 */
class SimulationObject : public Referencable {
protected:
     SimulationObject(const Reference& ref);
     SimulationObject(const Referencable& ref);
     SimulationObject(const DataObject& d);
public:
     /// Destructor
     virtual ~SimulationObject();

     /**
      * \brief Updates this object.
      *
      * \param u The Update to update this object with.
      */
     virtual void update(const Update& u) = 0;

     /**
      * \brief Extracts data from this object to the Buffer.
      *
      * \param b The Buffer to extract data to.
      */
     virtual void extract(Buffer& b) const = 0;

     /**
      * \brief Resets this object to the state it would have had if it
      * was created from the provided DataObject.
      *
      * \param d The DataObject to use as source for the reset.
      */
     virtual void reset(const DataObject& d) = 0;
};

#endif   // APPROXSIM_SIMULATIONOBJECT
