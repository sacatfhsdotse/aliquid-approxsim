#ifndef APPROXSIM_LOCKABLE_H
#define APPROXSIM_LOCKABLE_H

// System
#include <boost/thread/mutex.hpp>


typedef boost::mutex::scoped_lock Lock;


/**
 * \brief Wrapper around a mutex.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/05 07:42:41 $
 */
class Lockable {
private:
     /// The mutex.
     mutable boost::mutex mMutex;

public:
     boost::mutex& mutex() const { return mMutex; }
};

#endif   // _APPROXSIM_LOCKABLE_H
