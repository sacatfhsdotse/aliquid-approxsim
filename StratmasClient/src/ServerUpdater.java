package StratmasClient;


import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import StratmasClient.communication.ServerConnection;
import StratmasClient.communication.TSQueue;
import StratmasClient.communication.UpdateMessage;
import StratmasClient.communication.Update;
import StratmasClient.object.type.Declaration;
import StratmasClient.object.type.Type;
import StratmasClient.object.StratmasEvent;
import StratmasClient.object.StratmasEventListener;
import StratmasClient.object.StratmasObject;
import StratmasClient.object.Composite;
import StratmasClient.object.Polygon;
import StratmasClient.object.StratmasList;
import StratmasClient.object.type.TypeFactory;



/**
 * A class that handles the transfer of updates from the Client to the
 * server (during a simulation). An update should be performed
 * whenever a StratmasObject is modified during a simulation by
 * anything except the server, in order to synchronize the server's
 * and the client's views of the simulation. The ServerUpdater listens
 * to all ValueType descendants in the provided tree of
 * StratmasObjects and performs updates whenever an update triggering
 * event occurs.
 *
 * @version 1, $Date: 2006/10/03 14:56:41 $
 * @author  Per Alexius
 */
public class ServerUpdater implements StratmasEventListener {
     /** The UpdateHandler to use for sending updates. */
     private UpdateHandler mUH;

     /** Contains all objects that we listen to. */
     private Vector mListenedObjects = new Vector();

     /**
      * Adds this object as listener to the provided object. Calls
      * itself recursively on the provided object's children.
      *
      * @param obj The provided object.
      */
     private void listenTo(StratmasObject obj) {
          obj.addEventListener(this);
          mListenedObjects.add(obj);
          if (!obj.getType().canSubstitute("ValueType") || obj instanceof StratmasList) {
               for (Enumeration en = obj.children(); en.hasMoreElements(); ) {
                    listenTo((StratmasObject)en.nextElement());
               }
          }
     }

     /**
      * Stops listening to the provided object and all its children.
      *
      * @param obj The provided object.
      */
     private void stopListenTo(StratmasObject obj) {
          obj.removeEventListener(this);
          mListenedObjects.remove(obj);
          for (Enumeration en = obj.children(); en.hasMoreElements(); ) {
               stopListenTo((StratmasObject)en.nextElement());
          }
     }

     /**
      * Constructs a ServerUpdater for the StratmasObjects in the tree
      * rooted at the provided StratamsObject.
      *
      * @param sc The ServerConnection to use for sending updates.
      * @param root The root of the tree of StratmasObjects to handle
      * updates for.
      * @param controller The Controller object. Only needed in order
      * to be able to disconnect if an update failed. This should be
      * handled differently when the Controller class is rewritten.
      */
    public ServerUpdater(ServerConnection sc, StratmasObject root, Controller controller) {
        mUH = new UpdateHandler(sc, controller);
          listenTo(root);
     }

     /**
      * Forces this ServerUpdater to no longer listen to any
      * elements. Must be called when disconnecting from the server in
      * order to avoid sending updates to a server connection that
      * isn't active.
      */
     public void inactivate() {
          for (Iterator it = mListenedObjects.iterator(); it.hasNext(); ) {
               ((StratmasObject)it.next()).removeEventListener(this);
          }
          mListenedObjects.removeAllElements();
          mUH.kill();
     }

     /**
      * Sends an update message of specified type for specified object.
      *
      * @param o The object added, removed or updated or the object to
      * replace an old object with..
      * @param type The type of update (add, remove, replace or update).
      */
     private void sendUpdate(StratmasObject o, String type) {
          mUH.enqueueUpdate(o, type);
     }

     /**
      * Performs a server update if the event is a ValueChanged or
      * ChildChanged event and if the event isn't initiated by the
      * server, i.e. by a subscribed data update.
      *
      * @param e The event that occured.
      */
     public void eventOccured(StratmasEvent e) {
          StratmasObject obj = (StratmasObject)e.getSource();
          boolean serverUpdate = e.getInitiator() instanceof org.w3c.dom.Element;
          //          Debug.err.println("event " + e + ", " + ((StratmasObject)e.getSource()).getIdentifier());
          // We should update the server if the initiator isn't a dom
          // element (e.g. the server) and the event is a valueChanged
          // event or a childChanged event sent from a non-list and if
          // the source is a ValueType descendant.
          if (!serverUpdate &&
              (e.isValueChanged() || 
               e.isChildChanged() && !(obj instanceof StratmasList)) &&
              obj.getType().canSubstitute(TypeFactory.getType("ValueType"))) {
               // Wait until we have closed polygons before we send any updates.
               if ((obj instanceof Polygon && !((Polygon)obj).isClosed()) ||
                   (obj instanceof Composite && ((Composite)obj).hasUnclosed())) {
                    return;
               }
               sendUpdate(obj, UpdateMessage.MODIFY);
          }
          else if (e.isObjectAdded()) {
               StratmasObject added = (StratmasObject)e.getArgument();
               listenTo(added);
               if (!serverUpdate) {
                    sendUpdate(added, UpdateMessage.ADD);
               }
          }
          else if (e.isReplaced()) {
               stopListenTo((StratmasObject)e.getSource());
               StratmasObject newObj = (StratmasObject)e.getArgument();
               listenTo(newObj);
               if (!serverUpdate && newObj.getType().canSubstitute(TypeFactory.getType("ValueType"))) {
                    sendUpdate(newObj, UpdateMessage.REPLACE);
               }
          }
          else if (e.isRemoved()) {
               StratmasObject removed = (StratmasObject)e.getSource();
               stopListenTo(removed);
               if (!serverUpdate) {
                    // Don't have to send updates for removed required elements.
                    if (removed.getParent() != null) {
                         Declaration dec = removed.getParent().getType().getSubElement(removed.getIdentifier().toString());
                         if (dec != null && dec.isSingular()) {
                              return;
                         }
                    }
                    sendUpdate(removed, UpdateMessage.REMOVE);
               }
          }
     }
}



/**
 * This class handles chunking of updates so that each update won't
 * get an own message.
 *
 * @version 1, $Date: 2006/10/03 14:56:41 $
 * @author  Per Alexius
 */
class UpdateHandler implements Runnable {
     /** 
      * The time to wait after receiving a update before
      * sending an updatemessage.
      */
     private static final long sDelayms = 100;

     /** Set to true if we should quit. */
     private boolean mQuit = false;

     /** The queue in which incomming updates are stored. */
     private TSQueue mQueue = new TSQueue();  

     /** The ServerConnection to use. */
     private ServerConnection mSC = null;

    /** 
     * The Controller object. Only needed in order to be able to
     * disconnect if an update failed. This should be handled
     * differently when the Controller class is rewritten.
     */
    private Controller mController = null;

     /**
      * Creates a new UpdateHandler and connects it to the
      * provided ServerConnection.
      * 
      * @param serverConnection the connection to use for this updateHandler.
      * @param controller The Controller object. Only needed in order
      * to be able to disconnect if an update failed. This should be
      * handled differently when the Controller class is rewritten.
      */
    public UpdateHandler(ServerConnection serverConnection, Controller controller) {
        mController = controller;
          mSC = serverConnection;
          (new Thread(this)).start();
     }

     /**
      * Sends an update message of specified type for specified object.
      *
      * @param o The object added, removed or updated or the object to
      * replace an old object with..
      * @param type The type of update (add, remove, replace or update).
      */
     public void enqueueUpdate(StratmasObject o, String type) {
          mQueue.enqueue(new Update(o, type));
     }

     /**
      * Terminates the Thread running this UpdateHandler
      */
     public void kill() {
          mQuit = true;
          mQueue.enqueue(null);
     }

     /**
      * The main loop.
      */
     public void run() {
          while (true) {
               UpdateMessage msg = new UpdateMessage();
               Update u = (Update)mQueue.blockingDequeue();
               if (mQuit) {
                    break;
               }
               msg.addUpdate(u);
               long firstDequeueTime = System.currentTimeMillis();
               do {
                    u = (Update)mQueue.dequeue();
                    if (mQuit) {
                         break;
                    }
                    else if (u != null) {
                         msg.addUpdate(u);
                    }
               } while (System.currentTimeMillis() - firstDequeueTime < sDelayms);
               // Send
               if (mSC != null) {
                   // Disconnect if an update failed in order to avoid
                   // locking the client in "continous_step"
                   // mode. Should be handled differently when the
                   // Controller class is rewritten.
                   msg.addEventListener(new StratmasClient.communication.DefaultStratmasMessageListener() {
                           public void errorOccurred(StratmasClient.communication.StratmasMessageEvent e) {
                               mController.updateSimulationMode("disconnect");
                           }
                       });
                    mSC.send(msg);
               }
               else {
                    try {
                         Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
               }
          }
     }
}

          

