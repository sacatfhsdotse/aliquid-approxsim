// $Id: DefaultComplex.java,v 1.11 2006/10/03 14:32:02 alexius Exp $
/*
 * @(#)DefaultComplex.java
 */

package ApproxsimClient.object;

import ApproxsimClient.object.type.Type;
import ApproxsimClient.object.type.Declaration;
import ApproxsimClient.object.type.TypeFactory;

import ApproxsimClient.object.primitive.Timestamp;
import ApproxsimClient.object.primitive.Identifier;
import ApproxsimClient.ActionGroup;
import ApproxsimClient.Debug;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import java.util.Hashtable;

import java.awt.event.ActionEvent;

import org.w3c.dom.Element;

/**
 * DefaultComplex is the common origin of the objects handled by the ApproxsimClient.
 * 
 * @version 1, $Date: 2006/10/03 14:32:02 $
 * @author Daniel Ahlin
 */

class DefaultComplex extends ApproxsimObjectDynImpl {
    /**
     * The children of the node. Note that the order has significance.
     */
    Vector<ApproxsimObject> parts = new Vector<ApproxsimObject>();

    /**
     * The children of the node hashed on identifier.
     */
    Hashtable<String, ApproxsimObject> partsHash = new Hashtable<String, ApproxsimObject>();

    /**
     * Creates a new DefaultComplex, taking subparts from provided vector.
     * 
     * @param identifier the identifier for the object.
     * @param type the type of the object.
     * @param parts the parts of this complex
     */
    protected DefaultComplex(String identifier, Type type,
            Vector<ApproxsimObject> parts) {
        super(identifier, type);
        this.add(parts);
    }

    /**
     * Creates a new (empty) DefaultComplex.
     * 
     * @param identifier the identifier for the object.
     * @param type the type of the object.
     */
    protected DefaultComplex(String identifier, Type type) {
        super(identifier, type);
    }

    /**
     * Creates a new DefaultComplex from a Declaration.
     * 
     * @param declaration the declaration for this object.
     */
    protected DefaultComplex(Declaration declaration) {
        super(declaration);
    }

    /**
     * Creates a new DefaultComplex from a Declaration and changes the Identifier to the specified Identifier.
     * <p>
     * author Per Alexius
     * 
     * @param declaration The Declaration for this object.
     * @param identifier The Identifier to use as Identifier for this object.
     */
    protected DefaultComplex(Declaration declaration, String identifier) {
        this(declaration);
        setIdentifier(identifier);
    }

    /**
     * Creates a new DefaultComplex from a Declaration.
     * 
     * @param declaration the declaration for this object.
     * @param parts the parts of this complex
     */
    protected DefaultComplex(Declaration declaration,
            Vector<ApproxsimObject> parts) {
        super(declaration);
        this.add(parts);
    }

    /**
     * Returns the children of this object.
     */
    public Enumeration<ApproxsimObject> children() {
        return this.parts.elements();
    }

    /**
     * Returns false, meaning that this object can always contain children.
     */
    public boolean isLeaf() {
        return false;
    }

    /**
     * Returns the number of children this object contains.
     */
    public int getChildCount() {
        return this.parts.size();
    }

    /**
     * Creates an XML representation of the body of this object.
     * <p>
     * author Per Alexius
     * 
     * @param b The StringBuffer to write to.
     * @return The StringBuffer b with an XML representation of this object's body appended to it.
     */
    public StringBuffer bodyXML(StringBuffer b) {
        for (Iterator it = getType().getSubElements().iterator(); it.hasNext();) {
            Declaration dec = (Declaration) it.next();
            ApproxsimObject child = getChild(dec.getName());
            if (child != null) {
                child.toXML(b);
            }
        }
        return b;
    }

    /**
     * Returns a ApproxsimGUIConstructor suitable for constructing objects of this type.
     * 
     * @param declaration The declaration for which the GUI is created.
     */
    protected static ApproxsimGUIConstructor getGUIConstructor(
            Declaration declaration) {
        return new ApproxsimComplexGUIConstructor(declaration);
    }

    /**
     * Returns a ApproxsimVectorConstructor suitable for constructing objects of this type.
     * 
     * @param declaration The declaration for which the object is created.
     */
    protected static ApproxsimVectorConstructor getVectorConstructor(
            Declaration declaration) {
        return new DefaultComplexVectorConstructor(declaration);
    }

    /**
     * Creates a DefaultComplex from the specified Declaration. Each component is given its default value, i.e. it is created by using its
     * default constructor.
     * <p>
     * author Per Alexius
     * 
     * @param declaration The declaration for which the object is created.
     */
    protected static ApproxsimObject defaultCreate(Declaration declaration) {
        Vector<ApproxsimObject> newParts = new Vector<ApproxsimObject>();
        for (Iterator it = declaration.getType().getSubElements().iterator(); it
                .hasNext();) {
            Declaration dec = (Declaration) it.next();
            if (dec.isSingular()) {
                newParts.add(ApproxsimObjectFactory.defaultCreate(dec));
            } else if (dec.isUnbounded()) {
                newParts.add(new ApproxsimList(dec, new Vector()));
            }
        }
        return new DefaultComplex(declaration, newParts);
    }

    /**
     * Creates a DefaultComplex from a DOM element.
     * <p>
     * author Per Alexius
     * 
     * @param n The dom element from which the object is created.
     */
    protected static ApproxsimObject domCreate(Element n) {
        Type myType = TypeFactory.getType(n);
        Vector<ApproxsimObject> newParts = new Vector<ApproxsimObject>();
        for (Iterator it = myType.getSubElements().iterator(); it.hasNext();) {
            Declaration dec = (Declaration) it.next();
            if (dec.isUnbounded()) {
                Vector<ApproxsimObject> listElems = new Vector<ApproxsimObject>();
                Vector elems = XMLHelper
                        .getChildElementsByTag(n, dec.getName());
                for (Iterator it2 = elems.iterator(); it2.hasNext();) {
                    Element elem = (Element) it2.next();
                    listElems.add(ApproxsimObjectFactory.domCreate(elem));
                }
                // Create list
                newParts.add(ApproxsimObjectFactory.createList(dec, listElems));
            } else {
                Element elem = XMLHelper.getFirstChildByTag(n, dec.getName());
                if (elem != null) {
                    ApproxsimObjectFactory.domCreate(elem);
                    newParts.add(ApproxsimObjectFactory.domCreate(elem));
                }
            }
        }
        return ApproxsimObjectFactory.create(Identifier.getIdentifier(n),
                                            myType, newParts);
    }

    /**
     * Returns the child with the specified identifier (or null)
     * 
     * @param id identifier of object to get
     */
    public ApproxsimObject getChild(String id) {
        return partsHash.get(id);
    }

    /**
     * Removes the specified ApproxsimObject. Sets initiator to null.
     * 
     * @param child the ApproxsimObject to remove.
     */
    public void remove(ApproxsimObject child) {
        remove(child, null);
    }

    /**
     * Removes the specified ApproxsimObject
     * 
     * @param child the ApproxsimObject to remove.
     * @param initiator The initiator of the removal.
     */
    public void remove(ApproxsimObject child, Object initiator) {
        parts.remove(child);
        partsHash.remove(child.getIdentifier());
        child.fireRemoved(initiator);
    }

    /**
     * Removes all children of this object.
     */
    public void removeAllChildren() {
        while (!parts.isEmpty()) {
            remove(parts.firstElement());
        }
    }

    /**
     * Adds a new child object to this object, preserving the order of this object's attributes. If an identical identifier exists, the
     * previous entry is removed. Sets initiator to null.
     * <p>
     * author Per Alexius
     * 
     * @param part The ApproxsimObject to add.
     */
    public void orderPreservingAdd(ApproxsimObject part) {
        orderPreservingAdd(part, null);
    }

    /**
     * Adds a new child object to this object, preserving the order of this object's attributes. If an identical identifier exists, the
     * previous entry is removed.
     * <p>
     * author Per Alexius
     * 
     * @param part The ApproxsimObject to add.
     */
    public void orderPreservingAdd(ApproxsimObject part, Object initiator) {
        silentOrderPreservingAdd(part);
        fireObjectAdded(part, initiator);
    }

    /**
     * Adds a new child object to this object, preserving the order of this object's attributes. If an identical identifier exists, the
     * previous entry is removed. Generates no add event.
     * <p>
     * author Per Alexius
     * 
     * @param part The ApproxsimObject to add.
     */
    private void silentOrderPreservingAdd(ApproxsimObject part) {
        if (!Identifier.isAnonymous(part.getIdentifier())) {
            ApproxsimObject prevPart = getChild(part.getIdentifier());
            if (prevPart != null) {
                // Remove previous part.
                this.remove(prevPart);
            }

            int indexOfNewElement = -1;
            int currentIndex = 0;
            String partName = part.getIdentifier();
            Vector subDecs = getType().getSubElements();

            // Get the name objName of element i in the parts
            // vector. Go through subelements of this ApproxsimComplex'
            // Type. If objName matches the part's name then we know i
            // is the index where to insert the new part. If objName
            // matches the name of the subelement we grab the next
            // object from the parts vector and starts over. If no
            // index is found when the outer loop exits we know we
            // should add the new part to the end of the parts vector.
            for (int i = 0; i < parts.size() && indexOfNewElement == -1; i++) {
                ApproxsimObject obj = parts.elementAt(i);
                String objName = obj.getIdentifier();
                for (int j = currentIndex; j < subDecs.size(); j++) {
                    String decName = ((Declaration) subDecs.elementAt(j))
                            .getName();
                    if (decName.equals(partName)) {
                        indexOfNewElement = i;
                        break;
                    } else if (decName.equals(objName)) {
                        currentIndex = j++;
                        break;
                    }
                }

            }
            // Add to both tables.
            if (indexOfNewElement == -1) {
                parts.add(part);
            } else {
                parts.insertElementAt(part, indexOfNewElement);
            }

            partsHash.put(part.getIdentifier(), part);
            part.setParent(this);
        } else {
            throw new AssertionError("Anonymous objects are deprecated.");
        }
    }

    /**
     * Adds a new child object to this object. If child with identical identifier exists, the previous entry is removed.
     * <p>
     * author Daniel Ahlin
     * 
     * @param part the ApproxsimObject to add.
     * @param initiator The initiator of the add.
     */
    public void add(ApproxsimObject part, Object initiator) {
        orderPreservingAdd(part, initiator);
    }

    /**
     * Notifies listeners that this object has been removed and should no longer be listened to.
     * 
     * @param initiator The initator of the event.
     */
    public void fireRemoved(Object initiator) {
        // Notify listeners about my own removal.
        ApproxsimEvent event = ApproxsimEvent.getRemoved(this, initiator);
        for (int i = getEventListenerList().size() - 1; i >= 0; i--) {
            getEventListenerList().get(i).eventOccured(event);
        }

        // The children must be removed too.
        for (Enumeration<ApproxsimObject> en = children(); en.hasMoreElements();) {
            en.nextElement().fireRemoved(initiator);
        }
    }

    /**
     * Clones this object. Notice that the Identifier is NOT cloned. Both the clone and the original object will thus keep a reference to
     * the same Identifier object.
     * <p>
     * author Per Alexius
     * 
     * @return A clone of this object.
     */
    protected Object clone() {
        Vector<ApproxsimObject> elements = new Vector<ApproxsimObject>();
        for (Enumeration<ApproxsimObject> en = children(); en.hasMoreElements();) {
            elements.add((ApproxsimObject) en.nextElement().clone());
        }
        return new DefaultComplex(identifier, type, elements);
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
        Vector v = XMLHelper.getChildElementsByTag(n, "update");
        for (Iterator it = v.iterator(); it.hasNext();) {
            Element elem = (Element) it.next();
            String type = elem.getAttribute("xsi:type");
            String id = Identifier.getIdentifier(elem);
            if (type.equals("sp:UpdateScope")) {
                ApproxsimObject obj = getChild(id);
                if (obj != null) {
                    obj.update(elem, t);
                } else {
                    Debug.err.println("No updatable child "
                            + elem.getAttribute("identifier") + " in "
                            + getReference());
                }
            } else if (type.equals("sp:UpdateAdd")) {
                Element newObjElem = XMLHelper
                        .getFirstChildByTag(elem, "identifiable");
                orderPreservingAdd(ApproxsimObjectFactory.domCreate(newObjElem),
                                   elem);
            } else if (type.equals("sp:UpdateRemove")) {
                ApproxsimObject obj = getChild(id);
                if (obj != null) {
                    remove(obj, elem);
                } else {
                    Debug.err.println("No removeable child "
                            + elem.getAttribute("identifier") + " in "
                            + getReference());
                }
            } else if (type.equals("sp:UpdateReplace")) {
                ApproxsimObject obj = getChild(id);
                if (obj != null) {
                    Element newObjElem = XMLHelper
                            .getFirstChildByTag(elem, "newObject");
                    obj.replace(ApproxsimObjectFactory.domCreate(newObjElem),
                                elem);
                } else {
                    Debug.err.println("No removeable child "
                            + elem.getAttribute("identifier") + " in "
                            + getReference());
                }
            } else if (type.equals("sp:UpdateModify")) {
                ApproxsimObject obj = getChild(id);
                if (obj != null) {
                    obj.update(XMLHelper.getFirstChildByTag(elem, "newValue"),
                               t);
                } else {
                    Debug.err.println("No updatable child "
                            + elem.getAttribute("identifier") + " in "
                            + getReference());
                }
            }
        }
    }

//     /**
//      * Returns actions associated with this object
//      */
//     public Vector getActions()
//     {
//         Vector res = super.getActions();
//         // Ugly hack
//         if (! (this instanceof ApproxsimList)) {
//             final DefaultComplex self = this;        
//             // Offer to add attributes that are not present.        
//             for (Enumeration e = getType().getSubElements().elements();
//                  e.hasMoreElements();) {
//                 final Declaration declaration = (Declaration) e.nextElement();

//                 if (!hasChild(declaration.getName())) {
//                     res.add(new ApproxsimAbstractAction("Add " + declaration.getName(), true)  
//                         {
//                             public void actionPerformed(ActionEvent e)
//                             {
//                                 ApproxsimObject newObject = ApproxsimObjectFactory.defaultCreate(declaration);
//                                 if (newObject != null) {
//                                     add(newObject);
//                                 } else {
//                                     ApproxsimGUIConstructorDialog dialog = 
//                                         ApproxsimGUIConstructor.buildDialog(ApproxsimObjectFactory.guiCreate(declaration), true);
//                                     dialog.setVisible(true);
//                                     newObject = dialog.getApproxsimObject();
//                                     if (newObject != null) {
//                                         add(newObject);
//                                     }
//                                 }
//                             }
//                         });
//                 }
//             }
//         }        
//         return res;
//     }

    /**
     * Returns actions associated with this object
     */
    public ActionGroup getActionGroup() {
        ActionGroup ag = super.getActionGroup();
        if (!(this instanceof ApproxsimList)) {
            final DefaultComplex self = this;

            for (Enumeration e = getType().getSubElements().elements(); e
                    .hasMoreElements();) {
                final Declaration dec = (Declaration) e.nextElement();
                if (!hasChild(dec.getName())) {
                    ag.add(new ActionGroup("Add " + dec.getName(), true, false) {
                    	
                        private static final long serialVersionUID = -7535608805159655813L;

                        public void actionPerformed(ActionEvent e) {
                            ApproxsimObject newObject = ApproxsimObjectFactory
                                    .defaultCreate(dec);
                            if (newObject != null) {
                                self.orderPreservingAdd(newObject);
                            } else {
                                ApproxsimGUIConstructorDialog dialog = ApproxsimGUIConstructor
                                        .buildDialog(ApproxsimObjectFactory
                                                .guiCreate(dec), true);
                                dialog.setVisible(true);
                                newObject = dialog.getApproxsimObject();
                                if (newObject != null) {
                                    self.orderPreservingAdd(newObject);
                                }
                            }
                        }
                    });
                } else if (dec.isList()) {
                    ag.add(getAddActionGroupForListDec((ApproxsimList) getChild(dec
                                                               .getName()), dec));
                }
            }
        }
        return ag;
    }

    /**
     * Convenience method for creating the ActionGroup for adding elements to a list. Goes through the descendant types of the type of this
     * list and groups actions according to that.
     * 
     * @param listToAddTo The ApproxsimList that the elements should be added to.
     * @param declaration The Declaration of the ApproxsimList to create add actions for. May be the Declaration of the ApproxsimList to add
     *            to or in recursive calls a Declaration with a type that is a descendant to the type of the list.
     * @return An ActionGroup with all the add actions that applies to this list grouped by type.
     */
    protected static ActionGroup getAddActionGroupForListDec(
            ApproxsimList listToAddTo, Declaration declaration) {
        final ApproxsimList fListToAddTo = listToAddTo;
        final Declaration fDec = declaration;
        ActionGroup ret;
        Type t = fDec.getType();
        ActionGroup agToAdd = new ActionGroup("Add " + t.getName() + " to "
                + fDec.getName(), true, false) {
        	
            private static final long serialVersionUID = -3149299615409272768L;

            public void actionPerformed(ActionEvent e) {
                Declaration toCreate = (Declaration) fDec.clone();
                toCreate.setMinOccurs(1);
                toCreate.setMaxOccurs(1);
                toCreate.setUnbounded(false);

                ApproxsimObject newObject = ApproxsimObjectFactory
                        .defaultCreate(toCreate);
                if (newObject != null) {
                    newObject.setIdentifier("new "
                            + newObject.getType().getName());
                    fListToAddTo.addWithUniqueIdentifier(newObject);
                } else {
                    ApproxsimGUIConstructorDialog dialog = ApproxsimGUIConstructor
                            .buildDialog(ApproxsimObjectFactory
                                    .guiCreate(toCreate), true);
                    dialog.setVisible(true);
                    newObject = dialog.getApproxsimObject();
                    if (newObject != null) {
                        fListToAddTo.addWithUniqueIdentifier(newObject);
                    }
                }
            }
        };

        Enumeration en = t.getDerived();
        if (en.hasMoreElements()) {
            ret = new ActionGroup("Add " + t.getName(), true, true);
            if (!t.isAbstract()) {
                ret.add(agToAdd);
            }
            while (en.hasMoreElements()) {
                ret.add(getAddActionGroupForListDec(fListToAddTo, fDec
                        .clone((Type) en.nextElement())));
            }
        } else {
            ret = agToAdd;
        }
        return ret;
    }

    /**
     * Called when a (direct) child of this is replaced.
     * 
     * @param oldObj the old object being replaced
     * @param newObj the object replacing oldObj
     * @param initiator the object causing the replacement.
     */
    protected void replaceChild(ApproxsimObject oldObj, ApproxsimObject newObj,
            Object initiator) {
        if (!oldObj.getType().canSubstitute(TypeFactory.getType("ValueType"))) {
            throw new AssertionError(
                    "Replace not supported for other types than ValueType descendants");
        }
        if (!oldObj.getIdentifier().equals(newObj.getIdentifier())) {
            Debug.err.println("Changing identifier from "
                    + newObj.getIdentifier() + " to " + oldObj.getIdentifier());
            newObj.setIdentifier(oldObj.getIdentifier());
        }
        parts.remove(oldObj);
        partsHash.remove(oldObj.getIdentifier());

        // Must remove the children...
        for (Enumeration en = oldObj.children(); en.hasMoreElements();) {
            ((ApproxsimObject) en.nextElement()).fireRemoved(initiator);
        }

        // Add new element without generating an add-event.
        silentOrderPreservingAdd(newObj);

        // Tell about replacement.
        fireChildChanged(newObj, this);
    }
}
