package StratmasClient.timeline;

import java.util.Vector;
import java.util.Enumeration;

import StratmasClient.Client;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasEventListener;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.StratmasTimestamp;
import StratmasClient.object.StratmasDuration;
import StratmasClient.object.primitive.Timestamp;
import StratmasClient.object.type.TypeFactory;
import StratmasClient.filter.TypeFilter;
import StratmasClient.filter.CombinedORFilter;

/**
 * Timeline is primarily used to select the times for the simulation data to be delivered and visualized on
 * the client. Besides the timeline is used for visualization and modification of the activities. 
 *
 * @author Amir Filipovic 
 */
public class Timeline implements StratmasEventListener, ActivityAdapterListener {
    /**
     * Length of the minimum time step in miliseconds.
     */
    private long deltat;
    /**
     * Exact starting time of the simulation in milliseconds.
     */
    private long simulationStartTime = -1;
    /**
     * Current simulation (relative) time.
     */
    private long simulationCurrentTime = 0;
    /**
     * Last processed (relative) time to Client.
     */
    private long nextTime = 0;
    /**
     * Selected times that are neither processed to Client nor executed.
     */
    private Vector selectedTimes = new Vector();
    /**
     * Executed times.
     */
    private Vector executedTimes = new Vector();
    /**
     * The times processed to the server but not executed.
     */
    private Vector timesInProcess = new Vector();
    /**
     * Activities visualized in the timeline.
     */
    private Vector activityAdapters = new Vector();
    /**
     * Reference to Client.
     */
    private Client client;
    /**
     * Indicates if the client is in the state of waiting for the message from the timeline.
     */
    private boolean isClientWaiting = false;
    /**
     * Indicates if the time step is changed.
     */
    public boolean isdtChanged = false;
    /**
     * Reference to the currently used simulation timestep.
     */
    private StratmasDuration simulationTimestep;
    /**
     * Reference to the start time of the simulation.
     */
    private StratmasTimestamp simulationStartTimestamp;
    /**
     * The panel of the timeline.
     */
    private TimelinePanel timelinePanel;
     
    /**
     * Creates new timeline.
     *
     * @param dt length of the time step in miliseconds.
     * @param startTimestamp simulation start time.
     */
    public Timeline(StratmasDuration dt, StratmasTimestamp startTimestamp) {
        // get length of the time step
        simulationTimestep = dt;
        simulationTimestep.addEventListener(this);
        
        // start time
        simulationStartTimestamp = startTimestamp;
        simulationStartTimestamp.addEventListener(this);
        
        // update delta t
        deltat = dt.getValue().getMilliSecs();
        
        // create the timeline panel
        timelinePanel = new TimelinePanel(this);
        
        // initialize the timeline with the start time of the simulation
        setSimulationStartTime();
    }
    
    /**
     * Notifies the client.
     */
    public void notifyClient() {
        if (client != null) {
            client.setNotify();
        }
    }
        
    /**
     * Sets reference to the stratmas client.
     */
    public void setClient(Client client) {
        this.client = client;
        // set the timeline to listen to the client
        client.addEventListener(this);
    }
    
    /**
     * Returns the reference to the stratmas client.
     */
    public Client getClient() {
        return client;
    }
    
    /**
     * Returns the timeline panel.
     */
    public TimelinePanel getTimelinePanel() {
        return timelinePanel;
    }
    
    /**
     * Used to set up the timeline with all the time steps from the simulation start time
     * to the given time.
     *
     * @param endTime actual time in milliseconds - ends the selected time interval.
     */
    public void setUpTimeline(long endTime) {
        // set no times selected
        timelinePanel.selectAllTimes(false);
        // update selected times
        insert(selectedTimes, new TimeInterval(deltat, 0, endTime-simulationStartTime));
        // repaint the panel
        timelinePanel.updateScalePanel();
    }
    
    /**
     * Updates the start time of the simulation.
     */
    public void setSimulationStartTime() {
        simulationStartTime = simulationStartTimestamp.getValue().getMilliSecs();
        // update the displayed times in the dialog
        timelinePanel.updateInfoDialog();
    }
    
    /**
     * Updates the size of the time step.
     *
     * @param dt the new time step.
     * @param refTime the reference time ie. the time when the new time step starts to be
     *                in effect.
     */
    public synchronized void setDeltaT(long dt, long refTime) {
        // update the time step if no times are processed to the server 
        if (timesInProcess.isEmpty()) {
            // update time step
            deltat = dt;
            // update selected times
            synchronized (selectedTimes) {
                int counter = 0;
                while (counter < selectedTimes.size()) {
                    boolean success = ((TimeInterval)selectedTimes.get(counter)).setTimeStep(deltat, refTime);
                    if (!success) {
                        selectedTimes.remove(counter);        
                    }
                    else {
                        counter++;
                    }
                }
            }
            // update the displayed times in the dialog
            timelinePanel.updateInfoDialog();
            // update the scale panel
            timelinePanel.updateScalePanel();
        }
        // indicates that the time step is changed
        else {
            isdtChanged = true;
        }
    }
    
    /**
     * Updates the current simulation time. 
     *
     * @param time current simulation time in milliseconds.
     */
    public synchronized void updateCurrentTime(long time) {
        if (time != simulationCurrentTime) {
            try {
                // set current time
                simulationCurrentTime = time;
                // update the list of times in process
                if (!timesInProcess.isEmpty()) {
                    // remove the first time
                    TimeInterval ti = (TimeInterval)timesInProcess.firstElement();
                    boolean decreasable = ti.decreaseInterval(TimeInterval.LEFT);
                    if (!decreasable) {
                        timesInProcess.remove(0);        
                    }
                    // temporary solution
                    if (isdtChanged && timesInProcess.isEmpty()) {
                        // adjust the selected times according to the time step change
                        delete(selectedTimes, new TimeInterval(deltat, 0, simulationCurrentTime));
                        isdtChanged = false;
                        // update the time step
                        setDeltaT(simulationTimestep.getValue().getMilliSecs(), simulationCurrentTime);
                        // modify next time
                        nextTime = simulationCurrentTime;
                        notifyClient();
                    }
                }
                // update the list of executed times
                if (simulationCurrentTime != 0) {
                    insert(executedTimes, new TimeInterval(deltat, simulationCurrentTime, simulationCurrentTime));
                }
                // update the timeline panel
                if (timelinePanel != null) {
                    timelinePanel.updateCurrentTime();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }    
    
    /**
     * Checks if any selected time step exist.
     *
     * @return true if time step exists.
     */
    public boolean timeStepExists() {
        return !selectedTimes.isEmpty() && !isdtChanged;
    }
    
    /**
     * Returns start time of the simulation im milliseconds.
     */
    public long getSimStartTime() {
        return simulationStartTime;
    } 
    
    /**
     * Returns the size of the timestep.
     */
    public long getDT() {
        return deltat;
    } 

    /**
     * Returns and removes the first time step from the list.
     *
     * @return next time step if available, otherwise -1.
     */
    public int getNextTimeStep() {
        // Please don't remove the following three lines. They're
        // essential for the passive client the way it works right
        // now.
        if (!client.isActive()) {
            return 1;
        }
        //
        if (timeStepExists()) {
            synchronized (selectedTimes) {
                TimeInterval ti = (TimeInterval)selectedTimes.firstElement();
                long tmpTime = ti.getStartTime();
                // adjust the time interval
                boolean decreasable = ti.decreaseInterval(TimeInterval.LEFT);
                if (!decreasable) {
                    // remove the interval
                    selectedTimes.remove(0);
                }
                // update the list of the times in process
                insert(timesInProcess, new TimeInterval(deltat, tmpTime, tmpTime));
                // compute the time step
                int timeStep = (int)Math.ceil((tmpTime-nextTime)*1.0/deltat);
                // update the next time
                nextTime = tmpTime;
                // return the time step
                return timeStep;
            }
        }
        else {
            return -1;
        }
    }
    
    /**
     * Returns the list of adapters for the activities.
     */
    public Vector getActivityAdapters() {
        return activityAdapters;
    }
    
    /**
     * Retuns unexecuted time intervals.
     */
    public Vector getUnexecutedTimes() {
        return selectedTimes;
    }

    /**
     * Returns executed time intervals.
     */
    public Vector getExecutedTimes() {
        return executedTimes;
    }
    
    /**
     * Returns processed time intervals.
     */
    public Vector getProcessedTimes() {
        return timesInProcess;
    }
    
    /**
     * Returns exact curent time for the simulation.
     */
    public long getCurrentTime() {
        return simulationCurrentTime + simulationStartTime;
    }

    /**
     * Returns curent time (relative to the simulation start time) for the simulation.
     */
    public long getRelativeCurrentTime() {
        return simulationCurrentTime;
    }

    /**
     * Returns exact next chosen time which will be proceeded to the simulation.
     */
    public long getNextTime() {
        return nextTime + simulationStartTime;
    }
    
    /**
     * Returns next chosen time (relative to the simulation start time) which will 
     * be proceeded to the simulation.
     */
    public long getRelativeNextTime() {
        return nextTime;
    }

    /**
     * Resets the timeline.
     *
     * @param dt time step.
     * @param start_time simulation start time.
     */
    public void reset(StratmasDuration dt, StratmasTimestamp start_time) {
        // time step
        simulationTimestep.removeEventListener(this);
        simulationTimestep = dt;
        simulationTimestep.addEventListener(this);
        deltat = dt.getValue().getMilliSecs();
        
        // reset timeline panel
        if (timelinePanel == null) {
            timelinePanel = new TimelinePanel(this);
        }
        else {
            timelinePanel.reset();
        }

        // start time
        simulationStartTimestamp.removeEventListener(this);
        simulationStartTimestamp = start_time;
        simulationStartTimestamp.addEventListener(this);
        setSimulationStartTime();
        // current simulation time
        simulationCurrentTime = 0;
        // selected time following the current time
        nextTime = 0;
        // keep the chosen times in the timeline
        Vector[] vec = {executedTimes, timesInProcess};
        for (int i = 0; i < vec.length; i++) {
            while (!vec[i].isEmpty()) {
                TimeInterval ti = (TimeInterval)vec[i].remove(0);
                boolean success = ti.setTimeStep(deltat, 0);
                if (success) {
                    insert(selectedTimes, ti);
                }
            }
        }
        // update the information labels
        timelinePanel.updateInfoDialog();
        
        // indicates if the client is in the state of waiting for the message from the timeline
        isClientWaiting = false;
        
        // activities
        activityAdapters = new Vector();
    }
    
    /**
     * Removes the timeline.
     */
    public void remove() {
        // release all the objects the timeline listens to
        client.removeEventListener(this);
        client = null;
        
        // remove the panel
        timelinePanel.remove();

        // clear all the lists
        activityAdapters.removeAllElements();
        selectedTimes.removeAllElements();
        executedTimes.removeAllElements();
        timesInProcess.removeAllElements();
    }
    
    /**
     * Updates the timeline.
     */
    public synchronized void eventOccured(StratmasEvent e) {
        // update the current time on the timeline
        if (e.isSubscriptionHandled()) {
            long msecs = ((Timestamp)e.getArgument()).getMilliSecs();
            updateCurrentTime(msecs-simulationStartTime);
        }
        // update the time step
        else if (e.getSource() == simulationTimestep) {
            if (e.isRemoved()) {
                simulationTimestep.removeEventListener(this);
                simulationTimestep = null;
            }
            else if (e.isValueChanged()){
                setDeltaT(simulationTimestep.getValue().getMilliSecs(), simulationCurrentTime);
            }
        }
        // update the start time
        else if (e.getSource() == simulationStartTimestamp) {
            if (e.isRemoved()) {
                simulationStartTimestamp.removeEventListener(this);
                simulationStartTimestamp = null;
            }
            else if  (e.isValueChanged()) {
                setSimulationStartTime();
            }
        }
        // add new activity to the timeline or listener to a new element
        else if (e.isObjectAdded() && e.getArgument() instanceof StratmasObject) {
            StratmasObject arg = (StratmasObject)e.getArgument();
            if(arg.getType().canSubstitute("Element")) {
                importActivities(arg);
            }
            else if(arg.getType().canSubstitute("Activity")) {
                addActivity(arg);
            }
        }
    }  
    
    /**
     * The list of the selected times are updated by this procedure.
     *
     * @param indicator indicates if the time interval will be added to or deleted from
     *                  the list of the selected time intervals.
     * @param interval the new time interval.
     */
    public void updatePrenumerations(int indicator, TimeInterval interval) {
        // add new prenumerated times
        if (indicator == TimelineConstants.SELECT) {
            // update the list of selected times
            insert(selectedTimes, interval);
        }
        // remove unprenumerated times from the list of the prenumerations
        else if (indicator == TimelineConstants.DELETE) {
            // update the list of selected times
            delete(selectedTimes, interval);
        }
        // update the scale panel
        if (timelinePanel != null) {
            timelinePanel.updateScalePanel();
            // notify the client
            notifyClient();
        }
    }
    
    /**
     * Updates the sorted list of selected time intervals with a new one.
     *
     * @param v list of selected time intervals.
     * @param interval selected time interval which is to be added in the list.
     */
    private void insert(Vector v, TimeInterval interval) {
        int counter = 0;
        boolean inserted = false;
        // insert the interval
        while (counter < v.size() && !inserted) {
            TimeInterval ti = (TimeInterval)v.get(counter);
            if (interval.getStartTime() <= ti.getStartTime()) {
                v.add(counter, interval);
                inserted = true;
            }
            else {
                counter++;
            }
        }
        if (!inserted) {
            v.add(interval);
        }
        // check for intersections
        // inetrvals on the left 
        boolean done = false; 
        while (counter > 0 && !done) {
            TimeInterval ti = (TimeInterval)v.get(counter);
            TimeInterval tprev = (TimeInterval)v.get(counter - 1);
            if ((ti.getStartTime() <= tprev.getEndTime() || ti.getStartTime() - tprev.getEndTime() == ti.getTimeStep())
                && ti.getTimeStep() == tprev.getTimeStep()) {
                ti.add(tprev);
                v.remove(counter - 1);
                counter--;
            }
            else {
                done = true;
            }
        }
        // intervals on the right
        done = false; 
        while (counter < v.size() - 1 && !done) {
            TimeInterval ti = (TimeInterval)v.get(counter);
            TimeInterval tnext = (TimeInterval)v.get(counter + 1);
            if ((ti.getEndTime() >= tnext.getStartTime() || tnext.getStartTime() - ti.getEndTime() == ti.getTimeStep())
                && ti.getTimeStep() == tnext.getTimeStep()) {
                ti.add(tnext);
                v.remove(counter + 1);
            }
            else {
                done = true;
            }
        }
    }
    
    /**
     * Deletes a time interval form the list of selected time intervals.
     *
     * @param v list of selected time intervals.
     * @param interval selected time interval which is to be deleted from the list.
     */
    private void delete(Vector v, TimeInterval interval) {
        int counter = 0;
        while (counter < v.size()) {
            TimeInterval ti = (TimeInterval)v.get(counter);
            if (interval.contains(ti)) {
                v.remove(counter);
            }
            else if (interval.intersects(ti)) {
                v.remove(counter);
                TimeInterval[] intervals = ti.remove(interval);
                if (intervals != null) {
                    for (int i = 0; i < intervals.length; i++) {
                        v.add(counter, intervals[i]);
                        counter++;
                    }
                }
            }
            else {
                counter++;
            }
        }
    }
    
    /**
     * Adds a listener to each element of type Element or Activity in a subtree
     * with the given root node. 
     *
     * @param node the root of the subtree.
     */
    private void addListenerTo(StratmasObject node)
    {
        // create filter for elements and activities
        CombinedORFilter combFilter = new CombinedORFilter();
        combFilter.addFilter(new TypeFilter(TypeFactory.getType("Activity"), true));
        combFilter.addFilter(new TypeFilter(TypeFactory.getType("Element"), true));
        Enumeration eaList = combFilter.filterTree(node);
        for (; eaList.hasMoreElements();) {
            StratmasObject candidate = (StratmasObject) eaList.nextElement();
            // listen to this element
            candidate.addEventListener(this);
        }
    }
    
    /**
     * Adds the activities to the timeline.
     */
    public void importActivities(StratmasObject node) {
        // add listeners to all the elements
        addListenerTo(node);
        // add all activities 
        TypeFilter filter = new TypeFilter(TypeFactory.getType("Activity"), true);
        Enumeration acts = filter.filterTree(node);
        for (; acts.hasMoreElements(); ) {
            StratmasObject scom = (StratmasObject)acts.nextElement();
            if (scom.getType().canSubstitute("Activity") && scom.getChild("start") != null) {
                ActivityAdapter adapter = new ActivityAdapter(scom);
                adapter.addActivityAdapterListener(this);
                activityAdapters.add(adapter);
                // update the timeline panel
                timelinePanel.updateActivityList(adapter, TimelineConstants.ADD);
            }
        }
        // update the panel with the root object
        timelinePanel.updateActivityTable(node);
    }
    
    /**
     * Adds an activity to the timeline.
     */
    public void addActivity(StratmasObject activity) {
        if (!this.contains(activity) && activity.getChild("start") != null) {
            ActivityAdapter adapter = new ActivityAdapter(activity);
            adapter.addActivityAdapterListener(this);
            activityAdapters.add(adapter);
            // update the timeline panel
            timelinePanel.updateActivityList(adapter, TimelineConstants.ADD);
        }
    }
    
    /**
     * Removes an activity from the timeline.
     */
    public void removeActivity(StratmasObject activity) {
        for (int i = 0; i <  activityAdapters.size(); i++) {
            if (((ActivityAdapter)activityAdapters.get(i)).getActivity().equals(activity)) {
                ActivityAdapter adapter = (ActivityAdapter)activityAdapters.remove(i);
                if (timelinePanel != null) {
                    timelinePanel.updateActivityList(adapter, TimelineConstants.REMOVE);
                }
            }
        }
    }

    /**
     * Checks if an activity is already contained in the timeline.
     */
    public boolean contains(StratmasObject activity) {
        for (Enumeration e = activityAdapters.elements(); e.hasMoreElements(); ) {
            if (((ActivityAdapter)e.nextElement()).getActivity().equals(activity)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Signaled when an ActivityAdapters element is removed.
     *
     * @param  activityAdapter the ActivityAdapter whose element is being removed.
     */
    public void activityAdapterRemoved(ActivityAdapter activityAdapter) {
        removeActivity(activityAdapter.getActivity());
    }
    
    /**
     * Signaled when displaylists in an ActivityAdapter needs to be
     * updated.
     *
     * @param activityAdapter the ActivityAdapter that needs to be updated.
     */
    public void activityAdapterUpdated(ActivityAdapter activityAdapter) {
        activityAdapter.setSymbolUpdated(false);
        if (timelinePanel != null) {
            timelinePanel.updateActivityList();
        }
    }
    
    /**
     * Signaled when displaylists in an ActivityAdapter needs to be
     * updated.
     *
     * @param activityAdapter the ActivityAdapter that needs to be updated.
     */
    public void activityAdapterSelected(ActivityAdapter activityAdapter, boolean selected) {
        activityAdapter.setSymbolUpdated(false);
        activityAdapter.setSelected(selected);
        if (timelinePanel != null) {
            timelinePanel.updateActivityList();
        }
    }

}


