// System

// Own
#include "debugheader.h"
#include "Error.h"
#include "PresenceObject.h"
#include "PresenceObjectAllocator.h"


using namespace std;


/**
 * \brief Destructor
 */
PresenceObjectAllocator::~PresenceObjectAllocator()
{ 
     reset();
}
     
/**
 * \brief Provides a PresenceObject with the specified properties
 * either by using an already allocated PresenceObject or by
 * allocating a new one.
 *
 * \param c The cell index.
 * \param u The Unit.
 * \param f The fraction of the Unit.
 * \return A PresenceObject with the specified properties.
 */
PresenceObject* PresenceObjectAllocator::create(int c, Unit& u, double f)
{
     PresenceObject* res = 0;
     if (mFree.empty()) {
	  res = new PresenceObject(c, u, f);
	  mUsed.insert(res);
     }
     else {
	  res = *mFree.begin();
	  res->set(c, u, f);
	  mFree.erase(mFree.begin());
	  mUsed.insert(res);
     }
     return res;
}

/**
 * \brief Notifies the PresenceObjectAllocator that the provided
 * PresenceObject will no longer be used and that it thus may be
 * considered freed.
 *
 * \param p The PresenceObjec to dismiss.
 */
void PresenceObjectAllocator::dismiss(PresenceObject* p)
{
     if (mUsed.find(p) == mUsed.end()) {
	  Error e;
	  if (mFree.find(p) != mFree.end()) {
	       e << "Tried to dismiss already dismissed PresenceObject";
	  }
	  else {
	       e << "Tried to dismiss PresenceObject that wasn't ";
	       e << "created by the PresenceObjectAllocator";
	  }
	  throw e;
     }
     else {
	  mUsed.erase(p);
	  mFree.insert(p);
     }
}

/**
 * \brief Resets the PresenceObjectAllocator by deallocating all
 * memory used.
 */
void PresenceObjectAllocator::reset()
{
     set<PresenceObject*>::iterator it;
     for (it = mUsed.begin(); it != mUsed.end(); it++) {
	  delete *it;
     }
     for (it = mFree.begin(); it != mFree.end(); it++) {
	  delete *it;
     }
     mUsed.clear();
     mFree.clear();
}

