// $Id: GraphAdapter.java,v 1.0 2014-04-08 $
/*
 * @(#)GraphAdapter.java
 */

package StratmasClient.map.adapter;

import java.io.UnsupportedEncodingException;
import java.nio.DoubleBuffer;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLUtessellatorCallback;
import javax.media.opengl.glu.GLUtessellatorCallbackAdapter;

import StratmasClient.Debug;
import StratmasClient.map.Projection;
import StratmasClient.object.StratmasObject;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.gl2.GLUT;

/**
 * GraphAdapter adapts StratmasObjects descendants of Graph for viewing on a map window.
 * 
 * @version 1, $Date: 2014-04-08 $
 * @author Exuvo
 */
public class GraphAdapter extends ElementAdapter {
    /**
     * The whether to draw name of element under symbol.
     */
    boolean drawElementName = true;

    /**
     * Creates a new ElementAdapter.
     * 
     * @param element the Element to adapt.
     */
    protected GraphAdapter(StratmasObject element) {
        super(element);
    }

    /**
     * Creates a new ElementAdapter.
     * 
     * @param element the Element to adapt.
     * @param renderSelectionName the integer to use as the base for names in RENDER_SELECTION
     */
    protected GraphAdapter(StratmasObject element, int renderSelectionName) {
        super(element, renderSelectionName);
    }

    /**
     * Updates (recreates) the displayList that draws the symbol of the element this adapter represents.
     * 
     * @param proj the projection that maps lat and long into GL coordinates.
     * @param gld the gl drawable targeted.
     */
    protected void updateSymbolDisplayList(Projection proj, GLAutoDrawable gld) {
        GL2 gl = (GL2) gld.getGL();
        displayListsBuf.put(SYMBOL_POS,
                            (gl.glIsList(displayListsBuf.get(SYMBOL_POS))) ? displayListsBuf
                                    .get(SYMBOL_POS) : gl.glGenLists(1));

        // Draw a square proportional to the city size
        // Start list
        gl.glNewList(displayListsBuf.get(SYMBOL_POS), GL2.GL_COMPILE);

        // Pushes the name for RenderSelection mode.
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();

        double scale = getSymbolScale();
        if (getInvariantSymbolSize()) {
            gl.glMatrixMode(GL2.GL_PROJECTION);
            DoubleBuffer buf = Buffers.newDirectDoubleBuffer(16);
            gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, buf);
            scale = getSymbolScale() * 0.000003d / buf.get(0);
        }

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glScaled(scale, scale, 1.0d);

//        gl.glScaled(0.5 * inhabitantsScale, 0.5 * inhabitantsScale, 0.5 * inhabitantsScale);
        gl.glPushName(getRenderSelectionName() + 1 + SYMBOL_POS);
        gl.glBegin(GL2.GL_QUADS);
        gl.glColor4d(0.0d, 0.0d, 0.0d, getSymbolOpacity());
        gl.glVertex2d(-horizontalSymbolSize / 2, -verticalSymbolSize / 2);
        gl.glVertex2d(-horizontalSymbolSize / 2, verticalSymbolSize / 2);
        gl.glVertex2d(horizontalSymbolSize / 2, verticalSymbolSize / 2);
        gl.glVertex2d(horizontalSymbolSize / 2, -verticalSymbolSize / 2);
        gl.glEnd();
        gl.glPopMatrix();
        if (drawElementName()) {
            GLUT glut = new GLUT();
            // Draw name of element. The constants below are the unit
            // sizes of GLUTs Stroke fonts. This will draw the name of
            // the city on one line below the symbol in a height
            // 1/10th of the symbol size.
            // Wash string, replace unknowns with '_';
            String str = "";
            try {
                str = getGLUTIDString();
            } catch (UnsupportedEncodingException ex) {
                Debug.err.println(ex.toString());
            }

            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glPushMatrix();
            // Draw below marker at a tenth of the vertical size of the
            // symbol.
            double textScale = getVerticalSymbolSize() / (5 * (119.05 + 33.33));
            gl.glTranslated(-104.76 * textScale * str.length() / 2, -(119.05 * textScale + 2 + 0.5
                   /* * inhabitantsScale */ * getVerticalSymbolSize() / 2), 0);
            gl.glScaled(textScale, textScale, 1.0);

            // Draw in black if unselected, else red.
            if (isSelected()) {
                gl.glColor4dv(SELECTION_COLOR, 0);
            } else {
                gl.glColor4d(0.0d, 0.0d, 0.0d, 1.0d);
            }
            glut.glutStrokeString(GLUT.STROKE_MONO_ROMAN, str);

            gl.glPopMatrix();
        }
        gl.glPopName();
        gl.glPopMatrix();
        gl.glEndList();
        isSymbolUpdated = true;
    }

    /**
     * Returns true if the name of this population should be drawn along with the symbol.
     */
    public boolean drawElementName() {
        return this.drawElementName;
    }

    /**
     * Set whether the name of this population should be drawn along with the symbol.
     * 
     * @param flag true if name should be drawn
     */
    public void setDrawElementName(boolean flag) {
        if (this.drawElementName() != flag) {
            this.drawElementName = flag;
            isSymbolUpdated = false;
            fireAdapterUpdated();
        }
    }

    /**
     * Make adapter changes caused by selection state changes. This is protected for a reason, DO NOT USE THIS to set the selection status
     * of the element adapted, use getElement() and make the changes on the object instead.
     * 
     * @param selected true if it's selected.
     */
    public void setSelected(boolean selected) {
        if (this.selected != selected) {
            this.selected = selected;
            isSelectionMarkerUpdated = false;
            isSymbolUpdated = false;
            fireAdapterUpdated();
        }
    }

    /**
     * Updates (recreates) the displayList that draws the selection frame if the element this adapter represents is selected. Overridden to
     * account for differing symbolsize of PopulationAdapter.
     * 
     * @param proj the projection that maps lat and long into GL coordinates.
     * @param gld the gl drawable targeted.
     */
    protected void updateSelectionMarkerDisplayList(Projection proj, GLAutoDrawable gld) {
        GL2 gl = (GL2) gld.getGL();
        displayListsBuf
                .put(SELECTION_MARKER_POS,
                     (gl.glIsList(displayListsBuf.get(SELECTION_MARKER_POS))) ? displayListsBuf
                             .get(SELECTION_MARKER_POS) : gl.glGenLists(1));

        gl.glNewList(displayListsBuf.get(SELECTION_MARKER_POS), GL2.GL_COMPILE);
        // Pushes the name for RenderSelection mode.
        gl.glPushName(getRenderSelectionName() + 1 + SELECTION_MARKER_POS);
        if (isSelected()) {
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glPushMatrix();
            gl.glScaled(getSymbolScale(), getSymbolScale(), getSymbolScale());
//            gl.glScaled(0.5 * inhabitantsScale, 0.5 * inhabitantsScale, 0.5 * inhabitantsScale);
            gl.glBegin(GL2.GL_LINE_LOOP);
            gl.glColor4dv(SELECTION_COLOR, 0);
            gl.glVertex2d(-(horizontalSymbolSize / 2 + 1), -(verticalSymbolSize / 2 + 1));
            gl.glVertex2d(-(horizontalSymbolSize / 2 + 1), (verticalSymbolSize / 2 + 1));
            gl.glVertex2d((horizontalSymbolSize / 2 + 1), (verticalSymbolSize / 2 + 1));
            gl.glVertex2d((horizontalSymbolSize / 2 + 1), -(verticalSymbolSize / 2 + 1));
            gl.glEnd();
            gl.glPopMatrix();
        }
        gl.glPopName();
        gl.glEndList();
        isSelectionMarkerUpdated = true;
    }

    /**
     * Returns the tessellator callback to use for this ElementAdapter.
     * 
     * @param gld the glDrawable context to use.
     */
    protected GLUtessellatorCallback getLocationTessellatorCallback(GLAutoDrawable gld) {
        final GL2 gl = (GL2) gld.getGL();

        return new GLUtessellatorCallbackAdapter() {
            public void vertex(Object data) {
                double[] p = (double[]) data;
                gl.glColor4d(1.0d, 1.0d, 1.0d, 0.0d);
                gl.glVertex2dv(p, 0);
            }

            public void begin(int type) {
                gl.glBegin(type);
            }

            public void end() {
                gl.glEnd();
            }
        };
    }

}
