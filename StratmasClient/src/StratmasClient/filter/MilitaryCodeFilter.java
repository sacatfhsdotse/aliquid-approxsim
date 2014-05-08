package ApproxsimClient.filter;

import ApproxsimClient.object.ApproxsimObject;
import ApproxsimClient.object.SymbolIDCode;

/**
 * Passes all objects of type "MilitaryUnit" which have the right code letter on the specified position in the code string.
 */

public class MilitaryCodeFilter extends ApproxsimObjectFilter {
    // code letter
    private char letter;
    // position of the letter in the code
    private int position;

    /**
     * Creates a new MilitaryCodeFilter
     */
    public MilitaryCodeFilter(char letter, int position) {
        super();
        this.letter = letter;
        this.position = position;
    }

    /**
     * Returns true if the provided ApproxsimObject passes the filter.
     * 
     * @param sObj the object to test
     */
    public boolean pass(ApproxsimObject sObj) {
        if (sObj.getType().getName().equals("MilitaryUnit")) {
            try {
                SymbolIDCode code = (SymbolIDCode) sObj
                        .getChild("symbolIDCode");
                if (code.valueToString().charAt(position) == letter) {
                    return true;
                }
            } catch (RuntimeException e) {}
        }
        return false;
    }
}
