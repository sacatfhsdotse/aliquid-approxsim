package StratmasClient.timeline;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.awt.Font;
import java.awt.Color;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.StratmasEventListener;

/**
 * The list of military units used as resources for the activities in the table.
 *
 * @author Amir Filipovic
 */
public class ActivityTableComboBox extends JComboBox implements StratmasEventListener{
    /**
     * The list of the military units. The StratmasObject identifiers are used as keys while
     * the values are Vector objects containing the military units. Vector is used
     * as the value because of possible multiple occurance of the identifiers.
     */
    private Hashtable resources = new Hashtable();
    
    /**
     * Creates new combo box.
     */
    public ActivityTableComboBox() {
	super();
	this.setBackground(Color.WHITE);
	this.setFont(this.getFont().deriveFont(Font.PLAIN));
    }
    
    /**
     * Updates the list.
     */
    public synchronized void eventOccured(StratmasEvent e) {
	StratmasObject so = (StratmasObject)e.getSource();
	if (e.isIdentifierChanged()) {
	    removeResource(so, (String)e.getArgument());
	    addResource(so);
	}
	else if (e.isRemoved()) {
	    removeResource(so, so.getIdentifier());  
	}
	else if (e.isObjectAdded()) {
	    addResource(so);
	}
    }
    
    /**
     * Adds a military unit to the combo box.
     *
     * @param resource a military unit.
     */
    public void addResource(StratmasObject resource) {
	// a resource with the similar identifier already exists in the list
	if (resources.containsKey(resource.getIdentifier())) {
	    Vector resVec = (Vector)resources.get(resource.getIdentifier());
	    if (!resVec.contains(resource)) {
		resVec.add(resource);
	    }
	}
	// no resources with the similar identifier exist in the list
	else {
	    Vector refVec = new Vector();
	    refVec.add(resource);
	    resources.put(resource.getIdentifier(), refVec);
	    // update the combo box
	    int i = 1;
	    while (i < getItemCount() && resource.getIdentifier().compareTo((String)getItemAt(i)) > 0) {
		i++;
	    }
	    if (i < getItemCount()) {
		((DefaultComboBoxModel)this.getModel()).insertElementAt(resource.getIdentifier(), i);
	    }
	    else {
		this.addItem(resource.getIdentifier());
	    }
	}
	// listen to the resource
	resource.addEventListener(this);
    } 

    /**
     * Removes a military unit from the combo box.
     *
     * @param resource a military unit.
     * @param identifier the key of the resource in the list.
     */
    public void removeResource(StratmasObject resource, String identifier) {
	if (resources.containsKey(identifier)) {
	    Vector res = (Vector)resources.get(identifier);
	    res.remove(resource);
	    // remove the identifier from the list
	    if (res.isEmpty()) {
		resources.remove(identifier);
		// update the combo box
		this.removeItem(identifier);
	    }
	    // remove the listener
	    resource.removeEventListener(this);
	}
    } 
    
    /**
     * Returns the resources with the given identifier.
     */
    public Vector getResources(String id) {
	return (Vector)resources.get(id);
    } 
    
    /**
     * Returns true if the list contains the resource.
     */
    public boolean contains(StratmasObject resource) {
	if (resources.containsKey(resource.getIdentifier())) {
	    Vector res = getResources(resource.getIdentifier());
	    if (res.contains(resource)) {
		return true;
	    }
	}
	return false;
    }
    
}
