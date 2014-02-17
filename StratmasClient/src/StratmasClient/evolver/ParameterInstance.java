//         $Id: ParameterInstance.java,v 1.3 2005/10/28 12:15:25 dah Exp $
/*
 * @(#)ParameterInstance.java
 */

package StratmasClient.evolver;

/**
 * Represents an instance of a parameter. E. g. if f(x, y, z) were
 * sampled at (1, 2, 3) the corresponding ParameterInstances would be
 * <code>ParameterInstance(x, new Double(1))</code>,
 * <code>ParameterInstance(y, new Double(2))</code> and
 * <code>ParameterInstance(z, new Double(3))</code>.
 *
 * @version 1, $Date: 2005/10/28 12:15:25 $
 * @author  Daniel Ahlin
*/
public class ParameterInstance
{
    /**
     * The parameter.
     */
    Parameter parameter;
    /**
     * The value of this instance.
     */
    Object value;

    /**
     * Creates a new ParameterInstance with the provided parameter and value.
     * @param parameter the parameter of this instance.
     * @param value the value of this instance.
     */
    public ParameterInstance(Parameter parameter, Object value)
    {
        this.parameter = parameter;
        this.value = value;
    }
    
    /**
     * Returns the parameter of this instance
     */
    public Parameter getParameter()
    {
        return this.parameter;
    }

    /**
     * Returns the value of this instance
     */
    public Object getValue()
    {
        return this.value;
    }

    /**
     * Returns a string representation of this.
     */
    public String toString()
    {
        return getParameter().toString(this);
    }
}
