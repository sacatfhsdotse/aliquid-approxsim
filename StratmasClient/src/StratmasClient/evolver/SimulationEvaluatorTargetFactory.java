// $Id: SimulationEvaluatorTargetFactory.java,v 1.1 2005/11/01 09:36:13 dah Exp $
/*
 * @(#)SimulationEvaluatorTargetFactory.java
 */

package ApproxsimClient.evolver;

/**
 * Provides an SimulatorEvaluatorFactory with SimulationEvaluatorTarget instances
 * 
 * @version 1, $Date: 2005/11/01 09:36:13 $
 * @author Daniel Ahlin
 */
public interface SimulationEvaluatorTargetFactory {
    /**
     * Returns an instance of an SimulationEvaluatorTargetFactory.
     */
    public SimulationEvaluatorTarget createSimulationEvaluatorTarget();
}
