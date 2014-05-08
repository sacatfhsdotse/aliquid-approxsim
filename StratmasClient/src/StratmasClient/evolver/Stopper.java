// $Id: Stopper.java,v 1.1 2005/11/01 16:50:47 dah Exp $
/*
 * @(#)Stopper.java
 */

package ApproxsimClient.evolver;

/**
 * A stopper encapsulates the stop criterion for evolvers and some evaluators.
 * 
 * @version 1, $Date: 2005/11/01 16:50:47 $
 * @author Daniel Ahlin
 */
public interface Stopper {
    /**
     * Returns true if enough work is done.
     * 
     * @param o the object that wonders if it is finished.
     */
    public boolean isFinished(Object o);
}
