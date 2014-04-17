// $Id: StratmasObject.java,v 1.11 2006/09/27 12:56:07 alexius Exp $
/*
 * @(#)StratmasObject.java
 */

package StratmasClient.object;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.w3c.dom.Element;

import StratmasClient.ActionGroup;
import StratmasClient.Debug;
import StratmasClient.Icon;
import StratmasClient.StratmasConstants;
import StratmasClient.filter.StratmasObjectFilter;
import StratmasClient.object.primitive.Identifier;
import StratmasClient.object.primitive.Reference;
import StratmasClient.object.primitive.Timestamp;
import StratmasClient.object.type.Declaration;
import StratmasClient.object.type.Type;

/**
 * StratmasObject is the common origin of the objects handled by the StratmasClient. The tree-handling methods of this class acts like a
 * leaf-node.
 * 
 * @version 1, $Date: 2006/09/27 12:56:07 $
 * @author Daniel Ahlin
 */

public abstract class StratmasObject implements Transferable {
    /**
     * Convenience newline string for toXML methods.
     */
    protected final static String NL = System.getProperty("line.separator");

    /**
     * DataFlavor type used to transfer the reference of the object.
     */
    public static final DataFlavor STRATMAS_OBJECT_FLAVOR = createStratmasObjectFlavor();

    /**
     * Prefered indentation of TaclanV2 source.
     */
    protected final static String TACLANV2_INDENTATION = "  ";

    /**
     * A copy of an empty enumeration to use for children().
     */
    static Enumeration<StratmasObject> emptyEnumeration = new Enumeration<StratmasObject>() {
        public boolean hasMoreElements() {
            return false;
        }

        public StratmasObject nextElement() throws NoSuchElementException {
            throw new NoSuchElementException("No more elements.");
        }
    };

    /**
     * Returns the identifier of this object.
     */
    public abstract String getIdentifier();

    /**
     * Sets the identifier of this object.
     * 
     * @param identifier the new identifier of this object.
     */
    public abstract void setIdentifier(String identifier);

    /**
     * Returns the type of this object.
     */
    public abstract Type getType();

    /**
     * Returns the declaration specifying this object, or null if no such can be specified (e. g. if this object has no parent). Note that
     * the type of the returned declaration may be a superclass of the actual type the object has.
     */
    public Declaration getDeclaration() {
        if (getParent() == null) {
            return null;
        } else {
            return getParent().getType().getSubElement(getTag());
        }
    }

    /**
     * Returns a Reference to this object
     */
    public Reference getReference() {
        if (getParent() == null) {
            return new Reference(new String[] { getIdentifier() });
        } else {
            return new Reference(getParent().getReference(), getIdentifier());
        }
    }

    /**
     * Returns the child at the specified index assuming the child is enumerated by the objects type. Note that this means that getChild(j)
     * may return null while getChild(i) and getChild(k) does not for i < j < k.
     * 
     * @param index the index of the object.
     */
    public StratmasObject getChild(int index) {
        return null;
    }

    /**
     * Returns the child at the specified index assuming the child is enumerated by the objects type. Note that this means that getChild(j)
     * may return null while getChild(i) and getChild(k) does not for i < j < k.
     * 
     * @param identifier the identifier of the child to get.
     */
    public StratmasObject getChild(String identifier) {
        return null;
    }

    /**
     * Returns the number of children this object contains.
     */
    public int getChildCount() {
        return 0;
    }

    /**
     * Returns true if this object has children.
     */
    public boolean hasChildren() {
        return getChildCount() > 0;
    }

    /**
     * Returns true if this complex has a child with the specified name.
     * 
     * @param id identifier of object to search for
     */
    public boolean hasChild(String id) {
        return getChild(id) != null;
    }

    /**
     * Returns the children of this object.
     */
    public Enumeration<StratmasObject> children() {
        return emptyEnumeration;
    }

    /**
     * Returns the index of the provided child, according to the declaration or -1 if none.
     * 
     * @param child the child queried for.
     */
    public int getIndexOfChild(StratmasObject child) {
        return -1;
    }

    /**
     * Returns true if this object can have no children.
     */
    public boolean isLeaf() {
        return true;
    }

    /**
     * Convenience method for adding without identified initiator.
     * 
     * @param part the StratmasObject to add.
     */
    public void add(StratmasObject part) {
        add(part, null);
    }

    /**
     * Adds a new child object to this object. If child with identical identifier exists, the previous entry is removed.
     * 
     * @param part the StratmasObject to add.
     * @param initiator The initiator of the add.
     */
    public void add(StratmasObject part, Object initiator) {}

    /**
     * Adds all children in provided vector to this object. If any child with identical identifier already exists, the previous entry is
     * removed.
     * <p>
     * author Daniel Ahlin
     * 
     * @param parts the StratmasObjects to add.
     */
    public void add(Vector<StratmasObject> parts) {
        for (Enumeration<StratmasObject> ps = parts.elements(); ps
                .hasMoreElements();) {
            this.add(ps.nextElement());
        }
    }

    /**
     * Removes the provided object from the tree.
     * 
     * @param child child to remove
     */
    protected void remove(StratmasObject child) {}

    /**
     * Called when a (direct) child of this has changed.
     * 
     * @param child the child that changed
     * @param initiator The initiator of the Event.
     */
    public void childChanged(StratmasObject child, Object initiator) {
        fireChildChanged(child, initiator);
    }

    /**
     * Called when a (direct) child of this is replaced.
     * 
     * @param oldObj the old object being replaced
     * @param newObj the object replacing oldObj
     * @param initiator the object causing the replacement.
     */
    protected void replaceChild(StratmasObject oldObj, StratmasObject newObj,
            Object initiator) {}

    /**
     * Returns the parent of this object (or null if no parent).
     */
    public abstract StratmasObject getParent();

    /**
     * Sets the parent of this object. Note that implementations of this method have to make sure to call StratmasObjectFactory.attached iff
     * getParent()==null when this function is called with a non-null argument.
     * 
     * @param parent the new parent of this object.
     */
    protected abstract void setParent(StratmasObject parent);

    /**
     * Returns true if this object has a parent.
     */
    public boolean hasParent() {
        return getParent() != null;
    }

    /**
     * Removes this object from the tree.
     */
    public void remove() {
        if (getParent() != null) {
            getParent().remove(this);
        } else {
            fireRemoved(null);
        }
    }

    /**
     * Adds an event listener for to the eventlistenerlist.
     * 
     * @param listener the listener to add.
     */
    public abstract void addEventListener(StratmasEventListener listener);

    /**
     * Removes an event listener for from the eventlistenerlist.
     * 
     * @param listener the listener to remove.
     */
    public abstract void removeEventListener(StratmasEventListener listener);

    /**
     * Replaces this object with the provided object.
     * <p>
     * author Per Alexius
     * 
     * @param toReplaceWith The object to replace this object with.
     * @param initiator The initiator of this replace action.
     */
    public abstract void replace(StratmasObject toReplaceWith, Object initiator);

    /**
     * Returns the string representation of this object.
     */
    public String toString() {
        return getIdentifier();
    }

//     /**
//      * Returns actions associated with this object
//      */
//     public abstract Vector getActions();

    /**
     * Returns actions associated with this object
     */
    public abstract ActionGroup getActionGroup();

    /**
     * Returns the tag of this object (used for XML representation). For some objects this will be the same as the identifier. However for
     * nonsingular objects it will be the tag of the list holding them.
     */
    public String getTag() {
        // Can not use this.getDeclaration().getName() here since getDeclaration uses
        // getTag().

        if (getParent() instanceof StratmasList) {
            return getParent().getTag();
        } else {
            return getIdentifier();
        }
    }

    /**
     * Creates an XML representation of this object.
     * <p>
     * author Per Alexius
     * 
     * @return An XML representation of this object.
     */
    public String toXML() {
        return toXML(new StringBuffer()).toString();
    }

    /**
     * Creates an XML representation of this object.
     * <p>
     * author Per Alexius
     * 
     * @param b The StringBuffer to write to.
     * @return The StringBuffer b with an XML representation of this object appended to it.
     */
    public StringBuffer toXML(StringBuffer b) {
        b.append(NL).append("<").append(getTag());
        b.append(" xsi:type=\"sp:").append(getType().getName());
        if (getParent() instanceof StratmasList) {
            b.append("\" identifier=\"")
                    .append(XMLHelper.encodeSpecialCharacters(getIdentifier()));
        }
        b.append("\">");
        bodyXML(b);
        b.append("</").append(getTag()).append(">");
        return b;
    }

    /**
     * Creates an XML representation of the body of this object.
     * <p>
     * author Per Alexius
     * 
     * @param b The StringBuffer to write to.
     * @return The StringBuffer b with an XML representation of this object's body appended to it.
     */
    public abstract StringBuffer bodyXML(StringBuffer b);

    /**
     * Returns a StratmasGUIConstructor suitable for constructing objects of this type.
     * 
     * @param declaration the declaration for which the GUI is created.
     */
    protected static StratmasGUIConstructor getGUIConstructor(
            Declaration declaration) {
        throw new AssertionError("GUIConstructor for "
                + declaration.getType().getName() + " not implemented.");
    }

    /**
     * Returns a StratmasVectorConstructor suitable for constructing objects of this type.
     * 
     * @param declaration the declaration for which the object is created.
     */
    protected static StratmasVectorConstructor getVectorConstructor(
            Declaration declaration) {
        throw new AssertionError("VectorConstructor for "
                + declaration.getType().getName() + " not implemented.");
    }

    /**
     * Creates a StratmasObject from a DOM element.
     * 
     * @param n The dom element from which the object is created.
     */
    protected static StratmasObject domCreate(Element n) {
        throw new AssertionError("DomConstructor for "
                + n.getAttribute("xsi:type") + " not implemented.");
    }

    /**
     * Creates a StratmasObject from the specified Declaration.
     * <p>
     * author Per Alexius
     * 
     * @param declaration The declaration for which the object is created.
     */
    protected static StratmasObject defaultCreate(Declaration declaration) {
        throw new AssertionError("DomConstructor for "
                + declaration.getType().getName() + " not implemented.");
    }

    /**
     * Returns the icon used to symbolize this object.
     */
    public abstract Icon getIcon();

    /**
     * Returns data flavors.
     */
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { STRATMAS_OBJECT_FLAVOR,
                DataFlavor.stringFlavor };
    }

    /**
     * Checks if the given data flavor is supported.
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return STRATMAS_OBJECT_FLAVOR.match(flavor)
                || DataFlavor.stringFlavor.match(flavor);
    }

    /**
     * Return this object.
     */
    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException {
        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        if (flavor.match(DataFlavor.stringFlavor)) {
            StringBuffer b = new StringBuffer();
            b.append(StratmasConstants.xmlFileHeader);
            b.append("<identifiables xsi:type=\"sp:")
                    .append(getType().getName());
            b.append("\" identifier=\"").append(getIdentifier()).append("\">");
            bodyXML(b);
            b.append("</identifiables>");
            b.append(StratmasConstants.xmlFileFooter);
            return b.toString();
        } else {
            return this;
        }
    }

    /**
     * Initializes the STRATMAS_OBJECT_FLAVOR.
     * 
     * @return The STRATMAS_OBJECT_FLAVOR.
     */
    public static final DataFlavor createStratmasObjectFlavor() {
        DataFlavor flavor = null;
        try {
            flavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
        } catch (ClassNotFoundException e) {
            System.err.println("Couldn't create STRATMAS_OBJECT_FLAVOR");
        }
        return flavor;
    }

    /**
     * Returns all elements that passes the provided filter declared below this object.
     * 
     * @param filter the filter to use.
     */
    public Enumeration<StratmasObject> getFilteredChildren(
            StratmasObjectFilter filter) {
        return getFilteredChildren(filter, new Vector<StratmasObject>())
                .elements();
    }

    /**
     * Adds all objects that passes the provided filter declared below this object to the provided vector.
     * 
     * @param filter the filter to use.
     * @param v the vector to which to add the objects.
     */
    private Vector<StratmasObject> getFilteredChildren(
            StratmasObjectFilter filter, Vector<StratmasObject> v) {
        if (filter.pass(this)) {
            v.add(this);
        }
        for (Enumeration<StratmasObject> e = children(); e.hasMoreElements();) {
            StratmasObject sObj = e.nextElement();
            sObj.getFilteredChildren(filter, v);
        }

        return v;
    }

    /**
     * Returns a String holding this object in its Taclan V2 representation.
     */
    public String toTaclanV2() {
        return this.toTaclanV2StringBuffer(new StringBuffer()).toString();
    }

    /**
     * Writes this objects Taclan V2 representation to the supplied StringBuffer. Returns the same buffer.
     * 
     * @param buf the buffer.
     */
    protected StringBuffer toTaclanV2StringBuffer(StringBuffer buf) {
        return toTaclanV2StringBuffer(buf, "");
    }

    /**
     * Writes this objects Taclan V2 representation to the supplied StringBuffer with the specified indentation. Returns the same buffer.
     * 
     * @param buf the buffer.
     * @param indent the indentation.
     */
    protected StringBuffer toTaclanV2StringBuffer(StringBuffer buf,
            String indent) {
        buf.append(indent + getType().toTaclanV2() + " "
                + Identifier.toTaclanV2(getIdentifier()) + " {");
        if (!isLeaf()) {
            for (Enumeration<StratmasObject> cs = children(); cs
                    .hasMoreElements();) {
                buf.append("\n");
                (cs.nextElement()).toTaclanV2StringBuffer(buf, indent
                        + TACLANV2_INDENTATION);
            }
            buf.append("\n" + indent + "}");
        } else {
            buf.append("}");
        }

        return buf;
    }

    /**
     * Clones this object.
     * <p>
     * author Per Alexius
     * 
     * @return A clone of this object.
     */
    protected Object clone() {
        throw new AssertionError("clone() for object of type "
                + getType().toString() + " not implemented");
    }

    /**
     * Updates this object with the data contained in the Element n.
     * <p>
     * author Per Alexius
     * 
     * @param n The DOM Element from which to fetch the data.
     * @param t The simulation time for which the data is valid.
     */
    public void update(Element n, Timestamp t) {
        Debug.err.println("Update in StratmasObject for type "
                + getType().getName());
    }

    /**
     * Returns the root of the tree this StratmasObject is part of.
     */
    public StratmasObject getRoot() {
        StratmasObject walker = this;
        while (walker.getParent() != null) {
            walker = walker.getParent();
        }

        return walker;
    }

    /**
     * Returns true if obj is an ancestor of this
     * 
     * @param obj the prospective ancestor.
     */
    public boolean isAncestor(StratmasObject obj) {
        StratmasObject walker = getParent();
        while (walker != null && !walker.equals(obj)) {
            walker = walker.getParent();
        }

        return walker != null;
    }

    /**
     * Returns the "youngest" common ancestor of this and the provided other object, or null if none found.
     * 
     * @param other the other object
     */
    public StratmasObject getYoungestCommonAncestor(StratmasObject other) {
        HashSet<StratmasObject> set = new HashSet<StratmasObject>();

        for (StratmasObject walker = other; walker != null; walker = walker
                .getParent()) {
            set.add(walker);
        }

        for (StratmasObject walker = this; walker != null; walker = walker
                .getParent()) {
            if (set.contains(walker)) {
                return walker;
            }
        }

        return null;
    }

    /**
     * Returns true iff this can add provided object.
     * 
     * @param object the object to check for validity.
     */
    public boolean canAdd(StratmasObject object) {
        Declaration declaration = getType()
                .getSubElement(object.getIdentifier().toString());
        if (declaration != null
                && object.getType().canSubstitute(declaration.getType())) {
            // Now, make sure object is not an ancestor of this.
            return !isAncestor(object);
        }

        return false;
    }

    /**
     * Notifies listeners of on this object that the identifier has changed.
     * 
     * @param oldIdentifier the old identifier.
     * @param initiator the initiator the identifier change.
     */
    protected abstract void fireIdentifierChanged(String oldIdentifier,
            Object initiator);

    /**
     * Notifies listeners that this object has been removed and should no longer be listened to.
     * 
     * @param initiator The initiator of the removal.
     */
    protected abstract void fireRemoved(Object initiator);

    /**
     * Notifies listeners that a child object has been replaced (listeners on the child recieves a remove-event.).
     * 
     * @param newObj the object replacing the old.
     * @param initiator the initiator of the change.
     */
    protected abstract void fireReplaced(StratmasObject newObj, Object initiator);

    /**
     * Notifies listeners that an object has been added to this object.
     * 
     * @param added The StratmasObject that has been added.
     * @param initiator The initiator of the add.
     */
    public abstract void fireObjectAdded(StratmasObject added, Object initiator);

    /**
     * Notifies listeners that a child object has changed.
     * 
     * @param changed The StratmasObject that has changed.
     * @param initiator the initiator of the change.
     */
    public abstract void fireChildChanged(StratmasObject changed,
            Object initiator);

    /**
     * Notifies listeners that a child has been selected.
     */
    public abstract void fireSelected(boolean selected);

//     protected void finalize()
//     {
//          System.err.println("Finalizing " + this.toString());
//     }
}
