// $Id: ParsedIdentifier.java,v 1.6 2006/01/25 16:34:10 dah Exp $
/*
 * @(#)ParsedIdentifier.java
 */

package StratmasClient.TaclanV2;

/**
 * An object representing an identifier of the Taclan language. As such it contains artefacts of the language, it also contains the non
 * lexical type checks.
 * 
 * @version 1, 09/28/04
 * @author Daniel Ahlin
 */

public class ParsedIdentifier extends ParsedObject {
    /**
     * Indicates if this is actually an anonymous object.
     */
    boolean anonymous = false;

    /**
     * The name of the identifier.
     */
    String name;

    /**
     * The scope this id was declared in.
     */

    ParsedIdentifier scope = null;

    /**
     * @param pos where the identifier is used.
     * @param name the name of this element.
     */
    public ParsedIdentifier(SourcePosition pos, String name) {
        super(pos);
        this.name = name;
    }

    /**
     * Constructor for anonymous identifiers.
     * 
     * @param pos where the identifier is used.
     */
    public ParsedIdentifier(SourcePosition pos) {
        this(pos, "-- anonymous --");
        anonymous = true;
    }

    public static ParsedIdentifier getAnonymous() {
        return new ParsedIdentifier(SourcePosition.getUnknown());
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public static ParsedIdentifier createAnonymous(SourcePosition pos) {
        return new ParsedIdentifier(pos);
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }

    public void setScope(ParsedIdentifier scope) {
        this.scope = scope;
    }

    public ParsedIdentifier getScope() {
        return scope;
    }

    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Returns the Identifier represented by this Object.
     */
    public String getIdentifier() {
        String literal = toString();
        // Expand \n and \'
        literal = literal.replaceAll("\\\\n", "\n");
        literal = literal.replaceAll("\\\\'", "'");

        return literal;
    }

    public boolean equals(Object o) {
        if (o instanceof ParsedIdentifier) {
            ParsedIdentifier e = (ParsedIdentifier) o;
            return this.getName().equals(e.getName());
        } else if (o instanceof String) {
            return this.getName().equals((String) o);
        }

        return false;
    }
}
