package StratmasClient.communication;

import java.util.Timer;
import java.util.TimerTask;
import StratmasClient.ClientMainFrame;

/**
 * This class is used to monitor the intensity of the communication between the client and
 * the server. If this comunication is intensive, the progess bar in  ClientMainFrame.java
 * is started.
 *
 * @author Amir Filipovic
 */
public class SubscriptionCounter {
    /**
     * Timer used for delaying the activating of the progress bar in ClientMainFrame.
     */
    private static Timer timer;
     /**
     * Number of messages in the first sending queue.
     */
    private static int messNrInSendingQueue1 = 0;
    /**
     * Number of messages in the second sending queue.
     */
    private static int messNrInSendingQueue2 = 0;
    /**
     * Update the number of messages in the first sending queue.
     */
    public static void updateNrOfMessInSendingQueue1(int messNr) {
        messNrInSendingQueue1 = messNr;
    }
    
    /**
     * Update the number of messages in the second sending queue.
     */
    public static void updateNrOfMessInSendingQueue2(int messNr) {
        messNrInSendingQueue2 = messNr;
    }
    
    /**
     * Update the number of messages in the receiving queue.
     */
    public static void updateNrOfMessInReceivingQueue(int messNr) {
    }
    
    /**
     * Update the total number of the sended messages.
     */
    public static void updateNrOfSendedMessages() {
        startTimer(0.5);
    }
    
    /**
     * Update the total number of the received messages.
     */
    public static void updateNrOfReceivedMessages() {
        endTimer();
    }
        
    /**
     * Starts the timer and the progress bar in ClientMainFrame.
     *
     * @param secs number of seconds before the timer starts.
     */
    public static void startTimer(double secs) {
        timer = new Timer();
        int totalNrOfMessages = messNrInSendingQueue1+messNrInSendingQueue2;
        if (totalNrOfMessages <= 1) {
            timer.schedule(new TimerTask() {
                    public void run() {
                        ClientMainFrame.activateProgressBar(true, "Client - Server communication ...");
                    }
                }, (long)(secs*1000));
        }
        else {
            timer.schedule(new TimerTask() {
                    public void run() {
                        ClientMainFrame.activateProgressBar(true, "Client - Server communication ...");
                    }
                }, 0);
        }
    }
    
    /**
     * Ends the timer and sets the progress bar in ClientMainFrame to zero.
     */
    public static void endTimer() {
        if (timer != null) {
            timer.cancel();
            ClientMainFrame.activateProgressBar(false, "");
        }
    }
    
}
