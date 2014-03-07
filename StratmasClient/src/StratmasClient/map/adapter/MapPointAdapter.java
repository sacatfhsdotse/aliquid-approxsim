package StratmasClient.map.adapter;

import java.nio.DoubleBuffer;

import StratmasClient.object.Point;
import StratmasClient.Icon;
import StratmasClient.IconFactory;
import StratmasClient.object.StratmasEvent;
import StratmasClient.map.Projection;
import StratmasClient.map.SymbolToTextureMapper;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import com.jogamp.common.nio.Buffers;

/**
 * This adapter adapts Point for use on the map.
 *
 * @author Amir Filipovic
 */
public class MapPointAdapter extends MapDrawableAdapter {
    /**
     * Display list for the symbol.
     */
    private int symbolDisplayList;
    /**
     * Symbol scale.
     */
    private double symbolScale = 1.0d;
    /**
     * Whether symbol is updated since last redraw.
     */
    private boolean symbolUpdated = false;
    /**
     * Wheter symbol size should be drawn in constant size.
     */
    private boolean invariantSymbolSize = false;
    /**
     * The horizontal size of the symbol.
     */
    private static double horizontalSymbolSize = 5000;
    /**
     * The vertical size of the symbol.
     */
    private static double verticalSymbolSize = 5000;
    /**
     * The number of Render Selection names needed by this adapter.
     */
    public static int NR_RENDER_SELECTION_NAMES = 2;
 
    /**
     * Creates new adapter.
     *
     * @param point the object to adapt.
     * @param renderSelectionName the integer to use as the base for names in RENDER_SELECTION.
     */
    protected MapPointAdapter(Point point, int renderSelectionName) {
        super(point);
        setRenderSelectionName(renderSelectionName);
    }
    
    /**
     * Creates new adapter.
     *
     * @param point the object to adapt.
     */
    public MapPointAdapter(Point point) {
        super(point);
    }
    
    /**
     * Updates (recreates) the display list that draws the entire
     * object.
     *
     * @param proj the actual projection.
     * @param gld the gl drawable targeted.
     */
    protected void updateDisplayList(Projection proj, GLAutoDrawable gld) {
        GL2 gl = (GL2) gld.getGL();
         displayList = (gl.glIsList(displayList)) ? displayList : gl.glGenLists(1);
        //
        gl.glNewList(displayList, GL2.GL_COMPILE);
        // point display list
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        // Pushes the name for RenderSelection mode.        
        gl.glPushName(getRenderSelectionName());
        // get x-coordinate of the render position 
        // get y-coordinate of the render position
        double[] xy = proj.projToXY((Point)stComp);
        gl.glTranslated(xy[0], xy[1], 0);
        gl.glCallList(symbolDisplayList);
        gl.glPopName();
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPopMatrix();
        gl.glEndList();
        displayListUpdated = true;
    }
    
    /**
     * Updates (recreates) the display lists that draws the symbol of the object
     * this adapter represents.
     *
     * @param gld the gl drawable targeted.
     */
    protected void updateSymbolDisplayLists(GLAutoDrawable gld)
    {
        GL2 gl = (GL2) gld.getGL();
        symbolDisplayList = (gl.glIsList(symbolDisplayList))? symbolDisplayList : gl.glGenLists(1);
        
        // get texture from texture mapper
        int texture = SymbolToTextureMapper.getTexture(new Icon(IconFactory.class.getResource("icons/leaf.png")), gld);
        
        // Start list
        gl.glNewList(symbolDisplayList, GL2.GL_COMPILE);
        // Enable textures.        
        gl.glEnable(GL2.GL_TEXTURE_2D);
        gl.glBindTexture(GL2.GL_TEXTURE_2D, texture);
        gl.glTexParameterf(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
        gl.glTexParameterf(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP);
        gl.glTexParameterf(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, 
                           SymbolToTextureMapper.textureMagFilter);
        gl.glTexParameterf(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, 
                           SymbolToTextureMapper.textureMinFilter);
        gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, 
                     SymbolToTextureMapper.textureMode);
        
        // Pushes the name for RenderSelection mode.
        gl.glPushName(getRenderSelectionName() + 1);
        
        double scale = getSymbolScale();
        if (getInvariantSymbolSize()) {
            gl.glMatrixMode(GL2.GL_PROJECTION);
            DoubleBuffer buf = Buffers.newDirectDoubleBuffer(16);
            gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, buf);
            scale = getSymbolScale()*0.000004d/buf.get(0);
        }

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glScaled(scale, scale, 1.0d);
        gl.glBegin(GL2.GL_QUADS);
        gl.glColor4d(1.0d, 1.0d, 1.0d, 1.0d);
        gl.glTexCoord2f(0, 0);
        gl.glVertex2d(-horizontalSymbolSize/2, -verticalSymbolSize/2);
        gl.glTexCoord2f(0, 1);
        gl.glVertex2d(-horizontalSymbolSize/2, verticalSymbolSize/2);
        gl.glTexCoord2f(1, 1);
        gl.glVertex2d(horizontalSymbolSize/2, verticalSymbolSize/2);
        gl.glTexCoord2f(1, 0);
        gl.glVertex2d(horizontalSymbolSize/2, -verticalSymbolSize/2);
        gl.glEnd();
        gl.glPopName();
        gl.glPopMatrix();
        gl.glDisable(GL2.GL_TEXTURE_2D);
        gl.glEndList();
        symbolUpdated = true;
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
        if (!symbolUpdated) {
            updateSymbolDisplayLists(gld);
        }
        if (!displayListUpdated) {
            updateDisplayList(proj, gld);        
        }
    }
    
    /**
     * Invalidates the display lists.
     */
    public void invalidateAllLists() {
        symbolUpdated = false;
        displayListUpdated = false;
        fireAdapterUpdated();
    }

    /**
     * Invalidates the display list for the symbol. 
     */
    public void invalidateSymbolList() {
        this.symbolUpdated = false;
        fireAdapterUpdated();
    }

    /**
     * Sets whether symbol-size should be invariant with regard to map
     * scale.
     *
     * @param flag true if symbol size should be invariant
     */
    public void setInvariantSymbolSize(boolean flag) {
        if (this.invariantSymbolSize != flag) {
            this.invariantSymbolSize = flag;
            invalidateSymbolList();
        }
    }
    
    /**
     * Returns true if the symbol size is constant. 
     */
    public boolean getInvariantSymbolSize() {
        return this.invariantSymbolSize;
    }
    
    /**
     * Sets the symbol scale of this adapter to the specified value
     * (between 0.0 and 1.0 inclusive);
     *
     * @param symbolScale the new opacity.
     */
    public void setSymbolScale(double symbolScale) {
        if (this.symbolScale != symbolScale) {
            this.symbolScale = symbolScale;
            symbolUpdated = false;
            fireAdapterUpdated();
        }
    }
    
    /**
     * Returns the symbol scale of this adapter.
     */
    public double getSymbolScale() {
        return symbolScale;
    }
    
    /**
     * Returns the symbol horizontal size of the images.
     */
    public static double getHorizontalSymbolSize() {
        return horizontalSymbolSize;
    }

    /**
     * Returns the symbol vertical size of the images.
     */
    public static double getVerticalSymbolSize() {
        return verticalSymbolSize;
    }

    /**
     * Updates this adapter when the adapted object changes.
     *
     * @param event the event causing the change.
     */
    protected void valueChanged(StratmasEvent event) {
        displayListUpdated = false;
        fireAdapterUpdated();
    }
}
