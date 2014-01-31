// System
#include <sstream>
#include <string>
#include <boost/thread.hpp>

// Own
#include "ChangeTrackerAdapter.h"
#include "debugheader.h"
#include "Environment.h"
#include "Server.h"
#include "Session.h"
#include "Simulation.h"   // Only for timeStep(). Should perhaps be removed...
#include "SocketException.h"
#include "StratmasMessage.h"
#include "StratmasSocket.h"
#include "Subscription.h"
#include "XMLHandler.h"
#include "stdint.h"
#include "LogStream.h"

// Temporary
#include <fstream>
#include "stopwatch.h"

using namespace std;


/**
 * \brief Creates a Session object.
 *
 * \param parent The Server that created this object.
 * \param e A reference to the Engine object.
 * \param b A reference to the Buffer object.
 * \param isActive Indicates if this client is active ro not.
 * \param s A pointer to the Socket to use for communication.
 */
Session::Session(Server &parent, Engine &e, Buffer &b, bool isActive, StratmasSocket *s)
     : mServer(parent),
       mEng(e),
       mBuf(b),
       mId(s->id()),
       mActive(isActive),
       mDisconnect(false),
       mSocket(s),
       mXMLHandler(new XMLHandler(b,
				  Environment::DEFAULT_SCHEMA_NAMESPACE,
				  Environment::STRATMAS_PROTOCOL_SCHEMA,
				  mId)),
       mBufferResetCount(mBuf.resetCount()),
       mChangeTracker(new ContainerChangeTrackerAdapter(Reference::get(Reference::root(), "identifiables"), mId)),
       mRegisteredForUpdates(false)
{
}

/**
 * \brief Destructor.
 */
Session::~Session()
{
     if (mXMLHandler) { delete mXMLHandler; }
     if (mChangeTracker) { delete mChangeTracker; }
}

/**
 * \brief Closes this session.
 *
 * Sets the disconnect flag to true and if this is an active client
 * session - tells the Engine to end the current Scenario.
 */
void Session::closeSession()
{
     cout << "Closing Session." << endl;

     mDisconnect = true;

     // We must lock here so there won't be a gap between when the
     // scenario is destroyed and the active client is deregisterd.
     Lock lock(mutex());
     if (isActive()) {
	  mEng.put(eEngEndScenario);
	  mEng.wait();
     }
     mServer.notifyClosure(id());
     lock.unlock();
}
 
/**
 * \brief Takes a StratmasMessage, handles it and produces a
 * response.
 *
 * \param xml The message to handle.
 * \param response The message to send as response.
 */
void Session::handleStratmasMessage(const std::string &xml, std::string &response)
{
     ostringstream ost;
     ost.precision(24);

     StopWatch s;
     try {
	  switch (mXMLHandler->handle(xml)) {
	  case eConnect: {
	       // We must lock since we want to be sure that we are
	       // not in a state where the scenario has been destroyed
	       // but the active client has not yet been deregistered.
	       Lock lock(mutex());
	       if (!mActive) {
		    if (mEng.initialized()) {
			 mChangeTracker->objectAdded(mBuf.simulation()->ref(), -1);
		    }
		    else  {
			 Error e("Tried to connect passively to an uninitialized simulation.");
			 throw e;
		    }
	       }
	       lock.unlock();
	       mBigEndian = mXMLHandler->sessionBigEndian();
	       ConnectResponseMessage msg(mActive);
	       msg.toXML(ost);
	       break;
	  }
	  case eDisconnect: {
	       closeSession();
	       StatusMessage msg(mXMLHandler->lastType());
	       msg.toXML(ost);
	       break;
	  }
	  case eServerCapabilities: {
	       ServerCapabilitiesResponseMessage msg;
	       msg.toXML(ost);
	       break;
	  }
	  case eGetGrid: {
	       GetGridResponseMessage msg(mBuf, mBigEndian);
	       msg.toXML(ost);
	       break;
	  }
	  case eRegisterForUpdates: {
	       mRegisteredForUpdates = mXMLHandler->registeredForUpdatesFlag();
	       UpdateMessage msg(mBuf, *mChangeTracker, mRegisteredForUpdates);
	       mLastSentTime = msg.validForTime();
	       msg.toXML(ost);
	       break;
	  }
	  case eInitialization: {
	       handleInitialization();
	       StatusMessage msg(mXMLHandler->lastType());
	       msg.toXML(ost);
	       break;
	  }
	  case eSubscription: {
	       UpdateMessage msg(mBuf, *mChangeTracker, mRegisteredForUpdates);
	       mXMLHandler->getSubscriptions(msg);
	       msg.toXML(ost);
	       break;
	  }
	  case eStep: {
	       if (mEng.initialized()) {
		    if (mActive) {
			 mEng.setNumberOfTimesteps(mXMLHandler->numberOfTimesteps());
			 mEng.put(eEngStep);
			 if (mXMLHandler->detachedStep()) {
			      StatusMessage msg(mXMLHandler->lastType());
			      msg.toXML(ost);
			 }
			 else {
			      EngineStatusObject o = mEng.wait();
			      if (o.errorOccurred()) {
				   throw o.errors();
			      }
			      s.start();
			      UpdateMessage msg(mBuf, *mChangeTracker, mRegisteredForUpdates);
			      mXMLHandler->getSubscriptions(msg);
			      msg.toXML(ost);
			      s.stop();
			      debug("Production of UpdateMessage took " << s.secs() << " seconds");
//			      mLastSentTime = msg.validForTime();
			 }
		    }
		    else {
			 debug("lasttime: " << mLastSentTime.milliSeconds());
			 debug("num ts  : " << mXMLHandler->numberOfTimesteps());
			 debug("timestep: " << Simulation::timestep().milliSeconds());
			 Time nextTime = Time(0, 0, 0, 0, mLastSentTime.milliSeconds() +
					      mXMLHandler->numberOfTimesteps() *
					      Simulation::timestep().milliSeconds());

			 UniqueTime ut = mEng.registerInterestInTime(nextTime, &mQueue);
			 for (int i = 0; i < 40 && mQueue.size() == 0; i++) {
			      Environment::milliSleep(50);
			 }
			 if (mQueue.size() == 0) {
			      mEng.deregisterInterestInTime(ut);
			 }
			 else {
			      EngineStatusObject o = mQueue.dequeue();
			      if (o.errorOccurred()) {
				   throw o.errors();
			      }
			 }
			 UpdateMessage msg(mBuf, *mChangeTracker, mRegisteredForUpdates);
			 mXMLHandler->getSubscriptions(msg);
			 msg.toXML(ost);
			 mLastSentTime = msg.validForTime();
		    }
	       }
	       else {
		    Error e("Tried to step scenario that isn't initialized.");
		    throw e;
	       }
	       break;
	  }
	  case eUpdateServer: {
	       if (mActive && mEng.initialized()) {
 		    mBuf.put(mXMLHandler->takeOverUpdates());
 		    mEng.put(eEngUpdate);
 		    EngineStatusObject o = mEng.wait();
 		    if (o.errorOccurred()) {
 			 throw o.errors();
 		    }
	       }
	       else {
		    Error e(Error::eWarning);
		    e << "Tried to update "
		      << (mActive ? "uninitialized scenario." : "scenario from passive client.");
		    throw e;
	       }
 	       UpdateMessage msg(mBuf, *mChangeTracker, mRegisteredForUpdates);
	       mXMLHandler->getSubscriptions(msg);
 	       msg.toXML(ost);
	       break;
	  }
	  case eReset: {
	       if (mActive) {
		    mEng.put(eEngReset);
		    EngineStatusObject o = mEng.wait();
		    if (o.errorOccurred()) {
			 throw o.errors();
		    }
//  		    mXMLHandler->eraseSubscriptions();
		    UpdateMessage msg(mBuf, *mChangeTracker, mRegisteredForUpdates);
// 		    StatusMessage msg(mXMLHandler->lastType());
		    msg.toXML(ost);
	       }
	       else {
		    Error e(Error::eWarning);
		    e << "Tried to reset simulation from a passive client.";
		    throw e;
	       }
	       break;
	  }
	  case eProgressQuery: {
	       ProgressQueryResponseMessage msg(mBuf);
	       msg.toXML(ost);
	       break;
	  }
	  case eSetProperty: {
	       StatusMessage msg(mXMLHandler->lastType());
	       msg.toXML(ost);
	       break;
	  }
	  case eLoadQuery: {
	       LoadQueryResponseMessage msg(mServer);
	       msg.toXML(ost);
	       break;
	  }
	  default:
	       slog << "This is impossible. Please sit down and have a drink." << logEnd;
	       break;
	  }
     }
     catch (Error& e) {
	  slog << "Session " << id() << " caught Error: '" << e << "'" << logEnd;
	  ost.str("");
	  StatusMessage msg(mXMLHandler->lastType());
	  msg.addError(e);
	  msg.toXML(ost);
	  if (!isActive()) { closeSession(); }
     }
     catch (vector<Error>& e) {
	  slog << "Session " << id() << " caught Errors:" << "---" << logEnd;
	  ost.str("");
	  StatusMessage msg(mXMLHandler->lastType());
	  for(vector<Error>::iterator it = e.begin(); it != e.end(); it++) {
	       slog << *it << logEnd;
	       msg.addError(*it);
	  }
	  msg.toXML(ost);
	  if (!isActive()) { closeSession(); }
     }

     response = ost.str();
}

/**
 * \brief Handles initialization.
 */
void Session::handleInitialization()
{
     if (mActive) {
	  mXMLHandler->eraseSubscriptions();
	  mEng.createSimulation(mXMLHandler->takeOverSimulation(), id());
	  mEng.put(eEngInitSimulation);
	  EngineStatusObject o = mEng.wait();
	  if (o.errorOccurred()) {
	       throw o.errors();
	  }
     }	       
     else {
	  Error e("Tried to initialize simulation from a passive client");
	  throw e;
     }
}

/**
 * \brief Starts the Session.
 */
void Session::start()
{
     debug("Comm thread created");

     float totSecs = 0;
     int totRecvBytes = 0;
     int totProdBytes = 0;
     try {
	  std::string xml;
	  std::string response;
	  StopWatch s;
	  while (!mDisconnect) {
	       mSocket->recvStratmasMessage(xml);
	       s.start();
	       handleStratmasMessage(xml, response);
	       mSocket->sendStratmasMessage(response);
	       s.stop();
	       totSecs      += s.secs();
	       totRecvBytes += xml.length();
	       totProdBytes += response.length();
//  	       debug("Took " << s.secs() << " seconds to handle " 
// 		     << xml.length() << " bytes msg and produce and send a " 
// 		     << response.length() << " bytes response.");
//  	       debug("Tot: Recv: " << totRecvBytes << ", prod: " 
// 		     << totProdBytes << ", secs: " << totSecs);
	  }
     }
     catch (ConnectionClosedException &e) {
	  slog << "Connection closed by client" << logEnd;
	  closeSession();
     }
     catch (SocketException &e) {
	  slog << "Exception was caught:" << e.description() << logEnd;
	  closeSession();
     }
     catch (std::exception &e) {
          slog << "Exception was caught:" <<  e.what() << logEnd;
 	  closeSession();
     }
     catch (...) {
 	  slog << "Exception was caught when receiving or sending a StratmasMessage" << logEnd;
 	  closeSession();
     }

     delete mSocket;
     mSocket = 0;

     debug("Session with id " << mId << " run by thread "
	   << (mDisconnect ? "ending" : "pausing"));
}

/**
 * \brief Main method of threads handling a Session.
 *
 * \param instance An instance of the Session class that this thread
 * should handle.
 *
 * \return NULL if everything is ok.
 */
void *Session::staticStart(void *instance)
{
     Session *s = static_cast<Session*>(instance);
     s->start();
     delete s;
     return 0;
}

/**
 * \brief Sets the socket this Session should use for communication.
 *
 * \param s A pointer to the socket to use for communication.
 */
bool Session::setSocket(StratmasSocket *s)
{
     bool ret;
     Lock lock(mutex());
     if (mSocket) {
	  ret = false;
     }
     else {
	  mSocket = s;
	  ret = true;
     }
     lock.unlock();
     return ret;
}

string Session::simulationName() const
{
     return mBuf.simulationName();
}
