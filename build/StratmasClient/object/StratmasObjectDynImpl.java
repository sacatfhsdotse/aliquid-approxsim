// 	$Id: StratmasObjectDynImpl.java,v 1.4 2006/05/05 17:56:10 dah Exp $
/*
 * @(#)StratmasObject.java
 */

package StratmasClient.object;

import StratmasClient.Client;

import StratmasClient.object.type.Type;
import StratmasClient.object.type.Declaration;
import StratmasClient.object.primitive.Identifier;
import StratmasClient.object.primitive.Timestamp;
import StratmasClient.object.primitive.Reference;

import StratmasClient.Debug;
import StratmasClient.Icon;

import StratmasClient.filter.StratmasObjectFilter;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;
import java.util.EventObject;
import java.util.EventListener;
import javax.swing.event.EventListenerList;
import java.awt.event.ActionEvent;


/**
 * StratmasObjectDynImpl is a convinience implementation of
 * StratmasObject providing dynamic almost anything.
 *
 * @version 1, $Date: 2006/05/05 17:56:10 $
 * @author  Daniel Ahlin
*/
abstract class StratmasObjectDynImpl extends StratmasObjectImpl
{
    /**
     * The icon used to visualize this object.
     */
    Icon icon;

    /**
     * The type of this object.
     */
    Type type;

    /**
     * Creates a new StratmasObject.
     *
     * @param identifier the identifier for the object.
     * @param type the type of the object.
     */
    StratmasObjectDynImpl(String identifier, Type type)
    {
	super(identifier);
	this.type = type;
    }
    
    /**
     * Creates a new StratmasObject from a Declaration.
     *
     * @param declaration the declaration for this object.
     */
    StratmasObjectDynImpl(Declaration declaration)
    {
	this(declaration.getName(), declaration.getType());
    }

    /**
     * Creates a new StratmasObject from a Declaration and changes the
     * Identifier to the specified Identifier.
     *
     * <p> author Per Alexius
     *
     * @param declaration The Declaration for this object.
     * @param identifier The Identifier to use as Identifier for this
     *  object.
     */
     StratmasObjectDynImpl(Declaration declaration, String identifier) {
	  this(declaration);
	  setIdentifier(identifier);
     }

    /**
     * Returns the type of this object.
     */
    public Type getType()
    {
	return this.type;
    }

    /**
     * Returns the icon used to symbolize this object.
     */
    public Icon getIcon()
    {
	if (this.icon == null) {
	    createIcon();
	}
	return this.icon;
    }

    /**
     * Creates an icon for use in this object.
     */
    public void createIcon()
    {
	this.icon = Icon.getIcon(this);
    }

    /**
     * Sets the parent of this object. Overridden to allow icon change
     * on parent change.
     *
     * @param parent the new parent of this object.
     */
    protected void setParent(StratmasObject parent)
    {
	super.setParent(parent);
	if (this.icon != null) {
	    createIcon();
	}
    }
}
