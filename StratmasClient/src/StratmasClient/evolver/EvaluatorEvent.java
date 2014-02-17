//         $Id: EvaluatorEvent.java,v 1.1 2005/10/28 12:14:33 dah Exp $
/*
 * @(#)EvaluatorEvent.java
 */

package StratmasClient.evolver;

import java.util.EventObject;

/**
 * EvaluatorEvent is an event representing a noteworthy state change in
 * an evaluator.
 *
 * @version 1, $Date: 2005/10/28 12:14:33 $
 * @author  Daniel Ahlin
*/
public class EvaluatorEvent extends EventObject
{
    /**
     * Constructs a new EvaluatorEvent representing the specified type.
     *
     * @param evaluator the evaluator causing the event.
     * @param type the type of the event.
     */
    EvaluatorEvent(Evaluator evaluator)
    {
        super(evaluator);
    }

    /**
     * Convinience method returning the evaluator causing this event.
     */
    public Evaluator getEvaluator()
    {
        return (Evaluator) getSource();
    }
}
