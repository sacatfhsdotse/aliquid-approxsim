// $Id: StratmasList.java,v 1.7 2006/07/31 10:18:44 alexius Exp $
/*
 * @(#)StratmasList.java
 */

package StratmasClient.object;

import java.util.Enumeration;
import java.util.Vector;
import java.util.Iterator;

import StratmasClient.ActionGroup;

import StratmasClient.object.type.Type;
import StratmasClient.object.type.Declaration;
import StratmasClient.object.primitive.Identifier;

/**
 * StratmasList is an helper class to deal with non singular declarations with the same tag.
 * 
 * @version 1, $Date: 2006/07/31 10:18:44 $
 * @author Daniel Ahlin
 */

public class StratmasList extends DefaultComplex {
    /**
     * A declaration to use for this list if it does not have a parent.
     */
    Declaration declaration = null;

    /**
     * Creates a new StratmasList, taking elements from provided vector.
     * 
     * @param identifier the identifier for the object.
     * @param type the type of the object.
     * @param elements The elements of this list
     */
    protected StratmasList(String identifier, Type type, Vector elements) {
        super(identifier, type, elements);
    }

    /**
     * Creates a new (empty) StratmasList.
     * 
     * @param declaration the declaration describing this list.
     * @param parts the initial parts of this list.
     */
    protected StratmasList(Declaration declaration, Vector parts) {
        super(declaration, parts);
        this.declaration = declaration;
    }

    /**
     * Creates a new (empty) StratmasList.
     * 
     * @param declaration the declaration describing this list.
     */
    protected StratmasList(Declaration declaration) {
        this(declaration, new Vector());
    }

    /**
     * Sets the parent of this object.
     * 
     * @param parent the new parent of this object.
     */
    protected void setParent(StratmasObject parent) {
        super.setParent(parent);
        if (getParent() != null) {
            this.declaration = ((StratmasObject) getParent()).getType()
                    .getSubElement(getTag());
        }
    }

    /**
     * Returns the declaration specifying this object, or null if no such can be specified (e. g. if this object has no parent). Note that
     * the type of the returned declaration may be a superclass of the actual type the object has.
     */
    public Declaration getDeclaration() {
        return this.declaration;
    }

    /**
     * Overridden in order to avoid producing any XML.
     * <p>
     * author Per Alexius
     * 
     * @param b The StringBuffer not to write to.
     * @return The exact same StringBuffer b.
     */
    public StringBuffer toXML(StringBuffer b) {
        return bodyXML(b);
    }

    /**
     * Overriden to not consult its subDeclarations
     * <p>
     * author Daniel Ahlin
     * 
     * @param b The StringBuffer to write to.
     * @return The StringBuffer b with an XML representation of this object's body appended to it.
     */
    public StringBuffer bodyXML(StringBuffer b) {
        for (Iterator it = parts.iterator(); it.hasNext();) {
            ((StratmasObject) it.next()).toXML(b);
        }
        return b;
    }

    /**
     * Returns a StratmasGUIConstructor suitable for constructing objects of this type.
     * 
     * @param declaration the declaration for which the GUI is created.
     */
    protected static StratmasGUIConstructor getGUIConstructor(
            Declaration declaration) {
        return new StratmasListGUIConstructor(declaration);
    }

    /**
     * Returns a StratmasVectorConstructor suitable for constructing objects of this type.
     * 
     * @param declaration The declaration for which the object is created.
     */
    protected static StratmasVectorConstructor getVectorConstructor(
            Declaration declaration) {
        return new StratmasListVectorConstructor(declaration);
    }

    /**
     * Writes this objects Taclan V2 representation to the supplied StringBuffer with the specified indentation. Returns the same buffer.
     * 
     * @param buf the buffer.
     * @param indent the indentation.
     */
    protected StringBuffer toTaclanV2StringBuffer(StringBuffer buf,
            String indent) {
        buf.append(indent + Identifier.toTaclanV2(getIdentifier()) + " = {");
        if (!isLeaf()) {
            for (Enumeration cs = children(); cs.hasMoreElements();) {
                buf.append("\n");
                ((StratmasObject) cs.nextElement())
                        .toTaclanV2StringBuffer(buf, indent
                                + TACLANV2_INDENTATION);
            }
            buf.append("\n" + indent + "}");
        } else {
            buf.append("}");
        }

        return buf;
    }

    /**
     * Adds a new child object to this list. If a child with identical identifier exists a number will be added to the added object's
     * identifier.
     * <p>
     * author Per Alexius
     * 
     * @param part the StratmasObject to add.
     */
    public void addWithUniqueIdentifier(StratmasObject part) {
        int num = 1;
        String baseId = part.getIdentifier();
        while (getChild(part.getIdentifier()) != null) {
            num++;
            part.setIdentifier(baseId + " " + num);
        }
        super.add(part);
    }

    /**
     * Called when a (direct) child of this has changed. The default behaviour for StratmasList is to pass the event upwards in the tree.
     * 
     * @param child the child that changed
     */
    public void childChanged(StratmasObject child, Object initiator) {
        if (getParent() != null) {
            getParent().childChanged(this, initiator);
        }
        fireChildChanged(child, initiator);
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
        Vector elements = new Vector();
        for (Enumeration en = children(); en.hasMoreElements();) {
            elements.add(((StratmasObject) en.nextElement()).clone());
        }
        return new StratmasList(identifier, type, elements);
    }

    /**
     * Returns true if this list is full
     */
    public boolean isFull() {
        return getDeclaration() != null && !getDeclaration().isUnbounded()
                && getChildCount() >= getDeclaration().getMaxOccurs();
    }

    /**
     * Notifies this list that one of its children has changed identifier.
     * 
     * @param oldIdentifier The old identifier.
     * @param newIdentifier The new identifier.
     */
    public void childIdentifierChange(String oldIdentifier, String newIdentifier) {
        // Should be synchronized in some way!!!
        StratmasObject o = partsHash.remove(oldIdentifier);
        if (o != null) {
            partsHash.put(newIdentifier, o);
        }
    }

//     /**
//      * Returns actions associated with this object
//      */
//     public Vector getActions()
//     {
//         Vector res = super.getActions();

//         Vector candidates = new Vector();

//         if (!isFull()) {
//             candidates.add(getType());
//             candidates.addAll(getType().getExpandedDerived());
//             for (Enumeration e = candidates.elements(); 
//                  e.hasMoreElements();) {
//                 Type type = (Type) e.nextElement();
//                 if (!type.isAbstract()) {
//                     // "Unlist" the declaration.
//                     final StratmasList self = this;
//                     final Declaration declaration;
//                     if (getDeclaration() != null) {
//                         declaration = (Declaration) getDeclaration().clone(type);
//                         declaration.setMinOccurs(1);
//                         declaration.setMaxOccurs(1);
//                         declaration.setUnbounded(false);
//                     } else {
//                         declaration = new Declaration(type, 
//                                                       type.getName(), 
//                                                       1, 1, false);
//                     }

//                     res.add(new StratmasAbstractAction("Add " + declaration.getType().getName(), true)
//                         {
//                             public void actionPerformed(ActionEvent e)
//                             {
//                                 StratmasObject newObject = StratmasObjectFactory.defaultCreate(declaration);
//                                 if (newObject != null) {
//                                     addWithUniqueIdentifier(newObject);
//                                 } else {
//                                     StratmasGUIConstructorDialog dialog = 
//                                         StratmasGUIConstructor.buildDialog(StratmasObjectFactory.guiCreate(declaration), false);
//                                     dialog.setVisible(true);
//                                     newObject = dialog.getStratmasObject();
//                                     if (newObject != null) {
//                                         addWithUniqueIdentifier(newObject);
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
        if (!isFull() && getDeclaration() != null) {
            ag.add(getAddActionGroupForListDec(this, getDeclaration()));
        }
        return ag;
    }

    /**
     * Returns true iff this can add provided object.
     * 
     * @param object the object to check for validity.
     */
    public boolean canAdd(StratmasObject object) {
        if (object.getType().canSubstitute(getType())) {
            Declaration declaration = getDeclaration();
            if (declaration != null) {
                // Now, make sure there is room and object is not an
                // ancestor of this.
                return !isFull() && !isAncestor(object);
            } else {
                // No declaration, means we can't check multiplicity
                // constraints. Assume there are none. Then only check
                // ancestry.
                return !object.isAncestor(this);
            }
        }

        return false;
    }
}

/**
 * StratmasListVectorConstructor constructs a StratmasList using a vector.
 * 
 * @version 1, $Date: 2006/07/31 10:18:44 $
 * @author Per Alexius
 */
class StratmasListVectorConstructor extends StratmasVectorConstructor {
    /**
     * Creates a new object using specifications in declaration.
     * 
     * @param declaration the declaration to use.
     */
    public StratmasListVectorConstructor(Declaration declaration) {
        super(declaration);
    }

    /**
     * Returns the StratmasObject this component was created to provide.
     * 
     * @param parts the parts to use in constructing the object.
     */
    public StratmasObject getStratmasObject(Vector parts) {
        return new StratmasList(this.getDeclaration(), parts);
    }
}
