package ApproxsimClient.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.ActionEvent;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

import ApproxsimClient.Client;
import ApproxsimClient.Debug;
import ApproxsimClient.object.type.TypeFactory;
import ApproxsimClient.filter.PopulationFilter;
import ApproxsimClient.filter.TypeFilter;
import ApproxsimClient.filter.ActivityFilter;
import ApproxsimClient.filter.CombinedORFilter;
import ApproxsimClient.filter.MilitaryCodeFilter;
import ApproxsimClient.filter.ApproxsimObjectFilter;

/**
 * This class implements variety of panels used to control display of approxsim elements and other objects on the map.
 * 
 * @version 1.0
 * @author Amir Filipovic
 */
public class DisplayControl {
    /**
     * Reference to the client.
     */
    private Client client;
    /**
     * Reference to the map.
     */
    private MapDrawer drawer;
    /**
     * The filter for the elements of type MilitaryUnit.
     */
    private ApproxsimObjectFilter military_filter;
    /**
     * The filter for the elements of type Agency.
     */
    private ApproxsimObjectFilter agency_filter;
    /**
     * The filter for the elements of type Population.
     */
    private ApproxsimObjectFilter population_filter;
    /**
     * The filter for the elements of type Activity.
     */
    private ApproxsimObjectFilter activity_filter;

    /**
     * Creates the DisplayControl.
     */
    public DisplayControl(Client client, MapDrawer drawer) {
        this.client = client;
        this.drawer = drawer;
    }

    /**
     * Tools reference for disabeling
     */
    private List<JButton> toolsButtons = new ArrayList<JButton>();
    private ApproxsimObjectFilter road_filter;
    private ApproxsimObjectFilter other_filter;

    /**
     * Creates the panel for controling size of the symbols displayed in the map.
     */
    private JPanel createSymbolScalePanel() {
        final MapDrawer mapDrawer = drawer;
        final JLabel symbolScaleLabel = new JLabel(Double.toString(mapDrawer
                .getSymbolScale()));
        // slider for controling size of the symbols
        JSlider symbolScaleSlider = new JSlider(0, 100,
                (int) (100.0 / 5.0 * mapDrawer.getSymbolScale()));
        symbolScaleLabel.setFont(symbolScaleLabel.getFont()
                .deriveFont(Font.PLAIN));
        symbolScaleSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                if (!source.getValueIsAdjusting()) {
                    int scale = source.getValue();
                    mapDrawer
                            .setSymbolScale(0.0 + 5.0 * (((double) scale) / (source
                                    .getMaximum() - source.getMinimum())));
                    symbolScaleLabel.setText(Double
                            .toString(mapDrawer.symbolScale));
                }
            }
        });
        JPanel symbolScalePanel = new JPanel();
        symbolScalePanel.setLayout(new BoxLayout(symbolScalePanel,
                BoxLayout.Y_AXIS));
        JLabel symbolScaleSliderLabel = new JLabel(
                "Magnify symbols (0.0 - 5.0)");
        symbolScaleSliderLabel.setFont(symbolScaleSliderLabel.getFont()
                .deriveFont(Font.PLAIN));
        symbolScalePanel.add(symbolScaleSliderLabel);
        symbolScalePanel.add(symbolScaleSlider);
        symbolScalePanel.add(symbolScaleLabel);

        return symbolScalePanel;
    }

    /**
     * Creates the panel for controling opacity of the symbols displayed in the map.
     */
    private JPanel createOpacityPanel() {
        final MapDrawer mapDrawer = drawer;
        final JLabel opacityLabel = new JLabel(Double.toString(mapDrawer
                .getSymbolOpacity()));
        // slider for controling opacity of the symbols
        JSlider opacitySlider = new JSlider(0, 100, 100);
        opacityLabel.setFont(opacityLabel.getFont().deriveFont(Font.PLAIN));
        opacitySlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                if (!source.getValueIsAdjusting()) {
                    int scale = source.getValue();
                    mapDrawer.setSymbolOpacity(((double) scale)
                            / (source.getMaximum() - source.getMinimum()));
                    opacityLabel.setText(Double
                            .toString(mapDrawer.symbolOpacity));
                }
            }
        });
        JPanel opacityPanel = new JPanel();
        opacityPanel.setLayout(new BoxLayout(opacityPanel, BoxLayout.Y_AXIS));
        JLabel opacitySliderLabel = new JLabel("Symbol Opacity (0.0 - 1.0)");
        opacitySliderLabel.setFont(opacitySliderLabel.getFont()
                .deriveFont(Font.PLAIN));
        opacityPanel.add(opacitySliderLabel);
        opacityPanel.add(opacitySlider);
        opacityPanel.add(opacityLabel);

        return opacityPanel;
    }

    /**
     * Creates the panel for selection of : 1. Wheather the symbols displayed on the map will be magninfied under the mouse pointer. 2.
     * Wheather the size of the symbols will be constant (indipendent of the map scale) or not.
     */
    private JPanel createSymbolPreferenceCheckBoxPanel() {
        final MapDrawer mapDrawer = drawer;
        // symbol magnifier
        JCheckBox symbolMagnifierCheckBox = new JCheckBox(new AbstractAction(
                "Magnify symbols under mouse pointer") {
            /**
				 * 
				 */
            private static final long serialVersionUID = -481572609108159156L;

            public void actionPerformed(ActionEvent e) {
                JCheckBox box = (JCheckBox) e.getSource();
                mapDrawer.setIsEnabledSymbolMagnifier(box.isSelected());
            }
        });
        symbolMagnifierCheckBox.setSelected(mapDrawer
                .isEnabledSymbolMagnifier());
        symbolMagnifierCheckBox.setFont(symbolMagnifierCheckBox.getFont()
                .deriveFont(Font.PLAIN));
        symbolMagnifierCheckBox.doClick();
        // invariant symbol sizes switch
        JCheckBox invariantSymbolSizeCheckBox = new JCheckBox(
                new AbstractAction("Constant symbol size") {
                    /**
				 * 
				 */
                    private static final long serialVersionUID = -977190264641104247L;

                    public void actionPerformed(ActionEvent e) {
                        JCheckBox box = (JCheckBox) e.getSource();
                        mapDrawer.setInvariantSymbolSize(box.isSelected());
                    }
                });
        invariantSymbolSizeCheckBox.setSelected(mapDrawer
                .getInvariantSymbolSize());
        invariantSymbolSizeCheckBox.setFont(invariantSymbolSizeCheckBox
                .getFont().deriveFont(Font.PLAIN));
        JPanel symbolPrefPanel = new JPanel();
        symbolPrefPanel.setLayout(new GridLayout(2, 1));
        symbolPrefPanel.add(symbolMagnifierCheckBox);
        symbolPrefPanel.add(invariantSymbolSizeCheckBox);
        invariantSymbolSizeCheckBox.doClick();

        return symbolPrefPanel;
    }

    /**
     * Creates the panel for selection of : 1. Wheather only the present elements or all the elements will be displayed on the map.
     */
    private JPanel createElementPresencePanel() {
        final MapDrawer mapDrawer = drawer;
        JPanel presencePanel = new JPanel();
        presencePanel.setLayout(new GridLayout(1, 2, 2, 2));
        // button for selection of all the elements
        JRadioButton all_elements = new JRadioButton(new AbstractAction(
                "All Units & Teams") {
            /**
			 * 
			 */
            private static final long serialVersionUID = 903883054654956809L;

            public void actionPerformed(ActionEvent e) {
                mapDrawer.setIgnorePresent(true);
            }
        });
        all_elements.setSelected(true);
        all_elements.setFont(all_elements.getFont().deriveFont(Font.PLAIN));
        // button for selection of the present elements only
        JRadioButton present_elements = new JRadioButton(new AbstractAction(
                "Present Units & Teams") {
            /**
			 * 
			 */
            private static final long serialVersionUID = 2066617015277340404L;

            public void actionPerformed(ActionEvent e) {
                mapDrawer.setIgnorePresent(false);
            }
        });
        present_elements.setFont(present_elements.getFont()
                .deriveFont(Font.PLAIN));
        presencePanel.add(all_elements);
        presencePanel.add(present_elements);
        Border loweredetched = BorderFactory
                .createEtchedBorder(EtchedBorder.LOWERED);
        presencePanel
                .setBorder(BorderFactory.createCompoundBorder(BorderFactory
                                                                      .createTitledBorder(loweredetched,
                                                                                          "Presence"),
                                                              BorderFactory
                                                                      .createEmptyBorder(2,
                                                                                         2,
                                                                                         2,
                                                                                         2)));

        ButtonGroup element_buttons = new ButtonGroup();
        element_buttons.add(all_elements);
        element_buttons.add(present_elements);

        return presencePanel;
    }

    /**
     * Creates the panel for selection of the graticule density on the map.
     */
    private JPanel createGraticuleSelectionPanel() {
        final MapDrawer mapDrawer = drawer;
        JPanel graticules = new JPanel();
        graticules.setLayout(new GridLayout(2, 2, 2, 2));
        // button for selection of no graticules
        JRadioButton graticules_off = new JRadioButton(new AbstractAction(
                "None") {
            /**
			 * 
			 */
            private static final long serialVersionUID = -5272754506300499338L;

            public void actionPerformed(ActionEvent e) {
                mapDrawer.setGraticulesVisible(false);
            }
        });
        graticules_off.setFont(graticules_off.getFont().deriveFont(Font.PLAIN));
        graticules.add(graticules_off);
        // button for selection of graticules with one degree density
        JRadioButton graticule_01 = new JRadioButton(new AbstractAction(
                "1 degree") {
            /**
			 * 
			 */
            private static final long serialVersionUID = 5891387780840774911L;

            public void actionPerformed(ActionEvent e) {
                mapDrawer.setGraticuleSpacing(GraticuleLayer.ONE_DEGREE);
                mapDrawer.setGraticulesVisible(true);
            }
        });
        graticule_01.setFont(graticule_01.getFont().deriveFont(Font.PLAIN));
        graticules.add(graticule_01);
        // button for selection of graticules with five degrees density
        JRadioButton graticule_05 = new JRadioButton(new AbstractAction(
                "5 degrees") {
            /**
			 * 
			 */
            private static final long serialVersionUID = -5518823757796781171L;

            public void actionPerformed(ActionEvent e) {
                mapDrawer.setGraticuleSpacing(GraticuleLayer.FIVE_DEGREES);
                mapDrawer.setGraticulesVisible(true);
            }
        });
        graticule_05.setFont(graticule_05.getFont().deriveFont(Font.PLAIN));
        graticules.add(graticule_05);
        // button for selection of graticules with ten degrees density
        JRadioButton graticule_10 = new JRadioButton(new AbstractAction(
                "10 degrees") {
            /**
			 * 
			 */
            private static final long serialVersionUID = 6590218413603073521L;

            public void actionPerformed(ActionEvent e) {
                mapDrawer.setGraticuleSpacing(GraticuleLayer.TEN_DEGREES);
                mapDrawer.setGraticulesVisible(true);
            }
        });
        graticule_10.setFont(graticule_10.getFont().deriveFont(Font.PLAIN));
        graticule_10.doClick();
        graticules.add(graticule_10);
        graticules.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createTitledBorder("Graticule spacing"), BorderFactory
                .createEmptyBorder(2, 2, 2, 2)));
        ButtonGroup graticule_buttons = new ButtonGroup();
        graticule_buttons.add(graticules_off);
        graticule_buttons.add(graticule_01);
        graticule_buttons.add(graticule_05);
        graticule_buttons.add(graticule_10);

        return graticules;
    }

    /**
     * Creates the panel for selection of displaying names of the cities on the map or not.
     */
    private JPanel createPopulationNameSelectionPanel() {
        final MapDrawer mapDrawer = drawer;
        JPanel pos_panel = new JPanel();
        pos_panel.setLayout(new GridLayout(1, 2, 2, 2));
        // button for selection of names of the cities
        JRadioButton pos_show = new JRadioButton(new AbstractAction("Show") {
            /**
			 * 
			 */
            private static final long serialVersionUID = -2744191142537284881L;

            public void actionPerformed(ActionEvent e) {
                mapDrawer.setShowPopulationNames(true);
            }
        });
        pos_show.setFont(pos_show.getFont().deriveFont(Font.PLAIN));
        pos_panel.add(pos_show);
        // button for deselection of names of the cities
        JRadioButton pos_hide = new JRadioButton(new AbstractAction("Hide") {
            /**
			 * 
			 */
            private static final long serialVersionUID = 8593298642944973785L;

            public void actionPerformed(ActionEvent e) {
                mapDrawer.setShowPopulationNames(false);
            }
        });
        pos_hide.setFont(pos_hide.getFont().deriveFont(Font.PLAIN));
        pos_panel.add(pos_hide);
        pos_panel
                .setBorder(BorderFactory.createCompoundBorder(BorderFactory
                        .createTitledBorder("Name"), BorderFactory
                        .createEmptyBorder(2, 2, 2, 2)));
        ButtonGroup pos_buttons = new ButtonGroup();
        pos_buttons.add(pos_show);
        pos_buttons.add(pos_hide);
        pos_hide.doClick();

        return pos_panel;
    }

    /**
     * Creates the panel for selection of displaying cities depending of the population size.
     */
    private JPanel createPopulationLocationSelectionPanel() {
        final DisplayControl self = this;
        JPanel pop_panel = new JPanel();
        pop_panel.setLayout(new GridLayout(2, 3, 2, 2));
        // button for deselection of locations of the cities
        JRadioButton p_none = new JRadioButton(new AbstractAction("None") {
            /**
			 * 
			 */
            private static final long serialVersionUID = 2174062811143043544L;

            public void actionPerformed(ActionEvent e) {
                self.setPopulationFilter(null);
            }
        });
        p_none.setFont(p_none.getFont().deriveFont(Font.PLAIN));
        pop_panel.add(p_none);
        // button for selection of locations of all the cities
        JRadioButton p_all = new JRadioButton(new AbstractAction("All") {
            /**
			 * 
			 */
            private static final long serialVersionUID = -5210902590689479872L;

            public void actionPerformed(ActionEvent e) {
                self.setPopulationFilter(new TypeFilter(TypeFactory
                        .getType("Population")));
            }
        });
        p_all.setSelected(true);
        p_all.setFont(p_all.getFont().deriveFont(Font.PLAIN));
        pop_panel.add(p_all);
        // button for selection of locations of the cities with more then 100000 inhabitants
        JRadioButton p_100000 = new JRadioButton(
                new AbstractAction("> 100 000") {
                    /**
			 * 
			 */
                    private static final long serialVersionUID = -3454182932747058616L;

                    public void actionPerformed(ActionEvent e) {
                        self.setPopulationFilter(new PopulationFilter(100000));
                    }
                });
        p_100000.setFont(p_100000.getFont().deriveFont(Font.PLAIN));
        pop_panel.add(p_100000);
        // button for selection of locations of the cities with more then 500000 inhabitants
        JRadioButton p_500000 = new JRadioButton(
                new AbstractAction("> 500 000") {
                    /**
			 * 
			 */
                    private static final long serialVersionUID = 132237975167177916L;

                    public void actionPerformed(ActionEvent e) {
                        self.setPopulationFilter(new PopulationFilter(500000));
                    }
                });
        p_500000.setFont(p_500000.getFont().deriveFont(Font.PLAIN));
        pop_panel.add(p_500000);
        // button for selection of locations of the cities with more then 1000000 inhabitants
        JRadioButton p_1000000 = new JRadioButton(new AbstractAction(
                "> 1 000 000") {
            /**
			 * 
			 */
            private static final long serialVersionUID = -364185548771163402L;

            public void actionPerformed(ActionEvent e) {
                self.setPopulationFilter(new PopulationFilter(1000000));
            }
        });
        p_1000000.setFont(p_1000000.getFont().deriveFont(Font.PLAIN));
        pop_panel.add(p_1000000);
        pop_panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createTitledBorder("Location"), BorderFactory
                .createEmptyBorder(2, 2, 2, 2)));
        ButtonGroup pop_buttons = new ButtonGroup();
        pop_buttons.add(p_none);
        pop_buttons.add(p_all);
        pop_buttons.add(p_100000);
        pop_buttons.add(p_500000);
        pop_buttons.add(p_1000000);

        p_all.doClick();

        return pop_panel;
    }

    /**
     * Creates the panel for selection of displaying activities depending of their occurance in time.
     */
    private JPanel createActivityTimeDependencePanel() {
        JPanel activity_panel = new JPanel();
        activity_panel.setLayout(new GridLayout(1, 3, 2, 2));
        // button for selection of the past activities
        final JCheckBox pastActivities = new JCheckBox("Past");
        pastActivities.setFont(pastActivities.getFont().deriveFont(Font.PLAIN));
        pastActivities.setSelected(true);
        // button for selection of the present activities
        final JCheckBox presentActivities = new JCheckBox("Present");
        presentActivities.setFont(presentActivities.getFont()
                .deriveFont(Font.PLAIN));
        presentActivities.setSelected(true);
        // button for selection of the future activities
        final JCheckBox futureActivities = new JCheckBox("Future");
        futureActivities.setFont(futureActivities.getFont()
                .deriveFont(Font.PLAIN));
        futureActivities.setSelected(true);
        final DisplayControl self = this;
        final Client fclient = client;
        pastActivities.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                Debug.err.println("past selected");
                self.updateActivityFilter(fclient,
                                          e.getStateChange() == ItemEvent.SELECTED,
                                          presentActivities.isSelected(),
                                          futureActivities.isSelected());
            }
        });
        presentActivities.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                self.updateActivityFilter(fclient,
                                          pastActivities.isSelected(),
                                          e.getStateChange() == ItemEvent.SELECTED,
                                          futureActivities.isSelected());
            }
        });
        futureActivities.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                self.updateActivityFilter(fclient,
                                          pastActivities.isSelected(),
                                          presentActivities.isSelected(),
                                          e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        activity_panel.add(pastActivities);
        activity_panel.add(presentActivities);
        activity_panel.add(futureActivities);
        activity_panel.setBorder(BorderFactory
                .createCompoundBorder(BorderFactory
                        .createTitledBorder("Activities"), BorderFactory
                        .createEmptyBorder(2, 2, 2, 2)));

        // initialize the filter for activities
        activity_filter = new ActivityFilter(client, ActivityFilter.ALL);
        updateFilter();

        return activity_panel;
    }

    /**
     * Creates the panel for selection of military units depending of the force size and the mobility possibilities.
     */
    private JPanel createMilitaryUnitSelectionPanel() {
        final DisplayControl self = this;
        final JCheckBoxMenuItem[] force_items = new JCheckBoxMenuItem[MapConstants.forceUnits.length];
        final JCheckBoxMenuItem[] mobile_items = new JCheckBoxMenuItem[MapConstants.mobileUnits.length];
        final JCheckBox highestRankUnitsCheckBox = new JCheckBox("Top Units");
        // panel for selection of the military units listed by it's force
        JPanel forces_panel = new JPanel(new BorderLayout());
        JMenuBar menu_bar = new JMenuBar();
        final JMenu menu = new JMenu(" Select");
        menu.setFont(menu.getFont().deriveFont(Font.PLAIN));
        for (int i = 0; i < MapConstants.forceUnits.length; i++) {
            final char rankSymbol = MapConstants.forceSymbols[i];
            force_items[i] = new JCheckBoxMenuItem(new AbstractAction(
                    MapConstants.forceUnits[i]) {
                /**
				 * 
				 */
                private static final long serialVersionUID = 5545955183982926793L;

                public void actionPerformed(ActionEvent e) {
                    self.updateMilitaryUnitFilter(force_items, mobile_items);
                    // uncheck the item for top rank military units
                    JCheckBoxMenuItem thisItem = (JCheckBoxMenuItem) e
                            .getSource();
                    if (rankSymbol == self.getHighestRankUnit()
                            && !thisItem.isSelected()) {
                        highestRankUnitsCheckBox.setSelected(false);
                    }

                }
            });
            force_items[i].setFont(force_items[i].getFont()
                    .deriveFont(Font.PLAIN));
            menu.add(force_items[i]);
        }
        menu_bar.add(menu);
        forces_panel.add(menu_bar, BorderLayout.SOUTH);
        forces_panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createTitledBorder("Show Forces"), BorderFactory
                .createEmptyBorder(2, 2, 2, 2)));
        // panel for selection of the military units listed by it's mobility
        JPanel mobile_panel = new JPanel(new BorderLayout());
        JMenuBar menu_bar2 = new JMenuBar();
        final JMenu menu2 = new JMenu(" Select");
        menu2.setFont(menu2.getFont().deriveFont(Font.PLAIN));
        for (int i = 0; i < MapConstants.mobileUnits.length; i++) {
            mobile_items[i] = new JCheckBoxMenuItem(new AbstractAction(
                    MapConstants.mobileUnits[i]) {
                /**
				 * 
				 */
                private static final long serialVersionUID = 2195017416207928926L;

                public void actionPerformed(ActionEvent e) {
                    self.updateMilitaryUnitFilter(force_items, mobile_items);
                }
            });

            mobile_items[i].setFont(mobile_items[i].getFont()
                    .deriveFont(Font.PLAIN));
            menu2.add(mobile_items[i]);
        }
        menu_bar2.add(menu2);
        mobile_panel.add(menu_bar2, BorderLayout.SOUTH);
        mobile_panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createTitledBorder("Show Mobile"), BorderFactory
                .createEmptyBorder(2, 2, 2, 2)));
        // check box for selection of the military units with higest rank
        highestRankUnitsCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                // display the military units with highest rang
                char rankSymbol = self.getHighestRankUnit();
                // highest rank unit found
                if (rankSymbol != '0') {
                    JCheckBox thisBox = (JCheckBox) e.getSource();
                    int index = 0;
                    while (rankSymbol != MapConstants.forceSymbols[index]) {
                        index++;
                    }
                    if ((thisBox.isSelected() && !force_items[index]
                            .isSelected())
                            || (!thisBox.isSelected() && force_items[index]
                                    .isSelected())) {
                        force_items[index].doClick();
                    }
                }
            }
        });
        highestRankUnitsCheckBox.setFont(highestRankUnitsCheckBox.getFont()
                .deriveFont(Font.PLAIN));
        highestRankUnitsCheckBox.setEnabled(false);
        // check box for selection of all military units
        JCheckBox allUnitsCheckBox = new JCheckBox("All Units");
        allUnitsCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    for (int i = 0; i < force_items.length; i++) {
                        force_items[i].setSelected(false);
                    }
                    for (int i = 0; i < mobile_items.length; i++) {
                        mobile_items[i].setSelected(false);
                    }
                    menu.setEnabled(false);
                    menu2.setEnabled(false);
                    highestRankUnitsCheckBox.setSelected(false);
                    highestRankUnitsCheckBox.setEnabled(false);
                    // show all military units
                    self.setMilitaryUnitFilter(new TypeFilter(TypeFactory
                            .getType("MilitaryUnit")));
                } else {
                    menu.setEnabled(true);
                    menu2.setEnabled(true);
                    highestRankUnitsCheckBox.setEnabled(true);
                    // show only selected military units
                    self.setMilitaryUnitFilter(null);
                }
            }
        });
        allUnitsCheckBox.setFont(allUnitsCheckBox.getFont()
                .deriveFont(Font.PLAIN));
        allUnitsCheckBox.doClick();

        // panel consisting of the previously defined panels
        JPanel muLeftPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        muLeftPanel.add(allUnitsCheckBox);
        muLeftPanel.add(highestRankUnitsCheckBox);
        JPanel muPanel = new JPanel(new GridLayout(1, 3, 2, 2));
        muPanel.add(muLeftPanel);
        muPanel.add(forces_panel);
        muPanel.add(mobile_panel);
        muPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createTitledBorder("Military Units"), BorderFactory
                .createEmptyBorder(2, 2, 2, 2)));
        return muPanel;
    }

    /**
     * Creates the panel for selection of teams and agencies .
     */
    private JPanel createTeamsAndAgenciesSelectionPanel() {
        final DisplayControl self = this;
        JPanel agency_panel = new JPanel();
        agency_panel.setLayout(new GridLayout(2, 3, 2, 2));
        // check box array for selection of the different agencies
        final JCheckBox[] agency_boxes = new JCheckBox[MapConstants.agencies.length];
        for (int i = 0; i < MapConstants.agencies.length; i++) {
            agency_boxes[i] = new JCheckBox(MapConstants.agencies[i]);
            agency_boxes[i].addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    // create agency filter
                    CombinedORFilter agency_filter = new CombinedORFilter();
                    for (int i = 0; i < MapConstants.agencyTypes.length; i++) {
                        // show agencies
                        if (agency_boxes[i] != null
                                && agency_boxes[i].isSelected()) {
                            TypeFilter type_filter = new TypeFilter(TypeFactory
                                    .getType(MapConstants.agencyTypes[i]));
                            ((CombinedORFilter) agency_filter).add(type_filter);
                        }
                    }
                    self.setAgencyFilter(agency_filter);
                }
            });
            agency_boxes[i].setFont(agency_boxes[i].getFont()
                    .deriveFont(Font.PLAIN));
            agency_boxes[i].doClick();
            agency_panel.add(agency_boxes[i]);
        }
        agency_panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createTitledBorder("Agency Teams"), BorderFactory
                .createEmptyBorder(2, 2, 2, 2)));

        return agency_panel;
    }

    private JPanel createGraphSelectionPanel() {
        final DisplayControl self = this;
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 3, 2, 2));

        final JCheckBox roads = new JCheckBox("Roads");
        roads.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                CombinedORFilter filter = new CombinedORFilter();
                if (roads.isSelected()) {
                    TypeFilter type_filter = new TypeFilter(TypeFactory
                            .getType("PathEdge"));
                    ((CombinedORFilter) filter).add(type_filter);
                    TypeFilter type_filter2 = new TypeFilter(TypeFactory
                            .getType("PathNode"));
                    ((CombinedORFilter) filter).add(type_filter2);
                }
                self.setRoadsFilter(filter);
            }
        });
        panel.add(roads);
        roads.doClick();

        final JCheckBox other = new JCheckBox("Other");
        other.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                CombinedORFilter filter = new CombinedORFilter();
                if (roads.isSelected()) {
                    TypeFilter type_filter = new TypeFilter(TypeFactory
                            .getType("EffectEdge"));
                    ((CombinedORFilter) filter).add(type_filter);
                    TypeFilter type_filter2 = new TypeFilter(TypeFactory
                            .getType("EffectNode"));
                    ((CombinedORFilter) filter).add(type_filter2);
                }
                self.setOtherFilter(filter);
            }
        });
        panel.add(other);
        other.doClick();

        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createTitledBorder("Infrastructure"), BorderFactory
                .createEmptyBorder(2, 2, 2, 2)));
        return panel;
    }

    /**
     * Creates the panel for selection of displaying borders of the actual region or not.
     */
    private JPanel createRegionBordersSelectionPanel() {
        final MapDrawer mapDrawer = drawer;
        JPanel region = new JPanel();
        region.setLayout(new GridLayout(1, 1, 2, 2));
        // check box for selection of the borders of the region
        JCheckBox region_box = new JCheckBox("Show region borders");
        region_box.setFont(region_box.getFont().deriveFont(Font.PLAIN));
        region_box.setSelected(true);
        region_box.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                mapDrawer.setDrawRegionShapes(e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        region.add(region_box);
        region.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createTitledBorder("Region"), BorderFactory
                .createEmptyBorder(2, 2, 2, 2)));

        return region;
    }

    /**
     * Creates the panel for selection of representation for the process variables and factions.
     */
    private JPanel createPVRepresentationPanel() {
        final MapDrawer mapDrawer = drawer;
        JPanel button_panel = new JPanel();
        button_panel.setLayout(new GridLayout(2, 1, 2, 2));
        // button for selection of grid representation for the process variables & factions
        JRadioButton grid_button = new JRadioButton(new AbstractAction(
                "Grid Based Representation") {
            /**
			 * 
			 */
            private static final long serialVersionUID = -3351700659400941864L;

            public void actionPerformed(ActionEvent e) {
                mapDrawer.setPVGrid(true);
            }
        });
        grid_button.setSelected(true);
        grid_button.setFont(grid_button.getFont().deriveFont(Font.PLAIN));
        button_panel.add(grid_button);
        // button for selection of region based representation for the process variables & factions
        JRadioButton reg_button = new JRadioButton(new AbstractAction(
                "Region Based Representation") {
            /**
			 * 
			 */
            private static final long serialVersionUID = -696736514796263674L;

            public void actionPerformed(ActionEvent e) {
                mapDrawer.setPVGrid(false);
            }
        });
        reg_button.setFont(reg_button.getFont().deriveFont(Font.PLAIN));
        button_panel.add(reg_button);
        button_panel
                .setBorder(BorderFactory.createCompoundBorder(BorderFactory
                                                                      .createTitledBorder("Process Variables & Factions"),
                                                              BorderFactory
                                                                      .createEmptyBorder(2,
                                                                                         2,
                                                                                         2,
                                                                                         2)));
        ButtonGroup pv_buttons = new ButtonGroup();
        pv_buttons.add(grid_button);
        pv_buttons.add(reg_button);

        return button_panel;
    }

    /**
     * Creates the panel which contains the following subpanels: 1. The panel for the symbol scale selection. 2. The panel for the symbol
     * opacity selection. 3. The panel for the symbol preference selection.
     */
    private JPanel createSymbolSizeAndOpacityPanel() {
        JPanel symbolDrawPanel = new JPanel();
        symbolDrawPanel.setLayout(new GridLayout(3, 1, 2, 2));
        symbolDrawPanel.add(createSymbolScalePanel());
        symbolDrawPanel.add(createOpacityPanel());
        symbolDrawPanel.add(createSymbolPreferenceCheckBoxPanel());
        symbolDrawPanel
                .setBorder(BorderFactory.createCompoundBorder(BorderFactory
                                                                      .createTitledBorder("Symbol preferences"),
                                                              BorderFactory
                                                                      .createEmptyBorder(2,
                                                                                         2,
                                                                                         2,
                                                                                         2)));

        return symbolDrawPanel;
    }

    /**
     * Creates the panel which contains the following subpanels: 1. The panel for the element location opacity selection. 2. The panel for
     * display selection of location and outline of the elements.
     */
    private JPanel createElementLocationPanel() {
        final MapDrawer mapDrawer = drawer;
        JPanel locationDrawPanel = new JPanel();
        locationDrawPanel.setLayout(new GridLayout(4, 1, 2, 2));
        // population centers
        JPanel populationPanel = new JPanel(new GridLayout(1, 2, 2, 2));
        JCheckBox populationLocationCheckBox = new JCheckBox(
                new AbstractAction("Show Location") {
                    /**
				 * 
				 */
                    private static final long serialVersionUID = -2929965831575446010L;

                    public void actionPerformed(ActionEvent e) {
                        JCheckBox box = (JCheckBox) e.getSource();
                        mapDrawer.setIsEnabledOutline(box.isSelected(),
                                                      TypeFactory
                                                              .getType("Population"));
                    }
                });
        populationLocationCheckBox.setFont(populationLocationCheckBox.getFont()
                .deriveFont(Font.PLAIN));
        populationPanel.add(populationLocationCheckBox);
        JCheckBox populationDeploymentCheckBox = new JCheckBox(
                new AbstractAction("Show Deployment") {
                    /**
				 * 
				 */
                    private static final long serialVersionUID = 5646499091972004407L;

                    public void actionPerformed(ActionEvent e) {
                        JCheckBox box = (JCheckBox) e.getSource();
                        mapDrawer.setIsEnabledLocation(box.isSelected(),
                                                       TypeFactory
                                                               .getType("Population"));
                    }
                });
        populationDeploymentCheckBox.setFont(populationLocationCheckBox
                .getFont().deriveFont(Font.PLAIN));
        populationPanel.add(populationDeploymentCheckBox);
        populationPanel
                .setBorder(BorderFactory.createCompoundBorder(BorderFactory
                                                                      .createTitledBorder("Population Centers"),
                                                              BorderFactory
                                                                      .createEmptyBorder(1,
                                                                                         1,
                                                                                         1,
                                                                                         1)));
        locationDrawPanel.add(populationPanel);
        // military units
        JPanel militaryUnitsPanel = new JPanel(new GridLayout(1, 2, 2, 2));
        JCheckBox militaryUnitLocationCheckBox = new JCheckBox(
                new AbstractAction("Show Location") {
                    /**
				 * 
				 */
                    private static final long serialVersionUID = -953469475624748271L;

                    public void actionPerformed(ActionEvent e) {
                        JCheckBox box = (JCheckBox) e.getSource();
                        mapDrawer.setIsEnabledOutline(box.isSelected(),
                                                      TypeFactory
                                                              .getType("MilitaryUnit"));
                    }
                });
        militaryUnitLocationCheckBox.setFont(militaryUnitLocationCheckBox
                .getFont().deriveFont(Font.PLAIN));
        militaryUnitsPanel.add(militaryUnitLocationCheckBox);
        JCheckBox militaryUnitDeploymentCheckBox = new JCheckBox(
                new AbstractAction("Show Deployment") {
                    /**
				 * 
				 */
                    private static final long serialVersionUID = 273754542727809467L;

                    public void actionPerformed(ActionEvent e) {
                        JCheckBox box = (JCheckBox) e.getSource();
                        mapDrawer.setIsEnabledLocation(box.isSelected(),
                                                       TypeFactory
                                                               .getType("MilitaryUnit"));
                    }
                });
        militaryUnitDeploymentCheckBox.setFont(militaryUnitLocationCheckBox
                .getFont().deriveFont(Font.PLAIN));
        militaryUnitsPanel.add(militaryUnitDeploymentCheckBox);
        militaryUnitsPanel.setBorder(BorderFactory
                .createCompoundBorder(BorderFactory
                        .createTitledBorder("Military Units"), BorderFactory
                        .createEmptyBorder(1, 1, 1, 1)));
        locationDrawPanel.add(militaryUnitsPanel);
        // agency teams
        JPanel agencyTeamsPanel = new JPanel(new GridLayout(1, 2, 2, 2));
        JCheckBox agencyTeamLocationCheckBox = new JCheckBox(
                new AbstractAction("Show Location") {
                    /**
				 * 
				 */
                    private static final long serialVersionUID = -4198976078454643694L;

                    public void actionPerformed(ActionEvent e) {
                        JCheckBox box = (JCheckBox) e.getSource();
                        mapDrawer.setIsEnabledOutline(box.isSelected(),
                                                      TypeFactory
                                                              .getType("AgencyTeam"));
                    }
                });
        agencyTeamLocationCheckBox.setFont(agencyTeamLocationCheckBox.getFont()
                .deriveFont(Font.PLAIN));
        agencyTeamsPanel.add(agencyTeamLocationCheckBox);
        JCheckBox agencyTeamDeploymentCheckBox = new JCheckBox(
                new AbstractAction("Show Deployment") {
                    /**
				 * 
				 */
                    private static final long serialVersionUID = -273797585921848337L;

                    public void actionPerformed(ActionEvent e) {
                        JCheckBox box = (JCheckBox) e.getSource();
                        mapDrawer.setIsEnabledLocation(box.isSelected(),
                                                       TypeFactory
                                                               .getType("AgencyTeam"));
                    }
                });
        agencyTeamDeploymentCheckBox.setFont(agencyTeamLocationCheckBox
                .getFont().deriveFont(Font.PLAIN));
        agencyTeamsPanel.add(agencyTeamDeploymentCheckBox);
        agencyTeamsPanel.setBorder(BorderFactory
                .createCompoundBorder(BorderFactory
                        .createTitledBorder("Agency Teams"), BorderFactory
                        .createEmptyBorder(1, 1, 1, 1)));
        locationDrawPanel.add(agencyTeamsPanel);
        // activities
        JPanel activityPanel = new JPanel(new GridLayout(1, 1, 2, 2));
        JCheckBox activityLocationCheckBox = new JCheckBox(new AbstractAction(
                "Show Location") {
            private static final long serialVersionUID = 650911501488149982L;

            public void actionPerformed(ActionEvent e) {
                JCheckBox box = (JCheckBox) e.getSource();
                mapDrawer.setIsEnabledOutline(box.isSelected(),
                                              TypeFactory.getType("Activity"));
            }
        });
        activityLocationCheckBox.setFont(activityLocationCheckBox.getFont()
                .deriveFont(Font.PLAIN));
        activityPanel.add(activityLocationCheckBox);
        activityPanel.setBorder(BorderFactory
                .createCompoundBorder(BorderFactory
                        .createTitledBorder("Activities"), BorderFactory
                        .createEmptyBorder(1, 1, 1, 1)));
        locationDrawPanel.add(activityPanel);
        //
        locationDrawPanel
                .setBorder(BorderFactory.createCompoundBorder(BorderFactory
                                                                      .createTitledBorder("Location Preferences"),
                                                              BorderFactory
                                                                      .createEmptyBorder(2,
                                                                                         2,
                                                                                         2,
                                                                                         2)));

        return locationDrawPanel;
    }

    /**
     * Creates the panel which contains the following subpanels: 1. The panel for display selection of the names of the cities. 2. The panel
     * for selection of the cities depending of it's size.
     */
    private JPanel createPopulationSelectionPanel() {
        JPanel cities_panel = new JPanel();
        cities_panel.setLayout(new BoxLayout(cities_panel, BoxLayout.Y_AXIS));
        cities_panel.add(createPopulationNameSelectionPanel());
        cities_panel.add(createPopulationLocationSelectionPanel());
        cities_panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createTitledBorder("Population Centers"), BorderFactory
                .createEmptyBorder(2, 2, 2, 2)));

        return cities_panel;
    }

    /**
     * Create new combined filter and update the map with it.
     */
    private void updateFilter() {
        CombinedORFilter filter = new CombinedORFilter();
        if (military_filter != null) {
            filter.add(military_filter);
        }
        if (agency_filter != null) {
            filter.add(agency_filter);
        }
        if (activity_filter != null) {
            filter.add(activity_filter);
        }
        if (population_filter != null) {
            filter.add(population_filter);
        }
        if (road_filter != null) {
            filter.add(road_filter);
        }
        if (other_filter != null) {
            filter.add(other_filter);
        }
        // update the map
        drawer.setDrawnMapElementsFilter(filter);
    }

    /**
     * Updates the filter for elements of type Activity.
     * 
     * @param client reference to the client.
     * @param past if true the past activities will pass the filter.
     * @param present if true the present activities will pass the filter.
     * @param future if true the future activities will pass the filter.
     */
    public void updateActivityFilter(Client client, boolean past,
            boolean present, boolean future) {
        // create activity filter
        activity_filter = new CombinedORFilter();
        // past activities
        if (past) {
            ActivityFilter afilter = new ActivityFilter(client,
                    ActivityFilter.PAST);
            ((CombinedORFilter) activity_filter).add(afilter);
        }
        // present activities
        if (present) {
            ActivityFilter afilter = new ActivityFilter(client,
                    ActivityFilter.PRESENT);
            ((CombinedORFilter) activity_filter).add(afilter);
        }
        // future activities
        if (future) {
            ActivityFilter afilter = new ActivityFilter(client,
                    ActivityFilter.FUTURE);
            ((CombinedORFilter) activity_filter).add(afilter);
        }
        updateFilter();
    }

    /**
     * Updates the filter for elements of type MilitaryUnit.
     * 
     * @param force_items list of JCheckBox items where the items represent military force units.
     * @param mobile_items list of JCheckBox items where the items represent military mobile units.
     */
    public void updateMilitaryUnitFilter(JCheckBoxMenuItem[] force_items,
            JCheckBoxMenuItem[] mobile_items) {
        military_filter = new CombinedORFilter();
        // check military units listed by their force power
        for (int i = 0; i < force_items.length; i++) {
            if (force_items[i].isSelected()) {
                MilitaryCodeFilter code_filter = new MilitaryCodeFilter(
                        MapConstants.forceSymbols[i], 11);
                ((CombinedORFilter) military_filter).add(code_filter);
            }
        }
        // check military units listed by their mobile power
        for (int i = 0; i < mobile_items.length; i++) {
            if (mobile_items[i].isSelected()) {
                MilitaryCodeFilter code_filter = new MilitaryCodeFilter(
                        MapConstants.mobileSymbols[i], 10);
                ((CombinedORFilter) military_filter).add(code_filter);
            }
        }
        updateFilter();
    }

    /**
     * Updates the filter for elements of type MilitaryUnit.
     * 
     * @param filter the miltary units filter update.
     */
    public void setMilitaryUnitFilter(ApproxsimObjectFilter filter) {
        military_filter = filter;
        updateFilter();
    }

    /**
     * Updates the filter for elements of type Agency.
     * 
     * @param filter the agency filter update.
     */
    public void setAgencyFilter(ApproxsimObjectFilter filter) {
        agency_filter = filter;
        updateFilter();
    }

    /**
     * Updates the filter for elements of type Population.
     * 
     * @param filter the population filter update.
     */
    public void setPopulationFilter(ApproxsimObjectFilter filter) {
        population_filter = filter;
        updateFilter();
    }

    public void setRoadsFilter(ApproxsimObjectFilter filter) {
        road_filter = filter;
        updateFilter();
    }

    public void setOtherFilter(ApproxsimObjectFilter filter) {
        other_filter = filter;
        updateFilter();
    }

    /**
     * Returns the panel which controls display of the cities, borders and graticules.
     */
    public JPanel getTopographyPanel() {
        JPanel topographyPanel = new JPanel();
        topographyPanel.setLayout(new BoxLayout(topographyPanel,
                BoxLayout.PAGE_AXIS));
        // the panel for choosing the population
        topographyPanel.add(createPopulationSelectionPanel());
        // the panel for selecting to display the region borders
        topographyPanel.add(createRegionBordersSelectionPanel());
        // the panel for setting the graticules
        topographyPanel.add(createGraticuleSelectionPanel());
        //
        return topographyPanel;

    }

    /**
     * Returns the panel which controls display of the military units, activities, agencies and process variables.
     */
    public JPanel getSimulationPanel() {
        JPanel simulationPanel = new JPanel();
        simulationPanel.setLayout(new BoxLayout(simulationPanel,
                BoxLayout.PAGE_AXIS));
        // the panel for chosing between grid or region based representation for process variables
        simulationPanel.add(createPVRepresentationPanel());
        simulationPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        // the panel for chosing between present and all units & teams
        simulationPanel.add(createElementPresencePanel());
        // the panel for choosing miltary units
        simulationPanel.add(createMilitaryUnitSelectionPanel());
        // the panel for choosing activities
        simulationPanel.add(createActivityTimeDependencePanel());
        // the panel for the agency teams
        simulationPanel.add(createTeamsAndAgenciesSelectionPanel());
        // the panel for graphs
        simulationPanel.add(createGraphSelectionPanel());
        // set border
        simulationPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        //
        return simulationPanel;

    }

    /**
     * Returns the panel which controls display of the general features on the map.
     */
    public JPanel getGeneralDisplayPanel() {
        JPanel general_panel = new JPanel();
        general_panel.setLayout(new BoxLayout(general_panel,
                BoxLayout.PAGE_AXIS));
        // the panel for chosing between grid or region based representation for process variables
        general_panel.add(createPVRepresentationPanel());
        // the panel for chosing between present and all units & teams
        general_panel.add(createElementPresencePanel());
        // the panel for setting the graticules
        general_panel.add(createGraticuleSelectionPanel());
        // the panel for choosing activities
        general_panel.add(createActivityTimeDependencePanel());
        // the panel for selecting to display the region borders
        general_panel.add(createRegionBordersSelectionPanel());
        // set border
        general_panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        //
        return general_panel;
    }

    /**
     * Returns the panel which controls display of the elements on the map.
     */
    public JPanel getElementsDisplayPanel() {
        JPanel elements_panel = new JPanel();
        elements_panel.setLayout(new BoxLayout(elements_panel,
                BoxLayout.PAGE_AXIS));
        // the panel for choosing the population
        elements_panel.add(createPopulationSelectionPanel());
        // the panel for choosing miltary units
        elements_panel.add(createMilitaryUnitSelectionPanel());
        // the panel for the agency teams
        elements_panel.add(createTeamsAndAgenciesSelectionPanel());
        // set border
        elements_panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        //
        return elements_panel;
    }

    /**
     * Returns the panel which controls preferences of the elements on the map.
     */
    public JPanel getPreferenceDisplayPanel() {
        JPanel pref_panel = new JPanel();
        pref_panel.setLayout(new BoxLayout(pref_panel, BoxLayout.PAGE_AXIS));
        // symbol size and opacity
        pref_panel.add(createSymbolSizeAndOpacityPanel());
        // location switch and opacity
        pref_panel.add(createElementLocationPanel());
        // set border
        pref_panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        //
        return pref_panel;
    }

    /**
     * Returns the symbol of the military unit with highest rank in the map.
     */
    public char getHighestRankUnit() {
        // find the military units with highest rang
        int counter = 0;
        while (counter < MapConstants.forceSymbols.length) {
            Enumeration e = (new MilitaryCodeFilter(
                    MapConstants.forceSymbols[counter], 11)).filterTree(client
                    .getRootObject());
            if (e.hasMoreElements()) {
                return MapConstants.forceSymbols[counter];
            }
            counter++;
        }
        // not found
        return '0';
    }

}
