// $Id: ParsedObject.java,v 1.1 2005/02/03 10:20:58 dah Exp $
/*
 * @(#)ParsedObject.java
 */

package ApproxsimClient.TaclanV2;

/**
 * An object representing any parsed construct of the Taclan V2 language. As a parsed construct it contains artefacts of the language, e. g.
 * references to the source files
 * 
 * @version 1, 09/28/04
 * @author Daniel Ahlin
 */

public abstract class ParsedObject {
    /**
     * The position where language construct occured
     */
    protected SourcePosition pos;

    /**
     * @param pos where the parsed object is declared.
     */
    public ParsedObject(SourcePosition pos) {
        this.pos = pos;
    }

    /**
     * Returns the position where this ParsedObject where parsed
     */
    public SourcePosition getPos() {
        return pos;
    }
}
