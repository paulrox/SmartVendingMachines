package it.svm.iot.mn;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Set;
import java.util.Scanner;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.json.JSONArray;
import org.json.JSONObject;

import it.svm.iot.core.*;

/**
 * ADN for the SVM Middle Node
 * @author Paolo Sassi
 * @author Matteo Rotundo
 *
 */
public class ADN {

	/**
	 * Mca reference point for the MN.
	 */
	private static Mca MN_Mca = Mca.getInstance();
	
	/**
	 * Application Entity of the MN.
	 */
	private static AE MN_AE;
	
	/**
	 * List containing the registered containers.
	 */
	private static ArrayList<Container> containers = 
			new ArrayList<Container>();
	
	/**
	 * List of mote addresses.
	 */
	private static ArrayList<String> mote_addr = new ArrayList<String>();
	
	/**
	 * List of resource monitors.
	 */
	private static ArrayList<ResourceMonitor> monitors = new ArrayList<ResourceMonitor>();
	
	/**
	 * Semaphore used to synchronize the resource monitoring threads with
	 *  the ADN.
	 */
	private static SimpleSem om2m_sem = new SimpleSem();
	
	/**
	 * Private constructor for the ADN class.
	 */
	private ADN() {}
	
	/**
	 * Sends a CoAP GET request to the BR in order to receive the list of all
	 * the reachable motes.
	 * @param br_addr IPv6 address of the Border Router
	 */
	private static void getMoteAddresses(String br_addr) {
		URI uri = null;
		CoapClient br_client;
		CoapResponse response;
		JSONObject obj;
		JSONArray json_addr;
		
		/* Build the CoAP URI */
		try {
			uri = new URI("coap://[" + br_addr + "]:5683/addr");
		} catch (URISyntaxException e) {
			System.err.println("Invalid URI: " + e.getMessage());
			System.exit(-1);
		}
		/* Send the GET request to the BR "addr" resource */
		br_client = new CoapClient(uri);
		response = br_client.get();
		if (response != null) {
			System.out.println("Successfully received mote addresses from BR:");
			obj = new JSONObject(response.getResponseText());			
			json_addr = obj.getJSONArray("addr");
			for (Object addr : json_addr)
				mote_addr.add(addr.toString());
			System.out.println(mote_addr);
		} else {
			System.out.println("No response received from BR.");
		}
		
	}
	
	/**
	 * Registers to the MN the resources available on a single VM.
	 * @param vm_addr IPv6 address of the VM
	 * @param resources List of resources available on the VM
	 * @param vm_cont Container of the parent VM
	 */
	private static void registerResources(String vm_addr, Set<WebLink> resources,
			String vm_cont) {
		String uri_s, uri_c;
		
		for(WebLink res : resources) {
			uri_s = res.getURI();
			if (!uri_s.equalsIgnoreCase("/.well-known/core") && 
					!uri_s.equalsIgnoreCase("/id")) {
				/* Create the resource container */
				uri_c = uri_s.replace("/", "");
				containers.add(MN_Mca.createContainer(vm_cont, uri_c));
				
				if(!uri_s.toLowerCase().contains("price")) {
					/* We don't want to observe the products prices */
					monitors.add(new ResourceMonitor("coap://[" + vm_addr +
							"]:5683/" + uri_s, vm_cont + "/" + uri_c,
							om2m_sem));
					
				}
			}
		}
	}
	
	/**
	 * Uses the addresses of all the reachable motes to get the list of all
	 * the available resources and registers the containers for each VM.
	 */
	private static void registerVendingMachines() {
		URI uri = null;
		CoapClient mote_client;
		CoapResponse response;
		JSONObject obj;
		Set<WebLink> resources;
		String uri_s, type;
		int id;
		
		for(String addr : mote_addr) {
			/* Build the CoAP URI */
			try {
				uri = new URI("coap://[" + addr + "]:5683");
			} catch (URISyntaxException e) {
				System.err.println("Invalid URI: " + e.getMessage());
				System.exit(-1);
			}
			
			mote_client = new CoapClient(uri);
			resources = mote_client.discover();
			if (resources != null) {
				for (WebLink res : resources) {
					uri_s = res.getURI();
					if (!uri_s.equalsIgnoreCase("/.well-known/core"))
						if (uri_s.equalsIgnoreCase("/id")) {
							/* Send GET request for "id" resource */
							try {
								uri = new URI("coap://[" + addr +
										"]:5683/" + uri_s);
							} catch (URISyntaxException e) {
								System.err.println("Invalid URI: " + e.getMessage());
								System.exit(-1);
							}
							mote_client = new CoapClient(uri);
							response = mote_client.get();
							if (response != null) {
								obj = new JSONObject(response.getResponseText());			
								id = obj.getInt("id");
								type = obj.getString("type");
								System.out.printf("Received response from"
										+ " VM: %s%d\n", type, id);
								
								/* Add the container for the vending machine */
								System.out.println(Constants.MN_CSE_URI + "/" + 
										MN_AE.getRn());
								containers.add(MN_Mca.createContainer(
										Constants.MN_CSE_URI + "/" + 
										MN_AE.getRn(), "SVM_" + type + id));
								registerResources(addr, resources, 
										Constants.MN_CSE_URI + "/" + 
										MN_AE.getRn() + "/" + containers.get(
										containers.size()-1).getRn());
							} else {
								System.out.println("No response received"
										+ " from " + "coap://[" + addr + "]"
										+ ":5683 + uri_s");
							}
						}
				}
			}
			
		}
	}
	
	/**
	 * Main method for the ADN on the MN side
	 * @param args Arguments for the ADN
	 */
	public static void main(String[] args) {
		Scanner keyboard = new Scanner(System.in);
		Boolean exit = false;
		String input;
		System.out.printf("********** Middle Node ADN **********\n");
		MN_AE = MN_Mca.createAE(Constants.MN_CSE_URI, "SVM_Monitor");
		System.out.printf("AE registered on MN-CSE\n");
		//Container container = MN_Mca.createContainer("coap://127.0.0.1:5683/~/svm-mn-cse/svm-mn-name/SVM_Monitor", "DATA");
		//MN_Mca.createContentInstance("coap://127.0.0.1:5683/~/svm-mn-cse/svm-mn-name/SVM_Monitor/DATA", "ciao");
		
		getMoteAddresses(Constants.BR_ADDR);
		
		registerVendingMachines();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		for (ResourceMonitor mon : monitors) {
			mon.start();
		}
		//om2m_sem.semSignal();
		
		System.out.println("Enter 'q' to quit");
		while(!exit) {
			/* Busy wait */
			input = keyboard.nextLine();
			if (input != null && input.equals("q"))
				exit = true;
		}
		keyboard.close();
		System.out.println("OK");
	}

}
