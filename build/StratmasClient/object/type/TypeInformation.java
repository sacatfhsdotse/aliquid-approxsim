//         $Id: TypeInformation.java,v 1.5 2006/03/31 16:55:51 dah Exp $
/*
 * @(#)TypeInformation.java
 */

package StratmasClient.object.type;

import StratmasClient.LSJarXSDResolver;
import org.apache.xerces.xni.grammars.XMLGrammarLoader;

import java.util.Hashtable;
import java.util.Enumeration;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.apache.xerces.xs.*;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.ls.LSResourceResolver;
import org.apache.xerces.xni.parser.XMLEntityResolver;

/**
 * An object representing a type information for the Taclan
 * language.
 *
 * @version 1, $Date: 2006/03/31 16:55:51 $
 * @author  Daniel Ahlin
*/

public class TypeInformation
{
    /**
     * The XSModel of the type information.
     */
    XSModel model;

    /**
     * Resolved type definitions.
     */
    Hashtable resolvedTypes = new Hashtable();

    /**
     * The 'type' of the docement (i. e. as if the toplevel were in a
     * type description).
     */
    Type documentType = null;

    /**
     * The namespace.
     */
    String namespace = StratmasClient.StratmasConstants.stratmasNamespace;

    /**
     * Creates a new TypeInformation object
     *
     *@param location the location of the type information 
    */
    public TypeInformation(String location)
    {
        try {
            // Set DOM implementation to xerces
            System.setProperty(DOMImplementationRegistry.PROPERTY,
                               "org.apache.xerces.dom.DOMXSImplementationSourceImpl");
            // Choose correct implementation for examining XML Schemas.
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            
            
            XSImplementation impl = (XSImplementation) registry.getDOMImplementation("XS-Loader");
            
            /* Create loader and load location*/
            XSLoader loader = impl.createXSLoader(null);
            
            loader.getConfig().setParameter("http://apache.org/xml/features/validation/schema-full-checking",
                                            Boolean.TRUE);
            loader.getConfig().setParameter("error-handler", new DOMErrorHandler() 
                {
                    /**
                     * This method is called on the error handler when an error occurs.
                     *
                     * @param error the error;
                     */
                    public boolean handleError(DOMError error)
                    {
                        throw new AssertionError(error.getMessage());   
                    }
                });
            DOMConfiguration dc = loader.getConfig();
            Object resolver = dc.getParameter("resource-resolver");
            if (resolver == null) {
                 dc.setParameter("resource-resolver", new LSJarXSDResolver((LSResourceResolver)null));
            }
            else if (resolver instanceof LSResourceResolver) {
                 dc.setParameter("resource-resolver", new LSJarXSDResolver((LSResourceResolver)resolver));
            }
            else if (resolver instanceof XMLEntityResolver) {
                 dc.setParameter("resource-resolver", new LSJarXSDResolver((XMLEntityResolver)resolver));
            }
            
            this.model = loader.load(LSJarXSDResolver.getStreamInput(location));
            //this.model = loader.loadURI(location);
        }
        catch (ClassNotFoundException e) {
            throw new AssertionError(e.getMessage());
        }
        catch (InstantiationException e) {
            throw new AssertionError(e.getMessage());
        }
        catch (IllegalAccessException e) {
            throw new AssertionError(e.getMessage());
        }        
    }

    /**
     * Returns subelements in declaration of element of type name and namespace namespace.
     * 
     * @param name the name of the declaration.
     * @param namespace the namespace of the declaration.
     */
    public Type getType(String name, String namespace)
    {
        // Try looking for the type in resolvedTypes.
        Type type = (Type) resolvedTypes.get(createKey(name, namespace));
        if (type == null) {
            // Try looking for the type in the schema.
            XSTypeDefinition xt = model.getTypeDefinition(name, namespace);
            if (xt != null) {
                // Not there yet, create it.
                type = new TypeDefinition(xt, this);
                resolvedTypes.put(createKey(name, namespace), type);
            }
        }

        return type;
    }

    /**
     * Finds and registers all direct derivations of the specified
     * type.
     *
     * @param type the type for which to build the tree;
     */
    public void findDerived(Type type)
    {
        // Can't do anything for documentType
        if (type instanceof DocumentDefinition) {
            return;
        }

        /* Get all types. */
        XSNamedMap map = this.model.getComponents(XSConstants.TYPE_DEFINITION);        
        /* Get type's XSTypeDefiniton. */
        XSTypeDefinition xsType = this.model.getTypeDefinition(type.getName(), 
                                                               type.getNamespace());

        for (int i = 0; i < map.getLength(); i++) {
            XSTypeDefinition item = (XSTypeDefinition) map.item(i);
            if (item.getBaseType() != null) {
                if (item.getBaseType().equals(xsType)) {
                    type.addDerived(this.getType(item.getName(), item.getNamespace()));
                }
            }
        }
    }

    /**
     * Returns a type definition for the specified type
     *
     * @param type the type to get information for.
     */
    public Type getType(String type)
    {
        return getType(type, this.namespace);
    }

    /** 
     * Creates key of name and namespace for internal hashtable
     *
     * @param name the name.
     * @param namespace the namespace.
     */
    protected String createKey(String name, String namespace)
    {
        return name + ":" + namespace;
    }


    /**
     * Returns a string representation of this object.
     */
    public String toString() 
    {
        StringBuffer buf = new StringBuffer();
        Enumeration rs = resolvedTypes.elements();
        // No newline first time.
        if (rs.hasMoreElements()) {
            buf.append(rs.nextElement().toString());
        }
        while (rs.hasMoreElements()) {
            buf.append("\n" + rs.nextElement().toString());
        }

        return buf.toString();
    }

    /**
     * Returns a Type mirroring the 'type' of the docement (i. e. as if
     * the toplevel were in a type description).
     */
    public Type getDocumentType()
    {
        if (this.documentType == null) {
            createDocumentType();
        }
        
        return this.documentType;
    }

    /**
     * Creates a Type mirroring the 'type' of the docement (i. e. as if
     * the toplevel were in a type description).
     */
    protected void createDocumentType()
    {
        this.documentType = new DocumentDefinition(model, this);
    }

    public static void main(String argv[])
    {
        if (argv.length != 3) {
            System.err.println("Incorrect number of parameters");
            System.exit(1);
        }
        try {
            TypeInformation typeInformation = new TypeInformation(argv[0]);
            typeInformation.getDocumentType();
            System.out.println(typeInformation.getDocumentType());
            //Type type = typeInformation.getType(argv[2], argv[1]);
            //System.out.println(typeInformation);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(1);
        }

        System.exit(0);
    }
}
