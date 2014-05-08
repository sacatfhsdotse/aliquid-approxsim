package ApproxsimClient.filter;

import ApproxsimClient.object.ApproxsimObject;

/**
 * This filter passes all the elements in the actual subtree.
 */
public class SubtreeFilter extends ApproxsimObjectFilter {
    /**
     * Root of the subtree.
     */
    private ApproxsimObject root;

    /**
     * Create the filter.
     */
    public SubtreeFilter(ApproxsimObject root) {
        this.root = root;
    }

    /**
     * Returns true if root is the ancestor of the provided ApproxsimObject.
     * 
     * @param sObj the object to test
     */
    public boolean pass(ApproxsimObject sObj) {
        return sObj.equals(root) || sObj.isAncestor(root);
    }

}
