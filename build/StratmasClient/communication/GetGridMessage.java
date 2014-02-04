package StratmasClient.communication;


/**
 * Class representing the GetGridMessage. The GetGridMessage is the
 * message that a client sends to a server in order to find out how
 * the server has chosen to partition the grid.
 *
 * @version 1, $Date: 2006/03/22 14:30:49 $
 * @author  Per Alexius
 */
public class GetGridMessage extends StratmasMessage {
    /**
     * Returns a string representation of the type of this message.
     *
     * @return A string representation of the type of this message.
     */
     public String getTypeAsString() {
          return "GetGridMessage";
     }
}
