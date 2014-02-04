//         $Id: TypeDefinition.java,v 1.1 2006/03/22 14:30:52 dah Exp $
/*
 * @(#)TypeDefinition.java
 */

package StratmasClient.object.type;

import StratmasClient.Debug;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import org.apache.xerces.xs.*;

/**
 * An object representing a TypeDefinition in the Taclan
 * language.
 *
 * @version 1, $Date: 2006/03/22 14:30:52 $
 * @author  Daniel Ahlin
*/

public class TypeDefinition extends Type
{
    /**
     * The XSTypedefinition behind this type;
     */
    XSTypeDefinition type = null;

    /**
     * Creates a new type.
     * @param type the XSTypeDefinition from which this type is created.
     * @param typeInformation the type information environment in
     * which this type is defined.
    */
    public TypeDefinition(XSTypeDefinition type, TypeInformation typeInformation)
    {
        super(typeInformation);
        this.type = type;
        processSubelements();
    }

    protected void processSubelements() 
    {
        if (this.type.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
            XSSimpleTypeDefinition def = (XSSimpleTypeDefinition) this.type;
        }
        else {
            XSComplexTypeDefinition def = (XSComplexTypeDefinition) this.type;
            this.isAbstract = def.getAbstract();
            switch (def.getContentType())
                {
                case XSComplexTypeDefinition.CONTENTTYPE_MIXED:
                case XSComplexTypeDefinition.CONTENTTYPE_ELEMENT:
                    XSParticle particle = def.getParticle();
                    if (particle != null) {
                        processParticle(particle);
                    }
                    break;
                case XSComplexTypeDefinition.CONTENTTYPE_EMPTY:
                    // Nothing to do.
                    break;
                case XSComplexTypeDefinition.CONTENTTYPE_SIMPLE:
                    throw new AssertionError("CONTENTTYPE_SIMPLE not implemented for ComplexType.");
                default:
                    throw new AssertionError("Nonreachable default reached.");
                }

            XSObjectList attributeUses = def.getAttributeUses();
            processAttributeUses(attributeUses);
        }
    }

    /**
     * Processes a XSObjectList expected to contain XSAttributeUses.
     *
     * @param attributeUses the expected XSAttributUses.
     */
    protected void processAttributeUses(XSObjectList attributeUses)
    {
        for (int i = 0; i < attributeUses.getLength(); i++) {
            processAttributeUse((XSAttributeUse) attributeUses.item(i));
        }
    }

    /**
     * Processes a XSObjectList expected to contain particles.
     *
     * @param particles the expected particles.
     */
    protected void processParticles(XSObjectList particles)
    {
        for (int i = 0; i < particles.getLength(); i++) {
            processParticle((XSParticle) particles.item(i));
        }
    }

    /**
     * Processes a XSParticle
     *
     * @param particle the particle to process.
     */
    protected void processParticle(XSParticle particle)
    {
        XSTerm subterm = particle.getTerm();
        switch(subterm.getType()) {
        case XSConstants.ELEMENT_DECLARATION:
            XSElementDeclaration  declaration = (XSElementDeclaration) subterm;
            TypeDefinition subtype = null;
            // This conditional handles base case for recursive
            // definitions.
            if (declaration.getTypeDefinition().getName().equals(this.type.getName()) &&
                declaration.getTypeDefinition().getNamespace().equals(this.type.getNamespace())) {
                appendSubElement(new Declaration(particle, this));
            }
            else {
                subtype = (TypeDefinition) this.typeInformation.getType(declaration.getTypeDefinition().getName(), 
                                                  declaration.getTypeDefinition().getNamespace());
                appendSubElement(new Declaration(particle, getTypeInformation()));
            }
            break;
        case XSConstants.MODEL_GROUP:
            XSModelGroup group = (XSModelGroup) subterm;
            processParticles(group.getParticles());
            break;
        case XSConstants.WILDCARD:
            StratmasClient.Debug.err.println("FIXME: WILDCARD not implemented.");
            break;
        default:
            throw new AssertionError("Nonreachable default reached.");
        }
    }

    /**
     * Returns the name of this type.
     */
    public String getName()
    {
        return this.type.getName();
    }

    /**
     * Returns true if this can be a substitute for other (i. e if
     * this is a subclass of other).
     * FIXME: should check for substitution groups as well.
     *
     * @param other the type to check against
     */
    public boolean canSubstitute(Type other)
    {
        if (other instanceof TypeDefinition) {            
            short derivationTypes =  XSConstants.DERIVATION_RESTRICTION |  
                XSConstants.DERIVATION_EXTENSION | 
                XSConstants.DERIVATION_UNION | 
                XSConstants.DERIVATION_LIST ;
            return this.type.derivedFromType(((TypeDefinition) other).type, derivationTypes);
        } 
        else {
            return false;
        }
    }

    /**
     * Returns the namespace of the type.
     */
    public String getNamespace()
    {
        return this.type.getNamespace();
    }

    /**
     * Returns the base of this type (or null if none exist).
     */ 
    public Type getBaseType()
    {
        XSTypeDefinition baseType = this.type.getBaseType();
        if (baseType != null) {
            return this.typeInformation.getType(baseType.getName(), baseType.getNamespace());
        } else {
            return null;
        }
    }

    /**
     * Returns the annotations of this type.
     */
    public String[] getAnnotations()
    {
        XSObjectList annotations = null;
        
        if (type instanceof XSComplexTypeDefinition) {
            annotations = ((XSComplexTypeDefinition) type).getAnnotations();
        } else if (type instanceof XSSimpleTypeDefinition) {
            annotations = ((XSSimpleTypeDefinition) type).getAnnotations();
        } else {
            return null;
        }

        if (annotations == null) {
            return null;
        }
        
        String[] res = new String[annotations.getLength()];

        for (int i = 0; i < annotations.getLength(); i++) {
            res[i] = ((XSAnnotation) annotations.item(i)).getAnnotationString();
        }
        
        return res;
    }

    
    /**
     * Returns the valid target-types of this type or null if none
     * allowed.
     */
    public Type validReferenceType() 
    {
        Type referenceBaseType = typeInformation.getType("Reference");

        if (this.canSubstitute(referenceBaseType)) {
            String target = null;
            
            XSObjectList attributeUses = 
                ((XSComplexTypeDefinition) this.type).getAttributeUses();                        
            for (int i = 0; i < attributeUses.getLength(); i++) {
                XSAttributeUse attributeUse = ((XSAttributeUse) attributeUses.item(i));
                if (attributeUse.getAttrDeclaration().getName().equals("target")) {
                    if (attributeUse.getConstraintType() == XSConstants.VC_FIXED) {
                        target = attributeUse.getConstraintValue();
                    } else if (this.equals(referenceBaseType)) {
                        target = "Identifiable";
                    } else {
                        throw new AssertionError("Schema inconsistancy in definition of " + 
                                                 this.getName());
                    }

                    Type targetType = typeInformation.getType(target);
                    
                    if (targetType == null) {
                        throw new AssertionError("Schema inconsistancy in definition of " + 
                                                 this.getName());
                    } else {
                        return targetType;
                    }
                }
            }

            // Check if this is the void pointer reference.
            if (this.equals(referenceBaseType)) {
                return typeInformation.getType("Identifiable");
            }    
        }
        
        return null;
    }
}

