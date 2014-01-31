// 	$Id: ParameterInstanceSet.java,v 1.4 2005/10/28 19:20:35 dah Exp $
/*
 * @(#)ParameterInstanceSet.java
 */

package StratmasClient.evolver;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * A ParameterInstanceSet is a set of parameter instances, used to
 * contain the definition set of a sample. E. g. if f(x, y, z) were
 * sampled at (1, 2, 3) the corresponding ParameterInstanceSet would
 * contain the key->value pairs x->1, y->2 and z->3
 *
 * @version 1, $Date: 2005/10/28 19:20:35 $
 * @author  Daniel Ahlin
*/

public class ParameterInstanceSet extends Hashtable
{
    /**
     * Creates a new empty ParameterInstanceSet.
     */    
    public ParameterInstanceSet()
    {
	super();
    }

    /**
     * Adds a new parameter instance to the set (silently replacing
     * any existing parameter with the same name).
     *
     * @param parameterInstance parameterInstance to add.
     */
    public void add(ParameterInstance parameterInstance)
    {
	put(parameterInstance.getParameter().getName(), parameterInstance);
    }

    /**
     * Adds a new parameter instance to the set (silently replacing
     * any existing parameter with the same name).
     *
     * @param other parameterInstance to add.
     */
    public void addAll(ParameterInstanceSet other)
    {
	for (Enumeration e = other.getParameterInstances(); e.hasMoreElements();) {
	    ParameterInstance parameterInstance = (ParameterInstance) e.nextElement();
	    put(parameterInstance.getParameter().getName(), parameterInstance);
	}
    }

    /**
     * Returns the instance of the specified parameter or null if no
     * parameter with that name.
     *
     * @param parameter the parameter to get the instance for
     */
    public ParameterInstance getParameterInstance(Parameter parameter)
    {
	return (ParameterInstance) get(parameter.getName());
    }

    /**
     * Returns every instance
     */
    public Enumeration getParameterInstances()
    {
	return elements();
    }

    /**
     * Returns a string representation of this object.
     */
    public String toString()
    {
	StringBuffer res = new StringBuffer();

	res.append("{");
	for (Enumeration e = getParameterInstances(); e.hasMoreElements();) {
	    res.append(e.nextElement().toString() + ", ");
	}
	res.append("}");

	return res.toString();
    }
}
