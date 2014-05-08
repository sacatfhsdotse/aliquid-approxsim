#ifndef APPROXSIM_ENGINE_H
#define APPROXSIM_ENGINE_H

// System
#include <map>
#include <vector>

// Own
#include "Lockable.h"
#include "Time2.h"
#include "TSQueue.h"

// Forward Declarations
class Buffer;
class ChangeTrackerAdapter;
class Error;
class Simulation;

/**
 * \brief Enumeration for commands that may be sent to the Engine.
 */
enum eEngMsg {
     eEngInitSimulation,
     eEngStep,
     eEngUpdate,
     eEngReset,
     eEngEndScenario,
     eEngNoMsg
};


/**
 * \brief This is a helper class used to separate the different times
 * that different passive clients are interested in receiving data
 * for.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/06 09:18:09 $
 */
class UniqueTime {
private:
     static int smNextId;   ///< The next id to generate.
     int        mId;        ///< This UniqueTime's id.
     Time       mTime;      ///< This UniqueTime's time.

public:
     /**
      * \brief Creates a UniqueTime for the provided time.
      *
      * \param t The time.
      */
     UniqueTime(const Time &t) : mId(smNextId++), mTime(t) {}

     /**
      * \brief Copy constructor.
      *
      * \param t The UniqueTime to copy.
      */
     UniqueTime(const UniqueTime &t) : mId(t.mId), mTime(t.mTime) {}

     /**
      * \brief Accessor for the time.
      *
      * \return The time.
      */
     Time time() const { return mTime; }

     /**
      * \brief Less than operator.
      *
      * \param t The UniqueTime to compare to.
      * \return True if this object is less than the provided object,
      * false otherwise.
      */
     bool operator < (const UniqueTime &t) const { return (mTime < t.mTime ||
                                                                   mTime == t.mTime && mId < t.mId); }
};


/**
 * \brief Object returned by the Engine when a task enqueued by a
 * Session is finished.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/06 09:18:09 $
 */
class EngineStatusObject {
private:
     /// Vector holding the errors.
     std::vector<Error> mErrors;

public:
     /**
      * \brief Default constructor.
      */
     EngineStatusObject() {}

     /**
      * \brief Creates a status object containing an Error.
      *
      * \param e The error.
      */
     EngineStatusObject(const Error& e) { mErrors.push_back(e); }

     /**
      * \brief Creates a status object containing multiple Errors.
      *
      * \param e A vector containing the errors.
      */
     EngineStatusObject(const std::vector<Error>& e) : mErrors(e) {}

     /**
      * \brief Copy constructor.
      *
      * \param e The object to copy.
      */
     EngineStatusObject(const EngineStatusObject& e) : mErrors(e.mErrors) {}

     /**
      * \brief Checks if this status object contains any errors.
      *
      * \return True if this status object contains any errors.
      */
     bool errorOccurred() const { return (!mErrors.empty()); }

     /**
      * \brief Accessor for the error vector.
      *
      * \return The error vector.
      */
     std::vector<Error> errors() const { return mErrors; }

     /**
      * \brief Assignment operator.
      *
      * \param e The object to assign from.
      */
     EngineStatusObject& operator = (const EngineStatusObject& e) { mErrors = e.mErrors; return *this; }
};

/**
 * \brief This class represents the 'engine' that runs the
 * simulation.
 *
 * The engine listens to commands from a Session and acts according to
 * those commands. The thread running the Engine main loop is the
 * thread that performs the actual simulation work.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/06 09:18:09 $
 */
class Engine : public Lockable {
private:
     /// A reference to the Buffer.
     Buffer& mBuf;

     /**
      * \brief The queue used by other threads to communicate with the
      * Engine thread.
      */
     TSQueue<int> mQ;

     /**
      * \brief The queue used by the Engine to communicate with the
      * active client.
      */
     TSQueue<EngineStatusObject> mOutQ;

     /**
      * \brief Maps a UniqueTime to the queue used for communicating
      * with the thread representing for Session that is interested in
      * that time.
      */
     std::map<UniqueTime, TSQueue<EngineStatusObject>*> mTimeListeners;

     /// Indicates if the simulation is initialized or not.
     bool mInitialized;

     /// A pointer to the Simulation.
     Simulation* mSimulation;

     /// Number of steps to iterate when getting the next step command.
     int mNumberOfTimesteps;

     void endSimulation();
     void notifyAllTimeListeners(const char* errMsg = 0);
     
public:
     Engine(Buffer &b);
     ~Engine();

     /**
      * \brief Accessor for the mInitialized flag.
      *
      * \return The status of the mInitialized flag.
      */
     bool initialized() const { return mInitialized; }

     void createSimulation(DataObject* simulation, int64_t creator);

     /**
      * \brief Mutator for the number of timesteps.
      *
      * \param ts The number of timesteps.
      */
     void setNumberOfTimesteps(int ts) { mNumberOfTimesteps = ts; }
     
     /**
      * \brief Waits for the Engine to finish the task it is currently
      * performing.
      *
      * Blocks the calling thread until the task is performed. Called
      * by the Session representing the active client for example when
      * waiting for an initializatio or a time step to be finished.
      *
      * \return The status of the performed task according to the
      * eOutMsg enumeration.
      */
     EngineStatusObject wait() { return mOutQ.dequeue(); }

     void run();
     void put(enum eEngMsg msg);

     UniqueTime registerInterestInTime(Time t, TSQueue<EngineStatusObject>* q);
     void deregisterInterestInTime(UniqueTime ut);
     
     static void* start(void* engineToStart);
};

#endif   // APPROXSIM_ENGINE_H
