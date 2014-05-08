// $Id: SimulationEvaluatorFactory.java,v 1.2 2006/03/22 14:30:50 dah Exp $
/*
 * @(#)SimulationEvaluatorFactory.java
 */

package ApproxsimClient.evolver;

import ApproxsimClient.dispatcher.ApproxsimDispatcher;
import ApproxsimClient.object.ApproxsimEvent;
import ApproxsimClient.object.ApproxsimEventListener;
import ApproxsimClient.object.ApproxsimObject;
import ApproxsimClient.communication.ApproxsimSocket;

/**
 * Provides an Evolver with SimulatorEvaluator instances
 * 
 * @version 1, $Date: 2006/03/22 14:30:50 $
 * @author Daniel Ahlin
 */
public abstract class SimulationEvaluatorFactory implements EvaluatorFactory,
        ApproxsimEventListener {
    /**
     * The dispatcher used by this factory
     */
    ApproxsimDispatcher dispatcher;

    /**
     * The simulation to simulate
     */
    ApproxsimObject simulation = null;

    /**
     * Creates a new factory.
     * 
     * @param dispatcher the dispatcher to use when allocating new servers.
     * @param simulation the simulation to simulate.
     */
    SimulationEvaluatorFactory(ApproxsimDispatcher dispatcher,
            ApproxsimObject simulation) {
        this.dispatcher = dispatcher;
        setSimulation(simulation);
    }

    /**
     * Returns an instance of an Evaluator.
     */
    public Evaluator getEvaluator() {
        if (getSimulation() != null) {
            ServerSession session = createServerSession();
            if (session != null) {
                return new SimulationEvaluator(session, createTarget(),
                        getSimulation());
            }
        }

        return null;
    }

    /**
     * Sets the simulation of this object.
     * 
     * @param simulation the simulation to use.
     */
    void setSimulation(ApproxsimObject simulation) {
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
    ApproxsimObject getSimulation() {
        return this.simulation;
    }

    /**
     * Deregister the simulation on delete or replace.
     * 
     * @param event the event.
     */
    public void eventOccured(ApproxsimEvent event) {
        if (event.isRemoved() || event.isReplaced()) {
            setSimulation(null);
        }
    }

    /**
     * Creates the simulator targets fed to the SimulationEvaluators created for this factory. Returning null is not allowed.
     */
    abstract SimulationEvaluatorTarget createTarget();

    /**
     * Creates the server session fed to the SimulationEvaluators created for this factory returns. Returning null means no server session
     * could be created.
     */
    ServerSession createServerSession() {
        ApproxsimSocket socket = getDispatcher().allocateServer(10);
        if (socket != null) {
            return new DefaultServerSession(socket);
        } else {
            return null;
        }
    }

    /**
     * Returns the dispatcher used by this factory.
     */
    public ApproxsimDispatcher getDispatcher() {
        return this.dispatcher;
    }
}
