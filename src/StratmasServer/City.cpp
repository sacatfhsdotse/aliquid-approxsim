// System
#include <vector>

// Own
#include "City.h"
#include "DataObject.h"
#include "Distribution.h"
#include "Error.h"
#include "Faction.h"
#include "Reference.h"


using namespace std;


/**
 * \brief Creates a City from the provided DataObject.
 *
 * \param d The DataObject to create this City from.
 */
City::City(const DataObject& d) : Element(d)
{
     const vector<DataObject*>& grps = d.getChild("ethnicGroups")->objects();
     for (vector<DataObject*>::const_iterator it = grps.begin(); it != grps.end(); it++) {
	  const DataObject& dr = **it;
	  mPop[&dr.getChild("ethnicity")->getReference()] = dr.getChild("inhabitants")->getDouble();
     }

     for (std::map<const Reference*, double>::iterator it = mPop.begin(); it != mPop.end(); it++) {
	  mPVs.push_back(PVModification(eAllPopulation, *EthnicFaction::faction(*it->first), it->second));
     }

     double totPop = 0;
     for(std::map<const Reference*, double>::iterator it = mPop.begin(); it != mPop.end(); ++it) {
	  totPop += it->second;
     }
     mPop[&EthnicFaction::all().ref()] = totPop;
}

/**
 * \brief Gets the population for the Faction with the provided index.
 *
 * \param f The faction index.
 * \return The number of inhabitants for the specifid Faction.
 */
double City::population(int f) const
{
     Faction* fp = EthnicFaction::faction(f);
     if (!fp) {
	  Error e;
	  e << "No EthnicFaction for index " << f;
	  throw e;
     }
     return population(*fp);
}

/**
 * \brief Gets the population for the specified Faction.
 *
 * \param f The faction.
 * \return The number of inhabitants for the specifid Faction.
 */
double City::population(const Faction& f) const
{
     std::map<const Reference*, double>::const_iterator it = mPop.find(&f.ref());
     return (it == mPop.end() ? 0 : it->second);
}

/**
 * \brief For debugging purposes.
 *
 * \param o The ostream to write to.
 * \param c The City to write.
 * \return The provided ostream with the City written to it.
 */
ostream &operator << (ostream &o, const City &c)
{
     o << "City " << c.ref() << ", population:";
     for (std::map<const Reference*, double>::const_iterator it = c.mPop.begin(); it != c.mPop.end(); it++) {
	  o << endl << "  " << *it->first << " - " << it->second;
     }
     return o;
}

