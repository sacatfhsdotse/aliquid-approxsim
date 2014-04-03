//         $Id: SymbolIDCode.java,v 1.4 2006/05/16 09:10:48 alexius Exp $
/*
 * @(#)SymbolIDCode.java
 */

package StratmasClient.object;

import java.text.ParseException;

import StratmasClient.object.primitive.Identifier;
import StratmasClient.object.primitive.Timestamp;
import StratmasClient.object.type.TypeFactory;
import StratmasClient.object.type.Declaration;
import StratmasClient.object.type.Type;

import org.w3c.dom.Element;

/**
 * SymbolIDCode is a Stratmas symbolIDCode class
 *
 * @version 1, $Date: 2006/05/16 09:10:48 $
 * @author  Per Alexius
 */

public class SymbolIDCode extends StratmasSimple
{
    /**
     * The string containing the SymbolIDCode.
     */
    String value;
    
    /**
     * Creates a new SymbolIDCode.
     *
     * @param identifier the identifier for the object.
     * @param type the type of the object.
     * @param value the value of the object.
     */
    protected SymbolIDCode(String identifier, Type type, String value) throws ParseException
    {
         super(identifier, type);
         setValue(value, this);
    }
    
    /**
     * Creates a new SymbolIDCode from a Declaration.
     *
     * @param declaration the declaration for this object.
     * @param value the value of the string.
     */
    protected SymbolIDCode(Declaration declaration, String value) throws ParseException
    {
          super(declaration);
          setValue(value, this);
    }

    /**
     * Returns the string representation of the value part of this object.
     */
    public String valueToString() {
        return value;
    }
    
    /**
     * Returns the symbolIDCode.
     *
     * @return The symbolIDCode.
     */
    protected String getValue() {
        return value;
    }

    /**
     * Sets the value of this to the specified value. Responsible for
     * notifying interested parties of the event.
     *
     * @param newValue the new value of value.
     */
    private void setValue(String newValue, Object initiator) throws ParseException
    {
         String trimmedValue = newValue.trim();
         if (trimmedValue.length() != 15) {
              throw new ParseException("'" + trimmedValue + "' is an illegal Symbol ID code", 15);
         }
         else if (!trimmedValue.equals(this.value)) {
              this.value = trimmedValue;
              // HACK if there is a parent of this symbolcode, update its
              // icon.
              if (getParent() != null && getParent() instanceof StratmasObjectImpl) {
                   ((StratmasObjectDynImpl) getParent()).createIcon();
              }
        
              fireValueChanged(initiator);
         }
    }

    /**
     * Tries to update the value from the provided string.
     *
     * @param str the string to use
     * @throws ParseException if the new value is not 15 chars long
     */
    public void valueFromString(String str, Object initiator) throws ParseException
    {
         setValue(str, initiator);
    }

   
     /**
      * Creates an XML representation of the body of this object.
      *
      * <p>author  Per Alexius
      *
      * @param b The StringBuffer to write to.
      * @return The StringBuffer b with an XML representation of this
      * object's body appended to it.
      */
     public StringBuffer bodyXML(StringBuffer b) {
          b.append(NL).append("<value>");
          b.append("<value>").append(valueToString()).append("</value>");
          b.append("</value>");
          return b;
     }
    
     /**
      * Returns a StratmasVectorConstructor suitable for constructing
      * objects of this type.
      *
      * @param declaration the declaration for which the object is created.
      */
     protected static StratmasVectorConstructor getVectorConstructor(Declaration declaration) {
          return new SymbolIDCodeVectorConstructor(declaration);
     }

    /**
      * Creates a SymbolIDCode from a DOM element.
      *
      * @param n The dom element from which the object is created.
      */
    protected static StratmasObject domCreate(Element n)
    {
         try {
              return new SymbolIDCode(Identifier.getIdentifier(n),
                                      TypeFactory.getType(n),
                                      XMLHelper.getString(XMLHelper.getFirstChildByTag(n, "value"), "value"));
         } catch (ParseException e) {
               throw new AssertionError("Error creating SymbolIDCode from DOM element: " + e.getMessage());
         }
    }

    /**
     * Creates a SymbolIDCode from the specified Declaration.
     *
     * <p> author Per Alexius
     *
     * @param declaration The declaration for which the object is created.
     */
     protected static StratmasObject defaultCreate(Declaration declaration) {
          try {
               return new SymbolIDCode(declaration, "---------------");
          } catch (ParseException e) {
               throw new AssertionError("This is highly unlikely to occur...");
          }
     }

    /**
     * Writes this objects Taclan V2 representation to the supplied
     * StringBuffer with the specified indentation. Returns the same buffer.
     *
     * @param buf the buffer.
     * @param indent the indentation.
     */
    protected StringBuffer toTaclanV2StringBuffer(StringBuffer buf, String indent)
    {
        buf.append(indent + Identifier.toTaclanV2(getIdentifier()) + " = " + 
                   getType().toTaclanV2() + " {\n" + indent + indent + "value = " +
                   "\"" + getValue() + "\"\n" + indent + "}");
        return buf;
    }

    /**
     * Clones this object. Notice that the Identifier is NOT
     * cloned. Both the clone and the original object will thus keep a
     * reference to the same Identifier object.
     *
     * @return A clone of this object.
     */
     protected Object clone() {
          try {
               return new SymbolIDCode(identifier, type, getValue());
          } catch (ParseException e) {
               throw new AssertionError("This is highly unlikely to occur...");
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
        return new SymbolIDCodeGUIConstructor(declaration); 
    }


    /**
     * Updates this object with the data contained in the Element n.
     *
     * <p> author Per Alexius
     *
     * @param n The DOM Element from which to fetch the data.
     * @param t The simulation time for which the data is valid.
     */
    public void update(Element n, Timestamp t)
    {
        try {
            valueFromString(XMLHelper.getString(XMLHelper.getFirstChildByTag(n, "value"), "value"), n);
        } catch (ParseException e) {
            throw new AssertionError("Error updating from DOM element: " + e.getMessage());
        }
    }
}


/**
 * SymbolIDCodeVectorConstructor creates factories for creating SymbolIDCode objects.
 *
 * @version 1, $Date: 2006/05/16 09:10:48 $
 * @author  Per Alexius
 */
class SymbolIDCodeVectorConstructor extends StratmasVectorConstructor {
     /**
      * Creates a new SymbolIDCodeVectorConstructor using the supplied
      * declaration.  
      *
      * @param declaration the declaration to use.
      */
     public SymbolIDCodeVectorConstructor(Declaration declaration) {
          super(declaration);
     }

     /**
      * Returns the StratmasObject this component was created to provide.
      *
      * @param parts the parts to use in constructing the object.
      */
     public StratmasObject getStratmasObject(java.util.Vector parts) {
          SymbolIDCode ret = null;
          String symbolIDCode = ((StratmasString)parts.get(0)).valueToString();
          try {
               ret = new SymbolIDCode(this.getDeclaration(), symbolIDCode);
          } catch (ParseException e) {
          }
          return ret;
     }
}

/**
 * SymbolIDCodeGUIConstructor creates GUIs for creating
 * SymbolIDCode objects.
 *
 * @version 1, $Date: 2006/05/16 09:10:48 $
 * @author  Daniel Ahlin
*/
class SymbolIDCodeGUIConstructor extends StratmasGUIConstructor
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -2499712617976834287L;
	javax.swing.JTextField field;

    /**
     * Creates a new SymbolIDCodeGUIConstructor using the supplied declaration.
     * @param declaration the declaration to use.
     */
    public SymbolIDCodeGUIConstructor(Declaration declaration)
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
        if (field.getText().length() == 15) {
             try {
                  setStratmasObject(new SymbolIDCode(this.declaration, field.getText()));
             } catch (ParseException e) {
             }
        } else {
        }
    }
}
