#ifndef STRATMAS_LOG4C_H
#define STRATMAS_LOG4C_H

#include <boost/filesystem.hpp>
#include <log4cxx/logger.h>


class Log4C {
public:
    static bool init(const boost::filesystem::path& config);

private:

};

//extern log4cxx::LoggerPtr agenciesLog, dataManagementLog, debugLog, geoLog, networkLog, 
//       pvLog, simulationObjectsLog, taclanLog;

#endif /* STRATMAS_LOG4C_H */
