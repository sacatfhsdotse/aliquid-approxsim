#ifndef APPROXSIM_DATAOBJECTIMPL_H
#define APPROXSIM_DATAOBJECTIMPL_H

// System
#include <map>
#include <memory>

// Own
#include "DataObject.h"

// Forward Declarations
class Declaration;


/**
 * \brief ContainerDataObject is the abstract super class for all
 * lists and complex objects in the Approxsim xml schema except
 * ValueType descendants.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/27 09:43:40 $
 */
class ContainerDataObject : public DataObject {
protected:
     /**
      * \brief A vector with the children of this container. The order
      * of the children is the same as in the xml schema.
      */
     std::vector<DataObject*> mObjects;

     /// Maps a child's identifier to the child itself.
     std::map<std::string, DataObject*> mObjectMap;

     /**
      * \brief Copy constructor.
      *
      * \param c The DataObject to copy.
      */
     ContainerDataObject(const ContainerDataObject& c);

     /**
      * \brief Constructor that creates a DataObject in the provided scope
      * from the provided DOMElement.
      *
      * \param scope A Reference the scope to create the DataObject in.
      * \param n The DOMElement to create this DataObject from.
      */
     ContainerDataObject(const Reference& scope, const DOMElement* n) : DataObject(scope, n) {}

     /**
      * \brief Constructor that creates a DataObject of the specified
      * Type with the provided Reference.
      *
      * \param ref The Reference to the DataObject tp be created.
      * \param type The Type of the DataObject to be created.
      */
     ContainerDataObject(const Reference& ref, const Type& type) : DataObject(ref, type) {}
public:
     virtual ~ContainerDataObject();

     /**
      * \brief Returns the children of this DataObject.
      *
      * \return The children of this DataObject.
      */
     const std::vector<DataObject*>& objects() const { return mObjects; }
     bool hasChildren() const { return !objects().empty(); }
     void add(DataObject* o);
     void remove(const std::string& name);
     void replace(DataObject* o);

     DataObject* getChild(const std::string& name) const;
     void reg() const;
     void dereg() const;
     std::ostream& bodyXML(std::ostream& o, std::string indent) const;
     virtual void print(std::ostream& o, const std::string indent) const;
};



/**
 * \brief ComplexDataObjects represent complex objects in the Approxsim
 * xml schema except ValueType descendants.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/27 09:43:40 $
 */
class ComplexDataObject : public ContainerDataObject {
protected:
     /**
      * \brief Copy constructor.
      *
      * \param c The DataObject to copy.
      */
     ComplexDataObject(const ComplexDataObject& c) : ContainerDataObject(c) {}
public:
     ComplexDataObject(const Reference& scope, const DOMElement* n);

     /**
      * \brief Constructor that creates a DataObject of the specified
      * Type with the provided Reference.
      *
      * \param ref The Reference to the DataObject tp be created.
      * \param type The Type of the DataObject to be created.
      */
     ComplexDataObject(const Reference& ref, const Type& type);

     void orderPreservingAdd(DataObject* o);
     DataObject* clone() const { return new ComplexDataObject(*this); }
};



/**
 * \brief DataObjectsList represent lists in the Approxsim xml schema.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/27 09:43:40 $
 */
class DataObjectList : public ContainerDataObject {
private:
protected:
     /**
      * \brief Copy constructor.
      *
      * \param c The DataObject to copy.
      */
     DataObjectList(const DataObjectList& c) : ContainerDataObject(c) {}
public:
     DataObjectList(const Reference& scope, const Declaration& dec, const DOMElement* n);

     /**
      * \brief Constructor that creates a DataObject of the specified
      * Type with the provided Reference.
      *
      * \param ref The Reference to the DataObject tp be created.
      * \param type The Type of the DataObject to be created.
      */
     DataObjectList(const Reference& ref, const Type& type) : ContainerDataObject(ref, type) {}
     DataObject* clone() const { return new DataObjectList(*this); }
     std::ostream& toXML(std::ostream& o, std::string indent) const;
};
     


/**
 * \brief ApproxsimBool corresponds to the Boolean type in the Approxsim
 * xml schema.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/27 09:43:40 $
 */
class ApproxsimBool : public DataObject {
private:
     bool mValue;   ///< The value.
protected:
     /**
      * \brief Copy constructor.
      *
      * \param c The DataObject to copy.
      */
     ApproxsimBool(const ApproxsimBool& c) : DataObject(c), mValue(c.mValue) {}
public:
     ApproxsimBool(const Reference& scope, const DOMElement* n);

     /**
      * \brief Constructor that creates a DataObject of the specified
      * Type with the provided Reference.
      *
      * \param ref The Reference to the DataObject tp be created.
      * \param type The Type of the DataObject to be created.
      */
     ApproxsimBool(const Reference& ref, const Type& type) : DataObject(ref, type), mValue(0) {}
     bool getBool() const { return mValue; }
     void setBool(bool v) { mValue = v; }

     DataObject& operator = (const DataObject& d);

     DataObject* clone() const { return new ApproxsimBool(*this); }
     std::ostream& bodyXML(std::ostream& o, std::string indent) const;
     void print(std::ostream& o, const std::string indent) const;
};



/**
 * \brief ApproxsimDouble corresponds to the Double type in the
 * Approxsim xml schema.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/27 09:43:40 $
 */
class ApproxsimDouble : public DataObject {
private:
     double mValue;   ///< The value.
protected:
     /**
      * \brief Copy constructor.
      *
      * \param c The DataObject to copy.
      */
     ApproxsimDouble(const ApproxsimDouble& c) : DataObject(c), mValue(c.mValue) {}
public:
     ApproxsimDouble(const Reference& scope, const DOMElement* n);

     /**
      * \brief Constructor that creates a DataObject of the specified
      * Type with the provided Reference.
      *
      * \param ref The Reference to the DataObject tp be created.
      * \param type The Type of the DataObject to be created.
      */
     ApproxsimDouble(const Reference& ref, const Type& type) : DataObject(ref, type), mValue(0) {}
     double getDouble() const { return mValue; }
     void setDouble(double v) { mValue = v; }

     DataObject& operator = (const DataObject& d);

     DataObject* clone() const { return new ApproxsimDouble(*this); }
     std::ostream& bodyXML(std::ostream& o, std::string indent) const;
     void print(std::ostream& o, const std::string indent) const;
};



/**
 * \brief ApproxsimInt64_t corresponds to the NonNegativeInteger type
 * in the Approxsim xml schema.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/27 09:43:40 $
 */
class ApproxsimInt64_t : public DataObject {
private:
     int64_t mValue;   ///< The value.
protected:
     /**
      * \brief Copy constructor.
      *
      * \param c The DataObject to copy.
      */
     ApproxsimInt64_t(const ApproxsimInt64_t& c) : DataObject(c), mValue(c.mValue) {}
public:
     ApproxsimInt64_t(const Reference& scope, const DOMElement* n);

     /**
      * \brief Constructor that creates a DataObject of the specified
      * Type with the provided Reference.
      *
      * \param ref The Reference to the DataObject tp be created.
      * \param type The Type of the DataObject to be created.
      */
     ApproxsimInt64_t(const Reference& ref, const Type& type) : DataObject(ref, type), mValue(0) {}
     int64_t getInt64_t() const { return mValue; }
     void setInt64_t(int64_t v) { mValue = v; }

     DataObject& operator = (const DataObject& d);

     DataObject* clone() const { return new ApproxsimInt64_t(*this); }
     std::ostream& bodyXML(std::ostream& o, std::string indent) const;
     void print(std::ostream& o, const std::string indent) const;
};



/**
 * \brief ApproxsimReference corresponds to the Reference type in the
 * Approxsim xml schema.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/27 09:43:40 $
 */
class ApproxsimReference : public DataObject {
private:
     const Reference* mValue;   ///< The value.
protected:
     /**
      * \brief Copy constructor.
      *
      * \param c The DataObject to copy.
      */
     ApproxsimReference(const ApproxsimReference& c) : DataObject(c), mValue(c.mValue) {}
public:
     ApproxsimReference(const Reference& scope, const DOMElement* n);

     /**
      * \brief Constructor that creates a DataObject of the specified
      * Type with the provided Reference.
      *
      * \param ref The Reference to the DataObject tp be created.
      * \param type The Type of the DataObject to be created.
      */
     ApproxsimReference(const Reference& ref, const Type& type) : DataObject(ref, type), mValue(0) {}
     const Reference& getReference() const { return *mValue; }
     void setReference(const Reference& v) { mValue = &v; }

     DataObject& operator = (const DataObject& d);

     DataObject* clone() const { return new ApproxsimReference(*this); }
     std::ostream& bodyXML(std::ostream& o, std::string indent) const;
     void print(std::ostream& o, const std::string indent) const;
};



/**
 * \brief ApproxsimShape corresponds to the Shape type in the Approxsim
 * xml schema.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/27 09:43:40 $
 */
class ApproxsimShape : public DataObject {
private:
     Shape* mValue;   ///< The value.
protected:
     ApproxsimShape(const ApproxsimShape& c);
public:
     ApproxsimShape(const Reference& scope, const DOMElement* n);

     /**
      * \brief Constructor that creates a DataObject of the specified
      * Type with the provided Reference.
      *
      * \param ref The Reference to the DataObject tp be created.
      * \param type The Type of the DataObject to be created.
      */
     ApproxsimShape(const Reference& ref, const Type& type) : DataObject(ref, type), mValue(0) {}
     ~ApproxsimShape();
     Shape* getShape() const;

     /**
      * \brief Accessor for the actual Shape held by this object.
      *
      * \return The actual Shape held by this object.
      */
     Shape& getShapeRef() const { return *mValue; }
     void setShape(const Shape* v);

     DataObject& operator = (const DataObject& d);

     DataObject* clone() const { return new ApproxsimShape(*this); }
     std::ostream& bodyXML(std::ostream& o, std::string indent) const;
     void print(std::ostream& o, const std::string indent) const;
};



/**
 * \brief ApproxsimString corresponds to the String type in the
 * Approxsim xml schema.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/27 09:43:40 $
 */
class ApproxsimString : public DataObject {
private:
     std::string mValue;   ///< The value.
protected:
     /**
      * \brief Copy constructor.
      *
      * \param c The DataObject to copy.
      */
     ApproxsimString(const ApproxsimString& c) : DataObject(c), mValue(c.mValue) {}
public:
     ApproxsimString(const Reference& scope, const DOMElement* n);

     /**
      * \brief Constructor that creates a DataObject of the specified
      * Type with the provided Reference.
      *
      * \param ref The Reference to the DataObject tp be created.
      * \param type The Type of the DataObject to be created.
      */
     ApproxsimString(const Reference& ref, const Type& type) : DataObject(ref, type), mValue(0) {}
     std::string getString() const { return mValue; }
     void setString(const std::string& v) { mValue = v; }

     DataObject& operator = (const DataObject& d);

     DataObject* clone() const { return new ApproxsimString(*this); }
     std::ostream& bodyXML(std::ostream& o, std::string indent) const;
     void print(std::ostream& o, const std::string indent) const;
};



/**
 * \brief ApproxsimTime corresponds to the Timestamp and Duration types
 * in the Approxsim xml schema.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/27 09:43:40 $
 */
class ApproxsimTime : public DataObject {
private:
     Time mValue;   ///< The value.
protected:
     /**
      * \brief Copy constructor.
      *
      * \param c The DataObject to copy.
      */
     ApproxsimTime(const ApproxsimTime& c) : DataObject(c), mValue(c.mValue) {}
public:
     ApproxsimTime(const Reference& scope, const DOMElement* n);

     /**
      * \brief Constructor that creates a DataObject of the specified
      * Type with the provided Reference.
      *
      * \param ref The Reference to the DataObject tp be created.
      * \param type The Type of the DataObject to be created.
      */
     ApproxsimTime(const Reference& ref, const Type& type) : DataObject(ref, type), mValue(0) {}
     Time getTime() const { return mValue; }
     void setTime(Time v) { mValue = v; }

     DataObject& operator = (const DataObject& d);

     DataObject* clone() const { return new ApproxsimTime(*this); }
     std::ostream& bodyXML(std::ostream& o, std::string indent) const;
     void print(std::ostream& o, const std::string indent) const;
};



/**
 * \brief ApproxsimSymbolIDCode corresponds to the SymbolIDCode type in
 * the Approxsim xml schema.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/27 09:43:40 $
 */
class SymbolIDCode : public DataObject {
private:
     std::string mValue;   ///< The value.
protected:
     /**
      * \brief Copy constructor.
      *
      * \param c The DataObject to copy.
      */
     SymbolIDCode(const SymbolIDCode& c) : DataObject(c), mValue(c.mValue) {}
public:
     SymbolIDCode(const Reference& scope, const DOMElement* n);

     /**
      * \brief Constructor that creates a DataObject of the specified
      * Type with the provided Reference.
      *
      * \param ref The Reference to the DataObject tp be created.
      * \param type The Type of the DataObject to be created.
      */
     SymbolIDCode(const Reference& ref, const Type& type) : DataObject(ref, type) {}
     std::string getString() const { return mValue; }
     void setString(const std::string& v) { mValue = v; }

     DataObject& operator = (const DataObject& d);

     DataObject* clone() const { return new SymbolIDCode(*this); }
     std::ostream& bodyXML(std::ostream& o, std::string indent) const;
     void print(std::ostream& o, const std::string indent) const;
};


/**
 * \brief ApproxsimGraph corresponds to the Graph type in the
 * Approxsim xml schema.
 *
 * \author   Johannes Olegård
 * \date     $Date: 2014/04/25 19:54:00$
 */
template<class T>
class ApproxsimGraph : public DataObject {
private:
     std::shared_ptr<Graph<T>> mValue;   ///< The value.
protected:
     /**
      * \brief Copy constructor.
      *
      * \param c The DataObject to copy.
      */
     ApproxsimGraph(const ApproxsimGraph& c) : DataObject(c), mValue(c.mValue) {}
public:
     ApproxsimGraph(const Reference& scope, const DOMElement* n);

     /**
      * \brief Constructor that creates a DataObject of the specified
      * Type with the provided Reference.
      *
      * \param ref The Reference to the DataObject tp be created.
      * \param type The Type of the DataObject to be created.
      */
     ApproxsimGraph(const Reference& ref, const Type& type) : DataObject(ref, type), mValue(0) {}

     /**
      * 
      * \brief Accessor for the actual Graph held by this object.
      *
      * \return pointer to the actual Graph held by this object.
      */
     Graph<T>& getGraphRef() const { return *mValue; }

     DataObject& operator = (const DataObject& d);

     DataObject* clone() const { return new ApproxsimGraph<T>(*this); }
     std::ostream& bodyXML(std::ostream& o, std::string indent) const;
     void print(std::ostream& o, const std::string indent) const;
};


#endif   // APPROXSIM_DATAOBJECTIMPL_H
