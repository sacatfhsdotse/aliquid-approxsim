#ifndef STRATMAS_LOG4C_H
#define STRATMAS_LOG4C_H

#include <string>
#include <boost/filesystem.hpp>
#include <log4cxx/logger.h>

#include "LogStream.h"


class Log4C {
public:
    static void init(const boost::filesystem::path& config);

private:

};

class Log4SinkDebug : public LogSink
{
private:
     log4cxx::LoggerPtr logger;
public:
     Log4SinkDebug(std::string name) {logger = log4cxx::Logger::getLogger(name);}
     virtual ~Log4SinkDebug() {};
     virtual void sink(const LogMessage* const message){
         LOG4CXX_DEBUG(logger, message->getMessage())
     }
};

class Log4SinkInfo : public LogSink
{
private:
     log4cxx::LoggerPtr logger;
public:
     Log4SinkInfo(std::string name) {logger = log4cxx::Logger::getLogger(name);}
     virtual ~Log4SinkInfo() {};
     virtual void sink(const LogMessage* const message){
         LOG4CXX_INFO(logger, message->getMessage())
     }
};

extern log4cxx::LoggerPtr agenciesLog, dataManagementLog, debugLog, geoLog, networkLog, 
       pvLog, simulationObjectsLog, taclanLog, stratmasLog;

#endif /* STRATMAS_LOG4C_H */
