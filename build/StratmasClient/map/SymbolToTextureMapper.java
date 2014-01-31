// 	$Id: SymbolToTextureMapper.java,v 1.8 2006/04/18 13:01:16 dah Exp $
/*
 * @(#)SymbolToTextureMapper.java
 */

package StratmasClient.map;

import java.util.Enumeration;
import StratmasClient.object.StratmasObject;
import StratmasClient.Debug;
import StratmasClient.object.StratmasEventListener;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.StratmasBoolean;
import StratmasClient.object.Line;
import StratmasClient.object.Shape;
import StratmasClient.object.SimpleShape;
import StratmasClient.object.Point;
import StratmasClient.BoundingBox;
import StratmasClient.Icon;

import StratmasClient.filter.StratmasObjectFilter;
import StratmasClient.filter.StratmasObjectAdapter;

import java.util.Vector;
import java.util.Comparator;
import java.util.Collections;
import java.util.Hashtable;

import java.nio.ByteBuffer;
import com.sun.opengl.util.BufferUtil;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.GLAutoDrawable;

import java.awt.image.WritableRaster;
import java.awt.image.Raster;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.awt.image.ComponentColorModel;
import java.awt.color.ColorSpace;

import java.awt.image.DataBufferByte;
import java.awt.image.DataBuffer;

import java.util.EventListener;
import javax.swing.event.EventListenerList;

/**
 * A texture cache implementation
 *
 * @version 1, $Date: 2006/04/18 13:01:16 $
 * @author  Daniel Ahlin
 */
public class SymbolToTextureMapper
{
    /**
     *  A hashtabel mapping gl-contexts to hashtables mapping icons to
     *  texture names.
     */
    private static Hashtable glContexts = new Hashtable();

    /**
     * Whether to use mipmap texturing. Default is true. See also textureMinFilter.
     */
    static boolean useMipMap = true;

    /**
     * If mipmap texturing is used, whether let glu scale the icons to
     * different mipmaps. Default is true;
     */
    static boolean useGluMipMap = true;

    /**
     * Which method to use when the textured figure is smaller than
     * the texture (default is GL_LINEAR_MIPMAP_LINEAR). See also
     * useMipMap.
     */
    public static float textureMinFilter = GL.GL_LINEAR_MIPMAP_LINEAR;

    /**
     * Which method to use when the textured figure is larger than the
     * availiable texture (default is GL_LINEAR).
     */
    public static float textureMagFilter = GL.GL_LINEAR;

    /**
     * What texturing mode to use, default is GL_REPLACE.
     */
    public static float textureMode = GL.GL_MODULATE;

    /**
     * GLU to use.
     */
    public static GLU glu = new GLU();

    /**
     * Returns the texture for the given icon.
     * 
     * @param icon the icon to get the texture for.
     * @param glContext the glContext for which to get the texture.
     */
    static public int getTexture(Icon icon, GLAutoDrawable glContext)
    {
	Hashtable textureNames = getContext(glContext);
	Integer textureName;
	synchronized (textureNames) {
	    textureName = (Integer) textureNames.get(icon.getImage());
	    if (textureName == null) {
		textureName = createTexture(icon, glContext);
		textureNames.put(icon.getImage(), textureName);
	    }
	}

	return textureName.intValue();
    }

    /**
     * Creates a texture of the given image in the provided gl
     * context. Returns the integer name of the texture.
     *  
     * @param icon the icon to get the texture for.
     * @param gld the glDrawable for which the texture is created.
     */
    static private Integer createTexture(Icon icon, GLAutoDrawable gld)
    {
	gld.getGL().glEnable(GL.GL_TEXTURE_2D);
	// Generate new name and bind this texture to it.
	int newName[] = new int[1];
	gld.getGL().glGenTextures(1, newName, 0);
	
	Debug.err.println("Creating new texture " + newName[0] + " for " + 
			  icon.getImage().hashCode());
	
	gld.getGL().glBindTexture(GL.GL_TEXTURE_2D, newName[0]);
	gld.getGL().glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
	gld.getGL().glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
 	gld.getGL().glTexParameterf(GL.GL_TEXTURE_2D, 
				    GL.GL_TEXTURE_MAG_FILTER, 
				    textureMagFilter);
 	gld.getGL().glTexParameterf(GL.GL_TEXTURE_2D, 
				    GL.GL_TEXTURE_MIN_FILTER, 
				    textureMinFilter);
	gld.getGL().glTexEnvf(GL.GL_TEXTURE_ENV, 
			      GL.GL_TEXTURE_ENV_MODE, 
			      textureMode);

	if (useMipMap) {
	    if (useGluMipMap) {
		glu.gluBuild2DMipmaps(GL.GL_TEXTURE_2D,
				      GL.GL_RGBA,
				      icon.getIconWidth(),
				      icon.getIconHeight(), 
				      GL.GL_RGBA,
				      GL.GL_UNSIGNED_BYTE,
				      iconToByteArray(icon));
	    } else {
		gld.getGL().glTexImage2D(GL.GL_TEXTURE_2D,
					 0,
					 GL.GL_RGBA,
					 128,
					 128, 
					 0,
					 GL.GL_RGBA,
					 GL.GL_UNSIGNED_BYTE,
					 iconToByteArray(icon, 128, 128));
		gld.getGL().glTexImage2D(GL.GL_TEXTURE_2D,
					 1,
					 GL.GL_RGBA,
					 64,
					 64, 
					 0,
					 GL.GL_RGBA,
					 GL.GL_UNSIGNED_BYTE,
					 iconToByteArray(icon, 64, 64));
		gld.getGL().glTexImage2D(GL.GL_TEXTURE_2D,
					 2,
					 GL.GL_RGBA,
					 128,
					 128, 
					 0,
					 GL.GL_RGBA,
					 GL.GL_UNSIGNED_BYTE,
					 iconToByteArray(icon, 32, 32));		
	    }
	} else {
	    // Find closest power of 2
	    int pixels = icon.getIconWidth() > icon.getIconHeight() ? 
		icon.getIconWidth() : icon.getIconWidth();
	    int size; 
	    for (size = 2; size - pixels > size*2 - pixels; size *= 2);
	    
	    gld.getGL().glTexImage2D(GL.GL_TEXTURE_2D,
				     0,
				     GL.GL_RGBA,
				     size,
				     size, 
				     0,
				     GL.GL_RGBA,
				     GL.GL_UNSIGNED_BYTE,
				     iconToByteArray(icon, size, size));
	}

	gld.getGL().glBindTexture(GL.GL_TEXTURE_2D, 0);
	gld.getGL().glDisable(GL.GL_TEXTURE_2D);

	return new Integer(newName[0]);
    }
    
    
    /**
     * Converts the provided icon to an RGBA byte array.
     *
     * @param icon icon to convert.
     */
     private static ByteBuffer iconToByteArray(Icon icon)
     {
	 return iconToByteArray(icon, icon.getIconWidth(), icon.getIconHeight());
     }

    /**
     * Converts the provided icon to an RGBA byte array.
     *
     * @param icon icon to convert.
     * @param xRes horizontal resolution.
     * @param yRes vertical resolution.
     */
    private static ByteBuffer iconToByteArray(Icon icon, int xRes, int yRes)
    {
	if (icon.getIconWidth() != xRes || icon.getIconHeight() != yRes) {
	    icon = icon.getScaledInstance(xRes, yRes, Image.SCALE_SMOOTH);
	}

	WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, 
							       icon.getIconWidth(),
							       icon.getIconHeight(), 4, null);
	BufferedImage bufferedImage = 
	    new BufferedImage(new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
						      new int[] {8,8,8,8}, true, false,
						      ComponentColorModel.TRANSLUCENT,
						      DataBuffer.TYPE_BYTE), 
			      raster, false, null);
	Graphics2D graphics = bufferedImage.createGraphics();

	// Use an AffineTransformation to draw upside-down in the java sense, 
	// which will make it right-side-up in OpenGL.
	AffineTransform transform = new AffineTransform();
	transform.translate(0, icon.getIconHeight());
	transform.scale(1, -1d);
	graphics.transform(transform);
	graphics.drawImage (icon.getImage(), null, null);
	graphics.dispose();
	
	// Return the underlying byte array.	 
	byte[] foo = ((DataBufferByte) raster.getDataBuffer()).getData();
	ByteBuffer fio = BufferUtil.newByteBuffer(foo.length);
	fio.put(foo);
	fio.rewind();
	return fio;
    }

    /**
     * Returns the hashtable for the given GLAutoDrawable. Creates one on
     * demand.
     * 
     * @param glDrawable the glDrawable for which to get the hashtable.
     *
     */
    static private Hashtable getContext(GLAutoDrawable glDrawable)
    {
	synchronized (glContexts) {
	    Hashtable res = (Hashtable) glContexts.get(glDrawable.getGL());
	    if (res == null) {
		res = new Hashtable();
		glContexts.put(new GLWeakReference(glDrawable.getGL()), res);
	    }
	    return res;
	}
    }
}

class GLWeakReference extends java.lang.ref.WeakReference
{
    /**
     * The hashCode of the referent.
     */
    int hashCode;

    /**
     * Creates a weak reference to a gl context
     *
     * @param gl the gl
     */
    public GLWeakReference(GL gl)
    {
	super(gl);
	this.hashCode = gl.hashCode();
    }

    /**
     * Returns the hashcode of its referent.
     */
    public int hashCode()
    {
	return hashCode;
    }

    /**
     * Returns true if this reference is equal to 
     * a. this
     * b. a weak reference referencing the same context
     * c. the context refered to by this.
     */
    public boolean equals(Object o)
    {
	if (this == o) {
	    return true;
	} else if (o instanceof GLWeakReference) {
	    Object oa = get();
	    Object ob = ((GLWeakReference) o).get();
	    if (oa == null) {
		return oa == ob;
	    } else {
		return oa.equals(ob); 
	    }
	} else if (o instanceof GL) {
	    Object oa = get();
	    if (oa == null) {
		return oa == o;
	    } else {
		return oa.equals(o); 
	    }
	} else {
	    return false;
	}
    }
}
