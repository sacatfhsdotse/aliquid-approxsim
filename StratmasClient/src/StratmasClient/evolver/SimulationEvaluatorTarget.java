// $Id: SimulationEvaluatorTarget.java,v 1.4 2005/11/01 16:50:47 dah Exp $
/*
 * @(#)SimulationEvaluatorTarget.java
 */
package StratmasClient.evolver;

import StratmasClient.communication.Subscription;

/**
 * Interface used to abstract away subscription- and Evaluation-
 * details from SimulationEvaluator.
 */
interface SimulationEvaluatorTarget
{
    /**
     * Returns a subscription for this target
     */
    public Subscription getSubscription();

    /**
     * Creates the ParameterInstance acting as evaluation in the
     * Evaluations created by the SimulationEvaluator.
     * @param subscription
     */
    public ParameterInstance createEvaluation(); 

    /**
     * Increases the update count
     */
    public void increaseUpdateCount();

    /**
     * Returns the update count
     */
    public int getUpdateCount();

    /**
     * Sets the stopper of this target
     *
     * @param stopper the stopper
     */
    public void setStopper(Stopper stopper);

    /**
     * Returns the stopper of this target
     */
    public Stopper getStopper();
}

