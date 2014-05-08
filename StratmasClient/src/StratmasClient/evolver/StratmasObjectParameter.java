// $Id: ApproxsimObjectParameter.java,v 1.3 2006/03/22 14:30:50 dah Exp $
/*
 * @(#)ApproxsimObjectParameter.java
 */

package ApproxsimClient.evolver;

import ApproxsimClient.object.primitive.Reference;
import ApproxsimClient.object.ApproxsimObject;

import ApproxsimClient.filter.ApproxsimObjectAdapter;

/**
 * Represents a interface for parameters backed by ApproxsimObjects.
 * 
 * @version 1, $Date: 2006/03/22 14:30:50 $
 * @author Daniel Ahlin
 */
public interface ApproxsimObjectParameter extends ApproxsimObjectAdapter {
    /**
     * Returns the reference of the ApproxsimObject backing this parameter.
     */
    public Reference getReference();

    /**
     * Returns the ApproxsimObject backing this parameter.
     */
    public ApproxsimObject getApproxsimObject();
}
