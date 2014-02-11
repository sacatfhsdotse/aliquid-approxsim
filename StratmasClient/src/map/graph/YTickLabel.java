package StratmasClient.map.graph;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JLabel;

/**
 * This class is used to paint ticks on the y-axis in the graph.
 */
public class YTickLabel extends JLabel{
    /**
     * The actual color.
     */
    public static final Color COLOR = Color.BLACK;
    /**
     * The graph container.
     */
    private ProcessVariableXYGraph graph;
    
    /**
     * Creates new label.
     */
    public YTickLabel(ProcessVariableXYGraph graph) {
        this.graph = graph;
        setPreferredSize(new Dimension(3, this.getHeight()));
    }

    /**
     * Draws the paint ticks.
     */
    protected void paintComponent(Graphics g) {
        // let UI delegate paint first 
        super.paintComponent(g); 
        // paint my contents next....
        g.setColor(COLOR);
        // draw vertical line
        g.drawLine(this.getWidth() - 1, 0, this.getWidth() - 1, this.getHeight());
        // draw horizontal lines
        // the first tick
        g.drawLine(0, 0, this.getWidth() - 1, 0);
        // the middle ticks
        // special case - the difference between the bounds is one power of ten (logarithmic scale) and
        // the lower bound is different from zero
        if (graph.isLogarithmicScale() && graph.getLowerYBound() != 0 &&
            (int)Math.round(graph.log10(graph.getUpperYBound()) - graph.log10(graph.getLowerYBound())) == 1) {
            for (int i = 0; i < graph.MIDDLE_LOG_VALUES.length; i++) {
                int y = (int) (this.getHeight() - graph.log10(graph.MIDDLE_LOG_VALUES[i]) * this.getHeight());
                g.drawLine(0, y, this.getWidth() - 1, y);
            }
        }
        // all other cases
        else {
            double tickWidth = this.getHeight() * 1.0 / (graph.getNrOfDisplayedYValues() - 1);
            int i = 1;
            while (i < graph.getNrOfDisplayedYValues() - 1) {
                int y = (int) (i * tickWidth);
                g.drawLine(0, y, this.getWidth() - 1, y);
                i++;
            }
        }
        // the last tick
        g.drawLine(0, this.getHeight() - 1, this.getWidth() - 1, this.getHeight() - 1);
    }
    
    /**
     * Updates the label.
     */ 
    public void update() {
        this.validate();
        this.repaint();
    }
}
