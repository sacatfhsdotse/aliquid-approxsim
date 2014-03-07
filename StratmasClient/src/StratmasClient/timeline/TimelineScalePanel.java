package StratmasClient.timeline;

import java.util.Vector;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.awt.event.MouseEvent;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

/**
 * A panel which displays the scale as well as the selected times in the timeline.
 */
class TimelineScalePanel extends TimelineCanvasPanel {
    /**
     * The start time of the currently selecting interval.
     */
    private long selectionStartTime;
    /**
     * Indicates if the currently selected interval will be added or deleted to/from the
     * list of the selected times.
     */
    private int selectionIndicator; 
    /**
     * Identifier for the display list of the selected times.
     */
    private int selectedTimesDisplayList;
    /**
     * Indicates if the time selection/deletion is ongoing.
     */
    private boolean selectionMode = false;
    /**
     * The label for the paint ticks.
     */
    private TickLabel tickLabel;
    /**
     * The label for the scale values.
     */
    private ScaleTextLabel scaleLabel;

    /**
     * Creates new panel.
     *
     * @param timeline the timeline.
     */
    public TimelineScalePanel(Timeline timeline, TimelinePanel timelinePanel) {
        super(timeline, timelinePanel);
        // create the labels
        tickLabel = new TickLabel(TickLabel.NORTH, Color.BLACK);
        scaleLabel = new ScaleTextLabel(timelinePanel, Color.BLACK);
        
        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.add(tickLabel, BorderLayout.NORTH);
        labelPanel.add(scaleLabel, BorderLayout.CENTER);
        
        // set size of the labels
        tickLabel.setPreferredSize(new Dimension(100, 6));
        scaleLabel.setPreferredSize(new Dimension(100, (int)(getFont().getSize() * 1.5)));

        // set layout
        setLayout(new GridLayout(2, 1, 2, 2));
        add(canvas);
        add(labelPanel);
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    }
    
    /**
     * Initialization of the timeline.
     *
     * @param gld needed when opengl is used. 
     */
    public void init(GLAutoDrawable gld) {
        GL2 gl = (GL2) gld.getGL();
        
        // set the background color
        Color c = this.getBackground();
        float r = c.getRed() / 255.0f;
        float g = c.getGreen() / 255.0f;
        float b = c.getBlue() / 255.0f;
        gl.glClearColor(r, g, b, 0.0f);
        
        // enable shading
        gl.glShadeModel(GL2.GL_SMOOTH);
        
        // enable blending
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        
        // set actual matrix
        gl.glMatrixMode(GL2.GL_PROJECTION);
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
        GL2 gl = (GL2) gld.getGL();
        
        // set actual matrix
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        
        // set bounding box
        glu.gluOrtho2D(xmin, xmax, ymin, ymax);
        
        // update selected times
        updateSelectedTimesDisplayList(gl);
        
        // draw the color map
        drawGraph(gl);
    }
    
    /**
     * Starts the selection/deletion of the times in the timeline. The left mouse button
     * starts the selection interval while the other two buttons start the deletion
     * interval.TimelineScalePanel scalePanel
     *
     * @param e the mouse event.
     */
    public void mousePressed(MouseEvent e) {
        Object src = e.getSource();
        // is the client passive?
        boolean passive = timeline.getClient().isConnected() && !timeline.getClient().isActive();
        // active clinet only        
        if (!passive) {
            // get x window coordinate
            int x = (int)e.getX();
            int y = (int)e.getY();
            // get the bounds of the displayed time interval
            long tstart = timelinePanel.getDisplayedStartTime();
            long tend = timelinePanel.getDisplayedEndTime();
            // start the time selection
            if (timelinePanel.timeToMilliseconds(tend - tstart) > timeline.getDT()) {
                selectionMode = true;
                // get the starting time of the interval which is to be selected
                selectionStartTime = timelinePanel.timeToMilliseconds(Math.round(convertWindowXToCurrentTime(x)));
                if (selectionStartTime <= timeline.getRelativeNextTime()) {
                    selectionStartTime = timeline.getRelativeNextTime() + timeline.getDT();
                }
                // left mouse button
                if (e.getButton() == MouseEvent.BUTTON1) {
                    selectionIndicator = TimelineConstants.SELECT;
                }
                // right or middle mouse button
                else {
                    selectionIndicator = TimelineConstants.DELETE;
                }
            }
        }
    }
        
    /**
     * Ends the selection/deletion of the times in the timeline and updates the list
     * of selected times with the new time interval.
     *
     * @param e the mouse event.
     */
    public void mouseReleased(MouseEvent e) {
        // get x window coordinate
        int x = (int)e.getX();
        int y = (int)e.getY();
        if (selectionMode) {
            selectionMode = false;
            long selectionEndTime = timelinePanel.timeToMilliseconds(Math.round(convertWindowXToCurrentTime(x)));
            if (selectionEndTime <= timeline.getRelativeNextTime()) {
                selectionEndTime = timeline.getRelativeNextTime()+timeline.getDT();
            }
            // select time interval
            selectTimeInterval(selectionIndicator, selectionStartTime, selectionEndTime);
            // redraw
            update();
        }
    }
    
    /**
     * Updates the scale values.
     */
    public void updateScaleValues() {
        scaleLabel.update(timelinePanel.getDisplayedStartTime(), timelinePanel.getDisplayedEndTime());
    }
    
    /**
     * Updates the paint ticks.
     */
    public void updateScaleTicks() {
        tickLabel.update((int)(timelinePanel.getDisplayedEndTime() - timelinePanel.getDisplayedStartTime()));
    }

    /**
      * Updates the display list of the selected times.
      */
    private synchronized void updateSelectedTimesDisplayList(GL gl2) {
        GL2 gl = (GL2) gl2;
        // get the display list 
        selectedTimesDisplayList = (gl.glIsList(selectedTimesDisplayList)) ? selectedTimesDisplayList: gl.glGenLists(1);
        // the compiled display list
        gl.glNewList(selectedTimesDisplayList, GL2.GL_COMPILE);
        // line color
        gl.glColor4f(0.9f, 0.1f, 0.1f, 0.5f);
        // width of the lines
        gl.glLineWidth(1.0f);
        // get all selected times 
        Vector[] lists = new Vector[3];
        lists[0] = timeline.getExecutedTimes();
        lists[1] = timeline.getUnexecutedTimes();
        lists[2] = timeline.getProcessedTimes();
        // go through the lists
        for (int n = 0; n < lists.length; n++) {
            // different color for the processed times
            if (n == 2) {
                // different line color
                gl.glColor4f(0.9f, 0.6f, 0.1f, 0.5f);
            }
            for (int i = 0; i < lists[n].size(); i++) {
                TimeInterval ti = (TimeInterval)lists[n].get(i);
                long t1 = Math.max(ti.getStartTime(), timelinePanel.getDisplayedStartTimeInMsec());
                long t2 = Math.min(ti.getEndTime(), timelinePanel.getDisplayedEndTimeInMsec());
                long t1coord = convertCurrentTimeToProjectedX(timelinePanel.millisecondsToTimeUnit(t1));
                long t2coord = convertCurrentTimeToProjectedX(timelinePanel.millisecondsToTimeUnit(t2));
                if (dt2dx(ti.getTimeStep()) <= 1.0 && dt2dx(t2 - t1) > 1.0) {
                    gl.glBegin(GL2.GL_POLYGON);
                    gl.glVertex2d(t1coord, ymin);
                    gl.glVertex2d(t2coord, ymin);
                    gl.glVertex2d(t2coord, ymax);
                    gl.glVertex2d(t1coord, ymax);
                    gl.glEnd(); 
                }
                else if (dt2dx(ti.getTimeStep()) <= 1.0 && dt2dx(t2 - t1) <= 1.0) {
                    gl.glBegin(GL2.GL_LINES);
                    gl.glVertex2d(t1coord, ymin);
                    gl.glVertex2d(t1coord, ymax);
                    gl.glEnd(); 
                }
                else if (ti.getAllTimes(t1, t2) != null){
                    long[] times = ti.getAllTimes(t1, t2);
                    for ( int j = 0; j < times.length; j++) {
                        double t = timelinePanel.millisecondsToTimeUnit(times[j]);
                        int xdisp = convertCurrentTimeToProjectedX(t);
                        // adjust the max displayed time
                        if (xdisp == xmax) {
                            xdisp -= xmax / this.getWidth();
                        }
                        gl.glBegin(GL2.GL_LINES);
                        gl.glVertex2d(xdisp, ymin);
                        gl.glVertex2d(xdisp, ymax);
                        gl.glEnd();
                    }
                }
            }
        }
        // ends the display list
        gl.glEndList();
    }
  
    /**
     * Draw all the graphic elements.
     */
    protected void drawGraph(GL gl2) {
        GL2 gl = (GL2) gl2;
        // clear the window
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
        
        // draw the timeline bar borders
        gl.glColor3f(0.6f, 0.8f, 1.0f);
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex2d(xmin, ymin);
        gl.glVertex2d(xmax, ymin);
        gl.glVertex2d(xmax, ymax);
        gl.glVertex2d(xmin, ymax);
        gl.glEnd();

        // mark the selected times
        gl.glCallList(selectedTimesDisplayList);
        
        // display current simulation time
        long t2x = convertCurrentTimeToProjectedX(timelinePanel.getDisplayedStartTime());
        long ct2x = convertCurrentTimeToProjectedX(timelinePanel.millisecondsToTimeUnit(timeline.getRelativeCurrentTime()));
        if (t2x <= ct2x) {
            gl.glColor4f(0.9f, 0.7f, 0.4f, 0.5f);
            gl.glBegin(GL2.GL_POLYGON);
            gl.glVertex2d(t2x, ymin);
            gl.glVertex2d(Math.max(ct2x, t2x), ymin);
            gl.glVertex2d(Math.max(ct2x, t2x), ymax);
            gl.glVertex2d(t2x, ymax);
            gl.glEnd();
        }
        
        // the time selection ongoing
        if (selectionMode) {
            if (selectionIndicator == TimelineConstants.SELECT) {
                gl.glColor4f(0.9f, 0.1f, 0.1f, 0.5f);
            }
            else {
                gl.glColor4f(0.9f, 0.9f, 0.9f, 0.5f);
            }
            displayInterval(gl);
        }
    }
    
    /**
     * Displays the currently selected or deleted interval in the timeline.
     *
     * @param start_time start time of the selected/deleted interval in milliseconds.
     */
    private void displayInterval(GL gl2) {
        GL2 gl = (GL2) gl2;
        long startTime = Math.min(selectionStartTime, 
                                  timelinePanel.timeToMilliseconds(convertProjectedXToCurrentTime(currentCursorProjectedPos)));
        long endTime = Math.max(selectionStartTime, 
                                timelinePanel.timeToMilliseconds(convertProjectedXToCurrentTime(currentCursorProjectedPos)));
        // adjust start and end times according to the time step and the simulation start time
        if (startTime % timeline.getDT() != 0) {
            startTime = (long)Math.round(startTime * 1.0 / timeline.getDT()) * timeline.getDT();
        }
        endTime = (long)Math.round(endTime * 1.0 / timeline.getDT()) * timeline.getDT();
        // display the interval
        long tempX = -1;
        while (startTime <= endTime) {
            if (startTime > timeline.getRelativeNextTime() && startTime >= timelinePanel.getDisplayedStartTimeInMsec() && 
                startTime <= timelinePanel.getDisplayedEndTimeInMsec()) {
                long actualX = convertCurrentTimeToProjectedX(timelinePanel.millisecondsToTimeUnit(startTime));
                if (actualX != tempX) {
                    gl.glBegin(GL2.GL_LINES);
                    gl.glVertex2d(actualX, ymin);
                    gl.glVertex2d(actualX, ymax);
                    gl.glEnd();
                    tempX = actualX;
                }
            }
            startTime += timeline.getDT();
        }
    }

    /**
     * This method selects a time interval in the timeline. Depending on the indicator
     * the interval can be "selected" ie. used in the simulation or "deleted" ie. not 
     * used in the simulation.
     *
     * @param indicator     this indicator can have two values ie. TimelineConstants.SELECT and TimelineConstants.DELETE.
     * @param startSelected the start time of the interval in milliseconds.
     * @param endSelected   the end time of the interval in milliseconds. 
     */
    public void selectTimeInterval(int indicator, long startSelected, long endSelected) {
        // get the bounds of the displayed time interval
        long tstart = timelinePanel.getDisplayedStartTime();
        long tend   = timelinePanel.getDisplayedEndTime();
        // adjust the bounds of the selected interval to the reference time
        long startTime = (Math.min(startSelected, endSelected) < timelinePanel.timeToMilliseconds(tstart))? 
            timelinePanel.timeToMilliseconds(tstart) : Math.min(startSelected, endSelected);
        long endTime = (Math.max(startSelected, endSelected) > timelinePanel.timeToMilliseconds(tend))? 
            timelinePanel.timeToMilliseconds(tend) : Math.max(startSelected, endSelected);
        // update the list of the selected times
        long refTime = timeline.getRelativeNextTime();
        long dt = timeline.getDT();
        startTime = ((startTime - refTime) % dt != 0)? refTime + ((startTime - refTime) / dt + 1) * dt : startTime;
        endTime = ((endTime - refTime) % dt != 0)? (refTime + (endTime - refTime) / dt) * dt : endTime;
        if (endTime >= startTime) {
            timeline.updatePrenumerations(indicator, new TimeInterval(dt, startTime, endTime));
        }        
    }
    
}
    
