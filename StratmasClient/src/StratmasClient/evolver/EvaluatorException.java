//         $Id: EvaluatorException.java,v 1.1 2005/10/28 18:47:20 dah Exp $
/*
 * @(#)EvaluatorException.java
 */

package StratmasClient.evolver;

/**
 * An object representing any error in a evaluation run.
 *
 * @version 1, $Date: 2005/10/28 18:47:20 $
 * @author  Daniel Ahlin
*/

public class EvaluatorException extends Exception
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -758637620484065503L;

	/**
     * A string describing the exception.
     */
    String description;

    /**
     * The evaluator causing the the exception.
     */
    Evaluator evaluator;

    /**
     * Creates a new EvaluatorException.
     * @param description a string describing the exception.
     */
    public EvaluatorException(Evaluator evaluator, String description)
    {
        this.evaluator = evaluator;
        this.description = description;
    }

    /**
     * Returns a message detailing the cause of the exception.
     */
    public String getMessage()
    {
        return description;
    }

    /**
     * Returns a string representation of this object.
     */
    public String toString()
    {
        return getMessage();
    }

    /**
     * Returns the evaluator causing the error.
     */
    public Evaluator getEvaluator()
    {
        return this.evaluator;
    }
}
