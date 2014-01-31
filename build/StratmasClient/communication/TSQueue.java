package StratmasClient.communication;


import java.util.LinkedList;


/**
 * A thread safe queue with both blocking and non-blocking
 * dequeue.
 *
 * @version 1, $Date: 2006/03/22 14:30:50 $
 * @author  Per Alexius
*/
public class TSQueue {
     /** The LinkedList used to implement the queue. */
     private LinkedList mLL = new LinkedList();

     /**
      * Enqueues an Object.
      * 
      * @param o The Object to be enqueued.
      */
     public synchronized void enqueue(Object o) {
	  mLL.addLast(o);
	  notifyAll();
     }

     /**
      * Dequeues the Object at the front of the queue
      * 
      * @return The Object at the front of the queue or
      * null if the queue is empty.
      */
     public synchronized Object dequeue() {
	  return (isEmpty() ? null : mLL.removeFirst());
     }

     /**
      * Dequeues the Object at the front of the queue or if the queue
      * is empty - block until an Object is enqueued by another
      * thread.
      * 
      * @return The Object with the highest priority in the queue.
      */
     public synchronized Object blockingDequeue() {
	  while (isEmpty()) {
	       try {
		    wait();
	       } catch (InterruptedException e) {
	       }
	  }
	  return mLL.removeFirst();
     }

     /**
      * Removes all entries in the queue
      */
     public synchronized void clear() {
	  mLL.clear();
     }

     /**
      * Checks if the queue is empty.
      *
      * @return true if the queue is empty, false otherwise.
      */
     public boolean isEmpty() {
	  return mLL.isEmpty();
     }

     /**
      * Returns the number of elements in the queue.
      *
      * @return The number of elements in the queue.
      */
     public int size() {
	  return mLL.size();
     }
}
