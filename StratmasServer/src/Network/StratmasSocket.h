#ifndef _APPROXSIMSOCKET_H
#define _APPROXSIMSOCKET_H


// Own
#include "Socket.h"
#include "stdint.h"

/**
 * \brief Socket user for sending and receiving ApproxsimMessages.
 *
 * \author Per Alexius
 * \date   $Date: 2006/07/03 14:18:23 $
 */
class ApproxsimSocket : public Socket {
private:
     /// The id of this socket.
     int64_t mId;

     /**
      * \brief Contains the length of a message read from the
      * ApproxsimHeader.
      */
     int64_t mLength;

     /**
      * \brief True if we have received a ApproxsimHeader but not the
      * message the header refers to.
      */
     bool mReceivedHeader;

public:
     /// Default constructor.
     inline ApproxsimSocket() : Socket(), mId(-1), mLength(0), mReceivedHeader(false) {}
     ApproxsimSocket(std::string host, int port);

     /**
      * \brief Accessor for the id.
      *
      * \return The id of this Socket.
      */
     inline int64_t id() const { return mId; }

     /**
      * \brief Mutator for the id.
      *
      * \param id The new id of this Socket.
      */
     inline void id(int64_t id) { mId = id; }

     // Data Transimission
     bool sendApproxsimMessage(const std::string msg) const;
     int64_t recvApproxsimHeader();
     int recvApproxsimMessage(std::string &outMsg);
};


#endif   // _APPROXSIMSERVERSOCKET_H
