
//         $Id: Point.java,v 1.6 2006/04/21 07:55:46 alexius Exp $
/*
 * @(#)Point.java
 */

package StratmasClient.object;

import java.util.Vector;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import org.w3c.dom.Element;

import java.text.ParseException;

import StratmasClient.IconFactory;
import StratmasClient.Icon;
import StratmasClient.Debug;
import StratmasClient.proj.MGRSConversion;
import StratmasClient.object.type.Type;
import StratmasClient.object.type.Declaration;
import StratmasClient.object.type.TypeFactory;
import StratmasClient.object.primitive.Timestamp;
import StratmasClient.object.primitive.Reference;
import StratmasClient.object.primitive.Identifier;

import javax.swing.JTextField;
import javax.swing.JLabel;

/**
 * Represents a point
 *
 * @version 1, $Date: 2006/04/21 07:55:46 $
 * @author  Daniel Ahlin
*/

public class Point extends StratmasObjectImpl
{
    /**
     * The latitude component of this point.
     */
    double lat;
    
    /**
     * The longitude component of this point.
     */
    double lon;

    /**
     * The type of this object.
     */
    static Type type = TypeFactory.getType("Point");

    /**
     * The icon of this Point
     */
    static Icon icon = IconFactory.useTypeMapping(TypeFactory.getType("Point"));

    /**
     * Creates a new point.
     * 
     * @param identifier the identifier for the object.
     * @param lat the latitude component of this point.
     * @param lon the longitude component of this point.
     */
    protected Point(String identifier, double lat, double lon)
    {
        super(identifier);
        this.lon = lon;
        this.lat = lat;
    }

    /**
     * Creates a new Point from a Declaration.
     *
     * @param declaration the declaration for this object.
     * @param lat the latitude component of this point.
     * @param lon the longitude component of this point.
     */
    protected Point(Declaration declaration, double lat, double lon)
    {
        this(declaration.getName(), lat, lon);
    }

    /**
     * Returns the type of this object.
     */
    public Type getType()
    {
        return type;
    }

    /**
     * Returns the icon used to symbolize this object.
     */
    public Icon getIcon()
    {
        return icon;
    }

    /**
     * Returns the vertical component of this point.     
     */
    public double getLat()
    {
        return this.lat;
    }

    /**
     * Returns the horizontal component of this point.     
     */
    public double getLon()
    {
            return this.lon;
    }

    /**
     * Returns the MGRS value of this point.
     */
    public String getMGRSValue() {
        return MGRSConversion.convertGeodeticToMGRS(Math.toRadians(this.getLon()), 
                                                    Math.toRadians(this.getLat()), 5);
    }

    /**
     * Sets the latitude component of this point.     
     * @param lat the new latitude component of this point.
     */
    public void setLat(double lat, Object initiator)
    {
        this.lat = lat;
        fireValueChanged(initiator);
    }

    /**
     * Sets the longitude component of this point.     
     * @param lon the new longitude component of this point.
     */
    public void setLon(double lon, Object initiator)
    {
        this.lon = lon;
        fireValueChanged(initiator);
    }

    /**
     * Sets both longitude component of this point. (will not send
     * update notification until both are changed)
     * @param lat the new latitude component of this point.
     * @param lon the new longitude component of this point.
     */
    public void setLatLon(double lat, double lon, Object initiator)
    {
        this.lat = lat;
        this.lon = lon;
        fireValueChanged(initiator);
    }

    /**
     * Notifies all listeners that the value of this simple type has changed.
     *
     * @param initiator the initiator of the change.
     */
    protected void fireValueChanged(Object initiator)
    {
        // Always tell parent that we have changed (if we have one).
        if (getParent() != null) {
            getParent().childChanged(this, initiator);
        }
        
        // Guaranteed to return a non-null array
        Object[] listeners = getListenerList();
        if (listeners.length > 0) {
            if (listeners.length > 0) {
                StratmasEvent event = StratmasEvent.getValueChanged(this, initiator);
                for (int i = listeners.length - 2; i >= 0; i -= 2) {
                    ((StratmasEventListener) listeners[i + 1]).eventOccured(event);
                }
            }
        }
    }
    
    /**
     * Moves this point relative to its current position.
     *
     * <p>author  Per Alexius
     *
     * @param dx The distance to move given in degrees longitude.
     * @param dy The distance to move given in degrees latitude.
     */
    public void move(double dx, double dy)
    {
        setLatLon(getLat() + dy, getLon() + dx, this);
    }
    
    /**
     * Moves this point to the specified location.
     *
     * <p>author  Per Alexius
     *
     * @param lng The longitude of the new location.
     * @param lat The latitude of the new location.
     */
    public void moveTo(double lng, double lat)
    {
        move(lng - getLon(), lat - getLat());
    }

    /**
      * Creates an XML representation of the body of this object.
      *
      * <p>author  Per Alexius
      *
      * @param b The StringBuffer to write to.
      * @return The StringBuffer b with an XML representation of this
      * object's body appended to it.
      */
     public StringBuffer bodyXML(StringBuffer b) {
          b.append(NL).append("<lat>").append(getLat()).append("</lat>");
          b.append(NL).append("<lon>").append(getLon()).append("</lon>");
          return b;
     }

    /**
     * Returns a StratmasGUIConstructor suitable for constructing
     * objects of this type.
     *
     * @param declaration the declaration for which the GUI is created.
     */
    protected static StratmasGUIConstructor getGUIConstructor(Declaration declaration)
    {
        return new PointGUIConstructor(declaration); 
    }

    /**
     * Creates a Point from the specified Declaration.
     *
     * <p> author Per Alexius
     *
     * @param declaration The declaration for which the object is created.
     */
    protected static StratmasObject defaultCreate(Declaration declaration)
    {
        return new Point(declaration.getName(), 0.0d, 0.0d);
    }
    
    /**
     * Clones this object. Notice that the Identifier is NOT
     * cloned. Both the clone and the original object will thus keep a
     * reference to the same Identifier object.
     *
     * <p> author Per Alexius
     *
     * @return A clone of this object.
     */
     protected Object clone() {
          return new Point(identifier, getLat(), getLon());
     }

    /**
     * Creates a Shape from a DOM element.
     *
     * <p> author Per Alexius
     *
     * @param n The dom element from which the object is created.
     */
    protected static StratmasObject domCreate(Element n)
    {
        return 
            StratmasObjectFactory.createPoint(Identifier.getIdentifier(n),
                                              XMLHelper.getDouble(n, "lat"),
                                              XMLHelper.getDouble(n, "lon"));
    }
    
    /**
     * Updates this object with the data contained in the Element n.
     *
     * <p> author Per Alexius
     *
     * @param n The DOM Element from which to fetch the data.
     * @param t The simulation time for which the data is valid.
     */
    public void update(Element n, Timestamp t) 
    {
        setLatLon(XMLHelper.getDouble(n, "lat"),
                  XMLHelper.getDouble(n, "lon"), 
                  n);
    }
}

/**
 * PointGUIConstructor creates GUIs for creating Point objects.
 *
 * @version 1, $Date: 2006/04/21 07:55:46 $
 * @author  Daniel Ahlin
*/
class PointGUIConstructor extends StratmasGUIConstructor
{
    JTextField latField;
    JTextField lonField;

    /**
     * Creates a new PointGUIConstructor using the supplied
     * declaration.  * @param declaration the declaration to use.
     */
    public PointGUIConstructor(Declaration declaration)
    {
        super(declaration);
    }

    /**
     * Builds the panels used by this constructor
     */    
    protected void buildPanel()
    {
        this.add(new JLabel(declaration.getName() + "(lat lon)"));
        this.latField = new JTextField(5);
        this.lonField = new JTextField(5);
        this.add(this.latField);
        this.add(this.lonField);    
    }

    /**
     * Tries to create the StratmasObject from the values in the GUI.
     */
    protected void createStratmasObject()
    {
        try {
            setStratmasObject(new Point(this.getDeclaration().getName(), 
                                        Double.parseDouble(lonField.getText()), 
                                        Double.parseDouble(latField.getText())));
        } catch (NumberFormatException e) {
            setStratmasObject(null);
        }
    }
}
