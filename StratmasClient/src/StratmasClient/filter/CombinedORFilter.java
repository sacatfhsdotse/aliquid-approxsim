package ApproxsimClient.filter;

import java.util.Enumeration;
import java.util.Vector;
import ApproxsimClient.object.ApproxsimObject;

/**
 * CombinedORFilter filters out ApproxsimObjects accordning OR rules.
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
     * Returns true if the provided ApproxsimObject passes the filter.
     * 
     * @param sObj the object to test
     */
    public boolean pass(ApproxsimObject sObj) {
        boolean res = false;

        for (Enumeration e = getFilters(); e.hasMoreElements();) {
            if (((ApproxsimObjectFilter) e.nextElement()).pass(sObj)) {
                res = true;
                break;
            }
        }

        return applyInverted(res);
    }
}
