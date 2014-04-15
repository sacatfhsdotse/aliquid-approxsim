package StratmasClient;

import java.util.Enumeration;
import java.util.Vector;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import StratmasClient.object.StratmasEventListener;
import StratmasClient.object.StratmasEvent;


/**
 * This class contains necessary parameters for the actual screen device. 
 *
 * @version 1.0
 * @author Amir Filipovic 
 * @see <code>GraphicsEnvironment</code>
 */
public class Configuration {
    /**
     * List of listeners.
     */
    public static Vector<StratmasEventListener> listeners = new Vector<StratmasEventListener>();
    /**
     * Indicator for geodetic coordinates (latitude and longitude).
     */
    public static final int GEODETIC = 0;
    /**
     * Indicator for MGRS coordinates.
     */
    public static final int MGRS = 1;
    /**
     * Actual coodinate system.
     */
    private static int coordinate_system = GEODETIC;
    /**
     * Maximum display resolution (pixels) for the actual screen.
     */
    //private static double x_max_resolution;
    
    /**
     * Actual display resolution (pixels) for the actual screen.
     */
    //private static double x_act_resolution;
    
    /**
     * Dot pitch for the actual screen (value in meters).
     */
    //private static double dot_pitch = 0.0002;
    
    /**
     * GUI variables
     */
    private static JDialog dialog = new JDialog();
    // private static final JTextField max_text_field= new JTextField(10);
//     private static final JTextField act_text_field= new JTextField(10);
//     private static final JTextField dp_text_field= new JTextField(10);
    private static final JRadioButton geo_button = new JRadioButton("Geodetic");
    private static final JRadioButton mgrs_button = new JRadioButton("MGRS");
    
    /**
     * The flag used to check if the configuration is initialized. 
     */
    //private static boolean initialized = false;


    /**
     * Returns the Configuration object.
     */
    protected static Configuration getConfiguration() {
        return new Configuration();
    }
    
    /**
     * Initializes the configuration.
     */
    //private static void init() {
    //setMaxResolution();
    //setResolution();
    //        initialized = true;
    //}
    
    /**
     * Sets the coordinate system.
     */
    public static void setCoordinateSystem(int coord) {
        if (coord == GEODETIC) {
            coordinate_system = GEODETIC;
        }
        else if (coord == MGRS) {
            coordinate_system = MGRS;
        }
    }

    /**
     * Returns the coordinate system.
     */
    public static int getCoordinateSystem() {
        return coordinate_system;
    }
    
    /**
     * Sets maximum resolution.
     */
    // public static void setMaxResolution(double resolution) {
//         x_max_resolution = resolution;
//     }
    
    /**
     * Sets maximum resolution.
     */
    // public static void setMaxResolution() {
//         GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//         GraphicsDevice gs = ge.getDefaultScreenDevice();
//         DisplayMode[] dm = gs.getDisplayModes();
//         int maxr = 0;
//         for (int i = 0; i < dm.length; i++) {
//             if (dm[i].getWidth() > maxr) {
//                 maxr = dm[i].getWidth();
//             }
//         }
//         x_max_resolution = maxr;
//     }
    
    /**
     * Sets actual resolution.
     */
    // public static void setResolution(double resolution) {
//         x_act_resolution = resolution;
//     }
    
    /**
     * Sets actual resolution.
     */
    // public static void setResolution() {
//         GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//         GraphicsDevice gs = ge.getDefaultScreenDevice();
//         DisplayMode dm = gs.getDisplayMode();
//         x_act_resolution = dm.getWidth();
//     }
    
    /**
     * Sets dot pitch.
     */
    // public static void setDotPitch(double dpitch) {
//         dot_pitch = dpitch;
//     }

   /**
    * Returns maximum resolution.
    */
   //  public static double getMaxResolution() {
//         if (!initialized) {
//             init();
//         }
//         return x_max_resolution;
//     }

    /**
     * Returns actual resolution.
     */
    // public static double getResolution() {
//         if (!initialized) {
//             init();
//         }
//         return x_act_resolution;
//     }
    
    /**
     * Returns dot pitch.
     */
    // public static double getDotPitch() {
//         if (!initialized) {
//             init();
//         }
//         return dot_pitch;
//     } 

    /**
     * Creates the panel for the conversion of the coordinates.
     */
    private static JPanel createConversionPanel() {
        // create panel
        JPanel conv_panel = new JPanel();
        conv_panel.setLayout(new GridLayout(1,2,2,2));
        geo_button.setFont(geo_button.getFont().deriveFont(Font.PLAIN));
        mgrs_button.setFont(mgrs_button.getFont().deriveFont(Font.PLAIN));
        conv_panel.add(geo_button);
        conv_panel.add(mgrs_button);
        conv_panel.setBorder(BorderFactory.createCompoundBorder
                             (BorderFactory.createTitledBorder("Coordinate System"),
                              BorderFactory.createEmptyBorder(2,5,2,5)));
        
        ButtonGroup conv_buttons = new ButtonGroup();
        conv_buttons.add(geo_button);
        conv_buttons.add(mgrs_button);
        
        if (getCoordinateSystem() == GEODETIC) {
            geo_button.setSelected(true);  
        }
        else if (getCoordinateSystem() == MGRS) {
            mgrs_button.setSelected(true);  
        }

        return conv_panel;
    }
    
    /**
     * Creates the panel for the screen configuration.
     */
    // private static JPanel createScreenConfigurationPanel(JFrame frame) {
//         //
//         if (!initialized) {
//             init();
//         }
//         // create components
//         JLabel max_res = new JLabel("Maximum horizontal resolution : ");
//         max_res.setFont(max_res.getFont().deriveFont(Font.PLAIN));
//         JLabel act_res = new JLabel("Actual horizontal resolution : ");
//         act_res.setFont(act_res.getFont().deriveFont(Font.PLAIN));
//         JLabel dp = new JLabel("Dot Pitch (mm) : ");
//         dp.setFont(dp.getFont().deriveFont(Font.PLAIN));
//             max_text_field.setText((new Integer((int)x_max_resolution)).toString());
//         act_text_field.setText((new Integer((int)x_act_resolution)).toString());
//         dp_text_field.setText((new Double(dot_pitch*1000)).toString());
        
//         // set panel
//         JPanel cpanel = new JPanel();
//         cpanel.setLayout(new BoxLayout(cpanel, BoxLayout.LINE_AXIS));
//         JPanel label_panel = new JPanel();
//         label_panel.setLayout(new GridLayout(3,1,2,2));
//         label_panel.add(max_res);
//         label_panel.add(act_res);
//         label_panel.add(dp);
//         JPanel text_panel = new JPanel();
//         text_panel.setLayout(new GridLayout(3,1,2,2));
//         text_panel.add(max_text_field);
//         text_panel.add(act_text_field);
//         text_panel.add(dp_text_field);
//         cpanel.add(label_panel);
//         cpanel.add(text_panel);
//         cpanel.setBorder(BorderFactory.createCompoundBorder
//                          (BorderFactory.createTitledBorder("Graphic Configuration"),
//                           BorderFactory.createEmptyBorder(2,5,2,5)));
        
//         return cpanel;
//     }
    
    
    /**
     * Creates the configuration panel.
     *
     * @param frame the frame for the dialog.
     * @param dialog the dialog to show up.
     */
    private static JPanel createButtonPanel(JFrame frame, JDialog dialog) {
        final JDialog fdialog = dialog;
        JButton cancel_button = new JButton("Cancel");
        cancel_button.setFont(cancel_button.getFont().deriveFont(Font.PLAIN));
        cancel_button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    fdialog.setVisible(false);
                    fdialog.dispose();
                }
            });
        JButton ok_button = new JButton("OK");
        ok_button.setFont(ok_button.getFont().deriveFont(Font.PLAIN));
        ok_button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    //  try {
                    //                         // check screen configuration
                    //                         String maxs = max_text_field.getText();
                    //                         String acts = act_text_field.getText();
                    //                         String dps = dp_text_field.getText();
                    //                         int maxi = Integer.parseInt(maxs);
                    //                         int acti = Integer.parseInt(acts);
                    //                         double dpi = Double.parseDouble(dps);
                    //                         if (maxi > 0 && acti > 0 && dpi > 0) {
                    //                             Configuration.setMaxResolution(maxi);
                    //                             Configuration.setResolution(acti);
                    //                             Configuration.setDotPitch(dpi/1000);
                    //                             // update the maps
                    //                             Hashtable maps = Visualizer.getMaps();
                    //                             for (Enumeration en = maps.elements(); en.hasMoreElements(); ) {
                    //                                 ((StratMap)en.nextElement()).getZoomAndScale().update();
                    //                             }
                    //                             // check coordinates
                    if (geo_button.isSelected() && Configuration.getCoordinateSystem() == Configuration.MGRS) {
                        Configuration.setCoordinateSystem(Configuration.GEODETIC);
                        Configuration.fireEventOccured();
                    }
                    else if (mgrs_button.isSelected() && Configuration.getCoordinateSystem() == Configuration.GEODETIC) {
                        Configuration.setCoordinateSystem(Configuration.MGRS);
                        Configuration.fireEventOccured();
                    }
                    //}
                    //         else {
                    //                             throw new RuntimeException();
                    //                         }
                    //                     }
                    //                     catch (RuntimeException e) {
                    //                         if (Configuration.getCoordinateSystem() == Configuration.GEODETIC) {
                    //                             geo_button.setSelected(true);    
                    //                         }
                    //                         else {
                    //                             mgrs_button.setSelected(true);  
                    //                         }
                    //                         JOptionPane.showMessageDialog(fframe, "Configuration not valid.", "Inane error",
                    //                                                       JOptionPane.ERROR_MESSAGE);
                    //                     }
                    //
                    fdialog.setVisible(false);
                    fdialog.dispose();
                }
            });        
        
        JPanel button_panel = new JPanel();
        button_panel.setLayout(new BoxLayout(button_panel, BoxLayout.LINE_AXIS));
        button_panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        button_panel.add(Box.createHorizontalGlue());
        button_panel.add(cancel_button);
        button_panel.add(Box.createRigidArea(new Dimension(5, 0)));
        button_panel.add(ok_button);
        //
        return button_panel;
    }


    /**
     * Shows the dialog.
     */
    public static void showConfigurationFrame(JFrame frame) {
        // 
        dialog.setTitle("Preferences");
        // add coordinate panel
        JPanel coord_panel = createConversionPanel();
        // add screen configuration
        //JPanel screen_panel = createScreenConfigurationPanel(frame);
        // add the buttons
        JPanel button_panel = createButtonPanel(frame, dialog);
        
        // add all the conponents to the dialog               
        JPanel content_pane = new JPanel();
        content_pane.setLayout(new BoxLayout(content_pane, BoxLayout.PAGE_AXIS));
        content_pane.add(coord_panel);
        //content_pane.add(screen_panel);
        content_pane.add(button_panel);
        content_pane.setOpaque(true);
        dialog.setContentPane(content_pane);
        
        // show it
        dialog.setSize(new Dimension(300, 150));
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }
    
    /**
     * Adds a new listener to the list.
     */
    public static void addStratmasListener(StratmasEventListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Removes a listener from the list.
     */
    public static void removeStratmasListener(StratmasEventListener listener) {
        listeners.remove(listener);
    } 
    
    /**
     * Updates the listeners with new event.
     */
    public static void fireEventOccured() {
        for (Enumeration<StratmasEventListener> e = listeners.elements(); e.hasMoreElements(); ) {
            Object obj = e.nextElement();
            if (obj != null) {
                ((StratmasEventListener)obj).eventOccured(StratmasEvent.
                                                          getCoordSystemChanged(Configuration.getConfiguration()));
            }
        }
    }
    
    
}
