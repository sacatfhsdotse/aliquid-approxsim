package StratmasClient.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.IntBuffer;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import StratmasClient.BoundingBox;
import StratmasClient.Configuration;
import StratmasClient.Debug;
import StratmasClient.map.adapter.MapDrawableAdapter;
import StratmasClient.map.adapter.GraphNodeAdapter;
import StratmasClient.map.adapter.MapDrawableAdapterListener;
import StratmasClient.object.Shape;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.StratmasEventListener;
import StratmasClient.object.StratmasList;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.primitive.Reference;
import StratmasClient.proj.MGRSConversion;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.gl2.GLUT;

/**
 * The base class for the map. The shapes of the region and the graticules are displayed. Further the cursor position is displayed while
 * moving it.
 * 
 * @version 1.0
 * @author Amir Filipovic, Daniel Ahlin
 */
public abstract class BasicMapDrawer extends JPanel implements GLEventListener,
        MapDrawableAdapterListener, MouseListener, MouseMotionListener,
        MouseWheelListener {
    /**
	 * 
	 */
    private static final long serialVersionUID = -7511832126532781325L;
    /**
     * The reference to the map container.
     */
    protected BasicMap basicMap;
    /**
     * The geographical region consisting of <code>Shape</code> objects.
     */
    protected Region region;
    /**
     * The zooming &amp; scaling.
     */
    protected ZoomAndScale zoom_and_scale;
    /**
     * The position/navigation map.
     */
    protected PositionMap position_map;
    /**
     * The bounding box of the region displayed on the map. The size of this bounding box is somewhat larger then the bounding box of the
     * shape representing the region. The center of the box is equal to the center of the shape.
     */
    protected BoundingBox box;
    /**
     * The orthographic bounding box of the region displayed on the map. The size of this bounding box is larger then the bounding box of
     * the region shape and depends on the frame displaying the region. The purpose of this bounding box is to preserve the undistorted
     * display of the region. The center of the box is equal to the center of the currenly displayed part of the region on the map.
     */
    protected BoundingBox ort_box;
    /**
     * The horizontal center of the orthographic bounding box.
     */
    protected double ort_xc;
    /**
     * The vertical center of the orthographic bounding box.
     */
    protected double ort_yc;
    /**
     * The scaled orthographic bounding box of the region displayed on the map. This is the bounding box of the currently displayed part of
     * the region on the map.
     */
    protected BoundingBox orts_box;
    /**
     * Indicates if the mouse cursor is over the map.
     */
    protected boolean mouse_on = false;
    /**
     * Upper left x-coordinate of the viewport.
     */
    protected int view_x;
    /**
     * Upper left y-coordinate of the viewport.
     */
    protected int view_y;
    /**
     * Width of the viewport.
     */
    protected int view_width;
    /**
     * Height of the viewport.
     */
    protected int view_height;
    /**
     * Current position (on map) of the mouse cursor.
     */
    protected MapPoint current_pos;
    /**
     * Whether the mouse is currently being dragged (i.e. with left mousebutton pressed).
     */
    protected boolean draggingMouse = false;
    /**
     * X-position of mouse when mousedrag started or after last time map was moved.
     */
    protected int mouseDragStartX;
    /**
     * Y-position of mouse when mousedrag started or after last time map was moved.
     */
    protected int mouseDragStartY;
    /**
     * The latest time the map is drawn.
     */
    protected long latestUpdateTime = 0;
    /**
     * The minimum delay between the updates (in milliseconds).
     */
    protected long updateTimeDelay = 50;
    /**
     * Identifier for the display list of the graticules.
     */
    protected int graticuleDisplayList;
    /**
     * The actual graticule spacing.
     */
    protected int spacing = GraticuleLayer.TEN_DEGREES;
    /**
     * The background color.
     */
    private Color backgroundColor = new Color(0.6f, 0.8f, 1.0f);
    /**
     * The actual drawing area.
     */
    protected GLCanvas glc;
    /**
     * The glu to use
     */
    protected GLU glu = new GLU();
    /**
     * The text filed for the current position etc.
     */
    protected JTextField info_field = new JTextField();
    /**
     * The text filed for the region currently pointed by the mouse cursor.
     */
    protected JTextField regionTextField = new JTextField();
    /**
     * Font display class.
     */
    protected GLUT glut = new GLUT();
    /**
     * The frame.
     */
    protected JFrame frame = new JFrame();
    /**
     * Width of the frame.
     */
    protected int frame_width;
    /**
     * Height of the frame.
     */
    protected int frame_height;
    /**
     * The minimum allowed width for the display area ie. the canvas.
     */
    protected final int minDisplayAreaWidth = 50;
    /**
     * The minimum allowed height for the display area ie. the canvas.
     */
    protected final int minDisplayAreaHeight = 50;
    /**
     * The hashtable mapping renderSelectionNames to MapDrawableAdapters.
     */
    protected Hashtable<Integer, StratmasEventListener> renderSelectionNames = new Hashtable<Integer, StratmasEventListener>();
    /**
     * The counter assigning new renderSelectionNames
     */
    protected static int renderSelectionNameCounter = 1;
    /**
     * Display lists for displayed objects.
     */
    protected int mapDrawableDisplayLists[] = null;
    /**
     * Display lists for displayed objects that are drawn.
     */
    protected IntBuffer drawnMapDrawablesListBuf = Buffers
            .newDirectIntBuffer(0);
    /**
     * Indicates whether drawnMapDrawablesList need to be recreated.
     */
    protected boolean isDrawnMapDrawablesListUpdated = false;
    /**
     * The name of an empty list.
     */
    protected int emptyDisplayList = -1;
    /**
     * The blocksize in which new chunks are allocated to displayLists.
     */
    public static final int DISPLAYLISTS_BLOCKSIZE = 64;
    /**
     * Stack containing empty slots in mapDrawableDisplayList array.
     */
    protected Stack<Integer> mapDrawableDisplayListFreeStack = null;
    /**
     * Vector containing mapDrawableAdapters that has indicated that they need a recompile.
     */
    protected Vector<MapDrawableAdapter> mapDrawableAdapterRecompilation = new Vector<MapDrawableAdapter>();
    /**
     * Hashtable containing all mapDrawableAdapters that are used by this Map.
     */
    protected Hashtable<StratmasObject, MapDrawableAdapter> mapDrawableAdapters = new Hashtable<StratmasObject, MapDrawableAdapter>();
    /**
     * Comparator used to sort mapDrawableAdapters before drawing.
     */
    protected final Comparator<MapDrawableAdapter> mapDrawableAdapterComparator = new MapDrawableComparator();

    /**
     * Creates new BasicMapDrawer
     * 
     * @param basicMap the map container.
     * @param region the region displayed on the map.
     */
    public BasicMapDrawer(BasicMap basicMap, Region region) {
        // create JOGL canvas
        GLCapabilities glcaps = new GLCapabilities(GLProfile.getDefault());
        glcaps.setHardwareAccelerated(true);
        glcaps.setStencilBits(1);
        glc = new GLCanvas(glcaps);
        glc.addGLEventListener(this);
        glc.addMouseListener(this);
        glc.addMouseMotionListener(this);
        glc.addMouseWheelListener(this);

        // initialize the list of display lists for drawables
        initMapDrawableDisplayLists();

        // reference to the map container
        this.basicMap = basicMap;

        // region associated with the map
        this.region = region;
        for (Enumeration<Shape> e = region.getShapes().elements(); e
                .hasMoreElements();) {
            Shape s = e.nextElement();
            addMapDrawableAdapter(s);
        }

        // increase bounding box for the region
        box = region.getProjectedBounds();
        double dx = Math.abs(box.getXmax() - box.getXmin());
        double dy = Math.abs(box.getYmax() - box.getYmin());
        double xmin = box.getXmin() - dx / 10;
        double ymin = box.getYmin() - dy / 10;
        double xmax = box.getXmax() + dx / 10;
        double ymax = box.getYmax() + dy / 10;
        box = new BoundingBox(xmin, ymin, xmax, ymax, this.getProjection());

        // initialize orthographic view bounds and view center
        ort_box = (BoundingBox) box.clone();
        ort_xc = (ort_box.getXmax() + ort_box.getXmin()) / 2;
        ort_yc = (ort_box.getYmax() + ort_box.getYmin()) / 2;

        // initialize scaled orthographic view bounds and view center
        orts_box = (BoundingBox) box.clone();
    }

    /**
     * Initialization of the drawing area. Part of GLEventListener interface.
     * 
     * @param gld needed for OpenGL.
     */
    public void init(GLAutoDrawable gld) {
        GL2 gl = (GL2) gld.getGL();

        // set the background color
        float[] bColor = backgroundColor.getRGBComponents(null);
        gl.glClearColor(bColor[0], bColor[1], bColor[2], bColor[3]);

        // enable smoothing for lines
        gl.glEnable(GL2.GL_LINE_SMOOTH);

        // enable shading
        gl.glShadeModel(GL2.GL_SMOOTH);

        // enable blending
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

        // set actual matrix
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        // initialize bounding box
        glu.gluOrtho2D(ort_box.getXmin(), ort_box.getXmax(), ort_box.getYmin(),
                       ort_box.getYmax());

        // update the display list of the graticule lines
        updateGraticuleList(gl);

        // Possibly new GL, reregister all mapDrawableAdapters
        for (Enumeration<MapDrawableAdapter> e = mapDrawableAdapters.elements(); e
                .hasMoreElements();) {
            e.nextElement().invalidateAllLists();
        }

        int[] viewport = new int[4];
        gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
        reshape(gld, viewport[0], viewport[1], viewport[2], viewport[3]);

        update();
    }

    /**
     * Drawing elements on the map. Part of GLEventListener interface.
     * 
     * @param gld needed for OpenGL.
     */
    public void display(GLAutoDrawable gld) {
        GL2 gl = (GL2) gld.getGL();

        // update orthographics view bounds
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluOrtho2D(orts_box.getXmin(), orts_box.getXmax(),
                       orts_box.getYmin(), orts_box.getYmax());

        // update graticules
        updateGraticuleList(gl);

        // draw the map
        drawGraph(gld);
    }

    /**
     * Called when the size of the display area is changed. Part of GLEventListener interface.
     * 
     * @param drawable needed for OpenGL.
     * @param x leftmost screen coordinate of the display area.
     * @param y uppermost screen coordinate of the display area.
     * @param width width of the display area.
     * @param height height of the display area.
     */
    public void reshape(GLAutoDrawable drawable, int x, int y, int width,
            int height) {
        // get new orthographic view bounds such that aspect ratio is
        // equal to display window's
        BoundingBox tmpBox = updateOrthographicBounds(width, height);
        // update the display area if the bounds are valid
        if (tmpBox.isValid() && width > minDisplayAreaWidth
                && height > minDisplayAreaHeight) {
            // update the orthographic bounds
            ort_box = tmpBox;
            // update window size
            view_x = x;
            view_y = y;
            view_width = width;
            view_height = height;
            // update the size of the frame
            frame_width = frame.getWidth();
            frame_height = frame.getHeight();
            if (zoom_and_scale != null) {
                // update map scale
                zoom_and_scale.update();
            }
        }
        // restore the display area with the latest valid bounds
        else {
            frame.setBounds(frame.getX(), frame.getY(), frame_width,
                            frame_height);
            frame.validate();
        }
    }

    /**
     * Not implemented. Part of GLEventListener interface.
     */
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
            boolean deviceChanged) {}

    /**
     * Signaled when an MapDrawableAdapters object is removed. Part of MapDrawableAdapterListener interface.
     * 
     * @param drawableAdapter the MapDrawableAdapter whose object is being removed.
     */
    public void mapDrawableAdapterRemoved(MapDrawableAdapter drawableAdapter) {
        removeMapDrawableAdapter(drawableAdapter);
    }

    /**
     * Signaled when displaylists in an MapDrawableAdapter needs to be recompiled. Part of MapDrawableAdapterListener interface.
     * 
     * @param drawableAdapter the MapDrawableAdapter that needs to be updated.
     */
    public void mapDrawableAdapterUpdated(MapDrawableAdapter drawableAdapter) {
        synchronized (mapDrawableAdapterRecompilation) {
            //FIXME remove
            if (drawableAdapter instanceof GraphNodeAdapter){System.out.println("graph updated");}
            mapDrawableAdapterRecompilation.add(drawableAdapter);
            // update the list
            setIsDrawnMapDrawablesListUpdated(false);
        }
        update();
    }

    /**
     * Signaled when an object is added to a MapDrawableAdapters object. Part of MapDrawableAdapterListener interface.
     * 
     * @param object the object that is added.
     */
    public void mapDrawableAdapterChildAdded(StratmasObject object) {
        addMapDrawable(object);
    }

    /**
     * Not implemented. Part of MouseListener interface.
     */
    public void mouseClicked(MouseEvent e) {}

    /**
     * Indicates that the mouse cursor is entered the map. Part of MouseListener interface.
     * 
     * @param e the event created by entering the map.
     */
    public void mouseEntered(MouseEvent e) {
        mouse_on = true;
    }

    /**
     * Indicates that the mouse cursor is exited the map. Part of MouseListener interface.
     * 
     * @param e event created by exiting the map.
     */
    public void mouseExited(MouseEvent e) {
        mouse_on = false;
        // display current position
        displayCurrentPosition(new MapPoint(0, 0));
        // display pointed region
        displayPointedRegion("");
        // redraw
        update();
    }

    /**
     * Starts the dragging action.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        mouseDragStartX = e.getX();
        mouseDragStartY = e.getY();
        draggingMouse = true;
    }

    /**
     * Ends the dragging action.
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        if (draggingMouse) {
            draggingMouse = false;
        }
    }

    /**
     * Performs the dragging action. I.e. moves the map a little bit.
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        if (draggingMouse) {
            int x = e.getX();
            int y = e.getY();
            MapPoint p1 = convertToLonLat(mouseDragStartX, mouseDragStartY);
            MapPoint p2 = convertToLonLat(x, y);
            p1 = p1.getProjectedPoint(getProjection());
            p2 = p2.getProjectedPoint(getProjection());
            double dx = p2.getX() - p1.getX();
            double dy = p2.getY() - p1.getY();
            setXYCenter(ort_xc - dx, ort_yc - dy); // move reverse motion dirction
            mouseDragStartX = x;
            mouseDragStartY = y;
        }
    }

    /**
     * Upadates the current position on the map. Part of MouseMotionListener interface.
     * 
     * @param e event created by changing the position of the mouse cursor on the map.
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        // get window coordinates
        int x = (int) e.getX();
        int y = (int) e.getY();

        // necessary for multi-screen enviroment
        mouse_on = (x >= view_x && x <= view_x + view_width && y >= view_y && y <= view_y
                + view_height) ? true : false;

        // convert the current position to lon/lat
        current_pos = convertToLonLat(x, y);

        // display current position
        displayCurrentPosition(current_pos);

        // redraw
        update();
    }

    /**
     * Part of MouseWheelListener interface. Zooms the map as the mousewheel is moved.
     */
    public void mouseWheelMoved(MouseWheelEvent e) {
        int x = e.getX();
        int y = e.getY();
        int rot = -e.getWheelRotation(); // zoom in on scroll up.

        // mouse point before
        MapPoint p = convertToLonLat(x, y).getProjectedPoint(getProjection());
        double ux = p.getX();
        double uy = p.getY();

        zoom_and_scale.changeSliderValue(rot);

        // mouse point after
        p = convertToLonLat(x, y).getProjectedPoint(getProjection());
        double ux2 = p.getX();
        double uy2 = p.getY();

        // adjust so after = before.
        double dx = ux - ux2;
        double dy = uy - uy2;
        setXYCenter(ort_xc + dx, ort_yc + dy);
    }

    /**
     * Create the GUI and show it.
     * 
     * @param frame_title the title of the map.
     */
    public void createAndShowGUI(String frame_title) {
        // create and set up the window
        frame.setTitle(frame_title);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // add a listener to the frame
        final BasicMapDrawer self = this;
        final BasicMap smap = basicMap;
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (JOptionPane.showConfirmDialog(self.frame,
                                                  "Really close map?",
                                                  "Closing map window...",
                                                  JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    if (smap instanceof StratMap) {
                        Visualizer.removeMap((StratMap) smap);
                        ((StratMap) smap).doDispose();
                    }
                    self.doDispose();
                }
            }
        });

        // necessary when heavyweight and lightweight components intersect
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        // frame size (test adapted for now on)
        Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
        frame_width = screen_size.width / 2;
        frame_height = screen_size.height - screen_size.height / 5;
        frame.setSize(frame_width, frame_height);
        frame.setLocation((screen_size.width - frame_width) >> 1,
                          (screen_size.height - frame_height) >> 1);

        // create text field for the coordinate under the mouse cursor
        info_field.setEditable(false);
        info_field.setBackground(this.getBackground());

        // create text field for the region under the mouse cursor
        regionTextField.setEditable(false);
        regionTextField.setBackground(this.getBackground());

        JPanel textFieldPanel = new JPanel(new GridLayout(1, 2));
        textFieldPanel.add(info_field);
        textFieldPanel.add(regionTextField);

        // add the canvas and the text field to the panel
        setLayout(new BorderLayout());
        add(glc, BorderLayout.CENTER);
        add(textFieldPanel, BorderLayout.SOUTH);

        // add the panel to the farme
        frame.getContentPane().add(this, BorderLayout.CENTER);
        frame.setResizable(true);

        // thread safety recomendation
        final JFrame fframe = frame;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                fframe.setVisible(true);
            }
        });
    }

    /**
     * Updates the otrhographic bounds of the display area and adapts the displayed objects to the size of the area. This is needed to avoid
     * the distortion of the objects.
     * 
     * @param width width of the displayed area.
     * @param height height of the displayed area.
     * @return the orthographic bounds of the display area.
     */
    protected BoundingBox updateOrthographicBounds(int width, int height) {
        double x_ratio = (ort_box.getXmax() - ort_box.getXmin()) / width;
        double y_ratio = (ort_box.getYmax() - ort_box.getYmin()) / height;
        if (x_ratio < y_ratio) {
            double dx = (ort_box.getYmax() - ort_box.getYmin()) * width
                    / height;
            double ort_xmin = ort_xc - dx / 2;
            double ort_xmax = ort_xc + dx / 2;
            double ort_ymin = ort_box.getYmin();
            double ort_ymax = ort_box.getYmax();
            return new BoundingBox(ort_xmin, ort_ymin, ort_xmax, ort_ymax,
                    this.getProjection());
        } else {
            double dy = (ort_box.getXmax() - ort_box.getXmin()) * height
                    / width;
            double ort_xmin = ort_box.getXmin();
            double ort_xmax = ort_box.getXmax();
            double ort_ymin = ort_yc - dy / 2;
            double ort_ymax = ort_yc + dy / 2;
            return new BoundingBox(ort_xmin, ort_ymin, ort_xmax, ort_ymax,
                    this.getProjection());
        }
    }

    /**
     * Displays current position in geodetic or MGRS coordinates.
     * 
     * @param current_pos the current position.
     */
    public void displayCurrentPosition(MapPoint current_pos) {
        if (mouse_on) {
            // display geodetic coordinates
            if (Configuration.getCoordinateSystem() == Configuration.GEODETIC) {
                DecimalFormat resultFormat = new DecimalFormat("0.00");
                String lats = resultFormat.format(current_pos.getLat());
                String lons = resultFormat.format(current_pos.getLon());
                info_field.setText("Latitude: " + lats
                        + MapConstants.DEGREE_SYMBOL + ", Longitude: " + lons
                        + MapConstants.DEGREE_SYMBOL);
            }
            // display MGRS coordinates
            else if (Configuration.getCoordinateSystem() == Configuration.MGRS) {
                String mgrs = MGRSConversion.convertGeodeticToMGRS(Math
                        .toRadians(current_pos.getLon()), Math
                        .toRadians(current_pos.getLat()), 5);
                info_field.setText("MGRS: " + mgrs);
            }
        }
        // display empty string
        else {
            info_field.setText("");
        }
    }

    /**
     * Displays the region name currently under the mouse pointer.
     * 
     * @param regionName the name of the region under the mouse pointer.
     */
    public void displayPointedRegion(String regionName) {
        if (mouse_on && regionName.length() > 0) {
            regionTextField.setText("Region : " + regionName);
        }
        // display empty string
        else {
            regionTextField.setText("");
        }
    }

    /**
     * Resets the map.
     */
    public void reset() {
        // reset the map
        update();
    }

    /**
     * Removes all the elements.
     */
    public void remove() {
        // remove all render selection names
        renderSelectionNames.clear();
        // remove all adapters
        mapDrawableAdapterRecompilation.removeAllElements();
        for (Enumeration<StratmasObject> e = mapDrawableAdapters.keys(); e
                .hasMoreElements();) {
            removeMapDrawableAdapter(mapDrawableAdapters.get(e.nextElement()));
        }
    }

    /**
     * Sets scaled orthographic bounds.
     * 
     * @param sbox bouding box of scaled orthographic bounds.
     */
    public void setScaledBoundingBox(BoundingBox sbox) {
        orts_box = (BoundingBox) sbox.clone();
        update();
    }

    /**
     * Sets center of orthographic view into projected (x,y) point.
     * 
     * @param x projected x coordinate.
     * @param y projected y coordinate.
     */
    public void setXYCenter(double x, double y) {
        // new center view and orthographics view bounds
        double dx = x - ort_xc;
        double dy = y - ort_yc;
        double tmp_xmin = ort_box.getXmin() + dx;
        double tmp_xmax = ort_box.getXmax() + dx;
        double tmp_ymin = ort_box.getYmin() + dy;
        double tmp_ymax = ort_box.getYmax() + dy;
        // if inside the bounding box of the region
        if (!(tmp_xmin > box.getXmax() || tmp_xmax < box.getXmin()
                || tmp_ymin > box.getYmax() || tmp_ymax < box.getYmin())) {
            // update orthographic bounds
            ort_box = new BoundingBox(tmp_xmin, tmp_ymin, tmp_xmax, tmp_ymax,
                    this.getProjection());
            ort_xc = (ort_box.getXmax() + ort_box.getXmin()) / 2;
            ort_yc = (ort_box.getYmax() + ort_box.getYmin()) / 2;
            // update scaled orthographoc bounds
            double ort_xmins = orts_box.getXmin() + dx;
            double ort_xmaxs = orts_box.getXmax() + dx;
            double ort_ymins = orts_box.getYmin() + dy;
            double ort_ymaxs = orts_box.getYmax() + dy;
            orts_box = new BoundingBox(ort_xmins, ort_ymins, ort_xmaxs,
                    ort_ymaxs, this.getProjection());
            // redraw
            update();
        }
    }

    /**
     * Sets reference to actual zoom & scale controller.
     */
    public void setZoomAndScale(ZoomAndScale zoom_and_scale) {
        this.zoom_and_scale = zoom_and_scale;
    }

    /**
     * Disposes the map window.
     */
    public void doDispose() {
        frame.dispose();
    }

    /**
     * Returns the frame.
     */
    public JFrame getFrame() {
        return frame;
    }

    /**
     * Returns the actual projection.
     */
    public Projection getProjection() {
        return basicMap.getProjection();
    }

    /**
     * Returns the map container.
     */
    public BasicMap getBasicMap() {
        return basicMap;
    }

    /**
     * Return maximum distance between projected outer x coordinates visible on the map. This is the distance between lower leftmost and
     * rightmost x coordinates on the map when the scale is largest.
     */
    public double getMaxRange() {
        return ort_box.getXmax() - ort_box.getXmin();
    }

    /**
     * Return minimum distance between projected outer x coordinates visible on the map. This is the distance (in meters) between the lower
     * leftmost and rightmost x coordinates on the map when the scale is smallest.
     */
    public double getMinRange() {
        return 1000.0;
    }

    /**
     * Width of the portview in screen coordinates.
     */
    public int getViewWidth() {
        return view_width;
    }

    /**
     * Height of the portview in screen coordinates.
     */
    public int getViewHeight() {
        return view_height;
    }

    /**
     * Returns bounding box in projected coordinates.
     */
    public BoundingBox getBoundingBox() {
        return ort_box;
    }

    /**
     * Returns scaled bounding box in projected coordinates.
     */
    public BoundingBox getScaledBoundingBox() {
        return orts_box;
    }

    /**
     * Returns the background color.
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Returns a sublist of mapDrawableAdapters list. The class of the elements in the sublist are decided by the input argument.
     * 
     * @param specifiedClass the class of the output elements.
     * @return the list of elements.
     */
    public Vector<MapDrawableAdapter> mapDrawableAdapters(
            Class<? extends MapDrawableAdapter> specifiedClass) {
        Vector<MapDrawableAdapter> res = new Vector<MapDrawableAdapter>();
        for (Enumeration<MapDrawableAdapter> e = mapDrawableAdapters.elements(); e
                .hasMoreElements();) {
            MapDrawableAdapter o = e.nextElement();
            if (specifiedClass.isInstance(o)) {
                res.add(o);
            }
        }
        return res;
    }

    /**
     * Returns the MapDrawableAdapter which contains the object with the specified reference.
     * 
     * @param ref the reference of the searched object.
     */
    public MapDrawableAdapter getMapDrawableAdapter(Reference ref) {
        for (Enumeration<MapDrawableAdapter> e = mapDrawableAdapters.elements(); e
                .hasMoreElements();) {
            MapDrawableAdapter mda = e.nextElement();
            if (mda.getObject().getReference().equals(ref)) {
                return mda;
            }
        }
        return null;
    }

    /**
     * Converts window coordinates to longitude and latitude coordinates.
     * 
     * @param x x screen coordinate.
     * @param y y screen coordinate.
     * @return [x,y] projected coordinates.
     */
    public MapPoint convertToLonLat(int x, int y) {
        // convert to projected coordinates
        double xx = orts_box.getXmin() + (x - view_x)
                * (orts_box.getXmax() - orts_box.getXmin()) / view_width;
        double yy = orts_box.getYmin() + (view_height - y + view_y)
                * (orts_box.getYmax() - orts_box.getYmin()) / view_height;
        // convert to lon and lat coordinates
        double[] ll = basicMap.getProjection().projToLonLat(xx, yy);

        return new MapPoint(ll[0], ll[1]);
    }

    /**
     * Converts distance in the screen coordinates to distance in the projected coordinates.
     * 
     * @param pixs distance in the screen coordinates.
     * @return distance in the projected coordinates.
     */
    public double convertScreenDistanceToProjectedDistance(int pixs) {
        MapPoint p1 = convertToLonLat(0, 0);
        MapPoint p2 = convertToLonLat(pixs, 0);
        return Math.abs(p1.getProjectedPoint(getProjection()).getX()
                - p2.getProjectedPoint(getProjection()).getX());
    }

    /**
     * Updates the display list of the graticule lines.
     */
    public void updateGraticuleList(GL2 gl) {
        graticuleDisplayList = (gl.glIsList(graticuleDisplayList)) ? graticuleDisplayList
                : gl.glGenLists(1);

        // update the list
        gl.glNewList(graticuleDisplayList, GL2.GL_COMPILE);
        // line color
        gl.glColor4f(0.1f, 0.1f, 0.1f, 0.5f);
        // width of the shape line
        gl.glLineWidth(1.0f);
        // draw graticules
        BoundingBox regionBox = region.getLonLatBounds();
        GraticuleLayer.drawGraticules(gl, regionBox, this.getProjection(),
                                      spacing);
        // ends the display lists
        gl.glEndList();
    }

    /**
     * All elements shown in the scene are drawn here.
     * 
     * @param gld needed for OpenGL.
     */
    protected abstract void drawGraph(GLAutoDrawable gld);

    /**
     * Repaints the map.
     */
    public void update() {
        glc.validate();
        glc.repaint();
    }

    /**
     * Update the list representing which drawables should be drawn.
     */
    public void updateDrawnMapDrawablesList() {
        if (!isDrawnMapDrawablesListUpdated()) {
            // get all mapDrawableAdapters that should be drawn
            Vector<MapDrawableAdapter> v = new Vector<MapDrawableAdapter>();
            for (Enumeration<MapDrawableAdapter> e = mapDrawableAdapters
                    .elements(); e.hasMoreElements();) {
                v.add(e.nextElement());
            }

            // Sort the elements
            java.util.Collections.sort(v, mapDrawableAdapterComparator);
            int[] res = new int[v.size()];
            for (int i = 0; i < res.length; i++) {
                MapDrawableAdapter mda = (MapDrawableAdapter) v.get(i);
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
     * Indicates whether the list of currently drawn mapDrawableDisplayLists should be updated.
     */
    protected boolean isDrawnMapDrawablesListUpdated() {
        return this.isDrawnMapDrawablesListUpdated;
    }

    /**
     * Sets whether the list of currently drawn mapDrawableDisplayLists should be updated.
     * 
     * @param status false if mapDrawableDisplayLists needs to be updated.
     */
    protected void setIsDrawnMapDrawablesListUpdated(boolean status) {
        isDrawnMapDrawablesListUpdated = status;
    }

    /**
     * Adds a new mapDrawable (Element or Activity) and all its mapDrawable children for display in this mapDrawer.
     * 
     * @param mapDrawable the mapDrawable that is added.
     */
    public void addMapDrawable(StratmasObject mapDrawable) {
        for (Enumeration<StratmasObject> ee = mapDrawable.children(); ee
                .hasMoreElements();) {
            StratmasObject candidate = ee.nextElement();
            if (candidate.getType().canSubstitute("Element")
                    || candidate.getType().canSubstitute("Activity")) {
                if (candidate instanceof StratmasList) {
                    // Add any current elements and add a listener
                    // that imports any consequent ones.
                    for (Enumeration<StratmasObject> le = candidate.children(); le
                            .hasMoreElements();) {
                        addMapDrawable(le.nextElement());
                    }
                } else {
                    addMapDrawable(candidate);
                }
                // add listeners to the list of the activities
                if (mapDrawable.getType().canSubstitute("Element")
                        && candidate.getType().canSubstitute("Activity")) {
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
                }
            }
        }

        if (mapDrawable.getType().canSubstitute("Graph")) {
            for (Enumeration<StratmasObject> ee = mapDrawable.children(); ee
                    .hasMoreElements();) {
                StratmasObject candidate = ee.nextElement();
                if (candidate instanceof StratmasList) {
                    for (Enumeration<StratmasObject> le = candidate.children(); le
                            .hasMoreElements();) {
                        StratmasObject candidate2 = le.nextElement();
                        if (candidate2.getType().canSubstitute("Node")
                                || candidate2.getType().canSubstitute("Edge")) {
                            addMapDrawableAdapter(candidate2);
                        }
                    }
                } else {
                    if (candidate.getType().canSubstitute("Node")
                            || candidate.getType().canSubstitute("Edge")) {
                        addMapDrawableAdapter(candidate);
                    }
                }
            }
        } else {
            addMapDrawableAdapter(mapDrawable);
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
     * Removes a MapDrawableAdapter from this map.
     * 
     * @param drawableAdapter the adapter to remove.
     */
    protected void removeMapDrawableAdapter(MapDrawableAdapter drawableAdapter) {
        drawableAdapter.removeMapDrawableAdapterListener(this);
        mapDrawableAdapters.remove(drawableAdapter.getObject());
        renderSelectionNames.remove(new Integer(drawableAdapter
                .getRenderSelectionName()));
        removeMapDrawableDisplayList(drawableAdapter.getDisplayList());
        setIsDrawnMapDrawablesListUpdated(false);
        update();
    }

    /**
     * Removes all MapDrawableAdapters of the class defined by the input argument from this map.
     * 
     * @param specifiedClass the class of the adapters to remove.
     */
    protected void removeMapDrawableAdapters(
            Class<? extends MapDrawableAdapter> specifiedClass) {
        Vector<MapDrawableAdapter> adapters = mapDrawableAdapters(specifiedClass);
        for (int i = 0; i < adapters.size(); i++) {
            removeMapDrawableAdapter(adapters.get(i));
        }
    }

    /**
     * Assigns new renderSelectionNames
     * 
     * @param extent number of names to reserve.
     */
    protected synchronized int getNewRenderSelectionName(int extent) {
        int res = renderSelectionNameCounter;
        renderSelectionNameCounter += extent;

        return res;
    }

    /**
     * Assigns new renderSelectionName
     */
    protected synchronized int getNewRenderSelectionName() {
        int res = renderSelectionNameCounter;
        renderSelectionNameCounter++;

        return res;
    }

    /**
     * Updates the list of renderSelectionNames.
     * 
     * @param adapter the adapter with the render selection name.
     * @param insert if true the name and the adapter are inserted in the list, if false the name and the adapter are removed from the list.
     */
    public void updateRenderSelectionNameList(MapDrawableAdapter adapter,
            boolean insert) {
        if (insert) {
            int renderSelectionName = getNewRenderSelectionName(adapter
                    .getNrOfRenderSelectionNames());
            adapter.setRenderSelectionName(renderSelectionName);
            renderSelectionNames.put(new Integer(renderSelectionName), adapter);
        } else {
            renderSelectionNames.remove(new Integer(adapter
                    .getRenderSelectionName()));
        }
    }

    /**
     * Initializes the mapDrawableDisplayLists array and associated Stack.
     */
    protected void initMapDrawableDisplayLists() {
        this.mapDrawableDisplayLists = new int[0];
        mapDrawableDisplayListFreeStack = new Stack<Integer>();
    }

    /**
     * Adds a new displayList to the lists.
     * 
     * @param displayList the list to add.
     */
    protected synchronized void addMapDrawableDisplayList(int displayList) {
        if (mapDrawableDisplayListFreeStack.empty()) {
            int newDisplayLists[] = new int[this.mapDrawableDisplayLists.length
                    + DISPLAYLISTS_BLOCKSIZE];
            for (int i = 0; i < mapDrawableDisplayLists.length; i++) {
                newDisplayLists[i] = mapDrawableDisplayLists[i];
            }
            for (int i = newDisplayLists.length - 1; i >= mapDrawableDisplayLists.length; i--) {
                newDisplayLists[i] = this.emptyDisplayList;
                mapDrawableDisplayListFreeStack.push(new Integer(i));
            }
            mapDrawableDisplayLists = newDisplayLists;
        }

        mapDrawableDisplayLists[((Integer) mapDrawableDisplayListFreeStack
                .pop()).intValue()] = displayList;
    }

    /**
     * Removes a displayList from the ones being displayed each update.
     * 
     * @param displayList the list to remove.
     */
    protected synchronized void removeMapDrawableDisplayList(int displayList) {
        for (int i = 0; i < mapDrawableDisplayLists.length; i++) {
            if (mapDrawableDisplayLists[i] == displayList) {
                mapDrawableDisplayLists[i] = this.emptyDisplayList;
                mapDrawableDisplayListFreeStack.push(new Integer(i));
            }
        }
    }

}
