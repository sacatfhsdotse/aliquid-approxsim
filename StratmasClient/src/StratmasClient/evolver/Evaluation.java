// $Id: Evaluation.java,v 1.5 2005/10/28 12:20:55 dah Exp $
/*
 * @(#)Evaluation.java
 */

package StratmasClient.evolver;

/**
 * An evaluation of a ParameterInstanceSet
 * 
 * @version 1, $Date: 2005/10/28 12:20:55 $
 * @author Daniel Ahlin
 */
public class Evaluation {
    /**
     * The ParameterInstanceSet evaluated.
     */
    ParameterInstanceSet parameterInstanceSet;

    /**
     * The evaluation.
     */
    ParameterInstance evaluation;

    /**
     * Creates a new Evaluation
     * 
     * @param parameterInstanceSet the parameters resulting in this evaluation
     * @param evaluation the evaluation.
     */
    public Evaluation(ParameterInstanceSet parameterInstanceSet,
            ParameterInstance evaluation) {
        this.parameterInstanceSet = parameterInstanceSet;
        this.evaluation = evaluation;
    }

    /**
     * Returns the parameter instance set resulting in this evaluation.
     */
    public ParameterInstanceSet getParameterInstanceSet() {
        return this.parameterInstanceSet;
    }

    /**
     * Returns the evaluation.
     */
    public ParameterInstance getEvaluation() {
        return evaluation;
    }
}
