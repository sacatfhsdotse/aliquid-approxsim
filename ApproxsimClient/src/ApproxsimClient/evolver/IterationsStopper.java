// $Id: IterationsStopper.java,v 1.1 2005/11/01 16:50:47 dah Exp $
/*
 * @(#)IterationsStopper.java
 */

package ApproxsimClient.evolver;

/**
 * A stopper encapsulates the stop criterion for evolvers and some evaluators.
 * 
 * @version 1, $Date: 2005/11/01 16:50:47 $
 * @author Daniel Ahlin
 */
abstract public class IterationsStopper implements Stopper {
    /**
     * The minimum number of iterations
     */
    int minIterations;

    /**
     * Creates a new IterationsStopper that finishes when the at least the provided number of steps has been reached.
     * 
     * @param minIterations
     */
    IterationsStopper(int minIterations) {
        this.minIterations = minIterations;
    }

    /**
     * Returns the minimum number of iterations.
     */
    public int getMinIterations() {
        return this.minIterations;
    }

    /**
     * Returns true if enough work is done.
     * 
     * @param o the object that wonders if it is finished.
     */
    public boolean isFinished(Object o) {
        return getIterations(o) >= getMinIterations();
    }

    /**
     * Returns the number of iterations for the object o
     * 
     * @param o the object to get iterations from.
     */
    public abstract int getIterations(Object o);
}
