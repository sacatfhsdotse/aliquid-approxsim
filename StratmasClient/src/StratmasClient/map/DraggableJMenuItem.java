package ApproxsimClient.map;

import javax.swing.JMenuItem;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureEvent;

import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.Image;
import java.awt.Dimension;
import ApproxsimClient.object.ApproxsimObject;
import ApproxsimClient.Icon;

/**
 * This class enables drag action in DnD.
 */
public class DraggableJMenuItem extends JMenuItem implements
        DragGestureListener {
    /**
	 * 
	 */
    private static final long serialVersionUID = 2087717193285582922L;
    /**
     * The object associated with the item.
     */
    private ApproxsimObject object;
    /**
     * Used for the drag action.
     */
    private DragSource source;
    /**
     * The mouse pointer cursor.
     */
    private Cursor c;

    /**
     * Creates menu item which supports drag action in DnD.
     * 
     * @param object the object associated with the dragging action.
     */
    public DraggableJMenuItem(ApproxsimObject object) {
        super(object.getReference().getIdentifier().trim());
        this.object = object;
        source = new DragSource();
        source.createDefaultDragGestureRecognizer(this,
                                                  DnDConstants.ACTION_REFERENCE,
                                                  this);
        //
        Toolkit tk = Toolkit.getDefaultToolkit();
        Image image = ((Icon) object.getIcon()).getImage();
        Dimension bestsize = tk.getBestCursorSize(image.getWidth(null),
                                                  image.getHeight(null));
        if (bestsize.width != 0)
            c = tk.createCustomCursor(image, new java.awt.Point(
                    bestsize.width / 2, bestsize.height / 2), object.toString());
        else c = Cursor.getDefaultCursor();
    }

    /*
     * Drag gesture handler.
     * @param dge the event.
     */
    public void dragGestureRecognized(DragGestureEvent dge) {
        // set the dragged element
        DraggedElement.setElement(object);
        // start the drag
        source.startDrag(dge, c, object, new DragSourceAdapter() {});
    }

}
