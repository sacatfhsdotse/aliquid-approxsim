#ifndef STRATMAS_IOHANDLER
#define STRATMAS_IOHANDLER

// System
#include <iostream>
#include <string>


/**
 * \brief Class providing helpers for file IO.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/24 10:14:35 $
 */
class IOHandler {
private:
     /// True if we are allowed to write to files.
     static bool sWriteToFile;

public:
     static void enableFileOutput();
     static void dumpToFile(const std::string& toDump,
			    const std::string& filename,
			    std::ios_base::openmode mode = std::ios_base::trunc);

};


#endif   // STRATMAS_IOHANDLER
