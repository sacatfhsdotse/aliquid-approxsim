#ifndef _STRATMASDEBUGHEADER_H
#define _STRATMASDEBUGHEADER_H


#ifdef DEBUG
#include <iostream>
#include "Log4C.h"
#define stratmasDebug(x) LOG_TRACE(debugLog, x)
#define debugnnl(x) LOG_TRACE(debugLog, x)
#define debugl0(x, y) if (y > 0) { LOG_TRACE(debugLog, << x << ": " << y << std::endl); }
#define INDENT ("  ")
#else
#define stratmasDebug(x)
#define debugnnl(x)
#define debugl0(x, y)
#define INDENT ("")
#endif

#endif   // _STRATMASDEBUGHEADER_H
