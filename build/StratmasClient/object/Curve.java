// 	$Id: Curve.java,v 1.3 2006/03/31 16:55:51 dah Exp $
/*
 * @(#)Curve.java
 */

package StratmasClient.object;

import java.util.Vector;

import StratmasClient.object.type.Type;

/**
 * A curve defines a contigous set of one-dimensional points.
 *
 * @version 1, $Date: 2006/03/31 16:55:51 $
 * @author  Daniel Ahlin
*/

public abstract class Curve extends StratmasObjectImpl
{
    /**
     * Creates an Curve
     *
     * @param identifier the identifier for the object.
     */
    Curve(String identifier)
    {
	super(identifier);
    }

    /**
     * Returns a set of lines approximating this curve within the
     * specified error. N.B will only recalculate if old cached value
     * has a higher error than the previous.
     *
     * @param error the maximum difference between the true and
     * approximated line.
     */
    public abstract Vector getLineApproximation(double error);

     /**
      * Called when a (direct) child of this has changed. The default
      * behaviour for Curve is to pass the event upwards in the
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
     * Returns the startPoint of this curve.     
     */
    public abstract Point getStartPoint();

    /**
     * Returns the endPoint of this curve.
     */
    public abstract Point getEndPoint();
}
