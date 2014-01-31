package StratmasClient.filter;

import StratmasClient.object.StratmasObject;
import StratmasClient.object.type.Type;

/**
 * ChildWithNameAndTypefilter filters out StratmasObjects that have a
 * child with a specified name that is of a specified type.
 *
 * @version 1, $Date: 2006/03/22 14:30:50 $
 * @author  Per Alexius
*/

public class ChildWithNameAndTypeFilter extends StratmasObjectFilter {
     private String mName;
     private Type   mType;
     
     /**
      * Creates a new ChildWithNameAndTypeFilter allowing the
      * specified child name and type.
      *
      * @param name The child name to filter for.
      * @param type The child type to filter for.
      */
     public ChildWithNameAndTypeFilter(String name, Type type) {	
	  super();
	  mName = name;
	  mType = type;
     }
     
     /**
      * Returns true if the provided StratmasObject passes the filter.
      *
      * @param sObj the object to test
      */
     public boolean pass(StratmasObject sObj) {
	  StratmasObject child = sObj.getChild(mName);
	  if (child == null) {
	       return false;
	  }
	  else {
	       return (mType == null || child.getType().canSubstitute(mType));
	  }
     }
}
