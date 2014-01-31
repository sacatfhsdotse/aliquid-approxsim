#ifndef STRATMAS_FACTION_H
#define STRATMAS_FACTION_H

// System
//#include <ext/hash_map>
//#include <ext/hash_set>
#include <map>
#include <set>

// Own
#include "UpdatableSOAdapter.h"

// Forward Declarations
class DataObject;
class Faction;
class Reference;

// Type Definitions
//namespace stdext = ::__gnu_cxx; 
//typedef stdext::hash_set<const Reference*, hashReferenceP> RefSet;
//typedef stdext::hash_map<const Reference*, Faction*, hashReferenceP> RefFactionMap;
typedef std::set<const Reference*> RefSet;
typedef std::map<const Reference*, Faction*> RefFactionMap;


/**
 * \brief The Faction class is the abstract base class for the
 * Stratmas server representation of differennt types of Factions.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/05 14:49:43 $
 */
class Faction : public UpdatableSOAdapter {
private:
     /// Maps a Reference to its Faction
     static RefFactionMap mFactionRefMap;

     /**
      * \brief Maps References to the position in the enemy list to
      * the FactionReference to that faction.
      */
     std::map<const Reference*, const Reference*> mEnemyList;

     /// References to the factions that this faction consider as its enemies.
     RefSet mEnemy;

     void createEnemySet();
     
protected:
     /**
      * \brief The index of this Faction. Always 0 for the Faction
      * representing everyone and -1 for other than EthnicFactions
      */
     int mIndex;
      
     Faction();

public:
     Faction(const DataObject& d);
     virtual ~Faction();
     
     /**
      * \brief Extracts data from this object to the Buffer.
      *
      * \param b The Buffer to extract data to.
      */
     void extract(Buffer &b) const;
     void addObject(DataObject& toAdd, int64_t initiator);
     void removeObject(const Reference& toRemove, int64_t initiator);
     void replaceObject(DataObject& newObject, int64_t initiator);
     void modify(const DataObject& d);
     void reset(const DataObject& d);

     /**
      * \brief Maps a Faction's Reference to the faction itself.
      * 
      * \param ref The Reference to the faction.
      * \return A pointer to the faction with Reference ref or null of
      * no such faction exists.
      */
     static Faction* faction(const Reference& ref) {
	  RefFactionMap::iterator it = mFactionRefMap.find(&ref);
	  return (it == mFactionRefMap.end() ? 0 : it->second);
     }

     /**
      * \brief Checks if this faction is hostile towards the specified
      * faction.
      * 
      * \param f The faction to check for hostility towards.
      * \return True if the factions are enemies, false otherwise.
      */
     bool isHostileTowards(const Faction& f) {
	  if (&ref() == &f.ref()) {
	       return false;
	  }
	  return (mEnemy.find(&f.ref()) != mEnemy.end() || f.mEnemy.find(&ref()) != f.mEnemy.end());
     }
};


/**
 * \brief The EthnicFaction class contains the Stratmas server
 * representation of an EthnicFaction.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/05 14:49:43 $
 */
class EthnicFaction : public Faction {
private:
     /// Pointer to the 'all' faction.
     static EthnicFaction* sAllFaction;

     /// The index of the next faction to create.
     static int sCurrentIndex;

     /**
      * \brief Maps a Faction's index to the faction itself. Should
      * not contain the all faction.
      */
     static std::map<int, EthnicFaction*> mFactionIndexMap;

     /// Private default constructor for the all faction.
     EthnicFaction() : Faction() {}

public:
     /// The index of the all faction.
     static const int ALL = 0;

     /// Represents no faction.
     static const int NONE = -1;

     EthnicFaction(const DataObject& d);
     virtual ~EthnicFaction();
     
     static EthnicFaction& all();

     /**
      * \brief Returns true if this faction is the all faction.
      *
      * \return True if this faction is the all faction, false
      * otherwise.
      */
     bool isAll() const { return (mIndex == 0); }

     /**
      * \brief Returns the index of this faction.
      *
      * \return The index of this faction.
      */
     int index() const { return mIndex; }

     /**
      * \brief Maps a Faction's index to the faction itself.
      * 
      * \param i The index of the faction.
      * \return A pointer to the faction with index i of null of no
      * such faction exists.
      */
     static EthnicFaction* faction(int i) {
	  if (i == ALL) {
	       return &all();
	  }
	  else {
	       std::map<int, EthnicFaction*>::iterator it = mFactionIndexMap.find(i);
	       return (it == mFactionIndexMap.end() ? 0 : it->second);
	  }
     }

     /**
      * \brief Maps an EthnicFaction's Reference to the faction itself.
      * 
      * \param ref The Reference to the ethnic faction.
      * \return A pointer to the ethnic faction with Reference ref or null of
      * no such ethnic faction exists.
      */
     static EthnicFaction* faction(const Reference& ref) {
	  return dynamic_cast<EthnicFaction*>(Faction::faction(ref));
     }
};


/**
 * \brief The MilitaryFaction class contains the Stratmas server
 * representation of a MilitaryFaction.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/05 14:49:43 $
 */
class MilitaryFaction : public Faction {
public:
     /**
      * Constructor that creates a MilitaryFaction from a DataObject.
      *
      * \param d The DataObject to create this object from.
      */
     MilitaryFaction(const DataObject& d) : Faction(d) {}
};     

#endif   // STRATMAS_FACTION_H
