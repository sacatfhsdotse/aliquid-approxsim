// System
#include <algorithm>
#include <fstream>
#include <iostream>
#include <list>
#include <ctime>

// Xerces-c
#include <xercesc/dom/DOMElement.hpp>
#include <xercesc/dom/DOMNodeList.hpp>
#include <xercesc/dom/DOMText.hpp>
#include <xercesc/util/Base64.hpp>


// Own
#include "debugheader.h"
#include "Error.h"
#include "Reference.h"
#include "Shape.h" 
#include "StrX.h"
#include "XMLHelper.h"


using namespace std;


static const char *nodeTypeToStringMap[] = {
     "",
     "ELEMENT_NODE",
     "ATTRIBUTE_NODE",
     "TEXT_NODE",
     "CDATA_SECTION_NODE",
     "ENTITY_REFERENCE_NODE",
     "ENTITY_NODE",
     "PROCESSING_INSTRUCTION_NODE",
     "COMMENT_NODE",
     "DOCUMENT_NODE",
     "DOCUMENT_TYPE_NODE",
     "DOCUMENT_FRAGMENT_NODE",
     "NOTATION_NODE"
};

/**
 * \brief Maps node types to their names.
 *
 * \param i The node type.
 * \return A string with the name of the node type.
 */
string XMLHelper::nodeTypeToString(int i)
{
     return (i > 0 && i <=12 ? nodeTypeToStringMap[i] : "Unknown node type");
}

/**
 * \brief Removes the namespace from a string.
 *
 * Works by removing all charachters up to and including the last ':'
 * character.
 *
 * \param s The string to remove the namespace from.
 * \return The same string with the namespace removed.
 */
string &XMLHelper::removeNamespace(string &s) {
     size_t pos = s.find(':');
     if (pos != string::npos) {
	  s = s.substr(pos + 1);
     }
     return s;
}

/**
 * \brief Encodes XML special characters.
 *
 * \param s The string in which to encode the special characters.
 * \return A new string with special characters encoded.
 */
string XMLHelper::encodeSpecialCharacters(const std::string& s) {
     string res;
     for (unsigned int i = 0; i < s.length(); i++) {
	  switch (s[i]) {
	  case '&':
	       res += "&amp;";
	       break;
	  case '<':
	       res += "&lt;";
	       break;
	  case '>':
	       res += "&gt;";
	       break;
	  case '\'':
	       res += "&apos;";
	       break;
	  case '"':
	       res += "&quot;";
	       break;
	  default:
	       res += s[i];
	       break;
	  }
     }
     return res;
}

/**
 * \brief Encodes XML special characters.
 *
 * \param s The string in which to encode the special characters.
 * \return A new string with special characters encoded.
 */
string XMLHelper::encodeURLSpecialCharacters(const std::string& s) {
     string res;
     for (unsigned int i = 0; i < s.length(); i++) {
	  switch (s[i]) {
	  case ' ':
	       res += "%20";
	       break;
	  default:
	       res += s[i];
	       break;
	  }
     }
     return res;
}

/**
 * \brief Prints a Time object to the provided stream in XML Schema
 * dateTime format. Always assumes UTC.
 *
 * \param o The stream to write to. 
 * \param time The Time object to write.
 */
void XMLHelper::timeToDateTime(ostream& o, Time time)
{
     int64_t ms = time.milliSeconds();
     int64_t sec64 = ms / 1000;
     time_t sec = static_cast<time_t>(sec64);

     struct tm ts;
     memset(&ts, '0', sizeof(ts));
#ifdef __win__
     _gmtime64_s(&ts, &sec);
#else
      gmtime_r(&sec, &ts);
#endif

     ts.tm_year += 1900;
     ts.tm_mon += 1;

     char fillc = o.fill(); 
     o.fill('0');
     o << setw(4) << ts.tm_year << "-" << setw(2) << ts.tm_mon << "-" << setw(2) << ts.tm_mday << "T";
     o << setw(2) << ts.tm_hour << ":" << setw(2) << ts.tm_min << ":" << setw(2) << ts.tm_sec;
     o << "." << setw(3) << ms - (sec64 * 1000) << "Z";
     o.fill(fillc);
}

/**
 * \brief Converts the given XML Schema dateTime string to a Time
 * object.
 *
 * \param dateTime The dateTime string.
 * \return The Time object.
 */
Time XMLHelper::dateTimeToTime(const string& dateTime)
{
     int millis = 0;
     int tz_hour = 0;
     int tz_min = 0;
     char tz_prefix = 'Z';
     struct tm ts;
     memset(&ts, '0', sizeof(ts));

     if (dateTime.length() > 0) {
	  char c;
	  istringstream ist(dateTime);
	  if (ist) {
	       ist >> ts.tm_year >> c;
	  }
	  if (c == '-' && ist) {
	       ist >> ts.tm_mon >> c;
	  }
	  if (c == '-' && ist) {
	       ist >> ts.tm_mday >> c;
	  }
	  if (c == 'T' && ist) {
	       ist >> ts.tm_hour >> c;
	  }
	  if (c == ':' && ist) {
	       ist >> ts.tm_min >> c;
	  }
	  if (c == ':' && ist) {
	       ist >> ts.tm_sec;
	  }
	  else {
	       Error e;
	       e << "'" << dateTime << "' is not a valid dateTime string.";
	       throw e;
	  }
	  if (!ist.eof() && ist.peek() == '.') {
	       double tmp;
	       ist >> tmp;
	       millis = static_cast<int>(tmp * 1000);
	  }
	  if (!ist.eof() && ist.peek() != 'Z') {
	       ist >> tz_prefix;
	       ist >> tz_hour >> c;
	       if (!ist.eof() && c == ':') {
		    ist >> tz_min;
	       }
	       else {
		    Error e;
		    e << "The dateTime '" << dateTime << "' has an invalid time zone format.";
		    throw e;
	       }
	  }

	  ts.tm_year -= 1900;
	  ts.tm_mon -= 1;
	  // UTC is winter-time
	  ts.tm_isdst = 0;

	  int64_t tz_offset = (tz_hour * 3600 + tz_min * 60) * (tz_prefix == '-' ? -1 : 1);

	  return Time(0, 0, 0, 0, static_cast<int64_t>((-tz_offset + mktime(&ts)) * 1000 + millis));
     }
     else {
	  Error e;
	  e << "dateTime string has length = 0";
	  throw e;
     }
}

/**
 * \brief Base64 encodes data and werites it to the provided stream.
 *
 * Also handles swapping of byte order.
 *
 * \param toEncode The data to encode.
 * \param numElements Number of elements in the provided data array.
 * \param swapByteOrder Should be set to true if the byte order should
 *  be swapped.
 * \param o The stream to write to.
 * \return The provided stream with the base64 string written to it.
 */
template<class T> ostream& XMLHelper::base64Print(const T* const toEncode,
						  int numElements,
						  bool swapByteOrder,
						  ostream& o)
{
     if (swapByteOrder) {
	  T *swapped = new T[numElements];
	  for (int i = 0; i < numElements; i++) {
	       swapped[i] = toEncode[i];
	       ByteSwap(swapped[i]);
	  }
	  base64Print(reinterpret_cast<const int8_t*>(swapped), numElements * sizeof(T), o);
	  delete [] swapped;
     }
     else {
	  base64Print(reinterpret_cast<const int8_t*>(toEncode), numElements * sizeof(T), o);
     }
     return o;
}


/**
 * \brief Base64 encodes the provided byte array
 *
 * \param toEncode The data to encode.
 * \param nBytesToEncode Number of bytes in the provided data array.
 * \param o The stream to write to.
 * \return The provided stream with the base64 string written to it.
 */
ostream& XMLHelper::base64Print(const int8_t* toEncode, int nBytesToEncode, ostream& o)
{
     unsigned int encodedLength = 0;
     XMLByte* encoded = Base64::encode(reinterpret_cast<const XMLByte*>(toEncode),
				       nBytesToEncode / sizeof(XMLByte), &encodedLength);
     
     if(!encodedLength) {
	  Error e;
	  e << "Couldn't encode to base64";
	  throw e;
     }

     o.write(reinterpret_cast<char*>(encoded), encodedLength * sizeof(XMLByte));

     // Should be this way in order to avoid mismatched delete [] and new. Can't
     // use XMLString::release since it uses delete [].
#ifdef __win__
     XMLString::release(&encoded);
#else
     delete encoded;
#endif
     return o;
}

/**
 * \brief Gets an XMLCh string from the first subelement of the
 * provided DOMElement that has the tag 'tag'.
 *
 * \param n The parent DOMElement.
 * \param tag The tag of the subelement.
 * \return The XMLCh string or null if no element with the specified
 * tag was found.
 */
const XMLCh* XMLHelper::getXMLChString(const DOMElement &n, const char *tag)
{
     DOMElement* elem = getFirstChildByTag(n, tag);
     for (DOMNode *child = elem->getFirstChild(); child != 0; child = child->getNextSibling()) {
	  if (child->getNodeType() == DOMNode::TEXT_NODE) {
	       return static_cast<DOMText*>(child)->getData();
	  }
     }
     return 0;
}

/**
 * \brief Gets an int representation of the attribute named 'tag' of
 * the provided DOMElement.
 *
 * \param n The DOMElement to get the attribute from.
 * \param tag The name of the attribute.
 * \return The int representation of the attribute.
 */
int XMLHelper::getIntAttribute(const DOMElement &n, const char *tag)
{
     int ret;
     StrX id(n.getAttribute(XStr(tag).str()));
     char *endp = reinterpret_cast<char*>(4711);
     ret = strtol(id.str(), &endp, 10);
     if (endp == 0 && *endp != 0) {
	  Error e;
	  e << "Couldn't convert '" << id.str() << "' to an integer in Subscription::Subscription()";
	  throw e;
     }

     return ret;
}

/**
 * \brief Gets an double representation of the attribute named 'tag' of
 * the provided DOMElement.
 *
 * \param n The DOMElement to get the attribute from.
 * \param tag The name of the attribute.
 * \return The double representation of the attribute.
 */
double XMLHelper::getDoubleAttribute(const DOMElement &n, const char *tag)
{
     double ret;
     StrX id(n.getAttribute(XStr(tag).str()));
     ret = strtod(id.str(), 0);
     return ret;
}

/**
 * \brief Gets a string representation of the attribute named 'tag' of
 * the provided DOMElement.
 *
 * \param n The DOMElement to get the attribute from.
 * \param tag The name of the attribute.
 * \return The string representation of the attribute.
 */
string XMLHelper::getStringAttribute(const DOMElement &n, const char *tag)
{
     StrX type(n.getAttribute(XStr(tag).str()));
     return type.str();
}

/**
 * \brief Convenience function for getting the xsi:type attribute and
 * stripping the leading namespace from it.
 *
 * \param n The DOMElement to get the type attribute from.
 * \return The string representation of type.
 */
string XMLHelper::getTypeAttribute(const DOMElement &n)
{
     string type = getStringAttribute(n, "xsi:type");
     return removeNamespace(type);
}

/**
 * \brief Finds the first subelement of the provided DOMElement that
 * has a tag that matches the specified tag and returns a bool
 * representation its content.
 *
 * \param n The parent DOMElement.
 * \param tag The tag of the subelement.
 * \return The bool representation of the subelement's content.
 */
bool XMLHelper::getBool(const DOMElement &n, const char *tag)
{
     bool res;
     string tmp;
     getString(n, tag, tmp);
     if (tmp == "true" || tmp == "1") {
	  res = true;
     }
     else if (tmp == "false" || tmp == "0") {
	  res = false;
     }
     else {
	  Error e;
	  e << "Invalid boolean value: '" << tmp << "' for element " << tag;
	  throw e;
     }
     return res;
}

/**
 * \brief Finds the first subelement of the provided DOMElement that
 * has a tag that matches the specified tag and returns a double
 * representation its content.
 *
 * \param n The parent DOMElement.
 * \param tag The tag of the subelement.
 * \return The double representation of the subelement's content.
 */
double XMLHelper::getDouble(const DOMElement &n, const char *tag)
{
     string tmp;
     double res;
     getString(n, tag, tmp);
     res = strtod(tmp.c_str(), 0);
     return res;
}

/**
 * \brief Finds the first subelement of the provided DOMElement that
 * has a tag that matches the specified tag and returns a int
 * representation its content.
 *
 * \param n The parent DOMElement.
 * \param tag The tag of the subelement.
 * \return The int representation of the subelement's content.
 */
int XMLHelper::getInt(const DOMElement &n, const char *tag)
{
     string tmp;
     int res;
     getString(n, tag, tmp);
     res = strtol(tmp.c_str(), 0, 10);
     return res;
}

/**
 * \brief Finds the first subelement of the provided DOMElement that
 * has a tag that matches the specified tag and returns a int64_t
 * representation its content.
 *
 * \param n The parent DOMElement.
 * \param tag The tag of the subelement.
 * \return The int64_t representation of the subelement's content.
 */
int64_t XMLHelper::getLongInt(const DOMElement &n, const char *tag)
{
     string tmp;
     int64_t res;
     getString(n, tag, tmp);
#ifdef __win__
     res = _strtoi64(tmp.c_str(), 0, 10);
#else
     res = strtoll(tmp.c_str(), 0, 10);
#endif
     return res;
}

/**
 * \brief Finds the first subelement of the provided DOMElement that
 * has a tag that matches the specified tag and returns a Shape
 * representation its content.
 *
 * \param n The parent DOMElement.
 * \param tag The tag of the subelement.
 * \param scope The Reference to the scope this shape should live in.
 * \return The Shape representation of the subelement's content.
 */
Shape *XMLHelper::getShape(const DOMElement &n, const char *tag, const Reference& scope)
{
     DOMElement *shape = getFirstChildByTag(n, tag);
     if (!shape) {
	  Error e;
	  e << "No Shape element with tag '" << tag << "' found";
	  throw e;
     }
     return getShape(*shape, scope);
}

/**
 * \brief Finds the first subelement of the provided DOMElement that
 * has a tag that matches the specified tag and returns a string
 * representation its content.
 *
 * \param n The parent DOMElement.
 * \param tag The tag of the subelement.
 * \param outStr The string representation of the subelement's content.
 */
void XMLHelper::getString(const DOMElement &n, const char *tag, string &outStr)
{
     const XMLCh *str = getXMLChString(n, tag);
     outStr = (str ? StrX(str).str() : "");
}

/**
 * \brief Finds the first subelement of the provided DOMElement that
 * has a tag that matches the specified tag and returns a Time
 * representation of its contents.
 *
 * Used to extract data from ValueType descendants.
 *
 * \param n The parent DOMElement.
 * \param tag The tag of the subelement.
 * \return The Time representation of the subelement's content.
 */
Time XMLHelper::getTime(const DOMElement &n, const char *tag)
{
     string tmp;
     getString(n, tag, tmp);
     return dateTimeToTime(tmp);
}




/**
 * \brief Finds the first subelement of the provided DOMElement that
 * has a tag that matches the specified tag and returns a Time
 * representation of its value subelement.
 *
 * Used to extract data from ValueType descendants.
 *
 * \param n The parent DOMElement.
 * \param tag The tag of the subelement.
 * \return The Time representation of the value element's content.
 */
Time XMLHelper::getElementTimestampValue(const DOMElement &n, const char *tag)
{
     string tmp;
     getElementStringValue(n, tag, tmp);
     return dateTimeToTime(tmp);
}




/**
 * \brief Finds the first subelement of the provided DOMElement that
 * has a tag that matches the specified tag and returns a bool
 * representation of its value subelement.
 *
 * Used to extract data from ValueType descendants.
 *
 * \param n The parent DOMElement.
 * \param tag The tag of the subelement.
 * \return The bool representation of the value element's content.
 */
bool XMLHelper::getElementBoolValue(const DOMElement &n, const char *tag)
{
     DOMElement *elem = getFirstChildByTag(n, tag);
     if (!elem) {
	  Error e;
	  e << "No " << tag << " tag in element " << StrX(n.getNodeName()).str();
	  throw e;
     }
     return getBool(*elem, "value");
}

/**
 * \brief Finds the first subelement of the provided DOMElement that
 * has a tag that matches the specified tag and returns a double
 * representation of its value subelement.
 *
 * Used to extract data from ValueType descendants.
 *
 * \param n The parent DOMElement.
 * \param tag The tag of the subelement.
 * \return The double representation of the value element's content.
 */
double XMLHelper::getElementDoubleValue(const DOMElement &n, const char *tag)
{
     DOMElement *elem = getFirstChildByTag(n, tag);
     if (!elem) {
	  Error e;
	  e << "No " << tag << " tag in element " << StrX(n.getNodeName()).str();
	  throw e;
     }
     return getDouble(*elem, "value");
}

/**
 * \brief Finds the first subelement of the provided DOMElement that
 * has a tag that matches the specified tag and returns a int
 * representation of its value subelement.
 *
 * Used to extract data from ValueType descendants.
 *
 * \param n The parent DOMElement.
 * \param tag The tag of the subelement.
 * \return The int representation of the value element's content.
 */
int XMLHelper::getElementIntValue(const DOMElement &n, const char *tag)
{
     DOMElement *elem = getFirstChildByTag(n, tag);
     if (!elem) {
	  Error e;
	  e << "No " << tag << " tag in element " << StrX(n.getNodeName()).str();
	  throw e;
     }
     return getInt(*elem, "value");
}

/**
 * \brief Finds the first subelement of the provided DOMElement that
 * has a tag that matches the specified tag and returns a int64_t
 * representation of its value subelement.
 *
 * Used to extract data from ValueType descendants.
 *
 * \param n The parent DOMElement.
 * \param tag The tag of the subelement.
 * \return The int64_t representation of the value element's content.
 */
int64_t XMLHelper::getElementLongIntValue(const DOMElement &n, const char *tag)
{
     DOMElement *elem = getFirstChildByTag(n, tag);
     if (!elem) {
	  Error e;
	  e << "No " << tag << " tag in element " << StrX(n.getNodeName()).str();
	  throw e;
     }
     return getLongInt(*elem, "value");
}

/**
 * \brief Finds the first subelement of the provided DOMElement that
 * has a tag that matches the specified tag and returns a string
 * representation of its value subelement.
 *
 * Used to extract data from ValueType descendants.
 *
 * \param n The parent DOMElement.
 * \param tag The tag of the subelement.
 * \param outStr The string representation of the value element's
 * content.
 */
void XMLHelper::getElementStringValue(const DOMElement &n, const char *tag, string &outStr)
{
     DOMElement *elem = getFirstChildByTag(n, tag);
     if (!elem) {
	  Error e;
	  e << "No " << tag << " tag in element " << StrX(n.getNodeName()).str();
	  throw e;
     }
     getString(*elem, "value", outStr);
}

/**
 * \brief Gets a Shape representation of the provided DOMElement.
 *
 * \param n The parent DOMElement.
 * \param scope The Reference to the scope this shape should live in.
 * \return The Shape representation of the element's content.
 */
Shape *XMLHelper::getShape(const DOMElement &n, const Reference& scope)
{
     Shape *res = 0;

     string id = getStringAttribute(n, "identifier");

     // If no id is specified - use the tag of the element.
     if (id == "") {
	  id = StrX(n.getTagName()).str();
     }

     // Get the Reference to the shape to be created.
     const Reference& ref = Reference::get(scope, id);

     string type = getStringAttribute(n, "xsi:type");

     // Circle
     if (type == "sp:Circle") {
	  DOMElement *e = getFirstChildByTag(n, "center");
	  // Get radius and Lat Lng for center point
	  res = new Circle(LatLng(getDouble(*e, "lat"), getDouble(*e, "lon")),
			   getElementDoubleValue(n, "radius"), ref);
     }
     // Polygon, e.g. a number of curves that must be of type sp:Line
     else if (type == "sp:Polygon") {
	  vector<DOMElement*> curves;
	  getChildElementsByTag(n, "curves", curves);

	  gpc_polygon p = {0, 0, 0};
	  gpc_vertex_list *aList = new gpc_vertex_list;
	  aList->num_vertices = curves.size();

	  // Make sure we get the lines of the polygon in the right
	  // order, e.g line(i).p2 == line(i+1).p1 etc.
	  list<string> identifiers;
	  std::map<const Point, Line*> lines; // Mapping between startpoint and line.
	  Line* firstLine = 0;
	  for (vector<DOMElement*>::iterator it = curves.begin(); it != curves.end(); it++) {
	       StrX ctype((*it)->getAttribute(XStr("xsi:type").str()));
	       if (ctype == "sp:Line") {
		    Line* line = new Line(**it);
		    if (!firstLine) {
			 firstLine = line;
		    }
		    lines[line->p1()] = line;
	       }
	       else {
		    // Should clean up memory here.
		    Error e;
		    e << "Invalid Curve type: '" << ctype << "' in Polygon";
		    throw e;
	       }
	  }
	  // It isn't really necessary to use the first line since we
	  // should assume that the polygon contains no lines such
	  // that p1 == p2. However, this saves us if we have such
	  // line at the end of the polygon.
	  Line* line = firstLine;
	  aList->vertex = new gpc_vertex[aList->num_vertices];
	  for(int i = 0; i < aList->num_vertices; i++) {
	       identifiers.push_back(line->identifier());
	       aList->vertex[i].x = line->p1().x();
	       aList->vertex[i].y = line->p1().y();
	       // The next line must be the one which startpoint is this line's endpoint.
	       line = lines[line->p2()];
	       if (line == 0) {
		    Error e;
		    e << "Null Line while creating Polygon. This may indicate that the sent\n";
		    e << "polygon wasn't closed or that there is a roundoff error, e.g that\n";
		    e << "there are at least one pair of consecutive lines i and i+1 such that\n";
		    e << "line(i).p2 != line(i+1).p1";
		    throw e;
	       }
	  }
	  for (std::map<const Point, Line*>::iterator it = lines.begin(); it != lines.end(); it++) {
	       delete it->second;
	  }
	  gpc_add_contour(&p, aList, false);
	  delete [] aList->vertex;
	  delete aList;
	  res = new Polygon(p, identifiers, ref);
	  Polygon::deallocGpcPolygon(p);
     }
     else if (type == "sp:Composite") {
	  vector<DOMElement*> parts;
	  getChildElementsByTag(n, "shapes", parts);
	  CompositeShape *tmp = new CompositeShape(ref);
	  const Reference &listRef = Reference::get(tmp->ref(), "shapes");
	  for (vector<DOMElement*>::iterator it = parts.begin(); it != parts.end(); it++) {
	       tmp->addShape(getShape(**it, listRef));
	  }	  
	  res = tmp;
     }
     else {
	  Error e;
	  e << "Unknown Shape type : " << type;
	  throw e;
     }
     return res;
}

/**
 * \brief Finds the first subelement of the provided DOMElement that
 * has a tag that matches the specified tag.
 *
 * \param n The parent DOMElement.
 * \param tag The tag of the subelement.
 * \return The first subelement with a matching tag of null if no such
 * element was found..
 */
inline DOMElement *XMLHelper::getFirstChildByTag(const DOMElement &n, const char *tag)
{
     for (DOMNode *child = n.getFirstChild(); child != 0; child = child->getNextSibling()) {
	  if (child->getNodeType() == DOMNode::ELEMENT_NODE) {
	       DOMElement *elem = static_cast<DOMElement*>(child);
	       StrX nodeName(elem->getNodeName());
	       if (nodeName == tag) {
		    return elem;
	       }
	  }
     }
     return 0;
}

/**
 * \brief Finds the first subelement of the provided DOMElement that
 * has a tag that matches the specified tag.
 *
 * \param n The parent DOMElement.
 * \param tag The tag of the subelement.
 * \return The first subelement with a matching tag of null if no such
 * element was found..
 */
DOMElement* XMLHelper::getFirstChildByTag(const DOMElement &n, const std::string& tag)
{
     return getFirstChildByTag(n, tag.c_str());
}

/**
 * \brief Finds all subelements of the provided DOMElement that has a
 * tag that matches the specified tag.
 *
 * \param n The parent DOMElement.
 * \param tag The tag of the subelement.
 * \param ioV A vector that on return contains all subelements with
 * matching tag.
 */
void XMLHelper::getChildElementsByTag(const DOMElement &n, const char *tag, std::vector<DOMElement*> &ioV)
{
     for (DOMNode *child = n.getFirstChild(); child != 0; child = child->getNextSibling()) {
	  if (child->getNodeType() == DOMNode::ELEMENT_NODE) {
	       DOMElement *elem = static_cast<DOMElement*>(child);
	       StrX nodeName(elem->getNodeName());
	       if (nodeName == tag) {
		    ioV.push_back(elem);
	       }
	  }
     }
}

/**
 * \brief Finds all subelements of the provided DOMElement that has a
 * tag that matches the specified tag.
 *
 * \param n The parent DOMElement.
 * \param tag The tag of the subelement.
 * \param ioV A vector that on return contains all subelements with
 * matching tag.
 */
void XMLHelper::getChildElementsByTag(const DOMElement &n, const std::string& tag, std::vector<DOMElement*> &ioV)
{
     getChildElementsByTag(n, tag.c_str(), ioV);
}



/**
 * \brief Creates a Line from the provided DOMElement.
 *
 * \param n The DOMElement to create this Line from.
 */
Line::Line(const DOMElement& n) : mId(XMLHelper::getStringAttribute(n, "identifier"))
{
     DOMElement *p1 = XMLHelper::getFirstChildByTag(n, "p1");
     DOMElement *p2 = XMLHelper::getFirstChildByTag(n, "p2");
     mP1.set(XMLHelper::getDouble(*p1, "lon"), XMLHelper::getDouble(*p1, "lat"));
     mP2.set(XMLHelper::getDouble(*p2, "lon"), XMLHelper::getDouble(*p2, "lat"));
}
