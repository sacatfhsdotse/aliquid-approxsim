//         $Id: StratmasObjectFilter.java,v 1.5 2006/03/22 14:30:50 dah Exp $
/*
 * @(#)StratmasObjectFilter.java
 */

package StratmasClient.filter;

import StratmasClient.object.StratmasObject;

import java.util.Enumeration;
import java.util.Vector;

/**
 * StratmasObjectFilter filters out StratmasObjects accordning to
 * provided rules.
 *
 * @version 1, $Date: 2006/03/22 14:30:50 $
 * @author  Daniel Ahlin
*/

public abstract class StratmasObjectFilter
{
    /**
     * If the filter is inverted.
     */
    boolean inverted = false;

    /**
     * Creates a new filter with default rules (pass everything).
     *
     */
    public StratmasObjectFilter()
    {        
    }

    /**
     * Returns true if the provided StratmasObject passes the filter.
     *
     * @param sObj the object to test
     */
    public abstract boolean pass(StratmasObject sObj);

    /**
     * Returns true if the target of the provided StratmasObjectAdapter passes the filter.
     *
     * @param sObjAdapter the object to test
     */
    public boolean pass(StratmasObjectAdapter sObjAdapter)
    {
        return pass(sObjAdapter.getStratmasObject());
    }

    /**
     * Returns all object in tree rooted at sObj that passes this
     * filter.
     *
     * @param sObj the object to test
     */
    public Enumeration filterTree(StratmasObject sObj)
    {
        return filterTree(sObj, new Vector()).elements();        
    }

    /**
     * Returns all objects in vector that passes this
     * filter.
     *
     * @param vector the vector to test
     */
    public Vector filter(Vector vector)
    {
        return filter(vector.elements());
    }
    
    /**
     * Returns all objects in vector that passes this
     * filter.
     *
     * @param e the list to test
     */
    public Vector filter(Enumeration e)
    {
        Vector res = new Vector();
        for (; e.hasMoreElements();) {
            Object o = e.nextElement();
            if (o instanceof StratmasObject && pass((StratmasObject) o)) {
                res.add(o);
            } else if (o instanceof StratmasObjectAdapter && 
                       pass((StratmasObjectAdapter) o)) {
                res.add(o);
            }
        }
        
        return res;
    }

    /**
     * Returns true if this filter is inverted.
     */
    public boolean isInverted()
    {
        return this.inverted;
    }

    /**
     * Sets the inverted flag of this filter. (Inverting meaning that
     * all replies from pass will be inverted.)
     */
    public void setInverted(boolean inverted)
    {
        this.inverted = inverted;
    }

    /**
     * Returns the provided vector with all object in tree rooted at
     * sObj that passes this filter inserted.
     *
     * @param sObj the object to test
     * @param vector vector to add results to
     */
    public Vector filterTree(StratmasObject sObj, Vector vector)
    {
        if (pass(sObj)) {
            vector.add(sObj);
        }

        for (Enumeration e = sObj.children(); e.hasMoreElements();) {
            filterTree((StratmasObject) e.nextElement(), vector);
        }

        return vector;
    }

    /**
     * Applies the inverted flag of this filter to provided boolean
     *
     * @param b the boolean to possibly invert.
     */
    protected boolean applyInverted(boolean b)
    {
        if (isInverted()) {
            return !b;
        } else {
            return b;
        }
    }

}
