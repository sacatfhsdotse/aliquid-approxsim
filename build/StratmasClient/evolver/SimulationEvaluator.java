// 	$Id: SimulationEvaluator.java,v 1.6 2006/03/22 14:30:50 dah Exp $
/*
 * @(#)SimulationEvaluator.java
 */

package StratmasClient.evolver;

import StratmasClient.object.StratmasObject;
import StratmasClient.Debug;
import StratmasClient.object.type.TypeFactory;

import StratmasClient.object.StratmasEvent;
import StratmasClient.object.StratmasEventListener;

import StratmasClient.communication.ServerException;
import StratmasClient.communication.Subscription;

import java.util.Vector;
import java.util.Enumeration;

/**
 * An implementation of evaluator that runs a stratmas simulation to
 * get the evaluation measure.
 *
 * @version 1, $Date: 2006/03/22 14:30:50 $
 * @author  Daniel Ahlin
*/
public class SimulationEvaluator extends DefaultEvaluator implements StratmasEventListener
{
    /**
     * Vector containing Evaluations that are results of measured
     * timesteps passed before this evaluator finished.
     */
    Vector evaluations = new Vector();

    /**
     * The server session this SimulationEvaluator
     * utilizes.
     */
    ServerSession session;

    /**
     * The simulation to simulate
     */
    StratmasObject simulation = null;

    /**
     * The target of this evaluator, i. e its performance measure.
     */
    SimulationEvaluatorTarget target;

    /**
     * Creates a new SimulationEvaluator.
     *
     * @param session the connection this evaluator should
     * use.
     * @param target the target of this evaluator.
     * @param simulation the simulation to run
     */
    public SimulationEvaluator(ServerSession session, 
			       SimulationEvaluatorTarget target,
			       StratmasObject simulation)
    {	
	super();
	this.target = target;
	this.session = session;
	setSimulation(simulation);
    }

    /**
     * Returns a measure for the ParameterInstanceSet 
     *
     * @param set the parameters to measure.
     *
     * @throws EvaluatorException if an error occured while evaluating.
     */
    public Evaluation evaluate(ParameterInstanceSet set) throws EvaluatorException
    {
	// Check all parameters in set:
	// 1. Find out if they are backed by StratmasObjects.
	// 2. Check resolvability of all references.
	// FIXME: Note that 2 have to be removed or changed when the
	// sampler should be able to actually add objects (and not just
	// change existing ones).
	for (Enumeration e = set.getParameterInstances(); 
	     e.hasMoreElements();) {
	    ParameterInstance parameterInstance = 
		(ParameterInstance) e.nextElement();
	    if (! (parameterInstance.getParameter() instanceof StratmasObjectParameter)) {
		String errorMessage = getClass() + 
		    " is unable to evaluate parameters of type: " + 
		    parameterInstance.getParameter().getClass();
		fireError(errorMessage);
		throw new EvaluatorException(this, errorMessage);
	    } else if (((StratmasObjectParameter) parameterInstance.getParameter()).getReference().resolve(getSimulation()) == null) {
		String errorMessage = getClass() + ": " + 
		    ((StratmasObjectParameter) parameterInstance.getParameter()).getReference() + 
		    " does not exist in the simulation.";
		fireError(errorMessage);
		throw new EvaluatorException(this, errorMessage);
	    }
	}

	try {
	    getSession().initialize(getSimulation());
	    for (Enumeration e = set.getParameterInstances(); 
		 e.hasMoreElements();) {
		ParameterInstance parameterInstance = 
		    (ParameterInstance) e.nextElement();	    
		getSession().updateObject(((StratmasObjectParameter) parameterInstance.getParameter()).getReference(),
					  (StratmasObject) parameterInstance.getValue());
	    } 

	    getSession().registerSubscription(getTarget().getSubscription());
	   
	    while (!getTarget().getStopper().isFinished(this)) {
		// Blocking step until any subscriptions is updated.
		getSession().step();
		addEvaluation(new Evaluation(set, getTarget().createEvaluation()));
		getTarget().increaseUpdateCount();
	    }
	} catch (ServerException e) {
	    String errorMessage = getClass() + " was interrupted waiting " + 
		"for server update: " + e.getMessage();
	    fireError(errorMessage);
	    try {
		getSession().close();
	    } catch (ServerException ex) {
		// Don't care really.
		Debug.err.println("Error closing session: " + ex.getMessage());
	    }
	    throw new EvaluatorException(this, errorMessage);
	}
	
	try {
	    getSession().close();
	} catch (ServerException e) {
	    // Don't care really.
	    Debug.err.println("Error closing session: " + e.getMessage());
	}

	// This will (intentionally) return null if no current
	// evaluation.
	fireFinished();
	return getPreliminaryEvaluation();
    }


    /**
     * Sets the simulation of this object.
     *
     * @param simulation the simulation to use.
     */
    void setSimulation(StratmasObject simulation)
    {
	if (getSimulation() != null) {
	    getSimulation().removeEventListener(this);
	}

	if (simulation != null) {
	    simulation.addEventListener(this);
	}

	this.simulation = simulation;
    }

    /**
     * Returns the simulation to simulate.
     */
    StratmasObject getSimulation()
    {
	return this.simulation;
    }
    
    /**
     * Deregister the simulation on delete or replace.
     *
     * @param event the event.
     */
    public void eventOccured(StratmasEvent event)
    {
	if (event.isRemoved() || event.isReplaced()) {
	    setSimulation(null);
	}
    }


    /**
     * Returns the session this evaluator uses for communication with
     * the server.
     */
    ServerSession getSession() 
    {
	return this.session;
    }

    /**
     * Returns the target this evaluator is evaluating
     */
    SimulationEvaluatorTarget getTarget() 
    {
	return this.target;
    }

    /**
     * Returns copy of vector containing Evaluations that are results
     * of measured timesteps passed before this evaluator finished.
     */
    public Vector getEvaluations()
    {
	Vector res = null;

	synchronized (this.evaluations) {
	    res = (Vector) evaluations.clone();
	}

	return res;
    }

    /**
     * Returns the current value of the evaluation if any, else null.
     */
    public Evaluation getPreliminaryEvaluation()
    {
	Evaluation res = null;

	synchronized (this.evaluations) {
	    if (this.evaluations.size() > 0) {
		res = (Evaluation) this.evaluations.lastElement();
	    }
	}

	return res;
    }

    /**
     * Adds an preliminary evaluation to this evaluator.
     *
     * @param evaluation the evaluation to add
     */
    protected void addEvaluation(Evaluation evaluation)
    {
	synchronized (this.evaluations) {
	    this.evaluations.add(evaluation);
	}
	fireNewEvaluation(evaluation);
    }

    /**
     * Returns the max time a server request may take
     */
    long getTimeout()
    {
	return 20000;
    }

    /**
     * Returns a string representation of this
     */
    public String toString()
    {
	return getSession().toString();
    }
}

