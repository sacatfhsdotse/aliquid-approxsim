// $Id: LogStream.h,v 1.2 2006/07/25 14:52:00 dah Exp $
#ifndef STRATMAS_LOGSTREAM_H
#define STRATMAS_LOGSTREAM_H

// System
#include <sstream>
#include <iostream>
#include <cstdlib>

// Own
#include "Lockable.h"

// Forward declarations
class LogStream;
class StdLogStream;
class LogEnd;

// Debug references.
extern LogStream slog;
extern LogStream debug;
extern LogEnd logEnd;

/**
 * \brief Placeholder class used to mark the end of a log message.
 *
 * \author   Daniel Ahlin
 * \date     $Date: 2006/07/25 14:52:00 $
 */
class LogEnd
{
public:
     LogEnd() {};
};

/**
 * \brief This class represents a log message.
 *
 * \author   Daniel Ahlin
 * \date     $Date: 2006/07/25 14:52:00 $
 */
class LogMessage
{
private:
     /// A message describing the error.
     std::ostringstream mMessage;

     /// The log this message was created to be logged in. By being
     /// set to null, logStream also serves to flag that the message
     /// is already sent and that no more appending to this message is
     /// allowed.
     LogStream* mLogStream;

public:
     ~LogMessage();

     /**
      * \brief Creates a new log message.
      *
      * \param t The object to write.
      * \return A reference to this Error.
      */
     LogMessage(LogStream* logStream) : mLogStream(logStream) {}

     /**
      * \brief Writes the provided object to this Message
      *
      * \param t The object to write.
      * \return A reference to this message.
      */
     template<class T> LogMessage& operator << (T t) 
     { 
          mMessage << t; 
          return *this;  
     }

     const std::string getMessage() const 
     {
          return mMessage.str();
     }; 

     LogMessage& operator << (const LogEnd& end);
};

/**
 * \brief This class represents capabilities of a log sink.
 *
 * \author   Daniel Ahlin
 * \date     $Date: 2006/07/25 14:52:00 $
 */
class LogSink
{
private:
public:
     virtual ~LogSink() {};
     /**
      * \brief Posts the provided message to the log stream
      *
      */
     virtual void sink(const LogMessage* const message) = 0;
};

/**
 * \brief This class implements LogSink using std::cerr for output.
 *
 * \author   Daniel Ahlin
 * \date     $Date: 2006/07/25 14:52:00 $
 */
class StdLogSink : public Lockable, public LogSink
{
private:
     virtual const std::string getTimeStamp() const;
public:
     virtual ~StdLogSink() {};
     /**
      * \brief Posts the provided message to the log stream
      *
      */
     virtual void sink(const LogMessage* const message)
     {
          Lock lock(mutex());
          std::cerr << getTimeStamp() << ": " << 
               message->getMessage() << std::endl;
          lock.unlock();
     }
};

/**
 * \brief This class implements LogSink, suppresing output.
 *
 * \author   Daniel Ahlin
 * \date     $Date: 2006/07/25 14:52:00 $
 */
class NullLogSink : public LogSink
{
private:
public:
     virtual ~NullLogSink() {};
     /**
      * \brief Posts the provided message to the log stream
      *
      */
     virtual void sink(const LogMessage* const message) const {}
 };

/**
 * \brief This class is serves as a logging facility.
 *
 * \author   Daniel Ahlin
 * \date     $Date: 2006/07/25 14:52:00 $
 */
class LogStream
{
private:
     LogSink* mLogSink;

public:
     virtual ~LogStream();

     /**
      * \brief Creates a new LogStream that logs to cerr.
      */
     LogStream() : mLogSink(new StdLogSink()) {};

     /**
      * \brief Creates a new LogStream that uses the provided
      * LogSink. The LogStream will adopt the LogSink and delete it on
      * destruction or change of logSink.
      */
     LogStream(LogSink* logSink) : mLogSink(logSink) {};

     /**
      * \brief Commits a message and then releases it.
      *
      * \param message The object to commit.
      */
     virtual void postMessage(LogMessage* message)
     {
          mLogSink->sink(message);
          delete message;
     }

     /**
      * \brief Switches to a new sink. Note that the provided sink
      * should be new'ed and that LogStream will adopt it (and delete
      * it on destruction or change of LogSink).
      *
      *  TODO/NOTE It is questionable if this should be done without
      *  synchronization (depends on the atomicity of =).
      * 
      * \param message The object to commit.
      */
     virtual void setLogSink(LogSink* newSink)
     {
          LogSink* tmp = mLogSink;
          mLogSink = newSink;

          delete tmp;
     }

     /**
      * \brief Creates a new message and writes the provided object to
      * it.
      *
      * \param t The object to write.
      * \return A reference to this message.
      */
     template<class T> LogMessage& operator << (T t)
     {
               LogMessage* message = new LogMessage(this);
               *message << t;
               return *message;
     }
};

#endif   // STRATMAS_LOGSTREAM_H

