package ApproxsimClient.filter;

import ApproxsimClient.object.ApproxsimObject;

/**
 * ActivityResourceFilter passes objects of <code>Order</code> type depending on the type of their resources.
 * 
 * @author Amir Filipovic
 */
public class ActivityResourceFilter extends ApproxsimObjectFilter {
    /**
     * The filter for the resource.
     */
    private ApproxsimObjectFilter resourceFilter;

    /**
     * Creates a new ActivityResourceFilter.
     */
    public ActivityResourceFilter(ApproxsimObjectFilter resourceFilter) {
        super();
        this.resourceFilter = resourceFilter;
    }

    /**
     * Returns true if the provided ApproxsimObject passes the filter.
     * 
     * @param sObj the object to test
     */
    public boolean pass(ApproxsimObject sObj) {
        // only activities can pass
        if (sObj.getType().canSubstitute("Order")) {
            try {
                ApproxsimObject resource = (ApproxsimObject) sObj.getParent()
                        .getParent();
                return resourceFilter.pass(resource);
            } catch (NullPointerException exc) {
                return applyInverted(false);
            }
        }
        return applyInverted(false);
    }
}
