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
	 * Application Entity of the MN monitor.
	 */
	private static AE MN_AE_Monitor;

	/**
	 * Application Entity of the MN controller.
	 */
	private static AE MN_AE_Controller;

	/**
	 * List containing the registered containers.
	 */
	private static ArrayList<Container> containers = 
			new ArrayList<Container>();

	/**
	 * List of mote addresses.
	 */
	public static ArrayList<String> mote_addr = new ArrayList<String>();

	/**
	 * List of resource monitors.
	 */
	private static ArrayList<ResourceMonitor> monitors = new ArrayList<ResourceMonitor>();

	/**
	 * List of resource polling threads.
	 */
	private static ArrayList<PollingThread> polling_threads = new ArrayList<PollingThread>();

	/**
	 * List of the IDs of the discovered VMs.
	 */
	private static ArrayList<String> vm_id = new ArrayList<String>();
	
	/**
	 * List of the subscription status for the controller resources
	 * of the VMs.
	 */
	private static ArrayList<Boolean> sub_status = new ArrayList<Boolean>();
	
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
				containers.add(MN_Mca.createContainer(vm_cont, uri_c,
						"Monitor_" + vm_id.get(vm_id.size() - 1)));

				if (uri_s.toLowerCase().contains("alarm") || 
						uri_s.toLowerCase().contains("tempsens")) {
					/* Resources we want to observe */
					monitors.add(new ResourceMonitor("coap://[" + vm_addr +
							"]:5683/" + uri_s, vm_cont + "/" + uri_c));

				} else if (!uri_s.toLowerCase().contains("price") ||
						!uri_s.toLowerCase().contains("loc")){
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
								vm_id.add("SVM_" + type + id);
								sub_status.add(false);
								/* Add the container for the vending machine */
								containers.add(MN_Mca.createContainer(
										Constants.MN_CSE_URI + "/" + 
										MN_AE_Monitor.getRn(), "SVM_" + type +
										id, "Monitor"));
								registerResources(addr, resources, 
										Constants.MN_CSE_URI + "/" + 
										MN_AE_Monitor.getRn() + "/" +
										containers.get(containers.size() -
										1).getRn());
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
	private static void discover(String in_cse) {
		String ae_in_raw, containers_in_raw, parent_cont = "";;
		String[] containers_in = null;
		String[] ae_in, tmp;
		Boolean ae_found = false;
		Container cnt;
		int i = 0, j = 0;

		/* Search the "SVM_Controller" AE on the IN */
		ae_in_raw = MN_Mca.discoverResources(in_cse, "?fu=1&rty=2");
		if (ae_in_raw == null) return;
		ae_in = ae_in_raw.split(" ");
		for (String ae : ae_in) {
			if (ae.contains("SVM_Controller"))
				ae_found = true;
		}
		if (!ae_found) return;

		/* Discover the containers on the SVM_Controller */
		for (String id : vm_id) {
			if (!sub_status.get(i)) {
				/* Create the container for the controlled VM */
				cnt = MN_Mca.createContainer(Constants.MN_CSE_URI + 
						"/" + MN_AE_Controller.getRn(), id);
				if (cnt != null)
					/* The VM container has not been added yet */
					containers.add(cnt);
				containers_in_raw = MN_Mca.discoverResources(in_cse, 
						"?fu=1&rty=3&lbl=Controller_" + id);
				if (containers_in_raw == null)
					/* The VM has no resources containers, try again later */
					return;
				containers_in = containers_in_raw.split(" ");
				for (String cont : containers_in) {	
					/* Create the container for the controlled resources */
					tmp = cont.split("/");
					parent_cont = Constants.MN_CSE_URI + "/" + 
							MN_AE_Controller.getRn() + "/" + id;
					cnt = MN_Mca.createContainer(parent_cont,
							tmp[tmp.length - 1]);
					if (cnt != null) {
						/* The resource container has not been added yet */
						containers.add(cnt);
						/* Subscribe for the useful resources */
						subscribe(cont, "coap://127.0.0.1:5686/monitor");
					}
				}
				j++;
				if (j == Constants.MN_SUB_RES)
					/* The subscription has been performed on all the 
					 * resources */
					sub_status.set(i, true);
			}
			i++;
		}
	}
	
	/**
	 * Subscribe for updates on selected resources.
	 * @param container_in Container in the IN cse for the subscription.
	 * @param notification_url CoAP Server Url for the notification.
	 */
	private static void subscribe(String container_in, 
			String notification_url) {
		String []tmp;

		if (!container_in.toLowerCase().contains("sens")) {
			tmp = container_in.split("/");
			MN_Mca.createSubscription(Constants.IN_CSE_COAP  + 
					container_in , notification_url, tmp[tmp.length - 1] + 
					"_monitor");
			System.out.println("Subscribed to: " + Constants.IN_CSE_COAP +
					container_in);
		}
	}

	/**
	 * Main method for the ADN on the MN side
	 * @param args Arguments for the ADN
	 */
	public static void main(String[] args) throws InterruptedException {
		CoAPMonitorThread thread;

		System.out.printf("********** Middle Node ADN **********\n");

		MN_AE_Monitor = MN_Mca.createAE(Constants.MN_CSE_URI, "SVM_Monitor");
		System.out.printf("AE SVM_Monitor registered on MN-CSE\n");

		MN_AE_Controller = MN_Mca.createAE(Constants.MN_CSE_URI, "SVM_Controller");
		System.out.printf("AE SVM_Controller registered on MN-CSE\n");

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

		/* CoAP server for handling notifications from the subscriptions */
		thread = new CoAPMonitorThread("monitor", MN_Mca, 5686,
				Constants.MN_CSE_URI, mote_addr);
		thread.start();

		while(true) {
			/* Perform discovery on IN */
			discover(Constants.MN_CSE_COAP + "/" + Constants.IN_CSE_ID);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
