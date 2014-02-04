//         $Id: PathFilter.java,v 1.4 2006/03/22 14:30:50 dah Exp $
/*
 * @(#)Pathfilter.java
 */

package StratmasClient.filter;

import StratmasClient.object.StratmasObject;

import java.util.Vector;
import java.util.Enumeration;

/**
 * Pathfilter filters out StratmasObjects accordning to
 * provided rules.
 *
 * FIXME: There are great possibilities in overriding the filterTree
 * method in this class..
 *
 * @version 1, $Date: 2006/03/22 14:30:50 $
 * @author  Daniel Ahlin
*/

public class PathFilter extends StratmasObjectFilter
{
    /**
     * The filters this filter is made of.
     */
    Vector filters;

    /**
     * Where the target of this filter is supposed to be.
     */
    int targetIndex;

    /**
     * This filter is currently broken.
     * Creates a new PathFilter
     */
    private PathFilter()
    {                
        this.filters = new Vector();
    }
    
    /**
     * Sets a filter for the paths component i.
     *
     * @param filter the filter to use for component i
     * @param i which position in the path the test should apply to.
     */
    public void addComponent(StratmasObjectFilter filter, int i)
    {
        filters.add(i, filter);
    }

    /**
     * Sets a filter for the paths last component.
     *
     * @param filter the filter to use for the last component.
     */
    public void addComponent(StratmasObjectFilter filter)
    {
        filters.add(filter);
    }

    /**
     * Sets the index of the target for the path filter.  I. e
     * assuming filters f_0..f_j..f_n and objects s_0..s_i..s_m where
     * s0 is the parent of s1 and so forth. This filters pass function
     * means that if i is the index set, then s_i will pass iff f_i-1
     * passes its parent and so forth.
     *
     * @param targetIndex the index
     */
    public void setTargetIndex(int targetIndex)
    {
        this.targetIndex = targetIndex;
    }
    
    /**
     * Returns the index of the target for the path filter.  I. e
     * assuming filters f_0..f_j..f_n and objects s_0..s_i..s_m where
     * s0 is the parent of s1 and so forth. This filters pass function
     * means that if i is the index set, then s_i will pass iff f_i-1
     * passes its parent and so forth.
     */
    public int getTargetIndex()
    {
        return this.targetIndex;
    }

    /**
     * Gets the filter for the i'th path component.
     *
     * @param i the sequence of the filter to get
     */
    public StratmasObjectFilter getComponent(int i)
    {
        return (StratmasObjectFilter) this.filters.get(i);
    }

    /**
     * Returns the number of Components in filter.
     */
    public int getComponentCount()
    {
        return this.filters.size();
    }

    /**
     * Returns true if the provided StratmasObject passes the filter.
     *
     * @param sObj the object to test
     */
    public boolean pass(StratmasObject sObj)
    {
        boolean res = true;

        // Check sObj
        res = getComponent(getTargetIndex()).pass(sObj);

        // Check upwards.
        StratmasObject walker = sObj.getParent();
        for (int i = getTargetIndex() - 1; res && i > 0; i--) {
            res = (walker != null || getComponent(i).pass(walker));
            walker = walker.getParent();
        }

        // Check downwards.
        walker = sObj.getChild();
        for (int i = getTargetIndex() + 1; res && i < getComponentCount; i++) {
            res = (walker != null || getComponent(i).pass(walker));
            walker = walker.getParent();
        }

        return applyInverted(res);
    }
}

