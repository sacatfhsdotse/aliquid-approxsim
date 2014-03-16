// System
#include <cmath>

//Own
#include "BasicGrid.h"
#include "debugheader.h"
#include "random2.h"
#include "Reference.h"
#include "Shape.h"
#include "XMLHelper.h"

// Temporary
#include <fstream>


using namespace std;


/// Default constructor.
Shape::Shape() : mProjected(false), mChanges(PrivateRandom::privateRandomUniform())
{
}

/**
 * \brief Creates a Shape with the specified Reference.
 *
 * \param ref The Reference this Shape should be given.
 */
Shape::Shape(const Reference& ref)
     : Referencable(ref), mProjected(false), mChanges(PrivateRandom::privateRandomUniform())
{
}

/**
 * \brief Writes an XML representation of the cells covered by this
 * shape to the provided stream.
 *
 * This method is used when telling the client which cells a certain
 * region covers.
 *
 * \param grid A Reference to the Grid.
 * \param swapEndian Indicates if we have to change byte order in the
 * produced data.
 * \param o The stream to write to.
 * \return The stream with the xml representation written to it.
 */
ostream& Shape::cellsToXML(const BasicGrid& grid, bool swapEndian, ostream &o) const
{
     list<GridPos> coveredCells;
     cells(grid, coveredCells);
     int32_t* index = new int32_t[coveredCells.size()];
     int i = 0;
     for (list<GridPos>::iterator it = coveredCells.begin(); it != coveredCells.end(); it++) {
          index[i] = grid.posToActive(it->r, it->c);
          i++;
     }

     o << "<reference>" << endl;
     ref().toXML(o) << endl;
     o << "</reference>" << endl;
     o << "<cells>";
     XMLHelper::base64Print(index, coveredCells.size(), swapEndian, o);
     o << "</cells>" << endl;

     delete [] index;
     return o;
}

/**
 * \brief Creates a Circle with no Reference.
 *
 * \param l The center position of the Circle.
 * \param r The radius of the Circle.
 */
Circle::Circle(const LatLng l, double r) : Shape(Reference::nullRef()), mCenter(l), mRadius(r)
{
}

/**
 * \brief Returns a list containing pointers to all cells covered by
 * this Shape.
 *
 * \param g A reference to the Grid.
 * \param outCells A list that on return contains pointers to all
 * cells covered by this Shape.
 */
void Circle::cells(const BasicGrid& g, std::list<GridPos>& outCells) const
{
     g.cells(*this, outCells);
}


/**
 * \brief Gets the bounding box of this Shape
 *
 * \param t Top coordinate of this Shape's boundingbox.
 * \param l Left coordinate of this Shape's boundingbox.
 * \param b Bottom coordinate of this Shape's boundingbox.
 * \param r Right coordinate of this Shape's boundingbox.
 */
void Circle::boundingBox(double &t, double &l, double &b, double &r) const
{
     t = cenCoord().lat() + radius() / kMetersPerDegreeLat;
     b = cenCoord().lat() - radius() / kMetersPerDegreeLat;
     l = cenCoord().lng() - radius() / kMetersPerDegreeLat / cos(cenCoord().lat() * kDeg2Rad);;
     r = cenCoord().lng() + radius() / kMetersPerDegreeLat / cos(cenCoord().lat() * kDeg2Rad);;

     double junk;
     if (mProjected) {
          Projection::currentProjection()->coordToProj(cenCoord().lng(), t, junk, t);
          Projection::currentProjection()->coordToProj(cenCoord().lng(), b, junk, b);
          Projection::currentProjection()->coordToProj(l, cenCoord().lat(), l, junk);
          Projection::currentProjection()->coordToProj(r, cenCoord().lat(), r, junk);
     }
}

ostream &Circle::toXML(ostream &o, std::string indent) const
{
     o << indent << "<radius xsi:type=\"sp:Double\"><value>" << mRadius << "</value></radius>" << endl;
     o << indent << "<center xsi:type=\"sp:Point\">" << endl;
     o << indent + INDENT << "<lat>" << mCenter.lat() << "</lat><lon>" << mCenter.lng() << "</lon>" << endl;
     o << indent << "</center>" << endl;
     return o;
}
std::ostream &operator << (std::ostream &o, const Circle &c)
{
     return o << "Circle with radius: " << c.mRadius << ", center (lat, lng): "
              << c.mCenter.lat() << ", " << c.mCenter.lng();
}

/**
 * Default constructor.
 */
Polygon::Polygon()
{
     mBoundary.num_contours = 0;
     mBoundary.hole = NULL;
     mBoundary.contour = NULL;
}

/**
 * Creates a Polygon with the specified Reference.
 *
 * \param ref The Reference to this Polygon.
 */
Polygon::Polygon(const Reference& ref) : Shape(ref)
{
     mBoundary.num_contours = 0;
     mBoundary.hole = NULL;
     mBoundary.contour = NULL;
}

/**
 * \brief Constructor that creates a Polygon based on a gpc_polygon
 *
 * Assumes that p is given in lat lng.
 *
 * \param p The gpc_polygon to build this Polygon from.
 * \param lineId A vector containing the id:s of the lines that
 * constitutes this Polygon. Ordered in the same order as the points
 * in the provided gpc_polygon.
 * \param ref The Reference to this Polygon.
 */
Polygon::Polygon(const gpc_polygon &p, std::list<std::string> lineId, const Reference& ref)
     : Shape(ref), mLineId(lineId)
{
     deepCopyGpcPolygon(mBoundary, p);

     // Find the 'center' of this polygon e.g. calculate
     // the center of the bounding box
     double t, l, b, r;
     boundingBox(t, l, b, r);
     mCenter = LatLng(b + (t - b) / 2, l + (r - l) / 2);
}

/**
 * \brief Creates a rectangle with the specified center, angle, width
 * and height.
 *
 * \param cen The center of the rectangle.
 * \param ang The angle in which the rectangle leans.
 * \param w The width of the rectangle in meters.
 * \param h the height of the rectangle in meters.
 * \param r The reference to the shape to be created.
 */
Polygon::Polygon(LatLng cen, double ang, double w, double h, const Reference& r) : Shape(r)
{
     mCenter                = cen;
     mBoundary.num_contours = 0;
     mBoundary.hole         = 0;
     mBoundary.contour      = 0;
     mLineId.push_back("0");
     mLineId.push_back("1");
     mLineId.push_back("2");
     mLineId.push_back("3");

     gpc_vertex_list *aList = new gpc_vertex_list;
     aList->num_vertices = 4;
     aList->vertex = new gpc_vertex[4];

     double a = atan2(h, w);
     double y1 = w * sin(ang - a);
     double x1 = w * cos(ang - a);
     double y2 = w * sin(ang + a);
     double x2 = w * cos(ang + a);

     aList->vertex[0].y = cen.lat() + y1 * kDegreesLatPerMeter;
     aList->vertex[0].x = cen.lng() + x1 * kDegreesLatPerMeter * cos(aList->vertex[0].y);
     aList->vertex[1].y = cen.lat() + y2 * kDegreesLatPerMeter;
     aList->vertex[1].x = cen.lng() + x2 * kDegreesLatPerMeter * cos(aList->vertex[1].y);
     aList->vertex[2].y = cen.lat() - y1 * kDegreesLatPerMeter;
     aList->vertex[2].x = cen.lng() - x1 * kDegreesLatPerMeter * cos(aList->vertex[2].y);
     aList->vertex[3].y = cen.lat() - y2 * kDegreesLatPerMeter;
     aList->vertex[3].x = cen.lng() - x2 * kDegreesLatPerMeter * cos(aList->vertex[3].y);

     gpc_add_contour(&mBoundary, aList, false);
}

/**
 * \brief Copy constructor.
 *
 * \param p The Polygon to copy.
 */
Polygon::Polygon(const Polygon &p) : Shape(p.ref())
{
     mChanges    = p.mChanges;
     mCenter     = p.mCenter;
     mProjected  = p.mProjected;
     mLineId     = p.mLineId;
     deepCopyGpcPolygon(mBoundary, p.mBoundary);
}

/**
 * \brief Projects this Shape using the specified Projection.
 *
 * \param proj The projection to use.
 */
void Polygon::toProj(const Projection &proj)
{
     if (!mProjected) {
          proj.coordToProj(mBoundary, mBoundary);
          mProjected = true;
     }
}

/**
 * \brief Transforms this Shape to lat lng coordinate using the
 * provided projection.
 *
 * \param proj The projection to use.
 */
void Polygon::toCoord(const Projection &proj)
{
     if (mProjected) {
          proj.projToCoord(mBoundary, mBoundary);
          mProjected = false;
     }
}

/**
 * \brief Returns a list containing pointers to all cells covered by
 * this Shape.
 *
 * \param g A reference to the Grid.
 * \param outCells A list that on return contains pointers to all
 * cells covered by this Shape.
 */
void Polygon::cells(const BasicGrid &g, std::list<GridPos> &outCells) const
{
     g.cells(*this, outCells);
}

/**
 * \brief Gets the bounding box of this Shape
 *
 * \param t Top coordinate of this Shape's boundingbox.
 * \param l Left coordinate of this Shape's boundingbox.
 * \param b Bottom coordinate of this Shape's boundingbox.
 * \param r Right coordinate of this Shape's boundingbox.
 */
void Polygon::boundingBox(double &t, double &l, double &b, double &r) const 
{
     double minx, maxx, miny, maxy;
     minx = mBoundary.contour[0].vertex[0].x;
     miny = mBoundary.contour[0].vertex[0].y;
     maxx = minx;
     maxy = miny;
     for (int i = 0; i < mBoundary.num_contours; i++) {
          for (int j = 0; j < mBoundary.contour[i].num_vertices; j++) {
               minx = min(minx, mBoundary.contour[i].vertex[j].x);
               miny = min(miny, mBoundary.contour[i].vertex[j].y);
               maxx = max(maxx, mBoundary.contour[i].vertex[j].x);
               maxy = max(maxy, mBoundary.contour[i].vertex[j].y);
          }
     }
     t = maxy;
     l = minx;
     b = miny;
     r = maxx;
}

/**
 * \brief Moves this Shape relative to itself.
 *
 * \param dx The movement in x-direction in degrees longitude
 * \param dy The movement in y-direction in degrees latitude
 */
void Polygon::move(double dx, double dy)
{
     Shape::toCoord();

     for (int i = 0; i < mBoundary.num_contours; i++) {
          for (int j = 0; j < mBoundary.contour[i].num_vertices; j++) {
               mBoundary.contour[i].vertex[j].x += dx;
               mBoundary.contour[i].vertex[j].y += dy;
          }
     }

     // Find the 'center' of this polygon e.g. calculate
     // the center of the bounding box
     double t, l, b, r;
     boundingBox(t, l, b, r);
     mCenter = LatLng(b + (t - b) / 2, l + (r - l) / 2);
     mChanges++;
}

/**
 * \brief Moves this Shape to a new position.
 *
 * \param newPos The position to move to.
 */
void Polygon::move(LatLng newPos)
{
     move(newPos.lng() - mCenter.lng(), newPos.lat() - mCenter.lat());
}

ostream &Polygon::toXML(ostream &o, string indent) const
{
     double lat1;
     double lng1;
     double lat2;
     double lng2;
     for (int i = 0; i < mBoundary.num_contours; i++) {
          int numVert = mBoundary.contour[i].num_vertices;
          if (numVert > 0) {
               if (!mProjected) {
                    lng1 = mBoundary.contour[i].vertex[0].x;
                    lat1 = mBoundary.contour[i].vertex[0].y;
               }
               else {
                    Projection::mCurrent->projToCoord(mBoundary.contour[i].vertex[0].x, 
                                                      mBoundary.contour[i].vertex[0].y, lng1, lat1);
               }
          }
          list<string>::const_iterator it = mLineId.begin();
          for (int j = 1; j < numVert + 1; j++) {
               int jj = j % numVert;
               if (!mProjected) {
                    lng2 = mBoundary.contour[i].vertex[jj].x;
                    lat2 = mBoundary.contour[i].vertex[jj].y;
               }
               else {
                    Projection::mCurrent->projToCoord(mBoundary.contour[i].vertex[jj].x, 
                                                      mBoundary.contour[i].vertex[jj].y, lng2, lat2);
               }
               o << indent << "<curves xsi:type=\"sp:Line\" identifier=\"" << *it << "\">" << endl;
               o << indent + INDENT << "<p1 xsi:type=\"sp:Point\"><lat>" << lat1 << "</lat><lon>" << lng1 << "</lon></p1>" << endl;
               o << indent + INDENT << "<p2 xsi:type=\"sp:Point\"><lat>" << lat2 << "</lat><lon>" << lng2 << "</lon></p2>" << endl;
               o << indent << "</curves>" << endl;
               lat1 = lat2;
               lng1 = lng2;
               it++;
          }
     }
     return o;
}

/**
 * \brief Deallocates the memory used by a gpc_polygon.
 *
 * Notice that the gpc_polygon struct won't be deallocated itself,
 * but only it's contents. This leaves the possibility to deallocate
 * partly statically allocated gpc_polygons.
 *
 * \param p The gpc_polygon to be deallocated
 */
void Polygon::deallocGpcPolygon(gpc_polygon &p)
{
     for (int i = 0; i < p.num_contours; i++) {
          if (p.contour[i].vertex) {
               delete [] p.contour[i].vertex;
               p.contour[i].vertex = 0;
          }
     }
     p.num_contours = 0;
     if (p.hole   ) { delete [] p.hole   ; p.hole    = 0; }
     if (p.contour) { delete [] p.contour; p.contour = 0; }
}

/**
 * \brief Performs a deep copy of a gpc_polygon
 *
 * \param dst The destination gpc_polygon
 * \param src The source gpc_polygon
 */
void Polygon::deepCopyGpcPolygon(gpc_polygon &dst, const gpc_polygon &src)
{
     dst.num_contours = src.num_contours;
     dst.hole         = new int[src.num_contours];
     dst.contour      = new gpc_vertex_list[src.num_contours];
     memcpy(dst.hole, src.hole, src.num_contours * sizeof(int));
     memcpy(dst.contour, src.contour, src.num_contours * sizeof(gpc_vertex_list));
     for (int i = 0; i < src.num_contours; i++) {
          dst.contour[i].vertex = new gpc_vertex[src.contour[i].num_vertices];
          memcpy(dst.contour[i].vertex, src.contour[i].vertex, src.contour[i].num_vertices * sizeof(gpc_vertex));
     }
}

std::ostream &operator << (std::ostream &o, const Polygon &p)
{
     for (int i = 0; i < p.mBoundary.num_contours; i++) {
          o << "Part " << i << std::endl;
          for (int j = 0; j < p.mBoundary.contour[i].num_vertices; j++) {
               o << p.mBoundary.contour[i].vertex[j].x << ", " << p.mBoundary.contour[i].vertex[j].y << "    ";
          }
          o << std::endl;
     }
     return o;
}

/**
 * Destructor.
 */
CompositeShape::~CompositeShape()
{
     for (map<string, Shape*>::iterator it = mShapes.begin(); it != mShapes.end(); it++) {
          delete it->second;
     }
}

/**
 * \brief Adds a Shape to this CompositeShape
 *
 * \param s The Shape to add.
 */
void CompositeShape::addShape(Shape *s)
{
     mShapes[s->ref().name()] = s;
     mCenterCalculated = false;
     mChanges++;
}

/**
 * \brief Projects this Shape using the specified Projection.
 *
 * \param proj The projection to use.
 */
void CompositeShape::toProj(const Projection &proj)
{
     if (!mProjected) {
          for (map<string, Shape*>::iterator it = mShapes.begin(); it != mShapes.end(); it++) {
               it->second->toProj(proj);
          }
          mProjected = true;
     }
}

/**
 * \brief Transforms this Shape to lat lng coordinate using the
 * provided projection.
 *
 * \param proj The projection to use.
 */
void CompositeShape::toCoord(const Projection &proj)
{
     if (mProjected) {
          for (map<string, Shape*>::iterator it = mShapes.begin(); it != mShapes.end(); it++) {
               it->second->toCoord(proj);
          }
          mProjected = false;
     }
}

/**
 * \brief Returns a list containing pointers to all cells covered by
 * this Shape.
 *
 * \param g A reference to the Grid.
 * \param outCells A list that on return contains pointers to all
 * cells covered by this Shape.
 */
void CompositeShape::cells(const BasicGrid& g, std::list<GridPos>& outCells) const
{
     list<GridPos> tmp;
     for (map<string, Shape*>::const_iterator it = mShapes.begin(); it != mShapes.end(); it++) {
          it->second->cells(g, tmp);
          outCells.insert(outCells.end(), tmp.begin(), tmp.end());
          tmp.clear();
     }
     outCells.sort();
     outCells.unique();
}

/**
 * \brief Returns the center coordinate in lat lng of this Shape.
 *
 * \return The center coordinate of this Shape.
 */
LatLng CompositeShape::cenCoord() const
{
     if (!mCenterCalculated) {
          double tt, ll, bb, rr;
          boundingBox(tt, ll, bb, rr);   // Sets center...
     }
     return mCenter;
}

/**
 * \brief Gets the bounding box of this Shape
 *
 * \param t Top coordinate of this Shape's boundingbox.
 * \param l Left coordinate of this Shape's boundingbox.
 * \param b Bottom coordinate of this Shape's boundingbox.
 * \param r Right coordinate of this Shape's boundingbox.
 */
void CompositeShape::boundingBox(double &t, double &l, double &b, double &r) const
{
     if (!mShapes.empty()) {
          double tt, ll, bb, rr;
          mShapes.begin()->second->boundingBox(t, l, b, r);
          for (map<string, Shape*>::const_iterator it = ++mShapes.begin(); it != mShapes.end(); it++) {
               it->second->boundingBox(tt, ll, bb, rr);
               t = max(t, tt);
               l = min(l, ll);
               b = min(b, bb);
               r = max(r, rr);
          }     
          double cenx = l + (r - l) / 2;
          double ceny = b + (t - b) / 2;
          if (!mCenterCalculated) {
               mCenter = (mProjected ? ProjCoord(cenx, ceny).toLatLng() : LatLng(ceny, cenx));
               mCenterCalculated = true;
          }
     }
}

/**
 * \brief Moves this Shape relative to itself.
 *
 * \param dx The movement in x-direction in degrees longitude
 * \param dy The movement in y-direction in degrees latitude
 */
void CompositeShape::move(double dx, double dy)
{
     for (map<string, Shape*>::const_iterator it = mShapes.begin(); it != mShapes.end(); it++) {
          it->second->move(dx, dy);
     }     
     mChanges++;
     mCenterCalculated = false;
}

/**
 * \brief Moves this Shape to a new position.
 *
 * \param newPos The position to move to.
 */
void CompositeShape::move(LatLng newPos)
{
     move(newPos.lng() - cenCoord().lng(), newPos.lat() - cenCoord().lat());
}

/**
 * \brief Creates a deep copy of this Shape
 *
 * \return A newly allocated copy of this Shape.
 */
Shape *CompositeShape::clone() const
{
     CompositeShape *res = new CompositeShape(ref());
     res->mProjected        = mProjected;
     res->mCenterCalculated = mCenterCalculated;
     res->mChanges          = mChanges;
     for (map<string, Shape*>::const_iterator it = mShapes.begin(); it != mShapes.end(); it++) {
          res->addShape(it->second->clone());
     }     
     return res;
}

ostream &CompositeShape::toXML(ostream &o, string indent) const
{
     for (map<string, Shape*>::const_iterator it = mShapes.begin(); it != mShapes.end(); it++) {
          o << indent << "<shapes xsi:type=\"sp:" << it->second->type() << "\" identifier=\"" << it->second->ref().name() << "\">" << endl;
          it->second->toXML(o, indent + INDENT);
          o << indent << "</shapes>" << endl;
     }     
     return o;
}

/**
 * \brief Gets a vector containing all Shapes this CompositeShape and
 * its subshapes contains.
 *
 * \return A vector containing pointers to all Shapes this
 * CompositeShape and its subshapes contains.
 */
void CompositeShape::getFlattened(std::vector<Shape*>& shapes) const
{
     std::map<string, Shape*>::const_iterator it;
     for (it = mShapes.begin(); it != mShapes.end(); it++) {
          Shape* shape = it->second;
          if (shape->type() == "Composite") {
               dynamic_cast<CompositeShape*>(shape)->getFlattened(shapes);
          }
          else {
               shapes.push_back(shape);
          }
     }     
}

/**
 * \brief Finds out in which subshape the specified point is located.
 *
 * \param p The point.
 * \return The first found subschape that contains the point, or null
 * if no such subshape could be found.
 */
const Shape *CompositeShape::getRegionForPoint(const ProjCoord &p) const {
     double x = p.x();
     double y = p.y();

     int holes[1] = {0};
     gpc_vertex pts[3]  = {{x, y}, {x, y - 1e-10}, {x - 1e-10, y}};
     gpc_vertex_list list[1] = {{3, pts}};
     gpc_polygon notSoSingularPoint = {1, holes, list};
     
     std::map<string, Shape*>::const_iterator it;
     for (it = mShapes.begin(); it != mShapes.end(); it++) {
          string type(it->second->type());
          if (type == "Polygon") {
               Polygon *poly = dynamic_cast<Polygon*>(it->second);
               gpc_polygon result;
               gpc_polygon_clip(GPC_INT, const_cast<gpc_polygon*>(&poly->boundary()), &notSoSingularPoint, &result);     
               if (result.num_contours != 0) {
                    return poly;
               }
          }
          else if (type == "Composite") {
               CompositeShape *comp = dynamic_cast<CompositeShape*>(it->second);
               const Shape *ret = comp->getRegionForPoint(p);
               if (ret) {
                    return comp;
               }
          }
     }
     return 0;
}

/**
 * \brief Tries to find a shape with the given reference in this
 * CompositeShape.
 *
 * \param toFind The reference to the shape to look for.
 * \return The found shape if successful, 0 otherwise.
 */
Shape* CompositeShape::getPart(const Reference& toFind) const
{
     Shape* candidate = 0;
     vector<const Reference*> v;
     const Reference* r = &toFind;

     // Store the 'difference' between this shape's reference and the
     // given reference in a vector.
     for(; r != 0 && r->scope() != &ref(); r = r->scope()) {
          v.push_back(r);
     }

     // 'r' != 0 if the given reference points out a descendant of this shape.
     if (r) {
          const CompositeShape* comp = this;
          for(vector<const Reference*>::reverse_iterator it = v.rbegin(); it != v.rend(); ++it) {
               if ((*it)->name() == "shapes") {
                    continue;
               }
               // If we have a composite shape...
               if (comp) {
                    // Try to find a child with the correct name.
                    std::map<string, Shape*>::const_iterator mit = comp->mShapes.find((*it)->name());
                    if (mit != mShapes.end()) {
                         candidate = mit->second;
                         // Continue if we can...
                         comp = dynamic_cast<const CompositeShape*>(candidate);
                    }
                    else {
                         // Can't find child so let's abort.
                         candidate = 0;
                         break;
                    }
               }
               else {
                    // Haven't reached the end of the reference trace
                    // but we don't have a composit so let's abort.
                    candidate = 0;
                    break;
               }
          }
     }
     return candidate;
}
