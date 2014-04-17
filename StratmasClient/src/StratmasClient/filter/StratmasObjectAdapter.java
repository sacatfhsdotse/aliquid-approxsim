// $Id: StratmasObjectAdapter.java,v 1.2 2006/03/22 14:30:50 dah Exp $
/*
 * @(#)StratmasObjectAdapter.java
 */

package StratmasClient.filter;

import StratmasClient.object.StratmasObject;

/**
 * Interface specifying what filter needs to filter adapters of StratmasObjects transparantly
 * 
 * @version 1, $Date: 2006/03/22 14:30:50 $
 * @author Daniel Ahlin
 */

public interface StratmasObjectAdapter {
    /**
     * Returns the StratmasObject this adapter adapts.
     */
    public StratmasObject getStratmasObject();
}
