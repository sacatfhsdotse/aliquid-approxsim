// $Id: DefaultComplexVectorConstructor.java,v 1.1 2006/03/22 14:30:51 dah Exp $
/*
 * @(#)DefaultComplexVectorConstructor
 */

package ApproxsimClient.object;

import ApproxsimClient.object.type.Declaration;
import java.util.Vector;

/**
 * DefaultComplexVectorConstructor constructs a ApproxsimComplex using a vector.
 * 
 * @version 1, $Date: 2006/03/22 14:30:51 $
 * @author Daniel Ahlin
 */
public class DefaultComplexVectorConstructor extends ApproxsimVectorConstructor {
    /**
     * Creates a new object using specifications in declaration.
     * 
     * @param declaration the declaration to use.
     */
    public DefaultComplexVectorConstructor(Declaration declaration) {
        super(declaration);
    }

    /**
     * Returns the ApproxsimObject this component was created to provide.
     * 
     * @param parts the parts to use in constructing the object.
     */
    public ApproxsimObject getApproxsimObject(Vector parts) {
        return new DefaultComplex(this.getDeclaration(), parts);
    }
}
