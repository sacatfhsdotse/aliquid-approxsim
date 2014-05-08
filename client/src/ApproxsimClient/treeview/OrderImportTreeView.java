package ApproxsimClient.treeview;

import ApproxsimClient.Debug;
import ApproxsimClient.object.ApproxsimObject;
import ApproxsimClient.object.ApproxsimList;
import ApproxsimClient.object.ApproxsimObjectFactory;
import ApproxsimClient.filter.ApproxsimObjectFilter;
import ApproxsimClient.map.DraggedElement;

import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;

/**
 * Treeview used when importing orders from an order library. The main difference is that this treeview copies its elements on drag instead
 * of the original treeview's move policy.
 * 
 * @version 1, $Date: 2006/04/10 09:45:56 $
 * @author Per Alexius
 */
public class OrderImportTreeView extends TreeView {

    /**
	 * 
	 */
    private static final long serialVersionUID = 4013479827585585162L;

    /**
     * Creates a new Tree panel using the the specified object as root.
     * 
     * @param root the object to use as root for this tree.
     */
    public OrderImportTreeView(ApproxsimObjectAdapter root) {
        super(root);
        this.setDropTarget(new DropTarget(this, new DropTargetAdapter() {
            public void dragEnter(DropTargetDragEvent dtde) {
                dtde.acceptDrag(dtde.getDropAction());
            }

            public void dragOver(DropTargetDragEvent dtde) {
                dtde.acceptDrag(dtde.getDropAction());
            }

            public void drop(DropTargetDropEvent dtde) {
                boolean dropAccepted = false;
                try {
                    if (dtde.isDataFlavorSupported(ApproxsimObject.APPROXSIM_OBJECT_FLAVOR)) {
                        dtde.acceptDrop(DnDConstants.ACTION_LINK);
                        dropAccepted = true;
                        Object obj = dtde
                                .getTransferable()
                                .getTransferData(ApproxsimObject.APPROXSIM_OBJECT_FLAVOR);
                        // Apple's dnd implementation sucks... We must call the
                        // getTransferData method for the string flavor in order
                        // to get a valid callback.
                        dtde.getTransferable()
                                .getTransferData(DataFlavor.stringFlavor);
                        //
                        boolean complete = false;
                        if (obj instanceof ApproxsimObject) {
                            ApproxsimObject so = (ApproxsimObject) obj;
                            ApproxsimObject target = pointToObject(dtde
                                    .getLocation());
                            if (target instanceof ApproxsimList
                                    && so.getType().canSubstitute("Activity") &&  // May only drop activities.
                                    !(so instanceof ApproxsimList) &&  // May not drop lists.
                                    so.getParent() != null) {  // May not drop objects from the treeview itself.
                                ((ApproxsimList) ApproxsimObjectFactory
                                        .cloneObject(target))
                                        .addWithUniqueIdentifier((ApproxsimObject) so);
                                complete = true;
                            }
                        }
                        dtde.dropComplete(complete);

                    } else {
                        dtde.rejectDrop();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (dropAccepted) {
                        dtde.dropComplete(false);
                        Debug.err
                                .println("Exception thrown - Drop complete false");
                    } else {
                        dtde.rejectDrop();
                        Debug.err.println("Exception thrown - Drop rejected");
                    }
                }
                DraggedElement.setElement(null);
            }
        }));

        setEditable(true);
        setRootVisible(false);
    }

    /**
     * Gets the ApproxsimObject pointed to by the provided path.
     * 
     * @param path The path to fetch the ApproxsimObject for.
     * @return The ApproxsimObject pointed to by path or null if the path is invalid or no such object could be found.
     */
    public ApproxsimObject getObjectForPath(javax.swing.tree.TreePath path) {
        if (path != null) {
            ApproxsimObjectAdapter soa = (ApproxsimObjectAdapter) path
                    .getLastPathComponent();
            if (soa != null) {
                return ApproxsimObjectFactory.cloneObject((ApproxsimObject) soa
                        .getUserObject());
            }
        }
        return null;
    }

    /**
     * Creates a new Tree window using the the specified object as root. filtered using the specified filter.
     * 
     * @param root the object to use as root for this tree.
     * @param filter the object to use as root for this tree.
     */
    public static TreeViewFrame getDefaultFrame(ApproxsimObject root,
            ApproxsimObjectFilter filter) {
        TreeViewFrame res = new TreeViewFrame(getDefaultTreeView(root, filter));
        return res;
    }

    /**
     * Creates the default component used for visualizing a TreeView of the the specified object as root and defaults for all else..
     * 
     * @param root the object to use as root for this tree.
     */
    public static TreeView getDefaultTreeView(ApproxsimObject root,
            ApproxsimObjectFilter filter) {
        TreeView view = new OrderImportTreeView(new ApproxsimObjectAdapter(root,
                filter));
        view.setShowsRootHandles(false);
        // By defualt, don't show root handle for lists.
        if (root instanceof ApproxsimList) {
            view.setRootVisible(false);
        }
        return view;
    }
}
