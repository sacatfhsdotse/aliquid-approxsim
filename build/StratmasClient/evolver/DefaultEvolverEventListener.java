// 	$Id: DefaultEvolverEventListener.java,v 1.4 2005/11/01 09:36:13 dah Exp $
/*
 * @(#)DefaultEvolverEventListener.java
 */

package StratmasClient.evolver;

/**
 * Convinience EvolverEventListener that does ignores most things.
 *
 * @version 1, $Date: 2005/11/01 09:36:13 $
 * @author  Daniel Ahlin
*/
public abstract class DefaultEvolverEventListener implements EvolverEventListener 
{
    /**
     * Empty constructor.
     */
    public DefaultEvolverEventListener()
    {
    }

    /**
     * Called when the evolver starts, stops, continues, pauses or
     * aborts the evolving.
     *
     * @param event the event.
     */
    public void runningStateChanged(EvolverEvent event) 
    {
    }

    /**
     * Called when the evolver adds a new evaluation.
     *
     * @param event the event.
     * @param newEvaluation the new evaluation
     */
    public void newEvaluation(EvolverEvent event, Evaluation newEvaluation)
    {
    }

    /**
     * Called when the evolver starts a new Evaluator.
     *
     * @param event the event.
     * @param newEvaluator the new evaluation
     */
    public void newEvaluator(EvolverEvent event, Evaluator newEvaluator)
    {
    }

    /**
     * Called when the evolver has something noteworthy to report.
     *
     * @param event the event.
     * @param information the information
     */
    public void information(EvolverEvent event, String information)
    {
    }
}
