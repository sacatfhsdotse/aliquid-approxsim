#ifndef _STRATMASTIME_H
#define _STRATMASTIME_H


// System
#include <iomanip>
#include <iostream>
#include <limits>

// Own
#include "stdint.h"

#ifdef __win__
#define STRATMAS_INVALID_TIME (LLONG_MIN)
#define STRATMAS_MIN_TIME (LLONG_MIN + 1)
#define STRATMAS_MAX_TIME (LLONG_MAX)
#else
#define STRATMAS_INVALID_TIME (std::numeric_limits<int64_t>::min())
#define STRATMAS_MIN_TIME (std::numeric_limits<int64_t>::min() + 1)
#define STRATMAS_MAX_TIME (std::numeric_limits<int64_t>::max())
#endif

/**
 * \brief This class is used to represent timestamps and
 * intervalls.
 *
 * The internal representation is a 64-bit integer holding the
 * number of milliseconds, either from some reference time or in the
 * intervall.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/09/20 09:21:01 $
 */
class Time {
private:
     int64_t mMilliSec;   ///< The number of seconds from the reference time

public:
     Time() : mMilliSec(STRATMAS_INVALID_TIME) {} ///< Default constructor
     Time(const Time &t) : mMilliSec(t.mMilliSec) {}       ///< Copy constructor
     /**
      * \brief Constructor
      *
      * \param d The number of days from the reference time or in the intervall.
      * \param h The number of hours from the reference time or in the intervall.
      * \param m The number of minutes from the reference time or in the intervall.
      * \param s The number of seconds from the reference time or in the intervall.
      * \param ms The number of milliseconds from the reference time or in the intervall.
      */
     Time(int64_t d, int64_t h = 0, int64_t m = 0, int64_t s = 0, int64_t ms = 0)
	  : mMilliSec((d * 86400 + h * 3600 + m * 60 + s) * 1000 + ms) {}

     
     /**
      * \brief Returns the maximum time that Stratmas may represent.
      *
      * \return The maximum time that Stratmas may represent.
      */
     static Time maxTime() { return Time(0, 0, 0, 0, STRATMAS_MAX_TIME); }

     /**
      * \brief Returns the minimum time that Stratmas may represent.
      *
      * \return The minimum time that Stratmas may represent.
      */
     static Time minTime() { return Time(0, 0, 0, 0, STRATMAS_MIN_TIME); }

     /**
      * \brief Returns the number of hours represented by this Time
      *
      * \return The number of hours
      */
     int64_t days() const { return mMilliSec / 86400000; }

     /**
      * \brief Returns the number of hours represented by this Time
      *
      * \return The number of hours
      */
     double hoursd() const { return static_cast<double>(mMilliSec) / 3600000.0; }

     /**
      * \brief Returns the number of milliseconds represented by this
      * Time
      *
      * \return The number of milliseconds
      */
     int64_t milliSeconds() const { return mMilliSec; }

     /**
      * \brief Add a number of days.
      *
      * \param ndays The number of days to add.
      */
     void addDays(int ndays) { mMilliSec += 86400000 * ndays; }

     /**
      * \brief Add a number of hours.
      *
      * \param nhours The number of hours to add.
      */
     void addHours(int nhours) { mMilliSec += 3600000 * nhours; }

     /**
      * \brief Checks if this Time is a valid time.
      *
      * \return True if this time is valid, false otherwise.
      */
     bool isValid() const { return mMilliSec != STRATMAS_INVALID_TIME; }

     // Operators
     /// Less-than operator
     bool operator  < (const Time &t) const { return (mMilliSec < t.mMilliSec); }
     /// Greater-than operator
     bool operator  > (const Time &t) const { return (mMilliSec > t.mMilliSec); }
     /// Less-than-or-equal-to operator
     bool operator <= (const Time &t) const { return (mMilliSec <= t.mMilliSec); }
     /// Greater-than-or-equal-to operator
     bool operator >= (const Time &t) const { return (mMilliSec >= t.mMilliSec); }
     /// Equality operator
     bool operator == (const Time &t) const { return (mMilliSec == t.mMilliSec); }
     /// Not-equal-to operator
     bool operator != (const Time &t) const { return (mMilliSec != t.mMilliSec); }
     /// Add an intervall to this Time
     Time &operator += (const Time &t) { mMilliSec += t.mMilliSec; return *this; }
     /// Add two Time's
     Time operator + (const Time &t) { return Time(0, 0, 0, 0, mMilliSec + t.mMilliSec); }
     /// Subtract a Time from this Time.
     Time &operator -= (const Time &t) { mMilliSec -= t.mMilliSec; return *this; }
     /// Subtract one Time from another.
     Time operator - (const Time &t) { return Time(0, 0, 0, 0, mMilliSec - t.mMilliSec); }

     // Friends
     /// For debugging purposes
     friend std::ostream &operator << (std::ostream &o, const Time &t) {
	  int64_t d  = t.mMilliSec / 86400000;
	  int64_t h  = (t.mMilliSec - d * 86400000) / 3600000;
	  int64_t m  = (t.mMilliSec - d * 86400000 - h * 3600000) / 60000;
	  int64_t s  = (t.mMilliSec - d * 86400000 - h * 3600000 - m * 60000) / 1000;
	  int64_t ms = t.mMilliSec - d * 86400000 - h * 3600000 - m * 60000 - s * 1000;
	  char prev = o.fill('0');
	  o << "Day: " << d << " "
	    << std::setw(2) << h << ":"
	    << std::setw(2) << m << ":"
	    << std::setw(2) << s << ":"
	    << std::setw(3) << ms;
	  o.fill(prev);
	  return o;
     }
};

#endif  // _STRATMASTIME_H
