#include "Log4C.h"
#include <iostream>
#include <log4cxx/helpers/exception.h>
#include <log4cxx/xml/domconfigurator.h>
#include <log4cxx/basicconfigurator.h>

//(log4cxx::Logger::getLogger("stratmas."))
log4cxx::LoggerPtr agenciesLog(log4cxx::Logger::getLogger("stratmas.agencies")),
 dataManagementLog(log4cxx::Logger::getLogger("stratmas.dataManagement")),
 debugLog(log4cxx::Logger::getLogger("stratmas.debug")),
 geoLog(log4cxx::Logger::getLogger("stratmas.geo")),
 networkLog(log4cxx::Logger::getLogger("stratmas.network")),
 pvLog(log4cxx::Logger::getLogger("stratmas.pv")),
 simulationObjectsLog(log4cxx::Logger::getLogger("stratmas.simulationObjects")),
 taclanLog(log4cxx::Logger::getLogger("stratmas.taclan"));

bool Log4C::init(const boost::filesystem::path& config){
    if(exists(config)){
        //try{
            log4cxx::xml::DOMConfigurator::configureAndWatch(config.string());
            return true;
        /*}catch (log4cxx::helpers::Exception& e){
            std::cerr << e.what() << std::endl;
            exit(EXIT_FAILURE);
        }*/
    }else{
        log4cxx::BasicConfigurator::configure();
        LOG4CXX_ERROR(log4cxx::Logger::getLogger("logging.Log4C"), "Log4C config \"" << config << "\" not found, using default." )
        return false;
    }
}
