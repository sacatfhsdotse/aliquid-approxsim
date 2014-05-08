#ifndef APPROXSIM_XMLHELPER_H
#define APPROXSIM_XMLHELPER_H


// System
#include <string>
#include <vector>

// Xerces
#include <xercesc/util/XercesDefs.hpp>

// Own
#include "Time2.h"
#include "StrX.h"
#include "Graph.h"

using namespace std;

// Forward Declarations
class Reference;
class Shape;

namespace XERCES_CPP_NAMESPACE {
     class DOMElement;
}


XERCES_CPP_NAMESPACE_USE


/**
 * \brief This class contains various static functions for handling
 * xml related tasks.
 *
 * \author   Per Alexius
 * \date     $Date: 2007/01/24 13:13:27 $
 */
class XMLHelper {
private:
     /**
      * Private default constructor since this class should not be
      * instantiated.
      */
     XMLHelper() {}

public:
     static std::string  nodeTypeToString(int i);
     static std::string& removeNamespace(std::string &s);
     static std::string  encodeSpecialCharacters(const std::string& s);
     static std::string  encodeURLSpecialCharacters(const std::string& s);
     static void         timeToDateTime(std::ostream& o, Time time);
     static Time         dateTimeToTime(const std::string& dateTime);

     static std::ostream& base64Print(const int8_t* toEncode, int nBytesToEncode, std::ostream& o);
     template <class T> static std::ostream& base64Print(const T* const toEncode, int numElements, bool swapByteOrder, std::ostream& o);

     static const XMLCh* getXMLChString(const DOMElement &n, const char* tag);
     static int          getIntAttribute(const DOMElement &n, const char *tag);
     static double       getDoubleAttribute(const DOMElement &n, const char *tag);
     static std::string  getStringAttribute(const DOMElement &n, const char *tag);
     static std::string  getTypeAttribute(const DOMElement &n);

     static bool         getBool(const DOMElement &n, const char *tag);
     static double       getDouble(const DOMElement &n, const char *tag);
     static Effect       getEffect(const DOMElement &n, const char *tag);
     static int          getInt(const DOMElement &n, const char *tag);
     static int64_t      getLongInt(const DOMElement &n, const char *tag);
     static Shape*       getShape(const DOMElement &n, const char *tag, const Reference& scope);
     static void         getString(const DOMElement &n, const char *tag, std::string &outStr);
     static Time         getTime(const DOMElement &n, const char *tag);
     template<class T>
     static Graph<T>*    getGraph(const DOMElement &n, const Reference& scope);
                               
     static bool         getElementBoolValue(const DOMElement &n, const char *tag);
     static int          getElementIntValue(const DOMElement &n, const char *tag);
     static int64_t      getElementLongIntValue(const DOMElement &n, const char *tag);
     static double       getElementDoubleValue(const DOMElement &n, const char *tag);
     static void         getElementStringValue(const DOMElement &n, const char *tag, std::string &outStr);
     static Time         getElementTimestampValue(const DOMElement &n, const char *tag);
                               
     static Shape*       getShape(const DOMElement &n, const Reference& scope);

     static DOMElement*  getFirstChildByTag(const DOMElement &n, const char *tag);
     static DOMElement*  getFirstChildByTag(const DOMElement &n, const std::string& tag);
     static void         getChildElementsByTag(const DOMElement &n,
                                                      const char *tag,
                                                      std::vector<DOMElement*> &ioV);
     static void         getChildElementsByTag(const DOMElement &n,
                                                      const std::string& tag,
                                                      std::vector<DOMElement*> &ioV);
};


/**
 * \brief Class representing a point. Used when parsing Polygons.
 *
 * \author   Per Alexius
 * \date     $Date: 2007/01/24 13:13:27 $
 */
class Point {
private:
     double mX;   ///< The x coordinate.
     double mY;   ///< The y coordinate.
public:
     /// Default constructor.
     Point() : mX(0), mY(0) {}

     /**
      * \brief Copy constructor.
      *
      * \param p The Point to copy.
      */
     Point(const Point& p) : mX(p.x()), mY(p.y()) {}

     /**
      * \brief Mutator.
      *
      * \param x The x-coordinate.
      * \param y The y-coordinate.
      */
     void set(double x, double y) { mX = x; mY = y; }

     /**
      * \brief Accessor for the x-coordinate.
      *
      * \return x The x-coordinate.
      */
     double x() const { return mX; }

     /**
      * \brief Accessor for the y-coordinate.
      *
      * \return y The y-coordinate.
      */
     double y() const { return mY; }

     /**
      * \brief Assignment operator.
      *
      * \param p The Point to assign to this point.
      * \return The assigned Point.
      */
     Point& operator = (const Point& p) { mX = p.x(); mY = p.y(); return *this; }

     /**
      * \brief Less-than operator.
      *
      * \param p The Point to compare with.
      * \return True if this Point is less than the provided Point.
      */
     bool operator < (const Point& p) const { return ( (mX < p.x()) || ( (mX == p.x()) && (mY < p.y()) ) ); }

     /**
      * \brief Equality operator.
      *
      * \param p The Point to compare with.
      * \return True if this Point is equal to the provided Point.
      */
     bool operator == (const Point& p) const { return ( (mX == p.x()) && (mY == p.y()) ); }
};

/**
 * \brief Class representing a line. Used when parsing Polygons.
 *
 * \author   Per Alexius
 * \date     $Date: 2007/01/24 13:13:27 $
 */
class Line {
private:
     std::string mId;   ///< The id of the line.
     Point mP1;         ///< The start point.
     Point mP2;         ///< The end point.
public:
     inline Line(const DOMElement& n);
     
     /**
      * \brief Accessor for the identifier.
      *
      * \return The identifier.
      */
     const std::string& identifier() const { return mId; }

     /**
      * \brief Accessor for the start point.
      *
      * \return The start point.
      */
     const Point& p1() const { return mP1; }

     /**
      * \brief Accessor for the end point.
      *
      * \return The end point.
      */
     const Point& p2() const { return mP2; }
};

#endif   // APPROXSIM_XMLHELPER_H
