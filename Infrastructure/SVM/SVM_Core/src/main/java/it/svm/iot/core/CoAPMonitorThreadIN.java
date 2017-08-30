package it.svm.iot.core;

import java.net.SocketException;
import java.util.ArrayList;

public class CoAPMonitorThreadIN extends Thread{
	public String rn;
	public Mca mca;
	private int coap_port;
	private String cse;
	public ArrayList<VendingMachine> vms;
	
	
	public CoAPMonitorThreadIN(String name, Mca mca, int port, String cse, ArrayList<VendingMachine> vms) {
		rn = name;
		this.vms = vms;
		this.mca = mca;
		coap_port = port;
		this.cse = cse;
	}
	public void run(){
		CoAPMonitorIN server;
		try {
			server = new CoAPMonitorIN(rn, mca, coap_port, cse, vms);
			server.addEndpoints();
	    	server.start();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
