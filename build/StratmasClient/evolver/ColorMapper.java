// 	$Id: ColorMapper.java,v 1.1 2005/11/02 22:18:47 dah Exp $
/*
 * @(#)ColorMapper.java
 */

package StratmasClient.evolver;

/**
 * Defines an interface for mapping a double to a four component RGBA
 * color
 *
 * @version 1, $Date: 2005/11/02 22:18:47 $
 * @author  Daniel Ahlin
*/

public interface ColorMapper
{
    /**
     * Maps a color to the map d;
     */
    double[] map(double d);
}
