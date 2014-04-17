// $Id: Declaration.java,v 1.4 2006/03/31 16:55:51 dah Exp $
/*
 * @(#)Declaration.java
 */

package StratmasClient.object.type;

import org.apache.xerces.xs.*;

/**
 * An object representing a declaration of something in the Taclan type language.
 * 
 * @version 1, $Date: 2006/03/31 16:55:51 $
 * @author Daniel Ahlin
 */

public class Declaration {
    /**
     * The type of this declaration.
     */
    Type type;

    /**
     * The name of this declaration.
     */
    String name;

    /**
     * The lower bound of the multiplicity this declaration.
     */
    int minOccurs;

    /**
     * The upper bound of the multiplicity this declaration.
     */
    int maxOccurs;

    /**
     * Wheter the multiplicity is unbounded.
     */
    boolean unbounded;

    /**
     * String representation of any annotation associated with this object.
     */
    String annotation = null;

    /**
     * Private constructor for common parts of public constructors.
     * 
     * @param minOccurs the lower bound of the multiplicity this declaration.
     * @param maxOccurs the upper bound of the multiplicity this declaration.
     * @param unbounded wheter the multiplicity is unbounded.
     */
    private Declaration(int minOccurs, int maxOccurs, boolean unbounded) {
        this.minOccurs = minOccurs;
        this.maxOccurs = maxOccurs;
        this.unbounded = unbounded;
    }

    /**
     * Creates a new declaration.
     * 
     * @param type the type of the declaration.
     * @param name the name of the declaration.
     * @param minOccurs the lower bound of the multiplicity this declaration.
     * @param maxOccurs the upper bound of the multiplicity this declaration.
     * @param unbounded wheter the multiplicity is unbounded.
     */
    public Declaration(Type type, String name, int minOccurs, int maxOccurs,
            boolean unbounded) {
        this(minOccurs, maxOccurs, unbounded);
        this.type = type;
        this.name = name;
    }

    /**
     * Creates a new declaration. Using defaults, name will be the name of the type, minOccurs = maxOccurs = 1 and unbounded = false;
     * 
     * @param type the type of the declaration.
     */
    public Declaration(Type type) {
        this(type, type.getName(), 1, 1, false);
    }

    /**
     * Creates a new declaration. Using defaults, minOccurs = maxOccurs = 1 and unbounded = false;
     * 
     * @param type the type of the declaration.
     * @param identifier the name of the identifier.
     */
    public Declaration(Type type, String identifier) {
        this(type, identifier, 1, 1, false);
    }

    /**
     * Creates a new declaration.
     * 
     * @param particle the particle used to create the declaration. Note that it is an error to pass a particle for which
     *            particle.getTerm().getType() != XSConstants.ELEMENT_DECLARATION.
     * @param typeInformation the type information to use to resolv Type objects.
     */
    public Declaration(XSParticle particle, TypeInformation typeInformation) {
        this(particle.getMinOccurs(), particle.getMaxOccurs(), particle
                .getMaxOccursUnbounded());
        XSElementDeclaration declaration = (XSElementDeclaration) particle
                .getTerm();
        this.name = declaration.getName();
        this.type = typeInformation.getType(declaration.getTypeDefinition()
                .getName(), declaration.getTypeDefinition().getNamespace());
        if (declaration.getAnnotation() != null) {
            this.annotation = declaration.getAnnotation().getAnnotationString();
        }
    }

    /**
     * Creates a new declaration. This constructor is a special case for handling directly recursive typeDefinitions.
     * 
     * @param particle the particle used to create the declaration. Note that it is an error to pass a particle for which
     *            particle.getTerm().getType() != XSConstants.ELEMENT_DECLARATION.
     * @param type the type of this declaration.
     */
    protected Declaration(XSParticle particle, Type type) {
        this(particle.getMinOccurs(), particle.getMaxOccurs(), particle
                .getMaxOccursUnbounded());
        XSElementDeclaration declaration = (XSElementDeclaration) particle
                .getTerm();
        this.name = declaration.getName();
        this.type = type;
        if (declaration.getAnnotation() != null) {
            this.annotation = declaration.getAnnotation().getAnnotationString();
        }
    }

    /**
     * Returns a string representation of this object.
     */
    public String toString() {
        String res = type.getName() + "\t\t" + getName() + " (" + minOccurs
                + "..";
        if (!this.unbounded) {
            res += maxOccurs;
        }

        return res + ")";
    }

    /**
     * Returns the type of this declaration.
     */
    public Type getType() {
        return this.type;
    }

    /**
     * Returns the minimum number of allowed occurances of this declaration.
     */
    public int getMinOccurs() {
        return this.minOccurs;
    }

    /**
     * Sets the minimum number of allowed occurances of this declaration.
     * 
     * @param min the new minOccurs
     */
    public void setMinOccurs(int min) {
        this.minOccurs = min;
    }

    /**
     * Returns the maximum number of allowed occurances of this declaration.
     */
    public int getMaxOccurs() {
        return this.maxOccurs;
    }

    /**
     * Sets the maximum number of allowed occurances of this declaration.
     * 
     * @param max the new maxOccurs
     */
    public void setMaxOccurs(int max) {
        this.maxOccurs = max;
    }

    /**
     * Returns true if the multiplicity of this declaration is unbounded.
     */
    public boolean isUnbounded() {
        return this.unbounded;
    }

    /**
     * Sets if the multiplicity of this declaration is unbounded.
     * 
     * @param unbounded the value of unbounded.
     */
    public void setUnbounded(boolean unbounded) {
        this.unbounded = unbounded;
    }

    /**
     * Convinience method that checks if the multiplicity of this declaration is precisly 1.
     */
    public boolean isSingular() {
        return (this.getMinOccurs() == 1) && (!this.isUnbounded())
                && (getMaxOccurs() == 1);
    }

    /**
     * Convinience method that checks if the multiplicity of this declaration is 0..1.
     */
    public boolean isOptional() {
        return (getMinOccurs() == 0) && (!isUnbounded())
                && (getMaxOccurs() == 1);
    }

    /**
     * Convinience method that checks if the multiplicity of this declaration is 0..1.
     * 
     * @return True if this Declaration refers to a list, false otherwise.
     */
    public boolean isList() {
        return (isUnbounded() || getMinOccurs() > 1 || getMaxOccurs() > 1);
    }

    /**
     * Returns the name of this declaration.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the annotation of this declaration or null if none present
     */
    public String getAnnotation() {
        return annotation;
    }

    /**
     * Returns a copy of this declaration with the specified type as actual type. Behaviour is unspecified if the provided type is not a
     * valid substitution for the type of this declaration.
     * 
     * @param subType the type of the clone.
     */
    public Declaration clone(Type subType) {
        if (subType.canSubstitute(getType())) {
            return new Declaration(subType, getName(), getMinOccurs(),
                    getMaxOccurs(), isUnbounded());
        } else {
            throw new AssertionError("Non substitionable type provided: "
                    + subType.getName() + " may not serve as substitute for "
                    + getType().getName());
        }
    }

    /**
     * Returns a copy of this declaration
     */
    public Object clone() {
        return new Declaration(getType(), getName(), getMinOccurs(),
                getMaxOccurs(), isUnbounded());
    }
}
