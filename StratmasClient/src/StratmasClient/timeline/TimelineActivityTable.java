package StratmasClient.timeline;

import java.util.Vector;
import java.util.Enumeration;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.DefaultCellEditor;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasList;
import StratmasClient.object.type.TypeFactory;
import StratmasClient.filter.TypeFilter;
import StratmasClient.treeview.TreeView;
import StratmasClient.treeview.TreeViewFrame;

/**
 * The table contains the activities displayed in the timeline. The rows of the table can be sorted
 * with respect to the column values. 
 *
 * @author Amir Filipovic
 */
public class TimelineActivityTable extends JTable implements MouseListener{
    /**
     * Reference to the timeline.
     */
    private Timeline timeline;
 
    /**
     * Creates new activity table.
     *
     * @param timeline reference to the timeline.
     */
    public TimelineActivityTable(Timeline timeline) {
        super(new TimelineActivityTableModel(timeline));
        ((TimelineActivityTableModel)getModel()).setTableHeader(getTableHeader());
        
        // set reference to the timeline
        this.timeline = timeline;
        
        // add mouse listener
        addMouseListener(this);
        
        // set the different parameters
        setShowHorizontalLines(false);
        setRowSelectionAllowed(true);
        setColumnSelectionAllowed(true);
        setIntercellSpacing(new Dimension(0,0));
        setOpaque(false);
        setDefaultRenderer(Object.class, new ActivityTableCellRenderer());
        getTableHeader().setForeground(Color.BLUE);
    }
    
    /**
     * Displays all information about the selected object after clicking with
     * the right mouse button on it's identifier in the table.
     */
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            // get window coordinates
            java.awt.Point p = e.getPoint();
            // get the row and the column numbers of the clicked cell 
            int row = rowAtPoint(p);
            int col = columnAtPoint(p);
            // column for the activities
            if (col == 0) {
                StratmasObject so = getActivity(row);
                // create the menu
                JPopupMenu menu = new JPopupMenu();
                // open new window with the information about the selected object
                menu.add(getShowInformationItem(so));
                menu.addSeparator();
                // remove the selected activity
                menu.add(getRemoveActivityItem(so));
                menu.show(this, (int)p.getX(), (int)p.getY());
            }
            // clumn for the resources
            else if (col == 3) {
                try {
                    StratmasObject so = getActivity(row).getParent().getParent();
                    if (so.getType().canSubstitute("MilitaryUnit")) {
                        // create the menu
                        JPopupMenu menu = new JPopupMenu();
                        // open new window with the information about the selected object
                        menu.add(getShowInformationItem(so)); 
                        menu.show(this, (int)p.getX(), (int)p.getY());
                    }
                }
                catch (NullPointerException exc) {
                }
            }
        }
    }

    /**
     * Not implemented.
     */
    public void mouseEntered(MouseEvent e) {}
    
    /**
     * Not implemented.
     */
    public void mouseExited(MouseEvent e) {}

    /**
     * Not implemented.
     */
    public void mousePressed(MouseEvent e) {}
        
    /**
     * Not implemented.
     */
    public void mouseReleased(MouseEvent e) {}
    
    /**
     * Returns true if the table contains the activity.
     */
    public boolean contains(StratmasObject activity) {
        TimelineActivityTableModel tableModel = (TimelineActivityTableModel)getModel();
        return tableModel.getActivities().contains(activity);
    }
    
    /**
     * Adds an activity to the table.
     *
     * @param activity the activity.
     */
    public void addActivity(StratmasObject activity) {
        TimelineActivityTableModel tableModel = (TimelineActivityTableModel)getModel();
        tableModel.add(activity);
    }
    
    /**
     * Adds an activity to the table. If sorted the table remains sorted after the 
     * activity is inserted. 
     *
     * @param activity the activity.
     */
    public void addSortedActivity(StratmasObject activity) {
        TimelineActivityTableModel tableModel = (TimelineActivityTableModel)getModel();
        tableModel.addSorted(activity);
    }
    
    /**
     * Removes an activity from the table.
     *
     * @param activity the activity.
     */
    public void removeActivity(StratmasObject activity) {
        TimelineActivityTableModel tableModel = (TimelineActivityTableModel)getModel();
        tableModel.remove(activity);
    }
    
    /**
     * Removes all activities from the table.
     */
    public void removeAllActivities() {
        TimelineActivityTableModel tableModel = (TimelineActivityTableModel)getModel();
        tableModel.removeAll();
    }
    
    /**
     * Removes the table.
     */
    public void remove() {
        timeline = null;
        removeAllActivities();
    }
    
    /**
     * Resets the table.
     */
    public void reset() {
        ((TimelineActivityTableModel)getModel()).reset();        
    }
    
    /**
     * Returns the activity at the given row.
     *
     * @param row the row number in the table.
     *
     * @return the activity at the specified row in the table.
     */
    public StratmasObject getActivity(int row) {
        Vector activities = ((TimelineActivityTableModel)getModel()).getActivities();
        if (activities.size() > row) {
            return (StratmasObject) activities.get(row);
        }
        return null;
    }
    
    /**
     * Returns the row number in the table where the activity is displayed.
     *
     * @param activity the given activity.
     *
     * @return the row in the table where the activity is displayed. If the activity
     *         is not contained in the table, -1 is returned.
     */
    public int getRow(StratmasObject activity) {
        return ((TimelineActivityTableModel)getModel()).getActivities().indexOf(activity);
    }
    
    /**
     * Returns the background of the given row.
     *
     * @param row the row number.
     *
     * @return the background of the given row.
     */
    public Color getBackground(int row) {
        return (row % 2 == 0) ? TimelineConstants.LIGHT : TimelineConstants.LIGHTER;
    } 
    
    /**
     * Updates the list of resources.
     *
     * @param root the parent node of all resources.
     */
    public void updateResources(StratmasObject root) {
        ActivityTableComboBox militaryUnitsComboBox = 
            ((TimelineActivityTableModel)getModel()).getMilitaryUnitsComboBox();
        // add all military units 
        TypeFilter filter = new TypeFilter(TypeFactory.getType("MilitaryUnit"), true);
        Enumeration mUnits = filter.filterTree(root);
        for (; mUnits.hasMoreElements(); ) {
            StratmasObject scom = (StratmasObject)mUnits.nextElement();
            if (scom.getType().canSubstitute("MilitaryUnit") && !(scom instanceof StratmasList)) {
                militaryUnitsComboBox.addResource(scom);
            }
        }
        getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(militaryUnitsComboBox));
    }
    
    /**
     * Returns the menu item used to show information about an activity.
     */
    private JMenuItem getShowInformationItem(StratmasObject so) {
        final StratmasObject fso = so;
        JMenuItem item = new JMenuItem("More information about "+so.getIdentifier().trim());
        item.addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent event) {
                    final TreeViewFrame frame = TreeView.getDefaultFrame(fso);
                    frame.setEditable(true);
                    javax.swing.SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                frame.setVisible(true);
                            }
                        });
                }
            });
        return item;
    }

    /**
     * Returns the menu item used to remove an activity.
     */
    private JMenuItem getRemoveActivityItem(StratmasObject so) {
        final StratmasObject fso = so;
        JMenuItem removeActivityItem = new JMenuItem("Remove "+so.getIdentifier().trim());
        removeActivityItem .addActionListener(new AbstractAction() {
                public void actionPerformed(ActionEvent event) {
                    fso.remove();
                }
            });
        return removeActivityItem;
    } 
    
}

