package StratmasClient.communication;

import java.util.Vector;
import java.util.Hashtable;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import StratmasClient.Client;
import StratmasClient.Debug;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Class representing a connection to a stratmas server.
 *
 * @version 1, $Date: 2006/05/15 12:23:46 $
 * @author  Per Alexius
 */
public class ServerConnection implements Runnable {
     // For time step slider
//     private TimeSliderDebugFrame tsdf;
     /** The time when the last time step message was sent. **/
     private long timeForLastSentTimestep = System.currentTimeMillis();

     private static final int    sDefaultPrio      = 20;
     private static final int    sStepPrio         = 25;
     private static final int    sSubscriptionPrio = 50;
     private static final int    sInitPrio         = 75;
     private static final int    sTopPrio          = 100000;
    
     private int                 mPort;
     private String              mHost;
     private boolean             mQuitRuthless      = false;
     private boolean             mAlive             = false;
     private XMLHandler          mXMLHandler;
     private StratmasSocket      mSocket            = null;
     private PriorityQueue       mPQ                = new PriorityQueue();
     private Client              mClient;
     private int                 messTreshold       = 2;
     private Hashtable           mPrioHash          = new Hashtable();
    
     /**
      * Indicates whether the ServerConnection is connected or not.
      */
     private boolean connected = false;
     
     public ServerConnection(Client client, XMLHandler xh, String host, int port) {
          this(client, xh, host, port, -1);
     }
    
     public ServerConnection(Client client, XMLHandler xh, String host, int port, long id) {
          mHost       = host;
          mPort       = port;
          mXMLHandler = xh;
          mSocket = new StratmasSocket();
          mSocket.id(id);          
          mClient = client;


          // Set up message priorities
          mPrioHash.put("DisconnectMessage"    , new Integer(sTopPrio         ));
          mPrioHash.put("InitializationMessage", new Integer(sInitPrio        ));
          mPrioHash.put("SetPropertyMessage"   , new Integer(sTopPrio         ));
          mPrioHash.put("StepMessage"          , new Integer(sStepPrio        ));
          mPrioHash.put("SubscriptionMessage"  , new Integer(sSubscriptionPrio));
          mPrioHash.put("UpdateMessage"        , new Integer(sTopPrio         ));
     }

     public ServerConnection(Client client, XMLHandler xh, StratmasSocket socket) {
          mHost       = socket.getHost();
          mPort       = socket.getPort();
          mXMLHandler = xh;
          mSocket = socket;
          mClient = client;

          mAlive = true;
          setIsConnected(true);

          // Set up message priorities
          mPrioHash.put("DisconnectMessage"    , new Integer(sTopPrio         ));
          mPrioHash.put("InitializationMessage", new Integer(sInitPrio        ));
          mPrioHash.put("SetPropertyMessage"   , new Integer(sTopPrio         ));
          mPrioHash.put("StepMessage"          , new Integer(sStepPrio        ));
          mPrioHash.put("SubscriptionMessage"  , new Integer(sSubscriptionPrio));
          mPrioHash.put("UpdateMessage"        , new Integer(sTopPrio         ));
     }

    /**
     * Creates a ServerConnection operating independent of any
     * Client. Using a default XMLHandler.
     */
     public ServerConnection(StratmasSocket socket)
    {
        this(null, new XMLHandler(), socket);
    }

     /** Creates a thread that runs this server connection. */
     public void start() {
          (new Thread(this, getClass().getName())).start();
     }
    
     /**
      * Enqueues a message in the priorityqueue. The message will be
      * sent by the ServerConnection thread when it reaches the front
      * of the PriorityQueue. This method is non-blocking.
      *
      * @param msg The message to be sent.
      */
     public void send(StratmasMessage msg) {
          Integer prio = (Integer)mPrioHash.get(msg.getTypeAsString());
          mPQ.enqueue(msg, prio == null ? sDefaultPrio : prio.intValue());
     }
    
     /**
      * Enqueues a message in the priorityqueue. The message will be
      * sent by the ServerConnection thread when it reaches the front
      * of the PriorityQueue. This method is blocking and does not
      * return until the XMLHandler has signaled that it has handled
      * the contents of the response to the sent message.
      *
      * !!! Notice that a deadlock situation will occur if the thread
      * calling this method owns an object needed to complete the
      * sending, receiving and handling of the message and its
      * response, for example the Client object.
      *
      * @param msg The message to be sent.
      */
    public void blockingSend(StratmasMessage msg) throws ServerException
    {
          Integer prio = (Integer)mPrioHash.get(msg.getTypeAsString());
          class Blocker extends DefaultStratmasMessageListener 
          {
              Object block = new Object();
              ServerException error = null;

              public void messageHandled(StratmasMessageEvent e, Object reply) {
                  Debug.err.println(e.getMessage().getTypeAsString() + " SC handled");
                  synchronized (getBlock()) {
                      getBlock().notifyAll();
                  }
              }
              public void errorOccurred(StratmasMessageEvent e) {
                  Debug.err.println(e.getMessage().getTypeAsString() + " SC error");
                  this.error = new ServerException("Error sending " + 
                                                   e.getMessage().getTypeAsString());
                  synchronized (getBlock()) {
                      getBlock().notifyAll();
                  }
              }

              public Object getBlock()
              {
                  return this.block;
              }
              
              public ServerException getError()
              {
                  return this.error;
              }
          };
              
          Blocker listener = new Blocker();
          synchronized (listener.getBlock()) {
               msg.addEventListener(listener);
               mPQ.enqueue(msg, prio == null ? sDefaultPrio : prio.intValue());
               try {
                    listener.getBlock().wait();
               } catch(InterruptedException e) {
                   throw new ServerException(e.getMessage());
               }
          }

          if (listener.getError() != null) {
              throw listener.getError();
          }

     }
    
     public boolean thresholdReached() {
          return mPQ.size() < messTreshold && mXMLHandler.thresholdReached();
     }
     
     /**
      * Accessor for the mAlive flag.
      *
      * @return True if theconnection to the server is open, false
      * otherwise.
      */
     public boolean isAlive() {
          return mAlive;
     }
     
     /**
      * Closes this connection and eventually terminates the thread
      * running it.
      */
     public void disconnect() {
          mPQ.enqueue(null, sTopPrio);
     }

     /**
      * Closes this connection and terminates the thread running it within timeout milliseconds.
      *
      * @param timeout number of milliseconds to wait before
      * ruthlessly killing the connection.
      */
     public void disconnect(long timeout) {
         Timer timer = new Timer();
         timer.schedule(new TimerTask() 
             {
                 public void run()
                 {
                     if (isAlive()) {
                         quitRuthlessly();
                         socket().close();
                     }
                 }
             }, timeout);
         
         mPQ.enqueue(null, sTopPrio);
     }
    
     /** 
      * Kills this connection and the thread running it. Doesn't care
      * about the consequences. 
      */
     public void kill() {
          mQuitRuthless = true;
          disconnect();
     }

    /**
     * Sets the mQuitRuthless flag
     */
    void quitRuthlessly()
    {
        this.mQuitRuthless = true;
    }
    
    /**
     * Returns true if the serverconnection is connected
     */
    public boolean isConnected()
    {
        return this.connected;
    }

    /**
     * Sets flag indicating whether the serverconnection is connected.
     *
     * @param flag true if the connection should be regarded as
     * connected, false otherwise.
     */
    private void setIsConnected(boolean flag)
    {
        this.connected = flag;
    }

    /**
     * Connects the serverconnection
     */
    private void connect() throws ConnectException, IOException
    {
        if (!isConnected()) {
            mSocket.connect(mHost, mPort);
            SubscriptionCounter.updateNrOfSendedMessages();
            sendRecvHandle(new ConnectMessage());
            SubscriptionCounter.updateNrOfReceivedMessages();

            mAlive = true;
            setIsConnected(true);
        }
    }
     
     /**
      * Private mehod for sending a StratmasMessage, receiving the
      * response and forward it to the XMLHandler. Used internally in
      * order to handle firing of events.
      *
      * @param msg The message to be sent.
      */
     private void sendRecvHandle(StratmasMessage msg) throws IOException {
          try {
               // For time step slider
//                while (msg instanceof StepMessage &&
//                       (System.currentTimeMillis() - timeForLastSentTimestep) < tsdf.getWaitTimeMs()) {
//                     send(msg);
//                     msg = (StratmasMessage)mPQ.blockingDequeue();
//                     if (msg == null) {
//                          mPQ.clear();
//                          disconnect();
//                          return;
//                     }
//                }
               
               mSocket.sendMessage(msg.toXML());

               // For time step slider
//                if (msg instanceof StepMessage) {
//                     timeForLastSentTimestep = System.currentTimeMillis();
//                }

               msg.fireMessageSent();

               String xml = mSocket.recvMessage();
               msg.fireMessageReceived();
               mXMLHandler.handle(xml, msg);

               // For time step slider               
//                if (msg instanceof StepMessage) {
//                     tsdf.registerStepTime(System.currentTimeMillis() - timeForLastSentTimestep);
//                }
          } catch (IOException e) {
               msg.fireErrorOccurred();
               throw e;
          }
     }


     /** 
      * Opens a connection to a stratmas server, sends the connect
      * message and blocks on the queue where messages to be sent will
      * be enqueued. If null is enqueued - send a disconnect message
      * or quit ruthless (depending on wheter disconnect or kill has
      * been called).
      */
     public void run() {
          // For time step slider
//          tsdf = TimeSliderDebugFrame.openTimeSliderDebugFrame();

          StratmasMessage msg;
          try {
               // Init
               connect();
               
               // Running
               while (true) {
                    msg = (StratmasMessage)mPQ.blockingDequeue();
                    if (msg == null) {
                         break;
                    }
                    SubscriptionCounter.updateNrOfMessInSendingQueue1(mPQ.size());
                    SubscriptionCounter.updateNrOfSendedMessages();

                    sendRecvHandle(msg);

                    if (thresholdReached() && mClient != null) {
                         mClient.setNotify();
                    }

                    SubscriptionCounter.updateNrOfReceivedMessages();
               }
               
               if (!mQuitRuthless) {
                    // Cleanup
                    SubscriptionCounter.updateNrOfSendedMessages();
                    sendRecvHandle(new DisconnectMessage());
                    SubscriptionCounter.updateNrOfReceivedMessages();
               }
               mAlive = false;
          } catch (ConnectException e) {
               sendErrorMessage("general",
                                "Connection to " + mHost + ":" + mPort + " rejected",
                                "ConnectResponseMessage");
          } catch (UnknownHostException e) {
               sendErrorMessage("general", "Unknown host: " + mHost, "ConnectResponseMessage");
          } catch (java.net.SocketException e) {
               sendErrorMessage("general", "Connection closed. Is the server really running?", "Unknown");
          } catch (java.io.EOFException e) {
               sendErrorMessage("general",
                                "Connection closed. Is the server running? " + 
                                "Does it allow connections from your host?",
                                "Unknown");
          } catch (IOException e) {
               e.printStackTrace();
               sendErrorMessage("general", "IOException", "Unknown");
          }
          // Close socket if opened.
          if (mSocket != null) {
               mSocket.close();
          }
     }
    
    /**
     * Sends the error message to mClient.
     *
     * @param type type of the message.
     * @param message the error message.
     * @param response name of the subscription response message.
     */
     public void sendErrorMessage(String type, String message, String response) {
          mAlive = false;
          Hashtable ht = new Hashtable();
          Vector v = new Vector();
          v.add(message);
          ht.put(type, v);
          if (mClient != null) {
              mClient.updateStatus(ht, response);
          }
     }

     // Temporary
     public StratmasSocket socket() { return mSocket; }


    /**
     * Returns the xmlhandler of this connection.
     */
    public XMLHandler getXMLHandler()
    {
        return this.mXMLHandler;
    }

    /**
     * Returns the subscriptionhandler of this connection (or null if none).
     */
    public SubscriptionHandler getSubscriptionHandler()
    {
        if (getXMLHandler() != null) {
            return getXMLHandler().getSubscriptionHandler();
        } else {
            return null;
        }
    }
}
