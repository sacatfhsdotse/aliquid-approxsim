package StratmasClient.map.graph;

import java.util.Hashtable;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.glu.GLU;

/**
 * The drawing area for XY plot.
 *
 * @author Amir Filipovic 
 */
public class XYDrawingArea extends JPanel implements GLEventListener {
    /**
     * The minimum horizontal coordinate of the graph.
     */
    private int xmin = 0; 
    /**
     * The maximum horizontal coordinate of the graph.
     */
    private int xmax = 1000;    
    /**
     * The minimum vertical coordinate of the graph.
     */
    private int ymin = 0; 
    /**
     * The maximum vertical coordinate of the graph.
     */
    private int ymax = 1000;
    /**
     * The display list for the drawn horizontal lines.
     */
    private int verticalLinesDisplayList;
    /**
     * The display list for the drawn vertical lines.
     */
    private int horizontalLinesDisplayList;
    /**
     * Indicates if verticalLinesDisplayList has to be updated.
     */
    private boolean updateVerticalLinesDisplayList = false;
    /**
     * Indicates if horizontalLinesDisplayList has to be updated.
     */
    private boolean updateHorizontalLinesDisplayList = false;
    /**
     * The colors used to display factions.
     */
    private float[][] factionColors;
    /**
     * The actual drawing area.
     */
    private GLCanvas glcanvas;
    /**
     * The process variable graph.
     */
    private ProcessVariableXYGraph graph;
    /**
     * The glu to use.
     */
    private GLU glu = new GLU();

    /**
     * Creates new drawing area.
     *
     * @param graph reference to the container of this panel.
     * @param factionColors colors for each faction.
     */
    public XYDrawingArea(ProcessVariableXYGraph graph, Color[] factionColors) {
        this.graph = graph;
                
        // create JOGL panel
        GLCapabilities glcaps = new GLCapabilities(GLProfile.getDefault());
        glcaps.setHardwareAccelerated(true);
        glcanvas = new GLCanvas(glcaps);
        glcanvas.addGLEventListener(this);
        
        // color for each faction
        this.factionColors = new float[factionColors.length][3];
        for (int i = 0; i < factionColors.length; i++) {
            this.factionColors[i][0] = factionColors[i].getRed()/255.0f;
            this.factionColors[i][1] = factionColors[i].getGreen()/255.0f;
            this.factionColors[i][2] = factionColors[i].getBlue()/255.0f;
        }
        
        // set the panel
        setLayout(new BorderLayout());
        add(glcanvas, BorderLayout.CENTER);
        setPreferredSize(new Dimension(0,0));
    }
    
    /**
     * Initialization of the graph.
     *
     * @param gld openGL rendering surface interface. 
     */
    public void init(GLAutoDrawable gld) {
        GL2 gl = (GL2) gld.getGL();
        
        // set the background color
//         Color c = getBackground();
//         float r = c.getRed()/255.0f;
//         float g = c.getGreen()/255.0f;
//         float b = c.getBlue()/255.0f;
//         gl.glClearColor(r, g, b, 1.0f);
        gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        
        // set actual matrix
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        // update the display lists
        setUpHorizontalLines(gl);
        setUpVerticalLines(gl);
        
        // initialize bounding box
        glu.gluOrtho2D(xmin, xmax, ymin, ymax);
    }

    /**
     * Displays the graph.
     *
     * @param gld openGL rendering surface interface. 
     */
    public void display(GLAutoDrawable gld) {
        GL2 gl = (GL2) gld.getGL();
        
        // set actual matrix
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        
        // set bounding box
        glu.gluOrtho2D(xmin, xmax, ymin, ymax);
        
        // update the vertical lines
        if (updateVerticalLinesDisplayList) {
            setUpVerticalLines(gl);
        }
        
        // update the horizontal lines
        if (updateHorizontalLinesDisplayList) {
            setUpHorizontalLines(gl);
        }
        
        // draw the color map
        drawGraph(gl);
    }
    
    /**
     * Repaints the area when the canvas is resized.
     */
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        // If intel we currently need to fake an update. Idealy we
        // would like to have this a boolean variable, but we need a
        // GL context to get the string, so...
        if (drawable.getGL().glGetString(GL2.GL_VENDOR).matches(".*Intel.*")) {
             glcanvas.repaint(100);
         }
    }
    
    /**
     * Part of GLEventListener. Not implemented.
     */
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, 
                               boolean deviceChanged) {
    }
    
    /**
     * Updates the display list of the vertical lines.
     */
    public void updateVerticalLinesDisplayList() {
        updateVerticalLinesDisplayList = true;
    }

    /**
     * Updates the display list of the horizontal lines.
     */
    public void updateHorizontalLinesDisplayList() {
        updateHorizontalLinesDisplayList = true;
    }

    /**
     * Scales the time value for displaying on the graph.
     *
     * @param t the time expressed in relative time units.
     *
     * @return the x-coordinate in the projected space. 
     */
    private int scaleXValue(long t) {
        return (int)(xmin + (xmax - xmin) * ((double)(t - graph.getStartTime())) / 
                     ((double)(graph.getEndTime() - graph.getStartTime())));
    }
    
    /**
     * Scales y value for displaying on the graph. Both linear and logarithmic scales are handled.
     *
     * @param yval the y value.
     *
     * @return the y-coordinate in the projected space. 
     */
    private Long scaleYValue(double yval) {
        // linear scale
        if (graph.getActualScale().equals("Linear Scale")) {
            double yDiff = graph.getUpperYBound()- graph.getLowerYBound();
            return new Long((long)(ymin + (ymax - ymin) * (yval - graph.getLowerYBound()) / yDiff));
        }
        // logarithmic scale
        else {
            // the lower bound is equal to zero
            if (graph.getLowerYBound() == 0) {
                // the value is not smaller then the second lowest displayed value on the y-axis
                if (yval >= graph.getSecondLowerYBound()){
                    double logPVMin = graph.log10(graph.getSecondLowerYBound());
                    double logPVMax = graph.log10(graph.getUpperYBound());
                    double logPV = graph.log10(yval);
                    double subInterval = (ymax - ymin) / (logPVMax - logPVMin + 1);
                    double lg = subInterval + ymin + (ymax - ymin - subInterval) * (logPV - logPVMin) / (logPVMax - logPVMin); 
                    return new Long((long)lg);
                    
                }
                // the value is smaller then the second lowest displayed value on the y-axis but non-negative
                else if (yval > 0) {
                    double zeroValue = ProcessVariableXYGraph.MINIMUM_LOGARITHMIC_Y_VALUE / 10;
                    double logPVMin = graph.log10(zeroValue);
                    double logPVMax = graph.log10(graph.getSecondLowerYBound());
                    double logPV = graph.log10(yval);
                    double subInterval = (ymax - ymin) / (logPVMax - logPVMin + 2);
                    double lg = ymin + subInterval * (logPV - logPVMin) / (logPVMax - logPVMin); 
                    return new Long((long)lg);  
                    
                }
                else if (yval == 0) {
                    return new Long((long)ymin);
                }
                // the value is negative
                else {
                    return null;
                }        
            }
            // the lower bound is larger then zero
            else {
                // the value is larger then zero
                if (yval > 0){
                    double logPVMin = graph.log10(graph.getLowerYBound());
                    double logPVMax = graph.log10(graph.getUpperYBound());
                    double logPV = graph.log10(yval);
                    double lg    = ymin + (ymax - ymin) * (logPV - logPVMin) / (logPVMax - logPVMin); 
                    return new Long((long)lg);
                }
                // the value is not larger then zero
                else {
                    return null;
                }
            }
        }
    }
    
    /**
     * Redraws the graph.
     */
    public void update() {
        glcanvas.validate();
        glcanvas.repaint();        
    }
    
    /**
     * Resets the graph.
     */
    public void reset() {
        updateVerticalLinesDisplayList();
        updateHorizontalLinesDisplayList();
        update();
    }
    
    /**
     * Updates display list for the vertical lines.
     */
    private void setUpVerticalLines(GL gl2) {
         GL2 gl = (GL2) gl2;
         verticalLinesDisplayList = (gl.glIsList(verticalLinesDisplayList)) ? verticalLinesDisplayList : gl.glGenLists(1);
        
        // get x values for the lines
        int[] xval = new int[ProcessVariableXYGraph.NR_OF_DISPLAYED_TIME_VALUES];
        int diff = (int)((graph.getEndTime() - graph.getStartTime()) / (xval.length - 1));
        for (int i = 0; i < xval.length - 1; i++) {
            xval[i] = (int) (graph.getStartTime() + i * diff);
        }
        xval[xval.length - 1] = (int)graph.getEndTime();
        
        // update the display list
        gl.glNewList(verticalLinesDisplayList, GL2.GL_COMPILE);
        gl.glPushMatrix();
        // width of the lines
        gl.glLineWidth(1.0f);
        // color
        Color c = getBackground();
        gl.glColor4f(c.getRed()/255.0f, c.getGreen()/255.0f, c.getBlue()/255.0f, 1.0f);
//        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        // draw he lines
        for (int i = 0; i < xval.length; i++) {
            int val = xval[i];
            // get scaled value
            int x = scaleXValue(val);
            // draw line
            gl.glBegin(GL2.GL_LINES);
            gl.glVertex2d(x, ymin);
            gl.glVertex2d(x, ymax);
            gl.glEnd();
        }
        // the last line
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex2d(xmax - 1, ymin);
        gl.glVertex2d(xmax - 1, ymax);
        gl.glEnd();
        gl.glPopMatrix();
        gl.glEndList();
        
        updateVerticalLinesDisplayList = false;
    }
    
    /**
     * Updates display list for the horizontal lines.
     */
    public void setUpHorizontalLines(GL gl2) {
         GL2 gl = (GL2) gl2;
        horizontalLinesDisplayList = (gl.glIsList(horizontalLinesDisplayList)) ? horizontalLinesDisplayList : 
            gl.glGenLists(1);
        
        // get y values for the lines
        double[] yval = getHorizontalLineValues();
        if (yval != null) {
            // update the display list
            gl.glNewList(horizontalLinesDisplayList, GL2.GL_COMPILE);
            gl.glPushMatrix();
            // width of the lines
            gl.glLineWidth(1.0f);
            // color
            Color c = getBackground();
            gl.glColor4f(c.getRed()/255.0f, c.getGreen()/255.0f, c.getBlue()/255.0f, 1.0f);
            //gl.glColor3f(1.0f, 1.0f, 1.0f);
            for (int i = 0; i < yval.length; i++) {
                int y = scaleYValue(yval[i]).intValue();
                // draw line
                gl.glBegin(GL2.GL_LINES);
                gl.glVertex2d(xmin, y);
                gl.glVertex2d(xmax, y);
                gl.glEnd();
            }
            // the last line
            gl.glBegin(GL2.GL_LINES);
            gl.glVertex2d(xmin, ymax - 1);
            gl.glVertex2d(xmax, ymax - 1);
            gl.glEnd();
            gl.glPopMatrix();
            gl.glEndList();
            
            updateHorizontalLinesDisplayList = false;
        }
    }
    
    /**
     * Computes the coordinates for the horizontal lines.
     *
     * @return the y-coordinates for the horizontal lines. 
     */
    private double[] getHorizontalLineValues() {
        double[] yValues = new double[graph.getNrOfDisplayedYValues()];
        // linear scale
        if (graph.isLinearScale()) {
            double partDiff = (graph.getUpperYBound() - graph.getLowerYBound()) / (yValues.length - 1);
            for (int i = 0; i < yValues.length - 1; i++) {
                yValues[i] = graph.getLowerYBound() + i * partDiff;
            }
            yValues[yValues.length - 1] = graph.getUpperYBound();
        }
        // logarithmic scale
        else {
            double minValue = graph.getLowerYBound();
            double maxValue = graph.getUpperYBound();
            // special case - the difference between the bounds is one power of ten and the lower bound is non-zero
            if (graph.getLowerYBound() > 0 &&
                (int)Math.round(graph.log10(graph.getUpperYBound()) - graph.log10(graph.getLowerYBound())) == 1) {
                yValues[0] = graph.getLowerYBound();
                for (int i = 0; i < graph.MIDDLE_LOG_VALUES.length; i++) {
                    yValues[i + 1] = graph.getLowerYBound() * graph.MIDDLE_LOG_VALUES[i];
                } 
                yValues[3] = graph.getUpperYBound();
            }
            // all other cases
            else {
                double yVal = maxValue;
                int i = yValues.length - 1;
                while (i > 0) {
                    yValues[i] = yVal;
                    yVal = yVal / 10; 
                    i--;
                }
                yValues[0] = minValue;
            }
        }
        //
        return yValues;
    }
    
    /**
     * Draw the graph.
     */
    private void drawGraph(GL gl2) {
         GL2 gl = (GL2) gl2;
        // clear the window
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
        
        // draw the horizontal lines
        gl.glCallList(verticalLinesDisplayList);
        
        // draw the vertical lines
        gl.glCallList(horizontalLinesDisplayList);
        
        // draw the current values
        drawCurrentValues(gl); 
        
        // draw the values from the previous run
        if (graph.getShowPrevious()) {
            drawPreviousValues(gl);
        }
    }
    
    /**
     * Draws the pv values from the current simulation run in the graph.
     */
    private void drawCurrentValues(GL gl2) {
         GL2 gl = (GL2) gl2;
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
         gl.glLineWidth(1.0f);
        String[] factions =  graph.getFactions();
        SimulationPVValues currentValues = graph.getCurrentValues();
        // show values for each faction
        for (int i = 0; i < factions.length; i++) {
            int xscPrev = -1;
            int yscPrev = 0;
            // get color
            float[] col = factionColors[i];
            for (int j = 0; j < currentValues.times.size(); j++) {
                // get time unit
                long time = graph.getRelativeGraphTime(((Long)currentValues.times.get(j)).longValue());
                // get value
                Number val = (Number)((Hashtable)currentValues.values.get(j)).get(factions[i]);
                Long scaledValue = (val != null)? scaleYValue(val.doubleValue()) : null;
                if (scaledValue != null) {
                    // scale the time
                    int xsc = scaleXValue(time);
                    // scale the value
                    int ysc = scaledValue.intValue();
                    // modify the max y-value
                    ysc = (ysc == ymax)? ysc - 1 : ysc;
                    // set the color
                    gl.glColor3f(col[0], col[1], col[2]);
                    // start drawing lines from the second point
                    if (xscPrev != -1) {
                        gl.glBegin(GL2.GL_LINES);
                        gl.glVertex2d(xscPrev, yscPrev);
                        gl.glVertex2d(xsc, ysc);
                        gl.glEnd();
                    }
                    // the first point
                    else {
                        gl.glBegin(GL2.GL_POINTS);
                        gl.glVertex2d(xsc, ysc);
                        gl.glEnd();        
                    }
                    // update previous time values
                    xscPrev = xsc;
                    yscPrev = ysc;
                }
            }
        }
        gl.glPopMatrix();        
    }

    /**
     * Draws the pv values from the previous simulation run in the graph.
     */
    private void drawPreviousValues(GL gl2) {
         GL2 gl = (GL2) gl2;
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glPointSize(2.0f);
        String[] factions =  graph.getFactions();
        SimulationPVValues previousValues = graph.getPreviousValues();
        // show values for each faction
        for (int i = 0; i < factions.length; i++) {
            // get color
            float[] col = factionColors[i];
            for (int j = 0; j < previousValues.times.size(); j++) {
                // get time unit
                long time = graph.getRelativeGraphTime(((Long)previousValues.times.get(j)).longValue());
                // get value
                Hashtable hTable  =(Hashtable)previousValues.values.get(j);
                Number val = (Number) hTable.get(factions[i]);
                //Number val = (Number)((Hashtable)previousValues.values.get(j)).get(factions[i]);
                Long scaledValue = (val != null)? scaleYValue(val.doubleValue()) : null;
                if (scaledValue != null) {
                    // scale the time
                    int xsc = scaleXValue(time);
                    // scale the value
                    int ysc = scaledValue.intValue();
                    // modify the max y-value
                    ysc = (ysc == ymax)? ysc - 1 : ysc;
                    // set the color
                    gl.glColor3f(col[0], col[1], col[2]);
                    gl.glBegin(GL2.GL_POINTS);
                    gl.glVertex2d(xsc, ysc);
                    gl.glEnd();        
                }
            }
        }
        gl.glPopMatrix();
    }
    public void dispose(GLAutoDrawable glad){
      //TODO implement
    }

}
