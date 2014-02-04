//         $Id: SamplerFactory.java,v 1.1 2005/11/01 16:50:47 dah Exp $
/*
 * @(#)SamplerFactory.java
 */

package StratmasClient.evolver;

/**
 * Provides Sampler instances
 *
 * @version 1, $Date: 2005/11/01 16:50:47 $
 * @author  Daniel Ahlin
*/
public interface SamplerFactory
{
    /**
     * Returns an instance of an Sampler. 
     */
    public Sampler getSampler();
}
