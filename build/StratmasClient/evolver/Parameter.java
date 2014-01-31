// 	$Id: Parameter.java,v 1.4 2005/10/28 12:15:25 dah Exp $
/*
 * @(#)Parameter.java
 */

package StratmasClient.evolver;

import java.util.Comparator;

/**
 * Represents a parameter. E. g. if f(x, y, z) is used, the
 * corresponding Parameters would be:
 * <code>Parameter("x")</code>, 
 * <code>Parameter("y")</code> and
 * <code>Parameter("z")</code>.
 *
 * The class is abstract to force people to think before not
 * overriding the default implementations of getComparator and
 * getMetric.
 *
 * @version 1, $Date: 2005/10/28 12:15:25 $
 * @author  Daniel Ahlin
*/
public abstract class Parameter implements ParameterInstanceFactory
{
    /**
     * The name of the parameter.
     */
    String name;

    /**
     * Creates a new Parameter with the specified name.
     * 
     * @param name the name of the parameter
     */
    public Parameter(String name)
    {
	setName(name);
    }

    /**
     * Returns the name of this parameter.
     */
    public String getName()
    {
	return name;
    }

    /**
     * Sets the name of this parameter.
     */
    protected void setName(String name)
    {
	this.name = name;
    }

    /**
     * Returns a string representation of this object.
     */
    public String toString()
    {
	return getName();
    }

    /**
     * Returns a string representation of a parameterInstance of this parameter.
     */
    public String toString(ParameterInstance parameterInstance)
    {
	return parameterInstance.getValue().toString();
    }

    /**
     * Returns a comparator that can be used on parameter instances of
     * this parameter type. Returning null means that this parameter
     * is not an ordinal.
     */
    public Comparator getComparator()
    {
	return null;
    }

    /**
     * Returns an metric that can be used to obtain a distance measure
     * between two ParameterInstances of this parameter
     * type. Returning null means that this parameter has no meningful
     * metric.
     */
     public Metric getMetric()
     {
 	return null;
     }

    /**
     * Returns a ParameterInstance of this Parameter given the
     * provided Object. Returns null if this Parameter don't support
     * the provided object.
     * 
     * @param object the object to wrap in an instance.
     */ 
    public ParameterInstance getParameterInstance(Object object)
    {
	return new ParameterInstance(this, object);
    }

    /**
     * If possible, returns a ParameterInstance which is a neighbour
     * of the provided ParameterInstance along the provided gradient, else null
     * 
     * @param instance the object to provide a neigbour for.
     * @param gradient the gradient.
     */ 
    public abstract ParameterInstance getGradientNeighbour(ParameterInstance instance, 
							   double gradient);
}

