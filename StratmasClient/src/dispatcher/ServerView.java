//         $Id: ServerView.java,v 1.3 2006/01/25 15:37:14 alexius Exp $

/*
 * @(#)ServerView.java
 */

package StratmasClient.dispatcher;

import StratmasClient.Client;
import StratmasClient.StratmasDialog;

import java.util.Enumeration;
import java.util.Vector;
import java.awt.event.KeyEvent;
import java.awt.Image;

import javax.swing.JTree;
import javax.swing.JMenuBar;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JComponent;
import javax.swing.tree.TreeModel;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.JPopupMenu;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreePath;
import javax.swing.ProgressMonitor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputAdapter;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.awt.dnd.DnDConstants;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;

/**
 * ServerView is presentation of servers.
 *
 * @version 1, $Date: 2006/01/25 15:37:14 $
 * @author  Daniel Ahlin
*/

public class ServerView extends JTree
{
    /**
     * The default prefered width of icons used in the TreeCellRenderers.
     */
    private static final int DEFAULT_PREFERED_ICON_WIDTH = 16;
    /**
     * The default prefered height of icons used in the TreeCellRenderers.
     */
    private static final int DEFAULT_PREFERED_ICON_HEIGHT = 16;

    /**
     * Image for busy server.
     */
    Image busyServerImage =  new ImageIcon(ServerView.class.getResource("images/busy_server.png")).getImage();
    
    /**
     * Image for available server.
     */
    Image availableServerImage = new ImageIcon(ServerView.class.getResource("images/server.png")).getImage();

    /**
     * The prefered width of icons used in the TreeCellRenderers.
     */
    int preferedIconWidth = DEFAULT_PREFERED_ICON_WIDTH;
    /**
     * The prefered height of icons used in the TreeCellRenderers.
     */
    int preferedIconHeight = DEFAULT_PREFERED_ICON_HEIGHT;

    /**
     * The dispatcher backing this tree.
     */
    StratmasDispatcher stratmasDispatcher;

    /**
     * Creates a new Tree panel using the the specified dispatcher for
     * information.
     *
     * @param dispatcher the dispatcher this tree is a view of.
     */
    public ServerView(StratmasDispatcher dispatcher)
    {
        super(dispatcher.getServers());
        final ServerView self = this;
        this.stratmasDispatcher = dispatcher;

        // Add listener that ensures that added top level components
        // are shown when isRootVisible == false. And that the tree is
        // collapsed if no more objects are to be shown
        getModel().addTreeModelListener(new TreeModelListener() 
            {
                public void treeNodesChanged(TreeModelEvent e) {}
                public void treeNodesInserted(TreeModelEvent e) 
                {
                     if (!self.isRootVisible() && e.getTreePath().getPathCount() == 1) {
                         TreePath path = new TreePath(self.getModel().getRoot());
                         self.expandPath(path);
                     }
                }
                public void treeNodesRemoved(TreeModelEvent e) 
                {
                    if (!self.isRootVisible() && e.getTreePath().getPathCount() == 1 &&
                        self.getModel().getChildCount(self.getModel().getRoot()) <= 1) {
                         TreePath path = new TreePath(self.getModel().getRoot());
                         self.collapsePath(path);
                     }
                }
                public void treeStructureChanged(TreeModelEvent e) {}                
            });

        ServerViewCellRenderer renderer = 
            new ServerViewCellRenderer(this, 
                                       new ImageIcon(busyServerImage.getScaledInstance(getPreferedIconWidth(), getPreferedIconHeight(), Image.SCALE_SMOOTH)),
                                       new ImageIcon(availableServerImage.getScaledInstance(getPreferedIconWidth(), getPreferedIconHeight(), Image.SCALE_SMOOTH)));
        this.setCellRenderer(renderer);
                        
        initActions();

        this.addMouseListener(new MouseInputAdapter() 
            {
                public void mousePressed(MouseEvent e)
                {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        TreePath selPath = self.getPathForLocation(e.getX(), 
                                                                   e.getY());
                        if(selPath != null) {
                            self.showPopup(e.getX(),
                                           e.getY(),
                                           (DefaultMutableTreeNode) 
                                           selPath.getLastPathComponent());
                        } else {
                            self.showPopup(e.getX(),
                                           e.getY(),
                                           (DefaultMutableTreeNode)
                                           self.getModel().getRoot());
                        }
                    }
                }
            });
    }
    
    /**
     * Initalizes actions.
     */
    protected void initActions()
    {
        final ServerView self = this;
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 
                                                 ActionEvent.CTRL_MASK),
                          "ZoomIn");
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 
                                                 ActionEvent.CTRL_MASK), 
                          "ZoomOut");

        getActionMap().put("ZoomIn",
                           new AbstractAction("Zoom In", 
                                              new ImageIcon(ServerView.class.getResource("images/zoom_in.png"))) 
                           { 
                               public void actionPerformed(ActionEvent e) 
                               {
                                   scalePreferedIconSize(1.1);
                               }
                           });
        
        getActionMap().put("ZoomOut",
                           new AbstractAction("Zoom Out", 
                                              new ImageIcon(ServerView.class.getResource("images/zoom_out.png"))) 
                           { 
                               
                               public void actionPerformed(ActionEvent e) 
                               {
                                   scalePreferedIconSize(0.9);
                               }
                           });
        getActionMap().put("Refresh",
                           new AbstractAction("Refresh", 
                                              new ImageIcon(new ImageIcon(ServerView.class.getResource("images/refresh.png")).getImage().getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH))) 
                           { 
                               
                               public void actionPerformed(ActionEvent e) 
                               {
                                   Thread worker = new Thread()
                                       {
                                           public void run()
                                           {
                                               refreshServers();
                                           }
                                       };
                                   worker.start();
                                   Thread monitor = new Thread()
                                       {
                                           public void run()
                                           {
                                               ProgressMonitor widget = 
                                                     new ProgressMonitor(self, 
                                                                     "Fetching server list", 
                                                                     "", 0, 100); 
                                           }
                                       };
                                   monitor.start();

                                   
                               }
                           });
    }

    /**
     * Refreshes the list of servers.
     */
    public void refreshServers() 
    {
        DefaultMutableTreeNode newRoot = new DefaultMutableTreeNode("root");
        JTree.DynamicUtilTreeNode.createChildren(newRoot, getStratmasDispatcher().getServers());
        DefaultTreeModel newModel =  new DefaultTreeModel(newRoot, false);
        setModel(newModel);
    }

    /**
     * Helper function, returns the StratmasObject under the
     * drop, note that the root is assumed to 'cover' everything that
     * is not another node.
     *
     * @param point point in components coordinates.
     */
    protected DefaultMutableTreeNode pointToObject(Point point)
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
    protected DefaultMutableTreeNode pointToObject(int x, int y)
    {
        TreePath path = getPathForLocation(x, y);

        if (path != null) {
            return ((DefaultMutableTreeNode) path.getLastPathComponent());
        } else {
            return ((DefaultMutableTreeNode) getModel().getRoot());
        }
    }

    /*
     * Shows a popup for the specified StratmasServerAdapter at the
     * specified place.
     * 
     * @param x where to place the popup.
     * @param y where to place the popup.
     * @param objectAdapter the StratmasServerAdapter for which the
     * popup is shown.
     */
    protected void showPopup(int x, int y, DefaultMutableTreeNode objectAdapter)
    {
        if (objectAdapter.getUserObject() instanceof StratmasServer) {        
            JPopupMenu popup = new JPopupMenu(objectAdapter.toString());
            final StratmasServer server = (StratmasServer) objectAdapter.getUserObject();
            if (server.getSimulations().size() != 0) {
                for (Enumeration e = server.getSimulations().elements(); 
                     e.hasMoreElements();) {
                    AbstractAction action = new AbstractAction(e.nextElement().toString())
                        {
                            public void actionPerformed(ActionEvent event)
                            {
                                Client.getClient().setServerName(server.getHost());
                                Client.getClient().setServerPort(server.getPort());
                                StratmasDialog.showProgressBarDialog(null, 
                                                                     "Initializing - passive mode ...");
                                new Thread() 
                                {
                                    public void run() 
                                    {
                                        Client.getClient().getRootObjectFromServer();
                                        StratmasDialog.quitProgressBarDialog();
                                    }
                                }.start();
                            }
                        };
                    action.setEnabled(Client.getClient().getRootObject().getChildCount() == 0);
                    action.putValue(Action.SHORT_DESCRIPTION, 
                                    "Click to passively watch scenario.");
                    popup.add(action);
                }
            } else {
                AbstractAction action = new AbstractAction("No simulations")
                    {
                        public void actionPerformed(ActionEvent e)
                        {
                        }
                    };
                action.setEnabled(false);
                popup.add(action);
            }        
            popup.show(this, x, y);
        }
    }
    
    
    /**
     * Creates a new Tree window using the the specified dispatcher.
     *
     * @param dispatcher the dispatcher to use.
     */    
    public static ServerViewFrame getDefaultFrame(StratmasDispatcher dispatcher)
    {
        ServerViewFrame res = 
            new ServerViewFrame(getDefaultServerView(dispatcher));
        return res;
    }

    /**
     * Creates the default component used for visualizing a ServerView
     * of the the specified dispatcher and defaults for all else..
     *
     * @param dispatcher the dispatcher to use.
     */
    public static ServerView getDefaultServerView(StratmasDispatcher dispatcher)
    {
        final ServerView view = new ServerView(dispatcher);
        //view.setShowsRootHandles(false);

        return view;
    }

    /**
     * Returns the prefered width of icons used in the TreeCellRenderers.
     */
    public int getPreferedIconWidth()
    {
        return preferedIconWidth;
    }

    /**
     * Returns the prefered height of icons used in the TreeCellRenderers.
     */
    public int getPreferedIconHeight()
    {
        return preferedIconHeight;
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
        int newWidth = (int) (getPreferedIconWidth() * scale);
        int newHeight = (int) (getPreferedIconHeight() * scale);
        
        // make sure we actually increase/decrease
        if (scale > 1) {
            if (newWidth == getPreferedIconWidth()) {
                newWidth++;
            }
            if (newHeight == getPreferedIconHeight()) {
                newHeight++;
            }
        } else if (scale < 1) {
            if (newWidth == getPreferedIconWidth()) {
                newWidth--;
            }
            if (newHeight == getPreferedIconHeight()) {
                newHeight--;
            }
        }


        setPreferedIconSize(newWidth, newHeight);
    }

    /**
     * Sets the prefered width and height of icons used in the TreeCellRenderes
     *
     * @param width the width of the icons.
     * @param height the height of the icons.
     */
    public void setPreferedIconSize(int width, int height)
    {
        if (width > 0 || height > 0) {
            if (width > 0) {
                preferedIconWidth = width;
            }
            if (height > 0) {
                preferedIconHeight = height;
                setRowHeight(preferedIconHeight);
            }

            ((ServerViewCellRenderer) getCellRenderer()).setBusyServerIcon(new ImageIcon(busyServerImage.getScaledInstance(getPreferedIconWidth(), getPreferedIconHeight(), Image.SCALE_SMOOTH)));
            ((ServerViewCellRenderer) getCellRenderer()).setAvailableServerIcon(new ImageIcon(availableServerImage.getScaledInstance(getPreferedIconWidth(), getPreferedIconHeight(), Image.SCALE_SMOOTH)));
            //((ServerViewCellRenderer) getCellRenderer()).setFont(((ServerViewCellRenderer) getCellRenderer()).getFont().deriveFont(0.95f * ((float) getPreferedIconHeight())));
            
            validate();
        }
    }
    
    /**
     * Returns the dispatcher backing this tree.
     */
    public StratmasDispatcher getStratmasDispatcher()
    {
        return this.stratmasDispatcher;
    }
}

class ServerViewCellRenderer extends DefaultTreeCellRenderer
{
    /**
     * The serverView this renderer renders for.
     */
    ServerView serverView;

    /**
     * The image for a busy server.
     */
    ImageIcon busyServerIcon;

    /**
     * The image for a available server.
     */
    ImageIcon availableServerIcon;

    /**
     * Creates a new ServerViewCellRenderer for use with the specified
     * ServerView.
     *
     * @param serverView the serverView this renderer will render for.
     */
    ServerViewCellRenderer(ServerView serverView, ImageIcon busyServerIcon,
                           ImageIcon availableServerIcon)
    {
        this.serverView = serverView;
        this.busyServerIcon = busyServerIcon;
        this.availableServerIcon = availableServerIcon;
    }

    /**
     * Sets the image for a busy server.
     *
     * @param icon the icon to use.
     */
    public void setBusyServerIcon(ImageIcon icon)
    {
        this.busyServerIcon = icon;
    }

    /**
     * Sets the image for an available server.
     *
     * @param icon the icon to use.
     */
    public void setAvailableServerIcon(ImageIcon icon)
    {
        this.availableServerIcon = icon;
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
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

        if (node.getUserObject() instanceof StratmasServer) {
            
            StratmasServer server = (StratmasServer) node.getUserObject();
            this.hasFocus = hasFocus;
            setText(server.toString());
            
            if (sel) {
                setForeground(getTextSelectionColor());
            } else {
                setForeground(getTextNonSelectionColor());
            }
            
            ImageIcon icon = null;
            if (server.getSimulations().size() != 0) {
                icon = busyServerIcon;
            } else {
                icon = availableServerIcon;
            }
            
            setIcon(icon);
            setLeafIcon(icon);
            setOpenIcon(icon);
            setClosedIcon(icon);
            
            setComponentOrientation(tree.getComponentOrientation());
            
            selected = sel;
            
            return this;
        } else {
            return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        }
    }
}


