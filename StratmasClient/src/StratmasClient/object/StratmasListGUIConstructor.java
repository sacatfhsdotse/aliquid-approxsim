// $Id: StratmasListGUIConstructor.java,v 1.1 2006/03/22 14:30:51 dah Exp $
/*
 * @(#)StratmasObject.java
 */

package StratmasClient.object;

import StratmasClient.treeview.TreeView;

import StratmasClient.object.type.Declaration;

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
 * StratmasListGUIConstructor creates GUIs for creating StratmasList objects.
 * 
 * @version 1, $Date: 2006/03/22 14:30:51 $
 * @author Daniel Ahlin
 */
public class StratmasListGUIConstructor extends StratmasGUIConstructor {
    /**
	 * 
	 */
    private static final long serialVersionUID = 4556531159302973339L;
    /**
     * The StratmasList being built with this constructor;
     */
    StratmasList stratmasList;

    /**
     * Creates a new StratmasListGUIConstructor using specifications in the supplied declaration.
     * 
     * @param declaration the declaration to use.
     */
    public StratmasListGUIConstructor(Declaration declaration) {
        super(declaration, false);
    }

    /**
     * Builds the panel presented to the user.
     */
    public void buildPanel() {
        this.add(new JLabel(getDeclaration().getName()));

        final StratmasListGUIConstructor self = this;

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
        if (stratmasList == null) {
            // Prepare a forgiving declaration. The multiplicity
            // checks are made in createStratmasObject.
            final Declaration decl = (Declaration) this.getDeclaration()
                    .clone();
            decl.setMinOccurs(0);
            decl.setUnbounded(true);
            // Create a specialised version of StratmasList:
            stratmasList = new StratmasList(decl, new Vector()) {
                public Declaration getDeclaration() {
                    return decl;
                }
            };
        }

        final JDialog dialog = new JDialog((java.awt.Frame) null, this
                .getDeclaration().getName(), true);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JScrollPane(TreeView.getDefaultTreeView(stratmasList)));
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

    protected void createStratmasObject() {
        if (stratmasList == null) {
            setStratmasObject(new StratmasList(this.getDeclaration(),
                    new Vector()));
        } else if (stratmasList.getChildCount() >= this.getDeclaration()
                .getMinOccurs()
                && (stratmasList.getChildCount() <= this.getDeclaration()
                        .getMaxOccurs() || this.getDeclaration().isUnbounded())) {
            setStratmasObject(new StratmasList(this.getDeclaration(),
                    stratmasList.parts));
        } else {
            this.result = null;
        }
    }
}
