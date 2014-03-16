/*$Id: stdint.h,v 1.1 2006/07/03 14:18:24 dah Exp $*/

/* Partly accomodates lack of stdint.h on different plattforms. */

#ifndef STRATMAS_STDINT_H
#define STRATMAS_STDINT_H

// Platform Dependent
#ifdef OS_SOLARIS
#include <inttypes.h>
#elif OS_WIN32

#ifdef _LONGLONG
typedef _LONGLONG int64_t;
typedef _ULONGLONG uint64_t;
#else
typedef __int64 int64_t;
typedef unsigned __int64 uint64_t;
#endif
typedef __int32 int32_t;
typedef __int16 int16_t;
typedef __int8 int8_t;

typedef unsigned __int8 uint8_t;
#else
#include <stdint.h>
#endif

#endif 
/* STRATMAS_STDINT_H */
