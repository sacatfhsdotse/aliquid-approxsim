// $Id: LongParameter.java,v 1.1 2005/11/16 11:42:33 dah Exp $
/*
 * @(#)LongParameter.java
 */

package ApproxsimClient.evolver;

import java.util.Comparator;

/**
 * A parameter subclass usable for parameters belonging to the real numbers.
 * 
 * @version 1, $Date: 2005/11/16 11:42:33 $
 * @author Daniel Ahlin
 */

public class LongParameter extends Parameter {
    /**
     * The comparator of this type.
     */
    final static Comparator<ParameterInstance> comparator = new Comparator<ParameterInstance>() {
        /**
         * Compares its two arguments for order.
         * 
         * @param o1 first object.
         * @param o2 second object.
         */
        public int compare(ParameterInstance o1, ParameterInstance o2) {
            long o1d = ((LongParameter) o1.getParameter())
                    .getLong((ParameterInstance) o1);
            long o2d = ((LongParameter) o2.getParameter())
                    .getLong((ParameterInstance) o2);

            return (int) (o1d - o2d);
        }
    };

    /**
     * The metric of this type.
     */
    final static Metric metric = new Metric() {
        /**
         * Returns a measure of distance between two ParameterInstances.
         * 
         * @param a the first ParameterInstance
         * @param b the second ParameterInstance
         */
        public double d(ParameterInstance a, ParameterInstance b) {
            long ad = ((LongParameter) a.getParameter()).getLong(a);
            long bd = ((LongParameter) b.getParameter()).getLong(b);

            return Math.abs(((double) (ad - bd)));
        }
    };

    /**
     * Creates a new LongParameter with the specified name.
     * 
     * @param name the name of the parameter
     */
    public LongParameter(String name) {
        super(name);
    }

    /**
     * Returns a comparator that can be used on parameter instances of this parameter type. Returning null means that this parameter is not
     * an ordinal.
     */
    public Comparator<ParameterInstance> getComparator() {
        return comparator;
    }

    /**
     * Returns an metric that can be used to obtain a distance measure between two ParameterInstances of this parameter type. Returning null
     * means that this parameter has no meningful metric.
     */
    public Metric getMetric() {
        return metric;
    }

    /**
     * If possible, returns a ParameterInstance which is a neighbour of the provided ParameterInstance along the provided gradient, else
     * null. Override this if the backing object is not a Long. This implementation always steps at least one step if gradient != 0.
     * 
     * @param instance the object to provide a neigbour for.
     * @param gradient the gradient.
     */
    public ParameterInstance getGradientNeighbour(ParameterInstance instance,
            double gradient) {
        // Force at least one step.
        if (gradient != 0.0d && Math.abs(gradient) < 1.0) {
            if (gradient > 0) {
                gradient = 1.0;
            } else {
                gradient = -1.0;
            }
        }

        return new ParameterInstance(this, new Long(
                (long) (getLong(instance) + gradient)));
    }

    /**
     * Returns the double value of an instance of this type. Override this if the backing object is not a Double.
     * 
     * @param instance the instance
     */
    long getLong(ParameterInstance instance) {
        return ((Long) instance.getValue()).longValue();
    }

    /**
     * Returns a string representation of a parameterInstance of this parameter.
     */
    public String toString(ParameterInstance parameterInstance) {
        return Long.toString(getLong(parameterInstance));
    }
}
