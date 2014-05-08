// $Id: Line.java,v 1.4 2006/04/10 09:45:55 dah Exp $
/*
 * @(#)Line.java
 */

package ApproxsimClient.object;

import java.util.Vector;
import java.util.Enumeration;

import java.util.NoSuchElementException;

import ApproxsimClient.object.type.Type;
import ApproxsimClient.object.type.Declaration;
import ApproxsimClient.object.type.TypeFactory;
import ApproxsimClient.object.primitive.Timestamp;
import ApproxsimClient.object.primitive.Identifier;
import ApproxsimClient.IconFactory;
import ApproxsimClient.Icon;
import ApproxsimClient.map.Projection;

import org.w3c.dom.Element;

/**
 * Represents an ordinary (straight) line.
 * 
 * @version 1, $Date: 2006/04/10 09:45:55 $
 * @author Daniel Ahlin
 */
public class Line extends Curve {
    /**
     * The start-point of the line.
     */
    Point start;

    /**
     * The end-point of the line.
     */
    Point end;

    /**
     * The type of this object
     */
    static Type type = TypeFactory.getType("Line");

    /**
     * The icon of this object.
     */
    static Icon icon = IconFactory.useTypeMapping(TypeFactory.getType("Point"));

    /**
     * Creates a new Line.
     * 
     * @param identifier the identifier for the object.
     * @param start the start-point of the line.
     * @param end the end-point of the line.
     */
    protected Line(String identifier, Point start, Point end) {
        super(identifier);
        add(start);
        add(end);
    }

    /**
     * Creates a new Line from a Declaration.
     * 
     * @param declaration the declaration for this object.
     * @param start the start-point of the line.
     * @param end the end-point of the line.
     */
    protected Line(Declaration declaration, Point start, Point end) {
        this(declaration.getName(), start, end);
    }

    /**
     * Constructs an approximated line of this line.
     * 
     * @param error the maximum difference between the true and approximated line.
     */
    public Vector getLineApproximation(double error) {
        Vector res = new Vector();
        res.add(this);
        return res;
    }

    /**
     * Returns the startPoint of this line.
     */
    public Point getStartPoint() {
        return start;
    }

    /**
     * Returns the endPoint of this line.
     */
    public Point getEndPoint() {
        return end;
    }

    /**
     * Moves this line relative to its current position.
     * <p>
     * author Per Alexius
     * 
     * @param dx The distance to move given in degrees longitude.
     * @param dy The distance to move given in degrees latitude.
     */
    public void move(double dx, double dy) {
        start.move(dx, dy);
        end.move(dx, dy);
    }

    /**
     * Returns a ApproxsimVectorConstructor suitable for constructing objects of this type.
     * 
     * @param declaration the declaration for which the object is created.
     */
    protected static ApproxsimVectorConstructor getVectorConstructor(
            Declaration declaration) {
        return new LineVectorConstructor(declaration);
    }

    /**
     * Creates a Line from the specified Declaration.
     * 
     * @param declaration The declaration for which the object is created.
     */
    protected static ApproxsimObject defaultCreate(Declaration declaration) {
        Vector newParts = new Vector();
        for (java.util.Iterator it = declaration.getType().getSubElements()
                .iterator(); it.hasNext();) {
            Declaration dec = (Declaration) it.next();
            if (dec.isSingular()) {
                newParts.add(ApproxsimObjectFactory.defaultCreate(dec));
            }
        }

        return getVectorConstructor(declaration).getApproxsimObject(newParts);
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
        return new Line(identifier, (Point) getStartPoint().clone(),
                (Point) getEndPoint().clone());
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
        start.update(XMLHelper.getFirstChildByTag(n, "p1"), t);
        end.update(XMLHelper.getFirstChildByTag(n, "p2"), t);
    }

    /**
     * Creates a Line from the element n.
     * 
     * @param n The Element to get the object from.
     * @return The newly created Line.
     */
    protected static ApproxsimObject domCreate(Element n) {
        return new Line(Identifier.getIdentifier(n),
                (Point) ApproxsimObjectFactory.domCreate(XMLHelper
                        .getFirstChildByTag(n, "p1")),
                (Point) ApproxsimObjectFactory.domCreate(XMLHelper
                        .getFirstChildByTag(n, "p2")));
    }

    /**
     * Creates an XML representation of the body of this object.
     * <p>
     * author Per Alexius
     * 
     * @param b The StringBuffer to write to.
     * @return The StringBuffer b with an XML representation of this object's body appended to it.
     */
    public StringBuffer bodyXML(StringBuffer b) {
        start.toXML(b);
        end.toXML(b);
        return b;
    }

    /**
     * Checks if this line intersects another line. Both lines are projected with the actual projection before the test is performed.
     * 
     * @param line the line to be tested.
     * @param projection the actual projection.
     * @return true if the lines intersect, false otherwise.
     */
    public boolean intersects(Line line, Projection projection) {
        // first line
        double[] xy1 = projection.projToXY(getStartPoint());
        double x1 = xy1[0];
        double y1 = xy1[1];
        double[] xy2 = projection.projToXY(getEndPoint());
        double x2 = xy2[0];
        double y2 = xy2[1];
        // second line
        double[] uv1 = projection.projToXY(line.getStartPoint());
        double u1 = uv1[0];
        double v1 = uv1[1];
        double[] uv2 = projection.projToXY(line.getEndPoint());
        double u2 = uv2[0];
        double v2 = uv2[1];

        double b1 = (y2 - y1) / (x2 - x1);
        double b2 = (v2 - v1) / (u2 - u1);
        double a1 = y1 - b1 * x1;
        double a2 = v1 - b2 * u1;

        double xi = -(a1 - a2) / (b1 - b2);
        double yi = a1 + b1 * xi;

        if ((x1 - xi) * (xi - x2) >= 0 && (u1 - xi) * (xi - u2) >= 0
                && (y1 - yi) * (yi - y2) >= 0 && (v1 - yi) * (yi - v2) >= 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the type of this object.
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the icon used to symbolize this object.
     */
    public Icon getIcon() {
        return icon;
    }

    /**
     * Returns the child at the specified index assuming the child is enumerated by the objects type. Note that this means that getChild(j)
     * may return null while getChild(i) and getChild(k) does not for i < j < k.
     * 
     * @param index the index of the object.
     */
    public ApproxsimObject getChild(int index) {
        if (index == 0) {
            return start;
        } else if (index == 1) {
            return end;
        } else {
            return null;
        }
    }

    /**
     * Returns the child at the specified index assuming the child is enumerated by the objects type. Note that this means that getChild(j)
     * may return null while getChild(i) and getChild(k) does not for i < j < k.
     * 
     * @param identifier the identifier of the child to get.
     */
    public ApproxsimObject getChild(String identifier) {
        if (identifier.equals("p1")) {
            return start;
        } else if (identifier.equals("p2")) {
            return end;
        } else {
            return null;
        }
    }

    /**
     * Returns the number of children this object contains.
     */
    public int getChildCount() {
        return 2;
    }

    /**
     * Returns true if this complex has a child with the specified name.
     * 
     * @param id identifier of object to search for
     */
    public boolean hasChild(String id) {
        return id.equals("p1") || id.equals("p2");
    }

    /**
     * Returns the children of this object.
     */
    public Enumeration children() {
        /**
         * A copy of an empty enumeration to use for children().
         */
        return new Enumeration() {
            int index = 0;

            public boolean hasMoreElements() {
                return index < 2;
            }

            public Object nextElement() throws NoSuchElementException {
                if (index < 2) {
                    return getChild(index++);
                } else {
                    throw new NoSuchElementException("No more elements.");
                }
            }
        };
    }

    /**
     * Returns the index of the provided child, according to the declaration or -1 if none.
     * 
     * @param child the child queried for.
     */
    public int getIndexOfChild(ApproxsimObject child) {
        if (child == start) {
            return 0;
        } else if (child == end) {
            return 1;
        } else {
            return -1;
        }
    }

    /**
     * Returns true if this object can have no children.
     */
    public boolean isLeaf() {
        return false;
    }

    /**
     * Adds a new child object to this object. If child with identical identifier exists, the previous entry is removed.
     * 
     * @param part the ApproxsimObject to add.
     * @param initiator The initiator of the add.
     */
    public void add(ApproxsimObject part, Object initiator) {
        if (part instanceof Point) {
            if (part.getIdentifier().equals("p1")) {
                start = (Point) part;
                part.setParent(this);
                fireObjectAdded(part, initiator);
            } else if (part.getIdentifier().equals("p2")) {
                end = (Point) part;
                part.setParent(this);
                fireObjectAdded(part, initiator);
            }
        }
    }

    /**
     * Removes the provided object from the tree.
     * 
     * @param child child to remove
     */
    protected void remove(ApproxsimObject child) {
        throw new AssertionError("Removing necessary component of line");
    }

    /**
     * Called when a (direct) child of this is replaced.
     * 
     * @param oldObj the old object being replaced
     * @param newObj the object replacing oldObj
     * @param initiator the object causing the replacement.
     */
    protected void replaceChild(ApproxsimObject oldObj, ApproxsimObject newObj,
            Object initiator) {
        if (oldObj == start) {
            start = (Point) newObj;
            start.setParent(this);
        } else if (oldObj == end) {
            end = (Point) newObj;
            end.setParent(this);
        } else {
            throw new AssertionError("Replacing a non-child of this object");
        }
        fireChildChanged(newObj, this);
    }

}

/**
 * LineVectorConstructor creates factories for creating Line objects.
 * 
 * @version 1, $Date: 2006/04/10 09:45:55 $
 * @author Daniel Ahlin
 */
class LineVectorConstructor extends ApproxsimVectorConstructor {
    /**
     * Creates a new LineVectorConstructor using the supplied declaration.
     * 
     * @param declaration the declaration to use.
     */
    public LineVectorConstructor(Declaration declaration) {
        super(declaration);
    }

    /**
     * Returns the ApproxsimObject this component was created to provide.
     * 
     * @param parts the parts to use in constructing the object.
     */
    public ApproxsimObject getApproxsimObject(Vector parts) {
        Point p1 = (Point) parts.get(0);
        Point p2 = (Point) parts.get(1);

        if (!p1.getIdentifier().equals("p1")) {
            Point temp = p1;
            p1 = p2;
            p2 = temp;
        }

        if (!p1.getIdentifier().equals("p1")
                || !p2.getIdentifier().equals("p2")) {
            throw new AssertionError("Internal Line transport error.");
        }

        return new Line(this.getDeclaration(), p1, p2);
    }
}
