// $Id: Polygon.java,v 1.7 2007/01/24 14:08:55 amfi Exp $
/*
 * @(#)Polygon.java
 */

package ApproxsimClient.object;

import ApproxsimClient.object.type.Type;
import ApproxsimClient.object.type.Declaration;
import ApproxsimClient.object.type.TypeFactory;
import ApproxsimClient.object.primitive.Timestamp;
import ApproxsimClient.object.primitive.Identifier;

import ApproxsimClient.map.Projection;

import ApproxsimClient.BoundingBox;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

import org.w3c.dom.Element;

/**
 * A polygon defines a contigous set of two dimensional points. It is built by enclosing a region with lines.
 * 
 * @version 1, $Date: 2007/01/24 14:08:55 $
 * @author Daniel Ahlin
 */

public class Polygon extends Segmented {
    /**
     * Creates an identified polygon, defined by the supplied lines.
     * 
     * @param identifier the identifier of the shape.
     * @param lines the lines that makes up the polygon the shape.
     */
    protected Polygon(String identifier, Vector lines) {
        super(identifier, TypeFactory.getType("Polygon"));
        this.add(lines);
    }

    /**
     * Creates a polygon, defined by the supplied lines.
     * 
     * @param declaration the declaration of the shape.
     * @param lines the lines that makes up the polygon the shape.
     */
    protected Polygon(Declaration declaration, Vector lines) {
        this(declaration.getName(), lines);
    }

    /**
     * Constructs an approximated polygon of this shape.
     * 
     * @param error the maximal distance between the approximation and the true line.
     */
    protected Polygon constructPolygon(double error) {
        return this;
    }

    /**
     * Returns a ApproxsimVectorConstructor suitable for constructing objects of this type.
     * 
     * @param declaration the declaration for which the object is created.
     */
    protected static ApproxsimVectorConstructor getVectorConstructor(
            Declaration declaration) {
        return new PolygonVectorConstructor(declaration);
    }

    /**
     * Creates a Polygon from the specified Declaration.
     * <p>
     * author Per Alexius
     * 
     * @param declaration The declaration for which the object is created.
     */
    protected static ApproxsimObject defaultCreate(Declaration declaration) {
        throw new AssertionError("No default constructor for Polygon.");
    }

    /**
     * Creates this shapes bounding box. This method assumes that the polygon is nonempty.
     */
    public BoundingBox createBoundingBox() {
        double xmin = Double.POSITIVE_INFINITY;
        double ymin = Double.POSITIVE_INFINITY;
        double xmax = Double.NEGATIVE_INFINITY;
        double ymax = Double.NEGATIVE_INFINITY;

        for (Enumeration ls = this.getCurves(); ls.hasMoreElements();) {
            Line l = (Line) ls.nextElement();
            xmin = xmin < l.getStartPoint().getLon() ? xmin : l.getStartPoint()
                    .getLon();
            xmin = xmin < l.getEndPoint().getLon() ? xmin : l.getEndPoint()
                    .getLon();

            ymin = ymin < l.getStartPoint().getLat() ? ymin : l.getStartPoint()
                    .getLat();
            ymin = ymin < l.getEndPoint().getLat() ? ymin : l.getEndPoint()
                    .getLat();

            xmax = xmax > l.getStartPoint().getLon() ? xmax : l.getStartPoint()
                    .getLon();
            xmax = xmax > l.getEndPoint().getLon() ? xmax : l.getEndPoint()
                    .getLon();

            ymax = ymax > l.getStartPoint().getLat() ? ymax : l.getStartPoint()
                    .getLat();
            ymax = ymax > l.getEndPoint().getLat() ? ymax : l.getEndPoint()
                    .getLat();
        }

        return new BoundingBox(xmin, ymin, xmax, ymax);
    }

    /**
     * Returns the bounding box with respect to its projected parts.
     */
    public BoundingBox getBoundingBox(Projection proj) {
        double xmin = Double.POSITIVE_INFINITY;
        double ymin = Double.POSITIVE_INFINITY;
        double xmax = Double.NEGATIVE_INFINITY;
        double ymax = Double.NEGATIVE_INFINITY;

        for (Enumeration ls = this.getCurves(); ls.hasMoreElements();) {
            Line l = (Line) ls.nextElement();
            double[] p1 = proj.projToXY(l.getStartPoint());
            double[] p2 = proj.projToXY(l.getEndPoint());

            xmin = xmin < p1[0] ? xmin : p1[0];
            xmin = xmin < p2[0] ? xmin : p2[0];

            ymin = ymin < p1[1] ? ymin : p1[1];
            ymin = ymin < p2[1] ? ymin : p2[1];

            xmax = xmax > p1[0] ? xmax : p1[0];
            xmax = xmax > p2[0] ? xmax : p2[0];

            ymax = ymax > p1[1] ? ymax : p1[1];
            ymax = ymax > p2[1] ? ymax : p2[1];
        }
        return new BoundingBox(xmin, ymin, xmax, ymax, proj);
    }

    /**
     * Moves this shape relative to its current position.
     * <p>
     * author Per Alexius
     * 
     * @param dx The distance to move given in degrees longitude.
     * @param dy The distance to move given in degrees latitude.
     */
    public void move(double dx, double dy) {
        for (Enumeration en = getCurves(); en.hasMoreElements();) {
            ((Line) en.nextElement()).move(dx, dy);
        }
    }

    /**
     * Moves this shape to the specified location.
     * <p>
     * author Per Alexius
     * 
     * @param lng The longitude of the new location.
     * @param lat The latitude of the new location.
     */
    public void moveTo(double lng, double lat) {
        BoundingBox b = createBoundingBox();
        double cenLng = b.getWestLon() + (b.getEastLon() - b.getWestLon()) / 2;
        double cenLat = b.getSouthLat() + (b.getNorthLat() - b.getSouthLat())
                / 2;
        move(lng - cenLng, lat - cenLat);
    }

    /**
     * Clones this object. Notice that the Identifier is NOT cloned. Both the clone and the original object will thus keep a reference to
     * the same Identifier object.
     * <p>
     * author Per Alexius
     * 
     * @return A clone of this object.
     */
    protected Object clone() {
        Vector elements = new Vector();
        for (Enumeration en = children(); en.hasMoreElements();) {
            elements.add(((ApproxsimObject) en.nextElement()).clone());
        }
        return new Polygon(identifier, elements);
    }

    /**
     * Updates this object with the data contained in the Element n.
     * <p>
     * author Per Alexius
     * 
     * @param n The DOM Element from which to fetch the data.
     * @param t The simulation time for which the data is valid.
     */
    public void update(Element n, Timestamp t) {
        if (getType().equals(TypeFactory.getType(n))) {
            // Get the ApproxsimList containing the child curves.
            ApproxsimList curveList = (ApproxsimList) getChild("curves");
            if (curveList == null) {
                throw new AssertionError("Polygon without curve ApproxsimList");
            }

            // Go through the child curves in the new Polygon and
            // check them with the existing child curves. If any
            // new curves are detected or if any curve has been
            // removed we replace the enture Polygon.
            Vector v = XMLHelper.getChildElementsByTag(n, "curves");
            if (v.size() == curveList.getChildCount()) {
                for (Enumeration en = v.elements(); en.hasMoreElements();) {
                    // Cast the child curve Node to an Element.
                    Element curveElem = (Element) en.nextElement();

                    // Get the Identifier of the child curve.
                    String id = Identifier.getIdentifier(curveElem);

                    // Get the Type of the child curve.
                    Type typeOfNewChild = TypeFactory.getType(curveElem);

                    // Check if we have a child curve with the same
                    // name as the new one.
                    Curve childCurve = (Curve) curveList.getChild(id);
                    if (childCurve == null
                            || !childCurve.getType().equals(typeOfNewChild)) {
                        // Same number of curves but not same
                        // identifers -> replace.
                        replace(ApproxsimObjectFactory.domCreate(n), n);
                        return;
                    } else {
                        // Ok, it was just an update of the same
                        // child curve.
                        childCurve.update(curveElem, t);
                    }
                }
            } else {
                // More or less curves than in the old polygon -> replace.
                replace(ApproxsimObjectFactory.domCreate(n), n);
                return;
            }
        } else {
            // Different type -> replace
            replace(ApproxsimObjectFactory.domCreate(n), n);
        }
    }

    /**
     * Called when a (direct) child of this has changed. Overriden in order to spare the cached approximated polygon (which is this).
     * 
     * @param child the child that changed
     */
    public void childChanged(ApproxsimObject child, Object initiator) {
        if (getParent() != null) {
            getParent().childChanged(this, initiator);
        }

        fireChildChanged(child, initiator);
    }

    /**
     * Checks if this polygon is closed, e.g. that there is no line that has a start point that isn't any other line's endpoint.
     * 
     * @return True if the polygon is closed, false otherwise.
     */
    public boolean isClosed() {
        // Should be cached and updated!!!
        Hashtable h = new Hashtable();
        for (Enumeration en = getCurves(); en.hasMoreElements();) {
            Line l = (Line) en.nextElement();
            h.put(new java.awt.geom.Point2D.Double(l.getStartPoint().getLon(),
                    l.getStartPoint().getLat()), l);
//               h.put(l.getStartPoint(), l);
        }
        for (Enumeration en = h.elements(); en.hasMoreElements();) {
            Line l = (Line) en.nextElement();
            if (h.get(new java.awt.geom.Point2D.Double(
                    l.getEndPoint().getLon(), l.getEndPoint().getLat())) == null) {
//               if (h.get(l.getEndPoint()) == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the ordered set of points for this polygonial.
     */
    public Vector getOrderedSetOfPoints() {
        Hashtable h = new Hashtable();
        for (Enumeration en = getCurves(); en.hasMoreElements();) {
            Line l = (Line) en.nextElement();
            h.put(new java.awt.geom.Point2D.Double(l.getStartPoint().getLon(),
                    l.getStartPoint().getLat()), l.getEndPoint());
        }
        Vector points = new Vector();
        // get the first point
        Point point = (Point) h.elements().nextElement();
        while (point != null && !points.contains(point)) {
            points.add(point);
            point = (Point) h.get(new java.awt.geom.Point2D.Double(point
                    .getLon(), point.getLat()));
        }
        return points;
    }

}

/*
 * @(#)PolygonVectorConstructor
 */

/**
 * PolygonVectorConstructor constructs a Polygon using a vector.
 * 
 * @version 1, $Date: 2007/01/24 14:08:55 $
 * @author Daniel Ahlin
 */
class PolygonVectorConstructor extends ApproxsimVectorConstructor {
    /**
     * Creates a new object using specifications in declaration.
     * 
     * @param declaration the declaration to use.
     */
    public PolygonVectorConstructor(Declaration declaration) {
        super(declaration);
    }

    /**
     * Returns the ApproxsimObject this component was created to provide.
     * 
     * @param parts the parts to use in constructing the object.
     */
    public ApproxsimObject getApproxsimObject(Vector parts) {
        return new Polygon(this.getDeclaration(), parts);
    }
}
