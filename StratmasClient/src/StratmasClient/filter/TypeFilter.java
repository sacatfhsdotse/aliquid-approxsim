// $Id: TypeFilter.java,v 1.3 2006/03/22 14:30:50 dah Exp $
/*
 * @(#)Typefilter.java
 */

package StratmasClient.filter;

import StratmasClient.object.StratmasObject;

import StratmasClient.object.type.Type;

/**
 * Typefilter filters out StratmasObjects accordning to provided rules.
 * 
 * @version 1, $Date: 2006/03/22 14:30:50 $
 * @author Daniel Ahlin
 */

public class TypeFilter extends StratmasObjectFilter {
    /**
     * The type the filter should filter for.
     */
    Type type = null;

    /**
     * If type filter should allow subClasses.
     */
    boolean substitutionOk = false;

    /**
     * Creates a new TypeFilter allowing the specified type and optionaly its subtypes.
     * 
     * @param type the type to filter for.
     * @param substitutionOk if the filter also should pass subtypes of specified type.
     */
    public TypeFilter(Type type, boolean substitutionOk) {
        super();
        setType(type);
        setSubstitutionOk(substitutionOk);
    }

    /**
     * Creates a new TypeFilter allowing the specified types (allowing subtypes).
     * 
     * @param type the type to filter for.
     */
    public TypeFilter(Type type) {
        this(type, true);
    }

    /**
     * Sets the type this filter will test against.
     * 
     * @param type the type to test agains
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Returns the type this filter will test against.
     */
    public Type getType() {
        return this.type;
    }

    /**
     * Sets if filter should allow subtypes.
     * 
     * @param substitutionOk true if filter should allow subtypes.
     */
    public void setSubstitutionOk(boolean substitutionOk) {
        this.substitutionOk = substitutionOk;
    }

    /**
     * Returns if filter allows subtypes.
     */
    public boolean isSubstitutionOk() {
        return this.substitutionOk;
    }

    /**
     * Returns true if the provided StratmasObject passes the type constraint.
     * 
     * @param sObj the object to test
     */
    public boolean pass(StratmasObject sObj) {
        if (isSubstitutionOk()) {
            return applyInverted(sObj.getType().canSubstitute(getType()));
        } else {
            return applyInverted(sObj.getType().equals(getType()));
        }
    }
}
