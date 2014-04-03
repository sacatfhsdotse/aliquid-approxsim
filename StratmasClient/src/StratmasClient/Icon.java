//         $Id: Icon.java,v 1.8 2006/03/22 14:30:41 dah Exp $
/*
 * @(#)Icon.java
 */

package StratmasClient;

import StratmasClient.object.StratmasObject;

import javax.swing.ImageIcon;

import java.net.URL;

import java.awt.Image;
import java.awt.Dimension;

import java.util.WeakHashMap;

/**
 * A Icon used for StratmasObjects in StratmasClient.
 *
 * @version 1, $Date: 2006/03/22 14:30:41 $
 * @author  Daniel Ahlin
*/

public class Icon extends ImageIcon
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -1214318186452215999L;
	/**
     * Weakly cached version of icons
     */
    WeakHashMap<Dimension, Icon> cache = new WeakHashMap<Dimension, Icon>();

    /**
     * Creates an Icon from the specified URL.
     *
     * @param location the URL for the image.
     */
    public Icon(URL location)
    {
        super(location);
    }

    /**
     * Creates an Icon from the specified image.
     *
     * @param image the image.
     */
    public Icon(Image image)
    {
        super(image);
    }

    /**
     * Returns an Icon for the StratmasObject
     *
     * @param obj the object to get the icon for.
     */
    public static Icon getIcon(StratmasObject obj)
    {
        return IconFactory.getIcon(obj);
    }

    /**
     * Returns a scaled version of this Icon.
     * @param width - the width to which to scale the image.
     * @param height - the height to which to scale the image.
     * @param hints - flags to indicate the type of algorithm to use
     * for image resampling.
     */
    public Icon getScaledInstance(int width, int height, int hints)
    {
        return new Icon(getImage().getScaledInstance(width, height, hints));
    }

    /**
     * Returns a scaled version of this Icon.
     * @param width - the width to which to scale the image.
     * @param height - the height to which to scale the image.
     */
    public Icon getScaledInstance(int width, int height)
    {
        return getScaledInstance(new Dimension(width, height));
    }

    /**
     * Returns a scaled version of this Icon.
     * @param dim - the dimension of the instance
     */
    public Icon getScaledInstance(Dimension dim)
    {
        synchronized (cache) {
            Icon cached = cache.get(dim);
            if (cached == null) {
                cached = getScaledInstance(dim.width, dim.height, 
                                           Image.SCALE_SMOOTH);//DEFAULT);;
                cache.put(dim, cached);
            }
            return cached;
        }
    }    
}
