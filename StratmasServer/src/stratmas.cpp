/**
 * \file stratmas.cpp
 * 
 * \brief This file contains the main function of the StratmasServer.
 */


// System
#include <fstream>
#include <iostream>
#include <cstdlib>
#include <cstdio>

// Own
#include "Registrator.h"
#include "debugheader.h"
#include "Environment.h"
#include "IOHandler.h"
#include "LogStream.h"
#include "PropertyHandler.h"
#include "PVInfo.h"
#include "random.h"
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
     Environment::initEnvironment(argc, argv);

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
               debug("Registered with dispatcher at "
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
     putenv("TZ=UTC");
#ifdef __win__
     _tzset();
#else
      tzset();
#endif
}
