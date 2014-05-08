// $Id: ApproxsimSimple.java,v 1.3 2006/04/10 09:45:55 dah Exp $
/*
 * @(#)ApproxsimSimple.java
 */

package ApproxsimClient.object;

import ApproxsimClient.Icon;

import ApproxsimClient.object.type.Type;
import ApproxsimClient.object.type.Declaration;
import ApproxsimClient.object.primitive.Identifier;
import ApproxsimClient.object.primitive.Timestamp;

import java.text.ParseException;

import org.w3c.dom.Element;

/**
 * ApproxsimSimple is the common origin of the atomic objects handled by the ApproxsimClient.
 * 
 * @version 1, $Date: 2006/04/10 09:45:55 $
 * @author Daniel Ahlin
 */
public abstract class ApproxsimSimple extends ApproxsimObjectImpl {
    /**
     * The type of this object.
     */
    Type type;

    /**
     * Creates a new ApproxsimSimple.
     * 
     * @param identifier the identifier for the object.
     * @param type the type of the object.
     */
    protected ApproxsimSimple(String identifier, Type type) {
        super(identifier);
        this.type = type;
    }

    /**
     * Creates a new ApproxsimSimple from a Declaration.
     * 
     * @param declaration the declaration for this object.
     */
    protected ApproxsimSimple(Declaration declaration) {
        this(declaration.getName(), declaration.getType());
    }

    /**
     * Creates a new ApproxsimSimple from a Declaration and changes the Identifier to the specified Identifier.
     * <p>
     * author Per Alexius
     * 
     * @param declaration The Declaration for this object.
     * @param identifier The Identifier to use as Identifier for this object.
     */
    protected ApproxsimSimple(Declaration declaration, String identifier) {
        this(declaration);
        setIdentifier(identifier);
    }

    /**
     * Returns the type of this object.
     */
    public Type getType() {
        return this.type;
    }

    /**
     * Returns the string representation of the value part of this object.
     */
    public abstract String valueToString();

    /**
     * Tries to update the value from the provided string.
     * 
     * @param str the string to use
     * @param initiator the initiator of this change.
     * @throws ParseException if the str is not a value parsable to this type.
     */
    public abstract void valueFromString(String str, Object initiator)
            throws ParseException;

    /**
     * Tries to update the value from the provided string.
     * 
     * @param str the string to use
     * @throws ParseException if the str is not a value parsable to this type.
     */
    public void valueFromString(String str) throws ParseException {
        valueFromString(str, null);
    }

    /**
     * Returns a human readable string representation of the value part of this object.
     */
    public String valueToPrettyString() {
        return valueToString();
    }

    /**
     * Returns the string representation of this object.
     */
    public String toString() {
        return getIdentifier() + ": " + this.valueToPrettyString();
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
        b.append(XMLHelper.encodeSpecialCharacters(valueToString()));
        b.append("</value>");
        return b;
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
                + valueToString());
        return buf;
    }

    /**
     * Notifies all listeners that the value of this simple type has changed.
     */
    protected void fireValueChanged(Object initiator) {
        // Always tell parent that we have changed (if we have one).
        if (getParent() != null) {
            getParent().childChanged(this, initiator);
        }

        ApproxsimEvent event = ApproxsimEvent.getValueChanged(this,
                                                            initiator);
        for (int i = getEventListenerList().size() - 1; i >= 0; i--) {
            getEventListenerList().get(i).eventOccured(event);
        }
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
        try {
            valueFromString(XMLHelper.getString(n, "value"), n);
        } catch (ParseException e) {
            throw new AssertionError("Error updating from DOM element: "
                    + e.getMessage());
        }
    }

    /**
     * Returns the icon used to symbolize this object.
     */
    public Icon getIcon() {
        return Icon.getIcon(this);
    }
}
