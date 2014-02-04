#ifndef _TSQUEUE_H
#define _TSQUEUE_H

#include <queue>
#include <boost/thread/condition.hpp>
#include <boost/thread/mutex.hpp>

/**
 * \brief Threadsafe wrapper around the standard library queue.
 *
 * If a thread tries to dequeue an element when the queue is empty,
 * the thread will block until another thread enqueues an
 * element. Can be used for message passing between threads.
 *
 * \author   Per Alexius
 * \date     $Date: 2006/07/03 14:18:23 $
 */
template <class T> class TSQueue
{
private:
     boost::mutex mLock;   ///< Semaphore for this queue.
     boost::condition mEmpty;   ///< Condition variable for blocking on empty queue.
     std::queue<T> mQ;        ///< The queue.

public:
     /// Constructor.
     TSQueue() { }
     /// Destructor.
     ~TSQueue() { }
     
     /**
      * \brief Enqueue an element.
      *
      * \param t The element to be enqueued.
      */
     inline void enqueue(T t);

     /**
      * \brief Dequeue an element or block if there is no element.
      *
      * \return The dequeued element.
      */
     inline T dequeue();

     /**
      *   \brief Returns the number of elements in the queue
      *
      *   \return The number of elements in the queue
      */
     inline unsigned int size() const { return mQ.size(); }
};

template<class T>
inline void TSQueue<T>::enqueue(T t)
{

     boost::mutex::scoped_lock lk(mLock);   // Lock
     mQ.push(t);                            // Enqueue
     if (mQ.size() == 1) {                  // If the queue was empty 
          mEmpty.notify_one();              // Notify a waiting thread
     }
     lk.unlock();                           // Unlock by lk's
                                            // destructor but use
                                            // explicit unlock for
                                            // clarity.
}

template<class T>
inline T TSQueue<T>::dequeue()
{
     boost::mutex::scoped_lock lk(mLock);   // Lock

     if (mQ.empty()) {                      // If the queue is empty...
          mEmpty.wait(lk);                  // ...block and wait
     }
     // Read, remove, unlock and return
     T t = mQ.front();
     mQ.pop();
     lk.unlock();                           // Unlock by lk's
                                            // destructor but use
                                            // explicit unlock for
                                            // clarity.
     return t;
}

#endif   // _TSQUEUE_H
