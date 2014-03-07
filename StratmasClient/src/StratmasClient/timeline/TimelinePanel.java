package StratmasClient.timeline;

import java.text.SimpleDateFormat;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.BorderLayout;
import javax.swing.JSlider;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;
import javax.swing.SwingUtilities;
import StratmasClient.object.StratmasObject;
import StratmasClient.filter.StratmasObjectFilter;

/**
 * The panel for the timeline.
 */
public class TimelinePanel extends JPanel {
    /** 
     * The start time of the currently visible part of the timeline bar.
     */
    protected long tstart;
    /** 
     * The end time of the currently visible part of the timeline bar.
     */
    protected long tend;
    /**
     * The time bound of the timeline (expressed in days). 
     */
    protected long maxTimeBound = 100000; 
    /**
     * The currenly used time unit.
     */
    protected int currentTimeUnit;
    /**
     * Reference to the timeline.
     */
    protected Timeline timeline;
    /**
     * The date format used.
     */
    public static SimpleDateFormat dateFormat; 
    /**
     * Reference to the panel which displays the activities in the timeline.
     */
    private TimelineActivityPanel timelineActivityPanel;
    /**
     * Reference to the panel which displays the selected times and the timeline scale.
     */
    private TimelineScalePanel timelineScalePanel;
    /**
     * The creator of various timeline components.
     */
    private TimelineDisplayControl displayControl;
       
    /**
     * Creates new panel.
     *
     * @param timeline the timeline.
     */
    public TimelinePanel(Timeline timeline) {
        // set reference to the timeline
        this.timeline = timeline;
        
        // initialize the displayed time interval
        this.tstart = 0;
        this.tend = 100;
        
        // date format 
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS Z");
        dateFormat.setLenient(true);

        // initial time unit
        updateTimeUnit();
        
        // create the panels
        timelineActivityPanel = new TimelineActivityPanel(timeline, this);
        timelineScalePanel = new TimelineScalePanel(timeline, this);
        displayControl = new TimelineDisplayControl(timeline);
        
        // compose the panels
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(displayControl.getTimelineTools(this), BorderLayout.NORTH);
        northPanel.add(timelineActivityPanel, BorderLayout.CENTER);
        northPanel.add(displayControl.getTimelineSliderPanel(this), BorderLayout.SOUTH);
        JPanel tlinePanel = new JPanel(new BorderLayout());
        tlinePanel.add(northPanel, BorderLayout.CENTER);
            tlinePanel.add(timelineScalePanel, BorderLayout.SOUTH);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Timeline", tlinePanel);
        tabbedPane.add("Options", displayControl.getOptionsPanel(this));
        
        // 
        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
        
        // used when mixing awt and swing components
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    }
    
    /**
     * Creates the GUI and shows it. 
     */
    private void createAndShowGUI() {
        // create and set up the window
        JFrame frame = new JFrame(" Stratmas Timeline");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        // used when mixing awt and swing components
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        
        // frame size (test adapted for now on)
        int frame_width = 600;
        int frame_height = 400;
        Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(frame_width, frame_height); 
        frame.setLocation(screen_size.width-frame_width, screen_size.height-frame_height-screen_size.height/20);
        
        // set up the content pane
        setOpaque(true);
        frame.getContentPane().add(this);
        
        // display the window
        frame.setSize(frame_width, frame_height);
        frame.setResizable(true);
        
        // thread safety recommendation
        final JFrame fframe = frame;
        SwingUtilities.invokeLater (new Runnable() {
                public void run() {
                    fframe.setVisible(true);
                }
            });
    }
    
    /**
     * Returns the timeline activity panel.
     */
    public TimelineActivityPanel getTimelineActivityPanel() {
        return timelineActivityPanel;
    }
    
    /**
     * Returns the timeline scale panel.
     */
    public TimelineScalePanel getTimelineScalePanel() {
        return timelineScalePanel;
    }
    
    /**
     * Redraw the timeline.
     */
    public void update() {
        validate();
        repaint();
    }
    
    /**
     * Used to select/unselect all times in the timeline.
     */
    public void selectAllTimes(boolean select) {
        displayControl.selectAllTimes(select);        
    }

    /**
     * Updates the panels according to the current time.
     */
    public void updateCurrentTime() {
        updateScalePanel();
        updateDrawnActivities();
        updateInfoDialog();
        if (timeline.getRelativeCurrentTime() > getDisplayedEndTimeInMsec()) {
            // update the timeline view
            JSlider slider = displayControl.getTimelineSlider();
            slider.setValue(slider.getValue() + 1);
        }
    }
    
    /**
     * Updates the timeline scale panel.
     */
    public void updateScalePanel() {
        timelineScalePanel.update();
    }
    
    /**
     * Updates the information dialog.
     */
    public void updateInfoDialog() {
        displayControl.updateDialogInfo();
    }
    
    /**
     * Updates the activities drawn in the timeline.
     */
    public void updateDrawnActivities() {
        timelineActivityPanel.updateDrawnActivitiesList();
    }
    
    /**
     * Updates the list of activities.
     */
    public void updateActivityList(ActivityAdapter adapter, int indicator) {
        timelineActivityPanel.updateActivityList(adapter, indicator);
    }
    
    /**
     * Updates the list of activities.
     */
    public void updateActivityList() {
        timelineActivityPanel.updateActivityList();
    }
    
    /**
     * Updates the activity table.
     */
    public void updateActivityTable(StratmasObject node) {
        timelineActivityPanel.updateActivityTable(node);
    }

    /**
     * Updates the activity type filter.
     */
    public void updateActivityTypeFilter(StratmasObjectFilter filter, int indicator) {
        timelineActivityPanel.updateActivityTypeFilter(filter, indicator);
    }
    
    /**
     * Updates the activity time filter.
     */
    public void updateActivityTimeFilter(StratmasObjectFilter filter, int indicator) {
        timelineActivityPanel.updateActivityTimeFilter(filter, indicator);
    }
        
    /**
     * Resets the panel.
     */
    public void reset() {
        // update the timeline view
        displayControl.getTimelineSlider().setValue(0);
        
        // reset the activity panel
        if (timelineActivityPanel == null) {
            timelineActivityPanel = new TimelineActivityPanel(timeline, this);
        }
        else {
            timelineActivityPanel.reset();
        }
        
        // reset the scale panel
        if (timelineScalePanel == null) {
            timelineScalePanel = new TimelineScalePanel(timeline, this);
        }
        else {
            timelineScalePanel.reset();
        }
                
        // redraw
        update();
    }
    
    /**
     * Removes the panel.
     */
    public void remove() {
        timeline = null;
        displayControl.remove();
        timelineScalePanel.remove();
        timelineActivityPanel.remove();
    }

    /**
     * Returns the start time displayed in the timeline panel.
     */
    public long getDisplayedStartTime() {
        return tstart;
    }
    
    /**
     * Returns the start time displayed in the timeline panel in milliseconds.
     */
    public long getDisplayedStartTimeInMsec() {
        return timeToMilliseconds(tstart);
    }
    
    /**
     * Returns the end time displayed in the timeline panel.
     */
    public long getDisplayedEndTime() {
        return tend;
    }
    
    /**
     * Returns the end time displayed in the timeline panel in milliseconds.
     */
    public long getDisplayedEndTimeInMsec() {
        return timeToMilliseconds(tend);
    }
    
    /**
     * Sets the start time displayed in the timeline panel.
     *
     * @param time the relative start time expressed in the actual time unit.
     */
    public void setDisplayedStartTime(long time) {
        tstart = time;
    }
    
    /**
     * Sets the end time displayed in the timeline panel.
     *
     * @param time the relative end time expressed in the actual time unit.
     */
    public void setDisplayedEndTime(long time) {
        tend = time;
    }
    
    /**
     * Sets the displayed time and updates the panel.
     */
    public void setDisplayedTimes(long startTime, long endTime) {
        setDisplayedStartTime(startTime);
        setDisplayedEndTime(endTime);
        //
        timelineActivityPanel.updateScaleValues();
        timelineActivityPanel.updateActivityList();
        timelineScalePanel.updateScaleValues();
        timelineScalePanel.update();
    }
    
    /**
     * Converts the time counted according to the actual time unit
     * to the milliseconds counted relative to the simulation start time.
     *
     * @param t time expressed in the actual time unit.
     *
     * @return time expressed in milliseconds.
     */
    public long timeToMilliseconds(double t) {
        if (currentTimeUnit == TimelineConstants.DAY) {
            return (long)(t * 24 * 60 * 60 * 1000);
        }
        else if (currentTimeUnit == TimelineConstants.HOUR) {
            return (long)(t * 60 * 60 * 1000);
        }
        else if (currentTimeUnit == TimelineConstants.MINUTE) {
            return (long)(t * 60 * 1000);
        }
        else {
            return (long)(t * 1000);
        }
    }

    /**
     * Converts milliseconds to the actual time unit. The milliseconds
     * are counted relative to the simulation start time.
     *
     * @param msec time expressed in milliseconds.
     *
     * @return time expressed in the current time unit.
     */
    public double millisecondsToTimeUnit(long msec) {
        if (currentTimeUnit == TimelineConstants.DAY) {
            return msec * 1.0 / (24 * 60 * 60 * 1000);
        }
        else if (currentTimeUnit == TimelineConstants.HOUR) {
            return msec * 1.0 / (60 * 60 * 1000);
        }
        else if (currentTimeUnit == TimelineConstants.MINUTE) {
            return msec * 1.0 / (60 * 1000);
        }
        else {
            return msec * 1.0 / 1000;
        }
    }

    /**
     * Converts time in milliseconds to a <code>String</code>.
     *
     * @param time the time in milliseconds.
     * @param format format of the output <code>String</code>. The format can be compact 
     *               (<code>TimelinePanel.COMPACT</code>) and long (<code>TimelinePanel.LONG</code>).
     *
     * @return a <code>String</code> representing the time.
     */
    public static String millisecondsToString(long time, int format)
    {
        int milliseconds = (int)(time % 1000);
        int seconds = (int)((time / 1000) % 60);
        int minutes = (int)((time / 60000) % 60);
        int hours = (int)((time / 3600000) % 24);
        int days = (int)((time / 3600000)/24);
        // compact format
        if (format == TimelineConstants.COMPACT) {
            String seconds_str = (seconds == 0)? "0s" : seconds+"s";
            String minutes_str = (minutes == 0)? "0m" : minutes+"m";
            String hours_str = (hours == 0)? "0h" : hours+"h";
            String days_str = (days == 0)? "0d" : days+"d";
            return new String(days_str+":"+hours_str+":"+minutes_str+":"+seconds_str);
        }
        // long format
        else {
            String seconds_str = (seconds == 0)? "" : 
                ((seconds > 1)? seconds+" secs " : seconds+" sec ");
            String minutes_str = (minutes == 0)? "" : 
                ((minutes > 1)? minutes+" mins " : minutes+" min ") ;
            String hours_str = (hours == 0)? "" : ((hours > 1)? hours+" hours " : hours+" hour ") ;
            String days_str = (days == 0)? "" : ((days > 1)? days+" days " : days+" day ");
            return new String(days_str+""+hours_str+""+minutes_str+""+seconds_str);
        }
    }
    
    /**
     * Converts a time unit to a string.
     */
    public String getTimeUnitAsString() {
        if (currentTimeUnit == TimelineConstants.DAY) {
            return "Day";
        }
        else if (currentTimeUnit == TimelineConstants.HOUR) {
            return "Hour";
        }
        else if (currentTimeUnit == TimelineConstants.MINUTE) {
            return "Minute";
        }
        else if (currentTimeUnit == TimelineConstants.SECOND) {
            return "Second";
        }
        else {
            return null;
        }
    }
    
    /**
     * Updates the time unit.
     */
    public void updateTimeUnit() {
        long dt = timeline.getDT();
        int seconds = (int)((dt / 1000) % 60);
        int minutes = (int)((dt / 60000) % 60);
        int hours = (int)((dt / 3600000) % 24);
        int days = (int)((dt / 3600000)/24);
        if (days > 0) {
            currentTimeUnit = TimelineConstants.DAY;
        }
        else if (hours > 0) {
            currentTimeUnit = TimelineConstants.HOUR;
        }
        else if (minutes > 0) {
            currentTimeUnit = TimelineConstants.MINUTE;
        }
        else {
            currentTimeUnit = TimelineConstants.SECOND;
        }
    }
    
    /**
     * Uses for zooming purposes. This method is called when zoom_in button is pressed.
     */
    public void zoomInScale() {
        long tstart = getDisplayedStartTime();
        long tend = getDisplayedEndTime();
        long middle = (tend + tstart) / 2;
        long interval = tend - tstart;
        if (interval > 100 || (interval > 10 && !(getCurrentTimeUnit() == TimelineConstants.SECOND))) {
            interval = (tend - tstart) / 10;
            long sTime = middle - interval / 2;
            long maxStartTime = getMaxBoundInCurrentTimeUnit() - interval;
            sTime = (sTime < 0)? 0 : ((sTime > maxStartTime)? maxStartTime : sTime);
            setDisplayedStartTime(sTime);
            setDisplayedEndTime(getDisplayedStartTime() + interval);
        }
        else if (!(getCurrentTimeUnit() == TimelineConstants.SECOND)){
            setSmallerTimeUnit();
            interval = 100;
            long tu = (getCurrentTimeUnit() == TimelineConstants.HOUR)? 24 : 60;
            long sTime = middle * tu - interval / 2;
            long maxStartTime = getMaxBoundInCurrentTimeUnit() - interval;
            sTime = (sTime < 0)? 0 : ((sTime > maxStartTime)? maxStartTime : sTime);
            setDisplayedStartTime(sTime);
            setDisplayedEndTime(getDisplayedStartTime() + interval);
        }
        // update the panels
        timelineScalePanel.updateScaleValues();
        timelineScalePanel.updateScaleTicks();
        timelineScalePanel.update();
        timelineActivityPanel.updateScaleValues();
        timelineActivityPanel.updateScaleTicks();
        timelineActivityPanel.updateActivityList();
    }

    /**
     *  Uses for zooming purposes. This method is called when zoom_out button is pressed.
     */
    public void zoomOutScale() {
        long tstart = getDisplayedStartTime();
        long tend = getDisplayedEndTime();
        long middle = (tend + tstart) / 2;
        long interval = tend - tstart;
        if (interval <= 10 || (getCurrentTimeUnit() == TimelineConstants.DAY && interval < maxTimeBound)) {
            interval = interval * 10;
            long sTime = middle - interval / 2;
            long maxStartTime = getMaxBoundInCurrentTimeUnit() - interval;
            sTime = (sTime < 0)? 0 : ((sTime > maxStartTime)? maxStartTime : sTime);
            setDisplayedStartTime(sTime);
            setDisplayedEndTime(getDisplayedStartTime() + interval);
        }
        else if (!(getCurrentTimeUnit() == TimelineConstants.DAY)){
            setLargerTimeUnit();
            interval = 10;
            long tu = (getCurrentTimeUnit() == TimelineConstants.DAY)? 24 : 60;
            long sTime = middle / tu - interval / 2;
            long maxStartTime = getMaxBoundInCurrentTimeUnit() - interval;
            sTime = (sTime < 0)? 0 : ((sTime > maxStartTime)? maxStartTime : sTime);
            setDisplayedStartTime(sTime);
            setDisplayedEndTime(getDisplayedStartTime() + interval);
        }
        // update the panels
        timelineScalePanel.updateScaleValues();
        timelineScalePanel.updateScaleTicks();
        timelineScalePanel.update();
        timelineActivityPanel.updateScaleValues();
        timelineActivityPanel.updateScaleTicks();
        timelineActivityPanel.updateActivityList();
    }
    
    /**
     * Returns the time unit currently used to display the times in the timeline. 
     */
    public int getCurrentTimeUnit() {
        return currentTimeUnit;
    }
    
    /**
     * Returns the maximum time bound expressed in the current time unit.
     */
    public long getMaxBoundInCurrentTimeUnit() {
        if (currentTimeUnit == TimelineConstants.DAY) {
            return maxTimeBound;
        }
        else if (currentTimeUnit == TimelineConstants.HOUR) {
            return maxTimeBound * 24;
        }
        else if (currentTimeUnit == TimelineConstants.MINUTE) {
            return maxTimeBound * 24 * 60;
        }
        else {
            return maxTimeBound * 24 * 60 * 60;
        }
    }
    
    /**
     * Changes the currently displayed time unit to the smaller one.
     */
    public void setSmallerTimeUnit() {
        if (currentTimeUnit > TimelineConstants.SECOND) {
            currentTimeUnit--;
        }
    }
    
    /**
     * Changes the currently displayed time unit to the larger one.
     */
    public void setLargerTimeUnit() {
        if (currentTimeUnit < TimelineConstants.DAY) {
            currentTimeUnit++;
        }
    }
}
    
