package ApproxsimClient.treeview;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import ApproxsimClient.object.ApproxsimObject;
import ApproxsimClient.object.ApproxsimEvent;
import ApproxsimClient.object.Point;
import ApproxsimClient.Icon;
import ApproxsimClient.IconFactory;
import ApproxsimClient.Configuration;
import ApproxsimClient.filter.ApproxsimObjectFilter;
import ApproxsimClient.proj.MGRSConversion;

/**
 * PointAdapter adapts Points for viewing in the tree.
 */
public class PointAdapter extends ApproxsimObjectAdapter {

    /**
     * Creates a new PontAdapter.
     */
    protected PointAdapter() {
        super();
    }

    /**
     * Creates a new PointAdapter.
     * 
     * @param approxsimObject the object to adapt.
     */
    public PointAdapter(ApproxsimObject approxsimObject) {
        this.setUserObject(approxsimObject);
        Configuration.addApproxsimListener(this);
    }

    /**
     * Creates a new PointAdapter.
     * 
     * @param approxsimObject the object to adapt.
     */
    public PointAdapter(ApproxsimObject approxsimObject,
            ApproxsimObjectFilter filter) {
        this.setUserObject(approxsimObject);
        this.filter = filter;
        Configuration.addApproxsimListener(this);
    }

    /**
     * Initializes the children of this object.
     */
    protected void createChildren() {
        this.children = new Vector();

        if (Configuration.getCoordinateSystem() == Configuration.GEODETIC) {
            silentAdd(new PointCoordinateAdapter(getApproxsimObject(), "lat"),
                      children.size());
            silentAdd(new PointCoordinateAdapter(getApproxsimObject(), "lon"),
                      children.size());
        }
    }

    /**
     * Called when the ApproxsimObject this adapter adapts changes.
     * 
     * @param event the event causing the call.
     */
    public void eventOccured(ApproxsimEvent event) {
        if (event.isValueChanged()) {
            sendTreeNodesChangedEvent();
        } else if (event.isRemoved()) {
            if (getUserObject() != null) {
                getUserObject().removeEventListener(this);
            }
            Configuration.removeApproxsimListener(this);
            sendTreeNodeRemovedEvent();
            if (getParent() != null) {
                parent.remove(this);
            }
            approxsimObject = null;
        } else if (event.isObjectAdded()) {
            ApproxsimObject newObj = (ApproxsimObject) event.getArgument();
            add(newObj);
        } else if (event.isChildChanged()) {
            sendTreeNodesChangedEvent();
        } else if (event.isReplaced()) {
            getUserObject().removeEventListener(this);
            ApproxsimObjectAdapter parent = (ApproxsimObjectAdapter) getParent();
            if (parent != null) {
                sendTreeNodeRemovedEvent();
                parent.remove(this);
                parent.add((ApproxsimObject) event.getArgument());
            } else {
                ApproxsimObject o = (ApproxsimObject) event.getArgument();
                setUserObject(o);
                for (Enumeration en = o.children(); en.hasMoreElements();) {
                    add((ApproxsimObject) en.nextElement());
                }
                sendTreeNodesChangedEvent();
            }
        } else if (event.isCoordSystemChanged()) {
            // MGRS coordinates
            if (Configuration.getCoordinateSystem() == Configuration.MGRS) {
                while (!getChildren().isEmpty()) {
                    ApproxsimObjectAdapter soa = (ApproxsimObjectAdapter) getChildren()
                            .get(0);
                    // FIXME THIS SHOULD NOT BE A REMOVE EVENT!!!
                    soa.eventOccured(ApproxsimEvent.getRemoved(soa, null));
                }
                sendTreeNodesChangedEvent();
            }
            // geodetic (lat, lon) coordinates
            else if (Configuration.getCoordinateSystem() == Configuration.GEODETIC) {
                silentAdd(new PointCoordinateAdapter(getApproxsimObject(), "lat"),
                          children.size());
                silentAdd(new PointCoordinateAdapter(getApproxsimObject(), "lon"),
                          children.size());
                sort();
                for (Enumeration e = getChildren().elements(); e
                        .hasMoreElements();) {
                    sendTreeNodeAddedEvent((ApproxsimObjectAdapter) e
                            .nextElement());
                }
                sendTreeNodesChangedEvent();
            }
        }
    }

    /**
     * Tries to update the target of this adapter with the provided object.
     */
    public void update(Object o) {
        if (o instanceof String) {
            if (Configuration.getCoordinateSystem() == Configuration.MGRS) {
                double[] lon_lat = MGRSConversion.convertMGRSToGeodetic(o
                        .toString());
                if (lon_lat != null) {
                    ((Point) getUserObject())
                            .setLon(Math.toDegrees(lon_lat[0]), this);
                    ((Point) getUserObject())
                            .setLat(Math.toDegrees(lon_lat[1]), this);
                }

            }
        }
    }

    /**
     * Returns the string the invokation of the editor should hold for this value.
     */
    public String getTextTag() {
        if (approxsimObject == null) {
            return null;
        } else {
            String text = getApproxsimObject().getIdentifier();
            if (Configuration.getCoordinateSystem() == Configuration.MGRS) {
                String value = ((Point) getApproxsimObject()).getMGRSValue();
                return text + " : " + value;
            } else {
                return text;
            }
        }
    }

    /**
     * Returns the Icon the invokation of the editor should hold for this value.
     */
    public Icon getIcon() {
        if (getApproxsimObject() == null) {
            return null;
        } else if (Configuration.getCoordinateSystem() == Configuration.GEODETIC) {
            return getApproxsimObject().getIcon();
        } else {
            return IconFactory.getLeafIcon();
        }
    }

    /**
     * Returns the string the invokation of the editor should hold for this value.
     */
    public String toEditableString() {
        if (approxsimObject != null) {
            return "";
        }

        if (Configuration.getCoordinateSystem() == Configuration.GEODETIC) {
            return getApproxsimObject().getIdentifier();
        } else {
            return ((Point) getApproxsimObject()).getMGRSValue();
        }
    }

    /**
     * Returns true if this object can act as a container, else false.
     */
    public boolean getAllowsChildren() {
        return Configuration.getCoordinateSystem() == Configuration.GEODETIC;
    }

    /**
     * Returns true if this object has no children.
     */
    public boolean isLeaf() {
        return Configuration.getCoordinateSystem() != Configuration.GEODETIC;
    }
}

/**
 * Placeholder for synthetic children of point.
 */
class PointCoordinateAdapter extends ApproxsimObjectAdapter {
    /**
     * A vector containing child ApproxsimObjectAdapters Always empy for this class.
     */
    static Vector noChildren = new Vector();

    /**
     * The tag to use (lat or lon).
     */
    String tag;

    /**
     * Creates a new ApproxsimObjectAdapter.
     * 
     * @param approxsimObject the object to adapt.
     * @param tag whether this is lat or lon.
     */
    PointCoordinateAdapter(ApproxsimObject approxsimObject, String tag) {
        super(approxsimObject);
        if (tag != "lat" && tag != "lon") {
            throw new AssertionError(getClass().getName()
                    + " can only have tag \"lat\"  or \"lon\"");
        }
        this.tag = tag;
    }

    /**
     * Initializes the children of this object.
     */
    protected void createChildren() {
        this.children = noChildren;
    }

    /**
     * Returns true if this object can act as a container, else false.
     */
    public boolean getAllowsChildren() {
        return false;
    }

    /**
     * Returns true if this object has no children.
     */
    public boolean isLeaf() {
        return true;
    }

    /**
     * Tries to update the target of this adapter with the provided object.
     */
    public void update(Object o) {
        if (approxsimObject == null) {
            return;
        } else {
            if (o instanceof String) {
                try {
                    double val = Double.parseDouble((String) o);
                    getApproxsimObject();
                    if (tag == "lat") {
                        ((Point) getApproxsimObject()).setLat(val, this);
                    } else { // (tag == "lon")
                        ((Point) getApproxsimObject()).setLon(val, this);
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog((JFrame) null,
                                                  "Parse error:\nUnable to assign \""
                                                          + o + "\" to " + tag
                                                          + " of Point.",
                                                  "Parse Error",
                                                  JOptionPane.ERROR_MESSAGE);
                }
            } else {
                System.err.println("Don't know how to update using a "
                        + o.getClass().toString());
            }
        }
    }

    /**
     * Returns the string the invokation of the editor should hold for this value.
     */
    public String toEditableString() {
        if (getApproxsimObject() == null) {
            return "";
        } else if (tag == "lat") {
            return Double.toString(((Point) getApproxsimObject()).getLat());
        } else { // (tag == "lon")
            return Double.toString(((Point) getApproxsimObject()).getLon());
        }
    }

    /**
     * Returns true if two Adapters represents the same part of the same point
     */
    public boolean equals(Object o) {
        if (o instanceof PointCoordinateAdapter) {
            return approxsimObject == ((ApproxsimObjectAdapter) o).approxsimObject
                    && this.tag == ((PointCoordinateAdapter) o).tag;
        }
        return false;
    }

    /**
     * Returns the string the invokation of the editor should hold for this value.
     */
    public String getTextTag() {
        if (getApproxsimObject() != null) {
            if (tag == "lat") {
                return tag
                        + " "
                        + Double.toString(((Point) getApproxsimObject())
                                .getLat());
            } else { // (tag == "lon")
                return tag
                        + " "
                        + Double.toString(((Point) getApproxsimObject())
                                .getLon());
            }
        } else {
            return null;
        }
    }

    /**
     * Returns the Icon the invokation of the editor should hold for this value.
     */
    public Icon getIcon() {
        return IconFactory.getLeafIcon();
    }
}
