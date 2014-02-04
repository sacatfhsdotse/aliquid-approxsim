package StratmasClient.communication;


import java.io.*;
import java.net.*;
import StratmasClient.Debug;

/**
 * Wrapper around Socket class that may send and receive stratmas
 * messages. A stratmas message has the following structure.
 * <p>
 * <t> |---length---|---id----|---xml content---|
 * <p>
 * where 'length' is a 64 bit network byte order integer representing
 * the length of the xml contents , 'id' is a 64 bit network byte
 * order integer representing the id of the session (or -1 if the
 * socket is not yet connected) and 'xml content' is 'length' bytes of
 * data representing the xml contents of the message.
 * @version 1, $Date: 2006/09/11 09:33:42 $
 * @author  Per Alexius
 */
public class StratmasSocket {
     /** The id of the session. */
     private long mId;
     /** The socket. */
     private Socket mSocket;
     /** Stream for writing to the socket. */
     private DataOutputStream mOut;
     /** Stream for reading from the socket. */
     private DataInputStream mIn;

     /** Default constructor. */
     public StratmasSocket() {
          mId = -1;
     }

    /**
     * Returns the port of this socket (if connected), else 0.
     */
    public int getPort()
    {
        if (mSocket != null && mSocket.getRemoteSocketAddress() != null &&
            mSocket.getRemoteSocketAddress() instanceof InetSocketAddress) {
            return ((InetSocketAddress) mSocket.getRemoteSocketAddress()).getPort(); 
        } else {
            return 0;
        }
    }

    /**
     * Returns the host of this socket (if connected), else null.
     */
    public String getHost()
    {
        if (mSocket != null && mSocket.getRemoteSocketAddress() != null &&
            mSocket.getRemoteSocketAddress() instanceof InetSocketAddress) {
            return ((InetSocketAddress) mSocket.getRemoteSocketAddress()).getHostName(); 
        } else {
            return null;
        }
    }

     /**
      * Accessor for the id.
      *
      * @return The id of this session.
      */
     public long id() { return mId; }

     /**
      * Sets the id of this session if one want to connect to a
      * simulation that is running in detached mode.
      *
      * @param id of this session.
      */
     public void id(long id) { mId = id; }

     /** Closes the socket. */
     public void close() {
          try {
               if (mOut != null) {
                    mOut.close();
               }
               if (mIn != null) {
                    mIn.close();
               }
               if (mSocket != null) {
                    mSocket.close();
               }
          } catch (IOException e) {
               System.err.println("Error when closing socket and socket streams");
               System.exit(1);
          }
     }


     /**
      * Opens a connection to a stratmas server.
      * 
      * @param host The name of the host to connect to.
      * @param port The port to connect to.
      */
     public void connect(String host, int port) throws IOException {
          mSocket = new Socket(host, port);
          mOut = new DataOutputStream(new BufferedOutputStream(mSocket.getOutputStream()));
          mIn = new DataInputStream(new BufferedInputStream(mSocket.getInputStream()));
     }
     
     /**
      * Sends a stratmas message.
      * 
      * @param msg The message to be sent (xml).
      */
     public void sendMessage(String msg) throws IOException {
          byte [] flk = msg.getBytes("ISO-8859-1");

          // 64 bit
          long length = msg.length();
          mOut.writeLong(flk.length);
          mOut.writeLong(mId);
          mOut.write(flk, 0, flk.length);
          mOut.flush();
     }

     /**
      * Receives a stratmas message. Blocks until a message is
      * received.
      * 
      * @return The message received (xml).
      */
     public String recvMessage() throws IOException {
          long length = mIn.readLong();
          mId = mIn.readLong();
          byte [] tmp = new byte[new Long(length).intValue()];
          mIn.readFully(tmp);
          return new String(tmp, "ISO-8859-1");
     }
}
