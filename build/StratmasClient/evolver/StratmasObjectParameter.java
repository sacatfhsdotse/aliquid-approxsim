// 	$Id: StratmasObjectParameter.java,v 1.3 2006/03/22 14:30:50 dah Exp $
/*
 * @(#)StratmasObjectParameter.java
 */

package StratmasClient.evolver;

import StratmasClient.object.primitive.Reference;
import StratmasClient.object.StratmasObject;

import StratmasClient.filter.StratmasObjectAdapter;

/**
 * Represents a interface for parameters backed by StratmasObjects.
 *
 * @version 1, $Date: 2006/03/22 14:30:50 $
 * @author  Daniel Ahlin
*/
public interface StratmasObjectParameter extends StratmasObjectAdapter
{
    /**
     * Returns the reference of the StratmasObject backing this parameter.
     */ 
    public Reference getReference();

    /**
     * Returns the StratmasObject backing this parameter.
     */
    public StratmasObject getStratmasObject();
}

