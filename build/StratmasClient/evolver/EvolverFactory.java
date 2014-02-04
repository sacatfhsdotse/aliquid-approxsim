//         $Id: EvolverFactory.java,v 1.1 2005/10/28 12:14:34 dah Exp $
/*
 * @(#)EvolverFactory.java
 */

package StratmasClient.evolver;

/**
 * Provides Evolver instances
 *
 * @version 1, $Date: 2005/10/28 12:14:34 $
 * @author  Daniel Ahlin
*/
public interface EvolverFactory
{
    /**
     * Returns an instance of an Evolver. 
     */
    public Evolver getEvolver();
}
