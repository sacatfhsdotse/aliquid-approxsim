// $Id: WinEventSink.cpp,v 1.3 2006/07/27 15:28:29 dah Exp $
#ifdef OS_WIN32
// System
#include <windows.h>
#include <cstdlib>

// Own
#include "WinEventSink.h"

std::string WinEventSink::sSourceName("approxsim");
std::string WinEventSink::sServerName("");
HANDLE WinEventSink::sHandle = 0;
bool WinEventSink::sEventSourcedRegistered = false;

/**
 * \brief Creates a windows INFORMATION event and posts it.
 */
void WinEventSink::sink(const LogMessage* const message)
{
     std::string str = message->getMessage().c_str();
     LPCSTR msg = str.c_str();
     BOOL res;
     res = ReportEvent(sHandle,                   // event log handle
                       EVENTLOG_INFORMATION_TYPE, // event type
                       0,                         // category zero
                       0,                         // event identifier
                       NULL,                      // user security identifier
                       1,                         // one substitution string
                       0,                         // data?
                       &msg,                      // pointer to string array
                       NULL);                     // pointer to data
     if (res == 0) {
          // std:cerr most likely won't work, no other option though.
          std::cerr << "Log post failed: \"" << GetLastError() 
                    << "\"" << std::endl;
     }
}

/**
 * \brief Creates a new Syslog sink, calls RegisterEventSource if this is the
 * first object that will be created. If this is the first call, it
 * will use the provided string as identification (typically the name
 * of the program).
 */
WinEventSink* WinEventSink::createWinEventSink(const std::string& sourceName)
{
     if (!sEventSourcedRegistered) {
          sSourceName = sourceName;
          sHandle = RegisterEventSource(sServerName.empty() ? 
                                        0 : sServerName.c_str(),  
                                        TEXT(sSourceName.c_str()));
          if (sHandle == 0) {
               // Try to warn, (but cerr may be closed at this point)
               std::cerr << "Unable to set up logging, error: "
                         << GetLastError() << ". Exting...";
               exit(1);
          }

          sEventSourcedRegistered = true;
     }

     return new WinEventSink();
}

#endif /* OS_WIN32 */
