//         $Id: StratmasComplexGUIConstructor.java,v 1.1 2006/03/22 14:30:51 dah Exp $
/*
 * @(#)StratmasObject.java
 */

package StratmasClient.object;

import StratmasClient.object.type.Declaration;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;

/**
 * StratmasComplexGUIConstructor creates GUIs for creating
 * StratmasComplex objects.
 *
 * @version 1, $Date: 2006/03/22 14:30:51 $
 * @author  Daniel Ahlin
*/
public class StratmasComplexGUIConstructor extends StratmasGUIConstructor
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -8177601284453709935L;
	/**
     * The StratmasGUIConstructor's for each subpart of this constructor.
     */
    Vector subDeclarations;

    /**
     * Creates a new StratmasComplexGUIConstructor using
     * specifications in the supplied declaration.
     *
     * @param declaration the declaration to use.
     * @param useChooser lets the user choose different the subtypes
     * of the provided declaration.
     */
    public StratmasComplexGUIConstructor(Declaration declaration, boolean useChooser)
    {
        super(declaration, useChooser);
    }

    /**
     * Creates a new StratmasComplexGUIConstructor using
     * specifications in the supplied declaration. If the type in the
     * declaration is abstract
     * StratmasComplexGUIConstructor(declaration, true) is run.
     *
     * @param declaration the declaration to use.
     */
    public StratmasComplexGUIConstructor(Declaration declaration)
    {
        super(declaration);
    }
   
    /**
     * Builds the panels used by this constructor
     */    
    protected void buildPanel() 
    {
        if (subDeclarations == null) {
            subDeclarations = new Vector();
        }
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        for (Enumeration ss = getType().getSubElements().elements(); 
             ss.hasMoreElements();) {
            processSubDeclaration((Declaration) ss.nextElement());
        }
    }

    /**
     * Handle a subdeclaration
     *
     * @param subDeclaration the declaration to use.
     */
    protected void processSubDeclaration(Declaration subDeclaration)
    {
        StratmasGUIConstructor c = StratmasObjectFactory.guiCreate(subDeclaration);
        subDeclarations.add(c);
        add(c);
    }
    
    /**
     * Tries to create the StratmasObject from the values in the GUI.
     */
    protected void createStratmasObject()
    {
        Vector contents = new Vector();
        Vector unFinished = new Vector();

        for (Enumeration ss = this.subDeclarations.elements(); 
             ss.hasMoreElements();) {
            StratmasGUIConstructor s = 
                (StratmasGUIConstructor) ss.nextElement();
            StratmasObject sObj = s.getStratmasObject();
            if (sObj == null && s.getDeclaration().getMinOccurs() != 0) {
                unFinished.add(s.getDeclaration().getName());
            } else if (sObj != null) {
                contents.add(sObj);
            }
        }
        
        if (unFinished.isEmpty()) {
            Declaration decl = getDeclaration().clone(getType());
            StratmasVectorConstructor vectorConstructor = 
                StratmasObjectFactory.vectorCreate(decl);
            this.result = vectorConstructor.getStratmasObject(getIdentifier(), contents);
        } else {
            String errmsg = "The following required items are missing:";
            for (Enumeration ss = unFinished.elements(); ss.hasMoreElements();) {
                errmsg = errmsg + "\n" + ss.nextElement().toString();            
            }
            JOptionPane.showMessageDialog(null, errmsg, 
                                          this.getType().getName() + 
                                          " is not complete", 
                                          JOptionPane.WARNING_MESSAGE);                        
            this.result = null;
        }
    }
}

