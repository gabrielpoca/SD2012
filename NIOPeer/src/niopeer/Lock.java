package niopeer;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is responsible for concurrency and giving the lock to handlers.
 * When a handler wants the lock it calls handlerWaitMutex. When the lock has the lock
 * then it calls the lock method on the first handler in the waiters list.
 */

public class Lock {

	private ReentrantLock lock = new ReentrantLock();
	private LinkedList<Handler> waiters = new LinkedList<Handler>();

	/**
	 * Used to find out if lock has waiters.
	 * 
	 * @return Returns true if has waiters and false if not.
	 */
	public boolean hasWaiters() {
		if(waiters.size() >= 1)
		    return true;
		else
		    return false;
	}

	/**
	 * Waits for has_mutex to be true and sets has_waiters to true in the time
	 * between.
	 */
	public void handlerWaitMutex(Handler h) {
		lock.lock();
		waiters.add(h);
		lock.unlock();
	}

	/**
	 * Gives the mutex to a waiter. It is used after waiters() in order to give
	 * some the mutex.
	 */
	public void giveMutexToHandler() {
		lock.lock();
		Handler h = waiters.removeFirst();
		h.lock();
		lock.unlock();
	}

	/**
	 * Used for a waiter to return the mutex. If there are waiters then the
	 * first gets the mutex and returns false if not it returns true.
	 * 
	 * @return Returns true if the mutex should be passed to the next peer or
	 *         false it is passed to another waiter.
	 */
	public boolean handlerReturnMutex() {
		lock.lock();
		boolean res = false;
		if (waiters.size() >= 1) {
		    Handler h = waiters.removeFirst();
		    h.lock();
		} else {
			res = true;
		}
		lock.unlock();
		return res;
	}
}
