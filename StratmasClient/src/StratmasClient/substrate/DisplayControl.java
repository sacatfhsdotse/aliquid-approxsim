package StratmasClient.substrate;

import java.awt.Insets;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.BorderFactory;
import StratmasClient.StratmasDialog;
import StratmasClient.ClientMainFrame;
import StratmasClient.object.StratmasObject;

/**
 * This class is used to create GUI components used in StratmasEditor.
 */
public class DisplayControl {
    /**
     * Path to the images.
     */
    private static String path = "images/";
    /**
     * Reference to the substrate editor.
     */
    private SubstrateEditor substrateEditor;
    /**
     * The panning button.
     */
    private final JToggleButton panButton = new JToggleButton(new ImageIcon(SubstrateMapDrawer.class.getResource("images/pan24.png")));
    /**
     * The button used to reset the other buttons used for shape creation.
     */
    private final JToggleButton resetButton = new JToggleButton();
    /**
     * The button used to fill the defined areas with the selected color.
     */
    private final JToggleButton fillButton = new JToggleButton(new ImageIcon(SubstrateMapDrawer.class.getResource("images/fillArea.png")));
    /**
     * The button used to remove the last added shape.
     */
    private final JButton undoButton = new JButton(new ImageIcon(SubstrateMapDrawer.class.getResource("images/undo24.gif")));
    /**
     * The button used for circle creation.
     */
    private final JToggleButton circleButton = new JToggleButton(new ImageIcon(SubstrateMapDrawer.class.getResource("images/circle.png")));
    /**
     * The button used for rectangle creation.
     */
    private final JToggleButton rectButton = new JToggleButton(new ImageIcon(SubstrateMapDrawer.class.getResource("images/rectangle.png")));
    /**
     * The button used for polygon creation.
     */
    private final  JToggleButton polygonButton = new JToggleButton(new ImageIcon(SubstrateMapDrawer.class.getResource("images/polygon.png")));
    /**
     * The button used for inserting new point into polygon.
     */
    private final JToggleButton newPointButton = 
        new JToggleButton(new ImageIcon(SubstrateMapDrawer.class.getResource("images/newpoint.png")));
    /**
     * The button used for moving point of a polygon.
     */
    private final JToggleButton movePointButton = 
        new JToggleButton(new ImageIcon(SubstrateMapDrawer.class.getResource("images/movepoint.png")));
    /**
     * The button used for moving a shape.
     */
    private final JToggleButton moveShapeButton = 
        new JToggleButton(new ImageIcon(SubstrateMapDrawer.class.getResource("images/hand.png")));
    /**
     * The menu item used to import process variables.
     */
    private final JMenuItem importPVItem = new JMenuItem("Import Process Variables");
    /**
     * The menu item used to save the initialized process variables and factions.
     */
    private final JMenuItem saveItem = new JMenuItem("Save");
    /**
     * The menu item used to save the initialized process variables and factions.
     */
    private final JMenuItem saveAsItem = new JMenuItem("Save as ...");
    /**
     * The menu item used to continue to simulation.
     */
    private final JMenuItem simulationItem = new JMenuItem("Simulation");
    /**
     * The menu item used to add/remove factions.
     */
    private final JMenuItem createFactionItem = new JMenuItem("Add/remove Factions");
    /**
     * The menu item used to show the options dialog for the color map.
     */
    private final JMenuItem colorMapOptionsItem = new JMenuItem("Color Map Options");

    /**
     * Creates new DisplayControl.
     */
    public DisplayControl(SubstrateEditor substrateEditor) {
        this.substrateEditor = substrateEditor;
    }
    
    /**
     * Creates the navigation panel.
     */
    public JPanel createNavigationPanel() {
        final SubstrateMapDrawer drawer = substrateEditor.getSubstrateDrawer();
        
        // panning button
        JPanel panPanel = new JPanel();
        panButton.setMargin(new Insets(1, 1, 1, 1));
        panButton.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    drawer.setPanningMode((((JToggleButton)e.getSource()).isSelected())? true : false);
                }
            });
        panPanel.add(panButton);
        panPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Panning"), 
                                                              BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        
        // the navigation panel
        JPanel navigationPanel = new JPanel(new BorderLayout());
        navigationPanel.add(panPanel, BorderLayout.WEST);
        navigationPanel.add(drawer.getZoomAndScaleController().getZoomingPanel(),  BorderLayout.CENTER);
        
        return navigationPanel;
    } 
    
    /**
     * Creates the panel for process variables and factions.
     */ 
    public JPanel createPVPanel() {
        JPanel pvPanel = new JPanel(new BorderLayout());
        pvPanel.add(substrateEditor.getProcessVariableHandler().getPanel(), BorderLayout.WEST);
        pvPanel.add(substrateEditor.getFactionHandler().getPanel(), BorderLayout.EAST);
        
        return pvPanel;
    } 
   
    /**
     *  Creates the panel containing of the navigation panel and the panel of process variables and factions.
     */
    public JPanel createNorthPanel() {
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(createPVPanel(), BorderLayout.WEST);
        northPanel.add(new JLabel(), BorderLayout.CENTER);
        northPanel.add(createNavigationPanel(), BorderLayout.EAST);
        northPanel.setBorder(BorderFactory.createTitledBorder(""));
        
        return northPanel;
    }
    
    /**
     * Creates the panel which contains different buttons used to create new shapes as well as to set values to
     * the existing shapes. 
     */
    public JPanel createModeSelectionPanel() {
        final SubstrateMapDrawer drawer = substrateEditor.getSubstrateDrawer();
        final DisplayControl self = this;

        // the button used to reset the buttons for creation of new shapes
        final JToggleButton noneButton = new JToggleButton();
        noneButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    drawer.addActualShapeArea();
                    drawer.setSubstrateMode(SubstrateMapDrawer.SET_UNDEFINED_MODE);
                    drawer.removeShapeMaker();
                }
            });
        // the button used to reset all the buttons
        resetButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    noneButton.doClick();
                    drawer.setSubstrateMode(SubstrateMapDrawer.SET_UNDEFINED_MODE);
                    self.setEnabledShapeCreatorButtons(true);  
                }
            });
        // the button for filling defined areas with the selected color
        fillButton.setToolTipText("Fill area with color");
        fillButton.setMargin(new Insets(1, 1, 1, 1));
        fillButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (drawer.getSubstrateMode() != SubstrateMapDrawer.SET_AREA_VALUE_MODE) {
                        //noneButton.doClick();
                        drawer.addActualShapeArea();
                        drawer.removeShapeMaker();
                        drawer.setSubstrateMode(SubstrateMapDrawer.SET_AREA_VALUE_MODE);
                        //self.setEnabledShapeCreatorButtons(false);
                    }
                    //else {
                    //        drawer.setSubstrateMode(SubstrateMapDrawer.CREATE_AREA_MODE);
                        //self.setEnabledShapeCreatorButtons(true);
                    //}
                }
            });
        fillButton.setEnabled(false);
        // the button used to remove the last added shape
        undoButton.setToolTipText("Remove last added area");
        undoButton.setMargin(new Insets(1, 1, 1, 1));
        undoButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    drawer.addActualShapeArea();
                    drawer.removeLastAddedShape();
                }
            });
        undoButton.setEnabled(false);
        // the circular area button
        circleButton.setToolTipText("Create circular area");
        circleButton.setMargin(new Insets(1, 1, 1, 1));
        circleButton.setEnabled(false);
        circleButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    drawer.addActualShapeArea();
                    drawer.removeShapeMaker();
                    self.setEnabledPolygonButtons(false);
                    drawer.setSubstrateMode(SubstrateMapDrawer.CREATE_CIRCLE_MODE);
                    drawer.setShapeMaker(new CircleMaker(drawer));
                    // disable panning
                    if (panButton.isSelected()) {
                        panButton.doClick();
                    }
                }
            });
        // the rectangular area button 
        rectButton.setToolTipText("Create rectangular area");
        rectButton.setMargin(new Insets(1, 1, 1, 1));
        rectButton.setEnabled(false);
        rectButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                    drawer.addActualShapeArea();
                    drawer.removeShapeMaker();
                    self.setEnabledPolygonButtons(false);
                    drawer.setSubstrateMode(SubstrateMapDrawer.CREATE_RECTANGLE_MODE);
                    drawer.setShapeMaker(new RectangleMaker(drawer));
                    // disable panning
                    if (panButton.isSelected()) {
                        panButton.doClick();
                    }
                }
            });
        // the polygonial area button 
        polygonButton.setToolTipText("Create polygonial area");
        polygonButton.setMargin(new Insets(1, 1, 1, 1));
        polygonButton.setEnabled(false);
        polygonButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                    drawer.addActualShapeArea();
                    drawer.removeShapeMaker();
                    self.setEnabledPolygonButtons(true);
                    if (drawer.getShapeMaker() != null) {
                        drawer.getShapeMaker().clearAll();
                    }
                    drawer.setSubstrateMode(SubstrateMapDrawer.CREATE_POLYGON_MODE);
                    drawer.setShapeMaker(new PolygonMaker(drawer));
                    // disable panning
                    if (panButton.isSelected()) {
                        panButton.doClick();
                    }
                }
            });
        // the button for adding a new point into a polygon// the button used to create region shapes
        newPointButton.setToolTipText("Add new point to the area");
        newPointButton.setMargin(new Insets(1, 1, 1, 1));
        newPointButton.setEnabled(false);
        newPointButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (drawer.getSubstrateMode() != SubstrateMapDrawer.INSERT_POINT_MODE) { 
                            drawer.setSubstrateMode(SubstrateMapDrawer.INSERT_POINT_MODE);
                        // check if the polygonial is completed
                        if (drawer.getShapeMaker() != null) {
                            PolygonMaker pMaker = (PolygonMaker)drawer.getShapeMaker();
                            pMaker.addLastLine();
                        }
                        // disable panning
                        if (panButton.isSelected()) {
                            panButton.doClick();
                        }
                    }
                }
            });
        // the button for changing location of a point in a polygon
        movePointButton.setToolTipText("Change the area by moving the desired point");
        movePointButton.setMargin(new Insets(1, 1, 1, 1));
        movePointButton.setEnabled(false);
        movePointButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (drawer.getSubstrateMode() != SubstrateMapDrawer.MOVE_POINT_MODE) { 
                            drawer.setSubstrateMode(SubstrateMapDrawer.MOVE_POINT_MODE);
                        // check if the polygonial is completed
                        if (drawer.getShapeMaker() != null) {
                            PolygonMaker pMaker = (PolygonMaker)drawer.getShapeMaker();
                            pMaker.addLastLine();
                        }
                        // disable panning
                        if (panButton.isSelected()) {
                            panButton.doClick();
                        }
                    }
                }
            });
        // the button for moving an area 
        moveShapeButton.setToolTipText("Move the area");
        moveShapeButton.setMargin(new Insets(1, 1, 1, 1));
        moveShapeButton.setEnabled(false);
        moveShapeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (drawer.getSubstrateMode() != SubstrateMapDrawer.MOVE_POLYGON_MODE) { 
                            drawer.setSubstrateMode(SubstrateMapDrawer.MOVE_POLYGON_MODE);
                        // check if the polygonial is completed
                        if (drawer.getShapeMaker() != null && drawer.getShapeMaker() instanceof PolygonMaker) {
                            PolygonMaker pMaker = (PolygonMaker)drawer.getShapeMaker();
                            pMaker.addLastLine();
                        }
                        // disable panning
                        if (panButton.isSelected()) {
                            panButton.doClick();
                        }
                    }
                }
            });        
           
        ButtonGroup shapeCreatorButtons = new ButtonGroup();
        shapeCreatorButtons.add(noneButton);
        shapeCreatorButtons.add(fillButton);
        shapeCreatorButtons.add(circleButton);
        shapeCreatorButtons.add(rectButton);
        shapeCreatorButtons.add(polygonButton);        
        shapeCreatorButtons.add(newPointButton);
        shapeCreatorButtons.add(movePointButton);
        shapeCreatorButtons.add(moveShapeButton);
        
//         ButtonGroup sButtons = new ButtonGroup();
//         sButtons.add(fillButton);
//         sButtons.add(circleButton);
//         sButtons.add(rectButton);
//         sButtons.add(polygonButton);        
//         sButtons.add(moveShapeButton);
        
        JPanel gridPanel = new JPanel(new GridLayout(1, 14, 2, 2));
        gridPanel.add(fillButton);
        gridPanel.add(new JLabel());
        gridPanel.add(circleButton);
        gridPanel.add(rectButton);
        gridPanel.add(polygonButton);
        gridPanel.add(newPointButton);
        gridPanel.add(movePointButton);
        gridPanel.add(new JLabel());
        gridPanel.add(moveShapeButton);
        gridPanel.add(undoButton);
        
        JPanel finalPanel = new JPanel(new BorderLayout());
        finalPanel.add(gridPanel, BorderLayout.WEST);
        finalPanel.add(new JLabel(), BorderLayout.CENTER);
                
        return finalPanel;
    }
    
    /**
     * Enables / disables the buttons used for shape sreation.
     */
    public void setEnabledShapeCreatorButtons(boolean enable) {
        circleButton.setEnabled(enable);
        rectButton.setEnabled(enable);
        polygonButton.setEnabled(enable);
        newPointButton.setEnabled(false);
        movePointButton.setEnabled(false);
        moveShapeButton.setEnabled(enable);                            
    }
    
     /**
     * Enables / disables the buttons used for polygon modification.
     */
    public void setEnabledPolygonButtons(boolean enable) {
        newPointButton.setEnabled(enable);
        movePointButton.setEnabled(enable);
    }
    
    /**
     * Restes the shape creator buttons.
     */
    public void resetShapeCreatorButtons() {
        resetButton.doClick();
        if (fillButton.isSelected()) {
            fillButton.doClick();
        }
    }

    /**
     * Creates the panel containing text fields.
     */
    public JPanel createTextFieldPanel() {
        // text field for the coordinate under the mouse cursor 
        JTextField infoTextField = substrateEditor.getSubstrateDrawer().getCurrentLocationTextField();
        infoTextField.setEditable(false);
        
        // text field for the region under the mouse cursor 
        JTextField regionTextField = substrateEditor.getSubstrateDrawer().getCurrentRegionTextField();
        regionTextField.setEditable(false);
        
        JPanel textFieldPanel = new JPanel(new GridLayout(1, 2));
        textFieldPanel.add(infoTextField);
        textFieldPanel.add(regionTextField);
        
        return textFieldPanel;
    }
    
    /**
     * Creates the menu used in the editor.
     */
    public JMenuBar createMenuBar() {
        final SubstrateEditor sEditor = substrateEditor;
        
        // the "File" menu
        importPVItem.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent event) {
                    SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                ImportPVDialog.showDialog(sEditor);
                            }
                        });
                }
            });
        JMenuItem loadItem = new JMenuItem("Load ...");
        loadItem.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent event) {
                    Thread worker = new Thread() {
                            public void run() {
                                sEditor.readValuesFromFile();
                            }
                        };
                    worker.start();
                }
            });
        saveItem.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent event) {
                    Thread worker = new Thread() {
                            public void run() {
                                sEditor.saveValuesToExistingFile();
                            }
                        };
                    worker.start();
                }
            });
        saveItem.setEnabled(false);
        saveAsItem.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent event) {
                    Thread worker = new Thread() {
                            public void run() {
                                sEditor.saveValuesToNewFile();
                            }
                        };
                    worker.start();
                }
            });
        saveAsItem.setEnabled(false);
        simulationItem.setForeground(Color.BLUE);
        simulationItem.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent event) {
                    StratmasDialog.showProgressBarDialog(null, "Proceeding to simulation ...");
                    Thread thread = new Thread() {
                            public void run() {
                                // exit the editor
                                sEditor.exit();
                                // show main frame
                                final ClientMainFrame mainFrame = sEditor.getClient().getClientMainFrame();
                                SwingUtilities.invokeLater(new Runnable() {
                                        public void run() {
                                            mainFrame.pack();
                                            mainFrame.setSize(400, 600);
                                            mainFrame.setVisible(true);
                                        }
                                    });
                                // create map and timeline
                                StratmasObject root = sEditor.getClient().getRootObject();
                                if (root != null) {
                                    StratmasObject sim = (StratmasObject)root.children().nextElement();
                                    if (sim != null && sim.getType().canSubstitute("Simulation")) {
                                        sEditor.getClient().importSimulation(sim);
                                    }
                                }
                                StratmasDialog.quitProgressBarDialog();
                            }
                        };
                    thread.start();
                }
            });
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent event) {
                    if (JOptionPane.showConfirmDialog(null, "Do you want to exit?", "Exiting ...", 
                                                      JOptionPane.YES_NO_OPTION) == 0) {
                        sEditor.getSubstrateDrawer().doDispose();
                    }
                }
            });
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(importPVItem);
        fileMenu.add(loadItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(simulationItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);// the button used to create region shapes
        final JToggleButton regionButton = new JToggleButton(new ImageIcon(SubstrateMapDrawer.class.getResource(path+"shape.png")));
        
        // the "Options" menu
        createFactionItem.setEnabled(false);
        createFactionItem.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent event) {
                    CreateFactionDialog.showDialog(sEditor.getSubstrateDrawer().getFrame(), sEditor.getFactionHandler());
                }
            });
        colorMapOptionsItem.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent event) {
                    ColorMapDialog.showDialog(sEditor.getProcessVariable(), sEditor.getSubstrateDrawer().getFrame(),
                                              sEditor.getColorChooser());
                }
            });
        colorMapOptionsItem.setEnabled(false);
        JMenu optionsMenu = new JMenu("Options");
        optionsMenu.add(createFactionItem);
        optionsMenu.add(colorMapOptionsItem);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(optionsMenu);
    
        return menuBar;
    }
    
    /**
     * Enables/disables the button used to fill shapes with color. 
     */
    public void enableFillButton(boolean enable) {
        fillButton.setEnabled(enable);
    }
    
    /**
     * Enables/disables the button used to remove last added shape. 
     */
    public void enableUndoButton(boolean enable) {
        undoButton.setEnabled(enable);
    }

    /**
     * Enables/disables the menu item used for import of process variables. 
     */
    public void enableImportPVItem(boolean enable) {
        importPVItem.setEnabled(enable);
    }
    
    /**
     * Enables/disables the menu item used to save the initialized process variables. 
     */
    public void enableSaveItem(boolean enable) {
        saveItem.setEnabled(enable);
    }
    
    /**
     * Enables/disables the menu item used to save the initialized process variables. 
     */
    public void enableSaveAsItem(boolean enable) {
        saveAsItem.setEnabled(enable);
    } 
    
    /**
     * Enables/disables the menu item used to create factions. 
     */
    public void enableCreateFactionItem(boolean enable) {
        createFactionItem.setEnabled(enable);
    }
    
    /**
     * Enables/disables the menu item used to open dialog for the color map options. 
     */
    public void enableColorMapOptionsItem(boolean enable) {
        colorMapOptionsItem.setEnabled(enable);
    }
    
}
