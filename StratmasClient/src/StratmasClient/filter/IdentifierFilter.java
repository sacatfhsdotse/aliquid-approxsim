// $Id: IdentifierFilter.java,v 1.5 2006/03/22 14:30:50 dah Exp $
/*
 * @(#)Identifierfilter.java
 */

package StratmasClient.filter;

import StratmasClient.object.StratmasObject;

/**
 * Identifierfilter filters out StratmasObjects accordning to provided rules.
 * 
 * @version 1, $Date: 2006/03/22 14:30:50 $
 * @author Daniel Ahlin
 */

public class IdentifierFilter extends StratmasObjectFilter {
    /**
     * The Identifier the filter should filter for.
     */
    String identifier = null;

    /**
     * Creates a new IdentifierFilter allowing the specified identifier.
     * 
     * @param identifier the Identifier to filter for.
     */
    public IdentifierFilter(String identifier) {
        super();
        setIdentifier(identifier);
    }

    /**
     * Sets the Identifier this filter will test against.
     * 
     * @param identifier the Identifier to test agains
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Returns the identifier this filter will test against.
     */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * Returns true if the provided StratmasObject passes the filter.
     * 
     * @param sObj the object to test
     */
    public boolean pass(StratmasObject sObj) {
        return applyInverted(sObj.getIdentifier().equals(getIdentifier()));
    }
}
