// $Id: Environment.cpp,v 1.13 2006/09/11 09:00:30 dah Exp $
// System
#include <algorithm>
#include <cstdio>
#include <cstdlib>
#include <iostream>
#include <fstream>
#include <string>

// Boost
#include <boost/filesystem/operations.hpp>
#include <boost/filesystem/convenience.hpp>
#include <boost/filesystem/path.hpp>
#include <boost/program_options.hpp>

// Own
#include "debugheader.h"
#include "Environment.h"
#include "IPValidator.h"
#include "IOHandler.h"
#include "LogStream.h"
#include "SyslogSink.h"

using namespace std;
namespace fs = boost::filesystem;
namespace po = boost::program_options;

// Static Definitions
const string Environment::STRATMAS_VERSION("$Name: Stratmas_V_7_5 $");
const string Environment::DEFAULT_VERSION("under development...");
const string Environment::DEFAULT_SCHEMA_NAMESPACE("http://pdc.kth.se/stratmasNamespace");
const string Environment::XSD_NAMESPACE("http://www.w3.org/2001/XMLSchema");
const string Environment::STRATMAS_PROTOCOL_SCHEMA("stratmasProtocol.xsd");
bool         Environment::initStarted = false;

// File related variables
// BUG With regard to path:
//
// Apparantly on MacOSX (gcc 4.0.0)
// boost/libs/filesystem/src/path_posix_windows.cpp:120 in the 1.33.1
// release is unvisited when we are here causing checks at
// boost/libs/filesystem/src/path_posix_windows.cpp:159 to fail. This
// issue may be fixed in CVS and or 1.34. For now we will move real
// initializations to initEnvironment below.
fs::path     Environment::sInstallDir;
fs::path     Environment::sDumpDir;
fs::path     Environment::sExecutable;
fs::path     Environment::sConfigFile;
bool         Environment::sForceConfigFileRead = false;

// Net related variables.
string       Environment::sServerAddress("");
int          Environment::sServerPort = 28444;
bool         Environment::useDispatcher = false;
string       Environment::sDispatcherHost("");
int          Environment::sDispatcherPort = 4181;
ClientValidator*         Environment::spClientValidator = new PassClientValidator();

/**
 * \brief Gets the absolute path to the given filename.  The following
 * implicit conventions apply:
 *
 * 1. If the filename is absolute (=complete) the native
 *    representation will be provided.  
 *
 * 2. If the filename is relative it is resolved relative to
 *    getInstallDir() which currently is the directory where the
 *    executable lives (that may change) and the native absolute 
 *    path is returned.
 *
 * \return The absolute path to the given filename.
 */
std::string Environment::getNativePath(const std::string& path)
{
     return getNativePath(fs::path(path));
}

/**
 * \brief Gets the absolute path to the given filename.
 *
 * \return The absolute path to the given filename.
 */
std::string Environment::getNativePath(const fs::path& path)
{
     if (!path.is_absolute()) {
          return fs::absolute(path, getInstallDir()).string();
     } else {
          return path.string();
     }
}

/**
 * \brief tries to parse the provided string as a os-native path and
 * returns the stratmas native path representation. The following
 * implicit conventions apply:
 *
 * 1. If the filename is absolute (:absolute) it is stored absolute.
 * 2. If the filename is relative it is resolved relative to
 *    getInstallDir() which currently is the directory where the
 *    executable lives (that may change).
 *
 * TODO proper errorhandling.
 */
fs::path Environment::importNativePath(const std::string& nativePath)
{
     fs::path path(nativePath);
     if (!path.is_absolute()) {
          return fs::absolute(path, getInstallDir());
     } else {
          return path;
     }
}

/**
 * \brief Returns the directory where stratmas is assumed to be installed.
 *
 * \return the directory where stratmas is assumed to be installed
 */
const fs::path& Environment::getInstallDir()
{
     return sInstallDir;
}

/**
 * Returns the name of the program
 */
std::string Environment::getProgramName() 
{
     return fs::basename(sExecutable);
}

/**
 * Returns the directory designated for dumping debug files in.
 */
std::string Environment::getDumpDir() 
{
     return sDumpDir.string();
}

/**
 * \brief File tester
 *
 * \return returns true if the provided path points to an existing
 * non-directory.
 */
bool Environment::isFile(const fs::path& path)
{
     fs::path tmp(path);

     return fs::exists(tmp) && !fs::is_directory(tmp);
}

/**
 * \brief Accessor for the Stratmas version.
 *
 * \return The Stratmas version.
 */
string Environment::getVersion()
{
     string version = STRATMAS_VERSION;
     if (version.size() <= 7) {
          return string(DEFAULT_VERSION);
     }
     version = version.substr(6, version.size() - 7);
     int first = version.find_first_not_of(" ");
     int last = version.find_last_not_of(" ");
     if (first == -1 || last == -1) {
          return string(DEFAULT_VERSION);
     }
     version = version.substr(first, last - first + 1);
     if (version.size() == 0) {
          version = DEFAULT_VERSION;
     }
     return version;
}

/**
 * \brief Sets the path to the config file, an important side effect
 * of this function is that it also sets variables to the effect that
 * _not_ finding the configuration file is an error.
 *
 * As a temporary solution of the difficulties involving finding out
 * if this value was applied as a default or not, setting the config
 * file to the empty is a noop.
 */
void Environment::setConfigFile(const std::string& file)
{
     if (!file.empty()) {
          sConfigFile = importNativePath(file);
          sForceConfigFileRead = true;
     }
}

/**
 * \brief Sets the directory where stratmas will dump files, also
 * activates file dumping.
 */
void Environment::setDumpDir(const std::string& dirname)
{
     fs::path path = importNativePath(dirname);
     if (fs::is_directory(path)) {
          sDumpDir = path;
          IOHandler::enableFileOutput();
     } else {
          // Since this is not a public user functionality, just give
          // a warning if provided path is not a directory.
          slog <<  getNativePath(path) << " is not a directory. " 
               << "No dumps will be written." << logEnd;
     }
}

/**
 * \brief Suspends the calling thread for the specified number of
 * milliseconds
 */
void Environment::milliSleep(int milliSecs)
{
#ifdef __win__
               Sleep(milliSecs);
#else
               timespec ts;
               ts.tv_sec = milliSecs / 1000;
               ts.tv_nsec = (milliSecs % 1000) * 1000000;
               ::nanosleep(&ts, 0);
#endif
}

/**
 * \brief Initializes and configures the environment, exits on
 * error. Also handles (via initConfiguration) and exits on version or
 * help queries. This function can be called several times, however it
 * will immidiatelly return on all but the first invocation. Currently
 * the arbitration of "firstness" is not thread-safe, in practice this
 * should not currently be a problem.
 */
void Environment::initEnvironment(int argc, char** argv)
{
     // Short circuit any subsequent calls to initEnvironment.
     if (initStarted) {
          return;
     } else {
          initStarted = true;
     }
     
     // Bug fix, for comments see static initializations above.
     sInstallDir = fs::path(".");
     sDumpDir = fs::path(".");
     sExecutable = fs::path("stratmas");
     sConfigFile = fs::path("stratmas.cfg");

     // If any program name supplied, use that, else use default.
     if (argc > 0) {
          // Can't use importNativePath here since it depends on values
          // initialized by sExecutable
          sExecutable = fs::absolute(fs::path(std::string(argv[0])));
     }

     // Use progname to figure out install dir.
     initInstallDir(sExecutable);

     // Handle configuration last. Note that the Windows Service code
     // depends on this being the last function called in this
     // scope. (When starting stratmas as a Windows Service the
     // start/main thread will not return from this call-path.)
     initConfig(argc, argv); return;
}

/**
 * \brief Tries to determine the install dir, and dependent
 * paths. Currently it is necessary to hint the method with the path
 * to the executable.
 */
void Environment::initInstallDir(const fs::path& executable)
{
     sInstallDir = executable.branch_path();
}

/**
 * \brief Configures the environment, exits on error. Also handles
 * (and exits) on version or help queries. initInstallDir is assumed
 * to have been called prior to this function.
 */
void Environment::initConfig(int argc, char** argv)
{
     // Options that only makes sense on the command line (changes the
     // reason for running the program.) Note that the line for config
     // file is rather special to cope with defaultness.
     po::options_description invocation("Invocation options");
     invocation.add_options()
          ("help,?", "Show help message")
          ("version,v", "Show version")
          ("configfile", 
           po::value<std::string>()->notifier(&Environment::setConfigFile)->default_value("", (fs::path("INSTALLDIR") / sConfigFile).string()), 
           "Specify configuration file")
          ;
          
     // Configurable options.
     po::options_description configuration("Configuration");
     configuration.add_options()
          ("anyclient,a", "Disable allowed client checks")
          ("client,c", 
           po::value<vector<string> >()->composing(), 
           "Allowed clients")
          ("dispatcher,d", po::value(&sDispatcherHost), 
           "Register to dispatcher")
          ("dispatcher-port", 
           po::value(&sDispatcherPort)->default_value(getDispatcherPort()), 
           "Dispatcher port to use")
          ("host,h", 
           po::value(&sServerAddress)->default_value(getServerAddress()), 
           "Server address")
          ("port,p", 
           po::value(&sServerPort)->default_value(getServerPort()), 
           "Server port")
          ;

     // Options for developers, these will be hidden.
     po::options_description development("Developer options");
     development.add_options()
          ("outputdir,o", 
           po::value<std::string>()->notifier(&setDumpDir),
           "Output directory")
          ;

     // Add options specific to the current platform
     addPlatformOptions(&invocation, &configuration, &development);

     // Options valid on the command line:
     po::options_description cmdline_options;
     cmdline_options.add(invocation);
     cmdline_options.add(configuration);
     cmdline_options.add(development);
        
     // Configuration
     po::variables_map vm;

     // Get command line options.
     try {
          po::store(po::parse_command_line(argc, argv, cmdline_options), vm);
     } catch (po::error& e) {
          slog << "Command line error: " << e.what() << logEnd;
          exit(1);
     }
     notify(vm);

     // Handle help and version from here.
     if (vm.count("help") != 0) {
          po::options_description visible("Stratmas Server options");
          visible.add(invocation);
          visible.add(configuration);
          visible.print(std::cout);
          std::cout << std::endl;
          exit(0);
     } else if (vm.count("version") != 0) {
          std::cout << "Stratmas Server" << std::endl
                    << "    Version:   " << getVersion() << std::endl;
          exit(0);
     }
     
     // Get config file options.
     // Options valid in the config file:
     po::options_description config_file_options;
     config_file_options.add(configuration);
     config_file_options.add(development);

     try {
          if (sForceConfigFileRead && !isFile(sConfigFile)) {
               // If config file specified but not found, it is an error.
               slog << "Config file error: " 
                    << getNativePath(sConfigFile)
                    << " is not a file" << logEnd;
               exit(1);
          }
          std::ifstream ifs(getNativePath(sConfigFile).c_str());
          po::store(po::parse_config_file(ifs, config_file_options), vm);
     } catch (po::error& e) {
          slog << "Error in config file " 
               << getNativePath(sConfigFile) << ": " 
               << e.what() << logEnd;
          exit(1);
     }
     notify(vm);

     // If anyclient specified, keep default PassClientValidator, else
     // replace with validator we collected with client entries.
     if (vm.count("anyclient") == 0) {
          delete spClientValidator;
          IPValidator* validator = new IPValidator();
          if (vm.count("client") != 0) {
               vector<string> clients = vm["client"].as<vector<string> >();
               for (vector<string>::iterator it = clients.begin(); 
                    it != clients.end(); it++) {
                    validator->addValidIP(*it);
               }
          }

          spClientValidator = validator;
     }

     // Handle platform options last. Note that the Windows Service
     // code depends on this being the last function called in this
     // scope. (When starting stratmas as a Windows Service the
     // start/main thread will not return from this call-path.)
     handlePlatformOptions(&vm); return;
}
