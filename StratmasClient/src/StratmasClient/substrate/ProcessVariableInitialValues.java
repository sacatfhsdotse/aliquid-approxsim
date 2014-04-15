package StratmasClient.substrate;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Comparator;
import java.util.Collections;

import StratmasClient.ProcessVariableDescription;
import StratmasClient.object.StratmasObject;

/**
 * The initial values for a process variable and a faction.
 */
class ProcessVariableInitialValues {
    /**
     * The process variable.
     */
    private ProcessVariableDescription processVariable;
    /**
     * The faction.
     */
    private StratmasObject faction;
    /**
     * The ordered list of ShapeValuePair objects.
     */
    private Vector<ShapeValuePair> shapeList = new Vector<ShapeValuePair>();
    /**
     * Used to compare ShapeValuePair objects wrt the time of creation.
     */
    private Comparator<ShapeValuePair> creationTimeComparator = new Comparator<ShapeValuePair>() {
            public int compare(ShapeValuePair svp1, ShapeValuePair svp2) {
                long cTime1 = svp1.getCreationTime();

                long cTime2 = svp2.getCreationTime();
                return (cTime1 > cTime2)? 1 : -1;
            }
        };
    
    /**
     * Creates new object.
     *
     * @param processVariable a process variable.
     * @param faction a faction.
     * @param shapeValues a key-value list where keys are Shape objects and values are lists of ShapeValuePair objects.
     */
    public ProcessVariableInitialValues(ProcessVariableDescription processVariable, StratmasObject faction, Hashtable shapeValues) {
        this.processVariable = processVariable;
        this.faction = faction;
        // put all shapes and values in the list
        for (Enumeration<Vector<ShapeValuePair>> e = shapeValues.elements(); e.hasMoreElements();) {
            Vector<ShapeValuePair> shValues = e.nextElement();
            for (int i = 0; i < shValues.size(); i++) { 
                if (!shapeList.contains(shValues.get(i))) {
                    shapeList.add(shValues.get(i));
                }
            }
        }
        // sort the list
        Collections.sort(shapeList, creationTimeComparator);
    }

    /**
     * Creates new object.
     *
     * @param processVariable a process variable.
     * @param faction a faction.
     * @param shapeList a sorted list of ShapeValuePair objects.
     */
    public ProcessVariableInitialValues(ProcessVariableDescription processVariable, StratmasObject faction, Vector<ShapeValuePair> shapeList) {
        this.processVariable = processVariable;
        this.faction = faction;
        this.shapeList = shapeList;
    }
    
    public ProcessVariableInitialValues(ProcessVariableDescription processVariable, StratmasObject faction) {
        this.processVariable = processVariable;
        this.faction = faction;
    }

    /**
     * Returns the process variable.
     */
    public ProcessVariableDescription getProcessVariable() {
        return processVariable;
    }
    
    /**
     * Returns the faction.
     */
    public StratmasObject getFaction() {
        return faction;
    }

    /**
     * Returns the sorted list of ShapeValuePair objects.
     */
    public Vector<ShapeValuePair> getOrderedListOfShapes() {
        return shapeList;
    }
    
    /**
     *  Returns the values of this object as a String.
     */
    public String toString() {
        String hasFac = String.valueOf(processVariable.hasFactions());
        String min = String.valueOf(processVariable.getMin());
        String max = String.valueOf(processVariable.getMax());
        String pv = new String("process variable : " + processVariable.getName() + " " + processVariable.getCategory() + " " + hasFac + " " + min + " " + max + "\n");
        String facStr = (faction == null)? new String("") : new String("faction : " + faction.getReference().toString() + "\n");
        String shapes = new String("List of shape - value pairs : \n");
        for (int i = 0; i < shapeList.size(); i++) {
            shapes = shapes.concat(shapeList.get(i).toString() + "\n");
        }
        return pv + facStr + shapes;
    }
}
