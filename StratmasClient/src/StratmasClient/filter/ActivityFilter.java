package StratmasClient.filter;

import java.util.Enumeration;

import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasDecimal;
import StratmasClient.object.StratmasTimestamp;
import StratmasClient.object.primitive.Timestamp;
import StratmasClient.Client;

/**
 * ActivityFilter passes objects of <code>Activity</code> type depending on theirs occurence in time. 
 *
 * @author Amir Filipovic
 */
public class ActivityFilter extends StratmasObjectFilter
{
    // indicator for past activities
    public static int PAST = -1;
    // indicator for present activities 
    public static int PRESENT = 0;
    // indicator for future activities 
    public static int FUTURE = 1;
    // indicator for all activities
    public static int ALL = 2;
    
    // indicates which objects the filter will pass
    private int indicator;
    // reference to the client
    private Client client;
    
    /**
     * Creates a new ActivityFilter.
     */
    public ActivityFilter(Client client, int indicator)
    {        
        super();
        this.client = client;
        this.indicator = indicator;
    }
    
    /**
     * Returns true if the provided StratmasObject passes the filter.
     *
     * @param sObj the object to test
     */
    public boolean pass(StratmasObject sObj)
    {
        // only activities can pass
        if (sObj.getType().canSubstitute("Activity")) {
            // all activities can pass
            if (indicator  == ActivityFilter.ALL) {
                return applyInverted(true);
            }
            else if (client.getTimeline() != null) {
                long current_time = client.getTimeline().getCurrentTime();
                long activity_start_time = -1;
                long activity_end_time = -1;
                if (sObj.getChild("start") != null) {
                    activity_start_time = ((Timestamp)
                                           ((StratmasTimestamp)sObj.getChild("start")).getValue()).getMilliSecs();
                }
                if (sObj.getChild("end") != null) {
                    activity_end_time = ((Timestamp)
                                         ((StratmasTimestamp)sObj.getChild("end")).getValue()).getMilliSecs();
                }
                // activities no longer active pass
                if (indicator  == ActivityFilter.PAST) {
                    if (activity_end_time != -1 && activity_end_time < current_time) {
                        return applyInverted(true);
                    }
                }
                // activities currently active pass
                else if (indicator  == ActivityFilter.PRESENT) {
                    if ((activity_start_time == -1 || activity_start_time <= current_time) && 
                        (activity_end_time == -1 || activity_end_time >= current_time)) {
                        return applyInverted(true);
                    }
                }
                // activities that will be active in the future pass
                else if (indicator  == ActivityFilter.FUTURE) {
                    if (activity_start_time > current_time) {
                        return applyInverted(true);
                    }
                }
            }
        }
        return applyInverted(false);
    }
}
