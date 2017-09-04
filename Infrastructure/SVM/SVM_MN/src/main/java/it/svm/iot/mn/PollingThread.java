package it.svm.iot.mn;

import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;

import it.svm.iot.core.Mca;

/**
 * Class which implements the thread used to poll the resources on
 * the VMs.
 * @author Paolo Sassi
 * @author Matteo Rotundo
 *
 */

public class PollingThread extends Thread {
	
	private URI uri;
	private CoapClient client;
	private CoapResponse resp;
	private String old_resp;
	private Mca MN_Mca;
	private String res_uri;
	private String res_uri_cont;
	private String vm_cont;
	private String vm_addr;
	
	/**
	 * Constructor for the class PollingThread
	 * @param MN_Mca Mca reference point for the MN.
	 * @param uri_s Resource URI
	 * @param uri_c Resource URI for the Container
	 * @param vm_container Vending Machine Container
	 * @param vm_address Vending Machine Address
	 */
	
	public PollingThread(Mca MN_Mca_ref, String uri_s,
			String uri_c, String vm_container, String vm_address) {
			
		MN_Mca = MN_Mca_ref;
		res_uri = uri_s;
		res_uri_cont = uri_c;
		vm_cont = vm_container;
		vm_addr = vm_address;
		
		System.out.println("coap://["+vm_addr+"]:5683" + res_uri);
		try {
			uri = new URI("coap://["+vm_addr+"]:5683" + res_uri);
		} catch (URISyntaxException e) {
			System.err.println("Invalid URI: " + e.getMessage());
			System.exit(-1);
		}
		
		client = new CoapClient(uri);
		old_resp = new String("first_time");
	}
	
	/**
	 * PollingThread Body
	 */
	
	public void run() {
		
		while (true) {
			resp = client.get();
			if (resp != null) {		
				if (!resp.getResponseText().equals
						(old_resp)) {
					/* Add a content instance for the resource value */
					MN_Mca.createContentInstance(vm_cont + "/" + res_uri_cont,
							resp.getResponseText());
					old_resp = new String(resp.getResponseText());
				}
			}/* else {
				System.out.println("No new content"
						+ " from " + "coap://[" + vm_addr + "]"
						+ ":5683" + res_uri);
			}*/
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
