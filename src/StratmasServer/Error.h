#ifndef STRATMAS_ERROR_H
#define STRATMAS_ERROR_H

// System
#include <sstream>

// Forward Declarations
class Reference;


/**
 * \brief This class represents an error that the server has found and
 * that is fatal for the currently ongoing simulation. If a client
 * receives an Error the currently ongoing simulation should not be
 * trusted.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/06 12:55:09 $
 */
class Error
{
private:
     /// The type of the Error according to the eType enumeration.
     int mType;

     /// A message describing the error.
     std::ostringstream mMsg;
     
public:
     /**
      * \brief Enumeration for Error types.
      */
     enum eType { eWarning, eGeneral, eFatal }; 

     /**
      * \brief Default constructor that creates an Error with an empty
      * message;
      *
      * \param type The type of Error to create.
      */
     Error(int type = eGeneral) : mType(type), mMsg("") {}

     /**
      * \brief Consructor that creates an Error with the specified
      * message.
      *
      * \param s The message describing the error.
      * \param type The type of Error to create.
      */
     Error(const char* s, int type = eGeneral) : mType(type), mMsg(s) {}

     /**
      * \brief Copy consructor.
      *
      * \param e The Error to copy.
      */
     Error(const Error& e) : mType(e.mType), mMsg(e.mMsg.str()) {}
     
     /**
      * \brief Assignment operator
      *
      * \param e The Error which value to assign to this Error.
      * \return This Error with its new value.
      */
     Error& operator = (const Error& e) { mType = e.mType; mMsg.str(e.mMsg.str()); return *this; }

     /**
      * \brief Accessor for the Error type.
      *
      * \return The type of this error.
      */
     int type() const { return mType; }

     std::string typeStr() const;
     
     /**
      * \brief Writes the provided object to this Error's message
      * stream.
      *
      * \param t The object to write.
      * \return A reference to this Error.
      */
     template<class T> Error& operator << (T t) { mMsg << t; return *this; }

     Error& operator << (const Reference& ref);

     void toXML(std::ostream& o) const;

     // Friends
     friend std::ostream& operator << (std::ostream &o, const Error& e);
};



#endif   // STRATMAS_ERROR_H
