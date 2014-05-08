package ApproxsimClient.map.adapter;

import java.awt.Color;
import ApproxsimClient.object.Line;
import ApproxsimClient.map.Projection;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

/**
 * This adapter adapts Line for use on the map.
 * 
 * @author Amir Filipovic
 */
public class MapLineAdapter extends MapDrawableAdapter {
    /**
     * The number of Render Selection names needed by this adapter.
     */
    public static int NR_RENDER_SELECTION_NAMES = 1;
    /**
     * The default color of the lines.
     */
    public static Color DEFAULT_LINE_COLOR = new Color(1.0f, 0.0f, 0.0f);
    /**
     * The default width of the lines.
     */
    public static float DEFAULT_LINE_WIDTH = 2.0f;
    /**
     * The color of the shape lines.
     */
    private Color lineColor = DEFAULT_LINE_COLOR;
    /**
     * The width of the shape lines.
     */
    private float lineWidth = DEFAULT_LINE_WIDTH;

    /**
     * Creates new adapter.
     * 
     * @param line the object to adapt.
     * @param renderSelectionName the integer to use as the base for names in RENDER_SELECTION.
     */
    public MapLineAdapter(Line line, int renderSelectionName) {
        super(line);
        setRenderSelectionName(renderSelectionName);
    }

    /**
     * Creates new adapter.
     * 
     * @param line the object to adapt.
     */
    public MapLineAdapter(Line line) {
        super(line);
    }

    /**
     * Updates (recreates) the display list that draws the entire object.
     * 
     * @param proj the actual projection.
     * @param gld the gl drawable targeted.
     */
    protected void updateDisplayList(Projection proj, GLAutoDrawable gld) {
        GL2 gl = (GL2) gld.getGL();
        displayList = (gl.glIsList(displayList)) ? displayList : gl
                .glGenLists(1);

        gl.glNewList(displayList, GL2.GL_COMPILE);
        // point display list
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        // pushes the name for RenderSelection mode
        gl.glPushName(getRenderSelectionName());
        // get the start and the end points

        double[] p1 = proj.projToXY(((Line) stComp).getStartPoint());
        double[] p2 = proj.projToXY(((Line) stComp).getEndPoint());

        // line color
        float[] cColor = lineColor.getRGBColorComponents(null);
        gl.glColor3f(cColor[0], cColor[1], cColor[2]);
        // width of the line
        gl.glLineWidth(lineWidth);
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex2dv(p1, 0);
        gl.glVertex2dv(p2, 0);
        gl.glEnd();
        gl.glPopName();
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPopMatrix();
        gl.glEndList();
        displayListUpdated = true;
    }

    /**
     * Returns the number of renderSelectionNames needed for this adapter.
     */
    public int getNrOfRenderSelectionNames() {
        return NR_RENDER_SELECTION_NAMES;
    }

    /**
     * Updates the display lists
     */
    public void reCompile(Projection proj, GLAutoDrawable gld) {
        if (!displayListUpdated) {
            updateDisplayList(proj, gld);
        }
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
