package it.svm.iot.core;

import java.net.SocketException;

public class CoAPMonitorThread extends Thread{
	public String rn;
	public Mca mca;
	private int coap_port;
	private String cse;
	public CoAPMonitorThread(String name, Mca mca, int port, String cse) {
		rn = name;
		this.mca = mca;
		coap_port = port;
		this.cse = cse;
	}
	public void run(){
		CoAPMonitor server;
		try {
			server = new CoAPMonitor(rn, mca, coap_port, cse);
			server.addEndpoints();
	    	server.start();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
