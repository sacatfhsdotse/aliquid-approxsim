#ifndef _STRATMASSOCKET_H
#define _STRATMASSOCKET_H


// Own
#include "Socket.h"
#include "stdint.h"

/**
 * \brief Socket user for sending and receiving StratmasMessages.
 *
 * \author Per Alexius
 * \date   $Date: 2006/07/03 14:18:23 $
 */
class StratmasSocket : public Socket {
private:
     /// The id of this socket.
     int64_t mId;

     /**
      * \brief Contains the length of a message read from the
      * StratmasHeader.
      */
     int64_t mLength;

     /**
      * \brief True if we have received a StratmasHeader but not the
      * message the header refers to.
      */
     bool mReceivedHeader;

public:
     /// Default constructor.
     inline StratmasSocket() : Socket(), mId(-1), mLength(0), mReceivedHeader(false) {}
     StratmasSocket(std::string host, int port);

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
     bool sendStratmasMessage(const std::string msg) const;
     int64_t recvStratmasHeader();
     int recvStratmasMessage(std::string &outMsg);
};


#endif   // _STRATMASSERVERSOCKET_H
