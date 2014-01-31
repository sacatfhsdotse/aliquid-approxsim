// System
#include <fstream>

// Own
#include "Environment.h"
#include "IOHandler.h"


using namespace std;


// Static Definitions
#ifdef DEBUG
bool IOHandler::sWriteToFile = true;
#else
bool IOHandler::sWriteToFile = false;
#endif

/**
 * \brief Make calls to dumpToFile to actually dump to file. Currently
 * this is set from Environment iff the user specifies an output
 * dir. Note that compiling in DEBUG is also activates file output.
 */
void IOHandler::enableFileOutput()
{
     sWriteToFile = true;     
}

/**
 * \brief Dumps a string to the specified file, if we'er allowed to do
 * so.
 *
 * \param toDump The string to dump.
 * \param filename The name of the file (in the output directory) to dump to.
 * \param mode The openmode of the file.
 */
void IOHandler::dumpToFile(const string& toDump, const string& filename, ios_base::openmode mode)
{
     if (sWriteToFile) {
	  string filePath = Environment::getDumpDir() + "/" + filename;
	  ofstream o(Environment::getNativePath(filePath).c_str(), mode);
	  if (o) {
	       o << toDump;
	  }
	  o.close();
     }
}
