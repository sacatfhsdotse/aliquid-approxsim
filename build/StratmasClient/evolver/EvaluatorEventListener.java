//         $Id: EvaluatorEventListener.java,v 1.1 2005/10/28 12:14:33 dah Exp $
/*
 * @(#)EvaluatorEventListener.java
 */

package StratmasClient.evolver;

import java.util.EventListener;

/**
 * Specifies the methods called on objects listening to EvaluatorEvents
 *
 * @version 1, $Date: 2005/10/28 12:14:33 $
 * @author  Daniel Ahlin
*/
public interface EvaluatorEventListener extends EventListener 
{
    /**
     * Called when evaluator is finished with the
     * evaluation.
     *
     * @param event the event.
     */
    public void finished(EvaluatorEvent event);

    /**
     * Called when evaluator has a new preliminart evaluation.
     *
     * @param evaluation the preliminary evaluation.
     * @param event the event.
     */
    public void newPreliminaryEvaluation(EvaluatorEvent event, Evaluation evaluation);

    /**
     * Called when the an error has occured during the
     * evaluation.
     *
     * @param event the event.
     * @param errorMessage a string describing the error.
     */
    public void error(EvaluatorEvent event, String errorMessage);
}
