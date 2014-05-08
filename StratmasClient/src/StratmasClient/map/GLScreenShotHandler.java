// $Id: GLScreenShotHandler.java,v 1.4 2006/04/18 13:01:16 dah Exp $
/*
 * @(#)GLScreenShotHandler.java
 */

package StratmasClient.map;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import java.nio.ByteBuffer;
import com.jogamp.common.nio.Buffers;

import java.awt.image.BufferedImage;

/**
 * Specialized class for jogl screeenshots.
 * 
 * @author Daniel Ahlin
 * @version 1 ($Date: 2006/04/18 13:01:16 $)
 */

public class GLScreenShotHandler extends ScreenShotHandler {
    /**
     * Creates a new ScreenShotHandler that will handle the specified image.
     * 
     * @param image the image to handle.
     */
    private GLScreenShotHandler(BufferedImage image) {
        super(image);
    }

    /**
     * Switches drawbuffer to first availiable aux buffer if possible, else remain in the old drawbuffer.
     * 
     * @param gld the gl context
     * @return the previous setting of drawbuffer.
     */
    public static int changeDrawBuffer(GLAutoDrawable gld) {
        GL2 gl = (GL2) gld.getGL();
        GLU glu = new GLU();

        // Save original draw buffer.
        int[] drawBuffer = new int[1];
        gl.glGetIntegerv(GL2.GL_DRAW_BUFFER, drawBuffer, 0);

        // Get number of availiable AUX buffers;
        int[] maxAuxBuffers = new int[1];
        gl.glGetIntegerv(GL2.GL_AUX_BUFFERS, maxAuxBuffers, 0);

        // See if this is a double buffered device or not.
        byte[] hasBackBuffer = new byte[1];
        gl.glGetBooleanv(GL2.GL_DOUBLEBUFFER, hasBackBuffer, 0);

        int newBuffer = drawBuffer[0];
        if (maxAuxBuffers[0] > 0) {
            // If any aux buffers are availiable, use one of these.
            newBuffer = GL2.GL_AUX0 + maxAuxBuffers[0] - 1;
        } else if (hasBackBuffer[0] != 0) {
            if (drawBuffer[0] == GL2.GL_BACK) {
                newBuffer = GL2.GL_FRONT;
            } else {
                newBuffer = GL2.GL_BACK;
            }
        }

        if (newBuffer != drawBuffer[0]) {
            while (gl.glGetError() != GL2.GL_NO_ERROR);
            gl.glDrawBuffer(newBuffer);
            int foo = gl.glGetError();
            if (foo != GL2.GL_NO_ERROR) {
                System.err.println("Error switching draw buffer: "
                        + glu.gluErrorString(foo));
            }
        } else {
            System.err
                    .println("Warning: unable to allocate off-screen buffer for screenshot "
                            + "- the screen-shot may be corrupted.");
        }

        // Hack to fix broken glClear() when other jogl windows
        // overlaps this one.
        fakeClear(gld);

        return drawBuffer[0];
    }

    /**
     * Hack to fix broken glClear() when other jogl windows overlaps this one.
     * 
     * @param gld the gl context.
     */
    public static void fakeClear(GLAutoDrawable gld) {
        GL2 gl = (GL2) gld.getGL();
        GLU glu = new GLU();;

        double[] prevColor = new double[4];
        gl.glGetDoublev(GL2.GL_CURRENT_COLOR, prevColor, 0);
        double[] bgColor = new double[4];
        gl.glGetDoublev(GL2.GL_COLOR_CLEAR_VALUE, bgColor, 0);

        gl.glColor4dv(bgColor, 0);

        int[] matrixMode = new int[1];
        gl.glGetIntegerv(GL2.GL_MATRIX_MODE, matrixMode, 0);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        int[] size = getSize(gld);
        glu.gluOrtho2D(0, size[0], 0, size[1]);

        // Draw rectangle over the area.
        gl.glRecti(0, 0, size[0], size[1]);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPopMatrix();
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(matrixMode[0]);
        gl.glColor4dv(prevColor, 0);
    }

    /**
     * Returns the pixel width and height of the gld.
     * 
     * @param gld the gld to use.
     * @return an array where [0] == height and [1] == width.
     */
    public static int[] getSize(GLAutoDrawable gld) {
        GL2 gl = (GL2) gld.getGL();

        int[] viewport = new int[4];
        gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);

        return new int[] { viewport[2], viewport[3] };
    }

    /**
     * Makes a screenshot of a GL Drawable
     * 
     * @param gld the drawable to shoot.
     */
    public static void doGLScreenShot(GLAutoDrawable gld) {
        GL2 gl = (GL2) gld.getGL();
        GLU glu = new GLU();
        int[] size = getSize(gld);
        int width = size[0];
        int height = size[1];

        ByteBuffer image = Buffers.newDirectByteBuffer(width * height * 3);

        // Make the read buffer the same as the current drawbuffer.
        int[] origReadBuffer = new int[1];
        gl.glGetIntegerv(GL2.GL_READ_BUFFER, origReadBuffer, 0);
        int[] drawBuffer = new int[1];
        gl.glGetIntegerv(GL2.GL_DRAW_BUFFER, drawBuffer, 0);

        while (gl.glGetError() != GL2.GL_NO_ERROR);
        gl.glReadBuffer(drawBuffer[0]);
        int foo = gl.glGetError();
        if (foo != GL2.GL_NO_ERROR) {
            System.err.println("Error switching read buffer: "
                    + glu.gluErrorString(foo));
        }

        // Set transfer format and read the values into the image
        // buffer
        gl.glPixelStorei(GL2.GL_PACK_ALIGNMENT, 1);
        gl.glReadPixels(0, 0, width, height, GL2.GL_RGB, GL2.GL_UNSIGNED_BYTE,
                        image);

        // Repack image.
        int[] buf = new int[width * height];
        image.rewind();
        // Converts and flips the image
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int offset1 = i * width + j;
                int offset2 = ((height - 1) - i) * width + j;

                // Convert and flip.
                buf[offset1] = argb2argb(image, 3 * offset2);
                buf[offset2] = argb2argb(image, 3 * offset1);
            }
        }

        // Restore readbuffer to previous value.
        gl.glReadBuffer(origReadBuffer[0]);

        java.awt.image.BufferedImage bufferedImage = new java.awt.image.BufferedImage(
                width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
        bufferedImage.setRGB(0, 0, width, height, buf, 0, width);
        new GLScreenShotHandler(bufferedImage).start();
    }

    /**
     * Converts the bgra byte pattern at offset to argb
     * 
     * @param image the image in which to convert.
     * @param offset where in the image to convert.
     */
    public static int argb2argb(ByteBuffer image, int offset) {
        return 0 | ((((int) image.get(offset + 0)) & 0xff) << 16)
                | ((((int) image.get(offset + 1)) & 0xff) << 8)
                | ((((int) image.get(offset + 2)) & 0xff));
    }
}
