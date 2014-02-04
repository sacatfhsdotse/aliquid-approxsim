// $Id: LogStream.cpp,v 1.1 2006/07/25 09:15:45 dah Exp $

// Boost
#include <boost/date_time/posix_time/posix_time.hpp>

// own
#include "LogStream.h"

namespace bpt = boost::posix_time;

LogStream slog;
LogStream debug;
LogEnd logEnd;

/**
 * \brief Terminate the message by writing an instance of LogEnd
 * to it.
 *
 * \param t The object to write.
 * \return A reference to this Error.
 */
LogMessage& LogMessage::operator << (const LogEnd& end) 
{ 
     LogStream* tmp = this->mLogStream; 
     this->mLogStream = 0;
     tmp->postMessage(this);
     return *this; 
}

/**
 * \brief Destroys the LogStream and releases any used LogSink.
 */
LogStream::~LogStream()
{
     if (mLogSink != 0) {
          delete mLogSink;
     }
}

/**
 * \brief Destroys the LogMessage, posting it if not posted.
 */
LogMessage::~LogMessage()
{
     if (mLogStream != 0) {
          *this << logEnd;
     }
}

/**
 * \brief Returns a string representation of the current time.
 */
const std::string StdLogSink::getTimeStamp() const
{
     return bpt::to_iso_extended_string(bpt::second_clock::local_time());
}
