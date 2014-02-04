// $Id: optionsWin.cpp,v 1.2 2006/09/11 09:00:30 dah Exp $
#ifdef OS_WIN32
// Boost
#include <boost/program_options.hpp>
#include <cstdlib>

// Own
#include "Environment.h"
#include "LogStream.h"
#include "WinEventSink.h"

int main(int argc, char** argv);

// System, last here to prevent loading of winsock.h, (see for instance
// http://forums.microsoft.com/msdn/showpost.aspx?postid=318383&siteid=1).
#include <windows.h>

VOID WINAPI stratmasServiceMain(DWORD argc, LPTSTR* argv);
DWORD WINAPI stratmasHandlerEx(DWORD dwControl, DWORD dwEventType, 
                               LPVOID lpEventData, LPVOID lpContext);


// Variable carrying status information:
SERVICE_STATUS stratmasServiceStatus;
SERVICE_STATUS_HANDLE stratmasServiceStatusHandle;

/**
 * \brief Fills in platform specific options.
 */
void Environment::addPlatformOptions(po::options_description* invocation,
                                     po::options_description* configuration,
                                     po::options_description* development)
{
     invocation->add_options()
          ("installService", "Installs Stratmas as a service")
          ("uninstallService", "Uninstall any Stratmas service")
          ("startService", "Start the Stratmas service")
          ("stopService", "Stop the Stratmas service")
          ;
     development->add_options()
          ("service", "Start the Stratmas Server as a service")
          ;
}

/**
 * \brief Callback to handle platform options specified in
 * addPlatformOptions. Currently this function will only return if
 * none of the platform options are defined.
 */
void Environment::handlePlatformOptions(po::variables_map* vm)
{
     if (vm->count("installService") != 0) {
          SC_HANDLE dbHandle = OpenSCManager(0, SERVICES_ACTIVE_DATABASE,
                                             SC_MANAGER_CREATE_SERVICE);
          if (dbHandle == 0) {
               slog << "Error connecting to Service Manager: " << 
                    GetLastError() << logEnd;
               exit(1);
          }

           char* args = "\" --service";
           char executable[MAX_PATH + 21]; // 20 > strlen(args)
          executable[0] = '"';
               if (!GetModuleFileName(0, executable + 1, MAX_PATH)) {
               slog << "Error looking up server binary filename: " << 
                    GetLastError() << logEnd;
               exit(1);
          }
           strncpy(executable + strlen(executable), args, 20);
 
          SC_HANDLE srvHandle = CreateService( 
               dbHandle,                  // SCManager database 
               TEXT("stratmas"),          // name of service 
               TEXT("Stratmas Server"),   // service name to display 
               SERVICE_ALL_ACCESS,        // desired access 
               SERVICE_WIN32_OWN_PROCESS, // service type 
               SERVICE_DEMAND_START,      // start type 
               SERVICE_ERROR_NORMAL,      // error control type 
               executable,                // path to service's binary and args.
               0,                         // no load ordering group 
               0,                         // no tag identifier 
               0,                         // no dependencies 
               0,                         // currently LocalSystem but should 
                                          // be LocalService (~=nobody) account
               0);                        // no password 

          if (srvHandle == 0) {
               slog << "Error creating service: " << 
                    GetLastError() << logEnd;
               exit(1);
          }

          char* description = "Stratmas Server Service";
          SERVICE_DESCRIPTION descriptionWrapper;
          descriptionWrapper.lpDescription = description;
          if (!ChangeServiceConfig2(srvHandle, SERVICE_CONFIG_DESCRIPTION,
                                    &descriptionWrapper)) {
               slog << "Error setting service description: " << 
                    GetLastError() << logEnd;
               exit(1);
          };

          if (!CloseServiceHandle(srvHandle)) {
               slog << "Error closing service handle: " << 
                    GetLastError() << logEnd;
               exit(1);
          }
          if (!CloseServiceHandle(dbHandle)) {
               slog << "Error closing database handle: " << 
                    GetLastError() << logEnd;
               exit(1);
          }

          // Finished, exit.
          exit(0);

     } else if (vm->count("uninstallService") != 0) {
          SC_HANDLE dbHandle = OpenSCManager(0, SERVICES_ACTIVE_DATABASE,
                                             DELETE);
          if (dbHandle == 0) {
               slog << "Error connecting to Service Manager: " << 
                    GetLastError() << logEnd;
               exit(1);
          }
          
          SC_HANDLE srvHandle = OpenService(dbHandle, "stratmas", DELETE);

          if (srvHandle == 0) {
               slog << "Error retrieving service: " << 
                    GetLastError() << logEnd;
               exit(1);
          }
          if (!DeleteService(srvHandle)) {
               slog << "Error removing service: " << 
                    GetLastError() << logEnd;
               exit(1);
          }
          if (!CloseServiceHandle(srvHandle)) {
               slog << "Error closing service handle: " << 
                    GetLastError() << logEnd;
               exit(1);
          }
          if (!CloseServiceHandle(dbHandle)) {
               slog << "Error closing database handle: " << 
                    GetLastError() << logEnd;
               exit(1);
          }
          exit(0);
     } else if (vm->count("startService") != 0) {
          SC_HANDLE dbHandle = OpenSCManager(0, SERVICES_ACTIVE_DATABASE,
                                             GENERIC_EXECUTE);
          if (dbHandle == 0) {
               slog << "Error connecting to Service Manager: " << 
                    GetLastError() << logEnd;
               exit(1);
          }
          
          SC_HANDLE srvHandle = OpenService(dbHandle, "stratmas", 
                                            SERVICE_START);

          if (srvHandle == 0) {
               slog << "Error retrieving service: " << 
                    GetLastError() << logEnd;
               exit(1);
          }

          const char* arg = "--service";

          if (!StartService(srvHandle, 1, &arg)) {
               slog << "Error starting service: " << 
                    GetLastError() << logEnd;
               exit(1);
          }

          if (!CloseServiceHandle(srvHandle)) {
               slog << "Error closing service handle: " << 
                    GetLastError() << logEnd;
               exit(1);
          }
          if (!CloseServiceHandle(dbHandle)) {
               slog << "Error closing database handle: " << 
                    GetLastError() << logEnd;
               exit(1);
          }
          exit(0);
     } else if (vm->count("stopService") != 0) {
          SC_HANDLE dbHandle = OpenSCManager(0, SERVICES_ACTIVE_DATABASE,
                                             GENERIC_EXECUTE);
          if (dbHandle == 0) {
               slog << "Error connecting to Service Manager: " << 
                    GetLastError() << logEnd;
               exit(1);
          }
          
          SC_HANDLE srvHandle = OpenService(dbHandle, "stratmas", 
                                            SERVICE_STOP);

          if (srvHandle == 0) {
               slog << "Error retrieving service: " << 
                    GetLastError() << logEnd;
               exit(1);
          }

          SERVICE_STATUS status;
          if (!ControlService(srvHandle, SERVICE_CONTROL_STOP, &status)) {
               slog << "Error stopping service: " << 
                    GetLastError() << logEnd;
               exit(1);
          }

          if (status.dwCurrentState != SERVICE_STOPPED) {
               if (status.dwCurrentState == SERVICE_STOP_PENDING) {
                    slog << "Stratmas service claims to be stopping " 
                         << "(we will not wait for it)." << logEnd;
               } else {
                    slog << "Stratmas service not stopping, current state: "
                         << status.dwCurrentState << logEnd;
                    exit(1);
               }
          }

          if (!CloseServiceHandle(srvHandle)) {
               slog << "Error closing service handle: " << 
                    GetLastError() << logEnd;
               exit(1);
          }
          if (!CloseServiceHandle(dbHandle)) {
               slog << "Error closing database handle: " << 
                    GetLastError() << logEnd;
               exit(1);
          }
          exit(0);
     } else if (vm->count("service") != 0) {
          // Use Windows event logging.
          slog.setLogSink(WinEventSink::createWinEventSink(Environment::getProgramName().c_str()));
          SERVICE_TABLE_ENTRY dispatchTable[] = 
               { 
                    {"stratmas", ::stratmasServiceMain}, 
                    {0, 0} 
               }; 
 
          // This function should not return until termination (if not error).
          if (!StartServiceCtrlDispatcher(dispatchTable)) 
          { 
               slog << "Error starting service control dispatcher: " << 
                    GetLastError() << logEnd;
               exit(1);
          }
          exit(0);
     }
}

/**
 * \brief serves as serviceControlHandle for stratmas. Implements the
 * SERVICE_CONTROL_STOP and SERVICE_CONTROL_INTERROGAT control
 * messages. Everything else is an error.
 */
DWORD WINAPI ::stratmasHandlerEx(DWORD dwControl, DWORD dwEventType, 
                               LPVOID lpEventData, LPVOID lpContext)
{
     DWORD status; 
 
     switch(dwControl) { 
     case SERVICE_CONTROL_STOP:
          // Do whatever it takes to stop here. 
          stratmasServiceStatus.dwWin32ExitCode = 0; 
          stratmasServiceStatus.dwCurrentState  = SERVICE_STOPPED; 
          stratmasServiceStatus.dwCheckPoint    = 0; 
          stratmasServiceStatus.dwWaitHint      = 0; 
          if (!SetServiceStatus (::stratmasServiceStatusHandle, 
                                 &::stratmasServiceStatus)) { 
               slog << "SetServiceStatus error: " <<  GetLastError() << logEnd;
               exit(1);
          }
          slog << "Stratmas is stopping" << logEnd; 
          return 1;
     case SERVICE_CONTROL_INTERROGATE: 
          // Fall through to send current status. 
          break; 
     default: 
          slog << "Unsupported Service Request" << logEnd; 
          return 1;
     } 
     
     // Send current status. 
     if (!SetServiceStatus (stratmasServiceStatusHandle, 
                            &stratmasServiceStatus)) { 
          slog << "SetServiceStatus error: " <<  GetLastError() << logEnd; 
          return 1;
     }

     return 0;      
}

/**
 * \brief serves as ServiceMain for stratmas, basically tells the SCM
 * that all is dandy, then calls main(), this particular solution
 * heavily depends on initEnvironment is the first call in main and
 * that it can be called several times but will immidiatelly return on
 * any call but the first.
 */
VOID WINAPI ::stratmasServiceMain(DWORD argc, LPTSTR* argv)
{     
     ::stratmasServiceStatus.dwServiceType = SERVICE_WIN32; 
     ::stratmasServiceStatus.dwCurrentState = SERVICE_START_PENDING; 
     ::stratmasServiceStatus.dwControlsAccepted = SERVICE_ACCEPT_STOP;
     ::stratmasServiceStatus.dwWin32ExitCode = ERROR_SERVICE_SPECIFIC_ERROR ; 
     ::stratmasServiceStatus.dwServiceSpecificExitCode = NO_ERROR; 
     ::stratmasServiceStatus.dwCheckPoint = 0; 
     ::stratmasServiceStatus.dwWaitHint = 0; 
     
     ::stratmasServiceStatusHandle = 
            RegisterServiceCtrlHandlerEx("stratmas", stratmasHandlerEx, 0); 
     
     if (::stratmasServiceStatusHandle == (SERVICE_STATUS_HANDLE) 0) { 
          slog << "RegisterServiceCtrlHandler failed: " << 
               GetLastError() << logEnd; 
          exit(1);
     }
     
     // Report running status. 
     ::stratmasServiceStatus.dwCurrentState       = SERVICE_RUNNING; 
     ::stratmasServiceStatus.dwCheckPoint         = 0; 
     ::stratmasServiceStatus.dwWaitHint           = 0; 
     
     if (!SetServiceStatus (::stratmasServiceStatusHandle, 
                            &::stratmasServiceStatus)) 
     { 
          slog << "RegisterServiceCtrlHandler failed: " << 
               GetLastError() << logEnd; 
          exit(1);
     }
     
     ::main(0, 0);
}

#endif /* OS_WIN32 */
