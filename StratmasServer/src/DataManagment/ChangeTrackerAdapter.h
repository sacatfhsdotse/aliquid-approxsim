#ifndef APPROXSIM_CHANGETRACKERADAPTER
#define APPROXSIM_CHANGETRACKERADAPTER

// System
#include <map>
#include <string>

// Own
#include "SOFactoryListener.h"
#include "Time2.h"

// Forward Declarations
class DataObject;
class ContainerDataObject;
class Reference;
class SimpleType;
class ApproxsimBool;
class ApproxsimDouble;
class ApproxsimInt64_t;
class ApproxsimReference;
class ApproxsimShape;
class ApproxsimString;
class ApproxsimTime;
class SymbolIDCode;



/**
 * \brief This is the abstract super class for all types of
 * ChangeTrackerAdapters. A ChangeTrackerAdapter is an object that is
 * used to keep track of changes in DataObjects. They are used in
 * order to deliver no more than the necessary update information to
 * the clients.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/02 17:06:51 $
 */
class ChangeTrackerAdapter {
private:
public:
     virtual ~ChangeTrackerAdapter() {}

     /**
      * \brief Checks if the DataObject this adapter adapts has
      * changed since the last call to the toXML() function.
      *
      * \return True if the DataObject this adapter adapts has changed
      * since the last call to the toXML() function, false otherwise.
      */
     virtual bool changed() const = 0;

     /**
      * \brief Produces an XML representation of the changes in the
      * object this adapter adapts according to the Approxsim xml
      * schema.
      *
      * \param o The ostream to write the XML representation to.
      * \return The provided ostream with the XML representation
      * written to it.
      */
     std::ostream& toXML(std::ostream& o) { return toXML(o, ""); }

     /**
      * \brief Produces an XML representation of the changes in the
      * object this adapter adapts according to the Approxsim xml
      * schema. An indentation may be specified to increase
      * readability
      *
      * \param o The ostream to write the XML representation to.
      * \param indent The whitespace indentation.
      * \return The provided ostream with the XML representation
      * written to it.
      */
     virtual std::ostream& toXML(std::ostream& o, std::string indent) = 0;
};



/**
 * \brief The BoolChangeTrackerAdapter keeps track of changes in
 * ApproxsimBool objects.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/02 17:06:51 $
 */
class BoolChangeTrackerAdapter : public ChangeTrackerAdapter {
private:
     ApproxsimBool& mObject;   ///< The adapted DataObject.
     mutable bool mLast;      ///< The last value written.
public:
     BoolChangeTrackerAdapter(ApproxsimBool& v);
     bool changed() const;
     std::ostream& toXML(std::ostream& o, std::string indent);
};



/**
 * \brief The DoubleChangeTrackerAdapter keeps track of changes in
 * ApproxsimDouble objects.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/02 17:06:51 $
 */
class DoubleChangeTrackerAdapter : public ChangeTrackerAdapter {
private:
     ApproxsimDouble& mObject;   ///< The adapted DataObject.
     mutable double mLast;      ///< The last value written.
public:
     DoubleChangeTrackerAdapter(ApproxsimDouble& v);
     bool changed() const;
     std::ostream& toXML(std::ostream& o, std::string indent);
};



/**
 * \brief The Int64_tChangeTrackerAdapter keeps track of changes in
 * ApproxsimInt64_t objects.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/02 17:06:51 $
 */
class Int64_tChangeTrackerAdapter : public ChangeTrackerAdapter {
private:
     ApproxsimInt64_t& mObject;   ///< The adapted DataObject.
     mutable int64_t mLast;      ///< The last value written.
public:
     Int64_tChangeTrackerAdapter(ApproxsimInt64_t& v);
     bool changed() const;
     std::ostream& toXML(std::ostream& o, std::string indent);
};



/**
 * \brief The ReferenceChangeTrackerAdapter keeps track of changes in
 * ApproxsimReference objects.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/02 17:06:51 $
 */
class ReferenceChangeTrackerAdapter : public ChangeTrackerAdapter {
private:
     ApproxsimReference& mObject;       ///< The adapted DataObject.
     mutable const Reference* mLast;   ///< The last value written.
public:
     ReferenceChangeTrackerAdapter(ApproxsimReference& v);
     bool changed() const;
     std::ostream& toXML(std::ostream& o, std::string indent);
};



/**
 * \brief The ShapeChangeTrackerAdapter keeps track of changes in
 * ApproxsimShape objects.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/02 17:06:51 $
 */
class ShapeChangeTrackerAdapter : public ChangeTrackerAdapter {
private:
     ApproxsimShape& mObject;   ///< The adapted DataObject.
     /// The change count of the last Shape written.
     mutable int mLast;
public:
     ShapeChangeTrackerAdapter(ApproxsimShape& v);
     bool changed() const;
     std::ostream& toXML(std::ostream& o, std::string indent);
};



/**
 * \brief The StringChangeTrackerAdapter keeps track of changes in
 * ApproxsimString objects.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/02 17:06:51 $
 */
class StringChangeTrackerAdapter : public ChangeTrackerAdapter {
private:
     ApproxsimString& mObject;     ///< The adapted DataObject.
     mutable std::string mLast;   ///< The last value written.
public:
     StringChangeTrackerAdapter(ApproxsimString& v);
     bool changed() const;
     std::ostream& toXML(std::ostream& o, std::string indent);
};



/**
 * \brief The TimeChangeTrackerAdapter keeps track of changes in
 * ApproxsimTime objects.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/02 17:06:51 $
 */
class TimeChangeTrackerAdapter : public ChangeTrackerAdapter {
private:
     ApproxsimTime& mObject;   ///< The adapted DataObject.
     mutable Time mLast;      ///< The last value written.
public:
     TimeChangeTrackerAdapter(ApproxsimTime& v);
     bool changed() const;
     std::ostream& toXML(std::ostream& o, std::string indent);
};



/**
 * \brief The SymbolIDCodeChangeTrackerAdapter keeps track of changes
 * in ApproxsimSymbolIDCode objects.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/02 17:06:51 $
 */
class SymbolIDCodeChangeTrackerAdapter : public ChangeTrackerAdapter {
private:
     SymbolIDCode& mObject;       ///< The adapted DataObject.
     mutable std::string mLast;   ///< The last value written.
public:
     SymbolIDCodeChangeTrackerAdapter(SymbolIDCode& v);
     bool changed() const;
     std::ostream& toXML(std::ostream& o, std::string indent);
};



/**
 * \brief The ContainerChangeTrackerAdapter keeps track of changes in
 * ApproxsimContainer objects.
 *
 * ContainerChangeTrackerAdapter gets notified by the SOFactory when
 * SimulationObjects are created, deleted or replaced.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/02 17:06:51 $
 */
class ContainerChangeTrackerAdapter : public ChangeTrackerAdapter, public SOFactoryListener {
private:
     /**
      * \brief The id of the Session this
      * ContainerChangeTrackerAdapter belongs to. Used in order to
      * keep track of which changes we should register and which
      * changes that our own Session was originator to.
      */
     int64_t mId;

     /// The Reference to the adapted ContainerDataObject.
     const Reference& mReference;

     /**
      * \brief Caches the result from the changed() function until the
      * next call to toXML().
      */
     mutable bool mChanged;

     /**
      * \brief When a child to the DataObject this adapter adapts is
      * added, removed or replaced, this is stored in the mChanges
      * map. See objectAdded() for more information.
      */
     std::map<const Reference*, char> mChanges;

     /// The child adapters.
     std::map<const Reference*, ChangeTrackerAdapter*> mChildren;

     bool addable(const Reference& ref);
     void addChild(const Reference& ref);
     void removeChild(const Reference& ref);
public:
     ContainerChangeTrackerAdapter(const Reference& ref, int64_t id);
     virtual ~ContainerChangeTrackerAdapter();

     void objectAdded(const Reference& ref, int64_t initiator);
     void objectRemoved(const Reference& ref, int64_t initiator);

     bool changed() const;
     virtual std::ostream& toXML(std::ostream& o, std::string indent);
};



/**
 * \brief The ChangeTrackerAdapterFactory is used to create
 * ChangeTrackerAdapters.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/02 17:06:51 $
 */
class ChangeTrackerAdapterFactory {
public:
     static ChangeTrackerAdapter* createChangeTrackerAdapter(const Reference& r, int64_t id);
};

#endif   // APPROXSIM_CHANGETRACKERADAPTER
