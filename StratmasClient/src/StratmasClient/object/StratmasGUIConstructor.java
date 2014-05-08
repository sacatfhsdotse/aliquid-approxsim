// $Id: ApproxsimGUIConstructor.java,v 1.1 2006/03/22 14:30:51 dah Exp $
/*
 * @(#)ApproxsimGUIConstructor.java
 */

package ApproxsimClient.object;

import ApproxsimClient.object.type.Declaration;
import ApproxsimClient.object.type.Type;
import ApproxsimClient.TypeSelector;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.BoxLayout;

/**
 * ApproxsimGUIConstructor is a abstract superclass for JComponents acting as source of new ApproxsimObjects.
 * 
 * @version 1, $Date: 2006/03/22 14:30:51 $
 * @author Daniel Ahlin
 */
public abstract class ApproxsimGUIConstructor extends JPanel {
    /**
	 * 
	 */
    private static final long serialVersionUID = -856452493384542570L;

    /**
     * The declaration for which the result is constructed.
     */
    Declaration declaration;

    /**
     * The type of the constructed result (this may be a derivation of the type specified in the declaration).
     */
    Type type;

    /**
     * The identifier that the constructor uses to create the object.
     */
    String identifier;

    /**
     * The resulting ApproxsimObject
     */
    ApproxsimObject result = null;

    /**
     * If this gui constructor uses a chooser.
     */
    boolean useChooser = false;

    /**
     * Creates a new ApproxsimComplexGUIConstructor using specifications in the supplied declaration.
     * 
     * @param declaration the declaration to use.
     * @param useChooser lets the user choose different the subtypes of the provided declaration.
     */
    public ApproxsimGUIConstructor(Declaration declaration, boolean useChooser) {
        this.declaration = declaration;
        setIdentifier(declaration.getName());
        setType(getDeclaration().getType());
        this.useChooser = useChooser;
        if (this.useChooser) {
            buildChooser();
        } else {
            buildPanel();
        }
    }

    /**
     * Creates a new object using specifications in declaration.
     * 
     * @param declaration the declaration to use.
     */
    public ApproxsimGUIConstructor(Declaration declaration) {
        this(declaration, declaration.getType().isAbstract());
    }

    /**
     * Builds the panel presented to the user.
     */
    public void buildChooser() {
        this.add(new JLabel(declaration.getName()));
        final ApproxsimGUIConstructor self = this;
        final TypeSelector typeSelector = new TypeSelector(
                this.declaration.getType());
        // Hack to get selected item implicitly selected item.
        typeSelector.setSelectedIndex(0);
        this.add(typeSelector);
        final JButton createButton = new JButton();
        createButton.setAction(new AbstractAction("Create...") {
            /**
			 * 
			 */
            private static final long serialVersionUID = 4651651108071129477L;

            /**
             * Invoked when an action occurs.
             * 
             * @param e the event
             */
            public void actionPerformed(ActionEvent e) {
                ApproxsimGUIConstructorDialog deferedDialog = ApproxsimGUIConstructor
                        .buildDialog(ApproxsimObjectFactory
                                .guiCreate(declaration.clone(typeSelector
                                        .getSelectedType())));
                deferedDialog.setVisible(true);
                self.setApproxsimObject(deferedDialog.getApproxsimObject());
                if (self.getApproxsimObject() != null) {
                    createButton.setText("Redo...");
                }
            }
        });
        add(createButton);
    }

    /**
     * Builds the panels used by this constructor
     */
    protected abstract void buildPanel();

    /**
     * Tries to create the ApproxsimObject from the values in the GUI.
     */
    protected abstract void createApproxsimObject();

    /**
     * Sets the type for this constructor to create.
     * 
     * @param type the type to set as target.
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * The type for which this constructor is currently set.
     */
    public Type getType() {
        return this.type;
    }

    /**
     * Sets the identifier that the constructor uses to create the object.
     * 
     * @param identifier the identifier to set.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Returns the identifier that the constructor uses to creat the object.
     */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * Returns the ApproxsimObject this component was created to provide.
     */
    public ApproxsimObject getApproxsimObject() {
        if (this.result == null) {
            // If this constructor is handled by the chooser the defered
            // constructor will take care of the creation of the ApproxsimObject.
            if (!useChooser) {
                createApproxsimObject();
            }
        }

        if (this.result != null) {
            this.result.setIdentifier(getIdentifier());
        }

        return this.result;
    }

    /**
     * Sets the ApproxsimObject this component will provide.
     */
    protected void setApproxsimObject(ApproxsimObject object) {
        this.result = object;
    }

    /**
     * The declaration for which the result is constructed.
     */
    protected Declaration getDeclaration() {
        return this.declaration;
    }

    /**
     * Builds a dialog associated with this constructor.
     * 
     * @param constructor the constructor to build for.
     * @param fixedIdentifier wether the identifier of the object created is fixed.
     */
    public static final ApproxsimGUIConstructorDialog buildDialog(
            ApproxsimGUIConstructor constructor, boolean fixedIdentifier) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        // Handle identifier specifically.
        JPanel subpanel = new JPanel();
        subpanel.add(new JLabel("Name: "));
        JTextField identifierField = new JTextField(
                constructor.getIdentifier(), 15);

        final ApproxsimGUIConstructor fCons = constructor;
        identifierField.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                JTextField tF = (JTextField) e.getSource();
                fCons.setIdentifier(tF.getText());
            }
        });

        identifierField.setEditable(!fixedIdentifier);
        subpanel.add(identifierField);
        panel.add(subpanel);

        panel.add(new javax.swing.JSeparator());
        panel.add(constructor);
        ApproxsimGUIConstructorDialog dialog = new ApproxsimGUIConstructorDialog(
                constructor);
        panel.add(constructor.getExitButtons(dialog));
        dialog.getRootPane().getContentPane().add(panel);
        dialog.pack();

        return dialog;
    }

    /**
     * Builds a dialog associated with this constructor. This dialog will not allow the user to change the identifier of the object created.
     * 
     * @param constructor the constructor to build for.
     */
    public static final ApproxsimGUIConstructorDialog buildDialog(
            ApproxsimGUIConstructor constructor) {
        return buildDialog(constructor, true);
    }

    /**
     * Returns exititems to add at end of panel.
     * 
     * @param d the dialog the buttons control.
     */
    protected JPanel getExitButtons(ApproxsimGUIConstructorDialog d) {
        JPanel panel = new JPanel();
        final ApproxsimGUIConstructorDialog dialog = d;
        final ApproxsimGUIConstructor target = this;
        panel.add(new JButton(new AbstractAction("Create") {
            /**
			 * 
			 */
            private static final long serialVersionUID = -3635537960479502675L;

            public void actionPerformed(ActionEvent e) {
                if (target.getApproxsimObject() != null) {
                    dialog.dispose();
                } else {
                    JOptionPane
                            .showMessageDialog(dialog,
                                               "Unable to construct a \n"
                                                       + target.getDeclaration()
                                                               .getType()
                                                               .getName()
                                                       + " \nfrom the provided values.",
                                               "Error creating "
                                                       + target.getDeclaration()
                                                               .getType()
                                                               .getName(),
                                               JOptionPane.ERROR_MESSAGE);
                }
            }
        }));

        panel.add(new JButton(new AbstractAction("Cancel") {
            /**
			 * 
			 */
            private static final long serialVersionUID = 2459531676002574967L;

            public void actionPerformed(ActionEvent e) {
                dialog.cancel();
            }
        }));

        return panel;
    }
}
