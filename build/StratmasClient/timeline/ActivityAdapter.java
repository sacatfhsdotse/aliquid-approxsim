package StratmasClient.timeline;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.Image;
import java.awt.image.ImageFilter;
import java.awt.image.FilteredImageSource;
import javax.swing.event.EventListenerList;
import java.util.EventListener;
import java.util.Hashtable;
import java.lang.ref.WeakReference;

import StratmasClient.Debug;
import StratmasClient.Icon;
import StratmasClient.IconFactory;
import StratmasClient.object.StratmasObjectFactory;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.StratmasEventListener;
import StratmasClient.object.StratmasTimestamp;
import StratmasClient.object.SymbolIDCode;
import StratmasClient.object.primitive.Timestamp;
import StratmasClient.filter.StratmasObjectAdapter;
import StratmasClient.filter.OrderColorFilter;
import StratmasClient.map.SymbolToTextureMapper;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.GLAutoDrawable;

/**
 * This adapter is used to adapt StratmasObject of type activity for visualizing in the timeline.
 *
 * @author Amir Filipovic
 */
public class ActivityAdapter implements StratmasObjectAdapter, StratmasEventListener {
    /**
     * The time pointed by the left arrow in the timeline.
     */
    private long leftArrowPointedTime;
    /**
     * The time pointed by the right arrow in the timeline.
     */
    private long rightArrowPointedTime;
    /**
     * Display list for this activity.
     */
    private int activityDisplayList;
    /**
     * Whether the display list is updated since last redraw.
     */
    private boolean displayListUpdated = false;
    /**
     * All display lists used by this adapter except the total display list.
     */
    private int displayLists[] = new int[3];
    /**
     * The position of the activity symbol in the display lists array.
     */
    private static final int ACTIVITY_POS = 0;
    /**
     * The position of the left arrow symbol in the display lists array.
     */
    private static final int LEFT_ARROW_POS = 1;
    /**
     * The position of the right arrow symbol in the display lists array.
     */
    private static final int RIGHT_ARROW_POS = 2;
    /**
     * The number of Render Selection names needed by this adapter.
     */
    private static final int NR_RENDER_SELECTION_NAMES = 6;
    /**
     * The counter assigning new renderSelectionNames.
     */
    private static int renderSelectionNameCounter = 1;
    /**
     * The render selection name of this adapter. This is used in RENDER_SELECTION mode.
     */
    private int renderSelectionName = -1;
    /**
     * Activity symbol scale.
     */
    private double symbolScale = 1.0d;
    /**
     * Scale of the left and the right arrows.
     */
    private double arrowScale = 0.5d;
    /**
     * Whether symbol is updated since last redraw.
     */
    private boolean symbolUpdated = false;
    /**
     * Whether the activity is selected in the tree view.
     */
    private boolean isSelected = false;
    /**
     * Whether location is updated since last redraw.
     */
    private boolean locationUpdated = false;
    /**
     * The StratmasObject this adapter adapts.
     */
    private StratmasObject activity;
    /**
     * The horizontal size of the symbol.
     */
    private static double horizontalSymbolSize = 20;
    /**
     * The vertical size of the symbol.
     */
    private static double verticalSymbolSize = 20;
    /**
     * The list of images for the left arrow.
     */
    public static Hashtable leftArrowSymbols = new Hashtable();
    /**
     * The list of images for the right arrow.
     */
    public static Hashtable rightArrowSymbols = new Hashtable();
    /**
     * The listeners of this adapter.
     */
    private EventListenerList eventListenerList = new EventListenerList();
 
    /**
     * Creates a new ActivityAdapter.
     *
     * @param activity the object to adapt.
     */
    public ActivityAdapter(StratmasObject activity) {
	this.setActivity(activity);
	this.setRenderSelectionName();
	setLeftArrowPointedTime(getStartTime());
	setRightArrowPointedTime(getEndTime());
    }
    
    /**
     * Sets the target of this adapter.
     *
     * @param activity target of the adapter.
     */   
    private void setActivity(StratmasObject activity) {
	this.activity = activity;
	getActivity().addEventListener(this);
    }
    
    /**
     * Sets the renderSelectionName of this adapter.
     */
    private void setRenderSelectionName() {
	renderSelectionName = renderSelectionNameCounter;
	// update the counter
	renderSelectionNameCounter +=  NR_RENDER_SELECTION_NAMES;
    }

    /**
     * Returns the renderSelectionName of this adapter.
     */
    public int getRenderSelectionName() {
	return renderSelectionName;
    }
    
    /**
     * Checks the renderSelectionName of the activity symbol.
     *
     * @param name the name to check.
     *
     * @return true if the name is the renderSelectionName of the activity symbol, 
     *         false otherwise.
     */
    public boolean isRenderSelectionName(int name) {
	return renderSelectionName == name;
    }

    /**
     * Returns the renderSelectionName of the left arrow.
     */
    public int getLeftArrowRenderSelectionName() {
	return renderSelectionName + 1;
    }
    
    /**
     * Checks the renderSelectionName of the left arrow symbol.
     *
     * @param name the name to check.
     *
     * @return true if the name is the renderSelectionName of the left arrow symbol, 
     *         false otherwise.
     */
    public boolean isLeftArrowRenderSelectionName(int name) {
	return (renderSelectionName + 1) == name;
    }
    
    /**
     * Returns the renderSelectionName of the right arrow.
     */
    public int getRightArrowRenderSelectionName() {
	return renderSelectionName + 2;
    }
    
    /**
     * Checks the renderSelectionName of the right arrow symbol.
     *
     * @param name the name to check.
     *
     * @return true if the name is the renderSelectionName of the right arrow symbol, 
     *         false otherwise.
     */
    public boolean isRightArrowRenderSelectionName(int name) {
	return (renderSelectionName + 2) == name;
    }
    
    /**
     * Updates (recreates) the display list that draws the entire
     * object.
     *
     * @param timelineActivityPanel the panel where this activity is displayed.
     * @param gl the gl drawable targeted.
     */
    protected void updateActivityDisplayList(TimelineActivityPanel timelineActivityPanel, GL gl) {
 	activityDisplayList = (gl.glIsList(activityDisplayList)) ? activityDisplayList : gl.glGenLists(1);
	//
	gl.glNewList(activityDisplayList, GL.GL_COMPILE);
	// get y-coordinate of the render position
	int y = timelineActivityPanel.getYCoordinate(this);
	// get x-coordinate of the render position of the left arrow
	int x1 = timelineActivityPanel.getXCoordinate(getLeftArrowPointedTime());
	// get x-coordinate of the render position of the right arrow
	int x2 = timelineActivityPanel.getXCoordinate(getRightArrowPointedTime());
	// get x-coordinate of the render position of the activity symbol
	int x = timelineActivityPanel.getXCoordinate((getLeftArrowPointedTime() + getRightArrowPointedTime())/2);
	// draw arrows only if both the start and the end times exist
	if (activity.getChild("end") != null) {
	    // left arrow display list
	    drawLeftArrow(gl, x1, y);
	    // right arrow display list
	    drawRightArrow(gl, x2, y);
	    // connect the left and the right arrow
	    drawConnectionLine(gl, x1, x2, y);
	}
	// activity symbol display list
	gl.glMatrixMode(GL.GL_PROJECTION);
	gl.glPushMatrix();
	// Pushes the name for RenderSelection mode.	
	gl.glPushName(getRenderSelectionName());
	gl.glTranslated(x, y, 0);
	gl.glCallList(displayLists[0]);
	gl.glPopName();
	gl.glMatrixMode(GL.GL_PROJECTION);
	gl.glPopMatrix();
	// boundary box
	if (isSelected()) {
	    drawBoundaryBox(gl, x1, x2, y);
	}
	gl.glEndList();
	displayListUpdated = true;
    }
    
    /**
     * Updates (recreates) the display lists that draws all the symbols of the object
     * this adapter represents.
     *
     * @param gld the gl drawable targeted.
     */
    protected void updateSymbolDisplayLists(GLAutoDrawable gld)
    {
	GL gl = gld.getGL();
	for (int i = ACTIVITY_POS; i <= RIGHT_ARROW_POS; i++) {
	    displayLists[i] = (gl.glIsList(displayLists[i]))? displayLists[i] : gl.glGenLists(1);
	}
	
	// create icons for the left and the right arrows
	Icon leftArrowIcon = new Icon(IconFactory.class.getResource("icons/left_arrow.png"));
	Icon rightArrowIcon = new Icon(IconFactory.class.getResource("icons/right_arrow.png"));
	if (hasResource()) {
	    StratmasObject mu = activity.getParent().getParent();
	    // create new icons
	    leftArrowIcon = getArrowIcon((SymbolIDCode)mu.getChild("symbolIDCode"), leftArrowIcon.getImage(), leftArrowSymbols);
	    rightArrowIcon = getArrowIcon((SymbolIDCode)mu.getChild("symbolIDCode"), rightArrowIcon.getImage(), rightArrowSymbols);
	}
	
	// get textures from texture mapper
	int[] textures = new int[3];
	textures[0] = SymbolToTextureMapper.getTexture(getActivity().getIcon(), gld);
	textures[1] = SymbolToTextureMapper.getTexture(leftArrowIcon, gld);
	textures[2] = SymbolToTextureMapper.getTexture(rightArrowIcon, gld);
	
	for (int i = ACTIVITY_POS; i <= RIGHT_ARROW_POS; i++) {
	    // Start list
	    gl.glNewList(displayLists[i], GL.GL_COMPILE);
	  
	    if (i == ACTIVITY_POS) {
		createSymbol(gl, textures[i], getSymbolScale(), getRenderSelectionName() + i, 1.0);
	    }
	    else {
		createSymbol(gl, textures[i], arrowScale, getRenderSelectionName() + i, 1.0);	
	    }
	    gl.glEndList();
	}
	symbolUpdated = true;
    }

    /**
     * Used to create symbols used in the adapter. The symbols are : the activity symbol,
     * the left arrow symbol and the right arrow symbol.
     *
     * @param gl intrface to OpenGL.
     * @param texture the texture of the symbol.
     * @param symbolScale the scale of the symbol.
     * @param renderSelectionName the reneder selection name of the symbol.
     * @param alpha the opacity of the symbol.
     */
    private void createSymbol(GL gl, int texture, double symbolScale, int renderSelectionName, 
			      double alpha) {
	// Enable textures.	
	gl.glEnable(GL.GL_TEXTURE_2D);
	gl.glBindTexture(GL.GL_TEXTURE_2D, texture);
	gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
	gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
	gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
	gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_NEAREST);
	gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, SymbolToTextureMapper.textureMode);
	
	// Pushes the name for RenderSelection mode.
	gl.glMatrixMode(GL.GL_MODELVIEW);
	gl.glPushMatrix();
	gl.glScaled(symbolScale, symbolScale, 1.0d);
	gl.glPushName(renderSelectionName);
	gl.glBegin(GL.GL_QUADS);
	gl.glColor4d(1.0d, 1.0d, 1.0d, alpha);
	gl.glTexCoord2f(0, 0);
	gl.glVertex2d(-horizontalSymbolSize / 2, -verticalSymbolSize / 2);
	gl.glTexCoord2f(0, 1);
	gl.glVertex2d(-horizontalSymbolSize / 2, verticalSymbolSize / 2);
	gl.glTexCoord2f(1, 1);
	gl.glVertex2d(horizontalSymbolSize / 2, verticalSymbolSize / 2);
	gl.glTexCoord2f(1, 0);
	gl.glVertex2d(horizontalSymbolSize / 2, -verticalSymbolSize / 2);
	gl.glEnd();
	gl.glPopName();
	gl.glPopMatrix();
	gl.glDisable(GL.GL_TEXTURE_2D);
    }

    /**
     * Draws the boundary box around the symbols representing the activity adapted
     * by this adapter.
     *
     * @param gl interface to OpenGL.
     * @param x1 x-coordinate of the left arrow.
     * @param x2 x-coordinate of the right arrow.
     * @param y y-coordinate of the symbols.
     */
    public void drawBoundaryBox(GL gl, int x1, int x2, int y) {
	gl.glMatrixMode(GL.GL_PROJECTION);
	gl.glPushMatrix();
	gl.glPushName(getRenderSelectionName() + 4);
	gl.glColor3d(1.0d, 0.0d, 0.0d);
	gl.glBegin(GL.GL_LINE_LOOP);
	gl.glVertex2d(x1 - getHorizontalSymbolSize() / 2, y - getVerticalSymbolSize() / 2);
	gl.glVertex2d(x1 - getHorizontalSymbolSize() / 2, y + getVerticalSymbolSize() / 2);
	gl.glVertex2d(x2 + getHorizontalSymbolSize() / 2, y + getVerticalSymbolSize() / 2);
	gl.glVertex2d(x2 + getHorizontalSymbolSize() / 2, y - getVerticalSymbolSize() / 2);
	gl.glEnd();
	gl.glMatrixMode(GL.GL_PROJECTION);
	gl.glPopName();
	gl.glPopMatrix();
    }
    
    /**
     * Used to draw the connection line between the arrows.
     *
     * @param gl interface to OpenGL.
     * @param x1 x-coordinate of the left arrow.
     * @param x2 x-coordinate of the right arrow.
     * @param y y-coordinate of the symbols.
     */
    public void drawConnectionLine(GL gl, int x1, int x2, int y) {
	gl.glMatrixMode(GL.GL_PROJECTION);
	gl.glPushMatrix();
	gl.glPushName(getRenderSelectionName() + 5);
	if (hasResource()) {
	    // get the color of the resource
	    StratmasObject mu = activity.getParent().getParent();
	    Color c = IconFactory.getSymbolBackgroundColor((SymbolIDCode)mu.getChild("symbolIDCode"));
	    float[] colors = c.getRGBColorComponents(null);
	    gl.glColor4f(colors[0], colors[1], colors[2], 0.5f);
	}
	else {
	    gl.glColor4f(0.3f, 0.3f, 0.3f, 0.5f);
	}
	gl.glBegin(GL.GL_LINES);
	gl.glVertex2d(x1, y);
	gl.glVertex2d(x2, y);
	gl.glEnd();
	gl.glMatrixMode(GL.GL_PROJECTION);
	gl.glPopName();
	gl.glPopMatrix();
    }

    /**
     * Used to draw the left arrow.
     *
     * @param gl interface to OpenGL.
     * @param x x-coordinate of the arrow.
     * @param y y-coordinate of the arrow.
     */
    public void drawLeftArrow(GL gl, int x, int y) {
	gl.glMatrixMode(GL.GL_PROJECTION);
	gl.glPushMatrix();
	// adjust x coordinate
	int xadj = (int)(x + horizontalSymbolSize * arrowScale / 2);
	gl.glTranslated(xadj, y, 0);
	gl.glCallList(displayLists[1]);
	gl.glMatrixMode(GL.GL_PROJECTION);
	gl.glPopMatrix();
    }
    
    /**
     * Used to draw the right arrow.
     *
     * @param gl interface to OpenGL.
     * @param x x-coordinate of the arrow.
     * @param y y-coordinate of the arrow.
     */
    public void drawRightArrow(GL gl, int x, int y) {
	gl.glMatrixMode(GL.GL_PROJECTION);
	gl.glPushMatrix();
	// adjust x coordinate
	int xadj = (int)(x - horizontalSymbolSize * arrowScale / 2);
	gl.glTranslated(xadj, y, 0);
	gl.glCallList(displayLists[2]);
	gl.glMatrixMode(GL.GL_PROJECTION);
	gl.glPopMatrix();
    }
    
    /**
     * Returns the icons of the left and the right arrows.
     * OBS. Potential memory leak - there is a risk that the keys and weak references in 
     * imageList list are not collected by the garbage collector. 
     *
     * @param code the id code of the activity resource.
     * @param src the dafault image of activity.
     * @param imageList the list of arrow images.
     */
    public Icon getArrowIcon(SymbolIDCode code, Image src, Hashtable imageList) {
	String key = (code == null || code.valueToString().length() < 2) ? "-" : code.valueToString().substring(1, 2);
	WeakReference reference = (WeakReference) imageList.get(key);	
	Icon icon = null;
	if (reference != null) {
	    icon = (Icon) reference.get();
	}
	if (icon != null) {
	    return icon;  
	}
	else {
	    // get the color of the resource
	    Color muColor = IconFactory.getSymbolBackgroundColor(code);
	    // set the color to the order
	    ImageFilter colorFilter = OrderColorFilter.getFilter(muColor);
	    Icon newIcon = new Icon(Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(src.getSource(),colorFilter)));
	    imageList.put(key, new WeakReference(newIcon));
	    return newIcon;
	}
    }
    
    /**
     * Returns true if the activity has a resource.
     */
    public boolean hasResource() {
	return activity.getParent().getParent().getType().canSubstitute("MilitaryUnit");
    }
    
    /**
     * Returns true if the activity symbol is updated, false otherwise.
     */
    public boolean isSymbolUpdated() {
	return symbolUpdated;
    }
    
    /**
     * Updates the state of the symbol.
     *
     * @param updated if true the symbol has been updated, if false
     *                the symbol has to be updated.
     */
    public void setSymbolUpdated(boolean updated) {
	symbolUpdated = updated;
    }
       
    /**
     * Returns true if the activity this adapter represents is selected,
     * otherwise false.
     */
    public boolean isSelected() {
	return isSelected;
    }
    
    /**
     * Updates the state of the activity.
     *
     * @param selected if true the activity is selected, if false
     *                the symbol is not selected.
     */
    public void setSelected(boolean selected) {
	isSelected = selected;
    } 
    
    /**
     * Returns the display list of this adapter.
     */   
    public int getActivityDisplayList() {
	return activityDisplayList;
    }

    /**
     * Returns the activity this adapter adapts.
     */
    public StratmasObject getActivity() {
	return activity;
    }
    
    /**
     * Returns the StratmasObject this adapter adapts.
     */
    public StratmasObject getStratmasObject() {
	return getActivity();
    }
    
    /**
     * Returns true if two Adapters represents the same object
     */
    public boolean equals(Object o) {
 	if (o instanceof ActivityAdapter) {
 	    return getActivity() == ((ActivityAdapter) o).getActivity();
 	}
 	return false;

    }
    
    /**
     * Sets the symbol scale of this ActivityAdapter to the specified value
     * (between 0.0 and 1.0 inclusive);
     *
     * @param symbolScale the new opacity.
     */
    public void setSymbolScale(double symbolScale) {
	if (this.symbolScale != symbolScale) {
	    this.symbolScale = symbolScale;
	    symbolUpdated = false;
	    fireActivityAdapterUpdated();
	}
    }
    
    /**
     * Returns the symbol scale of this adapter.
     */
    public double getSymbolScale() {
	return symbolScale;
    }
    
    /**
     * Returns the symbol horizontal size of the images.
     */
    public static double getHorizontalSymbolSize() {
	return horizontalSymbolSize;
    }

    /**
     * Returns the symbol vertical size of the images.
     */
    public static double getVerticalSymbolSize() {
	return verticalSymbolSize;
    }
    
    /**
     * Sets the time pointed by the left arrow. 
     */
    public void setLeftArrowPointedTime(long leftArrowPointedTime) {
	this.leftArrowPointedTime = leftArrowPointedTime;
    }
    
    /**
     * Returns the time pointed by the left arrow. 
     */
    public long getLeftArrowPointedTime() {
	return leftArrowPointedTime;
    }

    /**
     * Sets the time pointed by the right arrow. 
     */
    public void setRightArrowPointedTime(long rightArrowPointedTime) {
	this.rightArrowPointedTime = rightArrowPointedTime;
    }

    /**
     * Returns the time pointed by the right arrow. 
     */
    public long getRightArrowPointedTime() {
	return rightArrowPointedTime;
    }

    /**
     * Updates the start and the end times of the activity.
     */
    public void updateActivityTimes() {
	((StratmasTimestamp)getActivity().getChild("start")).setValue(new Timestamp(getLeftArrowPointedTime()), this);
	if (getActivity().getChild("end") != null) {
	    ((StratmasTimestamp)getActivity().getChild("end")).setValue(new Timestamp(getRightArrowPointedTime()), this);
	}
    }
    
    /**
     * Returns start time of the activity in milliseconds.
     */
    public long getStartTime() {
	return ((StratmasTimestamp)getActivity().getChild("start")).getValue().getMilliSecs();
    }
    
    /**
     * Returns end time of the activity in milliseconds.
     */
    public long getEndTime() {
	StratmasObject obj = getActivity();
	StratmasTimestamp t = (StratmasTimestamp)obj.getChild("end");
	long ret;
	if (t == null) {
	     ret = ((StratmasTimestamp)obj.getChild("start")).getValue().getMilliSecs();
	}
	else {
	     ret = t.getValue().getMilliSecs();
	}
	return ret;
    }

    /**
     * Called when the object this adapter adapts changes.
     *
     * @param event the event causing the call.
     */
    public void eventOccured(StratmasEvent event) {
	if (event.isChildChanged()) {
	    childChanged(event);
	} 
	else if (event.isRemoved()) {
	    getActivity().removeEventListener(this);
	    fireActivityRemoved();
	} 
	else if (event.isReplaced()) {
	    // UNTESTED - the replace code is untested 2005-09-22
	    Debug.err.println("FIXME - Replace behavior untested in ActivityAdapter");
	    getActivity().removeEventListener(this);
	    setActivity((StratmasObject)event.getArgument());
	    fireActivityAdapterUpdated();
	} 
	else if (event.isSelected()) {
	    fireActivitySelected(true);
	}
	else if (event.isUnselected()) {
	    fireActivitySelected(false);
	}
    }
    
    /**
     * Updates this adapter when one of the adapted objects children changes.
     *
     * @param event the event causing the change.
     */
    protected void childChanged(StratmasEvent event) {
	StratmasObject child = (StratmasObject) event.getArgument();
	if (child.getIdentifier().equals("start")) {
	    setLeftArrowPointedTime(getStartTime());
	    if (getActivity().getChild("end") == null) {
		setRightArrowPointedTime(getStartTime());
	    }
	    displayListUpdated = false;
	    fireActivityAdapterUpdated();
	}
	else if (child.getIdentifier().equals("end")) {
	    setRightArrowPointedTime(getEndTime());
	    displayListUpdated = false;
	    fireActivityAdapterUpdated();
	}
    }

    /**
     * Returns a list of the listeners of this object.
     */
    private EventListenerList getEventListenerList() {
	return eventListenerList;
    }

    /**
     * Adds an event listener for to the eventlistenerlist.
     *
     * @param listener the listener to add.
     */
    private void addEventListener(EventListener listener) {
	getEventListenerList().add(ActivityAdapterListener.class, listener);
    }

    /**
     * Removes an event listener for from the eventlistenerlist.
     *
     * @param listener the listener to add.
     */
    private void removeEventListener(EventListener listener) {
	this.getEventListenerList().remove(ActivityAdapterListener.class, listener);
    }
    
    /**
     * Adds a listener to the ActivityAdapter.
     *
     * @param listener the listener to add.
     */
    public void addActivityAdapterListener(ActivityAdapterListener listener) {
	this.addEventListener(listener);
    }

    /**
     * Removes a listener from the ActivityAdapter.
     *
     * @param listener the listener to add.
     */
    public void removeActivityAdapterListener(ActivityAdapterListener listener) {
	this.removeEventListener(listener);
    }
    
    /**
     * Called when an ActivityAdapters element is removed.
     */
    protected void fireActivityRemoved(){
	Object[] listeners = getEventListenerList().getListenerList();
        for (int i = listeners.length - 1; i >= 0; i--) {
            if (listeners[i] instanceof ActivityAdapterListener) {
		((ActivityAdapterListener) listeners[i]).activityAdapterRemoved(this);
            }
        }
    }

    /**
     * Called when an ActivityAdapters element is updated.
     */
    protected void fireActivityAdapterUpdated(){
	Object[] listeners = getEventListenerList().getListenerList();
        for (int i = listeners.length - 1; i >= 0; i--) {
            if (listeners[i] instanceof ActivityAdapterListener) {
		((ActivityAdapterListener) listeners[i]).activityAdapterUpdated(this);
            }
        }
    }

    /**
     * Called when an ActivityAdapters element is selected/unselected.
     */
    protected void fireActivitySelected(boolean selected){
	Object[] listeners = getEventListenerList().getListenerList();
        for (int i = listeners.length - 1; i >= 0; i--) {
            if (listeners[i] instanceof ActivityAdapterListener) {
		((ActivityAdapterListener) listeners[i]).activityAdapterSelected(this, selected);
            }
        }
    }
}
