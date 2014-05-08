#ifndef _APPROXSIM_PRESENCEOBJECTALLOCATOR_H
#define _APPROXSIM_PRESENCEOBJECTALLOCATOR_H


// System
#include <set>

// Forward Declarations
class PresenceObject;
class Unit;


/**
 * \brief Helper class that makes the memory allocation for
 * PresenceObjects more effective.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/02 17:06:54 $
 */
class PresenceObjectAllocator {
private:
     /// Allocated and used PresenceObjects.
     std::set<PresenceObject*> mUsed;

     /// Allocated and unused PresenceObjects.
     std::set<PresenceObject*> mFree;

public:
     ~PresenceObjectAllocator();
     
     PresenceObject* create(int c, Unit& u, double f);
     void dismiss(PresenceObject* p);
     void reset();
};


#endif   // _APPROXSIM_PRESENCEOBJECTALLOCATOR_H
