package StratmasClient.substrate;

import java.text.DecimalFormat;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JLayeredPane;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import StratmasClient.ProcessVariableDescription;
import StratmasClient.StratmasDialog;

/**
 * This class is used to select a color from the color map and fill arbitrary shapes. Each color in the color
 * map represents a different value.  
 */
public class ColorChooser extends JPanel {
    /**
     * The actual value.
     */
    private double actualValue;
    /**
     * The label which shows the selected color.
     */
    final private JLabel selectedColorLabel = new JLabel();
    /**
     * The label which shows the background color.
     */
    final private JLabel backgroundColorLabel = new JLabel();
    /**
     * The selected value.
     */
    final private JTextField selectedValue = new JTextField(10); 
    /**
     * The button used to adjust the selected value.
     */
    private JButton adjustButton = new JButton("Adjust"); 
    /**
     * Reference to the resource map drawer.
     */
    private SubstrateMapDrawer drawer;
    /**
     * Reference to the color map.
     */
    private ColorMap colorMap;
    
    /**
     * Create new color chooser.
     */
    public ColorChooser(SubstrateEditor substrateEditor) {
        drawer = substrateEditor.getSubstrateDrawer();
        
        //create new color map
        ProcessVariableDescription pvd = substrateEditor.getProcessVariable();
        String scale        = (pvd != null)? pvd.getScale() : "Linear Scale";
        double minValue     = (pvd != null)? pvd.getMin() : 0;
        double maxValue     = (pvd != null)? pvd.getMax() : 100;
        String colorMapName = (pvd != null)? pvd.getColorMap() : ColorMap.COLOR_MAPS[0];
        colorMap = new ColorMap(this, scale, minValue, maxValue, colorMapName);
        
        // initialize the actual value
        actualValue = minValue;
        
        // create the panel
        createColorChooserPanel();
        
        // initialize the selected color
        updateSelectedColor(colorMap.getColorTable()[0]);
    }
    
    /**
     * Creates the panel containing the color map.
     */
    private void createColorChooserPanel() {
        JPanel eastPanel = new JPanel(new BorderLayout(5, 5));
        eastPanel.add(createSelectedValuePanel(), BorderLayout.NORTH);
        eastPanel.add(createSelectedColorPanel(), BorderLayout.CENTER);
        eastPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Actual Value"),
                                                               BorderFactory.createEmptyBorder(2, 2, 2, 2)));

        JPanel validPanel = new JPanel(new BorderLayout(5, 5));
        validPanel.add(colorMap.getPanel(), BorderLayout.CENTER);
        validPanel.add(eastPanel, BorderLayout.EAST);
        
        setLayout(new BorderLayout(5, 5));
        add(validPanel, BorderLayout.WEST);
        add(new JLabel(), BorderLayout.CENTER);
        setBorder(BorderFactory.createTitledBorder(""));
    }
    
    /**
     * Creates the panel for the selected value.
     */
    private JPanel createSelectedValuePanel() {
        final ColorChooser self = this;
        // the text field for the selected value
        selectedValue.setText(convertToString(colorMap.getMinValue()));
        selectedValue.setFont(selectedValue.getFont().deriveFont(Font.PLAIN));
        selectedValue.setBackground(this.getBackground());
        
        // the button for adjusting the selected value
        adjustButton.setMargin(new Insets(1, 5, 1, 5));
        adjustButton.setFont(adjustButton.getFont().deriveFont(Font.PLAIN));
        adjustButton.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent event) {
                    String textValue = selectedValue.getText();
                    try {
                        textValue =  textValue.replace(',','.');
                        double value = Double.parseDouble(textValue);
                        if (value < self.getColorMap().getMinValue() || value > self.getColorMap().getMaxValue()) {
                            throw new NumberFormatException();
                        }
                        self.updateActualValue(value, false);
                    }
                    catch (NumberFormatException e) {
                        String errStr = new String("Value not valid! Enter a value between " + 
                                                   self.convertToString(self.getColorMap().getMinValue()) + " and " + 
                                                   self.convertToString(self.getColorMap().getMaxValue()) + ".");
                        StratmasDialog.showErrorMessageDialog(self, errStr, "Input error");
                        self.updateActualValue(self.getActualValue(), false);
                    }
                }
            });
        
        JPanel selectedValuePanel = new JPanel(new BorderLayout(5, 0));
        selectedValuePanel.add(selectedValue, BorderLayout.CENTER);
        selectedValuePanel.add(adjustButton, BorderLayout.EAST);
        
        return selectedValuePanel;
    }
    
    /**
     * Creates the panel for the selected color.
     */
    private JPanel createSelectedColorPanel() {
        // the label for the background color
        backgroundColorLabel.setBackground(drawer.getBackgroundColor());
        backgroundColorLabel.setOpaque(true);
        final JPanel backgroundColorPanel = new JPanel(new BorderLayout());
        backgroundColorPanel.add(backgroundColorLabel, BorderLayout.CENTER);
        backgroundColorPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        
        // the label for the selected color        
        selectedColorLabel.setBackground(colorMap.getColorTable()[0]);
        selectedColorLabel.setOpaque(true);
        final JPanel selColorPanel = new JPanel(new BorderLayout());
        selColorPanel.add(selectedColorLabel, BorderLayout.CENTER);
        selColorPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        
        // the panel for the selected and the background colors
        JLayeredPane selectedColorPane = new JLayeredPane();
        selectedColorPane.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent event) {
                    JLayeredPane pane = (JLayeredPane)event.getSource();
                    if (pane.getWidth() > 0) {
                        backgroundColorPanel.setBounds(0, 0, 2 * pane.getWidth() / 3, 3 * pane.getHeight() / 4);
                        selColorPanel.setBounds(pane.getWidth() / 3, pane.getHeight() / 4, 
                                                2 * pane.getWidth() / 3, pane.getHeight());
                    }
                }
            });
        selectedColorPane.add(backgroundColorPanel, new Integer(0));
        selectedColorPane.add(selColorPanel, new Integer(1));
        JPanel selectedColorPanel = new JPanel(new BorderLayout());
        selectedColorPanel.add(selectedColorPane, BorderLayout.CENTER);
        selectedColorPanel.setBorder(BorderFactory.createEmptyBorder(2, 20, 2, 20));
        
        return selectedColorPanel;
    }
    
    /**
     * Updates this panel.
     */
    public void update() {
        validate();
        repaint();
    }
    
    /**
     * Updates the panel.
     */
    public void update(ProcessVariableDescription pvd) {
        // update the color map
        colorMap.update(pvd.getScale(), pvd.getMin(), pvd.getMax(), pvd.getColorMap());
        // update the actual value
        updateActualValue(pvd.getMin(), false);
        // update the drawer
        drawer.updateColoredRegions();
        drawer.updateShapeUnderCreation(getActualColor());
    }
    
    /**
     * Converts a number to a String.
     *
     * @param value the value which is converted.
     */
    public String convertToString(double value) {
        // linear scale
        if (colorMap.isLinearScale()) {
            if (value >= 10 || getMaxValue() >= 10) {
                DecimalFormat resultFormat = new DecimalFormat("0");
                return resultFormat.format(value);
            }
            else if (value >= 1) {
                DecimalFormat resultFormat = new DecimalFormat("0.#");
                return resultFormat.format(value);
            }
            else if (value > 0) {
                int lgVal = (int) Math.abs(Math.floor(Math.log(value) / Math.log(10)));
                DecimalFormat resultFormat = new DecimalFormat();
                resultFormat.setMinimumFractionDigits(lgVal);
                resultFormat.setMaximumFractionDigits(lgVal + 1);
                return resultFormat.format(value);
            }
            else {
                return "0";
            }
        }
        // logarithmic scale
        else {
            if (value >= 1 || getMaxValue() >= 100000) {
                DecimalFormat resultFormat = new DecimalFormat("0");
                return resultFormat.format(value);
            }
            else if (value > 0) {
                int lgVal = (int) Math.abs(Math.floor(Math.log(value) / Math.log(10)));
                DecimalFormat resultFormat = new DecimalFormat();
                resultFormat.setMinimumFractionDigits(lgVal);
                resultFormat.setMaximumFractionDigits(lgVal);
                return resultFormat.format(value);        
            }
            else {
                return "0";
            }
        }
    }
    
    /**
     * Updates the selected color.
     */
    public void updateSelectedColor(Color selectedColor) {
        selectedColorLabel.setBackground(selectedColor);
    }
    
    /**
     * Updates the actual value.
     */
    public void updateActualValue(double actualValue, boolean format) {
        try {
            this.actualValue = (format)? Double.parseDouble(convertToString(actualValue)) : actualValue;
        }
        catch (NumberFormatException e) {
            this.actualValue = actualValue; 
         }
        selectedValue.setText((format)? convertToString(actualValue) : String.valueOf(actualValue));
        updateSelectedColor(getMappingColor(actualValue));
        // update drawer
        drawer.updateShapeUnderCreation(getActualColor());
    }
    
    /**
     * Returns the color for the given value. 
     *
     * @param  value the given value.
     *
     * @return the color matching the value.
     */
    public Color getMappingColor(double value) {
        return colorMap.getMappingColor(value);
    }
    
    /**
     * Returns the minimum value.
     */
    public double getMinValue() {
        return colorMap.getMinValue();
    }
    
    /**
     * Returns the maximum value.
     */
    public double getMaxValue() {
        return colorMap.getMaxValue();
    }

    /**
     * Sets the actual value.
     */
    public void setActualValue(double value) {
        actualValue = value;
    }
    
    /**
     * Returns the actual value.
     */
    public double getActualValue() {
        return actualValue;
    }
    
    /**
     * Returns the actual color.
     */
    public Color getActualColor() {
        return getMappingColor(actualValue);        
    }
    
    /**
     * Returns the color map.
     */
    public ColorMap getColorMap() {
        return colorMap;
    } 
    
}
