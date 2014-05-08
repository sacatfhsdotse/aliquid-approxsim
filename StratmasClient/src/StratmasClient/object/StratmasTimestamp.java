// $Id: ApproxsimTimestamp.java,v 1.5 2006/05/16 12:37:02 alexius Exp $
/*
 * @(#)ApproxsimTimestamp.java
 */

package ApproxsimClient.object;

import ApproxsimClient.object.type.Type;
import ApproxsimClient.object.type.Declaration;
import ApproxsimClient.object.type.TypeFactory;
import ApproxsimClient.object.primitive.Identifier;
import ApproxsimClient.object.primitive.Timestamp;
import org.w3c.dom.Element;

import java.text.ParseException;

/**
 * ApproxsimTimestamp is a Approxsim timestamp class
 * 
 * @version 1, $Date: 2006/05/16 12:37:02 $
 * @author Per Alexius
 */

public class ApproxsimTimestamp extends ApproxsimSimple {
    /**
     * The number of seconds since 1:st jan 1970 00:00:00.
     */
    Timestamp value;

    /**
     * Creates a new ApproxsimTimestamp.
     * 
     * @param identifier the identifier for the object.
     * @param type the type of the object.
     * @param value the value of the object.
     */
    protected ApproxsimTimestamp(String identifier, Type type, long value) {
        super(identifier, type);
        this.value = new Timestamp(value);
    }

    /**
     * Creates a new ApproxsimTimestamp from a Declaration.
     * 
     * @param declaration the declaration for this object.
     * @param value the value of the string.
     */
    protected ApproxsimTimestamp(Declaration declaration, long value) {
        super(declaration);
        this.value = new Timestamp(value);
    }

    /**
     * Creates a new ApproxsimTimestamp from a Declaration.
     * 
     * @param declaration the declaration for this object.
     * @param value the value of the timestamp.
     */
    protected ApproxsimTimestamp(Declaration declaration, String value)
            throws ParseException {
        this(declaration, 0);
        this.value = Timestamp.parseTimestamp(value);
    }

    /**
     * Returns the string representation of the value part of this object.
     */
    public String valueToString() {
        return value.toDateTimeString();
//        return Long.toString(this.value.getMilliSecs());
    }

    /**
     * Returns a human readable string representation of the value part of this object.
     */
    public String valueToPrettyString() {
        return value.toString();
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
        b.append(NL).append("<value>");
        b.append(value.toDateTimeString());
        b.append("</value>");
        return b;
    }

    /**
     * Returns the timestamp
     * 
     * @return the timestamp
     */
    public Timestamp getValue() {
        return value;
    }

    /**
     * Sets the value of this to the specified value. Responsible for notifying interested parties of the event. FIXME: Notification not yet
     * implemented.
     * 
     * @param newValue the new value of value.
     */
    public void setValue(Timestamp newValue, Object initiator) {
        this.value = newValue;
        fireValueChanged(initiator);
    }

    /**
     * Tries to update the value from the provided string.
     * 
     * @param str the string to use
     * @throws ParseException if str is not a valid string representation of a timestamp
     */
    public void valueFromString(String str, Object initiator)
            throws ParseException {
        Timestamp test = Timestamp.parseTimestamp(str);
        if (!test.equals(getValue())) {
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
        return new ApproxsimTimestampGUIConstructor(declaration);
    }

    /**
     * Creates a ApproxsimTimestamp from a DOM element.
     * 
     * @param n The dom element from which the object is created.
     */
    protected static ApproxsimObject domCreate(Element n) {
        try {
            Timestamp t = Timestamp
                    .parseTimestamp(XMLHelper.getString(n, "value"));
            return new ApproxsimTimestamp(Identifier.getIdentifier(n),
                    TypeFactory.getType("Timestamp"), t.getMilliSecs());
        } catch (NumberFormatException e) {
            return null;
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Creates a ApproxsimTimestamp from the specified Declaration.
     * <p>
     * author Per Alexius
     * 
     * @param declaration The declaration for which the object is created.
     */
    protected static ApproxsimObject defaultCreate(Declaration declaration) {
        return new ApproxsimTimestamp(declaration, 0);
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
        return new ApproxsimTimestamp(identifier, type, value.getMilliSecs());
    }
}

/**
 * ApproxsimTimestampGUIConstructor creates GUIs for creating ApproxsimTimestamp objects.
 * 
 * @version 1, $Date: 2006/05/16 12:37:02 $
 * @author Per Alexius
 */
class ApproxsimTimestampGUIConstructor extends ApproxsimGUIConstructor {
    /**
	 * 
	 */
    private static final long serialVersionUID = -2673474213005674169L;
    javax.swing.JTextField field;

    /**
     * Creates a new ApproxsimTimestampGUIConstructor using the supplied declaration.
     * 
     * @param declaration the declaration to use.
     */
    public ApproxsimTimestampGUIConstructor(Declaration declaration) {
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
            setApproxsimObject(new ApproxsimTimestamp(this.declaration,
                    field.getText()));
        } catch (java.text.ParseException e) {
            System.err.println("FIXME: Don't fail quietly "
                    + getClass().toString());
        }
    }
}
