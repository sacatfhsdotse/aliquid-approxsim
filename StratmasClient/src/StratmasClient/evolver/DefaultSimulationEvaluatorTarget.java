// $Id: DefaultSimulationEvaluatorTarget.java,v 1.4 2005/11/01 16:50:46 dah Exp $
/*
 * @(#)DefaultSimulationEvaluatorTarget.java
 */

package StratmasClient.evolver;

import StratmasClient.communication.Subscription;

/**
 * A default implementation for a SimulationEvaluatorTarget. Before
 * extending this class, make sure to read the documentation of
 * setSubscription() and that the constructor calls this function.
 */
abstract class DefaultSimulationEvaluatorTarget implements SimulationEvaluatorTarget
{
    /**
     * Monitor used to implement waitForUpdate
     */
    final private Object updateMonitor = new Object();

    /**
     * The subscription of the target.
     */
    Subscription subscription;

    /**
     * The stopper of this target. Default stops after ten updates.
     */
    Stopper stopper = new Stopper()
        {
            /**
             * Returns true if enough work is done.
             *
             * @param o the object that wonders if it is finished.
             */
            public boolean isFinished (Object o)
            {
                return getUpdateCount() > 9;
            }
        };

    /**
     * The number of updates made on the subscription target.
     */
    int updateCount = 0;

    /**
     * Constructs a skeleton for a SimulationEvaluatorTarget targeted
     * at the provided Parameter.
     */
    DefaultSimulationEvaluatorTarget()
    {
        setSubscription(createSubscription());
    }

    /**
     * Returns the subscription of this target.
     */
    public Subscription getSubscription()
    {
        return subscription;
    }

    /**
     * Sets the subscription of this target.
     *
     * @param subscription the subscription to use.
     */
    private void setSubscription(Subscription subscription)
    {
        this.subscription = subscription;
    }

    /**
     * Returns the number of times the target has been updated
     */
    public int getUpdateCount()
    {
        return this.updateCount;
    }

    /**
     * Increases the update count
     */
    public void increaseUpdateCount()
    {
        this.updateCount++;
    }

    /**
     * Creates the subscription of this target. This subscription is
     * required to, in some way, make sure update() is called whenever
     * the subscription is updated.
     */
    abstract Subscription createSubscription();

    /**
     * Creates the ParameterInstance acting as evaluation in the
     * Evaluations created by the SimulationEvaluator.
     *
     * @param subscription
     */
    abstract public ParameterInstance createEvaluation();

    /**
     * Sets the stopper of this target
     *
     * @param stopper the stopper
     */
    public void setStopper(Stopper stopper)
    {
        this.stopper = stopper;
    }

    /**
     * Returns the stopper of this target
     */
    public Stopper getStopper()
    {
        return this.stopper;
    }
}

