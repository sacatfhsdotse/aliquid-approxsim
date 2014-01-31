package StratmasClient.communication;

import StratmasClient.object.StratmasObject;
import java.lang.StringBuffer;

/**
 * Class for grouping an update with its type.
 *
 * @version 1, $Date: 2006/05/15 12:24:32 $
 * @author  Per Alexius
 */
public class Update {
     /** The buffer containing the XML representation of this update. */
     private StringBuffer xml = new StringBuffer();

     /** 
      * Constructor.
      *
      * @param o The object added, removed or modified or the object to
      * replace an old object with..
      * @param type The type of update (add, remove, replace or modify).
      */
     public Update(StratmasObject o, String type) {
	  xml.append(XMLHelper.NL).append("<update xsi:type=\"sp:").append(type).append("\">");
	  xml.append(XMLHelper.NL).append("<reference>");
	  o.getReference().bodyXML(xml);
	  xml.append(XMLHelper.NL).append("</reference>");
	  if (type.equals(UpdateMessage.ADD)) {
	       xml.append(XMLHelper.NL).append("<identifiable xsi:type=\"sp:").append(o.getType().getName());
	       xml.append("\" identifier=\"").append(o.getIdentifier()).append("\">");
	       o.bodyXML(xml);
	       xml.append(XMLHelper.NL).append("</identifiable>");
	  }
	  else if (type.equals(UpdateMessage.REPLACE)) {
	       xml.append(XMLHelper.NL).append("<newObject xsi:type=\"sp:").append(o.getType().getName());
	       xml.append("\" identifier=\"").append(o.getIdentifier()).append("\">");
	       o.bodyXML(xml);
	       xml.append(XMLHelper.NL).append("</newObject>");
	  }
	  else if (type.equals(UpdateMessage.MODIFY)) {
	       xml.append(XMLHelper.NL).append("<newValue xsi:type=\"sp:").append(o.getType().getName());
	       xml.append("\" identifier=\"").append(o.getIdentifier()).append("\">");
	       o.bodyXML(xml);
	       xml.append(XMLHelper.NL).append("</newValue>");
	  }
	  xml.append(XMLHelper.NL).append("</update>");
     }

    /**
     * Creates an XML representation of this object.
     *
     * @return An XML representation of this object.
     */
    public String toXML() {
	 return xml.toString();
    }
}
