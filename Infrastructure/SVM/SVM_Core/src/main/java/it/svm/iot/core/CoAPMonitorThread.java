package it.svm.iot.core;

import java.net.SocketException;

public class CoAPMonitorThread extends Thread{
	public String rn;
	public CoAPMonitorThread(String name) {
		rn = name;
	}
	public void run(){
		CoAPMonitor server;
		try {
			server = new CoAPMonitor(rn);
			server.addEndpoints();
	    	server.start();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
