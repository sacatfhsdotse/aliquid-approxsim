package StratmasClient.map;

import java.util.Comparator;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.table.AbstractTableModel;

import StratmasClient.object.StratmasObject;
import StratmasClient.StratmasConstants;
import StratmasClient.ProcessVariableDescription;

/**
 * The table model for the table of process variables and factions.
 *
 * @version 1.0
 * @author Amir Filipovic 
 */
public class ProcessVariableTableModel extends AbstractTableModel {
    /**
	 * 
	 */
	private static final long serialVersionUID = -2644064665423546096L;
	/**
     * Names of the colum headers.
     */
    private Object[] columns;
    /**
     * The values of the process variables and factions.
     */
    private Object[][] pvValues;
    /**
     * Used to compare the process variable wrt the categories and the names.
     */
    private Comparator categoryComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                ProcessVariableDescription pvd1 = (ProcessVariableDescription)((Object[])o1)[0];
                ProcessVariableDescription pvd2 = (ProcessVariableDescription)((Object[])o2)[0];
                String c1 = pvd1.getCategory();
                String c2 = pvd2.getCategory();
                if (c1.equals(c2)) {
                    String n1 = pvd1.getName();
                    String n2 = pvd2.getName();
                    return n1.compareTo(n2);
                }
                else {
                    if (c1.equals(ProcessVariableTablePanel.getMainCategory())) {
                        return -1;
                    }
                    else if (c2.equals(ProcessVariableTablePanel.getMainCategory())) {
                        return 1;
                    }
                    else {
                        for (int i = 0; i < MapConstants.pvCategories.length; i++) {
                            if (c1.equals(MapConstants.pvCategories[i])) {
                                return -1;
                            }
                            else if (c2.equals(MapConstants.pvCategories[i])) {
                                return 1;
                            }
                        }
                        return c1.compareTo(c2); 
                    }
                }
            }
        };
    
    /**
     * Creates new table model. 
     *     
     * @param pv the process variables.
     * @param factions the factions.
     */
    public ProcessVariableTableModel(Vector pv, Vector factions) {
        // get columns
        columns = new Object[factions.size() + 3];
        columns[0] = new String("PV");
        columns[1] = new String("Category");
        columns[2] = new String(StratmasConstants.factionAll);
        for (int i = 0; i < factions.size(); i++) {
            columns[i+3] = (StratmasObject)factions.get(i);
        }
        // get rows
        pvValues = new Object[pv.size()][factions.size() + 3];
        for (int i = 0; i < pv.size(); i++) {
            pvValues[i][0] = (ProcessVariableDescription)pv.get(i);
        }  
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
        return pvValues.length;
    }
        
    /**
     * Returns the name of the column.
     *
     * @param col the column.
     */
    public String getColumnName(int col) {
        if (columns[col] instanceof StratmasObject) {
            return ((StratmasObject)columns[col]).getReference().getIdentifier().trim();
        }
        else {
            return (String)columns[col];
        }
    }
    
    /**
     * Returns the value of the specified cell.
     *     
     * @param row the row.
     * @param col the column.
     */
    public Object getValueAt(int row, int col) {
        if (col == 0) {
            return ((ProcessVariableDescription)pvValues[row][0]).getName();
        }
        else if (col == 1) {
            return ((ProcessVariableDescription)pvValues[row][0]).getCategory();
        }
        else {
            return pvValues[row][col];
        }
    }
    
    /**
     * Returns the process variable in the specified row.
     *
     * @param row the row.
     */
    public ProcessVariableDescription getProcessVariable(int row) {
        return (ProcessVariableDescription)pvValues[row][0];
    }
    
    /**
     * Returns all process variables which belong to the given category.
     *
     * @param category the category of the process variables.
     */
    public Vector getProcessVariables(String category) {
        Vector pvs = new Vector();
        for (int i = 0; i < pvValues.length; i++) {
            ProcessVariableDescription pv = (ProcessVariableDescription)pvValues[i][0];
            if (pv.getCategory().equals(category)) {
                pvs.add(pv);
            }
        }
        return pvs;
    }
    
    /*
     * Checks if the cell is editable.
     *     
     * @param row the row.
     * @param col the column.
     */
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    /*
     * Updates the chosen cell with the specified value.
     *     
     * @param value the value.
     * @param row the row.
     * @param col the column.
     */
    public void setValueAt(Object value, int row, int col) {
        pvValues[row][col] = value;
        fireTableCellUpdated(row, col);
    }
    
    /**
     * Sorts the table. It's sorted with respect to the categories of the process variables.
     */
    public void sort() {
        // sort the process variables
        Arrays.sort(pvValues, categoryComparator);
        fireTableDataChanged();
    }
    
}
