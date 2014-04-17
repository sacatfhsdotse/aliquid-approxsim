// $Id: ParsedDeclaration.java,v 1.9 2006/03/31 16:55:50 dah Exp $
/*
 * @(#)ParsedDeclaration.java
 */

package StratmasClient.TaclanV2;

import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasObjectFactory;

import StratmasClient.object.type.TypeInformation;
import StratmasClient.object.type.Declaration;
import StratmasClient.object.type.Type;

import java.util.Vector;

/**
 * An object representing a declaration in the Taclan V2 language. As a parsed construct it contains artefacts of the language, e. g.
 * references to the source files
 * 
 * @version 1, $Date: 2006/03/31 16:55:50 $
 * @author Daniel Ahlin
 */

public class ParsedDeclaration extends ParsedObject {
    /**
     * The type of the declaration.
     */
    ParsedIdentifier type;

    /**
     * The identifier of the declaration (may be anonymous).
     */
    ParsedIdentifier identifier;

    /**
     * The subdeclarations of this declaration.
     */
    ParsedDeclarationList declarations;

    /**
     * @param pos where the declaration were made.
     * @param type the type of the declaration.
     * @param identifier the identifier of the declaration (may be anonymous).
     * @param declarations the subdeclarations of the declaration.
     */
    public ParsedDeclaration(SourcePosition pos, ParsedIdentifier type,
            ParsedIdentifier identifier, ParsedDeclarationList declarations)
            throws SemanticException {
        super(pos);
        this.type = type;
        setIdentifier(identifier);
        this.declarations = declarations;
    }

    /**
     * Renames the declaration.
     * 
     * @param identifier the new name.
     */
    public void setIdentifier(ParsedIdentifier identifier) {
        this.identifier = identifier;
    }

    /**
     * Returns the identifier of this declaration.
     */
    public ParsedIdentifier getIdentifier() {
        return identifier;
    }

    /**
     * Returns true if this declaration is anonymous.
     */
    public boolean isAnonymous() {
        return identifier.isAnonymous();
    }

    /**
     * Returns the subdeclarations of this declaration.
     */
    protected ParsedDeclarationList getDeclarations() {
        return declarations;
    }

    /**
     * Returns the ParsedDeclaration identified by reference, or null if not found.
     * 
     * @param reference the wanted declaration.
     */
    public ParsedDeclaration getDeclaration(ParsedReference reference) {
        return declarations.getDeclaration(reference);
    }

    public ParsedIdentifier getType() {
        return type;
    }

    /**
     * Performs type checking on immidiates (i. e. instances) using the supplied TypeInformation
     * 
     * @param definedDeclaration the Declaration this ParsedDeclaration is checked against.
     * @param typeInformation the TypeInformation to use.
     */
    public void typeCheckImmidiates(Declaration definedDeclaration,
            TypeInformation typeInformation) throws SemanticException {
        Type definedType = definedDeclaration.getType();
        Type actualType = typeInformation.getType(getType().getName());
        if (actualType != null) {
            // Check if the declared type is an acceptable substitute for definedType
            if (actualType.canSubstitute(definedType)) {
                // Check subdeclarations
                try {
                    getDeclarations().typeCheckImmidiates(actualType,
                                                          typeInformation);
                } catch (SemanticException e) {
                    // Claim unclaimed errors.
                    e.claimUnclaimed(this);
                    throw e;
                }
            } else {
                throw new TypeErrorException(this, ": " + actualType.getName()
                        + " may not serve as a substitute for "
                        + definedType.getName());
            }
        } else {
            throw new TypeErrorException(this, "Unknown type: '"
                    + getType().getName() + "'");
        }
    }

    /**
     * Binds any reference to its intended target. Returns an array with any outstanding references in this scope.
     */
    public Vector bindReferences() throws SemanticException {
        // Bind references in the subdeclarations.
        return getDeclarations().bindReferences();
    }

    /**
     * Performs type checking on references using the supplied TypeInformation
     * 
     * @param definedType the expected type of this declaration.
     * @param typeInformation the TypeInformation to use.
     */
    public void typeCheckReferences(Type definedType,
            TypeInformation typeInformation) throws SemanticException {
//         Type type = typeInformation.getType(getType());
//         if (type != null) {
//             getDeclarations().typeCheckReferences(type, typeInformation);
//         } else {
//             throw new TypeErrorException(this, "Unknown type: '" + getType.toString() + "'");
//         }
    }

    /**
     * Returns a string representation of this object.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        if (getType() != null) {
            buf.append(getType().getName());
            if (!getIdentifier().isAnonymous()) {
                buf.append(" '" + getIdentifier().getName() + "'");
            }
        } else {
            if (!getIdentifier().isAnonymous()) {
                buf.append("'" + getIdentifier().getName() + "'");
            }
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
     * Returns the StratmasObject equivalent this declaration.
     * 
     * @param declaration the declaration to use.
     */
    public StratmasObject getStratmasObject(Declaration declaration)
            throws SemanticException {
        // The actual Type of this type:
        Type actualType = declaration.getType().getTypeInformation()
                .getType(this.getType().getName());
        // Create the declaration that actually reflects this type:
        Declaration nDecl = declaration.clone(actualType);
        try {
            return StratmasObjectFactory.vectorCreate(nDecl)
                    .getStratmasObject(this.getDeclarations()
                                               .getStratmasObjects(actualType));
        } catch (MissingDeclarationException e) {
            e.setScopeIfMissing(this);
            throw e;
        }
    }
}
