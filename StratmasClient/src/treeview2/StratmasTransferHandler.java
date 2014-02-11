//         $Id: StratmasTransferHandler.java,v 1.2 2006/03/22 14:30:52 dah Exp $

/*
 * @(#)TreeViewCellRenderer.java
 */

package StratmasClient.treeview2;

import StratmasClient.object.StratmasObject;
import StratmasClient.Debug;

import javax.swing.TransferHandler;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.tree.TreePath;
import javax.swing.JComponent;
import javax.swing.JTree;

import java.io.IOException;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;


/**
 * StratmasTransferHandler manages datatransfers between swing components.
 *
 * @version 1, $Date: 2006/03/22 14:30:52 $
 * @author  Daniel Ahlin
*/
class StratmasTransferHandler extends TransferHandler
{
    /**
     * Creates a new StratmasTransferHandler
     */
    StratmasTransferHandler()
    {
        super();
    }

    /**
     * Returns an object that establishes the look of a transfer.
     *
     * @param transferable the data to be transferred
     */
    public Icon getVisualRepresentation(Transferable transferable)
    {
        if (transferable instanceof StratmasObject) {
            return ((StratmasObject) transferable).getIcon().getScaledInstance(16, 16);
        } else {
            return super.getVisualRepresentation(transferable);
        }
    }

    /**
     * Returns the type of transfer actions supported by the source. 
     * 
     * @param component  the component holding the data to be transferred.
     */
    public int getSourceActions(JComponent c) 
    {
        return COPY_OR_MOVE;
    }

    /**
     * Creates a Transferable to use as the source for
     * a data transfer.
     *
     * @param component the component holding the data to be
     * transferred.
     */
    protected Transferable createTransferable(JComponent component)
    {
        if (component instanceof JTree) {
            TreePath[] selectedPaths = 
                ((JTree) component).getSelectionPaths();
            Object[] selectedObjects = new Object[selectedPaths.length];
            for (int i = 0; i < selectedPaths.length; i++) {
                selectedObjects[i] = 
                    selectedPaths[i].getLastPathComponent();
            }

            if (selectedObjects.length > 1) {
                Debug.err.println("Currently only support " + 
                                  "dragging one item at a time");
            }

            if (selectedObjects.length > 0 &&
                selectedObjects[0] instanceof Transferable) {
                return (Transferable) selectedObjects[0];
            } else {
                return null;
            }
            
        } else {
            return null;
        }        
    }

    /**
     * Indicates whether a component would accept an import of the given
     * set of data flavors prior to actually attempting to import it. 
     *
     * @param component  the component to receive the transfer.
     * @param transferFlavors  the data formats available
     */
    public boolean canImport(JComponent component, DataFlavor[] transferFlavors) 
    {
        boolean status = false;
        for (int i = 0; i < transferFlavors.length; i++) {
            if (transferFlavors[i].equals(StratmasObject.STRATMAS_OBJECT_FLAVOR) ||
                transferFlavors[i].equals(DataFlavor.stringFlavor)) {
                status = true;
            }
        }
        
        return status;
    }

    /**
     * Causes a transfer to a component from a clipboard or a DND drop
     * operation. Returns true if data was successfully imported.
     *
     * @param component  the component to receive the transfer.
     * @param transferable the data to import
     */
    public boolean importData(JComponent component, Transferable transferable) 
    {
        StratmasObject importedObject = getStratmasObject(transferable);

        if (importedObject != null) {
            if (component instanceof TreeView) {
                TreePath[] selectedPaths = 
                    ((JTree) component).getSelectionPaths();
                if (selectedPaths.length == 1 && 
                    selectedPaths[0].getLastPathComponent() 
                    instanceof StratmasObject ) {
                    StratmasObject reciever = 
                        (StratmasObject) selectedPaths[0].getLastPathComponent();
                    if (reciever.canAdd(importedObject)) {
                        importedObject.remove();
                        reciever.add(importedObject);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    Debug.err.println("Currently only support " + 
                                      "dropping to exactly one StratmasObject");
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * Converts a supported transferable to a StratmasObject and
     * returns it, or null if not supported transferable.
     *
     * @param transferable the transferable to convert.
     */
    StratmasObject getStratmasObject(Transferable transferable)
    {
        try {
            if (transferable.isDataFlavorSupported(StratmasObject.STRATMAS_OBJECT_FLAVOR)) {
                return (StratmasObject) transferable.getTransferData(StratmasObject.STRATMAS_OBJECT_FLAVOR);
            } else if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return StratmasClient.Client.importTaclanV2String((String) transferable.getTransferData(DataFlavor.stringFlavor));
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        } catch (UnsupportedFlavorException e) {
            return null;
        }
    }
}

