#ifndef APPROXSIM_ACTION_H
#define APPROXSIM_ACTION_H


// System
#include <vector>

// Own
#include "Grid.h"
#include "GridEffect.h"

// Forward Declarations
class Element;
class Faction;
class Shape;


/**
 * \brief Super class for all actions.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/06 12:55:07 $
 */
class Action {
protected:
     /// The target of this action.
     Grid &mTarget;

public:
     /**
      * \brief Constructor.
      *
      * \param target The target of this Action.
      */
     inline Action(Grid &target) : mTarget(target) {}

     /**
      * \brief Destructor.
      */
     inline virtual ~Action() {}

     /**
      * \brief Carries out this action.
      */
     inline virtual void carryOut() { mTarget.expose(*this); }
};



/**
 * \brief This class represents an Action that affects the grid.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/06 12:55:07 $
 */
class GridAction : public Action {
private:
     /// The location.
     const Shape& mLocation;

     /// The performer.
     const Element* mPerformer;

     /// A vector of effects for this Action.
     const std::vector<GridEffect>& mEffects;

     /// The fraction of full effect this action should have.
     double mFraction;

public:
     /**
      * \brief Constructor.
      *
      * \param e The target of this Action.
      * \param area The area to expose
      * \param performer The performer of the Action.
      * \param effects A vector of effects for this Action.
      * \param fraction The fraction of full effect this action should
      * have.
      */
     GridAction(Grid& e,
                       const Shape& area,
                       const Element* performer,
                       const std::vector<GridEffect> &effects,
                       double fraction)
          : Action(e), mLocation(area), mPerformer(performer), mEffects(effects), mFraction(fraction) {}

     /**
      * \brief Gets the area
      *
      * \return The area
      */
     const Shape& location() const { return mLocation; }

     /**
      * \brief Gets the performer of this action.
      *
      * \return The performer or null if there is none.
      */
     const Element* performer() const { return mPerformer; }

     /**
      * \brief Gets the number of effects of this action.
      *
      * \return The number of effects of this action.
      */
     int effects() const { return mEffects.size(); }

     /**
      * \brief Gets ths specified effect.
      *
      * \param index The index of the effect to fetch.
      * \return The specified effect.
      */
     GridEffect effect(int index) const {
          GridEffect e = mEffects[index];
          e.mSeverity *= mFraction;
          return e;
     }
};

#endif   // APPROXSIM_ACTION_H
