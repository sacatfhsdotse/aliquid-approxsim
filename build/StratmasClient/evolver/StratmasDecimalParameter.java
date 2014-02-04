//         $Id: StratmasDecimalParameter.java,v 1.8 2006/04/10 09:45:55 dah Exp $
/*
 * @(#)StratmasDecimalParameter.java
 */

package StratmasClient.evolver;

import StratmasClient.object.primitive.Reference;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.StratmasEventListener;
import StratmasClient.object.StratmasDecimal;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasObjectFactory;
import StratmasClient.object.primitive.Timestamp;

import java.util.Vector;

import org.w3c.dom.Element;

import StratmasClient.communication.Subscription;
import StratmasClient.communication.StratmasObjectSubscription;

/**
 * A parameter subclass usable for parameters backed by StratmasDecimals
 * 
 * @version 1, $Date: 2006/04/10 09:45:55 $
 * @author  Daniel Ahlin
*/

public class StratmasDecimalParameter extends DoubleParameter implements StratmasObjectParameter, 
                                                                         SimulationEvaluatorTargetFactory
{
    /**
     * The StratmasObject backing this parameter.
     */
    StratmasObject stratmasObject;

    /**
     * Creates a new StratmasDecimalParameter with the specified name.
     * 
     * @param object the object being adapted.
     */
    public StratmasDecimalParameter(StratmasDecimal object)
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
//                     return new GeneralSubscription((StratmasObject) getStratmasObject().clone(), 
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
     * else null. Override this if the backing object is not a Double.
     * 
     * @param instance the object to provide a neigbour for.
     * @param gradient the gradient.
     */ 
    public ParameterInstance getGradientNeighbour(ParameterInstance instance, 
                                                  double gradient)
    {
        StratmasDecimal neighbour = (StratmasDecimal)
            StratmasObjectFactory.cloneObject(((StratmasDecimal) instance.getValue()));
        neighbour.setValue(neighbour.getValue() + gradient);
        
        return getParameterInstance(neighbour);
    }
    
    /**
     * Returns the double value of an instance of this type. Override
     * this if the backing object is not a Double.
     *
     * @param instance the instance
     */
    double getDouble(ParameterInstance instance)
    {
        return ((StratmasDecimal) instance.getValue()).getValue();
    }
}
