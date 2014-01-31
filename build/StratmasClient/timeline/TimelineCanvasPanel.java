package StratmasClient.timeline;

import java.util.Vector;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.border.EmptyBorder;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;

import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLDrawableFactory;
import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.GLUT;

/**
 * Super class for all timeline panels which contain GLCanvas.
 */
public class TimelineCanvasPanel extends JPanel implements GLEventListener, MouseListener, MouseMotionListener {
    /**
     * The left bound of the drawing area.
     */
    protected int xmin;
    /**
     * The right bound of the drawing area.
     */
    protected int xmax;
    /**
     * The lower bound of the drawing area.
     */
    protected int ymin;
    /**
     * The upper bound of the drawing area.
     */
    protected int ymax;
    /**
     * The x-coordinate of the current position of the mouse cursor expressed in the projected coordinate system.
     */ 
    protected int currentCursorProjectedPos;
    /**
     * The latest time the panel is drawn. 
     */
    protected long latestUpdateTime = 0;
    /**
     * The minimum delay between the updates (in milliseconds).
     */
    protected long updateTimeDelay = 50;
    /**
     * The drawing area.
     */
    protected GLCanvas canvas;
    /**
     * The GLU library.
     */
    protected GLU glu = new GLU();
    /**
     * Reference to the timeline.
     */
    protected Timeline timeline;
    /**
     * Reference to the timeline panel.
     */
    protected TimelinePanel timelinePanel;
      
    /**
     * Creates new panel.
     *
     * @param timeline the timeline.
     */
    public TimelineCanvasPanel(Timeline timeline, TimelinePanel timelinePanel) {
	// set reference to the timeline and the timeline panel
	this.timeline = timeline;
	this.timelinePanel = timelinePanel;
	
	// create JOGL canvas
	GLCapabilities glcaps = new GLCapabilities();
	glcaps.setHardwareAccelerated(true);
	canvas = new GLCanvas(glcaps);
	canvas.addGLEventListener(this);
	canvas.addMouseListener(this);
	canvas.addMouseMotionListener(this);

	// initialize the area bounds
	xmin = 0;
	xmax = 1000;
	ymin = 0;
	ymax = 1000;
    }
    
    /**
     * Initialization of the timeline.
     *
     * @param gld needed when opengl is used. 
     */
    public void init(GLAutoDrawable gld) {
	GL gl = gld.getGL();
	
	// set the background color
	Color c = this.getBackground();
	float r = c.getRed() / 255.0f;
	float g = c.getGreen() / 255.0f;
	float b = c.getBlue() / 255.0f;
	gl.glClearColor(r, g, b, 0.0f);
	
	// enable shading
	gl.glShadeModel(GL.GL_SMOOTH);
	
	// enable blending
	gl.glEnable(GL.GL_BLEND);
	gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
	
	// set actual matrix
	gl.glMatrixMode(GL.GL_PROJECTION);
	gl.glLoadIdentity();
	
	// initialize bounding box
	glu.gluOrtho2D(xmin, xmax, ymin, ymax);
    }
    
    /**
     * Draws the timeline.
     *
     * @param gld needed when opengl is used. 
     */
    public void display(GLAutoDrawable gld) {
	GL gl = gld.getGL();
	
	// set actual matrix
	gl.glMatrixMode(GL.GL_PROJECTION);
	gl.glLoadIdentity();
	
	// set bounding box
	glu.gluOrtho2D(xmin, xmax, ymin, ymax);
	
	// draw the color map
	drawGraph(gl);
    }
    
    /**
     * Not implemented.
     */
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
    
    /**
     * Not implemented.
     */
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}
    
    /**
     * Not implemented.
     */
    public void mouseClicked(MouseEvent e) {}

    /**
     * Not implemented.
     */
    public void mouseEntered(MouseEvent e) {}
    
    /**
     * Not implemented.
     */
    public void mouseExited(MouseEvent e) {}

    /**
     * Not implemented.
     */
    public void mousePressed(MouseEvent e) {}
        
    /**
     * Not implemented.
     */
    public void mouseReleased(MouseEvent e) {}
	
    /**
     * Updates the position of the mouse cursor.
     *
     * @param e the mouse event.
     */
    public void mouseDragged(MouseEvent e) {
	Object src = e.getSource();
	if (src.equals(canvas)) {
	    // get x window coordinate
	    int x = (int)Math.round(e.getX());
	    int y = (int)Math.round(e.getY());
	    // update the position of the cursor
	    setCurrentCursorProjectedPos(x);
	    // redraw
	    if (System.currentTimeMillis() - latestUpdateTime > updateTimeDelay) {
		update();
		latestUpdateTime = System.currentTimeMillis();
	    }
	}
    }
        
    /**
     * Updates the position of the mouse cursor.
     *
     * @param e the mouse event.
     */
    public void mouseMoved(MouseEvent e) {
	Object src = e.getSource();
	if (src.equals(canvas)) {
	    // get x window coordinate
	    int x = (int)e.getX();
	    int y = (int)e.getY();
	    // update the position of the cursor
	    setCurrentCursorProjectedPos(x);
	    // redraw
	    if (System.currentTimeMillis() - latestUpdateTime > updateTimeDelay) {
		update();
		latestUpdateTime = System.currentTimeMillis();
	    }
	}
    }
    
    /**
     * Redraw the timeline.
     */
    public void update() {
	canvas.validate();
	canvas.repaint();
    }
    
    /**
     * Resets the panel.
     */
    public void reset() {
	// redraw
	update();
    }
    
    /**
     * Removes the panel.
     */
    public void remove() {
	timeline = null;
	canvas.removeGLEventListener(this);
	canvas.removeMouseListener(this);
	canvas.removeMouseMotionListener(this);
    }

    /**
     * Returns the x-coordinate of the position under the mouse cursor 
     * expresed in the projected coordinate system. 
     */
    protected int getCurrentCursorProjectedPos() {
	return currentCursorProjectedPos;
    }
    
    /**
     * Sets the x-coordinate of the curent mouse position in the projected coordinate system.
     *
     * @param x the x-coordinate in the window coordinate system
     */
    protected void setCurrentCursorProjectedPos(int x) {
	int currentPos = convertWindowXToProjectedX(x);
	currentCursorProjectedPos = (currentPos < xmin) ? xmin : (currentPos > xmax) ? xmax : currentPos;
    }

    /**
     * Converts the window x-coordinate in the projected x-coordinate.
     *
     * @param x the x-coordinate in the window coordinate system
     */
    protected int convertWindowXToProjectedX(int x) {
	if (canvas.getWidth() > 0) {
	    return (int) ((x * (xmax - xmin) *1.0 / canvas.getWidth()) + xmin);
	}
	else {
	    return 0;
	}
    }
    
    /**
     * Converts the projected x-coordinate in the window x-coordinate.
     *
     * @param x the x-coordinate in the projected coordinate system
     */
    protected int convertProjectedXToWindowX(int x) {
	return (int) (canvas.getWidth() * (x - xmin) * 1.0 / (xmax - xmin));
    }
    
    /**
     * Converts the given projected x-coordinate to the time in the timeline.
     *
     * @param x the projected x-coordinate.
     *
     * @return the relative time in the timeline.
     */
    public double convertProjectedXToCurrentTime(int x) {
	long tstart = timelinePanel.getDisplayedStartTime();
	long tend    = timelinePanel.getDisplayedEndTime();
	return (x - xmin) * (tend - tstart) * 1.0 / (xmax - xmin) + tstart;
    }
    
    /**
     * Converts the time given in the current time unit to the projected x-coordinate.
     *
     * @param t the relative time in the timeline.
     *
     * @return the projected x-coordinate.
     */
    public int convertCurrentTimeToProjectedX(double t) {
	long tstart = timelinePanel.getDisplayedStartTime();
	long tend    = timelinePanel.getDisplayedEndTime();
	return  (int) (xmin + ((t - tstart) * (xmax - xmin) * 1.0 / (tend - tstart)));
    }
    
    /**
     * Converts the given window x coordinate to the time in the timepanel.
     */
    public double convertWindowXToCurrentTime(int x) {
	long tstart = timelinePanel.getDisplayedStartTime();
	long tend    = timelinePanel.getDisplayedEndTime();
	int projectedX = convertWindowXToProjectedX(x);
	double t = convertProjectedXToCurrentTime(projectedX);
        t = (t < tstart)? tstart : ((t > tend)? tend : t);
	return t;
    }
    
    /**
     * Converts the time given in the current time unit to the window x-coordinate.
     */
    public int convertCurrentTimeToWindowX(double t) {
	int projectedX = convertCurrentTimeToProjectedX(t);
	return convertProjectedXToWindowX(projectedX);
    }
    
    /**
     * Converts a time interval into a length scale in the projected coordinates.
     */
    protected double dt2dx(long dt) {
	double x1 = convertCurrentTimeToProjectedX(0);
	double x2 = convertCurrentTimeToProjectedX(timelinePanel.millisecondsToTimeUnit(dt));
	double res = x2 - x1;
	return x2 - x1;
    }
    
    /**
     * Converts a length in the window coordinates into a length in the projected coordinates.
     */
    protected int dW2dP(int dW) {
        int x1 = convertWindowXToProjectedX(0);
	int x2 = convertWindowXToProjectedX(dW);
	int res = x2 - x1;
	return x2 - x1;
    }
   
    /**
     * Draw all the graphic elements.
     */
    protected void drawGraph(GL gl) {}
    
}
    
