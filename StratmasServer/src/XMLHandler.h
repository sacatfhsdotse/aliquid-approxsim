#ifndef STRATMAS_XMLHANDLER_H
#define STRATMAS_XMLHANDLER_H


// System
#include <map>
#include <string>

// Xerces
#include <xercesc/util/XercesDefs.hpp>

// Own

// Forward Declarations
class Buffer;   // For passing Buffer reference on to subscriptions
class DataObject;
class ParserErrorReporter;
class PVInitValueSet;
class Subscription;
class Update;

namespace XERCES_CPP_NAMESPACE {
     class DOMElement;
     class XercesDOMParser;
     class XMLEntityResolver;
}


XERCES_CPP_NAMESPACE_USE


/**
 * \brief Enumeration for the different kinds of StratmasMessages.
 */
enum eStratmasMsgType {
     eConnect,
     eDisconnect,
     eInitialization,
     eServerCapabilities,
     eGetGrid,
     eRegisterForUpdates,
     eSubscription,
     eStep,
     eUpdateServer,
     eReset,
     eProgressQuery,
     eSetProperty,
     eLoadQuery,
     eUnknown
};


/**
 * \brief This class handles the extraction of data from
 * StratmasMessages.
 *
 * \author   Per Alexius
 * \date     $Date: 2007/01/24 13:13:26 $
 */
class XMLHandler {
private:
     /// Id of the Session this XMLHandler belongs to.
     int64_t mId;

     /// Resolver for parser resource requests.
     XMLEntityResolver *mpEntityResolver;
     
     /// Pointer to the simulation DataObject.
     DataObject* mSimulation;

     /// The PVInitValueSet
     PVInitValueSet* mPVInitValueSet;

     /// Reference to the Buffer object.
     Buffer& mBuf;

     /**     
      * \brief Indicates the byte order of the client sending the
      * messages to be handled
      */
     bool mSessionBigEndian;

     /**     
      * \brief Indicates if the client sending the messages is
      * registered for updates.
      */
     bool mRegisteredForUpdates;

     /**     
      * \brief Maps subscription id to the actual subscription. Notice
      * that subscriptions are not stored anyplace else than in the
      * XMLHandler.
      */
     std::map<int, Subscription*> mSubscriptions;

     /**
      * \brief The number of timesteps to take before returning any
      * data (extracted from the last handled StepMessage).
      */
     int mNumberOfTimesteps;

     /**
      * \brief True if the last handled StepMessage was a 'detached'
      * step message.
      */
     bool mDetachedStep;

     /// A vector containing the Updates from the last message.
     std::vector<Update*> mUpdates;

     /// Type of the last StratmasMessage handled.
     std::string mLastType;

     /// The parser used to parse incoming messages.
     XercesDOMParser* mParser;

     /// The error reporter used during parsing.
     ParserErrorReporter* mErrorReporter;

     void addSubscription(Subscription *sub);
     void handleConnectMessage(DOMElement &n);
     void handleRegisterForUpdatesMessage(DOMElement &n);
     void handleStepMessage(DOMElement &n);
     void handleServerUpdateMessage(DOMElement &n);
     void handleSubscriptionMessage(DOMElement &n);
     void handleSetPropertyMessage(DOMElement &n);

public:
     XMLHandler(Buffer &buf, std::string ns, std::string schemaLocation, int64_t id);
     ~XMLHandler();

     /**
      * \brief Removes and returns the simulation DataObject.
      *
      * \return The simulation DataObject.
      */
     DataObject* takeOverSimulation() { 
          DataObject* d = mSimulation; mSimulation = 0; return d;
     }

     /**
      * \brief Removes and returns the updates vector.
      *
      * \return The updates vector.
      */
     std::vector<Update*> takeOverUpdates() {
          std::vector<Update*> tmp = mUpdates;
          mUpdates.clear();
          return tmp;
     }

     /**
      * \brief Accessor for the client byte order indicator.
      *
      * \return True if the client whose messages we handle has big
      * endian byte order.
      */
     bool sessionBigEndian() const { return mSessionBigEndian; }

     /**
      * \brief Accessor for the flag indicating if the client is
      * registered for updates.
      *
      * \return True if the client whose messages we handle is
      * registered for updates.
      */
     bool registeredForUpdatesFlag() const { return mRegisteredForUpdates; }

     /**
      * \brief Accessor for the type of the last message handled.
      *
      * \return The type of the last message handled.
      */
     const std::string &lastType() const { return mLastType; }

     /**
      * \brief Accessor for the number of timesteps in the last
      * handled StepMessage.
      *
      * \return The number of timesteps in the last handled
      * StepMessage.
      */
     int numberOfTimesteps() const { return mNumberOfTimesteps; }

     /**
      * \brief Accessor for the detached step flag.
      *
      * \return The status of the detached step flag.
      */
     bool detachedStep() const { return mDetachedStep; }

     void getSubscriptions(UpdateMessage& um) const;
     void getGridBasedSubscriptions(UpdateMessage& um) const;


     int handle(const std::string &xml);
     void createSubscription(DOMElement &n);
     void eraseSubscriptions();

};

#endif   // STRATMAS_XMLHANDLER_H
