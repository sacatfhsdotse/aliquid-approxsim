// System
#include <ostream>

// Own
#include "DataObjectImpl.h"
#include "debugheader.h"
#include "Declaration.h"
#include "Error.h"
#include "GoodStuff.h"
#include "Mapper.h"
#include "Shape.h"
#include "SOFactory.h"
#include "Type.h"
#include "TypeFactory.h"
#include "XMLHelper.h"
#include "Graph.h"


using namespace std;

/**
 * \brief Constructor that creates a DataObject in the provided scope
 * from the provided DOMElement.
 *
 * \param scope A Reference the scope to create the DataObject in.
 * \param n The DOMElement to create this DataObject from.
 */
DataObject::DataObject(const Reference& scope, const DOMElement* n) 
     : Referencable(scope, n), mType(TypeFactory::getType(XMLHelper::getTypeAttribute(*n))), mParent(0)
{
}

/**
 * \brief Accessor for the identifier.
 *
 * \return The identifier of this DataObject.
 */
const std::string& DataObject::identifier() const
{
     return ref().name();
}

/**
 * \brief Returns the children of this DataObject.
 *
 * \return The children of this DataObject.
 */
const vector<DataObject*>& DataObject::objects() const
{
     Error e;
     e << "Mustn't call 'objects()' for DataObject of type '" << getType().getName() << "'";
     throw e;
}

/**
 * \brief Mutator.
 *
 * \param v The new value.
 */
void DataObject::setBool(bool v)
{
     Error e;
     e << "Mutator for bool not valid for object " << ref() << " of type " << getType().getName();
     throw e;
}

/**
 * \brief Mutator.
 *
 * \param v The new value.
 */
void DataObject::setDouble(double v)
{
     Error e;
     e << "Mutator for double not valid for object " << ref() << " of type " << getType().getName();
     throw e;
}

/**
 * \brief Mutator.
 *
 * \param v The new value.
 */
void DataObject::setInt64_t(int64_t v)
{
     Error e;
     e << "Mutator for int64_t not valid for object " << ref() << " of type " << getType().getName();
     throw e;
}

/**
 * \brief Mutator.
 *
 * \param v The new value.
 */
void DataObject::setTime(Time v)
{
     Error e;
     e << "Mutator for Time not valid for object " << ref() << " of type " << getType().getName();
     throw e;
}

/**
 * \brief Mutator.
 *
 * \param v The new value.
 */
void DataObject::setString(const std::string& v)
{
     Error e;
     e << "Mutator for const std::string& not valid for object " << ref() << " of type " << getType().getName();
     throw e;
}

/**
 * \brief Mutator.
 *
 * \param v The new value.
 */
void DataObject::setReference(const Reference& v)
{
     Error e;
     e << "Mutator for const Reference& not valid for object " << ref() << " of type " << getType().getName();
     throw e;
}

/**
 * \brief Mutator.
 *
 * \param v The new value.
 */
void DataObject::setShape(const Shape* v)
{
     Error e;
     e << "Mutator for const Shape* not valid for type " << getType().getName();
     throw e;
}

/**
 * \brief Accessor
 *
 * \return The current value.
 */
bool DataObject::getBool() const
{
     Error e;
     e << "Accessor for bool not valid for object " << ref() << " of type " << getType().getName();
     throw e;
}

/**
 * \brief Accessor
 *
 * \return The current value.
 */
double DataObject::getDouble() const
{
     Error e;
     e << "Accessor for double not valid for object " << ref() << " of type " << getType().getName();
     throw e;
}

/**
 * \brief Accessor
 *
 * \return The current value.
 */
int64_t DataObject::getInt64_t() const
{
     Error e;
     e << "Accessor for int64_t not valid for object " << ref() << " of type " << getType().getName();
     throw e;
}

/**
 * \brief Accessor
 *
 * \return The current value.
 */
Time DataObject::getTime() const
{
     Error e;
     e << "Accessor for Time not valid for object " << ref() << " of type " << getType().getName();
     throw e;
}

/**
 * \brief Accessor
 *
 * \return The current value.
 */
string DataObject::getString() const
{
     Error e;
     e << "Accessor for const std::string& not valid for object " << ref() << " of type " << getType().getName();
     throw e;
}

/**
 * \brief Accessor
 *
 * \return The current value.
 */
const Reference& DataObject::getReference() const
{
     Error e;
     e << "Accessor for const Reference& not valid for object " << ref() << " of type " << getType().getName();
     throw e;
}

/**
 * \brief Accessor
 *
 * \return The current value.
 */
Shape *DataObject::getShape() const
{
     Error e;
     e << "Accessor for Shape* not valid for type " << getType().getName();
     throw e;
}

/**
 * \brief Assignment operator.
 *
 * \param d The object to copy.
 * \return The assigned object.
 */
DataObject& DataObject::operator = (const DataObject& d)
{
     Error e;
     e << d.ref() << " of type " << d.getType().getName() << " may not be assigned to type " << getType().getName();
     throw e;
}

/**
 * \brief Registers this DataObject with the Mapper.
 */
void DataObject::reg() const
{
     Mapper::reg(this);
}

/**
 * \brief Deregisters this DataObject from the Mapper.
 */
void DataObject::dereg() const
{
     Mapper::dereg(*this);
}

/**
 * \brief Produces an XML representation of this DataObject according
 * to the Approxsim xml schemas.
 *
 * \param o The ostream to print to.
 * \param indent Intention string for readable output.
 * \return The ostream with the XML representation written to it.
 */
ostream& DataObject::toXML(ostream& o, string indent) const
{
     bool listParent = (dynamic_cast<DataObjectList*>(mParent) != 0);
     string tag = (listParent ? mParent->identifier() : identifier());
     o << indent << "<" << tag << " xsi:type=\"sp:" << getType().getName() << "\"";
     if (listParent) {
          o << " identifier=\"" << identifier() << "\"";
     }
     o << ">";
     if (!dynamic_cast<const ContainerDataObject*>(this) || hasChildren()) {
          o << endl;
     }
     bodyXML(o, indent + INDENT);
     if (!dynamic_cast<const ContainerDataObject*>(this) || hasChildren()) {
          o << indent;
     }
     o << "</" << tag << ">" << endl;
     return o;
}

/**
 * \brief For debug purposes.
 *
 * \param o The ostream to print to.
 * \param indent Intention string for readable output.
 */
void DataObject::print(ostream& o, const std::string indent) const
{
     o << indent << identifier() << " (" << getType().getName() << ") ";
}



ContainerDataObject::ContainerDataObject(const ContainerDataObject& c) : DataObject(c)
{
     for (vector<DataObject*>::const_iterator it = c.mObjects.begin(); it != c.mObjects.end(); it++) {
          add((*it)->clone());
     }
}

ContainerDataObject::~ContainerDataObject()
{
     for (std::map<std::string, DataObject*>::iterator it = mObjectMap.begin(); it != mObjectMap.end(); it++) {
          delete it->second;
     }
}

/**
 * \brief Adds a child to this ContainerDataObject.
 *
 * \param o The child to add.
 */
void ContainerDataObject::add(DataObject* o) {
     if (o) {
          o->setParent(this);
          mObjects.push_back(o);
          mObjectMap[o->identifier()] = o;
     }
     else {
          Error e;
          e << "Null DataObject in ContainerDataObject::add() - Reference = '" << ref() << "'";
          throw e;
     }
}

/**
 * \brief Removes a child from this ContainerDataObject.
 *
 * \param name The identifier of the child to remove.
 */
void ContainerDataObject::remove(const std::string& name)
{
     for (vector<DataObject*>::iterator it = mObjects.begin(); it != mObjects.end(); it++) {
          if (name == (*it)->identifier()) {
               delete *it;
               mObjects.erase(it);
               break;
          }
     }
     mObjectMap.erase(name);
}

/**
 * \brief Replaces a child in this ContainerDataObject.
 *
 * \param newObj The object to replace the old object with.
 */
void ContainerDataObject::replace(DataObject* newObj)
{
     for (vector<DataObject*>::iterator it = mObjects.begin(); it != mObjects.end(); it++) {
          if (newObj->ref() == (*it)->ref()) {
               delete *it;
               *it = newObj;
               break;
          }
     }
     mObjectMap[newObj->identifier()] = newObj;
     newObj->setParent(this);
}



/**
 * \brief Returns the child with the specified identifier or null if
 * there is no such child.
 *
 * \param name The identifier of the child to get.
 * \return The child with the specified identifier or null if there is
 * no such child.
 */
DataObject* ContainerDataObject::getChild(const std::string& name) const
{
     std::map<std::string, DataObject*>::const_iterator it = mObjectMap.find(name);
     return (it == mObjectMap.end() ? 0 : it->second);
}

/**
 * \brief Registers this ContainerDataObject and all its children with
 * the Mapper.
 */
void ContainerDataObject::reg() const
{
     DataObject::reg();
     for (vector<DataObject*>::const_iterator it = mObjects.begin(); it != mObjects.end(); it++) {
          DataObject& d = **it;
          if (d.getType().canSubstitute("ValueType") || dynamic_cast<DataObjectList*>(&d)) { 
               d.reg();
          }
     }
}

/**
 * \brief Deregisters this ContainerDataObject and all its children
 * from the Mapper.
 */
void ContainerDataObject::dereg() const
{
     DataObject::dereg();
     for (vector<DataObject*>::const_iterator it = mObjects.begin(); it != mObjects.end(); it++) {
          DataObject& d = **it;
          if (d.getType().canSubstitute("ValueType") || static_cast<DataObjectList*>(&d)) { 
               d.dereg();
          }
     }
}

ostream& ContainerDataObject::bodyXML(ostream& o, string indent) const
{
     for (vector<DataObject*>::const_iterator it = mObjects.begin(); it != mObjects.end(); it++) {
          (*it)->toXML(o, indent);
     }
     return o;
}

void ContainerDataObject::print(ostream& o, const std::string indent) const
{
     DataObject::print(o, indent);
     for (auto& v : mObjects) {
          //o << endl << indent << kv.first << endl;
          o << endl;
          v->print(o, indent + INDENT);
     }

     for (auto& v : mObjectMap){
          o << endl << endl << v.first;
     }
}



/**
 * \brief Constructor that creates a ComplexDataObject in the provided
 * scope from the provided DOMElement.
 *
 * This constructor goes through its Type's sub Decalarations and
 * tries to create a child for each such Declaration.
 *
 * \param scope A Reference the scope to create the DataObject in.
 * \param n The DOMElement to create this DataObject from.
 */
ComplexDataObject::ComplexDataObject(const Reference& scope, const DOMElement* n) : ContainerDataObject(scope, n)
{
     for (vector<const Declaration*>::const_iterator it = getType().subElements().begin();
          it != getType().subElements().end();
          it++) {
          const Declaration& dec = **it;
          if (dec.isList()) {
               add(new DataObjectList(ref(), dec, n));
          }
          else {
               DOMElement *elem = XMLHelper::getFirstChildByTag(*n, dec.getName());
               if (elem) {
                    add(DataObjectFactory::createDataObject(ref(), elem));
               }
          }
     }
}

ComplexDataObject::ComplexDataObject(const Reference& ref, const Type& type) : ContainerDataObject(ref, type)
{
     for (vector<const Declaration*>::const_iterator it = getType().subElements().begin(); it != getType().subElements().end(); it++) {
          const Declaration& dec = **it;
          if (dec.isList()) {
               add(new DataObjectList(Reference::get(this->ref(), dec.getName()), dec.getType()));
          }
          else if (!dec.isOptional()) {
               add(DataObjectFactory::createDataObject(Reference::get(ref, dec.getName()), dec.getType()));
          }
     }
}

/**
 * \brief Adds a child to this ContainerDataObject preserving the
 * order as specified in the Approxsim xml schemas.
 *
 * \param o The child to add.
 */
void ComplexDataObject::orderPreservingAdd(DataObject* o)
{
     if (getChild(o->identifier())) {
          Error e;
          e << "Tried to add element '" << o->identifier() << "' to object ";
          e << identifier() << " that already has such an element.";
          throw e;
     }

     int indexOfNewElement = -1;
     int currentIndex = 0;
     string nameToAdd = o->identifier();
     const vector<const Declaration*>& subDecs = getType().subElements();

     // Get the name objName of element i in the mObjects vector. Go
     // through subelements of this ComplexDataObject's Type. If
     // objName matches the new child's name then we know i is the
     // index where to insert the new child. If objName matches the
     // name of the subelement we grab the next object from the
     // mObjects vector and starts over. If no index is found when the
     // outer loop exits we know we should add the new child to the
     // end of the mObjects vector.
     for(unsigned int i = 0; i < objects().size() && indexOfNewElement == -1; i++) {
          DataObject& obj = *objects()[i];
          string objName = obj.identifier();
          
          for (unsigned int j = currentIndex; j < subDecs.size(); j++) {
               string decName = subDecs[j]->getName();
               if (decName == nameToAdd) {
                    indexOfNewElement = i;
                    break;
               }
               else if (decName == objName) {
                    currentIndex = j++;
                    break;
               }
          }
     }
     if (indexOfNewElement == -1) {
          add(o);
     }
     else {
          o->setParent(this);
          mObjects.insert(mObjects.begin() + indexOfNewElement, o);
          mObjectMap[o->identifier()] = o;
     }
}

/**
 * \brief Constructor that creates a DataObjectList in the provided
 * scope from the provided DOMElement based on the provided
 * Declaration.
 *
 * \param scope A Reference the scope to create the DataObject in.
 * \param dec The Declaration to use when creating this list.
 * \param n The DOMElement to create this DataObject from.
 */
DataObjectList::DataObjectList(const Reference& scope, const Declaration& dec, const DOMElement* n)
     : ContainerDataObject(Reference::get(scope, dec.getName()), dec.getType())
{
     vector<DOMElement*> v;
     XMLHelper::getChildElementsByTag(*n, identifier(), v);
     if (getType().canSubstitute("ValueType")) {
          for (vector<DOMElement*>::iterator it = v.begin(); it != v.end(); it++) {
               add(DataObjectFactory::createDataObject(ref(), *it));
          }
     }
     else {
          for (vector<DOMElement*>::iterator it = v.begin(); it != v.end(); it++) {
               add(new ComplexDataObject(ref(), *it));
          }
     }
}

ostream& DataObjectList::toXML(ostream& o, string indent) const
{
     bodyXML(o, indent);
     return o;
}



/**
 * \brief Constructor that creates a DataObject in the provided scope
 * from the provided DOMElement.
 *
 * \param scope A Reference the scope to create the DataObject in.
 * \param n The DOMElement to create this DataObject from.
 */
ApproxsimBool::ApproxsimBool(const Reference& scope, const DOMElement* n) : DataObject(scope, n)
{
     mValue = XMLHelper::getBool(*n, "value");
}

DataObject& ApproxsimBool::operator = (const DataObject& d)
{
     if (d.getType().canSubstitute(getType())) {
          setBool(d.getBool());
     }
     else {
          DataObject::operator=(d);
     }
     return *this;
}

ostream& ApproxsimBool::bodyXML(ostream& o, string indent) const
{
     o << indent << "<value>" << getBool() << "</value>" << endl;
     return o;
}

void ApproxsimBool::print(ostream& o, const std::string indent) const 
{
     DataObject::print(o, indent);
     o << getBool();
}



/**
 * \brief Constructor that creates a DataObject in the provided scope
 * from the provided DOMElement.
 *
 * \param scope A Reference the scope to create the DataObject in.
 * \param n The DOMElement to create this DataObject from.
 */
ApproxsimDouble::ApproxsimDouble(const Reference& scope, const DOMElement* n) : DataObject(scope, n)
{
     mValue = XMLHelper::getDouble(*n, "value");
}

DataObject& ApproxsimDouble::operator = (const DataObject& d)
{
     if (d.getType().canSubstitute(getType())) {
          setDouble(d.getDouble());
     }
     else {
          DataObject::operator=(d);
     }
     return *this;
}

ostream& ApproxsimDouble::bodyXML(ostream& o, string indent) const
{
     o << indent << "<value>";
     if (!std::isnan(getDouble())) {
          o << getDouble();
     }
     else {
          o << "NaN";
     }
     o << "</value>" << endl;
     return o;
}

void ApproxsimDouble::print(ostream& o, const std::string indent) const 
{
     DataObject::print(o, indent);
     o << getDouble();
}



/**
 * \brief Constructor that creates a DataObject in the provided scope
 * from the provided DOMElement.
 *
 * \param scope A Reference the scope to create the DataObject in.
 * \param n The DOMElement to create this DataObject from.
 */
ApproxsimInt64_t::ApproxsimInt64_t(const Reference& scope, const DOMElement* n) : DataObject(scope, n)
{
     mValue = XMLHelper::getLongInt(*n, "value");
}

DataObject& ApproxsimInt64_t::operator = (const DataObject& d)
{
     if (d.getType().canSubstitute(getType())) {
          setInt64_t(d.getInt64_t());
     }
     else {
          DataObject::operator=(d);
     }
     return *this;
}

ostream& ApproxsimInt64_t::bodyXML(ostream& o, string indent) const
{
     o << indent << "<value>" << getInt64_t() << "</value>" << endl;
     return o;
}

void ApproxsimInt64_t::print(ostream& o, const std::string indent) const 
{
     DataObject::print(o, indent);
     o << getInt64_t();
}



/**
 * \brief Constructor that creates a DataObject in the provided scope
 * from the provided DOMElement.
 *
 * \param scope A Reference the scope to create the DataObject in.
 * \param n The DOMElement to create this DataObject from.
 */
ApproxsimReference::ApproxsimReference(const Reference& scope, const DOMElement* n) : DataObject(scope, n)
{
     mValue = &Reference::get(n);
}

DataObject& ApproxsimReference::operator = (const DataObject& d)
{
     if (d.getType().canSubstitute(getType())) {
          setReference(d.getReference());
     }
     else {
          DataObject::operator=(d);
     }
     return *this;
}

ostream& ApproxsimReference::bodyXML(ostream& o, string indent) const
{
     mValue->toXML(o, indent) << endl;
     return o;
}

void ApproxsimReference::print(ostream& o, const std::string indent) const 
{
     DataObject::print(o, indent);
     o << getReference();
}



/**
 * \brief Copy constructor.
 *
 * \param c The DataObject to copy.
 */
ApproxsimShape::ApproxsimShape(const ApproxsimShape& c) : DataObject(c), mValue(c.mValue->clone())
{
}

/**
 * \brief Constructor that creates a DataObject in the provided scope
 * from the provided DOMElement.
 *
 * \param scope A Reference the scope to create the DataObject in.
 * \param n The DOMElement to create this DataObject from.
 */
ApproxsimShape::ApproxsimShape(const Reference& scope, const DOMElement* n) : DataObject(scope, n)
{
     mValue = XMLHelper::getShape(*n, scope);
}

ApproxsimShape::~ApproxsimShape()
{
     delete mValue;
}

/**
 * \brief Gets a clone of the Shape. The caller is responsible for
 * freeing up the memory used by the returned Shape.
 *
 * /return A copy of the Shape.
 */
Shape* ApproxsimShape::getShape() const
{
     return mValue->clone();
}


/**
 * \brief Sets the Shape to a clone of the provided Shape.
 *
 * If the provided Shape is of different type that the existing - a
 * replaced event is triggered.
 *
 * /return A copy of the Shape.
 */
void ApproxsimShape::setShape(const Shape* v)
{
     if (mValue) {
          if (v->type() != mValue->type()) {
               approxsimDebug("this " << this << ", mValue " << mValue << ", v " << v);
               approxsimDebug("mValue->type() " << mValue->type() << ", v->type() " << v->type());
               ApproxsimShape* s = new ApproxsimShape(ref(), TypeFactory::getType(v->type()));
               s->setShape(v);
               SOFactory::simulationObjectReplaced(*s, -1);
          }
          else {
               delete mValue;
               mValue = v->clone();
          }
     }
     else {
          mValue = v->clone();
     }
}

DataObject& ApproxsimShape::operator = (const DataObject& d)
{
     if (d.getType().canSubstitute(getType())) {
          setShape(d.getShape());
          Circle* c = dynamic_cast<Circle*>(getShape());
          if (c) {
               approxsimDebug("New value: " << *c);
          }
     }
     else {
          DataObject::operator=(d);
     }
     return *this;
}

ostream& ApproxsimShape::bodyXML(ostream& o, string indent) const
{
     mValue->toXML(o, indent);
     return o;
}

void ApproxsimShape::print(ostream& o, const std::string indent) const 
{
     DataObject::print(o, indent);
     o << "center: " << mValue->cenCoord();
     if (getType() == TypeFactory::getType("Circle")) {
          const Circle &c = *dynamic_cast<const Circle*>(mValue);
          o << ", radius: " << c.radius();
     }
}



/**
 * \brief Constructor that creates a DataObject in the provided scope
 * from the provided DOMElement.
 *
 * \param scope A Reference the scope to create the DataObject in.
 * \param n The DOMElement to create this DataObject from.
 */
ApproxsimString::ApproxsimString(const Reference& scope, const DOMElement* n) : DataObject(scope, n)
{
     XMLHelper::getString(*n, "value", mValue);
}

DataObject& ApproxsimString::operator = (const DataObject& d)
{
     if (d.getType().canSubstitute(getType())) {
          setString(d.getString());
     }
     else {
          DataObject::operator=(d);
     }
     return *this;
}

ostream& ApproxsimString::bodyXML(ostream& o, string indent) const
{
     o << indent << "<value>" << getString() << "</value>" << endl;
     return o;
}

void ApproxsimString::print(ostream& o, const std::string indent) const 
{
     DataObject::print(o, indent);
     o << getString();
}



/**
 * \brief Constructor that creates a DataObject in the provided scope
 * from the provided DOMElement.
 *
 * \param scope A Reference the scope to create the DataObject in.
 * \param n The DOMElement to create this DataObject from.
 */
ApproxsimTime::ApproxsimTime(const Reference& scope, const DOMElement* n) : DataObject(scope, n)
{
     if (getType().getName() == "Duration") {
          mValue = Time(0, 0, 0, 0, XMLHelper::getLongInt(*n, "value"));
     }
     else {
          mValue = XMLHelper::getTime(*n, "value");
     }
}

DataObject& ApproxsimTime::operator = (const DataObject& d)
{
     if (d.getType().canSubstitute(getType())) {
          setTime(d.getTime());
     }
     else {
          DataObject::operator=(d);
     }
     return *this;
}

ostream& ApproxsimTime::bodyXML(ostream& o, string indent) const
{
     o << indent << "<value>";
     if (getType().getName() == "Duration") {
          o << getTime().milliSeconds();
     }
     else {
          XMLHelper::timeToDateTime(o, getTime());
     }
     o << "</value>" << endl;
     return o;
}

void ApproxsimTime::print(ostream& o, const std::string indent) const 
{
     DataObject::print(o, indent);
     o << getTime();
}



/**
 * \brief Constructor that creates a DataObject in the provided scope
 * from the provided DOMElement.
 *
 * \param scope A Reference the scope to create the DataObject in.
 * \param n The DOMElement to create this DataObject from.
 */
SymbolIDCode::SymbolIDCode(const Reference& scope, const DOMElement* n) : DataObject(scope, n)
{
     XMLHelper::getElementStringValue(*n, "value", mValue);
}

DataObject& SymbolIDCode::operator = (const DataObject& d)
{
     if (d.getType().canSubstitute(getType())) {
          setString(d.getString());
     }
     else {
          DataObject::operator=(d);
     }
     return *this;
}

ostream& SymbolIDCode::bodyXML(ostream& o, string indent) const
{
     o << indent << "<value xsi:type=\"sp:String\"><value>" << mValue << "</value></value>" << endl;
     return o;
}

void SymbolIDCode::print(ostream& o, const std::string indent) const 
{
     DataObject::print(o, indent);
     o << getString();
}


template<class T>
ApproxsimGraph<T>::ApproxsimGraph(const Reference& scope, const DOMElement* n) : DataObject(scope, n)
{
     mValue = std::shared_ptr<Graph<T>> (XMLHelper::getGraph<T>(*n, scope));
}

template<class T>
DataObject& ApproxsimGraph<T>::operator= (const DataObject& d)
{
     // TODO this.graph = d.graph
     DataObject::operator=(d);
     return *this;
}

template<class T>
void ApproxsimGraph<T>::print(ostream& o, const std::string indent) const
{
     mValue->print(o);
}

template<class T>
std::ostream& ApproxsimGraph<T>::bodyXML(std::ostream& o, std::string indent) const
{
     // TODO
     return o;
}




/**
 * \brief Creates a DataObject from the provided DOMElement.
 *
 * \param n The DOMElement to create the DataObject from.
 * \param scope The Reference to the scope this DataObject should live in.
 * \return The newly created DataObject.
 */
DataObject* DataObjectFactory::createDataObject(const Reference& scope, const DOMElement* n)
{
     DataObject* ret = 0;
     string typeStr = XMLHelper::getTypeAttribute(*n);
     const Type& type = TypeFactory::getType(typeStr);
     if (type.canSubstitute("Double")) {
          ret = new ApproxsimDouble(scope, n);
     }
     else if (type.canSubstitute("Timestamp") || type.canSubstitute("Duration")) {
          ret = new ApproxsimTime(scope, n);
     }
     else if (type.canSubstitute("NonNegativeInteger")) {
          ret = new ApproxsimInt64_t(scope, n);
     }
     else if (type.canSubstitute("String")) {
          ret = new ApproxsimString(scope, n);
     }
     else if (type.canSubstitute("Boolean")) {
          ret = new ApproxsimBool(scope, n);
     }
     else if (type.canSubstitute("Reference")) {
          ret = new ApproxsimReference(scope, n);
     }
     else if (type.canSubstitute("Shape")) {
          ret = new ApproxsimShape(scope, n);
     }
     else if (type.canSubstitute("PathGraph")) {
          ret = new ApproxsimGraph<PathData>(scope, n);
     }
     else if (type.canSubstitute("EffectGraph")) {
          ret = new ApproxsimGraph<EffectData>(scope, n);
     }
     else if (typeStr == "SymbolIDCode") {
          ret = new SymbolIDCode(scope, n);
     }
     else {
          ret = new ComplexDataObject(scope, n);
     }
     return ret;
}

/**
 * \brief Creates a default DataObject.
 *
 * \param ref The Reference to the DataObject tp be created.
 * \param type The Type of the DataObject to be created.
 * \return The newly created DataObject.
 */
DataObject* DataObjectFactory::createDataObject(const Reference& ref, const Type& type)
{
     DataObject* ret = 0;
     string typeStr = type.getName();
     if (type.canSubstitute("Double")) {
          ret = new ApproxsimDouble(ref, type);
     }
     else if (type.canSubstitute("Timestamp") || type.canSubstitute("Duration")) {
          ret = new ApproxsimTime(ref, type);
     }
     else if (type.canSubstitute("NonNegativeInteger")) {
          ret = new ApproxsimInt64_t(ref, type);
     }
     else if (type.canSubstitute("String")) {
          ret = new ApproxsimString(ref, type);
     }
     else if (type.canSubstitute("Boolean")) {
          ret = new ApproxsimBool(ref, type);
     }
     else if (type.canSubstitute("Reference")) {
          ret = new ApproxsimReference(ref, type);
     }
     else if (type.canSubstitute("Shape")) {
          ret = new ApproxsimShape(ref, type);
     }
     else if (typeStr == "SymbolIDCode") {
          ret = new SymbolIDCode(ref, type);
     }
     else {
          ret = new ComplexDataObject(ref, type);
     }
     return ret;
}

/**
 * \brief Adds an optional element to the provided DataObject.
 *
 * \param parent The DataObject to add an optional element to.
 * \param idToAdd The name of the optional element to add.
 */
DataObject* DataObjectFactory::addOptional(DataObject& parent, const string& idToAdd)
{
     const Declaration* dec = parent.getType().getSubElement(idToAdd);
     if (!dec) {
          Error e;
          e << "No optional element '" << idToAdd << "' in '" << parent.ref();
          throw e;
     }
     else if (!dec->isOptional()) {
          Error e;
          e << "Element '" << idToAdd << "' is not optional in '" << parent.ref();
          e << "' of type " << parent.getType().getName();
          throw e;
     }
     DataObject* newObj = createDataObject(Reference::get(parent.ref(), idToAdd), dec->getType());
     dynamic_cast<ComplexDataObject*>(&parent)->orderPreservingAdd(newObj);
     return newObj;
}

/**
 * \brief Convenience function for adding a DataObject to the
 * DataObject referenced by the provided Reference.
 *
 * \param parent Reference to the parent to add to.
 * \param objToAdd The DataObject to add.
 */
void DataObjectFactory::addObjectTo(const Reference& parent, DataObject& objToAdd)
{
     ContainerDataObject* parentObj = dynamic_cast<ContainerDataObject*>(Mapper::map(parent));
     if (parentObj) {
          if (!parentObj->getChild(objToAdd.identifier())) {
               if (ComplexDataObject* cdo = dynamic_cast<ComplexDataObject*>(parentObj)) {
                    cdo->orderPreservingAdd(&objToAdd);
               }
               else {
                    parentObj->add(&objToAdd);
               }
          }
          else {
               Error e;
               e << "Can't add object '" << objToAdd.identifier();
               e << "' to '" << parent << "'. Object already exists.";
               throw e;
          }
     }
     else {
          Error e;
          e << "Couldn't find parent to object '" << objToAdd.ref() << "'";
          throw e;
     }
}


// vim: ts=5 sw=5:
