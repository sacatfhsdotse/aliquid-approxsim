// $Id: DocumentDefinition.java,v 1.1 2006/03/22 14:30:52 dah Exp $
/*
 * @(#)DocumentDefinition.java
 */

package ApproxsimClient.object.type;

import org.apache.xerces.xs.*;

/**
 * An object representing a DocumentDefinition in the Taclan language.
 * 
 * @version 1, $Date: 2006/03/22 14:30:52 $
 * @author Daniel Ahlin
 */

public class DocumentDefinition extends Type {
    /**
     * The (fake) name of the DocumentDefinition.
     */
    String name = "Document";

    /**
     * The model behind this DocumentDefinition
     */
    XSModel model;

    /**
     * Creates a new DocumentDefinition.
     * 
     * @param model the Model this DocumentDefinition mirrors.
     * @param typeInformation the type environment of this model.
     */
    public DocumentDefinition(XSModel model, TypeInformation typeInformation) {
        super(typeInformation);
        this.model = model;

        processElements();
        processAttributes();
    }

    /**
     * Processes the elements.
     */
    protected void processElements() {
        XSNamedMap map = this.model
                .getComponents(XSConstants.ELEMENT_DECLARATION);
        for (int i = 0; i < map.getLength(); i++) {
            processElementDeclaration((XSElementDeclaration) map.item(i));
        }
    }

    /**
     * Processes the attributes.
     */
    protected void processAttributes() {
        XSNamedMap map = this.model
                .getComponents(XSConstants.ATTRIBUTE_DECLARATION);
        for (int i = 0; i < map.getLength(); i++) {
            processAttributeUse((XSAttributeUse) map.item(i));
        }
    }

    /**
     * Processes an ElementDeclaration
     * 
     * @param declaration the XSElementDeclaration to process.
     */
    protected void processElementDeclaration(XSElementDeclaration declaration) {
        TypeDefinition subtype = (TypeDefinition) this.typeInformation
                .getType(declaration.getTypeDefinition().getName(), declaration
                        .getTypeDefinition().getNamespace());

        appendSubElement(new Declaration(subtype, declaration.getName(), 1, 1,
                false));
    }

    /**
     * Returns the name of this type.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns true if this can be a substitute for other (i. e if this is a subclass of other). FIXME: should check for substitution groups
     * as well.
     * 
     * @param other the type to check against
     */
    public boolean canSubstitute(Type other) {
        return this == other;
    }

    /**
     * Returns the empty string.
     */
    public String getNamespace() {
        return "";
    }

    /**
     * Returns the base of this type (or null if none exist).
     */
    public Type getBaseType() {
        return null;
    }
}
