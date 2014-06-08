/**
 * \file approxsim.cpp
 * 
 * \brief This file contains the main function of the ApproxsimServer.
 */

// System
#include <fstream>
#include <iostream>
#include <cstdlib>
#include <cstdio>
#include <ctime>

#include <apr-1/apr_env.h>

// Own
#include "Log4C.h"
#include "Registrator.h"
#include "debugheader.h"
#include "Environment.h"
#include "IOHandler.h"
#include "LogStream.h"
#include "PropertyHandler.h"
#include "PVInfo.h"
#include "random2.h"
#include "Server.h"

// Static Definitions
bool GaussSaver::smSaved = false;
int  PrivateRandom::sRandIndex(0);
long PrivateRandom::sRandNum[kNumRand];

// Function declarations
static void initTimeZone();


/**
 * \brief The main function.
 *
 * \param argc argc
 * \param argv argv
 * \return Exit status.
 */
int main(int argc, char **argv)
{
     // Note that Environment::initEnvironment(argc, argv); is
     // expected to be called first in main.
     std::cout << "\n    A P P R O X S I M\n    -Crisis Sim\n\n";
     Environment::initEnvironment(argc, argv);

     // Initialize logging
     Log4C::init(Environment::getInstallDir() / "/log4.xml");

     // Set timezone to UTC (for XMLHelper)
     initTimeZone();
     // Initialize static PVInfo class
     PVInfo::init();
     // Initialize random numbers that doesn't affect simulation.
     PrivateRandom::initRandomNumberArray();

     Server *server;
     server = new Server(Environment::getServerPort(), 
                         Environment::getServerAddress(), 
                         Environment::getClientValidator());
     
     if (Environment::getUseDispatcher()) {
          Registrator registrator(Environment::getDispatcherHost(),
                                  Environment::getDispatcherPort(),
                                  Environment::getServerAddress(),
                                  Environment::getServerPort());
          if (!registrator.registerServer()) {
               slog << "Unable to register with dispatcher at " 
                    << Environment::getDispatcherHost() << ":" 
                    << Environment::getDispatcherPort() << logEnd;
               exit(1);
          } else {
               approxsimDebug("Registered with dispatcher at "
                     << Environment::getDispatcherHost() << ":" 
                     << Environment::getDispatcherPort());

          }
     }
     
     // Run server.
     try {
          server->start();
     }
     catch (...) {
          slog << "Server caught fatal exception. Shutting down..." 
               << logEnd;
     }
     
     return 0;
}

void initTimeZone()
{
     apr_env_set("TZ", "UTC", Environment::apr_pool);

#ifdef OS_WIN32
     _tzset();
#elif defined(COMPILER_CYGWIN)
	//TODO fix
#else
      tzset();
#endif
}
