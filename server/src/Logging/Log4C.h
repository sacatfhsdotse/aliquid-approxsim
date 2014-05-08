#ifndef APPROXSIM_LOG4C_H
#define APPROXSIM_LOG4C_H

#include <string>
#include <functional>
#include <sstream>
#include <boost/filesystem.hpp>
#include <log4cxx/logger.h>

#include "LogStream.h"

#define _LOG_LOCATION3(x) #x
#define _LOG_LOCATION2(x) _LOG_LOCATION3(x)
#define _LOG_LOCATION __FILE__ ":" _LOG_LOCATION2(__LINE__) ":" << __func__ << " "

#define LOG_TRACE(logger, expression) LOG4CXX_TRACE(logger, _LOG_LOCATION << expression)
#define LOG_DEBUG(logger, expression) LOG4CXX_DEBUG(logger, _LOG_LOCATION << expression)
#define LOG_INFO(logger, expression) LOG4CXX_INFO(logger, expression)
#define LOG_WARN(logger, expression) LOG4CXX_WARN(logger, _LOG_LOCATION << expression)
#define LOG_ERROR(logger, expression) LOG4CXX_ERROR(logger, _LOG_LOCATION << expression)
#define LOG_FATAL(logger, expression) LOG4CXX_FATAL(logger, expression)

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
         LOG_DEBUG(logger, message->getMessage())
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
         LOG_INFO(logger, message->getMessage())
     }
};

void run_n_log (log4cxx::LoggerPtr logger, std::function<void (std::ostream&)> p);
void run_n_log (log4cxx::LoggerPtr logger, std::function<void (std::ostream&)> p, char level);

extern log4cxx::LoggerPtr agenciesLog, dataManagementLog, debugLog, geoLog, networkLog, 
       pvLog, simulationObjectsLog, taclanLog, approxsimLog;

#endif /* APPROXSIM_LOG4C_H */
