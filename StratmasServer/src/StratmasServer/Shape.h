#ifndef STRATMAS_SHAPE_H
#define STRATMAS_SHAPE_H

// System
#include <list>
#include <map>
#include <string>
#include <vector>

// Own
#include "Error.h"
#include "GoodStuff.h"
#include "gpc.h"
#include "GridPos.h"
#include "LatLng.h"
#include "ProjCoord.h"
#include "Referencable.h"

// Forward Declarations
class BasicGrid;


/**
 * \brief An abstract base class for all Shapes.
 *
 * For more documentation of the various functions - see the
 * subclasses that implement them.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/19 07:04:39 $
 */
class Shape : public Referencable
{
protected:
     /**
      * \brief Indicates whether this shape is stored in projected
      *  coordinates or not.
      */
     bool mProjected;

     /**
      * \brief Keeps track of the number of times this Shape has been
      * changed.
      */
     int mChanges;

public:
     Shape();
     Shape(const Reference& ref);

     /// Destructor.
     virtual ~Shape() {}

     /**
      * \brief Make this shape believe that it has been modified.
      */
     void touch() { mChanges++; }

     /**
      * \brief Accessor for the projected flag.
      *
      * \return The status of the projected flag.
      */
     bool projected() const { return mProjected; }

     /**
      * \brief Accessor for the number of changes.
      *
      * \return The number of changes.
      */
     int changes() const { return mChanges; }

     /**
      * \brief Projects this Shape using the current projection.
      */
     virtual void toProj() { toProj(*Projection::currentProjection()); }

     /**
      * \brief Projects this Shape using the specified Projection.
      *
      * \param proj The projection to use.
      */
     virtual void toProj(const Projection &proj) = 0;

     /**
      * \brief Transforms this Shape to lat lng coordinate using the
      * current projection.
      */
     virtual void toCoord() { toCoord(*Projection::currentProjection()); }

     /**
      * \brief Transforms this Shape to lat lng coordinate using the
      * provided projection.
      *
      * \param proj The projection to use.
      */
     virtual void toCoord(const Projection &proj) = 0;

     /**
      * \brief Returns a list containing pointers to all cells covered
      * by this Shape.
      *
      * \param g A reference to the Grid.
      * \param outCells A list that on return contains pointers to all
      * cells covered by this Shape.
      */
     virtual void cells(const BasicGrid& g, std::list<GridPos> &outCells) const = 0;

     /**
      * \brief Returns the center coordinate in lat lng of this Shape.
      *
      * \return The center coordinate of this Shape.
      */
     virtual LatLng cenCoord() const = 0;

     /**
      * \brief Returns the center coordinate in projection space of
      * this Shape.
      *
      * \return The center coordinate of this Shape.
      */
     virtual ProjCoord cenProj() const = 0;

     /**
      * \brief Gets the bounding box of this Shape
      *
      * \param t Top coordinate of this Shape's boundingbox.
      * \param l Left coordinate of this Shape's boundingbox.
      * \param b Bottom coordinate of this Shape's boundingbox.
      * \param r Right coordinate of this Shape's boundingbox.
      */
     virtual void boundingBox(double &t, double &l, double &b, double &r) const = 0;

     /**
      * \brief Returns the area of this Shape.
      *
      * \return The area of this Shape.
      */
     virtual double area()  const = 0;

     /**
      * \brief Moves this Shape relative to itself.
      *
      * \param dx The movement in x-direction in degrees longitude
      * \param dy The movement in y-direction in degrees latitude
      */
     virtual void move(double dx, double dy) = 0;

     /**
      * \brief Moves this Shape to a new position.
      *
      * \param newPos The position to move to.
      */
     virtual void move(LatLng newPos) = 0;

     /**
      * \brief Creates a deep copy of this Shape
      *
      * \return A newly allocated copy of this Shape.
      */
     virtual Shape* clone() const = 0;

     /**
      * \brief Returns the stratmas protocol type of this shape.
      *
      * \return The stratmas protocol type of this shape.
      */
     virtual const std::string type() const = 0;

     /**
      * \brief Writes an XML representation of this object to the
      * provided stream.
      *
      * \param o The stream to write to.
      * \return The stream with the xml representation written to it.
      */
     std::ostream& toXML(std::ostream &o) const { return toXML(o, ""); }

     /**
      * \brief Writes an XML representation of this object to the
      * provided stream with nice indentation.
      *
      * \param o The stream to write to.
      * \param indent Indentation string.
      * \return The stream with the xml representation written to it.
      */
     virtual std::ostream& toXML(std::ostream &o, std::string indent) const = 0;
     virtual std::ostream& cellsToXML(const BasicGrid& grid, bool swapEndian, std::ostream &o) const;
};



/**
 * \brief A class representing a Circle
 *
 * \author Per Alexius
 * \date     $Date: 2006/07/19 07:04:39 $
 */
class Circle : public Shape {
protected:
     LatLng mCenter;    ///< The center of this Circle
     double mRadius;   ///< The radius of this Circle

public:
     /**
      * \brief Creates a Circle.
      *
      * \param l The center position of the Circle.
      * \param r The radius of the Circle.
      * \param ref The Reference to the Circle.
      */
     Circle(const LatLng l, double r, const Reference& ref) : Shape(ref), mCenter(l), mRadius(r) {}
     Circle(const LatLng l, double r);

     /// Destructor
     virtual ~Circle() {}

     /**
      * \brief Projects this Shape using the specified Projection.
      *
      * \param proj The projection to use.
      */
     void toProj(const Projection &proj) { mProjected = true; }

     /**
      * \brief Transforms this Shape to lat lng coordinate using the
      * provided projection.
      *
      * \param proj The projection to use.
      */
     void toCoord(const Projection &proj) { mProjected = false; }

     void cells(const BasicGrid &g, std::list<GridPos> &outCells) const;

     /**
      * \brief Returns the center coordinate in lat lng of this Shape.
      *
      * \return The center coordinate of this Shape.
      */
     LatLng cenCoord() const { return mCenter; }

     /**
      * \brief Returns the center coordinate in projection space of
      * this Shape.
      *
      * \return The center coordinate of this Shape.
      */
     ProjCoord cenProj() const { return mCenter.toCoord(); }
     void boundingBox(double &t, double &l, double &b, double &r) const;

     /**
      * \brief Returns the area of this Shape.
      *
      * \return The area of this Shape.
      */
     double area() const { return mRadius * mRadius * kPi; }

     /**
      * \brief Moves this Shape relative to itself.
      *
      * \param dx The movement in x-direction in degrees longitude
      * \param dy The movement in y-direction in degrees latitude
      */
     void move(double dx, double dy) {
	  if (dx != 0 || dy != 0) {
	       mCenter = LatLng(mCenter.lat() + dy, mCenter.lng() + dx);
	       mChanges++;
	  }
     }

     /**
      * \brief Moves this Shape to a new position.
      *
      * \param newPos The position to move to.
      */
     void move(LatLng newPos) {
	  if (newPos != mCenter) {
	       mCenter = newPos;
	       mChanges++;
	  }
     }

     /**
      * \brief Creates a deep copy of this Shape
      *
      * \return A newly allocated copy of this Shape.
      */
     Shape* clone() const {
	  Circle *r = new Circle(mCenter, mRadius, ref());
	  r->mProjected = mProjected;
	  r->mChanges = mChanges;
	  return r;
     }

     /**
      * \brief Returns the stratmas protocol type of this shape.
      *
      * \return The stratmas protocol type of this shape.
      */
     const std::string type() const { return "Circle"; }

     /**
      * \brief Accessor for the raduis of this Circle.
      *
      * \return The raduis of this Circle.
      */
     double radius() const { return mRadius; }

     std::ostream& toXML(std::ostream &o, std::string indent) const;

     // Friends
     /// For debugging purposes
     friend std::ostream& operator << (std::ostream &o, const Circle &c);
};


/**
 * \brief A class representing a Polygon
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/19 07:04:39 $
 */
class Polygon : public Shape {
protected:
     LatLng                 mCenter;     ///< Center of the Polygon's bounding box
     gpc_polygon            mBoundary;   ///< The Polygon's boundary is represented as a gpc_polygon
     std::list<std::string> mLineId;     ///< List containing the identifiers of the lines.   

public:
     Polygon();
     Polygon(const Reference& ref);
     Polygon(const gpc_polygon &p, std::list<std::string> lineId, const Reference& ref);
     Polygon(LatLng cen, double ang, double w, double h, const Reference& r);
     Polygon(const Polygon &p);

     // Destructor
     virtual ~Polygon() { deallocGpcPolygon(mBoundary); }

     void toProj(const Projection &proj);
     void toCoord(const Projection &proj);
     void cells(const BasicGrid &g, std::list<GridPos> &outCells) const;

     /**
      * \brief Returns the center coordinate in lat lng of this Shape.
      *
      * \return The center coordinate of this Shape.
      */
     LatLng cenCoord() const { return mCenter; }

     /**
      * \brief Returns the center coordinate in projection space of
      * this Shape.
      *
      * \return The center coordinate of this Shape.
      */
     ProjCoord cenProj() const { return mCenter.toCoord(); }
     void boundingBox(double &t, double &l, double &b, double &r) const;

     /**
      * \brief Returns the area of this Shape. Not yet implemented for
      * Polygons
      *
      * \return The area of this Shape.
      */
     double area() const { Error e("Polygon::area() is not yet implemented"); throw e; return 0; }

     void move(double dx, double dy);
     void move(LatLng newPos);

     /**
      * \brief Creates a deep copy of this Shape
      *
      * \return A newly allocated copy of this Shape.
      */
     Shape *clone() const { return new Polygon(*this); }

     /**
      * \brief Returns the stratmas protocol type of this shape.
      *
      * \return The stratmas protocol type of this shape.
      */
     const std::string type() const { return "Polygon"; }

     /**
      * \brief Accessor for the gpc_polygon that constitutes this
      * Polygon.
      *
      * \return The gpc_polygon that constitutes this Polygon.
      */
     const gpc_polygon &boundary() const { return mBoundary; }

     std::ostream& toXML(std::ostream &o, std::string indent) const;

     static void deallocGpcPolygon(gpc_polygon &p);
     static void deepCopyGpcPolygon(gpc_polygon &dst, const gpc_polygon &src);

     // Friends
     /// For debugging purposes
     friend std::ostream &operator << (std::ostream &o, const Polygon &p);
};


/**
 * \brief A class representing a CompositeShape.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/19 07:04:39 $
 */
class CompositeShape : public Shape
{
private:
     /// True if the center of this CompositeShape is calculated.
     mutable bool mCenterCalculated;

     /// The center of this CompositeShape.
     mutable LatLng mCenter;

     /**
      * \brief A map containing all subshapes this Composite Shape
      * contains.
      */
     std::map<std::string, Shape*> mShapes;
     
public:
     /**
      * \brief Creates an empty CompositeShape.
      *
      * \param ref The Reference to the Circle.
      */
     CompositeShape(const Reference& ref) : Shape(ref), mCenterCalculated(false) {}
     ~CompositeShape();

     void addShape(Shape *s);

     /**
      * \brief Gets the number of Shapes in this CompositeShape.
      *
      * \return The number of Shapes in this CompositeShape.
      */
     int parts() const { return mShapes.size(); }

     void toProj(const Projection &proj);
     void toCoord(const Projection &proj);
     void cells(const BasicGrid &g, std::list<GridPos> &outCells) const;
     LatLng cenCoord() const;

     /**
      * \brief Returns the center coordinate in projection space of
      * this Shape.
      *
      * \return The center coordinate of this Shape.
      */
     ProjCoord cenProj() const { return cenCoord().toCoord(); }
     void boundingBox(double &t, double &l, double &b, double &r) const;

     /**
      * \brief Returns the area of this Shape. Not yet implemented for
      * CompositeShape.
      *
      * \return The area of this Shape.
      */
     double area() const { Error e("CompositeShape::area() is not yet implemented"); throw e; return 0; }

     void move(double dx, double dy);
     void move(LatLng newPos);
     Shape *clone() const;

     /**
      * \brief Returns the stratmas protocol type of this shape.
      *
      * \return The stratmas protocol type of this shape.
      */
     const std::string type() const { return "Composite"; }

     std::ostream& toXML(std::ostream &o, std::string indent) const;
     void getFlattened(std::vector<Shape*>& shapes) const;
     const Shape* getRegionForPoint(const ProjCoord &p) const;
     Shape* getPart(const Reference& toFind) const;
};


#endif   // STRATMAS_SHAPE_H
