// $Id: ApproxsimGUIConstructorDialog.java,v 1.1 2006/03/22 14:30:51 dah Exp $
/*
 * @(#)ApproxsimGUIConstructor.java
 */

package ApproxsimClient.object;

import javax.swing.JDialog;

/**
 * A JDialog used to create a ApproxsimObject.
 * 
 * @version 1, $Date: 2006/03/22 14:30:51 $
 * @author Daniel Ahlin
 */
public class ApproxsimGUIConstructorDialog extends JDialog {
    /**
	 * 
	 */
    private static final long serialVersionUID = 3832051310348235016L;

    /**
     * The constructor this dialog wraps.
     */
    ApproxsimGUIConstructor constructor;

    /**
     * Indicates that this dialog was cancelled.
     */
    boolean cancelled = false;

    /**
     * Creates a new Dialog for the specified constructor
     * 
     * @param constructor constructor to wrap.
     */
    ApproxsimGUIConstructorDialog(ApproxsimGUIConstructor constructor) {
        super((java.awt.Frame) null, "Create "
                + constructor.getDeclaration().getType().getName(), true);
        this.constructor = constructor;
    }

    /**
     * Returns the ApproxsimObject this dialog is expected to create, or null if none has been created.
     */
    public ApproxsimObject getApproxsimObject() {
        if (!cancelled) {
            return constructor.getApproxsimObject();
        } else {
            return null;
        }
    }

    /**
     * Call to cancel dialog.
     */
    public void cancel() {
        cancelled = true;
        dispose();
    }
}
