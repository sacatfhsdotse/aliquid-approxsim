package StratmasClient.map.graph;

import java.awt.Graphics;
import java.awt.Color;
import javax.swing.JLabel;

/**
 * This class is used to paint ticks in the graph.
 */
public class TickLabel extends JLabel {
    /**
	 * 
	 */
    private static final long serialVersionUID = -3583571918418415061L;
    /**
     * Indicator for the horizontal line.
     */
    public static final int EAST = 0;
    /**
     * Indicator for the horizontal line.
     */
    public static final int SOUTH = 1;
    /**
     * The line indicator.
     */
    private int lineIndicator;
    /**
     * The actual color.
     */
    private Color color;
    /**
     * The length of the interval curretly displayed in the timeline.
     */
    private int intervalLength = 100;

    /**
     * The constructor.
     */
    public TickLabel(int lineIndicator, Color color) {
        this.lineIndicator = lineIndicator;
        this.color = color;
    }

    /**
     * Draws the paint ticks.
     */
    protected void paintComponent(Graphics g) {
        // let UI delegate paint first
        super.paintComponent(g);
        // paint my contents next....
        int yLine = (lineIndicator == NORTH) ? 0 : this.getHeight() - 1;
        g.setColor(color);
        // draw horizontal lines
        g.drawLine(0, yLine, this.getWidth() - 1, yLine);
        // draw vertical lines
        int tickNr = getPaintTickNumber();
        double tickWidth = this.getWidth() * 1.0 / tickNr;
        // draw paint ticks
        g.drawLine(0, 0, 0, this.getHeight());
        int i = 1;
        while (i < tickNr) {
            int x = (int) (i * tickWidth);
            int y = 0;
            if (lineIndicator == NORTH) {
                y = (i % 10 == 0) ? this.getHeight() : this.getHeight() / 3;
            } else {
                y = (i % 10 == 0) ? 0 : 2 * this.getHeight() / 3;
            }
            g.drawLine(x, yLine, x, y);
            i++;
        }
        g.drawLine(this.getWidth() - 1, 0, this.getWidth() - 1,
                   this.getHeight());
    }

    /**
     * Updates the label.
     */
    public void update(int intervalLength) {
        this.intervalLength = intervalLength;
        this.repaint();
    }

    /**
     * Returns the number of paint ticks.
     */
    protected int getPaintTickNumber() {
        return (intervalLength <= 10) ? 10 : 100;
    }
}
