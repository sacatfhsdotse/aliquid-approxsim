//         $Id: DefaultServerSession.java,v 1.8 2006/03/22 14:30:50 dah Exp $
/*
 * @(#)ServerSession.java
 */

package StratmasClient.evolver;

import StratmasClient.object.StratmasObject;
import StratmasClient.object.primitive.Reference;

import StratmasClient.dispatcher.StratmasDispatcher;

import StratmasClient.communication.StratmasSocket;
import StratmasClient.communication.ServerException;
import StratmasClient.communication.ServerConnection;
import StratmasClient.communication.StratmasMessage;
import StratmasClient.communication.DisconnectMessage;
import StratmasClient.communication.StepMessage;
import StratmasClient.communication.XMLHandler;
import StratmasClient.communication.SubscriptionHandler;
import StratmasClient.communication.Subscription;
import StratmasClient.communication.InitializationMessage;

/**
 * Implements a default version of ServerSession for convenience.
 *
 * @version 1, $Date: 2006/03/22 14:30:50 $
 * @author  Daniel Ahlin
*/
public class DefaultServerSession implements ServerSession
{
    /**
     * The ServerConnection used by this session.
     */
    ServerConnection connection;

    /**
     * Constructs a DefaultServerConnection given a StratmasSocket.
     *
     * @param socket the socket to the server. It is expected that the
     * socket is "active".
     */
    DefaultServerSession(StratmasSocket socket)
    {
        this.connection = new ServerConnection(socket);
        open();
    }

    /**
     * Registers the provided subscription.
     *
     * @param subscription the subscription to register
     */
    public void registerSubscription(Subscription subscription) throws ServerException
    {
        getConnection().getSubscriptionHandler().blockingRegSubscription(subscription);
    }

    /**
     * Initializes the session with the provided StratmasObject as
     * root. 
     *
     * @param root the object to use as root
     * @throws ServerException on communication error.
     */
    public void initialize(StratmasObject root) throws ServerException
    {
        send(new InitializationMessage(root));
    }

    /**
     * Closes the session. Note that it is an error to call any
     * function after the session has been close()'ed.
     * @throws ServerException on communication error.
     */
    public void close() throws ServerException
    {
        // Take out the handlers from the connection, in case the
        // connection decides to nullify their entries.
        XMLHandler xmlHandler = getConnection().getXMLHandler();
        SubscriptionHandler subscriptionHandler = 
            getConnection().getSubscriptionHandler();

        send(new DisconnectMessage());

        // Kill handlers.
        xmlHandler.kill();
        subscriptionHandler.kill();
        getConnection().kill();

        getConnection().socket().close();
    }

    /**
     * Opens the session. Note that is an error to call open on an
     * already open()'ed session.
     */
    public void open()
    {
        getConnection().start();
        getConnection().getXMLHandler().start();
        new SubscriptionHandler(getConnection()).start();
    }

    /**
     * Returns the connection this session uses for communication with
     * the server.
     */
    ServerConnection getConnection() 
    {
        return this.connection;
    }

    
    /**
     * Tries to take an attached step. 
     *
     * @throws ServerException on communication error.
     */
    public void step() throws ServerException
    {
        send(new StepMessage(1, false));
    }

    /**
     * Updates the reference with the provided object
     *
     * @param reference a reference to the position to update.
     * @param object the update to update with.
     *
     * @throws ServerException on communication error.
     */
    public void updateObject(final Reference reference, final StratmasObject object) 
        throws ServerException
    {
        send(new StratmasMessage()
            {
                /**
                 * Returns a string representation of the type of this message.
                 *
                 * @return A string representation of the type of this message.
                 */
                public String getTypeAsString()
                {
                    return "UpdateServerMessage";
                }
                
                /**
                 * Creates an XML representation of the body of this object.
                 *
                 * @param b The StringBuffer to write to.
                 * @return The StringBuffer b with an XML representation of this
                 * object's body appended to it.
                 */
                public StringBuffer bodyXML(StringBuffer b) 
                {
                    b.append(NL).append("<update xsi:type=\"sp:ServerUpdateModify\">");
                    b.append(NL).append("<reference>");
                    reference.bodyXML(b);
                    b.append(NL).append("</reference>");
                    b.append(NL).append("<newValue xsi:type=\"sp:").append(object.getType().getName());
                    b.append("\" identifier=\"").append(object.getIdentifier()).append("\">");
                    object.bodyXML(b);
                    b.append(NL).append("</newValue>");
                    b.append(NL).append("</update>");
                    return b;
                }
            });        
    }

    /**
     * The send method used by this session.
     * @param message the message to send.
     * 
     * @throws ServerException on communication error.
     */
    public void send(StratmasMessage message) throws ServerException
    {
        getConnection().blockingSend(message);
    }

    /**
     * Returns a string representation of this
     */
    public String toString()
    {
        return getConnection().socket().getHost() + ":" + getConnection().socket().getPort();
    }

    /**
     * Returns a new session using the default dispatcher, or returns
     * null if none availiable.
     */
    public static ServerSession allocateSession()
    {
        StratmasDispatcher dispatcher = StratmasDispatcher.getDefaultDispatcher();
        if (dispatcher != null) { 
            StratmasSocket socket = dispatcher.allocateServer(10);
            if (socket != null) {
                return new DefaultServerSession(socket);
            }
        }

        return null;
    }
}
