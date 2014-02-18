//         $Id: DefaultEvaluator.java,v 1.1 2005/10/28 12:14:33 dah Exp $
/*
 * @(#)DefaultEvaluator.java
 */

package StratmasClient.evolver;

import javax.swing.event.EventListenerList;

/**
 * Provides a default implementation for some common tasks in
 * Evaluator.
 *
 * @version 1, $Date: 2005/10/28 12:14:33 $
 * @author  Daniel Ahlin
*/
public abstract class DefaultEvaluator implements Evaluator
{
    /**
     * The listeners of this evaluator.
     */
    EventListenerList eventListenerList = new EventListenerList();

    /**
     * Indicates whether the evaluator has finished.
     */
    boolean isFinished = false;

    /**
     * Creates a new DefaultEvaluator.
     */
    public DefaultEvaluator()
    {        
    }

    /**
     * Returns true if the evaluation has finished.
     */
    public boolean isFinished()
    {
        return isFinished;
    }

    /**
     * Indicates that the evaluator has finished.
     */
    void finished()
    {
        this.isFinished = true;
        fireFinished();
    }

    /**
     * Returns a list of the listeners of this object.
     */
    protected EventListenerList getEventListenerList()
    {
        return this.eventListenerList;
    }

    /**
     * Register a new EvaluatorListener on this evaluator. 
     *
     * @param listener the listener to add.
     */
    public void addEventListener(EvaluatorEventListener listener)
    {
        this.getEventListenerList().add(EvaluatorEventListener.class, listener);
    }

    /**
     * Removes a EvaluatorListener from this evaluator. 
     *
     * @param listener the listener to remove.
     */
    public void removeEventListener(EvaluatorEventListener listener)
    {
        this.getEventListenerList().remove(EvaluatorEventListener.class, listener);
    }

    /**
     * Notifies listeners that the evaluator is finished with the
     * evaluation.
     */
    public void fireFinished()
    {
        EvaluatorEvent event = new EvaluatorEvent(this);
        
        // Guaranteed to return a non-null array
        Object[] listeners = getEventListenerList().getListenerList();
        
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            ((EvaluatorEventListener) listeners[i + 1]).finished(event);
        }
    }

    /**
     * Notifies listeners that an error has occured during the
     * evaluation.
     *
     * @param errorMessage a string describing the cause of the error.
     */
    public void fireError(String errorMessage)
    {
        EvaluatorEvent event = new EvaluatorEvent(this);
        
        // Guaranteed to return a non-null array
        Object[] listeners = getEventListenerList().getListenerList();
        
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            ((EvaluatorEventListener) listeners[i + 1]).error(event, 
                                                              errorMessage);
        }
    }

    /**
     * Notifies listeners that a new preliminary evaluation is availiable.
     *
     * @param evaluation the new evaluation.
     */
    public void fireNewEvaluation(Evaluation evaluation)
    {
        EvaluatorEvent event = new EvaluatorEvent(this);
        
        // Guaranteed to return a non-null array
        Object[] listeners = getEventListenerList().getListenerList();
        
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            ((EvaluatorEventListener) listeners[i + 1]).newPreliminaryEvaluation(event, 
                                                                                 evaluation);
        }
    }
}
