// $Id: Sampler.java,v 1.3 2005/10/28 12:20:55 dah Exp $
/*
 * @(#)Sampler.java
 */

package StratmasClient.evolver;

import java.util.Vector;

/**
 * A sampler provides new ParameterInstanceSets to sample. E. g. if f(x, y) is being maximized and there exist sevaral samples, f'(x[n - 1],
 * y[n - 1]), f'(x[n], y[n]) etc, the sampler returns a set {x[n + 1], y[n + 1]} to evaluate.
 * 
 * @version 1, $Date: 2005/10/28 12:20:55 $
 * @author Daniel Ahlin
 */
public interface Sampler {
    /**
     * Returns a new ParameterInstanceSet to sample
     * 
     * @param evaluations a vector of Evaluations of the previuos samples.
     */
    public ParameterInstanceSet getSample(Vector evaluations);
}
