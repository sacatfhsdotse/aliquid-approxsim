//         $Id: PassFilter.java,v 1.3 2006/03/22 14:30:50 dah Exp $
/*
 * @(#)Passfilter.java
 */

package StratmasClient.filter;

import StratmasClient.object.StratmasObject;

/**
 * PassFilter filters passes everything, unless inverted.
 *
 * @version 1, $Date: 2006/03/22 14:30:50 $
 * @author  Daniel Ahlin
*/

public class PassFilter extends StratmasObjectFilter
{
    /**
     * Creates a new PassFilter allowing everything
     */
    public PassFilter()
    {        
        super();
    }

    /**
     * Returns true if the provided StratmasObject passes the filter.
     *
     * @param sObj the object to test
     */
    public boolean pass(StratmasObject sObj)
    {
        return applyInverted(true);
    }
}
