// $Id: ApproxsimObjectDynImpl.java,v 1.4 2006/05/05 17:56:10 dah Exp $
/*
 * @(#)ApproxsimObject.java
 */

package ApproxsimClient.object;

import ApproxsimClient.object.type.Type;
import ApproxsimClient.object.type.Declaration;
import ApproxsimClient.Icon;

/**
 * ApproxsimObjectDynImpl is a convinience implementation of ApproxsimObject providing dynamic almost anything.
 * 
 * @version 1, $Date: 2006/05/05 17:56:10 $
 * @author Daniel Ahlin
 */
abstract class ApproxsimObjectDynImpl extends ApproxsimObjectImpl {
    /**
     * The icon used to visualize this object.
     */
    Icon icon;

    /**
     * The type of this object.
     */
    Type type;

    /**
     * Creates a new ApproxsimObject.
     * 
     * @param identifier the identifier for the object.
     * @param type the type of the object.
     */
    ApproxsimObjectDynImpl(String identifier, Type type) {
        super(identifier);
        this.type = type;
    }

    /**
     * Creates a new ApproxsimObject from a Declaration.
     * 
     * @param declaration the declaration for this object.
     */
    ApproxsimObjectDynImpl(Declaration declaration) {
        this(declaration.getName(), declaration.getType());
    }

    /**
     * Creates a new ApproxsimObject from a Declaration and changes the Identifier to the specified Identifier.
     * <p>
     * author Per Alexius
     * 
     * @param declaration The Declaration for this object.
     * @param identifier The Identifier to use as Identifier for this object.
     */
    ApproxsimObjectDynImpl(Declaration declaration, String identifier) {
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
     * Returns the icon used to symbolize this object.
     */
    public Icon getIcon() {
        if (this.icon == null) {
            createIcon();
        }
        return this.icon;
    }

    /**
     * Creates an icon for use in this object.
     */
    public void createIcon() {
        this.icon = Icon.getIcon(this);
    }

    /**
     * Sets the parent of this object. Overridden to allow icon change on parent change.
     * 
     * @param parent the new parent of this object.
     */
    protected void setParent(ApproxsimObject parent) {
        super.setParent(parent);
        if (this.icon != null) {
            createIcon();
        }
    }
}
