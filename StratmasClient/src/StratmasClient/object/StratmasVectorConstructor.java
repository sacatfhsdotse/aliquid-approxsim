// $Id: ApproxsimVectorConstructor.java,v 1.1 2006/03/22 14:30:52 dah Exp $
/*
 * @(#)ApproxsimObject.java
 */

package ApproxsimClient.object;

import ApproxsimClient.object.type.Declaration;
import java.util.Vector;

/**
 * ApproxsimVectorConstructor is a abstract supeclass for classes acting as source of new ApproxsimObjects created from hashes.
 * 
 * @version 1, $Date: 2006/03/22 14:30:52 $
 * @author Daniel Ahlin
 */
public abstract class ApproxsimVectorConstructor {
    /**
     * The declaration for which the result is constructed.
     */
    Declaration declaration;

    /**
     * Creates a new object using specifications in declaration.
     * 
     * @param declaration the declaration to use.
     */
    public ApproxsimVectorConstructor(Declaration declaration) {
        this.declaration = declaration;
    }

    /**
     * Returns the ApproxsimObject this component was created to provide.
     * 
     * @param parts the parts to use in constructing the object.
     */
    public abstract ApproxsimObject getApproxsimObject(
            Vector<ApproxsimObject> parts);

    /**
     * Returns the ApproxsimObject this component was created to provide.
     * 
     * @param identifier sets the identifier of the object to the one provided
     * @param parts the parts to use in constructing the object.
     */
    public ApproxsimObject getApproxsimObject(String identifier,
            Vector<ApproxsimObject> parts) {
        ApproxsimObject object = getApproxsimObject(parts);
        object.setIdentifier(identifier);
        return object;
    }

    /**
     * The declaration for which the result is constructed.
     */
    protected Declaration getDeclaration() {
        return this.declaration;
    }
}
