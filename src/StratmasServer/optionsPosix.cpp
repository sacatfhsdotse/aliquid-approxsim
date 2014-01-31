// $Id: optionsPosix.cpp,v 1.2 2006/09/11 09:00:30 dah Exp $
#ifdef OS_LINUX
// System
#include <unistd.h>

// Boost
#include <boost/program_options.hpp>

// Own
#include "Environment.h"
#include "LogStream.h"
#include "SyslogSink.h"

/**
 * \brief Fills in platform specific options, which in the posix 
 * case are the daemonization option.
 */
void Environment::addPlatformOptions(po::options_description* invocation,
				     po::options_description* configuration,
				     po::options_description* development)
{
     invocation->add_options()
	  ("daemonize", "Detach and run as a daemon")
	  ;
}

/**
 * \brief Callback to handle platform options specified in addPlatformOptions.
 */
void Environment::handlePlatformOptions(po::variables_map* vm)
{
     // Check for daemonization
     if (vm->count("daemonize") != 0) {
	  // Daemonizes this process by:
	  // 
	  // * chdir to /
	  // * reopening stdin, stdout and stderr on /dev/null
	  // * Changing to syslog based reporting.

	  if (daemon(0, 0) != 0) {
	       perror(Environment::getProgramName().c_str());
	       exit(1);
	  } else {
	       slog.setLogSink(SyslogSink::createSyslogSink(Environment::getProgramName().c_str()));
	  }
     }
}

#endif /* OS_LINUX */
