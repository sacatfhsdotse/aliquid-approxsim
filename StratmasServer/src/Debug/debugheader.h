#ifndef _STRATMASDEBUGHEADER_H
#define _STRATMASDEBUGHEADER_H


#ifdef DEBUG
#include <iostream>
#define debug(x) std::cerr << x << std::endl;
#define debugnnl(x) std::cerr << x;
#define debugl0(x, y) if (y > 0) { std::cerr << x << ": " << y << std::endl; }
#define INDENT ("  ")
#else
#define debug(x)
#define debugnnl(x)
#define debugl0(x, y)
#define INDENT ("")
#endif

#endif   // _STRATMASDEBUGHEADER_H
