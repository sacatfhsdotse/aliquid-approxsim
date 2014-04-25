#ifndef STRATMAS_DATAOBJECTIMPL_H
#define STRATMAS_DATAOBJECTIMPL_H

// System
#include <map>

// Own
#include "DataObject.h"

// Forward Declarations
class Declaration;


/**
 * \brief ContainerDataObject is the abstract super class for all
 * lists and complex objects in the Stratmas xml schema except
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
 * \brief ComplexDataObjects represent complex objects in the Stratmas
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
 * \brief DataObjectsList represent lists in the Stratmas xml schema.
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
 * \brief StratmasBool corresponds to the Boolean type in the Stratmas
 * xml schema.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/27 09:43:40 $
 */
class StratmasBool : public DataObject {
private:
     bool mValue;   ///< The value.
protected:
     /**
      * \brief Copy constructor.
      *
      * \param c The DataObject to copy.
      */
     StratmasBool(const StratmasBool& c) : DataObject(c), mValue(c.mValue) {}
public:
     StratmasBool(const Reference& scope, const DOMElement* n);

     /**
      * \brief Constructor that creates a DataObject of the specified
      * Type with the provided Reference.
      *
      * \param ref The Reference to the DataObject tp be created.
      * \param type The Type of the DataObject to be created.
      */
     StratmasBool(const Reference& ref, const Type& type) : DataObject(ref, type), mValue(0) {}
     bool getBool() const { return mValue; }
     void setBool(bool v) { mValue = v; }

     DataObject& operator = (const DataObject& d);

     DataObject* clone() const { return new StratmasBool(*this); }
     std::ostream& bodyXML(std::ostream& o, std::string indent) const;
     void print(std::ostream& o, const std::string indent) const;
};



/**
 * \brief StratmasDouble corresponds to the Double type in the
 * Stratmas xml schema.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/27 09:43:40 $
 */
class StratmasDouble : public DataObject {
private:
     double mValue;   ///< The value.
protected:
     /**
      * \brief Copy constructor.
      *
      * \param c The DataObject to copy.
      */
     StratmasDouble(const StratmasDouble& c) : DataObject(c), mValue(c.mValue) {}
public:
     StratmasDouble(const Reference& scope, const DOMElement* n);

     /**
      * \brief Constructor that creates a DataObject of the specified
      * Type with the provided Reference.
      *
      * \param ref The Reference to the DataObject tp be created.
      * \param type The Type of the DataObject to be created.
      */
     StratmasDouble(const Reference& ref, const Type& type) : DataObject(ref, type), mValue(0) {}
     double getDouble() const { return mValue; }
     void setDouble(double v) { mValue = v; }

     DataObject& operator = (const DataObject& d);

     DataObject* clone() const { return new StratmasDouble(*this); }
     std::ostream& bodyXML(std::ostream& o, std::string indent) const;
     void print(std::ostream& o, const std::string indent) const;
};



/**
 * \brief StratmasInt64_t corresponds to the NonNegativeInteger type
 * in the Stratmas xml schema.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/27 09:43:40 $
 */
class StratmasInt64_t : public DataObject {
private:
     int64_t mValue;   ///< The value.
protected:
     /**
      * \brief Copy constructor.
      *
      * \param c The DataObject to copy.
      */
     StratmasInt64_t(const StratmasInt64_t& c) : DataObject(c), mValue(c.mValue) {}
public:
     StratmasInt64_t(const Reference& scope, const DOMElement* n);

     /**
      * \brief Constructor that creates a DataObject of the specified
      * Type with the provided Reference.
      *
      * \param ref The Reference to the DataObject tp be created.
      * \param type The Type of the DataObject to be created.
      */
     StratmasInt64_t(const Reference& ref, const Type& type) : DataObject(ref, type), mValue(0) {}
     int64_t getInt64_t() const { return mValue; }
     void setInt64_t(int64_t v) { mValue = v; }

     DataObject& operator = (const DataObject& d);

     DataObject* clone() const { return new StratmasInt64_t(*this); }
     std::ostream& bodyXML(std::ostream& o, std::string indent) const;
     void print(std::ostream& o, const std::string indent) const;
};



/**
 * \brief StratmasReference corresponds to the Reference type in the
 * Stratmas xml schema.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/27 09:43:40 $
 */
class StratmasReference : public DataObject {
private:
     const Reference* mValue;   ///< The value.
protected:
     /**
      * \brief Copy constructor.
      *
      * \param c The DataObject to copy.
      */
     StratmasReference(const StratmasReference& c) : DataObject(c), mValue(c.mValue) {}
public:
     StratmasReference(const Reference& scope, const DOMElement* n);

     /**
      * \brief Constructor that creates a DataObject of the specified
      * Type with the provided Reference.
      *
      * \param ref The Reference to the DataObject tp be created.
      * \param type The Type of the DataObject to be created.
      */
     StratmasReference(const Reference& ref, const Type& type) : DataObject(ref, type), mValue(0) {}
     const Reference& getReference() const { return *mValue; }
     void setReference(const Reference& v) { mValue = &v; }

     DataObject& operator = (const DataObject& d);

     DataObject* clone() const { return new StratmasReference(*this); }
     std::ostream& bodyXML(std::ostream& o, std::string indent) const;
     void print(std::ostream& o, const std::string indent) const;
};



/**
 * \brief StratmasShape corresponds to the Shape type in the Stratmas
 * xml schema.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/27 09:43:40 $
 */
class StratmasShape : public DataObject {
private:
     Shape* mValue;   ///< The value.
protected:
     StratmasShape(const StratmasShape& c);
public:
     StratmasShape(const Reference& scope, const DOMElement* n);

     /**
      * \brief Constructor that creates a DataObject of the specified
      * Type with the provided Reference.
      *
      * \param ref The Reference to the DataObject tp be created.
      * \param type The Type of the DataObject to be created.
      */
     StratmasShape(const Reference& ref, const Type& type) : DataObject(ref, type), mValue(0) {}
     ~StratmasShape();
     Shape* getShape() const;

     /**
      * \brief Accessor for the actual Shape held by this object.
      *
      * \return The actual Shape held by this object.
      */
     Shape& getShapeRef() const { return *mValue; }
     void setShape(const Shape* v);

     DataObject& operator = (const DataObject& d);

     DataObject* clone() const { return new StratmasShape(*this); }
     std::ostream& bodyXML(std::ostream& o, std::string indent) const;
     void print(std::ostream& o, const std::string indent) const;
};



/**
 * \brief StratmasString corresponds to the String type in the
 * Stratmas xml schema.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/27 09:43:40 $
 */
class StratmasString : public DataObject {
private:
     std::string mValue;   ///< The value.
protected:
     /**
      * \brief Copy constructor.
      *
      * \param c The DataObject to copy.
      */
     StratmasString(const StratmasString& c) : DataObject(c), mValue(c.mValue) {}
public:
     StratmasString(const Reference& scope, const DOMElement* n);

     /**
      * \brief Constructor that creates a DataObject of the specified
      * Type with the provided Reference.
      *
      * \param ref The Reference to the DataObject tp be created.
      * \param type The Type of the DataObject to be created.
      */
     StratmasString(const Reference& ref, const Type& type) : DataObject(ref, type), mValue(0) {}
     std::string getString() const { return mValue; }
     void setString(const std::string& v) { mValue = v; }

     DataObject& operator = (const DataObject& d);

     DataObject* clone() const { return new StratmasString(*this); }
     std::ostream& bodyXML(std::ostream& o, std::string indent) const;
     void print(std::ostream& o, const std::string indent) const;
};



/**
 * \brief StratmasTime corresponds to the Timestamp and Duration types
 * in the Stratmas xml schema.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/03/27 09:43:40 $
 */
class StratmasTime : public DataObject {
private:
     Time mValue;   ///< The value.
protected:
     /**
      * \brief Copy constructor.
      *
      * \param c The DataObject to copy.
      */
     StratmasTime(const StratmasTime& c) : DataObject(c), mValue(c.mValue) {}
public:
     StratmasTime(const Reference& scope, const DOMElement* n);

     /**
      * \brief Constructor that creates a DataObject of the specified
      * Type with the provided Reference.
      *
      * \param ref The Reference to the DataObject tp be created.
      * \param type The Type of the DataObject to be created.
      */
     StratmasTime(const Reference& ref, const Type& type) : DataObject(ref, type), mValue(0) {}
     Time getTime() const { return mValue; }
     void setTime(Time v) { mValue = v; }

     DataObject& operator = (const DataObject& d);

     DataObject* clone() const { return new StratmasTime(*this); }
     std::ostream& bodyXML(std::ostream& o, std::string indent) const;
     void print(std::ostream& o, const std::string indent) const;
};



/**
 * \brief StratmasSymbolIDCode corresponds to the SymbolIDCode type in
 * the Stratmas xml schema.
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
 * \brief StratmasGraph corresponds to the Graph type in the
 * Stratmas xml schema.
 *
 * \author   Johannes Olegård
 * \date     $Date: 2014/04/25 19:54:00$
 */
class StratmasGraph : public DataObject {
private:
     Graph* mValue;   ///< The value.
protected:
     /**
      * \brief Copy constructor.
      *
      * \param c The DataObject to copy.
      */
     StratmasGraph(const StratmasGraph& c) : DataObject(c), mValue(c.mValue) {}
public:
     StratmasGraph(const Reference& scope, const DOMElement* n);
     ~StratmasGraph();

     /**
      * \brief Constructor that creates a DataObject of the specified
      * Type with the provided Reference.
      *
      * \param ref The Reference to the DataObject tp be created.
      * \param type The Type of the DataObject to be created.
      */
     StratmasGraph(const Reference& ref, const Type& type) : DataObject(ref, type), mValue(0) {}

     /**
      * 
      * \brief Accessor for the actual Graph held by this object.
      *
      * \return pointer to the actual Graph held by this object.
      */
     Graph& getGraphRef() const { return *mValue; }

     DataObject& operator = (const DataObject& d);

     DataObject* clone() const { return new StratmasGraph(*this); }
     std::ostream& bodyXML(std::ostream& o, std::string indent) const;
     void print(std::ostream& o, const std::string indent) const;
};


#endif   // STRATMAS_DATAOBJECTIMPL_H
