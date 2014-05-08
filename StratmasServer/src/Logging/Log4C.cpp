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

log4cxx::LoggerPtr agenciesLog(log4cxx::Logger::getLogger("approxsim.agencies")),
 dataManagementLog(log4cxx::Logger::getLogger("approxsim.dataManagement")),
 debugLog(log4cxx::Logger::getLogger("approxsim.debug")),
 geoLog(log4cxx::Logger::getLogger("approxsim.geo")),
 networkLog(log4cxx::Logger::getLogger("approxsim.network")),
 pvLog(log4cxx::Logger::getLogger("approxsim.pv")),
 simulationObjectsLog(log4cxx::Logger::getLogger("approxsim.simulationObjects")),
 taclanLog(log4cxx::Logger::getLogger("approxsim.taclan")),
 approxsimLog(log4cxx::Logger::getLogger("approxsim.log"));


const bool logDebug = false;

void Log4CexitHook(){
    LOG_FATAL(approxsimLog, "--Shutting down--\n");
}

void Log4C::init(const boost::filesystem::path& config){
    if(exists(config)){
        //try{
            std::string installDir = (Environment::getInstallDir() / "/").string();
            apr_env_set("approxsim.install", installDir.c_str(), Environment::apr_pool);

            if(logDebug) std::cout << "approxsim.install = " << log4cxx::helpers::System::getProperty("approxsim.install") << std::endl;

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
        LOG4CXX_ERROR(approxsimLog, "Log4C config \"" << config << "\" not found, using default configuration." )
    }

    LOG_INFO(approxsimLog, "Logging started");

    //TODO not working
    Log4SinkDebug* log4debug = new Log4SinkDebug("approxsim.debug");
    Log4SinkInfo* log4info = new Log4SinkInfo("approxsim.info");

    slog.setLogSink(log4info);
    debug.setLogSink(log4debug);

    atexit(Log4CexitHook);
}

void run_n_log(log4cxx::LoggerPtr logger, std::function<void (std::ostream&)> p){
    std::stringstream ss;
    p(ss);
    LOG_FATAL(logger, ss.str());
}

void run_n_log(log4cxx::LoggerPtr logger, std::function<void (std::ostream&)> p, char level){
    std::stringstream ss;
    p(ss);
    switch (level){
        case 't':
            LOG_TRACE(logger, ss.str());
            break;
        case 'd':
            LOG_DEBUG(logger, ss.str());
            break;
        case 'i':
            LOG_INFO(logger, ss.str());
            break;
        case 'w':
            LOG_WARN(logger, ss.str());
            break;
        case 'e':
            LOG_ERROR(logger, ss.str());
            break;
        case 'f':
            LOG_FATAL(logger, ss.str());
            break;
    }
}
