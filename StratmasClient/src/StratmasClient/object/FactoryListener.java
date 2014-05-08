// $Id: FactoryListener.java,v 1.1 2006/04/10 15:02:37 dah Exp $
/*
 * @(#)FactoryListener.java
 */

package ApproxsimClient.object;

import java.util.EventListener;

/**
 * Specifies the methods called on objects listening on ApproxsimObjectFactories.
 * 
 * @version 1, $Date: 2006/04/10 15:02:37 $
 * @author Daniel Ahlin
 */
public interface FactoryListener extends EventListener {
    /**
     * Called when a new ApproxsimObject has been created. Note that at the point of call, the object has not yet been attached to any parent
     * object.
     * 
     * @param object the object created.
     */
    public void approxsimObjectCreated(ApproxsimObject object);

    /**
     * Called when a ApproxsimObject when a previously detached ApproxsimObject is attached to a tree.
     * 
     * @param object the object created.
     */
    public void approxsimObjectAttached(ApproxsimObject object);
}
