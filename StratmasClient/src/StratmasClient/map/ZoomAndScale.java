package StratmasClient.map;

import java.util.Hashtable;
import java.lang.Object;
import java.lang.Math;
import java.lang.String;
import java.lang.RuntimeException;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import StratmasClient.BoundingBox;
import StratmasClient.StratmasDialog;

/**
 * Zomming and scaling functionalities.
 * <p>
 * Features implemented in this class are: <br>
 * 1. Zooming feature - the region can be zoomed from the largest scale, which is the longitude and latitude of its bounds obtained direct
 * from the ESRI file, to the smallest scale, which is the size of one cell. <br>
 * 2. Scalig feature - the actual scale is shown et each time.
 * 
 * @version 1.0
 * @author Amir Filipovic
 */
public class ZoomAndScale implements ActionListener, ChangeListener {
    /**
     * Minimum value for the slider.
     */
    private static final int MIN_VALUE = 0;
    /**
     * Maximum value for the slider.
     */
    private static final int MAX_VALUE = 100;
    /**
     * Initial value for the slider.
     */
    private static final int INIT_VALUE = 0;
    /**
     * The slider used for zooming.
     */
    private JSlider slider;
    /**
     * The current value of the slider.
     */
    private int slide_index = 0;
    /**
     * The current scale of the map.
     */
    private int act_scale;
    /**
     * GUI component showing the current scale.
     */
    private JTextField scale_field;
    /**
     * Reference to the map.
     */
    private BasicMapDrawer drawer;
    /**
     * Parameter nedded for scale calculation.
     */
    private double a = 0;
    /**
     * Screen distance (m)
     */
    private double dist_screen;
    /**
     * The panel containing the zooming and scale components.
     */
    private JPanel zoomAndScalePanel;
    /**
     * The panel containing the zooming components.
     */
    private JPanel zpanel;

    /**
     * Create zooming and scaling components.
     * 
     * @param drawer the actual map.
     * @param orientation orientation of the slider (JSlider.HORIZONTAL or JSlider.VERTICAL).
     */
    public ZoomAndScale(BasicMapDrawer drawer, int orientation) {
        // reference to the main map
        this.drawer = drawer;
        this.drawer.setZoomAndScale(this);

        // create zoom & scale panel
        createZoomAndScalePanel(orientation);
    }

    /**
     * Create the GUI and show it.
     */
    public void createAndShowGUI() {
        // Create and set up the window.
        JFrame frame = new JFrame("Position Map");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set up the content pane.
        zoomAndScalePanel.setOpaque(true); // content panes must be opaque
        frame.setContentPane(zoomAndScalePanel);

        // Display the window.
        frame.setSize(150, 300);
        frame.setResizable(true);
        frame.setVisible(true);
    }

    /**
     * Creates the panel which contains the zooming and scale components.
     * 
     * @param orientation orientation of the slider ie. JSlider.HORIZONTAL or JSlider.VERTICAL.
     */
    private void createZoomAndScalePanel(int orientation) {
        final ZoomAndScale self = this;
        // get the label images
        JLabel zoom_in = new JLabel(new ImageIcon(
                ZoomAndScale.class.getResource("images/zoom_in16.gif")));
        zoom_in.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                self.changeSliderValue(1);
            }
        });
        JLabel zoom_out = new JLabel(new ImageIcon(
                ZoomAndScale.class.getResource("images/zoom_out16.gif")));
        zoom_out.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                self.changeSliderValue(1);
            }
        });

        // configure the slider
        slider = new JSlider(orientation, MIN_VALUE, MAX_VALUE, INIT_VALUE);
        slider.addChangeListener(this);
        slider.setMajorTickSpacing(25);
        slider.setPaintTicks(true);
        Hashtable labelTable = new Hashtable();
        labelTable.put(new Integer(MAX_VALUE), new JLabel());
        labelTable.put(new Integer(MAX_VALUE - 25), new JLabel());
        labelTable.put(new Integer(MAX_VALUE - 50), new JLabel());
        labelTable.put(new Integer(MAX_VALUE - 75), new JLabel());
        labelTable.put(new Integer(0), new JLabel());
        slider.setLabelTable(labelTable);
        slider.setPaintLabels(true);
        slider.setInverted(true);
        slider.setOpaque(false);

        // map zooming
        zpanel = new JPanel();
        if (orientation == JSlider.VERTICAL) {
            zoom_out.setAlignmentX(Component.RIGHT_ALIGNMENT);
            zoom_in.setAlignmentX(Component.RIGHT_ALIGNMENT);
            zpanel.setLayout(new BoxLayout(zpanel, BoxLayout.Y_AXIS));
            zpanel.add(zoom_out);
            zpanel.add(Box.createRigidArea(new Dimension(0, 3)));
            zpanel.add(slider);
            zpanel.add(Box.createRigidArea(new Dimension(0, 3)));
            zpanel.add(zoom_in);
        } else {
            zpanel.setLayout(new BoxLayout(zpanel, BoxLayout.X_AXIS));
            zpanel.add(zoom_in);
            zpanel.add(Box.createRigidArea(new Dimension(3, 0)));
            zpanel.add(slider);
            zpanel.add(Box.createRigidArea(new Dimension(3, 0)));
            zpanel.add(zoom_out);
        }
        zpanel.setOpaque(false);
        zpanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createTitledBorder("Zooming"), BorderFactory
                .createEmptyBorder(2, 2, 2, 2)));

        // map scaling
        JPanel spanel = new JPanel();
        spanel.setLayout(new BorderLayout());
        scale_field = new JTextField(10);
        scale_field.addActionListener(this);
        spanel.add(scale_field, BorderLayout.CENTER);
        spanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createTitledBorder("Map Scale"), BorderFactory
                .createEmptyBorder(2, 2, 2, 2)));

        // set layout
        zoomAndScalePanel = new JPanel(new BorderLayout());
        zoomAndScalePanel.add(zpanel, BorderLayout.CENTER);
        zoomAndScalePanel.add(spanel, BorderLayout.SOUTH);
    }

    /**
     * Returns the panel for zooming and scale.
     */
    public JPanel getPanel() {
        return zoomAndScalePanel;
    }

    /**
     * Returns the panel for zooming.
     */
    public JPanel getZoomingPanel() {
        return zpanel;
    }

    /**
     * Computes new orthographic scaled bounds for the main map and updates the map scale. Used when zooming slider changes.
     * 
     * @param i scaled value between 0 and 1.
     */
    private void setOrthoBounds(double i) {
        double scale = calcScale(i);
        // compute new orthographics view bounds
        BoundingBox box = drawer.getBoundingBox();
        double dx1 = box.getXmax() - box.getXmin();
        double dy1 = box.getYmax() - box.getYmin();
        double dx2 = dx1 * scale;
        double dy2 = dy1 * scale;
        double ort_xmins = box.getXmin() + (dx1 - dx2) / 2;
        double ort_xmaxs = box.getXmax() - (dx1 - dx2) / 2;
        double ort_ymins = box.getYmin() + (dy1 - dy2) / 2;
        double ort_ymaxs = box.getYmax() - (dy1 - dy2) / 2;
        // update the main map
        drawer.setScaledBoundingBox(new BoundingBox(ort_xmins, ort_ymins,
                ort_xmaxs, ort_ymaxs, drawer.getProjection()));
        // compute constants for scaling function
        a = drawer.getMinRange() / dx1 - 1.0;
        // update screen distance
        dist_screen = drawer.getViewWidth() * MapConstants.ONE_INCH
                / Toolkit.getDefaultToolkit().getScreenResolution();
        // update the map scale
        act_scale = compScale(ort_xmins, (ort_ymins + ort_ymaxs) / 2,
                              ort_xmaxs, (ort_ymins + ort_ymaxs) / 2);
        scale_field.setText("1:" + act_scale);
        zoomAndScalePanel.updateUI();
    }

    /**
     * TODO
     */
    private double calcScale(double i) {
        return a * Math.pow(i, 0.25) + 1.0;
    }

    /**
     * TODO
     */
    public double getCurrentScale() {
        return calcScale(getScale());
    }

    /**
     * Computes actual map scale.
     * 
     * @param xmin minimum x value.
     * @param ymin minimum y value.
     * @param xmax maximum x value.
     * @param ymax maximum y value.
     */
    private int compScale(double xmin, double ymin, double xmax, double ymax) {
        // distance on the surface of the earth (km)
        double dist_earth = drawer.getProjection().getDistanceGC(xmin, ymin,
                                                                 xmax, ymax);
        int scale = (int) (dist_earth / dist_screen);
        return scale;
    }

    /**
     * Increases the value of the slider and updates the map.
     * 
     * @param i increment of the slider. Negative to decrement.
     */
    public void changeSliderValue(int i) {
        slide_index += i;
        if (slide_index > MAX_VALUE) {
            slide_index = MAX_VALUE;
        } else if (slide_index < MIN_VALUE) {
            slide_index = MIN_VALUE;
        }
        // set the new slider value
        slider.setValue(slide_index);
        update();
    }

    /**
     * Retrieves the current map scale (0 .. 1).
     * 
     * @return the scale.
     */
    public double getScale() {
        return (double) slide_index / (double) MAX_VALUE;
    }

    /**
     * Updates map scale.
     */
    public void update() {
        setOrthoBounds(getScale());
    }

    /**
     * Action is fired when a new scale value is entered.
     * 
     * @param ae event occured when new scale is entered.
     * @throws RuntimeException if the entered value is not valid.
     */
    public void actionPerformed(ActionEvent ae) {
        String text = scale_field.getText();
        // get max scale, min scale and screen distance
        double max_range = drawer.getMaxRange();
        double min_range = drawer.getMinRange();
        try {
            char one = text.charAt(0);
            char colon = text.charAt(1);
            // invalid string
            if (one != '1' || colon != ':') {
                throw new RuntimeException();
            }
            // parse the scale number
            else {
                String number = text.substring(2);
                int scale = (Integer.valueOf(number)).intValue();
                // actual range
                double act_range = scale * dist_screen;
                // check if the bounds are respected
                if (act_range < min_range || act_range > max_range) {
                    throw new RuntimeException();
                }
                // get the scaling value between 0.0 and 1.0
                double i = Math.pow((act_range - max_range)
                        / (min_range - max_range), 4);
                int new_slider_value = (int) Math.round(i * MAX_VALUE);
                // set the new slider value
                slider.setValue(new_slider_value);
                //
                update();
            }
        } catch (RuntimeException e) {
            errorMess();
            scale_field.setText("1:" + act_scale);
            zoomAndScalePanel.updateUI();
        }
    }

    /**
     * Action is fired when the slider status is changed. The main map is updated accordingly.
     * 
     * @param e event occured when the slider is used.
     */
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
        int val = (int) source.getValue();
        // get the scale
        double scale = (double) val / (double) MAX_VALUE;
        // update orthogaphical bounds in main map and compute actual map scale
        setOrthoBounds(scale);
        // update slider
        slide_index = val;
    }

    /**
     * Error message.
     */
    private void errorMess() {
        Object[] options = { "OK", "Help" };
        StratmasDialog.showOptionDialog(new JFrame("ERROR!!!"),
                                        "Invalid input value!",
                                        "Error message",
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.ERROR_MESSAGE, null,
                                        options, options[0]);
    }

}
