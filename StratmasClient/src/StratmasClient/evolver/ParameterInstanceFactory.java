// $Id: ParameterInstanceFactory.java,v 1.1 2005/10/28 12:14:34 dah Exp $
/*
 * @(#)ParameterInstancesFactory.java
 */

package ApproxsimClient.evolver;

/**
 * Maps Objects to suitable ParameterInstance instances.
 * 
 * @version 1, $Date: 2005/10/28 12:14:34 $
 * @author Daniel Ahlin
 */
public interface ParameterInstanceFactory {
    /**
     * Returns a ParameterInstance suitable for the provided object (or null if none found).
     * 
     * @param object the object to map
     */
    public ParameterInstance getParameterInstance(Object object);
}
