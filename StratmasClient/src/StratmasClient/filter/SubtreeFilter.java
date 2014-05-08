package StratmasClient.filter;

import StratmasClient.object.StratmasObject;

/**
 * This filter passes all the elements in the actual subtree.
 */
public class SubtreeFilter extends StratmasObjectFilter {
    /**
     * Root of the subtree.
     */
    private StratmasObject root;

    /**
     * Create the filter.
     */
    public SubtreeFilter(StratmasObject root) {
        this.root = root;
    }

    /**
     * Returns true if root is the ancestor of the provided StratmasObject.
     * 
     * @param sObj the object to test
     */
    public boolean pass(StratmasObject sObj) {
        return sObj.equals(root) || sObj.isAncestor(root);
    }

}
