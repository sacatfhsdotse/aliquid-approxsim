//         $Id: ReferenceFilter.java,v 1.5 2006/03/22 14:30:50 dah Exp $
/*
 * @(#)Pathfilter.java
 */

package StratmasClient.filter;

import StratmasClient.object.primitive.Reference;

/**
 *  This class filters out StratmasObjects with respect to their
 *  reference.
 *
 * @version 1, $Date: 2006/03/22 14:30:50 $
 * @author  Daniel Ahlin
*/

public class ReferenceFilter extends PathFilter
{
    /**
     * Currently broken
     * 
     * Creates a new ReferenceFilter looking for the provided
     * reference.
     *
     * @param ref the ref to filter after.
     */
    private ReferenceFilter(Reference ref)
    {        
        setTargetIndex(ref.getLength());

        Reference walker = ref;
        for (int i = ref.getLength(); i >= 0; i++) {
            addComponent(new IdentifierFilter(walker.getIdentifier()));
            walker = walker.scope();
        }
    }
}

