// $Id: TypeSelector.java,v 1.2 2006/03/22 14:30:42 dah Exp $
/*
 * @(#)TypeSelector.java
 */

package StratmasClient;

import StratmasClient.object.type.Type;
import javax.swing.JComboBox;
import java.util.Vector;
import java.util.Enumeration;

/**
 * A JComboBox used to select a specific type.
 * 
 * @version 1, $Date: 2006/03/22 14:30:42 $
 * @author Daniel Ahlin
 */
public class TypeSelector extends JComboBox {
    /**
	 * 
	 */
    private static final long serialVersionUID = -3268083996350073331L;
    /**
     * The types this selector can choose from.
     */
    Vector types = new Vector();

    /**
     * Creates a new TypeSelector presenting choices which are valid substitutions of the specified type.
     * 
     * @param type the specified type.
     */
    public TypeSelector(Type type) {
        super();
        expandDerived(type);
    }

    /**
     * Expands the derived types of the specified type.
     * 
     * @param type the type to process.
     */
    protected void expandDerived(Type type) {
        addType(type);
        for (Enumeration ts = type.getExpandedDerived().elements(); ts
                .hasMoreElements();) {
            Type t = (Type) ts.nextElement();
            addType(t);
        }
    }

    /**
     * Adds the specified type as an option.
     * 
     * @param type the type to add.
     */
    protected void addType(Type type) {
        if (!type.isAbstract()) {
            this.types.add(type);
            this.addItem(type.getName());
        }
    }

    /**
     * Returns the type matching the selected item.
     */
    public Type getSelectedType() {
        return (Type) this.types.get(this.getSelectedIndex());
    }
}
