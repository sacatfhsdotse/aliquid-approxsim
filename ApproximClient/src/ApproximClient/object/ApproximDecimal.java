// $Id: ApproxsimDecimal.java,v 1.3 2006/04/10 09:45:55 dah Exp $
/*
 * @(#)ApproxsimDecimal.java
 */

package ApproxsimClient.object;

import ApproxsimClient.object.type.Type;
import ApproxsimClient.object.type.Declaration;
import ApproxsimClient.object.type.TypeFactory;

import ApproxsimClient.object.primitive.Identifier;

import org.w3c.dom.Element;

import java.text.ParseException;

/**
 * ApproxsimDecimal is a Approxsim adapter for floating point numbers (read doubles).
 * 
 * @version 1, $Date: 2006/04/10 09:45:55 $
 * @author Daniel Ahlin
 */

public class ApproxsimDecimal extends ApproxsimSimple {
    /**
     * The actual double this object represents.
     */
    double value;

    /**
     * Creates a new ApproxsimDecimal.
     * 
     * @param identifier the identifier for the object.
     * @param type the type of the object.
     * @param value the value of the object.
     */
    protected ApproxsimDecimal(String identifier, Type type, double value) {
        super(identifier, type);
        this.value = value;
    }

    /**
     * Creates a new ApproxsimDecimal using default type.
     * 
     * @param identifier the identifier for the object.
     * @param value the value of the object.
     */
    protected ApproxsimDecimal(String identifier, double value) {
        super(identifier, TypeFactory.getType("Double"));
        this.value = value;
    }

    /**
     * Creates a new ApproxsimDecimal from a Declaration.
     * 
     * @param declaration the declaration for this object.
     * @param value the value of the string.
     */
    protected ApproxsimDecimal(Declaration declaration, double value) {
        super(declaration);
        this.value = value;
    }

    /**
     * Returns the string representation of the value part of this object.
     */
    public String valueToString() {
        return Double.toString(this.value);
    }

    /**
     * Accessor for the value of this ApproxsimDecimal.
     * 
     * @return The value of this ApproxsimDecimal.
     */
    public double getValue() {
        return value;
    }

    /**
     * Sets the value of this to the specified value. Responsible for notifying interested parties of the event.
     * 
     * @param newValue the new value of value.
     * @param initiator the object causing the update.
     */
    public void setValue(double newValue, Object initiator) {
        if (this.value != newValue) {
            this.value = newValue;
            fireValueChanged(initiator);
        }
    }

    /**
     * Sets the value of this to the specified value. Responsible for notifying interested parties of the event.
     * 
     * @param newValue the new value of value.
     */
    public void setValue(double newValue) {
        setValue(newValue, null);
    }

    /**
     * Tries to update the value from the provided string.
     * 
     * @param str the string to use
     * @throws ParseException if str is not a valid double.
     */
    public void valueFromString(String str, Object initiator)
            throws ParseException {
        try {
            double test = Double.parseDouble(str);
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
        return new ApproxsimDecimalGUIConstructor(declaration);
    }

    /**
     * Creates a ApproxsimDecimal from a DOM element.
     * <p>
     * author Per Alexius
     * 
     * @param n The dom element from which the object is created.
     */
    protected static ApproxsimObject domCreate(Element n) {
        try {
            return new ApproxsimDecimal(Identifier.getIdentifier(n),
                    TypeFactory.getType("Double"), XMLHelper.getDouble(n,
                                                                       "value"));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Creates a ApproxsimDecimal from the specified Declaration.
     * <p>
     * author Per Alexius
     * 
     * @param declaration The declaration for which the object is created.
     */
    protected static ApproxsimObject defaultCreate(Declaration declaration) {
        return new ApproxsimDecimal(declaration, 0);
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
        return new ApproxsimDecimal(identifier, type, value);
    }
}

/**
 * ApproxsimDecimalGUIConstructor creates GUIs for creating ApproxsimDecimal objects.
 * 
 * @version 1, $Date: 2006/04/10 09:45:55 $
 * @author Daniel Ahlin
 */
class ApproxsimDecimalGUIConstructor extends ApproxsimGUIConstructor {
    /**
	 * 
	 */
    private static final long serialVersionUID = -4717445994361906709L;
    javax.swing.JTextField field;

    /**
     * Creates a new ApproxsimDecimalGUIConstructor using the supplied declaration.
     * 
     * @param declaration the declaration to use.
     */
    public ApproxsimDecimalGUIConstructor(Declaration declaration) {
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
            setApproxsimObject(new ApproxsimDecimal(this.declaration,
                    Double.parseDouble(field.getText())));
        } catch (NumberFormatException e) {
            setApproxsimObject(null);
        }
    }
}
