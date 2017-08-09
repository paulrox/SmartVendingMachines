package it.svm.iot.core;

/**
 * Implementation of a simple synchronization semaphore.
 * @author Paolo Sassi
 * @author Matteo Rotundo
 *
 */
public class SimpleSem {
	Object lock = new Object();
	Boolean ready;
	
	/**
	 * Semaphore constructor
	 * @param ready Sets the initial value of the semaphore (free or busy).
	 */
	public SimpleSem(Boolean ready) {
		this.ready = ready;
	}
	
	/**
	 * Performs the wait operation on the semaphore.
	 */
	public void semWait() {
		synchronized(lock) {
			while(!ready) {
				try {
					lock.wait();
				} catch(InterruptedException e) {}
			}
			ready = false;
		}
	}
	
	/**
	 * Performs the signal operation of the semaphore and wakes up all the 
	 * threads that were blocked on it.
	 */
	public void semSignal() {
		synchronized(lock) {
			ready = true;
			lock.notifyAll();
		}
	}
}
