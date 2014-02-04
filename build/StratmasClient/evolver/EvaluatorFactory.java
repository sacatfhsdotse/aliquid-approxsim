//         $Id: EvaluatorFactory.java,v 1.2 2005/10/28 19:20:30 dah Exp $
/*
 * @(#)EvaluatorFactory.java
 */

package StratmasClient.evolver;

/**
 * Provides an Evolver with Evaluator instances
 *
 * @version 1, $Date: 2005/10/28 19:20:30 $
 * @author  Daniel Ahlin
*/
public interface EvaluatorFactory
{
    /**
     * Returns an instance of an Evaluator. 
     */
    public Evaluator getEvaluator();
}
