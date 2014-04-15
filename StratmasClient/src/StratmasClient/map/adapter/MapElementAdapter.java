package StratmasClient.map.adapter;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.Enumeration;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.media.opengl.glu.GLUtessellator;
import javax.media.opengl.glu.GLUtessellatorCallback;
import javax.media.opengl.glu.GLUtessellatorCallbackAdapter;

import StratmasClient.BoundingBox;
import StratmasClient.Debug;
import StratmasClient.map.Projection;
import StratmasClient.map.SymbolToTextureMapper;
import StratmasClient.object.Circle;
import StratmasClient.object.Line;
import StratmasClient.object.Shape;
import StratmasClient.object.SimpleShape;
import StratmasClient.object.StratmasDecimal;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.SymbolIDCode;

import com.jogamp.common.nio.Buffers;

/**
 * MapElementAdapter adapts StratmasObjects descendants of Element and Activity for
 * viewing on a map window.
 *
 * @version 1, 
 * @author  Daniel Ahlin
*/
public class MapElementAdapter extends MapDrawableAdapter{
    /**
     * The color of the selection marking.
     */
    static final double[] SELECTION_COLOR = {1.0d, 0.0d, 0.0d, 1.0d};
    /**
     * The position of the location displaylist in the displayListsBuf buffer.
     */
    static final int LOCATION_POS = 0;
    /**
     * The position of the symbol displaylist in the displayListsBuf buffer.
     */
    static final int SYMBOL_POS = 1;
    /**
     * The position of the selection marker displaylist in the displayListsBuf buffer.
     */
    static final int SELECTION_MARKER_POS = 2;
    /**
     * The position of the outline marker displaylist in the displayListsBuf buffer.
     */
    static final int OUTLINE_MARKER_POS = 3;
    /**
     * The position of the connection arrow displaylist in the displayListsBuf buffer.
     */
    static final int CONNECTION_ARROW_POS = 4;
    /**
     * The number of used display list.
     */
    static final int USED_DISPLAY_LISTS = CONNECTION_ARROW_POS + 2;
    /**
     * All displayLists used by the mapElementAdapter except the total displaylist.
     */
    IntBuffer displayListsBuf = Buffers.newDirectIntBuffer(USED_DISPLAY_LISTS - 1);
    /**
     * The number of Render Selection names needed by this adapter.
     */
    public static int NR_RENDER_SELECTION_NAMES = 6;
    /**
     * Whether location is updated since last redraw.
     */
    boolean isLocationUpdated = false;
     /**
     * Whether symbol is updated since last redraw.
     */
    boolean isSymbolUpdated = false;
     /**
     * Whether selection marker is updated since last redraw.
     */
    boolean isSelectionMarkerUpdated = false;
    /**
     * Whether outline marker is updated since last redraw.
     */
    boolean isOutlineMarkerUpdated = false;
    /**
     * Whether connection arrow is updated since last redraw.
     */
    boolean connectionArrowUpdated = false;
    /**
     * True if the object is selected.
     */
    boolean selected = false;
    /**
     * True if the object is outlined.
     */
    boolean outlined = false;
    /**
     * The horizontal size of the symbol.
     */
    double horizontalSymbolSize = 50000;
    /**
     * The vertical size of the symbol.
     */
    double verticalSymbolSize = 50000;
    /**
     * Symbol opacity
     */
    double symbolOpacity = 1.0d;
    /**
     * Symbol scale
     */
    double symbolScale = 1.0d;
    /**
     * Whether to draw location.
     */
    boolean drawLocation = false;
    /**
     * Whether to draw location  as an outline.
     */
    boolean drawLocationOutline = false;
    /**
     * Wheter symbol size should be drawn in constant size.
     */
    boolean invariantSymbolSize = false;

    /**
     * Location opacity
     */
    double locationOpacity = 1.0d;
    
    /**
     * Creates a new MapElementAdapter.
     *
     * @param mapElement the object to adapt.
     */
    protected MapElementAdapter(StratmasObject mapElement)
    {
        super(mapElement);
    }

    /**
     * Creates a new MapElementAdapter.
     *
     * @param mapElement the object to adapt.
     * @param renderSelectionName the integer to use as the base for names in RENDER_SELECTION
     */
    protected MapElementAdapter(StratmasObject mapElement, int renderSelectionName)
    {
        super(mapElement);
        setRenderSelectionName(renderSelectionName);
    }
    
    /**
     * Updates (recreates) the displayList that draws the entire object.
     *
     * @param proj the projection that maps lat and long into GL coordinates.
     * @param gld the gl drawable targeted.
     */
    protected void updateDisplayList(Projection proj, GLAutoDrawable gld)
    {
        GL2 gl = (GL2) gld.getGL();
         this.displayList = 
             (gl.glIsList(this.displayList)) ?
             this.displayList : gl.glGenLists(1);
        
        gl.glNewList(getDisplayList(), GL2.GL_COMPILE);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        // Pushes the name for RenderSelection mode.        
        gl.glPushName(getRenderSelectionName());
        // Set render position for consequent calls
        double projectedPosition[] = proj.projToXY(getLonLat());
        gl.glTranslated(projectedPosition[0], projectedPosition[1], 0);
        // Call all sublists.
        gl.glCallLists(displayListsBuf.capacity(), GL2.GL_INT, displayListsBuf);
        gl.glPopName();
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPopMatrix();
        gl.glEndList();
        displayListUpdated = true;
    }
    
    /**
     * Updates (recreates) the displayList that draws the symbol of the object
     * this adapter represents.
     *
     * @param proj the projection that maps lat and long into GL coordinates.
     * @param gld the gl drawable targeted.
     */
    protected void updateSymbolDisplayList(Projection proj, GLAutoDrawable gld)
    {
        GL2 gl = (GL2) gld.getGL();
         displayListsBuf.put(SYMBOL_POS, 
                            (gl.glIsList(displayListsBuf.get(SYMBOL_POS)) ?
                             displayListsBuf.get(SYMBOL_POS) : gl.glGenLists(1)));

        // Get texture from texture mapper. This is done outside the
        // list in case the mapper needs to create a newtexture
        // definition. (Which should not get compiled.)
        int texture = SymbolToTextureMapper.getTexture(getObject().getIcon(), gld);

        // Start list
        gl.glNewList(displayListsBuf.get(SYMBOL_POS), GL2.GL_COMPILE);
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
        gl.glPushName(getRenderSelectionName() + 1 + SYMBOL_POS);

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
        gl.glColor4d(1.0d, 1.0d, 1.0d, getSymbolOpacity());
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
        isSymbolUpdated = true;
    }

    /**
     * Returns the displayList used to draw the symbol.
     */
    public int getSymbolDisplayList()
    {
        return displayListsBuf.get(SYMBOL_POS);
    }

    /**
     * Updates (recreates) the displayList that draws the selection
     * frame if the mapElement this adapter represents is selected.
     *
     * @param proj the projection that maps lat and long into GL coordinates.
     * @param gld the gl drawable targeted.
     */
    protected void updateSelectionMarkerDisplayList(Projection proj, GLAutoDrawable gld)
    {
         GL2 gl = (GL2) gld.getGL();
         displayListsBuf.put(SELECTION_MARKER_POS, 
                            (gl.glIsList(displayListsBuf.get(SELECTION_MARKER_POS))) ?
                            displayListsBuf.get(SELECTION_MARKER_POS) : gl.glGenLists(1));
                         
        gl.glNewList(displayListsBuf.get(SELECTION_MARKER_POS), GL2.GL_COMPILE);
        // Pushes the name for RenderSelection mode.
        gl.glPushName(getRenderSelectionName() + 1 + SELECTION_MARKER_POS);
        if (isSelected()) {
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glPushMatrix();
            gl.glScaled(getSymbolScale(), getSymbolScale(), getSymbolScale());
            gl.glBegin(GL2.GL_LINE_LOOP);
            gl.glColor4dv(SELECTION_COLOR, 0); 
            gl.glVertex2d(-(horizontalSymbolSize/2 + 1), -(verticalSymbolSize/2 + 1));
            gl.glVertex2d(-(horizontalSymbolSize/2 + 1), (verticalSymbolSize/2 + 1));
            gl.glVertex2d((horizontalSymbolSize/2 + 1), (verticalSymbolSize/2 + 1));
            gl.glVertex2d((horizontalSymbolSize/2 + 1), -(verticalSymbolSize/2 + 1));
            gl.glEnd();
            gl.glPopMatrix();
        }
        gl.glPopName();
        gl.glEndList();
         isSelectionMarkerUpdated = true;
    }

    /**
     * Updates (recreates) the displayList that draws the outline
     * frame if the mapElement this adapter represents is outlined.
     *
     * @param proj the projection that maps lat and long into GL coordinates.
     * @param gld the gl drawable targeted.
     */
    protected void updateOutlineMarkerDisplayList(Projection proj, GLAutoDrawable gld)
    {
         GL2 gl = (GL2) gld.getGL();
         displayListsBuf.put(OUTLINE_MARKER_POS, 
                            (gl.glIsList(displayListsBuf.get(OUTLINE_MARKER_POS))) ?
                            displayListsBuf.get(OUTLINE_MARKER_POS) : gl.glGenLists(1));
                            
        gl.glNewList(displayListsBuf.get(OUTLINE_MARKER_POS), GL2.GL_COMPILE);
        // Pushes the name for RenderSelection mode.
        gl.glPushName(getRenderSelectionName() + 1 + OUTLINE_MARKER_POS);
        if (isOutlined()) {
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glPushMatrix();
            gl.glScaled(getSymbolScale(), getSymbolScale(), getSymbolScale());
            gl.glEnable(GL2.GL_LINE_STIPPLE);
            gl.glPushAttrib (GL2.GL_LINE_BIT);
            gl.glLineStipple(3, (short)0xAAAA);
            gl.glBegin(GL2.GL_LINE_LOOP);
            gl.glColor3d(0.0, 0.0, 0.0); 
            gl.glVertex2d(-(horizontalSymbolSize/2 + 1), -(verticalSymbolSize/2 + 1));
            gl.glVertex2d(-(horizontalSymbolSize/2 + 1), (verticalSymbolSize/2 + 1));
            gl.glVertex2d((horizontalSymbolSize/2 + 1), (verticalSymbolSize/2 + 1));
            gl.glVertex2d((horizontalSymbolSize/2 + 1), -(verticalSymbolSize/2 + 1));
            gl.glEnd();
            gl.glPopAttrib ();
            gl.glPopMatrix();
        }
        gl.glPopName();
        gl.glEndList();
         isOutlineMarkerUpdated = true;
    }

    /**
     * Updates (recreates) the displayList that draws the location
     * of the object this adapter represents is selected.
     * 
     * @param proj the projection that maps lat and long into GL coordinates.
     * @param gld the gl drawable targeted.
     */
    protected void updateLocationDisplayList(Projection proj, GLAutoDrawable gld)
    {
         final GL2 gl = (GL2) gld.getGL();
         final GLU glu = new GLU();
         displayListsBuf.put(LOCATION_POS,
                            (gl.glIsList(displayListsBuf.get(LOCATION_POS))) ?
                            displayListsBuf.get(LOCATION_POS) : gl.glGenLists(1));
        
        gl.glNewList(displayListsBuf.get(LOCATION_POS), GL2.GL_COMPILE);
        // Pushes the name for RenderSelection mode.       
        //gl.glPushName(getRenderSelectionName() + 1 + LOCATION_POS);
        // Hack to avoid location in picking.
        gl.glPopName();
        // Shape in absolute coordinates for now
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        // Use the same quadric and tess for all operations.
        GLUquadric quadric = null;
        GLUtessellator tess = null; 
        
        double projectedPosition[] = proj.projToXY(getLonLat());
        gl.glTranslated(-projectedPosition[0], -projectedPosition[1], 0);
        
        if (getDrawLocation()) {
            // Prepare stencil
            gl.glClearStencil(0);
            gl.glClear(GL2.GL_STENCIL_BUFFER_BIT);
            gl.glEnable(GL2.GL_STENCIL_TEST);
            gl.glStencilFunc(GL2.GL_ALWAYS, 1, 1);                    
            gl.glStencilOp(GL2.GL_REPLACE, GL2.GL_REPLACE, GL2.GL_REPLACE);
 
            Shape shape = (Shape) getStratmasObject().getChild("location");

            if (shape != null) {
                for (Enumeration<SimpleShape> se = shape.constructSimpleShapes().elements(); 
                     se.hasMoreElements();) {
                    SimpleShape simpleShape = se.nextElement();        
                    if (simpleShape instanceof Circle) {
                        if (quadric == null) {
                            quadric = glu.gluNewQuadric();
                        }
                        Circle circle = (Circle) simpleShape;
                        // Get center
                        double centerLat = circle.getCenter().getLat();
                        double centerLon = circle.getCenter().getLon();        
                        // The radius in degrees
                        double latDistance = circle.getRadius() / 111000;
                        double[] centerPoint = proj.projToXY(centerLon, centerLat);
                        double[] periferPoint = 
                            proj.projToXY(centerLon, 
                                          centerLat + 
                                          (centerLat < 0 ? -1 : 1) * latDistance);
                        double projXDist = periferPoint[0] - centerPoint[0];
                        double projYDist = periferPoint[1] - centerPoint[1];
                        double radius = Math.sqrt(projXDist*projXDist + 
                                                  projYDist*projYDist);
                        gl.glTranslated(centerPoint[0], centerPoint[1], 0.0d);
                        glu.gluQuadricDrawStyle(quadric, GLU.GLU_FILL);
                        gl.glColor4d(1.0d, 1.0d, 1.0d, 0.0d);
                        glu.gluDisk(quadric, 0.0d, radius, 20, 20);
                        gl.glTranslated(-centerPoint[0], -centerPoint[1], 0.0d);
                    } else {
                        if (tess == null) {
                            tess = getLocationTessellator(gld);
                        }
                        GLU.gluBeginPolygon(tess);
                        for (Enumeration le = simpleShape.getPolygon(100.0).getCurves();
                             le.hasMoreElements();) {
                            Line line = (Line) le.nextElement();

                            double[] p1 = proj.projToXY(line.getStartPoint());
                            double[] p2 = proj.projToXY(line.getEndPoint());
                            double[] v1 = {p1[0], p1[1], 0};
                            double[] v2 = {p2[0], p2[1], 0};
                            GLU.gluTessVertex(tess, v1, 0, v1);
                            GLU.gluTessVertex(tess, v2, 0, v2);
                        }
                        GLU.gluNextContour(tess, GLU.GLU_UNKNOWN);
                        GLU.gluEndPolygon(tess);
                    }
                }

                // Draw pseudo distribution on bounding box using the stencil.
                gl.glStencilFunc(GL2.GL_EQUAL, 1, 1);
                gl.glStencilOp(GL2.GL_KEEP, GL2.GL_KEEP, GL2.GL_KEEP);
                BoundingBox boundingBox = shape.getBoundingBox();
                double[] max = proj.projToXY(boundingBox.getEastLon(), 
                                             boundingBox.getNorthLat());
                double[] min = proj.projToXY(boundingBox.getWestLon(), 
                                             boundingBox.getSouthLat());
                // FIXME: temporary fix for render selection not
                // seeming to care about stencil buffer.
                //                  gl.glPopName();
                //                  gl.glPopName();
                drawDistribution(gl, glu, min, max, proj);
                //gl.glPushName(getRenderSelectionName());
                //gl.glPushName(getRenderSelectionName() + 1 + LOCATION_POS);
                gl.glDisable(GL2.GL_STENCIL_TEST);
            }
        }
        if(getDrawLocationOutline()) {
            for (Enumeration se = ((Shape) getStratmasObject().getChild("location")).constructSimpleShapes().elements(); 
                 se.hasMoreElements();) {
                SimpleShape simpleShape = (SimpleShape) se.nextElement();
                double[] outlineColor = getOutlineColor();
                gl.glColor4d(outlineColor[0], outlineColor[1], outlineColor[2], getSymbolOpacity());
                gl.glLineWidth(1.0f);
                if (simpleShape instanceof Circle) {
                    if (quadric == null) {
                        quadric = glu.gluNewQuadric();
                    }
                    Circle circle = (Circle) simpleShape;
                    // Get center
                    double centerLat = circle.getCenter().getLat();
                    double centerLon = circle.getCenter().getLon();        
                    // The radius in degrees
                    double latDistance = circle.getRadius() / 111000;
                    double[] centerPoint = proj.projToXY(centerLon, centerLat);
                    double[] periferPoint = 
                        proj.projToXY(centerLon, 
                                      centerLat + 
                                      (centerLat < 0 ? -1 : 1) * latDistance);
                    double projXDist = periferPoint[0] - centerPoint[0];
                    double projYDist = periferPoint[1] - centerPoint[1];
                    double radius = Math.sqrt(projXDist*projXDist + 
                                              projYDist*projYDist);
                    gl.glTranslated(centerPoint[0], centerPoint[1], 0.0d);
                    glu.gluQuadricDrawStyle(quadric, GLU.GLU_SILHOUETTE);
                    glu.gluDisk(quadric, 0.0d, radius, 20, 20);
                    gl.glTranslated(-centerPoint[0], -centerPoint[1], 0.0d);
                } else {
                    gl.glBegin(GL2.GL_LINES);
                    for (Enumeration le = simpleShape.getPolygon(100.0).getCurves();
                     le.hasMoreElements();) {
                        Line line = (Line) le.nextElement();
                        gl.glVertex2dv(proj.projToXY(line.getStartPoint()), 0);
                        gl.glVertex2dv(proj.projToXY(line.getEndPoint()), 0);
                    }
                    gl.glEnd();
                }
            }
        }

        // Clean up.
        if (quadric != null) {
            glu.gluDeleteQuadric(quadric);
        }
        if (tess != null) {
            GLU.gluDeleteTess(tess);
        }

        //gl.glPopName();
        gl.glPushName(getRenderSelectionName());
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPopMatrix();
        gl.glEndList();
        isLocationUpdated = true;
    }

    /**
     * Temporary distribution drawing
     * FIX: alpha
     *
     * @param gl the gl context
     * @param glu the glu context
     * @param min the minimum point of the bounding box.
     * @param max the maximum point of the bounding box.
     * @param proj the projection to use.
     */
    void drawDistribution(GL gl2, GLU glu, double[] min, double[] max, 
                          Projection proj)
    {
        GL2 gl = (GL2) gl2;
        double[] color = getLocationColor();
        proj.projToXY(getLonLat());

        StratmasObject distribution = getObject().getChild("deployment");
        if (distribution == null) {
            gl.glColor4d(color[0], color[1], color[2], 0.2d * getLocationOpacity());  
            gl.glRectdv(min, 0, max, 0);
        } else if (distribution.getType().canSubstitute("NormalDistribution") ||
                   distribution.getType().canSubstitute("StratmasCityDistribution")) {            

            StratmasObject std = distribution.getChild("sigmaMeters");
            double sigmaMeters;
            if (std != null && std instanceof StratmasDecimal) {
                sigmaMeters = ((StratmasDecimal) std).getValue();
            } else {
                // Nice default.
                sigmaMeters = 20000;
            }
            // Rescale to gl coordinates:
            // Sigma in degrees
            double latSigma = sigmaMeters / 111000;
            double centerLon = getLon();
            double centerLat = getLat();
            double[] centerPoint = proj.projToXY(centerLon, centerLat);
            double[] periferPoint = 
                proj.projToXY(centerLon, 
                              centerLat + 
                              (centerLat < 0 ? -1 : 1) * latSigma);
            double projXDist = periferPoint[0] - centerPoint[0];
            double projYDist = periferPoint[1] - centerPoint[1];

            // Sigma in screen coordinates
            double sigma = Math.sqrt(projXDist*projXDist + 
                                     projYDist*projYDist);

            // Color with alpha field.
            double[] adjustedColor = new double[] {color[0], color[1], color[2], 0.0d};

            int xtiles = 25;
            int ytiles = 25;
            double xspan = (max[0] - min[0]);
            double yspan = (max[1] - min[1]);
            double dx = xspan / xtiles;
            double dy = yspan / ytiles;

            double[] lowerRow = new double[(xtiles + 1) * 3];
            double[] upperRow = new double[(xtiles + 1) * 3];

            double x = 0;
            for (int j = 0; j < lowerRow.length; j+=3) {
                lowerRow[j] = x;
                lowerRow[j + 1] = 0.0d;
                // This function modifies lowerRow[j + 2]
                normalF(lowerRow, j, sigma, xspan, yspan);

                upperRow[j] = x;
                upperRow[j + 1] = dy;
                // This function modifies lowerRow[j + 2]
                normalF(upperRow, j, sigma, xspan, yspan);
                x += dx;
            }

            double alphaScale = getLocationOpacity();

// Wireframe debug rows
//              gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
//              gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_LINE);
//              gl.glPolygonMode(GL2.GL_BACK, GL2.GL_LINE);
            gl.glTranslated(min[0], min[1], 0);
            gl.glBegin(GL2.GL_TRIANGLE_STRIP);
            for (int i = 0; i < ytiles; i++) {
                if (i % 2 == 0) {
                     for (int j = 0; j < lowerRow.length; j += 3) {                        
                        // Use values in lower row
                        adjustedColor[3] = alphaScale*lowerRow[j + 2];
                        gl.glColor4dv(adjustedColor, 0);
                        gl.glVertex2dv(lowerRow, j);
                        // Then recalculate them for use as upper row
                        // in next iteration. Note that x does not
                        // need to be recalculated.
                        lowerRow[j + 1] = upperRow[j + 1] + dy;
                        // This function modifies lowerRow[j + 2]
                        normalF(lowerRow, j, sigma, xspan, yspan);
                        
                        // Use values in upper row, these values will
                        // be lower row next iteration.
                        adjustedColor[3] = alphaScale*upperRow[j + 2];
                        gl.glColor4dv(adjustedColor, 0);
                        gl.glVertex2dv(upperRow, j);
                    }
                } else {
                    for (int j = lowerRow.length - 3; j >= 0; j -= 3) {
                        // See comments in i%2 == 0 case
                        adjustedColor[3] = alphaScale*lowerRow[j + 2];
                        gl.glColor4dv(adjustedColor, 0);
                        gl.glVertex2dv(lowerRow, j);
                        lowerRow[j + 1] = upperRow[j + 1] + dy;
                        // This function modifies lowerRow[j + 2]
                        normalF(lowerRow, j, sigma, xspan, yspan);

                        adjustedColor[3] = alphaScale*upperRow[j + 2];
                        gl.glColor4dv(adjustedColor, 0);
                        gl.glVertex2dv(upperRow, j);
                    }
                    
                }
                // Swith upper and lower row.
                double[] temp = upperRow;
                upperRow = lowerRow;
                lowerRow = temp;
            }
            gl.glEnd();
            gl.glTranslated(-min[0], -min[1], 0);

        } else if (distribution.getType().canSubstitute("UniformDistribution") || 
                   distribution.getType().canSubstitute("RandomUniformDistribution")) {
            gl.glColor4d(color[0], color[1], color[2], 0.5d * getLocationOpacity());  
            gl.glRectdv(min, 0, max, 0);
        } else {
            // Unknown distribution
            Debug.err.println("Unknown distribution in " + getClass());
        }
    }

    /**
     * TODO
     * span is the length of the diagonal of the box.
     * x is the x component of the point.
     */
    public void normalF(double[] values, int i, double sigma, double xspan, double yspan) 
    {
        double x = (values[i] - xspan/2);///xspan;
        double y = (values[i + 1] - yspan/2);///yspan;

        values[i + 2] = normalDist(sigma, Math.sqrt(x*x + y*y));
    }

    /**
     * The exponential
     * 
     * @param sigma the sigma.
     * @param distance distance from origo.
     */
    public double normalDist(double sigma, double distance)
    {
        double res = //(1.0d/sigma*sqrt2pi) * 
            Math.exp(-(distance * distance)/(2*sigma*sigma));
        return res;
    }

    /**
     * Sets whether to draw location or not.
     *
     * @param flag true if locatione should be drawn.
     */
    public void setDrawLocation(boolean flag)
    {
        if (getDrawLocation() != flag) {
            this.drawLocation = flag;
            isLocationUpdated = false;
            fireAdapterUpdated();
        }        
    }
    
    /**
     * Sets whether to draw location outline.
     *
     * @param flag true if location outline should be drawn.
     */
    public void setDrawLocationOutline(boolean flag)
    {
        if (getDrawLocationOutline() != flag) {
            this.drawLocationOutline = flag;
            isLocationUpdated = false;
            fireAdapterUpdated();
        }
    }
    
    /**
     * Return true if location outline is drawn.
     */
    public boolean getDrawLocationOutline()
    {
        return this.drawLocationOutline;
    }

    /**
     * Return true if location is drawn.
     */
    public boolean getDrawLocation()
    {
        return this.drawLocation;
    }
    
    /**
     * Returns the tessellator to use for drawing the location of this
     * MapElementAdapter.
     *
     * @param gld the glDrawable context to use.
     */
    protected GLUtessellator getLocationTessellator(GLAutoDrawable gld)
    {
        GLUtessellator tess = GLU.gluNewTess();
        GLUtessellatorCallback adapter = getLocationTessellatorCallback(gld);
        GLU.gluTessProperty(tess, GLU.GLU_TESS_BOUNDARY_ONLY, GLU.GLU_FALSE);
        GLU.gluTessProperty(tess, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_ODD);
        GLU.gluTessCallback(tess, GLU.GLU_TESS_BEGIN, adapter);
        GLU.gluTessCallback(tess, GLU.GLU_TESS_VERTEX, adapter);
        GLU.gluTessCallback(tess, GLU.GLU_TESS_END, adapter);
        GLU.gluTessCallback(tess, GLU.GLU_TESS_ERROR, adapter);
        
        return tess;
    }

    /**
     * Returns the tessellator callback to use for this StratmasElementAdapter
     *
     * @param gld the glDrawable context to use.
     */
    protected GLUtessellatorCallback getLocationTessellatorCallback(GLAutoDrawable gld)
    {
        final GL2 gl = (GL2) gld.getGL();

        return new GLUtessellatorCallbackAdapter() 
            {
                public void vertex(Object data) 
                {
                    double[] p = (double[]) data;
                    gl.glColor4d(0.2d, 0.2d, 0.2d, 0.2);
                    gl.glVertex2dv(p, 0);
                }
                public void begin(int type)
                {
                    gl.glBegin(type);
                }
                public void end() 
                {
                    gl.glEnd();
                }
            };
    }

    /**
     * Invalidates all lists (useful for GL switches)
     */
    public void invalidateAllLists()
    {
        this.isSymbolUpdated = false;
        this.isSelectionMarkerUpdated = false;
        this.isOutlineMarkerUpdated = false;
        this.isLocationUpdated = false;
        this.displayListUpdated = false;
        
        fireAdapterUpdated();
    }

    /**
     * Invalidates all lists (useful for GL switches)
     */
    public void invalidateSymbolList()
    {
        this.isSymbolUpdated = false;
        fireAdapterUpdated();
    }
    
    /**
     * Returns the number of renderSelectionNames needed for this adapter.
     */
    public int getNrOfRenderSelectionNames() {
        return NR_RENDER_SELECTION_NAMES;
    }

    /**
     * Redraws recompiles display lists.
     *
     * @param proj the projection that maps lat and long into GL coordinates.
     * @param gld the drawable to use.
     */
    public void reCompile(Projection proj, GLAutoDrawable gld)
    {
        if (!isSymbolUpdated) {
            updateSymbolDisplayList(proj, gld);
        }
        if (!isOutlineMarkerUpdated) {
            updateOutlineMarkerDisplayList(proj, gld);
        }
        if (!isSelectionMarkerUpdated) {
            updateSelectionMarkerDisplayList(proj, gld);
        }
        if (!isLocationUpdated) {
            updateLocationDisplayList(proj, gld);
        }
        if (!displayListUpdated) {
            updateDisplayList(proj, gld);
        }        
    }

    /**
     * Returns the longitude and latitude of the center of the
     * position of the object this adapter adapts, or NaN, NaN if no 
     *
     * @return [lon, lat]
     */
    protected double[] getLonLat()
    {
        StratmasObject walker = stComp;
        while (walker != null && walker.getChild("location") == null) {
            walker = walker.getParent();
        }
        
        if (walker != null) {
            BoundingBox box = ((Shape) walker.getChild("location")).getBoundingBox();
            return  new double[] {box.getWestLon() + (box.getEastLon() - box.getWestLon()) / 2,
                                  box.getSouthLat() + (box.getNorthLat() - box.getSouthLat()) / 2};
        } else {
            Debug.err.println("Should not be here!");
            return new double[] {0.0d, 0.0d};
        }

    }
    
    /**
     * Returns the latitiude of the center of the position of the object this adapter adapts.
     */
    protected double getLat()
    {
        StratmasObject walker = stComp;
        while (walker != null && walker.getChild("location") == null) {
            walker = walker.getParent();
        }

        if (walker != null) {
            BoundingBox box = ((Shape) walker.getChild("location")).getBoundingBox();
            return  box.getSouthLat() + (box.getNorthLat() - box.getSouthLat()) / 2;
        } else {
            Debug.err.println("Should not be here!");
            return 0.0d;
        }

    }

    /**
     * Returns the longitude of the center of the position of the object this adapter adapts.
     */
    protected double getLon()
    {
        StratmasObject walker = stComp;
        while (walker != null && walker.getChild("location") == null) {
            walker = walker.getParent();
        } 
        if (walker != null) {
            BoundingBox box = ((Shape) walker.getChild("location")).getBoundingBox();
            return  box.getWestLon() + (box.getEastLon() - box.getWestLon()) / 2;
        } else {
            Debug.err.println("Should not be here!");
            return 0.0d;
        }
    }
    
    /**
     * Sets the symbol opacity of this MapElementAdapter to the specified value
     * (between 0.0 and 1.0 inclusive);
     *
     * @param symbolOpacity the new opacity.
     */
    public void setSymbolOpacity(double symbolOpacity)
    {
        if (this.symbolOpacity != symbolOpacity) {
            this.symbolOpacity = symbolOpacity;
            isSymbolUpdated = false;
            isLocationUpdated = false;
            fireAdapterUpdated();
        }
    }

    /**
     * Returns the symbol opacity of this MapElementAdapter.
     */
    public double getSymbolOpacity()
    {
        return this.symbolOpacity;
    } 
    

    /**
     * 
     */
    public boolean getInvariantSymbolSize()
    {
        return this.invariantSymbolSize;
    }

    /**
     * Sets whether symbol-size should be invariant with regard to map
     * scale.
     *
     * @param flag true if symbol size should be invariant
     */
    public void setInvariantSymbolSize(boolean flag)
    {
        if (this.invariantSymbolSize != flag) {
            this.invariantSymbolSize = flag;
            invalidateSymbolList();
        }
    }


    /**
     * Sets the location opacity of this MapElementAdapter to the specified value
     * (between 0.0 and 1.0 inclusive);
     *
     * @param locationOpacity the new opacity.
     */
    public void setLocationOpacity(double locationOpacity)
    {
        if (this.locationOpacity != locationOpacity) {
            this.locationOpacity = locationOpacity;
            isLocationUpdated = false;
            fireAdapterUpdated();
        }
    }

    /**
     * Returns the location opacity of this MapElementAdapter.
     */
    public double getLocationOpacity()
    {
        return this.locationOpacity;
    }

    /**
     * Sets the symbol scale of this MapElementAdapter to the specified value
     * (between 0.0 and 1.0 inclusive);
     *
     * @param symbolScale the new opacity.
     */
    public void setSymbolScale(double symbolScale)
    {
        if (this.symbolScale != symbolScale) {
            this.symbolScale = symbolScale;
            isSymbolUpdated = false;
            isSelectionMarkerUpdated = false;
            fireAdapterUpdated();
        }
    }

    /**
     * Returns the symbol scale of this MapElementAdapter.
     */
    public double getSymbolScale()
    {
        return this.symbolScale;
    }

    /**
     * Returns the symbol horizontal size of this MapElementAdapter.
     */
    public double getHorizontalSymbolSize()
    {
        return this.horizontalSymbolSize;
    }

    /**
     * Returns the symbol vertical size of this MapElementAdapter.
     */
    public double getVerticalSymbolSize()
    {
        return this.verticalSymbolSize;
    }

    /**
     * Called when the object this adapter adapts changes.
     *
     * @param event the event causing the call.
     */
    public void eventOccured(StratmasEvent event)
    {
        if (event.isChildChanged()) {
            childChanged(event);
        } else if (event.isRemoved()) {
            getObject().removeEventListener(this);
            fireAdapterRemoved();
        } else if (event.isSelected()) {
            setSelected(true);
        } else if (event.isUnselected()) {
            setSelected(false);
        } else if (event.isReplaced()) {
            throw new AssertionError("Replace behavior not implemented");
        } 
    }

    /**
     * Updates this adapter when one of the adapted objects children changes.
     *
     * @param event the event causing the change.
     */
    protected void childChanged(StratmasEvent event)
    {
        StratmasObject child = (StratmasObject) event.getArgument();
        if (child.getIdentifier().equals("location")) {
            displayListUpdated = false;
            isLocationUpdated = false;
            fireAdapterUpdated();
        }
    }

    /**
     * Make adapter changes caused by selection state changes. This is
     * protected for a reason, DO NOT USE THIS to set the selection
     * status of the object adapted.
     *
     * @param selected true if it's selected.
     */
    protected void setSelected(boolean selected) 
    {
        if (this.selected != selected) {
            this.selected = selected;
            isSelectionMarkerUpdated = false;
            displayListUpdated = false;
            fireAdapterUpdated();
        }
    }
    
    /**
     * Make adapter changes caused by outline state changes.
     *
     * @param outlined true if it's outlined.
     */
    public void setOutlined(boolean outlined) 
    {
        if (this.outlined != outlined) {
            this.outlined = outlined;
            isOutlineMarkerUpdated = false;
            displayListUpdated = false;
            fireAdapterUpdated();
        }
    }
      
    /**
     * Returns true if the object adapted is currently selected,
     * otherwise false.
     */
    public boolean isSelected() 
    {
        return selected;
    }
    
    /**
     * Returns true if the object adapted is currently outlined,
     * otherwise false.
     */
    public boolean isOutlined() 
    {
        return outlined;
    }
    
    /**
     * Indicates that the symbol has to be updated.
     */
    public void updateSymbol() {
        isSymbolUpdated = false;
    }

    /**
     * Returns a color used to draw outline of the location of an adapted object.
     */
    public double[] getOutlineColor() {
        return getLocationColor();
    }

    /**
     * Returns a color used to draw the location of an adapted object
     */
    public double[] getLocationColor()
    {
        StratmasObject code = getObject().getChild("symbolIDCode");
        if (code != null && code instanceof SymbolIDCode) {
            return getLocationColor((SymbolIDCode) code);
        } else {
            // Default
            return new double[] {25.0/255.0, 25.0/255.0, 25.0/255.0};
        }
    }
    
    /**
     * Returns the correct color for each App6A affiliation 
     * @param code the symbolIdCode
     */
    public static double[] getLocationColor(SymbolIDCode code)
    {
        String affString = null;

        if (code == null || 
            code.valueToString().length() < 2) {
            affString = "-";
        } else {
            affString = code.valueToString().substring(1, 2);
        }

        if (affString.equals("A") || affString.equals("F")) {
            // Friend / Assumed Friend
            return new double[] {128.0/255.0, 224.0/255.0, 255.0/255.0};
        } else if (affString.equals("U") || affString.equals("P")) {
            // Unknown / Pending
            return new double[] {255.0/255.0, 255.0/255.0, 128.0/255.0};
        } else if (affString.equals("S") || affString.equals("H") ||
                   affString.equals("J") || affString.equals("K")) {
            // Suspect / Hostile / Joker / Faker
            return new double[] {255.0/255.0, 128.0/255.0, 128.0/255.0};
        } else if (affString.equals("N")) {
            // Neutral
            return new double[] {170.0/255.0, 255.0/255.0, 170.0/255.0};
        } else {
            // None specified (not specified in the standard either, make it light gray)
            return new double[] {40.0/255.0, 40.0/255.0, 40.0/255.0};
        }
    }
 }
