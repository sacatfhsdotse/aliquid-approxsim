//         $Id: TreeViewFrame.java,v 1.13 2006/07/31 10:19:12 alexius Exp $

/*
 * @(#)TreeViewFrame.java
 */

package StratmasClient.treeview;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasEventListener;
import StratmasClient.object.StratmasEvent;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;
import javax.swing.JMenu;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.AbstractAction;
import javax.swing.WindowConstants;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;


/**
 * TreeViewFrame is JFrame adapted to use with a treeview
 *
 * @version 1, $Date: 2006/07/31 10:19:12 $
 * @author  Daniel Ahlin
*/
public class TreeViewFrame extends JFrame implements StratmasEventListener
{
    /**
     * The TreeView to frame 
     */
    TreeView treeView;

    /**
     * The ToolBar of the frame.
     */
    JToolBar toolBar;

    /**
     * Creates a frame for the specified TreeView
     *
     * @param treeView the TreeView to visualize
     */
    public TreeViewFrame(TreeView treeView)
    {
        super(((StratmasObjectAdapter) treeView.getModel()).getUserObject().getIdentifier());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.treeView = treeView;
        getContentPane().add(new JScrollPane(getTreeView()));
        setIconImage(getRootObject().getIcon().getImage());
        updateMenu();
        updateToolBar();
        getRootObject().addEventListener(this);

        pack();
    }

    /**
     * Called when the StratmasObject that is the root of the
     * TreeView framed is called.
     *
     * @param event the event causing the call.
     */
    public void eventOccured(StratmasEvent event)
    {
        if (event.isValueChanged() || 
            event.isObjectAdded()) {
            updateMenu();
        } else if (event.isRemoved()) {
            ((StratmasObject) event.getSource()).removeEventListener(this);
            dispose();
        } else if (event.isIdentifierChanged()) {
             setTitle(((StratmasObject)event.getSource()).getIdentifier());
        } else if (event.isReplaced()) {
             throw new AssertionError("Replace behavior not implemented");
        } 
        
    }

    /**
     * Returns the root object of the TreeView this object frames.
     */
    public StratmasObject getRootObject() 
    {
        return ((StratmasObjectAdapter) treeView.getModel()).getUserObject();
    }

    /**
     * Updates the menu of this frame.
     */
    public void updateMenu()
    {        
        JMenuBar res = new JMenuBar();
        JMenu menu = new JMenu("Actions", true);

        
        getRootObject().getActionGroup().addToMenu(menu, isEditable());
//         for (Enumeration e = getActions().elements(); e.hasMoreElements();) {
//             menu.add((Action) e.nextElement());
//         }
        menu.addSeparator();
        menu.add(new AbstractAction("Close") 
            {
                public void actionPerformed(ActionEvent e) 
                {
                    if (getTreeView().getTopLevelAncestor() instanceof TreeViewFrame) {
                        ((TreeViewFrame) getTreeView().getTopLevelAncestor()).dispose();
                    } else {
                        if (getTreeView().getParent() != null) {
                            getTreeView().getParent().remove(getTreeView());
                            getTreeView().getParent().validate();
                            getTreeView().getParent().repaint();
                        }
                    }
                }
            });
        
        res.add(menu);

        setJMenuBar(res);
        validate();        
    }

    /**
     * Updates the toolBar of this frame.
     */
    public void updateToolBar()
    {
        if (toolBar == null) {
            toolBar = new JToolBar();
            getContentPane().add(toolBar, BorderLayout.PAGE_START);
        } else {
            toolBar.removeAll();
        }
        
        if (getTreeView().getActionMap().get("ZoomIn") != null) {
            toolBar.add(getTreeView().getActionMap().get("ZoomIn"));
        }
        if (getTreeView().getActionMap().get("ZoomOut") != null) {
            toolBar.add(getTreeView().getActionMap().get("ZoomOut"));
        }
    }

//     /**
//      * Returns the common actions of this frame.
//      */
//     public Vector getActions()
//     {
//         Vector res = new Vector();
        
//         for (Enumeration e = getRootObject().getActions().elements(); 
//              e.hasMoreElements();) {
//             StratmasAbstractAction action = (StratmasAbstractAction)
//                 e.nextElement();
//             if (!isEditable()) {
//                 if (action.isMutator()) {
//                     action.setEnabled(false);
//                 }
//             }
//             res.add(action);
//         }

//         return res;
//     }

    /**
     * Returns true if editing is enabled in this frame.
     */
    public boolean isEditable()
    {
        return getTreeView().isEditable();
    }

    /**
     *  Releases listener on rootObject, then calls JFrame.dispose();
     */
    public void dispose()
    {
        getRootObject().removeEventListener(this);
        setJMenuBar(null);
        this.treeView = null;
        super.dispose();
    }

    /**
     * Set to true to allow editing in this frame.
     *
     * @param editable true to allow editing in this frame.
     */
    public void setEditable(boolean editable)
    {
        getTreeView().setEditable(editable);
        updateMenu();
    }

    /**
     * Returns the treeview of this frame
     */
    public TreeView getTreeView()
    {
        return treeView;
    }
}
