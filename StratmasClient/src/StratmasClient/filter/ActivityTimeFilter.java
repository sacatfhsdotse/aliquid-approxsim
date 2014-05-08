package ApproxsimClient.filter;

import ApproxsimClient.object.ApproxsimObject;
import ApproxsimClient.object.ApproxsimTimestamp;
import ApproxsimClient.object.primitive.Timestamp;
import ApproxsimClient.timeline.Timeline;

/**
 * ActivityTimeFilter passes objects of <code>Activity</code> type depending on theirs occurence in time.
 * 
 * @author Amir Filipovic
 */
public class ActivityTimeFilter extends ApproxsimObjectFilter {
    /**
     * Indicator for past activities.
     */
    public static int PAST = -1;
    /**
     * Indicator for present activities.
     */
    public static int PRESENT = 0;
    /**
     * Indicator for future activities.
     */
    public static int FUTURE = 1;
    /**
     * Indicator for all activities.
     */
    public static int ALL = 2;
    /**
     * Indicates which objects the filter will pass.
     */
    private int indicator;
    /**
     * Reference to the timeline.
     */
    private Timeline timeline;

    /**
     * Creates a new ActivityTimeFilter.
     */
    public ActivityTimeFilter(Timeline timeline, int indicator) {
        super();
        this.timeline = timeline;
        this.indicator = indicator;
    }

    /**
     * Returns true if the provided ApproxsimObject passes the filter.
     * 
     * @param sObj the object to test
     */
    public boolean pass(ApproxsimObject sObj) {
        // only activities can pass
        if (sObj.getType().canSubstitute("Activity")) {
            // all activities can pass
            if (indicator == ActivityFilter.ALL) {
                return applyInverted(true);
            } else if (timeline != null) {
                long currentTime = timeline.getCurrentTime();
                long activityStartTime = -1;
                long activityEndTime = -1;
                if (sObj.getChild("start") != null) {
                    activityStartTime = ((Timestamp) ((ApproxsimTimestamp) sObj
                            .getChild("start")).getValue()).getMilliSecs();
                }
                if (sObj.getChild("end") != null) {
                    activityEndTime = ((Timestamp) ((ApproxsimTimestamp) sObj
                            .getChild("end")).getValue()).getMilliSecs();
                }
                // activities no longer active pass
                if (indicator == ActivityFilter.PAST) {
                    if (activityEndTime != -1 && activityEndTime < currentTime) {
                        return applyInverted(true);
                    }
                }
                // activities currently active pass
                else if (indicator == ActivityFilter.PRESENT) {
                    if ((activityStartTime == -1 || activityStartTime <= currentTime)
                            && (activityEndTime == -1 || activityEndTime >= currentTime)) {
                        return applyInverted(true);
                    }
                }
                // activities that will be active in the future pass
                else if (indicator == ActivityFilter.FUTURE) {
                    if (activityStartTime > currentTime) {
                        return applyInverted(true);
                    }
                }
            }
        }
        return applyInverted(false);
    }
}
