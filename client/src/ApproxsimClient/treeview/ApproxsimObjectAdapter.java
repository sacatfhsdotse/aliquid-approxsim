// $Id: ApproxsimObjectAdapter.java,v 1.39 2006/08/31 14:45:12 alexius Exp $
/*
 * @(#)ApproxsimObjectAdapter.java
 */

package ApproxsimClient.treeview;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import ApproxsimClient.Icon;
import ApproxsimClient.filter.ApproxsimObjectFilter;
import ApproxsimClient.object.ApproxsimEvent;
import ApproxsimClient.object.ApproxsimEventListener;
import ApproxsimClient.object.ApproxsimList;
import ApproxsimClient.object.ApproxsimObject;
import ApproxsimClient.object.ApproxsimSimple;

/**
 * ApproxsimObjectAdapter adapts ApproxsimObjects for viewing in the tree.
 * 
 * @version 1, $Date: 2006/08/31 14:45:12 $
 * @author Daniel Ahlin
 */
public class ApproxsimObjectAdapter implements MutableTreeNode, TreeModel,
        ApproxsimEventListener, ApproxsimClient.filter.ApproxsimObjectAdapter {
    /**
     * The ApproxsimObject this adapter adapts.
     */
    ApproxsimObject approxsimObject;

    /**
     * The listeners of this object.
     */
    List<TreeModelListener> eventListenerList = new ArrayList<TreeModelListener>();

    /**
     * A vector containing child ApproxsimObjectAdapters
     */
    Vector<MutableTreeNode> children = null;

    /**
     * A vector containing listeners for invisible lists.
     */
    Vector<InvisibleListListener> invisibleListListeners = null;

    /**
     * ApproxsimObjectAdapter
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
    ApproxsimObjectFilter filter = null;

    /**
     * Creates a new ApproxsimObjectAdapter.
     */
    protected ApproxsimObjectAdapter() {}

    /**
     * Creates a new ApproxsimObjectAdapter.
     * 
     * @param approxsimObject the object to adapt.
     */
    protected ApproxsimObjectAdapter(ApproxsimObject approxsimObject) {
        this.setUserObject(approxsimObject);
    }

    /**
     * Creates a new ApproxsimObjectAdapter.
     * 
     * @param approxsimObject the object to adapt.
     */
    protected ApproxsimObjectAdapter(ApproxsimObject approxsimObject,
            ApproxsimObjectFilter filter) {
        this.setUserObject(approxsimObject);
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
        if (approxsimObject == null) {
            this.children = new Vector<MutableTreeNode>();
        } else {
            this.children = new Vector<MutableTreeNode>();

            if (filter == null) {
                for (Enumeration e = approxsimObject.children(); e
                        .hasMoreElements();) {
                    silentAdd(ApproxsimObjectAdapter.getApproxsimObjectAdapter((ApproxsimObject) e
                                      .nextElement()), this.children.size());
                }

                if (approxsimObject instanceof ApproxsimList) {
                    sort();
                }
            } else {
                for (Enumeration e = approxsimObject.children(); e
                        .hasMoreElements();) {
                    ApproxsimObject sObj = (ApproxsimObject) e.nextElement();
                    if (sObj instanceof ApproxsimList) {
                        // Must listen to the invisible list in order
                        // to receive add events for objects in the
                        // list that passes the filter.
                        if (filter.pass(sObj)) {
                            addInvisibleListListener(new InvisibleListListener(
                                    this, (ApproxsimList) sObj));
                        }
                        for (Enumeration i = sObj.children(); i
                                .hasMoreElements();) {
                            silentAdd(ApproxsimObjectAdapter
                                              .getApproxsimObjectAdapter((ApproxsimObject) i
                                                                                .nextElement(),
                                                                        filter),
                                      this.children.size());
                        }
                    } else {
                        silentAdd(ApproxsimObjectAdapter
                                          .getApproxsimObjectAdapter(sObj,
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

                if (((ApproxsimObjectAdapter) sObj1)
                        .getUserObject()
                        .getType()
                        .equals(((ApproxsimObjectAdapter) sObj2).getUserObject()
                                        .getType())) {
                    return ((ApproxsimObjectAdapter) sObj1)
                            .getUserObject()
                            .getIdentifier()
                            .compareTo(((ApproxsimObjectAdapter) sObj2)
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
    protected void add(ApproxsimObjectAdapter child) {
        add(child, getChildren().size());
    }

    /**
     * Adds the provided adapter as a child to this.
     * 
     * @param child the child to add.
     * @param index
     */
    protected void add(ApproxsimObjectAdapter child, int index) {
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
    protected boolean silentAdd(ApproxsimObjectAdapter child, int index) {
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
    protected void add(ApproxsimObject child) {
        add(child, getChildren().size());
    }

    /**
     * Adds the provided adapter as a child to this.
     * 
     * @param child the child to add.
     */
    protected void add(ApproxsimObject child, int index) {
        if (filter != null) {
            add(ApproxsimObjectAdapter.getApproxsimObjectAdapter(child, filter),
                index);
        } else {
            add(ApproxsimObjectAdapter.getApproxsimObjectAdapter(child), index);
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

        if (approxsimObject == null) {
            return false;
        } else {
            return !approxsimObject.isLeaf();
        }
    }

    /**
     * Returns the children at specified index or null if no such index.
     * 
     * @param childIndex the index of the child requested.
     */
    public TreeNode getChildAt(int childIndex) {
        return (ApproxsimObjectAdapter) getChildren().elementAt(childIndex);
    }

    /**
     * Returns the child of parent at index index in the parent's child array.
     * 
     * @param parent the parent;
     * @param index the index of the child requested.
     */
    public Object getChild(Object parent, int index) {
        return ((ApproxsimObjectAdapter) parent).getChildAt(index);
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
     * Returns true if two Adapters represents the same ApproxsimObject
     */
    public boolean equals(Object o) {
        if (o instanceof ApproxsimObjectAdapter) {
            return approxsimObject == ((ApproxsimObjectAdapter) o).approxsimObject;
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
        return ((ApproxsimObjectAdapter) parent).getChildCount();
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
        return ((ApproxsimObjectAdapter) parent)
                .getIndex((ApproxsimObjectAdapter) child);
    }

    /**
     * Called when the ApproxsimObject this adapter adapts changes.
     * 
     * @param event the event causing the call.
     */
    public void eventOccured(ApproxsimEvent event) {
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
            approxsimObject = null;
        } else if (event.isObjectAdded()) {
            // Only interested in this if getChildren has been called,
            // else we will pick it up when the first getChildren
            // triggers the childCreate function. This is not really
            // thread safe. Maybe better to call getChildren here.
            if (this.children != null) {
                add((ApproxsimObject) event.getArgument());
            }
        } else if (event.isChildChanged()) {
            // HACK special handling of children affecting the look of
            // the parent icon.
            ApproxsimObject child = (ApproxsimObject) event.getArgument();
            if (child.getIdentifier().equals("symbolIDCode")) {
                sendTreeNodesChangedEvent();
            }
        } else if (event.isReplaced()) {
            getUserObject().removeEventListener(this);
            ApproxsimObjectAdapter parent = (ApproxsimObjectAdapter) getParent();
            if (parent != null) {
                // System.out.println("index before = "+getParent().getIndex(this));
                int ind = getParent().getIndex(this);
                sendTreeNodeRemovedEvent();
                parent.remove(this);
                parent.add((ApproxsimObject) event.getArgument(), ind);
            } else {
                ApproxsimObject o = (ApproxsimObject) event.getArgument();
                setUserObject(o);
                for (Enumeration en = o.children(); en.hasMoreElements();) {
                    add((ApproxsimObject) en.nextElement());
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
        ApproxsimObjectAdapter model = (ApproxsimObjectAdapter) event.getSource();
        model.fireTreeNodesChanged(event);
    }

    /**
     * Builds a TreeModelEvent by traversing the adapter-tree to the top
     */
    protected TreeModelEvent buildTreeNodesChangedEvent() {
        if (getParent() != null) {
            Object[] children = { this };
            int[] childIndices = { getParent().getIndex(this) };
            Vector<ApproxsimObjectAdapter> ancestors = new Vector<ApproxsimObjectAdapter>();
            for (ApproxsimObjectAdapter walker = (ApproxsimObjectAdapter) getParent(); walker != null; walker = (ApproxsimObjectAdapter) walker
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
    protected void sendTreeNodeAddedEvent(ApproxsimObjectAdapter object) {
        TreeModelEvent event = buildTreeNodeAddedEvent(object);
        ApproxsimObjectAdapter model = (ApproxsimObjectAdapter) event.getSource();
        model.fireTreeNodesInserted(event);
    }

    /**
     * Builds a TreeModelEvent by traversing the adapter-tree to the top
     * 
     * @param object the object that was added.
     */
    protected TreeModelEvent buildTreeNodeAddedEvent(
            ApproxsimObjectAdapter object) {
        Object[] children = { object };
        int[] childIndices = { getIndex(object) };
        Vector<ApproxsimObjectAdapter> ancestors = new Vector<ApproxsimObjectAdapter>();
        for (ApproxsimObjectAdapter walker = this; walker != null; walker = (ApproxsimObjectAdapter) walker
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
        ApproxsimObjectAdapter model = (ApproxsimObjectAdapter) event.getSource();
        model.fireTreeNodesRemoved(event);
    }

    /**
     * Builds a TreeModelEvent by traversing the adapter-tree to the top
     */
    protected TreeModelEvent buildTreeNodeRemovedEvent() {
        if (getParent() != null) {
            Object[] children = { this };
            // int[] childIndices = {getParent().getIndex(this)};
            Vector<ApproxsimObjectAdapter> ancestors = new Vector<ApproxsimObjectAdapter>();
            for (ApproxsimObjectAdapter walker = (ApproxsimObjectAdapter) getParent(); walker != null; walker = (ApproxsimObjectAdapter) walker
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
        for (int i = eventListenerList.size() - 1; i >= 0; i--) {
            eventListenerList.get(i).treeNodesChanged(event);
        }
    }

    /**
     * Called to notify listeners that tree nodes have been inserted.
     */
    protected void fireTreeNodesInserted(TreeModelEvent event) {
        for (int i = eventListenerList.size() - 1; i >= 0; i--) {
            eventListenerList.get(i).treeNodesInserted(event);
        }
    }

    /**
     * Called to notify listeners that tree nodes have been removed.
     */
    protected void fireTreeNodesRemoved(TreeModelEvent event) {
        for (int i = eventListenerList.size() - 1; i >= 0; i--) {
            eventListenerList.get(i).treeNodesRemoved(event);
        }
    }

    /**
     * Called to notify listeners that the tree structure has drastically changed.
     */
    protected void fireTreeStructureChanged(TreeModelEvent event) {
        for (int i = eventListenerList.size() - 1; i >= 0; i--) {
            eventListenerList.get(i).treeStructureChanged(event);
        }
    }

    /**
     * Updates the node depending if it's selected in the tree or not.
     * 
     * @param selected true if it's selected.
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
        if (approxsimObject != null) {
            approxsimObject.fireSelected(selected);
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
        ApproxsimObject object = getApproxsimObject();
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
        return ((ApproxsimObjectAdapter) node).isLeaf();
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
    private List<TreeModelListener> getEventListenerList() {
        return this.eventListenerList;
    }

    /**
     * Adds an event listener for to the eventlistenerlist.
     * 
     * @param listener the listener to add.
     */
    private void addEventListener(TreeModelListener listener) {
        this.getEventListenerList().add(listener);
    }

    /**
     * Removes an event listener for from the eventlistenerlist.
     * 
     * @param listener the listener to add.
     */
    private void removeEventListener(TreeModelListener listener) {
        this.getEventListenerList().remove(listener);
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
        ApproxsimObjectAdapter sObj = (ApproxsimObjectAdapter) path
                .getLastPathComponent();
        sObj.update(newValue);
    }

    /**
     * Tries to update the target of this adapter with the provided object.
     */
    public void update(Object o) {
        if (approxsimObject == null) {
            return;
        }

        if (o instanceof String) {
            if (approxsimObject instanceof ApproxsimSimple) {
                try {
                    ((ApproxsimSimple) approxsimObject).valueFromString(o
                            .toString(), this);
                } catch (ParseException e) {
                    JOptionPane.showMessageDialog((JFrame) null,
                                                  "Parse error:\nUnable to assign \""
                                                          + o
                                                          + "\" to a/an "
                                                          + getApproxsimObject()
                                                                  .getType()
                                                                  .getName(),
                                                  "Parse Error",
                                                  JOptionPane.ERROR_MESSAGE);
                }
            } else {
                if (approxsimObject.getParent() != null
                        && approxsimObject.getParent() instanceof ApproxsimList) {
                    approxsimObject.setIdentifier(o.toString());
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

    public ApproxsimObject getUserObject() {
        return getApproxsimObject();
    }

    /**
     * Returns the approxsimobject this adapter adapts.
     */
    public ApproxsimObject getApproxsimObject() {
        return approxsimObject;
    }

    /**
     * Returns the string the invokation of the editor should hold for this value.
     */
    public String toEditableString() {
        if (approxsimObject == null) {
            return "";
        }

        if (approxsimObject instanceof ApproxsimSimple) {
            return ((ApproxsimSimple) approxsimObject).valueToPrettyString();
        } else {
            return approxsimObject.getIdentifier();
        }
    }

    /**
     * Returns the string the invokation of the editor should hold for this value.
     */
    public String getTextTag() {
        if (approxsimObject != null) {
            if (approxsimObject.isLeaf()) {
                return approxsimObject.toString();
            } else {
                return approxsimObject.getIdentifier();
            }
        } else {
            return null;
        }
    }

    /**
     * Returns the Icon the invokation of the editor should hold for this value.
     */
    public Icon getIcon() {
        if (approxsimObject != null) {
            return approxsimObject.getIcon();
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
        ApproxsimObject oldObject = getUserObject();
        if (oldObject != null) {
            oldObject.removeEventListener(this);
        }

        this.approxsimObject = (ApproxsimObject) object;

        if (object != null) {
            this.approxsimObject.addEventListener(this);
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
     * Creates ApproxsimObjectAdapters suitable for the given object. This is the approved method of getting ApproxsimObjectAdapters...
     * 
     * @param approxsimObject the approxsimObject to adapt.
     */
    public static ApproxsimObjectAdapter getApproxsimObjectAdapter(
            ApproxsimObject approxsimObject) {
        if (approxsimObject.getType().canSubstitute("Point")) {
            return new PointAdapter(approxsimObject);
        } else if (approxsimObject.getType().canSubstitute("ParameterGroup")) {
            ApproxsimObjectFilter filter = new ApproxsimObjectFilter() {
                public boolean pass(ApproxsimObject o) {
                    return (o.getType().canSubstitute("ParameterGroup") || o
                            .getType().canSubstitute("SimpleType"));
                }
            };
            return getApproxsimObjectAdapter(approxsimObject, filter);
        } else {
            return new ApproxsimObjectAdapter(approxsimObject);
        }
    }

    /**
     * Creates ApproxsimObjectAdapters suitable for the given object. This is the approved method of getting ApproxsimObjectAdapters...
     * 
     * @param approxsimObject the approxsimObject to adapt.
     * @param approxsimFilter the filter for this object.
     */
    public static ApproxsimObjectAdapter getApproxsimObjectAdapter(
            ApproxsimObject approxsimObject, ApproxsimObjectFilter approxsimFilter) {
        if (approxsimObject.getType().canSubstitute("Point")) {
            return new PointAdapter(approxsimObject, approxsimFilter);
        } else if (approxsimObject.getType().canSubstitute("ParameterGroup")) {
            final ApproxsimObjectFilter fApproxsimFilter = approxsimFilter;
            ApproxsimObjectFilter filter = new ApproxsimObjectFilter() {
                public boolean pass(ApproxsimObject o) {
                    return (fApproxsimFilter.pass(o) && (o.getType()
                            .canSubstitute("ParameterGroup") || o.getType()
                            .canSubstitute("SimpleType")));
                }
            };
            return new ApproxsimObjectAdapter(approxsimObject, filter);
        } else {
            return new ApproxsimObjectAdapter(approxsimObject, approxsimFilter);
        }
    }

    /**
     * Return the tree path of this adapter.
     * 
     * @return tree path of this adapter.
     */
    public TreePath getTreePath() {
        Vector<ApproxsimObjectAdapter> ancestors = new Vector<ApproxsimObjectAdapter>();
        for (ApproxsimObjectAdapter walker = (ApproxsimObjectAdapter) this; walker != null; walker = (ApproxsimObjectAdapter) walker
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
                    ((ApproxsimObjectAdapter) e.nextElement()).dispose();
                }
            }
            // Removes the eventlistener on the ApproxsimObject, will
            // also make getChildren return an empty vector.
            setUserObject(null);
        }
    }

    /**
     * Adds an InvisibleListListener. The listener does not listen to the adapter directly but to a StramtasList that is a child of the
     * object the adapter adapts. Since this list is not adapted by any ApproxsimObjectAdapter it must be listened to explicitly in order to
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
 * An InvisibleListListener listens to lists that is not visible in a TreeView (and thus not adapted by any ApproxsimObjectAdapter) due to
 * the current filter. When elements are added to the list the listener forwards the add (i.e. adds the object) to the actual adapter.
 * 
 * @version 1, $Date: 2006/08/31 14:45:12 $
 * @author Per Alexius
 */
class InvisibleListListener implements ApproxsimEventListener {
    /**
     * The adapter to notify about added objects.
     */
    ApproxsimObjectAdapter adapter;

    /**
     * The list to listen to.
     */
    ApproxsimList object;

    /**
     * Creates a new InvisibleListListener.
     * 
     * @param toNotify The adapter to notify about added objects.
     * @param toWatch The list to listen to.
     */
    InvisibleListListener(ApproxsimObjectAdapter toNotify, ApproxsimList toWatch) {
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
    public void eventOccured(ApproxsimEvent event) {
        if (event.isObjectAdded()) {
            adapter.add((ApproxsimObject) event.getArgument());
        } else if (event.isRemoved()) {
            dispose();
            adapter.removeInvisibleListListener(this);
            adapter = null;
        } else if (event.isReplaced()) {
            object.removeEventListener(this);
            object = (ApproxsimList) event.getArgument();
            object.addEventListener(this);
        }
    }
}
