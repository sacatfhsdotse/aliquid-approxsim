// 	$Id: Evaluator.java,v 1.4 2005/10/28 18:47:20 dah Exp $
/*
 * @(#)Evaluator.java
 */

package StratmasClient.evolver;

/**
 * An Evaluator provides performance measure for a
 * ParameterInstanceSet. E. g. given a set of parameter instances {x,
 * y, x, ...} the evaluator returns an evaluation of the fitness of
 * that particalar set of parameter instances.
 *
 * @version 1, $Date: 2005/10/28 18:47:20 $
 * @author  Daniel Ahlin
*/
public interface Evaluator
{
    /**
     * Returns a measure for the ParameterInstanceSet 
     *
     * @param set the parameters to measure.
     *
     * @throws EvaluatorException if an error occured while evaluating.
     */
    public Evaluation evaluate(ParameterInstanceSet set) throws EvaluatorException;


    /**
     * Register a new EvaluatorListener on this evaluator. 
     *
     * @param listener the listener to add.
     */
    public void addEventListener(EvaluatorEventListener listener);

    /**
     * Removes a EvaluatorListener from this evaluator. 
     *
     * @param listener the listener to remove.
     */
    public void removeEventListener(EvaluatorEventListener listener);

    /**
     * Returns true if the evaluation has finished.
     */
    public boolean isFinished();

    /**
     * Returns the preliminary value of the evaluation if any, else null.
     */
    public Evaluation getPreliminaryEvaluation();
}
