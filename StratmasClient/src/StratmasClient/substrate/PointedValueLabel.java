package StratmasClient.substrate;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import javax.swing.JLabel;

/**
 * Displays the value on the color map pointed by the mouse cursor.
 */
public class PointedValueLabel extends JLabel{
    /**
	 * 
	 */
	private static final long serialVersionUID = 2250385010018148006L;
	/**
     * The displayed value.
     */
    private String displayedValue;
    /**
     * The x-coordinate of the displayed value in the label.
     */
    private int valuePosition;

    /**
     * Sets the value.
     *
     * @param displayedValue the value displayed in the label.
     * @param pos x-coordinate of the value in the label.
     */
    public void setValue(String displayedValue, int pos) {
        this.displayedValue = displayedValue;
        valuePosition = pos;
    }
    
    /**
     * Updates this label.
     */
    public void update() {
        validate();
        repaint();
    }
    
    /**
     * Draws the color map label.
     */
    protected void paintComponent(Graphics g) {
        // let UI delegate paint first 
        super.paintComponent(g); 
        
        // display the value
        if (displayedValue != null) {
            // set color
            g.setColor(Color.BLACK);
            // get font characteristics
            FontMetrics fm = g.getFontMetrics();
            g.setFont(this.getFont().deriveFont(Font.PLAIN));
            // get the position of the value
            int XBASE = (valuePosition + fm.stringWidth(displayedValue) > this.getWidth())? 
                this.getWidth() - fm.stringWidth(displayedValue) : valuePosition;
            int YBASE = fm.getMaxAscent() + fm.getLeading();
            g.drawString(displayedValue, XBASE, YBASE);
        }

    }
}
