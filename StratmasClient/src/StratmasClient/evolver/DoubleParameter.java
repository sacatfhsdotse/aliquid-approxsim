//         $Id: DoubleParameter.java,v 1.3 2005/10/28 12:15:24 dah Exp $
/*
 * @(#)DoubleParameter.java
 */

package StratmasClient.evolver;

import java.util.Comparator;

/**
 * A parameter subclass usable for parameters belonging to the real
 * numbers.
 * 
 * @version 1, $Date: 2005/10/28 12:15:24 $
 * @author  Daniel Ahlin
*/

public class DoubleParameter extends Parameter
{
    /**
     * The comparator of this type.
     */
    final static Comparator<ParameterInstance> comparator = new Comparator<ParameterInstance>()
        {
            /**
             * Compares its two arguments for order.
             *
             * @param o1 first object.
             * @param o2 second object.
             */
            public int compare(ParameterInstance o1, ParameterInstance o2)
            {
                double o1d = 
                    ((DoubleParameter) o1.getParameter()).getDouble(o1);
                double o2d = 
                    ((DoubleParameter) o2.getParameter()).getDouble(o2);

                return Double.compare(o1d, o2d);
            }
        };

    /**
     * The metric of this type.
     */
     final static Metric metric = new Metric()
         {
             /**
              * Returns a measure of distance between two ParameterInstances.
              *
              * @param a the first ParameterInstance
              * @param b the second ParameterInstance
              */
             public double d(ParameterInstance a, ParameterInstance b)
             {
                double ad = ((DoubleParameter) a.getParameter()).getDouble(a);
                double bd = ((DoubleParameter) b.getParameter()).getDouble(b);

                 return Math.abs(ad - bd);
             }
         };

    /**
     * Creates a new DoubleParameter with the specified name.
     * 
     * @param name the name of the parameter
     */
    public DoubleParameter(String name)
    {
        super(name);
    }

    /**
     * Returns a comparator that can be used on parameter instances of
     * this parameter type. Returning null means that this parameter
     * is not an ordinal.
     */
    public Comparator<ParameterInstance> getComparator()
    {
        return comparator;
    }

    /**
     * Returns an metric that can be used to obtain a distance measure
     * between two ParameterInstances of this parameter
     * type. Returning null means that this parameter has no meningful
     * metric.
     */
    public Metric getMetric()
    {
         return metric;
    }

    /**
     * If possible, returns a ParameterInstance which is a neighbour
     * of the provided ParameterInstance along the provided gradient,
     * else null. Override this if the backing object is not a Double.
     * 
     * @param instance the object to provide a neigbour for.
     * @param gradient the gradient.
     */ 
    public ParameterInstance getGradientNeighbour(ParameterInstance instance, 
                                                  double gradient)
    {
        return new ParameterInstance(this,
                                     new Double(getDouble(instance) + gradient));
    }

    /**
     * Returns the double value of an instance of this type. Override
     * this if the backing object is not a Double.
     *
     * @param instance the instance
     */
    double getDouble(ParameterInstance instance)
    {
        return ((Double) instance.getValue()).doubleValue();
    }

    /**
     * Returns a string representation of a parameterInstance of this parameter.
     */
    public String toString(ParameterInstance parameterInstance)
    {
        return Double.toString(getDouble(parameterInstance));
    }
}
