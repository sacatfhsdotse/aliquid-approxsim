//         $Id: Interpolator.java,v 1.3 2005/11/02 22:18:48 dah Exp $
/*
 * @(#)Interpolator.java
 */

package StratmasClient.evolver;

/**
 * Holding class for static interpolator methods
 */
public class Interpolator
{
    /**
     * Interpolates a point using weighted mean of all existing
     * samples (using Shepards method). If values is an exact sample
     * that value is returned instead. If samples.length == 0, the
     * result is undefined.
     *
     * @param samples the samples to use as basis for the
     * interpolation.
     *
     * @param iIndices the indices of the parameters to be
     * interpolated. It is assumed that for for any a, b in
     * [0,iIndices.length - 1], iIndices[a] == iIndices[b] iff a == b
     * and if a < b then iIndices[a] < iIndices[b]. (I. e sorted and
     * containing no duplicates).
     * 
     * @param values an array for where values[iIndex] should be
     * interpolated and _filled in_. Note that values[] are expected
     * to have the same size and order as samples[*][]. values[i] for
     * i = iIndices[0..]  is ignored and may contain any value.
     *
     * @return values with the values[iIndex[0]..iIndex[iIndex.length
     * - 1]] interpolated.
     */
    public static double[] interpolate(double[][] samples, int iIndices[], double[] values)
    {
        // Find indices of values to interpolate from. Set these
        // values in the result array.
        int[] pIndices = new int[values.length - iIndices.length];
        for (int i = 0, pStrider = 0, iStrider = 0; 
             i < values.length; i++) {
            if (iStrider < iIndices.length && 
                i == iIndices[iStrider]) {
                iStrider++;
            } else {
                pIndices[pStrider++] = i;
            }
        }
        
        return interpolate(samples, iIndices, pIndices, values);
    }
    
    /**
     * Interpolates a point using weighted mean of all existing
     * samples (using Shepards method). If values is an exact sample
     * that value is returned instead. If no samples.length == 0, the
     * result is undefined.
     *
     * @param samples the samples to use as basis for the
     * interpolation.
     * 
     * @param iIndices the indices of the parameters to be
     * interpolated.
     *
     * @param pIndices the indices of the parameters to 
     * interpolate from.
     *
     * @param values an array for where values[iIndex] should be
     * interpolated and _filled in_. Note that values[] are expected
     * to have the same size and order as samples[*][]. values[i] for
     * i = iIndices[0..]  is ignored and may contain any value.
     *
     * @return values with the values[iIndex[0]..iIndex[iIndex.length
     * - 1]] interpolated.
     */
    public static double[] interpolate(double[][] samples, int iIndices[], 
                                       int[] pIndices, double[] values)
    {
        // Zero out interpolation points.
        for (int j = 0; j < iIndices.length; j++) {
            values[iIndices[j]] = 0.0;
        }
        
        double invDistanceSum = 0;
        for (int i = 0; i < samples.length; i++) {
            double distanceMeasure = 0;
            for (int j = 0; j < pIndices.length; j++) {
                // The sum of the squared distance
                double d = values[pIndices[j]] - samples[i][pIndices[j]];
                distanceMeasure += d*d;
            }            

            // Invert it if nonzero
            if (distanceMeasure == 0) {
                // No need to interpolate, spot on. This return
                // statement also ensures that distanceMeasure > 0 at the
                // end of the for loop;
                for (int j = 0; j < iIndices.length; j++) {
                    values[iIndices[j]] = samples[i][iIndices[j]];
                }
                
                return values;
            }
            // Invert distanceMeasure (want to favor closeness to sampled point)
            distanceMeasure = 1.0 / distanceMeasure;

            for (int j = 0; j < iIndices.length; j++) {
                values[iIndices[j]] += distanceMeasure*samples[i][iIndices[j]];
            }

            invDistanceSum += distanceMeasure;
        }

        // Adjust with total measure-sum sum.
        for (int j = 0; j < iIndices.length; j++) {
            values[iIndices[j]] /= invDistanceSum;
        }

        return values;
    }
}
