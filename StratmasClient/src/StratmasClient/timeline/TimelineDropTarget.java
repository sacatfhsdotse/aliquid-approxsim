package ApproxsimClient.timeline;

import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetAdapter;

import ApproxsimClient.Debug;
import ApproxsimClient.object.ApproxsimObject;
import ApproxsimClient.object.ApproxsimTimestamp;
import ApproxsimClient.object.primitive.Timestamp;
import ApproxsimClient.map.DraggedElement;

/**
 * This class implements DropTargetListener for use in the Timeline.
 */
class TimelineDropTarget extends DropTargetAdapter {
    /**
     * Reference to the timeline.
     */
    private Timeline timeline;

    /**
     * Creates new TimelineDropTarget.
     */
    protected TimelineDropTarget(Timeline timeline) {
        this.timeline = timeline;
    }

    /**
     * Called while a drag operation is ongoing, when the mouse pointer enters the operable part of the timeline.
     */
    public void dragEnter(DropTargetDragEvent dtde) {
        dtde.acceptDrag(dtde.getDropAction());
    }

    /**
     * Called when a drag operation is ongoing, while the mouse pointer is still over the operable part of the timeline.
     */
    public void dragOver(DropTargetDragEvent dtde) {
        dtde.acceptDrag(dtde.getDropAction());
    }

    /**
     * Called when the drag operation has terminated with a drop on the operable part of the timeline.
     */
    public void drop(DropTargetDropEvent dtde) {
        TimelinePanel timelinePanel = timeline.getTimelinePanel();
        TimelineActivityPanel activityPanel = timeline.getTimelinePanel()
                .getTimelineActivityPanel();
        boolean dropAccepted = false;
        java.awt.Point pt = dtde.getLocation();
        pt.getY();
        int x = (int) pt.getX();
        try {
            if (dtde.isDataFlavorSupported(DataFlavor.stringFlavor)
                    || dtde.isDataFlavorSupported(ApproxsimObject.APPROXSIM_OBJECT_FLAVOR)) {
                dtde.acceptDrop(DnDConstants.ACTION_LINK);
                dropAccepted = true;
                Object obj;
                if (dtde.getTransferable()
                        .isDataFlavorSupported(ApproxsimObject.APPROXSIM_OBJECT_FLAVOR)) {
                    // accept ApproxsimObject
                    obj = dtde
                            .getTransferable()
                            .getTransferData(ApproxsimObject.APPROXSIM_OBJECT_FLAVOR);
                } else {
                    // accept String
                    obj = dtde.getTransferable()
                            .getTransferData(DataFlavor.stringFlavor);
                }
                if (obj instanceof ApproxsimObject) {
                    ApproxsimObject so = (ApproxsimObject) obj;
                    if (so.getChild("start") != null) {
                        if (so.getChild("end") != null) {
                            long old_start = ((ApproxsimTimestamp) so
                                    .getChild("start")).getValue()
                                    .getMilliSecs();
                            long old_end = ((ApproxsimTimestamp) so
                                    .getChild("end")).getValue().getMilliSecs();
                            long old_center = (old_start + old_end) / 2;
                            long new_center = timelinePanel
                                    .timeToMilliseconds(activityPanel
                                            .convertProjectedXToCurrentTime(x))
                                    + timeline.getSimStartTime();
                            long new_start = old_start
                                    + (new_center - old_center);
                            long new_end = old_end + (new_center - old_center);
                            // update the activity
                            Timestamp sTime = new Timestamp(new_start);
                            Timestamp eTime = new Timestamp(new_end);
                            if (sTime.getMilliSecs() > timeline
                                    .getCurrentTime()
                                    && eTime.getMilliSecs() > timeline
                                            .getCurrentTime()) {
                                ((ApproxsimTimestamp) so.getChild("start"))
                                        .setValue(sTime, this);
                                ((ApproxsimTimestamp) so.getChild("end"))
                                        .setValue(eTime, this);
                            }
                        } else {
                            long new_center = timelinePanel
                                    .timeToMilliseconds(activityPanel
                                            .convertProjectedXToCurrentTime(x))
                                    + timeline.getSimStartTime();
                            if (new_center > timeline.getCurrentTime()) {
                                ((ApproxsimTimestamp) so.getChild("start"))
                                        .setValue(new Timestamp(new_center),
                                                  this);
                            }
                        }
                        //
                        dtde.dropComplete(true);
                    } else if (so instanceof ApproxsimTimestamp) {
                        ApproxsimTimestamp st = (ApproxsimTimestamp) so;
                        long newTime = timelinePanel
                                .timeToMilliseconds(activityPanel
                                        .convertProjectedXToCurrentTime(x))
                                + timeline.getSimStartTime();
                        st.setValue(new Timestamp(newTime), this);
                        dtde.dropComplete(true);
                    }
                } else {
                    dtde.dropComplete(false);
                }
                // update the timeline panel
                activityPanel.update();
            } else {
                dtde.rejectDrop();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (dropAccepted) {
                dtde.dropComplete(false);
                Debug.err.println("Exception thrown - Drop complete false");
            } else {
                dtde.rejectDrop();
                Debug.err.println("Exception thrown - Drop rejected");
            }
        }
        // indicate that no element is dragged at the moment
        DraggedElement.setElement(null);
    }

}
