#ifndef STRATMAS_PARAMETERGROUP_H
#define STRATMAS_PARAMETERGROUP_H


// System
#include <iostream>
#include <map>
#include <string>
#include <vector>

// Own
#include "Buffer.h"
#include "DataObject.h"
#include "Error.h"
#include "SOFactory.h"
#include "Type.h"
#include "TypeFactory.h"
#include "UpdatableSOAdapter.h"
#include "Update.h"


/**
 * \brief Empty enumeration for ParameterGroups without parameters.
 */
enum NOTYPE {};


/**
 * \brief This struct defines a parameter entry to be used with the
 * TemplateParameterGroup class.
 *
 * \author Per Alexius
 * \date   $Date: 2006/09/04 14:39:16 $
 */
struct ParameterEntry {
     int index;
     const char* name;
     const char* type;
     double defaultValue;
};


/**
 * \brief This struct defines a parameter group entry to be used with
 * the TemplateParameterGroup class.
 *
 * \author Per Alexius
 * \date   $Date: 2006/09/04 14:39:16 $
 */
struct ParameterGroupEntry {
     const char* name;
};


/**
 * \brief Abstract ParameterGroup.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/09/04 14:39:16 $
 */
class ParameterGroup : public UpdatableSOAdapter {
public:
     ParameterGroup(const DataObject& d) : UpdatableSOAdapter(d) {}
     virtual ~ParameterGroup() {}

     /**
      * \brief Prepares this SimulationObject for simulation.
      *
      * Should be called after creation and reset and before the simulation
      * starts.
      */
     virtual void prepareForSimulation() = 0;
     std::ostream& printMe(std::ostream& o) const { return printMe(o, ""); }
     virtual std::ostream& printMe(std::ostream& o, std::string indent) const = 0;
};


/**
 * \brief Helper class which purpose is to facilitate creation of new
 * ParameterGroups.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/09/04 14:39:16 $
 */
template<class ENUM, int SIZE> class TemplateParameterGroup : public ParameterGroup {
private:
     const ParameterGroupEntry* const kGroupEntries;
     const int kNumGroupEntries;
     const ParameterEntry* const kEntries;

     std::map<std::string, ParameterGroup*> mParameterGroups;
     double mParameters[SIZE + 1];  // Hack in order to allow SIZE == 0
     std::map<std::string, ENUM> mNameToIndex;

     /**
      * \brief Sets values to default.
      */
     void setDefault() {
          for (int i = 0; i < SIZE; ++i) {
               mParameters[kEntries[i].index] = kEntries[i].defaultValue;
          }
     }

     void getDataFromDataObject(const DataObject& d) {
          const std::vector<DataObject*>& param = d.getChild("parameters")->objects();
          for (std::vector<DataObject*>::const_iterator it = param.begin(); it != param.end(); ++it) {
               typename std::map<std::string, ENUM>::iterator it2 = mNameToIndex.find((*it)->identifier());
               if (it2 != mNameToIndex.end()) {
                    mParameters[it2->second] = (*it)->getDouble();
               }
          }
     }

public:
     /**
      * Creates a parameter group from the provided DataObject. The
      * ParameterEntry list determines which parameters that are
      * required for this to be a ParameterGroup of the desired 'type'
      * 
      * \param d The DataObject to create this SimulationObject from.
      * \param entries The ParameterEntry list determining which
      * parameters that are required and types and default values.
      */
     TemplateParameterGroup(const DataObject& d,
                            const ParameterGroupEntry* const groupEntries,
                            int numGroupEntries,
                            const ParameterEntry* const entries)
          : ParameterGroup(d),
          kGroupEntries(groupEntries),
          kNumGroupEntries(numGroupEntries),
          kEntries(entries) {

          DataObject& pgs = *d.getChild("parameterGroups");
          for (int i = 0; i < kNumGroupEntries; ++i) {
               DataObject* pg = pgs.getChild(kGroupEntries[i].name);
               if (pg) {
                    mParameterGroups[kGroupEntries[i].name] =
                         dynamic_cast<ParameterGroup*>(SOFactory::createSimulationObject(*pg));
               }
               // Ignore non-required ParameterGroups.
          }

          DataObject& parameters = *d.getChild("parameters");
          for (int i = 0; i < SIZE; ++i) {
               DataObject* parameter = parameters.getChild(kEntries[i].name);
               if (parameter) {
                    mNameToIndex[kEntries[i].name] = static_cast<ENUM>(kEntries[i].index);
               }
          }

          setDefault();
          getDataFromDataObject(d);
     }

     /**
      * Destructor.
      */
     virtual ~TemplateParameterGroup() {
          for (std::map<std::string, ParameterGroup*>::iterator it = mParameterGroups.begin();
               it != mParameterGroups.end(); ++it) {
               SOFactory::removeSimulationObject(it->second);
          }
     }

     /**
      * \brief Prepares this SimulationObject for simulation.
      *
      * Should be called after creation and reset and before the simulation
      * starts.
      */
     virtual void prepareForSimulation() {
          ParameterGroup* pgp;
          // Add necessary parameter groups that wasn't present in the initial DataObject.
          for (int i = 0; i < kNumGroupEntries; ++i) {
               std::map<std::string, ParameterGroup*>::iterator it = mParameterGroups.find(kGroupEntries[i].name);
               if (it == mParameterGroups.end()) {
                    const Type& pgType = Mapper::map(ref())->getType(); // Type: ParameterGroup
                    const Reference& refToPgToBeCreated = 
                         Reference::get(Reference::get(ref(), "parameterGroups"), kGroupEntries[i].name);
                    pgp = dynamic_cast<ParameterGroup*>(SOFactory::createSimulationObject(refToPgToBeCreated, pgType));
                    mParameterGroups[kGroupEntries[i].name] = pgp;
               }
          }

          // Add necessary parameters that wasn't present in the initial DataObject.
          for (int i = 0; i < SIZE; ++i) {
               typename std::map<std::string, ENUM>::iterator it = mNameToIndex.find(kEntries[i].name);
               if (it == mNameToIndex.end()) {
                    const Type& pType = TypeFactory::getType(kEntries[i].type);
                    const Reference& refToPToBeCreated = Reference::get(Reference::get(ref(), "parameters"),
                                                                         kEntries[i].name);
                    SOFactory::createSimpleInList(refToPToBeCreated, pType);
                    mNameToIndex[kEntries[i].name] = static_cast<ENUM>(kEntries[i].index);
               }
          }

          for (std::map<std::string, ParameterGroup*>::iterator it = mParameterGroups.begin();
               it != mParameterGroups.end(); ++it) {
               it->second->prepareForSimulation();
          }
     }

     void addObject(DataObject& toAdd, int64_t initiator) {
          debug("ParameterGroup " << ref() << " ignoring added object " << toAdd.ref().name());
     }

     void removeObject(const Reference& toRemove, int64_t initiator) {
          typename std::map<std::string, ENUM>::iterator it = mNameToIndex.find(toRemove.name());
          if (it != mNameToIndex.end()) {
               // Mustn't remove required parameter so let's add it again.
               DataObject* d = Mapper::map(toRemove);
               DataObject* addAgain = d->clone();
               SOFactory::simulationObjectRemoved(toRemove, initiator);
               SOFactory::createSimple(*addAgain);
          }
          else {
               typename std::map<std::string, ParameterGroup*>::iterator it = mParameterGroups.find(toRemove.name());
               if (it != mParameterGroups.end()) {
                    // Mustn't remove required parameter groups so let's add it again.
                    DataObject* d = Mapper::map(toRemove);
                    DataObject* addAgain = d->clone();
                    SOFactory::removeSimulationObject(it->second, initiator);
                    mParameterGroups[toRemove.name()] =
                         dynamic_cast<ParameterGroup*>(SOFactory::createSimulationObject(*addAgain));
               }
          }
     }

     void replaceObject(DataObject& newObject, int64_t initiator) {
          debug("ParameterGroup " << ref() << " ignoring to replace object " << newObject.identifier());
     }

     void modify(const DataObject& d) {
          typename std::map<std::string, ENUM>::iterator it = mNameToIndex.find(d.identifier());
          if (it != mNameToIndex.end()) {
               mParameters[it->second] = d.getDouble();
               debug("Setting " << it->first << " to " << mParameters[it->second]);
          }
          else {
               debug("No updatable attribute '" << d.identifier() << "' in '" << ref() << "'. Ignoring...");
          }
     }

     /**
      * \brief Extracts data from this object to the Buffer.
      *
      * \param b The Buffer to extract data to.
      */
     void extract(Buffer &b) const {
          DataObject& parameters = *b.map(ref())->getChild("parameters");
          for (int i = 0; i < SIZE; ++i) {
               DataObject* parameter = parameters.getChild(kEntries[i].name);
               if (parameter) {
                    parameter->setDouble(mParameters[kEntries[i].index]);
               }
          }
     }

     /**
      * \brief Resets this object to the state it would have had if it
      * was created from the provided DataObject.
      *
      * \param d The DataObject to use as source for the reset.
      */
     void reset(const DataObject& d) { setDefault(); getDataFromDataObject(d); }

     /**
      * \brief Accessor for parameters.
      *
      * \param i The index of the parameter as specified in kEntries.
      * \return The value of the given parameter.
      */
     double param(ENUM i) const { return mParameters[i]; }

     std::ostream& printMe(std::ostream& o, std::string indent) const {
          o << indent << ref().name();
          indent += "  ";
          for (std::map<std::string, ParameterGroup*>::const_iterator it = mParameterGroups.begin();
               it != mParameterGroups.end(); ++it) {
               o << std::endl;
               it->second->printMe(o, indent);
          }
          for (typename std::map<std::string, ENUM>::const_iterator it = mNameToIndex.begin(); it != mNameToIndex.end(); ++it) {
               o << std::endl << indent << it->first << ": " << mParameters[it->second];
          }
          return o;
     }
};

#endif   // STRATMAS_PARAMETERGROUP_H
