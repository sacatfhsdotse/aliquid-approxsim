// $Id: GraphAdapter.java,v 1.0 2014-04-08 $
/*
 * @(#)GraphAdapter.java
 */

package StratmasClient.map.adapter;

import java.awt.Color;
import java.nio.DoubleBuffer;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLUtessellatorCallback;
import javax.media.opengl.glu.GLUtessellatorCallbackAdapter;

import StratmasClient.Debug;
import StratmasClient.map.Projection;
import StratmasClient.object.Point;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasReference;

import com.jogamp.common.nio.Buffers;

/**
 * GraphAdapter adapts StratmasObjects descendants of Graph for viewing on a map window.
 * 
 * @version 1, $Date: 2014-04-08 $
 * @author Exuvo
 */
public class GraphEdgeAdapter extends MapElementAdapter {

    /**
     * The default color of the lines.
     */
    public static Color DEFAULT_LINE_COLOR = new Color(0.0f, 0.8f, 0.0f);
    /**
     * The default width of the lines.
     */
    public static float DEFAULT_LINE_WIDTH = 6000.0f; // TODO find better value
    /**
     * The color of the shape lines.
     */
    private Color lineColor = DEFAULT_LINE_COLOR;
    /**
     * The width of the shape lines.
     */
    private float lineWidth = DEFAULT_LINE_WIDTH;

    /**
     * Creates a new ElementAdapter.
     * 
     * @param element the Element to adapt.
     */
    protected GraphEdgeAdapter(StratmasObject element) {
        super(element);
        //TODO add listener for node position changes/removal
    }

    /**
     * Creates a new ElementAdapter.
     * 
     * @param element the Element to adapt.
     * @param renderSelectionName the integer to use as the base for names in RENDER_SELECTION
     */
    protected GraphEdgeAdapter(StratmasObject element, int renderSelectionName) {
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
        displayListsBuf
                .put(SYMBOL_POS,
                     (gl.glIsList(displayListsBuf.get(SYMBOL_POS))) ? displayListsBuf
                             .get(SYMBOL_POS) : gl.glGenLists(1));

        // Start list
        gl.glNewList(displayListsBuf.get(SYMBOL_POS), GL2.GL_COMPILE);

        // Pushes the name for RenderSelection mode.
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glPushName(getRenderSelectionName() + 1 + SYMBOL_POS);

        double[] p1 = getOriginLonLat();
        double[] p2 = getTargetLonLat();
        p1 = proj.projToXY(p1);
        p2 = proj.projToXY(p2);
        
        float[] cColor = lineColor.getRGBColorComponents(null);
        
        // because coordinates must be relative to getLonLat().
        double[] center = proj.projToXY(getLonLat());
        p1[0] -= center[0];
        p1[1] -= center[1];
        p2[0] -= center[0];
        p2[1] -= center[1];
        
        // rotate diff 90deg clockwise
        double[] n = {p2[1] - p1[1], p1[0]-p2[0]};
        double nLen = Math.sqrt(n[0]*n[0] + n[1]*n[1]);
        n[0] *= lineWidth/2/nLen; // TODO multiply with some sort of scale?
        n[1] *= lineWidth/2/nLen;
        
        gl.glBegin(GL2.GL_POLYGON);
        gl.glLineWidth(lineWidth);
        gl.glColor4d(cColor[0], cColor[1], cColor[2], getSymbolOpacity());
        gl.glVertex2d(p1[0]+n[0], p1[1]+n[1]);
        gl.glVertex2d(p2[0]+n[0], p2[1]+n[1]);
        gl.glVertex2d(p2[0]-n[0], p2[1]-n[1]);
        gl.glVertex2d(p1[0]-n[0], p1[1]-n[1]);
        gl.glEnd();
        
        gl.glPopName();
        gl.glPopMatrix();
        gl.glEndList();
        isSymbolUpdated = true;
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

    protected double[] getOriginLonLat() {
        StratmasObject walker = getObject();
        while (walker != null && walker.getChild("origin") == null) {
            walker = walker.getParent();
        }

        if (walker != null) {
            Point p = (Point) ((StratmasReference) walker.getChild("origin"))
                    .getValue().resolve(walker).getChild("point");
            return new double[] { p.getLon(), p.getLat() };
        } else {
            Debug.err.println("Should not be here!");
            return new double[] { 0.0d, 0.0d };
        }
    }

    protected double[] getTargetLonLat() {
        StratmasObject walker = getObject();
        while (walker != null && walker.getChild("target") == null) {
            walker = walker.getParent();
        }

        if (walker != null) {
            Point p = (Point) ((StratmasReference) walker.getChild("target"))
                    .getValue().resolve(walker).getChild("point");
            return new double[] { p.getLon(), p.getLat() };
        } else {
            Debug.err.println("Should not be here!");
            return new double[] { 0.0d, 0.0d };
        }
    }

    // Object center
    protected double[] getLonLat() {
        double[] o = getOriginLonLat();
        double[] t = getTargetLonLat();
        double[] a = new double[] { (o[0] + t[0]) / 2, (o[1] + t[1]) / 2 };
        return a;
    }

    protected double[] getLonLatDiff() {
        double[] o = getOriginLonLat();
        double[] t = getTargetLonLat();
        return new double[] { o[0] - t[0], o[1] - t[1] };
    }

    /**
     * Returns the longitude of the center of the position of the object this adapter adapts.
     */
    protected double getLon() {
        return getLonLat()[0];
    }

    /**
     * Returns the latitiude of the center of the position of the object this adapter adapts.
     */
    protected double getLat() {
        return getLonLat()[1];
    }

    /**
     * Returns the tessellator callback to use for this ElementAdapter.
     * 
     * @param gld the glDrawable context to use.
     */
    protected GLUtessellatorCallback getLocationTessellatorCallback(
            GLAutoDrawable gld) {
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

    public void eventOccured(StratmasEvent event) {
        super.eventOccured(event);
    }

    protected void childChanged(StratmasEvent event) {
        super.childChanged(event);
    }

    /**
     * Sets color of the line.
     * 
     * @param lineColor color of the line.
     */
    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
        displayListUpdated = false;
        fireAdapterUpdated();
    }

    /**
     * Sets width of the line.
     * 
     * @param lineWidth width of the line.
     */
    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
        displayListUpdated = false;
        fireAdapterUpdated();
    }

}
