//         $Id: ParsedDeclarationList.java,v 1.9 2006/03/31 16:55:50 dah Exp $
/*
 * @(#)ParsedDeclarationList.java
 */

package StratmasClient.TaclanV2;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import StratmasClient.object.StratmasObjectFactory;
import StratmasClient.object.type.TypeInformation;
import StratmasClient.object.type.Declaration;
import StratmasClient.object.type.Type;

/**
 * An object representing a sequence of declarations in the Taclan V2
 * language.
 *
 * @version 1, $Date: 2006/03/31 16:55:50 $
 * @author  Daniel Ahlin
*/

public class ParsedDeclarationList
{
    /**
     * The declarations in the list.
     */
    Vector parts = new Vector();
    
    /**
     * A hashtable mapping identifiers to declarations.
     */
    Hashtable partsTable = new Hashtable();

    /**
     * Creates a new empty ParsedDeclarationList.
     */
    public ParsedDeclarationList()
    {}

    /**
     * Adds a parsed declaration to the top of the list.
     *
     * @param part the ParsedDeclaration to add.
     */
    public void push(ParsedDeclaration part) throws IdConflictException
    {
        if (!part.isAnonymous()) {
            ParsedDeclaration firstDeclaration = 
                getDeclaration(part.getIdentifier());
            if (firstDeclaration != null) {
                throw new IdConflictException(firstDeclaration.getIdentifier(), 
                                                part.getIdentifier());
            } else {
                this.parts.add(0, part);
                partsTable.put(part.getIdentifier(), part);
            }
        } else {
            this.parts.add(0, part);
        }
    }

    /**
     * Adds a parsed declaration to the end of the list.
     *
     * @param part the ParsedDeclaration to add.
     */
    public void add(ParsedDeclaration part) throws IdConflictException
    {
        if (!part.isAnonymous()) {
            ParsedDeclaration firstDeclaration = 
                getDeclaration(part.getIdentifier());
            if (firstDeclaration != null) {
                throw new IdConflictException(firstDeclaration.getIdentifier(), 
                                                part.getIdentifier());
            } else {
                this.parts.add(part);
                partsTable.put(part.getIdentifier(), part);
            }
        } else {
            this.parts.add(part);
        }
    }

    /**
     * Adds a parsed DeclarationList to the top of the list.
     *
     * @param parts the ParsedDeclarationList to add.
     */
    public void push(ParsedDeclarationList parts) throws CollectedErrorsException
    {
        // FIXME dont add to vector unless added to partsTable!!!
        CollectedErrorsException errors = null;
        for (Enumeration ps = parts.parts.elements(); ps.hasMoreElements();) {
            ParsedDeclaration decl = (ParsedDeclaration) ps.nextElement();
            try {
                push(decl);
            }
            catch (IdConflictException e) {
                if (errors == null) {
                    errors = new CollectedErrorsException();
                }
                errors.add(e);
            }
        }
        if (errors != null) {
            throw errors;
        }
    }

    /**
     * Adds a parsed DeclarationList to the end of the list.
     *
     * @param parts the ParsedDeclarationList to add.
     */
    public void add(ParsedDeclarationList parts) throws CollectedErrorsException
    {
        // FIXME dont add to vector unless added to partsTable!!!
        CollectedErrorsException errors = null;
        for (Enumeration ps = parts.parts.elements(); ps.hasMoreElements();) {
            ParsedDeclaration decl = (ParsedDeclaration) ps.nextElement();
            try {
                add(decl);
            }
            catch (IdConflictException e) {
                if (errors == null) {
                    errors = new CollectedErrorsException();
                }
                errors.add(e);
            }
        }
        if (errors != null) {
            throw errors;
        }
    }

    /**
     * Returns the parts this DeclarationList consist of.
     */    
    protected Vector getParts()
    {
        return parts;
    }

    /**
     * Returns the ParsedDeclaration identified by reference, or null
     * if not found.
     *
     * @param reference the wanted reference.
     */
    public ParsedDeclaration getDeclaration(ParsedReference reference)
    {
        ParsedDeclaration declaration = 
            getDeclaration(reference.getIdentifier());
        if (declaration == null || 
            !reference.hasTail()) {
            return declaration;
        } else {
            return declaration.getDeclaration(reference.getTail());
        }
    }

    /**
     * Returns the toplevel ParsedDeclaration identified by
     * identifier, or null if not found.
     *
     * @param identifier the wanted identifier.
     */
    public ParsedDeclaration getDeclaration(ParsedIdentifier identifier)
    {
        return (ParsedDeclaration) partsTable.get(identifier);
    }

    /**
     * Returns the toplevel ParsedDeclaration identified by
     * identifier, or null if not found.
     *
     * @param identifier the wanted identifier.
     */
    public ParsedDeclaration getDeclaration(String identifier)
    {
        return (ParsedDeclaration) partsTable.get(identifier);
    }

    /**
     * Returns an empty declaration list.
     */
    public static ParsedDeclarationList getEmpty()
    {
        return new ParsedDeclarationList();
    }

    /**
     * Performs type checking on immidiates (i. e. instances) using the supplied Type and TypeInformation
     * also reorders the assignments in the proper order.
     *
     * @param type the Type of the enclosing declaration.
     * @param typeInformation the TypeInformation to use.
     */
    public void typeCheckImmidiates(Type type, TypeInformation typeInformation) throws SemanticException
    {
        Vector errors = new Vector();
        Vector reorderedParts = new Vector();
        // For all declarations in type...
         for (Enumeration ps = type.getSubElements().elements(); 
             ps.hasMoreElements();) {
            Declaration definedDeclaration = (Declaration) ps.nextElement();
            //System.err.println(definedDeclaration.getType().getName());
            // Try to get ParsedDeclaration matching the tag of the Declaration.
            ParsedDeclaration parsedDeclaration = this.getDeclaration(definedDeclaration.getName());
            // Any ParsedDeclaration found?
            if (parsedDeclaration == null) {
                // Check if this subdeclaration is optional
                if (definedDeclaration.getMinOccurs() != 0) {
                    // Not present and defaults not yet supported.
                    errors.add(new MissingDeclarationException(definedDeclaration, 
                                                          "(No defaults availiable.)"));
                }
            }
            else {
                try {
                    parsedDeclaration.typeCheckImmidiates(definedDeclaration, 
                                                          typeInformation);
                    reorderedParts.add(parsedDeclaration);
                } catch (TypeErrorException e) {
                    errors.add(e);
                }
            }
        }

        // Check if there are ParsedDeclarations in the list not processed.
        if (this.parts.size() > reorderedParts.size()) {
            
        }

        if (!errors.isEmpty()) {
            throw new CollectedErrorsException(errors);
        }
    }

    /**
     * Performs type checking on references using the supplied TypeInformation 
     *
     * @param type the Type of the enclosing declaration.
     * @param typeInformation the TypeInformation to use.
     */
    public void typeCheckReferences(Type type, TypeInformation typeInformation) throws SemanticException
    {        
//         Vector errors = new Vector();
//         for (Enumeration ps = this.getParts().elements(); ps.hasMoreElements();) {
//             try {
//                 ((TypeCheckable) ps.nextElement()).typeCheckReferences(subtype, typeInformation);
//             } catch (TypeErrorException e) {
//                 errors.add(e);
//             }
//         }

    }

    /**
     * Binds any reference to its intended target. Returns an array
     * with any outstanding references in this scope.
     */
    public Vector bindReferences() throws SemanticException
    {
        Vector outstanding = new Vector();
        Vector errors = new Vector();
        for (Enumeration ps = this.getParts().elements(); 
             ps.hasMoreElements();) {
            ParsedDeclaration parsedDeclaration = (ParsedDeclaration) ps.nextElement();
            Vector unresolvedCandidates = parsedDeclaration.bindReferences();
            for (Enumeration us = unresolvedCandidates.elements();
                 us.hasMoreElements();) {
                ParsedReference ref = (ParsedReference) us.nextElement();
                ParsedDeclaration target = this.getDeclaration(ref.getIdentifier());                
                if (target != null) {
                    // Found a binding for the first component of the
                    // reference. Make the reference descend that
                    // declaration.
                    try {
                        ref.bind(target);
                    }
                    catch (UnresolvedReferenceException e) {
                        errors.add(e);
                    }
                }
                else {
                    // Add this to the outstanding references in this
                    // scope.
                    outstanding.add(ref);
                }
            }
            
        }
        
        if (!errors.isEmpty()) {
            throw new CollectedErrorsException();
        }

        return outstanding;
    }

    /**
     * Returns the number of declarations in this list.
     */
    public int getSize()
    {
        return parts.size();
    }

    /**
     * Returns a string representation of this object.
     */
    public String toString()
    {
        if (getSize() > 0) {
            StringBuffer buf = new StringBuffer();
            for (Enumeration ps = this.getParts().elements(); 
                 ps.hasMoreElements();) {
                ParsedDeclaration decl = (ParsedDeclaration) ps.nextElement();
                buf.append("\n" + decl.toString());
            }
            return buf.toString();
        }
        else {
            return "";
        }
    }


    /**
     * Returns a vector containing the StratmasObject equivalent of
     * the members of this list.
     *
     * @param type the type of the objects in this list.
     */
    public Vector getStratmasObjects(Type type) throws SemanticException
    {
        Vector result = new Vector();
        for (Enumeration ds = type.getSubElements().elements(); 
             ds.hasMoreElements();) {
            Declaration d = (Declaration) ds.nextElement();
            ParsedDeclaration ps = this.getDeclaration(d.getName());
            if (ps == null) {
                if (d.getMinOccurs() != 0) {
                    throw new MissingDeclarationException(d, "");
                } else if (d.isUnbounded() ||
                           d.getMaxOccurs() > 1) {
                    // Here there may be a list, create it, even though it is empty.                    
                    result.add(StratmasObjectFactory.createList(d));
                }
            }
            else {
                result.add(ps.getStratmasObject(d));
            }
        }

        return result;
    }
}
