// $Id: ServerSession.java,v 1.5 2006/03/22 14:30:50 dah Exp $
/*
 * @(#)ServerSession.java
 */

package StratmasClient.evolver;

import StratmasClient.object.StratmasObject;
import StratmasClient.object.primitive.Reference;
import StratmasClient.communication.Subscription;
import StratmasClient.communication.ServerException;
import StratmasClient.communication.StratmasMessage;

/**
 * Defines the calls the SimulationEvolver exepects to make to a server.
 * 
 * @version 1, $Date: 2006/03/22 14:30:50 $
 * @author Daniel Ahlin
 */
public interface ServerSession {
    /**
     * Registers the provided subscription
     * 
     * @param subscription the subscription to register
     */
    public void registerSubscription(Subscription subscription)
            throws ServerException;

    /**
     * Initializes the session with the provided StratmasObject as root.
     * 
     * @param root the object to use as root
     * @throws ServerException on communication error.
     */
    public void initialize(StratmasObject root) throws ServerException;

    /**
     * Closes the session. Note that it is an error to call any function after the session has been close()'ed.
     * 
     * @throws ServerException on communication error.
     */
    public void close() throws ServerException;

    /**
     * Opens the session. Note that is an error to call open on an already open()'ed session.
     * 
     * @throws ServerException on communication error.
     */
    public void open() throws ServerException;

    /**
     * Tries to take an attached step.
     * 
     * @throws ServerException on communication error.
     */
    public void step() throws ServerException;

    /**
     * Updates the reference with the provided object
     * 
     * @param reference a reference to the position to update.
     * @param object the update to update with.
     * @throws ServerException on communication error.
     */
    public void updateObject(Reference reference, StratmasObject object)
            throws ServerException;

    /**
     * The send method used by this session.
     * 
     * @param message the message to send.
     * @throws ServerException on communication error.
     */
    public void send(StratmasMessage message) throws ServerException;
}
