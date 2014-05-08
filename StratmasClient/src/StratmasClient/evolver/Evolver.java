// $Id: Evolver.java,v 1.9 2005/11/02 22:18:47 dah Exp $
/*
 * @(#)Evolver.java
 */

package StratmasClient.evolver;

import java.util.Vector;
import java.util.Enumeration;

import javax.swing.event.EventListenerList;

/**
 * Evolver is a container class for all the objects needed to perform an optimization.
 * 
 * @version 1, $Date: 2005/11/02 22:18:47 $
 * @author Daniel Ahlin
 */
public class Evolver extends Thread {
    /**
     * The initial settings.
     */
    ParameterInstanceSet initialSettings;

    /**
     * The the Parameter of the evaluation target.
     */
    Parameter evaluationParameter;

    /**
     * The evaluatorFactory provides evaluators which in turn provides performance measures for ParameterInstanceSets
     */
    EvaluatorFactory evaluatorFactory;

    /**
     * The sampler (provides new ParameterInstanceSets to sample)
     */
    Sampler sampler;

    /**
     * The stopper (provides information about when we are done)
     */
    Stopper stopper;

    /**
     * The evaluations done so far.
     */
    Vector evaluations = new Vector();

    /**
     * The listeners of this evolver.
     */
    EventListenerList eventListenerList = new EventListenerList();

    /**
     * Indicates whether the Evolver is (or about to be) paused.
     */
    boolean pause = false;

    /**
     * The lock that pause sleeps on.
     */
    Object pauseLock = new Object();

    /**
     * Indicates whether the Evolver is (or about to be) aborted.
     */
    boolean abort = false;

    /**
     * Creates a new evolver using the provided parameters.
     * 
     * @param initialSettings the initial settings of the Evolver (the evolver also uses this to find out all parameters to evolve)
     * @param evaluationParameter the Parameter of the evaluation target.
     * @param evaluatorFactory provides evaluator instances.
     * @param sampler class providing new samples (based on existing ones).
     * @param stopper class providing a stop criterion.
     */
    public Evolver(ParameterInstanceSet initialSettings,
            Parameter evaluationParameter, EvaluatorFactory evaluatorFactory,
            Sampler sampler, Stopper stopper) {
        this.evaluationParameter = evaluationParameter;
        this.initialSettings = initialSettings;
        this.evaluatorFactory = evaluatorFactory;
        this.sampler = sampler;
        setStopper(stopper);
    }

    /**
     * Returns the evaluated parameter settings.
     */
    public Vector getEvaluations() {
        return (Vector) evaluations.clone();
    }

    /**
     * Returns the parameter type of the evaluation target.
     */
    public Parameter getEvaluationParameter() {
        return this.evaluationParameter;
    }

    /**
     * Returns the the parameters this Evolver evolves.
     */
    public Vector getParameters() {
        Vector res = new Vector();

        for (Enumeration e = initialSettings.getParameterInstances(); e
                .hasMoreElements();) {
            res.add(((ParameterInstance) e.nextElement()).getParameter());
        }

        return res;
    }

    /**
     * Steps forth until either stopped or the some internal stopCondition is reached.
     */
    public void run() {
        fireRunningStateChanged();
        while (true) {
            // What to do this round:
            if (isInterrupted() || isAborted()) {
                // If not a requested abort, this indicates an error.
                if (!isAborted()) {
                    throw new AssertionError(
                            "Evolver thread unexpectedly interrupted.");
                } else {
                    break;
                }
            } else if (isPaused()) {
                // Wait until unpaused, unless an InterruptedException
                // occurs, handle that by running through loop again.
                try {
                    fireRunningStateChanged();
                    synchronized (getPauseLock()) {
                        getPauseLock().wait();
                    }
                    fireRunningStateChanged();
                } catch (InterruptedException e) {}
            } else if (isFinished()) {
                // No more work to do. Exit nicely.
                break;
            } else {
                fireInformation("Step: " + getEvaluations().size());
                Evaluator evaluator = getEvaluator(8);
                if (evaluator == null) {
                    fireInformation("Timed out waiting for Evaluator, aborting");
                    abort();
                } else {
                    evaluator.addEventListener(getDefaultEvaluatorListener());
                    fireNewEvaluator(evaluator);
                    try {
                        step(evaluator);
                    } catch (EvaluatorException e) {
                        fireInformation("Error evaluating sample"
                                + e.getMessage());
                    }
                }
            }
        }
        fireRunningStateChanged();
    }

    /**
     * Pauses the evolver if it is running and unpaused, else the call is ignored.
     */
    public synchronized void pause() {
        if (!isPaused()) {
            this.pause = true;
        }
    }

    /**
     * Continues a paused evolver, if the evolver is running or unstarted the call is ignored.
     */
    public synchronized void unPause() {
        if (isAlive() && isPaused()) {
            this.pause = false;
            synchronized (getPauseLock()) {
                getPauseLock().notify();
            }
        }
    }

    /**
     * Returns the lock pause sleeps on
     */
    Object getPauseLock() {
        return this.pauseLock;
    }

    /**
     * Returns true if this evolver is paused.
     */
    public boolean isPaused() {
        return this.pause;
    }

    /**
     * Aborts the evolver if it is running or paused, else the call is ignored.
     */
    public synchronized void abort() {
        if (isAlive() && !isAborted()) {
            this.abort = true;
            interrupt();
        }
    }

    /**
     * Returns true if the evolver is finished
     */
    public boolean isFinished() {
        return getStopper().isFinished(this);
    }

    /**
     * Returns true if this evolver is aborted.
     */
    public boolean isAborted() {
        return this.abort;
    }

    /**
     * Returns the object encapsulating the stop criterion for this evolver.
     */
    public Stopper getStopper() {
        return this.stopper;
    }

    /**
     * Sets the object encapsulating the stop criterion for this evolver.
     */
    public void setStopper(Stopper stopper) {
        this.stopper = stopper;
    }

    /**
     * Takes the next step in the evaluation chain using the provided evaluator.
     * 
     * @param evaluator the evaluator to use.
     */
    public void step(Evaluator evaluator) throws EvaluatorException {
        if (evaluations.size() == 0) {
            // Debug.err.println(initialSettings);
            Evaluation evaluation = evaluator.evaluate(initialSettings);
            addEvaluation(evaluation);
        } else {
            ParameterInstanceSet newSample = getSampler()
                    .getSample(getEvaluations());
            // Debug.err.println(newSample);
            Evaluation evaluation = evaluator.evaluate(newSample);
            addEvaluation(evaluation);
        }
    }

    /**
     * Returns the default Evaluator listener
     */
    public EvaluatorEventListener getDefaultEvaluatorListener() {
        return new EvaluatorEventListener() {
            /**
             * Called when evaluator is finished with the evaluation.
             * 
             * @param event the event.
             */
            public void finished(EvaluatorEvent event) {
                event.getEvaluator().removeEventListener(this);
            }

            /**
             * Called when the an error has occured during the evaluation.
             * 
             * @param event the event.
             * @param errorMessage a string describing the error.
             */
            public void error(EvaluatorEvent event, String errorMessage) {
                event.getEvaluator().removeEventListener(this);
                fireInformation(errorMessage);
            }

            /**
             * Called when evaluator has a new preliminart evaluation.
             * 
             * @param evaluation the preliminary evaluation.
             * @param event the event.
             */
            public void newPreliminaryEvaluation(EvaluatorEvent event,
                    Evaluation evaluation) {}
        };
    }

    /**
     * Adds an evaluation to this evolver
     * 
     * @param evaluation the evaluation to add
     */
    public void addEvaluation(Evaluation evaluation) {
        synchronized (this.evaluations) {
            evaluations.add(evaluation);
        }
        fireNewEvaluation(evaluation);
    }

    /**
     * Returns a evaluator for this evolver
     */
    public Evaluator getEvaluator() {
        return getEvaluatorFactory().getEvaluator();
    }

    /**
     * Returns an evaluator for this evolver, using binary exponential backdrop if no evaluator is provided. The initial fallback time is
     * half a second.
     * 
     * @param fallbacks number of times to wait before giving up.
     */
    public Evaluator getEvaluator(int fallbacks) {
        long wait = 500;

        for (int i = 0; i < fallbacks; i++) {
            Evaluator evaluator = getEvaluator();
            if (evaluator != null) {
                return evaluator;
            } else {
                fireInformation("Unable to obtain Evaluator - waiting " + wait
                        + "ms");
                try {
                    sleep(wait);
                } catch (InterruptedException e) {
                    return null;
                }
            }
            wait *= 2;
        }

        return null;
    }

    /**
     * Returns the EvaluatorFactory of this evolver
     */
    public EvaluatorFactory getEvaluatorFactory() {
        return evaluatorFactory;
    }

    /**
     * Returns the sampler of this evolver
     */
    public Sampler getSampler() {
        return sampler;
    }

    /**
     * Returns a list of the listeners of this object.
     */
    protected EventListenerList getEventListenerList() {
        return this.eventListenerList;
    }

    /**
     * Adds an event listener for to the eventlistenerlist.
     * 
     * @param listener the listener to add.
     */
    public void addEventListener(EvolverEventListener listener) {
        this.getEventListenerList().add(EvolverEventListener.class, listener);
    }

    /**
     * Removes an event listener for from the eventlistenerlist.
     * 
     * @param listener the listener to add.
     */
    public void removeEventListener(EvolverEventListener listener) {
        this.getEventListenerList()
                .remove(EvolverEventListener.class, listener);
    }

    /**
     * Notifies listeners that this evolver has gotten hold of a new evaluation.
     * 
     * @param newEvaluation the added evaluation.
     */
    public void fireNewEvaluation(Evaluation newEvaluation) {
        EvolverEvent event = new EvolverEvent(this);

        // Guaranteed to return a non-null array
        Object[] listeners = getEventListenerList().getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            ((EvolverEventListener) listeners[i + 1])
                    .newEvaluation(event, newEvaluation);
        }
    }

    /**
     * Notifies listeners that this evolver has gotten a new evaluator.
     * 
     * @param newEvaluator the added evaluator.
     */
    public void fireNewEvaluator(Evaluator newEvaluator) {
        EvolverEvent event = new EvolverEvent(this);

        // Guaranteed to return a non-null array
        Object[] listeners = getEventListenerList().getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            ((EvolverEventListener) listeners[i + 1])
                    .newEvaluator(event, newEvaluator);
        }
    }

    /**
     * Notifies listeners that the evolver has changed is running state.
     */
    public void fireRunningStateChanged() {
        EvolverEvent event = new EvolverEvent(this);

        // Guaranteed to return a non-null array
        Object[] listeners = getEventListenerList().getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            ((EvolverEventListener) listeners[i + 1])
                    .runningStateChanged(event);
        }
    }

    /**
     * Notifies listeners that the evolver wants to log something
     */
    public void fireInformation(String information) {
        EvolverEvent event = new EvolverEvent(this);

        // Guaranteed to return a non-null array
        Object[] listeners = getEventListenerList().getListenerList();

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            ((EvolverEventListener) listeners[i + 1]).information(event,
                                                                  information);
        }
    }
}
