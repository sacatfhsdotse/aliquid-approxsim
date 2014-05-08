// $Id: ApproxsimString.java,v 1.3 2006/04/10 09:45:55 dah Exp $
/*
 * @(#)ApproxsimString.java
 */

package ApproxsimClient.object;

import ApproxsimClient.object.type.Type;
import ApproxsimClient.object.type.Declaration;
import ApproxsimClient.object.type.TypeFactory;
import ApproxsimClient.object.primitive.Identifier;
import org.w3c.dom.Element;

/**
 * ApproxsimString is a Approxsim adapter for strings.
 * 
 * @version 1, $Date: 2006/04/10 09:45:55 $
 * @author Daniel Ahlin
 */

public class ApproxsimString extends ApproxsimSimple {
    /**
     * The actual string this object represents.
     */
    String value;

    /**
     * Creates a new ApproxsimString.
     * 
     * @param identifier the identifier for the object.
     * @param type the type of the object.
     * @param value the value of the string.
     */
    protected ApproxsimString(String identifier, Type type, String value) {
        super(identifier, type);
        this.value = value;
    }

    /**
     * Creates a new ApproxsimString from a Declaration.
     * 
     * @param declaration the declaration for this object.
     * @param value the value of the string.
     */
    protected ApproxsimString(Declaration declaration, String value) {
        super(declaration);
        this.value = value;
    }

    /**
     * Returns the string representation of the value part of this object.
     */
    public String valueToString() {
        return this.value;
    }

    /**
     * Returns the the value of this ApproxsimString
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Sets the value of this to the specified value. Responsible for notifying interested parties of the event. FIXME: Notification not yet
     * implemented.
     * 
     * @param newValue the new value of value.
     */
    private void setValue(String newValue, Object initiator) {
        this.value = newValue;
        fireValueChanged(initiator);
    }

    /**
     * Tries to update the value from the provided string.
     * 
     * @param str the string to use
     */
    public void valueFromString(String str, Object initiator) {
        if (!getValue().equals(str)) {
            setValue(str, initiator);
        }
    }

    /**
     * Returns a ApproxsimGUIConstructor suitable for constructing objects of this type.
     * 
     * @param declaration the declaration for which the GUI is created.
     */
    protected static ApproxsimGUIConstructor getGUIConstructor(
            Declaration declaration) {
        return new ApproxsimStringGUIConstructor(declaration);
    }

    /**
     * Creates a ApproxsimString from a DOM element.
     * <p>
     * author Per Alexius
     * 
     * @param n The dom element from which the object is created.
     */
    protected static ApproxsimObject domCreate(Element n) {
        return new ApproxsimString(Identifier.getIdentifier(n),
                TypeFactory.getType("String"), XMLHelper.getString(n, "value"));
    }

    /**
     * Creates a ApproxsimString from the specified Declaration.
     * <p>
     * author Per Alexius
     * 
     * @param declaration The declaration for which the object is created.
     */
    protected static ApproxsimObject defaultCreate(Declaration declaration) {
        return new ApproxsimString(declaration, "");
    }

    /**
     * Writes this objects Taclan V2 representation to the supplied StringBuffer with the specified indentation. Returns the same buffer.
     * 
     * @param buf the buffer.
     * @param indent the indentation.
     */
    protected StringBuffer toTaclanV2StringBuffer(StringBuffer buf,
            String indent) {
        // Wash a bit.
        String wash = valueToString();
        wash = wash.replaceAll("\\\\", "\\\\");
        wash = wash.replaceAll("\n", "\\\\n");
        wash = wash.replaceAll("\"", "\\\\\"");

        buf.append(indent + Identifier.toTaclanV2(getIdentifier()) + " = "
                + "\"" + wash + "\"");
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
        return new ApproxsimString(identifier, type, value);
    }
}

/**
 * ApproxsimStringGUIConstructor creates GUIs for creating ApproxsimString objects.
 * 
 * @version 1, $Date: 2006/04/10 09:45:55 $
 * @author Daniel Ahlin
 */
class ApproxsimStringGUIConstructor extends ApproxsimGUIConstructor {
    /**
	 * 
	 */
    private static final long serialVersionUID = 2060323101712336817L;
    javax.swing.JTextField field;

    /**
     * Creates a new ApproxsimStringGUIConstructor using the supplied declaration.
     * 
     * @param declaration the declaration to use.
     */
    public ApproxsimStringGUIConstructor(Declaration declaration) {
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
        setApproxsimObject(new ApproxsimString(this.declaration, field.getText()));
    }
}
