// $Id: TypeFactory.java,v 1.2 2006/03/31 16:55:51 dah Exp $
/*
 * @(#)StratmasObjectFactory.java
 */

package StratmasClient.object.type;

import StratmasClient.object.type.Type;
import StratmasClient.object.type.TypeInformation;
import StratmasClient.object.XMLHelper;

import StratmasClient.StratmasConstants;

import org.w3c.dom.Element;

/**
 * TypeFactory is globally availiable resource for aquiring types.
 * 
 * @version 1, $Date: 2006/03/31 16:55:51 $
 * @author Daniel Ahlin
 */

public class TypeFactory {
    /**
     * The typeInformation object of this class.
     */
    static TypeInformation typeInformation = null;

    /**
     * Default namespace to look in.
     */
    static String defaultNamespace = StratmasConstants.stratmasNamespace;

    /**
     * Uri from which to load the schema.
     */
    static String defaultUri = StratmasConstants.STRATMAS_SIMULATION_SCHEMA;

    /**
     * Creates the typeInformation object to use when constructing types.
     * 
     * @param uri the location of the uri.
     */
    static TypeInformation createTypeInformation(String uri) {
        return new TypeInformation(uri);
    }

    /**
     * Retrieves an instance of Type with specified name and namespace.
     * 
     * @param type the name of the type
     * @param namespace the namespace to look in.
     */
    public static Type getType(String type, String namespace) {
        if (typeInformation == null) {
            typeInformation = createTypeInformation(defaultUri);
        }

        return typeInformation.getType(type, namespace);
    }

    /**
     * Retrieves an instance of Type with specified name from default namespace.
     * 
     * @param type the name of the type
     */
    public static Type getType(String type) {
        return getType(type, defaultNamespace);
    }

    /**
     * Retrieves an instance of Type matching the type of the specified element, or null if none found.
     * 
     * @param element the element to get type for.
     */
    public static Type getType(Element element) {
        return XMLHelper.getType(element);
    }

    /**
     * Retrieves type typeInformation the TypeFactory uses.
     */
    public static TypeInformation getTypeInformation() {
        return typeInformation;
    }
}
