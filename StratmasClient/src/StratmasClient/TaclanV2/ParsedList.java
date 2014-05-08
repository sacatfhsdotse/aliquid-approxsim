// $Id: ParsedList.java,v 1.11 2006/03/31 16:55:50 dah Exp $
/*
 * @(#)ParsedList.java
 */

package StratmasClient.TaclanV2;

import java.util.Vector;
import java.util.Enumeration;

import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasObjectFactory;
import StratmasClient.object.type.TypeInformation;
import StratmasClient.object.type.Declaration;

/**
 * An object representing a declaration of list of declarations in the Taclan V2 language The members of the list are required to have some
 * common superclass availiable in a provided xsd. As a parsed construct it contains artefacts of the language, e. g. references to the
 * source files
 * 
 * @version 1, $Date: 2006/03/31 16:55:50 $
 * @author Daniel Ahlin
 */

public class ParsedList extends ParsedDeclaration {
    /**
     * @param pos where the declaration were made.
     * @param name the name of the list.
     * @param declarations the declarations made in the list.
     */
    public ParsedList(SourcePosition pos, ParsedIdentifier name,
            ParsedDeclarationList declarations) throws SemanticException {
        super(pos, null, name, declarations);
    }

    /**
     * @param pos where the declaration were made.
     * @param declarations the declarations made in the list.
     */
    public ParsedList(SourcePosition pos, ParsedDeclarationList declarations)
            throws SemanticException {
        this(pos, ParsedIdentifier.getAnonymous(), declarations);
    }

    /**
     * Performs type checking on immidiates (i. e. instances) using the supplied TypeInformation. For a List this means that every member of
     * the list shall pass an ImmidiateTypeCheck against the type.
     * 
     * @param definedDeclaration the Declaration this ParsedDeclaration is checked against.
     * @param typeInformation the TypeInformation to use.
     */
    public void typeCheckImmidiates(Declaration definedDeclaration,
            TypeInformation typeInformation) throws SemanticException {
        Vector errors = new Vector();

        // Create singular clone of declaration to check against.
        Declaration singularDeclaration = (Declaration) definedDeclaration
                .clone();
        singularDeclaration.setMinOccurs(1);
        singularDeclaration.setMaxOccurs(1);
        singularDeclaration.setUnbounded(false);

        // Check subdeclarations
        for (Enumeration ps = getDeclarations().getParts().elements(); ps
                .hasMoreElements();) {
            ParsedDeclaration p = (ParsedDeclaration) ps.nextElement();
            try {
                p.typeCheckImmidiates(singularDeclaration, typeInformation);
            } catch (SemanticException e) {
                errors.add(e);
            }
        }

        // Check multiplicity constraints:
        int size = getDeclarations().getSize();
        if (definedDeclaration.getMinOccurs() > size
                || (!definedDeclaration.isUnbounded() && definedDeclaration
                        .getMaxOccurs() < size)) {
            errors.add(new TypeErrorException(this, " contains an "
                    + "incorrect number of declarations."));
        }
        if (!errors.isEmpty()) {
            throw new CollectedErrorsException(errors);
        }
    }

    /**
     * Returns a string representation of this object.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();

        if (!getIdentifier().isAnonymous()) {
            buf.append("'" + getIdentifier().getName() + "' =");
        }

        buf.append(" {");

        String declStr = this.getDeclarations().toString();
        if (declStr.length() != 0) {
            buf.append(declStr.replaceAll("\n", "\n  ") + "\n}");
        } else {
            buf.append("}");
        }

        return buf.toString();
    }

    /**
     * Returns the StratmasObject equivalent to this declaration.
     * 
     * @param declaration the declaration to use.
     */
    public StratmasObject getStratmasObject(Declaration declaration)
            throws SemanticException {
        Vector result = new Vector();
        for (Enumeration ps = getDeclarations().getParts().elements(); ps
                .hasMoreElements();) {
            ParsedDeclaration p = (ParsedDeclaration) ps.nextElement();
            StratmasObject sObj = p.getStratmasObject(declaration);
            sObj.setIdentifier(p.getIdentifier().getIdentifier());
            result.add(sObj);
        }

        return StratmasObjectFactory.createList(declaration, result);
    }

//     /**
//      * Returns the StratmasObject equivalent to this declaration.
//      *
//      * @param declaration the declaration to use.
//      */
//     public Vector getStratmasObjects(Declaration declaration) throws SemanticException
//     {
//         Vector result = new Vector();
//         for (Enumeration ps = getDeclarations().getParts().elements(); 
//              ps.hasMoreElements();) {
//             ParsedDeclaration p = (ParsedDeclaration) ps.nextElement();
//             StratmasObject pres = p.getStratmasObject(declaration); 
//             pres.setIdentifier(p.getIdentifier().getIdentifier());
//             result.add(pres);
//         }

//         return result;
//     }
}
