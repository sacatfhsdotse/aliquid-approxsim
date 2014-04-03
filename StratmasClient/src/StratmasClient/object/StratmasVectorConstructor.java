//         $Id: StratmasVectorConstructor.java,v 1.1 2006/03/22 14:30:52 dah Exp $
/*
 * @(#)StratmasObject.java
 */

package StratmasClient.object;

import StratmasClient.object.type.Declaration;
import java.util.Vector;

/**
 * StratmasVectorConstructor is a abstract supeclass for classes 
 * acting as source of new StratmasObjects created from hashes.
 *
 * @version 1, $Date: 2006/03/22 14:30:52 $
 * @author  Daniel Ahlin
*/
public abstract class StratmasVectorConstructor
{
    /**
     * The declaration for which the result is constructed.
     */
    Declaration declaration;

    /**
     * Creates a new object using specifications in declaration.
     *
     * @param declaration the declaration to use.
     */
    public StratmasVectorConstructor(Declaration declaration)
    {
        this.declaration = declaration;
    }

    /**
     * Returns the StratmasObject this component was created to provide.
     *
     * @param parts the parts to use in constructing the object.
     */
    public abstract StratmasObject getStratmasObject(Vector<StratmasObject> parts);

    /**
     * Returns the StratmasObject this component was created to provide.
     *
     * @param identifier sets the identifier of the object to the one provided
     * @param parts the parts to use in constructing the object.
     */
    public StratmasObject getStratmasObject(String identifier, Vector<StratmasObject> parts)
    {
        StratmasObject object = getStratmasObject(parts);
        object.setIdentifier(identifier);
        return object;
    }
    
    /**
     * The declaration for which the result is constructed.
     */
    protected Declaration getDeclaration()
    {
        return this.declaration;
    }
}
