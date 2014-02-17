package StratmasClient.filter;

import StratmasClient.object.StratmasObject;

/**
 * ActivityResourceFilter passes objects of <code>Order</code> type depending on the type of
 * their resources. 
 *
 * @author Amir Filipovic
 */
public class ActivityResourceFilter extends StratmasObjectFilter {
    /**
     * The filter for the resource.
     */
    private StratmasObjectFilter resourceFilter;
    
    /**
     * Creates a new ActivityResourceFilter.
     */
    public ActivityResourceFilter(StratmasObjectFilter resourceFilter)  {        
        super();
        this.resourceFilter = resourceFilter;
    }
    
    /**
     * Returns true if the provided StratmasObject passes the filter.
     *
     * @param sObj the object to test
     */
    public boolean pass(StratmasObject sObj) {
        // only activities can pass
        if (sObj.getType().canSubstitute("Order")) {
            try {
                StratmasObject resource = (StratmasObject)sObj.getParent().getParent();
                return resourceFilter.pass(resource);
            }
            catch (NullPointerException exc) {
                return applyInverted(false);
            }
        }
        return applyInverted(false);
    }
}
