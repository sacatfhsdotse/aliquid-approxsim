// $Id: StratmasAbstractAction.java,v 1.4 2006/07/31 10:17:49 alexius Exp $
/*
 * @(#) StratmasAbstractAction
 */

package StratmasClient;

import javax.swing.AbstractAction;

/**
 * StratmasAbstractAction is subclass of AbstractAction that allows defining if the action is a mutator.
 * 
 * @version 1, $Date: 2006/07/31 10:17:49 $
 * @author Daniel Ahlin
 */
public abstract class StratmasAbstractAction extends AbstractAction {
    /**
	 * 
	 */
    private static final long serialVersionUID = 3039626999751292803L;
    /**
     * Indicates that this action changes the object.
     */
    boolean isMutator;

    /**
     * Creates a new StratmasAbstractAction with the specified name
     * 
     * @param str the name of the action.
     * @param isMutator indicating if this action changes the object or not.
     */
    public StratmasAbstractAction(String str, boolean isMutator) {
        super(str);
        this.isMutator = isMutator;
    }

    /**
     * Returns true if this action is mutator
     */
    public boolean isMutator() {
        return this.isMutator;
    }
}
