//         $Id: Shape.java,v 1.4 2006/07/31 10:18:44 alexius Exp $
/*
 * @(#)Shape.java
 */

package StratmasClient.object;

import StratmasClient.map.Visualizer;
import StratmasClient.map.Projection;
import StratmasClient.object.type.Type;
import StratmasClient.object.type.Declaration;
import StratmasClient.object.type.TypeFactory;
import StratmasClient.object.primitive.Reference;
import StratmasClient.object.primitive.Identifier;

import org.w3c.dom.Element;

import StratmasClient.ActionGroup;
import StratmasClient.BoundingBox;

import java.util.Enumeration;
import java.util.Vector;
import java.awt.event.ActionEvent;


/**
 * A shape defines a set of two dimensional points. It is built by
 * enclosing regions with curves or by combining other shapes into a
 * new shape.
 *
 * @version 1, $Date: 2006/07/31 10:18:44 $
 * @author  Daniel Ahlin
*/

public abstract class Shape extends DefaultComplex
{
    /**
     * Bounding box of the shape.
     */
    BoundingBox box;
    
    /**
     * Creates an identified shape of specified type.
     *
     * @param identifier the identifier for the object.
     * @param type the type of the object.
     */
    protected Shape(String identifier, Type type)
    {
        super(identifier, type);
    }

    /**
     * Reduces this Shape and adds it to supplied  Vector.
     *
     *@param res vector to add result to.
     */
    public abstract Vector<SimpleShape> constructSimpleShapes(Vector<SimpleShape> res);

    /**
     * Reduces this Shape to a Vector of SimepleShapes.
     */
    public Vector<SimpleShape> constructSimpleShapes()
    {
        Vector<SimpleShape> res = new Vector<SimpleShape>();
        return constructSimpleShapes(res);
    }
    
    /**
     * Sets bounding box of the shape.
     *
     * @param box bounding box of the shape.
     */
    public void setBoundingBox(BoundingBox box) {
        this.box = box;
    }

    /**
     * Returns bounding box of the shape.
     *
     * @return bounding box of the shape.
     */
    public BoundingBox getBoundingBox() {
         box = createBoundingBox();
         return box;
    }

    /**
     * Returns bounding box of the shape with respect to it's projected parts.
     * Obs that this bounding box depends on the given projection and usually
     * it's not equal to the bounding box obtained by getBoundingBox() method.
     * In this box all the projected parts are inside the projected bounds.
     */
    public abstract BoundingBox getBoundingBox(Projection proj);

    /**
     * Creates this shapes bounding box.
     */
    public abstract BoundingBox createBoundingBox();

     /**
      * Moves this shape relative to its current position.
      *
      * <p>author  Per Alexius
      *
      * @param dx The distance to move given in degrees longitude.
      * @param dy The distance to move given in degrees latitude.
      */
     public abstract void move(double dx, double dy);

     /**
      * Moves this shape to the specified location.
      *
      * <p>author  Per Alexius
      *
      * @param lng The longitude of the new location.
      * @param lat The latitude of the new location.
      */
     public abstract void moveTo(double lng, double lat);
      
    /**
     * Creates a Shape from the specified Declaration. The default
     * Shape is a Circle.
     *
     * <p> author Per Alexius
     *
     * @param declaration The declaration for which the object is created.
     */
    protected static StratmasObject defaultCreate(Declaration declaration)
    {
        return Circle.defaultCreate(declaration.clone(TypeFactory.getType("Circle")));
    }

    /**
     *
     */
    public Vector<Shape> getAncestralShapes()
    {
        Vector<Shape> res = new Vector<Shape>();
        for (StratmasObject walker = this; walker.getParent() != null && (walker.getParent() instanceof Shape ||
                                                              walker.getParent() instanceof StratmasList);
             walker = (StratmasObject) walker.getParent()) {
            if (walker.getParent() instanceof Shape) {
                res.add((Shape)walker.getParent());
            }
        }
        
        return res;
    }

    /**
     * Called when a (direct) child of this has changed. The default
     * behaviour for Shape is to pass the event upwards in the
     * tree.
     *
     * @param child the child that changed
     */
     public void childChanged(StratmasObject child, Object initiator) {        
          if (getParent() != null) {
               getParent().childChanged(this, initiator);
          }

          fireChildChanged(child, initiator);
     }

    /**
     * Returns a StratmasGUIConstructor suitable for constructing
     * objects of this type.
     *
     * @param declaration the declaration for which the GUI is created.
     */
    protected static StratmasGUIConstructor getGUIConstructor(Declaration declaration)
    {
        return new StratmasComplexGUIConstructor(declaration);
    }
    
//     /**
//      * Returns actions associated with this object
//      */
//     public Vector getActions()
//     {
//         Vector res = new Vector();
//         res.addAll(super.getActions());
        
//         final Shape self = this;
//         for (StratmasObject walker = this; walker != null; 
//              walker = walker.getParent()) {
//             if (walker.getIdentifier().equals("map")) {
//                 StratmasAbstractAction showMapAction = 
//                     new StratmasAbstractAction("Show in new Map", false)
//                     {
//                         public void actionPerformed(ActionEvent e)
//                         {
//                             Visualizer.createNewStratMap(self);
//                         }
//                     };
//                 showMapAction.setEnabled(true);
//                 res.add(showMapAction);
                
//                 break;
//             }
//         }
        
//         return res;
//     }

    /**
     * Returns actions associated with this object
     */
    public ActionGroup getActionGroup()
    {
        ActionGroup ag = super.getActionGroup();
        final Shape self = this;
        for (StratmasObject walker = this; walker != null; walker = walker.getParent()) {
            if (walker.getIdentifier().equals("map")) {
                ActionGroup showMapAction =  new ActionGroup("Show in new Map", false, false) {
                        public void actionPerformed(ActionEvent e) {
                            Visualizer.createNewStratMap(self);
                        }
                    };
                showMapAction.setEnabled(true);
                ag.add(showMapAction);
                break;
            }
        }
        return ag;
    }
}
