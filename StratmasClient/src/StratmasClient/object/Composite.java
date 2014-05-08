// $Id: Composite.java,v 1.4 2006/05/11 16:43:03 alexius Exp $
/*
 * @(#)Composite.java
 */

package ApproxsimClient.object;

import java.util.Vector;
import java.util.Enumeration;
import java.util.HashSet;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import ApproxsimClient.BoundingBox;

import ApproxsimClient.object.type.Declaration;
import ApproxsimClient.object.type.Type;
import ApproxsimClient.object.type.TypeFactory;
import ApproxsimClient.map.Projection;
import ApproxsimClient.object.primitive.Timestamp;
import ApproxsimClient.object.primitive.Identifier;

/**
 * A composite defines a set of two dimensional shapes.
 * 
 * @version 1, $Date: 2006/05/11 16:43:03 $
 * @author Daniel Ahlin
 */

public class Composite extends Shape {
    /**
     * Creates an identified composite, defined by the supplied shapes.
     * 
     * @param identifier the identifier of the shape.
     * @param shapes the subshapes of the shape.
     */
    protected Composite(String identifier, Vector<ApproxsimObject> shapes) {
        super(identifier, TypeFactory.getType("Composite"));
        this.add(shapes);
    }

    /**
     * Creates a polygon, defined by the supplied lines.
     * 
     * @param declaration the declaration of the shape.
     * @param lines the lines that makes up the polygon the shape.
     */
    protected Composite(Declaration declaration, Vector lines) {
        this(declaration.getName(), lines);
    }

    /**
     * Returns the parts of this Composite.
     */
    public Enumeration getParts() {
        // We know that Composite is expected to contain a
        // ApproxsimList as its only direct child.
        ApproxsimList shapes = (ApproxsimList) this.getChild("shapes");
        return shapes.children();
    }

    /**
     * Reduces this Shape and adds it to supplied Vector.
     * 
     * @param res vector to add result to.
     */
    public Vector<SimpleShape> constructSimpleShapes(Vector<SimpleShape> res) {
        for (Enumeration ss = this.getParts(); ss.hasMoreElements();) {
            Shape s = (Shape) ss.nextElement();
            s.constructSimpleShapes(res);
        }
        return res;
    }

    /**
     * Returns a ApproxsimVectorConstructor suitable for constructing objects of this type.
     * 
     * @param declaration the declaration for which the object is created.
     */
    protected static ApproxsimVectorConstructor getVectorConstructor(
            Declaration declaration) {
        return new CompositeVectorConstructor(declaration);
    }

    /**
     * Creates a Composite from the specified Declaration.
     * <p>
     * author Per Alexius
     * 
     * @param declaration The declaration for which the object is created.
     */
    protected static ApproxsimObject defaultCreate(Declaration declaration) {
        throw new AssertionError("No default constructor for Composite.");
    }

    /**
     * Creates this shapes bounding box. This method assumes that the Composite is nonempty.
     */
    public BoundingBox createBoundingBox() {
        double xmin = Double.POSITIVE_INFINITY;
        double ymin = Double.POSITIVE_INFINITY;
        double xmax = Double.NEGATIVE_INFINITY;
        double ymax = Double.NEGATIVE_INFINITY;

        BoundingBox res = new BoundingBox(xmin, ymin, xmax, ymax);

        for (Enumeration ss = this.getParts(); ss.hasMoreElements();) {
            Shape s = (Shape) ss.nextElement();
            res.combine(s.getBoundingBox());
        }

        return res;
    }

    /**
     * Returns the bounding box with respect to its projected parts. This method assumes that the Composite is nonempty.
     */
    public BoundingBox getBoundingBox(Projection proj) {
        Enumeration ss = this.getParts();
        BoundingBox res = ((Shape) ss.nextElement()).getBoundingBox(proj);
        for (; ss.hasMoreElements();) {
            Shape s = (Shape) ss.nextElement();
            res = BoundingBox.combine(res, s.getBoundingBox(proj), proj);
        }

        return res;
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
        for (Enumeration en = getParts(); en.hasMoreElements();) {
            ((Shape) en.nextElement()).move(dx, dy);
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
        Vector<ApproxsimObject> elements = new Vector<ApproxsimObject>();
        for (Enumeration en = children(); en.hasMoreElements();) {
            elements.add((ApproxsimObject) ((ApproxsimObject) en.nextElement())
                    .clone());
        }
        return new Composite(identifier, elements);
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
            // Get the ApproxsimList containing the child shapes.
            ApproxsimList shapeList = (ApproxsimList) getChild("shapes");
            if (shapeList == null) {
                throw new AssertionError("Composite without shape ApproxsimList");
            }

            HashSet<String> newIDs = new HashSet<String>();
            // Go through the child shapes in the new Composite and
            // check them with the existing child shapes.
            for (Node child = n.getFirstChild(); child != null; child = child
                    .getNextSibling()) {
                if (child.getNodeType() == Node.ELEMENT_NODE
                        && child.getNodeName().equals("shapes")) {
                    // Cast the child shape Node to an Element.
                    Element shapeElem = (Element) child;

                    // Get the Identifier of the child shape.
                    String id = Identifier.getIdentifier(shapeElem);
                    newIDs.add(id);

                    // Get the Type of the child shape.
                    Type typeOfNewChild = TypeFactory.getType(shapeElem);

                    // Check if we have a child shape with the same
                    // name as the new one.
                    Shape childShape = (Shape) shapeList.getChild(id);
                    if (childShape == null) {
                        // If not, we add the new child shape.
                        shapeList.add(ApproxsimObjectFactory
                                .domCreate(shapeElem));
                    } else if (!childShape.getType().equals(typeOfNewChild)) {
                        // If we do, but it has a different Type than
                        // the new one, replace it with the new one.
                        shapeList.remove(childShape);
                        shapeList.add(ApproxsimObjectFactory
                                .domCreate(shapeElem));
                    } else {
                        // Ok, it was just an update of the same
                        // child shape.
                        childShape.update(shapeElem, t);
                    }
                }
            }

            // Remove all of our current child shapes that wasn't
            // mentioned in the update.
            for (Enumeration en = shapeList.children(); en.hasMoreElements();) {
                ApproxsimObject o = (ApproxsimObject) en.nextElement();
                if (newIDs.contains(o.getIdentifier())) {
                    shapeList.remove(o);
                }
            }
        } else {
            replace(ApproxsimObjectFactory.domCreate(n), n);
        }
    }

    /**
     * Checks if this Composite contains any Polygons that are not closed.
     * 
     * @return True if there are at least one polygon in this Composite that is not closed, false otherwise.
     */
    public boolean hasUnclosed() {
        for (Enumeration en = getParts(); en.hasMoreElements();) {
            Shape s = (Shape) en.nextElement();
            if (s instanceof Polygon && !((Polygon) s).isClosed()) {
                return true;
            } else if (s instanceof Composite && ((Composite) s).hasUnclosed()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a Shape from a DOM element.
     * <p>
     * author Per Alexius
     * 
     * @param n The dom element from which the object is created.
     */
    protected static ApproxsimObject domCreate(Element n) {
        Vector<ApproxsimObject> shapes = new Vector<ApproxsimObject>();
        for (Node child = n.getFirstChild(); child != null; child = child
                .getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE
                    && child.getNodeName().equals("shapes")) {
                shapes.add(ApproxsimObjectFactory.domCreate((Element) child));
            }
        }
        ApproxsimList list = new ApproxsimList(new Declaration(
                TypeFactory.getType("Shape"), "shapes", 1, -1, true), shapes);
        Vector<ApproxsimObject> listVec = new Vector<ApproxsimObject>();
        listVec.add(list);
        return new Composite(Identifier.getIdentifier(n), listVec);
    }

}

/*
 * @(#)CompositeVectorConstructor
 */

/**
 * CompositeVectorConstructor constructs a composite using a vector.
 * 
 * @version 1, $Date: 2006/05/11 16:43:03 $
 * @author Daniel Ahlin
 */
class CompositeVectorConstructor extends ApproxsimVectorConstructor {
    /**
     * Creates a new object using specifications in declaration.
     * 
     * @param declaration the declaration to use.
     */
    public CompositeVectorConstructor(Declaration declaration) {
        super(declaration);
    }

    /**
     * Returns the ApproxsimObject this component was created to provide.
     * 
     * @param parts the parts to use in constructing the object.
     */
    public ApproxsimObject getApproxsimObject(Vector parts) {
        return new Composite(this.getDeclaration(), parts);
    }
}
