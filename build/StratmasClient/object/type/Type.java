// 	$Id: Type.java,v 1.1 2006/03/22 14:30:52 dah Exp $
/*
 * @(#)Type.java
 */

package StratmasClient.object.type;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import org.apache.xerces.xs.*;

/**
 * An object representing a Type in the Taclan
 * language.
 *
 * @version 1, $Date: 2006/03/22 14:30:52 $
 * @author  Daniel Ahlin
*/

public abstract class Type
{
    /**
     * The TypeInformation this type was fetched from (used for
     * additional lookups).
     */
    TypeInformation typeInformation = null;

    /**
     * The types directly derived from this type.
     */
    Hashtable derivedTypes = null;

    /**
     * Whether this type is abstract.
     */
    boolean isAbstract = true;;

    /**
     * The subelements of this type (in correct order.)
     */
    Vector subElements = new Vector();

    /**
     * A hash representation of the subelements.
     */
    Hashtable subElementHash = new Hashtable();

    /**
     * The attributes of this type (in correct order.)
     */
    Vector attributes = new Vector();

    /**
     * A hash representation of the attributes.
     */
    Hashtable attributesHash = new Hashtable();

    /**
     * Creates a new type.
     *
     * @param typeInformation the type information environment in
     * which this type is defined.
    */
    public Type(TypeInformation typeInformation)
    {
	this.typeInformation = typeInformation;
    }

    /**
     * Returns the name of this type.
     */
    public abstract String getName();

    /**
     * Appends a subelement to this type.
     *
     * @param declaration the declaration of the subelement
     */    
    protected void appendSubElement(Declaration declaration)
    {
	subElements.add(declaration);
	subElementHash.put(declaration.getName(), declaration);
    }

    /**
     * Appends an attribute to this type.
     *
     * @param attribute the attribute to add.
     */ 
    protected void appendAttribute(TypeAttribute attribute)
    {
	attributes.add(attribute);
	attributesHash.put(attribute.getName(), attribute);
    }

    /**
     * Processes a XSAttributeUse
     *
     * @param attributeUse the XSAttributeUse to process.
     */
    protected void processAttributeUse(XSAttributeUse attributeUse)
    {
	XSAttributeDeclaration declaration = attributeUse.getAttrDeclaration();
	Type subtype = this.typeInformation.getType(declaration.getTypeDefinition().getName(), 
						    declaration.getTypeDefinition().getNamespace());

	appendAttribute(new TypeAttribute(subtype, declaration.getName()));
    }

    /**
     * Returns a string representation of this class.
     */
    public String toString()
    {
	StringBuffer buf = new StringBuffer();
	for (Enumeration ss = subElements.elements(); ss.hasMoreElements(); ) {
	    buf.append("\n");
	    Declaration subElement = (Declaration) ss.nextElement();
	    buf.append(subElement.toString());	    
	}
	for (Enumeration ss = attributes.elements(); ss.hasMoreElements(); ) {
	    buf.append("\n");
	    TypeAttribute subElement = (TypeAttribute) ss.nextElement();
	    buf.append(subElement.toString());	    
	}
	String subelementstr = buf.toString().replaceAll("\n", "\n  ");
	
	return 	getName() + " {" + subelementstr + "\n}";
    }

    /**
     * Returns false if this type is instansiable.
     */
    public boolean isAbstract() 
    {
	return isAbstract;
    }

    /**
     * Returns the subelements of this type.
     */
    public Vector getSubElements()
    {
	return subElements;
    }

    /**
     * Returns the subelement with the specified tag.
     *
     * @param tag the tag to search for.
     */
    public Declaration getSubElement(String tag)
    {
	return (Declaration) subElementHash.get(tag);
    }
    
    /**
     * Returns true if this can be a substitute for other (i. e if
     * this is a subclass of other).
     * FIXME: should check for substitution groups as well.
     *
     * @param other the type to check against
     */
    public abstract boolean canSubstitute(Type other);

    /**
     * Returns true if this can be a substitute for other (i. e if
     * this is a subclass of other).
     * FIXME: should check for substitution groups as well.
     *
     * @param other the type to check against
     * @param namespace the namespace of other
     */
    public boolean canSubstitute(String other, String namespace)
    {
	return canSubstitute(typeInformation.getType(other, namespace));
    }

    /**
     * Returns true if this can be a substitute for other (i. e if
     * this is a subclass of other).
     * FIXME: should check for substitution groups as well.
     *
     * @param other the type to check against
     */
    public boolean canSubstitute(String other)
    {
	return canSubstitute(other, this.getNamespace());
    }

    /**
     * Returns the namespace of the type.
     */
    public abstract String getNamespace();

    /**
     * Adds a type as a derived type.
     *
     * @param derived the type to add.
     */
    protected void addDerived(Type derived)
    {
	derivedTypes.put(derived, derived);
    }
    
    /**
     * Returns the base of this type (or null if none exist).
     */ 
    public abstract Type getBaseType();

    /**
     * Returns all direct derivations of this type.
     */
    public Enumeration getDerived()
    {
	if (this.derivedTypes == null) {
	    this.derivedTypes = new Hashtable();
	    this.typeInformation.findDerived(this);
	}

	return this.derivedTypes.elements();
    }
    
    /**
     * Maps the type and its descendents types to the type of the base.
     *
     * @param base the base which to reduce to.
     * @param map the map into which the mapping is to be put.
     */
    protected void reduceBranch(Type base, Hashtable map)
    {
	/* Map this type */
	map.put(this, base);

	/* Map children */
	for (Enumeration ds = this.getDerived(); ds.hasMoreElements();) {
	    Type derived = (Type) ds.nextElement();
	    derived.reduceBranch(base, map);
	}
    }

    /**
     * Returns the type derived from this, corresponding to the name
     * and namespace of the supplied type (or null if no such can be found).
     *
     * @param type the type to try to match
     */
    protected Type getDerived(Type type)
    {
	return (Type) this.derivedTypes.get(type);
    }

    /**
     * Creates a mapping between this type and another type.
     *
     * The mapping is on the form Type -> Type.
     *
     * @param otherType the other type.
     * @param map the map into which the mapping is to be put.
     */
    public void createMapping (Type otherType, Hashtable map)
    {
	/* Map this type */
	map.put(this, otherType);
	
	for (Enumeration ds = this.getDerived(); ds.hasMoreElements();) {
	    Type derived = (Type) ds.nextElement();
	    Type  otherDerived = (Type) otherType.getDerived(derived);
	    if (otherDerived != null) {
		/* Descend this branch.*/
		derived.createMapping(otherDerived, map);
	    }
	    else {
		/* No otherDerived found, reduce all derivation in
		 * this branch to otherTypes type. */
		derived.reduceBranch(otherType, map);
	    }
	}
    }

    /**
     * Returns the hashcode of this type.
     */
    public int hashCode()
    {
	return (this.getNamespace() + ":" + this.getName()).hashCode();
    }

    /**
     * Returns true if this type equals the specified object.
     *
     * @param o the object to compare against.
     */
    public boolean equals(Object o)
    {
	if (o instanceof Type) {
	    return ((Type) o).getName().equals(this.getName()) &&
		((Type) o).getNamespace().equals(this.getNamespace());
	}

	return false;
	
    }

    /**
     * Returns the typeinformation object this type is created in.
     */
    public TypeInformation getTypeInformation()
    {
	return this.typeInformation;
    }

    /**
     * Returns a String holding this object in its Taclan V2
     * representation.
     */
    public String toTaclanV2()
    {
	if (getName().matches("[A-Za-z_][A-Za-z_0-9]*")) {
	    return getName();
	} else {
	    return "'" + getName() + "'";
	}
    }

    /**
     * Returns the annotations of this type.
     */
    public String[] getAnnotations()
    {
	return new String[0];
    }

    /**
     * Returns a Vector containing all derived types of this type.
     */    
    public Vector getExpandedDerived()
    {
	return getExpandedDerived(new Vector());
    }

    /**
     * Expands the derived types of the specified type and puts it in
     * res.
     * 
     * @param res the type to process.
     */    
    protected Vector getExpandedDerived(Vector res)
    {
	for (Enumeration ts = getDerived(); ts.hasMoreElements();) {
	    Type type = (Type) ts.nextElement();
	    res.add(type);
	    type.getExpandedDerived(res);
	}

	return res;
    }

    /**
     * Returns the valid target-types of this type or null if none
     * allowed.
     */
    public Type validReferenceType() 
    {
	return null;
    }

    /** 
     * Returns true if the provided type can be referenced by this.
     *
     * @param t the type to check
     */
    public boolean isValidReferenceType(Type t)
    {
	Type t2 = this.validReferenceType();
	
	if (t2 != null && t != null && t.canSubstitute(t2)) {
	    return true;
	} else {
	    return false;
	}
    }
}

