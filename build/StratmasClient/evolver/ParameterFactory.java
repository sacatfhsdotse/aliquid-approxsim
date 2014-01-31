// 	$Id: ParameterFactory.java,v 1.1 2005/10/28 12:14:34 dah Exp $
/*
 * @(#)ParametersFactory.java
 */

package StratmasClient.evolver;

/**
 * Maps Objects to suitable Parameter instances.
 *
 * @version 1, $Date: 2005/10/28 12:14:34 $
 * @author  Daniel Ahlin
*/
public interface ParameterFactory
{
    /**
     * Returns an instance of Parameter suitable for the provided
     * object (or null if none found).
     *
     * @param object the object to map
     */
    public Parameter getParameter(Object object);
}
