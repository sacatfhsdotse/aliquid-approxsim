// 	$Id: CombinedFilter.java,v 1.4 2006/03/30 10:13:30 amfi Exp $
/*
 * @(#)Combinedfilter.java
 */

package StratmasClient.filter;

import java.util.Enumeration;
import java.util.Vector;
import StratmasClient.object.StratmasObject;

/**
 * Combinedfilter filters out StratmasObjects accordning to
 * provided rules.
 *
 * @version 1, $Date: 2006/03/30 10:13:30 $
 * @author  Daniel Ahlin
*/

public class CombinedFilter extends StratmasObjectFilter
{
    /**
     * The filters this filter is made of.
     */
    Vector filters = new Vector();

    /**
     * Creates a new filter.
     *
     * @param filters the filters that will make up this filter
     */
    public CombinedFilter(Vector filters)
    {	
	super();
	this.add(filters);
    }

    /**
     * Creates a new filter with no subfilters.
     *
     */
    public CombinedFilter()
    {	
	this(new Vector());
    }

    /**
     * Returns true if the provided StratmasObject passes the filter.
     *
     * @param sObj the object to test
     */
    public boolean pass(StratmasObject sObj)
    {
	boolean res = true;
	
	for (Enumeration e = getFilters(); e.hasMoreElements();) {
	    if (! ((StratmasObjectFilter) e.nextElement()).pass(sObj)) {
		res = false;
		break;
	    }
	}

	return applyInverted(res);
    }

    public void addFilter(StratmasObjectFilter filter)
    {
	this.filters.add(filter);
    }

    /**
     * Adds the specified filter to this filter..
     *
     * @param filter the filter to add.
     */
    public void add(StratmasObjectFilter filter)
    {
	this.filters.add(filter);
    }

    /**
     * Adds the specified filters to this filter..
     *
     * @param filters the filters to add.
     */
    public void add(Vector filters)
    {
	for (Enumeration e = filters.elements(); e.hasMoreElements();) {
	    add((StratmasObjectFilter) e.nextElement());
	}
    }

    /**
     * Removes the specified filter from this filter..
     *
     * @param filter the filter to remove.
     */
    public void remove(StratmasObjectFilter filter)
    {
	this.filters.remove(filter);
    }


    /**
     * Returns the filters making up this filter.
     */
    public Enumeration getFilters()
    {
	return this.filters.elements();
    }
}
