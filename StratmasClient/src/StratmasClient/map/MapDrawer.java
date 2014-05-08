package StratmasClient.map;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.io.UnsupportedEncodingException;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.Enumeration;
import java.util.TimerTask;
import java.util.Vector;

import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES2;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import StratmasClient.BoundingBox;
import StratmasClient.Debug;
import StratmasClient.Icon;
import StratmasClient.filter.CombinedORFilter;
import StratmasClient.filter.PassFilter;
import StratmasClient.filter.StratmasObjectFilter;
import StratmasClient.filter.TypeFilter;
import StratmasClient.map.adapter.ElementAdapter;
import StratmasClient.map.adapter.MapActivityAdapter;
import StratmasClient.map.adapter.MapDrawableAdapter;
import StratmasClient.map.adapter.MapElementAdapter;
import StratmasClient.map.adapter.MapShapeAdapter;
import StratmasClient.map.adapter.PopulationAdapter;
import StratmasClient.map.adapter.GraphNodeAdapter;
import StratmasClient.object.Shape;
import StratmasClient.object.SimpleShape;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.StratmasEventListener;
import StratmasClient.object.StratmasList;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.type.Type;
import StratmasClient.object.type.TypeFactory;
import StratmasClient.treeview.TreeView;
import StratmasClient.treeview.TreeViewFrame;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.gl2.GLUT;

/**
 * All currently visible elements in the main map are drawed in this class. Thus all shapes, cells, elements, graticules etc. currently
 * displayed are handled here. <br>
 * The map is rendered by using OpenGL binding for Java (JOGL). <code> GLCanvas </code> is used as a drawing area.
 * <p>
 * Drag'n'drop is supported in this class. All <code>StratmasObject</code> elements with location can be dropped on the map. Further on,
 * elements of type "MilitaryUnits" and "AgencyTeams" can be both dragged and dropped on the map.
 * 
 * @version 1.0
 * @author Amir Filipovic, Daniel Ahlin
 */
public class MapDrawer extends BasicMapDrawer implements DragGestureListener,
        StratmasEventListener {
    /**
	 * 
	 */
    private static final long serialVersionUID = 2900439555092722945L;
    /**
     * Layer of cells that cover the geographical region.
     */
    protected GridLayer cell_layer;
    /**
     * The position/navigation map.
     */
    protected PositionMap position_map;
    /**
     * The color map.
     */
    protected ColorMap color_map;
    /**
     * Indicates if the graticule lines have to be updated.
     */
    protected boolean update_graticules = false;
    /**
     * Whether to display region border or not.
     */
    protected boolean show_geo_region = true;
    /**
     * Whether to display graticules or not.
     */
    protected boolean show_graticules = true;
    /**
     * Whether to display elements or not.
     */
    protected boolean show_elements = true;
    /**
     * Whether to ignore present or not.
     */
    protected boolean ignorePresent = true;
    /**
     * Whether to display city location or not.
     */
    protected boolean show_city_location = true;
    /**
     * Indicates if the representation of the process variables is grid based or region based.
     */
    protected boolean grid_based_pv = true;
    /**
     * Used for the implementation of DnD.
     */
    protected DragGestureRecognizer recognizer;
    /**
     * Used for the implementation of DnD.
     */
    protected DragSource source;
    /**
     * Used for the implementation of DnD.
     */
    protected DropTarget target;
    /**
     * Used for the implementation of DnD.
     */
    protected DropTargetListener dropTargetListener;
    /**
     * Used to create different kinds of menus used in the map.
     */
    protected MapDrawerMenuCreator menuCreator;
    /**
     * Last horizontal mouse coordinate for last renderselction
     */
    protected int renderSelectionMouseX = 0;
    /**
     * Last vertical mouse coordinate for last renderselction
     */
    protected int renderSelectionMouseY = 0;
    /**
     * The horizontal center coordinate for render selection;
     */
    protected double renderSelectionX;
    /**
     * The vertical center coordinate for render selection;
     */
    protected double renderSelectionY;
    /**
     * The horizontal tolerance in render selection;
     */
    protected double renderSelectionDeltaX = 1;
    /**
     * The vertical tolerance in render selection;
     */
    protected double renderSelectionDeltaY = 1;
    /**
     * The result of the latest renderSelection;
     */
    protected RenderSelection latestRenderSelection = new RenderSelection();
    /**
     * The opacity to use for symbols.
     */
    protected double symbolOpacity = 1.0d;
    /**
     * How much to scale the symbols
     */
    protected double symbolScale = 0.3d;
    /**
     * Whether to show location or not.
     */
    protected boolean showLocation = false;
    /**
     * Whether to show outline or not.
     */
    protected boolean showOutline = false;
    /**
     * Whether symbol size should be invariant with regard to map scale.
     */
    protected boolean invariantSymbolSize = false;
    /**
     * Location alpha multiplier.
     */
    protected double locationOpacity = 1.0d;
    /**
     * Timer for tasks performed while the mouse is not moving.
     */
    protected java.util.Timer mouseMovedTimer = new java.util.Timer();
    /**
     * How many milliseconds to wait before magnifying the symbols under the cursor.
     */
    protected long magnifierTimeout = 500;
    /**
     * How much to scale the magnifier.
     */
    protected double magnifierSizeScale = 1.0d;
    /**
     * Number of steps for magnifier to reach full size.
     */
    protected int magnifierSizeSteps = 10;
    /**
     * Whether to render a magnification of the symbols currently rendered under the mouse cursor.
     */
    protected boolean showingSymbolMagnification = false;
    /**
     * Whether to start symbol magnification or not.
     */
    protected boolean isEnabledSymbolMagnifier = true;
    /**
     * Whether to show populationNames or not.
     */
    protected boolean showPopulationNames = false;
    /**
     * Filter indicating which StratmasObjects that are dragable (as in drag-and-drop).
     */
    protected StratmasObjectFilter dragFilter;
    /**
     * Filter indicating which elements and activities to draw.
     */
    protected StratmasObjectFilter drawnMapElementsFilter = new PassFilter();
    /**
     * Indicates that a screenshot should be made during the next redraw.
     */
    protected boolean doScreenShot = false;
    public ToolMode mode;

    /**
     * Creates new MapDrawer.
     * 
     * @param basicMap the map container.
     * @param region the region displayed on the map.
     * @param position_map the panning tool.
     */
    public MapDrawer(StratMap basicMap, Region region, PositionMap position_map) {
        super(basicMap, region);

        // filter for drawable elements
        CombinedORFilter dFilter = new CombinedORFilter();
        dFilter.add(new TypeFilter(TypeFactory.getType("MilitaryUnit"), true));
        dFilter.add(new TypeFilter(TypeFactory.getType("AgencyTeam"), true));
        dFilter.add(new TypeFilter(TypeFactory.getType("Activity"), true));
        dFilter.add(new TypeFilter(TypeFactory.getType("Node"), true));
        this.dragFilter = dFilter;

        // region associated with the map
        region.addListener(this);

        // position map
        this.position_map = position_map;
        this.position_map.setMap(this);

        // helper object used for creation of different kinds of menues in the map
        menuCreator = new MapDrawerMenuCreator(basicMap.getClient(), this,
                region);

        // used for the drag action i DnD
        source = new DragSource();
        recognizer = source
                .createDefaultDragGestureRecognizer(glc,
                                                    DnDConstants.ACTION_REFERENCE,
                                                    this);

        // used for the drop action in DnD
        dropTargetListener = new MapDrawerDropTarget(basicMap.getClient(),
                this, region);
        target = new DropTarget(this, dropTargetListener);
        this.setDropTarget(target);

        // import all elements and activities to the map
        importMapElements();
    }

    /**
     * Drawing elements on the map. Part of GLEventListener interface.
     * 
     * @param gld needed for OpenGL2.
     */
    public void display(GLAutoDrawable gld) {
        GL2 gl = (GL2) gld.getGL();

        //
        int origDrawBuffer = -1;
        if (doScreenShot()) {
            origDrawBuffer = GLScreenShotHandler.changeDrawBuffer(gld);
        }

        // update orthographics view bounds
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluOrtho2D(orts_box.getXmin(), orts_box.getXmax(),
                       orts_box.getYmin(), orts_box.getYmax());

        // update graticules
        if (update_graticules) {
            updateGraticuleList(gl);
            update_graticules = false;
        }

        // update small map
        position_map.update(orts_box);

        // draw the map
        drawGraph(gld);

        if (doScreenShot()) {
            screenShot(gld);
            gl.glDrawBuffer(origDrawBuffer);
        }

        updateRenderSelection(gld);
    }

    /**
     * Causes different actions by clicking the mouse. These actions depend of the clicked button (left or right), if the click is simple or
     * double and the current mode of the map. Part of MouseListener interface.
     * 
     * @param e event created by clicking the mouse.
     */
    public void mouseClicked(MouseEvent e) {
        mouseMovedTimer.cancel();

        // get window coordinates
        int x = (int) e.getX();
        int y = (int) e.getY();

        setRenderSelectionArea(x, y);

        // convert the curent position to lon/lat
        MapPoint p = convertToLonLat(x, y);

        // get projected coordinates
        Projection proj = basicMap.getProjection();
        p.getProjectedPoint(proj).getX();
        p.getProjectedPoint(proj).getY();

        // right mouse button
        if (e.getButton() == MouseEvent.BUTTON3) {
            // create new menu
            JPopupMenu menu = new JPopupMenu();
            // get the submenu for the pointed regions
            JMenu submenu = menuCreator.getMenuForRegions();
            if (submenu != null) {
                menu.add(submenu);
            }
            // get the submenu for the pointed military units
            submenu = menuCreator.getMenuForMilitaryUnits();
            if (submenu != null) {
                menu.add(submenu);
            }
            // get the submenu for the graph nodes
            submenu = menuCreator.getMenuForGraphNodes();
            if (submenu != null) {
                menu.add(submenu);
            }
            // get the submenu for the postion of the pointed elements
            submenu = menuCreator.getMenuForElementsPosition();
            if (submenu != null) {
                menu.add(submenu);
            }
            // get the submenu for the AOR
            submenu = menuCreator.getMenuForAOR();
            if (submenu != null) {
                menu.add(submenu);
            }
            // get the submenu for the selected elements
            submenu = menuCreator.getMenuForElementsForSelection(true);
            if (submenu != null) {
                menu.add(submenu);
            }
            // get the submenu for the unselected elements
            submenu = menuCreator.getMenuForElementsForSelection(false);
            if (submenu != null) {
                menu.add(submenu);
            }
            // show the menu
            if (menu.getComponentCount() > 0) {
                menu.show(this, x, y);
            }
        }
        // left mouse button
        else if (e.getButton() == MouseEvent.BUTTON1) {
            // show information of the pointed element
            if (e.getClickCount() == 2) {
                // find all elemets located at the pointed location
                Vector<StratmasObject> pointedElements = mapElementsUnderCursor();
                // if only one element found
                if (pointedElements.size() == 1) {
                    final TreeViewFrame frame = TreeView
                            .getDefaultFrame(pointedElements.firstElement());
                    javax.swing.SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            frame.setEditable(true);
                            frame.setVisible(true);
                        }
                    });
                } else if (pointedElements.size() > 1) {
                    // create menu
                    JPopupMenu menu = new JPopupMenu();
                    JMenu submenu = menuCreator
                            .getMenuForElements(pointedElements);
                    menu.add(submenu);
                    menu.show(this, x, y);
                }
            }
        }
        //
        update();
    }

    /**
     * Used to move points and shapes on the map and also to change the radius of the drawn circle while creating a new area. Part of
     * MouseMotionListener interface.
     * 
     * @param e the generated event.
     */
    public void mouseDragged(MouseEvent e) {
        super.mouseDragged(e);
        // get window coordinates
        int x = (int) e.getX();
        int y = (int) e.getY();
        setRenderSelectionArea(x, y);

        convertToLonLat(x, y);

        // necessary for multi-screen environment
        mouse_on = (x >= view_x && x <= view_x + view_width && y >= view_y && y <= view_y
                + view_height) ? true : false;
        // convert the current position to lon/lat
        current_pos = convertToLonLat(x, y);

        if (System.currentTimeMillis() - latestUpdateTime > updateTimeDelay) {
            // display current position
            displayCurrentPosition(current_pos);
            // redraw
            update();
        }
    }

    /**
     * Upadates the curent position on the map in (lat,lon) coordinates. Part of MouseMotionListener interface.
     * 
     * @param e event created by changing the position of the mouse on the map.
     */
    public void mouseMoved(MouseEvent e) {
        // disable magnifying of the symbols
        this.setShowingSymbolMagnification(false);

        // get window coordinates
        int x = (int) e.getX();
        int y = (int) e.getY();

        setRenderSelectionArea(x, y);

        
        // check if the symbol magnifier should be turned on 
        mouseMovedTimer.cancel();
        if (isEnabledSymbolMagnifier()) {
            mouseMovedTimer = new java.util.Timer();
            final MapDrawer self = this;
            for (int i = 1; i <= magnifierSizeSteps; i++) {
            final int foo = i;
            mouseMovedTimer.schedule(new TimerTask() {
                public void run() {
                    self.setShowingSymbolMagnification(true);
                    self.setMagnifierSizeScale((((double) (foo))/((double) self.getMagnifierSizeSteps())));
                    self.update();
                }
            }, this.magnifierTimeout + (long) ((((double) (i)) / ((double) getMagnifierSizeSteps())) * this.magnifierTimeout));
            }
        }


        // necessary for multi-screen enviroment
        mouse_on = (x >= view_x && x <= view_x + view_width && y >= view_y && y <= view_y
                + view_height) ? true : false;

        // convert the current position to lon/lat
        current_pos = convertToLonLat(x, y);

        if (System.currentTimeMillis() - latestUpdateTime > updateTimeDelay) {
            // display current position
            displayCurrentPosition(current_pos);
            // display the region under the mouse cursor
            Vector<Object> adVec = mapDrawableAdaptersUnderCursor(MapShapeAdapter.class);
            if (!adVec.isEmpty()) {
                if (adVec.size() == 1) {
                    displayPointedRegion(((Shape) ((MapShapeAdapter) adVec
                            .firstElement()).getObject()).getIdentifier());
                } else {
                    int tmpIndex = 0;
                    while (tmpIndex < adVec.size()
                            && !(((MapShapeAdapter) adVec.get(tmpIndex))
                                    .getObject() instanceof SimpleShape)) {
                        tmpIndex++;
                    }
                    if (tmpIndex < adVec.size()) {
                        displayPointedRegion(((Shape) ((MapShapeAdapter) adVec
                                .get(tmpIndex)).getObject()).getIdentifier());
                    }
                }
            } else {
                displayPointedRegion("");
            }
            // redraw
            update();
        }
    }

    /**
     * Updates the map. Part of StratmasEventListener interface.
     * 
     * @param se the occured event.
     */
    public void eventOccured(StratmasEvent se) {
        // redraw the map
        if (se.isSubscriptionHandled()) {
            update();
        }
        // update the region
        else if (se.isRegionUpdated()) {
            // update bounding box for the region
            box = region.getProjectedBounds();
            double dx = Math.abs(box.getXmax() - box.getXmin());
            double dy = Math.abs(box.getYmax() - box.getYmin());
            double xmin = box.getXmin() - dx / 10;
            double ymin = box.getYmin() - dy / 10;
            double xmax = box.getXmax() + dx / 10;
            double ymax = box.getYmax() + dy / 10;
            box = new BoundingBox(xmin, ymin, xmax, ymax, this.getProjection());

            // update orthographic view bounds and view center
            ort_box = (BoundingBox) box.clone();
            ort_xc = (ort_box.getXmax() + ort_box.getXmin()) / 2;
            ort_yc = (ort_box.getYmax() + ort_box.getYmin()) / 2;

            // update scaled orthographic view bounds and view center
            orts_box = (BoundingBox) box.clone();

            // get new orthographic view bounds such that aspect ratio is
            // equal to display window's
            ort_box = updateOrthographicBounds(view_width, view_height);

            // new projection update positioning of symbols
            for (Enumeration<MapDrawableAdapter> e = mapDrawableAdapters
                    .elements(); e.hasMoreElements();) {
                e.nextElement().invalidateAllLists();
            }

            // update map scale
            zoom_and_scale.update();
            // update graticules
            update_graticules = true;
            //
            update();
        }
        // update the graticule lines
        else if (se.areGraticulesUpdated()) {
            update_graticules = true;
            update();
        }
    }

    /*
     * Drag gesture handler. Part of DragGestureListener interface.
     * @param dge event created when a drag is started.
     */
    public void dragGestureRecognized(final DragGestureEvent dge) {
        // cancel features
        mouseMovedTimer.cancel();
        this.setShowingSymbolMagnification(false);
        // get location
        int x = (int) (dge.getDragOrigin().getX());
        int y = (int) (dge.getDragOrigin().getY());
        // get elements
        Vector<StratmasObject> v = dragFilter.filter(mapElementsUnderCursor());
        // define cursor for the object
        Cursor c;
        Toolkit tk = Toolkit.getDefaultToolkit();
        // if there's anything to drag
        if (!v.isEmpty()) {
            Image image = ((v.get(0)).getIcon()).getImage();
            Dimension bestsize = tk.getBestCursorSize(image.getWidth(null),
                                                      image.getHeight(null));
            if (bestsize.width != 0 && v.size() == 1)
                c = tk.createCustomCursor(image, new java.awt.Point(
                                                  bestsize.width / 2,
                                                  bestsize.height / 2),
                                          (v.get(0))
                                                  .toString());
            else c = Cursor.getDefaultCursor();
            // only one element on the current location
            if (v.size() == 1) {
                // set the dragged element
                DraggedElement.setElement(v.get(0));
                // start the drag
                source.startDrag(dge, c, v.get(0),
                                 new DragSourceAdapter() {});
            }
            // several elements on the current location
            else if (v.size() > 1) {
                // create and show menu
                menuCreator.getDraggedElementsMenu(dragFilter).show(this, x, y);
            }
        }
    }

    /**
     * Create the GUI and show it.
     */
    public void createAndShowGUI(String frame_title) {
        super.createAndShowGUI(frame_title);
        //
        if (Debug.isInDebugMode()) {
            // MapDrawerDebugFrame.openMapDrawerDebugFrame(this);
        }
    }

    /**
     * Updates the render selection array
     * 
     * @param gld the drawable
     */
    protected void updateRenderSelection(GLAutoDrawable gld) {
        GL2 gl = (GL2) gld.getGL();

        IntBuffer renderSelectionBuffer;
        int renderSelectionBufferAllocationSize = 2048;

        int hits = -1;

        do {
            renderSelectionBuffer = Buffers
                    .newDirectIntBuffer(renderSelectionBufferAllocationSize);
            gl.glSelectBuffer(renderSelectionBuffer.capacity(),
                              renderSelectionBuffer);

            // Enable render selection.
            gl.glRenderMode(GL2.GL_SELECT);

            // Init names.
            gl.glInitNames();

            // Sets the selection area.
            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glPushMatrix();
            gl.glLoadIdentity();

            glu.gluOrtho2D(renderSelectionX - renderSelectionDeltaX / 2,
                           renderSelectionX + renderSelectionDeltaX / 2,
                           renderSelectionY - renderSelectionDeltaY / 2,
                           renderSelectionY + renderSelectionDeltaY / 2);
            // Draw symbols.
            updateDrawnMapDrawablesList();
            gl.glCallLists(drawnMapDrawablesListBuf.capacity(), GL2ES2.GL_INT,
                           drawnMapDrawablesListBuf);

            // draw the grid
            if (grid_based_pv && cell_layer != null) {
                if (!cell_layer.isDisplayListUpdated()) {
                    cell_layer.updateDisplayList(getProjection(), gld);
                }
                gl.glCallList(cell_layer.getDisplayList());
            }

            // Restore view
            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glPopMatrix();
            gl.glFlush();

            // End render selection mode.
            hits = ((GL2) gld.getGL()).glRenderMode(GL2.GL_RENDER);

            if (hits < 0) {
                // To small selectionBuffer, try double size.
                renderSelectionBufferAllocationSize = renderSelectionBufferAllocationSize * 2;
            }
        } while (hits < 0);

        this.latestRenderSelection = new RenderSelection(hits,
                renderSelectionBuffer, renderSelectionNames);
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
        this.renderSelectionX = p.getProjectedPoint(this.getProjection())
                .getX();
        this.renderSelectionY = p.getProjectedPoint(this.getProjection())
                .getY();
        this.renderSelectionDeltaY = deltaY;
        this.renderSelectionDeltaX = deltaX;

        this.renderSelectionMouseX = x;
        this.renderSelectionMouseY = y;
    }

    /**
     * Sets selection area for subsequent renderSelection calls.
     * 
     * @param x horizontal component of center in screen coordinates.
     * @param y vertical component of center in screen coordinates. screen coordinates.
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
     * Finds out if the element is registred on the map.
     * 
     * @param element the element to be checked.
     * @return true if the element is registred on the map as MapDrawableAdapter, false otherwise.
     */
    public boolean registredOnMap(StratmasObject element) {
        return mapDrawableAdapters.containsKey(element);
    }

    /**
     * Returns the MapElementAdapter for the given StratmasObject. Null is returned if the MapElementAdapter doesn't exist.
     */
    public MapElementAdapter getMapElementAdapter(StratmasObject element) {
        return (MapElementAdapter) mapDrawableAdapters.get(element);
    }

    /**
     * Returns true if the given element (or activity) is displayed on the map, false otherwise
     * 
     * @param element element or activity to check.
     */
    public boolean isElementDisplayed(StratmasObject element) {
        // get adapter of the element
        MapElementAdapter mea = getMapElementAdapter(element);
        // get display list of the adapter
        int dList = mea.getDisplayList();
        // check if the display list is in drawnMapDrawablesList
        for (int i = 0; i < drawnMapDrawablesListBuf.capacity(); i++) {
            if (drawnMapDrawablesListBuf.get(i) == dList) {
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the actual filter for the elements on the map.
     * 
     * @param filter the new filter.
     */
    public void setDrawnMapElementsFilter(StratmasObjectFilter filter) {
        this.drawnMapElementsFilter = filter;
        setIsDrawnMapDrawablesListUpdated(false);
        update();
    }

    /**
     * Returns the filter which decides which elements to draw on the map.
     */
    public StratmasObjectFilter getDrawnMapElementsFilter() {
        return this.drawnMapElementsFilter;
    }

    /**
     * Sets reference to the grid layer.
     */
    public void setGridLayer(GridLayer cell_layer) {
        this.cell_layer = cell_layer;
        menuCreator.setGridLayer(cell_layer);
        // set renderSelectionName for the grid
        cell_layer.setRenderSelectionName(getNewRenderSelectionName(cell_layer
                .getNrOfRenderSelectionNames()));
        // update renderSelectionNames
        renderSelectionNames
                .put(new Integer(cell_layer.getRenderSelectionName()),
                     cell_layer);
        //
        cell_layer.updateActiveCells();
    }

    /**
     * Sets reference to actual color map controller.
     */
    public void setColorMap(ColorMap color_map) {
        this.color_map = color_map;
    }

    /**
     * Displays location of the cities if true.
     * 
     * @param vis true to display location of the cities, false to hide it.
     */
    public void setCityLocationVisible(boolean vis) {
        // dah - Implement here
        show_city_location = vis;
        update();
    }

    /**
     * Sets the visual representation of the process variables.
     * 
     * @param ok if true the process variables are displayed as a grid of cells, otherwise the region based representation is used.
     */
    public void setPVGrid(boolean ok) {
        grid_based_pv = ok;
        if (grid_based_pv) {
            // update the shape adapters
            for (Enumeration<MapDrawableAdapter> adapters = mapDrawableAdapters
                    .elements(); adapters.hasMoreElements();) {
                MapDrawableAdapter mdAdapter = adapters.nextElement();
                if (mdAdapter instanceof MapShapeAdapter
                        && region.contains((Shape) mdAdapter.getObject())) {
                    ((MapShapeAdapter) mdAdapter).setShapeAreaTransparent();
                }
            }
        } else if (cell_layer != null) {
            // update the shape adapters
            for (Enumeration<MapDrawableAdapter> adapters = mapDrawableAdapters
                    .elements(); adapters.hasMoreElements();) {
                MapDrawableAdapter mdAdapter = adapters.nextElement();
                if (mdAdapter instanceof MapShapeAdapter
                        && region.contains((Shape) mdAdapter.getObject())) {
                    double value = cell_layer
                            .getAverageValueForCells((Shape) mdAdapter
                                    .getObject());
                    ((MapShapeAdapter) mdAdapter).setShapeAreaColor(color_map
                            .getMappingColor(value));
                }
            }
        }
        // redraw
        update();
    }

    /**
     * Displays the region borders if true.
     * 
     * @param vis true to display the region borders.
     */
    public void setDrawRegionShapes(boolean vis) {
        show_geo_region = vis;
        // update the shape adapters
        for (Enumeration<MapDrawableAdapter> adapters = mapDrawableAdapters
                .elements(); adapters.hasMoreElements();) {
            MapDrawableAdapter mdAdapter = adapters.nextElement();
            if (mdAdapter instanceof MapShapeAdapter
                    && region.contains((Shape) mdAdapter.getObject())) {
                ((MapShapeAdapter) mdAdapter).setShapeVisible(show_geo_region);
            }
        }
        setIsDrawnMapDrawablesListUpdated(false);
        update();
    }

    /**
     * Displays the graticule lines if true.
     * 
     * @param vis true to display the graticules.
     */
    public void setGraticulesVisible(boolean vis) {
        show_graticules = vis;
        update();
    }

    /**
     * Updates spacing of the graticule lines.
     * 
     * @param s spacing between graticules.
     */
    public void setGraticuleSpacing(int s) {
        spacing = s;
        update_graticules = true;
        update();
    }

    /**
     * Sets scaled orthographic bounds.
     * 
     * @param sbox bouding box of scaled orthographic bounds.
     */
    public void setScaledBoundingBox(BoundingBox sbox) {
        orts_box = (BoundingBox) sbox.clone();

        // Projection will change, so we need to update symbols if they are invariant.
        if (getInvariantSymbolSize()) {
            for (Enumeration<MapDrawableAdapter> e = mapDrawableAdapters
                    .elements(); e.hasMoreElements();) {
                MapDrawableAdapter mda = e.nextElement();
                if (mda instanceof MapElementAdapter) {
                    ((MapElementAdapter) mda).invalidateSymbolList();
                }
            }
        }
        update();
    }

    /**
     * Disposes the map window.
     */
    public void doDispose() {
        mouseMovedTimer.cancel();
        frame.dispose();
    }

    /**
     * Notifies that the grid has to be updated.
     */
    public void setUpdatePVValues() {
        cell_layer.invalidateDisplayList();
        if (!grid_based_pv) {
            // update the shape adapters
            for (Enumeration<MapDrawableAdapter> adapters = mapDrawableAdapters
                    .elements(); adapters.hasMoreElements();) {
                MapDrawableAdapter mdAdapter = adapters.nextElement();
                if (mdAdapter instanceof MapShapeAdapter
                        && region.contains((Shape) mdAdapter.getObject())) {
                    double value = cell_layer
                            .getAverageValueForCells((Shape) mdAdapter
                                    .getObject());
                    ((MapShapeAdapter) mdAdapter).setShapeAreaColor(color_map
                            .getMappingColor(value));
                }
            }
        }

        // redraw
        update();
    }

    /**
     * Returns true if the representation of the displayed process variable is grid based, false otherwise.
     */
    public boolean gridBasedPV() {
        return grid_based_pv;
    }

    /**
     * Returns a vector with StratmasObject inherited elements presently drawn under the cursor. NB the objects has to be rendered to be
     * returned by this function.
     * 
     * @return the drawn elements and activities currently under the cursor.
     */
    public Vector<StratmasObject> mapElementsUnderCursor() {
        Vector<StratmasObject> res = new Vector<StratmasObject>();
        for (Enumeration e = latestRenderSelection.getTopSelectionObjects()
                .elements(); e.hasMoreElements();) {
            Object o = e.nextElement();
            if (o instanceof MapElementAdapter) {
                res.add(((MapElementAdapter) o).getStratmasObject());
            }
        }
        return res;
    }

    /**
     * Returns a vector with all MapDrawableAdapters presently _drawn_ under the cursor. NB the objects has to be rendered to be returned by
     * this function.
     * 
     * @return MapDrawables currently under the cursor on the map.
     */
    public Vector<Object> mapDrawableAdaptersUnderCursor() {
        Vector<Object> res = new Vector<Object>();
        for (Enumeration e = latestRenderSelection.getTopSelectionObjects()
                .elements(); e.hasMoreElements();) {
            Object o = e.nextElement();
            if (o instanceof MapDrawableAdapter) {
                res.add(o);
            }
        }
        return res;
    }

    /**
     * Returns a vector with the MapElementAdapters of the type specified by the input argument presently drawn under the cursor.
     * 
     * @param specifiedClass the class of the output elements.
     * @return the list of elements.
     */
    public Vector<Object> mapDrawableAdaptersUnderCursor(
            Class<? extends MapDrawableAdapter> specifiedClass) {
        Vector<Object> res = new Vector<Object>();
        for (Enumeration e = latestRenderSelection.getTopSelectionObjects()
                .elements(); e.hasMoreElements();) {
            Object o = e.nextElement();
            if (specifiedClass.isInstance(o)) {
                res.add(o);
            }
        }
        return res;
    }

    /**
     * All elements shown in the scene are drawn here.
     * 
     * @param gld needed for OpenGL2.
     */
    protected void drawGraph(GLAutoDrawable gld) {
        GL2 gl = (GL2) gld.getGL();
        // clear the window
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
        // draw pv for each cell
        if (grid_based_pv && cell_layer != null) {
            if (!cell_layer.isDisplayListUpdated()) {
                cell_layer.updateDisplayList(getProjection(), gld);
            }
            gl.glCallList(cell_layer.getDisplayList());
        }

        if (show_graticules) {
            // draw graticules
            gl.glCallList(graticuleDisplayList);
        }

        if (show_elements) {
            // draw elements
            // Recompile changed elements
            Vector<MapDrawableAdapter> toUpdate = mapDrawableAdapterRecompilation;
            this.mapDrawableAdapterRecompilation = new Vector<MapDrawableAdapter>();
            for (Enumeration<MapDrawableAdapter> e = toUpdate.elements(); e
                    .hasMoreElements();) {
                MapDrawableAdapter adapter = e.nextElement();
                int oldDisplayList = adapter.getDisplayList();
                adapter.reCompile(basicMap.getProjection(), glc);
                if (oldDisplayList != adapter.getDisplayList()) {
                    removeMapDrawableDisplayList(oldDisplayList);
                    addMapDrawableDisplayList(adapter.getDisplayList());
                }
            }
            updateDrawnMapDrawablesList();
            gl.glCallLists(drawnMapDrawablesListBuf.capacity(), GL2ES2.GL_INT,
                           drawnMapDrawablesListBuf);

            if (isShowingSymbolMagnification()) {
                // magnify drawn elements
                magnifyElements(gld);
                // Restore view
                gl.glMatrixMode(GL2.GL_MODELVIEW);
                gl.glPopMatrix();
                gl.glMatrixMode(GL2.GL_PROJECTION);
                gl.glPopMatrix();
            }
        }
    }

    /**
     * Shows subunits of the specified StratmasObject.
     * 
     * @param so the object to show subunits for.
     */
    protected void showSubunits(StratmasObject so) {
        final StratmasObject parent = so;
        StratmasObjectFilter filter = new StratmasObjectFilter() {
            /**
             * Returns true if the provided StratmasObject is a descendant of parent and not a list and has a type that may substitute
             * parents type.
             * 
             * @param sObj the object to test
             */
            public boolean pass(StratmasObject sObj) {
                return sObj != null
                        && (!(sObj instanceof StratmasList))
                        && sObj.getType().canSubstitute(parent.getType())
                        && (sObj.getParent() != null && sObj.getParent()
                                .equals(parent));
            }
        };
        if (getDrawnMapElementsFilter() instanceof CombinedORFilter) {
            ((CombinedORFilter) getDrawnMapElementsFilter()).add(filter);
            setDrawnMapElementsFilter(getDrawnMapElementsFilter());
        } else {
            CombinedORFilter orFilter = new CombinedORFilter();
            orFilter.add(getDrawnMapElementsFilter());
            orFilter.add(filter);
            setDrawnMapElementsFilter(orFilter);
        }
    }

    /**
     * Sets flag indicating whether a panel showing a magnification of the symbols currently rendered under the cursor should be drawn.
     * 
     * @param flag
     */
    void setShowingSymbolMagnification(boolean flag) {
        this.showingSymbolMagnification = flag;
    }

    /**
     * Sets the size scaler of the magnifier panel.
     * 
     * @param scale
     */
    void setMagnifierSizeScale(double scale) {
        this.magnifierSizeScale = scale;
    }

    /**
     * Returns the number of step the size of the magnifier panel is animated in.
     */
    int getMagnifierSizeSteps() {
        return this.magnifierSizeSteps;
    }

    /**
     * Returns true if a panel showing a magnification of the symbols currently rendered under the cursor should be drawn.
     */
    boolean isShowingSymbolMagnification() {
        return this.showingSymbolMagnification;
    }

    /**
     * Returns true if the symbol magnifier is enabled.
     */
    public boolean isEnabledSymbolMagnifier() {
        return this.isEnabledSymbolMagnifier;
    }

    /**
     * Sets whether the symbol magnifier should be enabled or not.
     */
    public void setIsEnabledSymbolMagnifier(boolean flag) {
        this.isEnabledSymbolMagnifier = flag;
    }

    /**
     * Returns true if the location drawing is enabled.
     */
    public boolean isEnabledLocation() {
        return this.showLocation;
    }

    /**
     * Sets whether the location drawing should be enabled or not.
     * 
     * @param flag the flag
     */
    public void setIsEnabledLocation(boolean flag) {
        if (this.showLocation != flag) {
            this.showLocation = flag;
            for (Enumeration<MapDrawableAdapter> adapters = mapDrawableAdapters
                    .elements(); adapters.hasMoreElements();) {
                MapDrawableAdapter mdAdapter = adapters.nextElement();
                if (mdAdapter instanceof MapElementAdapter) {
                    ((MapElementAdapter) mdAdapter).setDrawLocation(flag);
                }
            }
            update();
        }
    }

    /**
     * Sets whether the location drawing should be enabled or not.
     * 
     * @param flag the flag.
     * @param type the type of the object.
     */
    public void setIsEnabledLocation(boolean flag, Type type) {
        for (Enumeration<MapDrawableAdapter> adapters = mapDrawableAdapters
                .elements(); adapters.hasMoreElements();) {
            MapDrawableAdapter mdAdapter = adapters.nextElement();
            if (mdAdapter.getObject().getType().canSubstitute(type)) {
                if (mdAdapter instanceof MapElementAdapter) {
                    ((MapElementAdapter) mdAdapter).setDrawLocation(flag);
                }
            }
        }
        update();
    }

    /**
     * Returns true if the location outline drawing is enabled.
     */
    public boolean isEnabledOutline() {
        return this.showOutline;
    }

    /**
     * Sets whether the location outline drawing should be enabled or not.
     * 
     * @param flag the flag
     */
    public void setIsEnabledOutline(boolean flag) {
        if (this.showOutline != flag) {
            this.showOutline = flag;
            for (Enumeration<MapDrawableAdapter> adapters = mapDrawableAdapters
                    .elements(); adapters.hasMoreElements();) {
                MapDrawableAdapter mdAdapter = adapters.nextElement();
                if (mdAdapter instanceof MapElementAdapter) {
                    ((MapElementAdapter) mdAdapter)
                            .setDrawLocationOutline(flag);
                }
            }
            update();
        }
    }

    /**
     * Sets whether the location outline drawing should be enabled or not.
     * 
     * @param flag the flag.
     * @param type the type of the object.
     */
    public void setIsEnabledOutline(boolean flag, Type type) {
        for (Enumeration<MapDrawableAdapter> adapters = mapDrawableAdapters
                .elements(); adapters.hasMoreElements();) {
            MapDrawableAdapter mdAdapter = adapters.nextElement();
            if (mdAdapter.getObject().getType().canSubstitute(type)) {
                if (mdAdapter instanceof MapElementAdapter) {
                    ((MapElementAdapter) mdAdapter)
                            .setDrawLocationOutline(flag);
                }
            }
        }
        update();
    }

    /**
     * Update the list representing which drawables should be drawn
     */
    public void updateDrawnMapDrawablesList() {
        if (!isDrawnMapDrawablesListUpdated()) {
            // Get all mapDrawableAdapters that should be drawn
            Vector<MapDrawableAdapter> v = this.drawnMapElementsFilter
                    .filter(mapDrawableAdapters.elements());
            // add selected element
            for (Enumeration<MapDrawableAdapter> e = mapDrawableAdapters
                    .elements(); e.hasMoreElements();) {
                MapDrawableAdapter mda = e.nextElement();
                if (mda instanceof MapElementAdapter) {
                    MapElementAdapter mea = (MapElementAdapter) mda;
                    if (mea.isSelected() && !v.contains(mea)) {
                        v.add(mea);
                    }
                }
            }
            // update activities with connection arrows
            for (Enumeration<MapDrawableAdapter> e = v.elements(); e
                    .hasMoreElements();) {
                MapDrawableAdapter mda = e.nextElement();
                if (mda instanceof MapActivityAdapter) {
                    MapActivityAdapter maa = (MapActivityAdapter) mda;
                    if (maa.getOwner() != null
                            && v.contains(getMapElementAdapter(maa.getOwner()))) {
                        maa.setArrowDisplayed(true);
                    } else {
                        maa.setArrowDisplayed(false);
                    }
                    maa.reCompile(getProjection(), glc);
                }
            }
            // update the list with the shapes
            if (show_geo_region || !grid_based_pv) {
                for (Enumeration<MapDrawableAdapter> e = mapDrawableAdapters
                        .elements(); e.hasMoreElements();) {
                    MapDrawableAdapter mda = e.nextElement();
                    if (mda instanceof MapShapeAdapter
                            && region.contains((Shape) mda.getObject())) {
                        v.add((MapShapeAdapter) mda);
                    }
                }
            }

            // Sort the elements
            java.util.Collections.sort(v, mapDrawableAdapterComparator);
            int[] res = new int[v.size()];
            for (int i = 0; i < res.length; i++) {
                MapDrawableAdapter mda = v.get(i);
                res[i] = mda.getDisplayList();
            }
            // update the list of the drawn elements
            drawnMapDrawablesListBuf = Buffers.newDirectIntBuffer(res.length);
            drawnMapDrawablesListBuf.put(res);
            drawnMapDrawablesListBuf.rewind();
            isDrawnMapDrawablesListUpdated = true;
        }
    }

    /**
     * Adds a new MapDrawableAdapter to this map.
     */
    protected MapDrawableAdapter addMapDrawableAdapter(
            StratmasObject mapDrawable) {
        MapDrawableAdapter drawableAdapter = MapDrawableAdapter
                .getMapDrawableAdapter(mapDrawable);
        int renderSelectionName = getNewRenderSelectionName(drawableAdapter
                .getNrOfRenderSelectionNames());
        drawableAdapter.setRenderSelectionName(renderSelectionName);
        synchronized (mapDrawableAdapters) {
            mapDrawableAdapters.put(mapDrawable, drawableAdapter);
        }

        if (drawableAdapter instanceof MapElementAdapter) {
            ((MapElementAdapter) drawableAdapter)
                    .setSymbolOpacity(getSymbolOpacity());
            ((MapElementAdapter) drawableAdapter)
                    .setSymbolScale(getSymbolScale());
            ((MapElementAdapter) drawableAdapter)
                    .setLocationOpacity(getLocationOpacity());
            ((MapElementAdapter) drawableAdapter)
                    .setDrawLocation(isEnabledLocation());
            ((MapElementAdapter) drawableAdapter)
                    .setDrawLocationOutline(isEnabledOutline());
            ((MapElementAdapter) drawableAdapter)
                    .setInvariantSymbolSize(getInvariantSymbolSize());
        }
        if (drawableAdapter instanceof ElementAdapter) {
            ((ElementAdapter) drawableAdapter)
                    .setIgnorePresent(getIgnorePresent());
        }
        if (drawableAdapter instanceof PopulationAdapter) {
            ((PopulationAdapter) drawableAdapter)
                    .setDrawElementName(getShowPopulationNames());
        }

        renderSelectionNames.put(new Integer(renderSelectionName),
                                 drawableAdapter);
        drawableAdapter.addMapDrawableAdapterListener(this);
        
        synchronized (mapDrawableAdapterRecompilation) {
            mapDrawableAdapterRecompilation.add(drawableAdapter);
        }

        addMapDrawableDisplayList(drawableAdapter.getDisplayList());
        setIsDrawnMapDrawablesListUpdated(false);

        return drawableAdapter;
    }

    /**
     * Whether to show population names or not.
     * 
     * @param flag true if population names should be shown.
     */
    public void setShowPopulationNames(boolean flag) {
        if (getShowPopulationNames() != flag) {
            this.showPopulationNames = flag;
            for (Enumeration<MapDrawableAdapter> e = (new TypeFilter(
                    TypeFactory.getType("Population")))
                    .filter(mapDrawableAdapters.elements()).elements(); e
                    .hasMoreElements();) {
                ((PopulationAdapter) e.nextElement())
                        .setDrawElementName(getShowPopulationNames());
            }
            update();
        }
    }

    /**
     * Returns true if names of populations are drawn.
     */
    public boolean getShowPopulationNames() {
        return this.showPopulationNames;
    }

    /**
     * Whether to symbol-size should be invariant or not
     * 
     * @param flag true if symbol-size should be invariant.
     */
    public void setInvariantSymbolSize(boolean flag) {
        if (getInvariantSymbolSize() != flag) {
            this.invariantSymbolSize = flag;
            for (Enumeration<MapDrawableAdapter> e = mapDrawableAdapters
                    .elements(); e.hasMoreElements();) {
                MapDrawableAdapter mda = e.nextElement();
                if (mda instanceof MapElementAdapter) {
                    ((MapElementAdapter) mda).setInvariantSymbolSize(flag);
                }
            }
            update();
        }
    }

    /**
     * Returns true if symbol-size should be invariant with regard to map scale.
     */
    public boolean getInvariantSymbolSize() {
        return this.invariantSymbolSize;
    }

    /**
     * Sets the symbol opacity of this MapElementAdapter to the specified value (between 0.0 and 1.0 inclusive);
     * 
     * @param symbolOpacity the new opacity.
     */
    public void setSymbolOpacity(double symbolOpacity) {
        if (this.symbolOpacity != symbolOpacity) {
            this.symbolOpacity = symbolOpacity;
            for (Enumeration<MapDrawableAdapter> e = mapDrawableAdapters
                    .elements(); e.hasMoreElements();) {
                MapDrawableAdapter mda = e.nextElement();
                if (mda instanceof MapElementAdapter) {
                    ((MapElementAdapter) mda)
                            .setSymbolOpacity(getSymbolOpacity());
                }
            }
        }
    }

    /**
     * Returns the symbol opacity of this MapElementAdapter.
     */
    public double getSymbolOpacity() {
        return this.symbolOpacity;
    }

    /**
     * Sets the symbol scale of this MapElementAdapter to the specified value (between 0.0 and 1.0 inclusive);
     * 
     * @param symbolScale the new opacity.
     */
    public void setSymbolScale(double symbolScale) {
        if (this.symbolScale != symbolScale) {
            this.symbolScale = symbolScale;
            for (Enumeration<MapDrawableAdapter> e = mapDrawableAdapters
                    .elements(); e.hasMoreElements();) {
                MapDrawableAdapter mda = e.nextElement();
                if (mda instanceof MapElementAdapter) {
                    ((MapElementAdapter) mda).setSymbolScale(getSymbolScale());
                }
            }
            update();
        }
    }

    /**
     * Returns the symbol scale of this MapElementAdapter.
     */
    public double getSymbolScale() {
        return this.symbolScale;
    }

    /**
     * Returns the symbol opacity of this MapElementAdapter.
     */
    public double getLocationOpacity() {
        return this.locationOpacity;
    }

    /**
     * Sets the location opacity of this MapElementAdapter to the specified value (between 0.0 and 1.0 inclusive);
     * 
     * @param locationOpacity the new opacity.
     */
    public void setLocationOpacity(double locationOpacity) {
        if (this.locationOpacity != locationOpacity) {
            this.locationOpacity = locationOpacity;
            for (Enumeration<MapDrawableAdapter> e = mapDrawableAdapters
                    .elements(); e.hasMoreElements();) {
                MapDrawableAdapter mda = e.nextElement();
                if (mda instanceof MapElementAdapter) {
                    ((MapElementAdapter) mda)
                            .setLocationOpacity(getLocationOpacity());
                }
            }
        }
    }

    /**
     * Make adapter adapter always draw element, present or not
     * 
     * @param flag true if always draw.
     */
    protected void setIgnorePresent(boolean flag) {
        if (flag != getIgnorePresent()) {
            this.ignorePresent = flag;
            for (Enumeration<MapDrawableAdapter> adapters = mapDrawableAdapters
                    .elements(); adapters.hasMoreElements();) {
                MapDrawableAdapter adapter = adapters.nextElement();
                if (adapter instanceof ElementAdapter) {
                    ((ElementAdapter) adapter)
                            .setIgnorePresent(getIgnorePresent());
                }
            }
            update();
        }
    }

    /**
     * Returns true if adapters always draw element, present or not
     */
    protected boolean getIgnorePresent() {
        return this.ignorePresent;
    }

    /**
     * Take a screen shot of the current map.
     * 
     * @param gld the GL context.
     */
    void screenShot(GLAutoDrawable gld) {
        doScreenShot = false;
        // Try shooting the screen.
        try {
            GLScreenShotHandler.doGLScreenShot(gld);
        } catch (RuntimeException e) {

            throw e;
        }
    }

    /**
     * Returns true if screenshot has been requested since the last redraw.
     */
    protected boolean doScreenShot() {
        return this.doScreenShot;
    }

    /**
     * Requests that a screenshot should be made.
     */
    public void setDoScreenShot() {
        doScreenShot = true;
        glc.validate();
        glc.repaint(10);
    }

    /**
     * Indicates that screenshot is finished.
     */
    protected void unSetDoScreenShot() {
        doScreenShot = false;
    }

    /**
     * This method is used to magnify symbols currenly drawn on the map.
     */
    protected void magnifyElements(GLAutoDrawable gld) {
        GL2 gl = (GL2) gld.getGL();
        Vector<Object> elements = mapDrawableAdaptersUnderCursor(MapElementAdapter.class);
        if (elements.size() > 0) {
            // Draws a magnification of the symbols under the cursor

            // Some constants (at least presently...).
            double hTileSize = 50;
            double vTileSize = 50;

            double hTileSpacing = 5;
            double vTileSpacing = 5;
            int textLines = 4;
            double vTextSize = 10;
            double vTextSpace = textLines * vTextSize;
            // 104.76 is the GLUT monospace font width. 119.05
            // + 33.33 is the maximal height of the GLUT
            // monospace font.
            int charsPerLine = (int) (hTileSize / (104.76 * vTextSize / (119.05 + 33.33)));

            double hSubPanelSize = hTileSize + hTileSpacing * 2;
            double vSubPanelSize = vTileSize + vTileSpacing + vTextSpace;

            int[] viewport = new int[4];
            gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);

            // Decide layout.
            double aspectRatio = ((double) viewport[2])
                    / ((double) viewport[3]);
            double subPanelSizeRatio = hSubPanelSize / vSubPanelSize;

            int hTiles = (int) (Math.round(Math.sqrt((double) elements.size())
                    * aspectRatio / subPanelSizeRatio));
            hTiles = hTiles != 0 ? hTiles : 1;
            hTiles = hTiles > elements.size() ? elements.size() : hTiles;
            int vTiles = elements.size() / hTiles
                    + (elements.size() % hTiles == 0 ? 0 : 1);

            double hPanelSize = hTiles * hSubPanelSize;
            double vPanelSize = vTiles * vSubPanelSize;

            gl.glMatrixMode(GL2.GL_PROJECTION);

            double symbolScale = 1.0d;
            if (getInvariantSymbolSize()) {
                DoubleBuffer buf = Buffers.newDirectDoubleBuffer(16);
                gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, buf);
                symbolScale = 0.000004d / buf.get(0);
            }

            gl.glPushMatrix();
            gl.glLoadIdentity();

            glu.gluOrtho2D(0 - hPanelSize / 2, 0 + hPanelSize / 2,
                           0 - vPanelSize / 2, 0 + vPanelSize / 2);

            double scale = this.magnifierSizeScale * 0.5d;
            if (vTiles <= hTiles) {
                gl.glScaled(scale,
                            scale
                                    * (vPanelSize / hPanelSize)
                                    * (((double) viewport[2]) / ((double) viewport[3])),
                            1.0d);
            } else {
                gl.glScaled(scale
                                    * (hPanelSize / vPanelSize)
                                    * (((double) viewport[3]) / ((double) viewport[2])),
                            scale, 1.0d);
            }

            // Extra bells and whistles
            // gl.glRotated((1.0d - this.magnifierSizeScale) * 180.0, 0.0d, 0.0d, 1.0d);

            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glPushMatrix();
            gl.glLoadIdentity();
            gl.glTranslated(-hPanelSize / 2, -vPanelSize / 2, 0);

            gl.glColor4d(1.0d, 1.0d, 1.0d, 0.2d);
            gl.glRectd(0, 0, hPanelSize, vPanelSize);

            Enumeration<Object> e = elements.elements();
            GLUT glut = new GLUT();
            for (int i = (vTiles - 1); i >= 0 && e.hasMoreElements(); i--) {
                for (int j = 0; j < hTiles && e.hasMoreElements(); j++) {
                    MapDrawableAdapter drawableAdapter = (MapDrawableAdapter) e
                            .nextElement();
                    if (drawableAdapter instanceof MapElementAdapter) {
                        MapElementAdapter elementAdapter = (MapElementAdapter) drawableAdapter;
                        // Position on tile.
                        gl.glMatrixMode(GL2.GL_MODELVIEW);
                        gl.glPushMatrix();
                        gl.glTranslated(j * hSubPanelSize, i * vSubPanelSize,
                                        0.0d);

                        // Draw tile background.
                        // gl.glColor4d(1.0d, 1.0d, 1.0d, 0.4d);
                        gl.glColor4d(1.0d, 1.0d, 1.0d,
                                     0.4 + 0.6 * (1.0 - getSymbolOpacity()));
                        gl.glRectd(hTileSpacing, vTileSpacing, hSubPanelSize
                                - hTileSpacing, vSubPanelSize - vTileSpacing);

                        // Scale and draw symbol.
                        gl.glMatrixMode(GL2.GL_MODELVIEW);
                        gl.glPushMatrix();
                        // Symbols are drawn from center...
                        gl.glTranslated(hSubPanelSize / 2, vTextSpace
                                + vTileSize / 2, 0);

                        gl.glScaled(hTileSize
                                            / (elementAdapter
                                                    .getHorizontalSymbolSize()
                                                    * elementAdapter
                                                            .getSymbolScale() * symbolScale),
                                    hTileSize
                                            / (elementAdapter
                                                    .getVerticalSymbolSize()
                                                    * elementAdapter
                                                            .getSymbolScale() * symbolScale),
                                    1.0d);

                        // Draw symbol.
                        gl.glCallList(elementAdapter.getSymbolDisplayList());

                        gl.glMatrixMode(GL2.GL_MODELVIEW);
                        gl.glPopMatrix();

                        // Draw name of element. Check
                        // comments above for the meaning of
                        // the constants.
                        gl.glMatrixMode(GL2.GL_MODELVIEW);
                        gl.glPushMatrix();
                        gl.glTranslated(hTileSpacing,
                                        vTextSpace - vTileSpacing, 0);
                        gl.glScaled(vTextSize / (119.05 + 33.33), vTextSize
                                / (119.05 + 33.33), 1.0);
                        gl.glColor4d(0.0d, 0.0d, 0.0d, 1.0d);

                        // Do not draw name if this is a population
                        // adapter and we show population names, since
                        // the name will then be a part of the symbol.
                        if (!(elementAdapter instanceof PopulationAdapter && getShowPopulationNames())) {
                            try {
                                String str = elementAdapter.getGLUTIDString();
                                for (int k = 0; k < textLines
                                        && k * charsPerLine <= str.length(); k++) {
                                    if ((k + 1) * charsPerLine < str.length()) {
                                        glut.glutStrokeString(GLUT.STROKE_MONO_ROMAN,
                                                              str.substring(k
                                                                                    * charsPerLine,
                                                                            (k + 1)
                                                                                    * charsPerLine));
                                    } else {
                                        glut.glutStrokeString(GLUT.STROKE_MONO_ROMAN,
                                                              str.substring(k
                                                                                    * charsPerLine,
                                                                            str.length()));
                                    }
                                    gl.glTranslated(-(charsPerLine * 104.76d),
                                                    -(119.05 + 33.33), 0.0);
                                }

                            } catch (UnsupportedEncodingException ex) {
                                Debug.err.println(ex.toString());
                            }
                        }
                        gl.glMatrixMode(GL2.GL_MODELVIEW);
                        gl.glPopMatrix();

                        gl.glMatrixMode(GL2.GL_MODELVIEW);
                        gl.glPopMatrix();
                    }
                }
            }
        }
    }

    /**
     * Imports all elements and activities to the map.
     */
    public void importMapElements() {
        // Find the element-list in the scenario. Import all elements
        // from that list and then add a listener that imports any
        // additional elements added to elements
        TypeFilter filter = new TypeFilter(TypeFactory.getType("Scenario"),
                true);
        for (Enumeration<StratmasObject> e = filter.filterTree(basicMap
                .getClient().getRootObject()); e.hasMoreElements();) {
            StratmasObject scenario = e.nextElement();
            // Find lists that may generate new Elements and put a listener on it.
            for (Enumeration<StratmasObject> ee = scenario.children(); ee
                    .hasMoreElements();) {
                StratmasObject candidate = ee.nextElement();
                if (candidate.getType().canSubstitute("Element")
                        || candidate.getType().canSubstitute("Activity")
                        || candidate.getType().canSubstitute("Graph")) {
                    if (candidate instanceof StratmasList) {
                        // Add any current elements and add a listener that imports any consequent ones.
                        for (Enumeration<StratmasObject> le = candidate
                                .children(); le.hasMoreElements();) {
                            addMapDrawable(le.nextElement());
                        }
                        candidate.addEventListener(new StratmasEventListener() {
                            public void eventOccured(StratmasEvent subEvent) {
                                if (subEvent.isObjectAdded()) {
                                    addMapDrawable((StratmasObject) subEvent
                                            .getArgument());
                                } else if (subEvent.isRemoved()) {
                                    ((StratmasObject) subEvent.getSource())
                                            .removeEventListener(this);
                                } else if (subEvent.isReplaced()) {
                                    // UNTESTED - the replace code is untested 2005-09-22
                                    Debug.err
                                            .println("FIXME - Replace behavior untested in MapDrawer");
                                    ((StratmasObject) subEvent.getSource())
                                            .removeEventListener(this);
                                    ((StratmasObject) subEvent.getArgument())
                                            .addEventListener(this);
                                }
                            }
                        });
                    } else {
                        addMapDrawable(candidate);
                    }
                }
            }
        }
    }

    /**
     * Updates this component, calls super and sets latestUpdateTimer.
     */
    public void update() {
        // FIXME set the dragged objects as needing refresh instead of global here
        setIsDrawnMapDrawablesListUpdated(false);
        super.update();
        latestUpdateTime = System.currentTimeMillis();
    }

    public void dispose(GLAutoDrawable glad) {
        // TODO implement
    }
}
