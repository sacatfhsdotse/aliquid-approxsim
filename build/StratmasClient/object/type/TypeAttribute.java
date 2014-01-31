// 	$Id: TypeAttribute.java,v 1.1 2006/03/22 14:30:52 dah Exp $
/*
 * @(#)TypeAttribute.java
 */

package StratmasClient.object.type;

import org.apache.xerces.xs.*;

/**
 * An object representing an attribute in the Taclan type 
 * language.
 *
 * @version 1, $Date: 2006/03/22 14:30:52 $
 * @author  Daniel Ahlin
*/

public class TypeAttribute
{
    /**
     * The type of this declaration.
     */
    Type type;

    /**
     * The name of this declaration.
     */
    String name;

    /**
     * Creates a new declaration.
     *
     * @param type the type of the declaration.
     * @param name the name of the declaration.
     */
    public TypeAttribute(Type type, String name)
    {
	this.type = type;
	this.name = name;
    }



    /**
     * Returns a string representation of this object.
     */
    public String toString() 
    {
	return type.getName() + "\t\t" + getName() + " (Attribute)";
    }

    /** 
     * Returns the name of this declaration.
     */
    public String getName()
    {
	return name;
    }
}

