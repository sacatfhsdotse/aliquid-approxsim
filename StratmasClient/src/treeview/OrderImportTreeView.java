package StratmasClient.treeview;

import StratmasClient.Debug;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasList;
import StratmasClient.object.StratmasObjectFactory;
import StratmasClient.filter.StratmasObjectFilter;
import StratmasClient.map.DraggedElement;

import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;

/**
 * Treeview used when importing orders from an order library. The main
 * difference is that this treeview copies its elements on drag
 * instead of the original treeview's move policy.
 *
 * @version 1, $Date: 2006/04/10 09:45:56 $
 * @author  Per Alexius
 */
public class OrderImportTreeView extends TreeView {

     /**
      * Creates a new Tree panel using the the specified object as root.
      *
      * @param root the object to use as root for this tree.
      */
     public OrderImportTreeView(StratmasObjectAdapter root) {
          super(root);
          final TreeView self = this;
          
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
                              if (dtde.isDataFlavorSupported(StratmasObject.STRATMAS_OBJECT_FLAVOR)) {
                                   dtde.acceptDrop(DnDConstants.ACTION_LINK);
                                   dropAccepted = true;
                                   Object obj = dtde.getTransferable().getTransferData(StratmasObject.STRATMAS_OBJECT_FLAVOR);
                                   // Apple's dnd implementation sucks... We must call the
                                   // getTransferData method for the string flavor in order
                                   // to get a valid callback.
                                   dtde.getTransferable().getTransferData(DataFlavor.stringFlavor);
                                   //
                                   boolean complete = false;
                                   if (obj instanceof StratmasObject) {
                                        StratmasObject so = (StratmasObject)obj;
                                        StratmasObject target = pointToObject(dtde.getLocation());
                                        if (target instanceof StratmasList &&
                                            so.getType().canSubstitute("Activity") &&  // May only drop activities.
                                            !(so instanceof StratmasList) &&  // May not drop lists.
                                            so.getParent() !=  null) {  //  May not drop objects from the treeview itself.
                                            ((StratmasList) StratmasObjectFactory.cloneObject(target)).addWithUniqueIdentifier((StratmasObject) so);
                                             complete = true;
                                        }
                                   }
                                   dtde.dropComplete(complete);

                              }
                              else {
                                   dtde.rejectDrop();
                              }
                         } catch (Exception e) {
                              e.printStackTrace();
                              if (dropAccepted) {
                                   dtde.dropComplete(false);
                                   Debug.err.println("Exception thrown - Drop complete false");
                              }
                              else {
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
      * Gets the StratmasObject pointed to by the provided path.
      *
      * @param path The path to fetch the StratmasObject for.
      * @return The StratmasObject pointed to by path or null if the
      * path is invalid or no such object could be found.
      */
     public StratmasObject getObjectForPath(javax.swing.tree.TreePath path) {
          if (path != null) {
               StratmasObjectAdapter soa =  (StratmasObjectAdapter)path.getLastPathComponent();
               if (soa != null) {
                   return StratmasObjectFactory.cloneObject((StratmasObject)soa.getUserObject());
               }
          }
          return null;
     }

     /**
      * Creates a new Tree window using the the specified object as root.
      * filtered using the specified filter.
      * @param root the object to use as root for this tree.
      * @param filter the object to use as root for this tree.
      */    
     public static TreeViewFrame getDefaultFrame(StratmasObject root, StratmasObjectFilter filter) {        
          TreeViewFrame res = new TreeViewFrame(getDefaultTreeView(root, filter));
          return res;
     }
     
     
     /**
      * Creates the default component used for visualizing a TreeView
      * of the the specified object as root and defaults for all else..
      *
      * @param root the object to use as root for this tree.
      */
     public static TreeView getDefaultTreeView(StratmasObject root, StratmasObjectFilter filter) {
          TreeView view = new OrderImportTreeView(new StratmasObjectAdapter(root, filter));
          view.setShowsRootHandles(false);
          // By defualt, don't show root handle for lists.
          if (root instanceof StratmasList) {
               view.setRootVisible(false);
          }
          return view;
     }
}

