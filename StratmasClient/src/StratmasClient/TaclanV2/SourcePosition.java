// $Id: SourcePosition.java,v 1.1 2005/02/03 10:20:59 dah Exp $

/*
 * @(#)SourcePosition.java
 */

package StratmasClient.TaclanV2;

/**
 * An object representing the location of a lexical symbol in a parsed source. Mainly used to construct meaningful error-messages.
 * 
 * @version 1, 09/28/04
 * @author Daniel Ahlin
 */

public class SourcePosition {
    /**
     * The source the position was declared in (typically a filename).
     */
    String source;

    /**
     * The symbol starting this Sourceposition
     */
    StratmasClient.TaclanV2.java_cup.runtime.Symbol start;

    /**
     * The symbol ending this Sourceposition
     */
    StratmasClient.TaclanV2.java_cup.runtime.Symbol end;

    /**
     * Creates a new sourceposition using a start- and end-symbol
     * 
     * @param source the source where the token was declared (typically a filename)
     * @param startToken the symbol starting this lexical element.
     * @param endToken the symbol starting this lexical element.
     */
    public SourcePosition(String source,
            StratmasClient.TaclanV2.java_cup.runtime.Symbol startToken,
            StratmasClient.TaclanV2.java_cup.runtime.Symbol endToken) {
        this.source = source;
        this.start = startToken;
        this.end = endToken;
    }

    /**
     * Creates a new sourceposition using just one symbol
     * 
     * @param source the source where the token was declared (typically a filename)
     * @param startToken the symbol starting this lexical element.
     */
    public SourcePosition(String source,
            StratmasClient.TaclanV2.java_cup.runtime.Symbol startToken) {
        this(source, startToken, startToken);
    }

    /**
     * Creates a new sourceposition using no symbol, just recording where the declaration were made.
     * 
     * @param source the source where the token was declared (typically a filename)
     */
    public SourcePosition(String source) {
        this(source, null);
    }

    public String toString() {
        if (start != null) {
            return this.getSource() + ": " + start.getRow();
        } else {
            return this.getSource() + ":? ";
        }

    }

    /**
     * Returns a SourcePosition representing an unknown source.
     */
    public static SourcePosition getUnknown() {
        return new SourcePosition("unknown");
    }

    /**
     * Returns a string representation of the source.
     */
    public String getSource() {
        return this.source;
    }
}
