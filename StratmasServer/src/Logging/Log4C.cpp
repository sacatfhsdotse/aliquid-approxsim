// System
#include <stdlib.h>
#include <cstring>
#include <cstdlib>

#include <iostream>
#include <log4cxx/helpers/system.h>
#include <log4cxx/xml/domconfigurator.h>
#include <log4cxx/basicconfigurator.h>
#include <log4cxx/logmanager.h>
#include <log4cxx/fileappender.h>
#include <apr-1/apr_env.h>

// Own
#include "Log4C.h"
#include "../Environment.h"

log4cxx::LoggerPtr agenciesLog(log4cxx::Logger::getLogger("stratmas.agencies")),
 dataManagementLog(log4cxx::Logger::getLogger("stratmas.dataManagement")),
 debugLog(log4cxx::Logger::getLogger("stratmas.debug")),
 geoLog(log4cxx::Logger::getLogger("stratmas.geo")),
 networkLog(log4cxx::Logger::getLogger("stratmas.network")),
 pvLog(log4cxx::Logger::getLogger("stratmas.pv")),
 simulationObjectsLog(log4cxx::Logger::getLogger("stratmas.simulationObjects")),
 taclanLog(log4cxx::Logger::getLogger("stratmas.taclan")),
 stratmasLog(log4cxx::Logger::getLogger("stratmas.log"));


const bool logDebug = false;

void Log4CexitHook(){
    LOG_FATAL(stratmasLog, "--Shutting down--\n");
}

void Log4C::init(const boost::filesystem::path& config){
    if(exists(config)){
        //try{
            std::string installDir = (Environment::getInstallDir() / "/").string();
            apr_env_set("stratmas.install", installDir.c_str(), Environment::apr_pool);

            if(logDebug) std::cout << "stratmas.install = " << log4cxx::helpers::System::getProperty("stratmas.install") << std::endl;

            log4cxx::xml::DOMConfigurator::configureAndWatch(config.string());
            
            if(logDebug){
                for(log4cxx::LoggerPtr l : log4cxx::LogManager::getCurrentLoggers()){
                    for(log4cxx::AppenderPtr a : l->getAllAppenders()){
                        std::cout << "Appender " << a->getName();
                        log4cxx::FileAppender* fa = dynamic_cast<log4cxx::FileAppender*>(&*a);
                        if(fa != NULL){
                            boost::filesystem::path p(fa->getFile());
                            std::cout << " is a FileAppender with path '" << p.string() << "'";
                            /*if(!p.is_absolute()){
                                fa->setFile((Environment::getInstallDir() / p).string());
                                boost::filesystem::path newPath(fa->getFile());
                                std::cout << ", updated path '" << newPath.string() << "'";
                            }*/
                        }
                        std::cout << std::endl;
                    }
                }
            }
        /*}catch (log4cxx::helpers::Exception& e){
            std::cerr << e.what() << std::endl;
            exit(EXIT_FAILURE);
        }*/
    }else{
        log4cxx::BasicConfigurator::configure();
        LOG4CXX_ERROR(stratmasLog, "Log4C config \"" << config << "\" not found, using default configuration." )
    }

    LOG_INFO(stratmasLog, "Logging started");

    //TODO not working
    Log4SinkDebug* log4debug = new Log4SinkDebug("stratmas.debug");
    Log4SinkInfo* log4info = new Log4SinkInfo("stratmas.info");

    slog.setLogSink(log4info);
    debug.setLogSink(log4debug);

    atexit(Log4CexitHook);
}
