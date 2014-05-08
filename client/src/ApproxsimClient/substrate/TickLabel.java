package ApproxsimClient.substrate;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JLabel;

/**
 * This class is used to paint ticks on the color map.
 */
public class TickLabel extends JLabel {
    /**
	 * 
	 */
    private static final long serialVersionUID = 4496288227579233513L;
    /**
     * The actual color.
     */
    public static final Color COLOR = Color.BLACK;
    /**
     * The number of ticks.
     */
    private int tickNumber;

    /**
     * Creates new label.
     */
    public TickLabel(int tNumber) {
        tickNumber = tNumber;
        setPreferredSize(new Dimension(this.getWidth(), 10));
    }

    /**
     * Sets the number of displayed ticks.
     */
    public void setTickNumber(int tNumber) {
        tickNumber = tNumber;
    }

    /**
     * Draws the paint ticks.
     */
    protected void paintComponent(Graphics g) {
        // let UI delegate paint first
        super.paintComponent(g);
        // paint the contents next....
        g.setColor(TickLabel.COLOR);
        // draw horizontal lines
        g.drawLine(0, 0, this.getWidth() - 1, 0);
        // draw vertical lines
        double tickWidth = this.getWidth() * 1.0 / (tickNumber - 1);
        // draw paint ticks
        g.drawLine(0, 0, 0, this.getHeight());
        int i = 1;
        while (i < tickNumber - 1) {
            int x = (int) (i * tickWidth);
            g.drawLine(x, 0, x, this.getHeight() / 3);
            i++;
        }
        g.drawLine(this.getWidth() - 1, 0, this.getWidth() - 1,
                   this.getHeight());
    }

    /**
     * Updates the label.
     */
    public void update() {
        this.validate();
        this.repaint();
    }
}
