// $Id: ApproxsimReference.java,v 1.5 2006/07/19 07:01:59 alexius Exp $
/*
 * @(#)ApproxsimReference.java
 */

package ApproxsimClient.object;

import ApproxsimClient.object.type.Type;
import ApproxsimClient.object.type.TypeFactory;
import ApproxsimClient.object.type.Declaration;
import ApproxsimClient.object.primitive.Reference;
import ApproxsimClient.object.primitive.Identifier;
import ApproxsimClient.object.primitive.Timestamp;

import org.w3c.dom.Element;

import java.text.ParseException;

/**
 * ApproxsimReference is a Approxsim adapter for references.
 * 
 * @version 1, $Date: 2006/07/19 07:01:59 $
 * @author Daniel Ahlin
 */

public class ApproxsimReference extends ApproxsimSimple {
    /**
     * The actual reference this object represents.
     */
    Reference value;

    /**
     * Creates a new ApproxsimReference.
     * 
     * @param identifier the identifier for the object.
     * @param type the type of the object.
     * @param value the value of the object.
     */
    protected ApproxsimReference(String identifier, Type type, Reference value) {
        super(identifier, type);
        this.value = value;
    }

    /**
     * Creates a new ApproxsimReference from a Declaration.
     * 
     * @param declaration the declaration for this object.
     * @param value the value of the string.
     */
    protected ApproxsimReference(Declaration declaration, Reference value) {
        super(declaration);
        this.value = value;
    }

    /**
     * Creates a new ApproxsimReference from a Declaration.
     * 
     * @param declaration the declaration for this object.
     * @param str the value of the string.
     */
    protected ApproxsimReference(Declaration declaration, String str) {
        super(declaration);
        this.value = Reference.parseReference(str);
    }

    /**
     * Returns the string representation of the value part of this object.
     */
    public String valueToString() {
        return value.toString();
    }

    public Reference getValue() {
        return value;
    }

    /**
     * Sets the value of this to the specified value. Responsible for notifying interested parties of the event.
     * 
     * @param newValue the new value of value.
     */
    public void setValue(Reference newValue, Object initiator) {
        this.value = newValue;
        fireValueChanged(initiator);
    }

    /**
     * Tries to update the value from the provided string. FIXME: Maybe we should test resolvability here, then again maybe not.
     * 
     * @param str the string to use
     * @throws ParseException if str is not a valid string representation of a reference
     */
    public void valueFromString(String str, Object initiator)
            throws ParseException {
        Reference test = Reference.parseReference(str);

        if (test != null) {
            if (!getValue().equals(test)) {
                setValue(test, initiator);
            }
        } else {
            throw new ParseException("Illegal reference.", 0);
        }
    }

    /**
     * Creates an XML representation of the body of this object.
     * 
     * @param b The StringBuffer to write to.
     * @return The StringBuffer b with an XML representation of this object's body appended to it.
     */
    public StringBuffer bodyXML(StringBuffer b) {
        // Resolve the reference and print the reference to that
        // object.
        ApproxsimObject target = value.resolve(this.getParent());
        if (target == null) {
            getValue().bodyXML(b);
        } else {
            target.getReference().bodyXML(b);
        }

        return b;
    }

    /**
     * Creates a ApproxsimReference from a DOM element.
     * <p>
     * author Per Alexius
     * 
     * @param n The dom element from which the object is created.
     */
    protected static ApproxsimObject domCreate(Element n) {
        return new ApproxsimReference(Identifier.getIdentifier(n),
                TypeFactory.getType(n), Reference.getReference(n));
    }

    /**
     * Creates a ApproxsimReference from the specified Declaration.
     * <p>
     * author Per Alexius
     * 
     * @param declaration The declaration for which the object is created.
     */
    protected static ApproxsimObject defaultCreate(Declaration declaration) {
        return new ApproxsimReference(declaration, "UNKNOWN");
    }

    /**
     * Writes this objects Taclan V2 representation to the supplied StringBuffer with the specified indentation. Returns the same buffer.
     * 
     * @param buf the buffer.
     * @param indent the indentation.
     */
    protected StringBuffer toTaclanV2StringBuffer(StringBuffer buf,
            String indent) {
        buf.append(indent + Identifier.toTaclanV2(getIdentifier()) + " = "
                + getValue().toTaclanV2());
        return buf;
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
        return new ApproxsimReference(identifier, type,
                (Reference) value.clone());
    }

    /**
     * Returns a ApproxsimGUIConstructor suitable for constructing objects of this type.
     * 
     * @param declaration the declaration for which the GUI is created.
     */
    protected static ApproxsimGUIConstructor getGUIConstructor(
            Declaration declaration) {
        return new ApproxsimReferenceGUIConstructor(declaration);
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
        setValue(Reference.getReference(n), n);
    }
}

/**
 * ApproxsimReferenceGUIConstructor creates GUIs for creating ApproxsimReference objects.
 * 
 * @version 1, $Date: 2006/07/19 07:01:59 $
 * @author Per Alexius
 */
class ApproxsimReferenceGUIConstructor extends ApproxsimGUIConstructor {
    /**
	 * 
	 */
    private static final long serialVersionUID = 6003112187583127032L;
    javax.swing.JTextField field;

    /**
     * Creates a new ApproxsimReferenceGUIConstructor using the supplied declaration.
     * 
     * @param declaration the declaration to use.
     */
    public ApproxsimReferenceGUIConstructor(Declaration declaration) {
        super(declaration);
    }

    /**
     * Builds the panels used by this constructor
     */
    protected void buildPanel() {
        this.add(new javax.swing.JLabel(declaration.getName()));
        this.field = new javax.swing.JTextField(10);
        this.add(this.field);
    }

    /**
     * Tries to create the ApproxsimObject from the values in the GUI.
     */
    protected void createApproxsimObject() {
        if (field.getText().length() != 0) {
            ApproxsimReference prospect = new ApproxsimReference(
                    this.declaration, field.getText());
            setApproxsimObject(prospect);
        } else {
            setApproxsimObject(null);
        }
    }
}
