package StratmasClient.timeline;

import java.text.ParseException;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Comparator;
import java.util.Collections;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import StratmasClient.Debug;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasObjectFactory;
import StratmasClient.object.StratmasTimestamp;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.StratmasEventListener;
import StratmasClient.object.StratmasReference;
import StratmasClient.object.StratmasList;
import StratmasClient.object.primitive.Timestamp;
import StratmasClient.object.type.TypeFactory;
import StratmasClient.object.type.Declaration;

/**
 * The table model for the table of activities.
 * 
 * @author Amir Filipovic
 */
public class TimelineActivityTableModel extends AbstractTableModel implements
        StratmasEventListener {
    /**
	 * 
	 */
    private static final long serialVersionUID = 7305358507421557020L;
    /**
     * The table header.
     */
    private JTableHeader tableHeader;
    /**
     * The column names.
     */
    private String[] columns = { "Activity", "Start time", "End time",
            "Resource", "Affiliation" };
    /**
     * The list of activities.
     */
    private Vector<StratmasObject> activityList = new Vector<StratmasObject>();
    /**
     * Indicator for the order of activities in the table.
     */
    public static final int DESCENDING = -1;
    /**
     * Indicator for unsorted activities in the table.
     */
    public static final int NOT_SORTED = 0;
    /**
     * Indicator for ascending order of activities in the table.
     */
    public static final int ASCENDING = 1;
    /**
     * The actual order of the activities in the table.
     */
    private int currentOrder = NOT_SORTED;
    /**
     * The last sorted column.
     */
    private int lastSortedCol = -1;
    /**
     * The last removed row in the table.
     */
    private int lastRemovedRow = -1;
    /**
     * The list of all military units.
     */
    private ActivityTableComboBox militaryUnitsComboBox = new ActivityTableComboBox();
    /**
     * Used to compare start times of the activities.
     */
    public static final Comparator<StratmasObject> START_TIME_COMPARATOR = new Comparator<StratmasObject>() {
        public int compare(StratmasObject o1, StratmasObject o2) {
            StratmasTimestamp st1 = (StratmasTimestamp) o1.getChild("start");
            StratmasTimestamp st2 = (StratmasTimestamp) o2.getChild("start");
            if (st1 == null) {
                return -1;
            } else if (st2 == null) {
                return 1;
            }
            Timestamp t1 = st1.getValue();
            Timestamp t2 = st2.getValue();
            return (t1.getMilliSecs() < t2.getMilliSecs()) ? -1 : (t1
                    .getMilliSecs() > t2.getMilliSecs()) ? 1 : 0;
        }
    };
    /**
     * Used to compare end times of the activities.
     */
    public static final Comparator<StratmasObject> END_TIME_COMPARATOR = new Comparator<StratmasObject>() {
        public int compare(StratmasObject o1, StratmasObject o2) {
            StratmasTimestamp st1 = (StratmasTimestamp) o1.getChild("end");
            StratmasTimestamp st2 = (StratmasTimestamp) o2.getChild("end");
            if (st1 == null) {
                return -1;
            } else if (st2 == null) {
                return 1;
            }
            Timestamp t1 = st1.getValue();
            Timestamp t2 = st2.getValue();
            return (t1.getMilliSecs() < t2.getMilliSecs()) ? -1 : (t1
                    .getMilliSecs() > t2.getMilliSecs()) ? 1 : 0;
        }
    };
    /**
     * Used to compare the activity names.
     */
    public static final Comparator<StratmasObject> ACTIVITY_NAME_COMPARATOR = new Comparator<StratmasObject>() {
        public int compare(StratmasObject o1, StratmasObject o2) {
            String s1 = o1.getIdentifier();
            String s2 = o2.getIdentifier();
            return s1.compareTo(s2);
        }
    };
    /**
     * Used to compare names of the resources.
     */
    public static final Comparator<StratmasObject> MU_NAME_COMPARATOR = new Comparator<StratmasObject>() {
        public int compare(StratmasObject o1, StratmasObject o2) {
            try {
                StratmasObject mu1 = o1.getParent().getParent();
                StratmasObject mu2 = o2.getParent().getParent();
                if (!mu1.getType().canSubstitute("MilitaryUnit")) {
                    return -1;
                } else if (!mu2.getType().canSubstitute("MilitaryUnit")) {
                    return 1;
                }
                String s1 = mu1.getIdentifier();
                String s2 = mu2.getIdentifier();
                return s1.compareTo(s2);
            } catch (NullPointerException exc) {
                return 0;
            }
        }
    };
    /**
     * Used to compare names of the affiliations of the resources.
     */
    public static final Comparator<StratmasObject> AFF_NAME_COMPARATOR = new Comparator<StratmasObject>() {
        public int compare(StratmasObject o1, StratmasObject o2) {
            try {
                StratmasObject mu1 = o1.getParent().getParent();
                StratmasObject mu2 = o2.getParent().getParent();
                if (!mu1.getType().canSubstitute("MilitaryUnit")) {
                    return -1;
                } else if (!mu2.getType().canSubstitute("MilitaryUnit")) {
                    return 1;
                }
                String s1 = ((StratmasReference) mu1.getChild("affiliation"))
                        .getValue().getIdentifier();
                String s2 = ((StratmasReference) mu2.getChild("affiliation"))
                        .getValue().getIdentifier();
                return s1.compareTo(s2);
            } catch (NullPointerException exc) {
                return 0;
            }
        }
    };
    /**
     * The list of comparators. Rawtype becouse Java does not support generics at runtime, static does not help.
     */
    @SuppressWarnings("rawtypes") private Comparator[] comparators = {
            ACTIVITY_NAME_COMPARATOR, START_TIME_COMPARATOR,
            END_TIME_COMPARATOR, MU_NAME_COMPARATOR, AFF_NAME_COMPARATOR };

    /**
     * Creates new table model.
     */
    public TimelineActivityTableModel(Timeline timeline) {
        // update the display of the activity symbols when the table changes
        final Timeline tline = timeline;
        addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                tline.getTimelinePanel().updateActivityList();
            }
        });
    }

    /**
     * Returns the number of columns.
     */
    public int getColumnCount() {
        return columns.length;
    }

    /**
     * Returns the number of rows.
     */
    public int getRowCount() {
        return activityList.size();
    }

    /**
     * Returns the name of the column.
     */
    public String getColumnName(int col) {
        return columns[col];
    }

    /**
     * Returns the class of the column.
     */
    public Class<String> getColumnClass(int col) {
        return String.class;
    }

    /**
     * Returns the value of the specified cell.
     */
    public synchronized Object getValueAt(int row, int col) {
        if (row < activityList.size()) {
            StratmasObject activity = activityList.get(row);
            // name of the activity
            if (col == 0) {
                return activity.getIdentifier();
            }
            // start time of the activity
            else if (col == 1) {
                StratmasTimestamp t = (StratmasTimestamp) activity
                        .getChild("start");
                if (t != null) {
                    return t.valueToPrettyString();
                }
            }
            // end time of the activity
            else if (col == 2) {
                StratmasTimestamp t = (StratmasTimestamp) activity
                        .getChild("end");
                if (t != null) {
                    return t.valueToPrettyString();
                }
            }
            // military units which executes the activity
            else if (col == 3) {
                try {
                    StratmasObject parent = activity.getParent().getParent();
                    if (parent.getType().canSubstitute("MilitaryUnit")) {
                        return parent.getIdentifier();
                    } else {
                        return new String("");
                    }
                } catch (NullPointerException exc) {
                    return null;
                }
            }
            // affiliation of the military unit
            else if (col == 4) {
                try {
                    StratmasObject parent = activity.getParent().getParent();
                    if (parent.getType().canSubstitute("MilitaryUnit")) {
                        return ((StratmasReference) parent
                                .getChild("affiliation")).getValue()
                                .getIdentifier();
                    } else {
                        return new String("");
                    }
                } catch (NullPointerException exc) {
                    return null;
                }
            }
        }
        //
        return null;
    }

    /*
     * Checks if the cell is editable.
     */
    public boolean isCellEditable(int row, int col) {
        StratmasObject activity = activityList.get(row);
        // end time not allowed according to the schema
        if (col == 2) {
            Declaration decl = activity.getType().getSubElement("end");
            if (decl == null) {
                return false;
            }
        } else if (col == 3) {
            try {
                // get the current resource value
                StratmasObject parent = activity.getParent().getParent();
                if (!parent.getType().canSubstitute("MilitaryUnit")) {
                    return false;
                }
            } catch (NullPointerException exc) {
                return false;
            }
        } else if (col == 4) {
            return false;
        }
        return true;
    }

    /**
     * Updates the activity.
     * 
     * @param value the modified activity value.
     * @param row the row of the modified cell.
     * @param col the column of the modified cell.
     */
    public void setValueAt(Object value, int row, int col) {
        final StratmasObject activity = activityList.get(row);
        // change the identifier of the activity
        if (col == 0) {
            activity.setIdentifier((String) value);
        }
        // change the start time of the activity
        else if (col == 1) {
            String timeString = (String) value;
            StratmasTimestamp t = (StratmasTimestamp) activity
                    .getChild("start");
            if (t != null) {
                try {
                    t.valueFromString(timeString, this);
                } catch (ParseException e) {
                    JOptionPane
                            .showMessageDialog((JFrame) null,
                                               "Parse error:\nUnable to assign \""
                                                       + timeString
                                                       + "\" to a Timestamp ",
                                               "Parse Error",
                                               JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        // change the end time of the activity
        else if (col == 2) {
            String timeString = (String) value;
            try {
                StratmasTimestamp endTime = (StratmasTimestamp) activity
                        .getChild("end");
                Declaration decl = activity.getType().getSubElement("end");
                // modify the end time
                if (endTime != null
                        && (timeString.length() > 0 || !decl.isOptional())) {
                    endTime.valueFromString(timeString, this);
                }
                // remove the end time
                else if (endTime != null && timeString.length() == 0
                        && decl.isOptional()) {
                    endTime.remove();
                }
                // add the end time
                else if (endTime == null && timeString.length() > 0) {
                    StratmasTimestamp endT = (StratmasTimestamp) StratmasObjectFactory
                            .create(TypeFactory.getType("Timestamp"));
                    endT.setIdentifier("end");
                    endT.valueFromString(timeString, this);
                    activity.add(endT);
                }
            } catch (ParseException e) {
                JOptionPane.showMessageDialog((JFrame) null,
                                              "Parse error:\nUnable to assign \""
                                                      + timeString
                                                      + "\" to a Timestamp ",
                                              "Parse Error",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
        // change the resource of the activity
        else if (col == 3) {
            String newValue = (String) value;
            try {
                // get the current resource value
                StratmasObject parent = activity.getParent().getParent();
                if (parent.getType().canSubstitute("MilitaryUnit")) {
                    // military units with the selected identifier
                    Vector selectedUnits = militaryUnitsComboBox
                            .getResources(newValue);
                    // the resource is changed
                    if (!newValue.equals(parent.getIdentifier())
                            || (selectedUnits != null && selectedUnits.size() > 1)) {
                        // only one military unit found
                        if (selectedUnits.size() == 1) {
                            lastRemovedRow = activityList.indexOf(activity);
                            activity.remove();
                            // get the new resource
                            StratmasObject res = (StratmasObject) selectedUnits
                                    .firstElement();
                            ((StratmasList) res.getChild("activities"))
                                    .addWithUniqueIdentifier(activity);

                        }
                        // several military units found
                        else if (selectedUnits.size() > 1
                                && militaryUnitsComboBox.isPopupVisible()) {
                            // create a menu
                            JPopupMenu menu = new JPopupMenu();
                            JMenu submenu = new JMenu(
                                    "Please refine your selection :");
                            for (Enumeration e = selectedUnits.elements(); e
                                    .hasMoreElements();) {
                                final StratmasObject mUnit = (StratmasObject) e
                                        .nextElement();
                                JMenuItem item = new JMenuItem(
                                        getPathOfMilitaryUnits(mUnit));
                                item.addActionListener(new AbstractAction() {
                                    /**
									 * 
									 */
                                    private static final long serialVersionUID = 3644128782915075854L;

                                    public void actionPerformed(
                                            ActionEvent event) {
                                        activity.remove();
                                        ((StratmasList) mUnit
                                                .getChild("activities"))
                                                .addWithUniqueIdentifier(activity);
                                    }
                                });
                                submenu.add(item);
                            }
                            menu.add(submenu);
                            menu.show(militaryUnitsComboBox,
                                      (int) militaryUnitsComboBox.getRootPane()
                                              .getLocation(null).getX(),
                                      (int) militaryUnitsComboBox.getRootPane()
                                              .getLocation(null).getY());
                        }
                    }
                }
            } catch (NullPointerException exc) {
                JOptionPane.showMessageDialog((JFrame) null,
                                              "Unable to change the resource for activity "
                                                      + activity
                                                              .getIdentifier(),
                                              "Error",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Called when an activity contained in the table has to be updated.
     * 
     * @param event the event causing the call.
     */
    public void eventOccured(StratmasEvent event) {
        // the start time and the end time are considered
        if (event.isChildChanged()) {
            int row = activityList.indexOf((StratmasObject) event.getSource());
            StratmasObject child = (StratmasObject) event.getArgument();
            // update the start time
            if (child.getIdentifier().equals("start")) {
                fireTableCellUpdated(row, 1);
                if (lastSortedCol == 1) {
                    setUnsorted();
                }
            }
            // update the end time
            if (child.getIdentifier().equals("end")) {
                fireTableCellUpdated(row, 2);
                if (lastSortedCol == 2) {
                    setUnsorted();
                }
            }
        } else if (event.isIdentifierChanged()) {
            StratmasObject src = (StratmasObject) event.getSource();
            // update the name of the military unit which exectutes the activity
            if (src.getType().canSubstitute("MilitaryUnit")) {
                for (int i = 0; i < activityList.size(); i++) {
                    if (activityList.get(i).getParent().getParent().equals(src)) {
                        fireTableCellUpdated(i, 3);
                    }
                }
                if (lastSortedCol == 3) {
                    setUnsorted();
                }
            }
            // update the name of the activity
            else {
                int row = activityList.indexOf(src);
                fireTableCellUpdated(row, 0);
                if (lastSortedCol == 0) {
                    setUnsorted();
                }
            }
        }
        // replace the activity
        else if (event.isReplaced()) {
            // UNTESTED - the replace code is untested
            Debug.err
                    .println("FIXME - Replace behavior untested in TimelineActivityTable");
            remove((StratmasObject) event.getSource());
            add((StratmasObject) event.getArgument());
        }
    }

    /**
     * Adds a new activity to the table.
     */
    public void add(StratmasObject activity) {
        if (!activityList.contains(activity)) {
            try {
                // used when the activity has changed it's resource in the table
                if (lastRemovedRow != -1) {
                    activityList.add(lastRemovedRow, activity);
                    lastRemovedRow = -1;
                }
                // used when "new" activity is added
                else {
                    activityList.add(activity);
                }
                activity.addEventListener(this);
                // check for the military unit
                StratmasObject anc = activity.getParent().getParent();
                if (anc.getType().canSubstitute("MilitaryUnit")
                        && !militaryUnitsComboBox.contains(anc)) {
                    anc.addEventListener(this);
                }
                fireTableRowsInserted(activityList.size() - 1,
                                      activityList.size() - 1);
                setUnsorted();
            } catch (NullPointerException exc) {}
        }
    }

    /**
     * Adds a new activity to the table. If the table is sorted the activity is inserted such that the sorting is remained.
     */
    public void addSorted(StratmasObject activity) {
        if (!activityList.contains(activity)) {
            if (currentOrder == NOT_SORTED) {
                add(activity);
            } else {
                try {
                    Comparator<StratmasObject> comp = comparators[lastSortedCol];
                    int i = 0;
                    boolean inserted = false;
                    while (i < activityList.size() && !inserted) {
                        int res = comp.compare(activity, activityList.get(i));
                        if (res > 0 && currentOrder == ASCENDING) {
                            i++;
                        } else if (res < 0 && currentOrder == DESCENDING) {
                            i++;
                        } else {
                            activityList.add(i, activity);
                            inserted = true;
                        }
                    }
                    if (!inserted) {
                        activityList.add(activity);
                    }
                    activity.addEventListener(this);
                    // check for the military unit
                    StratmasObject anc = activity.getParent().getParent();
                    if (anc.getType().canSubstitute("MilitaryUnit")
                            && !militaryUnitsComboBox.contains(anc)) {
                        anc.addEventListener(this);
                    }
                    fireTableRowsInserted(i, i);
                } catch (NullPointerException exc) {}
            }
        }
    }

    /**
     * Removes an activity from the table.
     */
    public void remove(StratmasObject activity) {
        if (activityList.contains(activity)) {
            int index = activityList.indexOf(activity);
            activityList.remove(activity);
            activity.removeEventListener(this);
            // check for the military unit
            StratmasObject anc = activity.getParent().getParent();
            if (anc.getType().canSubstitute("MilitaryUnit")) {
                anc.removeEventListener(this);
            }
            fireTableRowsDeleted(index, index);
            if (activityList.isEmpty()) {
                setUnsorted();
            }
        }
    }

    /**
     * Removes all activities from the table.
     */
    public void removeAll() {
        while (!activityList.isEmpty()) {
            remove(activityList.firstElement());
        }
    }

    /**
     * Resets the table model.
     */
    public void reset() {
        // militaryUnitsComboBox.reset();
        removeAll();
        militaryUnitsComboBox = new ActivityTableComboBox();
    }

    /**
     * Sets the actual table header.
     */
    public void setTableHeader(JTableHeader tableHeader) {
        this.tableHeader = tableHeader;
        if (this.tableHeader != null) {
            final TimelineActivityTableModel self = this;
            this.tableHeader.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    JTableHeader h = (JTableHeader) e.getSource();
                    TableColumnModel columnModel = h.getColumnModel();
                    int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                    int column = columnModel.getColumn(viewColumn)
                            .getModelIndex();
                    // sort the table
                    self.sort(column);
                }
            });
            final TableCellRenderer fRenderer = this.tableHeader
                    .getDefaultRenderer();
            this.tableHeader.setDefaultRenderer(new TableCellRenderer() {
                public Component getTableCellRendererComponent(JTable table,
                        Object value, boolean isSelected, boolean hasFocus,
                        int row, int column) {
                    Component c = fRenderer
                            .getTableCellRendererComponent(table, value,
                                                           isSelected,
                                                           hasFocus, row,
                                                           column);
                    if (c instanceof JLabel) {
                        JLabel l = (JLabel) c;
                        l.setHorizontalTextPosition(JLabel.LEFT);
                        int modelColumn = table
                                .convertColumnIndexToModel(column);
                        l.setIcon(getHeaderRendererIcon(modelColumn, l
                                .getFont().getSize()));
                    }
                    return c;
                }
            });
        }
    }

    /**
     * Returns the list of activities displayed in the table.
     */
    public Vector<StratmasObject> getActivities() {
        return activityList;
    }

    /**
     * Returns the tree path consisting of miltary units only.
     */
    public String getPathOfMilitaryUnits(StratmasObject militaryUnit) {
        String path = militaryUnit.getIdentifier();
        StratmasObject mu = militaryUnit;
        while (mu.getParent() != null) {
            mu = mu.getParent();
            if (mu.getType().canSubstitute("MilitaryUnit")
                    && !(mu instanceof StratmasList)) {
                path = mu.getIdentifier().concat(" : ").concat(path);
            }
        }
        return path;
    }

    /**
     * Returns the combo box which contains the list of military units.
     */
    public ActivityTableComboBox getMilitaryUnitsComboBox() {
        return militaryUnitsComboBox;
    }

    /**
     * Sorts the table. It's sorted with respect to the values of the column where the sorting is initialized.
     * 
     * @param col the column where the sorting is initialized.
     */
    public void sort(int col) {
        // sort the table
        sortByOrder(comparators[col], col);
        // update the latest column the sorting was fired
        lastSortedCol = col;
        // update the table
        fireTableDataChanged();
        if (tableHeader != null) {
            tableHeader.repaint();
        }
    }

    /**
     * Sorts the table.
     * 
     * @param comparator the comaparator used for the sorting.
     * @param col the column where the sorting is initialized.
     */
    private void sortByOrder(Comparator<StratmasObject> comparator, int col) {
        // if the table is already sorted wrt the column then reverse it
        if (currentOrder == ASCENDING && lastSortedCol == col) {
            Collections.reverse(activityList);
            currentOrder = DESCENDING;
        }
        // sort the table
        else {
            Collections.sort(activityList, comparator);
            currentOrder = ASCENDING;
        }
    }

    /**
     * Sets the table unsorted anr repaints the header.
     */
    private void setUnsorted() {
        currentOrder = NOT_SORTED;
        if (tableHeader != null) {
            tableHeader.repaint();
        }
    }

    /**
     * Returns the icon used for rendering the header.
     * 
     * @param column the header column.
     * @param size the size of the icon.
     */
    protected Icon getHeaderRendererIcon(int column, int size) {
        // not sorted or not actual column
        if (currentOrder == NOT_SORTED || lastSortedCol != column) {
            return null;
        }
        // get the arrow
        return new Arrow((currentOrder == ASCENDING), size);
    }

    /**
     * The icon used for the header rendering.
     */
    private static class Arrow implements Icon {
        /**
         * Indicator for descending or ascending order.
         */
        private boolean descending;
        /**
         * The size of the icon.
         */
        private int size;

        /**
         * Creates the arrow.
         */
        public Arrow(boolean descending, int size) {
            this.descending = descending;
            this.size = size;
        }

        /**
         * Paints the icon.
         */
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Color color = (c == null) ? Color.GRAY : c.getBackground();
            int dx = size / 2;
            int dy = descending ? dx : -dx;
            // Align icon (roughly) with font baseline.
            y = y + 5 * size / 6 + (descending ? -dy : 0);
            int shift = descending ? 1 : -1;
            g.translate(x, y);

            // Right diagonal.
            g.setColor(color.darker());
            g.drawLine(dx / 2, dy, 0, 0);
            g.drawLine(dx / 2, dy + shift, 0, shift);

            // Left diagonal.
            g.setColor(color.brighter());
            g.drawLine(dx / 2, dy, dx, 0);
            g.drawLine(dx / 2, dy + shift, dx, shift);

            // Horizontal line.
            if (descending) {
                g.setColor(color.darker().darker());
            } else {
                g.setColor(color.brighter().brighter());
            }
            g.drawLine(dx, 0, 0, 0);

            g.setColor(color);
            g.translate(-x, -y);
        }

        /**
         * Returns the icon width.
         */
        public int getIconWidth() {
            return size;
        }

        /**
         * Returns the icon height.
         */
        public int getIconHeight() {
            return size;
        }
    }
}
