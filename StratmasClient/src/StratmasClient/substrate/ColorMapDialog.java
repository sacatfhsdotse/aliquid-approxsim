package StratmasClient.substrate;

import java.awt.Font;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.ButtonGroup;

import StratmasClient.StratmasDialog;
import StratmasClient.ProcessVariableDescription;

/**
 * ColorMapDialog is used to set color map options for a process variable in SubstrateEditor.
 */
public class ColorMapDialog extends JDialog implements ActionListener {
   /**
	 * 
	 */
	private static final long serialVersionUID = 2552288079896338217L;
/**
     * Instance of the actual dialog.
     */
    private static ColorMapDialog dialog;
    /**
     * The button for the linear scale selection.
     */
    private JRadioButton linButton;
    /**
     * The button for the logarithmic scale selection.
     */
    private JRadioButton logButton;
    /**
     * The box which contains the color maps.
     */
    private JComboBox colorMapBox;
    /**
     * The cancel button.
     */
    private JButton cancelButton;
    /**
     * The accept button.
     */
    private JButton setButton;
    /**
     * The text field for the the minimum value.
     */
    private JTextField minTextField;
    /**
     * The text field for the the maximum value.
     */
    private JTextField maxTextField;
    /**
     * The actual process variable.
     */
    private ProcessVariableDescription processVariable;
    /**
     * The color map panel.
     */
    private ColorChooser colorChooser;
     
    /**
     * Sets up and shows the dialog.
     *
     * @param processVariable actual process variable.
     * @param frameComp actual frame for the dialog.
     * @param colorChooser the color map panel.
     */
    public static void showDialog(ProcessVariableDescription processVariable, Component frameComp, ColorChooser colorChooser) {
        Frame frame = JOptionPane.getFrameForComponent(frameComp);
        dialog = new ColorMapDialog(processVariable, frame, colorChooser);
        dialog.setVisible(true);
    }
    
    /**
     * Creates color map dialog.
     *
     * @param processVariable actual process variable.
     * @param frameComp actual frame for the dialog.
     * @param colorChooser the color map panel.
     *
     */
    private ColorMapDialog(ProcessVariableDescription processVariable, Frame frame, ColorChooser colorChooser) {
        super(frame, new String("Color Map Values for " + processVariable.getName()), true);
        this.processVariable = processVariable;
        this.colorChooser = colorChooser;
        
        // radio buttons
        JPanel scalePanel = new JPanel();
        scalePanel.setLayout(new GridLayout(2, 1, 3, 3));
        linButton = new JRadioButton("Linear Scale");
        if (processVariable.getScale().equals("Linear Scale")) {
            linButton.setSelected(true);
        }
        linButton.setFont(linButton.getFont().deriveFont(Font.PLAIN));
        linButton.addActionListener(this);
        scalePanel.add(linButton);
        logButton = new JRadioButton("Logarithmic Scale");
        if (processVariable.getScale().equals("Logarithmic Scale")) {
            logButton.setSelected(true);
        }
        logButton.setFont(logButton.getFont().deriveFont(Font.PLAIN));
        logButton.addActionListener(this);
        scalePanel.add(logButton);
        scalePanel.setBorder(BorderFactory. createCompoundBorder(BorderFactory.createTitledBorder("Set Color Map Scale"),
                                                                 BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        
        ButtonGroup scaleButtons = new ButtonGroup();
        scaleButtons.add(linButton);
        scaleButtons.add(logButton);
        
        // minimum and maximum text fields
        JPanel minmaxPanel = new JPanel();
        minmaxPanel.setLayout(new GridLayout(2, 2, 3, 3));
        JLabel minLabel = new JLabel("Minimum value : ");
        minLabel.setFont(minLabel.getFont().deriveFont(Font.PLAIN));
        minmaxPanel.add(minLabel);
        minTextField = new JTextField(10);
        minTextField.setText((new Double(processVariable.getMin())).toString());
        minTextField.selectAll();
        minmaxPanel.add(minTextField);
        JLabel maxLabel = new JLabel("Maximum value : ");
        maxLabel.setFont(maxLabel.getFont().deriveFont(Font.PLAIN));
        minmaxPanel.add(maxLabel);
        maxTextField = new JTextField(10);
        maxTextField.setText((new Double(processVariable.getMax())).toString());
        maxTextField.selectAll();
        minmaxPanel.add(maxTextField);
        minmaxPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Set Minimum & Maximum"),
                                                                 BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        
        // combo box
        JPanel comboPanel = new JPanel();
        colorMapBox = new JComboBox(ColorMap.COLOR_MAPS);
        colorMapBox.setFont(colorMapBox.getFont().deriveFont(Font.PLAIN));
        int index = getComboIndex(processVariable.getColorMap());
        colorMapBox.setSelectedIndex(index);
        colorMapBox.addActionListener(this);
        comboPanel.add(colorMapBox);
        comboPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Set Color Map"),
                                                                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        
        // create and initialize the buttons
        cancelButton = new JButton("Cancel");
        cancelButton.setFont(cancelButton.getFont().deriveFont(Font.PLAIN));
        cancelButton.setMargin(new Insets(1, 2, 1, 2));
        cancelButton.addActionListener(this);
        setButton = new JButton("Set");
        setButton.setFont(setButton.getFont().deriveFont(Font.PLAIN));
        setButton.setMargin(new Insets(1, 6, 1, 6));
        setButton.addActionListener(this);
        getRootPane().setDefaultButton(setButton);

        // lay out the buttons from left to right.
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        buttonPanel.add(setButton);

        // put everything together
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.add(scalePanel);
        contentPane.add(minmaxPanel);
        contentPane.add(comboPanel);
        contentPane.add(buttonPanel);

        // initialize values
        pack();
        setLocationRelativeTo(null);
        minTextField.requestFocusInWindow();
    }
    
    /**
     * Fires an action when a dialog button is pressed.
     *
     * @param e action event generated by the dialog.
     */
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        // handle "Cancel" button
        if (cancelButton.equals(obj)) {
            ColorMapDialog.dialog.setVisible(false);
        }
        // handle "Set" button
        else if (setButton.equals(obj)) {
            try {
                double minValue = (Double.valueOf(minTextField.getText())).doubleValue();
                double maxValue = (Double.valueOf(maxTextField.getText())).doubleValue();
                // check the boundary values
                if (minValue > 100000000000.0 || minValue < 0 || minValue >= maxValue || maxValue > 100000000000.0 ) {
                    throw new RuntimeException();
                }
                // update the process variable
                if (linButton.isSelected()) {
                    processVariable.setLinearScale();
                }
                else {
                    processVariable.setLogarithmicScale();
                }
                processVariable.setMin(minValue);
                processVariable.setMax(maxValue);
                processVariable.setColorMap((String)colorMapBox.getSelectedItem());
                colorChooser.update(processVariable);
                ColorMapDialog.dialog.setVisible(false);
            }
            catch (RuntimeException exc) {
                errorMess();
            }
        }
    }
    
    /**
     * Help function - used in the constructor. 
     *
     * @param s  one of the predefined color maps.
     *
     * @return position of the color scale map in the array of the predefined color maps.
     */
    private int getComboIndex(String s) {
        for (int i = 0; i < ColorMap.COLOR_MAPS.length; i++) {
            if (s.equals(ColorMap.COLOR_MAPS[i])) {
                return  i;
            }
        }
        return 0;
    }
    
    /**
     * Error message.
     */
    private void errorMess() {
        Object[] options = {"OK", "Help"}; 
        StratmasDialog.showOptionDialog(new JFrame("ERROR!!!"), "Invalid input value!", "Error message",
                                        JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null,
                                        options, options[0]);
    }
        
}
