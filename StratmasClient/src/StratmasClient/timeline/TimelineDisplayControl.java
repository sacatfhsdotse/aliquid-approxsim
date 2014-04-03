package StratmasClient.timeline;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector; 
import java.awt.Font;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JToolBar;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import StratmasClient.Client;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasList;
import StratmasClient.object.StratmasObjectFactory;
import StratmasClient.object.type.TypeFactory;
import StratmasClient.object.type.Type;
import StratmasClient.map.MapConstants;
import StratmasClient.filter.ActivityTimeFilter;
import StratmasClient.filter.ActivityResourceFilter;
import StratmasClient.filter.MilitaryCodeFilter;
import StratmasClient.filter.TypeFilter;
import StratmasClient.filter.IdentifierFilter;

/**
 * This class implements the various panels used with the timeline.
 */
public class TimelineDisplayControl {
    /**
     * Reference to the timeline.
     */
    private Timeline timeline;
    /**
     * Displays the time pointed by the mouse cursor in the timeline. 
     */
    private JLabel pointedTimeLabel = new JLabel();
    /**
     * Displays the time step.
     */
    private JLabel deltatLabel = new JLabel();
    /**
     * Displays the start time of the simulation.
     */
    private JLabel startTimeLabel = new JLabel();
    /**
     * Displays the current simulation time.
     */
    private JLabel currentTimeLabel = new JLabel();
    /**
     * The menu for the resource selection with respect ti its size.
     */
    final JMenu forceMenu = new JMenu("Select");
    /**
     * The menu for the resource selection with respect ti its mobility.
     */
    final JMenu mobileMenu = new JMenu("Select");
    /**
     * The slider used in the timeline.
     */
    final JSlider timelineSlider = new JSlider(0, 0, 0);
    /**
     * The check box used to select/unselect all times in the timeline.
     */
    JCheckBox selectionBox = new JCheckBox("All Times");

    /**
     * Creates new TimelineDisplayControl.
     */
    public TimelineDisplayControl(Timeline timeline) {
        this.timeline = timeline;
    }
    
    /**
     * Creates the panel for selecting all times in the timeline for prenumeration.
     */
    private JPanel createAllTimesSelectionPanel() {
        final Timeline tline = timeline;
        JPanel selectionPanel = new JPanel(new GridLayout(1, 1, 2, 2));
        selectionBox.setFont(selectionBox.getFont().deriveFont(Font.PLAIN));
        selectionBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JCheckBox thisBox = (JCheckBox)e.getSource();
                    // all times selected
                    if (thisBox.isSelected()) {
                        long startTime = (tline.getRelativeCurrentTime() == 0)? tline.getDT() :  tline.getRelativeCurrentTime();
                        tline.updatePrenumerations(TimelineConstants.SELECT, new TimeInterval(tline.getDT(), startTime, Long.MAX_VALUE));
                        if (tline.getClient() != null) {
                            tline.notifyClient();
                        }
                    }
                    // no times selected
                    else {
                        tline.updatePrenumerations(TimelineConstants.DELETE, new TimeInterval(tline.getDT(), 0, Long.MAX_VALUE));        
                    }
                }
            });
        selectionPanel.add(selectionBox);

        TitledBorder timeBorder = BorderFactory.createTitledBorder("Time Selection");
        selectionPanel.setBorder(BorderFactory.createCompoundBorder(timeBorder, BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        selectionPanel.setPreferredSize(timeBorder.getMinimumSize(selectionPanel));

        // select all the times 
        selectionBox.doClick();
    
        return selectionPanel;
    }
    
    /**
     * Used to select/unselect all times in the timeline.
     */
    public void selectAllTimes(boolean select) {
        if (select != selectionBox.isSelected()) {
            selectionBox.doClick();
        }
    }
    
    /**
     * Creates a panel for selecting a military unit with respect to its force.
     */
    private JPanel createMilitaryUnitsForcePanel(TimelinePanel timelinePanel) {
        // create the panel
        JPanel forcePanel = new JPanel(new BorderLayout(5, 5));
        forcePanel.add(createMilitaryUnitsForceMenuBar(timelinePanel), BorderLayout.CENTER);
        forcePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Resource Size"),
                                                                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        return forcePanel;
    }
    
    /**
     * Creates a menu bar for selecting a military unit with respect to its force.
     */
    private JMenuBar createMilitaryUnitsForceMenuBar(TimelinePanel timelinePanel) {
        final TimelinePanel tlinePanel = timelinePanel;
        // create force menu bar
        JMenuBar menuBar = new JMenuBar();
        forceMenu.setFont(forceMenu.getFont().deriveFont(Font.PLAIN));
        JCheckBoxMenuItem[] forceItems = new JCheckBoxMenuItem[MapConstants.forceUnits.length];
        for (int i = 0; i < MapConstants.forceUnits.length; i++) {
            forceItems[i] = new JCheckBoxMenuItem(MapConstants.forceUnits[i]);
            forceItems[i].setFont(forceItems[i].getFont().deriveFont(Font.PLAIN));
            // create a filter for each force
            final MilitaryCodeFilter codeFilter = new MilitaryCodeFilter(MapConstants.forceSymbols[i], 11);
            final ActivityResourceFilter resourceFilter = new ActivityResourceFilter(codeFilter);
            forceItems[i].addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JCheckBoxMenuItem thisBox = (JCheckBoxMenuItem)e.getSource();
                        if (thisBox.isSelected()) {
                            tlinePanel.updateActivityTypeFilter(resourceFilter, TimelineConstants.ADD);
                        }
                        else {
                            tlinePanel.updateActivityTypeFilter(resourceFilter, TimelineConstants.REMOVE);
                        }
                    }
                });
            forceMenu.add(forceItems[i]);
        }
        menuBar.add(forceMenu);
        return menuBar;
    }
    
    /**
     * Creates a panel for selecting a military unit with respect to its mobility.
     */
    private JPanel createMilitaryUnitsMobilityPanel(TimelinePanel timelinePanel) {
        // create the panel
        JPanel mobilePanel = new JPanel(new BorderLayout(5, 5));        
        mobilePanel.add(createMilitaryUnitsMobilityMenuBar(timelinePanel), BorderLayout.CENTER);
        mobilePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Resource Mobility"),
                                                                 BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        return mobilePanel;
    }

    /**
     * Creates a menu bar for selecting a military unit with respect to its mobility.
     */
    private JMenuBar createMilitaryUnitsMobilityMenuBar(TimelinePanel timelinePanel) {
        final TimelinePanel tlinePanel = timelinePanel;
        // create force menu bar
        JMenuBar menuBar = new JMenuBar();
        mobileMenu.setFont(mobileMenu.getFont().deriveFont(Font.PLAIN));
        JCheckBoxMenuItem[] mobileItems = new JCheckBoxMenuItem[MapConstants.mobileUnits.length];
        for (int i = 0; i < MapConstants.mobileUnits.length; i++) {
            mobileItems[i] = new JCheckBoxMenuItem(MapConstants.mobileUnits[i]);
            mobileItems[i].setFont(mobileItems[i].getFont().deriveFont(Font.PLAIN));
            // create a filter for each mobility group
            final MilitaryCodeFilter codeFilter = new MilitaryCodeFilter(MapConstants.mobileSymbols[i], 10);
            final ActivityResourceFilter resourceFilter = new ActivityResourceFilter(codeFilter);
            mobileItems[i].addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JCheckBoxMenuItem thisBox = (JCheckBoxMenuItem)e.getSource();
                        if (thisBox.isSelected()) {
                            tlinePanel.updateActivityTypeFilter(resourceFilter, TimelineConstants.ADD);
                        }
                        else {
                            tlinePanel.updateActivityTypeFilter(resourceFilter, TimelineConstants.REMOVE);
                        }
                    }
                });
            mobileMenu.add(mobileItems[i]);
        }
        menuBar.add(mobileMenu);
        return menuBar;
    }
    
    /**
     * Creates the panel for selection of the activities with respect to the active time.
     */
    public JPanel createActivityTimeSelectionPanel(TimelinePanel timelinePanel) {
        final TimelinePanel tlinePanel = timelinePanel;
        final ActivityTimeFilter pastFilter = new ActivityTimeFilter(timeline, ActivityTimeFilter.PAST);
        final ActivityTimeFilter presentFilter = new ActivityTimeFilter(timeline, ActivityTimeFilter.PRESENT);
        final ActivityTimeFilter futureFilter = new ActivityTimeFilter(timeline, ActivityTimeFilter.FUTURE);
        JCheckBox pastCheckBox = new JCheckBox("Past Activities");
        pastCheckBox.setFont(pastCheckBox.getFont().deriveFont(Font.PLAIN));
        pastCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JCheckBox thisBox = (JCheckBox)e.getSource();
                    if (thisBox.isSelected()) {
                        tlinePanel.updateActivityTimeFilter(pastFilter, TimelineConstants.ADD);
                    }
                    else {
                        tlinePanel.updateActivityTimeFilter(pastFilter, TimelineConstants.REMOVE);
                    }
                }
            });
        pastCheckBox.doClick();
        JCheckBox presentCheckBox = new JCheckBox("Present Activities");
        presentCheckBox.setFont(presentCheckBox.getFont().deriveFont(Font.PLAIN));
        presentCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JCheckBox thisBox = (JCheckBox)e.getSource();
                    if (thisBox.isSelected()) {
                        tlinePanel.updateActivityTimeFilter(presentFilter, TimelineConstants.ADD);
                    }
                    else {
                        tlinePanel.updateActivityTimeFilter(presentFilter, TimelineConstants.REMOVE);
                    } 
                }
            });
        presentCheckBox.doClick();
        JCheckBox futureCheckBox = new JCheckBox("Future Activities");
        futureCheckBox.setFont(futureCheckBox.getFont().deriveFont(Font.PLAIN));
        futureCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JCheckBox thisBox = (JCheckBox)e.getSource();
                    if (thisBox.isSelected()) {
                        tlinePanel.updateActivityTimeFilter(futureFilter, TimelineConstants.ADD);
                    }
                    else {
                        tlinePanel.updateActivityTimeFilter(futureFilter, TimelineConstants.REMOVE);
                    }
                }
            });
        futureCheckBox.doClick();
        // set up the panel
        JPanel activityTimePanel = new JPanel(new GridLayout(1, 3, 2, 2));
        activityTimePanel.add(pastCheckBox);
        activityTimePanel.add(presentCheckBox);
        activityTimePanel.add(futureCheckBox);
        activityTimePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Active Time"),
                                                                       BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        return         activityTimePanel;
    }
    
    /**
     * Creates the panel for order selection. 
     */
    public JPanel createResourceActivityPanel(TimelinePanel timelinePanel) {
        final TimelinePanel tlinePanel = timelinePanel;
        // the activities owned by military units
        JCheckBox orderCheckBox = new JCheckBox("All Orders");
        orderCheckBox.setFont(orderCheckBox.getFont().deriveFont(Font.PLAIN));
        // the filter for the orders
        final TypeFilter typeFilter = new TypeFilter(TypeFactory.getType("Order"), true);
        orderCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JCheckBox thisBox = (JCheckBox)e.getSource();
                    if (thisBox.isSelected()) {
                        tlinePanel.updateActivityTypeFilter(typeFilter, TimelineConstants.ADD);        
                        forceMenu.setEnabled(false);
                        mobileMenu.setEnabled(false);
                    }
                    else {
                        tlinePanel.updateActivityTypeFilter(typeFilter, TimelineConstants.REMOVE);
                        forceMenu.setEnabled(true);
                        mobileMenu.setEnabled(true);
                    }
                }
            });
        orderCheckBox.doClick();
        // create check box panel
        JPanel cboxPanel = new JPanel();
        cboxPanel.setLayout(new BoxLayout(cboxPanel, BoxLayout.X_AXIS));
        cboxPanel.add(orderCheckBox);
        // the panel for the menus
        JPanel menuPanel = new JPanel(new GridLayout(1, 2, 2, 2));
        menuPanel.add(createMilitaryUnitsForcePanel(timelinePanel));
        menuPanel.add(createMilitaryUnitsMobilityPanel(timelinePanel));
        // the final panel 
        JPanel resourcePanel = new JPanel(new BorderLayout());
        resourcePanel.add(cboxPanel, BorderLayout.CENTER);
        resourcePanel.add(menuPanel, BorderLayout.SOUTH);
        resourcePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Orders"),
                                                                   BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        return resourcePanel;
    }
    
    /**
     * Creates the panel for event selection. 
     */
    public JPanel createNoResourceActivityPanel(TimelinePanel timelinePanel) {
        final TimelinePanel tlinePanel = timelinePanel;
        // the "hand of god" check box
        JCheckBox handOfGodBox = new JCheckBox("All Events");
        handOfGodBox.setFont(handOfGodBox.getFont().deriveFont(Font.PLAIN));
        // the filter for the "hand of god" activities
        final TypeFilter typeFilter = new TypeFilter(TypeFactory.getType("ActorLessActivity"), true);
        handOfGodBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JCheckBox thisBox = (JCheckBox)e.getSource();
                    if (thisBox.isSelected()) {
                        tlinePanel.updateActivityTypeFilter(typeFilter, TimelineConstants.ADD);
                    }
                    else {
                        tlinePanel.updateActivityTypeFilter(typeFilter, TimelineConstants.REMOVE);
                    }
                }
            });
        handOfGodBox.doClick();
        // create check box panel
        JPanel cboxPanel = new JPanel();
        cboxPanel.setLayout(new BoxLayout(cboxPanel, BoxLayout.X_AXIS));
        cboxPanel.add(handOfGodBox);
        cboxPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Events"),
                                                               BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        return         cboxPanel;
    }
    
    /**
     * Creates the panel for selection of the activities displayed in the timeline. 
     */
    private JPanel createActivitySelectionPanel(TimelinePanel timelinePanel) {
        JPanel activityTypePanel = new JPanel(new BorderLayout());
        activityTypePanel.add(createNoResourceActivityPanel(timelinePanel), BorderLayout.NORTH);
        activityTypePanel.add(createResourceActivityPanel(timelinePanel), BorderLayout.CENTER);
        JPanel activityPanel = new JPanel(new BorderLayout());
        activityPanel.add(activityTypePanel, BorderLayout.NORTH);
        activityPanel.add(createActivityTimeSelectionPanel(timelinePanel), BorderLayout.CENTER);
        activityPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Activity Selection"),
                                                                    BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        return activityPanel;
    }
    
    /**
     * Cretates the panel which contains previously defined subpanels.
     */
    public JPanel getOptionsPanel(TimelinePanel timelinePanel) {
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.X_AXIS));
        optionsPanel.add(createActivitySelectionPanel(timelinePanel));
        optionsPanel.add(createAllTimesSelectionPanel());
        return optionsPanel;
    }

    /**
     * Creates a panel which contains the slider used in the timeline.
     */
    public JPanel getTimelineSliderPanel(TimelinePanel timelinePanel) {
        final TimelinePanel tlinePanel    = timelinePanel;
        // the slider
        int halfInterval = (int) (tlinePanel.getDisplayedEndTime() - tlinePanel.getDisplayedStartTime()) / 2;
        timelineSlider.setMaximum((int) tlinePanel.getMaxBoundInCurrentTimeUnit() / halfInterval - 2);
        timelineSlider.setPaintTrack(false);
        timelineSlider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    long interval  = tlinePanel.getDisplayedEndTime() - tlinePanel.getDisplayedStartTime();
                    long startTime = timelineSlider.getValue() * (interval / 2);
                    long endTime   = startTime + interval;
                    tlinePanel.setDisplayedTimes(startTime, endTime);
                }
            });
        // the labels
        JLabel forwardLabel = new JLabel(new ImageIcon(Client.class.getResource("map/images/fast_forward16.gif")));
        forwardLabel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    int val = timelineSlider.getValue() + 1;
                    if (val <= timelineSlider.getMaximum()) {
                        timelineSlider.setValue(val);
                    }
                }
            });
        JLabel backwardLabel = new JLabel(new ImageIcon(Client.class.getResource("map/images/fast_back16.gif")));
        backwardLabel.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    int val = timelineSlider.getValue() - 1;
                    if (val >= timelineSlider.getMinimum()) {
                        timelineSlider.setValue(val);
                    }
                }
            });
        // add the slider to the panel
        JPanel sliderPanel = new JPanel(new BorderLayout(10, 10));
        sliderPanel.add(timelineSlider, BorderLayout.CENTER);
        sliderPanel.add(forwardLabel, BorderLayout.EAST);
        sliderPanel.add(backwardLabel, BorderLayout.WEST);

        return sliderPanel;
    }
    
    /**
     * Creates different tools for use in the timeline.
     */
    public JToolBar getTimelineTools(TimelinePanel timelinePanel) {
        final TimelineDisplayControl self = this;
        final TimelinePanel tlinePanel    = timelinePanel;
        final Timeline tline              = timeline;
        // add new "Hand of God" activity
        JButton addHandOfGod = new JButton(new ImageIcon(Client.class.getResource("map/images/addactivity16.png")));
        addHandOfGod.setMargin(new Insets(0, 10, 0, 10));
        addHandOfGod.setToolTipText("Add Event");
        addHandOfGod.addActionListener(new AbstractAction() {
                /**
			 * 
			 */
			private static final long serialVersionUID = -2250180086264966237L;

				public void actionPerformed(ActionEvent e) {
                    StratmasObject activity = StratmasObjectFactory.create(TypeFactory.getType("CustomPVModification"));
                    IdentifierFilter scFilter = new IdentifierFilter("scenario");
                    StratmasObject scenario = (StratmasObject)scFilter.filterTree(tline.getClient().getRootObject()).nextElement();
                    // add new activity
                    ((StratmasList)scenario.getChild("activities")).addWithUniqueIdentifier(activity);
                }
            });
        // add new military unit based activity
        JButton addMUActivity = new JButton(new ImageIcon(Client.class.getResource("map/images/addmuactivity16.png")));
        addMUActivity.setMargin(new Insets(0, 10, 0, 10));
        addMUActivity.setToolTipText("Add Order");
        addMUActivity.addActionListener(new AbstractAction() {
                /**
			 * 
			 */
			private static final long serialVersionUID = 3289433899719925644L;

				public void actionPerformed(ActionEvent e) {
                    final JDialog dialog = self.getSelectResourceDialog();
                    dialog.setSize(new Dimension(200, 200));
                    dialog.setLocationRelativeTo(tline.getTimelinePanel());
                    SwingUtilities.invokeLater (new Runnable() {
                            public void run() {
                                dialog.setVisible(true);
                            }
                        });
                }
            });
        // the button that opens the information dialog
        JButton info_button = new JButton(new ImageIcon(Client.class.getResource("map/images/Information16.gif")));
        info_button.setMargin(new Insets(0, 10, 0, 10));
        info_button.setToolTipText("Information");
        info_button.addActionListener(new AbstractAction() {
                /**
			 * 
			 */
			private static final long serialVersionUID = 7600508564077994238L;

				public void actionPerformed(ActionEvent e) {
                    final JDialog dialog = self.getInformationDialog();
                    dialog.setSize(new Dimension(300, 250));
                    dialog.setLocationRelativeTo(tlinePanel);
                    SwingUtilities.invokeLater (new Runnable() {
                            public void run() {
                                dialog.setVisible(true);
                            }
                        });
                }
            });
        // the button that zooms in the timeline
        JButton zoom_in = new JButton(new ImageIcon(Client.class.getResource("map/images/zoom_in16.gif")));
        zoom_in.setMargin(new Insets(0, 10, 0, 10));
        zoom_in.setToolTipText("Zoom In");
        zoom_in.addActionListener(new AbstractAction() {
                /**
			 * 
			 */
			private static final long serialVersionUID = -8856555516154869972L;

				public void actionPerformed(ActionEvent e) {
                    tlinePanel.zoomInScale();
                    // update the slider
                    int halfInterval = (int) (tlinePanel.getDisplayedEndTime() - tlinePanel.getDisplayedStartTime()) / 2;
                    int maxValue = (int) (tlinePanel.getMaxBoundInCurrentTimeUnit() / halfInterval - 2);
                    int val = (int) (tlinePanel.getDisplayedStartTime() / halfInterval);
                    timelineSlider.setMaximum(maxValue);
                    timelineSlider.setValue(val);
                }
            });
        // the button that zooms out the timeline
        JButton zoom_out = new JButton(new ImageIcon(Client.class.getResource("map/images/zoom_out16.gif")));
        zoom_out.setMargin(new Insets(0, 10, 0, 10));
        zoom_out.setToolTipText("Zoom Out");
        zoom_out.addActionListener(new AbstractAction() {
                /**
			 * 
			 */
			private static final long serialVersionUID = 1034407522137421951L;

				public void actionPerformed(ActionEvent e) {
                    tlinePanel.zoomOutScale();
                    // update the slider
                    int halfInterval = (int) (tlinePanel.getDisplayedEndTime() - tlinePanel.getDisplayedStartTime()) / 2;
                    int maxValue = (int) (tlinePanel.getMaxBoundInCurrentTimeUnit() / halfInterval - 2);
                    int val = (int) (tlinePanel.getDisplayedStartTime() / halfInterval);
                    timelineSlider.setMaximum(maxValue);
                    timelineSlider.setValue(val);
                }
            });
        // add all the buttons to the panel
        JToolBar buttonToolBar = new JToolBar();
        buttonToolBar.setFloatable(false);
        buttonToolBar.setLayout(new BoxLayout(buttonToolBar, BoxLayout.X_AXIS));
        buttonToolBar.add(addHandOfGod);
        buttonToolBar.add(Box.createRigidArea(new Dimension(5, 0)));
        buttonToolBar.add(addMUActivity);
        buttonToolBar.add(Box.createRigidArea(new Dimension(5, 0)));
        buttonToolBar.add(info_button);
        buttonToolBar.add(Box.createHorizontalGlue());
         buttonToolBar.add(zoom_in);
        buttonToolBar.add(Box.createRigidArea(new Dimension(5, 0)));
         buttonToolBar.add(zoom_out);
        buttonToolBar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        
        return buttonToolBar;
    }
    
    /**
     * Set the time pointed by the mouse cursor.
     */
    public void setPointedTimeLabel(String text) {
        pointedTimeLabel.setText(text);
    }

    /**
     * Returns the dialog for selection of the activity resource.
     */
    public JDialog getSelectResourceDialog() {
        // create the dialog
        final JDialog dialog = new JDialog(new JFrame(), "Order selection");
        
        // add the combo box which contains the orders
        final JComboBox orderBox = new JComboBox();
        orderBox.setFont(orderBox.getFont().deriveFont(Font.PLAIN));
        // add all the orders to the combo box 
        Vector candidates = new Vector();
        candidates.addAll(TypeFactory.getType("Order").getExpandedDerived());
        for (Enumeration e = candidates.elements(); e.hasMoreElements(); ) {
            Type type = (Type)e.nextElement();
            if (!type.isAbstract()) {
                orderBox.addItem(type.getName());
            }
        }
        JPanel orderBoxPanel = new JPanel(new BorderLayout());
        orderBoxPanel.add(orderBox, BorderLayout.CENTER);
        orderBoxPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Select the order"),
                                                                   BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        
        // add the combo box which contains the military units
        final JComboBox muBox = new JComboBox();
        muBox.setFont(muBox.getFont().deriveFont(Font.PLAIN));
        // add all military units to the combo box 
        TypeFilter filter = new TypeFilter(TypeFactory.getType("MilitaryUnit"), true);
        Enumeration mUnits = filter.filterTree(timeline.getClient().getRootObject());
        for (; mUnits.hasMoreElements(); ) {
            StratmasObject scom = (StratmasObject)mUnits.nextElement();
            if (scom.getType().canSubstitute("MilitaryUnit") && !(scom instanceof StratmasList)) {
                muBox.addItem(scom);
            }
        }
        JPanel muBoxPanel = new JPanel(new BorderLayout());
        muBoxPanel.add(muBox, BorderLayout.CENTER);
        muBoxPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Select the resource"),
                                                              BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        // add the OK button
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String orderName = (String)orderBox.getSelectedItem();
                    StratmasObject activity = StratmasObjectFactory.create(TypeFactory.getType(orderName));
                    ((StratmasList)((StratmasObject)muBox.getSelectedItem()).getChild("activities")).
                        addWithUniqueIdentifier(activity);
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            });
        JPanel okPanel = new JPanel();
        okPanel.setLayout(new BoxLayout(okPanel, BoxLayout.LINE_AXIS));
        okPanel.add(Box.createHorizontalGlue());
        okPanel.add(okButton);
        okPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // compose the dialog            
        JPanel content_pane = new JPanel();
        content_pane.setLayout(new BoxLayout(content_pane, BoxLayout.PAGE_AXIS));
        content_pane.add(orderBoxPanel);
        content_pane.add(Box.createRigidArea(new Dimension(0, 5)));
        content_pane.add(muBoxPanel);
        content_pane.add(Box.createRigidArea(new Dimension(0, 5)));
        content_pane.add(okPanel);
        content_pane.setOpaque(true);

        dialog.setContentPane(content_pane);
        return dialog;
    }
    
    /**
     * Returns the information dialog.
     */
    public JDialog getInformationDialog() {
        // create the dialog
        final JDialog dialog = new JDialog(new JFrame(), "Timeline information");
        // set the current time step
        JPanel step_size_panel = new JPanel();
        step_size_panel.setLayout(new BoxLayout(step_size_panel, BoxLayout.LINE_AXIS));
        deltatLabel.setFont(deltatLabel.getFont().deriveFont(Font.PLAIN));
        step_size_panel.add(deltatLabel);
        step_size_panel.add(Box.createHorizontalGlue());
        step_size_panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Time Step Size"),
                                                                     BorderFactory.createEmptyBorder(2, 5, 2, 5)));
        // set the simulation start time
        JPanel start_time_panel = new JPanel();
        start_time_panel.setLayout(new BoxLayout(start_time_panel, BoxLayout.LINE_AXIS));
        startTimeLabel.setFont(startTimeLabel.getFont().deriveFont(Font.PLAIN));
        start_time_panel.add(startTimeLabel);
        start_time_panel.add(Box.createHorizontalGlue());
        start_time_panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Simulation Start Time"),
                                                                      BorderFactory.createEmptyBorder(2, 5, 2, 5)));
        // set the simulation current time
        JPanel current_time_panel = new JPanel();
        current_time_panel.setLayout(new BoxLayout(current_time_panel, BoxLayout.LINE_AXIS));
        currentTimeLabel.setFont(currentTimeLabel.getFont().deriveFont(Font.PLAIN));
        current_time_panel.add(currentTimeLabel);
        current_time_panel.add(Box.createHorizontalGlue());
        current_time_panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Simulation Current Time"),
                                                                        BorderFactory.createEmptyBorder(2, 5, 2, 5)));
        // arrange the panels
        JPanel info_panel = new JPanel();
        info_panel.setLayout(new GridLayout(3,1,2,2));
        info_panel.add(step_size_panel);
        info_panel.add(start_time_panel);
        info_panel.add(current_time_panel);
        
        // add the close button
        JButton close_button = new JButton("Close");
        close_button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            });
        JPanel close_panel = new JPanel();
        close_panel.setLayout(new BoxLayout(close_panel, BoxLayout.LINE_AXIS));
        close_panel.add(Box.createHorizontalGlue());
        close_panel.add(close_button);
        close_panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 5));
        
        // compose the dialog            
        JPanel content_pane = new JPanel();
        content_pane.setLayout(new BoxLayout(content_pane, BoxLayout.PAGE_AXIS));
        content_pane.add(info_panel);
        content_pane.add(close_panel);
        content_pane.setOpaque(true);
        content_pane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Information"),
                                                                  BorderFactory.createEmptyBorder(2, 5, 2, 5)));
        dialog.setContentPane(content_pane);
        return dialog;
    }
    
    /**
     * Updates the information dialog.
     */
    public void updateDialogInfo() {
        deltatLabel.setText(TimelinePanel.millisecondsToString(timeline.getDT(), TimelineConstants.LONG));
        startTimeLabel.setText(TimelinePanel.dateFormat.format(new Date(timeline.getSimStartTime())));
        currentTimeLabel.setText(TimelinePanel.dateFormat.format(new Date(timeline.getCurrentTime())));
    }
    
    /**
     * Returns the timeline slider.
     */
    public JSlider getTimelineSlider() {
        return timelineSlider;
    }
    
    /**
     * Removes the display control.
     */
    public void remove() {
        timeline = null;
    }
    
}

