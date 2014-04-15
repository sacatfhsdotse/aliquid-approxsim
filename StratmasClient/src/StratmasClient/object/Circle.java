//         $Id: Circle.java,v 1.6 2006/05/11 16:43:03 alexius Exp $
/*
 * @(#)Circle.java
 */

package StratmasClient.object;

import java.util.Vector;

import StratmasClient.BoundingBox;

import StratmasClient.object.type.Declaration;
import StratmasClient.object.type.TypeFactory;
import StratmasClient.object.primitive.Identifier;
import StratmasClient.object.primitive.Timestamp;
import StratmasClient.map.Projection;

import org.w3c.dom.Element;

/**
 * A circle defines a set of two dimensional points contained by a
 * scalars reach from a twodimensional point.
 *
 * @version 1, $Date: 2006/05/11 16:43:03 $
 * @author  Daniel Ahlin
*/

public class Circle extends SimpleShape
{
    /**
     * The position of the circle.
     */
    Point position;
    
    /**
     * The radius of the circle.
     */
    StratmasDecimal radius;

    /**
     * Creates an identified Circle.
     *
     * @param identifier the identifier of the Circle.
     * @param position the position of the circle.
     * @param radius the radius of the circle.
     */
    protected Circle(String identifier, Point position, StratmasDecimal radius)
    {
        super(identifier, TypeFactory.getType("Circle"));
        this.position = position;
        this.radius = radius;
        this.add(radius);
        this.add(position);
    }

    /**
     * Creates a new Circle from a Declaration.
     *
     * @param declaration the declaration for this object.
     * @param position the start-point of the line.
     * @param radius the end-point of the line.
     */
    protected Circle(Declaration declaration, Point position, StratmasDecimal radius)
    {
        this(declaration.getName(), position, radius);
    }

    /**
     * Returns center of the circle.
     *
     * @return center position of the circle
     */
    public Point getCenter() 
    {
        return position;
    }

    /**
     * Returns radius of the circle.
     *
     * @return radius of the circle
     */
    public double getRadius() 
    {
         return radius.getValue();
    }
    
     /**
      * Sets the radius of the circle.
      */
     public void setRadius(double radius) {
          this.radius.setValue(radius, this);
     }
     
    /**
     * Constructs an approximated polygon of this Circle.
     *
    * @param error the maximal difference between the true line and its approximation.
     */
    protected Polygon constructPolygon(double error)
    {
        // Get minimal number of partitions acceptable with the
        // specified error. Always use at least three lines, never more than 100.
        int partitions = (int) Math.ceil(Math.PI/(Math.acos((getRadius() - error)/getRadius())));
        partitions = partitions >= 3 ? partitions : 3;
        if (partitions > 100) {
            partitions = 100;
            //Debug.err.println(getClass().getName() + ": Clamping approximated polygon to " + 
            //                      partitions + " segments.");
        }
        double angleStep = 2*Math.PI/((double) partitions);

        // Get center
        double centerLat = getCenter().getLat();
        double centerLon = getCenter().getLon();        
        // The radius in degrees
        double latDistance = getRadius() / 111000;
        double lonDistance = latDistance / 
            Math.cos(Math.toRadians(centerLat));


        // Create lines
        Vector<Line> lines = new Vector<Line>();
        // First line along degreeLine
        double startLat = centerLat + latDistance*Math.sin(0);
        double startLon = centerLon + lonDistance*Math.cos(0);
        double lon1 = startLon;
        double lat1 = startLat;
        for (int i = 1; i < partitions; i++) {
            double angle = ((double) i) * angleStep;
            double lat2 = centerLat + latDistance*Math.sin(angle);
            double lon2 = centerLon + lonDistance*Math.cos(angle);
            lines.add(StratmasObjectFactory.createLine(Integer.toString(i), 
                               StratmasObjectFactory.createPoint("p1", lat1, lon1),
                               StratmasObjectFactory.createPoint("p2", lat2, lon2)));
            lat1 = lat2;
            lon1 = lon2;
        }
        // Tie polygon together
        lines.add(StratmasObjectFactory.createLine(Integer.toString(partitions), 
                           StratmasObjectFactory.createPoint("p1", lat1, lon1),
                           StratmasObjectFactory.createPoint("p2", startLat, startLon)));
        
        
        Vector<StratmasList> v = new Vector<StratmasList>();
        v.add(new StratmasList("curves", TypeFactory.getType("Line"), 
                               lines));
        return new Polygon("Approximation of " + getIdentifier(), v);
    }

    /**
     * Creates this shapes bounding box.
     */
    public BoundingBox createBoundingBox()
    {
        // 111000 == the distance in meters between every latitude degree.
        double latDistance = getRadius() / 111000;
        double lonDistance = latDistance / 
            Math.cos(Math.toRadians(getCenter().getLat()));
        
        return new BoundingBox (getCenter().getLon() - lonDistance,
                                getCenter().getLat() - latDistance,
                                getCenter().getLon() + lonDistance,
                                getCenter().getLat() + latDistance);
    }

    /**
     * Returns the bounding box with respect to its projected parts. 
      */
    public BoundingBox getBoundingBox(Projection proj)
    {                
        return this.constructPolygon(1).getBoundingBox(proj);
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
          position.move(dx, dy);
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
          position.moveTo(lng, lat);
     }



    /**
     * Returns a StratmasVectorConstructor suitable for constructing
     * objects of this type.
     *
     * @param declaration the declaration for which the object is created.
     */
    protected static StratmasVectorConstructor getVectorConstructor(Declaration declaration)
    {
        return new CircleVectorConstructor(declaration);
    }
    
    /**
     * Creates a Circle from the specified Declaration.
     *
     * <p> author Per Alexius
     *
     * @param declaration The declaration for which the object is created.
     */
    protected static StratmasObject defaultCreate(Declaration declaration)
    {
        Vector<StratmasObject> newParts = new Vector<StratmasObject>();
        for (java.util.Iterator it = declaration.getType().getSubElements().iterator(); it.hasNext(); ) {
            Declaration dec = (Declaration)it.next();
            if (dec.isSingular()) {
                newParts.add(StratmasObjectFactory.defaultCreate(dec));
            }
        }
        return getVectorConstructor(declaration).getStratmasObject(newParts);
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
        return 
            new Circle(Identifier.getIdentifier(n),
                       (Point)
                       StratmasObjectFactory.domCreate(XMLHelper.getFirstChildByTag(n, "center")),
                       (StratmasDecimal) 
                       StratmasObjectFactory.domCreate(XMLHelper.getFirstChildByTag(n, "radius")));
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
          return new Circle(identifier, (Point) getCenter().clone(), (StratmasDecimal) radius.clone());
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
            position.update(XMLHelper.getFirstChildByTag(n, "center"), t);
            radius.update(XMLHelper.getFirstChildByTag(n, "radius"), t);
        }
        else {
            replace(StratmasObjectFactory.domCreate(n), n);
        }
    }
}

/**
 * CircleVectorConstructor creates factories for creating Circle objects.
 *
 * @version 1, $Date: 2006/05/11 16:43:03 $
 * @author  Daniel Ahlin
*/
class CircleVectorConstructor extends StratmasVectorConstructor
{
    /**
     * Creates a new CircleVectorConstructor using the supplied
     * declaration.  
     *
     * @param declaration the declaration to use.
     */
    public CircleVectorConstructor(Declaration declaration)
    {
        super(declaration);
    }

    /**
     * Returns the StratmasObject this component was created to provide.
     *
     * @param parts the parts to use in constructing the object.
     */
    public StratmasObject getStratmasObject(Vector<StratmasObject> parts)
    {
        StratmasObject position = parts.get(0);
        StratmasObject radius = parts.get(1);
        
        if (!position.getIdentifier().equals("center")) {
            StratmasObject temp = position;
            position = radius;
            radius = temp;
        }
        
        if (!position.getIdentifier().equals("center") ||
            !radius.getIdentifier().equals("radius")) {
            throw new AssertionError("Internal Circle transport error.");
        }

        return new Circle(this.getDeclaration(), (Point) position, ((StratmasDecimal) radius));
    }
}
