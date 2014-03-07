//         $Id: StratmasDecimal.java,v 1.3 2006/04/10 09:45:55 dah Exp $
/*
 * @(#)StratmasDecimal.java
 */

package StratmasClient.object;

import StratmasClient.object.type.Type;
import StratmasClient.object.type.Declaration;
import StratmasClient.object.type.TypeFactory;

import StratmasClient.object.primitive.Identifier;

import org.w3c.dom.Element;

import java.text.ParseException;

/**
 * StratmasDecimal is a Stratmas adapter for floating point numbers
 * (read doubles).
 *
 * @version 1, $Date: 2006/04/10 09:45:55 $
 * @author  Daniel Ahlin
*/

public class StratmasDecimal extends StratmasSimple
{
    /**
     * The actual double this object represents.
     */
    double value;

    /**
     * Creates a new StratmasDecimal.
     *
     * @param identifier the identifier for the object.
     * @param type the type of the object.
     * @param value the value of the object.
     */
    protected StratmasDecimal(String identifier, Type type, double value)
    {
        super(identifier, type);
        this.value = value;
    }

    /**
     * Creates a new StratmasDecimal using default type.
     *
     * @param identifier the identifier for the object.
     * @param value the value of the object.
     */
    protected StratmasDecimal(String identifier, double value)
    {
        super(identifier, TypeFactory.getType("Double"));
        this.value = value;
    }

    /**
     * Creates a new StratmasDecimal from a Declaration.
     *
     * @param declaration the declaration for this object.
     * @param value the value of the string.
     */
    protected StratmasDecimal(Declaration declaration, double value)
    {
        super(declaration);
        this.value = value;
    }

    /**
     * Returns the string representation of the value part of this object.
     */
    public String valueToString()
    {
        return Double.toString(this.value);
    }

     /**
      * Accessor for the value of this StratmasDecimal.
      *
      * @return The value of this StratmasDecimal.
      */
    public double getValue()
    {
        return value;
    }

     /**
      * Sets the value of this to the specified value. Responsible for
      * notifying interested parties of the event.
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
     * Sets the value of this to the specified value. Responsible for
     * notifying interested parties of the event.
     *
     * @param newValue the new value of value.
     */
     public void setValue(double newValue)
    {
        setValue(newValue, null);
    }

    /**
     * Tries to update the value from the provided string.
     *
     * @param str the string to use
     * @throws ParseException if str is not a valid double.
     */
    public void valueFromString(String str, Object initiator) 
        throws ParseException
    {
        try {
            double test = Double.parseDouble(str);
            if(test != getValue()) {
                setValue(test, initiator);
            }
        } catch (NumberFormatException e) {
            throw new ParseException(e.getMessage(), 0);
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
        return new StratmasDecimalGUIConstructor(declaration); 
    }    


    /**
     * Creates a StratmasDecimal from a DOM element.
     *
     * <p> author Per Alexius
     *    
     * @param n The dom element from which the object is created.
     */
    protected static StratmasObject domCreate(Element n)
    {
        try {
            return new StratmasDecimal(Identifier.getIdentifier(n),
                                       TypeFactory.getType("Double"),
                                       XMLHelper.getDouble(n, "value"));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Creates a StratmasDecimal from the specified Declaration.
     *
     * <p> author Per Alexius
     *
     * @param declaration The declaration for which the object is created.
     */
    protected static StratmasObject defaultCreate(Declaration declaration)
    {
        return new StratmasDecimal(declaration, 0);
    }
    
    /**
     * Clones this object. Notice that the Identifier is NOT
     * cloned. Both the clone and the original object will thus keep a
     * reference to the same Identifier object.
     *
     * <p> author Per Alexius
     *
     * @return A clone of this object.
     */
     protected Object clone() {
          return new StratmasDecimal(identifier, type, value);
     }
}

/**
 * StratmasDecimalGUIConstructor creates GUIs for creating
 * StratmasDecimal objects.
 *
 * @version 1, $Date: 2006/04/10 09:45:55 $
 * @author  Daniel Ahlin
*/
class StratmasDecimalGUIConstructor extends StratmasGUIConstructor
{
    javax.swing.JTextField field;

    /**
     * Creates a new StratmasDecimalGUIConstructor using the supplied declaration.
     * @param declaration the declaration to use.
     */
    public StratmasDecimalGUIConstructor(Declaration declaration)
    {
        super(declaration);
    }

    /**
     * Builds the panels used by this constructor
     */    
    protected void buildPanel()
    {
        this.add(new javax.swing.JLabel(declaration.getName()));
        this.field = new javax.swing.JTextField(10);
        this.add(this.field);
    }

    /**
     * Tries to create the StratmasObject from the values in the GUI.
     */
    protected void createStratmasObject()
    {
        try {
            setStratmasObject(new StratmasDecimal(this.declaration, Double.parseDouble(field.getText())));
        } catch (NumberFormatException e) {
            setStratmasObject(null);
        }        
    }
}
