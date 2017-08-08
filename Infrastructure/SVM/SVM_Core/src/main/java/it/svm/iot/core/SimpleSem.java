package it.svm.iot.core;

public class SimpleSem {
	Object lock = new Object();
	Boolean ready = false;
	
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
	
	public void semSignal() {
		synchronized(lock) {
			ready = true;
			lock.notifyAll();
		}
	}
}
