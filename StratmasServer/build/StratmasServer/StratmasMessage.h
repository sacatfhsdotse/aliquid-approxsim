#ifndef STRATMAS_MESSAGE_H
#define STRATMAS_MESSAGE_H

// System
#include <iosfwd>
#include <string>
#include <vector>

// Own
#include "Error.h"
#include "Time.h"

// Forward Declarations
class Buffer;
class ChangeTrackerAdapter;
class Server;
class Subscription;

/**
 * \brief Abstract base class for all types of StratmasMessage.
 *
 * This class also contains some helpers for creating XML
 * representations of StratmasMessages.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/06 14:23:12 $
 */
class StratmasMessage {
protected:
     virtual void openMessage(std::ostream &o, const std::string &type) const;
     virtual void closeMessage(std::ostream &o) const;
public:
     /// Destructor.
     virtual ~StratmasMessage() {}

     /**
      * \brief Produces the XML representation of a message. Must be
      * overridden by all subclasses
      *
      * \param o The stream to which the message is written
      */
     virtual void toXML(std::ostream &o) const = 0;
};



/**
 * \brief Class representing the ConnectResponseMessage.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/06 14:23:12 $
 */
class ConnectResponseMessage : public StratmasMessage {
private:
     /// True if this message is sent to an active client.
     bool mActive;
public:
     /**
      * \brief Constructor.
      *
      * \param active Indicates whether the client receiving the
      * message is active or not.
      */
     ConnectResponseMessage(bool active) : mActive(active) {}
     void toXML(std::ostream &o) const;
};



/**
 * \brief Class representing the ServerCapabilitiesResponseMessage.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/06 14:23:12 $
 */
class ServerCapabilitiesResponseMessage : public StratmasMessage {
private:

public:
     /// Constructor
     ServerCapabilitiesResponseMessage() {}
     void toXML(std::ostream &o) const;
};



/**
 * \brief Class representing the GetGridResponseMessage.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/06 14:23:12 $
 */
class GetGridResponseMessage : public StratmasMessage {
private:
     /// The Buffer to fetch data from.
     const Buffer &mBuf;

     /// Indicates the byte order of the client receiving the message.
     bool mSessionBigEndian;

public:
     /**
      * \brief Constructor.
      *
      * \param buf The Buffer to fetch data from, if necessary.
      * \param bigEndian Indicates the byte order of the client
      * receiving the message
      */
     GetGridResponseMessage(const Buffer &buf, bool bigEndian)
	  : mBuf(buf), mSessionBigEndian(bigEndian) {}
     void toXML(std::ostream &o) const;
};



/**
 * \brief Class representing the StatusMessage.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/06 14:23:12 $
 */
class StatusMessage : public StratmasMessage {
private:
     /// String holding the type of message this message is a response to.
     const std::string mType;

     /**
      * \brief Vector containing all errors that should be reported in
      * this status message.
      */
     std::vector<Error> mErrors;

public:
     /**
      * \brief Constructs a StatusMessage with the specified type.
      *
      * \param type The type of the StatusMessage to create.
      */
     StatusMessage(const std::string &type) : mType(type) {}

     /**
      * \brief Adds an Error to this message.
      *
      * \param e The Error to be added
      */
     void addError(const Error& e) { mErrors.push_back(e); }

     void toXML(std::ostream &o) const;
};



/**
 * \brief Class representing the UpdateMessage.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/06 14:23:12 $
 */
class UpdateMessage : public StratmasMessage {
private:
     Buffer& mBuf;   ///< The Buffer to fetch data from.
     ChangeTrackerAdapter& mChangeTracker;   ///< The ChangeTracker.
     
     /**
      * \brief True if the client receiving this message is registered
      * for updates.
      */
     bool mRegisteredForUpdates;

     /**
      * \brief Vector containing pointers to all Subscriptions which
      * data should be sent with this message.
      */
     std::vector<Subscription*> mSubscriptions;

     /**
      * \brief The simulation time for which the data in the last
      * produced XML representation is valid.
      */
     mutable Time mValidForTime;
public:
     /**
      * \brief Constructor.
      *
      * \param b The Buffer.
      * \param c The ChangeTrackerAdapter.
      * \param r Indicates if the client is registered for updates.
      */
     UpdateMessage(Buffer& b, ChangeTrackerAdapter& c, bool r)
	  : mBuf(b), mChangeTracker(c), mRegisteredForUpdates(r) {}

     /**
      * \brief Accessor for the simulation time for which the data in
      * the last produced XML representation is valid.
      *
      * \return The simulation time for which the data in the last
      * produced XML representation is valid.
      */
     Time validForTime() const { return mValidForTime; }

     /**
      * \brief Adds a Subscription to this message.
      *
      * \param s The Subscription to be added
      */
     void addSubscription(Subscription *s) { mSubscriptions.push_back(s); }

     void toXML(std::ostream &o) const;
};



/**
 * \brief Class representing the ProgressQueryResponseMessage.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/06 14:23:12 $
 */
class ProgressQueryResponseMessage : public StratmasMessage {
private:
     Buffer &mBuf;   ///< The Buffer from which data should be fetched.
public:
     /**
      * \brief Creates a ProgressQueryResponseMessage that fetches its
      * data from the specified Buffer.
      *
      * \param b The Buffer to fetch data from.
      */
     ProgressQueryResponseMessage(Buffer& b) : mBuf(b) {}

     /// Destructor.
     virtual ~ProgressQueryResponseMessage() {}
     void toXML(std::ostream &o) const;
};



/**
 * \brief Class representing the LoadQueryResponseMessage.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/06 14:23:12 $
 */
class LoadQueryResponseMessage : public StratmasMessage {
private:
     /// The Server from which information should be fetched.
     const Server& mServer;
public:
     /**
      * \brief Creates a LoadQueryResponseMessage that fetches its
      * information from the specified Server.
      *
      * \param s The Server to fetch information from.
      */
     LoadQueryResponseMessage(const Server &s) : mServer(s) {}
     void toXML(std::ostream &o) const;
};

#endif   // STRATMAS_MESSAGE_H
