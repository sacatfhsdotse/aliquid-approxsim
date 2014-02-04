package StratmasClient.communication;


/**
 * This class represents the events that may be sent by a
 * StratmasMessage to indicate the progress of the message. Events are
 * currently sent when the message is sent to the server, when the
 * client has received the answer and when the answer is handled or
 * when something has gone wrong.
 *
 * @version 1, $Date: 2006/03/22 14:30:49 $
 * @author  Per Alexius
 */
public class StratmasMessageEvent extends java.util.EventObject {

     /**
      * Constructor
      *
      * @param source The source of the event, i.e. the
      * StratmasMessage that generated the event.
      */
     public StratmasMessageEvent(StratmasMessage source) {
          super(source);
     }

     /**
      * Gets the message that generated this event.
      *
      * @return The message that generated this event.
      */
     public StratmasMessage getMessage() {
          return (StratmasMessage)getSource();
     }
}
