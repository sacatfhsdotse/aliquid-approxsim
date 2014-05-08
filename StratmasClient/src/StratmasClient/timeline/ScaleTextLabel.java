package StratmasClient.timeline;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import javax.swing.JLabel;

/**
 * This class is used to draw scale numbers in the timeline.
 */
public class ScaleTextLabel extends JLabel {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1548321135549416539L;
    /**
     * The actual color.
     */
    private Color color;
    /**
     * The actual font.
     */
    private Font font;
    /**
     * The start value.
     */
    private long startValue = 0;
    /**
     * The end value.
     */
    private long endValue = 100;
    /**
     * The maximum number of displayed values.
     */
    private int maxValueNr = 11;
    /**
     * Reference to the timeline panel.
     */
    private TimelinePanel timelinePanel;

    /**
     * Creates the label with defult font.
     */
    public ScaleTextLabel(TimelinePanel timelinePanel, Color color) {
        this.timelinePanel = timelinePanel;
        this.color = color;
        font = this.getFont().deriveFont(Font.PLAIN);
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
        int increment = (int) (endValue - startValue) / (maxValueNr - 1);
        String[] values = new String[maxValueNr];
        for (int i = 0; i < maxValueNr; i++) {
            values[i] = createCompactString(startValue + i * increment);
        }
        return values;
    }

    /**
     * Creates a string for display.
     * 
     * @param i the number converted to string.
     */
    public String createCompactString(long i) {
        // create a string
        String s = (new Long(i)).toString();
        String t = timelinePanel.getTimeUnitAsString().substring(0, 1)
                .toLowerCase();
        s = s.concat(t);
        return s;
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
        g.setColor(color);
        // get font characteristics
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        if (this.getWidth() > fm.stringWidth(stringValues[0])
                + fm.stringWidth(stringValues[stringValues.length - 1])) {
            int XBASE = 0;
            int YBASE = fm.getMaxAscent() + fm.getLeading();
            // draw the start string
            g.drawString(stringValues[0], XBASE, YBASE);
            // check if the middle strings will be drawn
            double incr = this.getWidth() * 1.0 / (maxValueNr - 1);
            XBASE = (int) (incr - fm.stringWidth(stringValues[1]) / 2.0);
            double startValueLength = fm.stringWidth(stringValues[1])
                    + fm.stringWidth(stringValues[1]) / 4.0;
            if (XBASE > startValueLength) {
                // draw all the middle strings
                for (int i = 1; i < stringValues.length - 1; i++) {
                    XBASE = (int) (i * incr - fm.stringWidth(stringValues[i]) / 2.0);
                    g.drawString(stringValues[i], XBASE, YBASE);
                }
            } else {
                // draw only the middle string
                int i = stringValues.length / 2;
                XBASE = (int) (i * incr - fm.stringWidth(stringValues[i]) / 2.0);
                if (XBASE > startValueLength) {
                    g.drawString(stringValues[i], XBASE, YBASE);
                }
            }
            // draw the end string
            XBASE = this.getWidth()
                    - fm.stringWidth(stringValues[stringValues.length - 1]);
            g.drawString(stringValues[stringValues.length - 1], XBASE, YBASE);
        }
    }

    /**
     * Updates the label.
     */
    public void update(long startValue, long endValue) {
        this.startValue = startValue;
        this.endValue = endValue;
        this.repaint();
    }
}
