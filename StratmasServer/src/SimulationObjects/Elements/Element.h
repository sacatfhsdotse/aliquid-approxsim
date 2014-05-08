#ifndef APPROXSIM_ELEMENT_H
#define APPROXSIM_ELEMENT_H


// Own
#include "UpdatableSOAdapter.h"

// Forward Declarations
class Distribution;
class LatLng;
class Shape;


/**
 * \brief Abstract base class for a Approxsim Element.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/02/28 17:48:16 $
 */
class Element : public UpdatableSOAdapter {
protected:
     /// This Element's Location
     Shape* mLocation;

     /// The distribution of this Element.
     Distribution* mDeployment;

public:
     Element(const Reference& ref, const Shape& location);
     Element(const DataObject& d);
     virtual ~Element();

     /**
      * \brief Checks for presence.
      *
      * \return True if this Element is present, false otherwise.
      */
     virtual bool present() const = 0;

     /**
      * \brief Accessor for the location.
      *
      * \return The location.
      */
     const Shape& location() const { return *mLocation; }

     /**
      * \brief Accessor for the distribution.
      *
      * \return The Distribution.
      */
     inline const Distribution& deployment() const { return *mDeployment; }

     LatLng center() const;

     void extract(Buffer &b) const;
     virtual void replaceObject(DataObject& newObject, int64_t initiator);
     virtual void modify(const DataObject& d);
     virtual void reset(const DataObject& d);
};

#endif   // APPROXSIM_ELEMENT_H
