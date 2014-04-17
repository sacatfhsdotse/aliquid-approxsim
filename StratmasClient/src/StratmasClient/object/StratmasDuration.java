// $Id: StratmasDuration.java,v 1.3 2006/04/10 09:45:55 dah Exp $
/*
 * @(#)StratmasDuration.java
 */

package StratmasClient.object;

import StratmasClient.object.type.Type;
import StratmasClient.object.type.Declaration;
import StratmasClient.object.type.TypeFactory;
import StratmasClient.object.primitive.Duration;
import StratmasClient.object.primitive.Identifier;
import StratmasClient.object.primitive.Timestamp;
import org.w3c.dom.Element;

import java.text.ParseException;

/**
 * StratmasDuration is a Stratmas duration class
 * 
 * @version 1, $Date: 2006/04/10 09:45:55 $
 * @author Daniel Ahlin
 */

public class StratmasDuration extends StratmasSimple {
    /**
     * The number of milliseconds the duration is.
     */
    Duration value;

    /**
     * Creates a new StratmasDuration.
     * 
     * @param identifier the identifier for the object.
     * @param type the type of the object.
     * @param value the value of the object.
     */
    protected StratmasDuration(String identifier, Type type, long value) {
        super(identifier, type);
        this.value = new Duration(value);
    }

    /**
     * Creates a new StratmasDuration from a Declaration.
     * 
     * @param declaration the declaration for this object.
     * @param value the value of the string.
     */
    protected StratmasDuration(Declaration declaration, long value) {
        super(declaration);
        this.value = new Duration(value);
    }

    /**
     * Creates a new StratmasDuration from a Declaration.
     * 
     * @param declaration the declaration for this object.
     * @param value the value of the duration.
     */
    protected StratmasDuration(Declaration declaration, String value)
            throws ParseException {
        this(declaration, 0);
        this.value = Duration.parseDuration(value);
    }

    /**
     * Returns the string representation of the value part of this object.
     */
    public String valueToString() {
        return Long.toString(value.getMilliSecs());
    }

    /**
     * Returns a human readable string representation of the value part of this object.
     */
    public String valueToPrettyString() {
        return value.toString();
    }

    /**
     * Returns the number of milliseconds.
     * 
     * @return The number of milliseconds.
     */
    public Duration getValue() {
        return this.value;
    }

    /**
     * Sets the value of this to the specified value. Responsible for notifying interested parties of the event. FIXME: Notification not yet
     * implemented.
     * 
     * @param newValue the new value of value.
     */
    private void setValue(Duration newValue, Object initiator) {
        this.value = newValue;
        fireValueChanged(initiator);
    }

    /**
     * Tries to update the value from the provided string.
     * 
     * @param str the string to use
     * @throws ParseException if str is not a valid string representation of a Duration
     */
    public void valueFromString(String str, Object initiator)
            throws ParseException {
        Duration test = Duration.parseDuration(str);
        if (!test.equals(getValue())) {
            setValue(test, initiator);
        }
    }

    /**
     * Returns a StratmasGUIConstructor suitable for constructing objects of this type.
     * 
     * @param declaration the declaration for which the GUI is created.
     */
    protected static StratmasGUIConstructor getGUIConstructor(
            Declaration declaration) {
        return new StratmasDurationGUIConstructor(declaration);
    }

    /**
     * Creates a StratmasDuration from a DOM element.
     * 
     * @param n The dom element from which the object is created.
     */
    protected static StratmasObject domCreate(Element n) {
        try {
            return new StratmasDuration(Identifier.getIdentifier(n),
                    TypeFactory.getType("Duration"), XMLHelper.getLong(n,
                                                                       "value"));
        } catch (NumberFormatException e) {
            System.err.println("Number format exeption for "
                    + XMLHelper.getString(n, "value"));
            return null;
        }
    }

    /**
     * Creates a StratmasDuration from the specified Declaration.
     * <p>
     * author Per Alexius
     * 
     * @param declaration The declaration for which the object is created.
     */
    public static StratmasObject defaultCreate(Declaration declaration) {
        return new StratmasDuration(declaration, 0);
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
                + "\"" + valueToPrettyString() + "\"");
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
        return new StratmasDuration(identifier, type, value.getMilliSecs());
    }

    /**
     * Updates this object with the data contained in the Element n.
     * <p>
     * author Per Alexius
     * 
     * @param n The DOM Element from which to fetch the data.
     * @param t The simulation time for which the data is valid.
     */
    public void update(org.w3c.dom.Element n, Timestamp t) {
        setValue(new Duration(XMLHelper.getLong(n, "value")), n);
    }
}

/**
 * StratmasDurationGUIConstructor creates GUIs for creating StratmasDuration objects.
 * 
 * @version 1, $Date: 2006/04/10 09:45:55 $
 * @author Per Alexius
 */
class StratmasDurationGUIConstructor extends StratmasGUIConstructor {
    /**
	 * 
	 */
    private static final long serialVersionUID = 8348999474215724056L;
    javax.swing.JTextField field;

    /**
     * Creates a new StratmasDurationGUIConstructor using the supplied declaration.
     * 
     * @param declaration the declaration to use.
     */
    public StratmasDurationGUIConstructor(Declaration declaration) {
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
            setStratmasObject(new StratmasDuration(this.declaration,
                    field.getText()));
        } catch (java.text.ParseException e) {
            System.err.println("FIXME: Don't fail quietly "
                    + getClass().toString());
        }
    }
}
