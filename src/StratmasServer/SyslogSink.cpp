// $Id: SyslogSink.cpp,v 1.1 2006/07/25 14:52:00 dah Exp $
#ifdef OS_LINUX

// System
#include <syslog.h>

// Own
#include "SyslogSink.h"

// TODO: find out if syslog is thread safe.

// Statics
std::string SyslogSink::sIdent("stratmas");
bool SyslogSink::sSyslogOpened = false;

/**
 * \brief syslog(3)'s the provieded message.
 */
void SyslogSink::sink(const LogMessage* const message)
{
     syslog(LOG_INFO, "%s", message->getMessage().c_str());
}

/**
 * \brief Creates a new Syslog sink, calls openlog(3) if this is the
 * first object that will be created. If this is the first call, it
 * will use the provided string as identification (typically the name
 * of the program).
 */
SyslogSink* SyslogSink::createSyslogSink(const std::string& ident)
{
     if (!sSyslogOpened) {
	  sIdent = ident;
	  ::openlog(sIdent.c_str(), LOG_ODELAY | LOG_CONS | LOG_PID, 
		    LOG_DAEMON);
	  sSyslogOpened = true;
     }

     return new SyslogSink();
}

#endif /* OS_LINUX */
