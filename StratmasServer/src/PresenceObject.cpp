// System
#include <ostream>

// Own
#include "debugheader.h"
#include "PresenceObject.h"
#include "Unit.h"


using namespace std;


/**
 * \brief Lets the Unit this PresenceObject refers to affect the units
 * in the PresenceObjects in the provided vector.
 *
 * Called by the CombatGrid after finding out which units that
 * overlaps which cells.
 *
 * \param potentialVictims Vector with potential victims.
 */
void PresenceObject::affect(vector<PresenceObject*>& potentialVictims) const
{
     for (vector<PresenceObject*>::iterator it = potentialVictims.begin();
          it != potentialVictims.end(); it++) {
          Unit& victim = (*it)->unit();
          if(unit().isHostileTowards(victim)) {
               if (victim.untouchable()) {
                    victim.registerSpotter(*this);
               }
               else {
                    if (!unit().untouchable()) {
                         victim.registerEnemy(*this);
                    }
                    else {
                         unit().registerPotentialAmbush(**it, fraction());
                    }
               }
          }
     }
}
