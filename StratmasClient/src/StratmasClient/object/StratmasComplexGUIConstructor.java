// $Id: ApproxsimComplexGUIConstructor.java,v 1.1 2006/03/22 14:30:51 dah Exp $
/*
 * @(#)ApproxsimObject.java
 */

package ApproxsimClient.object;

import ApproxsimClient.object.type.Declaration;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;

/**
 * ApproxsimComplexGUIConstructor creates GUIs for creating ApproxsimComplex objects.
 * 
 * @version 1, $Date: 2006/03/22 14:30:51 $
 * @author Daniel Ahlin
 */
public class ApproxsimComplexGUIConstructor extends ApproxsimGUIConstructor {
    /**
	 * 
	 */
    private static final long serialVersionUID = -8177601284453709935L;
    /**
     * The ApproxsimGUIConstructor's for each subpart of this constructor.
     */
    Vector<ApproxsimGUIConstructor> subDeclarations;

    /**
     * Creates a new ApproxsimComplexGUIConstructor using specifications in the supplied declaration.
     * 
     * @param declaration the declaration to use.
     * @param useChooser lets the user choose different the subtypes of the provided declaration.
     */
    public ApproxsimComplexGUIConstructor(Declaration declaration,
            boolean useChooser) {
        super(declaration, useChooser);
    }

    /**
     * Creates a new ApproxsimComplexGUIConstructor using specifications in the supplied declaration. If the type in the declaration is
     * abstract ApproxsimComplexGUIConstructor(declaration, true) is run.
     * 
     * @param declaration the declaration to use.
     */
    public ApproxsimComplexGUIConstructor(Declaration declaration) {
        super(declaration);
    }

    /**
     * Builds the panels used by this constructor
     */
    protected void buildPanel() {
        if (subDeclarations == null) {
            subDeclarations = new Vector<ApproxsimGUIConstructor>();
        }
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        for (Enumeration ss = getType().getSubElements().elements(); ss
                .hasMoreElements();) {
            processSubDeclaration((Declaration) ss.nextElement());
        }
    }

    /**
     * Handle a subdeclaration
     * 
     * @param subDeclaration the declaration to use.
     */
    protected void processSubDeclaration(Declaration subDeclaration) {
        ApproxsimGUIConstructor c = ApproxsimObjectFactory
                .guiCreate(subDeclaration);
        subDeclarations.add(c);
        add(c);
    }

    /**
     * Tries to create the ApproxsimObject from the values in the GUI.
     */
    protected void createApproxsimObject() {
        Vector<ApproxsimObject> contents = new Vector<ApproxsimObject>();
        Vector<String> unFinished = new Vector<String>();

        for (Enumeration<ApproxsimGUIConstructor> ss = this.subDeclarations
                .elements(); ss.hasMoreElements();) {
            ApproxsimGUIConstructor s = ss.nextElement();
            ApproxsimObject sObj = s.getApproxsimObject();
            if (sObj == null && s.getDeclaration().getMinOccurs() != 0) {
                unFinished.add(s.getDeclaration().getName());
            } else if (sObj != null) {
                contents.add(sObj);
            }
        }

        if (unFinished.isEmpty()) {
            Declaration decl = getDeclaration().clone(getType());
            ApproxsimVectorConstructor vectorConstructor = ApproxsimObjectFactory
                    .vectorCreate(decl);
            this.result = vectorConstructor.getApproxsimObject(getIdentifier(),
                                                              contents);
        } else {
            String errmsg = "The following required items are missing:";
            for (Enumeration<String> ss = unFinished.elements(); ss
                    .hasMoreElements();) {
                errmsg = errmsg + "\n" + ss.nextElement().toString();
            }
            JOptionPane.showMessageDialog(null, errmsg, this.getType()
                                                  .getName()
                                                  + " is not complete",
                                          JOptionPane.WARNING_MESSAGE);
            this.result = null;
        }
    }
}
