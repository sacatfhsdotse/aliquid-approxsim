// 	$Id: StratmasGUIConstructor.java,v 1.1 2006/03/22 14:30:51 dah Exp $
/*
 * @(#)StratmasGUIConstructor.java
 */

package StratmasClient.object;

import StratmasClient.object.type.Declaration;
import StratmasClient.object.type.Type;
import StratmasClient.TypeSelector;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import java.util.Vector;
import java.util.Enumeration;
import java.awt.event.ActionEvent;
import javax.swing.BoxLayout;

/**
 * StratmasGUIConstructor is a abstract superclass for JComponents
 * acting as source of new StratmasObjects.
 *
 * @version 1, $Date: 2006/03/22 14:30:51 $
 * @author  Daniel Ahlin
*/
public abstract class StratmasGUIConstructor extends JPanel
{
    /**
     * The declaration for which the result is constructed.
     */
    Declaration declaration;

    /**
     * The type of the constructed result (this may be a derivation of
     * the type specified in the  declaration).
     */
    Type type;

    /**
     * The identifier that the constructor uses to create the object.
     */
    String identifier;
    
    /**
     * The resulting StratmasObject
     */
    StratmasObject result = null;

    /**
     * If this gui constructor uses a chooser.
     */
    boolean useChooser = false;


    /**
     * Creates a new StratmasComplexGUIConstructor using
     * specifications in the supplied declaration.
     *
     * @param declaration the declaration to use.
     * @param useChooser lets the user choose different the subtypes
     * of the provided declaration.
     */
    public StratmasGUIConstructor(Declaration declaration, boolean useChooser)
    {
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
    public StratmasGUIConstructor(Declaration declaration)
    {
	this(declaration, declaration.getType().isAbstract());
    }

    /**
     * Builds the panel presented to the user.
     */   
    public void buildChooser()
    {
	this.add(new JLabel(declaration.getName()));
	final StratmasGUIConstructor self = this;
	final TypeSelector typeSelector = new TypeSelector(this.declaration.getType());
	// Hack to get selected item implicitly selected item.
	typeSelector.setSelectedIndex(0);
	this.add(typeSelector);
	final JButton createButton = new JButton();
	createButton.setAction(new AbstractAction("Create...") {
		/**
		 * Invoked when an action occurs.
		 *
		 * @param e the event
		 */
		public void actionPerformed(ActionEvent e)
		{
		    StratmasGUIConstructorDialog deferedDialog = 
			StratmasGUIConstructor.buildDialog(StratmasObjectFactory.guiCreate(declaration.clone(typeSelector.getSelectedType())));
		    deferedDialog.setVisible(true);
		    self.setStratmasObject(deferedDialog.getStratmasObject());
		    if (self.getStratmasObject() != null) {
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
     * Tries to create the StratmasObject from the values in the GUI.
     */
    protected abstract void createStratmasObject();

    /**
     * Sets the type for this constructor to create.
     *
     * @param type the type to set as target.
     */
    public void setType(Type type)
    {
	this.type = type;
    }
    
    /**
     * The type for which this constructor is currently set.
     */
    public Type getType()
    {
	return this.type;
    }

    /**
     * Sets the identifier that the constructor uses to create the object.
     *
     * @param identifier the identifier to set.
     */
    public void setIdentifier(String identifier)
    {
	this.identifier = identifier;
    }
    
    /**
     * Returns the identifier that the constructor uses to creat the object.
     */
    public String getIdentifier()
    {
	return this.identifier;
    }

    /**
     * Returns the StratmasObject this component was created to provide.
     */
    public StratmasObject getStratmasObject()
    {
	if (this.result == null) {
	    // If this constructor is handled by the chooser the defered
	    // constructor will take care of the creation of the StratmasObject.	    
	    if (! useChooser) {
		createStratmasObject();
	    }
	}

	if (this.result != null) {
	    this.result.setIdentifier(getIdentifier());
	}

	return this.result;
    }

    /**
     * Sets the StratmasObject this component will provide.
     */
    protected void setStratmasObject(StratmasObject object)
    {
	this.result = object;
    }

    /**
     * The declaration for which the result is constructed.
     */
    protected Declaration getDeclaration()
    {
	return this.declaration;
    }

    /**
     * Builds a dialog associated with this constructor.
     *
     * @param constructor the constructor to build for.
     * @param fixedIdentifier wether the identifier of the object
     * created is fixed.
     */    
    public static final StratmasGUIConstructorDialog buildDialog(StratmasGUIConstructor constructor, boolean fixedIdentifier) 
    {
	JPanel panel = new JPanel();
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	// Handle identifier specifically.
	JPanel subpanel = new JPanel();
	subpanel.add(new JLabel("Name: "));
	JTextField identifierField = 
	    new JTextField(constructor.getIdentifier(), 15);

	final StratmasGUIConstructor fCons = constructor;
	identifierField.addCaretListener(new CaretListener()
	    {
		public void caretUpdate(CaretEvent e)
		{
		    JTextField tF = (JTextField) e.getSource();		    
		    fCons.setIdentifier(tF.getText());
		}
	    });

	identifierField.setEditable(!fixedIdentifier);
	subpanel.add(identifierField);
	panel.add(subpanel);

	panel.add(new javax.swing.JSeparator());
	panel.add(constructor);
	StratmasGUIConstructorDialog dialog = new StratmasGUIConstructorDialog(constructor);
	panel.add(constructor.getExitButtons(dialog));
	dialog.getRootPane().getContentPane().add(panel);
	dialog.pack();

	return dialog;
    }

    /**
     * Builds a dialog associated with this constructor. This dialog
     * will not allow the user to change the identifier of the object
     * created.
     *
     * @param constructor the constructor to build for.
     */    
    public static final StratmasGUIConstructorDialog buildDialog(StratmasGUIConstructor constructor) 
    {
	return buildDialog(constructor, true);
    }
    
    /**
     * Returns exititems to add at end of panel.
     *
     * @param d the dialog the buttons control.
     */
    protected JPanel getExitButtons(StratmasGUIConstructorDialog d)
    {
	JPanel panel = new JPanel();
	final StratmasGUIConstructorDialog dialog = d;
	final StratmasGUIConstructor target = this;
	panel.add(new JButton(new AbstractAction("Create") {
		public void actionPerformed(ActionEvent e)		{
		    if (target.getStratmasObject() != null) {
			dialog.dispose();
		    } else {
			JOptionPane.showMessageDialog(dialog, "Unable to construct a \n" + 
						      target.getDeclaration().getType().getName() + 
						      " \nfrom the provided values.",
						      "Error creating " + 
						      target.getDeclaration().getType().getName(), 
						      JOptionPane.ERROR_MESSAGE);
		    }
		}
	    }));

	panel.add(new JButton(new AbstractAction("Cancel") {
		public void actionPerformed(ActionEvent e)
		{
		    dialog.cancel();
		}
	    }));
    
	return panel;
    }
}
