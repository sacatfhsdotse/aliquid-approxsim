// 	$Id: Metric.java,v 1.3 2005/10/28 12:20:55 dah Exp $
/*
 * @(#)Metric.java
 */

package StratmasClient.evolver;

/**
 * Represents the capabilities of a metric as used in the evolver
 *
 * @version 1, $Date: 2005/10/28 12:20:55 $
 * @author  Daniel Ahlin
*/
public interface Metric
{
    /**
     * Returns a measure of distance between two ParameterInstances.
     *
     * @param a the first ParameterInstance
     * @param b the second ParameterInstance
     */
    public double d(ParameterInstance a, ParameterInstance b);
}
