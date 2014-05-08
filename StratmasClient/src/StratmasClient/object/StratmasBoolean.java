package ApproxsimClient.object;

import ApproxsimClient.object.type.Type;
import ApproxsimClient.object.type.Declaration;
import ApproxsimClient.object.type.TypeFactory;

import ApproxsimClient.object.primitive.Identifier;

import org.w3c.dom.Element;

import java.text.ParseException;

/**
 * ApproxsimBoolean is a Approxsim adapter for booleans.
 * 
 * @version 1, $Date: 2006/04/10 09:45:55 $
 * @author Per Alexius
 */

public class ApproxsimBoolean extends ApproxsimSimple {
    /**
     * The actual boolean this object represents.
     */
    boolean value;

    /**
     * Creates a new ApproxsimBoolean.
     * 
     * @param identifier the identifier for the object.
     * @param type the type of the object.
     * @param value the value of the object.
     */
    protected ApproxsimBoolean(String identifier, Type type, boolean value) {
        super(identifier, type);
        this.value = value;
    }

    /**
     * Creates a new ApproxsimBoolean from a Declaration.
     * 
     * @param declaration the declaration for this object.
     * @param value the value of the string.
     */
    protected ApproxsimBoolean(Declaration declaration, boolean value) {
        super(declaration);
        this.value = value;
    }

    /**
     * Returns the string representation of the value part of this object.
     */
    public String valueToString() {
        return Boolean.toString(this.value);
    }

    public boolean getValue() {
        return value;
    }

    /**
     * Sets the value of this to the specified value. Responsible for notifying interested parties of the event. FIXME: Notification not yet
     * implemented.
     * 
     * @param newValue the new value of value.
     */
    private void setValue(boolean newValue, Object initiator) {
        this.value = newValue;
        fireValueChanged(initiator);
    }

    /**
     * Tries to update the value from the provided string.
     * 
     * @param str the string to use
     * @throws ParseException if the str is not a value parsable to this type.
     */
    public void valueFromString(String str, Object initiator)
            throws ParseException {
        boolean test;
        if (str.equals("true") || str.equals("1")) {
            test = true;
        } else if (str.equals("false") || str.equals("0")) {
            test = false;
        } else {
            throw new ParseException(
                    "Couldn't create Boolean from the string '" + str + "'", 0);
        }

        if (test != getValue()) {
            setValue(test, initiator);
        }
    }

    /**
     * Returns a ApproxsimGUIConstructor suitable for constructing objects of this type.
     * 
     * @param declaration the declaration for which the GUI is created.
     */
    protected static ApproxsimGUIConstructor getGUIConstructor(
            Declaration declaration) {
        return new ApproxsimBooleanGUIConstructor(declaration);
    }

    /**
     * Creates a ApproxsimBoolean from a DOM element.
     * 
     * @param n The dom element from which the object is created.
     */
    protected static ApproxsimObject domCreate(Element n) {
        try {
            return new ApproxsimBoolean(Identifier.getIdentifier(n),
                    TypeFactory.getType(n), XMLHelper.getBoolean(n, "value"));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Creates a ApproxsimBoolean from the specified Declaration.
     * <p>
     * author Per Alexius
     * 
     * @param declaration The declaration for which the object is created.
     */
    protected static ApproxsimObject defaultCreate(Declaration declaration) {
        return new ApproxsimBoolean(declaration, false);
    }

    /**
     * Clones this object. Notice that the Identifier is NOT cloned. Both the clone and the original object will thus keep a reference to
     * the same Identifier object.
     * 
     * @return A clone of this object.
     */
    protected Object clone() {
        return new ApproxsimBoolean(identifier, type, value);
    }
}

/**
 * ApproxsimBooleanGUIConstructor creates GUIs for creating ApproxsimBoolean objects.
 * 
 * @version 1, $Date: 2006/04/10 09:45:55 $
 * @author Per Alexius
 */
class ApproxsimBooleanGUIConstructor extends ApproxsimGUIConstructor {
    /**
	 * 
	 */
    private static final long serialVersionUID = 7632844240093970800L;
    javax.swing.JComboBox field;

    /**
     * Creates a new ApproxsimBooleanGUIConstructor using the supplied declaration.
     * 
     * @param declaration the declaration to use.
     */
    public ApproxsimBooleanGUIConstructor(Declaration declaration) {
        super(declaration);
    }

    /**
     * Builds the panels used by this constructor
     */
    protected void buildPanel() {
        this.add(new javax.swing.JLabel(declaration.getName()));
        this.field = new javax.swing.JComboBox(new Boolean[] {
                new Boolean(true), new Boolean(false) });
        this.add(this.field);
    }

    /**
     * Tries to create the ApproxsimObject from the values in the GUI.
     */
    protected void createApproxsimObject() {
        setApproxsimObject(new ApproxsimBoolean(this.declaration,
                ((Boolean) field.getSelectedItem()).booleanValue()));
    }
}
