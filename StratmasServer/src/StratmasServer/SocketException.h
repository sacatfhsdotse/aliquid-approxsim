#ifndef _STRATMASSOCKETEXCEPTION_H
#define _STRATMASSOCKETEXCEPTION_H


#include <string>


/**
 * \brief Exception used by Socket class.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/21 13:35:29 $
 */
class SocketException
{
private:
     /// The error message.
     std::string mStr;

public:
     /**
      * \brief Creates a SocketException with the specified message.
      *
      * \param s The message.
      */
     inline SocketException(std::string s) : mStr ( s ) {};

     /**
      * \brief Destructor.
      */
     inline ~SocketException() {};

     /**
      * \brief Accessor for the message.
      *
      * \return The message.
      */
     inline const std::string description() { return mStr; }

     /**
      * \brief Complementary accessor for the message.
      *
      * \return The message.
      */
     inline const std::string what() { return mStr; }
};

/**
 * \brief Exception used by Socket class when connection is closed by
 * client.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/21 13:35:29 $
 */
class ConnectionClosedException : public SocketException {
public:
     /**
      * \brief Constructor.
      */
     inline ConnectionClosedException() : SocketException("Connection closed") {}
};

#endif   // _STRATMASSOCKETEXCEPTION_H
