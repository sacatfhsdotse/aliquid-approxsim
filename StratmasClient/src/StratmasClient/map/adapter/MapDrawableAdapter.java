package StratmasClient.map.adapter;

import javax.swing.event.EventListenerList;
import java.util.EventListener;

import StratmasClient.object.Shape;
import StratmasClient.object.Line;
import StratmasClient.object.Point;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.StratmasEventListener;
import StratmasClient.filter.StratmasObjectAdapter;
import StratmasClient.map.Projection;

import java.io.UnsupportedEncodingException;

import javax.media.opengl.GLAutoDrawable;

/**
 * The adapter which adapts all StratmasObjects that can be drawn on the map. 
 *
 * @author Daniel Ahlin, Amir Filipovic
 */
public abstract class MapDrawableAdapter implements StratmasEventListener, StratmasObjectAdapter {
    /**
     * Display list for the object this adapter adapts.
     */
    protected int displayList;
    /**
     * Whether the display list is updated since last redraw.
     */
    protected boolean displayListUpdated = false;
    /**
     * The render selection name of this adapter. This is used in RENDER_SELECTION mode.
     */
    protected int renderSelectionName = -1;
    /**
     * The object this adapter adapts.
     */
    protected StratmasObject stComp;
    /**
     * The listeners of this adapter.
     */
    protected EventListenerList eventListenerList = new EventListenerList();
    
    /**
     * Creates MapDrawableAdapters suitable for the given object. This is the approved method of getting
     * MapDrawableAdapters...
     * 
     * @param mapDrawable the mapDrawable to adapt.
     */
    public static MapDrawableAdapter getMapDrawableAdapter(StratmasObject mapDrawable)
    {
        if (mapDrawable.getType().canSubstitute("MilitaryUnit")) {
            return new MilitaryUnitAdapter(mapDrawable);
        } else if (mapDrawable.getType().canSubstitute("Population")) {
            return new PopulationAdapter(mapDrawable);
        } else if (mapDrawable.getType().canSubstitute("Element")) {
            return new ElementAdapter(mapDrawable);
        } else if (mapDrawable.getType().canSubstitute("Activity")) {
            return new MapActivityAdapter(mapDrawable); 
        } else if (mapDrawable instanceof Shape) {
            return new MapShapeAdapter((Shape)mapDrawable); 
        } else if (mapDrawable instanceof Line) {
            return new MapLineAdapter((Line)mapDrawable); 
        } else if (mapDrawable instanceof Point) {
            return new MapPointAdapter((Point)mapDrawable); 
        } else {
            throw new AssertionError(MapDrawableAdapter.class.getName() + " can not be used for "+mapDrawable.getType().getName());
        }
    }
    
    /**
     * Creates a new adapter.
     *
     * @param stComp the object to adapt.
     */
    public MapDrawableAdapter(StratmasObject stComp) {
        this.setObject(stComp);
    }
    
    /**
     * Updates (recreates) the displayList that draws the entire drawable.
     *
     * @param proj the projection that maps lat and long into GL coordinates.
     * @param gld the gl drawable targeted.
     */
    protected abstract void updateDisplayList(Projection proj, GLAutoDrawable gld);
    

    /**
     * Sets the target of this adapter.
     */   
    protected void setObject(StratmasObject stComp) {
        this.stComp = stComp;
        getObject().addEventListener(this);
    }
    
    /**
     * Sets the renderSelectionName of this adapter
     * 
     * @param renderSelectionName
     */
    public void setRenderSelectionName(int renderSelectionName) {
        this.renderSelectionName = renderSelectionName;
    }
    
    /**
     * Returns the renderSelectionName of this adapter.
     */
    public int getRenderSelectionName() {
        return renderSelectionName;
    }

    /**
     * Returns the number of renderSelectionNames needed for this adapter.
     */
    public abstract int getNrOfRenderSelectionNames();
    
    /**
     * Returns true if the given name is the render selection name of this adapter.
     */
    public boolean isRenderSelectionName(int name) {
        return renderSelectionName == name;
    }

    /**
     * Returns the display list of this adapter.
     */   
    public int getDisplayList() {
        return displayList;
    }
    
    /**
     * Returns the object this adapter adapts.
     */
    public StratmasObject getObject() {
        return stComp;
    }
    
    /**
     * Updates the display lists
     */
    public abstract void reCompile(Projection proj, GLAutoDrawable gld);
    
    /**
     * Invalidates the display lists.
     */
    public void invalidateAllLists() {
        displayListUpdated = false;
        fireAdapterUpdated();
    }

    /**
     * Invalidates the display list.
     */
    public void invalidateDisplayList()
    {
        this.displayListUpdated = false;
        fireAdapterUpdated();
    }

    
    /**
     * Returns the StratmasObject this adapter adapts.
     */
    public StratmasObject getStratmasObject() {
        return getObject();
    }
    
    /**
     * Returns true if two adapters represents the same object.
     */
    public boolean equals(Object o) {
         if (o instanceof MapDrawableAdapter) {
             return getObject() == ((MapDrawableAdapter) o).getObject();
         }
         return false;
    }
    
    /**
     * Called when the object this adapter adapts changes.
     *
     * @param event the event causing the call.
     */
    public void eventOccured(StratmasEvent event) {
        if (event.isChildChanged()) {
            childChanged(event);
        } else if (event.isValueChanged()) {
            valueChanged(event);
        } else if (event.isRemoved()) {
            getObject().removeEventListener(this);
            fireAdapterRemoved();
        } else if (event.isReplaced()) {
            throw new AssertionError("Replace behavior not implemented");
         } 
    }
    
    /**
     * Updates this adapter when one of the adapted objects children changes.
     *
     * @param event the event causing the change.
     */
    protected void childChanged(StratmasEvent event) {
        displayListUpdated = false;
        fireAdapterUpdated();
    } 

    /**
     * Updates this adapter when the adapted object
     * changes. Ordinarily this means nothing, so override this
     * function if an implementing subclass cares.
     *
     * @param event the event causing the change.
     */
    protected void valueChanged(StratmasEvent event) {
    }
    
    /**
     * Returns a list of the listeners of this object.
     */
    private EventListenerList getEventListenerList() {
        return this.eventListenerList;
    }

    /**
     * Adds an event listener for to the eventlistenerlist.
     *
     * @param listener the listener to add.
     */
    private void addEventListener(EventListener listener) {
        this.getEventListenerList().add(MapDrawableAdapterListener.class, listener);
    }

    /**
     * Removes an event listener for from the eventlistenerlist.
     *
     * @param listener the listener to add.
     */
    private void removeEventListener(EventListener listener) {
        this.getEventListenerList().remove(MapDrawableAdapterListener.class, listener);
    }

    /**
     * Adds a listener to the StratmasElementAdapter.
     *
     * @param listener the listener to add.
     */
    public void addMapDrawableAdapterListener(MapDrawableAdapterListener listener) {
        this.addEventListener(listener);
    }

    /**
     * Removes a listener from the StratmasElementAdapter.
     *
     * @param listener the listener to add.
     */
    public void removeMapDrawableAdapterListener(MapDrawableAdapterListener listener) {
        this.removeEventListener(listener);
    }
    
    /**
     * Called when an MapDrawableAdapters object is removed.
     */
    protected void fireAdapterRemoved()
    {
        Object[] listeners = getEventListenerList().getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == MapDrawableAdapterListener.class) {
                ((MapDrawableAdapterListener) listeners[i + 1]).mapDrawableAdapterRemoved(this);
            }
        }
    }
    
    /**
     * Called when an MapDrawableAdapters object is updated.
     */
    protected void fireAdapterUpdated()
    {
        Object[] listeners = getEventListenerList().getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == MapDrawableAdapterListener.class) {
                ((MapDrawableAdapterListener) listeners[i + 1]).mapDrawableAdapterUpdated(this);
            }
        }
    }

    /**
     * Called when an MapDrawableAdapters object is updated (and needs to be redrawn).
     * 
     * @param obj the object added.
     */
    protected void fireAdapterChildAdded(StratmasObject obj)
    {
        Object[] listeners = getEventListenerList().getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == MapDrawableAdapterListener.class) {
                ((MapDrawableAdapterListener) listeners[i + 1]).mapDrawableAdapterChildAdded(obj);
            }
        }
    }

    /**
     * Utility function to get a glut compatible ID string for use in
     * GLUT text functions.
     */
    public String getGLUTIDString() throws UnsupportedEncodingException
    {
        byte[] buf = 
            getStratmasObject().getIdentifier().getBytes("ISO-8859-1");
        byte[] newBuf = new byte[2*buf.length];

        int writei = 0;
        boolean allCaps = true;
        for (int readi = 0; readi < buf.length; readi++) {
            if (buf[readi] >= 32 && buf[readi] <= 127) {
                if (buf[readi] >= 0x61 && buf[readi] <= 0x7a) {
                    allCaps = false;
                }
                newBuf[writei++] = buf[readi];
            } else if (buf[readi] == (0xe5 - 0x100)) {
                // å -> aa
                newBuf[writei++] = 0x61;
                newBuf[writei++] = 0x61;
                allCaps = false;
            } else if (buf[readi] == (0xc5 - 0x100)) {
                // Å -> Aa
                newBuf[writei++] = 0x41;
                newBuf[writei++] = 0x61;
            } else if (buf[readi] == (0xe4 - 0x100)) {
                // ä -> ae
                newBuf[writei++] = 0x61;
                newBuf[writei++] = 0x65;
                allCaps = false;
            } else if (buf[readi] == (0xc4 - 0x100)) {
                // Ä -> Ae
                newBuf[writei++] = 0x41;
                newBuf[writei++] = 0x65;
            } else if (buf[readi] == (0xf6 - 0x100)) {
                // ö -> oe
                newBuf[writei++] = 0x6f;
                newBuf[writei++] = 0x65;
                allCaps = false;
            } else if (buf[readi] == (0xd6 - 0x100)) {
                // Ö -> Oe
                newBuf[writei++] = 0x4f;
                newBuf[writei++] = 0x65;
            } else {
                // Non printable
                newBuf[writei++] = 95;
            }
        }

        if (allCaps) {
            // Should not happen that often, so lets do work all over
            // again.
            writei = 0;
            for (int readi = 0; readi < buf.length; readi++) {
                if (buf[readi] >= 32 && buf[readi] <= 127) {
                    newBuf[writei++] = buf[readi];
                } else if (buf[readi] == (0xc5 - 0x100)) {
                    // Å -> AA
                    newBuf[writei++] = 0x41;
                    newBuf[writei++] = 0x41;
                } else if (buf[readi] == (0xc4 - 0x100)) {
                    // Ä -> AE
                    newBuf[writei++] = 0x41;
                    newBuf[writei++] = 0x45;
                } else if (buf[readi] == (0xd6 - 0x100)) {
                    // Ö -> O
                    newBuf[writei++] = 0x4f;
                    newBuf[writei++] = 0x45;
                } else {
                    // Non printable
                    newBuf[writei++] = 95;
                }
            }
        }

        return new String(newBuf, 0, writei, "US-ASCII");
    }
}
