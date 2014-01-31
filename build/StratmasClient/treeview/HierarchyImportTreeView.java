package StratmasClient.treeview;


import StratmasClient.Debug;
import StratmasClient.Icon;
import StratmasClient.HierarchyImportSet;
import StratmasClient.object.StratmasList;
import StratmasClient.object.StratmasObject;
import StratmasClient.filter.StratmasObjectFilter;
import StratmasClient.map.DraggedElement;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;


/**
 * HierarchyImportTreeView is presentation of a tree of StratmasObjects
 * imported from Hierarchy.
 *
 * @version 1, $Date: 2006/05/22 09:54:18 $
 * @author  Per Alexius
 */
public class HierarchyImportTreeView extends TreeView {
     /**
      * The import set used to transfer selected unused objects to the
      * simulation.
      */
     private HierarchyImportSet mImportSet;

     /**
      * Must save default selection color so we can reset selection
      * color in the TreeCellRenderer.
      */
     protected Color mSelectionColor;

     /**
      * Creates a new Tree panel using the the specified object as root.
      *
      * @param importset the importset to use as root for this tree.
      */
     public HierarchyImportTreeView(HierarchyImportSet importset) {
	  super(importset.getRootAdapter());
	  setExpandsSelectedPaths(false);
	  mImportSet = importset;

	  setSelectionModel(new javax.swing.tree.DefaultTreeSelectionModel() {
		    public void setSelectionPath(TreePath path) {
			 Vector paths = new Vector();
			 for (TreePath tp = path; tp != null; tp = tp.getParentPath()) {
			      paths.add(tp);
			 }
			 if (isCollapsed(path)) {
			      getSubPaths(path, paths);
			 }
			 else {
			      paths.add(path);
			 }
			 TreePath[] tmp = new TreePath[paths.size()];
			 paths.copyInto(tmp);
			 super.setSelectionPaths(tmp);
		    }
		    public void addSelectionPath(TreePath path) {
			 Vector paths = new Vector();
			 for (TreePath tp = path; tp != null; tp = tp.getParentPath()) {
			      paths.add(tp);
			 }
			 if (isCollapsed(path)) {
			      getSubPaths(path, paths);
			 }
			 else {
			      paths.add(path);
			 }
			 TreePath[] tmp = new TreePath[paths.size()];
			 paths.copyInto(tmp);
			 super.addSelectionPaths(tmp);
		    }
		    public void removeSelectionPath(TreePath path) {
			 Vector paths = new Vector();
			 getSubPaths(path, paths);
			 TreePath[] tmp = new TreePath[paths.size()];
			 paths.copyInto(tmp);
			 super.removeSelectionPaths(tmp);
		    }
		    private void getSubPaths(TreePath path, Vector paths) {
			 StratmasObjectAdapter soa = (StratmasObjectAdapter)path.getLastPathComponent();
			 for (Enumeration en = soa.children(); en.hasMoreElements(); ) {
			      getSubPaths( ((StratmasObjectAdapter)en.nextElement()).getTreePath(), paths);
			 }
			 paths.add(path);
		    }
	       });


	  // Must save default selection color so we can reset
	  // selection color in the TreeCellRenderer.
	  mSelectionColor = (new DefaultTreeCellRenderer()).getBackgroundSelectionColor();

	  // Use our 'tailored' cell renderer.
	  setCellRenderer(new HierarchyImportTreeViewCellRenderer(this));

	  getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

	  final HierarchyImportTreeView self = this;

	  dndSourceAdapter = new DragSourceAdapter() {
		    public void dragDropEnd(DragSourceDropEvent dsde) {
			 if (dsde.getDropSuccess() && dsde.getDropAction() == DnDConstants.ACTION_LINK) {
			      self.repaint();
			 }
		    }
	       };

	  // Should not be able to edit imported units.
	  setEditable(false);
     }
    
     /**
      * Drag Gesture handler.
      */
     public void dragGestureRecognized(DragGestureEvent dge) {
	  Transferable transferable = mImportSet;

	  TreePath path = getPathForLocation((int)dge.getDragOrigin().getX(), 
					     (int)dge.getDragOrigin().getY());
	  if (path == null) {
	       Debug.err.println("Empty path in dragGestureReco...!");
	       // We can't move an empty selection
	       return;
	  }

	  HierarchyObjectAdapter hoa =  (HierarchyObjectAdapter)path.getLastPathComponent();
	  if (hoa == null) {
	       Debug.err.println("No HOA for path in dragGestureReco...!");
	       return;
	  }

	  // define cursor for the object 
	  Cursor c = Cursor.getDefaultCursor();

	  if (hoa.isUsed()) {
	       c = DragSource.DefaultLinkNoDrop;
	  }
	  else {
	       Toolkit tk = Toolkit.getDefaultToolkit();
	       Image image = ((Icon)hoa.getUserObject().getIcon()).getImage();
	       Dimension bestsize = tk.getBestCursorSize(image.getWidth(null),image.getHeight(null));
	       if (bestsize.width != 0) {
		    c = tk.createCustomCursor(image, new java.awt.Point(bestsize.width/2, bestsize.height/2), 
					      hoa.getUserObject().toString());
	       }
	  }
	  dndSource.startDrag(dge, 
			      c, 
			      transferable, 
			      dndSourceAdapter);
     }
     
     public void unselectImportedElements() {
	  mImportSet.destroy();
     }

     /**
      * Creates a new Tree window using the the specified object as root.
      * filtered using the specified filter.
      * @param root the object to use as root for this tree.
      * @param filter the object to use as root for this tree.
      */    
     public static TreeViewFrame getDefaultFrame(StratmasObject root, StratmasObjectFilter filter) {	
	  final HierarchyImportTreeView view = (HierarchyImportTreeView)getDefaultTreeView(root, filter);
	  TreeViewFrame res = new TreeViewFrame(view);
	  // Add listener that kills the pane if the frame is disposed
	  res.addWindowListener(new WindowAdapter() {
		    public void windowClosed(WindowEvent e) {
			 view.unselectImportedElements();
		    }
	       });
	  return res;
     }
     
     
     /**
      * Creates the default component used for visualizing a TreeView
      * of the the specified object as root and defaults for all else..
      *
      * @param root the object to use as root for this tree.
      */
     public static TreeView getDefaultTreeView(StratmasObject root, StratmasObjectFilter filter) {
	  HierarchyImportTreeView view =
	       new HierarchyImportTreeView(new HierarchyImportSet(new HierarchyObjectAdapter(root, filter)));
	  view.setShowsRootHandles(false);
	  // By defualt, don't show root handle for lists.
	  if (root instanceof StratmasList) {
	       view.setRootVisible(false);
	  }
	  return view;
     }
}

class HierarchyImportTreeViewCellRenderer extends DefaultTreeCellRenderer {
     /**
      * The HierarchyImportTreeView this renderer renders for.
      */
     HierarchyImportTreeView mTreeView;

     /**
      * Creates a new TreeViewCellRenderer for use with the specified
      * TreeView.
      *
      * @param treeView the treeView this renderer will render for.
      */
     HierarchyImportTreeViewCellRenderer(HierarchyImportTreeView treeView) {
	  mTreeView = treeView;
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
	       HierarchyObjectAdapter val = (HierarchyObjectAdapter)value;
	
	       this.hasFocus = val.getParent() == null;
	       setText(val.getTextTag());

	       // Used objects are gray, selected unused objects uses
	       // 'default selection color'
	       if (val.isUsed()) {
		    setBackgroundSelectionColor(Color.LIGHT_GRAY);
	       }
	       else {
		    setBackgroundSelectionColor(mTreeView.mSelectionColor);
	       }
	       
	       StratmasClient.Icon icon = val.getIcon().getScaledInstance(mTreeView.getPreferedIconSize());

	       setIcon(icon);
	       setLeafIcon(icon);
	       setOpenIcon(icon);
	       setClosedIcon(icon);
	       
	       setComponentOrientation(tree.getComponentOrientation());
	       
	       // Get the renderer to render our 'selected' or 'used'
	       // background color.
	       selected = val.isSelected() || val.isUsed();
	       
	       return this;
	  }
}
