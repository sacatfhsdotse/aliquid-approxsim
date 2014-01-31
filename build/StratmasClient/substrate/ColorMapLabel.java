package StratmasClient.substrate;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JLabel;

/**
 * This class is used to paint the color map. 
 */
public class ColorMapLabel extends JLabel implements MouseListener, MouseMotionListener{
    /**
     * Reference to the color chooser.
     */
    private ColorChooser colorChooser;
    /**
     * Reference to the color map.
     */
    private ColorMap colorMap;
    
    /**
     * Creates the label.
     */
    public ColorMapLabel(ColorChooser colorChooser, ColorMap colorMap) {
	this.colorChooser = colorChooser;
	this.colorMap     = colorMap;
	addMouseListener(this);
	addMouseMotionListener(this);
	setPreferredSize(new Dimension(300, 20));
    }
    
    /**
     * Updates the ColorChooser with the value curenly pointed by the mouse cursor.
     */
    public void mouseMoved(MouseEvent event) {
	double pointedValue = convertXToValue(event.getX());
	colorMap.updatePointedValue(new Double(pointedValue), event.getX());
    }
    
    /**
     * Not implemented.
     */
    public void mouseDragged(MouseEvent event) {}
    
    /**
     * Updates the selected value in the ColorChooser.
     */
    public void mouseClicked(MouseEvent event) {
	double pointedValue = convertXToValue(event.getX());
	colorChooser.updateActualValue(pointedValue, true);
    }
    
    /**
     * Not implemented.
     */
    public void mouseEntered(MouseEvent event) {}
    
    /**
     * Updates the ColorChooser such that the label showing the value pointing by the mouse
     * cursor is empty.
     */
    public void mouseExited(MouseEvent event) {
	colorMap.updatePointedValue(null, 0);	
    }
    
    /**
     * Not implemented.
     */
    public void mousePressed(MouseEvent event) {}
    
    /**
     * Not implemented.
     */
    public void mouseReleased(MouseEvent event) {}
    
    /**
     * Converts the x coordinate to the value represented by the color map.
     */
    private double convertXToValue(double x) {
	double pointedValue = 0;
	double[] displayedValues = colorMap.getDisplayedValues();
	double minVal = displayedValues[0];
	double maxVal = displayedValues[displayedValues.length - 1];
	// linear scale
	if (colorMap.isLinearScale()) {
	    pointedValue = x * (maxVal - minVal) / this.getWidth() + minVal;
	}
	// logarithmic scale
	else {
	    double maxValue = Math.log(maxVal) / Math.log(10);
	    double minValue = Math.log(minVal) / Math.log(10);
	    pointedValue = Math.pow(10, x *(maxValue - minValue) / this.getWidth() + minValue);
	}
	return (x == 0)? colorMap.getMinValue() : ((x == this.getWidth() - 1)? maxVal : pointedValue);	
    }
    
    /**
     * Draws the color map label.
     */
    protected void paintComponent(Graphics g) {
	// let UI delegate paint first 
	super.paintComponent(g); 
	
	// draw color map
	Color[] colors = colorMap.getColorTable();
	double cellWidth = ((double)this.getWidth()) / colors.length;
	int cellWidthInt = (int)Math.ceil(cellWidth);
	for (int i = 0; i < colors.length; i++) {
	    // set color
	    g.setColor(colors[i]);
	    // fill the area 
	    int x = (int)Math.floor(i * cellWidth);
	    g.fillRect(x, 0, cellWidthInt, this.getHeight());
	} 
    }
    
    /**
     * Updates this label.
     */
    public void update() {
	validate();
	repaint();
    }
}
