// System
#include <iostream>

// Own
#include "debugheader.h"
#include "GoodStuff.h"
#include "SocketException.h"
#include "ApproxsimSocket.h"


using namespace std;


static const int one = 1;
bool bigEndian = (!(*(char*)&one));


/**
 * \brief Creates a socket that connects to the specified host and
 * port.
 *
 * \param host The host to connect to.
 * \param port The port to connect to.
 */
ApproxsimSocket::ApproxsimSocket(std::string host, int port)
     : Socket(), mId(-1), mLength(0), mReceivedHeader(false)
{
     if (!Socket::create()) {
          throw SocketException("Could not create client socket.");
     }
     
     if (!Socket::connect(host, port)) {
          throw SocketException("Could not bind to port.");
     }
}

/**
 * \brief Sends a approxsim message.
 *
 * \param msg The message to send.
 * \return True if all is ok.
 */
bool ApproxsimSocket::sendApproxsimMessage(const std::string msg) const
{
     int64_t len = msg.size();
     int64_t id  = mId;
     if (!bigEndian) {
          ByteSwap(len);
          ByteSwap(id);

     }

     if (!send(&len, 8)) {
          return false; 
     }
     if (!send(&id, 8)) {
          return false; 
     }
     return send(msg.c_str(), msg.size());
}

/**
 * \brief Receives a ApproxsimHeader.
 *
 * \return The id contained in the ApproxsimHeader.
 */
int64_t ApproxsimSocket::recvApproxsimHeader()
{
     int status = 0;
     status = recvf(&mLength, 8);
     if (status) {
          if (!bigEndian) {
               ByteSwap(mLength);
          }
     }
     else if (status == 0) {
          throw ConnectionClosedException();
     }
     else {
          throw SocketException("Could not read 'length' from socket.");
     }

     status = recvf(&mId, 8);
     if (status) {
          if (!bigEndian) {
               ByteSwap(mId);
          }
     }
     else {
          throw SocketException("Could not read 'id' from socket.");
     }

     mReceivedHeader = true;
     return mId;
}

/**
 * \brief Receives a ApproxsimMessage.
 *
 * \return The number of bytes in the received message.
 */
int ApproxsimSocket::recvApproxsimMessage(std::string &outMsg)
{
     int status = 0;

     if (!mReceivedHeader) {
          recvApproxsimHeader();
     }

     char *buf  = new char[mLength + 1];
     if (!buf) {
          throw SocketException("To large message or invalid ApproxsimMessageHeader.");
     }
     status = recvf(buf, mLength);
     if (status < 0) {
          outMsg = "";
          delete [] buf;
          throw SocketException("Could not read 'message' from socket.");
     }
     else {
          buf[mLength] = '\0';
          outMsg = buf;
          delete [] buf;
     }

     mReceivedHeader = false;
     return status;
}
