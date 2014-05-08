// $Id: ApproxsimListGUIConstructor.java,v 1.1 2006/03/22 14:30:51 dah Exp $
/*
 * @(#)ApproxsimObject.java
 */

package ApproxsimClient.object;

import ApproxsimClient.treeview.TreeView;

import ApproxsimClient.object.type.Declaration;

import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import java.awt.event.ActionEvent;

/**
 * ApproxsimListGUIConstructor creates GUIs for creating ApproxsimList objects.
 * 
 * @version 1, $Date: 2006/03/22 14:30:51 $
 * @author Daniel Ahlin
 */
public class ApproxsimListGUIConstructor extends ApproxsimGUIConstructor {
    /**
	 * 
	 */
    private static final long serialVersionUID = 4556531159302973339L;
    /**
     * The ApproxsimList being built with this constructor;
     */
    ApproxsimList approxsimList;

    /**
     * Creates a new ApproxsimListGUIConstructor using specifications in the supplied declaration.
     * 
     * @param declaration the declaration to use.
     */
    public ApproxsimListGUIConstructor(Declaration declaration) {
        super(declaration, false);
    }

    /**
     * Builds the panel presented to the user.
     */
    public void buildPanel() {
        this.add(new JLabel(getDeclaration().getName()));

        final ApproxsimListGUIConstructor self = this;

        add(new JButton(new AbstractAction("Edit") {
            /**
				 * 
				 */
            private static final long serialVersionUID = -2756869603941494642L;

            /**
             * Invoked when an action occurs.
             * 
             * @param e the event
             */
            public void actionPerformed(ActionEvent e) {
                self.showEditDialog();
            }
        }));
    }

    /**
     * Builds a dialog associated with this constructor.
     */
    public void showEditDialog() {
        if (approxsimList == null) {
            // Prepare a forgiving declaration. The multiplicity
            // checks are made in createApproxsimObject.
            final Declaration decl = (Declaration) this.getDeclaration()
                    .clone();
            decl.setMinOccurs(0);
            decl.setUnbounded(true);
            // Create a specialised version of ApproxsimList:
            approxsimList = new ApproxsimList(decl, new Vector()) {
                public Declaration getDeclaration() {
                    return decl;
                }
            };
        }

        final JDialog dialog = new JDialog((java.awt.Frame) null, this
                .getDeclaration().getName(), true);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JScrollPane(TreeView.getDefaultTreeView(approxsimList)));
        panel.add(new JButton(new AbstractAction("Done") {
            /**
				 * 
				 */
            private static final long serialVersionUID = 6307745430212686755L;

            /**
             * Invoked when an action occurs.
             * 
             * @param e the event
             */
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        }));

        dialog.getRootPane().getContentPane().add(panel);
        dialog.pack();
        dialog.setVisible(true);
    }

    protected void createApproxsimObject() {
        if (approxsimList == null) {
            setApproxsimObject(new ApproxsimList(this.getDeclaration(),
                    new Vector()));
        } else if (approxsimList.getChildCount() >= this.getDeclaration()
                .getMinOccurs()
                && (approxsimList.getChildCount() <= this.getDeclaration()
                        .getMaxOccurs() || this.getDeclaration().isUnbounded())) {
            setApproxsimObject(new ApproxsimList(this.getDeclaration(),
                    approxsimList.parts));
        } else {
            this.result = null;
        }
    }
}
