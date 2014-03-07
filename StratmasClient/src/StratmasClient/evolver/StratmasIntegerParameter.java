//         $Id: StratmasIntegerParameter.java,v 1.4 2006/04/10 09:45:55 dah Exp $
/*
 * @(#)StratmasIntegerParameter.java
 */

package StratmasClient.evolver;

import StratmasClient.object.primitive.Reference;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.StratmasEventListener;
import StratmasClient.object.StratmasInteger;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasObjectFactory;
import StratmasClient.communication.Subscription;
import StratmasClient.communication.StratmasObjectSubscription;

/**
 * A parameter subclass usable for parameters backed by StratmasIntegers
 * 
 * @version 1, $Date: 2006/04/10 09:45:55 $
 * @author  Daniel Ahlin
*/

public class StratmasIntegerParameter extends LongParameter implements StratmasObjectParameter, 
                                                                         SimulationEvaluatorTargetFactory
{
    /**
     * The StratmasObject backing this parameter.
     */
    StratmasObject stratmasObject;

    /**
     * Creates a new StratmasIntegerParameter with the specified name.
     * 
     * @param object the object being adapted.
     */
    public StratmasIntegerParameter(StratmasInteger object)
    {
        super(object.getReference().toString());
        setStratmasObject(object);
    }


    /**
     * Returns an instance of a SimulatorEvaluatorTarget that targets
     * this parameter.
     */
    public SimulationEvaluatorTarget createSimulationEvaluatorTarget()
    {
        return new DefaultSimulationEvaluatorTarget()
            {
                /**
                 * Creates the subscription of this target. This
                 * subscription is required to, in some way, make sure
                 * update() is called whenever the subscription is
                 * updated.
                 */
                Subscription createSubscription()
                {
//                     return new GenralSubscription((StratmasObject) getStratmasObject().clone(), 
//                                                    getStratmasObject().getReference());
                     return new StratmasObjectSubscription(StratmasObjectFactory.cloneObject(getStratmasObject()),
                                                           getStratmasObject().getReference());
                }

                /**
                 * Creates the ParameterInstance acting as evaluation in the
                 * Evaluations created by the SimulationEvaluator.
                 */
                public ParameterInstance createEvaluation()
                {
                    return getParameterInstance(((StratmasObjectSubscription) getSubscription()).object());
                }
            };
    }

    /**
     * Returns the reference of the StratmasObject backing this parameter.
     */ 
    public Reference getReference()
    {
        return getStratmasObject().getReference();
    }

    /**
     * Returns the StratmasObject backing this parameter.
     */ 
    public StratmasObject getStratmasObject()
    {
        return this.stratmasObject;
    }

    /**
     *  Sets the StratmasObject backing this parameter.
     *
     * @param object the new object 
     */ 
    public void setStratmasObject(StratmasObject object)
    {
        if (object != null) {
            object.addEventListener(new StratmasEventListener()
            {
                public void eventOccured(StratmasEvent event)
                {
                    if (event.isRemoved()) {
                        ((StratmasObject) event.getSource()).removeEventListener(this);
                        setStratmasObject(null);
                    } else if (event.isReplaced()) {
                        setStratmasObject((StratmasObject) event.getArgument());
                    }
                }
            });
        }

        this.stratmasObject = object;
    }

    /**
     * If possible, returns a ParameterInstance which is a neighbour
     * of the provided ParameterInstance along the provided gradient,
     * else null. Override this if the backing object is not a Long.
     *
     * This implementation always steps at least one step if gradient
     * != 0.
     * 
     * @param instance the object to provide a neigbour for.
     * @param gradient the gradient.
     */ 
    public ParameterInstance getGradientNeighbour(ParameterInstance instance, 
                                                  double gradient)
    {
        // Force at least one step.
        if (gradient != 0.0d && 
            Math.abs(gradient) < 1.0) {
            if (gradient > 0) {
                gradient = 1.0;
            } else {
                gradient = -1.0;
            }
        }

        StratmasInteger neighbour = (StratmasInteger)
            StratmasObjectFactory.cloneObject((StratmasInteger) instance.getValue());
        long newValue = (long) (neighbour.getValue() + gradient);
        newValue = newValue > 0 ? newValue : 0;

        neighbour.setValue(newValue);
        
        return getParameterInstance(neighbour);
    }
   
    /**
     * Returns the long value of an instance of this type. Override
     * this if the backing object is not a Long.
     *
     * @param instance the instance
     */
    long getLong(ParameterInstance instance)
    {
        return ((StratmasInteger) instance.getValue()).getValue();
    }
}
