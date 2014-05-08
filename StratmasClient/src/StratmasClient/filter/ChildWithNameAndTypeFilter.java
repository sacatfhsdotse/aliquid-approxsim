package ApproxsimClient.filter;

import ApproxsimClient.object.ApproxsimObject;
import ApproxsimClient.object.type.Type;

/**
 * ChildWithNameAndTypefilter filters out ApproxsimObjects that have a child with a specified name that is of a specified type.
 * 
 * @version 1, $Date: 2006/03/22 14:30:50 $
 * @author Per Alexius
 */

public class ChildWithNameAndTypeFilter extends ApproxsimObjectFilter {
    private String mName;
    private Type mType;

    /**
     * Creates a new ChildWithNameAndTypeFilter allowing the specified child name and type.
     * 
     * @param name The child name to filter for.
     * @param type The child type to filter for.
     */
    public ChildWithNameAndTypeFilter(String name, Type type) {
        super();
        mName = name;
        mType = type;
    }

    /**
     * Returns true if the provided ApproxsimObject passes the filter.
     * 
     * @param sObj the object to test
     */
    public boolean pass(ApproxsimObject sObj) {
        ApproxsimObject child = sObj.getChild(mName);
        if (child == null) {
            return false;
        } else {
            return (mType == null || child.getType().canSubstitute(mType));
        }
    }
}
