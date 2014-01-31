package StratmasClient.map.graph;

import java.text.DecimalFormat;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Dimension;
import javax.swing.JLabel;

import StratmasClient.ProcessVariableDescription;

/**
 * This class is used to draw scale numbers on the y-axis in the graph.
 */
public class YScaleTextLabel extends JLabel{
    /**
     * The actual font.
     */
    private Font font;
    /**
     * The graph container.
     */
    private ProcessVariableXYGraph graph;
    /**
     * The format used for the values displayed on the y-axis when the scale is linear.
     */
    private DecimalFormat linearFormat;
    
    /**
     * Creates the label with default font.
     */
    public YScaleTextLabel(ProcessVariableXYGraph graph) {
	this.graph = graph;
	font = this.getFont().deriveFont(Font.PLAIN);
	setLinearFormat();
	setPreferredSize(new Dimension(10, this.getHeight()));
    }
    
    /**
     * Sets the font.
     */
    public void changeFont(Font font) {
	this.font = font.deriveFont(Font.PLAIN);
	this.repaint();
    }
    
    /**
     * Creates strings from the scale numbers.
     */
    private String[] getValueStrings() {
	double increment = (graph.getNrOfDisplayedYValues() - 1 > 0)? (graph.getUpperYBound() - graph.getLowerYBound()) / 
	    (graph.getNrOfDisplayedYValues() - 1) : 0;
	String[] values = new String[graph.getNrOfDisplayedYValues()];
	// linear scale
	if (graph.isLinearScale()) {
	    for (int i = 0; i < graph.getNrOfDisplayedYValues() - 1; i++) {
		values[i] = getLinFormat(graph.getLowerYBound() + i * increment);
	    }
	    values[graph.getNrOfDisplayedYValues() - 1] = getLinFormat(graph.getUpperYBound());
	}
	// logarithmic scale
	else {
	    // the lower bound is larger then zero
	    if (graph.getLowerYBound() > 0) {
		// special case - the difference between the bounds is one power of ten
		if ((int)Math.round(graph.log10(graph.getUpperYBound()) - graph.log10(graph.getLowerYBound())) == 1) {
		    values[0] = getLogFormat(graph.getLowerYBound());
		    for (int i = 0; i < graph.MIDDLE_LOG_VALUES.length; i++) {
			double val = graph.getLowerYBound() * graph.MIDDLE_LOG_VALUES[i];
			values[i + 1] = (val > 10)? Long.toString(Math.round(val)) : Double.toString(val);
		    }
		    values[values.length - 1] = getLogFormat(graph.getUpperYBound());
		}
		// all other cases
		else {
		    for (int i = 0; i < graph.getNrOfDisplayedYValues(); i++) {
			values[i] = getLogFormat(graph.getLowerYBound() * Math.pow(10, i));
		    }
		}
	    }
	    //  the lower bound is equal to zero
	    else {
		values[0] = Long.toString(0);
		for (int i = 1; i < graph.getNrOfDisplayedYValues(); i++) {
		    values[i] = getLogFormat(graph.getSecondLowerYBound() * Math.pow(10, i - 1));
		}	
	    }
	}
	return values;
    }
    
    /**
     * Sets the number format for the values displayed on the y-axis for linear scale.
     */
    public void setLinearFormat() {
	linearFormat = new DecimalFormat();
	ProcessVariableDescription processVariable = graph.getProcessVariable();
	double diff = Math.pow(10, Math.ceil(graph.log10(processVariable.getMax() - processVariable.getMin())));
	double yMargin = (diff < 100000)? diff / 100 : 1000;
	// set the number of decimals
	int decNr = (int) (-graph.log10(yMargin) + 3);
	linearFormat.setMaximumFractionDigits((decNr < 0)? 0 : decNr);
    }

    /**
     * This method formats the input value when the scale is linear.
     */
    private String getLinFormat(double value) {
	return linearFormat.format(value);
    }

    /**
     * This method formats the input value such that only the most significant digit is retained.
     */
    private String getLogFormat(double value) {
	DecimalFormat yScaleFormat = new DecimalFormat();
	if (value < 1) {
	    yScaleFormat.setMaximumFractionDigits((int)Math.round(-Math.log(value) / Math.log(10)));
	}
	else {
	    yScaleFormat.setMaximumFractionDigits(0);
	}
	return yScaleFormat.format(value);
    }
    
    /**
     * Draws the scale numbers.
     */
    protected void paintComponent(Graphics g) {
	// let UI delegate paint first 
	super.paintComponent(g); 
	// paint my contents next....
	String[] stringValues = getValueStrings();
	FontMetrics fm = g.getFontMetrics();
	int lastPosition;
	// set color
	g.setColor(Color.BLACK);
	// set font
	g.setFont(font);
	// draw the min value
	int XBASE = 0;
	int YBASE = this.getHeight();
	lastPosition = YBASE - font.getSize();
	g.drawString(stringValues[0], XBASE, YBASE);
	int labelWidth = fm.stringWidth(stringValues[0]);
	// draw the middle values
	// special case - the difference between the bounds is one power of ten (logarithmic scale) and
	// the lower bound is larger then zero
	if (graph.isLogarithmicScale() && graph.getLowerYBound() > 0 &&
	    (int)Math.round(graph.log10(graph.getUpperYBound()) - graph.log10(graph.getLowerYBound())) == 1) {
	    for (int i = 0; i < graph.MIDDLE_LOG_VALUES.length; i++) {
		YBASE = (int) (this.getHeight() - graph.log10(graph.MIDDLE_LOG_VALUES[i]) * this.getHeight() + font.getSize() / 2);
		if (lastPosition - YBASE >= font.getSize() &&  YBASE >= 2.5 * font.getSize()) {
		    g.drawString(stringValues[i + 1], XBASE, YBASE);
		    labelWidth = (fm.stringWidth(stringValues[i + 1]) > labelWidth)? fm.stringWidth(stringValues[i + 1]) : labelWidth;
		    lastPosition = YBASE - font.getSize();
		} 
	    }
	}
	// all the other cases
	else {
	    double incr = this.getHeight() * 1.0 / (graph.getNrOfDisplayedYValues() - 1);
	    for (int i = 1; i < stringValues.length - 1;i++) {
		YBASE = (int) (this.getHeight() - i * incr + font.getSize() / 2);
		if (lastPosition - YBASE >= font.getSize() &&  YBASE >= 2.5 * font.getSize()) {
		    g.drawString(stringValues[i], XBASE, YBASE);
		    labelWidth = (fm.stringWidth(stringValues[i]) > labelWidth)? fm.stringWidth(stringValues[i]) : labelWidth;
		    lastPosition = YBASE - font.getSize();
		} 
	    }
	}
	// draw the max value
	XBASE = 0;
	YBASE = font.getSize();
	if (lastPosition - YBASE >= font.getSize() / 2) {
	    g.drawString(stringValues[stringValues.length - 1], XBASE, YBASE);
	    labelWidth = (fm.stringWidth(stringValues[stringValues.length - 1]) > labelWidth)? 
		fm.stringWidth(stringValues[stringValues.length - 1]) : labelWidth;
	}
        // redraw
 	setPreferredSize(new Dimension(labelWidth, this.getHeight()));
 	this.invalidate();
 	graph.setYLabelUpdated();
    }
    
    /**
     * Updates the label.
     */ 
    public void update() {
	this.invalidate();
	this.repaint();
    }
}
