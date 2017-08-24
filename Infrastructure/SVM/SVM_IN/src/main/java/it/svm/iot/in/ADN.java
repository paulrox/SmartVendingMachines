package it.svm.iot.in;

import java.util.ArrayList;
import java.util.Scanner;
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
	 * Application Entity of the MN.
	 */
	
	private static AE IN_AE;
	
	/**
	 * List containing the registered containers.
	 */
	
	private static ArrayList<Container> containers = 
			new ArrayList<Container>();
	
	/**
	 * Discover all the useful resources on the MN.
	 * @param mn_cse URI of the MN
	 */
	
	private static String[] discover(String mn_cse) {
		String containers_mn_raw;
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
				parent_cont = Constants.IN_CSE_URI + "/" + 
						IN_AE.getRn() + "/" + tmp[tmp.length - 1];
				containers.add(IN_Mca.createContainer(Constants.IN_CSE_URI + 
						"/" + IN_AE.getRn(), tmp[tmp.length - 1]));
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
	 * @param mn_cse Middle Node CSE Uri to be subscribed.
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
				System.out.println("coap://127.0.0.1:5684/~"  + 
					cont_uri);
				tmp = cont_uri.split("/");
				
				IN_Mca.createSubscription("coap://127.0.0.1:5684/~"  + 
					cont_uri , notification_url, tmp[tmp.length - 1] + "_monitor");
			}
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
		Scanner keyboard = new Scanner(System.in);
		Boolean exit = false;
		String input;
		String[] containers_mn;
		CoAPMonitorThread thread;
		
		System.out.printf("********** Infrastructure Node ADN **********\n");
		IN_AE = IN_Mca.createAE(Constants.IN_CSE_URI, "SVM_Monitor");
		System.out.printf("AE registered on IN-CSE\n");
		
		/* CoAP server for handling notifications from the subscriptions */
		thread = new CoAPMonitorThread("monitor");
		thread.start();
		
		/* Discovering MN containers */
		containers_mn = discover(Constants.MN_CSE_SHORT_URI);
		
		/* Subscribe for the resources to be sensed */
		subscribe(containers_mn, "coap://127.0.0.1:5685/monitor");
		
		System.out.println("Enter 'q' to quit");
		while(!exit) {
			/* Busy wait */
			input = keyboard.nextLine();
			if (input != null && input.equals("q"))
				exit = true;
		}
		keyboard.close();
	}
}
