//         $Id: IdentifierRegexFilter.java,v 1.3 2006/03/22 14:30:50 dah Exp $
/*
 * @(#)IdentifierRegexfilter.java
 */

package StratmasClient.filter;

import StratmasClient.object.StratmasObject;

/**
 * IdentifierRegexfilter filters out StratmasObjects accordning to
 * provided rules.
 *
 * @version 1, $Date: 2006/03/22 14:30:50 $
 * @author  Daniel Ahlin
*/

public class IdentifierRegexFilter extends StratmasObjectFilter
{
    /**
     * The IdentifierRegex the filter should filter for.
     */
    String regex = null;

    /**
     * Creates a new IdentifierRegexFilter allowing the objects with
     * identifiers matching the specified regex.
     *
     * @param regex the regex to match against.
     */
    public IdentifierRegexFilter(String regex)
    {        
        super();
        setRegex(regex);
    }

    /**
     * Sets the regex this filter will test against.
     *
     * @param regex the regex to match against.
     */
    public void setRegex(String regex)
    {
        this.regex = regex;
    }

    /**
     * Returns the regex this filter will test against.
     */
    public String getIdentifierRegex()
    {
        return this.regex;
    }

    /**
     * Returns true if the provided StratmasObject passes the filter.
     *
     * @param sObj the object to test
     */
    public boolean pass(StratmasObject sObj)
    {
        return applyInverted(sObj.getIdentifier().matches(getIdentifierRegex()));
    }
}
