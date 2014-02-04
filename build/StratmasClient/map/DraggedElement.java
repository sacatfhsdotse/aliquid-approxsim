package StratmasClient.map;

import StratmasClient.object.StratmasObject;

/**
 * This class is used for the dragged object in Drag'n'drop action. It's a workaround to be able to 
 * obtain the dragged object before it's dropped (ver. 1.4.2).
 * 
 */
public class DraggedElement {
    /**
     * The dragged element.
     */
    private static StratmasObject draggedElement;
    
    /**
     * Sets the dragged element.
     */
    public static void setElement(StratmasObject element) {
        draggedElement = element;
    }
    
    /**
     * Returns the dragged element.
     */
    public static StratmasObject getElement() {
        return draggedElement;
    }
}
