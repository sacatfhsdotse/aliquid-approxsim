package StratmasClient.communication;

import java.lang.StringBuffer;
import StratmasClient.Client;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.StratmasList;


/**
 * Class representing the initialization message. The initialization
 * message is the message that a client sends to a server in order to
 * initialize a simulation.
 *
 * @version 1, $Date: 2007/01/31 12:55:14 $
 * @author  Per Alexius
 */
public class InitializationMessage extends StratmasMessage {
    /** The simulation */
    private StratmasObject mSimulation;
    /** The client **/
    private Client client;
    
     /**
      * Creates an initialization message from the given simulation
      * object.
      * 
      * @param s The simulation object.
      */
     public InitializationMessage(StratmasObject s) {
          mSimulation = s;
          if (mSimulation instanceof StratmasList) {
               throw new AssertionError("Tried to create InitializationMessage with list of simulations.");
          }
     }
    
    /**
     * Creates an initialization message from the given simulation object.
     * 
     * @param s The simulation object.
     */
    public InitializationMessage(StratmasObject s, Client client) {
        mSimulation = s;
        this.client = client;
        if (mSimulation instanceof StratmasList) {
            throw new AssertionError("Tried to create InitializationMessage with list of simulations.");
        }
    }

    /**
     * Returns a string representation of the type of this message.
     *
     * @return A string representation of the type of this message.
     */
     public String getTypeAsString() {
          return "InitializationMessage";
     }

     /**
      * Creates an XML representation of the body of this object.
      *
      * @param b The StringBuffer to write to.
      * @return The StringBuffer b with an XML representation of this
      * object's body appended to it.
      */
     public StringBuffer bodyXML(StringBuffer b) {
          b.append(NL).append("<simulation xsi:type=\"sp:" ).append(mSimulation.getType().getName());
          b.append("\" identifier=\"");
          b.append(StratmasClient.communication.XMLHandler.encodeSpecialCharacters(mSimulation.getIdentifier()));
          b.append("\">");
          mSimulation.bodyXML(b);
          b.append(NL).append("</simulation>");
         // insert the initial values for process variables
          if (client != null) {
              StringBuffer pvBuffer = client.getInitialValuesForProcessVariables();
              if (pvBuffer != null) {
                  b.append(pvBuffer);
              }
          } 
          
          return b;
     }
}
