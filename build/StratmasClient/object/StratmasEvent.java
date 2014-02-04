package StratmasClient.object;

import StratmasClient.object.primitive.Timestamp;

/**
 * A class representing different kinds of Stratmas specific events.
 *
 * @version 1, $Date: 2006/03/31 16:55:51 $
 * @author  Per Alexius
 */
public class StratmasEvent extends java.util.EventObject {
    protected static final String CHILDCHANGED         = "childChanged";
    protected static final String GENERIC              = "generic";
    protected static final String OBJECTADDED          = "objectAdded";
    protected static final String OBJECTCREATED        = "objectCreated";
    protected static final String REMOVED              = "removed";
    protected static final String REPLACED             = "replaced";
    protected static final String SUBSCRIPTIONHANDLED  = "subscriptionHandled";
    protected static final String VALUECHANGED         = "valueChanged";
    protected static final String SELECTED             = "selected";
    protected static final String UNSELECTED           = "unselected";
    protected static final String REGIONUPDATED        = "regionUpdated";
    protected static final String GRIDUPDATED          = "gridUpdated";
    protected static final String GRATICULESUPDATED    = "graticulesUpdated";
    protected static final String COORDSYSTEMCHANGED   = "coordSystemChanged";
    protected static final String IDENTIFIERCHANGED    = "identifierChanged";
    
     /** The message that defines which type of event this is. */
     private String message;
     /**
      * The initiator of the event. This is not the same as the
      * source. For example, if an object o is dragged and dropped on
      * the map, the map initiates an event that has o as its source.
      */
     private Object mInitiator = null;
     /** Optional argument. */
     private Object mArgument  = null;
    
     /**
      * Creates a StratmasEvent.
      *
      * @param source The source of the event.
      * @param message The message that defines which type of event
      * this is.
      */
     protected StratmasEvent(Object source, String message) {
          super(source);
          this.message = message;
     }
    
     /**
      * Creates a StratmasEvent.
      *
      * @param source The source of the event.
      * @param message The message that defines which type of event
      * this is.
      * @param initiator The initiator of the event.
      * @param arg An optional argument.
      */
     protected StratmasEvent(Object source, String message, Object initiator, Object arg) {
          this(source, message);
          mInitiator = initiator;
          mArgument  = arg;
     }
    
     /**
      * Accessor for the message.
      *
      * @return The message.
      */
     public String getStratmasMessage() {
          return message;
     }
        
     /**
      * Accessor for the initiator.
      *
      * @return The initiator.
      */
     public Object getInitiator() {
          return mInitiator;
     }
        
     /**
      * Accessor for the argument.
      *
      * @return The argument.
      */
     public Object getArgument() {
          return mArgument;
     }
    
     /**
      * Creates a new Generic event. This type of event may be used
      * when the event type isn't carrying any information.
      *
      * @param source The source of the event.
      *
      * @return The newly created event.
      */
     public static StratmasEvent getGeneric(Object source) {
          return new StratmasEvent(source, GENERIC);
     }
     
     /**
      * Checks if this is a Generic event.
      *
      * @return true if this is an Generic event, false otherwise.
      */
     public boolean isGeneric() {
          return GENERIC.equals(message);
     }
    
     /**
      * Creates a new ObjectAdded event. This type of event is
      * triggered by a StratmasObject when a child is added.
      *
      * @param source The source of the event, i.e. the StratmasObject
      * to which the child was added.
      * @param added The StratmasObject that was added.
      * @param initiator The initiator of the event.
      *
      * @return The newly created event.
      */
     public static StratmasEvent getObjectAdded(Object source, StratmasObject added, Object initiator) {
          return new StratmasEvent(source, OBJECTADDED, initiator, added);
     }
    
     /**
      * Checks if this is an ObjectAdded event.
      *
      * @return true if this is an ObjectAdded event, false otherwise.
      */
     public boolean isObjectAdded() {
          return OBJECTADDED.equals(message);
     }
    
     /**
      * Checks if this is an ObjectCreated event.
      *
      * @return true if this is an ObjectCreated event, false otherwise.
      */
     public boolean isObjectCreated() {
          return OBJECTCREATED.equals(message);
     }
    
     /**
      * Creates a new ObjectRemoved event. This type of event is
      * triggered by a StratmasObject when it is deleted.
      *
      * @param source The source of the event, i.e. the StratmasObject
      * that was removed.
      * @param initiator The initiator of the event.
      *
      * @return The newly created event.
      */
     public static StratmasEvent getRemoved(Object source, Object initiator) {
          return new StratmasEvent(source, REMOVED, initiator, null);
     }
    
     /**
      * Checks if this is an ObjectRemoved event.
      *
      * @return true if this is an ObjectRemoved event, false
      * otherwise.
      */
     public boolean isRemoved() {
          return REMOVED.equals(message);
     }
    
     /**
      * Creates a new replaced event. This type of event is
      * triggered by a StratmasObject when it is replaced by another
      * object.
      *
      * @param source The source of the event e.g. the object that was
      * replaced.
      * @param initiator The initiator of the event.
      * @param newObject The object that has replaced the old object.
      *
      * @return The newly created event.
      */
     public static StratmasEvent getReplaced(Object source,
                                             Object initiator,
                                             StratmasObject newObject) {
          return new StratmasEvent(source, REPLACED, initiator, newObject);
     }
    
     /**
      * Checks if this is a Replaced event.
      *
      * @return true if this is a Replaced event, false
      * otherwise.
      */
     public boolean isReplaced() {
          return REPLACED.equals(message);
     }
    
     /**
      * Creates a new ChildChanged event. This type of event is
      * triggered by a StratmasComplex descendant when one of its
      * children has changed.
      *
      * @param source The source of the event, i.e. the
      * StratmasComplex descendant which child has changed.
      * @param initiator The initiator of the event.
      * @param changed The child that has changed.
      *
      * @return The newly created event.
      */
     public static StratmasEvent getChildChanged(Object source,
                                                 Object initiator,
                                                 StratmasObject changed) {
          return new StratmasEvent(source, CHILDCHANGED, initiator, changed);
     }
    
     /**
      * Checks if this is a ChildChanged event.
      *
      * @return true if this is a ChildChanged event, false
      * otherwise.
      */
     public boolean isChildChanged() {
          return CHILDCHANGED.equals(message);
     }
    
     /**
      * Creates a new SubscriptionHandled event. This type of event is
      * triggered by the Client when the XMLHandler has handled all
      * data that arrived from the server for a certain timestep.
      *
      * @param source The source of the event, i.e. the Client.
      * @param time The simulation time for which the handled data is
      * valid.
      *
      * @return The newly created event.
      */
     public static StratmasEvent getSubscriptionHandled(Object source, Timestamp time) {
          return new StratmasEvent(source, SUBSCRIPTIONHANDLED, null, time);
     }
    
     /**
      * Checks if this is a SubscriptionHandled event.
      *
      * @return true if this is a SubscriptionHandled event, false
      * otherwise.
      */
     public boolean isSubscriptionHandled() {
          return SUBSCRIPTIONHANDLED.equals(message);
     }
    
     /**
      * Creates a new ValueChanged event. This type of event is
      * triggered by a StratmasSimple descendant when its value
      * changes.
      *
      * @param source The source of the event, i.e. the StratmasObject
      * that has changed.
      * @param initiator The initiator of the event.
      *
      * @return The newly created event.
      */
     public static StratmasEvent getValueChanged(Object source, Object initiator) {
          return new StratmasEvent(source, VALUECHANGED, initiator, null);
     }
    
     /**
      * Checks if this is a ValueChanged event.
      *
      * @return true if this is a ValueChanged event, false
      * otherwise.
      */
     public boolean isValueChanged() {
          return VALUECHANGED.equals(message);
     }
    
     /**
      * Creates a new Selected event. This type of event is
      * triggered when a StratmasObject is selected. Currently,
      * selection of objects may only be performed through the
      * treeview.
      *
      * @param source The source of the event, i.e. the StratmasObject
      * that was selected.
      *
      * @return The newly created event.
      */
     public static StratmasEvent getSelected(Object source) {
          return new StratmasEvent(source, SELECTED);
     }

     /**
      * Checks if this is a Selected event.
      *
      * @return true if this is a Selected event, false otherwise.
      */
     public boolean isSelected() {
          return SELECTED.equals(message);
     }
    
     /**
      * Creates a new Unselected event. This type of event is
      * triggered when a StratmasObject is unselected. Currently,
      * unselection of objects may only be performed through the
      * treeview.
      *
      * @param source The source of the event, i.e. the StratmasObject
      * that was unselected.
      *
      * @return The newly created event.
      */
     public static StratmasEvent getUnselected(Object source) {
          return new StratmasEvent(source, UNSELECTED);
     }

     /**
      * Checks if this is a Unselected event.
      *
      * @return true if this is a Unselected event, false otherwise.
      */
     public boolean isUnselected() {
          return UNSELECTED.equals(message);
     }

    
    /**
     * Creates a RegionUpdated event. This event is triggered when the 
     * actual region showed in the map is updated. This happens when
     * a new Shape is added to/ removed from the region.
     *
     * @param source the source of the event i.e., where the event is
     *               triggered.
     * 
     */
    public static StratmasEvent getRegionUpdated(Object source) {
        return new StratmasEvent(source, REGIONUPDATED);
    }        

    /**
     * Checks if this is a RegionUpdated event.
     *
     * @return true if this is a RegionUpdated event, false otherwise.
     */
    public boolean isRegionUpdated() {
        return REGIONUPDATED.equals(message);
    }

    /**
     * Creates a GridUpdated event. This event is triggered when the
     * grid is updated with new values.
     *
     * @param source the source of the event i.e., where the event is
     *               triggered.
     */
    public static StratmasEvent getGridUpdated(Object source) {
        return new StratmasEvent(source, GRIDUPDATED);
    }        

    /**
     * Checks if this is a GridUpdated event.
     *
     * @return true if this is a GridUpdated event, false otherwise.
     */
    public boolean isGridUpdated() {
        return GRIDUPDATED.equals(message);
    }
    
    /**
     * Creates a GraticulesUpdated event. This event is triggered when the
     * graticules are updated.
     *
     * @param source the source of the event i.e., where the event is
     *               triggered.
     */
    public static StratmasEvent getGraticulesUpdated(Object source) {
        return new StratmasEvent(source, GRATICULESUPDATED);
    }        

    /**
     * Checks if this is a GraticulesUpdated event.
     *
     * @return true if this is a GraticulesUpdated event, false otherwise.
     */
    public boolean areGraticulesUpdated() {
        return GRATICULESUPDATED.equals(message);
    }
    
    /**
     * Creates a CoordSystemChanged event. This event is triggered when the
     * coordinate representation visible to the user is changed.
     *
     * @param source the source of the event i.e., where the event is
     *               triggered.
     */
    public static StratmasEvent getCoordSystemChanged(Object source) {
        return new StratmasEvent(source, COORDSYSTEMCHANGED);
    }        
    
    /**
     * Checks if this is a CoordSystemChanged event.
     *
     * @return true if this is a CoordSystemChangedUpdated event, false otherwise.
     */
    public boolean isCoordSystemChanged() {
        return COORDSYSTEMCHANGED.equals(message);
    }
    
     /**
      * Creates a new identifierChanged event. This type of event is
      * triggered by a StratmasObject when its identifier changes.
      *
      * @param source The source of the event e.g. the object which
      * identifier was changed. Notice that by the time this event is
      * triggered the identifier has already changed.
      * @param oldIdentifier The old identifier.
      *
      * @return The newly created event.
      */
     public static StratmasEvent getIdentifierChanged(Object source,
                                                      String oldIdentifier) {
          return new StratmasEvent(source, IDENTIFIERCHANGED, null, oldIdentifier);
     }
    
     /**
      * Checks if this is a Replaced event.
      *
      * @return true if this is a Replaced event, false
      * otherwise.
      */
     public boolean isIdentifierChanged() {
          return IDENTIFIERCHANGED.equals(message);
     }
    
    /**
     * The String representation of the object.
     *
     * @return the message of the event.
     */
    public String toString() {
        return message;
     }
}
