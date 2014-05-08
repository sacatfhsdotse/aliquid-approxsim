// $Id: ApproxsimInteger.java,v 1.3 2006/04/10 09:45:55 dah Exp $
/*
 * @(#)ApproxsimInteger.java
 */

package ApproxsimClient.object;

import ApproxsimClient.object.type.Type;
import ApproxsimClient.object.type.Declaration;
import ApproxsimClient.object.type.TypeFactory;
import ApproxsimClient.object.primitive.Identifier;

import org.w3c.dom.Element;

import java.text.ParseException;

/**
 * ApproxsimInteger is a Approxsim adapter for integers (read longs).
 * 
 * @version 1, $Date: 2006/04/10 09:45:55 $
 * @author Daniel Ahlin
 */

public class ApproxsimInteger extends ApproxsimSimple {
    /**
     * The actual long this object represents.
     */
    long value;

    /**
     * Creates a new ApproxsimInteger.
     * 
     * @param identifier the identifier for the object.
     * @param type the type of the object.
     * @param value the value of the object.
     */
    protected ApproxsimInteger(String identifier, Type type, long value) {
        super(identifier, type);
        this.value = value;
    }

    /**
     * Creates a new ApproxsimInteger from a Declaration.
     * 
     * @param declaration the declaration for this object.
     * @param value the value of the string.
     */
    protected ApproxsimInteger(Declaration declaration, long value) {
        super(declaration);
        this.value = value;
    }

    /**
     * Returns the string representation of the value part of this object.
     */
    public String valueToString() {
        return Long.toString(this.value);
    }

    public long getValue() {
        return value;
    }

    /**
     * Sets the value of this to the specified value. Responsible for notifying interested parties of the event.
     * 
     * @param newValue the new value of value.
     */
    private void setValue(long newValue, Object initiator) {
        this.value = newValue;
        fireValueChanged(initiator);
    }

    /**
     * Sets the value of this to the specified value. Responsible for notifying interested parties of the event.
     * 
     * @param newValue the new value of value.
     */
    public void setValue(long newValue) {
        setValue(newValue, null);
    }

    /**
     * Tries to update the value from the provided string.
     * 
     * @param str the string to use
     * @param initiator the initiator of this change.
     * @throws ParseException if str is not a valid double.
     */
    public void valueFromString(String str, Object initiator)
            throws ParseException {
        try {
            long test = Long.parseLong(str);

            if (test != getValue()) {
                setValue(test, initiator);
            }
        } catch (NumberFormatException e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }

    /**
     * Returns a ApproxsimGUIConstructor suitable for constructing objects of this type.
     * 
     * @param declaration the declaration for which the GUI is created.
     */
    protected static ApproxsimGUIConstructor getGUIConstructor(
            Declaration declaration) {
        return new ApproxsimIntegerGUIConstructor(declaration);
    }

    /**
     * Creates a ApproxsimInteger from a DOM element.
     * 
     * @param n The dom element from which the object is created.
     */
    protected static ApproxsimObject domCreate(Element n) {
        try {
            return new ApproxsimInteger(Identifier.getIdentifier(n),
                    TypeFactory.getType(n), XMLHelper.getLong(n, "value"));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Creates a ApproxsimInteger from the specified Declaration.
     * <p>
     * author Per Alexius
     * 
     * @param declaration The declaration for which the object is created.
     */
    protected static ApproxsimObject defaultCreate(Declaration declaration) {
        return new ApproxsimInteger(declaration, 0);
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
        return new ApproxsimInteger(identifier, type, value);
    }
}

/**
 * ApproxsimIntegerGUIConstructor creates GUIs for creating ApproxsimInteger objects.
 * 
 * @version 1, $Date: 2006/04/10 09:45:55 $
 * @author Daniel Ahlin
 */
class ApproxsimIntegerGUIConstructor extends ApproxsimGUIConstructor {
    /**
	 * 
	 */
    private static final long serialVersionUID = -7202391616892372609L;
    javax.swing.JTextField field;

    /**
     * Creates a new ApproxsimIntegerGUIConstructor using the supplied declaration.
     * 
     * @param declaration the declaration to use.
     */
    public ApproxsimIntegerGUIConstructor(Declaration declaration) {
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
        try {
            setApproxsimObject(new ApproxsimInteger(this.declaration,
                    Long.parseLong(field.getText())));
        } catch (NumberFormatException e) {
            setApproxsimObject(null);
        }
    }
}
