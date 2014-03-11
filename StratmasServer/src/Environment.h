#ifndef STRATMAS_ENVIRONMENT
#define STRATMAS_ENVIRONMENT

// System
#include <string>
#include <sstream>

// Own
#include "ClientValidator.h"

namespace boost {
     namespace filesystem {
          class path;
     }
     namespace program_options {
          class options_description;
          class variables_map;
     }
};
namespace fs = boost::filesystem;
namespace po = boost::program_options;

/**
 * \brief Helper class for keeping track of some environment related
 * variables.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/09/11 09:00:30 $
 */
class Environment {
private:
     /// CVS will replace this string with the current version number.
     static const std::string STRATMAS_VERSION;
     /// Version string for versions with no version number.
     static const std::string DEFAULT_VERSION;

     /** If environment initialization has started. Currently this is
      *  mainly used to ensure that a subsequent calls to main when
      *  using Windows * S ervices won't try to reinitialize the
      *  environment.
      */
     static bool initStarted;

     /// The path to the program.
     static fs::path sExecutable;
     /// The installation directory.
     static fs::path sInstallDir;
     /// Path to the configuration file.
     static fs::path sConfigFile;
     /// The folder to write files to.
     static fs::path sDumpDir;
     /// If set to true, the lack of a config file is an error.
     static bool sForceConfigFileRead;

     /// The server address.
     static std::string sServerAddress;
     /// The server port.
     static int sServerPort;

     /// If a dispatcher should be used.
     static bool useDispatcher;
     /// The dispatcher address.
     static std::string sDispatcherHost;
     /// The dispatcher port.
     static int sDispatcherPort;

     // The client validator to use.
     static ClientValidator* spClientValidator;
     
     static void initInstallDir(const fs::path& executable);
     static void initConfig(int argc, char** argv);

     static void setConfigFile(const std::string& file);
     static void setDumpDir(const std::string& dirname);

     // Note that the following two function are defined in two
     // separate files optionsPosix.cpp and optionsWin.cpp
     static void addPlatformOptions(po::options_description* invocation,
                                    po::options_description* configuration,
                                    po::options_description* development);
     static void handlePlatformOptions(po::variables_map* vm);

     static std::string getNativePath(const fs::path& path);
     static std::string getInstalledPath(const fs::path& installedPath);

     static fs::path importNativePath(const std::string& nativePath);
     static bool isFile(const fs::path& path);
public:
     /// The default namespace.
     static const std::string DEFAULT_SCHEMA_NAMESPACE;
     /// The xsd namespace.
     static const std::string XSD_NAMESPACE;
     /// The name of the main Stratmas schema file.
     static const std::string STRATMAS_PROTOCOL_SCHEMA;

     // File operations.
     static std::string getNativePath(const std::string& path);
     static std::string getDumpDir();
     static const fs::path& getInstallDir();

     // Misc utilities
     static void milliSleep(int secs);
     static std::string getVersion();
     static void initEnvironment(int argc, char** argv);
     static std::string getProgramName();

     // Net addresses
     static int getServerPort() {return sServerPort;}
     static const std::string& getServerAddress() {return sServerAddress;}
     static ClientValidator* getClientValidator() {return spClientValidator;}
     
     static bool getUseDispatcher() {return !getDispatcherHost().empty();}
     static const std::string& getDispatcherHost() {return sDispatcherHost;}
     static int getDispatcherPort() {return sDispatcherPort;}
};

//Fixes for broken environments
#if defined(COMPILER_CYGWIN) || defined(COMPILER_MINGW)
// Mingw doesn't define putenv()
extern int putenv(char*);
extern void tzset(void);

// Mingw doesn't implement to_string
namespace std {
    template < typename T > std::string to_string( const T& n ) {
        std::ostringstream stm ;
        stm << n ;
        return stm.str() ;
    }
}

#endif

#endif   // STRATMAS_ENVIRONMENT
