package ApproxsimClient.filter;

import java.util.Enumeration;

import ApproxsimClient.object.ApproxsimObject;
import ApproxsimClient.object.ApproxsimDecimal;

/**
 * PopulationFilter filters passes each object of <code>Population</code> type if the number on inhabitants is larger than the given one.
 */

public class PopulationFilter extends ApproxsimObjectFilter {
    // nr of inhabitants
    private int inhabitants;

    /**
     * Creates a new PopulationFilter.
     */
    public PopulationFilter(int nr) {
        super();
        inhabitants = nr;
    }

    /**
     * Returns true if the provided ApproxsimObject passes the filter.
     * 
     * @param sObj the object to test
     */
    public boolean pass(ApproxsimObject sObj) {
        int inhabitants = 0;
        if (sObj.getType().getName().equals("Population")) {
            // compute number of inhabitants
            for (Enumeration e = sObj.getChild("ethnicGroups").children(); e
                    .hasMoreElements();) {
                inhabitants += (int) ((ApproxsimDecimal) ((ApproxsimObject) e
                        .nextElement()).getChild("inhabitants")).getValue();
            }
            if (this.inhabitants < inhabitants) {
                return true;
            }
        }
        return false;
    }
}
