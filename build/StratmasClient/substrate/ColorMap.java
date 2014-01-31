package StratmasClient.substrate;

import java.text.DecimalFormat;
import java.lang.RuntimeException;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import javax.swing.border.EmptyBorder;

import StratmasClient.Debug;

/**
 * This is the color map used in the substrate editor.
 */
public class ColorMap {
    /**
     * The  number of color shades.
     */
    public static final int COLOR_SHADES = 256;
    /**
     * The color map table.
     */
    private final Color[] COLOR_TABLE = new Color[COLOR_SHADES];
    /**
     * The defined color maps.
     */
    public static final String[] COLOR_MAPS = {"blue-green-yellow-red", "blue-green-yellow", "blue-yellow-orange-red",
					       "white-lightgrey-cyan-black", "blue-white", "red-white", "black-white"};
    /**
     * The number of displayed values.
     */
    private int nrOfDisplayedValues;
    /**
     * The displayed values in this map.
     */
    private double[] displayedValues;
    /**
     * Name of the color table.
     */
    private String colorMapName;
    /**
     * The actual scale (logarithmic or linear).
     */
    private String actualScale;
    /**
     * The  minimum value represented by the color map.
     */
    private double minValue;
    /**
     * The  maximum value represented by the color map.
     */
    private double maxValue;
    /**
     * The panel which contains the color map. 
     */
    private JPanel colorMapPanel;
    /**
     * The actual value pointed with the mouse cursor.
     */
    private PointedValueLabel pointedValueLabel; 
    /**
     * The color map label.
     */
    private ColorMapLabel colorMapLabel;
    /**
     * The label for the paint ticks.
     */
    private TickLabel tickLabel;
    /**
     * The label for the color map values.
     */
    private ColorMapValuesLabel colorMapValuesLabel;
    /**
     * Reference to the color choser.
     */
    private ColorChooser colorChooser;
    
    /**
     * Creates new color map.
     *
     * @param colorChooser reference to the color selection object.
     * @param scale        the actual scale.
     * @param minVal       the actual lower bound.
     * @param maxVal       the actual upper bound.
     * @param cmap         name of the actual color map.
     */
    public ColorMap(ColorChooser colorChooser, String scale, double minVal, double maxVal, String cmap) {
	this.colorChooser = colorChooser;
	actualScale  = scale;
	minValue     = minVal;
	maxValue     = maxVal;
	
	// compute the number of displayed values
	setNrOfDisplayedValues();
	
	// compute the displayed values
	setDisplayedValues();
	
	// initialize color map table (256 different shades)
	selectColorMap(cmap);
	
	// create the color map label
	colorMapLabel = new ColorMapLabel(colorChooser, this);
	
	// create the tick label
	tickLabel = new TickLabel(nrOfDisplayedValues);
	
	// create the label for the displayed values
	colorMapValuesLabel = new ColorMapValuesLabel(this);
	
	// create the label for the pointed value
	pointedValueLabel = new PointedValueLabel();
	
	// create the color map panel
	colorMapPanel = createColorMapPanel();
    }
    
    /**
     * Creates the panel for the color map.
     */
    private JPanel createColorMapPanel() {
	// the color bar panel
	JPanel colorBarPanel = new JPanel(new BorderLayout());
	colorBarPanel.add(colorMapLabel, BorderLayout.CENTER);
	colorBarPanel.setBorder(BorderFactory.createRaisedBevelBorder());
	
	JPanel displayedValuesPanel = new JPanel(new BorderLayout());
	displayedValuesPanel.add(tickLabel, BorderLayout.NORTH);
	displayedValuesPanel.add(colorMapValuesLabel, BorderLayout.CENTER);
	displayedValuesPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
	
	JPanel panel = new JPanel(new GridLayout(3, 1, 0, 2));
	panel.add(pointedValueLabel);
	panel.add(colorBarPanel);
	panel.add(displayedValuesPanel);
	panel.setPreferredSize(new Dimension(300, 80));	
		
	return panel;
    }
    
    /**
     * Returns the color map panel.
     */
    public JPanel getPanel() {
	return colorMapPanel;
    }
    
    /**
     * Update color map table with a given combination of colors. Minimum number of colors is two. 
     * Transition between colors is computed by using Hermite interpolation with slope zero. 
     *
     * @param cmap array of colors (between 2 and 256).
     */
    public void setColorMap(Color[] cmap) {
	try {
	    //prepare x-scale for interpolation
	    int nrOfColors = cmap.length;
	    int[] xvalues = new int[nrOfColors];
	    int delx = COLOR_TABLE.length / (nrOfColors - 1);
	    for (int i = 0; i < nrOfColors; i++) {
		xvalues[i] = i * delx;
	    }
	    // adjust for last x value
	    xvalues[nrOfColors - 1] = COLOR_TABLE.length - 1;
	    // assign values to color map
	    for (int i = 0; i < COLOR_TABLE.length; i++) {
		int j = 0;
		while (!(i >= xvalues[j] && i <= xvalues[j + 1])) {
		    j++;
		}
		// Hermites algorithm
		float x = (float)i;
		float x1 = (float)xvalues[j];
		float x2 = (float)xvalues[j + 1];
		float[] ccmap1 = cmap[j].getRGBColorComponents(null);
		float[] ccmap2 = cmap[j + 1].getRGBColorComponents(null);
		float[] ccol = new float[3];
		for (int k = 0; k < 3; k++) {
		    float c1 = ccmap1[k];
		    float c2 = (ccmap2[k] - ccmap1[k]) / (x2 - x1);
		    float c3 = -c2 / ((float)Math.pow((x2 - x1), 2));
		    float c4 = c3;
		    float px = c1 + c2 * (x - x1) + (x - x1) * (x - x2) * (c3 * (x - x1) + c4 * (x - x2));
		    ccol[k] = (px > 1.0)? 1.0f :((px < 0.0f)? 0.0f : px);
		}
		COLOR_TABLE[i] = new Color(ccol[0], ccol[1], ccol[2]);
	    }
	    
	    update();
	}
	catch (RuntimeException e) {
	    e.printStackTrace();
	    Debug.err.println("ERROR : Color map update is not valid!");
	}
    }
    
    /**
     * Sets the name of the color map.
     */
    public void setColorMapName(String colorMapName) {
	this.colorMapName = colorMapName;
    }

    /**
     * Selects the actual color map. 
     *
     * @param cmapName name of the color map scale.
     */
    private void selectColorMap(String cmapName) {
	if (cmapName.equals("blue-green-yellow-red")) {
	    Color[] cmap = {Color.BLUE, Color.GREEN, Color.YELLOW, Color.RED};
	    setColorMap(cmap);
	    setColorMapName("blue-green-yellow-red");
	}
	else if (cmapName.equals("blue-green-yellow")) {
	    Color[] cmap = {Color.BLUE, Color.GREEN, Color.YELLOW};
	    setColorMap(cmap);
	    setColorMapName("blue-green-yellow");
	}
	else if (cmapName.equals("blue-yellow-orange-red")) {
	    Color[] cmap = {Color.BLUE, Color.YELLOW, Color.ORANGE, Color.RED};
	    setColorMap(cmap);
	    setColorMapName("blue-yellow-orange-red");
	}
	else if (cmapName.equals("white-lightgrey-cyan-black")) {
	    Color[] cmap = {Color.WHITE, Color.LIGHT_GRAY, Color.CYAN, Color.BLACK};
	    setColorMap(cmap);
	    setColorMapName("white-lightgrey-cyan-black");
	}
	else if (cmapName.equals("blue-white")) {
	    Color[] cmap = {Color.BLUE, Color.WHITE};
	    setColorMap(cmap);
	    setColorMapName("blue-white");
	}
	else if (cmapName.equals("red-white")) {
	    Color[] cmap = {Color.RED, Color.WHITE};
	    setColorMap(cmap);
	    setColorMapName("red-white");
	}
	else if (cmapName.equals("black-white")) {
	    Color[] cmap = {Color.BLACK, Color.WHITE};
	    setColorMap(cmap);
	    setColorMapName("black-white");
	}
    }
    
    /**
     * Computes the number of displayed values in the color map.
     */
    public void setNrOfDisplayedValues() {
	//linear scale
	if (isLinearScale()) {
	    nrOfDisplayedValues = 6;
	}
	// logarithmic scale
	else {
	    // get the max value 
	    int logMax = (int)Math.ceil(Math.log(maxValue) / Math.log(10));
	    // get the min value
	    int logMin;
	    if (minValue > 0) {
		logMin = (int)Math.floor(Math.log(minValue) / Math.log(10));
	    }
	    // special case for zero value
	    else {
		logMin = logMax - 5;
		if (logMin >= 0) {
		    logMin = -1;
		}
		else if (logMin < -10) {
		    logMin  = logMax - 1;
		}
	    }
	    nrOfDisplayedValues = logMax - logMin + 1; 
	}
    }
    
    /**
     * Returns the number of displayed values.
     */
    public int getNrOfDisplayedValues() {
	return nrOfDisplayedValues;
    }
    
    /**
     * Computes the values displayed in the color map.
     */
    public void setDisplayedValues() {
	displayedValues = new double[getNrOfDisplayedValues()];
	// linear scale
	if (isLinearScale()) {
	    double increment = (getMaxValue() - getMinValue()) / (getNrOfDisplayedValues() - 1);
	    for (int i = 0; i < getNrOfDisplayedValues(); i++) {
		displayedValues[i] = getMinValue() + i * increment;
	    }
	}
	// logarithmic scale
	else {
	    // get the max value 
	    double maxVal = Math.pow(10, Math.ceil(Math.log(getMaxValue()) / Math.log(10)));
	    // get the min value
	    double minVal = Math.pow(10, Math.ceil(Math.log(getMaxValue()) / Math.log(10)) - getNrOfDisplayedValues() + 1);
	    // all the values 
	    for (int i = 0; i < getNrOfDisplayedValues(); i++) {
		displayedValues[i] = minVal * Math.pow(10, i);
	    }
	}
    }
    
    /**
     * Returns the displayed values.
     */
    public double[] getDisplayedValues() {
	return displayedValues;
    }
    
    /**
     * Returns the actual color table. 
     */
    public Color[] getColorTable() {
	return COLOR_TABLE;
    }
    
    /**
     * Updates the color map panel.
     */
    public void update() {
	if (colorMapPanel != null) {
	    colorMapPanel.validate();
	    colorMapPanel.repaint();
	}
    }
    
    /**
     * Updates the color map values.
     *
     * @param scale  new scale.
     * @param minVal new lower bound.
     * @param maxVal new upper bound.
     * @param cmap   name of new color map.
     */
    public void update(String scale, double minVal, double maxVal, String cmap) {
	actualScale = scale;
	minValue    = minVal;
	maxValue    = maxVal;
	
	// update the number of displayed values
	setNrOfDisplayedValues();
	
	// update the displayed values
	setDisplayedValues();

	// update the color map table
	if (!colorMapName.equals(cmap)) {
	    selectColorMap(cmap);
	}
	
	// update the color map label
	colorMapLabel.update();
	
	// update the tick label
	tickLabel.setTickNumber(nrOfDisplayedValues);
	tickLabel.update();

	// update the label for the displayed values
	colorMapValuesLabel.update();
	
	// update the label for the pointed value
	pointedValueLabel.update();
	
	// update the color map 
	update();
    }
    
    /**
     * Returns the color for the given value. The color is computed according to the actual scale, bounds 
     * and color map.
     *
     * @param  value the given value.
     *
     * @return the color matching the value.
     */
    public Color getMappingColor(double value) {
	if (value <= minValue) {
	    return COLOR_TABLE[0];
	}
	else if (value >= maxValue) {
	    return COLOR_TABLE[COLOR_SHADES - 1];
	}
	// linear scale
	else if (isLinearScale()) {
	    return COLOR_TABLE[(int) Math.round((value - minValue) / (maxValue - minValue) * (COLOR_SHADES - 1))];
	}
	// logarithmic scale
	else {
	    double minVal = Math.log(displayedValues[0]) / Math.log(10);
	    double maxVal = Math.log(displayedValues[displayedValues.length - 1]) / Math.log(10);
	    double val    = (value <= 0)? Math.log(displayedValues[0]) / Math.log(10) : Math.log(value) / Math.log(10);
	    double res    = (val - minVal) / (maxVal - minVal);
	    return COLOR_TABLE[(int) Math.round((val - minVal) / (maxVal - minVal) * (COLOR_SHADES - 1))];   
	}
    }
    
    /**
     * Updates the label which shows the value pointed in the color map by the mouse cursor.
     *
     * @param value       the value pointed by the mouse cursor.
     * @param valPosition position of the mouse cursor.
     */
    public void updatePointedValue(Double value, int valPosition) {
	String valStr = (value == null)? "" : convertToString(value.doubleValue());
	pointedValueLabel.setValue(valStr, valPosition);
	pointedValueLabel.update();
    }
    
    /**
     * Sets the maximum value.
     */
    public void setMaxValue(double value) {
	maxValue = value;
    } 
    
    /**
     * Returns the maximum value.
     */
    public double getMaxValue() {
	return maxValue;
    }
    
    /**
     * Sets the minimum value. 
     */
    public void setMinValue(double value) {
	minValue = value;	
    }
    
    /**
     * Returns the minimum value. 
     */
    public double getMinValue() {
	return minValue;
    }
    
    /**
     * Returns true if the actual scale is linear.
     */
    public boolean isLinearScale() {
	return actualScale.equals("Linear Scale");
    }
    
    /**
     * Returns true if the actual scale is logarithmic.
     */
    public boolean isLogarithmicScale() {
	return actualScale.equals("Logarithmic Scale");
    }
    
    /**
     * Converts number to a String.
     *
     * @param value the number which is converted.
     */
    public String convertToString(double value) {
	return colorChooser.convertToString(value);
    }
    
}
