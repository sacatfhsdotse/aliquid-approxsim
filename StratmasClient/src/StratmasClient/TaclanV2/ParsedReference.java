//         $Id: ParsedReference.java,v 1.6 2006/03/31 16:55:50 dah Exp $
/*
 * @(#)ParsedElement.java
 */

package StratmasClient.TaclanV2;

import java.util.Vector;

/**
 * An object representing a reference to some declared object of the
 * Taclan language.  As such it contains artefacts of the language, it
 * also contains the non lexical type checks.
 *
 * @version 1, 09/28/04
 * @author  Daniel Ahlin
*/

public class ParsedReference extends ParsedPrimitive
{
    /**
     * The identifier that is the first component of the reference.
     */
    ParsedIdentifier head;

    /**
     * The tail of this reference
     */
    ParsedReference tail;

    /**
     * The target of this reference.
     */
    ParsedDeclaration target = null;

    /**
     * Creates a new reference using the provided id.
     * 
     * @param head the scope component of the reference
     */
    public ParsedReference(ParsedIdentifier head) throws SemanticException
    {
        super(head.getPos());
        this.head = head;
        this.tail = null;
    }

    /**
     * Sets the tail of this reference.
     *
     * @param tail the new tail of this reference.
     */
    public void setTail(ParsedReference tail) 
    {
        this.tail = tail;
    }

    /**
     * Sets the target of this reference.
     *
     * @param target the new target of this reference.
     */
    public void setTarget(ParsedDeclaration target)
    {
        this.target = target;
    }

    /**
     * Gets the target of this reference.
     */
    public ParsedDeclaration getTarget()
    {
        return target;
    }
    
    /**
     * Returns the head of this reference, i. e. its first component.
     */
    public ParsedIdentifier getHead()
    {
        return head;
    }

    /**
     * Returns the tail of this reference.
     */
    public ParsedReference getTail()
    {
        return tail;
    }

    /**
     * Binds any reference to its intended target. For a reference
     * that means returning a Vector containing itself, if not
     * previously bound.
     */
    public Vector bindReferences() throws SemanticException
    {
        Vector res = new Vector();
        if (target == null) {
            res.add(this);
        }

        return res;        
    }

    /**
     * Tries to bind this ParsedReference by traversing the given
     * ParsedDeclaration.
     *
     * @param declaration the declaration.
     */
    public void bind(ParsedDeclaration declaration) throws SemanticException
    {
        // Check if there still is a tail to process.
        if (this.getTail() != null) {
            ParsedDeclaration target = declaration.getDeclarations().getDeclaration(this.getTail().getHead());
            if (target != null) {
                this.getTail().bind(declaration);
                this.setTarget(this.getTail().getTarget());
            }
            else {
                throw new UnresolvedReferenceException(this, declaration);
            }
        } else {
            this.setTarget(declaration);
        }
    }

    /**
     * Returns true if this reference has a tail (i. e. if this
     * reference has more than one component.)
     */
    public boolean hasTail()
    {
        return tail != null;
    }

    /**
     * Returns a string representation of the value this object holds.
     */
    public String valueToString()
    {
        if (this.getTail() != null) {
            return getHead().getName() + ":" + getTail().valueToString();
        }
        else {
            return getHead().getName();
        }
    }
}
