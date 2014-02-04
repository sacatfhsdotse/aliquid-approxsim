package StratmasClient.timeline;

/**
 * This class is used to define time interval in the timeline.
 */
public class TimeInterval {
    /**
     * Start time of the interval in milliseconds.
     */
    private long startTime;
    /**
     * End time of the interval in milliseconds.
     */
    private long endTime;
    /**
     * Time step in milliseconds.
     */
    private long timeStep;
    /**
     * Indicates that the left side of the interval is treated.
     */
    public static int LEFT = 0;
    /**
     * Indicates that the right side of the interval is treated.
     */
    public static int RIGHT = 1;

    /**
     * Creates a time interval.
     */
    public TimeInterval(long timeStep, long startTime, long endTime) {
        this.timeStep = timeStep;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Returns number of time steps the time interval contains.
     */
    public long getNrOfTimeSteps() {
        return (endTime - startTime) / timeStep;
    }
    
    /**
     * Returns number of times the time interval contains.
     */
    public long getNrOfTimes() {
        return getNrOfTimeSteps() + 1;
    }

    /**
     * Return all times this interval contains such that the difference between two
     * consecutive times is equal to the time step.
     */
    public long[] getAllTimes() {
        long[] times = new long[(int)((endTime - startTime) / timeStep + 1)];
        for (int i = 0; i < times.length; i++) {
            times[i] = startTime + i * timeStep;
        }
        return times;
    }

    /**
     * Return all times in the subinterval of this interval such that the difference 
     * between two consecutive times is equal to the time step.
     *
     * @param tstart start time of the subinterval.
     * @param tend end time of the subinterval.
     */
    public long[] getAllTimes(long tstart, long tend) {
        // adjust start and of the subinterval
        if (tstart > startTime) {
            long adj = ((tstart - startTime) % timeStep == 0)? (tstart - startTime) : 
                ((tstart - startTime) / timeStep + 1) * timeStep; 
            tstart = startTime + adj;
        }
        else {
            tstart = startTime;
        }
        // adjust end time of the subinterval
        if (tend < endTime) {
            long adj = ((tend - startTime) % timeStep == 0)? (tend - startTime) : ((tend - startTime) / timeStep) * timeStep;
            tend = startTime + adj; 
        }
        else {
            tend = endTime;
        }
        // store all times between the start and the end times
        if (tend >= tstart) {
            long[] times = new long[(int)((tend - tstart) / timeStep + 1)];
            for (int i = 0; i < times.length; i++) {
                times[i] = tstart + i * timeStep;
            }
            return times;
        }
        //
        return null;
    }
    
    /**
     * Returns the start time of the interval.
     */
    public long getStartTime() {
        return startTime;
    }
    
    /**
     * Returns the end time of the interval.
     */
    public long getEndTime() {
        return endTime;
    }
    
    /**
     * Returns the time step of the interval.
     */
    public long getTimeStep() {
        return timeStep;
    }
    
     /**
     * Sets the time step and adjusts the start and end times of the interval to the
     * reference time.
     *
     * @param timeStep new time step.
     * @param refTime reference time.
     *
     * @return true if the interval is adjusted to the new time step, false otherwise.
     */
    public boolean setTimeStep(long timeStep, long refTime) {
        this.timeStep = timeStep;
        // adjust interval to the new time step;
        if ((startTime - refTime) % this.timeStep != 0) {
            startTime = refTime + ((startTime - refTime) / this.timeStep+1) * this.timeStep;
        } 
        if ((endTime - refTime) % this.timeStep != 0) {
            endTime = refTime + (endTime - refTime) / this.timeStep * this.timeStep;
        }
        // check if the adjustment went right
        if (startTime <= endTime) {
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * Increses the interval with one time step.
     *
     * @param indicator indicates if the interval will be increased from the left
     *                  or from the right.
     */
    public void increaseInterval(int indicator){
        if (indicator == TimeInterval.LEFT) {
            startTime -= timeStep;
        }
        else if (indicator == TimeInterval.RIGHT) {
            endTime += timeStep;
        }
    }
    
    /**
     * Decreases the interval with one time step.
     *
     * @param indicator indicates if the interval will be decreased from the left
     *                  or from the right.
     */
    public boolean decreaseInterval(int indicator){
        // check if the interval can be decreased
        if (endTime - startTime >= timeStep) {
            if (indicator == TimeInterval.LEFT) {
                startTime += timeStep;
            }
            else if (indicator == TimeInterval.RIGHT) {
                endTime -= timeStep;
            }
            //
            return true;
        }
        //
        return false;
    }
    
    /**
     * Checks if the two intervals intersect.
     */
    public boolean intersects(TimeInterval interval) {
        return (!(getEndTime() < interval.getStartTime() || getStartTime() > interval.getEndTime()));
    }
    
    /**
     * Checks if an interval is contained in this one.
     */
    public boolean contains(TimeInterval interval) {
        return ((getStartTime() <= interval.getStartTime()) && (getEndTime() >= interval.getEndTime()));
    }
    
    /**
     * Adds new time interval to this one.
     * Obs. This method should be called only if the two intervals intersect.
     */
    public void add(TimeInterval interval) {
        startTime = Math.min(getStartTime(), interval.getStartTime());
        endTime = Math.max(getEndTime(), interval.getEndTime());
    }
    
    /**
     * Removes a time interval from this one.
     */
    public TimeInterval[] remove(TimeInterval interval) {
        TimeInterval[] intervals;
        if (interval.contains(this)) {
            return null;
        }
        else if (!this.intersects(interval)) {
            intervals = new TimeInterval[1];
            intervals[0] = this;
            return intervals;
        }
        long tmpStart = interval.getStartTime() - interval.getTimeStep();
        TimeInterval t1 = (tmpStart >= startTime)? new TimeInterval(timeStep, startTime, tmpStart) : null;
        long tmpEnd = interval.getEndTime() + interval.getTimeStep();
        TimeInterval t2 = (tmpEnd <= endTime)? new TimeInterval(timeStep, tmpEnd, endTime) : null;
        //
        if (t1 != null && t2 != null) {
            intervals = new TimeInterval[2];
            intervals[0] = t1;
            intervals[1] = t2;
            return intervals;
        }
        else if (t1 != null || t2 != null) {
            intervals = new TimeInterval[1];
            intervals[0] = (t1 == null)? t2 : t1;
            return intervals;
        }
        //
        return null;
    }
        
}
