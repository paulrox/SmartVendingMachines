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
	 * List of the IDs of the discovered VMs.
	 */
	private static ArrayList<String> vm_id = new ArrayList<String>();
	
	/**
	 * Discover all the useful resources on the MN.
	 * @param mn_cse URI of the MN
	 * @return List of the discovered resources
	 */
	private static void discover(String mn_cse) {
		String containers_mn_raw = null; 
		String parent_cont = "";
		String[] containers_mn, tmp;
	
		/* Discover the containers on the MN */
		System.out.println("Discover VMs on MN...");
		containers_mn_raw = IN_Mca.discoverResources(mn_cse,
				"?fu=1&rty=3&lbl=Monitor");
		if (containers_mn_raw == null) return;
		containers_mn = containers_mn_raw.split(" ");
		
		for (String cont : containers_mn) {	
			/* Create the container for the monitored VM */
			tmp = cont.split("/");
			System.out.printf("Discovered: %s\n", tmp[tmp.length - 1]);
			containers.add(IN_Mca.createContainer(Constants.IN_CSE_URI + 
					"/" + IN_AE_Monitor.getRn(), tmp[tmp.length - 1]));
			vm_id.add(tmp[tmp.length - 1]);
			/* Create the container for the controlled VM */
			containers.add(IN_Mca.createContainer(Constants.IN_CSE_URI +
					"/" + IN_AE_Controller.getRn(), tmp[tmp.length - 1],
					"Controller"));
		}
		
		for (String id : vm_id) {
			containers_mn_raw = IN_Mca.discoverResources(mn_cse, 
					"?fu=1&rty=3&lbl=Monitor_" + id);
			if (containers_mn_raw == null) return;
			containers_mn = containers_mn_raw.split(" ");
			
			for (String cont : containers_mn) {	
				/* Create the container for the monitored resources */
				tmp = cont.split("/");
				parent_cont = Constants.IN_CSE_URI + "/" + 
						IN_AE_Monitor.getRn() + "/" + id;
				containers.add(IN_Mca.createContainer(parent_cont,
						tmp[tmp.length - 1]));
				/* We don't need the sensed temperature in the controller
				 * containers
				 */
				if (!cont.contains("sens")) {
					/* Create the container for the controlled resources */
					parent_cont = Constants.IN_CSE_URI + "/" + 
							IN_AE_Controller.getRn() + "/" + id;
					containers.add(IN_Mca.createContainer(parent_cont,
							tmp[tmp.length - 1], "Controller_" + id));
				}
				/* Subscribe for the useful resources */
				subscribe(cont, "coap://127.0.0.1:5685/monitor");
			}
		}
	}
	
	/**
	 * Subscribe for updates on selected resources.
	 * @param container_mn Container in the MN cse for the subscription.
	 * @param notification_url CoAP Server Url for the notification.
	 */
	private static void subscribe(String container_mn, 
			String notification_url) {
		String []tmp;
		
		if (container_mn.toLowerCase().contains("sens") ||
				container_mn.toLowerCase().contains("qty") ||
				container_mn.toLowerCase().contains("alarm") ||
				container_mn.toLowerCase().contains("status")) {
			tmp = container_mn.split("/");
			IN_Mca.createSubscription(Constants.MN_CSE_COAP  + 
					container_mn , notification_url, tmp[tmp.length - 1] + 
					"_monitor");
			System.out.println("Subscribed to: " + Constants.MN_CSE_COAP +
					container_mn);
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
		CoAPMonitorThread thread;
		
		System.out.printf("********** Infrastructure Node ADN **********\n");
		
		/* Creating Monitor AE */
		IN_AE_Monitor = IN_Mca.createAE(Constants.IN_CSE_URI, "SVM_Monitor");
		System.out.printf("AE SVM_Monitor registered on IN-CSE\n");
		
		/* Creating Controller AE */
		IN_AE_Controller = IN_Mca.createAE(Constants.IN_CSE_URI, "SVM_Controller");
		System.out.printf("AE SVM_Controller registered on IN-CSE\n");
		
		/* CoAP server for handling notifications from the subscriptions */
		thread = new CoAPMonitorThread("monitor", IN_Mca, 5685,
				Constants.IN_CSE_URI);
		thread.start();
		
		/* Discovering MN containers */
		discover(Constants.IN_CSE_COAP + "/" + Constants.MN_CSE_ID);
		

		while(true) {
		}
	}
}
