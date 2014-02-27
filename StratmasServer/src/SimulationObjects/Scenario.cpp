// System
#include <fstream>   // For region file conversion.

// Own
#include "Activity.h"
#include "Agency.h"
#include "AgencyFactory.h"
#include "AgencyTeam.h"
#include "Buffer.h"
#include "City.h"
#include "CombatGrid.h"
#include "DataObjectImpl.h"
#include "debugheader.h"
#include "Disease.h"
#include "Error.h"
#include "Faction.h"
#include "Grid.h"
#include "GridCell.h"
#include "GridDataHandler.h"
#include "GridPartitioner.h"
#include "Map.h"
#include "random.h"
#include "Region.h"
#include "RegionFile.h"
#include "Resetter.h"
#include "Scenario.h"
#include "Simulation.h"
#include "SOFactory.h"
#include "SOMapper.h"
#include "TimeStepper.h"
#include "Type.h"
#include "Unit.h"
#include "Update.h"


using namespace std;


/**
 * \brief Creates a Scenario simulation object from the provided DataObjec.
 *
 * \param d The DataObject to create this object from.
 */
Scenario::Scenario(const DataObject& d)
     : UpdatableSOAdapter(d),
       mGrid(0), mCombatGrid(0), mNumEthnicFactions(0),
       mDisease(dynamic_cast<Disease*>(SOFactory::createSimulationObject(*d.getChild("disease")))),
       mHDI(d.getChild("HDI")->getDouble()),
       mUnemployment(d.getChild("unemployment")->getDouble())
{
     mMap = new Map(dynamic_cast<StratmasShape*>(d.getChild("map"))->getShapeRef());

     // Initialize factions
     const vector<DataObject*>& factions = d.getChild("factions")->objects();
     for (vector<DataObject*>::const_iterator it = factions.begin(); it != factions.end(); it++) {
          Faction* f = dynamic_cast<Faction*>(SOFactory::createSimulationObject(**it));
          if (f) {
               mFactions.push_back(f);
               if (dynamic_cast<EthnicFaction*>(f)) {
                    mNumEthnicFactions++;
               }
          }
          else {
               Error e;
               e << "Couldn't create Faction from object '" << (*it)->ref();
               e << "' of type '" << (*it)->getType().getName() << "'";
               throw e;
          }
     }

     // Initialize cities
     const vector<DataObject*>& popcen = d.getChild("populationCenters")->objects();
     for (vector<DataObject*>::const_iterator it = popcen.begin(); it != popcen.end(); it++) {
          mCities.push_back(dynamic_cast<City*>(SOFactory::createSimulationObject(**it)));
     }

     // Initialize forces
     const vector<DataObject*>& forces = d.getChild("militaryUnits")->objects();
     for (vector<DataObject*>::const_iterator it = forces.begin(); it != forces.end(); it++) {
          mForces.push_back(dynamic_cast<Unit*>(SOFactory::createSimulationObject(**it)));
     }

     // Initialize ageny teams
     const vector<DataObject*>& agencyTeams = d.getChild("agencyTeams")->objects();
     for (vector<DataObject*>::const_iterator it = agencyTeams.begin(); it != agencyTeams.end(); it++) {
          mAgencyTeams.push_back(dynamic_cast<AgencyTeam*>(SOFactory::createSimulationObject(**it)));
     }

     // Initialize activities
     const vector<DataObject*>& activities = d.getChild("activities")->objects();
     for (vector<DataObject*>::const_iterator it = activities.begin(); it != activities.end(); it++) {
          mActivities.push_back(dynamic_cast<Activity*>(SOFactory::createSimulationObject(**it)));
     }

     // Initialize regions
     const vector<DataObject*>& regions = d.getChild("regions")->objects();
     for (vector<DataObject*>::const_iterator it = regions.begin(); it != regions.end(); it++) {
          mRegions.push_back(dynamic_cast<Region*>(SOFactory::createSimulationObject(**it)));
     }
     // Sort the events in order of increasing start and end time
     // Shouldn't need sorting...
//     sort(mActivities.begin(), mActivities.end(), lessActivityPointer());
}
 
/**
 * \brief Destructor.
 */
Scenario::~Scenario()
{
     if (mMap       ) { delete mMap       ; }
     if (mGrid      ) { delete mGrid      ; }
     if (mCombatGrid) { delete mCombatGrid; }

     SOFactory::removeSimulationObject(mDisease);
     
     for (vector<Faction*>::iterator it = mFactions.begin(); it != mFactions.end(); it++) {
          SOFactory::removeSimulationObject(*it);
     }
     for (vector<City*>::iterator it = mCities.begin(); it != mCities.end(); it++) {
          SOFactory::removeSimulationObject(*it);
     }
     for (vector<Unit*>::iterator it = mForces.begin(); it != mForces.end(); it++) {
          SOFactory::removeSimulationObject(*it);
     }
     for (vector<AgencyTeam*>::iterator it = mAgencyTeams.begin(); it != mAgencyTeams.end(); it++) {
          SOFactory::removeSimulationObject(*it);
     }
     for (vector<Agency*>::iterator it = mAgencies.begin(); it != mAgencies.end(); it++) {
          delete *it;
     }
     for (vector<Activity*>::iterator it = mActivities.begin(); it != mActivities.end(); it++) {
          SOFactory::removeSimulationObject(*it);
     }
     for (vector<Region*>::iterator it = mRegions.begin(); it != mRegions.end(); it++) {
          SOFactory::removeSimulationObject(*it);
     }
}

/**
 * \brief Prepares this SimulationObject for simulation.
 *
 * Should be called after creation and reset and before the simulation
 * starts.
 *
 * \param g The GridPartitoner to use when creating the grid.
 * \param m The ModelParameters object.
 * \param startTime The start time of the simulation.
 */
void Scenario::prepareForSimulation(const GridPartitioner& g, const ModelParameters& m, Time startTime)
{
     mNextGridUpdate = startTime;
     mCurrentTime = startTime;

     mGrid = g.createGrid(*mMap, mNumEthnicFactions);
     mGrid->setParameters(*mDisease, m, mHDI, mUnemployment);
     mGrid->populate(mCities);

     mCombatGrid = new CombatGrid(*mMap, *mGrid, mForces, mFactions);

     mGridDataHandler = new GridDataHandler(*mGrid, *mCombatGrid, mFactions);

     // Determine which cells that belongs to which region.
     for (vector<Region*>::iterator it = mRegions.begin(); it != mRegions.end(); ++it) {
          (*it)->prepareForSimulation(*mMap, *mGrid, *mGridDataHandler);
     }

     mGrid->init(mRegions);

     for (vector<Unit*>::iterator it = mForces.begin(); it != mForces.end(); ++it) {
          (*it)->prepareForSimulation(*mGrid, startTime);
     }

     for (vector<AgencyTeam*>::iterator it = mAgencyTeams.begin(); it != mAgencyTeams.end(); ++it) {
          (*it)->prepareForSimulation(*mGrid, *mGridDataHandler);
     }
     // On return we have the agencies in the mAgencies vector.
     AgencyFactory::createAgencies(*mGrid, mAgencyTeams, mAgencies);

     for (vector<Activity*>::iterator it = mActivities.begin(); it != mActivities.end(); ++it) {
          (*it)->prepareForSimulation(*mGrid, startTime);
     }

     // Temporary
//     createCityRegionMapping();
}

/**
 * \brief Advances the simulation to the specified time.
 *
 * \param currentTime The current simulation time.
 */
void Scenario::step(Time currentTime)
{
     mCurrentTime = currentTime;

     if (!mMap) {
          Error e;
          e << "Tried to step a Simulation that hasn't been initialized";
          throw e;
     }

     mCombatGrid->setUpBattleField();

     // Units
     for (vector<Unit*>::iterator it = mForces.begin(); it != mForces.end(); ++it) {
          (*it)->setup(mCurrentTime);
     }
     for (vector<Unit*>::iterator it = mForces.begin(); it != mForces.end(); ++it) {
          (*it)->act(mCurrentTime);
     }
          
     // Agencies
     for (vector<Agency*>::iterator it = mAgencies.begin(); it != mAgencies.end(); ++it) {
          (*it)->act(mCurrentTime);
     }

     // Grid
     while (mCurrentTime > mNextGridUpdate) {
          // Iterate over all Activities e.g. events.
          for (vector<Activity*>::iterator it = mActivities.begin(); it != mActivities.end(); ++it) {
               Activity& a = **it;
               if (a.isActive(mCurrentTime)) {
                    a.perform(0);
               }
          }
          for (vector<Region*>::iterator it = mRegions.begin(); it != mRegions.end(); ++it) {
               (*it)->update();
          }
          mGrid->step(mRegions);
          mNextGridUpdate.addDays(1);
//          mNextGridUpdate.addHours(4);
     }

     mCombatGrid->registerCombat();
}

/**
 * \brief Adds the SimulationObject created from the provided
 * DataObject to this object.
 *
 * \param toAdd The DataObject to create the new SimulationObject from.
 * \param initiator The id of the initiator of the update.
 */
void Scenario::addObject(DataObject& toAdd, int64_t initiator)
{
     const Type& type = toAdd.getType();
     if (type.canSubstitute("MilitaryUnit")) {
          Unit* u = dynamic_cast<Unit*>(SOFactory::createSimulationObject(toAdd, initiator));
          u->prepareForSimulation(*mGrid, mCurrentTime);
          mForces.push_back(u);
     }
     else if (type.canSubstitute("AgencyTeam")) {
          AgencyTeam* a = dynamic_cast<AgencyTeam*>(SOFactory::createSimulationObject(toAdd, initiator));
          a->prepareForSimulation(*mGrid, *mGridDataHandler);
          mAgencyTeams.push_back(a);
          AgencyFactory::addTeam(*mGrid, *a, mAgencies);
     }
     else if (type.canSubstitute("Activity")) {
          Activity* a = dynamic_cast<Activity*>(SOFactory::createSimulationObject(toAdd, initiator));
          a->prepareForSimulation(*mGrid, Simulation::simulationTime());
          mActivities.push_back(a);
     }
     else if (type.canSubstitute("Population")) {
          Error e("It isn't allowed to add cities during a simulation.");
          throw e;
     }
     else if (type.canSubstitute("Faction")) {
          Error e("It isn't allowed to add factions during a simulation.");
          throw e;
     }
     else {
          UpdatableSOAdapter::addObject(toAdd, initiator);
     }
}

/**
 * \brief Removes the SimulationObject referenced by the provided
 * Reference from this object.
 *
 * \param toRemove The Reference to the object to remove.
 * \param initiator The id of the initiator of the update.
 */
void Scenario::removeObject(const Reference& toRemove, int64_t initiator)
{
     SimulationObject* o = SOMapper::map(toRemove);

     if (Unit* a = dynamic_cast<Unit*>(o)) {
          mForces.erase(find(mForces.begin(), mForces.end(), a));
          SOFactory::removeSimulationObject(a, initiator);
     }
     else if (AgencyTeam* a = dynamic_cast<AgencyTeam*>(o)) {
          mAgencyTeams.erase(find(mAgencyTeams.begin(), mAgencyTeams.end(), a));
          AgencyFactory::removeTeam(*a, mAgencies);
          SOFactory::removeSimulationObject(a, initiator);
     }
     else if (Activity* a = dynamic_cast<Activity*>(o)) {
          mActivities.erase(find(mActivities.begin(), mActivities.end(), a));
          SOFactory::removeSimulationObject(a, initiator);
     }
     else if (dynamic_cast<City*>(o)) {
          Error e("It isn't allowed to remove cities during a simulation.");
          throw e;
     }
     else if (dynamic_cast<Faction*>(o)) {
          Error e("It isn't allowed to remove factions during a simulation.");
          throw e;
     }
     else {
          Error e;
          e << "Can't remove element '" << toRemove.name() << "' from '" << ref() << "'";
          throw e;
     }
}

/**
 * \brief Modifies this object with data from the provided DataObject.
 *
 * \param d The DataObject containing the new value.
 */
void Scenario::modify(const DataObject& d)
{
     const string& attr = d.identifier();
     if (attr == "HDI") {
          mHDI = d.getDouble();
          debug("Setting HDI to " << mHDI);
     }
     else if (attr == "unemployment") {
          mUnemployment = d.getDouble();
          debug("Setting unemployment to " << mUnemployment);
     }
     else {
          Error e;
          e << "No updatable attribute '" << attr << "' in '" << ref() << "'";
          throw e;
     }
     mGrid->updateParameters(mHDI, mUnemployment);
}

/**
 * \brief Extracts data from this object to the Buffer.
 *
 * \param b The Buffer to extract data to.
 */
void Scenario::extract(Buffer& b) const 
{
     DataObject& me = *b.map(ref());
     me.getChild("HDI")->setDouble(mHDI);
     me.getChild("unemployment")->setDouble(mUnemployment);
     mCombatGrid->unitsToGrid();
     b.extractGridData(mGridDataHandler);
}

/**
 * \brief Resets this object to the state it would have had if it was
 * created from the provided DataObject.
 *
 * \param d The DataObject to use as source for the reset.
 */
void Scenario::reset(const DataObject& d)
{
     Resetter<Activity>::reset(mActivities, d.getChild("activities")->objects());

     if (mMap) {
          delete mMap;
     }
     mMap = new Map(dynamic_cast<StratmasShape*>(d.getChild("map"))->getShapeRef());

     mDisease->reset(*d.getChild("disease"));

     // Reset elements
     Resetter<City>::reset(mCities, d.getChild("populationCenters")->objects());
     Resetter<Unit>::reset(mForces, d.getChild("militaryUnits")->objects());
     Resetter<AgencyTeam>::reset(mAgencyTeams, d.getChild("agencyTeams")->objects());

     // Reset factions - mNumEthnicFactions must be the same since it
     // isn't allowed to add ethnic factions during simulation.
     Resetter<Faction>::reset(mFactions, d.getChild("factions")->objects());

     Resetter<Region>::reset(mRegions, d.getChild("regions")->objects());

     // Delete Agencies - will be recreated in prepareForSimulation.
     for (vector<Agency*>::iterator it = mAgencies.begin(); it != mAgencies.end(); it++) {
          delete *it;
     }
     mAgencies.clear();

     mHDI = d.getChild("HDI")->getDouble();
     mUnemployment = d.getChild("unemployment")->getDouble();

     if (mGrid) { delete mGrid; }
     if (mCombatGrid) { delete mCombatGrid; }
}


/**
 * \brief Helper function used to find out which region contains which
 * cities. Not used during simulation but only when converting old
 * scenarios (taclan and other necessary files) files to new taclan2
 * files.
 */
void Scenario::createCityRegionMapping() {
     ofstream o("cities.csv", ios::trunc);
     o.setf(ios_base::fixed);
     o << "Name";
     for (int i = 1; i < mNumEthnicFactions + 1; i++) {
          o << "," << EthnicFaction::faction(i)->ref().name();
     }
     o << endl;

     RegionFile rf("regionFile.txt");
     
     cerr << rf << endl;

     for (vector<City*>::iterator it = mCities.begin(); it != mCities.end(); ++it) {
          City &city = **it;
          list<GridPos> l;
          city.location().cells(*mGrid, l);
          std::map<string, double> fraction;
          std::map<const Reference*, double> regionCount;
          std::map<const Reference*, double>::iterator rit;
          double totCount = 0;

          for (list<GridPos>::iterator lit = l.begin(); lit != l.end(); ++lit) {
               GridCell* gcp = mGrid->cell(*lit);
               const vector<const Region*>& v = gcp->regions();
               for(vector<const Region*>::const_iterator vit = v.begin(); vit != v.end(); ++vit) {
                    const Reference* refToRegion = &(*vit)->ref();
                    rit = regionCount.find(refToRegion);
                    if (rit == regionCount.end()) {
                         regionCount[refToRegion] = 0;
                    }
                    regionCount[refToRegion]++;
                    totCount++;
               }
          }

          for (int i = 1; i < mNumEthnicFactions + 1; ++i) {
               const EthnicFaction& fac = *EthnicFaction::faction(i);
               double frac = 0;
               for (rit = regionCount.begin(); rit != regionCount.end(); ++rit) {
                    frac += rf.fractionForRegionAndFaction(rit->first->name(), fac.ref().name()) * rit->second / totCount;
               }
               fraction[fac.ref().name()] = frac;
          }


          o << city.ref().name();
          for (int i = 1; i < mNumEthnicFactions + 1; ++i) {
               const EthnicFaction& fac = *EthnicFaction::faction(i);
               o << "," << city.population() * fraction[fac.ref().name()];
          }
          o << endl;
     }
     o.close();
}
