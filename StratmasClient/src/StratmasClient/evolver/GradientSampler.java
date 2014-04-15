//         $Id: GradientSampler.java,v 1.12 2005/11/02 22:18:48 dah Exp $
/*
 * @(#)GradientSampler.java
 */

package StratmasClient.evolver;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Random;

/**
 * GradientSampler produces samples that lies in the direction of the
 * gradient.
 *
 * @version 1, $Date: 2005/11/02 22:18:48 $
 * @author  Daniel Ahlin
*/
public class GradientSampler implements Sampler
{
    /**
     * Indicates wheter this sampler sampler minimizes or maximizes.
     */
    boolean isMinimizing = true;

    /**
     * Creates a new GradientSampler.
     * 
     * @param isMinimizing true if this sampler should seek to minimize
     * the evaluations.
     */
    public GradientSampler(boolean isMinimizing)
    {
        this.isMinimizing = isMinimizing;
    }

    /**
     * Creates a new GradientSampler that samples towards a maximum.
     */
    public GradientSampler()
    {
        this(false);
    }

    /**
     * Returns true if this is a minimizing sampler.
     */
    public boolean isMinimizing()
    {
        return this.isMinimizing;
    }

    /**
     * Returns a new ParameterInstanceSet to sample. It is assumed
     * that the provided evaluations contains at least one sample (in
     * order for the sampler to know what Parameters to create samples
     * for) else it returns null.
     *
     * @param evaluations a vector of Evaluations of the previuos
     * samples.
     */
    public ParameterInstanceSet getSample(Vector evaluations)
    {
        if (evaluations.size() == 0) {
            return null;
        } else if (evaluations.size() == 1) {
            return createRandomNeighbour((Evaluation) evaluations.get(0));
        } else {
            return createGradientNeighbour(evaluations);
        }
    }

    /**
     * Creates a sample based on the gradient of the previous samples.
     *
     * @param evaluations previous samples, note that it is assumed
     * that evaluations.size() >= 2.
     */
    ParameterInstanceSet createGradientNeighbour(Vector evaluations)
    {
        Hashtable<Parameter, Double> gradients = 
            getBackwardDifference((Evaluation) evaluations.elementAt(evaluations.size() - 2), 
                                  (Evaluation) evaluations.elementAt(evaluations.size() - 1));

        ParameterInstanceSet latest = 
            ((Evaluation) evaluations.elementAt(evaluations.size() - 1)).getParameterInstanceSet();
        ParameterInstanceSet result = new ParameterInstanceSet();
        // Add parameters from latest sample (adds in loop will
        // replace those parameterinstances that has a gradient).
        result.addAll(latest);
        // Create new samples based on gradients.
        for (Enumeration<Parameter> e = gradients.keys(); e.hasMoreElements();) {
            Parameter parameter = e.nextElement();
            Double gradient = gradients.get(parameter);
            if (gradient.isNaN() || gradient.isInfinite()) { 
//                 Debug.err.println("Bad gradient for " + parameter.getName() + ": " + gradient);
            } else {
                result.add(parameter.getGradientNeighbour(latest.getParameterInstance(parameter), gradient.doubleValue()));
            }
        }

        return result;
    }

    /**
     * Calculates the backward difference of two evaluations.
     *
     * @param backward the backward point.
     * @param center the point for which the derivative is approximated.
     */
    Hashtable<Parameter, Double> getBackwardDifference(Evaluation backward, Evaluation center) 
    {
        Hashtable<Parameter, Double> res = new Hashtable<Parameter, Double>();

        // Calculate the difference of the evaluation.
        double eDiff = backward.getEvaluation().getParameter().getMetric().d(backward.getEvaluation(),
                                                                             center.getEvaluation());
        eDiff *= 
            ((double) backward.getEvaluation().getParameter().getComparator().compare(backward.getEvaluation(),
                                                                                      center.getEvaluation()));

        for (Enumeration e = backward.getParameterInstanceSet().getParameterInstances(); 
             e.hasMoreElements(); ) {
            ParameterInstance instance = (ParameterInstance) e.nextElement();
            // Calculate the difference of each particular parameter.
            double pDiff = instance.getParameter().getMetric().d(instance, center.getParameterInstanceSet().getParameterInstance(instance.getParameter()));
            pDiff *= ((double) instance.getParameter().getComparator().compare(instance, center.getParameterInstanceSet().getParameterInstance(instance.getParameter())));
            Double gradient = null;
            if (eDiff == 0.0d) {
                // If no difference in eDiff double the length of previous step.
                gradient = new Double(pDiff * 2);
            } else if (pDiff == 0.0d) {
                // If standing still in dimension, continue to do so.
                gradient = new Double(0.0);
            } else {
                // Use some sort of scaled difference:
                gradient = new Double(eDiff/pDiff);
            }

//             Debug.err.println("eDiff = " + eDiff + " pDiff = " + pDiff + " gradient = " + gradient);
            res.put(instance.getParameter(), gradient);
        }
        
        return res;
    }

    /**
     * Creates a random neighbour of a evaluation.
     *
     * @param evaluation the evaluation to create the neigbour for.
     */
    ParameterInstanceSet createRandomNeighbour(Evaluation evaluation)
    {
        Random random = new Random(System.currentTimeMillis());

        ParameterInstanceSet result = new ParameterInstanceSet();
        result.addAll(evaluation.getParameterInstanceSet());
        for (Enumeration e = result.getParameterInstances(); e.hasMoreElements();) {
            ParameterInstance parameterInstance = (ParameterInstance) e.nextElement();
            double gradient = random.nextGaussian() - 0.5d;
            result.add(parameterInstance.getParameter().getGradientNeighbour(parameterInstance, gradient));
        }
        return result;
    }
}
