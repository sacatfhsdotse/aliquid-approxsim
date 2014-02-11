package StratmasClient.substrate;

import java.nio.IntBuffer;
import java.util.Stack;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Comparator;
import java.awt.Color;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTextField;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.JPopupMenu;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.awt.GLCanvas;
import com.jogamp.common.nio.Buffers;

import StratmasClient.BoundingBox;
import StratmasClient.StratmasDialog;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasObjectFactory;
import StratmasClient.object.StratmasList;
import StratmasClient.object.Shape;
import StratmasClient.object.Circle;
import StratmasClient.object.Point;
import StratmasClient.object.Line;
import StratmasClient.object.SimpleShape;
import StratmasClient.map.GeoMath;
import StratmasClient.map.BasicMap;
import StratmasClient.map.BasicMapDrawer;
import StratmasClient.map.RenderSelection;
import StratmasClient.map.Region;
import StratmasClient.map.ZoomAndScale;
import StratmasClient.map.MapPoint;
import StratmasClient.map.Projection;
import StratmasClient.map.adapter.MapDrawableAdapter;
import StratmasClient.map.adapter.MapShapeAdapter;
import StratmasClient.map.adapter.MapPointAdapter;
import StratmasClient.map.adapter.MapLineAdapter;

/**
 * This class is used to display shapes for regions and population centers as well as to create new shapes. 
 * Further on, all the shapes can be initialized by filling a shape with a color which represents a certain 
 * value. The colors are chosen by using ColorChooser. 
 *
 * @author Amir Filipovic
 */
public class SubstrateMapDrawer extends BasicMapDrawer {
    /**
     * Indicates undefined mode.
     */
    public static int SET_UNDEFINED_MODE = 0;
    /**
     * Indicates mode for setting values to shape areas.
     */
    public static int SET_AREA_VALUE_MODE = 1;
    /**
     * Indicates mode for creation of new areas.
     */
    public static int CREATE_AREA_MODE = 2;
    /**
     * Indicates mode for creation of new circle.
     */
    public static int CREATE_CIRCLE_MODE = 3;
    /**
     * Indicates mode for creation of new rectangle.
     */
    public static int CREATE_RECTANGLE_MODE = 4;
    /**
     * Indicates mode for creation of new polygon.
     */
    public static int CREATE_POLYGON_MODE = 5;
    /**
     * Indicates mode for moving a polygon point.
     */
    public static int MOVE_POINT_MODE = 6;
    /**
     * Indicates mode for inserting a polygon point.
     */
    public static int INSERT_POINT_MODE = 7;
    /**
     * Indicates mode for moving a polygon.
     */
    public static int MOVE_POLYGON_MODE = 8;
    /**
     * The current mode of the drawer.
     */
    protected int substrateMode;
    /**
     * The object used for shape creation.
     */
    protected ShapeMaker shapeMaker;
    /**
     * Last horizontal mouse coordinate for last render selection.
     */
    protected int renderSelectionMouseX = 0;
    /**
     * Last vertical mouse coordinate for last render selection.
     */
    protected int renderSelectionMouseY = 0;
    /**
     * The horizontal center coordinate for render selection.
     */
    protected double renderSelectionX;
    /**
     * The vertical center coordinate for render selection.
     */
    protected double renderSelectionY;
    /**
     * The horizontal tolerance in render selection.
     */
    protected double renderSelectionDeltaX = 1;
    /**
     * The vertical tolerance in render selection.
     */
    protected double renderSelectionDeltaY = 1;
    /**
     * The result of the latest renderSelection.
     */
    protected RenderSelection latestRenderSelection = new RenderSelection();
    /**
     * Indicates if the drawer is in panning mode.
     */
    private boolean panningMode = false; 
    /**
     * Reference to the resource editor.
     */
    private SubstrateEditor substrateEditor;
    /**
     * The currently highlighted shape.
     */
    private Shape highlightedShape;
    /**
     * The list of highlighted SimpleShape objects. 
     */
    private Vector highlightedShapes = new Vector();
    /**
     * The list of shape adapters where each shape the adapter represents is assigned a value. 
     */
    private Hashtable shapeValues = new Hashtable();
    /**
     * The sorted list of created shapes according to the time of creation.
     */
    private Stack createdShapeAreas = new Stack();
    /**
     * Used to compare the adapters. It's modified from the comparator used in the superclass
     * such that it also compares shapes depending if those are highlighted or not. 
     */
    private Comparator shapeAdaptedComparator;
    /**
     * Display lists for lines of displayed shapes that are drawn.
     */
    protected IntBuffer drawnShapeLinesListBuf = Buffers.newDirectIntBuffer(0);
    /**
     * Display lists for areas of displayed shapes that are drawn.
     */
    protected IntBuffer drawnShapeAreasListBuf = Buffers.newDirectIntBuffer(0);
    /**
     * Display lists for points that are drawn.
     */
    protected IntBuffer drawnPointsListBuf = Buffers.newDirectIntBuffer(0);
    /**
     * Display lists for lines that are drawn.
     */
    protected IntBuffer drawnLinesListBuf = Buffers.newDirectIntBuffer(0);
    
    /**
     * Creates new drawer.
     *
     * @param basicMap reference to the basic map.
     * @param region the displayed region.
     * @param substrateEditor reference to the substrate editor.
     */
    public SubstrateMapDrawer(BasicMap basicMap, Region region, SubstrateEditor substrateEditor) {
        super(basicMap, region);
        this.substrateEditor = substrateEditor; 
        
        // modify the comparator 
        shapeAdaptedComparator = new Comparator() {
                public int compare(Object o1, Object o2) {
                    MapDrawableAdapter d1 = (MapDrawableAdapter) o1;
                    MapDrawableAdapter d2 = (MapDrawableAdapter) o2;
                    if (d1 instanceof MapShapeAdapter && d2 instanceof MapShapeAdapter) {
                        if (((MapShapeAdapter)d1).isHighlighted()) {
                            return 1;
                        }
                        else {
                            return -1;
                        }
                    }
                    else {
                        return mapDrawableAdapterComparator.compare(d1, d2);
                    }
                }
            };
        
        // set zoom & scale
        setZoomAndScale(new ZoomAndScale(this, JSlider.HORIZONTAL));

        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    }
    
    /**
     * Initialization of the drawing area. Part of GLEventListener interface.
     *
     * @param gld needed for OpenGL.
     */
    public void init(GLAutoDrawable gld) {
        super.init(gld);
        initializeShapeValues();
    }
    
    /**
     * Drawing elements on the map. Part of GLEventListener interface.
     *
     * @param gld needed for OpenGL.
     */
    public void display(GLAutoDrawable gld) {
        super.display(gld);
        updateRenderSelection(gld);
    }
    
    /**
     * Updates the render selection array.
     *
     * @param gld the drawable.
     */
    protected void updateRenderSelection(GLAutoDrawable gld) {
        GL2 gl = (GL2) gld.getGL();
        
        IntBuffer renderSelectionBuffer;
        int renderSelectionBufferAllocationSize = 2048;
        
        int hits = -1;
        
        do {
            renderSelectionBuffer = Buffers.newDirectIntBuffer(renderSelectionBufferAllocationSize);
            gl.glSelectBuffer(renderSelectionBuffer.capacity(), renderSelectionBuffer);
            
            // enable render selection
            gl.glRenderMode(GL2.GL_SELECT);
            
            // init names
            gl.glInitNames();
            
            // set the selection area
            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glPushMatrix();
            gl.glLoadIdentity();
            glu.gluOrtho2D(renderSelectionX - renderSelectionDeltaX / 2, renderSelectionX + renderSelectionDeltaX / 2, 
                           renderSelectionY - renderSelectionDeltaY / 2, renderSelectionY + renderSelectionDeltaY / 2);
            
            // draw shapes
            updateDrawnMapDrawablesList();
            gl.glCallLists(drawnMapDrawablesListBuf.capacity(), gl.GL_INT, drawnMapDrawablesListBuf);
            
            // draw the actual shape
            if (shapeMaker != null && shapeMaker.getShapeAdapter() != null) {
                gl.glCallList(shapeMaker.getShapeAdapter().getDisplayList());
            }

            // restore view
            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glPopMatrix();
            gl.glFlush();
            
            // end render selection mode
            hits = ((GL2)gld.getGL()).glRenderMode(GL2.GL_RENDER);
            
            if (hits < 0) {
                // too small selectionBuffer, try double size
                renderSelectionBufferAllocationSize = renderSelectionBufferAllocationSize * 2;
            }
        } while (hits < 0);
        
        this.latestRenderSelection = new RenderSelection(hits, renderSelectionBuffer, renderSelectionNames);
    }

    /**
     * Sets selection area for subsequent renderSelection calls.
     *
     * @param x horizontal component of center in screen coordinates.
     * @param y vertical component of center in screen coordinates.
     * @param deltaX tolerance of horizontal component of center in screen coordinates.
     * @param deltaY tolerance of vertical component of center in screen coordinates.
     */
    protected void setRenderSelectionArea(int x, int y, int deltaX, int deltaY) {
        MapPoint p = convertToLonLat(x, y);
        this.renderSelectionX = p.getProjectedPoint(this.getProjection()).getX();
        this.renderSelectionY = p.getProjectedPoint(this.getProjection()).getY();
        this.renderSelectionDeltaY = convertScreenDistanceToProjectedDistance(deltaY);
        this.renderSelectionDeltaX = convertScreenDistanceToProjectedDistance(deltaX);
        
        this.renderSelectionMouseX = x;
        this.renderSelectionMouseY = y;
    }

    /**
     * Sets selection area for subsequent renderSelection calls.
     *
     * @param x horizontal component of center in screen coordinates.
     * @param y vertical component of center in screen coordinates.
     */
    protected void setRenderSelectionArea(int x, int y) {
        setRenderSelectionArea(x, y, 1, 1);
    }
    
    /**
     * Returns the render selection object.
     */
    public RenderSelection getRenderSelection() {
        return latestRenderSelection;
    }
    
    /**
     * Returns the text field which displays coordinates of the location currently pointed by the mouse pointer. 
     */
    public JTextField getCurrentLocationTextField() {
        return info_field;
    }
    
    /**
     * Returns the text field which displays the region currently pointed by the mouse pointer. 
     */
    public JTextField getCurrentRegionTextField() {
        return regionTextField;
    }
    
    /**
     * Returns the canvas where all the elements are drawn. 
     */
    public GLCanvas getGLCanvas() {
        return glc;
    }
    
    /**
     * Returns the controller for zooming and scaling.
     */
    public ZoomAndScale getZoomAndScaleController() {
        return zoom_and_scale;
    }

    /**
     * Sets the mode for panning.
     */
    public void setPanningMode(boolean panningMode) {
        this.panningMode = panningMode;
    }
    
    /**
     * Returns the mode for the panning.
     */
    public boolean getPanningMode() {
        return panningMode;
    }
    
    /**
     * Returns the shape values.
     */
    public Hashtable getShapeValues() {
        return shapeValues; 
    }
    
    /**
     * Returns sorted list of ShapeValuePair objects.
     */
    public Stack getCreatedShapeAreas() {
        return createdShapeAreas;
    }
    
    /**
     * Returns key-value pairs of the created shapes and the corresponding values.
     */
    public Hashtable getCreatedShapeValues() {
        Hashtable sAreas = new Hashtable();
        for (int i = 0; i < createdShapeAreas.size(); i++) {
            ShapeValuePair svp =  (ShapeValuePair)createdShapeAreas.get(i);
            sAreas.put(svp.getShape(), svp);
        }
        return sAreas;
    }
    
    /**
     * Sets the mode for the drawer.
     */
    public void setSubstrateMode(int substrateMode) {
        this.substrateMode = substrateMode;
    }
    
    /**
     * Returns the mode for the drawer.
     */
    public int getSubstrateMode() {
        return substrateMode;
    }
    
    /**
     * Sets the shape maker.
     */
    public void setShapeMaker(ShapeMaker shapeMaker) {
        resetShapeMaker();
        this.shapeMaker = shapeMaker;
    }
    
    /**
     * Returns the shape maker.
     */
    public ShapeMaker getShapeMaker() {
        return shapeMaker;
    }

    /**
     * Sets the scaled orthographic bounds.
     *
     * @param sbox bounding box of the scaled orthographic bounds.
     */
    public void setScaledBoundingBox(BoundingBox sbox) {
        orts_box = (BoundingBox)sbox.clone();
        
        // projection will change, so we need to update symbols if they are invariant.
        for (Enumeration e = mapDrawableAdapters.elements(); e.hasMoreElements();) {
            MapDrawableAdapter mda = (MapDrawableAdapter)e.nextElement();
            if (mda instanceof MapPointAdapter) {
                ((MapPointAdapter)mda).invalidateSymbolList();
            }
        }
        update();
    }
    
    /**
     * Creates new MapPointAdapter and adds it to the list of adapters.
     */
    protected MapPointAdapter addMapPointAdapter(Point point) {
        MapPointAdapter adapter = (MapPointAdapter)addMapDrawableAdapter(point);
        adapter.setInvariantSymbolSize(true);
        return adapter;
    }
    
    /**
     * Removes a MapPointAdapter from the list of adapters.
     */
    protected void removeMapPointAdapter(MapPointAdapter adapter) {
        removeMapDrawableAdapter(adapter);
    }
    
    /**
     * Creates new MapLineAdapter and adds it to the list of adapters.
     */
    protected MapLineAdapter addMapLineAdapter(Line line) {
        MapLineAdapter adapter = (MapLineAdapter)addMapDrawableAdapter(line);
        adapter.setLineColor(substrateEditor.getActualColor());
        return adapter;
    }
    
    /**
     * Removes a MapLineAdapter from the list of adapters.
     */
    protected void removeMapLineAdapter(MapLineAdapter adapter) {
        removeMapDrawableAdapter(adapter);
    }
    
    /**
     * Indicates if the list of adapters has to be updated.
     */
    protected void setIsDrawnMapDrawablesListUpdated(boolean updated) {
        super.setIsDrawnMapDrawablesListUpdated(updated);
    }

    /**
     * Removes the last added shape.
     */
    protected void removeLastAddedShape() {
        if (!createdShapeAreas.isEmpty()) {
            ShapeValuePair svp = (ShapeValuePair)createdShapeAreas.pop();
            svp.getShape().remove();
        }
    }
    
    /**
     * Part of MouseListener interface.
     */
    public void mouseClicked(MouseEvent e) {
        // update the current position
        MapPoint p = updateCurrentPosition((int) e.getX(), (int) e.getY());

        //left mouse button
        if (e.getButton() == MouseEvent.BUTTON1) {
            // set color to the selected region
            if (substrateMode == SET_AREA_VALUE_MODE) {
                setColorToSelectedRegion(substrateEditor.getActualColor());
            }
            // add new point to the polygon
            else if (substrateMode == CREATE_POLYGON_MODE) {
                if (!((PolygonMaker)shapeMaker).isPolygonCompleted()) {
                    ((PolygonMaker)shapeMaker).addPoint(StratmasObjectFactory.createPoint("p1", p.getLat(), p.getLon()));
                }
            }
             // insert new point into the polygon
            else if (substrateMode == INSERT_POINT_MODE) {
                Vector mlAdapters = mapDrawableAdaptersUnderCursor(MapLineAdapter.class);
                if (!mlAdapters.isEmpty()) {
                    MapLineAdapter mlAdapter = (MapLineAdapter)mlAdapters.firstElement();
                    Point newPoint = StratmasObjectFactory.createPoint("p1", p.getLat(), p.getLon());
                    ((PolygonMaker)shapeMaker).insertPoint(newPoint, mlAdapter);
                }
            }
            // move the area
            else if (substrateMode == MOVE_POLYGON_MODE) {
                if (shapeMaker != null) {
                    shapeMaker.moveShape(p.getLon(), p.getLat());
                }
            }
        }
        // right mouse button
        else if (e.getButton() == MouseEvent.BUTTON3) {
            if (substrateMode == SET_AREA_VALUE_MODE) {
                if (highlightedShape != null) {
                    highlightedShape = getParentShape(highlightedShape);
                    if (highlightedShape == null) {
                        setPointedRegion();
                    }
                    // highlight the region under the mouse cursor
                    highlightPointedRegion();
                    // display the region under the mouse cursor
                    displayHighlightedRegion();
                }
            }
            // complete the polygon by adding the last line
            else if (substrateMode == CREATE_POLYGON_MODE) {
                ((PolygonMaker)shapeMaker).addLastLine();
                ((PolygonMaker)shapeMaker).createMapShapeAdapter(substrateEditor.getActualColor());
            }
        }
        // redraw
        update();
    }
    
    /**
     * Indicates that the mouse cursor is exited the map. Part of MouseListener interface.
     *
     * @param e event created by exiting the map.
     */
    public void mouseExited(MouseEvent e) {
        mouse_on = false;
        // display current position
        displayCurrentPosition(new MapPoint(0,0));
        // display pointed region
        displayPointedRegion("");
        // reset the highlighted shapes 
        while (!highlightedShapes.isEmpty()) {
            ((MapShapeAdapter)highlightedShapes.remove(0)).setShapeLineColor(MapShapeAdapter.DEFAULT_LINE_COLOR);
        }
        // redraw
        update();
    }
    
    /**
     * Part of MouseListener interface.
     */
    public void mousePressed(MouseEvent e) {
        // update the current position
        MapPoint p = updateCurrentPosition((int) e.getX(), (int) e.getY());

        // set center of the circle
        if (substrateMode == CREATE_CIRCLE_MODE && !((CircleMaker)shapeMaker).isCircleCreated()) {
            ((CircleMaker)shapeMaker).createCircle(p.getLon(), p.getLat(), 0);
            shapeMaker.createMapShapeAdapter(substrateEditor.getActualColor());
        }
        // create new rectangular area
        else if (substrateMode == CREATE_RECTANGLE_MODE && !((RectangleMaker)shapeMaker).isRectangleCreated()) {
            ((RectangleMaker)shapeMaker).createRectangle(p.getLon(), p.getLat());
            shapeMaker.createMapShapeAdapter(substrateEditor.getActualColor());
        }
    }        
    
    /**
     * Used to pan the map when panningMode is enabled. Part of MouseMotionListener interface.
     */
    public void mouseDragged(MouseEvent e) {
        // move the map
        if (getPanningMode()) {
            MapPoint p = convertToLonLat((int) e.getX(), (int) e.getY());
            double dx = p.getProjectedPoint(getProjection()).getX() - current_pos.getProjectedPoint(getProjection()).getX();
            double dy = p.getProjectedPoint(getProjection()).getY() - current_pos.getProjectedPoint(getProjection()).getY();
            setXYCenter(ort_xc - dx, ort_yc - dy);
        }
        else {
            // update the current position
            MapPoint p = updateCurrentPosition((int) e.getX(), (int) e.getY());
            // set radius of the circle
            if (substrateMode == CREATE_CIRCLE_MODE) {
                ((CircleMaker)shapeMaker).setRadius(p.getLon(), p.getLat());
            }
            // set steering point of the rectangle 
            else if (substrateMode == CREATE_RECTANGLE_MODE) {
                ((RectangleMaker)shapeMaker).setSteerPoint(p.getLon(), p.getLat()); 
            }
            // move the polygon point
            else if (substrateMode == MOVE_POINT_MODE) {
                Vector mpAdapters = mapDrawableAdaptersUnderCursor(MapPointAdapter.class);
                if (!mpAdapters.isEmpty()) {
                    Point mPoint = (Point)((MapPointAdapter)mpAdapters.firstElement()).getObject();
                    ((PolygonMaker)shapeMaker).movePolygonPoint(mPoint, p.getLon(), p.getLat());
                }
            }
            // move the area
            else if (substrateMode == MOVE_POLYGON_MODE) {
                if (shapeMaker != null) {
                    shapeMaker.moveShape(p.getLon(), p.getLat());
                }
            }
        }
        // redraw        
        update();
    }
    
    /**
     * Upadates the curent position on the map. Part of MouseMotionListener interface. 
     *
     * @param e event created by changing the position of the mouse cursor on the map.
     */
    public void mouseMoved(MouseEvent e) {
        // update the current position
        updateCurrentPosition((int) e.getX(), (int) e.getY());
        
        // set the region currently under the mouse cursor
        boolean highlightedShapeChanged = setPointedRegion();
        
        if (highlightedShapeChanged){ 
            // display the region under the mouse cursor
            displayHighlightedRegion();
            
            if (substrateMode == SET_AREA_VALUE_MODE) {
                // highlight the region under the mouse cursor
                highlightPointedRegion();
            }
        }
        // redraw
        update();
    }
    
    /**
     * Updates the current position pointed by the mouse cursor.
     */
    private MapPoint updateCurrentPosition(int x, int y) {
        // set the render selection area
        setRenderSelectionArea(x, y);
        if (substrateMode == INSERT_POINT_MODE) {
            setRenderSelectionArea(x, y, 5, 5); 
        }
        
        // convert the current position to lon/lat
        current_pos = convertToLonLat(x, y);
        
        // display current position
        displayCurrentPosition(current_pos);
        
        // necessary for multi-screen enviroment
        mouse_on = (x >= view_x && x <= view_x + view_width && y >= view_y && y <= view_y + view_height)? true : false;
        
        return current_pos;
    }
    
    /**
     * Adds the actual shape to the list of created shapes
     */
    public void addActualShapeArea() {
        if (shapeMaker != null && shapeMaker.getShapeAdapter() != null) {
            if (!shapeMaker.isShapeSimple()) {
                showShapeErrorDialog();
            }
            else {
                Shape actualShape = (Shape)shapeMaker.getShapeAdapter().getObject();
                createdShapeAreas.push(new ShapeValuePair(actualShape, substrateEditor.getActualValue(), false));
                // check intersection with the regions
                for (Enumeration e = mapDrawableAdapters.elements(); e.hasMoreElements();) {
                    MapDrawableAdapter adapter = (MapDrawableAdapter)e.nextElement();
                    // check if it is MapShapeAdapter
                    if (adapter instanceof MapShapeAdapter) {
                        Shape shape = (Shape)adapter.getObject();
                        // check if the shape intersects with the actual one
                        if (shape.getBoundingBox().intersects(getProjection(), actualShape.getBoundingBox())) {
                            ((MapShapeAdapter)adapter).addIntersectingShape(actualShape, substrateEditor.getActualColor());
                        }
                    }
                }
            }
            resetShapeMaker();
            update();
        }
    }
    
    /**
     * Returns the list of shapes which "intersect" the given shape. Two shapes "intersect" when their bounding boxes intersect.
     */
    public Vector getIntersectingShapes(Shape sh) {
        Vector intShapes = new Vector();
        for (Enumeration e = mapDrawableAdapters.elements(); e.hasMoreElements();) {
            MapDrawableAdapter adapter = (MapDrawableAdapter)e.nextElement();
            // check if it is MapShapeAdapter
            if (adapter instanceof MapShapeAdapter) {
                Shape shape = (Shape)adapter.getObject();
                // check if the shape intersects with the actual one
                if (shape.getBoundingBox().intersects(getProjection(), sh.getBoundingBox())) {
                    intShapes.add(shape);
                }
            }
        }
        return intShapes;
    }
    
    /**
     * Displays the dialog when the created shape is not valid. 
     */
    private void showShapeErrorDialog() {
        StratmasDialog.showErrorMessageDialog(null, "Not valid area!", "Area definition!");
    }

    /**
     * Updates color of the shape which is under creation. 
     */
     public void updateShapeUnderCreation(Color color) {
         if (shapeMaker != null) {
             shapeMaker.updateShapeColor(color);
             update();
         }
     }
    
    /**
     * Resets the actual shape creator.
     */
    public void resetShapeMaker() {
        if (shapeMaker != null) {
            shapeMaker.clearAll();
        }
    }
    
    /**
     * Removes the shape creator. 
     */
    public void removeShapeMaker() {
        resetShapeMaker();
        shapeMaker = null;
    }

    /**
     * Updates the region under the mouse cursor.
     *
     * @return true if the region has changed, false otherwise.
     */
    private boolean  setPointedRegion() {
        Vector adVec = mapDrawableAdaptersUnderCursor(MapShapeAdapter.class, SimpleShape.class);
        if (!adVec.isEmpty()) {
            // no shape highlighted
            if (highlightedShape == null) {
                highlightedShape = (Shape)((MapShapeAdapter)adVec.firstElement()).getObject();
                return true;
            }
            // check if the highlighted shape is in the list
            for (int i = 0; i < adVec.size(); i++) {
                Shape shape = (Shape)((MapShapeAdapter)adVec.get(i)).getObject();
                if (shape.equals(highlightedShape) && shape.isAncestor(highlightedShape)) {
                    return false;
                }
            }
            // the highlighted shape is changed
            highlightedShape = (Shape)((MapShapeAdapter)adVec.firstElement()).getObject();
            return true;
        }
        else {
            // no shape is under the mouse cursor
            if (highlightedShape != null) {
                highlightedShape = null;
                return true;
            }
            else {
                return false;
            }
        }
    }
    
    /**
     * Displays the region(s) currently pointed by the mouse pointer.
     */
    protected void displayHighlightedRegion() {
        if (highlightedShape == null) {
            displayPointedRegion(""); 
        }
        else {
            displayPointedRegion(getShapeName(highlightedShape));
        }
    }
    
    /**
     * Highlights the shape under the mouse cursor. The color of the highlighted shape is
     * chosen with ColorChooser.
     */
    protected void highlightPointedRegion() {
        Vector prevHighlighted = highlightedShapes;
        highlightedShapes = new Vector();
        if (highlightedShape != null) {
            Vector adVec = new Vector();
            MapDrawableAdapter mda = getMapDrawableAdapter(highlightedShape.getReference());
            // find shapes to highlight
            if (mda != null) {
                adVec.add(mda);
            }
            else {
                adVec = getChildrenShapeAdapters(highlightedShape);
            }
            // highlight the shapes
            for (int i = 0; i < adVec.size(); i++) {
                MapShapeAdapter msa = (MapShapeAdapter)adVec.get(i);
                if (prevHighlighted.contains(msa)) {
                    prevHighlighted.remove(msa);
                }
                else {
                    if (substrateMode == SET_AREA_VALUE_MODE) { 
                        // set the color of the region borders
                        msa.setShapeLineColor(substrateEditor.getActualColor());
                    }
                    // set the width of the region borders
                    msa.setShapeLineWidth(2 * MapShapeAdapter.DEFAULT_LINE_WIDTH); 
                }
                highlightedShapes.add(msa);
            }
        }
        // reset the shapes which are not under the cursor
        for (int i = 0; i < prevHighlighted.size(); i++) {
            ((MapShapeAdapter)prevHighlighted.get(i)).setShapeLineColor(MapShapeAdapter.DEFAULT_LINE_COLOR);
            ((MapShapeAdapter)prevHighlighted.get(i)).setShapeLineWidth(MapShapeAdapter.DEFAULT_LINE_WIDTH);
        }
        update();
    }

    /**
     * Sets the color to the selected region.
     */
    protected void setColorToSelectedRegion(Color color) {
        // set color to the shapes
        for (int i = 0; i < highlightedShapes.size(); i++) {
            MapShapeAdapter msa = (MapShapeAdapter)highlightedShapes.get(i);
            msa.setShapeAreaColor(color);
            // update the list of values for the shapes
            shapeValues.put(msa, new ShapeValuePair((Shape)msa.getObject(), substrateEditor.getActualValue(), true));   
        }
        update();
    }
    
    /**
     * Fills the actual shape with a color.
     */
    public void setActualColorToCreatedShape() {
        if (shapeMaker != null && shapeMaker.getShapeAdapter() != null) {
            shapeMaker.getShapeAdapter().setShapeAreaColor(substrateEditor.getActualColor());
            update();
        }
    }
    
    /**
     * Updates colors of the regions which are assigned values.
     */
    public void updateColoredRegions() {
        for (Enumeration e = mapDrawableAdapters.elements(); e.hasMoreElements();) {
            MapDrawableAdapter mda = (MapDrawableAdapter)e.nextElement();
            if (mda instanceof MapShapeAdapter) {
                MapShapeAdapter msa = (MapShapeAdapter)mda;
                if (shapeValues.get(msa) != null) {
                    double value = ((ShapeValuePair)shapeValues.get(msa)).getValue();
                    msa.setShapeAreaBackground(substrateEditor.getMappingColor(value));
                }
                else {
                    msa.setShapeAreaBackground(substrateEditor.getInitialColor());
                }
                msa.updateIntersectingShapes(getCreatedShapeValues(), substrateEditor);
             }
        }
        update();
    }

    /**
     * Initializes all the shapes with a color which represent the minimum value.
     */
    public void initializeShapeValues() {
        createdShapeAreas.removeAllElements();
        shapeValues.clear();
        for (Enumeration e = mapDrawableAdapters.elements(); e.hasMoreElements();) {
            MapDrawableAdapter adapter = (MapDrawableAdapter)e.nextElement();
            if (adapter instanceof MapShapeAdapter) {
                ((MapShapeAdapter)adapter).setShapeAreaColor(substrateEditor.getInitialColor());
            }
        }
    }
    
    /**
     * Resets the shape values. 
     *
     * @param sValues the key-value pairs where the keys are Shape objects and the values are 
     *                lists of ShapeValuePair objects. 
     */
    public void resetShapeValues(Hashtable sValues) {
        initializeShapeValues();
        // add new shape values
        for (Enumeration e = sValues.keys(); e.hasMoreElements();) {
            Shape shape = (Shape)e.nextElement();
            MapDrawableAdapter mda = getMapDrawableAdapter(shape.getReference());
            if (mda != null) {
                Vector svPairs = (Vector)sValues.get(shape);
                int startIndex = 0;
                // check if the first value is overall shape value
                ShapeValuePair svp = (ShapeValuePair)svPairs.firstElement();
                if (svp.getShape().equals(mda.getObject())) {
                    shapeValues.put(mda, svp);
                    ((MapShapeAdapter)mda).setShapeAreaColor(substrateEditor.getMappingColor(svp.getValue()));
                    startIndex = 1; 
                }
                // get the values for the intersections
                for (int i = startIndex; i < svPairs.size(); i++) {
                    svp = (ShapeValuePair)svPairs.get(i);
                    createdShapeAreas.push(svp);
                    ((MapShapeAdapter)mda).addIntersectingShape(svp.getShape(), 
                                                                substrateEditor.getMappingColor(svp.getValue()));
                }
            }
        }
        update();
    }
    
    /**
     * Returns a vector with the MapElementAdapters of the type specified by the input
     * arguments presently drawn under the cursor.
     *
     * @param adapterClass the class of the output elements.
     * @param objectClass the class of the elements adapted by the output adapters.
     *
     * @return the list of elements.  
     */
    public Vector mapDrawableAdaptersUnderCursor(Class adapterClass,  Class objectClass) {
        Vector res = new Vector();
        for(Enumeration e = latestRenderSelection.getTopSelectionObjects().elements(); e.hasMoreElements();) {
            Object o = e.nextElement();
            if (adapterClass.isInstance(o)) {
                if (objectClass.isInstance(((MapDrawableAdapter)o).getObject())) {
                    res.add(o);
                }
            }
        }
        return res;
    }

    /**
     * Returns a vector with the MapElementAdapters of the type specified by the input
     * argument presently drawn under the cursor.
     *
     * @param specifiedClass the class of the output elements.
     *
     * @return the list of elements.  
     */
    public Vector mapDrawableAdaptersUnderCursor(Class specifiedClass) {
        Vector res = new Vector();
        for(Enumeration e = latestRenderSelection.getTopSelectionObjects().elements(); e.hasMoreElements();) {
            Object o = e.nextElement();
            if (specifiedClass.isInstance(o)) {
                res.add(o);
            }
        }
        return res;
    }

    /**
     * Returns a vector with all MapDrawableAdapters presently _drawn_ under the cursor. 
     * NB the objects has to be rendered to be returned by this function.
     *
     * @return MapDrawables currently under the cursor on the map.
     */
    public Vector mapDrawableAdaptersUnderCursor() {
        Vector res = new Vector();
        for(Enumeration e = latestRenderSelection.getTopSelectionObjects().elements(); e.hasMoreElements();) {
            Object o = e.nextElement();
            if (o instanceof MapDrawableAdapter) {
                
                res.add(o);
            }
        }
        return res;
    }
    
    /**
     * Draws the shapes in the map.
     */
    protected void drawGraph(GLAutoDrawable gld) {
        GL2 gl = (GL2) gld.getGL();
        // clear the window
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);

        // draw graticules
        //gl.glCallList(graticuleDisplayList);
                
        // update shapes
        Vector toUpdate = mapDrawableAdapterRecompilation;
        this.mapDrawableAdapterRecompilation = new Vector();
        for(Enumeration e = toUpdate.elements(); e.hasMoreElements();) {
            MapDrawableAdapter adapter = (MapDrawableAdapter) e.nextElement();
            int oldDisplayList = adapter.getDisplayList();
            adapter.reCompile(basicMap.getProjection(), glc);
            if (oldDisplayList != adapter.getDisplayList()) {
                removeMapDrawableDisplayList(oldDisplayList);
                addMapDrawableDisplayList(adapter.getDisplayList());
            }
        }
        updateDrawnMapDrawablesList();

        // update actual shape
        if (shapeMaker != null && shapeMaker.getShapeAdapter() != null) {
            shapeMaker.getShapeAdapter().reCompile(basicMap.getProjection(), glc);
        }
        
        // draw shape areas
        gl.glCallLists(drawnShapeAreasListBuf.capacity(), gl.GL_INT, drawnShapeAreasListBuf);
        if (shapeMaker != null && shapeMaker.getShapeAdapter() != null) {
            gl.glCallList(shapeMaker.getShapeAdapter().getShapeAreaDisplayList());
        }
        
        // draw lines of the shapes
        gl.glCallLists(drawnShapeLinesListBuf.capacity(), gl.GL_INT, drawnShapeLinesListBuf);
        if (shapeMaker != null && shapeMaker.getShapeAdapter() != null) {
            gl.glCallList(shapeMaker.getShapeAdapter().getShapeLinesDisplayList());
        }
        
        // draw lines
        gl.glCallLists(drawnLinesListBuf.capacity(), gl.GL_INT, drawnLinesListBuf);
        
        // definition of new area for the element
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        // line color
        float[] cColor = substrateEditor.getActualColor().getRGBColorComponents(null);
        gl.glColor3f(cColor[0], cColor[1], cColor[2]);
        // width of the shape line
        gl.glLineWidth(2.0f);
        Projection proj = getProjection();
        if (substrateMode == CREATE_POLYGON_MODE && ((PolygonMaker)shapeMaker).getLastPoint() != null) {
            // draw last line when defining new polygonial 
            gl.glBegin(GL2.GL_LINES);
            MapPoint p2 = current_pos.getProjectedPoint(proj);
            gl.glVertex2dv(proj.projToXY(((PolygonMaker)shapeMaker).getLastPoint()), 0);
            gl.glVertex2d(p2.getX(), p2.getY());
            gl.glEnd();
        }
        gl.glPopMatrix();

        // draw points
        gl.glCallLists(drawnPointsListBuf.capacity(), gl.GL_INT, drawnPointsListBuf);
        
        // draw graticules
        gl.glCallList(graticuleDisplayList);
    }
    
    /**
     * Update the list representing which drawables should be drawn.
     */
    public void updateDrawnMapDrawablesList() {
        if (!isDrawnMapDrawablesListUpdated()) {
            // get all mapDrawableAdapters that should be drawn
            Vector drawableList = new Vector();
            Vector shapeList    = new Vector();
            Vector lineList     = new Vector();
            Vector pointList    = new Vector();
            for (Enumeration e = mapDrawableAdapters.elements(); e.hasMoreElements(); ) {
                drawableList.add(e.nextElement());
            }
            
            // sort the elements
            java.util.Collections.sort(drawableList, shapeAdaptedComparator);
            
            // update the list of display lists for drawables
            int[] drawableDisplayLists = new int[drawableList.size()];
            for (int i = 0; i < drawableDisplayLists.length; i++) {
                MapDrawableAdapter mda = (MapDrawableAdapter) drawableList.get(i);
                drawableDisplayLists[i] = mda.getDisplayList();
                if (mda instanceof MapShapeAdapter) {
                    shapeList.add(mda);
                }
                else if (mda instanceof MapLineAdapter) {
                    lineList.add(mda);
                }
                else if (mda instanceof MapPointAdapter) {
                    pointList.add(mda);
                }
            }
            drawnMapDrawablesListBuf = Buffers.newDirectIntBuffer(drawableDisplayLists.length);
            drawnMapDrawablesListBuf.put(drawableDisplayLists);
            drawnMapDrawablesListBuf.rewind();

            // update the list of display lists for lines and areas for the shapes
            int[] shapeAreasDisplayLists = new int[shapeList.size()];
            int[] shapeLinesDisplayLists = new int[shapeList.size()];
            for (int i = 0; i < shapeAreasDisplayLists.length; i++) {
                MapShapeAdapter msa = (MapShapeAdapter) shapeList.get(i);
                shapeAreasDisplayLists[i] = msa.getShapeAreaDisplayList();
                shapeLinesDisplayLists[i] = msa.getShapeLinesDisplayList();
            }
            drawnShapeLinesListBuf = Buffers.newDirectIntBuffer(shapeLinesDisplayLists.length);
            drawnShapeAreasListBuf = Buffers.newDirectIntBuffer(shapeAreasDisplayLists.length);
            drawnShapeLinesListBuf.put(shapeLinesDisplayLists);
            drawnShapeAreasListBuf.put(shapeAreasDisplayLists);
            drawnShapeLinesListBuf.rewind();
            drawnShapeAreasListBuf.rewind();

            // update the list of display lists for lines
            int[] linesDisplayLists = new int[lineList.size()];
            for (int i = 0; i < linesDisplayLists.length; i++) {
                MapLineAdapter mla = (MapLineAdapter) lineList.get(i);
                linesDisplayLists[i] = mla.getDisplayList();
            }
            drawnLinesListBuf = Buffers.newDirectIntBuffer(linesDisplayLists.length);
            drawnLinesListBuf.put(linesDisplayLists);
            drawnLinesListBuf.rewind();
            
            // update the list of display lists for points
            int[] pointsDisplayLists = new int[pointList.size()];
            for (int i = 0; i < pointsDisplayLists.length; i++) {
                MapPointAdapter mpa = (MapPointAdapter) pointList.get(i);
                pointsDisplayLists[i] = mpa.getDisplayList();
            }
            drawnPointsListBuf = Buffers.newDirectIntBuffer(pointsDisplayLists.length);
            drawnPointsListBuf.put(pointsDisplayLists);
            drawnPointsListBuf.rewind();
            
            isDrawnMapDrawablesListUpdated = true;
        }
    }
    
    /**
     * Returns all the shape adapters such that the given StratmasObjects is ancestor of the
     * adapted shapes.
     *
     * @param so the actual StratmasObject.
     *
     * @return the list of shape adapters.
     */
    protected Vector getChildrenShapeAdapters(StratmasObject so) {
        Vector childrenAdapters = new Vector();
        for (Enumeration e = mapDrawableAdapters.elements(); e.hasMoreElements(); ) {
            MapDrawableAdapter mda = (MapDrawableAdapter)e.nextElement();
            if (mda instanceof MapShapeAdapter) {
                StratmasObject sObj = mda.getObject();
                if (sObj.isAncestor(so)) {
                    childrenAdapters.add(mda);
                }
            }
        }
        return childrenAdapters;
    }
    
    /**
     * Returns the ancestor shape of the given object. 
     *
     * @param so the actual object.
     */
    private Shape getParentShape(StratmasObject so) {
        StratmasObject walker = so.getParent();
        while (walker != null && !(walker instanceof Shape)) {
            walker = walker.getParent();
        }
        return (walker != null)? (Shape)walker : null;
    }
    
    /**
     * Returns the name of the given shape.
     */
    public String getShapeName(Shape shape) {
        String shapeName = shape.getIdentifier();
        if (shapeName.equals("map")) {
            return new String("Complete Region");
        }
        StratmasObject walker = shape.getParent();
        while (walker != null && !walker.getIdentifier().equals("map")) {
            if (!(walker instanceof StratmasList)) {
                shapeName = walker.getIdentifier().concat(" - ").concat(shapeName);
            }
            walker = walker.getParent();
        }
        if (walker != null) {
            return shapeName;
        } 
        else {
            return shape.getIdentifier();
        }
    }
    
    /**
     * Returns true if the shape is ESRI shape.
     */
    public boolean isEsri(Shape shape) {
        MapDrawableAdapter adapter = getMapDrawableAdapter(shape.getReference());
        return (adapter != null)? true : false;
    }
    
    public void dispose(GLAutoDrawable glad){
      //TODO implement
    }

}

