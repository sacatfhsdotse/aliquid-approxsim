package StratmasClient.treeview;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.Point;
import StratmasClient.Icon;
import StratmasClient.IconFactory;
import StratmasClient.Configuration;
import StratmasClient.filter.StratmasObjectFilter;
import StratmasClient.proj.MGRSConversion;

/**
 * PointAdapter adapts Points for viewing in the tree.
 *
 */
public class PointAdapter extends StratmasObjectAdapter {
   
    /**
     * Creates a new PontAdapter.
     */
    protected PointAdapter() 
    {
        super();
    }
   
    /**
     * Creates a new PointAdapter.
     *
     * @param stratmasObject the object to adapt.
     */
    public PointAdapter(StratmasObject stratmasObject) 
    {
        this.setUserObject(stratmasObject);
        Configuration.addStratmasListener(this);
    }
    
    /**
     * Creates a new PointAdapter.
     *
     * @param stratmasObject the object to adapt.
     */
    public PointAdapter(StratmasObject stratmasObject, StratmasObjectFilter filter) 
    {
        this.setUserObject(stratmasObject);
        this.filter = filter;
        Configuration.addStratmasListener(this);        
    }

    /**
     * Initializes the children of this object.
     */
    protected void createChildren()
    {
        this.children = new Vector();

        if (Configuration.getCoordinateSystem() == Configuration.GEODETIC) {
            silentAdd(new PointCoordinateAdapter(getStratmasObject(), "lat"), 
                      children.size());
            silentAdd(new PointCoordinateAdapter(getStratmasObject(), "lon"), 
                      children.size());
        }
    }
        
    /**
     * Called when the StratmasObject this adapter adapts changes.
     *
     * @param event the event causing the call.
     */
    public void eventOccured(StratmasEvent event){
        if (event.isValueChanged()) {
            sendTreeNodesChangedEvent();
        } else if (event.isRemoved()) {
            if (getUserObject() != null) {
                getUserObject().removeEventListener(this);
            }
            Configuration.removeStratmasListener(this);
            sendTreeNodeRemovedEvent();
            if (getParent() != null) {
                parent.remove(this);
            }
            stratmasObject = null;
        } else if (event.isObjectAdded()) {
            StratmasObject newObj = (StratmasObject) event.getArgument();
            add(newObj);
        } else if (event.isChildChanged()) {
            sendTreeNodesChangedEvent();
        } else if (event.isReplaced()) {
            getUserObject().removeEventListener(this);
            StratmasObjectAdapter parent = (StratmasObjectAdapter)getParent();
            if (parent != null) {
                sendTreeNodeRemovedEvent();
                 parent.remove(this);
                 parent.add((StratmasObject)event.getArgument());
            }
            else {
                StratmasObject o = (StratmasObject)event.getArgument();
                 setUserObject(o);
                 for (Enumeration en = o.children(); en.hasMoreElements(); ) {
                     add((StratmasObject)en.nextElement());
                 }
                 sendTreeNodesChangedEvent();
            }
        }
        else if (event.isCoordSystemChanged()) {
            // MGRS coordinates
            if (Configuration.getCoordinateSystem() == Configuration.MGRS) {
                while (!getChildren().isEmpty()) {
                    StratmasObjectAdapter soa = (StratmasObjectAdapter) getChildren().get(0);
                    //FIXME THIS SHOULD NOT BE A REMOVE EVENT!!!
                    soa.eventOccured(StratmasEvent.getRemoved(soa, null));
                }
                sendTreeNodesChangedEvent();
            }
            // geodetic (lat, lon) coordinates
            else if (Configuration.getCoordinateSystem() == Configuration.GEODETIC) {
                silentAdd(new PointCoordinateAdapter(getStratmasObject(), "lat"), 
                          children.size());
                silentAdd(new PointCoordinateAdapter(getStratmasObject(), "lon"), 
                          children.size());
                sort();
                for (Enumeration e = getChildren().elements(); e.hasMoreElements();) {
                    sendTreeNodeAddedEvent((StratmasObjectAdapter)e.nextElement());
                }
                sendTreeNodesChangedEvent();
            }
        }
    }
    
    /**
     * Tries to update the target of this adapter with the provided
     * object.
     */   
    public void update(Object o)
    {
        if (o instanceof String) {
            if (Configuration.getCoordinateSystem() == Configuration.MGRS) {
                double[] lon_lat = MGRSConversion.convertMGRSToGeodetic(o.toString());
                if (lon_lat != null) {
                    ((Point)getUserObject()).setLon(Math.toDegrees(lon_lat[0]), this);
                    ((Point)getUserObject()).setLat(Math.toDegrees(lon_lat[1]), this);   
                }

            }
        }
    }
    
    
    /**
     * Returns the string the invokation of the editor should hold for
     * this value.
     */    
    public String getTextTag()
    {        
        if (stratmasObject == null) {
            return null;
        } else {
            String text = getStratmasObject().getIdentifier();
            if (Configuration.getCoordinateSystem() == Configuration.MGRS) {
                String value = ((Point)getStratmasObject()).getMGRSValue();
                return text+" : "+value;
            }
            else {
                return text;
            }
        }
    }
    
    /**
     * Returns the Icon the invokation of the editor should hold for
     * this value.
     */    
    public Icon getIcon()
    {        
        if (getStratmasObject() == null) {
            return null;
        } else if (Configuration.getCoordinateSystem() == Configuration.GEODETIC) {
            return getStratmasObject().getIcon();
        } else {
            return IconFactory.getLeafIcon();
        }
    }
    
    /**
     * Returns the string the invokation of the editor should hold for
     * this value.
     */    
    public String toEditableString()
    {
        if (stratmasObject != null) {
            return "";
        }

        if (Configuration.getCoordinateSystem() == Configuration.GEODETIC) {
            return getStratmasObject().getIdentifier();  
        }
        else {
            return ((Point)getStratmasObject()).getMGRSValue();
        }
    }

    /**
     * Returns true if this object can act as a container, else false.
     */
    public boolean getAllowsChildren()
    {
        return Configuration.getCoordinateSystem() == Configuration.GEODETIC;
    }
    
    /**
     * Returns true if this object has no children.
     */
    public boolean isLeaf()
    {
        return Configuration.getCoordinateSystem() != Configuration.GEODETIC;
    }    
}

/**
 * Placeholder for synthetic children of point.
 */
class PointCoordinateAdapter extends StratmasObjectAdapter
{
    /**
     * A vector containing child StratmasObjectAdapters Always empy for this class.
     */
    static Vector noChildren = new Vector();

    /**
     * The tag to use (lat or lon).
     */
    String tag;

    /**
     * Creates a new StratmasObjectAdapter.
     *
     * @param stratmasObject the object to adapt.
     * @param tag whether this is lat or lon.
     */
    PointCoordinateAdapter(StratmasObject stratmasObject, 
                           String tag)
    {
        super(stratmasObject);
        if (tag != "lat" && tag != "lon") {
            throw new AssertionError(getClass().getName() + 
                                     " can only have tag \"lat\"  or \"lon\"");
        }
        this.tag = tag;
    }

    /**
     * Initializes the children of this object.
     */
    protected void createChildren()
    {
        this.children = noChildren;
    }

    /**
     * Returns true if this object can act as a container, else false.
     */
    public boolean getAllowsChildren()
    {
        return false;
    }
    
    /**
     * Returns true if this object has no children.
     */
    public boolean isLeaf()
    {
        return true;
    }

    /**
     * Tries to update the target of this adapter with the provided
     * object.
     */   
    public void update(Object o)
    {
        if (stratmasObject == null) {
            return;
        } else {
            if (o instanceof String) {
                try {
                    double val = Double.parseDouble((String) o);
                    getStratmasObject();
                    if (tag == "lat") {
                        ((Point) getStratmasObject()).setLat(val, this);
                    } else { // (tag == "lon")
                        ((Point) getStratmasObject()).setLon(val, this);
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog((JFrame) null, "Parse error:\nUnable to assign \"" + 
                                                  o + "\" to " + tag + " of Point.",
                                                  "Parse Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                System.err.println("Don't know how to update using a " + o.getClass().toString());
            }
        }
    }

    /**
     * Returns the string the invokation of the editor should hold for
     * this value.
     */    
    public String toEditableString()
    {
        if (getStratmasObject() == null) {
            return "";
        } else if (tag == "lat") {
            return Double.toString(((Point) getStratmasObject()).getLat());
        } else { // (tag == "lon")
            return Double.toString(((Point) getStratmasObject()).getLon());
        }
    }

    /**
     * Returns true if two Adapters represents the same part of the same point
     */
    public boolean equals(Object o)
    {
        if (o instanceof PointCoordinateAdapter) {
            return stratmasObject == ((StratmasObjectAdapter) o).stratmasObject &&
                this.tag == ((PointCoordinateAdapter) o).tag;
        }
        return false;
    }

    /**
     * Returns the string the invokation of the editor should hold for
     * this value.
     */    
    public String getTextTag()
    {        
        if (getStratmasObject() != null) {
            if (tag == "lat") {
                return tag + " " + Double.toString(((Point) getStratmasObject()).getLat());
            } else { //(tag == "lon")
                return tag + " " + Double.toString(((Point) getStratmasObject()).getLon());
            }
        } else {
            return null;
        }
    }

    /**
     * Returns the Icon the invokation of the editor should hold for
     * this value.
     */    
    public Icon getIcon()
    {        
        return IconFactory.getLeafIcon();
    }
}
