package it.svm.iot.core;

import java.net.SocketException;
import java.util.ArrayList;

public class CoAPMonitorThread extends Thread{
	public String rn;
	public Mca mca;
	public ArrayList<String> mote_addr;
	private int coap_port;
	private String cse;
	public ArrayList<String> vm_id;
	
	public CoAPMonitorThread(String name, Mca mca, int port, 
			String cse, ArrayList<String> mote_addr, ArrayList<String> vm_id) {
		rn = name;
		this.mca = mca;
		this.mote_addr = mote_addr;
		this.vm_id = vm_id;
		coap_port = port;
		this.cse = cse;
	}
	public void run(){
		CoAPMonitor server;
		try {
			server = new CoAPMonitor(rn, mca, coap_port, cse, mote_addr, vm_id);
			server.addEndpoints();
	    	server.start();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
