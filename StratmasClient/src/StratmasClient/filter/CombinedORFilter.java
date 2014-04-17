package StratmasClient.filter;

import java.util.Enumeration;
import java.util.Vector;
import StratmasClient.object.StratmasObject;

/**
 * CombinedORFilter filters out StratmasObjects accordning OR rules.
 */

public class CombinedORFilter extends CombinedFilter {
    /**
     * Creates a new filter.
     * 
     * @param filters the filters that will make up this filter
     */
    public CombinedORFilter(Vector filters) {
        super(filters);
    }

    /**
     * Creates a new filter with no subfilters.
     */
    public CombinedORFilter() {
        super();
    }

    /**
     * Returns true if the provided StratmasObject passes the filter.
     * 
     * @param sObj the object to test
     */
    public boolean pass(StratmasObject sObj) {
        boolean res = false;

        for (Enumeration e = getFilters(); e.hasMoreElements();) {
            if (((StratmasObjectFilter) e.nextElement()).pass(sObj)) {
                res = true;
                break;
            }
        }

        return applyInverted(res);
    }
}
