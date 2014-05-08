package ApproxsimClient.timeline;

import java.util.EventListener;

/**
 * ActivityAdapterListener's listens to changes in ActivityAdapters.
 * 
 * @version 1
 * @author Daniel Ahlin
 */

public interface ActivityAdapterListener extends EventListener {
    /**
     * Signaled when an ActivityAdapters activity is removed.
     * 
     * @param activity_adapter the ActivityAdapter whose activity is being removed.
     */
    public abstract void activityAdapterRemoved(ActivityAdapter activity_adapter);

    /**
     * Signaled when displaylists in an ActivityAdapter needs to be updated.
     * 
     * @param activity_adapter the ActivityAdapter that needs to be updated.
     */
    public abstract void activityAdapterUpdated(ActivityAdapter activity_adapter);

    /**
     * Signaled when an ActivityAdapters activity is selected.
     * 
     * @param activity_adapter the ActivityAdapter that needs to be updated.
     * @param selected indicates if the activity is selected or unselected.
     */
    public abstract void activityAdapterSelected(
            ActivityAdapter activity_adapter, boolean selected);
}
