#ifndef STRATMAS_SOFACTORYLISTENER
#define STRATMAS_SOFACTORYLISTENER


// System

// Own
#include "stdint.h"

// Forward Declarations
class Reference;


/**
 * \brief SOFactoryListener is a pure virtual class defining the
 * interface for objects that listen to the SOFactory.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/03 14:18:23 $
 */
class SOFactoryListener {
public:
     /**
      * \brief Destructor
      */
     virtual ~SOFactoryListener() {}

     /**
      * \brief Called when an object has been added by the SOFactory.
      *
      * Both the SimulationObject and the corresponding DataObject
      * exists and are registered when this call occurs.
      *
      * \param ref The Reference to the object that was added.
      * \param initiator The id of the initiator of the event.
      */
     virtual void objectAdded(const Reference& ref, int64_t initiator) = 0;

     /**
      * \brief Called when an object has been removed by the SOFactory.
      * 
      * When this function is called the SimulationObject is already
      * deleted and deregistered. The corresponding DataObject does
      * still exist and is still registered.
      *
      * \param ref The Reference to the object that is removed
      * \param initiator The id of the initiator of the event.
      */
     virtual void objectRemoved(const Reference& ref, int64_t initiator) = 0;
};

#endif   // STRATMAS_SOFACTORYLISTENER
