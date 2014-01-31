// 	$Id: FactoryListener.java,v 1.1 2006/04/10 15:02:37 dah Exp $
/*
 * @(#)FactoryListener.java
 */

package StratmasClient.object;

import java.util.EventListener;

/**
 * Specifies the methods called on objects listening on StratmasObjectFactories.
 *
 * @version 1, $Date: 2006/04/10 15:02:37 $
 * @author  Daniel Ahlin
*/
public interface FactoryListener extends EventListener 
{
    /**
     * Called when a new StratmasObject has been created. Note that at
     * the point of call, the object has not yet been attached to any
     * parent object.
     *
     * @param object the object created.
     */
    public void stratmasObjectCreated(StratmasObject object);

    /**
     * Called when a StratmasObject when a previously detached
     * StratmasObject is attached to a tree.
     *
     * @param object the object created.
     */
    public void stratmasObjectAttached(StratmasObject object);
}
