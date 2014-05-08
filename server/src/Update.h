#ifndef APPROXSIM_UPDATE_H
#define APPROXSIM_UPDATE_H

// Xerces
#include <xercesc/util/XercesDefs.hpp>

// Forward Declarations
class DataObject;
class Reference;

namespace XERCES_CPP_NAMESPACE {
     class DOMElement;
}


XERCES_CPP_NAMESPACE_USE


/**
 * \brief Class representing an update sent by the client.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/05/24 12:32:12 $
 */
class Update {
private:
     /// The id of the initiator of the update.
     int64_t mInitiator;

     /// Reference to the object to be updated.
     const Reference* mReference;

     /// Reference to the object that should be notified of the update.
     const Reference* mTarget;

     /**
      * \brief The DataObject containing the update information except
      * for remove updates that does not need such information.
      */
     DataObject* mObject;

     /// The type of Update according to the eType enumeration.
     int mType;

     static const Reference* findReferenceToClosestComplexParent(const Reference& ref);

public:
     /// Enumeration for Update types.
     enum eType {eAdd, eRemove, eReplace, eModify};

     Update(const DOMElement& n, int64_t initiator);
     ~Update();

     /**
      * \brief Accessor for the initiator of this update.
      *
      * \return The initiator of this object.
      */
     inline int64_t getInitiator() const { return mInitiator; }

     /**
      * \brief Accessor for the Reference to the object the update
      * refers to.
      *
      * \return The Reference to the object the update refers to.
      */
     inline const Reference& getReference() const { return *mReference; }

     /**
      * \brief Gets the reference to the object that should be notified of
      * the update.
      *
      * \return The reference to the object that should be notified of the
      * update.
      */
     inline const Reference& getTargetRef() const { return *mTarget; }

     /**
      * \brief Accessor for the object the update refers to.
      *
      * \return The object the update refers to.
      */
     inline DataObject* getObject() const { return mObject; }

     /**
      * \brief Accessor for the type of this Update.
      *
      * \return The type of this Update as specified in the eType
      * enumeration.
      */
     inline int getType() const { return mType; }

     /**
      * \brief Gets the type of this Update as a string.
      *
      * \return The type of this Update as a string as specified in
      * the schema.
      */
     const char* getTypeAsString() const;
};


#endif   // APPROXSIM_UPDATE_H
