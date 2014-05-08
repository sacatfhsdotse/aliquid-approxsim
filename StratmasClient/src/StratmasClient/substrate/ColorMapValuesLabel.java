package StratmasClient.substrate;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import javax.swing.JLabel;

/**
 * This class is used to draw the color map values.
 */
public class ColorMapValuesLabel extends JLabel {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1017064844658174028L;
    /**
     * The color map.
     */
    private ColorMap colorMap;

    /**
     * Creates the label.
     */
    public ColorMapValuesLabel(ColorMap colorMap) {
        this.colorMap = colorMap;
        setFont(this.getFont().deriveFont(Font.PLAIN));
    }

    /**
     * Creates strings from the scale numbers.
     */
    private String[] getValueStrings() {
        double[] displayedValues = colorMap.getDisplayedValues();
        String[] values = new String[colorMap.getNrOfDisplayedValues()];
        // separate treatment for the first value
        if (colorMap.isLogarithmicScale() && colorMap.getMinValue() == 0) {
            values[0] = new String("0");
        } else {
            values[0] = colorMap.convertToString(displayedValues[0]);
        }
        // all the values but the first
        for (int i = 1; i < colorMap.getNrOfDisplayedValues(); i++) {
            values[i] = colorMap.convertToString(displayedValues[i]);
        }

        return values;
    }

    /**
     * Draws the scale numbers.
     */
    protected void paintComponent(Graphics g) {
        // let UI delegate paint first
        super.paintComponent(g);
        // paint my contents next....
        String[] stringValues = getValueStrings();
        // set color
        g.setColor(Color.BLACK);
        // get font characteristics
        g.setFont(this.getFont().deriveFont(Font.PLAIN));
        FontMetrics fm = g.getFontMetrics();
        int XBASE = 0;
        int YBASE = fm.getMaxAscent() + fm.getLeading();
        // draw the start string
        g.drawString(stringValues[0], XBASE, YBASE);
        // draw values in the middle
        int accStart = fm.stringWidth(stringValues[0]) + 5;
        int accEnd = this.getWidth()
                - fm.stringWidth(stringValues[stringValues.length - 1]) - 5;
        int dist = this.getWidth() / (stringValues.length - 1);
        for (int i = 1; i < stringValues.length - 1; i++) {
            int startPos = dist * i - fm.stringWidth(stringValues[i]) / 2;
            int endPos = startPos + fm.stringWidth(stringValues[i]);
            if (startPos > accStart && endPos < accEnd) {
                XBASE = startPos;
                g.drawString(stringValues[i], XBASE, YBASE);
                accStart = XBASE + fm.stringWidth(stringValues[i]) + 5;
            }
        }
        // draw the end string
        XBASE = this.getWidth()
                - fm.stringWidth(stringValues[stringValues.length - 1]);
        g.drawString(stringValues[stringValues.length - 1], XBASE, YBASE);
    }

    /**
     * Updates the label.
     */
    public void update() {
        this.validate();
        this.repaint();
    }
}
