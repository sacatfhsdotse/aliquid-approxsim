#ifndef STRATMAS_GOODSTUFF_H
#define STRATMAS_GOODSTUFF_H

// System
#include <cmath>
#include <algorithm>   // For min, max and swap

/**
 * \file GoodStuff.h
 *
 * \brief This file contains some useful constants and functions.
 */

// Constants
#ifdef __win__
const double kPi      = (4.0 * ::atan2(1.0, 1.0));   ///< pi
#else
const double kPi      = (4.0 * std::atan2(1.0, 1.0));   ///< pi
#endif
const double k2Pi     = 2 * kPi;                        ///< 2 * pi
const double kDeg2Rad = kPi / 180.0;                    ///< For converting degrees to radians
const double kRad2Deg = 180.0 / kPi;                    ///< For converting radians to degrees

/// Approximate number of kilometers per degree latitude.
const double kKmPerDegreeLat  = 40008.0 / 360.0;

/// Approximate number of meters per degree latitude.
const double kMetersPerDegreeLat  = 40008000.0 / 360.0;

/// Approximate number of degrees latitude per meter.
const double kDegreesLatPerMeter  = 1.0 / kMetersPerDegreeLat;

/// Square of approximate number of kilometers per degree latitude.
const double kKmPerDegreeLat2 = kKmPerDegreeLat * kKmPerDegreeLat;

/// Square of approximate number of meters per degree latitude.
const double kMetersPerDegreeLat2 = kMetersPerDegreeLat * kMetersPerDegreeLat;

/**
 * \brief Poor round function adapted from older versions of Stratmas.
 *
 * \param x The value to be rounded.
 * \return The rounded value.
 */
inline int Round(float  x) { return int(x + 0.5); }

/**
 * \brief Poor round function adapted from older versions of Stratmas.
 *
 * \param x The value to be rounded.
 * \return The rounded value.
 */
inline int Round(double x) { return int(x + 0.5); }


/**
 * \brief Makes sure x fits in the intervall [bot, top]. If it doesn't
 * - round x to either of bot or top that is closest.
 *
 * \param x     The value to be checked
 * \param bot   The lower bound
 * \param top   The upper bound
 * \return      An integer value in the intervall [bot, top]
 */
#ifdef __win__
inline int    between(int x, int bot, int top)
{
  int res = bot < x ? x : bot;
  res = top > x ? x : top;
  return res;
}
#else
 inline int    between(int x, int bot, int top)           { return std::max(bot, std::min(top, x)); }
#endif



/**
 * \brief Makes sure x fits in the intervall [bot, top]. If it doesn't
 * - round x to either of bot or top that is closest.
 *
 * \param x     The value to be checked
 * \param bot   The lower bound
 * \param top   The upper bound
 * \return      A double value in the intervall [bot, top]
 */
#ifdef __win__
inline double between(double x, double bot, double top)
{
  double res = bot < x ? x : bot;
  res = top > x ? x : top;
  return res;
}
#else
 inline double between(double x, double bot, double top) { return std::max(bot, std::min(top, x)); }
#endif

/// For swapping endian of data type of any size
#define ByteSwap(x) ByteSwapX((unsigned char *) &x,sizeof(x))

/**
 * \brief For swapping byte order of data type of any size.
 *
 * \param b The data to swap byte order for.
 * \param n The number of bytes of data.
 */
inline void ByteSwapX(unsigned char *b, int n)
{
     register int i = 0;
     register int j = n - 1;
     while (i<j) {
          std::swap(b[i], b[j]);
          i++;
          j--;
     }
} 

// Plattform Dependent
// Must define own isnan since cmath undefines it. This macro is
// copied from /usr/include/architecture/ppc/math.h
#ifdef __CYGWIN__
#define isnan(x) (isnan(x))
#elif __sun__
#define isnan(x) (isnan(x))
#elif __win__
#include <float.h>
#define isnan(x) (_isnan(x))
#else
#define isnan(x) (isnan(x))
#endif

#endif   // STRATMAS_GOODSTUFF_H
