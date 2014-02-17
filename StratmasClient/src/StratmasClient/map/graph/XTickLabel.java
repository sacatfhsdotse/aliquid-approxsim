package StratmasClient.map.graph;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JLabel;

/**
 * This class is used to paint ticks on the x-axis in the graph.
 */
public class XTickLabel extends JLabel{
    /**
     * The actual color.
     */
    public static final Color COLOR = Color.BLACK;
    
    /**
     * Creates new label.
     */
    public XTickLabel() {
        setPreferredSize(new Dimension(this.getWidth(), 3));
    }

    /**
     * Draws the paint ticks.
     */
    protected void paintComponent(Graphics g) {
        // let UI delegate paint first 
        super.paintComponent(g); 
        // paint the contents next....
        g.setColor(XTickLabel.COLOR);
        // draw horizontal lines
        g.drawLine(0, 0, this.getWidth() - 1, 0);
        // draw vertical lines
        double tickWidth = this.getWidth() * 1.0 / (ProcessVariableXYGraph.NR_OF_DISPLAYED_TIME_VALUES - 1);
        // draw paint ticks
        g.drawLine(0, 0, 0, this.getHeight());
        int i = 1;
        while (i < ProcessVariableXYGraph.NR_OF_DISPLAYED_TIME_VALUES - 1) {
            int x = (int) (i * tickWidth);
            g.drawLine(x, 0, x, this.getHeight());
            i++;
        }
        g.drawLine(this.getWidth() - 1, 0, this.getWidth() - 1, this.getHeight());
    }
    
    /**
     * Updates the label.
     */ 
    public void update() {
        this.validate();
        this.repaint();
    }
}
