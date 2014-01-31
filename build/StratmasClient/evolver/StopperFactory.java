// 	$Id: StopperFactory.java,v 1.1 2005/11/01 16:50:47 dah Exp $
/*
 * @(#)StopperFactory.java
 */

package StratmasClient.evolver;

/**
 * Provides Stopper instances
 *
 * @version 1, $Date: 2005/11/01 16:50:47 $
 * @author  Daniel Ahlin
*/
public interface StopperFactory
{
    /**
     * Returns an instance of an Stopper. 
     */
    public Stopper getStopper();
}
