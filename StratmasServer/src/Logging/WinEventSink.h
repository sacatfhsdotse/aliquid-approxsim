// $Id: WinEventSink.h,v 1.1 2006/07/25 14:52:00 dah Exp $

// Own
#include "LogStream.h"

/**
 * \brief This class implements LogSink using Windows events
 *
 * \author   Daniel Ahlin
 * \date     $Date: 2006/07/25 14:52:00 $
 */
class WinEventSink : public LogSink
{
private:
     // The name of this event source (typically the name of the service).
     static std::string sSourceName;
     // The server to log to, empty for localhost.
     static std::string sServerName;
     // The handle to the log.
     static HANDLE sHandle;

     // True if RegisterEventSource has been called.
     static bool sEventSourcedRegistered;
     WinEventSink() {};
public:
     virtual ~WinEventSink() {};
     virtual void sink(const LogMessage* const message);
     static WinEventSink* createWinEventSink(const std::string& sourceName);
};
