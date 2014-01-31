package StratmasClient.map;

import java.text.DecimalFormat;
import java.nio.IntBuffer;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.net.URL;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JToolBar;
import javax.swing.JTextField;
import javax.swing.JSlider;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.ButtonGroup;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.GLAutoDrawable;
import com.sun.opengl.util.BufferUtil;

import StratmasClient.object.type.TypeFactory;
import StratmasClient.object.StratmasObject;
import StratmasClient.StratmasDialog;
import StratmasClient.object.Line;
import StratmasClient.object.Point;
import StratmasClient.object.Circle;
import StratmasClient.object.Polygon;
import StratmasClient.object.Shape;
import StratmasClient.object.StratmasObjectFactory;
import StratmasClient.object.SimpleShape;
import StratmasClient.BoundingBox;
import StratmasClient.map.adapter.MapElementAdapter;
import StratmasClient.map.adapter.MapDrawableAdapter;
import StratmasClient.map.adapter.MapActivityAdapter;
import StratmasClient.map.adapter.MapLineAdapter;
import StratmasClient.map.adapter.MapPointAdapter;
import StratmasClient.map.adapter.MapShapeAdapter;

/**
 * This class is used for creating new area for military units, teams, agencies and activities.
 * The created area can be polygonial or circular. The existing area can be moved to another 
 * location. If the area is circular its radius can be changed. If it is polygonial the vertices
 * can be moved and the new ones can be inserted. 
 *
 * @version 1.0
 * @author Amir Filipovic, Daniel Ahlin
 */
public class AreaCreationDrawer extends BasicMapDrawer
{
    /**
     * Last horizontal mouse coordinate for last renderselction.
     */
    protected int renderSelectionMouseX = 0;
    /**
     * Last vertical mouse coordinate for last renderselction.
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
     * The list of the overlapping points.
     */ 
    private Hashtable overlappingPoints = new Hashtable();
    /**
     * Idicator for the undefined mode.  
     */
    public final static int UNDEFINED = 0;
    /**
     * Idicator for the creation of polygonial shape.  
     */
    public final static int POLYGONIAL = 1;
    /**
     * Idicator for the creation of circular shape.  
     */
    public final static int CIRCLE = 2;
    /**
     * Idicator for the moving of the shape.  
     */
    public final static int MOVE = 3;
    /**
     * Idicator for the moving of the point.  
     */
    public final static int MOVEPOINT = 4;
    /**
     * Idicator for the inserting the point to the polygonial shape.  
     */
    public final static int INSERTPOINT = 5;
    /**
     * Current creator mode.
     */
    private int creatorMode;
    /**
     * The actual circle.
     */
    private Circle circle;
    /**
     * The first inserted point in the polygonial.
     */
    private Point firstPoint;
    /**
     * The last inserted point in the polygonial.
     */
    private Point lastPoint;
    /**
     * Indicates if the first and the last added points are connected with a line.
     */
    private boolean polygonCompleted = false;
    /**
     * StratmasObject for which the area is created.
     */
    private StratmasObject sComp;
    /**
     * Path to the images.
     */
    private String path = "images/";
    /**
     * The tool bar.
     */
    private JToolBar tools;
    /**
     * Circle button.
     */
    private JToggleButton circleButton;
    /**
     * Polygon button.
     */
    private JToggleButton polygonButton;
    /**
     * New point button.
     */
    private JToggleButton newPointButton;
    /**
     * Move point button.
     */
    private JToggleButton movePointButton;
    /**
     * Move shape button.
     */
    private JToggleButton moveShapeButton;
    /**
     * Used to turn off the selected button.
     */
    private JToggleButton noneButton = new JToggleButton();
    /**
     * The panning button.
     */
    private JToggleButton panButton;
    /**
     * Delete button.
     */
    private JButton deleteButton;
    /**
     * Accept button.
     */
    private JButton acceptButton;
    /**
     * Cancel button.
     */
    private JButton cancelButton;
    /**
     * Information label.
     */
    private JLabel infoLabel;
    
    /**
     * Creates new AreaCreationDrawer.
     *
     * @param basicMap the map container.
     * @param region   the region displayed on the map.
     * @param sComp    the object for which a new area is created.
     */
    public AreaCreationDrawer(BasicMap basicMap, Region region, StratmasObject sComp) {
	super(basicMap, region);
	// get the object
	this.sComp = sComp;
	// set zoom & scale
	setZoomAndScale(new ZoomAndScale(this, JSlider.HORIZONTAL));
	// add the element
	addMapDrawableAdapter(sComp);
	((MapElementAdapter)mapDrawableAdapters.get(sComp)).setSymbolOpacity(1.0);
	((MapElementAdapter)mapDrawableAdapters.get(sComp)).setSymbolScale(1.0);
	((MapElementAdapter)mapDrawableAdapters.get(sComp)).setLocationOpacity(1.0);
	((MapElementAdapter)mapDrawableAdapters.get(sComp)).setDrawLocation(true);
	((MapElementAdapter)mapDrawableAdapters.get(sComp)).setDrawLocationOutline(true);
	((MapElementAdapter)mapDrawableAdapters.get(sComp)).setInvariantSymbolSize(true);
	((MapElementAdapter)mapDrawableAdapters.get(sComp)).invalidateAllLists();
	// show gui
	createAndShowGUI(new String("Area definition for "+sComp.getType().getName()+" "+sComp.getIdentifier()));
    }
    
    /**
     * Drawing elements on the map. Part of GLEventListener interface.
     *
     * @param gld needed for OpenGL.
     */
    public void display(GLAutoDrawable gld) {
	super.display(gld);

	updateRenderSelection(gld);

	// draw the map
	drawGraph(gld);
    }

    /**
     * Causes different actions by clicking the mouse. These actions depend of the clicked 
     * button (left or right), if the click is simple or double.
     * Part of MouseListener interface.
     *
     * @param e event created by clicking the mouse.
     */
    public void mouseClicked(MouseEvent e)
    {
	// get window coordinates
	int x = (int)e.getX();
	int y = (int)e.getY(); 

	setRenderSelectionArea(x, y);  
	if (getCreatorMode() == AreaCreationDrawer.INSERTPOINT) {
	    setRenderSelectionArea(x, y, 5, 5);   
	}
	
	// convert the curent position to lon/lat
	MapPoint p = convertToLonLat(x, y);

	// get projected coordinates	
	Projection proj = basicMap.getProjection();
	double xx = p.getProjectedPoint(proj).getX();
	double yy = p.getProjectedPoint(proj).getY();
	
	// left mouse button
	if (e.getButton() == MouseEvent.BUTTON1){
	    // add new point to the polygon
	    if (getCreatorMode() == POLYGONIAL && !isPolygonCompleted()) {
		addPoint(StratmasObjectFactory.createPoint("p1", p.getLat(), p.getLon()));
	    }
	    // update radius of the circle

	    else if (getCreatorMode() == AreaCreationDrawer.CIRCLE) {
		updateRadius(StratmasObjectFactory.createPoint("center", p.getLat(), p.getLon()));
	    }
	    // nove the area
	    else if (getCreatorMode() == AreaCreationDrawer.MOVE) {
		moveShape(p.getLon(), p.getLat());
	    }
	    // insert new point into the polygon
	    else if (getCreatorMode() == AreaCreationDrawer.INSERTPOINT) {
		Vector mlAdapters = mapDrawableAdaptersUnderCursor(MapLineAdapter.class);
		if (!mlAdapters.isEmpty()) {
		    MapLineAdapter mlAdapter = (MapLineAdapter)mlAdapters.firstElement();
		    Line line = (Line)mlAdapter.getObject();
		    insertPoint(StratmasObjectFactory.createPoint("p1", p.getLat(), p.getLon()), line);
		}
	    }
	}
	// right mouse button
	else if (e.getButton() == MouseEvent.BUTTON3){
	    // complete the polygon by adding the last line
	    if (getCreatorMode() == AreaCreationDrawer.POLYGONIAL) {
		addLastLine();
	    }
	}
	//
	update();
    }
    
    /**
     * Used to move the points and shapes on the map and also to change the radius of the drawn
     * circle while creating a new area. Part of MouseMotionListener interface.
     *
     * @param e the generated event.
     */
    public void mouseDragged(MouseEvent e) {
	// get window coordinates
	int x = (int)e.getX();
	int y = (int)e.getY();

	setRenderSelectionArea(x, y);

	// convert to lon/lat
	MapPoint p = convertToLonLat(x, y);
	
	// move the map
	if (panButton.isSelected()) {
	    double dx = p.getProjectedPoint(getProjection()).getX() - current_pos.getProjectedPoint(getProjection()).getX();
	    double dy = p.getProjectedPoint(getProjection()).getY() - current_pos.getProjectedPoint(getProjection()).getY();
	    setXYCenter(ort_xc - dx, ort_yc - dy);
	    
	}
	else {
	    // move the dragged shape
	    if (getCreatorMode() == AreaCreationDrawer.MOVE) {
		moveShape(p.getLon(), p.getLat());
	    }
	    // move the dragged point. In fact two Point objects are moved here. Both Point objects
	    // represents one point which connects two lines. 
	    else if (getCreatorMode() == AreaCreationDrawer.MOVEPOINT) {
		Vector mpAdapters = mapDrawableAdaptersUnderCursor(MapPointAdapter.class);
		if (!mpAdapters.isEmpty()) {
		    // get the first Point object
		    MapPointAdapter mpAdapter = (MapPointAdapter)mpAdapters.firstElement();
		    Point point = (Point)mpAdapter.getObject();
		    // find another Point objects which represents the same point as the first one
		    Point twinP = (Point)overlappingPoints.get(point);
		    if (twinP == null) {
			//twinP = findMeetingPoint(point);
		    }
		    if (twinP != null) {
			point.setLat(p.getLat(), this);
			point.setLon(p.getLon(), this);
			twinP.setLat(p.getLat(), this);
			twinP.setLon(p.getLon(), this); 
		    }
		}
	    }
	    // change the radius of the circle
	    else if (getCreatorMode() == AreaCreationDrawer.CIRCLE) {
		updateRadius(StratmasObjectFactory.createPoint("center", p.getLat(), p.getLon()));
		// display the radius
		DecimalFormat resultFormat = new DecimalFormat("0.00");
		String radius = resultFormat.format(circle.getRadius());
		info_field.setText("Radius : "+radius+" m");
	    }
	    
	    if (getCreatorMode() == AreaCreationDrawer.CIRCLE) {
		// display the radius
		DecimalFormat resultFormat = new DecimalFormat("0.00");
		String radius = resultFormat.format(circle.getRadius());
		info_field.setText("Radius : "+radius+" m");
	    }
	    else {
		// convert the current position to lon/lat
		current_pos = convertToLonLat(x, y);
		// display current position
		displayCurrentPosition(current_pos);
	    }
	}
	// necessary for multi-screen enviroment
	mouse_on = (x >= view_x && x <= view_x+view_width && y >= view_y && y <= view_y+view_height)? true : false;
	//
	update();	
    }
    
    /**
     * Upadates the curent position on the map. Part of MouseMotionListener interface.
     *
     * @param e event created by changing the position of the mouse cursor on the map.
     */
    public void mouseMoved(MouseEvent e) {
	// get window coordinates
	int x = (int) e.getX();
	int y = (int) e.getY();
	
	setRenderSelectionArea(x, y); 
	if (getCreatorMode() == AreaCreationDrawer.INSERTPOINT) {
	    setRenderSelectionArea(x, y, 5, 5);   
	} 
	
	// necessary for multi-screen enviroment
	mouse_on = (x >= view_x && x <= view_x+view_width && y >= view_y && y <= view_y+view_height)? true : false;
	
	// convert the current position to lon/lat
	current_pos = convertToLonLat(x, y);
	
	// display current position
	displayCurrentPosition(current_pos);
	
	// display the region under the mouse cursor
	Vector adVec = mapDrawableAdaptersUnderCursor(MapShapeAdapter.class);
	if (!adVec.isEmpty()) {
	    if (adVec.size() == 1) {
		displayPointedRegion(((Shape)((MapShapeAdapter)adVec.firstElement()).getObject()).getIdentifier()); 
	    }
	    else {
		int tmpIndex = 0;
		while (tmpIndex < adVec.size() && 
		       !(((MapShapeAdapter)adVec.get(tmpIndex)).getObject() instanceof SimpleShape)) {
		    tmpIndex++;
		}
		if (tmpIndex < adVec.size()) {
		    displayPointedRegion(((Shape)((MapShapeAdapter)adVec.get(tmpIndex)).getObject()).getIdentifier());  
		}
	    }
	}
	else {
	    displayPointedRegion("");
	}
	
	// redraw
	update();
    }
    
    /**
     * Create the GUI and show it. 
     */
    public void createAndShowGUI(String title) {
	// create and set up the window
        frame = new JFrame(title);
	final AreaCreationDrawer self = this;
	frame.addWindowListener( new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    self.exitCreator();
		}
	    });
	
	// necessary when heavyweight and lightweight components intersect
	JPopupMenu.setDefaultLightWeightPopupEnabled(false);
	
	// frame size (test adapted for now on)
	Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
	frame_width = screen_size.width/3;
	frame_height = screen_size.height/2;
	frame.setSize(frame_width, frame_height); 
        frame.setLocation((screen_size.width-frame_width) >> 1, 
			  (screen_size.height-frame_height) >> 1
			  );
	
	// create text field for the coordinate under the mouse cursor 
	info_field = new JTextField();
	info_field.setEditable(false);
	info_field.setBackground(this.getBackground());
	
	// create text field for the region under the mouse cursor 
	regionTextField = new JTextField();
	regionTextField.setEditable(false);
	regionTextField.setBackground(this.getBackground());
	
	JPanel textFieldPanel = new JPanel(new GridLayout(1, 2));
	textFieldPanel.add(info_field);
	textFieldPanel.add(regionTextField);
	
	// add the canvas and the text field to the panel
	setLayout(new BorderLayout());
	add(glc, BorderLayout.CENTER);
	add(textFieldPanel, BorderLayout.SOUTH);
	
	// compose the toolbar
	composeGUI();
	
	// add the panel to the farme
	frame.getContentPane().add(this, BorderLayout.CENTER);
	frame.getContentPane().add(tools, BorderLayout.NORTH);
	frame.setResizable(true);
	
	// thread safety recomendation
	final JFrame fframe = frame;
	SwingUtilities.invokeLater (new Runnable() 
	    {
		public void run() {
		    fframe.setVisible(true);
		}
	    });
    }
    
    /**
     *  Creates the toolbar nedded for creation of the user defined area.
     */
    public void composeGUI() {
	final AreaCreationDrawer self = this;
	final JToggleButton fButton = noneButton;
	// set the mode for the shape creation
	creatorMode = UNDEFINED;
	// create the tool bar
	JToolBar toolBar = new JToolBar();
	circleButton = new JToggleButton(new ImageIcon(AreaCreationDrawer.class.getResource(path+"circle.png")));
	circleButton.setToolTipText("Create circular area");
	circleButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    if (self.getCreatorMode() != AreaCreationDrawer.CIRCLE) {
			self.clearAll();
			self.setCreatorMode(AreaCreationDrawer.CIRCLE);
			self.initCircle();
		    }
		    else {
			fButton.doClick();
		    }
		}
	    });
	toolBar.add(circleButton);
	polygonButton = new JToggleButton(new ImageIcon(AreaCreationDrawer.class.getResource(path+"polygon.png")));
	polygonButton.setToolTipText("Create polygonial area");
	polygonButton.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent event) {
		    if (self.getCreatorMode() != AreaCreationDrawer.POLYGONIAL) {
			self.clearAll();
			self.setCreatorMode(AreaCreationDrawer.POLYGONIAL);
		    }
		    else {
			fButton.doClick();
		    }
		}
	    });
	toolBar.add(polygonButton);
	newPointButton = new JToggleButton(new ImageIcon(AreaCreationDrawer.class.getResource(path+"newpoint.png")));
	newPointButton.setToolTipText("Add new point to the area");
	newPointButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    if (self.getCreatorMode() != AreaCreationDrawer.INSERTPOINT) {
			// check if the polygonial is not completed
			if (self.getCreatorMode() == AreaCreationDrawer.POLYGONIAL && !self.isPolygonCompleted()) {
			    self.addLastLine();
			}
			self.setCreatorMode(AreaCreationDrawer.INSERTPOINT);
			self.setFirstPoint(null);
			self.setLastPoint(null);
			self.setCircle(null);
			// use actual area if no points are chosen
			Vector pointAdapters = self.mapDrawableAdapters(MapPointAdapter.class);
			if (pointAdapters.isEmpty()) {
			    self.initPolygon(); 
			}
		    }
		    else {
			fButton.doClick();
		    }
		}
	    });
	toolBar.add(newPointButton);
	movePointButton = new JToggleButton(new ImageIcon(AreaCreationDrawer.class.getResource(path+"movepoint.png")));
	movePointButton.setToolTipText("Change the area by moving the desired point");
	movePointButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    if (self.getCreatorMode() != AreaCreationDrawer.MOVEPOINT) {
			// check if the polygonial is not completed
			if (self.getCreatorMode() == AreaCreationDrawer.POLYGONIAL && !self.isPolygonCompleted()) {
			    self.addLastLine();
			}
			self.setCreatorMode(AreaCreationDrawer.MOVEPOINT);
			self.setFirstPoint(null);
			self.setLastPoint(null);
			self.setCircle(null);
			// use actual area if no points are chosen
			Vector pointAdapters = self.mapDrawableAdapters(MapPointAdapter.class);
			if (pointAdapters.isEmpty()) { 
			    self.initPolygon(); 
			}
		    }
		    else {
			fButton.doClick();
		    }
		}
	    });
	toolBar.add(movePointButton);
	moveShapeButton = new JToggleButton(new ImageIcon(AreaCreationDrawer.class.getResource(path+"hand.png")));
	moveShapeButton.setToolTipText("Move the area");
	moveShapeButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    if (self.getCreatorMode() != AreaCreationDrawer.MOVE) {
			// check if the polygonial is not completed
			if (self.getCreatorMode() == AreaCreationDrawer.POLYGONIAL && !self.isPolygonCompleted()) {
			    self.addLastLine();
			}
			self.setCreatorMode(AreaCreationDrawer.MOVE);
			self.setFirstPoint(null);
			self.setLastPoint(null);
			// use actual area if no points are chosen
			Vector pointAdapters = self.mapDrawableAdapters(MapPointAdapter.class);
			if (self.getCircle() == null && pointAdapters.isEmpty()) {
			    // polygonial area
			    self.initPolygon();
			    // circular area
			    self.initCircle();
			}
		    }
		    else {
			fButton.doClick();
		    }
		}
	    });
	toolBar.add(moveShapeButton);
	noneButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    self.clearAll();
		    self.setCreatorMode(AreaCreationDrawer.UNDEFINED); 
		}
	    });
	// group the toggle buttons
	ButtonGroup group = new ButtonGroup();
	group.add(circleButton);
	group.add(polygonButton);
	group.add(newPointButton);
	group.add(movePointButton);
	group.add(moveShapeButton);
	group.add(noneButton);
	//
	toolBar.addSeparator();
	deleteButton = new JButton(new ImageIcon(AreaCreationDrawer.class.getResource(path+"Delete16.gif")));
	deleteButton.setToolTipText("Delete");
	deleteButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    if (self.getCreatorMode() == AreaCreationDrawer.POLYGONIAL) {
			self.clearAll();
			self.setCreatorMode(AreaCreationDrawer.POLYGONIAL);
		    }
		    else if (self.getCreatorMode() == AreaCreationDrawer.CIRCLE) {
			self.clearAll();
			self.setCreatorMode(AreaCreationDrawer.CIRCLE);
			self.initCircle();
		    }
		}
	    });
	toolBar.add(deleteButton);
	toolBar.addSeparator();
	cancelButton = new JButton(new ImageIcon(AreaCreationDrawer.class.getResource(path+"cancel.png")));
	cancelButton.setToolTipText("Cancel");
	cancelButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		  self.exitCreator();
		}
	    });
	toolBar.add(cancelButton);
	acceptButton = new JButton(new ImageIcon(AreaCreationDrawer.class.getResource(path+"ok.png")));
	acceptButton.setToolTipText("Accept");
	acceptButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    // the new area is added to the object
		    boolean accepted = self.updateElementWithShape(sComp);
		    if (accepted) {
			self.exitCreator();
		    } 
		}
	    });
	toolBar.add(acceptButton);
	toolBar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Area Creation"),
							     BorderFactory.createEmptyBorder(2,2,2,2)));
	// panning button
	JToolBar panBar = new JToolBar();
	panButton = new JToggleButton(new ImageIcon(AreaCreationDrawer.class.getResource(path+"pan.png")));
	panBar.addSeparator();
	panBar.add(panButton);
	panBar.addSeparator();
	panBar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Panning"),
							    BorderFactory.createEmptyBorder(2,2,2,2)));
	//
	tools = new JToolBar("Shape Creator");
	tools.add(toolBar);
	tools.addSeparator();
	tools.add(panBar);
	tools.addSeparator();
	tools.add(zoom_and_scale.getZoomingPanel());
    }
    
    /**
     * Updates the render selection array.
     *
     * @param gld the drawable.
     */
    protected void updateRenderSelection(GLAutoDrawable gld) {
	GL gl = gld.getGL();
	
	IntBuffer renderSelectionBuffer;
	int renderSelectionBufferAllocationSize = 2048;
	
	int hits = -1;

	do {
	    renderSelectionBuffer = BufferUtil.newIntBuffer(renderSelectionBufferAllocationSize);
	    gl.glSelectBuffer(renderSelectionBuffer.capacity(), renderSelectionBuffer);
	
	    // Enable render selection.
	    gl.glRenderMode(GL.GL_SELECT);
	    
	    // Init names.
	    gl.glInitNames();

	    // Sets the selection area.
	    gl.glMatrixMode(GL.GL_PROJECTION);
	    gl.glPushMatrix();
	    gl.glLoadIdentity();

	    glu.gluOrtho2D(renderSelectionX - renderSelectionDeltaX/2, 
			   renderSelectionX + renderSelectionDeltaX/2, 
			   renderSelectionY - renderSelectionDeltaY/2,
			   renderSelectionY + renderSelectionDeltaY/2);
	    
	    // Draw symbols.
	    updateDrawnMapDrawablesList();
	    gl.glCallLists(drawnMapDrawablesListBuf.capacity(), gl.GL_INT, drawnMapDrawablesListBuf);

	    // Restore view
	    gl.glMatrixMode(GL.GL_PROJECTION);
	    gl.glPopMatrix();
	    gl.glFlush();
	    
	    // End render selection mode.
	    hits = gld.getGL().glRenderMode(GL.GL_RENDER);
	    
	    if (hits < 0) {
		// To small selectionBuffer, try double size.
		renderSelectionBufferAllocationSize = 
		    renderSelectionBufferAllocationSize * 2;
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
     * @param deltaY tolerance of vertical component of center in
     * screen coordinates.
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
     * screen coordinates.
     */
    protected void setRenderSelectionArea(int x, int y) {
	setRenderSelectionArea(x, y, 1, 1);
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
     * Adds a new MapDrawableAdapter to this map.
     */
    protected MapDrawableAdapter addMapDrawableAdapter(StratmasObject mapDrawable) {
	MapDrawableAdapter drawableAdapter = MapDrawableAdapter.getMapDrawableAdapter(mapDrawable);
	int renderSelectionName = getNewRenderSelectionName(drawableAdapter.getNrOfRenderSelectionNames());
	drawableAdapter.setRenderSelectionName(renderSelectionName);
	synchronized (mapDrawableAdapters) {
	    mapDrawableAdapters.put(mapDrawable, drawableAdapter);
	}
	
	// special treatment for points
	if (drawableAdapter instanceof MapPointAdapter) {
	    ((MapPointAdapter)drawableAdapter).setInvariantSymbolSize(true);
	}
	
	renderSelectionNames.put(new Integer(renderSelectionName), drawableAdapter);
	drawableAdapter.addMapDrawableAdapterListener(this);
	synchronized (mapDrawableAdapterRecompilation) {
	    mapDrawableAdapterRecompilation.add(drawableAdapter);
	}
	
	addMapDrawableDisplayList(drawableAdapter.getDisplayList());
	setIsDrawnMapDrawablesListUpdated(false);
	
	return drawableAdapter;
    }
    
    /**
     * All elements shown in the scene are drawn here.
     *
     * @param gld needed for OpenGL.
     */
    protected void drawGraph(GLAutoDrawable gld) {
	GL gl = gld.getGL();
	// clear the window
	gl.glClear(GL.GL_COLOR_BUFFER_BIT);
	
	// draw graticules
	gl.glCallList(graticuleDisplayList);
 	
	// recompile changed elements
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
	gl.glCallLists(drawnMapDrawablesListBuf.capacity(), gl.GL_INT, drawnMapDrawablesListBuf);

	// definition of new area for the element
	gl.glMatrixMode(GL.GL_MODELVIEW);
	gl.glPushMatrix();
	// line color
	gl.glColor3f(1.0f, 0.0f, 0.0f);
	// width of the shape line
	gl.glLineWidth(2.0f);
	Projection proj = basicMap.getProjection();
	if (getCircle() != null) {
	    // draw circle
	    SimpleShape sShape = (SimpleShape)getCircle();
	    Polygon pol = sShape.getPolygon(1);
	    gl.glBegin(GL.GL_LINE_LOOP);
	    for (Enumeration e = pol.getCurves(); e.hasMoreElements();) {
		gl.glVertex2dv(proj.projToXY(((Line)e.nextElement()).getStartPoint()), 0);
	    }
	    gl.glEnd();	
	}
	else if (getCreatorMode() == AreaCreationDrawer.POLYGONIAL &&
		 getLastPoint() != null) {
	    // draw last line when defining new polygonial 
	    gl.glBegin(GL.GL_LINES);
	    gl.glColor4f(1.0f, 0.0f, 0.0f, 0.3f);
	    MapPoint p2 = current_pos.getProjectedPoint(proj);
	    gl.glVertex2dv(proj.projToXY(getLastPoint()), 0);
	    gl.glVertex2d(p2.getX(), p2.getY());
	    gl.glEnd();
	}
	gl.glPopMatrix();
    }
    
    /**
     * Creates the initial polygonial shape.
     */
    private void initPolygon() {
	StratmasObject location = sComp.getChild("location");
	if (location != null && location.getType().getName().equals("Polygon")) {
	    Polygon pol = (Polygon) StratmasObjectFactory.cloneObject(sComp.getChild("location"));
	    Enumeration curves =pol.getCurves();
	    for (;curves.hasMoreElements();) {
		Line line = (Line)curves.nextElement();
		addMapDrawableAdapter(line);
		addMapDrawableAdapter(line.getStartPoint());
		addMapDrawableAdapter(line.getEndPoint());
	    }
	    polygonCompleted = true;
	}
    }
    
    /**
     * Creates the initial circular shape.
     */
    private void initCircle() {
	// initialize the circle
	StratmasObject location = sComp.getChild("location");
	if (location != null && location.getType().getName().equals("Circle")) {
	    circle = (Circle) StratmasObjectFactory.cloneObject(sComp.getChild("location"));
	    // set center of the circle
	    Point center = getInitialCenter();
	    circle.moveTo(center.getLon(), center.getLat());
	}
    }
    
    /**
     * Sends calls to necessary methods when exiting the creator.
     */
    private void exitCreator() {
	clearAll();
	setCreatorMode(AreaCreationDrawer.UNDEFINED);
	doDispose();
    }
    
    /**
     * Returns true if the creation of the polygonial is completed.
     */
    public boolean isPolygonCompleted() {
	return polygonCompleted;
    }
    
    /**
     * Returns the circle.
     */
    public Circle getCircle() {
	return circle;
    }
    
    /**
     * Sets the circle.
     */
    public void setCircle(Circle circle) {
	this.circle = circle;
    }
    
    /**
     * Returns the first inserted polygonial point.
     */
    public Point getFirstPoint() {
	return firstPoint;
    }

    /**
     * Sets the first point of the polygonial.
     */
    public void setFirstPoint(Point firstPoint) {
	this.firstPoint = firstPoint;
    }
    
    /**
     * Returns the last inserted polygonial point.
     */
    public Point getLastPoint() {
	return lastPoint;
    }
    
    /**
     * Sets the last point of the polygonial.
     */
    public void setLastPoint(Point lastPoint) {
	this.lastPoint = lastPoint;
    }
    
    /**
     * Adds new point to the polygonial. This method is used while
     * the polygonial is beeing created.
     *
     * @param point point to be added to the polygonial.
     */
    public  void addPoint(Point point) {
	// add first point to the polygonial
	if (firstPoint == null) {
	    firstPoint = point;
	}
	// add new line to the polygonial
	if (lastPoint != null) {
	    Point p2 = StratmasObjectFactory.createPoint("p2", point.getLat(), point.getLon());
	    addMapDrawableAdapter(p2); 
	    addLine(lastPoint, p2);
	    overlappingPoints.put(point, p2);
	    overlappingPoints.put(p2, point);
	}
	lastPoint = point;
	lastPoint.setIdentifier("p1");
	addMapDrawableAdapter(lastPoint);  
    }

    /**
     * Adds a line created of the two given points to the polygonial.
     *
     * @param p1 the start point of the line.
     * @param p2 the end point of the line.
     */
    private void addLine(Point p1, Point p2) {
	Vector lineAdapters = mapDrawableAdapters(MapLineAdapter.class);
	Line line = StratmasObjectFactory.createLine(String.valueOf(lineAdapters.size()), p1, p2);

	addMapDrawableAdapter(line);
    }
    
    /**
     * Completes the polygonial by adding a line between the last
     * and the first points in the list. If the polygonial cannot be
     * created all the points and lines are removed from the creator.
     */
    public void addLastLine() {
	if (!isPolygonCompleted()) {
	    Vector lineAdapters = mapDrawableAdapters(MapLineAdapter.class);
	    if (lineAdapters.size() >= 2) {
		Point p2 = StratmasObjectFactory.createPoint("p2", firstPoint.getLat(), firstPoint.getLon());
		addMapDrawableAdapter(p2); 
		addLine(lastPoint, p2);
		overlappingPoints.put(firstPoint, p2);
		overlappingPoints.put(p2, firstPoint);
		polygonCompleted = true;
		firstPoint = null;
		lastPoint = null;
	    }
	    else {
		// if no polygon can be created
		clearAll();
	    }
	}
    }
    
    /**
     * Inserts new point into the polygonial. This method is used to
     * insert a point into previously definied polygonial.
     *
     * @param point the point to be inserted.
     * @param line the line which is divided by the inserted point into
     *             two new lines. This line is later replaced by the new
     *             lines.
     */
    public void insertPoint(Point point, Line line) {
	try {
	    // replace the line
	    MapDrawableAdapter lineAdapter = getMapDrawableAdapter(line.getReference());
	    if (lineAdapter != null) {
		//change identifiers for all the lines that come after the actual line 
		int lineId = Integer.parseInt(lineAdapter.getObject().getIdentifier().toString());
		Vector lineAdapters = mapDrawableAdapters(MapLineAdapter.class);
		for (int i = 0; i < lineAdapters.size(); i++) {
		    MapLineAdapter ad = (MapLineAdapter)lineAdapters.get(i);
		    int id = Integer.parseInt(ad.getObject().getIdentifier().toString());
		    if (id > lineId) {
			((Line)ad.getObject()).setIdentifier(String.valueOf(id+1));
		    }
		}
		// remove the actual line
		removeMapDrawableAdapter(lineAdapter);
       	
		// add new lines 
		// create and add the first line
		Point p2 = point;
		p2.setIdentifier("p2");
		addMapDrawableAdapter(p2);
		Line l1 =  StratmasObjectFactory.createLine(String.valueOf(lineId), line.getStartPoint(), p2);
		addMapDrawableAdapter(l1);

		// create and add the second line
		Point p1 = StratmasObjectFactory.createPoint("p1", point.getLat(), point.getLon());
		addMapDrawableAdapter(p1);
		Line l2 =  StratmasObjectFactory.createLine(String.valueOf(lineId+1), p1, line.getEndPoint());
		addMapDrawableAdapter(l2);
		
		overlappingPoints.put(p1, p2);
		overlappingPoints.put(p2, p1);
	    }
	}
	catch (NumberFormatException e) {
	    e.printStackTrace();
	}
    }
    
    /**
     * Clears all line and point adapters from mapDrawableAdapters list.
     */
    public void clearAll() {
	circle     = null;
	firstPoint = null;
	lastPoint  = null;
	polygonCompleted = false;
	removeMapDrawableAdapters(MapLineAdapter.class);
	removeMapDrawableAdapters(MapPointAdapter.class);
	setIsDrawnMapDrawablesListUpdated(false);
	overlappingPoints.clear();
	update();
    }

    /**
     * Updates the radius of the circular shape.
     */
    public void updateRadius(Point point) {
	Point center = null;
	// set the center of the circle
	Shape initShape = (sComp.getChild("location") != null)? (Shape)sComp.getChild("location") :
	    getTemporaryCircle();
	Shape location = (circle == null)? initShape : circle;
	if (location.getType().getName().equals("Circle")) {
	    center = ((Circle)location).getCenter();
	}
	else {
	    BoundingBox box = location.getBoundingBox();
	    double lonCenter = (box.getEastLon()+box.getWestLon())/2;
	    double latCenter = (box.getNorthLat()+box.getSouthLat())/2;	
	    center = StratmasObjectFactory.createPoint("center", latCenter, lonCenter);
	} 
	// compute the radius
	double radius = GeoMath.distanceGC(center.getLat(), center.getLon(), point.getLat(), point.getLon());
	// update the shape
	if (circle == null) {
	    circle = StratmasObjectFactory.createCircle("location", center.getLat(), center.getLon(), radius);
	}
	else {
	    circle.setRadius(radius);
	} 
    }
    
    /**
     * Cretates a Circle with zero radius. This method is used to obtain
     * the initial Circle for objects that don't have area.
     *
     * @return the temporary Circle.
     */
    private Circle getTemporaryCircle() {
	return StratmasObjectFactory.createCircle("location", 
						  getInitialCenter().getLat(),
						  getInitialCenter().getLon(), 
						  0d);
    }
    
    /**
     * Initializes the center of the area of the actual object. If the object is
     * visible on the map then the center of it's BoundingBox is the center of the
     * area. If the object is Activity without location and it's owner is visible 
     * on the map then the center of the owner's BoundingBox is the center of the 
     * area. In all other cases the center of the area is the center of the visible 
     * part of the map.
     *
     * @return the center of the initial area.  
     */
    private Point getInitialCenter() {
	BoundingBox locBox = null;
	Point center = null;
	// get the location bounds
	if (sComp.getChild("location") != null) {
	    locBox = ((Shape)sComp.getChild("location")).getBoundingBox();
	}
	else if (sComp.getType().canSubstitute("Activity")) {
	    locBox = ((Shape)sComp.getParent().getParent().getChild("location")).getBoundingBox();  
	}
	// set the center
	if (locBox != null) {
	    center =  StratmasObjectFactory.createPoint("center", 
							(locBox.getNorthLat() + locBox.getSouthLat())/2,
							(locBox.getEastLon() + locBox.getWestLon())/2);
	}
	// check if the center is visible on the map
	BoundingBox bbox = getScaledBoundingBox();
	if (center != null && center.getLon() >= bbox.getWestLon() && center.getLon() <= bbox.getEastLon() &&
	    center.getLat() >= bbox.getSouthLat() && center.getLat() <= bbox.getNorthLat()) {
	    return center;
	} 
	else {
	    return StratmasObjectFactory.createPoint("center", 
						     (bbox.getNorthLat() + bbox.getSouthLat())/2,
						     (bbox.getEastLon() + bbox.getWestLon())/2);
	}
    }
    
    /**
     * Sets the mode of the creator.
     */
    public void setCreatorMode(int mode) {
	creatorMode = mode;
    }
    
    /**
     * Returns the mode of the creator.
     */
    public int getCreatorMode() {
	return creatorMode;
    }
    
    /**
     * Sets scaled orthographic bounds.
     *
     * @param sbox bouding box of scaled orthographic bounds.
     */
    public void setScaledBoundingBox(BoundingBox sbox) {
	orts_box = (BoundingBox)sbox.clone();
	
	// Projection will change, so we need to update symbols if they are invariant.
	for (Enumeration e = mapDrawableAdapters.elements(); e.hasMoreElements();) {
	    MapDrawableAdapter mda = (MapDrawableAdapter)e.nextElement();
	    if (mda instanceof MapElementAdapter) {
		((MapElementAdapter)mda).invalidateSymbolList();
	    }
	    else if (mda instanceof MapPointAdapter) {
		((MapPointAdapter)mda).invalidateSymbolList();
	    }
	}
    	update();
    }
    
    /**
     * Moves the area in the chosen direction.
     *
     * @param lon longitude of the new center of the actual area's bounding box.
     * @param lat latitude of the new center of the actual area's bounding box.
     *
     */
    public void moveShape(double lon, double lat) {
	BoundingBox box = getAreaBoundingBox();
	if (box != null) {
	    double lonCenter = (box.getEastLon()+box.getWestLon())/2;
	    double latCenter = (box.getNorthLat()+box.getSouthLat())/2;
	    // circular area
	    if (circle != null) {
		circle.move(lon-lonCenter, lat-latCenter);
	    }
	    else {
		// polygonial area
		Vector lineAdapters = mapDrawableAdapters(MapLineAdapter.class);
		for (Enumeration e = lineAdapters.elements(); e.hasMoreElements(); ) {
		    Line line = (Line)((MapLineAdapter)e.nextElement()).getObject();
		    line.move(lon-lonCenter, lat-latCenter);
		}
	    }
	}
    }
    
    /**
     * Returns the bounding box of the area.
     *
     * @return the bounding box of the area.
     */
    public BoundingBox getAreaBoundingBox() {
	// bounding box of the circle
	if (circle != null) {
	    return circle.getBoundingBox();
	}
	// bounding box of the polygonial
	Vector pointAdapters = mapDrawableAdapters(MapPointAdapter.class);
	if (!pointAdapters.isEmpty()) {
	    Point p = (Point)((MapPointAdapter)pointAdapters.firstElement()).getObject();
	    double minLon = p.getLon();
	    double maxLon = p.getLon();
	    double minLat = p.getLat();
	    double maxLat = p.getLat();
	    for (int i = 1; i < pointAdapters.size(); i++) {
		p = (Point)((MapPointAdapter)pointAdapters.get(i)).getObject();
		minLon = (p.getLon() < minLon)? p.getLon() : minLon;
		maxLon = (p.getLon() > maxLon)? p.getLon() : maxLon;
		minLat = (p.getLat() < minLat)? p.getLat() : minLat;
		maxLat = (p.getLat() > maxLat)? p.getLat() : maxLat;
	    }
	    return new BoundingBox(minLon, minLat, maxLon, maxLat);
	}
	//
	return null;
    }
    
    /**
     * Creates the polygonial shape from the list of lines.
     *
     * @return the polygonial shape.
     */
    public Polygon createPolygonialShape() {
	// add the last line if the polygonial is not completed
	if (!polygonCompleted) {
	    addLastLine();
	}
	// create the polygonial
	Vector lineAdapters = mapDrawableAdapters(MapLineAdapter.class);
	if (lineAdapters.size() >= 3) {
	    // get all the lines
	    Vector lines = new Vector();
	    lines.setSize(lineAdapters.size());
	    for (int i = 0; i < lineAdapters.size(); i++) {
		Line line = (Line)((MapLineAdapter)lineAdapters.get(i)).getObject();
		lines.removeElementAt(Integer.parseInt(line.getIdentifier()));
		lines.add(Integer.parseInt(line.getIdentifier()), line);
	    }
	    // create the polygonial shape
	    return StratmasObjectFactory.createPolygon("location", lines);
	}
	//
	return null;
    }
    
    /**
     * Checks if the polygon is simple.
     *
     * @param polygon the polygon to test.
     *
     * @return true if the polygon is simple, false otherwise.
     */
    private boolean isPolygonSimple(Polygon polygon) {
	Vector tempLines = new Vector();
	for (Enumeration e = polygon.getCurves(); e.hasMoreElements(); ) {
	    tempLines.add(e.nextElement());
	}
	for (int i = 0; i < tempLines.size()-2; i++) {
	    int lastLine = (i == 0)? tempLines.size()-1 : tempLines.size();
	    for (int j = i+2; j < lastLine; j++) {
		Line l1 = (Line)tempLines.get(i);
		Line l2 = (Line)tempLines.get(j);
		if (l1.intersects(l2, getProjection())) {
		    return false;
		}
	    }
	}
	return true;
    }
    
    /**
     * Finds the "twin" Point of the given Point. In the polygonial shape
     * the point where two lines meet represents of two Point objects. This
     * method finds the "other" Point object when one is given.
     *
     * @param p a point in the polygonial shape.
     *
     * @return the "twin" point.
     */
    public Point findMeetingPoint(Point p) {
	// find the point 
	Line line = (Line)p.getParent();
	boolean startPoint = (line.getStartPoint().equals(p))? true : false;
	Vector lineAdapters = mapDrawableAdapters(MapLineAdapter.class);
	for (int i = 0; i < lineAdapters.size(); i++) {
	    Line tmpLine = (Line)((MapLineAdapter)lineAdapters.get(i)).getObject();
	    Point tmpPoint  = (startPoint)? tmpLine.getEndPoint() : tmpLine.getStartPoint();
	    if (tmpPoint.getLat() == p.getLat() && tmpPoint.getLon() == p.getLon()) {
		overlappingPoints.put(p, tmpPoint);
		overlappingPoints.put(tmpPoint, p);
		return tmpPoint;
	    }
	}
	
	return null;
    }
   
    /**
     * Updates the location of the StratmasObject.
     *
     * @param sComp the updated element.
     * 
     * @return true if the element is updated with new shape.
     */
    public boolean updateElementWithShape(StratmasObject sComp) {
	Shape sShape = null;

	// get circular shape if it exists
	sShape = circle;
	if (sShape == null) {
	    // if not, try with polygonial
	    Polygon pol = createPolygonialShape();
	    if (pol != null) {
		sShape = (isPolygonSimple(pol))? pol : null; 
		if (sShape == null) {
		    int res = StratmasDialog.showOptionDialog(null,
							      "Not valid area. Exit anyway?",
							      "Area definition!",
							      JOptionPane.YES_NO_OPTION,
							      JOptionPane.QUESTION_MESSAGE,
							      null,
							      null,
							      null);
		    return res == JOptionPane.YES_OPTION;	
		}
	    }
	}
	// update the element with the new shape if it exists 
	if (sShape != null) {
	    // replace the old area
	    if (sComp.hasChild("location")) {
		StratmasObject location = (StratmasObject)sComp.getChild("location");
		location.replace(sShape, this);
	    }
	    // add area if no area exits
	    else {
		sComp.add(sShape);
	    }
	    return true;
	}
	else {
	    int res = StratmasDialog.showOptionDialog(null,
						      "No area defined. Exit anyway?",
						      "Area definition!",
						      JOptionPane.YES_NO_OPTION,
						      JOptionPane.QUESTION_MESSAGE,
						      null,
						      null,
						      null);
	    return res == JOptionPane.YES_OPTION;
	}
    }
    
}

