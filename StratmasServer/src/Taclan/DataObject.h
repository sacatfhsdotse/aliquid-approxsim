#ifndef STRATMAS_DATAOBJECT_H
#define STRATMAS_DATAOBJECT_H

// System
#include <iosfwd>
#include <string>
#include <vector>

// Xerces
#include <xercesc/util/XercesDefs.hpp>

// Own
#include "Referencable.h"
#include "Time2.h"

// Forward Declarations
class ContainerDataObject;
class Shape;
class Type;

namespace XERCES_CPP_NAMESPACE {
     class DOMElement;
}


/**
 * \brief This is the abstract super class for all types of
 * DataObjects. A DataObject is the kind of object that is used to
 * store the data that is communicated with the client.
 *
 * DataObjects are created according to the Stratmas xml schema. They
 * are then used to create the corresponding SimulationObjects
 *
 * \author   Per Alexius
 * \date     $Date: 2006/05/24 12:32:10 $
 */
class DataObject : public Referencable {
private:
     const Type& mType;              ///< The Type of the DataObject.
     ContainerDataObject* mParent;   ///< The parent of this DataObject.
protected:
     DataObject(const Reference& scope, const DOMElement* n);
     /**
      * \brief Constructor that creates a DataObject of the specified
      * Type with the provided Reference.
      *
      * \param ref The Reference to the DataObject tp be created.
      * \param type The Type of the DataObject to be created.
      */
     DataObject(const Reference& ref, const Type& type) : Referencable(ref), mType(type), mParent(0) {}

     /**
      * \brief Copy constuctor.
      *
      * \param c The DataObject to copy.
      */
     DataObject(const DataObject& c) : Referencable(c), mType(c.mType), mParent(0) {}
public:
     /// Destructor.
     virtual ~DataObject() {}

     /**
      * \brief Accessor for the Type.
      *
      * \return The Type of this DataObject.
      */
     const Type& getType() const { return mType; }
     const std::string& identifier() const;
     virtual const std::vector<DataObject*>& objects() const;

     /**
      * \brief Checks if this DataObject has children.
      *
      * \return True if this DataObject has children, false otherwise.
      */
     virtual bool hasChildren() const { return false; }

     /**
      * \brief Returns the child with the specified identifier or null
      * if there is no such child.
      *
      * \param name The identifier of the child to get.
      * \return The child with the specified identifier or null if
      * there is no such child.
      */
     virtual DataObject* getChild(const std::string& name) const { return 0; }

     /**
      * \brief Sets the parent of this DataObject.
      *
      * \param parent The new parent.
      */
     void setParent(ContainerDataObject* parent) { mParent = parent; }

     /**
      * \brief Accessor for the parent.
      *
      * \return The parent.
      */
     ContainerDataObject* getParent() const { return mParent; }

     virtual void setBool(bool v);
     virtual void setDouble(double v);
     virtual void setInt64_t(int64_t v);
     virtual void setTime(Time v);
     virtual void setString(const std::string& v);
     virtual void setReference(const Reference& v);
     virtual void setShape(const Shape* v);

     virtual bool             getBool() const;
     virtual double           getDouble() const;
     virtual int64_t          getInt64_t() const;
     virtual Time             getTime() const;
     virtual std::string      getString() const;
     virtual const Reference& getReference() const;
     virtual Shape*           getShape() const;

     virtual DataObject& operator = (const DataObject& d);

     /**
      * \brief For debugging purpooses.
      *
      * \param o The ostream to write to.
      */
     void print(std::ostream& o) const { print(o, ""); }
     virtual void print(std::ostream& o, const std::string indent) const;

     /**
      * \brief Creates a clone of this DataObject.
      *
      * \return A clone of this DataObject.
      */
     virtual DataObject* clone() const = 0;
     virtual void reg() const;
     virtual void dereg() const;

     /**
      * \brief Produces an XML representation of this DataObject
      * according to the xml schemas.
      *
      * \param o The ostream to print to.
      * \return The ostream with the XML representation written to it.
      */
     virtual std::ostream& toXML(std::ostream& o) const { return toXML(o, ""); }
     virtual std::ostream& toXML(std::ostream& o, std::string indent) const;

     /**
      * \brief Produces an XML representation of the body of this
      * DataObject according to the xml schemas.
      *
      * \param o The ostream to print to.
      * \param indent Intention string for readable output.
      * \return The ostream with the XML representation written to it.
      */
     virtual std::ostream& bodyXML(std::ostream& o, std::string indent) const = 0;

     /**
      * \brief For debugging purpooses.
      *
      * \param o The ostream to write to.
      * \param d The DataObject to write.
      * \return The ostream with the DataObject written to it.
      */
     friend std::ostream& operator << (std::ostream& o, const DataObject& d) { d.print(o); return o; }
};



/**
 * \brief Factory for creating DataObjects.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/05/24 12:32:10 $
 */
class DataObjectFactory {
public:
     static DataObject* createDataObject(const Reference& scope, const DOMElement* n);
     static DataObject* createDataObject(const Reference& ref, const Type& type);
     static DataObject* addOptional(DataObject& parent, const std::string& idToAdd);
     static void addObjectTo(const Reference& parent, DataObject& objToAdd);
};


#endif   // STRATMAS_DATAOBJECT_H
