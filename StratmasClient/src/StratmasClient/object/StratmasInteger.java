// $Id: StratmasInteger.java,v 1.3 2006/04/10 09:45:55 dah Exp $
/*
 * @(#)StratmasInteger.java
 */

package StratmasClient.object;

import StratmasClient.object.type.Type;
import StratmasClient.object.type.Declaration;
import StratmasClient.object.type.TypeFactory;
import StratmasClient.object.primitive.Identifier;

import org.w3c.dom.Element;

import java.text.ParseException;

/**
 * StratmasInteger is a Stratmas adapter for integers (read longs).
 * 
 * @version 1, $Date: 2006/04/10 09:45:55 $
 * @author Daniel Ahlin
 */

public class StratmasInteger extends StratmasSimple {
    /**
     * The actual long this object represents.
     */
    long value;

    /**
     * Creates a new StratmasInteger.
     * 
     * @param identifier the identifier for the object.
     * @param type the type of the object.
     * @param value the value of the object.
     */
    protected StratmasInteger(String identifier, Type type, long value) {
        super(identifier, type);
        this.value = value;
    }

    /**
     * Creates a new StratmasInteger from a Declaration.
     * 
     * @param declaration the declaration for this object.
     * @param value the value of the string.
     */
    protected StratmasInteger(Declaration declaration, long value) {
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
     * Returns a StratmasGUIConstructor suitable for constructing objects of this type.
     * 
     * @param declaration the declaration for which the GUI is created.
     */
    protected static StratmasGUIConstructor getGUIConstructor(
            Declaration declaration) {
        return new StratmasIntegerGUIConstructor(declaration);
    }

    /**
     * Creates a StratmasInteger from a DOM element.
     * 
     * @param n The dom element from which the object is created.
     */
    protected static StratmasObject domCreate(Element n) {
        try {
            return new StratmasInteger(Identifier.getIdentifier(n),
                    TypeFactory.getType(n), XMLHelper.getLong(n, "value"));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Creates a StratmasInteger from the specified Declaration.
     * <p>
     * author Per Alexius
     * 
     * @param declaration The declaration for which the object is created.
     */
    protected static StratmasObject defaultCreate(Declaration declaration) {
        return new StratmasInteger(declaration, 0);
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
        return new StratmasInteger(identifier, type, value);
    }
}

/**
 * StratmasIntegerGUIConstructor creates GUIs for creating StratmasInteger objects.
 * 
 * @version 1, $Date: 2006/04/10 09:45:55 $
 * @author Daniel Ahlin
 */
class StratmasIntegerGUIConstructor extends StratmasGUIConstructor {
    /**
	 * 
	 */
    private static final long serialVersionUID = -7202391616892372609L;
    javax.swing.JTextField field;

    /**
     * Creates a new StratmasIntegerGUIConstructor using the supplied declaration.
     * 
     * @param declaration the declaration to use.
     */
    public StratmasIntegerGUIConstructor(Declaration declaration) {
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
     * Tries to create the StratmasObject from the values in the GUI.
     */
    protected void createStratmasObject() {
        try {
            setStratmasObject(new StratmasInteger(this.declaration,
                    Long.parseLong(field.getText())));
        } catch (NumberFormatException e) {
            setStratmasObject(null);
        }
    }
}
