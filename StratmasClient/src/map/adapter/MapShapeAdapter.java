package StratmasClient.map.adapter;

import java.awt.Color;
import javax.swing.event.EventListenerList;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;
import java.util.EventListener;

import StratmasClient.object.Point;
import StratmasClient.object.Line;
import StratmasClient.object.Polygon;
import StratmasClient.object.Circle;
import StratmasClient.object.Shape;
import StratmasClient.object.SimpleShape;
import StratmasClient.object.Composite;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasList;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.StratmasEventListener;
import StratmasClient.filter.StratmasObjectAdapter;
import StratmasClient.map.Projection;
import StratmasClient.substrate.SubstrateEditor;
import StratmasClient.substrate.ShapeValuePair;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLUtessellator;
import javax.media.opengl.glu.GLUtessellatorCallbackAdapter;
import javax.media.opengl.glu.GLUtessellatorCallback;

/**
 * This adapter adapts Shape for use on the map.
 *
 * @author Amir Filipovic
 */
public class MapShapeAdapter extends MapDrawableAdapter {
    /**
     * Display list for the shape lines.
     */
    protected int shapeLinesDisplayList;
    /**
     * Display list for the shape area.
     */
    protected int shapeAreaDisplayList;
    /**
     * The default color of the lines.
     */
    public static Color DEFAULT_LINE_COLOR = new Color(0.2f, 0.2f, 0.2f);
    /**
     * The default width of the lines.
     */
    public static float DEFAULT_LINE_WIDTH = 1.0f;
    /**
     * The color of the shape lines.
     */
    private Color lineColor = DEFAULT_LINE_COLOR;
    /**
     * The width of the shape lines.
     */
    private float lineWidth = DEFAULT_LINE_WIDTH;
    /**
     * The color of the shape area.
     */
    private Color fillColor = new Color(0, 0, 0, 0);
    /**
     * Indicates if the shape is visible.
     */
    private boolean shapeVisible = true;
    /**
     * The number of Render Selection names needed by this adapter.
     */
    public static int NR_RENDER_SELECTION_NAMES = 1;
    /**
     * The list of ShapeColorPair objects. Each object contains an intersecting shape with its color value.
     */
    private Vector intersectingShapes = new Vector();
    
    /**
     * Creates new adapter.
     *
     * @param shape the object to adapt.
     */
    protected MapShapeAdapter(Shape shape) {
        super(shape);
    }

    /**
     * Creates new adapter.
     *
     * @param shape the object to adapt.
     * @param renderSelectionName the integer to use as the base for names in RENDER_SELECTION.
     */
    protected MapShapeAdapter(Shape shape, int renderSelectionName) {
        super(shape);
        setRenderSelectionName(renderSelectionName);
    }
    
    /**
     * Updates (recreates) the display list that draws the entire object.
     *
     * @param proj the actual projection.
     * @param gld the gl drawable targeted.
     */
    protected void updateDisplayList(Projection proj, GLAutoDrawable gld) {
        GL2 gl = (GL2) gld.getGL();
        // update the display list
         displayList = (gl.glIsList(displayList)) ? displayList : gl.glGenLists(1);
        gl.glNewList(displayList, GL2.GL_COMPILE);
        // shape display list
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        // pushes the name for RenderSelection mode        
        gl.glPushName(getRenderSelectionName());
        // draw polygon
        fillShape(gld, proj);
        if (shapeVisible) {
            drawShapeLines(gld, proj);
        }
        gl.glPopName();
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPopMatrix();
        gl.glEndList();
        displayListUpdated = true;
    }
    
    /**
     * Updates (recreates) the display list that draws the shape lines.
     *
     * @param proj the actual projection.
     * @param gld the gl drawable targeted.
     */
    protected void updateShapeLinesDisplayList(Projection proj, GLAutoDrawable gld) {
        GL2 gl = (GL2) gld.getGL();
        // update the display list
         shapeLinesDisplayList = (gl.glIsList(shapeLinesDisplayList)) ? shapeLinesDisplayList : gl.glGenLists(1);
        gl.glNewList(shapeLinesDisplayList, GL2.GL_COMPILE);
        // shape display list
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        // draw shape lines
        drawShapeLines(gld, proj);
            gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPopMatrix();
        gl.glEndList();
    }
    
    /**
     * Updates (recreates) the display list that draws the shape area.
     *
     * @param proj the actual projection.
     * @param gld the gl drawable targeted.
     */
    protected void updateShapeAreaDisplayList(Projection proj, GLAutoDrawable gld) {
        GL2 gl = (GL2) gld.getGL();
        // update the display list
         shapeAreaDisplayList = (gl.glIsList(shapeAreaDisplayList)) ? shapeAreaDisplayList : gl.glGenLists(1);
        gl.glNewList(shapeAreaDisplayList, GL2.GL_COMPILE);
        // shape display list
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        // fill the shape with color 
        fillShape(gld, proj);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPopMatrix();
        gl.glEndList();
    }
    
    /**
     * Draws the lines of the shape. 
     *
     * @param gld     interface to the OpenGL routines.
     * @param proj    the actual projection.
     */
    protected void drawShapeLines(GLAutoDrawable gld, Projection proj) {
        // draw polygon lines
        if (stComp instanceof Polygon) {
            drawPolygonLines(gld, proj, (Polygon)stComp);
        }
        // draw circle lines
        else if (stComp instanceof Circle) {
            drawCircleLines(gld, proj, (Circle)stComp);
        }
        // draw composite shape lines
        else if (stComp instanceof Composite) {
            drawCompositeLines(gld, proj, (Composite)stComp);
        }
    }
    
    /**
     * Fills the shape with color. 
     *
     * @param gld     interface to the OpenGL routines.
     * @param proj    the actual projection.
     */
    protected void fillShape(GLAutoDrawable gld, Projection proj) {
        // fill the polygon with color 
        if (stComp instanceof Polygon) {
            fillPolygon(gld, proj, (Polygon)stComp);
        }
        // fill the circle with color
        else if (stComp instanceof Circle) {
            fillCircle(gld, proj, (Circle)stComp);
        }
        // fill the composite shape with color
        else if (stComp instanceof Composite) {
            fillComposite(gld, proj, (Composite)stComp);
        }
    }

    /**
     * Draws the lines of the polygon. 
     *
     * @param gld     interface to the OpenGL routines.
     * @param proj    the actual projection.
     * @param polygon the polygon to draw.
     */
    protected void drawPolygonLines(GLAutoDrawable gld, Projection proj, Polygon polygon) {
        GL2 gl = (GL2) gld.getGL();

        // draw lines of the polygon
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        // line color
        float[] cColor = lineColor.getRGBColorComponents(null);
        gl.glColor3f(cColor[0], cColor[1], cColor[2]);
        // width of the shape line
        gl.glLineWidth(lineWidth);
        gl.glBegin(GL2.GL_LINES);
        for (Enumeration e = polygon.getCurves(); e.hasMoreElements();) {
            Line line = (Line)e.nextElement();
            gl.glVertex2dv(proj.projToXY(line.getStartPoint()), 0);
            gl.glVertex2dv(proj.projToXY(line.getEndPoint()), 0);
        }
        gl.glEnd();
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPopMatrix();
    }
    
    /**
     * Fills the polygon with color. 
     *
     * @param gld     interface to the OpenGL routines.
     * @param proj    the actual projection.
     * @param polygon the polygon to draw.
     */
    protected void fillPolygon(GLAutoDrawable gld, Projection proj, Polygon polygon) {
        GL2 gl = (GL2) gld.getGL();
        GLU glu = new GLU();

        // draw tesselated polygon
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        // create new tessellator
        GLUtessellator tess = getShapeTessellator(gld, fillColor);
        // start tesselation for the polygon
        glu.gluBeginPolygon(tess);
        for (Enumeration e = polygon.getCurves(); e.hasMoreElements();) {
            Line line = (Line)e.nextElement();
            double[] xy = proj.projToXY(line.getStartPoint());
            float[] rgba = fillColor.getRGBComponents(null);
            double[] xyrgba = {xy[0], xy[1], 0, rgba[0], rgba[1], rgba[2], rgba[3]}; 
            glu.gluTessVertex(tess, xyrgba, 0, xyrgba);
        }        
        glu.gluNextContour(tess,GLU.GLU_UNKNOWN);
        glu.gluEndPolygon(tess);

        // find intersections with the intersecting shapes 
        fillPolygonIntersections(gld, proj, polygon);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPopMatrix();
    }
    
    /**
     * Finds intersections between the shape this adapter adapts and the given shapes. These intersection
     * areas are then colored with the given colors. 
     *
     * @param gld     interface to the OpenGL routines.
     * @param proj    the actual projection.
     * @param polygon the polygon to draw.
     */
    protected void fillPolygonIntersections(GLAutoDrawable gld, Projection proj, Polygon polygon) {
        GL2 gl = (GL2) gld.getGL();
        GLU glu = new GLU();
        // find intersections
        for (int i = 0; i < intersectingShapes.size(); i++) {
            Shape sh  = ((ShapeColorPair)intersectingShapes.get(i)).getShape();
            Color color = ((ShapeColorPair)intersectingShapes.get(i)).getColor();
            Polygon pol = (sh instanceof Circle)? ((Circle)sh).getPolygon(1) : ((sh instanceof Polygon)? (Polygon)sh : null);
            if (pol != null) {
                GLUtessellatorCallback adapter = getLocationTessellatorCallback(gld, color);
                // tesselate the shape intersection
                GLUtessellator tess =  getShapeIntersectionTessellator(gld, adapter);
                glu.gluTessBeginPolygon(tess, null);
                // the actual shape
                tesselateContour(proj, glu, tess, polygon, color);
                // the intersecting shape
                tesselateContour(proj, glu, tess, pol, color);
                glu.gluTessEndPolygon(tess);
            }
        }
    }
    
    /**
     * This method is used for contour tesselation from a set of points.
     */
    private void tesselateContour(Projection proj, GLU glu, GLUtessellator tess, Polygon polygon, Color color) {
        glu.gluTessBeginContour(tess);
        // the set of points
        Vector polygonPoints = polygon.getOrderedSetOfPoints();
        // check winding
        boolean ccw = getWinding(polygonPoints, proj);
        int startValue = (ccw)? 0 : polygonPoints.size() - 1;
        int endValue = (ccw)? polygonPoints.size() : -1;
        int ind = (ccw)? 1 : -1;
        for (int i = startValue; i != endValue; i = i + ind) {
            double[] xy = proj.projToXY((Point)polygonPoints.get(i));
            float[] rgba = color.getRGBComponents(null);
            double[] xyrgba = {xy[0], xy[1], 0, rgba[0], rgba[1], rgba[2], rgba[3]}; 
            glu.gluTessVertex(tess, xyrgba, 0, xyrgba);
        }
        glu.gluTessEndContour(tess);
    }
    
    /**
     * This method determines the winding of the polygon.
     * 
     * @param polyPoints the list of points in the polygon.
     *          
     * @return true if the windind is counter clockwise (CCW) false otherwise (CW).
     */
    public static boolean getWinding(Vector polyPoints, Projection proj) {
        int n = polyPoints.size();
        double area = 0;
        
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            double[] xyi = proj.projToXY((Point)polyPoints.get(i));
            double[] xyj = proj.projToXY((Point)polyPoints.get(j));
            area += xyi[0] * xyj[1];
            area -= xyj[0] * xyi[1];
        }
        area /= 2.0;
        
        return (area > 0)? true : false;
    }
    
    /**
     * Converts a circle to a polygonial and draws its lines.
     *
     * @param gld    interface to the OpenGL routines.
     * @param proj   the actual projection.
     * @param circle the circle to draw.
     */
    protected void drawCircleLines(GLAutoDrawable gld, Projection proj, Circle circle) {
        drawPolygonLines(gld, proj, circle.getPolygon(1));
    }
    
    /**
     * Converts the circle to a polygonial and fill it with color.
     *
     * @param gld    interface to the OpenGL routines.
     * @param proj   the actual projection.
     * @param circle the circle to draw.
     */
    protected void fillCircle(GLAutoDrawable gld, Projection proj, Circle circle) {
        fillPolygon(gld, proj, circle.getPolygon(1));
    }
    
    /**
     * Converts the composite into simple shapes and draws lines of those shapes.
     * 
     * @param gld  interface to the OpenGL routines.
     * @param proj the actual projection.
     * @param comp the composite to draw.
     */
    protected void drawCompositeLines(GLAutoDrawable gld, Projection proj, Composite comp) {
        Vector simpleShapes = comp.constructSimpleShapes(new Vector());
        for (Enumeration e = simpleShapes.elements(); e.hasMoreElements(); ) {
            SimpleShape sShape = (SimpleShape)e.nextElement();
            if (sShape instanceof Polygon) {
                drawPolygonLines(gld, proj, (Polygon)sShape);
            }
            else if (sShape instanceof Circle) {
                drawCircleLines(gld, proj, (Circle)sShape);
            }
        }
    }
    
    /**
     * Converts the composite into simple shapes and fills those shapes with color.
     * 
     * @param gld  interface to the OpenGL routines.
     * @param proj the actual projection.
     * @param comp the composite to draw.
     */
    protected void fillComposite(GLAutoDrawable gld, Projection proj, Composite comp) {
        Vector simpleShapes = comp.constructSimpleShapes(new Vector());
        for (Enumeration e = simpleShapes.elements(); e.hasMoreElements(); ) {
            SimpleShape sShape = (SimpleShape)e.nextElement();
            if (sShape instanceof Polygon) {
                fillPolygon(gld, proj, (Polygon)sShape);
            }
            else if (sShape instanceof Circle) {
                fillCircle(gld, proj, (Circle)sShape);
            }
        }
    }
    
    /**
     * Returns the tessellator to use for drawing the shape. 
     *
     * @param gld the glDrawable context to use.
     */
    protected GLUtessellator getShapeTessellator(GLAutoDrawable gld, Color color) {
        GLU glu = new GLU();
        
        GLUtessellator tess = glu.gluNewTess();
        GLUtessellatorCallback adapter = getLocationTessellatorCallback(gld, color);
        glu.gluTessCallback(tess, GLU.GLU_TESS_BEGIN, adapter);
        glu.gluTessCallback(tess, GLU.GLU_TESS_VERTEX, adapter);
        glu.gluTessCallback(tess, GLU.GLU_TESS_END, adapter);
        glu.gluTessCallback(tess, GLU.GLU_TESS_ERROR, adapter);
        glu.gluTessProperty(tess, GLU.GLU_TESS_BOUNDARY_ONLY, GLU.GLU_FALSE);
        glu.gluTessProperty(tess, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_ODD);
                
        return tess;
    }
    
    /**
     * Returns the tessellator to use for drawing the shape intersection.
     *
     * @param gld the glDrawable context to use.
     */
    protected GLUtessellator getShapeIntersectionTessellator(GLAutoDrawable gld, GLUtessellatorCallback adapter) {
        GLU glu = new GLU();
        
        GLUtessellator tess = glu.gluNewTess();
        glu.gluTessProperty(tess, GLU.GLU_TESS_WINDING_RULE, GLU.GLU_TESS_WINDING_ABS_GEQ_TWO);
        glu.gluTessCallback(tess, GLU.GLU_TESS_BEGIN, adapter);
        glu.gluTessCallback(tess, GLU.GLU_TESS_VERTEX, adapter);
        glu.gluTessCallback(tess, GLU.GLU_TESS_END, adapter);
        glu.gluTessCallback(tess, GLU.GLU_TESS_ERROR, adapter);
        glu.gluTessCallback(tess, GLU.GLU_TESS_COMBINE, adapter);
        
        return tess;
    }
    
    /**
     * Returns the tessellator callback to use for this MapShapeAdapter.
     *
     * @param gld the glDrawable context to use.
     */
    protected GLUtessellatorCallback getLocationTessellatorCallback(GLAutoDrawable gld,  final Color color) {
        final GL2 gl = (GL2) gld.getGL();
        
        return new GLUtessellatorCallbackAdapter() {
                public void vertex(Object data) {
                    double[] d = (double[])data;
                    gl.glColor4d(d[3], d[4], d[5], d[6]);
                    gl.glVertex2d(d[0], d[1]);
                }
                
                public void combine(double[] coords, Object[] data, float[] weight, Object[] outData) {
                    float[] col = color.getRGBComponents(null);
                    double[] vertex = new double[7];
                    
                    vertex[0] = coords[0];
                    vertex[1] = coords[1];
                    vertex[2] = 0;
                    vertex[3] = col[0];
                    vertex[4] = col[1];
                    vertex[5] = col[2];
                    vertex[6] = col[3];

                    outData[0] = vertex;
                }
                
                public void begin(int type){
                    gl.glBegin(type);
                }
                
                public void end() {
                    gl.glEnd();
                }
            };
    }
    
    /**
     * Returns the number of renderSelectionNames needed for this adapter.
     */
    public int getNrOfRenderSelectionNames() {
        return NR_RENDER_SELECTION_NAMES;
    }
    
    /**
     * Returns the display list for the shape lines of this adapter.
     */   
    public int getShapeLinesDisplayList() {
        return shapeLinesDisplayList;
    }
    
    /**
     * Returns the display list for the shape area of this adapter.
     */   
    public int getShapeAreaDisplayList() {
        return shapeAreaDisplayList;
    }

    /**
     * Updates the display lists.
     */
    public void reCompile(Projection proj, GLAutoDrawable gld) {
        if (!displayListUpdated) {
            updateDisplayList(proj, gld);        
            updateShapeAreaDisplayList(proj, gld);
            updateShapeLinesDisplayList(proj, gld);
        }
    }
    
    /**
     * Sets the shape visible or not.
     *
     * @param visible if true the shape is visible, if false it's not.
     */
    public void setShapeVisible(boolean visible) {
        shapeVisible = visible;
        displayListUpdated = false;
        fireAdapterUpdated();
    }

    /**
     * Sets the color of the shape area.
     *
     * @param areaColor the color of the shape area.
     */
    public void setShapeAreaColor(Color areaColor) {
        fillColor = areaColor;
        displayListUpdated = false;
        intersectingShapes.removeAllElements();
        fireAdapterUpdated();
    }
    
    /**
     * Returns the color of the shape area.
     */
    public Color getShapeAreaColor() {
        return fillColor;
    }

    /**
     * Sets the color of the shape area without removing the intersecting shapes.
     *
     * @param areaColor the color of the shape area.
     */
    public void setShapeAreaBackground(Color areaColor) {
        fillColor = areaColor;
        displayListUpdated = false;
        fireAdapterUpdated();
    }
    
    /**
     * Sets the color of the shape area transparent.
     */
    public void setShapeAreaTransparent() {
        fillColor = new Color(0, 0, 0, 0);
        displayListUpdated = false;
        intersectingShapes.removeAllElements();
        fireAdapterUpdated();
    }
    
    /**
     * Sets the color of the shape lines.
     *
     * @param lineColor the color of the shape lines.
     */
    public void setShapeLineColor(Color lineColor) {
        this.lineColor = lineColor;
        displayListUpdated = false;
        fireAdapterUpdated();
    }
    
    /**
     * Sets the width of the shape lines.
     *
     * @param lineWidth the width of the shape lines.
     */
    public void setShapeLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
        displayListUpdated = false;
        fireAdapterUpdated();
    }
    
    /**
     * Returns true if the lines of the shape are not colored with he default color.
     */
    public boolean isHighlighted() {
        return !lineColor.equals(DEFAULT_LINE_COLOR);
    }
    
    /**
     * Adds a shape which intersects the shape this adapter adapts in the list of shapes.
     *
     * @param shape the intersecting shape.
     * @param color the color of the intersecting shape area.
     */
    public void addIntersectingShape(Shape shape, Color color) {
        shape.addEventListener(this);
        intersectingShapes.add(new ShapeColorPair(shape, color));
        displayListUpdated = false;
        fireAdapterUpdated();
    }
    
    /**
     * Removes an intersecting shape from the list of shapes.
     */
    public void removeIntersectingShape(Shape sh) {
        sh.removeEventListener(this);
        for (int i = 0; i < intersectingShapes.size(); i++) {
            ShapeColorPair scp = (ShapeColorPair)intersectingShapes.get(i);
            if (scp.getShape().equals(sh)) {
                intersectingShapes.remove(scp);
                break;
            }
        }
        displayListUpdated = false;
        fireAdapterUpdated();
    }

    /**
     * Updates the intersecting shapes with the actual color map.
     *
     * @param createdShapeAreas the list of all created shapes and their values.
     * @param substrateEditor reference to the substrate editor. 
     */
    public void updateIntersectingShapes(Hashtable createdShapeAreas, SubstrateEditor substrateEditor) {
        for (int i = 0; i < intersectingShapes.size(); i++) {
            ShapeColorPair shp = (ShapeColorPair)intersectingShapes.get(i);
            if (createdShapeAreas.get(shp.getShape()) != null) { 
                double value = ((ShapeValuePair)createdShapeAreas.get(shp.getShape())).getValue();
                shp.setColor(substrateEditor.getMappingColor(value));
            }
        }
        displayListUpdated = false;
        fireAdapterUpdated();
    }
    
    /**
     * Returns the list of intersecting shapes.
     */
    public Vector getIntersectingShapes() {
        Vector inShapes = new Vector();
        for (int i = 0; i < intersectingShapes.size(); i++) {
            inShapes.add(((ShapeColorPair)intersectingShapes.get(i)).getShape());
        }
        return inShapes;
    }
    
    /**
     * Called when the object this adapter adapts changes.
     *
     * @param event the event causing the call.
     */
    public void eventOccured(StratmasEvent event) {
        if (event.isChildChanged()) {
            childChanged(event);
        } else if (event.isValueChanged()) {
            valueChanged(event);
        } else if (event.isRemoved()) {
            StratmasObject source = (StratmasObject)event.getSource();
            if (source.equals(getObject())) {
                getObject().removeEventListener(this);
                fireAdapterRemoved();
            }
            else if (source instanceof Shape){
                removeIntersectingShape((Shape)source);
            }
            
        } else if (event.isReplaced()) {
            throw new AssertionError("Replace behavior not implemented");
         } 
    }
    
    /**
     * This class is used to represent the intersecting shapes with their color values. 
     */
    class ShapeColorPair {
        /**
         * The actual shape.
         */
        private Shape shape;
        /**
         * The actual color of the shape area.
         */
        private Color color;
        
        /**
         * Creates new pair.
         */ 
        public ShapeColorPair(Shape shape, Color color) {
            this.shape = shape;
            this.color = color;
        }
        
        /**
         * Sets the color.
         */
        public void setColor(Color color) {
            this.color = color;
        }
        
        /**
         * Returns the color.
         */
        public Color getColor() {
            return color;
        }
        
        /**
         * Returns the shape.
         */
        public Shape getShape() {
            return shape;
        }
    }
    
}
