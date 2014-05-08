// System
#include <ostream>
#include <vector>
#include <cstdlib>

// Own
#include "AccessRightHandler.h"
#include "ChangeTrackerAdapter.h"
#include "DataObjectImpl.h"
#include "Error.h"
#include "Mapper.h"
#include "Reference.h"
#include "Shape.h"
#include "SOFactory.h"
#include "TypeFactory.h"


using namespace std;


/**
 * \brief Helper for producing XML representations of adapters for
 * ValueType descendants.
 *
 * \param o The ostream to write to.
 * \param d The adapted DataObject.
 * \param indent The whitespace indentation.
 * \return The provided ostream with the XML representation written to
 * it.
 */
ostream& XMLize(ostream& o, const DataObject& d, string indent)
{
     o << indent << "<update xsi:type=\"sp:UpdateModify\" identifier=\"" << d.identifier() << "\">" << endl;
     o << indent << INDENT << "<newValue xsi:type=\"sp:" << d.getType().getName() << "\">" << endl;
     d.bodyXML(o, indent + INDENT + INDENT);
     o << indent << INDENT << "</newValue>" << endl;
     o << indent << "</update>" << endl;
     return o;
}


/**
 * \brief Creates a ChangeTrackerAdapter for the provided DataObject.
 *
 * \param v The DataObject to track changes for.
 */
BoolChangeTrackerAdapter::BoolChangeTrackerAdapter(ApproxsimBool& v) : mObject(v), mLast(mObject.getBool())
{
}

bool BoolChangeTrackerAdapter::changed() const
{
     return (mLast != mObject.getBool());
}

ostream& BoolChangeTrackerAdapter::toXML(ostream& o, string indent)
{
     mLast = mObject.getBool();
     return XMLize(o, mObject, indent);
}



/**
 * \brief Creates a ChangeTrackerAdapter for the provided DataObject.
 *
 * \param v The DataObject to track changes for.
 */
DoubleChangeTrackerAdapter::DoubleChangeTrackerAdapter(ApproxsimDouble& v) : mObject(v), mLast(mObject.getDouble())
{
}

bool DoubleChangeTrackerAdapter::changed() const
{
     return (mLast != mObject.getDouble());
}

ostream& DoubleChangeTrackerAdapter::toXML(ostream& o, string indent)
{
     mLast = mObject.getDouble();
     return XMLize(o, mObject, indent);
}



/**
 * \brief Creates a ChangeTrackerAdapter for the provided DataObject.
 *
 * \param v The DataObject to track changes for.
 */
Int64_tChangeTrackerAdapter::Int64_tChangeTrackerAdapter(ApproxsimInt64_t& v) : mObject(v), mLast(mObject.getInt64_t())
{
}

bool Int64_tChangeTrackerAdapter::changed() const
{
     return (mLast != mObject.getInt64_t());
}

ostream& Int64_tChangeTrackerAdapter::toXML(ostream& o, string indent)
{
     mLast = mObject.getInt64_t();
     return XMLize(o, mObject, indent);
}



/**
 * \brief Creates a ChangeTrackerAdapter for the provided DataObject.
 *
 * \param v The DataObject to track changes for.
 */
ReferenceChangeTrackerAdapter::ReferenceChangeTrackerAdapter(ApproxsimReference& v)
     : mObject(v), mLast(&mObject.getReference())
{
}

bool ReferenceChangeTrackerAdapter::changed() const
{
     return !(*mLast == mObject.getReference());
}

ostream& ReferenceChangeTrackerAdapter::toXML(ostream& o, string indent)
{
     mLast = &mObject.getReference();
     return XMLize(o, mObject, indent);
}



/**
 * \brief Creates a ChangeTrackerAdapter for the provided DataObject.
 *
 * \param v The DataObject to track changes for.
 */
ShapeChangeTrackerAdapter::ShapeChangeTrackerAdapter(ApproxsimShape& v)
     : mObject(v), mLast(mObject.getShapeRef().changes())
{
}

bool ShapeChangeTrackerAdapter::changed() const
{
     return (mLast != mObject.getShapeRef().changes());
}

ostream& ShapeChangeTrackerAdapter::toXML(ostream& o, string indent)
{
     mLast = mObject.getShapeRef().changes();
     return XMLize(o, mObject, indent);
}



/**
 * \brief Creates a ChangeTrackerAdapter for the provided DataObject.
 *
 * \param v The DataObject to track changes for.
 */
StringChangeTrackerAdapter::StringChangeTrackerAdapter(ApproxsimString& v) : mObject(v), mLast(mObject.getString())
{
}

bool StringChangeTrackerAdapter::changed() const
{
     return (mLast != mObject.getString());
}

ostream& StringChangeTrackerAdapter::toXML(ostream& o, string indent)
{
     mLast = mObject.getString();
     return XMLize(o, mObject, indent);
}



/**
 * \brief Creates a ChangeTrackerAdapter for the provided DataObject.
 *
 * \param v The DataObject to track changes for.
 */
SymbolIDCodeChangeTrackerAdapter::SymbolIDCodeChangeTrackerAdapter(SymbolIDCode& v) : mObject(v), mLast(mObject.getString())
{
}

bool SymbolIDCodeChangeTrackerAdapter::changed() const
{
     return (mLast != mObject.getString());
}

ostream& SymbolIDCodeChangeTrackerAdapter::toXML(ostream& o, string indent)
{
     mLast = mObject.getString();
     return XMLize(o, mObject, indent);
}



/**
 * \brief Creates a ChangeTrackerAdapter for the provided DataObject.
 *
 * \param v The DataObject to track changes for.
 */
TimeChangeTrackerAdapter::TimeChangeTrackerAdapter(ApproxsimTime& v) : mObject(v), mLast(mObject.getTime())
{
}

bool TimeChangeTrackerAdapter::changed() const
{
     return (mLast != mObject.getTime());
}

ostream& TimeChangeTrackerAdapter::toXML(ostream& o, string indent)
{
     mLast = mObject.getTime();
     return XMLize(o, mObject, indent);
}



/**
 * \brief Creates a ContainerChangeTrackerAdapter for the DataObject
 * the provided Reference refers to..
 *
 * \param ref The Reference to the DataObject to track changes for.
 * \param id The id of the Session this adapter belongs to.
 */
ContainerChangeTrackerAdapter::ContainerChangeTrackerAdapter(const Reference& ref, int64_t id)
     : mId(id), mReference(ref), mChanged(false)
{
     DataObject* d = Mapper::map(mReference);
     // It is a valid operation to create a new
     // ContainerChangeTrackerAdapter without a corresponding
     // DataObject. Objects may be added later on. This is practiced
     // for the 'virtual' root object i.e. there is no corresponding
     // root DataObject but when the Simulation gets created (or
     // explicitly added) we will be notified.
     if (d) {
          for (vector<DataObject*>::const_iterator it = d->objects().begin(); it != d->objects().end(); it++) {
               addChild((*it)->ref());
          }
     }
     SOFactory::addListener(mReference, *this);
}

ContainerChangeTrackerAdapter::~ContainerChangeTrackerAdapter()
{
     SOFactory::removeListener(mReference, *this);
     for (std::map<const Reference*, ChangeTrackerAdapter*>::iterator it =  mChildren.begin(); it != mChildren.end(); it++) {
          delete it->second;
     }
}

/**
 * \brief Checks if we need to add an adapter for the object
 * referenced by the provided Reference.
 *
 * For example - we don't have to create adapters for objects that
 * won't change, such as Cities (Populations).
 *
 * \param ref The Reference to the object to check.
 * \return True if we need an adapter for the object refered to by
 * ref, false otherwise.
 */
bool ContainerChangeTrackerAdapter::addable(const Reference& ref)
{
     return AccessRightHandler::changeable(ref);
}

/**
 * \brief Adds a child adapter (if necessary) to this adapter.
 *
 * \param ref The Reference to the object to add an adapter for.
 */
void ContainerChangeTrackerAdapter::addChild(const Reference& ref)
{
     if (addable(ref)) {
          if (mChildren.find(&ref) == mChildren.end()) {
               mChildren[&ref] = ChangeTrackerAdapterFactory::createChangeTrackerAdapter(ref, mId);
          }
          else {
               Error e;
               e << "Tried to add ChangeTrackerAdapter for object '" << ref << "' twice.";
               throw e;
          }
     }
}

/**
 * \brief Removes a child adapter from this adapter.
 *
 * \param ref The Reference to the object to remove an adapter for.
 */
void ContainerChangeTrackerAdapter::removeChild(const Reference& ref)
{
     if (addable(ref)) {
          std::map<const Reference*, ChangeTrackerAdapter*>::iterator it = mChildren.find(&ref);
          if (it != mChildren.end()) {
               delete it->second;
               mChildren.erase(it);
          }
          else {
               approxsimDebug("Removed change adapter for " << ref << " twice!!! But it's ok...");
          }
     }
}

/**
 * \brief Notifies this change tracker that an object has been added.
 *
 * A ContainerChangeTrackerAdapter only needs to store one entry per
 * child (optionals and listelements) about what has happend to the
 * object since the last call to toXML(). The table below shows how an
 * entry will change depending on current state and the event that
 * occurred (0 = nothing, a = add, r = remove, x = exchange/replace).
 *
 * <p><table>
 * <tr> <th>Gets</th> <th></th> <th>a</th> <th>r</th> <th>x</th> </tr>
 * <tr> <th>Has</th> <th>0</th> <td>a</td> <td>r</td> <td>x</td> </tr>
 * <tr> <th></th> <th>a</th> <td>-</td> <td>0</td> <td>a</td> </tr>
 * <tr> <th></th> <th>r</th> <td>x</td> <td>-</td> <td>-</td> </tr>
 * <tr> <th></th> <th>x</th> <td>-</td> <td>r</td> <td>r</td> </tr>
 * </table>
 *
 * \param ref The reference to the added object
 * \param initiator The initiator of the event.
 */
void ContainerChangeTrackerAdapter::objectAdded(const Reference& ref, int64_t initiator)
{
     // Shouldn't count updates from the client this object is
     // tracking changes for as changes.
     if (initiator == mId) {
          addChild(ref);
     }
     else {
          std::map<const Reference*, char>::iterator it = mChanges.find(&ref);
          if (it == mChanges.end()) {
               mChanges[&ref] = 'a';
               approxsimDebug("---Object " << ref << " added -> 'a'");
          }
          else if (it->second == 'r') {
               mChanges[&ref] = 'x';
               approxsimDebug("---Object " << ref << " added -> 'x'");
          }
          else {
               Error e;
               e << "Tried to add '" << ref << "' to '" << mReference;
               e << "' that already has an 'added' entry for that object.";
               throw e;
          }
          // Adapter for the new object will be added during toXML()-call.
     }
}

/**
 *  \brief Notifies this change tracker that an object has been
 *  removed. See objectAdded() for more information.
 *
 * \param ref The reference to the removed object
 * \param initiator The initiator of the event.
 */
void ContainerChangeTrackerAdapter::objectRemoved(const Reference& ref, int64_t initiator)
{
     // Shouldn't count updates from the client this object is
     // tracking changes for as changes.
     if (initiator != mId) {
          // We shouldn't save changes for pruned branches.
          std::map<const Reference*, ChangeTrackerAdapter*>::iterator it = mChildren.find(&ref);
          if (it != mChildren.end()) {
               std::map<const Reference*, char>::iterator it = mChanges.find(&ref);
               if (it == mChanges.end() || it->second == 'x') {
                    mChanges[&ref] = 'r';
               }
               else if (it->second == 'a') {
                    mChanges.erase(&ref);
               }
               else {
                    Error e;
                    e << "Tried to remove '" << ref << "' to '" << mReference;
                    e << "' that already has a 'remove' entry for that object.";
                    throw e;
               }
          }
     }
     // Always remove adapter directly.
     removeChild(ref);
}

/**
 * \brief Checks if the DataObject this adapter adapts has
 * changed since the last call to the toXML() function.
 *
 * The adapted ContainerDataObject is considered to be changed if any
 * of its children has been modifed, added, removed or replaced.
 *
 * \return True if the ContainerDataObject this adapter adapts has
 * changed since the last call to the toXML() function, false
 * otherwise.
 */
bool ContainerChangeTrackerAdapter::changed() const
{
     if (!mChanged) {
          if (!mChanges.empty()) {
               mChanged = true;
          }
          else {
               for (std::map<const Reference*, ChangeTrackerAdapter*>::const_iterator it = mChildren.begin();
                    it != mChildren.end(); it++) {
                    if (it->second->changed()) {
                         mChanged = true;
                         break;
                    }
               }
          }
     }
     return mChanged;
}

ostream& ContainerChangeTrackerAdapter::toXML(ostream& o, string indent)
{
     o << indent << "<update xsi:type=\"sp:UpdateScope\" identifier=\"" << mReference.name() << "\">" << endl;

     // First handle changed objects. Notice that there should be no
     // change adapters for added (yet) or removed objects.
     for (std::map<const Reference*, ChangeTrackerAdapter*>::iterator it = mChildren.begin(); it != mChildren.end(); it++) {
          ChangeTrackerAdapter& child = *it->second;
          if (child.changed()) {
               child.toXML(o, indent + INDENT);
          }
     }
     // Handle added, removed and replaced obejcts.
     for (std::map<const Reference*, char>::iterator it = mChanges.begin(); it != mChanges.end(); it++) {
          if (it->second == 'a') {
               DataObject& added = *Mapper::map(*it->first);
               o << indent << INDENT << "<update xsi:type=\"sp:UpdateAdd\" identifier=\"" << added.identifier() << "\">" << endl;
               o << indent << INDENT << INDENT << "<identifiable xsi:type=\"sp:" << added.getType().getName()
                 << "\" identifier=\"" << added.identifier() << "\">" << endl;
               added.bodyXML(o, indent + INDENT + INDENT + INDENT);
               o << indent << INDENT << INDENT << "</identifiable>" << endl;
               o << indent << INDENT << "</update>" << endl;

               // Change tracker adapter for added object.
               addChild(*it->first);
          }
          else if (it->second == 'r') {
               o << indent << INDENT<< "<update xsi:type=\"sp:UpdateRemove\" identifier=\""
                 << it->first->name() << "\"></update>" << endl;
          }
          else {
               DataObject& r = *Mapper::map(*it->first);
               o << indent << INDENT << "<update xsi:type=\"sp:UpdateReplace\" identifier=\"" << r.identifier() << "\">" << endl;
               o << indent << INDENT << INDENT << "<newObject xsi:type=\"sp:" << r.getType().getName() << "\">" << endl;
               r.bodyXML(o, indent + INDENT + INDENT + INDENT);
               o << indent << INDENT << INDENT << "</newObject>" << endl;
               o << indent << INDENT << "</update>" << endl;

               // Change tracker adapter for added object. It is here
               // assumed that replace is implemented as remove + add
               // for ChangeTrackerAdapters.
               addChild(*it->first);
          }
     }
     o << indent << "</update>" << endl;

     mChanged = false;
     mChanges.clear();
     return o;
}



/**
 * \brief Creates a ChangeTrackerAdapter for the object refered to by
 * the provided Reference.
 *
 * \param r The Reference to the DataObject to adapt.
 * \param id The id of the Session the created adapter will belong to.
 */
ChangeTrackerAdapter* ChangeTrackerAdapterFactory::createChangeTrackerAdapter(const Reference& r, int64_t id)
{
     ChangeTrackerAdapter* ret = 0;
     DataObject* d = Mapper::map(r);
     if (d) {
           if (ApproxsimBool* o = dynamic_cast<ApproxsimBool*>(d)) {
                ret = new BoolChangeTrackerAdapter(*o);
           }
           else if (ApproxsimDouble* o = dynamic_cast<ApproxsimDouble*>(d)) {
                ret = new DoubleChangeTrackerAdapter(*o);
           }
           else if (ApproxsimInt64_t* o = dynamic_cast<ApproxsimInt64_t*>(d)) {
                ret = new Int64_tChangeTrackerAdapter(*o);
           }
          else if (ApproxsimReference* o = dynamic_cast<ApproxsimReference*>(d)) {
               ret = new ReferenceChangeTrackerAdapter(*o);
          }
          else if (ApproxsimShape* o = dynamic_cast<ApproxsimShape*>(d)) {
               ret = new ShapeChangeTrackerAdapter(*o);
          }
           else if (ApproxsimString* o = dynamic_cast<ApproxsimString*>(d)) {
                ret = new StringChangeTrackerAdapter(*o);
           }
          else if (SymbolIDCode* o = dynamic_cast<SymbolIDCode*>(d)) {
               ret = new SymbolIDCodeChangeTrackerAdapter(*o);
          }
           else if (ApproxsimTime* o = dynamic_cast<ApproxsimTime*>(d)) {
                ret = new TimeChangeTrackerAdapter(*o);
           }
          else if (dynamic_cast<ContainerDataObject*>(d)) {
               ret = new ContainerChangeTrackerAdapter(r, id);
          }
          else {
               Error e;
               e << "No ChangeTrackerAdapter available for object " << d->ref();
               throw e;
          }
     }
     else {
          Error e;
          e << "Couldn't find DataObject for reference: '" << r << "'";
          throw e;
     }
     return ret;
}
