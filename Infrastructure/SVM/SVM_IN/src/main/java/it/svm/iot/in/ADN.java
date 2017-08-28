package it.svm.iot.in;

import java.util.ArrayList;
import it.svm.iot.core.*;


/**
 * ADN for the SVM Infrastructure Node.
 * @author Paolo Sassi
 * @author Matteo Rotundo
 *
 */
public class ADN {
	
	/**
	 * Mca reference point for the IN.
	 */
	
	private static Mca IN_Mca = Mca.getInstance();
	
	/**
	 * Application Entities
	 */
	
	private static AE IN_AE_Monitor;
	private static AE IN_AE_Controller;
	
	/**
	 * List containing the registered containers.
	 */
	
	private static ArrayList<Container> containers = 
			new ArrayList<Container>();
	
	/**
	 * Discover all the useful resources on the MN.
	 * @param mn_cse URI of the MN
	 * @return List of the discovered resources
	 */
	
	private static String[] discover(String mn_cse) {
		String containers_mn_raw, id;
		String parent_cont = "";
		String[] containers_mn, tmp;
		int i = 0, vm_pos = 0;
	
		/* Discover the containers on the MN */
		containers_mn_raw = IN_Mca.discoverResources(mn_cse, "?fu=1&rty=3");
		containers_mn = containers_mn_raw.split(" ");
		
		for (String cont : containers_mn) {
			
			tmp = cont.split("/");
			if (i == vm_pos) {
				/* Create the container for the VM */
				id = tmp[tmp.length - 1].substring(4, 6);
				System.out.printf("Discovered VM: %s\n", id);
				parent_cont = Constants.IN_CSE_URI + "/" + 
						IN_AE_Monitor.getRn() + "/" + tmp[tmp.length - 1];
				containers.add(IN_Mca.createContainer(Constants.IN_CSE_URI + 
						"/" + IN_AE_Monitor.getRn(), tmp[tmp.length - 1]));
				vm_pos += (Constants.NUM_RESOURCES + 1);
			} else {
				/* Create the container for the resource */
				containers.add(IN_Mca.createContainer(parent_cont,
						tmp[tmp.length - 1]));
			}
			i++;
		}
		
		return containers_mn;
	}
	
	/**
	 * 
	 * @param containers_mn Array of containers in the MN cse 
	 * 					 	for the subscription.
	 * @param notification_url CoAP Server Url for the notification.
	 */
	
	private static void subscribe(String[] containers_mn, 
			String notification_url) {
		String []tmp;
		for (String cont_uri : containers_mn) {	
			if (cont_uri.toLowerCase().contains("sens") ||
					cont_uri.toLowerCase().contains("qty") ||
					cont_uri.toLowerCase().contains("alarm") ||
					cont_uri.toLowerCase().contains("status")) {
				tmp = cont_uri.split("/");
				IN_Mca.createSubscription(Constants.MN_CSE_COAP  + 
					cont_uri , notification_url, tmp[tmp.length - 1] + 
						"_monitor");
				System.out.println("Subscribed to: " + Constants.MN_CSE_COAP +
					cont_uri);
			}
		}
	}
	
	/**
	 * It inits the AE Controller with all the useful containers
	 * @param containers_mn Containers on the MN
	 */
	
	private static void init_controller(String[] containers_mn) {
		String []tmp;
		String parent_cont = "";
		int i = 0, vm_pos = 0;
		
		for (String cont : containers_mn) {

			tmp = cont.split("/");
			if (i == vm_pos) {
				/* Create the container for the VM */
				parent_cont = Constants.IN_CSE_URI + "/" + 
						IN_AE_Controller.getRn() + "/" + tmp[tmp.length - 1];
				containers.add(IN_Mca.createContainer(Constants.IN_CSE_URI + 
						"/" + IN_AE_Controller.getRn(), tmp[tmp.length - 1]));
				vm_pos += (Constants.NUM_RESOURCES + 1);
			} else {
				/* Create the container for the resource */
				containers.add(IN_Mca.createContainer(parent_cont,
						tmp[tmp.length - 1]));
			}
			i++;
		}
		
	}
	/**
	 * Private constructor for the ADN class.
	 */
	private ADN() {}

	/**
	 * Main method for the ADN on the IN side
	 * @param args Arguments for the ADN
	 */
	public static void main(String[] args) {
		String[] containers_mn;
		CoAPMonitorThread thread;
		
		System.out.printf("********** Infrastructure Node ADN **********\n");
		
		IN_AE_Monitor = IN_Mca.createAE(Constants.IN_CSE_URI, "SVM_Monitor");
		System.out.printf("AE SVM_Monitor registered on IN-CSE\n");
		
		/* CoAP server for handling notifications from the subscriptions */
		thread = new CoAPMonitorThread("monitor");
		thread.start();
		
		/* Discovering MN containers */
		containers_mn = discover(Constants.IN_CSE_COAP + "/" + 
				Constants.MN_CSE_ID);
		
		/* Subscribe for the resources to be sensed */
		System.out.println("Subscribing to the discovered resources...");
		subscribe(containers_mn, "coap://127.0.0.1:5685/monitor");
		
		/* Creating Controller AE */
		IN_AE_Controller = IN_Mca.createAE(Constants.IN_CSE_URI, "SVM_Controller");
		System.out.printf("AE SVM_Controller registered on IN-CSE\n");
		init_controller(containers_mn);

		while(true) {
		}
	}
}
