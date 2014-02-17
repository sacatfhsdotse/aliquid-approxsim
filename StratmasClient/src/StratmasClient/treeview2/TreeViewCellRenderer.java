//         $Id: TreeViewCellRenderer.java,v 1.2 2006/03/22 14:30:52 dah Exp $

/*
 * @(#)TreeViewCellRenderer.java
 */

package StratmasClient.treeview2;

import StratmasClient.object.StratmasObject;
import StratmasClient.Icon;

import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.JTree;

import java.awt.Component;

/**
 * TreeViewCellRender renders cells in TreeViews
 *
 * @version 2, $Date: 2006/03/22 14:30:52 $
 * @author  Daniel Ahlin
*/
class TreeViewCellRenderer extends DefaultTreeCellRenderer
{
    /**
     * Creates a new TreeViewCellRenderer for use with the specified
     * TreeView.
     */
    TreeViewCellRenderer()
    {
        super();
    }

    /**
     * Returns the component representation of the specified value for
     * the specified tree.
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean sel, boolean expanded,
                                                  boolean leaf, int row,
                                                  boolean hasFocus) 
    {
        // Focus status
        this.hasFocus = hasFocus;

        // Selected or not.
        if (sel) {
            setForeground(getTextSelectionColor());
        } else {
            setForeground(getTextNonSelectionColor());
        }
        selected = sel;

        // Get orientation from tree.
        setComponentOrientation(tree.getComponentOrientation());

        // Text of textfield and icon.
        if (value instanceof StratmasObject) {
            setText(value.toString());
            Icon icon = ((StratmasObject) value).getIcon().getScaledInstance(((TreeView) tree).getPreferedIconSize());
            setIcon(icon);
            setLeafIcon(icon);
            setOpenIcon(icon);
            setClosedIcon(icon);
            setEnabled(true);
        } else {
            setText(value.toString());
            setIcon(getDefaultLeafIcon());
            setDisabledIcon(getDefaultLeafIcon());
            setEnabled(false);
        }
                           
        return this;
    }
}

