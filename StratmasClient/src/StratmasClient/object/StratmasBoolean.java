package StratmasClient.object;

import StratmasClient.object.type.Type;
import StratmasClient.object.type.Declaration;
import StratmasClient.object.type.TypeFactory;

import StratmasClient.object.primitive.Identifier;

import org.w3c.dom.Element;

import java.text.ParseException;

/**
 * StratmasBoolean is a Stratmas adapter for booleans.
 *
 * @version 1, $Date: 2006/04/10 09:45:55 $
 * @author  Per Alexius
*/

public class StratmasBoolean extends StratmasSimple
{
    /**
     * The actual boolean this object represents.
     */
    boolean value;

    /**
     * Creates a new StratmasBoolean.
     *
     * @param identifier the identifier for the object.
     * @param type the type of the object.
     * @param value the value of the object.
     */
    protected StratmasBoolean(String identifier, Type type, boolean value)
    {
        super(identifier, type);
        this.value = value;
    }

    /**
     * Creates a new StratmasBoolean from a Declaration.
     *
     * @param declaration the declaration for this object.
     * @param value the value of the string.
     */
    protected StratmasBoolean(Declaration declaration, boolean value)
    {
        super(declaration);
        this.value = value;
    }

    /**
     * Returns the string representation of the value part of this object.
     */
    public String valueToString()
    {
        return Boolean.toString(this.value);
    }

    public boolean getValue()
    {
        return value;
    }

    /**
     * Sets the value of this to the specified value. Responsible for
     * notifying interested parties of the event.
     * FIXME: Notification not yet implemented.
     *
     * @param newValue the new value of value.
     */
    private void setValue(boolean newValue, Object initiator)
    {
        this.value = newValue;
        fireValueChanged(initiator);
    }

    /**
     * Tries to update the value from the provided string.
     *
     * @param str the string to use
     * @throws ParseException if the str is not a value
     * parsable to this type.
     */
    public void valueFromString(String str, Object initiator) throws ParseException
    {
         boolean test;
         if (str.equals("true") || str.equals("1")) {
              test = true;
         }
         else if (str.equals("false") || str.equals("0")) {
              test = false;
         }
         else {
             throw new ParseException("Couldn't create Boolean from the string '" + str + "'", 0);
         }

         if(test != getValue()) {
              setValue(test, initiator);
         }
    }

    /**
     * Returns a StratmasGUIConstructor suitable for constructing
     * objects of this type.
     *
     * @param declaration the declaration for which the GUI is created.
     */
    protected static StratmasGUIConstructor getGUIConstructor(Declaration declaration)
    {
        return new StratmasBooleanGUIConstructor(declaration); 
    }
    
    /**
     * Creates a StratmasBoolean from a DOM element.
     *    
     * @param n The dom element from which the object is created.
     */
    protected static StratmasObject domCreate(Element n)
    {
        try {
            return new StratmasBoolean(Identifier.getIdentifier(n),
                                       TypeFactory.getType(n),
                                       XMLHelper.getBoolean(n, "value"));
        } catch (NumberFormatException e) {
            return null;
        }        
    }

    /**
     * Creates a StratmasBoolean from the specified Declaration.
     *
     * <p> author Per Alexius
     *
     * @param declaration The declaration for which the object is created.
     */
    protected static StratmasObject defaultCreate(Declaration declaration)
    {
        return new StratmasBoolean(declaration, false);
    }
    
    /**
     * Clones this object. Notice that the Identifier is NOT
     * cloned. Both the clone and the original object will thus keep a
     * reference to the same Identifier object.
     *
     * @return A clone of this object.
     */
     protected Object clone() {
          return new StratmasBoolean(identifier, type, value);
     }
}

/**
 * StratmasBooleanGUIConstructor creates GUIs for creating
 * StratmasBoolean objects.
 *
 * @version 1, $Date: 2006/04/10 09:45:55 $
 * @author  Per Alexius
*/
class StratmasBooleanGUIConstructor extends StratmasGUIConstructor
{
    javax.swing.JComboBox field;

    /**
     * Creates a new StratmasBooleanGUIConstructor using the supplied
     * declaration.  
     *
     * @param declaration the declaration to use.
     */
    public StratmasBooleanGUIConstructor(Declaration declaration)
    {
        super(declaration);
    }

    /**
     * Builds the panels used by this constructor
     */    
    protected void buildPanel()
    {
        this.add(new javax.swing.JLabel(declaration.getName()));
        this.field = new javax.swing.JComboBox(new Boolean[] {new Boolean(true), 
                                                              new Boolean(false)});        
        this.add(this.field);
    }

    /**
     * Tries to create the StratmasObject from the values in the GUI.
     */
    protected void createStratmasObject()
    {
        setStratmasObject(new StratmasBoolean(this.declaration, 
                                              ((Boolean) field.getSelectedItem()).booleanValue()));
    }
}
