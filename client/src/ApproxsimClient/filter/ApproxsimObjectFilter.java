// $Id: ApproxsimObjectFilter.java,v 1.5 2006/03/22 14:30:50 dah Exp $
/*
 * @(#)ApproxsimObjectFilter.java
 */

package ApproxsimClient.filter;

import ApproxsimClient.object.ApproxsimObject;

import java.util.Enumeration;
import java.util.Vector;

/**
 * ApproxsimObjectFilter filters out ApproxsimObjects accordning to provided rules.
 * 
 * @version 1, $Date: 2006/03/22 14:30:50 $
 * @author Daniel Ahlin
 */

public abstract class ApproxsimObjectFilter {
    /**
     * If the filter is inverted.
     */
    boolean inverted = false;

    /**
     * Creates a new filter with default rules (pass everything).
     */
    public ApproxsimObjectFilter() {}

    /**
     * Returns true if the provided ApproxsimObject passes the filter.
     * 
     * @param sObj the object to test
     */
    public abstract boolean pass(ApproxsimObject sObj);

    /**
     * Returns true if the target of the provided ApproxsimObjectAdapter passes the filter.
     * 
     * @param sObjAdapter the object to test
     */
    public boolean pass(ApproxsimObjectAdapter sObjAdapter) {
        return pass(sObjAdapter.getApproxsimObject());
    }

    /**
     * Returns all object in tree rooted at sObj that passes this filter.
     * 
     * @param sObj the object to test
     */
    public Enumeration filterTree(ApproxsimObject sObj) {
        return filterTree(sObj, new Vector()).elements();
    }

    /**
     * Returns all objects in vector that passes this filter.
     * 
     * @param vector the vector to test
     */
    public Vector filter(Vector vector) {
        return filter(vector.elements());
    }

    /**
     * Returns all objects in vector that passes this filter.
     * 
     * @param e the list to test
     */
    public Vector filter(Enumeration e) {
        Vector res = new Vector();
        for (; e.hasMoreElements();) {
            Object o = e.nextElement();
            if (o instanceof ApproxsimObject && pass((ApproxsimObject) o)) {
                res.add(o);
            } else if (o instanceof ApproxsimObjectAdapter
                    && pass((ApproxsimObjectAdapter) o)) {
                res.add(o);
            }
        }

        return res;
    }

    /**
     * Returns true if this filter is inverted.
     */
    public boolean isInverted() {
        return this.inverted;
    }

    /**
     * Sets the inverted flag of this filter. (Inverting meaning that all replies from pass will be inverted.)
     */
    public ApproxsimObjectFilter setInverted(boolean inverted) {
        this.inverted = inverted;
        return this;
    }

    /**
     * Returns the provided vector with all object in tree rooted at sObj that passes this filter inserted.
     * 
     * @param sObj the object to test
     * @param vector vector to add results to
     */
    public Vector filterTree(ApproxsimObject sObj, Vector vector) {
        if (pass(sObj)) {
            vector.add(sObj);
        }

        for (Enumeration e = sObj.children(); e.hasMoreElements();) {
            filterTree((ApproxsimObject) e.nextElement(), vector);
        }

        return vector;
    }

    /**
     * Applies the inverted flag of this filter to provided boolean
     * 
     * @param b the boolean to possibly invert.
     */
    protected boolean applyInverted(boolean b) {
        if (isInverted()) {
            return !b;
        } else {
            return b;
        }
    }

}
