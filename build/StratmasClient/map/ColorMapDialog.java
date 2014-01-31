package StratmasClient.map;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;
import java.lang.RuntimeException;
import java.awt.event.*;

import StratmasClient.StratmasDialog;

/**
 * ColorMapDialog is used to set options for the color map. The following options
 * can be chosen:
 * 1. Scale for the diplayed values - linear or logarithmic.
 * 2. Upper and lower bounds for the diplayed values. The lowest bound should be zero
 *    if linear scale is used or 0.1 if logarithmic scale is used. The highest bound 
 *    shoudn't be larger then 100 000 000 000.
 * 3. Color scale map - four different choices are available.
 *
 * @version 1.0
 * @author Amir Filipovic 
 */
public class ColorMapDialog extends JDialog implements ActionListener {
    /**
     * Instance of the actual dialog.
     */
    private static ColorMapDialog dialog;
    /**
     * New scale.
     */
    private static String scale = "";
    /**
     * Old scale.
     */
    private static String old_scale = "";
    /**
     * New bounds for the displayed values.
     */
    private float min, max;
    /**
     * Old bounds for the displayed values.
     */
    private float old_min, old_max;
    /**
     * New color combination.
     */
    private String color_map;
    /**
     * Old color combination.
     */
    private String old_color_map;
    /**
     * Pre-defined color combinations.
     */
    private String[] color_maps = {"blue-green-yellow-red", "blue-green-yellow", "lightblue-yellow-orange-red",
				   "white-lightgrey-olivebrown-black", "blue-white", "red-white", "black-white"};
    /**
     * List of values that returns ie., [scale, min, max, color_map] (all strings).
     */
    private static Vector values = new Vector();
    /**
     * The button for the linear scale selection.
     */
    private JRadioButton lin_button;
    /**
     * The button for the logarithmic scale selection.
     */
    private JRadioButton log_button;
    /**
     * The box which contains the color maps.
     */
    private JComboBox layer_box;
    /**
     * The help button.
     */
    private JButton help_button;
    /**
     * The cancel button.
     */
    private JButton cancel_button;
    /**
     * The accept button.
     */
    private JButton set_button;
    /**
     * The text field for the the minimum scale value.
     */
    private JTextField min_text_field;
    /**
     * The text field for the the maximum scale value.
     */
    private JTextField max_text_field;
    /**
     * The font for the gui components.
     */
    private Font font;
    /**
     * Size of the font.
     */
    private int font_size;
    
    /**
     * Set up and show the dialog.
     *
     * @param frame_comp actual frame for the dialog.
     * @param location_comp where the dialog wants to be displayed.
     * @param scale actual scale of the color map.
     * @param min actual minimum bound.
     * @param max actual maximum bound.
     * @param color_map actual color scale map.
     *
     * @return updated variables for color map setup. The vector contains four
     *         <code>String</code> elements ie. {scale, min, max, color map}.
     */
    public static Vector showDialog(Component frame_comp, Component location_comp, String scale,
				    float min, float max, String color_map) {
        Frame frame = JOptionPane.getFrameForComponent(frame_comp);
        dialog = new ColorMapDialog(frame, location_comp, scale, min, max, color_map);
        dialog.setVisible(true);
        return values;
    }
        
    /**
     * Create color map dialog.
     *
     * @param frame_comp actual frame for the dialog.
     * @param location_comp where the dialog wants to be displayed.
     * @param scale actual scale of the color map.
     * @param min actual minimum bound.
     * @param max actual maximum bound.
     * @param color_map actual color scale map.
     *
     */
    private ColorMapDialog(Frame frame, Component location_comp, String scale, float min, float max, 
			   String color_map) {
	//
        super(frame, "Color Map Options", true);
	old_scale = scale;
	old_min = min;
	old_max = max;
	old_color_map = color_map;

	// radio buttons
	JPanel scale_panel = new JPanel();
	scale_panel.setLayout(new GridLayout(2,1,3,3));
        lin_button = new JRadioButton("Linear Scale");
	if (old_scale.equals("Linear Scale")) {
	    lin_button.setSelected(true);
	}
	lin_button.setFont(lin_button.getFont().deriveFont(Font.PLAIN));
	lin_button.addActionListener(this);
	scale_panel.add(lin_button);
	log_button = new JRadioButton("Logarithmic Scale");
	if (old_scale.equals("Logarithmic Scale")) {
	    log_button.setSelected(true);
	}
	log_button.setFont(log_button.getFont().deriveFont(Font.PLAIN));
	log_button.addActionListener(this);
	scale_panel.add(log_button);
	scale_panel.setBorder(BorderFactory.
			      createCompoundBorder(BorderFactory.createTitledBorder("Choose Scale"),
						   BorderFactory.createEmptyBorder(5,5,5,5)));
	
	// minimum and maximum text fields
	JPanel minmax_panel = new JPanel();
	minmax_panel.setLayout(new GridLayout(2,2,3,3));
	JLabel min_label = new JLabel("Minimum value : ");
	min_label.setFont(min_label.getFont().deriveFont(Font.PLAIN));
	minmax_panel.add(min_label);
	min_text_field = new JTextField(10);
	min_text_field.setText((new Float(old_min)).toString());
	min_text_field.selectAll();
	minmax_panel.add(min_text_field);
	JLabel max_label = new JLabel("Maximum value : ");
	max_label.setFont(max_label.getFont().deriveFont(Font.PLAIN));
	minmax_panel.add(max_label);
	max_text_field = new JTextField(10);
	max_text_field.setText((new Float(old_max)).toString());
	max_text_field.selectAll();
	minmax_panel.add(max_text_field);
	minmax_panel.setBorder(BorderFactory.
			       createCompoundBorder(BorderFactory.createTitledBorder("Set Minimum & Maximum"),
						    BorderFactory.createEmptyBorder(5,5,5,5)));
	
	// combo box
	JPanel combo_panel = new JPanel();
	layer_box = new JComboBox(color_maps);
	layer_box.setFont(layer_box.getFont().deriveFont(Font.PLAIN));
	int index = getComboIndex(old_color_map);
	layer_box.setSelectedIndex(index);
	layer_box.addActionListener(this);
	combo_panel.add(layer_box);
	combo_panel.setBorder(BorderFactory.
			      createCompoundBorder(BorderFactory.createTitledBorder("Set Color Map"),
						   BorderFactory.createEmptyBorder(5,5,5,5)));
	
        // create and initialize the buttons
	help_button = new JButton("Help");
	help_button.setFont(help_button.getFont().deriveFont(Font.PLAIN));
	help_button.setMargin(new Insets(1,5,1,5));
        help_button.addActionListener(this);
        cancel_button = new JButton("Cancel");
	cancel_button.setFont(cancel_button.getFont().deriveFont(Font.PLAIN));
	cancel_button.setMargin(new Insets(1,2,1,2));
        cancel_button.addActionListener(this);
        set_button = new JButton("Set");
	set_button.setFont(set_button.getFont().deriveFont(Font.PLAIN));
	set_button.setMargin(new Insets(1,6,1,6));
        set_button.addActionListener(this);
	getRootPane().setDefaultButton(set_button);

        // lay out the buttons from left to right.
        JPanel button_panel = new JPanel();
        button_panel.setLayout(new BoxLayout(button_panel, BoxLayout.LINE_AXIS));
        button_panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        button_panel.add(Box.createHorizontalGlue());
	//button_panel.add(help_button);
	//button_panel.add(Box.createRigidArea(new Dimension(5, 0)));
	button_panel.add(cancel_button);
        button_panel.add(Box.createRigidArea(new Dimension(5, 0)));
        button_panel.add(set_button);

        // put everything together
        Container content_pane = getContentPane();
	content_pane.setLayout(new BoxLayout(content_pane, BoxLayout.Y_AXIS));
	content_pane.add(scale_panel);
	content_pane.add(minmax_panel);
	content_pane.add(combo_panel);
        content_pane.add(button_panel);

        // initialize values
        pack();
	min_text_field.requestFocusInWindow();
        setLocationRelativeTo(location_comp);
    }
    
    /**
     * Fire action vhen the dialog buttons are pressed or the scale option is changed.
     * "Help" button fire no action for now on.
     * "Cancel" button dosn't update the color map with new options.
     * "Set" button updates the color map with new options.
     *
     * @param e action event generated by the dialog.
     */
    public void actionPerformed(ActionEvent e) {
	Object o = e.getSource();
	// handle scale change
        if (lin_button.equals(o)) {
	    lin_button.setSelected(true);
	    log_button.setSelected(false);
	    scale = lin_button.getText();
	}
	else if (log_button.equals(o)) {
	    log_button.setSelected(true);
	    lin_button.setSelected(false);
	    scale = log_button.getText();
        }
	// handle cancel button
	else if (cancel_button.equals(o)) {
	    insertToVec(old_scale, old_min, old_max, old_color_map);
	    ColorMapDialog.dialog.setVisible(false);
	}
	// handle set button
	else if (set_button.equals(o)) {
	    boolean valid = true;
	    try {
		// check if scale is selected
		if (lin_button.isSelected()) {
		    scale = lin_button.getText();
		}
		else if (log_button.isSelected()) {
		    scale = log_button.getText();
		}
		else {
		    throw new RuntimeException();
		}
		// get boundary values
		min = (Float.valueOf(min_text_field.getText())).floatValue();
		max = (Float.valueOf(max_text_field.getText())).floatValue();
		// check boundary values
		if (min > 100000000000.0 || min < 0 || min >= max || max > 100000000000.0 ) {
		    throw new RuntimeException();
		}
		color_map = (String)layer_box.getSelectedItem();
	    }
	    catch (RuntimeException exc) {
		errorMess();
		valid = false;
	    }
	    if (valid) {
		insertToVec(scale, min, max, color_map);
		ColorMapDialog.dialog.setVisible(false);
	    }
	}
    }
    
    /**
     * Update vector with options.
     *
     * @param scale scale (logarithmic or linear).
     * @param min min scale bound.
     * @param max max scale bound.
     * @param cmap color scale map.
     */
    private void insertToVec(String scale, float min, float max, String cmap) {
	values.removeAllElements();
	values.add(scale);
	values.add((new Float(min)).toString());
	values.add((new Float(max)).toString());
	values.add(cmap);
    }
    
    /**
     * Help function - used in constructor. 
     *
     * @param s color scale map. Has to be one of the predefined color maps.
     *
     * @return position of the color scale map in the array of the predefined
     *         color maps.
     */
    private int getComboIndex(String s) {
	int ind = 0;
	for (int i = 0; i < color_maps.length; i++) {
	    if (s.equals(color_maps[i])) {
		ind = i;
	    }
	}
	return ind;
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
