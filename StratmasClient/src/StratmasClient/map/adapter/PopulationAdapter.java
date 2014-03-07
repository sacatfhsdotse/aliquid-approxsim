//         $Id: PopulationAdapter.java,v 1.14 2006/10/02 12:22:24 dah Exp $
/*
 * @(#)PopulationAdapter.java
 */

package StratmasClient.map.adapter;

import java.util.Enumeration;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasEventListener;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.StratmasDecimal;
import StratmasClient.Debug;

import java.io.UnsupportedEncodingException;

import StratmasClient.filter.StratmasObjectFilter;

import StratmasClient.map.Projection;

import javax.media.opengl.GL2;
import com.jogamp.opengl.util.gl2.GLUT;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLUtessellatorCallbackAdapter;
import javax.media.opengl.glu.GLUtessellatorCallback;
import com.jogamp.common.nio.Buffers;

import java.nio.DoubleBuffer;

/**
 * PopulationAdapter adapts StratmasObjects descendants of Population for
 * viewing on a map window.
 *
 * @version 1, $Date: 2006/10/02 12:22:24 $
 * @author  Daniel Ahlin
*/
public class PopulationAdapter extends ElementAdapter
{
    /**
     * The current scale of the population
     */
    private double inhabitantsScale;
    
    /**
     * The total number of inhabitants.
     */
    private double totalInhabitants;

    /**
     * The different scale levels indicating inhabitants.
     */
    static double[] inhabitantsScaleMarks = {0, 100000, 500000, 1000000};

    /**
     * The listener used to notify about inhabitants changes
     */
    StratmasEventListener inhabitantsListener = null;

    /**
     * The whether to draw name of element under symbol.
     */
    boolean drawElementName = true;

    /**
     * Creates a new ElementAdapter.
     *
     * @param element the Element to adapt.
     */
    protected PopulationAdapter(StratmasObject element)
    {
        super(element);
        resetInhabitantListener();
        
        // Register a listener for the populationgroup list.
        if (element.getChild("ethnicGroups") != null) {
            element.getChild("ethnicGroups").addEventListener(new StratmasEventListener() 
                {
                    public void eventOccured(StratmasEvent event)
                    {
                        if (event.isObjectAdded()) {
                            resetInhabitantListener();
                        } else if (event.isRemoved()) {
                            ((StratmasObject) event.getSource()).removeEventListener(this);
                            resetInhabitantListener();
                        } else if (event.isReplaced()) {
                             throw new AssertionError("Replace behavior not implemented");
                        } 
                    }
                });
        }
    }

    /**
     * Creates a new ElementAdapter.
     *
     * @param element the Element to adapt.
     * @param renderSelectionName the integer to use as the base for names in RENDER_SELECTION
     */
    protected PopulationAdapter(StratmasObject element, int renderSelectionName)
    {
        super(element, renderSelectionName);
        resetInhabitantListener();

        // Register a listener for the populationgroup list.
        if (element.getChild("ethnicGroups") != null) {
            element.getChild("ethnicGroups").addEventListener(new StratmasEventListener() 
                {
                    public void eventOccured(StratmasEvent event)
                    {
                        if (event.isObjectAdded()) {
                            resetInhabitantListener();
                        } else if (event.isRemoved()) {
                            ((StratmasObject) event.getSource()).removeEventListener(this);
                            resetInhabitantListener();
                        } else if (event.isReplaced()) {
                             throw new AssertionError("Replace behavior not implemented");
                        } 
                    }
                });
        }
    }

    /**
     * Called when the Element this adapter adapts changes. Overriden
     * to handle changes removal and addition of the ethnicGroup list.
     *
     * @param event the event causing the call.
     */
    public void eventOccured(StratmasEvent event)
    {
        super.eventOccured(event);
        if (event.isObjectAdded() && 
            ((StratmasObject) event.getArgument()).getType().canSubstitute("Element")) {
            fireAdapterChildAdded((StratmasObject) event.getArgument());
        }
    }

    /**
     * Updates (recreates) the displayList that draws the symbol of the element
     * this adapter represents.
     * @param proj the projection that maps lat and long into GL coordinates.
     * @param gld the gl drawable targeted.
     */
    protected void updateSymbolDisplayList(Projection proj, GLAutoDrawable gld)
    {
        GL2 gl = (GL2) gld.getGL();
         displayListsBuf.put(SYMBOL_POS, 
                            (gl.glIsList(displayListsBuf.get(SYMBOL_POS))) ?
                            displayListsBuf.get(SYMBOL_POS) : gl.glGenLists(1));

        // Draw a square proportional to the city size
        // Start list
        gl.glNewList(displayListsBuf.get(SYMBOL_POS), GL2.GL_COMPILE);
        
        // Pushes the name for RenderSelection mode.
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();

        double scale = getSymbolScale();
        if (getInvariantSymbolSize()) {
            gl.glMatrixMode(GL2.GL_PROJECTION);
            DoubleBuffer buf = Buffers.newDirectDoubleBuffer(16);
            gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, buf);
            scale = getSymbolScale()*0.000003d/buf.get(0);
        }

         gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glScaled(scale, scale, 1.0d);
        
        gl.glScaled(0.5 * inhabitantsScale, 
                    0.5 * inhabitantsScale, 
                    0.5 * inhabitantsScale);
        gl.glPushName(getRenderSelectionName() + 1 + SYMBOL_POS);
        gl.glBegin(GL2.GL_QUADS);
        gl.glColor4d(0.0d, 0.0d, 0.0d, getSymbolOpacity());
         gl.glVertex2d(-horizontalSymbolSize/2, -verticalSymbolSize/2);
         gl.glVertex2d(-horizontalSymbolSize/2, verticalSymbolSize/2);
         gl.glVertex2d(horizontalSymbolSize/2, verticalSymbolSize/2);
         gl.glVertex2d(horizontalSymbolSize/2, -verticalSymbolSize/2);
        gl.glEnd();
        gl.glPopMatrix();
        if (drawElementName()) {
            GLUT glut = new GLUT();
            // Draw name of element. The constants below are the unit
            // sizes of GLUTs Stroke fonts. This will draw the name of
            // the city on one line below the symbol in a height
            // 1/10th of the symbolsize.
            // Wash string, replace unknowns with '_';
            String str = "";
            try {
                str = getGLUTIDString();
            } catch (UnsupportedEncodingException ex) {
                Debug.err.println(ex.toString());
            }

            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glPushMatrix();
            // Draw below marker at a tenth of the vertical size of the
            // symbol.
            double textScale = getVerticalSymbolSize()/(5*(119.05 + 33.33));
            gl.glTranslated(-104.76 * textScale * str.length()/2, 
                            -(119.05 * textScale + 2 + 0.5 * inhabitantsScale * getVerticalSymbolSize()/ 2) , 
                            0);
            gl.glScaled(textScale, textScale , 1.0);

            // Draw in black if unselected, else red.
            if (isSelected()) {
                gl.glColor4dv(SELECTION_COLOR, 0);
            } else {
                gl.glColor4d(0.0d, 0.0d, 0.0d, 1.0d);
            }
            glut.glutStrokeString(GLUT.STROKE_MONO_ROMAN, str);

            gl.glPopMatrix();
        }    
        gl.glPopName();
        gl.glPopMatrix();
        gl.glEndList();
        isSymbolUpdated = true;
    }

    /**
     * Returns true if the name of this population should be drawn along with the symbol.
     */
    public boolean drawElementName() 
    {
        return this.drawElementName;
    }

    /**
     * Set whether the name of this population should be drawn along
     * with the symbol.
     *
     * @param flag true if name should be drawn
     */
    public void setDrawElementName(boolean flag) 
    {
        if (this.drawElementName() != flag) {
            this.drawElementName = flag;
            isSymbolUpdated = false;
            fireAdapterUpdated();
        }
    }

    /**
     * Make adapter changes caused by selection state changes. This is
     * protected for a reason, DO NOT USE THIS to set the selection
     * status of the element adapted, use getElement() and make the
     * changes on the object instead.
     *
     * @param selected true if it's selected.
     */
    public void setSelected(boolean selected) 
    {
        if (this.selected != selected) {
            this.selected = selected;
            isSelectionMarkerUpdated = false;
            isSymbolUpdated = false;
            fireAdapterUpdated();
        }
    }

    /**
     * Updates (recreates) the displayList that draws the selection
     * frame if the element this adapter represents is
     * selected. Overridden to account for differing symbolsize of
     * PopulationAdapter.
     *
     * @param proj the projection that maps lat and long into GL coordinates.
     * @param gld the gl drawable targeted.
     */
    protected void updateSelectionMarkerDisplayList(Projection proj, GLAutoDrawable gld)
    {
         GL2 gl = (GL2) gld.getGL();
         displayListsBuf.put(SELECTION_MARKER_POS, 
                            (gl.glIsList(displayListsBuf.get(SELECTION_MARKER_POS))) ?
                            displayListsBuf.get(SELECTION_MARKER_POS) : gl.glGenLists(1));
        
        gl.glNewList(displayListsBuf.get(SELECTION_MARKER_POS), GL2.GL_COMPILE);
        // Pushes the name for RenderSelection mode.
        gl.glPushName(getRenderSelectionName() + 1 + SELECTION_MARKER_POS);
        if (isSelected()) {
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glPushMatrix();
            gl.glScaled(getSymbolScale(), getSymbolScale(), getSymbolScale());
            gl.glScaled(0.5 * inhabitantsScale, 
                        0.5 * inhabitantsScale, 
                        0.5 * inhabitantsScale);
            gl.glBegin(GL2.GL_LINE_LOOP);
            gl.glColor4dv(SELECTION_COLOR, 0); 
            gl.glVertex2d(-(horizontalSymbolSize/2 + 1), -(verticalSymbolSize/2 + 1));
            gl.glVertex2d(-(horizontalSymbolSize/2 + 1), (verticalSymbolSize/2 + 1));
            gl.glVertex2d((horizontalSymbolSize/2 + 1), (verticalSymbolSize/2 + 1));
            gl.glVertex2d((horizontalSymbolSize/2 + 1), -(verticalSymbolSize/2 + 1));
            gl.glEnd();
            gl.glPopMatrix();
        }
        gl.glPopName();
        gl.glEndList();
         isSelectionMarkerUpdated = true;
    }

    /**
     * Recalculates the inhabitants scale.
     */
    public void updateInhabitantsScale()
    {
        // Find all inhabitants
        StratmasObjectFilter filter = new StratmasObjectFilter()
            {
                /**
                 * Returns true for any object called inhabitants that
                 * are of type Double.
                 */                
                public boolean pass(StratmasObject obj) 
                {
                    return obj.getIdentifier().equals("inhabitants") &&
                        obj.getType().canSubstitute("Double");
                }
            };

        // Sum inhabitants
        double total = 0.0;
        for (Enumeration e = filter.filterTree(getStratmasObject()); e.hasMoreElements();) {
            StratmasDecimal dec = (StratmasDecimal) e.nextElement();
            total += dec.getValue();
        }

        setTotalInhabitants(total);

        // Scale to discrete sizes.
        int scale = 0;
        while (scale < inhabitantsScaleMarks.length && 
               total > inhabitantsScaleMarks[scale]) {
            scale++;
        }

        inhabitantsScale = ((double) scale) / ((double) inhabitantsScaleMarks.length);

        isSymbolUpdated = false;
        isSelectionMarkerUpdated = false;
        isLocationUpdated = false;
        fireAdapterUpdated();
    }

    /**
     * Returns the currentTotal of inhabitants.
     */
    public double getTotalInhabitants()
    {
        return this.totalInhabitants;
    }

    /**
     * Returns the currentTotal of inhabitants.
     */
    private void setTotalInhabitants(double totalInhabitants)
    {
        this.totalInhabitants = totalInhabitants;
    }
    
    /**
     * Sets listener on all children of this element that are called
     * inhabitants and are of type Double.
     */
    synchronized protected void resetInhabitantListener()
    {
        if (inhabitantsListener == null) {
            final PopulationAdapter self = this;
            this.inhabitantsListener = new StratmasEventListener()
                {
                    /**
                     * Called when the Element this adapter adapts changes.
                     *
                     * @param event the event causing the call.
                     */
                    public void eventOccured(StratmasEvent event)
                    {
                        if (event.isValueChanged()) {
                            self.updateInhabitantsScale();
                        } else if (event.isRemoved()) {
                            getObject().removeEventListener(this);
                            self.updateInhabitantsScale();
                        } else if (event.isReplaced()) {
                             throw new AssertionError("Replace behavior not implemented");
                        } 
                        
                    }
                };
        }

        // Find all inhabitants
        StratmasObjectFilter filter = new StratmasObjectFilter()
            {
                /**
                 * Returns true for any object called inhabitants that
                 * are of type Double.
                 */                
                public boolean pass(StratmasObject obj) 
                {
                    return obj.getIdentifier().equals("inhabitants") &&
                        obj.getType().canSubstitute("Double");
                }
            };
        for (Enumeration e = filter.filterTree(getStratmasObject()); e.hasMoreElements();) {
            StratmasDecimal dec = (StratmasDecimal) e.nextElement();
            // Lazily making sure we are not alread listening
            dec.removeEventListener(this.inhabitantsListener);
            dec.addEventListener(this.inhabitantsListener);
        }
        
        updateInhabitantsScale();
    }

            /**
     * Returns the tessellator callback to use for this ElementAdapter.
     *
     * @param gld the glDrawable context to use.
     */
    protected GLUtessellatorCallback getLocationTessellatorCallback(GLAutoDrawable gld)
    {
        final GL2 gl = (GL2) gld.getGL();
        
        final double[] color = getLocationColor(getTotalInhabitants());
        return new GLUtessellatorCallbackAdapter() 
            {
                public void vertex(Object data) 
                {
                    double[] p = (double[]) data;
                    gl.glColor4d(1.0d, 1.0d, 1.0d, 0.0d);
                    gl.glVertex2dv(p, 0);
                }
                public void begin(int type)
                {
                    gl.glBegin(type);
                }
                public void end() 
                {
                    gl.glEnd();
                }
            };
    }

    /**
     * Maps inhabitants to a color. Clamps inhabitants to [0, 50000000].
     *
     * @param inhabitants the number of inhabitants
     */
    public static double[] getLocationColor(double inhabitants)
    {
        double min = 0.0;
        double max = 50000000.0;
        
        inhabitants = inhabitants >= min ? inhabitants : min;
        inhabitants = inhabitants <= max ? inhabitants : max;
        
        // Map interval to 2*pi
        double mappedValue = inhabitants * (2*Math.PI/(max - min));
        
        return new double[] {mappedValue > Math.PI ? Math.cos(mappedValue - Math.PI) : 0,
                             mappedValue > Math.PI / 2 && mappedValue < 1.5 * Math.PI ? Math.cos(mappedValue - Math.PI / 2) : 0,
                             mappedValue < Math.PI ? Math.cos(mappedValue) : 0};
    }
}
