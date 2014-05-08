package StratmasClient.map;

import javax.swing.JButton;
import javax.swing.ImageIcon;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Cursor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureEvent;
import java.awt.datatransfer.StringSelection;

/**
 * This class enables drag action in DnD.
 */
public class DraggableJButton extends JButton implements DragGestureListener {
    /**
	 * 
	 */
    private static final long serialVersionUID = -5310281336989140806L;
    /**
     * Used for the drag action.
     */
    private DragSource source;
    /**
     * Name of the button.
     */
    private String name;
    /**
     * The mouse pointer cursor.
     */
    private Cursor c;

    /**
     * Creates menu item which supports drag action in DnD.
     * 
     * @param image the image displayed on the button.
     * @param name the name of the button.
     */
    public DraggableJButton(ImageIcon image, String name) {
        super(image);
        this.name = name;
        source = new DragSource();
        source.createDefaultDragGestureRecognizer(this,
                                                  DnDConstants.ACTION_REFERENCE,
                                                  this);
        //
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension bestsize = tk.getBestCursorSize(16, 16);
        if (bestsize.width != 0)
            c = tk.createCustomCursor(((ImageIcon) this.getIcon()).getImage(),
                                      new Point(0, 0), name);
        else c = Cursor.getDefaultCursor();
    }

    /*
     * Drag gesture handler.
     * @param dge the event.
     */
    public void dragGestureRecognized(DragGestureEvent dge) {
        source.startDrag(dge, c, new StringSelection(name),
                         new DragSourceAdapter() {});
    }

}
