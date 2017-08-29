package it.svm.iot.mn;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Set;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.WebLink;
import org.json.JSONArray;
import org.json.JSONObject;

import it.svm.iot.core.*;

/**
 * ADN for the SVM Middle Node.
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
	 * List of resource polling threads.
	 */
	private static ArrayList<PollingThread> polling_threads = new ArrayList<PollingThread>();
	
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
		CoapClient client;
		CoapResponse resp;
		URI uri = null;
		
		for (WebLink res : resources) {
			uri_s = res.getURI();
			if (!uri_s.equalsIgnoreCase("/.well-known/core") && 
					!uri_s.equalsIgnoreCase("/id")) {
				/* Create the resource container */
				uri_c = uri_s.replace("/", "");
				containers.add(MN_Mca.createContainer(vm_cont, uri_c));
				
				if (uri_s.toLowerCase().contains("alarm")) {
					/* Resources we want to observe */
					monitors.add(new ResourceMonitor("coap://[" + vm_addr +
							"]:5683/" + uri_s, vm_cont + "/" + uri_c));
					
				} else if (!uri_s.toLowerCase().contains("price")){
					/* Resources we want to poll */
					polling_threads.add(new PollingThread(MN_Mca, uri_s, uri_c, 
							vm_cont, vm_addr));
				} else {
					/* Resources which are not observed, send a simple GET*/
					try {
						uri = new URI("coap://["+vm_addr+"]:5683/" + uri_s);
					} catch (URISyntaxException e) {
						System.err.println("Invalid URI: " + e.getMessage());
						System.exit(-1);
					}
					client = new CoapClient(uri);
					resp = client.get();
					if (resp != null) {
						/* Add a content instance for the resource value */
						MN_Mca.createContentInstance(vm_cont + "/" + uri_c,
								resp.getResponseText());
					} else {
						System.out.println("No response received"
								+ " from " + "coap://[" + vm_addr + "]"
								+ ":5683/" + uri_s);
					}
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
		
		for (String addr : mote_addr) {
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
								System.out.printf("Found"
										+ " VM: %s%d\n", type, id);
								
								/* Add the container for the vending machine */
								containers.add(MN_Mca.createContainer(
										Constants.MN_CSE_URI + "/" + 
										MN_AE.getRn(), "SVM_" + type + id));
								registerResources(addr, resources, 
										Constants.MN_CSE_URI + "/" + 
										MN_AE.getRn() + "/" + containers.get(
										containers.size()-1).getRn());
								System.out.printf("Registered resources for"
										+ " VM: %s%d\n", type, id);
							} else {
								System.out.println("No response received"
										+ " from " + "coap://[" + addr + "]"
										+ ":5683/" + uri_s);
							}
						}
				}
			}
			
		}
		/* Register the shutdown hook in order to stop the monitoring threads.
		 */
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() { 
		    		for (ResourceMonitor mon : monitors) {
		    			mon.stopMonitor();
		    		}
		    	}
		});
	}
	
	/**
	 * Discover all the useful resources on the IN.
	 * @param in_cse URI of the IN
	 * @return List of the discovered resources
	 */
	private static String[] discover(String in_cse) {
		String ae_in_raw, containers_in_raw;
		String[] containers_in, ae_in;
		Boolean ae_found = false;
		
		/* Search the "SVM_Controller" AE on the IN */
		ae_in_raw = MN_Mca.discoverResources(in_cse, "?fu=1&rty=2");
		if (ae_in_raw == null) return null;
		ae_in = ae_in_raw.split(" ");
		for (String ae : ae_in) {
			if (ae.contains("SVM_Controller")) {
				System.out.println("SVM_Controller Found");
				ae_found = true;
			}
		}
		if (!ae_found) return null;

		/* Discover the containers on the SVM_Controller */
		containers_in_raw = MN_Mca.discoverResources(in_cse, "?fu=1&rty=3");
		if (containers_in_raw == null) return null;
		System.out.println(containers_in_raw);
		containers_in = containers_in_raw.split(" ");
		
		return containers_in;
	}
	
	/**
	 * Main method for the ADN on the MN side
	 * @param args Arguments for the ADN
	 */
	public static void main(String[] args) throws InterruptedException {
		String[] tmp;
		System.out.printf("********** Middle Node ADN **********\n");
		MN_AE = MN_Mca.createAE(Constants.MN_CSE_URI, "SVM_Monitor");
		System.out.printf("AE registered on MN-CSE\n");
		
		getMoteAddresses(Constants.BR_ADDR);
		System.out.println("Registering the VM...");
		registerVendingMachines();

		/* Start the resource monitors */
		for (ResourceMonitor mon : monitors) {
			mon.start();
		}
		for (PollingThread pt : polling_threads) {
			pt.start();
		}
		
		while(true) {
			/* Perform discovery on IN */
			System.out.println("Trying to discover CONTROLLER on IN");
			tmp = discover(Constants.MN_CSE_COAP + "/" + Constants.IN_CSE_ID);
			if (tmp != null) {
				for (String cont : tmp) {
					System.out.println(cont);
				}
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
