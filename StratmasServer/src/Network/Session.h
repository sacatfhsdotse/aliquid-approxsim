#ifndef _STRATMAS_SESSION_H
#define _STRATMAS_SESSION_H

// System

// Own
#include "stdint.h"
#include "Lockable.h"
#include "Time2.h"
#include "TSQueue.h"

// Forward declarations
class Buffer;
class ContainerChangeTrackerAdapter;
class Engine;
class EngineStatusObject;
class Reference;
class Server;
class StratmasSocket;
class XMLHandler;


/**
 * \brief Class that handles a session between the Server and a
 * client.
 *
 * When the Server receives a ConnectMessage from a client a Session
 * is created to handle further connumication from that client
 * regarding that specific Session. The Session then lives until
 * that client sends a DisconnectMessage.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/24 10:14:36 $
 */
class Session : public Lockable {
private:
      /// The Server object that created this Session
     Server& mServer;
     /// Reference to the Engine object.
     Engine& mEng;
     /// Reference to the Buffer object.
     Buffer& mBuf;
     /// Id of this Session.
     int64_t mId;
     /// Indicates if this Session is with is an active client or not.
     bool mActive;
     /// Endian of the architecture of the client.
     bool mBigEndian;
     /// Set to true when we should disconnect.
     bool mDisconnect;
     /// The socket over which to communicate.
     StratmasSocket* mSocket;
     /// The XMLHandler Object that handles parsing of xml messages.
     XMLHandler* mXMLHandler;
     /// Keeps the last simulation time for which data was sent to the client.
     Time mLastSentTime;
     /// Queue used by the Engine thread to communicate with us.
     TSQueue<EngineStatusObject>   mQueue;
     /// Keep track of the buffer's reset count.
     int mBufferResetCount;
     /// The ChangeTrackerAdapter that keeps track of changes in the Simulation.
     ContainerChangeTrackerAdapter* mChangeTracker;
     /// Status flag indicating if the client has registered for updates.
     bool mRegisteredForUpdates;

public:
     Session(Server &parent, Engine &e, Buffer &b, bool isMaster, StratmasSocket *s);
     ~Session();

     void closeSession();

     void handleStratmasMessage(const std::string &xml, std::string &response);
     void handleInitialization();

     void start();
     static void *staticStart(void *instance);
     
     bool setSocket(StratmasSocket *s);

     /**
      * \brief Accessors for the id.
      *
      * \return The id of this Session.
      */
     inline int64_t id() const { return mId; }

     /**
      * \brief Accessors for the active flag.
      *
      * \return The state of the active flag.
      */
     inline bool isActive() const { return mActive; }

     /**
      * \brief Returns the name of the simulation this session
      * handles.
      *
      * \return The name of the simulation this session handles.
      */
     std::string simulationName() const;
     
};

#endif   // _STRATMAS_SESSION_H
