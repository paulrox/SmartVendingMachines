package it.svm.iot.core;

import java.net.SocketException;
import java.util.ArrayList;

public class CoAPMonitorThread extends Thread{
	public String rn;
	public ArrayList<String> mote_addr;
	private int coap_port;
	public ArrayList<String> vm_id;
	
	public CoAPMonitorThread(String name, int port, 
			ArrayList<String> mote_addr, ArrayList<String> vm_id) {
		rn = name;
		this.mote_addr = mote_addr;
		this.vm_id = vm_id;
		coap_port = port;
	}
	public void run(){
		CoAPMonitor server;
		try {
			server = new CoAPMonitor(rn, coap_port, mote_addr, vm_id);
			server.addEndpoints();
	    	server.start();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
