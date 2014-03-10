#ifndef _STRATMASDEBUGHEADER_H
#define _STRATMASDEBUGHEADER_H


#ifdef DEBUG
#include <iostream>
#include "Log4C.h"
#define stratmasDebug(x) LOG4CXX_DEBUG(debugLog, x)
#define debugnnl(x) LOG4CXX_DEBUG(debugLog, x)
#define debugl0(x, y) if (y > 0) { LOG4CXX_DEBUG(debugLog, << x << ": " << y << std::endl); }
#define INDENT ("  ")
#else
#define stratmasDebug(x)
#define debugnnl(x)
#define debugl0(x, y)
#define INDENT ("")
#endif

#endif   // _STRATMASDEBUGHEADER_H
