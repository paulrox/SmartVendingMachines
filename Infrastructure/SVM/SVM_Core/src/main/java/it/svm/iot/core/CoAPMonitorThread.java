package it.svm.iot.core;

import java.net.SocketException;

public class CoAPMonitorThread extends Thread{
	public String rn;
	public Mca IN_Mca;
	public CoAPMonitorThread(String name, Mca mca) {
		rn = name;
		IN_Mca = mca;
	}
	public void run(){
		CoAPMonitor server;
		try {
			server = new CoAPMonitor(rn, IN_Mca);
			server.addEndpoints();
	    	server.start();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
