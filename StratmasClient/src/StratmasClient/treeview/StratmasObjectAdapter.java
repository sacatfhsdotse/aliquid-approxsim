// $Id: StratmasObjectAdapter.java,v 1.39 2006/08/31 14:45:12 alexius Exp $
/*
 * @(#)StratmasObjectAdapter.java
 */

package StratmasClient.treeview;

import java.util.Enumeration;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasSimple;
import StratmasClient.object.StratmasList;
import StratmasClient.filter.StratmasObjectFilter;

import java.util.Iterator;
import java.util.Vector;
import java.util.Comparator;
import java.util.Collections;

import java.text.ParseException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.TreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeModel;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import java.util.EventListener;
import javax.swing.event.EventListenerList;
import StratmasClient.Icon;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.StratmasEventListener;

/**
 * StratmasObjectAdapter adapts StratmasObjects for viewing in the tree.
 * 
 * @version 1, $Date: 2006/08/31 14:45:12 $
 * @author Daniel Ahlin
 */
public class StratmasObjectAdapter implements MutableTreeNode, TreeModel,
        StratmasEventListener, StratmasClient.filter.StratmasObjectAdapter {
    /**
     * The StratmasObject this adapter adapts.
     */
    StratmasObject stratmasObject;

    /**
     * The listeners of this object.
     */
    EventListenerList eventListenerList = new EventListenerList();

    /**
     * A vector containing child StratmasObjectAdapters
     */
    Vector<MutableTreeNode> children = null;

    /**
     * A vector containing listeners for invisible lists.
     */
    Vector<InvisibleListListener> invisibleListListeners = null;

    /**
     * StratmasObjectAdapter
     */
    MutableTreeNode parent;

    /**
     * True if the node is currently selected.
     */
    boolean selected = false;

    /**
     * True if this node has been disposed.
     */
    boolean disposed = false;

    /**
     * True if the node is currently selected.
     */
    StratmasObjectFilter filter = null;

    /**
     * Creates a new StratmasObjectAdapter.
     */
    protected StratmasObjectAdapter() {}

    /**
     * Creates a new StratmasObjectAdapter.
     * 
     * @param stratmasObject the object to adapt.
     */
    protected StratmasObjectAdapter(StratmasObject stratmasObject) {
        this.setUserObject(stratmasObject);
    }

    /**
     * Creates a new StratmasObjectAdapter.
     * 
     * @param stratmasObject the object to adapt.
     */
    protected StratmasObjectAdapter(StratmasObject stratmasObject,
            StratmasObjectFilter filter) {
        this.setUserObject(stratmasObject);
        this.filter = filter;
    }

    /**
     * Returns the children vector, creating it if necessary.
     */
    protected Vector<MutableTreeNode> getChildren() {
        synchronized (this) {
            if (children == null) {
                createChildren();
            }
        }

        return children;
    }

    /**
     * Initializes the children of this object.
     */
    protected void createChildren() {
        if (stratmasObject == null) {
            this.children = new Vector<MutableTreeNode>();
        } else {
            this.children = new Vector<MutableTreeNode>();

            if (filter == null) {
                for (Enumeration e = stratmasObject.children(); e
                        .hasMoreElements();) {
                    silentAdd(StratmasObjectAdapter.getStratmasObjectAdapter((StratmasObject) e
                                      .nextElement()), this.children.size());
                }

                if (stratmasObject instanceof StratmasList) {
                    sort();
                }
            } else {
                for (Enumeration e = stratmasObject.children(); e
                        .hasMoreElements();) {
                    StratmasObject sObj = (StratmasObject) e.nextElement();
                    if (sObj instanceof StratmasList) {
                        // Must listen to the invisible list in order
                        // to receive add events for objects in the
                        // list that passes the filter.
                        if (filter.pass(sObj)) {
                            addInvisibleListListener(new InvisibleListListener(
                                    this, (StratmasList) sObj));
                        }
                        for (Enumeration i = sObj.children(); i
                                .hasMoreElements();) {
                            silentAdd(StratmasObjectAdapter
                                              .getStratmasObjectAdapter((StratmasObject) i
                                                                                .nextElement(),
                                                                        filter),
                                      this.children.size());
                        }
                    } else {
                        silentAdd(StratmasObjectAdapter
                                          .getStratmasObjectAdapter(sObj,
                                                                    filter),
                                  this.children.size());
                    }
                }

                sort();
            }
        }
    }

    /**
     * Sorts the all children except activityof this object.
     */
    public synchronized void sort() {
        Comparator<MutableTreeNode> comparator = new Comparator<MutableTreeNode>() {
            public int compare(MutableTreeNode sObj1, MutableTreeNode sObj2) {

                if (((StratmasObjectAdapter) sObj1)
                        .getUserObject()
                        .getType()
                        .equals(((StratmasObjectAdapter) sObj2).getUserObject()
                                        .getType())) {
                    return ((StratmasObjectAdapter) sObj1)
                            .getUserObject()
                            .getIdentifier()
                            .compareTo(((StratmasObjectAdapter) sObj2)
                                               .getUserObject().getIdentifier());
                } else {
                    return 0;
                }
            }

            public boolean equals(Object o) {
                return o.getClass() == this.getClass();
            }
        };

        Collections.sort(children, comparator);
    }

    /**
     * Adds the provided adapter as a child to this.
     * 
     * @param child the child to add.
     */
    protected void add(StratmasObjectAdapter child) {
        add(child, getChildren().size());
    }

    /**
     * Adds the provided adapter as a child to this.
     * 
     * @param child the child to add.
     * @param index
     */
    protected void add(StratmasObjectAdapter child, int index) {
        if (silentAdd(child, index)) {
            sendTreeNodeAddedEvent(child);
        }
    }

    /**
     * Adds the provided adapter as a child to this, if it passes filter. Returns true if the child was added.
     * 
     * @param child the child to add.
     * @param index where to put child.
     */
    protected boolean silentAdd(StratmasObjectAdapter child, int index) {
        if (filter == null || filter.pass(child.getUserObject())) {
            getChildren().add(index, child);
            child.setParent(this);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Adds the provided adapter as a child to this.
     * 
     * @param child the child to add.
     */
    protected void add(StratmasObject child) {
        add(child, getChildren().size());
    }

    /**
     * Adds the provided adapter as a child to this.
     * 
     * @param child the child to add.
     */
    protected void add(StratmasObject child, int index) {
        if (filter != null) {
            add(StratmasObjectAdapter.getStratmasObjectAdapter(child, filter),
                index);
        } else {
            add(StratmasObjectAdapter.getStratmasObjectAdapter(child), index);
        }
    }

    /**
     * Returns the children of this object.
     */
    public Enumeration<MutableTreeNode> children() {
        return getChildren().elements();
    }

    /**
     * Returns true if this object can act as a container, else false.
     */
    public boolean getAllowsChildren() {
        // HACK create children here to prevent reentrancy problems
        // with the JTree HiearchyCache
        getChildren();

        if (stratmasObject == null) {
            return false;
        } else {
            return !stratmasObject.isLeaf();
        }
    }

    /**
     * Returns the children at specified index or null if no such index.
     * 
     * @param childIndex the index of the child requested.
     */
    public TreeNode getChildAt(int childIndex) {
        return (StratmasObjectAdapter) getChildren().elementAt(childIndex);
    }

    /**
     * Returns the child of parent at index index in the parent's child array.
     * 
     * @param parent the parent;
     * @param index the index of the child requested.
     */
    public Object getChild(Object parent, int index) {
        return ((StratmasObjectAdapter) parent).getChildAt(index);
    }

    /**
     * Returns the root of the treemodel (this will always be this since the TreeModel is, so to speek, a facet of this object.)
     */
    public Object getRoot() {
        return this;
    }

    /**
     * Returns the parent of this object (or null if no parent).
     */
    public TreeNode getParent() {
        return parent();
    }

    /**
     * Returns true if two Adapters represents the same StratmasObject
     */
    public boolean equals(Object o) {
        if (o instanceof StratmasObjectAdapter) {
            return stratmasObject == ((StratmasObjectAdapter) o).stratmasObject;
        }
        return false;
    }

    /**
     * Returns the number of children this object contains.
     */
    public int getChildCount() {
        return getChildren().size();
    }

    /**
     * Returns the number of children for the object parent
     * 
     * @param parent the parent;
     */
    public int getChildCount(Object parent) {
        return ((StratmasObjectAdapter) parent).getChildCount();
    }

    /**
     * Returns the index of the specified subnode, or -1 if no such node is a child.
     * 
     * @param node the requested node.
     */
    public int getIndex(TreeNode node) {
        return getChildren().indexOf(node);
    }

    /**
     * Returns index of child in parents array.
     * 
     * @param parent the parent.
     * @param child the child.
     */
    public int getIndexOfChild(Object parent, Object child) {
        return ((StratmasObjectAdapter) parent)
                .getIndex((StratmasObjectAdapter) child);
    }

    /**
     * Called when the StratmasObject this adapter adapts changes.
     * 
     * @param event the event causing the call.
     */
    public void eventOccured(StratmasEvent event) {
        if (event.isValueChanged()) {
            sendTreeNodesChangedEvent();
        } else if (event.isRemoved()) {
            if (getUserObject() != null) {
                getUserObject().removeEventListener(this);
            }
            // getUserObject().removeEventListener(this);
            sendTreeNodeRemovedEvent();
            if (getParent() != null) {
                parent.remove(this);
            }
            stratmasObject = null;
        } else if (event.isObjectAdded()) {
            // Only interested in this if getChildren has been called,
            // else we will pick it up when the first getChildren
            // triggers the childCreate function. This is not really
            // thread safe. Maybe better to call getChildren here.
            if (this.children != null) {
                add((StratmasObject) event.getArgument());
            }
        } else if (event.isChildChanged()) {
            // HACK special handling of children affecting the look of
            // the parent icon.
            StratmasObject child = (StratmasObject) event.getArgument();
            if (child.getIdentifier().equals("symbolIDCode")) {
                sendTreeNodesChangedEvent();
            }
        } else if (event.isReplaced()) {
            getUserObject().removeEventListener(this);
            StratmasObjectAdapter parent = (StratmasObjectAdapter) getParent();
            if (parent != null) {
                // System.out.println("index before = "+getParent().getIndex(this));
                int ind = getParent().getIndex(this);
                sendTreeNodeRemovedEvent();
                parent.remove(this);
                parent.add((StratmasObject) event.getArgument(), ind);
            } else {
                StratmasObject o = (StratmasObject) event.getArgument();
                setUserObject(o);
                for (Enumeration en = o.children(); en.hasMoreElements();) {
                    add((StratmasObject) en.nextElement());
                }
                sendTreeNodesChangedEvent();
            }
        }
    }

    /**
     * Builds and sends a TreeModelEvent indicating that this node has changed.
     */
    protected void sendTreeNodesChangedEvent() {
        TreeModelEvent event = buildTreeNodesChangedEvent();
        StratmasObjectAdapter model = (StratmasObjectAdapter) event.getSource();
        model.fireTreeNodesChanged(event);
    }

    /**
     * Builds a TreeModelEvent by traversing the adapter-tree to the top
     */
    protected TreeModelEvent buildTreeNodesChangedEvent() {
        if (getParent() != null) {
            Object[] children = { this };
            int[] childIndices = { getParent().getIndex(this) };
            Vector<StratmasObjectAdapter> ancestors = new Vector<StratmasObjectAdapter>();
            for (StratmasObjectAdapter walker = (StratmasObjectAdapter) getParent(); walker != null; walker = (StratmasObjectAdapter) walker
                    .getParent()) {
                ancestors.add(0, walker);
            }
            Object[] path = ancestors.toArray();

            return new TreeModelEvent(path[0], path, childIndices, children);
        } else {
            return new TreeModelEvent(this, new Object[] { this }, null, null);
        }
    }

    /**
     * Builds and sends a TreeModelEvent indicating that some object was added.
     * 
     * @param object the object that was added.
     */
    protected void sendTreeNodeAddedEvent(StratmasObjectAdapter object) {
        TreeModelEvent event = buildTreeNodeAddedEvent(object);
        StratmasObjectAdapter model = (StratmasObjectAdapter) event.getSource();
        model.fireTreeNodesInserted(event);
    }

    /**
     * Builds a TreeModelEvent by traversing the adapter-tree to the top
     * 
     * @param object the object that was added.
     */
    protected TreeModelEvent buildTreeNodeAddedEvent(
            StratmasObjectAdapter object) {
        Object[] children = { object };
        int[] childIndices = { getIndex(object) };
        Vector<StratmasObjectAdapter> ancestors = new Vector<StratmasObjectAdapter>();
        for (StratmasObjectAdapter walker = this; walker != null; walker = (StratmasObjectAdapter) walker
                .getParent()) {
            ancestors.add(0, walker);
        }
        Object[] path = ancestors.toArray();

        return new TreeModelEvent(path[0], path, childIndices, children);
    }

    /**
     * Builds and sends a TreeModelEvent indicating that this node was removed.
     */
    protected void sendTreeNodeRemovedEvent() {
        TreeModelEvent event = buildTreeNodeRemovedEvent();
        StratmasObjectAdapter model = (StratmasObjectAdapter) event.getSource();
        model.fireTreeNodesRemoved(event);
    }

    /**
     * Builds a TreeModelEvent by traversing the adapter-tree to the top
     */
    protected TreeModelEvent buildTreeNodeRemovedEvent() {
        if (getParent() != null) {
            Object[] children = { this };
            // int[] childIndices = {getParent().getIndex(this)};
            Vector<StratmasObjectAdapter> ancestors = new Vector<StratmasObjectAdapter>();
            for (StratmasObjectAdapter walker = (StratmasObjectAdapter) getParent(); walker != null; walker = (StratmasObjectAdapter) walker
                    .getParent()) {
                ancestors.add(0, walker);
            }
            Object[] path = ancestors.toArray();
            int[] childIndices = { getParent().getIndex(this) };
            return new TreeModelEvent(path[0], path, childIndices, children);
        } else {
            return new TreeModelEvent(this, new Object[] { this }, null, null);
        }
    }

    /**
     * Called to notify listeners that tree nodes have changed.
     */
    protected void fireTreeNodesChanged(TreeModelEvent event) {
        Object[] listeners = getEventListenerList().getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            ((TreeModelListener) listeners[i + 1]).treeNodesChanged(event);
        }
    }

    /**
     * Called to notify listeners that tree nodes have been inserted.
     */
    protected void fireTreeNodesInserted(TreeModelEvent event) {
        Object[] listeners = getEventListenerList().getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            ((TreeModelListener) listeners[i + 1]).treeNodesInserted(event);
        }
    }

    /**
     * Called to notify listeners that tree nodes have been removed.
     */
    protected void fireTreeNodesRemoved(TreeModelEvent event) {
        Object[] listeners = getEventListenerList().getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            ((TreeModelListener) listeners[i + 1]).treeNodesRemoved(event);
        }
    }

    /**
     * Called to notify listeners that the tree structure has drastically changed.
     */
    protected void fireTreeStructureChanged(TreeModelEvent event) {
        Object[] listeners = getEventListenerList().getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            ((TreeModelListener) listeners[i + 1])
                    .treeStructureChanged(event);
        }
    }

    /**
     * Updates the node depending if it's selected in the tree or not.
     * 
     * @param selected true if it's selected.
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
        if (stratmasObject != null) {
            stratmasObject.fireSelected(selected);
        }
    }

    /**
     * Returns true if the node is currently selected, otherwise false.
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Returns true if this object has no children.
     */
    public boolean isLeaf() {
        // HACK create children here to prevent reentrancy problems
        // with the JTree HiearchyCache
        getChildren();
        StratmasObject object = getStratmasObject();
        if (object != null) {
            return object.isLeaf();
        } else {
            return true;
        }
    }

    /**
     * Returns true if the specified node is a leaf node.
     * 
     * @param node the node.
     */
    public boolean isLeaf(Object node) {
        return ((StratmasObjectAdapter) node).isLeaf();
    }

    /**
     * Returns the TreeNode holding this object or null if nothing is holding it
     */
    public TreeNode parent() {
        return parent;
    }

    /**
     * Sets the parent of the receiver to newParent.
     * 
     * @param newParent the new parent.
     */
    public void setParent(MutableTreeNode newParent) {
        this.parent = newParent;
    }

    /**
     * Returns true if this object has a parent.
     */
    public boolean hasParent() {
        return this.getParent() != null;
    }

    /**
     * Returns a list of the listeners of this object.
     */
    private EventListenerList getEventListenerList() {
        return this.eventListenerList;
    }

    /**
     * Adds an event listener for to the eventlistenerlist.
     * 
     * @param listener the listener to add.
     */
    private void addEventListener(EventListener listener) {
        this.getEventListenerList().add(EventListener.class, listener);
    }

    /**
     * Removes an event listener for from the eventlistenerlist.
     * 
     * @param listener the listener to add.
     */
    private void removeEventListener(EventListener listener) {
        this.getEventListenerList().remove(EventListener.class, listener);
    }

    /**
     * Adds a listener for the TreeModelEvent, posted after the tree changes.
     * 
     * @param listener the listener to add.
     */
    public void addTreeModelListener(TreeModelListener listener) {
        this.addEventListener(listener);
    }

    /**
     * Removes a listener for the TreeModelEvent, previously added with addTreeModelListener.
     * 
     * @param listener the listener to remove.
     */
    public void removeTreeModelListener(TreeModelListener listener) {
        this.removeEventListener(listener);
    }

    /**
     * Messaged when the user has altered the value for the item identified by path to newValue
     * 
     * @param path the path to the changed node.
     * @param newValue the new value of the node.
     */
    public void valueForPathChanged(TreePath path, Object newValue) {
        StratmasObjectAdapter sObj = (StratmasObjectAdapter) path
                .getLastPathComponent();
        sObj.update(newValue);
    }

    /**
     * Tries to update the target of this adapter with the provided object.
     */
    public void update(Object o) {
        if (stratmasObject == null) {
            return;
        }

        if (o instanceof String) {
            if (stratmasObject instanceof StratmasSimple) {
                try {
                    ((StratmasSimple) stratmasObject).valueFromString(o
                            .toString(), this);
                } catch (ParseException e) {
                    JOptionPane.showMessageDialog((JFrame) null,
                                                  "Parse error:\nUnable to assign \""
                                                          + o
                                                          + "\" to a/an "
                                                          + getStratmasObject()
                                                                  .getType()
                                                                  .getName(),
                                                  "Parse Error",
                                                  JOptionPane.ERROR_MESSAGE);
                }
            } else {
                if (stratmasObject.getParent() != null
                        && stratmasObject.getParent() instanceof StratmasList) {
                    stratmasObject.setIdentifier(o.toString());
                }
            }
        } else {
            System.err.println("Don't know how to update using a "
                    + o.getClass().toString());
        }
    }

    /*
     * Adds child to the receiver at index.
     * @param child child to add.
     * @param index where to add the child.
     */
    public void insert(MutableTreeNode child, int index) {
        getChildren().add(index, child);
    }

    /**
     * Removes the child at index from the receiver.
     * 
     * @param index the index of the child to be removed.
     */
    public void remove(int index) {
        getChildren().remove(index);
    }

    /**
     * Removes node from the receiver.
     * 
     * @param node the node to remove.
     */
    public void remove(MutableTreeNode node) {
        getChildren().remove(node);
    }

    /**
     * Removes the receiver from its parent.
     */
    public void removeFromParent() {
        if (getParent() != null) {
            parent.remove(this);
        }
    }

    public StratmasObject getUserObject() {
        return getStratmasObject();
    }

    /**
     * Returns the stratmasobject this adapter adapts.
     */
    public StratmasObject getStratmasObject() {
        return stratmasObject;
    }

    /**
     * Returns the string the invokation of the editor should hold for this value.
     */
    public String toEditableString() {
        if (stratmasObject == null) {
            return "";
        }

        if (stratmasObject instanceof StratmasSimple) {
            return ((StratmasSimple) stratmasObject).valueToPrettyString();
        } else {
            return stratmasObject.getIdentifier();
        }
    }

    /**
     * Returns the string the invokation of the editor should hold for this value.
     */
    public String getTextTag() {
        if (stratmasObject != null) {
            if (stratmasObject.isLeaf()) {
                return stratmasObject.toString();
            } else {
                return stratmasObject.getIdentifier();
            }
        } else {
            return null;
        }
    }

    /**
     * Returns the Icon the invokation of the editor should hold for this value.
     */
    public Icon getIcon() {
        if (stratmasObject != null) {
            return stratmasObject.getIcon();
        } else {
            return null;
        }
    }

    /**
     * Resets the user object of the receiver to object.
     * 
     * @param object the object this adapter reflects.
     */
    public void setUserObject(Object object) {
        StratmasObject oldObject = getUserObject();
        if (oldObject != null) {
            oldObject.removeEventListener(this);
        }

        this.stratmasObject = (StratmasObject) object;

        if (object != null) {
            this.stratmasObject.addEventListener(this);
        } else if (invisibleListListeners != null) {
            // The user object will (should... must!) be set to null
            // when the adapter is destroyed.
            for (Iterator<InvisibleListListener> it = invisibleListListeners
                    .iterator(); it.hasNext();) {
                it.next().dispose();
            }
            invisibleListListeners.clear();
        }
    }

    /**
     * Creates StratmasObjectAdapters suitable for the given object. This is the approved method of getting StratmasObjectAdapters...
     * 
     * @param stratmasObject the stratmasObject to adapt.
     */
    public static StratmasObjectAdapter getStratmasObjectAdapter(
            StratmasObject stratmasObject) {
        if (stratmasObject.getType().canSubstitute("Point")) {
            return new PointAdapter(stratmasObject);
        } else if (stratmasObject.getType().canSubstitute("ParameterGroup")) {
            StratmasObjectFilter filter = new StratmasObjectFilter() {
                public boolean pass(StratmasObject o) {
                    return (o.getType().canSubstitute("ParameterGroup") || o
                            .getType().canSubstitute("SimpleType"));
                }
            };
            return getStratmasObjectAdapter(stratmasObject, filter);
        } else {
            return new StratmasObjectAdapter(stratmasObject);
        }
    }

    /**
     * Creates StratmasObjectAdapters suitable for the given object. This is the approved method of getting StratmasObjectAdapters...
     * 
     * @param stratmasObject the stratmasObject to adapt.
     * @param stratmasFilter the filter for this object.
     */
    public static StratmasObjectAdapter getStratmasObjectAdapter(
            StratmasObject stratmasObject, StratmasObjectFilter stratmasFilter) {
        if (stratmasObject.getType().canSubstitute("Point")) {
            return new PointAdapter(stratmasObject, stratmasFilter);
        } else if (stratmasObject.getType().canSubstitute("ParameterGroup")) {
            final StratmasObjectFilter fStratmasFilter = stratmasFilter;
            StratmasObjectFilter filter = new StratmasObjectFilter() {
                public boolean pass(StratmasObject o) {
                    return (fStratmasFilter.pass(o) && (o.getType()
                            .canSubstitute("ParameterGroup") || o.getType()
                            .canSubstitute("SimpleType")));
                }
            };
            return new StratmasObjectAdapter(stratmasObject, filter);
        } else {
            return new StratmasObjectAdapter(stratmasObject, stratmasFilter);
        }
    }

    /**
     * Return the tree path of this adapter.
     * 
     * @return tree path of this adapter.
     */
    public TreePath getTreePath() {
        Vector<StratmasObjectAdapter> ancestors = new Vector<StratmasObjectAdapter>();
        for (StratmasObjectAdapter walker = (StratmasObjectAdapter) this; walker != null; walker = (StratmasObjectAdapter) walker
                .getParent()) {
            ancestors.add(0, walker);
        }
        Object[] path = ancestors.toArray();
        return new TreePath(path);
    }

    /**
     * Disposes this adapter, making it unfit for further use and releases all handles.
     */
    void dispose() {
        // Synchronized to avoid that getChildren() creates the vector
        // we are emptying.
        synchronized (this) {
            // If any children, dispose them first;
            if (children != null) {
                for (Enumeration<MutableTreeNode> e = children(); e
                        .hasMoreElements();) {
                    ((StratmasObjectAdapter) e.nextElement()).dispose();
                }
            }
            // Removes the eventlistener on the StratmasObject, will
            // also make getChildren return an empty vector.
            setUserObject(null);
        }
    }

    /**
     * Adds an InvisibleListListener. The listener does not listen to the adapter directly but to a StramtasList that is a child of the
     * object the adapter adapts. Since this list is not adapted by any StratmasObjectAdapter it must be listened to explicitly in order to
     * be able to forward add events for the list to the adapter that needs to add a new child adapter for the added object.
     * 
     * @param listener the listener to add.
     */
    private void addInvisibleListListener(InvisibleListListener listener) {
        if (invisibleListListeners == null) {
            invisibleListListeners = new Vector<InvisibleListListener>();
        }
        invisibleListListeners.add(listener);
    }

    /**
     * Removes an InvisibleListListener.
     * 
     * @param listener the listener to remove.
     */
    void removeInvisibleListListener(InvisibleListListener listener) {
        invisibleListListeners.remove(listener);
        if (invisibleListListeners.isEmpty()) {
            invisibleListListeners = null;
        }
    }
}

/**
 * An InvisibleListListener listens to lists that is not visible in a TreeView (and thus not adapted by any StratmasObjectAdapter) due to
 * the current filter. When elements are added to the list the listener forwards the add (i.e. adds the object) to the actual adapter.
 * 
 * @version 1, $Date: 2006/08/31 14:45:12 $
 * @author Per Alexius
 */
class InvisibleListListener implements StratmasEventListener {
    /**
     * The adapter to notify about added objects.
     */
    StratmasObjectAdapter adapter;

    /**
     * The list to listen to.
     */
    StratmasList object;

    /**
     * Creates a new InvisibleListListener.
     * 
     * @param toNotify The adapter to notify about added objects.
     * @param toWatch The list to listen to.
     */
    InvisibleListListener(StratmasObjectAdapter toNotify, StratmasList toWatch) {
        this.adapter = toNotify;
        this.object = toWatch;
        object.addEventListener(this);
    }

    /**
     * Cleans up after this listener.
     */
    public void dispose() {
        object.removeEventListener(this);
        object = null;
    }

    /**
     * Handle events.
     * 
     * @param event the event causing the call.
     */
    @Override
    public void eventOccured(StratmasEvent event) {
        if (event.isObjectAdded()) {
            adapter.add((StratmasObject) event.getArgument());
        } else if (event.isRemoved()) {
            dispose();
            adapter.removeInvisibleListListener(this);
            adapter = null;
        } else if (event.isReplaced()) {
            object.removeEventListener(this);
            object = (StratmasList) event.getArgument();
            object.addEventListener(this);
        }
    }
}
