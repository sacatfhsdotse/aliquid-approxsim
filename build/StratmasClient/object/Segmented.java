// 	$Id: Segmented.java,v 1.3 2006/05/11 16:43:04 alexius Exp $
/*
 * @(#)SimpleShape.java
 */

package StratmasClient.object;

import StratmasClient.object.type.Type;

import java.util.Enumeration;

/**
 * A simpleshape defines a set of two dimensional points by enclosing
 * it in curves.
 *
 * @version 1, $Date: 2006/05/11 16:43:04 $
 * @author  Daniel Ahlin
*/

public abstract class Segmented extends SimpleShape
{    
    /**
     * Creates a identified Segmented.
     *
     * @param identifier the identifier of the segmented shape.
     * @param type the type of this segmented.
     */
    Segmented(String identifier, Type type)
    {
	super(identifier, type);
    }

    /**
     * Returns the curves making up this segmented.
     */
    public Enumeration getCurves()
    {	
	// We know that Segmented is expected to contain a
	// StratmasList as its only direct child.
	StratmasList curves = (StratmasList) this.getChild("curves");
	return curves.children();
    }
}
