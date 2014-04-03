//         $Id: TreeView.java,v 1.49 2006/07/31 11:50:56 alexius Exp $

/*
 * @(#)TreeView.java
 */

package StratmasClient.treeview;

import StratmasClient.map.DraggedElement;
import StratmasClient.object.StratmasObject;
import StratmasClient.filter.StratmasObjectFilter;
import StratmasClient.filter.TypeFilter;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.StratmasEventListener;
import StratmasClient.object.StratmasList;
import StratmasClient.object.StratmasReference;
import StratmasClient.ClientMainFrame;
import StratmasClient.Debug;
import StratmasClient.Icon;

import java.util.Enumeration;
import java.util.Vector;
import java.awt.event.KeyEvent;
import javax.swing.JTree;
import javax.swing.ImageIcon;
import javax.swing.tree.TreeModel;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.JPopupMenu;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.event.MouseInputAdapter;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import java.awt.event.WindowEvent;
import java.awt.Window;
import java.awt.event.WindowListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.awt.dnd.DnDConstants;
import java.awt.Point;
import java.awt.Image;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;

/**
 * TreeView is presentation of a tree of StratmasObjects.
 *
 * @version 1, $Date: 2006/07/31 11:50:56 $
 * @author  Daniel Ahlin
*/

public class TreeView extends JTree implements DragGestureListener, WindowListener, StratmasEventListener
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -6703749292998360374L;

	/**
     * The prefered size of icons used in the TreeCellRenderers.
     */
    Dimension preferedIconSize = new Dimension(16, 16);

    /**
     * A reference to the current Window this JTree resides in, or
     * null if none.
     */
    Window currentWindow = null;

    /**
     * Dnd source handler
     */
    DragSource dndSource;

    /**
     * Dnd source adapter
     */
    protected DragSourceAdapter dndSourceAdapter;

    protected TreeSelectionListener treeSelectionListener;
     
    /**
     * Creates a new Tree panel using the the specified object as root.
     *
     * @param root the object to use as root for this tree.
     */
    public TreeView(StratmasObjectAdapter root)
    {
        super((TreeModel) root);

        // Add listener that kills this tree if root's object is deleted.
        root.getStratmasObject().addEventListener(this); 

        // Add listener that ensures that added top level components
        // are shown when isRootVisible == false. And that the tree is
        // collapsed if no more objects are to be shown
        root.addTreeModelListener(new TreeModelListener() 
            {
                public void treeNodesChanged(TreeModelEvent e) {}
                public void treeNodesInserted(TreeModelEvent e) 
                {
                     if (!isRootVisible() && e.getTreePath().getPathCount() == 1) {
                         TreePath path = new TreePath(getModel().getRoot());
                         expandPath(path);
                     }
                }
                public void treeNodesRemoved(TreeModelEvent e) 
                {
                    if (!isRootVisible() && e.getTreePath().getPathCount() == 1 &&
                        getModel().getChildCount(getModel().getRoot()) <= 1) {
                         TreePath path = new TreePath(getModel().getRoot());
                         collapsePath(path);
                     }
                }
                public void treeStructureChanged(TreeModelEvent e) {}                
            });

        TreeViewCellRenderer renderer = new TreeViewCellRenderer(this);
        this.setCellRenderer(renderer);
        DefaultTreeCellEditor editor = new DefaultTreeCellEditor(this, renderer);
        this.setCellEditor(editor);
        
        initZoom();
        initDnd();

        this.addMouseListener(new MouseInputAdapter() 
            {
                public void mousePressed(MouseEvent e)
                {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        TreePath selPath = getPathForLocation(e.getX(), 
                                                              e.getY());
                        if(selPath != null) {
                            showPopup(e.getX(),
                                      e.getY(),
                                      (StratmasObjectAdapter) 
                                      selPath.getLastPathComponent());
                        } else {
                            showPopup(e.getX(),
                                      e.getY(),
                                      (StratmasObjectAdapter) 
                                      getModel().getRoot());
                        }
                    }
                }
            });
        
        treeSelectionListener = new TreeSelectionListener() {
                  public void valueChanged(TreeSelectionEvent e) {
                       TreePath[] paths = e.getPaths();
                       for (int i = 0; i < paths.length; i++) {
                            if (e.isAddedPath(paths[i])) {
                                 ((StratmasObjectAdapter) paths[i].getLastPathComponent()).setSelected(true);
                            }
                            else {
                                 ((StratmasObjectAdapter) paths[i].getLastPathComponent()).setSelected(false);
                            }
                    }
                }
             };
        this.addTreeSelectionListener(treeSelectionListener);

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
                            if (obj instanceof StratmasObject) {
                                StratmasObject so = (StratmasObject)obj;
                                StratmasObject target = pointToObject(dtde.getLocation());
                                      if (target instanceof StratmasList &&
                                          so.getType().canSubstitute(target.getType()) &&
                                          so != target &&
                                          !(so instanceof StratmasList)) {
                                          so.remove();
                                          // Default behavior when adding Activities is to give duplicate
                                          // objects a unique identifier instead of overwriting the old instance.
                                          if (so.getType().canSubstitute("Activity")) {
                                               ((StratmasList)target).addWithUniqueIdentifier(so);
                                          }
                                          else {
                                               ((StratmasList)target).add(so);
                                          }                                          
                                          dtde.dropComplete(true);
                                      } else if (target instanceof StratmasReference) {
                                          if (target.getType().isValidReferenceType(so.getType())) {
                                              ((StratmasReference) target).setValue(so.getReference(), null);
                                              dtde.dropComplete(true);
                                          } else {
                                              dtde.dropComplete(false);
                                          }
                                          
                                      } else {
                                          dtde.dropComplete(false);
                                      }
                            }
                            else {
                                 dtde.dropComplete(false);
                            }
                        }
                        else if (dtde.isDataFlavorSupported(StratmasClient.HierarchyImportSet.HIERARCHYIMPORTSET_FLAVOR)) {
                             dtde.rejectDrop();
                        }
                        else if (dtde.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                            dtde.acceptDrop(DnDConstants.ACTION_LINK);
                            dropAccepted = true;
                            String string = (String)dtde.getTransferable().getTransferData(DataFlavor.stringFlavor);
                            StratmasList stratmasList = null;
                            StratmasObject importedRoot = StratmasClient.Client.importXMLString(string);
                            if (importedRoot != null) {
                                 stratmasList = (StratmasList)importedRoot.getChild("identifiables");
                            }
                            
                            StratmasObject target = pointToObject(dtde.getLocation());
                            if (stratmasList != null && target instanceof StratmasList) {
                                StratmasList targetList = (StratmasList) target;
                                // Add to vector first to not
                                // disturb the enumeration we are
                                // using by removing objects from its collection.
                                Vector<StratmasObject> v = new Vector<StratmasObject>();
                                for (Enumeration e = stratmasList.children(); e.hasMoreElements();) {
                                    StratmasObject sObj = (StratmasObject) e.nextElement();
                                    if (sObj.getType().canSubstitute(targetList.getType())) {
                                        v.add(sObj);
                                    }
                                }
                                for (Enumeration<StratmasObject> e = v.elements(); e.hasMoreElements();) {
                                    StratmasObject sObj = e.nextElement();
                                    sObj.remove();
                                    targetList.add(sObj);
                                }
                                dtde.dropComplete(true);
                            } else {
                                dtde.dropComplete(false);
                            }
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
                    //
                    DraggedElement.setElement(null);
                }
            }));
        
        setDragEnabled(false);
        setEditable(true);
    }
    
    /**
     * Initalizes zoom actions.
     */
    protected void initZoom()
    {
        final TreeView self = this;
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 
                                                 ActionEvent.CTRL_MASK),
                          "ZoomIn");
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 
                                                 ActionEvent.CTRL_MASK), 
                          "ZoomOut");

        getActionMap().put("ZoomIn",
                           new AbstractAction("Zoom In", 
                                              new ImageIcon(TreeView.class.getResource("images/zoom_in.png"))) 
                           { 
                               /**
							 * 
							 */
							private static final long serialVersionUID = -3884381676516049432L;

							public void actionPerformed(ActionEvent e) 
                               {
                                   self.scalePreferedIconSize(1.1);
                               }
                           });
        
        getActionMap().put("ZoomOut",
                           new AbstractAction("Zoom Out", 
                                              new ImageIcon(TreeView.class.getResource("images/zoom_out.png"))) 
                           { 
                               
                               /**
							 * 
							 */
							private static final long serialVersionUID = 9118724480437588086L;

							public void actionPerformed(ActionEvent e) 
                               {
                                 self.scalePreferedIconSize(0.9);
                               }
                           });

    }

    /**
     * Initalizes dnd actions.
     */
    protected void initDnd()
    {
        dndSource = new DragSource();
        dndSource.createDefaultDragGestureRecognizer(this, 
		                                             DnDConstants.ACTION_REFERENCE, 
		                                             this);
        dndSourceAdapter = new DragSourceAdapter() {
                  public void dragDropEnd(DragSourceDropEvent dsde) {
                       if (dsde.getDropSuccess() && 
                           (dsde.getDropAction() == DnDConstants.ACTION_REFERENCE)) {
                            Debug.err.println("Drop succeded");
                       }
                  }
             };
    }
    
    /**
     * Helper function, checks if object in vector are possible to add
     * to provided object.
     *
     * @param candidates
     * @param target
     */
    protected boolean checkObjects(Vector candidates, StratmasObject target) 
    {
        boolean status = true;
        if (target.isLeaf()) {
            status = false;
        } else         if (target instanceof StratmasList) {
            StratmasList list = (StratmasList) target;
            for (Enumeration e = candidates.elements(); e.hasMoreElements();) {
                StratmasObject candidate = (StratmasObject) e.nextElement(); 
                // Check type and name conflict
                // FIXME Note that names can still conflict inside the vector.
                if (!candidate.getType().canSubstitute(list.getType()) ||
                    list.getChild(candidate.getIdentifier()) != null) {
                    status = false;
                    break;
                }
            }
        } else {
            for (Enumeration e = candidates.elements(); e.hasMoreElements();) {
                StratmasObject candidate = (StratmasObject) e.nextElement(); 
                // Check if any unfilled declaration matches
                // FIXME Note that names can still conflict inside the vector.
                // I. e. someone could provide 2 locations in this drop.
                if (target.getChild(candidate.getIdentifier()) != null ||
                    target.getType().getSubElement(candidate.getIdentifier()) == null) {
                    status = false;
                    break;
                }
            }
        }

        return status;
    }

    /**
     * Helper function, returns the StratmasObject under the
     * drop, note that the root is assumed to 'cover' everything that
     * is not another node.
     *
     * @param point point in components coordinates.
     */
    protected StratmasObject pointToObject(Point point)
    {
        return pointToObject((int) point.getX(), (int) point.getY());
    }

    /**
     * Helper function, returns the StratmasObject under the
     * drop, note that the root is assumed to 'cover' everything that
     * is not another node.
     *
     * @param x x in components coordinates.
     * @param y y in components coordinates.
     */
    protected StratmasObject pointToObject(int x, int y)
    {
        TreePath path = getPathForLocation(x, y);

        if (path != null) {
            return ((StratmasObjectAdapter) path.getLastPathComponent()).getUserObject();
        } else {
            return ((StratmasObjectAdapter) getModel().getRoot()).getUserObject();
        }
    }

    /**
     * Helper function returns the StratmasObjects in the
     * transferable, or null if no such.
     *
     * @param transferable transferable to extract.
    */
    public Vector<StratmasObject> transferableToObjects(Transferable transferable)
    {
        Vector<StratmasObject> res = new Vector<StratmasObject>();
        DataFlavor[] flavors = transferable.getTransferDataFlavors();        
        try {
            for (int i = 0; i < flavors.length; i++) {
                if (transferable.isDataFlavorSupported(flavors[i])) {
                    res.add((StratmasObject) 
                            transferable.getTransferData(flavors[i]));                
                }
            }
        } catch (UnsupportedFlavorException e) {
            return null;
        } catch (IOException e) {
            return null;
        }

        return res;
    }

    /*
     * Shows a popup for the specified StratmasObjectAdapter at the
     * specified place.
     * 
     * @param x where to place the popup.
     * @param y where to place the popup.
     * @param objectAdapter the StratmasObjectAdapter for which the
     * popup is shown.
     */
    protected void showPopup(int x, int y, 
                             StratmasObjectAdapter objectAdapter)
    {
        final StratmasObject sObj = objectAdapter.getUserObject();
        final TreeView self = this;

        //        Vector actions = sObj.getActions();
        JPopupMenu popup = new JPopupMenu(sObj.getIdentifier());

        popup.add(new AbstractAction("View branch in separate tree")
            {
                /**
				 * 
				 */
				private static final long serialVersionUID = 3283889135596655736L;

				public void actionPerformed(ActionEvent e)
                {
                    final TreeViewFrame frame = 
                        TreeView.getDefaultFrame(sObj);
                    frame.setEditable(isEditable());
                    if (self.getTopLevelAncestor() instanceof ClientMainFrame) {
                        ((ClientMainFrame) self.getTopLevelAncestor()).tabFrame(frame);
                    } else {
                        javax.swing.SwingUtilities.invokeLater(new Runnable() 
                            {
                                public void run() 
                                {
                                    frame.setVisible(true);
                                }
                            });
                    }
                }
            });
        popup.add(new AbstractAction("View only " + sObj.getType().getName() + "(s)")
            {
                /**
				 * 
				 */
				private static final long serialVersionUID = -8506687345435322101L;
				final TypeFilter filter = new TypeFilter(sObj.getType());
                public void actionPerformed(ActionEvent e)
                {
                    final TreeViewFrame frame = 
                        TreeView.getDefaultFrame(sObj, filter);
                    frame.setEditable(isEditable());
                    if (self.getTopLevelAncestor() instanceof ClientMainFrame) {
                        ((ClientMainFrame) self.getTopLevelAncestor()).tabFrame(frame);
                    } else {
                        javax.swing.SwingUtilities.invokeLater(new Runnable() 
                            {
                                public void run() 
                                {
                                    frame.setVisible(true);
                                }
                            });
                    }
                }
            });

        popup.add(new javax.swing.JSeparator());
        
//         for (Enumeration e = actions.elements(); e.hasMoreElements();) {
//             StratmasAbstractAction action = (StratmasAbstractAction)
//                 e.nextElement();
//             if (!isEditable()) {
//                 if (action.isMutator()) {
//                     action.setEnabled(false);
//                 }
//             }
//             popup.add(action);
//         }
        
        sObj.getActionGroup().addToPopupMenu(popup, isEditable());

        popup.show(this, x, y);
    }
    
    
     /**
      * Gets the StratmasObject pointed to by the provided path. This
      * method is called in the dragGestureRecognized method. Its
      * purpose is to be overridden by TreeView descendants in order
      * to override the 'MOVE' default behavior for drag
      * operations. This should be implemented differently as soon as
      * Apple fixes the DnD implementation that obviously is broken in
      * 1.4.2.
      *
      * @param path The path to fetch the StratmasObject for.
      * @return The StratmasObject pointed to by path or null if the
      * path is invalid or no such object could be found.
      */
     public StratmasObject getObjectForPath(TreePath path) {
          if (path != null) {
               StratmasObjectAdapter soa =  (StratmasObjectAdapter)path.getLastPathComponent();
               if (soa != null) {
                    return soa.getUserObject();
               }
          }
          return null;
     }

    /*
     * Drag Gesture handler.
     */
    public void dragGestureRecognized(DragGestureEvent dge) 
    {
        TreePath path = getPathForLocation((int)dge.getDragOrigin().getX(), 
                                           (int)dge.getDragOrigin().getY());
        if (path == null) {
            // We can't move an empty selection
            return;
        }

        // Apple's dnd implementation sucks. Must have a selection
        // when starting to drag.
        if (isSelectionEmpty()) {
             setSelectionPath(path);
        }

        // Convenient to have a method for this that may be overriden
        // in subclasses in order to override the default MOVE
        // behavior of drag operations.
         Transferable transferable = getObjectForPath(path);
        
        // start drag
        try {
            // define cursor for the object 
            Cursor c;
            Toolkit tk = Toolkit.getDefaultToolkit();
            Image image = ((Icon)((StratmasObject) transferable).getIcon()).getImage();
            Dimension bestsize = tk.getBestCursorSize(image.getWidth(null),image.getHeight(null));
            if (bestsize.width != 0) {
                c = tk.createCustomCursor(image, new java.awt.Point(bestsize.width/2, bestsize.height/2), 
                                          ((StratmasObject) transferable).toString());
            }
            else {
                c = Cursor.getDefaultCursor();
            }
            // set the dragged element
            DraggedElement.setElement((StratmasObject)transferable);
            //start the drag
            dndSource.startDrag(dge, 
                                c, 
                                transferable, 
                                dndSourceAdapter);
        } catch (java.awt.dnd.InvalidDnDOperationException e) {
            Debug.err.println("InvalidDnDOperationException caught in TreeView dragGestuerRecognized: " + e);
            
        }
    }


    /**
     * Called by the renderers to convert the specified value to text.
     * @param value the Object to convert to text
     * @param selected true if the node is selected
     * @param expanded true if the node is expanded
     * @param leaf true if the node is a leaf node
     * @param row an integer specifying the node's display row, where
     * 0 is the first row in the display
     * @param hasFocus true if the node has the focus
     */    
    public String convertValueToText(Object value, boolean selected, 
                                     boolean expanded, boolean leaf, 
                                     int row, boolean hasFocus)
    {
        return ((StratmasObjectAdapter) value).toEditableString();
    }


    /**
     * Creates a new Tree window using the the specified object as root.
     *
     * @param root the object to use as root for this tree.
     */    
    public static TreeViewFrame getDefaultFrame(StratmasObject root)
    {
        TreeViewFrame res = new TreeViewFrame(getDefaultTreeView(root));
        return res;
    }

    /**
     * Creates a new Tree window using the the specified object as root.
     * filtered using the specified filter.
     * @param root the object to use as root for this tree.
     * @param filter the object to use as root for this tree.
     */    
    public static TreeViewFrame getDefaultFrame(StratmasObject root, 
                                                StratmasObjectFilter filter)
    {        
        TreeViewFrame res = new TreeViewFrame(getDefaultTreeView(root, 
                                                                 filter));
        return res;
    }

    /**
     * Creates the default component used for visualizing a TreeView
     * of the the specified object as root and defaults for all else..
     *
     * @param root the object to use as root for this tree.
     */
    public static TreeView getDefaultTreeView(StratmasObject root)
    {
        final TreeView view = new TreeView(StratmasObjectAdapter.getStratmasObjectAdapter(root));
        view.setShowsRootHandles(false);
        if (root instanceof StratmasList) {
            view.setRootVisible(true);
        }

        return view;
    }

    /**
     * Creates the default component used for visualizing a TreeView
     * of the the specified object as root and defaults for all else..
     *
     * @param root the object to use as root for this tree.
     */
    public static TreeView getDefaultTreeView(StratmasObject root, 
                                                StratmasObjectFilter filter)
    {
        TreeView view = new TreeView(StratmasObjectAdapter.getStratmasObjectAdapter(root, filter));
        view.setShowsRootHandles(false);
        // By defualt, dont show root handle for lists.
        if (root instanceof StratmasList) {
            view.setRootVisible(false);
        }
        
        return view;
    }


    /**
     * Symmetrically scales the prefered width and height of icons
     * used in the TreeCellRenderes. Note that if scale > 1 (<1) the
     * icon will always increase (decrease) with at least one pixel.
     *
     * @param scale the height of the icons.
     */
    public void scalePreferedIconSize(double scale)
    {
        int newWidth = (int) (preferedIconSize.getWidth() * scale);
        int newHeight = (int) (preferedIconSize.getHeight() * scale);
        
        // make sure we actually increase/decrease
        if (scale > 1) {
            if (newWidth == (int) preferedIconSize.getWidth()) {
                newWidth++;
            }
            if (newHeight == (int) preferedIconSize.getHeight()) {
                newHeight++;
            }
        } else if (scale < 1) {
            if (newWidth == (int) preferedIconSize.getWidth()) {
                newWidth--;
            }
            if (newHeight == (int) preferedIconSize.getHeight()) {
                newHeight--;
            }
        }

        setPreferedIconSize(newWidth, newHeight);
    }

    /**
     * Sets the prefered width and height of icons used in the
     * TreeCellRenderes if width <= 0 || height <= 0 nothing will
     * happen.
     *
     * @param width the width of the icons.
     * @param height the height of the icons.
     */
    public void setPreferedIconSize(int width, int height)
    {
        if (width > 0 && height > 0) {
            preferedIconSize.setSize(width, height);
            
            setRowHeight((int) preferedIconSize.getHeight());
            validate();
        }
    }

    /**
     * Gets the prefered width and height of icons used in the
     * TreeCellRenderes
     */
    public Dimension getPreferedIconSize()
    {
        return preferedIconSize;
    }

     // TEMPORARY FOR DEBUGGING!!!
     public String actionToString(int action) {
          switch (action) {
          case DnDConstants.ACTION_COPY:
               return "ACTION_COPY";
          case DnDConstants.ACTION_COPY_OR_MOVE:
               return "ACTION_COPY_OR_MOVE";
          case DnDConstants.ACTION_LINK:
               return "ACTION_LINK";
          case DnDConstants.ACTION_MOVE:
               return "ACTION_MOVE";
          case DnDConstants.ACTION_NONE:
               return "ACTION_NONE";
          }
          return "Unknown!!! = " + action;
     }

    
    /**
     * Invoked the first time a window is made visible.
     */
    public void windowOpened(WindowEvent e)
    {
        //Not interested
    }

    /**
     * Invoked when the user attempts to close the window
     * from the window's system menu.
     */
    public void windowClosing(WindowEvent e)
    {
        //Not interested
    }

    /**
     * Invoked when a window has been closed as the result
     * of calling dispose on the window.
     */
    public void windowClosed(WindowEvent e)
    {
        // Remove listeners
        e.getWindow().removeWindowListener(this);
        StratmasObjectAdapter root = (StratmasObjectAdapter) getModel().getRoot();
        StratmasObject rootObj = root.getStratmasObject();
        if (rootObj != null) {
             rootObj.removeEventListener(this);
        }
        // Kill off the adapters
        ((StratmasObjectAdapter) getModel().getRoot()).dispose();
//        setModel(null);
    }

    /**
     * Invoked when a window is changed from a normal to a
     * minimized state. For many platforms, a minimized window 
     * is displayed as the icon specified in the window's 
     * iconImage property.
     * @see java.awt.Frame#setIconImage
     */
    public void windowIconified(WindowEvent e)
    {
        //Not interested
    }

    /**
     * Invoked when a window is changed from a minimized
     * to a normal state.
     */
    public void windowDeiconified(WindowEvent e)
    {
        //Not interested
    }

    /**
     * Invoked when the Window is set to be the active Window. Only a Frame or
     * a Dialog can be the active Window. The native windowing system may
     * denote the active Window or its children with special decorations, such
     * as a highlighted title bar. The active Window is always either the
     * focused Window, or the first Frame or Dialog that is an owner of the
     * focused Window.
     */
    public void windowActivated(WindowEvent e)
    {
        //Not interested
    }

    /**
     * Invoked when a Window is no longer the active Window. Only a Frame or a
     * Dialog can be the active Window. The native windowing system may denote
     * the active Window or its children with special decorations, such as a
     * highlighted title bar. The active Window is always either the focused
     * Window, or the first Frame or Dialog that is an owner of the focused
     * Window.
     */
    public void windowDeactivated(WindowEvent e)
    {
        //Not interested
    }

    /**
     * Called when this window is added to a container.
     */
    public void addNotify()
    {
        super.addNotify();
        Container container = getTopLevelAncestor();
        if (container instanceof Window) {
            if (container == this.currentWindow) {
                // No change.
            } else {
                if (this.currentWindow != null) {
                    this.currentWindow.removeWindowListener(this);
                }
                this.currentWindow = (Window) container;
                currentWindow.addWindowListener(this);
            }
        } else {
            this.currentWindow.removeWindowListener(this);
            this.currentWindow = null;
        }
    }

    /**
     * Called when this window is removed from a container.
     */
    public void removeNotify()
    {
        super.removeNotify();
        Container container = getTopLevelAncestor();
        if (container instanceof Window) {
            if (container == this.currentWindow) {
                // No change.
            } else {
                if (this.currentWindow != null) {
                    this.currentWindow.removeWindowListener(this);
                }
                this.currentWindow = (Window) container;
                currentWindow.addWindowListener(this);
            }
        } else {
            this.currentWindow.removeWindowListener(this);
            this.currentWindow = null;
        }
    }

    /**
     * Ensures that the 
     */
    public void eventOccured(StratmasEvent event)
    {
        if (event.isRemoved()) {
            if (getParent() != null) {
                getParent().remove(this);
            }
            ((StratmasObject) event.getSource()).removeEventListener(this);
        } else if (event.isReplaced()) {
            throw new AssertionError("Replace behavior not implemented");
        } 
        
    }
}

class TreeViewCellRenderer extends DefaultTreeCellRenderer
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 6437329528204104986L;
	/**
     * The treeView this renderer renders for.
     */
    TreeView treeView;

    /**
     * Creates a new TreeViewCellRenderer for use with the specified
     * TreeView.
     *
     * @param treeView the treeView this renderer will render for.
     */
    TreeViewCellRenderer(TreeView treeView)
    {
        this.treeView = treeView;
    }

    /**
     * Returns the component representation of the specified value for
     * the specified tree.
     */
    public java.awt.Component getTreeCellRendererComponent(JTree tree,
                                                           Object value,
                                                           boolean sel,
                                                           boolean expanded,
                                                           boolean leaf,
                                                           int row,
                                                           boolean hasFocus) 
    {
        StratmasObjectAdapter val = (StratmasObjectAdapter) value;
        
        this.hasFocus = hasFocus;

        String tag = val.getTextTag();
        if (tag != null) {
            setText(tag);
        }

        if (sel) {
            setForeground(getTextSelectionColor());
        } else {
            setForeground(getTextNonSelectionColor());
        }
        
        StratmasClient.Icon icon = val.getIcon();
        if (icon != null) {
            icon = icon.getScaledInstance(treeView.getPreferedIconSize());
            setIcon(icon);
            setLeafIcon(icon);
            setOpenIcon(icon);
            setClosedIcon(icon);
        }
        
        setComponentOrientation(tree.getComponentOrientation());
            
        selected = sel;
        
        return this;
    }
}

