package StratmasClient.timeline;

import java.net.URL;
import java.nio.IntBuffer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import java.text.DecimalFormat;
import java.awt.Font;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.Insets;
import java.awt.Image;
import java.awt.ScrollPane;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Component;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.AdjustmentListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ToolTipManager;
import javax.swing.JToggleButton;
import javax.swing.JRadioButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JDialog;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.JPopupMenu;
import javax.swing.ImageIcon;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.event.AncestorListener;
import javax.swing.event.AncestorEvent;
import javax.swing.event.MouseInputListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.border.TitledBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.basic.BasicScrollBarUI; 
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLDrawableFactory;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.gl2.GLUT;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSource;
import java.awt.dnd.DnDConstants;

import StratmasClient.*;
import StratmasClient.object.*;
import StratmasClient.object.primitive.*;
import StratmasClient.object.type.TypeFactory;
import StratmasClient.map.GridLayer;
import StratmasClient.map.RenderSelection;
import StratmasClient.map.DraggableJMenuItem;
import StratmasClient.map.DraggedElement;
import StratmasClient.map.MapConstants;
import StratmasClient.filter.ActivityFilter;
import StratmasClient.filter.CombinedORFilter;
import StratmasClient.filter.CombinedFilter;
import StratmasClient.filter.SubtreeFilter;
import StratmasClient.filter.StratmasObjectFilter;
import StratmasClient.filter.MilitaryCodeFilter;
import StratmasClient.filter.TypeFilter;
import StratmasClient.treeview.TreeView;
import StratmasClient.treeview.TreeViewFrame;

/**
 * This panel displays the activities in the timeline. Each activity is represented with it's identifier, 
 * the start and end times, the resource, the affiliation  and the symbol with two arrows (at the start 
 * and end times). Both the symbol and the arrows are DnD enabled.   
 *
 * @author Amir Filipovic 
 */
public class TimelineActivityPanel extends TimelineCanvasPanel implements DragGestureListener {
    /**
     * The scroll bar used for this panel.
     */ 
    private JScrollBar scrollBar;
    /**
     * The source for drag in DnD.
     */
    private DragSource source;
    /**
     * Used in connection with DnD.
     */
    private DragGestureRecognizer recognizer;
    /**
     * The list of currently drawn activities
     */
    private Hashtable drawnActivities = new Hashtable();
    /**
     * Indicates if the list of drawn activities has to be updated.
     */
    private boolean update_activity_list = true;
    /**
     * Indicates if the render selection list has to be updated.
     */
    private boolean update_render_selection = false;
    /**
     * The activity type filter. It filters the activities with respect to resource.
     */
    private CombinedORFilter activityTypeFilter = new CombinedORFilter();
    /**
     * The activity time filter. It filters the activities with respect to time.
     */
    private CombinedORFilter activityTimeFilter = new CombinedORFilter();
    /**
     * The adapter currently updated with the mouse cursor.
     */
    private ActivityAdapter active_adapter;
    /**
     * The render selection name of the active adapter.
     */
    private int active_render_selection_name;
    /**
     * The filter indicating which StratmasObjects that are dragable (as in drag-and-drop). 
     */
    private StratmasObjectFilter drag_filter;
    /**
     * The result of the latest render selection.
     */
    private RenderSelection latest_render_selection = new RenderSelection();
    /**
     * Last horizontal mouse coordinat for last render selection.
     */
    private int render_selection_mouse_x = 0;
    /**
     * Last vertical mouse coordinat for last render selection.
     */
    private int render_selection_mouse_y = 0;
    /**
     * The counter assigning new renderSelectionNames.
     */
    private int render_selection_name_counter = 1;
    /**
     * The hashtable mapping renderSelectionNames to ActivityAdapters.
     */
    protected Hashtable render_selection_names = new Hashtable();
    /**
     * The horizontal center coordinate for render selection.
     */
    private double render_selection_x;
    /**
     * The vertical center coordinate for render selection.
     */
    private double render_selection_y;
    /**
     * The horizontal tolerance in render selection.
     */
    private double render_selection_dx = 1;
    /**
     * The vertical tolerance in render selection.
     */
    private double render_selection_dy = 1;
    /**
     * The table of activities in the timeline.
     */
    private TimelineActivityTable activityTable;
    /**
     * The first visible table row in the panel.
     */
    private int firstVisibleRow = 0; 
    /**
     * The table viewport.
     */
    private Rectangle visibleTableArea;
    /**
     * Indicates if the table viewport is changed.
     */
    private boolean visibleTableAreaChanged = false;
    /**
     * The label for the scale.
     */
    private ScaleTextLabel textLabel;
      /**
     * The label for the paint ticks.
     */
    private TickLabel tickLabel;
    /**
     * The viewport for the activity table.
     */
    private JViewport view;
 
    /**
     * Creates new panel.
     *
     * @param timeline reference to the timeline.
     */
    public TimelineActivityPanel(Timeline timeline, TimelinePanel timelinePanel) {
        super(timeline, timelinePanel);

        // initialize the area bounds
        xmin = 0;
        xmax = canvas.getWidth();
        ymin = 0;
        ymax = canvas.getHeight();
        
        // create the table of activities
        activityTable = new TimelineActivityTable(timeline);

        // add the canvas to a panel
        JPanel canvasPanel = new JPanel();
        canvasPanel.setLayout(new BorderLayout());
        canvasPanel.add(canvas, BorderLayout.CENTER);
        canvasPanel.setPreferredSize(new Dimension(0,0));
        canvasPanel.setMinimumSize(new Dimension(0,0));
        
        // create the scroll bar
        scrollBar = new JScrollBar(JScrollBar.VERTICAL, 0, 0, 0, 0);
        final TimelineActivityTable ftable = activityTable;
        final TimelineActivityPanel self = this;
        final GLCanvas fcanvas = canvas;
        scrollBar.addAdjustmentListener(new AdjustmentListener() {
                public void adjustmentValueChanged(AdjustmentEvent e) {
                    if (ftable.getRowCount() > 0) {
                        Rectangle visArea = ftable.getVisibleRect();
                        int yTrans =  scrollBar.getValue()*ftable.getRowHeight(0) - (int) visArea.getY();
                        if (visArea.getHeight() + ftable.getRowHeight(0) * scrollBar.getValue() > ftable.getHeight()) {
                            yTrans = (int) (ftable.getHeight() - (visArea.getHeight() + visArea.getY()));
                        }
                        visArea.translate(0, yTrans);
                        // update the viewport of the panel
                        self.updateViewport(visArea);
                    }
                }
            });

        // create a viewport for the table
        view = new JViewport();
        view.setBackground(TimelineConstants.LIGHTER);
        view.setView(activityTable);

        // set panel for the activity table
        activityTable.getTableHeader().setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        activityTable.getTableHeader().setMinimumSize(new Dimension(0, 25));
        activityTable.getTableHeader().setPreferredSize(new Dimension(Integer.MAX_VALUE, 25));

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(activityTable.getTableHeader(), BorderLayout.NORTH);
        tablePanel.add(view, BorderLayout.CENTER);
        
        // create the labels        
        tickLabel = new TickLabel(TickLabel.SOUTH, Color.BLUE);
        textLabel = new ScaleTextLabel(timelinePanel, Color.BLUE);
        
        JPanel scaleValues = new JPanel(new BorderLayout());
        scaleValues.add(textLabel, BorderLayout.CENTER);
        scaleValues.add(tickLabel, BorderLayout.SOUTH);

        tickLabel.setPreferredSize(new Dimension(100, 6));
        textLabel.setPreferredSize(new Dimension(100, (int)(getFont().getSize() * 1.5)));
        
        scaleValues.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        scaleValues.setMinimumSize(new Dimension(0, 25));
        scaleValues.setPreferredSize(new Dimension(Integer.MAX_VALUE, 25));
        scaleValues.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

        JPanel cPanel = new JPanel(new SpringLayout());
        cPanel.add(scaleValues);
        cPanel.add(canvasPanel);
        SpringUtilities.makeCompactGrid(cPanel, 2, 1, 0, 0, 0, 0);

        // arrange the split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablePanel, cPanel);
        splitPane.setOneTouchExpandable(false);
        splitPane.setDividerLocation(300);
        splitPane.setDividerSize(5);
        splitPane.setResizeWeight(1);
        splitPane.setBackground(TimelineConstants.LIGHTER);
        final JScrollBar fScrollBar = scrollBar;
        splitPane.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    // update the scroll bar
                    self.updateScrollBar();
                    // adjust the viewport
                    if (fScrollBar.getValue() == fScrollBar.getMaximum()) {
                        Rectangle visArea = ftable.getVisibleRect();
                        int yTrans =  (int) (ftable.getHeight() - (visArea.getY() + visArea.getHeight()));
                        visArea.translate(0, yTrans);
                        // update the viewport of the panel
                        self.updateViewport(visArea);
                    }
                } 
            });
        tablePanel.setMinimumSize(new Dimension(0, 0));
        cPanel.setMinimumSize(new Dimension(scrollBar.getWidth(), 0));

        JLabel dummyLabel = new JLabel();
        dummyLabel.setPreferredSize(new Dimension(1, 25));
        JPanel scrollBarPanel = new JPanel(new BorderLayout());
        scrollBarPanel.add(scrollBar, BorderLayout.CENTER);
        scrollBarPanel.add(dummyLabel, BorderLayout.NORTH);

        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);
        add(scrollBarPanel, BorderLayout.EAST);
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        
        // create filter for the activities 
        CombinedORFilter dfilter = new CombinedORFilter();
        dfilter.add(new TypeFilter(TypeFactory.getType("Activity"), true));
        drag_filter = dfilter;
        
        // used for drag action in DnD
        source = new DragSource();
        recognizer = source.createDefaultDragGestureRecognizer(canvas, DnDConstants.ACTION_REFERENCE, this);
        
        // used for drop action in DnD
        DropTargetListener dropTargetListener = new TimelineDropTarget(timeline);
        canvas.setDropTarget(new DropTarget(this, dropTargetListener));
    }
    
    /**
     * Initialization of the timeline.
     *
     * @param gld needed when opengl is used. 
     */
    public void init(GLAutoDrawable gld) {
        GL2 gl = (GL2) gld.getGL();
        
        // set the background color
        float r = TimelineConstants.LIGHTER.getRed()/255.0f;
        float g = TimelineConstants.LIGHTER.getGreen()/255.0f;
        float b = TimelineConstants.LIGHTER.getBlue()/255.0f;
        gld.getGL().glClearColor(r, g, b, 0.0f);
        
        // set hints
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST); 
        
        // enable smoothing for lines
        gl.glEnable(GL2.GL_LINE_SMOOTH);
        
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
        
        // update the activities 
        for (Enumeration e = drawnActivities.elements(); e.hasMoreElements(); ) {
            ((ActivityAdapter)e.nextElement()).setSymbolUpdated(false);
        }
        updateActivityDisplayLists(gld);
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
        
        // update list of displayed activities
        if (update_activity_list) {
            updateActivityDisplayLists(gld);
            update_activity_list = false;
        }
        
        // draw the color map
        drawGraph(gld.getGL());
        
        // update the render selection 
        updateRenderSelection(gld);
    }
    
    /**
     * Updates width and length of the canvas.
     *
     * @param drawable needed for OpenGL2.
     * @param x leftmost screen coordinate of the display area.
     * @param y uppermost screen coordinate of the display area.
     * @param width width of the display area.
     * @param height height of the display area.
     */
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        // update the viewport
        xmax = xmin + width;
        ymax = ymin + height;
        
        // update 
        updateActivityDisplayLists(drawable);
    }
    
    /**
     * Not implemented.
     */
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}
    
    /**
     * Updates the render selection array
     *
     * @param gld the drawable
     */
    private void updateRenderSelection(GLAutoDrawable gld)
    {
        GL2 gl = (GL2) gld.getGL();
        
        IntBuffer render_selection_buffer;
        int render_selection_buffer_allocation_size = 2048;
        
        int hits = -1;

        do {
            render_selection_buffer = Buffers.newDirectIntBuffer(render_selection_buffer_allocation_size);
            gl.glSelectBuffer(render_selection_buffer.capacity(), render_selection_buffer);
            
            // Enable render selection.
            gl.glRenderMode(GL2.GL_SELECT);
            
            // Init names.
            gl.glInitNames();
            
            // Sets the selection area.
            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glPushMatrix();
            gl.glLoadIdentity();

            glu.gluOrtho2D(render_selection_x - render_selection_dx / 2, 
                           render_selection_x + render_selection_dx / 2, 
                           render_selection_y - render_selection_dy / 2,
                           render_selection_y + render_selection_dy / 2);

            // Draw symbols.
            updateActivityDisplayLists(gld);

            // Draw activities
            for (Enumeration e = drawnActivities.elements(); e.hasMoreElements(); ) {
                    gl.glCallList(((ActivityAdapter)e.nextElement()).getActivityDisplayList());
            }
                        
            // Restore view
            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glPopMatrix();
            gl.glFlush();
            
            // End render selection mode.
            hits = gl.glRenderMode(GL2.GL_RENDER);
            
            if (hits < 0) {
                // To small selectionBuffer, try double size.
                render_selection_buffer_allocation_size = render_selection_buffer_allocation_size * 2;
            }
        } while (hits < 0);
        latest_render_selection = new RenderSelection(hits, render_selection_buffer, render_selection_names);
    }
    
    /**
     * Sets selection area for subsequent renderSelection calls.
     *
     * @param x horizontal component of center in screen coordinates.
     * @param y vertical component of center in screen coordinates.
     * @param deltax tolerance of horizontal component of center in screen coordinates.
     * @param deltay tolerance of vertical component of center in
     * screen coordinates.
     */
    private void setRenderSelectionArea(int x, int y, int deltax, int deltay) {
        this.render_selection_x = x;
        this.render_selection_y = y;
        this.render_selection_dy = deltay;
        this.render_selection_dx = deltax;

        this.render_selection_mouse_x = x;
        this.render_selection_mouse_y = y;
        
        update_render_selection = true;
        update();
    }

    /**
     * Sets selection area for subsequent renderSelection calls.
     *
     * @param x horizontal component of center in screen coordinates.
     * @param y vertical component of center in screen coordinates.
     * screen coordinates.
     */
    private void setRenderSelectionArea(int x, int y) {
        setRenderSelectionArea(x, y, 5, 5);
    }
    
    /**
     * Updates the activity table with the resources.
     *
     * @param root the ancestor of all resources.
     */
    public void updateActivityTable(StratmasObject root) {
        activityTable.updateResources(root);
    }

    /**
     * Updates the scale values.
     */
    public void updateScaleValues() {
        textLabel.update(timelinePanel.getDisplayedStartTime(), timelinePanel.getDisplayedEndTime());
    }
    
    /**
     * Updates the paint ticks.
     */
    public void updateScaleTicks() {
        tickLabel.update((int)(timelinePanel.getDisplayedEndTime() - timelinePanel.getDisplayedStartTime()));
    }
    
    /**
     * Updates the viewport of the activity panel.
     */
    public void updateViewport(Rectangle visArea) {
        // set the first visible table row in the viewport
        firstVisibleRow = (int)(visArea.getY() / activityTable.getRowHeight(0));
        // set the viewport of the activity table
        visibleTableArea = visArea;
        // idicate that the viewport is changed
        visibleTableAreaChanged = true;
        // if table displayed only
        if (canvas.getWidth() == 0 || canvas.getHeight() == 0) {
            activityTable.scrollRectToVisible(visibleTableArea);
        }
        // update the panel
        updateActivityList();
    }

    /**
     * Updates the filter of the activities with respect to time.
     */
    public void updateActivityTimeFilter(StratmasObjectFilter filter, int indicator) {
        if (indicator == TimelineConstants.ADD) {
            activityTimeFilter.add(filter);
        }
        else if (indicator == TimelineConstants.REMOVE) {
            activityTimeFilter.remove(filter); 
        }
        // 
        updateDrawnActivitiesList();
    }
    
    /**
     * Updates the filter of the activities with respect to resource.
     */
    public void updateActivityTypeFilter(StratmasObjectFilter filter, int indicator) {
        if (indicator == TimelineConstants.ADD) {
            activityTypeFilter.add(filter);
        }
        else if (indicator == TimelineConstants.REMOVE) {
            activityTypeFilter.remove(filter); 
        }
        // 
        updateDrawnActivitiesList();
    }
    
    /**
     * Updates the list of the drawn activities. The list is first cleared and then all
     * the activities are filtered with the actual filter.
     */
    public void updateDrawnActivitiesList() {
        // add all activities which pass the filter
        for (Enumeration e = timeline.getActivityAdapters().elements(); e.hasMoreElements();) {
            ActivityAdapter adapter = (ActivityAdapter)e.nextElement();
            if (activityTimeFilter.pass(adapter.getActivity()) && activityTypeFilter.pass(adapter.getActivity())) {
                if (!activityTable.contains(adapter.getActivity())) {
                    activityTable.addSortedActivity(adapter.getActivity());
                    drawnActivities.put(adapter.getActivity(), adapter);
                    //
                    updateScrollBar();
                }
            }
            else if (activityTable.contains(adapter.getActivity())) {
                activityTable.removeActivity(adapter.getActivity());
                drawnActivities.remove(adapter.getActivity());        
                //
                updateScrollBar();
            }
        }
        //
        updateActivityList();        
    }
  
    /**
     * Updates the scroll bar in the timeline.
     */
    public void updateScrollBar() {
        int nrOfActivityLines = activityTable.getRowCount();
        int height = (canvas.getHeight() > 0) ? canvas.getHeight() : view.getHeight();
        int nrOfDisplayedLines = (nrOfActivityLines > 0) ?  height / activityTable.getRowHeight() : 0;
        scrollBar.setMaximum(Math.max(0, nrOfActivityLines - nrOfDisplayedLines));
        // show the scroll bar
        final boolean visible = (nrOfActivityLines > nrOfDisplayedLines) ? true : false;
        final JScrollBar fScrollBar = scrollBar;
        SwingUtilities.invokeLater (new Runnable() {
                public void run() {
                    fScrollBar.setVisible(visible);
                }
            });
    }
    
    /**
     * Updates the list of the activities.
     */
    public void updateActivityDisplayLists(GLAutoDrawable gld) {
        for (Enumeration e = drawnActivities.elements(); e.hasMoreElements(); ) {
            ActivityAdapter adapter = (ActivityAdapter)e.nextElement();
            if (!adapter.isSymbolUpdated()) {
                adapter.updateSymbolDisplayLists(gld);
            }
            adapter.updateActivityDisplayList(this, gld.getGL());
        }
    }
    
    /**
     * Updates the list of the activities.
     */
    public void updateActivityList() {
        update_activity_list = true;
        update();
    }
    
    /**
     * Updates the list of activity adapters with a new adapter.
     *
     * @param adapter to be added to the sorted list.
     */
    public void updateActivityList(ActivityAdapter adapter, int indicator) {
        // add new activity 
        if (indicator == TimelineConstants.ADD) {
            // add new render selection names
            render_selection_names.put(new Integer(adapter.getRenderSelectionName()), adapter);
            render_selection_names.put(new Integer(adapter.getLeftArrowRenderSelectionName()), adapter);
            render_selection_names.put(new Integer(adapter.getRightArrowRenderSelectionName()), adapter);
            //filtered adapters only
            if (activityTimeFilter.pass(adapter.getActivity()) && activityTypeFilter.pass(adapter.getActivity())) {
                // add the adapter to the list
                if (!drawnActivities.contains(adapter)) {
                    drawnActivities.put(adapter.getActivity(), adapter);
                }
                // add the activity to the table
                activityTable.addActivity(adapter.getActivity());
            }
        }
        // remove the activity
        else if (indicator == TimelineConstants.REMOVE) {
            // remove the render selection names
            render_selection_names.remove(new Integer(adapter.getRenderSelectionName()));  
            render_selection_names.remove(new Integer(adapter.getLeftArrowRenderSelectionName()));  
            render_selection_names.remove(new Integer(adapter.getRightArrowRenderSelectionName()));
            // remove the adapter to the list
            if (drawnActivities.contains(adapter)) {
                drawnActivities.remove(adapter.getActivity());
            }
            // remove the activity to the table
            activityTable.removeActivity(adapter.getActivity());
        }

        //update the scroll bar
        updateScrollBar();
        
        update();
    }
    
    /**
     * Resets the panel.
     */
    public void reset() {
        // remove the displayed activities
        drawnActivities.clear();
        activityTable.reset();

        // redraw
        update();
    }
    
    /**
     * Removes the panel.
     */
    public void remove() {
        super.remove();
        drawnActivities.clear();
        render_selection_names.clear();
        activityTable.remove();
    }
    
    /**
     * Returns the window x-coordinate of the timeline panel for a given time in milliseconds. 
     * The given time is counted from 1.1.1970.
     */
    public int getXCoordinate(long msecs) {
        // get the bounds of the displayed interval
        long tstart = timelinePanel.getDisplayedStartTime();
        long tend = timelinePanel.getDisplayedEndTime();
        return (int)convertCurrentTimeToProjectedX(timelinePanel.millisecondsToTimeUnit(msecs - timeline.getSimStartTime()));
    }
    
    /**
     * Retuns the y-coordinate of the timeline panel where the adapter will be displayed.
     */
    public int getYCoordinate(ActivityAdapter adapter) {
        // get y-coordinate according to the Java's coordinate system
        int rowNr = activityTable.getRow(adapter.getActivity());
        if (rowNr >= 0) {
            int javaY = (int) (rowNr * activityTable.getRowHeight(rowNr) + activityTable.getRowHeight(rowNr) / 2 
                               - visibleTableArea.getY()); 
            // convert to the OpenGL coordinate system 
            return ymax - javaY;
        }
        return -1;
    }
    
    /**
     * Returns the number of the displayed activities ie. the number of the visible rows in the 
     * activity table. Also the rows not completely visible are counted.
     */
    public int getNrOfDisplayedActivities() {
        // all activities are displayed
        if (view.getHeight() > activityTable.getHeight()) {
            return activityTable.getRowCount();
        }
        // the activities currenly visible
        else {
            return view.getHeight()/activityTable.getRowHeight(0) + 1;
        }
    }
    
    /**
     * Draw all the graphic elements.
     */
    protected void drawGraph(GL gl2) {
        GL2 gl = (GL2) gl2;
        // clear the window
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);

        // adjust drawing of the activities in the timeline canvas - from the top or from the bottom
        boolean rev  = (scrollBar.getValue() == scrollBar.getMaximum() && view.getHeight() < activityTable.getHeight()) ? 
            true : false;
        
        // draw the activities and the valid background for each activity
        int startRow = (rev) ? activityTable.getRowCount() - 1 : firstVisibleRow;
        int endRow   = (rev) ? startRow - getNrOfDisplayedActivities() + 1 : startRow + getNrOfDisplayedActivities() - 1; 
        int y1       = (rev) ? canvas.getHeight() - activityTable.getRowHeight(startRow): 0;
        int i        = startRow;
        boolean cont = (!drawnActivities.isEmpty()) ? true : false;
        while (cont) {
            int y2 = y1 + activityTable.getRowHeight(i);
            // draw the background
            Color col = activityTable.getBackground(i);
            gl.glColor3f(col.getRed() / 255f, col.getGreen() / 255f, col.getBlue() / 255f);
            gl.glBegin(GL2.GL_QUADS);
            gl.glVertex2d(xmin, ymax - y1);
            gl.glVertex2d(xmax, ymax - y1);
            gl.glVertex2d(xmax, ymax - y2);
            gl.glVertex2d(xmin, ymax - y2);
            gl.glEnd();
            // draw the activity
            StratmasObject activity = activityTable.getActivity(i);
            if (activity != null) {
                gl.glCallList(((ActivityAdapter)drawnActivities.get(activity)).getActivityDisplayList());
            }
            //
            i    = (rev) ? i - 1 : i + 1;
            y1   = (rev) ? y1 - activityTable.getRowHeight(i) : y2;
            cont = (rev && i < endRow) ? false : (!rev && i > endRow) ? false : true; 
        }
        
        // update the viewport of the activity table
        if (visibleTableAreaChanged) {
            visibleTableAreaChanged = false;
            activityTable.scrollRectToVisible(visibleTableArea);
        }
        
        // display current simulation time
        long t2x = convertCurrentTimeToProjectedX(timelinePanel.getDisplayedStartTime());
        long ct2x = convertCurrentTimeToProjectedX(timelinePanel.millisecondsToTimeUnit(timeline.getRelativeCurrentTime()));
        if (t2x <= ct2x) {
            gl.glColor4f(0.9f, 0.7f, 0.4f, 0.3f);
            gl.glBegin(GL2.GL_POLYGON);
            gl.glVertex2d(t2x, ymin);
            gl.glVertex2d(Math.max(ct2x, t2x), ymin);
            gl.glVertex2d(Math.max(ct2x, t2x), ymax);
            gl.glVertex2d(t2x, ymax);
            gl.glEnd();
        }
    }
    
    /**
     * Defines the actions fired when the mouse is clicked.
     * Double clicking with the left mouse button over an activity opens an infomation window about it.
     *
     * @param e the mouse event.
     */
    public void mouseClicked(MouseEvent e) {
        // get source
        Object src = e.getSource();
        int x = (int)e.getX();
        int y = (int)e.getY();
        // left mouse button
        if (e.getButton() == MouseEvent.BUTTON1) {
            // show the information for the chosen activity
            if (e.getClickCount() == 2 && !elementsUnderCursor().isEmpty()) {
                StratmasObject activity = getActivityUnderCursor();
                // if an activity is found
                if (activity != null) {
                    final TreeViewFrame frame = TreeView.getDefaultFrame(activity);
                    frame.setEditable(true);
                    javax.swing.SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                frame.setVisible(true);
                            }
                        });
                }
            }
        }
    }
    
    /**
     * If the cursor is above an activity arrow, the updating of the activity is initialized.
     *
     * @param e the mouse event.
     */
    public void mousePressed(MouseEvent e) {
        Object src = e.getSource();
        // check if the client is passive
        boolean passive = timeline.getClient().isConnected() && !timeline.getClient().isActive();
        // only if the client is active
        if (src.equals(canvas) && !passive) {
            // get x window coordinate
            int x = (int)e.getX();
            int y = (int)e.getY();
            // get the bounds of the displayed time interval
            long tstart = timelinePanel.getDisplayedStartTime();
            long tend = timelinePanel.getDisplayedEndTime();
            // update render selection list
            setRenderSelectionArea(x, ymax - y);
            // initialize updating of the activity time interval
            if (!elementsUnderCursor().isEmpty()) {
                // check if no activity symbols are under the cursor otherwise abort the action
                boolean validAction = (getActivityUnderCursor() == null);
                // if no activity symbols are under the cursor but an arrow
                if (validAction) {
                    // left arrow
                    if (getLeftArrowUnderCursor() != null) {
                        ActivityAdapter tmpAdapter = (ActivityAdapter)drawnActivities.get(getLeftArrowUnderCursor());
                        if (tmpAdapter.getLeftArrowPointedTime() >= timeline.getCurrentTime()) {
                            active_adapter = tmpAdapter;
                            active_render_selection_name = active_adapter.getLeftArrowRenderSelectionName();
                        }
                    }
                    // right arrow
                    else if (getRightArrowUnderCursor() != null) {
                        ActivityAdapter tmpAdapter = (ActivityAdapter)drawnActivities.get(getRightArrowUnderCursor());
                        if (tmpAdapter.getRightArrowPointedTime() >= timeline.getCurrentTime()) {
                            active_adapter = tmpAdapter;
                            active_render_selection_name = active_adapter.getRightArrowRenderSelectionName();
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Ends the updating of the start/end time of the activity currently under the cursor.
     *
     * @param e the mouse event.
     */
    public void mouseReleased(MouseEvent e) {
        // get x window coordinate
        int x = (int)e.getX();
        int y = (int)e.getY();
        // ends updating of the activity under the cursor
        if (active_adapter != null) {
            active_adapter.updateActivityTimes();
            active_adapter = null;
        }
        
    }
    
    /**
     * Updates the start/end time of the activity currently under the cursor.
     *
     * @param e the mouse event.
     */
    public void mouseDragged(MouseEvent e) {
        Object src = e.getSource();
        // check if the client is pasive
        boolean passive = timeline.getClient().isConnected() && !timeline.getClient().isActive();
        // if the client is active
        if (src.equals(canvas) && !passive) {
            // get x window coordinate
            int x = (int)Math.round(e.getX());
            int y = (int)Math.round(e.getY());
            // update render selection list
            setRenderSelectionArea(x, ymax - y);
            // updates the activity under the cursor
            if (active_adapter != null && active_adapter.getActivity().getChild("end") != null) {
                // updates the start time of the activity
                if (active_adapter.isLeftArrowRenderSelectionName(active_render_selection_name)) {
                    long t = timelinePanel.timeToMilliseconds(convertWindowXToCurrentTime(x)) + timeline.getSimStartTime();
                    if (t < active_adapter.getRightArrowPointedTime() && t >= timeline.getCurrentTime()) {
                        active_adapter.setLeftArrowPointedTime(t);
                    }
                }
                // updates the end time of the activity
                else if (active_adapter.isRightArrowRenderSelectionName(active_render_selection_name)) {
                    long t = timelinePanel.timeToMilliseconds(convertWindowXToCurrentTime(x)) + timeline.getSimStartTime();
                    if (t > active_adapter.getLeftArrowPointedTime() && t >= timeline.getCurrentTime()) {
                        active_adapter.setRightArrowPointedTime(t);
                    }
                }
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
            // update render selection list
            setRenderSelectionArea(x, ymax - y);
        }
    }
    
    /**
     * Drag gesture handler.
     */
    public void dragGestureRecognized(final DragGestureEvent dge)
    {
        // check if the client is passive
        boolean passive = timeline.getClient().isConnected() && !timeline.getClient().isActive();
        // dragging enabled only for active client 
        if (!passive) {
            // get the location
            int x = (int)(dge.getDragOrigin().getX());
            int y = (int)(dge.getDragOrigin().getY());
            // get the selected element
            StratmasObject selectedActivity = getActivityUnderCursor();
            // check if the activity can be dragged
            boolean draggable = false;
            if (selectedActivity != null && drag_filter.pass(selectedActivity)) {
                boolean startOk = ((StratmasTimestamp)selectedActivity.getChild("start")).getValue().getMilliSecs() >= 
                    timeline.getCurrentTime();
                boolean endOk = (StratmasTimestamp)selectedActivity.getChild("end") == null || 
                    ((StratmasTimestamp)selectedActivity.getChild("end")).getValue().getMilliSecs() >= timeline.getCurrentTime();
                draggable = startOk && endOk;
            }
            if (draggable) {
                // define cursor for the object 
                Cursor c;
                Toolkit tk = Toolkit.getDefaultToolkit();
                Image image = ((Icon) selectedActivity.getIcon()).getImage();
                Dimension bestsize = tk.getBestCursorSize(image.getWidth(null), image.getHeight(null));
                if (bestsize.width != 0) {
                    c = tk.createCustomCursor(image, new java.awt.Point(bestsize.width/2,  bestsize.height/2), 
                                              selectedActivity.toString());
                }
                else {
                    c = Cursor.getDefaultCursor();
                }
                // set the dragged element
                DraggedElement.setElement(selectedActivity);
                // start the drag
                source.startDrag(dge, c, selectedActivity, new DragSourceAdapter(){});
            }
        }
    }
    
    /**
     * Returns a list of all Activities presently drawn under the cursor. 
     * NB the objects has to be rendered to be returned by this function.
     *
     * @return hashtable with all Activities presently drawn under the cursor. The
     *         render selection names are used as keys.
     */
    public Hashtable elementsUnderCursor()
    {
        Hashtable res = new Hashtable();
        Vector selected_objects = latest_render_selection.getTopSelectionObjects();
        int[] selected_names = latest_render_selection.getTopSelectionNames();
        for(int i = 0; i < selected_objects.size(); i++) {
            Object o = selected_objects.get(i);
            if (o instanceof ActivityAdapter) {
                res.put(new Integer(selected_names[i]), o);
            }
        }
        return res;
    }
    
    /**
     * Returns the activity if it's left arrow is currently under the mouse cursor. If there's
     * no such activity null is returned.
     */
    public StratmasObject getLeftArrowUnderCursor() {
        for (Enumeration e = elementsUnderCursor().keys(); e.hasMoreElements(); ) {
            Integer key = (Integer) e.nextElement();
            ActivityAdapter adapter = (ActivityAdapter) elementsUnderCursor().get(key);
            int renderSelectionName = key.intValue();
            if (adapter.isLeftArrowRenderSelectionName(renderSelectionName)) {
                return adapter.getStratmasObject();
            }
        }
        return null;
    }
    
    /**
     * Returns the activity if it's right arrow is currently under the mouse cursor. If there's
     * no such activity null is returned.
     */
    public StratmasObject getRightArrowUnderCursor() {
        for (Enumeration e = elementsUnderCursor().keys(); e.hasMoreElements(); ) {
            Integer key = (Integer) e.nextElement();
            ActivityAdapter adapter = (ActivityAdapter) elementsUnderCursor().get(key);
            int renderSelectionName = key.intValue();
            if (adapter.isRightArrowRenderSelectionName(renderSelectionName)) {
                return adapter.getStratmasObject();
            }
        }
        return null;
    }
    
    /**
     * Returns the activity if it's symbol is currently under the mouse cursor. If there's
     * no such activity null is returned.
     */
    public StratmasObject getActivityUnderCursor() {
        for (Enumeration e = elementsUnderCursor().keys(); e.hasMoreElements(); ) {
            Integer key = (Integer) e.nextElement();
            ActivityAdapter adapter = (ActivityAdapter) elementsUnderCursor().get(key);
            int renderSelectionName = key.intValue();
            if (adapter.isRenderSelectionName(renderSelectionName)) {
                return adapter.getStratmasObject();
            }
        }
        return null;
    }
    
}

