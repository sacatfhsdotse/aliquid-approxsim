// $Id: ApproxsimObjectAdapter.java,v 1.2 2006/03/22 14:30:50 dah Exp $
/*
 * @(#)ApproxsimObjectAdapter.java
 */

package ApproxsimClient.filter;

import ApproxsimClient.object.ApproxsimObject;

/**
 * Interface specifying what filter needs to filter adapters of ApproxsimObjects transparantly
 * 
 * @version 1, $Date: 2006/03/22 14:30:50 $
 * @author Daniel Ahlin
 */

public interface ApproxsimObjectAdapter {
    /**
     * Returns the ApproxsimObject this adapter adapts.
     */
    public ApproxsimObject getApproxsimObject();
}
