//         $Id: TreeViewModel.java,v 1.2 2006/03/22 14:30:52 dah Exp $
/*
 * @(#)TreeViewModel.java
 */

package StratmasClient.treeview2;

import StratmasClient.Debug;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasList;
import StratmasClient.object.StratmasEventListener;
import StratmasClient.object.StratmasEvent;

import StratmasClient.object.type.Declaration;
import StratmasClient.object.type.Type;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.WeakHashMap;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;

/**
 * TreeViewModel is a model adapting the StratmasObject hierarchy to
 * JTree requirements.
 *
 * @version 1, $Date: 2006/03/22 14:30:52 $
 * @author  Daniel Ahlin
*/
public class TreeViewModel implements TreeModel, StratmasEventListener
{
    /**
     * The root of the tree.
     */
    StratmasObject root;

    /**
     * List of TreeModelListeners
     */
    Vector treeModelListeners = new Vector();

    /**
     * Weak hashMap controlling which StratmasObjects we are currently
     * listenening to.
     */
    WeakHashMap stratmasObjects = new WeakHashMap();

    /**
     * Creates a new TreeViewModel with the specified root.
     * 
     * @param root the root.
     */       
    public TreeViewModel(StratmasObject root)
    {
        this.root = root;
        listen(root, 0);
    }
    
    /**
     * Returns the root of the tree, or null if none.
     */
    public Object getRoot()
    {
        return this.root;
    }

    /**
     * Returns the index'th child for the specified parent
     * 
     * @param parent the parent to get index'th child for for
     */
    public Object getChild(Object parent, int index)
    {
        if (parent instanceof StratmasList) {
            int i = 0;
            Object child = null;
            for (Enumeration e = ((StratmasList) parent).children(); e.hasMoreElements() && i <= index; i++) {
                child = e.nextElement();
            }

            // Add an event listener when we return a child.
            return listen((StratmasObject) child, index);
        } else {
            Vector v = ((StratmasObject) parent).getType().getSubElements();
            if (index < v.size()) {
                StratmasObject child = ((StratmasObject) parent).getChild(((Declaration) v.get(index)).getName());
                if (child == null) {
                    return new String(((Declaration) v.get(index)).getName()  + " is disabled");
                } else {
                    return listen((StratmasObject) child, index);
                }
            } else {
                return null;
            }
        }
    }

    /**
     * Returns the number of children of specified parent.
     *
     * @param parent a node in the tree.
     */
    public int getChildCount(Object parent)
    {
        if (parent instanceof StratmasObject) {
            return ((StratmasObject) parent).getChildCount();
        } else {
            return 0;
        }
    }


    /**
     * Returns true if specified node is a leaf.
     *
     * @param node a node in the tree.
     */
    public boolean isLeaf(Object node)
    {
        if (node instanceof StratmasObject) {
            return ((StratmasObject) node).isLeaf();
        } else {
            return true;
        }
    }

    /**
      * Messaged when the user has altered the value for the item identified.
      *
      * @param path path to the node.
      * @param newValue the new value from the TreeCellEditor
      */
    public void valueForPathChanged(TreePath path, Object newValue)
    {
        Debug.err.println(newValue.getClass());
    }

    /**
     * Returns the index of child in parent.
     *
     * @param parent a node in the tree.
     * @param child the node we are interested in.
     */
    public int getIndexOfChild(Object parent, Object child)
    {
        if (parent instanceof StratmasList) {
            if (child instanceof StratmasObject) {
                int i = 0;
                for (Enumeration e = ((StratmasList) parent).children(); e.hasMoreElements();) {
                    if (child == e.nextElement()) {
                        return i;
                    } else {
                        i++;
                    }
                } 
                
                // check stratmasObjects before giving up.
                Object o = getValue(child);
                if (o != null && o instanceof Integer) {
                    return ((Integer) o).intValue();
                } else {
                    return -1;
                }
            } else {
                return -1;
            }
        } else if (parent instanceof StratmasObject) {
            if (child instanceof StratmasObject) {
                return ((StratmasObject) parent).getType().getSubElements().indexOf(((StratmasObject) parent).getType().getSubElement(((StratmasObject) child).getIdentifier().toString()));
            } else {
                return ((StratmasObject) parent).getType().getSubElements().indexOf(((StratmasObject) parent).getType().getSubElement(child.toString()));
            }
        } else {
            return -1;
        }
    }

    /**
     * Called when an event has occured on a shown node.
     *
     * @param event event description.
     */
    public void eventOccured(StratmasEvent event)
    {
        if (event.isValueChanged()) {
            nodeChanged(event);
        } else if (event.isRemoved()) {
            nodeRemoved(event);
        } else if (event.isObjectAdded()) {
            nodeAdded(event);
        } else if (event.isChildChanged()) {
            nodeChildChanged(event);
        } else if (event.isReplaced()) {
            nodeReplaced(event);
        } else {
            Debug.err.println("Unhandled event in " + getClass());
        }
    }
    
    /**
     * Called when a node changes.
     *
     * @param event event description.
     */
    public void nodeChanged(StratmasEvent event)
    {
        // Only care about this one if it is the root that is
        // affected. Note that childchanged should take care of the
        // rest.
        if (event.getSource() == getRoot()) {
            TreeModelEvent treeModelEvent = 
                buildTreeModelEvent((StratmasObject) event.getSource());
            for (Iterator it = treeModelListeners.iterator(); it.hasNext();) {
                ((TreeModelListener) it.next()).treeNodesChanged(treeModelEvent);
            }
        }
    }

    /**
     * Called when a node is removed.
     *
     * @param event event description.
     */
    public void nodeRemoved(StratmasEvent event)
    {
        TreeModelEvent treeModelEvent = 
            buildTreeModelEvent((StratmasObject) event.getSource());
        for (Iterator it = treeModelListeners.iterator(); it.hasNext();) {
            ((TreeModelListener) it.next()).treeNodesRemoved(treeModelEvent);
        }
    }

    /**
     * Called when a node is added.
     *
     * @param event event description.
     */
    public void nodeAdded(StratmasEvent event)
    {
        TreeModelEvent treeModelEvent = 
            buildTreeModelEvent((StratmasObject) event.getArgument());
        for (Iterator it = treeModelListeners.iterator(); it.hasNext();) {
            ((TreeModelListener) it.next()).treeNodesInserted(treeModelEvent);
        }
    }

    /**
     * Called when a nodes child changes.
     *
     * @param event event description.
     */
    public void nodeChildChanged(StratmasEvent event)
    {
        TreeModelEvent treeModelEvent = 
            buildTreeModelEvent((StratmasObject) event.getArgument());
        for (Iterator it = treeModelListeners.iterator(); it.hasNext();) {
            ((TreeModelListener) it.next()).treeNodesChanged(treeModelEvent);
        }
    }

    /**
     * Called when a node is replaced.
     *
     * @param event event description.
     */
    public void nodeReplaced(StratmasEvent event)
    {
        TreeModelEvent treeModelEvent = null;
        if (event.getSource() == getRoot()) {
            treeModelEvent = new TreeModelEvent(this, 
                                                new Object[] {event.getArgument()});
        } else {
            treeModelEvent = 
                new TreeModelEvent(this, buildPath((StratmasObject) event.getSource(),
                                                   true));
        }
        
        for (Iterator it = treeModelListeners.iterator(); it.hasNext();) {
            ((TreeModelListener) it.next()).treeStructureChanged(treeModelEvent);
        }
    }

    /**
     * Builds a TreeModelEvent suitable for treeNodesChanged,
     * treeNodesInserted and treeNodesRemoved
     *
     * @param stratmasObject the object to build for.
     */
    TreeModelEvent buildTreeModelEvent(StratmasObject stratmasObject) 
    {
        // Special handling of root events
        if (stratmasObject == getRoot()) {
            // Root node affected, signaled by setting indices and
            // children to null.
            return new TreeModelEvent(this, new Object[] {stratmasObject}, 
                                      null, null);
        } else {
            Object[] path = buildPath(stratmasObject, false);
            int[] indices = new int[1];
            Object[] children = new Object[1];
            children[0] = stratmasObject;
            indices[0] = getIndexOfChild(path[path.length - 1], children[0]);
            return new TreeModelEvent(this, path, indices, children);
        }
    }

    /**
     * Builds a Object[] path to the provided StratmasObject up to and
     * including the getRoot().
     *
     * @param stratmasObject the object to build for.
     * @param includeObject true if the provided object should be
     * included in the path.
     */
    Object[] buildPath(StratmasObject stratmasObject, boolean includeObject)
    {
        // First find out how large a Objects[] we have to
        // allocate.
        int depth = 0;
        if (includeObject) {
            depth = 1;
        }
        for (StratmasObject walker = stratmasObject; walker != this.root; 
             walker = walker.getParent()) {
            depth++;
        }
        StratmasObject[] path = new StratmasObject[depth];

        // Then build the path.
        if (depth > 0) {
            if (includeObject) {
                path[path.length - 1] = stratmasObject;
            } else {
                path[path.length - 1] = stratmasObject.getParent();
            }
            for (int i = path.length - 1; i > 0; i--) {
                path[i - 1] = path[i].getParent();
            }
        }

        return path;
    }
    
    /**
     * Begins to listen to the provided StratmasObject if it is not
     * already listened to. Returns the object as a convinience. Note
     * that we only need to listen to non-leaf objects since
     * childChanged takes care of notification of children changes.
     *
     * @param stratmasObject the object
     * @param index the index assigned to the object.
     */
    public StratmasObject listen(StratmasObject stratmasObject, int index)
    {
        if (stratmasObject != null) {
            boolean doAdd = false;
            synchronized (stratmasObjects) {
                if (!stratmasObjects.containsKey(stratmasObject)) {
                    stratmasObjects.put(stratmasObject, new Integer(index));
                    doAdd = true;
                }
            }
            if (doAdd) {
                stratmasObject.addEventListener(this);
            }
        }

        return stratmasObject;
    }

    /**
     * Returns the object stored as value in the stratmasObjects hash.
     *
     * @param key key
     */
    public Object getValue(Object key)
    {
        Object o = null;
        synchronized (stratmasObjects) {
            o = stratmasObjects.get(key);
        }
        return o;
    }

    /**
     * Adds a listener for the TreeModel.
     *
     * @param listener the listener
     */
    public void addTreeModelListener(TreeModelListener listener)
    {        
        treeModelListeners.add(listener);
    }

    /**
     * Removes a listener previously added.
     *
     * @param listener the listener to remove
     */  
    public void removeTreeModelListener(TreeModelListener listener)
    {        
        treeModelListeners.remove(listener);
    }
}
