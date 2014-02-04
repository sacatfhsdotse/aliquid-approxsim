package StratmasClient.filter;

import java.util.Enumeration;

import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasDecimal;

/**
 * PopulationFilter filters passes each object of <code>Population</code> type if the number on inhabitants is
 * larger than the given one.
 *
 */

public class PopulationFilter extends StratmasObjectFilter
{
    // nr of inhabitants
    private int inhabitants;
    
    /**
     * Creates a new PopulationFilter.
     */
    public PopulationFilter(int nr)
    {        
        super();
        inhabitants = nr;
    }
    
    /**
     * Returns true if the provided StratmasObject passes the filter.
     *
     * @param sObj the object to test
     */
    public boolean pass(StratmasObject sObj)
    {
        int inhabitants = 0;
        if (sObj.getType().getName().equals("Population")) {
            // compute number of inhabitants
            for (Enumeration e = sObj.getChild("ethnicGroups").children(); e.hasMoreElements();) {
                inhabitants += (int)((StratmasDecimal)((StratmasObject)e.nextElement()).getChild("inhabitants")).getValue();
            }
            if (this.inhabitants < inhabitants) {
                return true;
            }
        }
        return false;
    }
}
