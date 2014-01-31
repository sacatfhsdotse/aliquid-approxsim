package StratmasClient.map;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.text.DecimalFormat;
import java.lang.Character;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.*;
import java.net.URL;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUtessellator;
import javax.media.opengl.glu.GLUtessellatorCallbackAdapter;

import StratmasClient.Configuration;
import StratmasClient.object.Shape;
import StratmasClient.object.SimpleShape;
import StratmasClient.object.Composite;
import StratmasClient.object.Polygon;
import StratmasClient.object.Line;
import StratmasClient.object.Point;
import StratmasClient.BoundingBox;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.StratmasEventListener;
import StratmasClient.proj.MGRSConversion;

/**
 * Position/navigation window.
 * <p>
 * The following features are implemented in this class: <br>
 * 1. Shapes of the whole actual region are displayed in an interactive display window. A different
 *    colored rectangular area shows a region currently displayed in the main map ie. <code>MapDrawer</code>
 *    window. It is possible  to change the center of the area displayed in the main map by mouse 
 *    clicking in the window. <br>
 * 2. Panning buttons which enable to pan the main map in eight directions: north, south, east, west,
 *    northeast, northwest, southeast and southwest. <br>
 * 3. Minumum and maximum longitude and latitude values of the area displayed in the main map are shown.
 *
 * @see <code>MapDrawer</code>
 *
 * @version 1.0
 * @author Amir Filipovic 
 */
public class PositionMap implements GLEventListener, ActionListener, MouseListener, 
						   MouseMotionListener,StratmasEventListener {
    /**
     * Bounding box of the displayed region.
     */
    private BoundingBox box;
    /**
     * Panned bounding box of the displayed region.
     */
    private BoundingBox pbox;
    /**
     * Left screen coordinate of the display window.
     */
    private int view_x;
    /**
     * Upper screen coordinate of the display window.
     */
    private int view_y;
    /**
     * Width of the display window.
     */
    private int view_height;
    /**
     * Height of the display window.
     */
    private int view_width;
    /**
     * The geographical region diplayed in the map.
     */
    private Region region;
    /**
     * Adapter for tesselation callback mathods.
     */
    private TessellatorAdapter adapter;
    /*
     * Reference to the main container.
     */
    private StratMap stratmap;
    /*
     * Reference to the main map.
     */ 
    private BasicMapDrawer drawer;
    /**
     * Display list for the region.
     */
    private int reg;
    /**
     * Display list for the tesselated region.
     */
    private int tess_reg;
    /**
     * The actual drawing area.
     */
    private GLCanvas glcanvas;
    /**
     * The glu to use
     */
    private GLU glu = new GLU();
    /**
     * Width of the drawing area.
     */
    private int glcanvas_width = 150;
    /**
     * Height of the drawing area.
     */
    private int glcanvas_height = 150;
    /**
     * Button for panning the map up.
     */
    private JButton up_button;
    /**
     * Button for panning the map down.
     */
    private JButton down_button;
    /**
     * Button for panning the map left.
     */
    private JButton left_button;
    /**
     * Button for panning the map right.
     */
    private JButton right_button;
    /**
     * Button for panning the map upleft.
     */
    private JButton up_left_button;
    /**
     * Button for panning the map upright.
     */
    private JButton up_right_button;
    /**
     * Button for panning the map downleft.
     */
    private JButton down_left_button;
    /**
     * Button for panning the map downright.
     */
    private JButton down_right_button;
    /**
     * Button for panning the map upleft.
     */
    private JButton left_up_button = new JButton();
    /**
     * Button for panning the map upright.
     */
    private JButton right_up_button = new JButton();
    /**
     * Button for panning the map downleft.
     */
    private JButton left_down_button = new JButton();
    /**
     * Button for panning the map downright.
     */
    private JButton right_down_button = new JButton();
    /**
     * Label for north-east coordinates.
     */
    private JLabel ne_label = new JLabel();
    /**
     * Label for south-west coordinates
     */
    private JLabel sw_label = new JLabel();
    /**
     * The panel containing the map.
     */
    private JPanel positionMapPanel;
    /**
     * Format used to display coordinates.
     */
    private DecimalFormat dec_format = new DecimalFormat("0.0");
    /**
     * Representation of the symbol for degree.
     */
    private String degree_symbol;
    /**
     * Indicates if the region has to be updated.
     */
    private boolean update_region = false; 
    /**
     * Indicates if the dragging action has started.
     */
    private boolean drag_started = false;

    /**
     * Creates new position/navigation map.
     *
     * @param stratmap contains all elements of the current map.
     * @param region region shapes to be displayed.
     */
    public PositionMap(StratMap stratmap, Region region) {
	// create JOGL canvas
        GLCapabilities glcaps = new GLCapabilities();
	glcanvas = new GLCanvas(glcaps);
	glcanvas.addGLEventListener(this);
	glcanvas.addMouseListener(this);
	glcanvas.addMouseMotionListener(this);

	// 
	this.stratmap = stratmap;
	
	// geographical region associated with the map
	this.region = region;
	region.addListener(this);
	
	// projected bounds of the region
	box = region.getProjectedBounds();
		
	//adapt the bounding box
	double dx = Math.abs(box.getXmax()-box.getXmin());
	double dy = Math.abs(box.getYmax()-box.getYmin());
	double xmin = box.getXmin()-dx/10;
	double ymin = box.getYmin()-dy/10;
	double xmax = box.getXmax()+dx/10;
	double ymax = box.getYmax()+dy/10;
	box = new BoundingBox(xmin, ymin, xmax, ymax, stratmap.getProjection());
	
	// initialize panned boundary box
	pbox = (BoundingBox)box.clone();

	// get degree symbol
	degree_symbol = new String((new Character('\u00B0')).toString());
	
	// create the panel
	createPositionMapPanel();
	
	// show GUI
	// createAndShowGUI();
    }
    
    /**
     * Set reference to main map.
     *
     * @param drawer the main map window.
     */
    public void setMap(BasicMapDrawer drawer) {
	this.drawer = drawer;
    }
    
    /**
     * Create the GUI and show it.
     */
    public void createAndShowGUI() {
        //Create and set up the window.
        final JFrame frame = new JFrame("Position Map");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
        //Set up the content pane.
        positionMapPanel.setOpaque(true); //content panes must be opaque
        frame.setContentPane(positionMapPanel);
	
        //Display the window.
	frame.setSize(250, 250);
	frame.setResizable(true);
	
	// thread safety recommendation
	SwingUtilities.invokeLater (
				    new Runnable() {
					public void run() {
					    frame.setVisible(true);
					}
				    }
				    );
    }
    
    /**
     * Creates the panel containing the position map.
     */
    private void createPositionMapPanel() {
	// button colors
	Color middle = new Color(223, 223, 223);
	Color aside = new Color(213, 213, 213, 0);
	// create buttons
	up_button = new JButton(new ImageIcon(PositionMap.class.getResource("images/up16.gif")));
	down_button = new JButton(new ImageIcon(PositionMap.class.getResource("images/down16.gif")));
	left_button = new JButton(new ImageIcon(PositionMap.class.getResource("images/back16.gif")));
	right_button = new JButton(new ImageIcon(PositionMap.class.getResource("images/forward16.gif")));
	up_left_button = new JButton(new ImageIcon(PositionMap.class.getResource("images/left_up16.gif")));
	up_right_button = new JButton(new ImageIcon(PositionMap.class.getResource("images/right_up16.gif")));
	down_left_button = new JButton(new ImageIcon(PositionMap.class.getResource("images/left_down16.gif")));
	down_right_button = new JButton(new ImageIcon(PositionMap.class.getResource("images/right_down16.gif")));
	// add buttons and position map
	JPanel north_panel = new JPanel();
	north_panel.setLayout(new GridLayout(1,3,0,0));
	up_left_button.setBackground(aside);
	up_left_button.setContentAreaFilled(false);
	up_left_button.addActionListener(this);
	up_left_button.setBorderPainted(false);
	up_left_button.setMargin(new Insets(0,0,0,0));
	up_left_button.setHorizontalAlignment(SwingConstants.LEFT);
	north_panel.add(up_left_button);
	up_button.setBackground(middle);
	up_button.addActionListener(this);
	up_button.setBorderPainted(false);
	up_button.setMargin(new Insets(0,0,0,0));
	north_panel.add(up_button);
	up_right_button.setBackground(aside);
	up_right_button.setContentAreaFilled(false);
	up_right_button.addActionListener(this);
	up_right_button.setBorderPainted(false);
	up_right_button.setMargin(new Insets(0,0,0,0));
	up_right_button.setHorizontalAlignment(SwingConstants.RIGHT);
	north_panel.add(up_right_button);
	JPanel south_panel = new JPanel();
	south_panel.setLayout(new GridLayout(1,3,0,0));
	down_left_button.addActionListener(this);
	down_left_button.setBackground(aside);
	down_left_button.setContentAreaFilled(false);
	down_left_button.setBorderPainted(false);
	down_left_button.setMargin(new Insets(0,0,0,0));
	down_left_button.setHorizontalAlignment(SwingConstants.LEFT);
	south_panel.add(down_left_button);
	down_button.setBackground(middle);
	down_button.addActionListener(this);
	down_button.setBorderPainted(false);
	down_button.setMargin(new Insets(0,0,0,0));
	south_panel.add(down_button);
	down_right_button.setBackground(aside);
	down_right_button.setContentAreaFilled(false);
	down_right_button.addActionListener(this);
	down_right_button.setBorderPainted(false);
	down_right_button.setMargin(new Insets(0,0,0,0));
	down_right_button.setHorizontalAlignment(SwingConstants.RIGHT);
	south_panel.add(down_right_button);
	JPanel left_panel = new JPanel();
	left_panel.setLayout(new GridLayout(3,1,0,0));
	left_up_button.setBackground(aside);
	left_up_button.setContentAreaFilled(false);
	left_up_button.addActionListener(this);
	left_up_button.setBorderPainted(false);
	left_up_button.setPreferredSize(new Dimension(0,0));
	left_up_button.setModel(up_left_button.getModel());
	left_panel.add(left_up_button);
	left_button.setBackground(middle);
	left_button.addActionListener(this);
	left_button.setBorderPainted(false);
	left_button.setMargin(new Insets(0,0,0,0));
	left_panel.add(left_button);
	left_down_button.setBackground(aside);
	left_down_button.setContentAreaFilled(false);
	left_down_button.addActionListener(this);
	left_down_button.setBorderPainted(false);
	left_down_button.setPreferredSize(new Dimension(0,0));
	left_down_button.setModel(down_left_button.getModel());
	left_panel.add(left_down_button);
	JPanel right_panel = new JPanel();
	right_panel.setLayout(new GridLayout(3,1,0,0));
	right_up_button.setBackground(aside);
	right_up_button.addActionListener(this);
	right_up_button.setContentAreaFilled(false);
	right_up_button.setBorderPainted(false);
	right_up_button.setMargin(new Insets(0,0,0,0));
	right_up_button.setPreferredSize(new Dimension(0,0));
	right_panel.add(right_up_button);
	right_button.setBackground(middle);
	right_button.addActionListener(this);
	right_button.setBorderPainted(false);
	right_button.setMargin(new Insets(0,0,0,0));
	right_panel.add(right_button);
	right_down_button.setBackground(aside);
	right_down_button.setContentAreaFilled(false);
	right_down_button.addActionListener(this);
	right_down_button.setBorderPainted(false);
	right_down_button.setPreferredSize(new Dimension(0,0));
	right_down_button.setModel(down_right_button.getModel());
	right_panel.add(right_down_button);
	// add canvas to a panel
	JPanel canvas_panel = new JPanel();
	canvas_panel.setLayout(new BorderLayout());
	canvas_panel.add(glcanvas, BorderLayout.CENTER);
	canvas_panel.setPreferredSize(new Dimension(0,0));
	JPanel main_panel = new JPanel();
	main_panel.setLayout(new BorderLayout(0,0));
	main_panel.add(canvas_panel, BorderLayout.CENTER);
	main_panel.add(north_panel, BorderLayout.NORTH);
	main_panel.add(south_panel, BorderLayout.SOUTH);
	main_panel.add(left_panel, BorderLayout.WEST);
	main_panel.add(right_panel, BorderLayout.EAST);
	main_panel.setBorder(BorderFactory.createRaisedBevelBorder());
	
	// add min & max boundary for visible area label
	JPanel label_panel = new JPanel();
	label_panel.setLayout(new BoxLayout(label_panel, BoxLayout.X_AXIS));
	JPanel left_label_panel = new JPanel();
	left_label_panel.setLayout(new GridLayout(2,1,1,1));
	JLabel max_text = new JLabel("NE :");
	max_text.setFont(max_text.getFont().deriveFont(Font.PLAIN));
	left_label_panel.add(max_text);
	JLabel min_text = new JLabel("SW :");
	min_text.setFont(min_text.getFont().deriveFont(Font.PLAIN));
	left_label_panel.add(min_text);
	JPanel right_label_panel = new JPanel();
	right_label_panel.setLayout(new GridLayout(2,1,1,1));
	ne_label.setFont(ne_label.getFont().deriveFont(Font.PLAIN));
	ne_label.setForeground(new Color(1.0f, 0.0f, 0.0f, 0.8f));
	right_label_panel.add(ne_label);
	sw_label.setFont(sw_label.getFont().deriveFont(Font.PLAIN));
	sw_label.setForeground(new Color(1.0f, 0.0f, 0.0f, 0.8f));
	right_label_panel.add(sw_label);
	label_panel.add(left_label_panel);
	label_panel.add(right_label_panel);
	label_panel.setBorder(BorderFactory.
			      createCompoundBorder(BorderFactory.createTitledBorder("Visible Area"),
						   BorderFactory.createEmptyBorder(2,2,2,2)));
	//
	positionMapPanel = new JPanel(new BorderLayout());
	positionMapPanel.add(main_panel, BorderLayout.CENTER);
	positionMapPanel.add(label_panel, BorderLayout.SOUTH);
	positionMapPanel.setBorder(BorderFactory.
				   createCompoundBorder(BorderFactory.createTitledBorder("Navigation Map"),
							BorderFactory.createEmptyBorder(2,2,2,2)));
	
    }
    
    /**
     * Returns the position map panel.
     */
    public JPanel getPanel() {
	return positionMapPanel;
    } 
    
    /**
     * Returns the reduced panel only consisting of the panning buttons.
     */
    public JPanel getReducedPanel() {
	JPanel reducedPanel = new JPanel(new GridLayout(3,3));
	reducedPanel.add(up_left_button);
	reducedPanel.add(up_button);
	reducedPanel.add(up_right_button);
	reducedPanel.add(left_button);
	reducedPanel.add(new JLabel());
	reducedPanel.add(right_button);
	reducedPanel.add(down_left_button);
	reducedPanel.add(down_button);
	reducedPanel.add(down_right_button);
	reducedPanel.setBorder(BorderFactory.
			       createCompoundBorder(BorderFactory.createTitledBorder("Panning"),
						    BorderFactory.createEmptyBorder(1,1,1,1)));
	return reducedPanel;
    }
    
    /**
     * Initialization of the display window.
     *
     * @param gld needed when opengl is used.
     */
    public void init(GLAutoDrawable gld) {
	GL gl = gld.getGL();
	
	// set the background color
	float b_red = 0.1f;
	float b_green = 0.5f;
	float b_blue = 0.9f;
	gl.glClearColor(b_red, b_green, b_blue, 0.5f);
	
	// enable blending
	gl.glEnable(GL.GL_BLEND);
	gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

	// set actual matrix
	gl.glMatrixMode(GL.GL_PROJECTION);
	gl.glLoadIdentity();
	
	// initialize bounding box
	glu.gluOrtho2D(pbox.getXmin(), pbox.getXmax(), pbox.getYmin(), pbox.getYmax());
	
	// initialize callback object for the tesselation
	adapter = new TessellatorAdapter(gl, glu);
	
	// create display lists
	buildLists(gld);
	
	// update display lists for the region
	updateRegionList(gl);
	updateTesselatedList(gl, glu);
    }
    
    /**
     * Draw elements in the window.
     *
     * @param gld needed when opengl is used.
     */
    public void display(GLAutoDrawable gld) {
	GL gl = gld.getGL();
	
	// update orthographics view bounds
	gl.glMatrixMode(GL.GL_PROJECTION);
	gl.glLoadIdentity();
	glu.gluOrtho2D(box.getXmin(), box.getXmax(), box.getYmin(), box.getYmax());
	
	// update the region
	if (update_region) {
	    updateRegionList(gl);
	    updateTesselatedList(gl, glu);
	    update_region = false;
	}
	
	// draw the map
	drawGraph(gl);
    }
        
    /**
     * Update bounds for the part of region visible in the main map.
     *
     * @param bbox bounding box of the visible region in the main map.
     */
    public void update(BoundingBox bbox) {
	pbox = (BoundingBox)bbox.clone();
	// update map
	double[] lon_lat1 = toLonLat(pbox.getXmin(), pbox.getYmin());
	double[] lon_lat2 = toLonLat(pbox.getXmax(), pbox.getYmax());
	// geodetic coordinates
	if (Configuration.getCoordinateSystem() == Configuration.GEODETIC) {
	    sw_label.setText(dec_format.format(lon_lat1[1])+degree_symbol+", "+dec_format.format(lon_lat1[0])+degree_symbol);
	    ne_label.setText(dec_format.format(lon_lat2[1])+degree_symbol+", "+dec_format.format(lon_lat2[0])+degree_symbol);
	}
	// MGRS coordinates
	else if (Configuration.getCoordinateSystem() == Configuration.MGRS) {
	    String mgrs1 = MGRSConversion.convertGeodeticToMGRS(Math.toRadians(lon_lat1[0]), 
								Math.toRadians(lon_lat1[1]), 5);
	    String mgrs2 = MGRSConversion.convertGeodeticToMGRS(Math.toRadians(lon_lat2[0]), 
								Math.toRadians(lon_lat2[1]), 5);
	    sw_label.setText(mgrs1);
	    ne_label.setText(mgrs2);
	}
	update();
    }
    
    /**
     * Update orthographic view bounds such that the displayed region is 
     * not disturbed.
     *
     * @param drawable needed when opengl is used.
     * @param x left screen coordinate of the display window.
     * @param y upper screen coordinate of the display window.
     * @param width width of the display window.
     * @param height height of the display window.
     */
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
	// update window size
	view_x = x;
	view_y = y;
	view_width = width;
	view_height = height;
	
	// get new orthographic view bounds such that aspect ratio is
	// equal to display window's
	if (width > 0 && height > 0) {
	    box = updateOrthographicBounds(width, height);
	}
    }
    
    /**
     * Not implemented.
     */
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, 
			       boolean deviceChanged) {
    }
    
    /**
     * Updates the otrhographic bounds of the display area. This is needed to avoid the 
     * distortion of the displayed region.
     *
     * @param width width of the displayed area.
     * @param height height of the displayed area.
     *
     * @return the orthographic bounds of the display area.
     */
    private BoundingBox updateOrthographicBounds(int width, int height) {
	BoundingBox regionBox = region.getProjectedBounds();
	double dx = Math.abs(regionBox.getXmax()-regionBox.getXmin());
	double dy = Math.abs(regionBox.getYmax()-regionBox.getYmin());
	double xmin = regionBox.getXmin()-dx/10;
	double ymin = regionBox.getYmin()-dy/10;
	double xmax = regionBox.getXmax()+dx/10;
	double ymax = regionBox.getYmax()+dy/10;
	double x_ratio = (xmax-xmin)/width;
	double y_ratio = (ymax-ymin)/height;
	if (x_ratio < y_ratio) {
	    dx = (ymax-ymin)*width/height;
	    xmin = (xmin+xmax)/2-dx/2;
	    xmax = (xmin+xmax)/2+dx/2;
	}
	else {
	    dy = (xmax-xmin)*height/width;
	    ymin = (ymin+ymax)/2-dy/2;
	    ymax = (ymin+ymax)/2+dy/2;
	    }
	// get the bounding box
	return new BoundingBox(xmin, ymin, xmax, ymax, stratmap.getProjection());
    }
    
    /**
     * Updates the map when the region has been changed.
     */
    public void eventOccured(StratmasEvent se) {
	// region updated
	if (se.isRegionUpdated()) {
	    // projected bounds of the updated region
	    box = region.getProjectedBounds();
	    
	    // adapt the bounding box
	    double dx = Math.abs(box.getXmax()-box.getXmin());
	    double dy = Math.abs(box.getYmax()-box.getYmin());
	    double xmin = box.getXmin()-dx/10;
	    double ymin = box.getYmin()-dy/10;
	    double xmax = box.getXmax()+dx/10;
	    double ymax = box.getYmax()+dy/10;
	    box = new BoundingBox(xmin, ymin, xmax, ymax, stratmap.getProjection());
	    
	    // update panned boundary box
	    pbox = (BoundingBox)box.clone();
	    
	    // get new orthographic view bounds such that aspect ratio is
	    // equal to display window's
	    if (view_width > 0 && view_height > 0) {
		box = updateOrthographicBounds(view_width, view_height);		
	    }
	    // update the region
	    update_region = true;
	    update();
	}
    }
    
    /**
     * Action is fired when pressing panning button. Both the current window
     * display area and the main map display area is updated.
     *
     * @param e action event occured by pressing one of the buttons.
     */
    public void actionPerformed(ActionEvent e) {
	Object source = e.getSource();
	// treat buttons
	double delx = (pbox.getXmax()-pbox.getXmin())/10;
	double dely = (pbox.getYmax()-pbox.getYmin())/10;
	double xc = (pbox.getXmax()+pbox.getXmin())/2;
	double yc = (pbox.getYmax()+pbox.getYmin())/2;
	if (source.equals(up_button)) {
	    yc = (yc+dely > box.getYmax())? box.getYmax() : yc+dely;
	}
	else if (source.equals(down_button)) {
	    yc = (yc-dely < box.getYmin())? box.getYmin() : yc-dely;
	}
	else if (source.equals(left_button)) {
	    xc = (xc-delx < box.getXmin())? box.getXmin() : xc-delx;
	}
	else if (source.equals(right_button)) {
	    xc = (xc+delx > box.getXmax())? box.getXmax() : xc+delx;
	}
	else if (source.equals(up_left_button) || source.equals(left_up_button)) {
	    xc = (xc-delx < box.getXmin())? box.getXmin() : xc-delx;
	    yc = (yc+dely > box.getYmax())? box.getYmax() : yc+dely;
	}
	else if (source.equals(up_right_button) || source.equals(right_up_button)) {
	    xc = (xc+delx > box.getXmax())? box.getXmax() : xc+delx;
	    yc = (yc+dely > box.getYmax())? box.getYmax() : yc+dely;
	}
	else if (source.equals(down_left_button) || source.equals(left_down_button)) {
	    xc = (xc-delx < box.getXmin())? box.getXmin() : xc-delx;
	    yc = (yc-dely < box.getYmin())? box.getYmin() : yc-dely;
	}
	else if (source.equals(down_right_button) || source.equals(right_down_button)) {
	    xc = (xc+delx > box.getXmax())? box.getXmax() : xc+delx;
	    yc = (yc-dely < box.getYmin())? box.getYmin() : yc-dely;
	}
	drawer.setXYCenter(xc, yc);
    }

    /**
     * Convert window coordinates to projected x and y coordinates.
     *
     * @param x x window coordinate.
     * @param y y window coordinate.
     *
     * @return [x, y] projected coordinates with the actual projection.
     */
    public double[] convertToProjCoords(int x, int y) {
	// initialize the output values
	double[] xy = {0, 0};
	// projected coordinates
	xy[0] = box.getXmin() + (x-view_x)*(box.getXmax()-box.getXmin())/view_width;
	xy[1] = box.getYmin() + (view_height-y+view_y)*(box.getYmax()-box.getYmin())/view_height;
	//
	return xy;
    }
    
    /**
     * Convert projected coordinates to (lon, lat).
     *
     * @param x x projected coordinate.
     * @param y y projected coordinate.
     *
     * @return [lon, lat] values.
     */
    private double[] toLonLat(double x, double y) {
	return stratmap.getProjection().projToLonLat(x,y);
    }
    
    /**
     * Creates empty display lists for the region shapes.
     */
    private void buildLists(GLAutoDrawable drawable) {
	GL gl = drawable.getGL();
	// building the list
	reg = gl.glGenLists(1);	
	// compiled display list (contains shapes of the regions)
	gl.glNewList(reg, GL.GL_COMPILE);
	// ends display list
	gl.glEndList();
	// building the list
	tess_reg = gl.glGenLists(1);	
	// compiled display list (contains tesselated regions)
	gl.glNewList(tess_reg, GL.GL_COMPILE);
	// ends display list
	gl.glEndList();
    }
        
    /**
     *  Updates display lists for the region shapes.
     */
    private void updateRegionList(GL gl) {
	// get shapes
	Vector shapes = (Vector)region.getShapes();
	Vector simple_shapes = new Vector();
	if (!shapes.isEmpty()) {
	    simple_shapes = ((Shape)shapes.get(0)).constructSimpleShapes(new Vector());
	    for (int i = 1; i < shapes.size(); i++) {
		simple_shapes = ((Shape)shapes.get(i)).constructSimpleShapes(simple_shapes);
	    }
	}
	// compiled display list (contains shapes of the regions)
	gl.glNewList(reg, GL.GL_COMPILE);
	// line color
	gl.glColor3f(0.1f, 0.1f, 0.1f);
	// width of the shape line
	gl.glLineWidth(1.0f);
	// draw geographical region
	gl.glBegin(GL.GL_LINES);
	Projection proj = stratmap.getProjection();
	for (int i = 0; i < simple_shapes.size(); i++) {	
	    SimpleShape ss = (SimpleShape)simple_shapes.get(i);
	    Polygon pol = (Polygon)ss.getPolygon(1.0);
	    // for each polygonial
	    for (Enumeration e = pol.getCurves(); e.hasMoreElements();) {
		Line line = (Line) e.nextElement();
		gl.glVertex2dv(proj.projToXY(line.getStartPoint()), 0);
		gl.glVertex2dv(proj.projToXY(line.getEndPoint()), 0);
	    }
	}
	gl.glEnd();
	// ends display list
	gl.glEndList();	
    }

    /**
     * TessellatorAdapter - used to describe the callback methods used for the tesselation 
     * of the polygons.
     */
    class TessellatorAdapter extends GLUtessellatorCallbackAdapter {
	/**
	 * Interface to OpenGL.
	 */
	GL gl;
	/**
	 * Provides access to the OpenGL utility library routines.
	 */
	GLU glu;
	/**
	 * Creates the adapter.
	 */
	public TessellatorAdapter(GL gl, GLU glu) {
	    this.gl = gl;
	    this.glu = glu;
	}
	/**
	 * The vertex callback method is invoked between the begin and end callback methods
	 */
	public void vertex(Object data) {
	    double[] p = (double[]) data;
	    gl.glColor3f(0.1f, 0.8f, 0.1f);
	    gl.glVertex2d(p[0], p[1]);
	}
	/**
	 * The begin callback method is invoked like glBegin to indicate the start of a 
	 * (triangle) primitive.
	 */
	public void begin(int type) {
	    gl.glBegin(type);
	}
	/**
	 * The end callback serves the same purpose as glEnd.
	 */
	public void end() {
	    gl.glEnd();
	}
	/**
	 *
	 */
	public void combine(double[] coords, Object[] data, float[] weight, Object[] outData) {
	    float[] col = {0.1f, 0.8f, 0.1f, 1.0f};
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
	/**
	 *  The error callback method is called when an error is encountered
	 */
	public void error(int errnum) {
	    String estring;
	    estring = glu.gluErrorString(errnum);
	    System.out.println("Tessellation Error: " + estring);
	    throw new RuntimeException();
	}
    }

    /**
     * Updates the display list consisting of the tesselated shapes the displayed
     * region contains.
     *
     * @param GL interface to OpenGL.
     * @param GLU interface to OpenGL utility library routines.
     */
    private void updateTesselatedList(GL gl, GLU glu) {
	// get shapes
	Vector shapes = (Vector)region.getShapes();
	Vector simple_shapes = new Vector();
	if (!shapes.isEmpty()) {
	    simple_shapes = ((Shape)shapes.get(0)).constructSimpleShapes(new Vector());
	    for (int i = 1; i < shapes.size(); i++) {
		simple_shapes = ((Shape)shapes.get(i)).constructSimpleShapes(simple_shapes);
	    }
	}
	
	// create new tessellator
	GLUtessellator tess = glu.gluNewTess();
	glu.gluTessProperty(tess,GLU.GLU_TESS_BOUNDARY_ONLY,GLU.GLU_FALSE);
	glu.gluTessProperty(tess,GLU.GLU_TESS_WINDING_RULE,GLU.GLU_TESS_WINDING_ODD);
	
	// define callback functions
	glu.gluTessCallback(tess, GLU.GLU_TESS_VERTEX, adapter);
	glu.gluTessCallback(tess, GLU.GLU_TESS_BEGIN, adapter);
	glu.gluTessCallback(tess, GLU.GLU_TESS_END, adapter);
	glu.gluTessCallback(tess, GLU.GLU_TESS_ERROR, adapter);
	glu.gluTessCallback(tess, GLU.GLU_TESS_COMBINE, adapter);

	// create display list for all shapes
	gl.glNewList(tess_reg, GL.GL_COMPILE);
	Projection proj = stratmap.getProjection();
	for (int i = 0; i < simple_shapes.size(); i++) {
	    SimpleShape ssh = (SimpleShape)simple_shapes.get(i);
	    if (!ssh.isHole()) {
		Polygon pol = (Polygon)ssh.getPolygon(1.0);
		// start tesselation
		glu.gluBeginPolygon(tess);
		// for each polygonial
		for (Enumeration e = pol.getCurves(); e.hasMoreElements();) {
		    Line line = (Line) e.nextElement();
		    double[] xy = proj.projToXY(line.getStartPoint());
		    double[] vv = {xy[0], xy[1], 0};
		    glu.gluTessVertex(tess, vv, 0, vv);
		}
		//
		glu.gluNextContour(tess,GLU.GLU_UNKNOWN);
		glu.gluEndPolygon(tess);
	    }
	}
	// ends display list
	gl.glEndList();
    }
    
    /**
     * All elements shown in the display window are drawn here.
     */
    private void drawGraph(GL gl) {
	// clear the window
	gl.glClear(GL.GL_COLOR_BUFFER_BIT);

	// draw tesselated polygons
	gl.glCallList(tess_reg);
	
	// draw region shapes
	gl.glCallList(reg);

	// bounding box
	// line color
	gl.glColor3f(0.7f, 0.7f, 0.7f);
	// width of the shape line
	gl.glLineWidth(3.0f);
	// draw bounding box
	gl.glBegin(GL.GL_LINE_LOOP);
	gl.glVertex2d(box.getXmin(), box.getYmin());
	gl.glVertex2d(box.getXmax(), box.getYmin());
	gl.glVertex2d(box.getXmax(), box.getYmax());
	gl.glVertex2d(box.getXmin(), box.getYmax());
	gl.glEnd();
       
	// visual area
	gl.glColor4f(0.9f, 0.9f, 0.9f, 0.5f);
	// width of the shape line
	gl.glLineWidth(2.0f);
	// draw bounding box
	gl.glBegin(GL.GL_QUADS);
	gl.glVertex2d(pbox.getXmin(), pbox.getYmin());
	gl.glVertex2d(pbox.getXmax(), pbox.getYmin());
	gl.glVertex2d(pbox.getXmax(), pbox.getYmax());
	gl.glVertex2d(pbox.getXmin(), pbox.getYmax());
	gl.glEnd();
	
	// bounding box of the visual area
	gl.glColor4f(1.0f, 0.0f, 0.0f, 0.5f);
	// width of the shape line
	gl.glLineWidth(1.0f);
	// draw bounding box
	gl.glBegin(GL.GL_LINE_LOOP);
	gl.glVertex2d(pbox.getXmin(), pbox.getYmin());
	gl.glVertex2d(pbox.getXmax(), pbox.getYmin());
	gl.glVertex2d(pbox.getXmax(), pbox.getYmax());
	gl.glVertex2d(pbox.getXmin(), pbox.getYmax());
	gl.glEnd();
    }

    /**
     * Not implemented.
     */
    public void mouseClicked(MouseEvent e) {
    }
    
    /**
     * Not implemented. 
     */
    public void mouseEntered(MouseEvent e) {}
    
    /**
     * Not implemented. 
     */
    public void mouseExited(MouseEvent e) {}
    
    /**
     * Starts the dragging action of the rectangular area visible in the main map. 
     */
    public void mousePressed(MouseEvent e) {
	int x = (int)e.getX();
	int y = (int)e.getY();
	// convert to projected coordinates
	double[] xy = convertToProjCoords(x, y);
	// convert to lat/lon
	double[] lon_lat = toLonLat(xy[0], xy[1]);
	// get the box boundaries
	double[] lon_lat1 = toLonLat(pbox.getXmin(), pbox.getYmin());
	double[] lon_lat2 = toLonLat(pbox.getXmax(), pbox.getYmax());
	if (lon_lat[0] >= lon_lat1[0] && lon_lat[0] <= lon_lat2[0] &&
	    lon_lat[1] >= lon_lat1[1] && lon_lat[1] <= lon_lat2[1]) {
	    drag_started = true;
	}
    }
    
    
    /**
     * Ends the dragging action. 
     */
    public void mouseReleased(MouseEvent e) {
	if (drag_started) {
	    drag_started = false;
	}
    }

    /**
     * Moves the rectangular area visible in the main map to the location currently
     * pointed by the mouse.
     */
    public void mouseDragged(MouseEvent e) {
	if (drag_started) {
	    int x = (int)e.getX();
	    int y = (int)e.getY();
	    // convert to projected coordinates
	    double[] xy = convertToProjCoords(x, y);
	    double xx = (xy[0] > box.getXmax())? box.getXmax() : ((xy[0] < box.getXmin())? box.getXmin() : xy[0]);
	    double yy = (xy[1] > box.getYmax())? box.getYmax() : ((xy[1] < box.getYmin())? box.getYmin() : xy[1]);
	    // update main map
	    drawer.setXYCenter(xx, yy);
	}
    }
    
    /**
     * Not implemented. 
     */
    public void mouseMoved(MouseEvent e) {
    }
    
    /**
     * Redraw the map.
     */
    public void update() {
	glcanvas.validate();
	glcanvas.repaint();
    }
    
}
