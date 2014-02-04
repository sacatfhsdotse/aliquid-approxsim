//         $Id: Composite.java,v 1.4 2006/05/11 16:43:03 alexius Exp $
/*
 * @(#)Composite.java
 */

package StratmasClient.object;

import java.util.Vector;
import java.util.Enumeration;
import java.util.HashSet;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import StratmasClient.BoundingBox;

import StratmasClient.object.type.Declaration;
import StratmasClient.object.type.Type;
import StratmasClient.object.type.TypeFactory;
import StratmasClient.map.Projection;
import StratmasClient.object.primitive.Timestamp;
import StratmasClient.object.primitive.Identifier;

/**
 * A composite defines a set of two dimensional shapes.
 *
 * @version 1, $Date: 2006/05/11 16:43:03 $
 * @author  Daniel Ahlin
 */

public class Composite extends Shape
{
    /**
     * Creates an identified composite, defined by the supplied shapes.
     *
     * @param identifier the identifier of the shape.
     * @param shapes the subshapes of the shape.
     */
    protected Composite(String identifier, Vector shapes)
    {
        super(identifier, TypeFactory.getType("Composite"));
        this.add(shapes);
    }

    /**
     * Creates a polygon, defined by the supplied lines.
     *
     * @param declaration the declaration of the shape.
     * @param lines the lines that makes up the polygon the shape.
     */
    protected Composite(Declaration declaration, Vector lines)
    {
        this(declaration.getName(), lines);
    }

    /**
     * Returns the parts of this Composite.
     */
    public Enumeration getParts()
    {
        // We know that Composite is expected to contain a
        // StratmasList as its only direct child.
        StratmasList shapes = (StratmasList) this.getChild("shapes");
        return shapes.children();
    }

    /**
     * Reduces this Shape and adds it to supplied  Vector.
     *
     *@param res vector to add result to.
     */
    public Vector constructSimpleShapes(Vector res)
    {
        for (Enumeration ss = this.getParts(); ss.hasMoreElements();) {
            Shape s = (Shape) ss.nextElement();
            s.constructSimpleShapes(res);
        }
        return res;
    }

    /**
     * Returns a StratmasVectorConstructor suitable for constructing
     * objects of this type.
     *
     * @param declaration the declaration for which the object is created.
     */
    protected static StratmasVectorConstructor getVectorConstructor(Declaration declaration)
    {
        return new CompositeVectorConstructor(declaration);
    }

    /**
     * Creates a Composite from the specified Declaration.
     *
     * <p> author Per Alexius
     *
     * @param declaration The declaration for which the object is created.
     */
    protected static StratmasObject defaultCreate(Declaration declaration)
    {
        throw new AssertionError("No default constructor for Composite.");
    }
    
    /**
     * Creates this shapes bounding box. This method assumes that the Composite is nonempty.
     */
    public BoundingBox createBoundingBox()
    {                
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
     * Returns the bounding box with respect to its projected parts. 
     * This method assumes that the Composite is nonempty.
     */
    public BoundingBox getBoundingBox(Projection proj)
    {                
        Enumeration ss = this.getParts();
        BoundingBox res = ((Shape) ss.nextElement()).getBoundingBox(proj);
        for (;ss.hasMoreElements();) {
            Shape s = (Shape) ss.nextElement();
            res = BoundingBox.combine(res, s.getBoundingBox(proj), proj);
        }
        
        return res;
    }

     /**
      * Moves this shape relative to its current position.
      *
      * <p>author  Per Alexius
      *
      * @param dx The distance to move given in degrees longitude.
      * @param dy The distance to move given in degrees latitude.
      */
     public void move(double dx, double dy) {
          for(Enumeration en = getParts(); en.hasMoreElements(); ) {
               ((Shape)en.nextElement()).move(dx, dy);
          }
     }

     /**
      * Moves this shape to the specified location.
      *
      * <p>author  Per Alexius
      *
      * @param lng The longitude of the new location.
      * @param lat The latitude of the new location.
      */
     public void moveTo(double lng, double lat) {
          BoundingBox b = createBoundingBox();
          double cenLng = b.getWestLon() + (b.getEastLon() - b.getWestLon()) / 2;
          double cenLat = b.getSouthLat() + (b.getNorthLat() - b.getSouthLat()) / 2;
          move(lng - cenLng, lat - cenLat);
     }

    /**
     * Clones this object. Notice that the Identifier is NOT
     * cloned. Both the clone and the original object will thus keep a
     * reference to the same Identifier object.
     *
     * <p> author Per Alexius
     *
     * @return A clone of this object.
     */
     protected Object clone() {
          Vector elements = new Vector();
          for (Enumeration en = children(); en.hasMoreElements(); ) {
               elements.add(((StratmasObject)en.nextElement()).clone());
          }
          return new Composite(identifier, elements);
     }

    /**
     * Updates this object with the data contained in the Element n.
     *
     * <p> author Per Alexius
     *
     * @param n The DOM Element from which to fetch the data.
     * @param t The simulation time for which the data is valid.
     */
    public void update(Element n, Timestamp t) 
    {
        if (getType().equals(TypeFactory.getType(n))) {
            // Get the StratmasList containing the child shapes.
            StratmasList shapeList = (StratmasList)getChild("shapes");
            if (shapeList == null) {
                throw new AssertionError("Composite without shape StratmasList");
            }
            
            HashSet newIDs = new HashSet();
            // Go through the child shapes in the new Composite and
            // check them with the existing child shapes. 
            for (Node child = n.getFirstChild(); 
                 child != null; child = child.getNextSibling()) {
                if (child.getNodeType() == Node.ELEMENT_NODE &&
                    child.getNodeName().equals("shapes")) {
                    // Cast the child shape Node to an Element.
                    Element shapeElem = (Element) child;
                    
                    // Get the Identifier of the child shape.
                    String id = Identifier.getIdentifier(shapeElem);
                    newIDs.add(id);
                    
                    // Get the Type of the child shape.
                    Type typeOfNewChild = TypeFactory.getType(shapeElem);
                    
                    // Check if we have a child shape with the same
                    // name as the new one.
                    Shape childShape = (Shape)shapeList.getChild(id);
                    if (childShape == null) {
                        // If not, we add the new child shape.
                        shapeList.add(StratmasObjectFactory.domCreate(shapeElem));
                    }
                    else if (!childShape.getType().equals(typeOfNewChild)) {
                        // If we do, but it has a different Type than
                        // the new one, replace it with the new one.
                        shapeList.remove(childShape);
                        shapeList.add(StratmasObjectFactory.domCreate(shapeElem));
                    }
                    else {
                        // Ok, it was just an update of the same
                        // child shape.
                        childShape.update(shapeElem, t);
                    }
                }
            }
            
            // Remove all of our current child shapes that wasn't
            // mentioned in the update.
            for (Enumeration en = shapeList.children(); 
                 en.hasMoreElements(); ) {
                StratmasObject o = (StratmasObject)en.nextElement();
                if(newIDs.contains(o.getIdentifier())) {
                    shapeList.remove(o);
                }
            }
        }
        else {
            replace(StratmasObjectFactory.domCreate(n), n);
        }
    }

    /**
     * Checks if this Composite contains any Polygons that are not
     * closed.
     *
     * @return True if there are at least one polygon in this
     * Composite that is not closed, false otherwise.
     */
    public boolean hasUnclosed() 
    {
        for (Enumeration en = getParts(); en.hasMoreElements(); ) {
            Shape s = (Shape) en.nextElement();
            if (s instanceof Polygon && !((Polygon)s).isClosed()) {
                return true;
            }
            else if (s instanceof Composite && ((Composite)s).hasUnclosed()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a Shape from a DOM element.
     *
     * <p> author Per Alexius
     *
     * @param n The dom element from which the object is created.
     */
    protected static StratmasObject domCreate(Element n) 
    {
        Vector shapes = new Vector();
        for (Node child = n.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("shapes")) {
                shapes.add(StratmasObjectFactory.domCreate((Element)child));
            }
        }
        StratmasList list = new StratmasList(new Declaration(TypeFactory.getType("Shape"), "shapes", 
                                                             1, -1, true), shapes);
        Vector listVec = new Vector();
        listVec.add(list);          
        return new Composite(Identifier.getIdentifier(n), listVec);
    }
          
}


/*
 * @(#)CompositeVectorConstructor
 */

/**
 * CompositeVectorConstructor constructs a composite using
 * a vector.
 *
 * @version 1, $Date: 2006/05/11 16:43:03 $
 * @author  Daniel Ahlin
*/
class CompositeVectorConstructor extends StratmasVectorConstructor
{
    /**
     * Creates a new object using specifications in declaration.
     *
     * @param declaration the declaration to use.
     */
    public CompositeVectorConstructor(Declaration declaration)
    {
        super(declaration);
    }

    /**
     * Returns the StratmasObject this component was created to provide.
     *
     * @param parts the parts to use in constructing the object.
     */
    public StratmasObject getStratmasObject(Vector parts)
    {
        return new Composite(this.getDeclaration(), parts);
    }
}
