//         $Id: TreeView.java,v 1.3 2006/03/31 16:55:52 dah Exp $

/*
 * @(#)TreeView.java
 */

package StratmasClient.treeview2;

import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasList;
import StratmasClient.object.StratmasAbstractAction;
import StratmasClient.ClientMainFrame;
import StratmasClient.Debug;

import StratmasClient.filter.StratmasObjectFilter;
import StratmasClient.filter.TypeFilter;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JTree;
import javax.swing.JSeparator;
import javax.swing.JPopupMenu;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;

import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.MouseInputAdapter;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;

import java.awt.Point;
import java.awt.Dimension;

/**
 * TreeView is presentation of a tree of StratmasObjects.
 *
 * @version 2, $Date: 2006/03/31 16:55:52 $
 * @author  Daniel Ahlin
*/
public class TreeView extends JTree
{
    /**
     * The prefered size of icons used in the TreeCellRenderers.
     */
    Dimension preferedIconSize = new Dimension(16, 16);

    /**
     * Creates a new Tree panel using the the specified object as root.
     *
     * @param root the object to use as root for this tree.
     */
    public TreeView(TreeViewModel root)
    {
        super(root);
        
        setCellRenderer(new TreeViewCellRenderer());
        //setCellEditor(new TreeViewCellEditor());
        initZoom();
        initMouseListener();
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        setEditable(true);
        setTransferHandler(new StratmasTransferHandler());
        setDragEnabled(true);

        getInputMap().put(KeyStroke.getKeyStroke("ctrl X"),
                          getTransferHandler().getCutAction().getValue(Action.NAME));
        getInputMap().put(KeyStroke.getKeyStroke("ctrl C"),
                          getTransferHandler().getCopyAction().getValue(Action.NAME));
        getInputMap().put(KeyStroke.getKeyStroke("ctrl V"),
                          getTransferHandler().getPasteAction().getValue(Action.NAME));
        getActionMap().put(getTransferHandler().getCutAction().getValue(Action.NAME),
                           getTransferHandler().getCutAction());
        getActionMap().put(getTransferHandler().getCopyAction().getValue(Action.NAME),
                           getTransferHandler().getCopyAction());
        getActionMap().put(getTransferHandler().getPasteAction().getValue(Action.NAME),
                           getTransferHandler().getPasteAction());
    }

    /**
     * Registers a mouse listener that calls contextAction when a
     * click defined as showPopup happens.
     */
    void initMouseListener()
    {
        this.addMouseListener(new MouseInputAdapter() 
            {
                public void mousePressed(MouseEvent e)
                {
                    if (e.isPopupTrigger()) {
                            showPopup(e.getX(),
                                      e.getY());
                    }
                }
            });
    }
    
    /**
     * Initalizes zoom actions.
     */
    protected void initZoom()
    {
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 
                                                 ActionEvent.CTRL_MASK),
                          "ZoomIn");
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 
                                                 ActionEvent.CTRL_MASK), 
                          "ZoomOut");
        
        getActionMap().put("ZoomIn",
                           new AbstractAction("Zoom In", 
                                              new ImageIcon(StratmasClient.treeview.TreeView.class.getResource("images/zoom_in.png"))) 
                           {
                               public void actionPerformed(ActionEvent e) 
                               {
                                   scalePreferedIconSize(1.1);
                               }
                           });
        
        getActionMap().put("ZoomOut",
                           new AbstractAction("Zoom Out", 
                                              new ImageIcon(StratmasClient.treeview.TreeView.class.getResource("images/zoom_out.png"))) 
                           { 
                               
                               public void actionPerformed(ActionEvent e) 
                               {
                                   scalePreferedIconSize(0.9);
                               }
                           });
    }

    /**
     * Helper function, returns the StratmasObject under the
     * point, note that the root is assumed to 'cover' everything that
     * is not another node.
     *
     * @param point point in components coordinates.
     */
    protected Object pointToObject(Point point)
    {
        return pointToObject((int) point.getX(), (int) point.getY());
    }

    /**
     * Helper function, returns the StratmasObject under the point,
     * note that the root is assumed to 'cover' everything that is not
     * another node.
     *
     * @param x x in components coordinates.
     * @param y y in components coordinates.
     */
    protected Object pointToObject(int x, int y)
    {
        TreePath path = getPathForLocation(x, y);

        if (path != null) {
            return path.getLastPathComponent();
        } else {
            return getModel().getRoot();
        }
    }

    /**
     * Shows a popup for the specified StratmasObjectAdapter at the
     * specified place.
     * 
     * @param x where to place the popup.
     * @param y where to place the popup.
     */
    protected void showPopup(int x, int y)
    {
        JPopupMenu popup = createPopup(pointToObject(x, y));
        if (popup != null) {
            popup.show(this, x, y);
        }
    }

    /**
     * Creates a popup for StratmasObjects
     *
     * @param object the object
     */
    public JPopupMenu createPopup(StratmasObject object)
    {
        final StratmasObject sObj = object;

        
        Vector actions = sObj.getActions();
        JPopupMenu popup = new JPopupMenu(sObj.getIdentifier().toString());
        
        popup.add(new AbstractAction("View branch in separate tree")
            {
                public void actionPerformed(ActionEvent e)
                {
                    final TreeViewFrame frame = 
                        TreeView.getDefaultFrame(sObj);
                    frame.setEditable(isEditable());
                    if (getTopLevelAncestor() instanceof ClientMainFrame) {
                        ((ClientMainFrame) getTopLevelAncestor()).tabFrame(frame);
                    } else {
                        SwingUtilities.invokeLater(new Runnable() 
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
                TypeFilter filter = new TypeFilter(sObj.getType());
                public void actionPerformed(ActionEvent e)
                {
                    final TreeViewFrame frame = 
                        TreeView.getDefaultFrame(sObj, filter);
                    frame.setEditable(isEditable());
                    if (getTopLevelAncestor() instanceof ClientMainFrame) {
                        ((ClientMainFrame) getTopLevelAncestor()).tabFrame(frame);
                    } else {
                        SwingUtilities.invokeLater(new Runnable() 
                            {
                                public void run() 
                                {
                                    frame.setVisible(true);
                                }
                            });
                    }
                }
            });
            
        popup.add(new JSeparator());
        
        // Add cut and copy actions.
        popup.add(getTransferHandler().getCutAction());
        popup.add(getTransferHandler().getCopyAction());
        popup.add(new JSeparator());
        
        
        
        for (Enumeration e = actions.elements(); e.hasMoreElements();) {
            StratmasAbstractAction action = (StratmasAbstractAction)
                e.nextElement();
            if (!isEditable()) {
                if (action.isMutator()) {
                    action.setEnabled(false);
                }
            }
            popup.add(action);
        }

        return popup;
    }

    /**
     * Creates a popup for objects
     *
     * @param object the object
     */
    public JPopupMenu createPopup(Object object)
    {
        if (object instanceof StratmasObject) {
            return createPopup((StratmasObject) object);
        } else {
            return null;
        }
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
    
    /**
     * Creates a new Tree window using the the specified object as root.
     *
     * @param root the object to use as root for this tree.
     */    
    public static TreeViewFrame getDefaultFrame(StratmasObject root)
    {
        return new TreeViewFrame(getDefaultTreeView(root));
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
        return new TreeViewFrame(getDefaultTreeView(root, filter));
    }

    /**
     * Creates the default component used for visualizing a TreeView
     * of the the specified object as root and defaults for all else..
     *
     * @param root the object to use as root for this tree.
     */
    public static TreeView getDefaultTreeView(StratmasObject root)
    {
        final TreeView view = new TreeView(new TreeViewModel(root));
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
        TreeView view = new TreeView(new TreeViewModel(root));
        view.setShowsRootHandles(false);
        // By defualt, dont show root handle for lists.
        if (root instanceof StratmasList) {
            view.setRootVisible(false);
        }
        
        return view;
    }

}
