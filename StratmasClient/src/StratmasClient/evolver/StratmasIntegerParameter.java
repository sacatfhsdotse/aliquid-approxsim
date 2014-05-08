// $Id: ApproxsimIntegerParameter.java,v 1.4 2006/04/10 09:45:55 dah Exp $
/*
 * @(#)ApproxsimIntegerParameter.java
 */

package ApproxsimClient.evolver;

import ApproxsimClient.object.primitive.Reference;
import ApproxsimClient.object.ApproxsimEvent;
import ApproxsimClient.object.ApproxsimEventListener;
import ApproxsimClient.object.ApproxsimInteger;
import ApproxsimClient.object.ApproxsimObject;
import ApproxsimClient.object.ApproxsimObjectFactory;
import ApproxsimClient.communication.Subscription;
import ApproxsimClient.communication.ApproxsimObjectSubscription;

/**
 * A parameter subclass usable for parameters backed by ApproxsimIntegers
 * 
 * @version 1, $Date: 2006/04/10 09:45:55 $
 * @author Daniel Ahlin
 */

public class ApproxsimIntegerParameter extends LongParameter implements
        ApproxsimObjectParameter, SimulationEvaluatorTargetFactory {
    /**
     * The ApproxsimObject backing this parameter.
     */
    ApproxsimObject approxsimObject;

    /**
     * Creates a new ApproxsimIntegerParameter with the specified name.
     * 
     * @param object the object being adapted.
     */
    public ApproxsimIntegerParameter(ApproxsimInteger object) {
        super(object.getReference().toString());
        setApproxsimObject(object);
    }

    /**
     * Returns an instance of a SimulatorEvaluatorTarget that targets this parameter.
     */
    public SimulationEvaluatorTarget createSimulationEvaluatorTarget() {
        return new DefaultSimulationEvaluatorTarget() {
            /**
             * Creates the subscription of this target. This subscription is required to, in some way, make sure update() is called whenever
             * the subscription is updated.
             */
            Subscription createSubscription() {
//                     return new GenralSubscription((ApproxsimObject) getApproxsimObject().clone(), 
//                                                    getApproxsimObject().getReference());
                return new ApproxsimObjectSubscription(
                        ApproxsimObjectFactory.cloneObject(getApproxsimObject()),
                        getApproxsimObject().getReference());
            }

            /**
             * Creates the ParameterInstance acting as evaluation in the Evaluations created by the SimulationEvaluator.
             */
            public ParameterInstance createEvaluation() {
                return getParameterInstance(((ApproxsimObjectSubscription) getSubscription())
                        .object());
            }
        };
    }

    /**
     * Returns the reference of the ApproxsimObject backing this parameter.
     */
    public Reference getReference() {
        return getApproxsimObject().getReference();
    }

    /**
     * Returns the ApproxsimObject backing this parameter.
     */
    public ApproxsimObject getApproxsimObject() {
        return this.approxsimObject;
    }

    /**
     * Sets the ApproxsimObject backing this parameter.
     * 
     * @param object the new object
     */
    public void setApproxsimObject(ApproxsimObject object) {
        if (object != null) {
            object.addEventListener(new ApproxsimEventListener() {
                public void eventOccured(ApproxsimEvent event) {
                    if (event.isRemoved()) {
                        ((ApproxsimObject) event.getSource())
                                .removeEventListener(this);
                        setApproxsimObject(null);
                    } else if (event.isReplaced()) {
                        setApproxsimObject((ApproxsimObject) event.getArgument());
                    }
                }
            });
        }

        this.approxsimObject = object;
    }

    /**
     * If possible, returns a ParameterInstance which is a neighbour of the provided ParameterInstance along the provided gradient, else
     * null. Override this if the backing object is not a Long. This implementation always steps at least one step if gradient != 0.
     * 
     * @param instance the object to provide a neigbour for.
     * @param gradient the gradient.
     */
    public ParameterInstance getGradientNeighbour(ParameterInstance instance,
            double gradient) {
        // Force at least one step.
        if (gradient != 0.0d && Math.abs(gradient) < 1.0) {
            if (gradient > 0) {
                gradient = 1.0;
            } else {
                gradient = -1.0;
            }
        }

        ApproxsimInteger neighbour = (ApproxsimInteger) ApproxsimObjectFactory
                .cloneObject((ApproxsimInteger) instance.getValue());
        long newValue = (long) (neighbour.getValue() + gradient);
        newValue = newValue > 0 ? newValue : 0;

        neighbour.setValue(newValue);

        return getParameterInstance(neighbour);
    }

    /**
     * Returns the long value of an instance of this type. Override this if the backing object is not a Long.
     * 
     * @param instance the instance
     */
    long getLong(ParameterInstance instance) {
        return ((ApproxsimInteger) instance.getValue()).getValue();
    }
}
