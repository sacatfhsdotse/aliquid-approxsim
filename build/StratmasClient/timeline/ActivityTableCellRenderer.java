package StratmasClient.timeline;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * The cell renderer for the activity table.
 */
public class ActivityTableCellRenderer extends DefaultTableCellRenderer {

    /**
     * Returns the default table cell renderer. 
     *
     * @param table the JTable.
     * @param value the value to assign to the cell at [row, column].
     * @param isSelected true if cell is selected.
     * @param hasFocus true if cell has focus.
     * @param row the row of the cell to render.
     * @param column the column of the cell to render. 
     *
     * @return the deafult table cell renderer.
     */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
                                                   boolean hasFocus, int row, int column) {
        DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();
        Component renderer = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        ((JLabel) renderer).setOpaque(true);
        // color each other row with different color
        if (!isSelected) {
            Color backgroundColor = (row % 2 == 0)? TimelineConstants.LIGHT : TimelineConstants.LIGHTER;
            renderer.setBackground(backgroundColor);
        }
        return renderer;
    }
}
