package ApproxsimClient.communication;

import java.lang.Comparable;
import java.util.TreeMap;

/**
 * A thread safe priority queue with both blocking and non-blocking dequeue. The queue has FIFO behaviour when two Objects A and B are given
 * the same priority.
 * 
 * @version 1, $Date: 2005/10/19 13:11:57 $
 * @author Per Alexius
 */
public class PriorityQueue<T> {
    /** The counter that keeps track of the FIFO order. */
    private int mCount = 0;

    /** The TreeMap used to implement the queue. */
    private TreeMap<PQKey, T> mMap = new TreeMap<PQKey, T>();

    /**
     * Enqueues an Object with priority prio.
     * 
     * @param o The Object to be enqueued.
     * @param prio The priority of the enqueued Object.
     */
    public synchronized void enqueue(T o, int prio) {
        mMap.put(new PQKey(prio, mCount++), o);
        notifyAll();
    }

    /**
     * Dequeues the Object with the highest priority.
     * 
     * @return The Object with the highest priority in the queue or null if the queue is empty.
     */
    public synchronized T dequeue() {
        return (mMap.isEmpty() ? null : mMap.remove(mMap.lastKey()));
    }

    /**
     * Removes all entries in the queue
     */
    public synchronized void clear() {
        mMap.clear();
    }

    /**
     * Dequeues the Object with the highest priority or if the queue is empty - block until an Object is enqueued by another thread.
     * 
     * @return The Object with the highest priority in the queue.
     */
    public synchronized T blockingDequeue() {
        while (mMap.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
        return mMap.remove(mMap.lastKey());
    }

    /**
     * Checks if the queue is empty.
     * 
     * @return true if the queue is empty, false otherwise.
     */
    public boolean empty() {
        return mMap.isEmpty();
    }

    /**
     * Returns the size of the queue.
     * 
     * @return size of the queue.
     */
    public int size() {
        return mMap.size();
    }

    /**
     * A class used as key in a PriorityQueue. The ordering between two keys A and B is as follows: A is less than B if A's priority is less
     * than B's priority or if A and B has the same priority and A's count is larger than B's.
     */
    public class PQKey implements Comparable<PQKey> {
        /** The priority */
        private int mPrio;
        /** The count keeping track of the key enqueuement order */
        private int mCount;

        /**
         * Creates a new PQKey.
         * 
         * @param prio The priority
         * @param count The count keeping track of the key enqueuement order.
         */
        public PQKey(int prio, int count) {
            mPrio = prio;
            mCount = count;
        }

        /**
         * Implementation of the Comparable interface.
         * 
         * @param o The key to compare this key to.
         * @return -1, 0 or 1 if this key is less than, equal to or greater than o, respectively.
         */
        public int compareTo(PQKey p) {
            if (equals(p)) {
                return 0;
            } else {
                return (lessThan(p) ? -1 : 1);
            }
        }

        /**
         * Checks if this key is less than n.
         * 
         * @param n The key to compare this key to.
         * @return true if this key is less than n, false otherwise.
         */
        public boolean lessThan(PQKey n) {
            return (mPrio < n.mPrio || mPrio == n.mPrio && mCount > n.mCount);
        }

        /**
         * Checks if this key is equal to n.
         * 
         * @param n The key to compare this key to.
         * @return true if this key is equal to n, false otherwise.
         */
        public boolean equals(PQKey n) {
            return (mPrio == n.mPrio && mCount == n.mCount);
        }
    }
}
