//         $Id: ElementAdapter.java,v 1.8 2006/09/04 18:57:30 amfi Exp $
/*
 * @(#)ElementAdapter.java
 */

package StratmasClient.map.adapter;

import java.util.Enumeration;
import StratmasClient.Debug;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasEventListener;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.StratmasBoolean;
import StratmasClient.object.StratmasList;
import StratmasClient.map.Projection;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

/**
 * ElementAdapter adapts StratmasObjects descendants of Elements for
 * viewing on a map window.
 *
 * @version 1, $Date: 2006/09/04 18:57:30 $
 * @author  Daniel Ahlin
*/
public class ElementAdapter extends MapElementAdapter {
    /**
     * Indicates if this element should be drawn even if not present.
     */
    boolean ignorePresent = true;

    /**
     * Creates a new ElementAdapter.
     *
     * @param element the Element to adapt.
     */
    protected ElementAdapter(StratmasObject element) {
        this(element, 0);
    }
    
    /**
     * Creates a new ElementAdapter.
     *
     * @param element the Element to adapt.
     * @param renderSelectionName the integer to use as the base for names in RENDER_SELECTION
     */
    protected ElementAdapter(StratmasObject element, int renderSelectionName) {
        super(element, renderSelectionName);
        
        // Set a listener on any lists that may generate new Elements.
        for (Enumeration<StratmasObject> e = element.children(); e.hasMoreElements();) {
            StratmasObject candidate = e.nextElement();
            if (candidate.getType().canSubstitute("Element")) {
                if (candidate instanceof StratmasList) {
                    // Add any current elements and add a listener
                    // that imports any consequent ones.
                    candidate.addEventListener(new StratmasEventListener() {
                            public void eventOccured(StratmasEvent subEvent) {
                                if (subEvent.isObjectAdded()) {
                                    fireAdapterChildAdded((StratmasObject) subEvent.getArgument());
                                } else if (subEvent.isRemoved()) {
                                    ((StratmasObject) subEvent.getSource()).removeEventListener(this);
                                } else if (subEvent.isReplaced()) {                        
                                    // UNTESTED - the replace code is untested 2005-09-22
                                    Debug.err.println("FIXME - Replace behavior untested in ElementAdapter1");
                                    ((StratmasObject)subEvent.getSource()).removeEventListener(this);
                                    ((StratmasObject)subEvent.getArgument()).addEventListener(this);
                                }
                                
                            }
                        });
                }
            }
        }

        // Add an adapter to listen to deployement changes.
        element.getChild("deployment").addEventListener(new StratmasEventListener() {
                public void eventOccured(StratmasEvent event) {
                    if (event.isRemoved()) {
                        ((StratmasObject) event.getSource()).removeEventListener(this);
                    } else if (event.isReplaced()) {
                        ((StratmasObject) event.getSource()).removeEventListener(this);
                        ((StratmasObject) event.getArgument()).addEventListener(this);
                        displayListUpdated = false;
                        isLocationUpdated = false;
                        fireAdapterUpdated();
                    } else if (event.isChildChanged()) {
                        displayListUpdated = false;
                        isLocationUpdated = false;
                        fireAdapterUpdated();
                    }                   
                }
            });
    }

    /**
     * Updates (recreates) the displayList that draws the entire
     * element.
     *
     * @param proj the projection that maps lat and long into GL coordinates.
     * @param gld the gl drawable targeted.
     */
    protected void updateDisplayList(Projection proj, GLAutoDrawable gld) {
        GL2 gl = (GL2) gld.getGL();
         this.displayList = (gl.glIsList(this.displayList)) ? this.displayList : gl.glGenLists(1);
        
        gl.glNewList(getDisplayList(), GL2.GL_COMPILE);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        // Pushes the name for RenderSelection mode.        
        gl.glPushName(getRenderSelectionName());
        // Set render position for consequent calls
        double projectedPosition[] = proj.projToXY(getLon(), getLat());
        gl.glTranslated(projectedPosition[0], projectedPosition[1], 0);
        // Call all sublists.
        if (isPresent() ||
            getIgnorePresent() || 
            isSelected()) {
            gl.glCallLists(displayListsBuf.capacity(), GL2.GL_INT, displayListsBuf);
        }
        gl.glPopName();
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPopMatrix();
        gl.glEndList();
        displayListUpdated = true;
    }

    /**
     * Called when the Element this adapter adapts changes.
     *
     * @param event the event causing the call.
     */
    public void eventOccured(StratmasEvent event) {
        super.eventOccured(event);
        //
        if (event.isObjectAdded() && 
            ((StratmasObject) event.getArgument()).getType().canSubstitute("Element")) {
            if (event.getArgument() instanceof StratmasList) {
                // A list which may generate new Elements has been added. Will have to listen to that.
                ((StratmasObject) event.getArgument()).addEventListener(new StratmasEventListener() {
                        public void eventOccured(StratmasEvent subEvent) {
                            if (subEvent.isObjectAdded()) {
                                fireAdapterChildAdded((StratmasObject) subEvent.getArgument());
                            } else if (subEvent.isRemoved()) {
                                ((StratmasObject) subEvent.getSource()).removeEventListener(this);
                            } else if (subEvent.isReplaced()) {
                                // UNTESTED - the replace code is untested 2005-09-22
                                Debug.err.println("FIXME - Replace behavior untested in ElementAdapter2");
                                ((StratmasObject)subEvent.getSource()).removeEventListener(this);
                                ((StratmasObject)subEvent.getArgument()).addEventListener(this);
                            }                            
                        }
                    });
            } else {
                // Ordinary Element added.
                fireAdapterChildAdded((StratmasObject) event.getArgument());
            }
        }
    }
    
    /**
     * Updates this adapter when one of the adapted elements children changes.
     *
     * @param event the event causing the change.
     */
    protected void childChanged(StratmasEvent event) {
        super.childChanged(event);
        //
        StratmasObject child = (StratmasObject) event.getArgument();
        if (child.getIdentifier().equals("present")) {
            displayListUpdated = false;
            fireAdapterUpdated();
        }
    }

    /**
     * Make adapter adapter always draw element, present or not
     *
     * @param flag true if always draw.
     */
    public void setIgnorePresent(boolean flag) {
        if (flag != getIgnorePresent()) {
            this.ignorePresent = flag;
            displayListUpdated = false;
            fireAdapterUpdated();
        }
    }

    /**
     * Returns true if  adapter always draw element, present or not
     */
    protected boolean getIgnorePresent() {
        return this.ignorePresent;
    }
    
    /**
     * Returns true if the element adapted is present, else false.
     */
    protected boolean isPresent() {
        return ((StratmasBoolean) getStratmasObject().getChild("present")).getValue();
    }

}

