// System
#include <iostream>

//Own
#include "Buffer.h"
#include "ChangeTrackerAdapter.h"
#include "debugheader.h"
#include "Engine.h"
#include "Error.h"
#include "Simulation.h"
#include "SOFactory.h"
#include "SOMapper.h"
#include "TSQueue.h"
#include "LogStream.h"

// Temporary
#include <sstream>
#include <fstream>
#include "random.h"
#include "stopwatch.h"

// Static Definitions
int UniqueTime::smNextId = 0;


using namespace std;


/**
 * \brief Constructor.
 *
 * \param b The Buffer.
 */
Engine::Engine(Buffer& b) : mBuf(b), mInitialized(false), mSimulation(0), mNumberOfTimesteps(1)
{
}

/**
 * \brief Destructor.
 */
Engine::~Engine()
{
     SOFactory::removeSimulationObject(mSimulation);
}

/**
 * \brief Ends the current Simulation.
 */
void Engine::endSimulation()
{
     mInitialized = false;
     if (mSimulation) {
          SOFactory::removeSimulationObject(mSimulation);
          mSimulation = 0;
     }
}

/**
 * \brief Notifies all time listeners that something has happend that
 * may have changed the simulation so that an UpdateMessage should be
 * sent.
 */
void Engine::notifyAllTimeListeners(const char* errMsg)
{
     EngineStatusObject eso;
     if (errMsg) {
          eso = EngineStatusObject(Error(errMsg));
     }
     Lock lock(mutex());
     while (mTimeListeners.begin() != mTimeListeners.end()) {
          mTimeListeners.begin()->second->enqueue(eso);
          mTimeListeners.erase(mTimeListeners.begin());
     }
     lock.unlock();
}

/**
 * \brief The main loop of the Engine.
 *
 * Blocks and waits for commands from the Session representing the
 * active client. When receiving a command the appropriate actions
 * will be taken.
 */
void Engine::run() {
     int msg;
     cout << "Engine running" << endl;
     while (true) {
          try {
               msg = mQ.dequeue();
               StopWatch s;
               switch (msg) {
               case eEngInitSimulation: {
                    mSimulation->prepareForSimulation();
                    Lock bufLock(mBuf.mutex());
                    SOMapper::extract(mBuf);
                    bufLock.unlock();
                    break;
               }
               case eEngStep: {
                    mBuf.engineIdle(false);
                    for (int i = 0; i < mNumberOfTimesteps; i++) {
                         s.start();
                         mSimulation->step();
                         s.stop();
                         debug("Step took " << s.secs() << " seconds");
                         Lock bufLock(mBuf.mutex());
                         s.start();
                         SOMapper::extract(mBuf);
                         s.stop();
                         debug("Extraction took " << s.secs() << " seconds");
                         bufLock.unlock();

                         // Notify all listeners listening for the
                         // current time or a time that has passed.
                         Lock lock(mutex());
                         while (mTimeListeners.begin() != mTimeListeners.end() &&
                                mBuf.simTime() >= mTimeListeners.begin()->first.time()) {
                              mTimeListeners.begin()->second->enqueue(EngineStatusObject());
                              mTimeListeners.erase(mTimeListeners.begin());
                         }
                         lock.unlock();
//                         debug("Simulation time after timestep: " << tt - mBuf.simulationData().mStartTime);
                    }

                    mBuf.engineIdle(true);
                    break;
               }
               case eEngUpdate: {
                    // Only active client runs this code - no need for
                    // the two calls to be made atomically.
                    mBuf.transferUpdatesToSimulation();
                    Lock bufLock(mBuf.mutex());
                    SOMapper::extract(mBuf);
                    bufLock.unlock();
                    notifyAllTimeListeners();
                    break;
               }
               case eEngReset: {
                    if (mInitialized) {
                         Lock bufLock(mBuf.mutex());
                         mBuf.reset();
                         mSimulation->reset(*mBuf.originalSimulation());
                         mSimulation->prepareForSimulation();
                         mBuf.currentTime(Simulation::simulationTime());
                         SOMapper::extract(mBuf);

                         bufLock.unlock();
                         notifyAllTimeListeners();
                    }
                    break;
               }
               case eEngEndScenario:
                    notifyAllTimeListeners("Simulation reset by active client.");
                    endSimulation();
                    break;
               default:
                    slog << "Unknown" << logEnd;
                    break;
               }
               // Everything is ok.
               mOutQ.enqueue(EngineStatusObject());
          }
          catch(Error& e) {
               slog << "Engine caught error \"" << e << "\" in Engine main loop. Notifying Session..." << logEnd;
               try {
                    endSimulation();
               } catch (...) {
                    vector<Error> errs;
                    errs.push_back(e);
                    errs.push_back(Error("Couldn't end scenario", Error::eFatal));
                    throw errs;
               }
                mOutQ.enqueue(EngineStatusObject(e));
          }
          catch (vector<Error>& e) {
               slog << "Engine caught errors in Engine main loop. Notifying Session..." << logEnd;
               for(vector<Error>::iterator it = e.begin(); it != e.end(); it++) {
                    slog << *it << "---" << logEnd;
               }
               try {
                    endSimulation();
               } catch (...) {
                    e.push_back(Error("Couldn't end scenario", Error::eFatal));
                    throw e;
               }
                mOutQ.enqueue(EngineStatusObject(e));
          }
     }
}

/**
 * \brief Ends current simulation (if any) and creates a new one base
 * on the provided DataObject.
 *
 * \param simulation The DataObject to create the simulation from.
 * \param creator The id of the Session creating the simulation.
 */
void Engine::createSimulation(DataObject* simulation, int64_t creator)
{
     Lock bufLock(mBuf.mutex());
     endSimulation();
     mBuf.put(simulation);
     mSimulation = dynamic_cast<Simulation*>(SOFactory::createSimulationObject(*simulation, creator));
     bufLock.unlock();
     mInitialized = true;
}

/**
 * \brief Sends a command to the Engine.
 *
 * \param msg The command to send according to the eEngMsg
 * enumeration.
 */
void Engine::put(enum eEngMsg msg)
{
     if (msg != eEngNoMsg) { 
          mQ.enqueue(msg);
     }
}

/**
 * \brief Notifies the Engine that a passive client is interested in
 * getting data for a specified time step.
 *
 * Called by Session objects representing passive clients when
 * receiving a StepMessage. The simulation time asked for may or may
 * not have passed.
 *
 * \param t The simulation time the passive client is interested in
 * getting data for.
 * \param q The TSQueue the Engine should use when notifying the
 * passive client that the data is available.
 * \return A UniqueTime that may be used later in order to deregister
 * interest in this time.
 */
UniqueTime Engine::registerInterestInTime(Time t, TSQueue<EngineStatusObject>* q)
{
     UniqueTime ut(t);
     if (mBuf.simTime() >= t) {
          q->enqueue(EngineStatusObject());
          debug("Time " << t << " has already past");
     }
     else {
          Lock lock(mutex());
          mTimeListeners[ut] = q;
          lock.unlock();
//          debug("Registering interest in time " << ut.time() - mBuf.simulationData().mStartTime);
     }
     return ut;
}

/**
 * \brief Notifies the Engine that a passive client is no longer
 * interested in getting data for a specified time step.
 *
 * Called by Session objects representing passive clients when using a
 * polling strategy for detecting if any new time steps have been
 * calculated.
 *
 * \param ut The UniqueTime received when registering interest in the
 * time we now want to deregister interest in.
 */
void Engine::deregisterInterestInTime(UniqueTime ut)
{
     Lock lock(mutex());
     mTimeListeners.erase(ut);
     lock.unlock();
}


/**
 * \brief Starts the specified engine.
 *
 * Used as start function when creating the engine thread.
 *
 * \param engineToStart The Engine instance to start.
 * \return Null if the tread exited successfully, undefined otherwise.
 */
void *Engine::start(void* engineToStart)
{
     Engine *e = static_cast<Engine*>(engineToStart);
     e->run();
     return NULL;
}

