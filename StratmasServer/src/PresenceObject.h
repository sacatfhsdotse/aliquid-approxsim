#ifndef _APPROXSIM_PRESENCEOBJECT_H
#define _APPROXSIM_PRESENCEOBJECT_H

// System
#include <vector>

// Own

// Forward Declarations
class Unit;


/**
 * \brief PresenceObjects are used to mark units' presence in grid
 * cells. This information is then used by the combat model.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/06 12:55:11 $
 */
class PresenceObject {
private:
     int mCell;        ///< The cell this PresenceObject refers to.
     Unit* mUnit;      ///< The Unit this PresenceObject refers to.

     /// The fraction of the unit present in the cell this PresenceObject refers to.
     double mFraction;

public:
     /**
      * \brief Create a PresenceObject with the specified properties.
      *
      * \param c The cell index.
      * \param u The Unit.
      * \param f The fraction of the Unit.
      */
     PresenceObject(int c, Unit& u, double f) : mCell(c), mUnit(&u), mFraction(f) {}
     
     /**
      * \brief Sets the properties of a PresenceObject.
      *
      * \param c The cell index.
      * \param u The Unit.
      * \param f The fraction of the Unit.
      */
     void set(int c, Unit& u, double f) { mCell = c; mUnit = &u; mFraction = f; }

     /**
      * \brief Accessor for the cell index.
      *
      * \return The cell's index.
      */
     int cell() const { return mCell; }

     /**
      * \brief Accessor for the Unit.
      *
      * \return The Unit.
      */
     Unit& unit() const { return *mUnit; }

     /**
      * \brief Accessor for the fraction.
      *
      * \return The fraction.
      */
     double fraction() const { return mFraction; }

     void affect(std::vector<PresenceObject*>& potentialVictims) const;


     /**
      * \brief Less than operator.
      *
      * A PresenceObject p1 is less than another PresenceObject p2 if:
      *    <p>
      *    p1's cell index is less than p2's
      *    <p>
      *    or
      *    <p>
      *    p1 and p2 have the same cell index and p1's unit pointer is
      *    less than p2's.
      *
      * \param p The PresenceObject to compare to.
      * \return True if this PresenceObject is less than p.
      */
     bool operator < (const PresenceObject& p) const {
          return ( mCell < p.mCell || (mCell == p.mCell && this < &p) );
     }
};


/**
 * \brief Function object for less-than operator for pointer to
 * PresenceObjects.
 *
 * A PresenceObject is less than another PresenceObject if it has a
   lower cell index.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/06 12:55:11 $
 */
struct lessPresenceObjectPointer {
     /**
      * \brief Less-than operator for pointers to PresenceObjects.
      *
      * \param p1 The first PresenceObject.
      * \param p2 The second PresenceObject.
      * \return True if the first PresenceObject is less than the
      * other PresenceObject, false otherwise.
      */
     bool operator()(const PresenceObject* const p1, const PresenceObject* const p2) const {
          if (!p1 || !p2) {
               return false;
          }
          else {
               return *p1 < *p2;
          }
     }
};

#endif   // _APPROXSIM_PRESENCEOBJECT_H
