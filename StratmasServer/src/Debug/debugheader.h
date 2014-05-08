#ifndef _APPROXSIMDEBUGHEADER_H
#define _APPROXSIMDEBUGHEADER_H


#ifdef DEBUG
#include <iostream>
#include "Log4C.h"
#define approxsimDebug(x) LOG_TRACE(debugLog, x)
#define debugnnl(x) LOG_TRACE(debugLog, x)
#define debugl0(x, y) if (y > 0) { LOG_TRACE(debugLog, << x << ": " << y << std::endl); }
#define INDENT ("  ")
#else
#define approxsimDebug(x)
#define debugnnl(x)
#define debugl0(x, y)
#define INDENT ("")
#endif

#endif   // _APPROXSIMDEBUGHEADER_H
