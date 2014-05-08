#ifndef _APPROXSIM_TIMESTEPPER_H
#define _APPROXSIM_TIMESTEPPER_H

// Own
#include "SimulationObject.h"
#include "Time2.h"

// Forward Declarations
class DataObject;
class Update;


/**
 * \brief An abstract base class for all TimeSteppers
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/06 14:23:13 $
 */
class TimeStepper : public SimulationObject {
public:
     /**
      * \brief Creates a TimeStepper from the provided DataObject.
      *
      * \param d The DataObject to create this TimeStepper from.
      */
     TimeStepper(const DataObject& d) : SimulationObject(d) {}

     /**
      * \brief Get the length of the timestep.
      *
      * \return The length of the timestep.
      */
     virtual Time dt() = 0;
};


/**
 * \brief A TimeStepper that takes timesteps of constant length.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/06 14:23:13 $
 */
class ConstantStepper : public TimeStepper {
private:
     /// Length of the timestep.
     Time mDt;

public:
     ConstantStepper(const DataObject &);

     void update(const Update& u);
     void extract(Buffer &b) const;
     void reset(const DataObject& d);

     /**
      * \brief Get the length of the timestep.
      *
      * \return The length of the timestep.
      */
     Time dt() { return mDt; }
};

#endif   // _APPROXSIM_TIMESTEPPER_H
