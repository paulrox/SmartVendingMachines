package it.svm.iot.in;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.Request;
import org.json.JSONException;
import org.json.JSONObject;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

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
	 * List containing the vending machines
	 */
	public static ArrayList<VendingMachine> vms =
			new ArrayList<VendingMachine>();
	
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
	 * It initializes the SVM_Monitor with the content instances in the MN
	 * It creates also a VM class for each vending machine
	 * @param mn_cse URI MN CSE
	 */
	private static void init_monitor_container(String mn_cse) {
		String containers_mn_raw = null, parent_cont = "", response;
		String[] containers_mn, tmp;
		URI uri = null;
		CoapClient client;
		
		
		for (String id : vm_id) {
			vms.add(new VendingMachine());
			vms.get(vms.size() - 1).setId(Integer.parseInt(id.substring(5, id.length())));
			
			containers_mn_raw = IN_Mca.discoverResources(mn_cse, 
					"?fu=1&rty=3&lbl=Monitor_" + id);
			containers_mn = containers_mn_raw.split(" ");

			for (String cont : containers_mn) {	
				JSONObject root = null;
				int retry = 0;
				tmp = cont.split("/");
				parent_cont = Constants.MN_CSE_URI + "/" + 
						IN_AE_Monitor.getRn() + "/" + id;
			
				try {
					uri = new URI(parent_cont + "/" +
							tmp[tmp.length - 1] + "/" + "la");
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				while (root == null) {
					client = new CoapClient(uri);
					Request req = Request.newGet();
					req.getOptions().addOption(new Option(267, 2));
					req.getOptions().addOption(new Option(256, "admin:admin"));
					req.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
					req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
					/* GET request for the last content instance */
					CoapResponse responseBody = client.advanced(req);
					response = new String(responseBody.getPayload());
					try {
						root = new JSONObject(response);
					} catch (JSONException e) {
						/* The container is still empty */
						retry++;
						if (retry == 3) {
							/* There is a problem with the application, exit */
							e.printStackTrace();
							System.exit(-1);
						}
						/* Wait before retrying */
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}
				JSONObject m2mcin = root.getJSONObject("m2m:cin");
				response = m2mcin.getString("con");
				parent_cont = Constants.IN_CSE_URI + "/" + 
						IN_AE_Monitor.getRn() + "/" + id;
				IN_Mca.createContentInstance(parent_cont + "/" +
						tmp[tmp.length - 1], response);
				vms.get(vms.size() - 1).set_vm_res(response, tmp[tmp.length - 1]);	
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
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		CoAPMonitorThreadIN thread;
		
		System.out.printf("********** Infrastructure Node ADN **********\n");
		
		/* Creating Monitor AE */
		IN_AE_Monitor = IN_Mca.createAE(Constants.IN_CSE_URI, "SVM_Monitor");
		System.out.printf("AE SVM_Monitor registered on IN-CSE\n");
		
		/* Creating Controller AE */
		IN_AE_Controller = IN_Mca.createAE(Constants.IN_CSE_URI, "SVM_Controller");
		System.out.printf("AE SVM_Controller registered on IN-CSE\n");
		
		/* CoAP server for handling notifications from the subscriptions */
		thread = new CoAPMonitorThreadIN("monitor", IN_Mca, 5685,
				Constants.IN_CSE_URI, vms);
		thread.start();
		
		/* Discovering MN containers */
		discover(Constants.IN_CSE_COAP + "/" + Constants.MN_CSE_ID);
		
		init_monitor_container(Constants.IN_CSE_COAP + "/" + Constants.MN_CSE_ID);
			

		Server server = new Server(8000);
        WebSocketHandler wsHandler = new WebSocketHandler() {
            @Override
            public void configure(WebSocketServletFactory factory) {
                factory.register(MyWebSocketHandler.class);
            }
        };
        server.setHandler(wsHandler);
        server.start();
        server.join();
    	
		while(true) {
			
		}
		
	}
}
