// $Id: SyslogSink.h,v 1.1 2006/07/25 14:52:00 dah Exp $

// Own
#include "LogStream.h"

/**
 * \brief This class implements LogSink using Posix 1003.1-2001 calls
 * (i. e. syslog(3))
 *
 * \author   Daniel Ahlin
 * \date     $Date: 2006/07/25 14:52:00 $
 */
class SyslogSink : public LogSink
{
private:
     // The ident used in openlog(3) (typically the name of the service)
     static std::string sIdent;
     // True if openlog has been called.
     static bool sSyslogOpened;
     SyslogSink() {};
public:
     virtual ~SyslogSink() {};
     virtual void sink(const LogMessage* const message);
     static SyslogSink* createSyslogSink(const std::string& ident);
};
