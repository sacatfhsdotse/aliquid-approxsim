// 	$Id: SimulationEvaluatorFactory.java,v 1.2 2006/03/22 14:30:50 dah Exp $
/*
 * @(#)SimulationEvaluatorFactory.java
 */

package StratmasClient.evolver;

import StratmasClient.dispatcher.StratmasDispatcher;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.StratmasEventListener;
import StratmasClient.object.StratmasObject;
import StratmasClient.communication.StratmasSocket;

/**
 * Provides an Evolver with SimulatorEvaluator instances
 *
 * @version 1, $Date: 2006/03/22 14:30:50 $
 * @author  Daniel Ahlin
*/
public abstract class SimulationEvaluatorFactory implements EvaluatorFactory, StratmasEventListener
{
    /**
     * The dispatcher used by this factory
     */
    StratmasDispatcher dispatcher;
    
    /**
     * The simulation to simulate
     */
    StratmasObject simulation = null;

    /**
     * Creates a new factory.
     *
     * @param dispatcher the dispatcher to use when allocating new servers.
     * @param simulation the simulation to simulate.
     */
    SimulationEvaluatorFactory(StratmasDispatcher dispatcher, StratmasObject simulation)
    {
	this.dispatcher = dispatcher;
	setSimulation(simulation);
    }

    /**
     * Returns an instance of an Evaluator. 
     */
    public Evaluator getEvaluator()
    {
	if (getSimulation() != null) {
	    ServerSession session = createServerSession();
	    if (session != null) {
		return new SimulationEvaluator(session, createTarget(), getSimulation());
	    }
	}
	
	return null;
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
     * Creates the simulator targets fed to the SimulationEvaluators
     * created for this factory. Returning null is not allowed.
     */
    abstract SimulationEvaluatorTarget createTarget();

    /**
     * Creates the server session fed to the SimulationEvaluators
     * created for this factory returns. Returning null means no
     * server session could be created.
     */
    ServerSession createServerSession()
    {
	StratmasSocket socket = getDispatcher().allocateServer(10);
	if (socket != null) {
	    return new DefaultServerSession(socket);
	} else {
	    return null;
	}
    }

    /**
     * Returns the dispatcher used by this factory.
     */
    public StratmasDispatcher getDispatcher()
    {
	return this.dispatcher;
    }
}
