package StratmasClient.map.graph;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Dimension;
import javax.swing.JLabel;

/**
 * This class is used to draw the scale numbers on the time axis in the graph.
 */
public class XScaleTextLabel extends JLabel {
    /**
	 * 
	 */
    private static final long serialVersionUID = -270179816409395839L;
    /**
     * The actual font.
     */
    private Font font;
    /**
     * The graph container.
     */
    private ProcessVariableXYGraph graph;

    /**
     * Creates the label with default font.
     */
    public XScaleTextLabel(ProcessVariableXYGraph graph) {
        this.graph = graph;
        font = this.getFont().deriveFont(Font.PLAIN);
        setPreferredSize(new Dimension(this.getWidth(), (int) (getFont()
                .getSize() * 1.5)));
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
        int increment = (int) (graph.getEndTime() - graph.getStartTime())
                / (ProcessVariableXYGraph.NR_OF_DISPLAYED_TIME_VALUES - 1);
        String[] values = new String[ProcessVariableXYGraph.NR_OF_DISPLAYED_TIME_VALUES];
        for (int i = 0; i < ProcessVariableXYGraph.NR_OF_DISPLAYED_TIME_VALUES; i++) {
            values[i] = new Long(graph.getStartTime() + i * increment)
                    .toString();
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
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        if (this.getWidth() > fm.stringWidth(stringValues[0])
                + fm.stringWidth(stringValues[stringValues.length - 1])) {
            int XBASE = 0;
            int YBASE = fm.getMaxAscent() + fm.getLeading();
            // draw the start string
            g.drawString(stringValues[0], XBASE, YBASE);
            // check if the middle strings will be drawn
            double incr = this.getWidth() * 1.0
                    / (ProcessVariableXYGraph.NR_OF_DISPLAYED_TIME_VALUES - 1);
            XBASE = (int) (incr - fm.stringWidth(stringValues[1]) / 2.0);
            double startTimeLength = fm.stringWidth(stringValues[1])
                    + fm.stringWidth(stringValues[1]) / 4.0;
            if (XBASE > startTimeLength) {
                // draw all the middle strings
                for (int i = 1; i < stringValues.length - 1; i++) {
                    XBASE = (int) (i * incr - fm.stringWidth(stringValues[i]) / 2.0);
                    g.drawString(stringValues[i], XBASE, YBASE);
                }
            } else {
                // draw only the middle string
                int i = stringValues.length / 2;
                XBASE = (int) (i * incr - fm.stringWidth(stringValues[i]) / 2.0);
                if (XBASE > startTimeLength) {
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
    public void update() {
        this.validate();
        this.repaint();
    }
}
