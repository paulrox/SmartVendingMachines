package it.svm.iot.mn;

import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.californium.core.CoapClient;
import it.svm.iot.core.SimpleSem;

/**
 * Class which implements the threads used to observe the resources on
 * the VMs.
 * @author Paolo Sassi
 * @author Matteo Rotundo
 *
 */
public class ResourceMonitor extends Thread {
	ResourceObserver obs;
	CoapClient client;
	URI uri;
	SimpleSem sem;
	
	public ResourceMonitor(String mote_uri, String cont, SimpleSem sem) {
		uri = null;
		this.sem = sem;
		
		try {
			uri = new URI(mote_uri);
		} catch (URISyntaxException e) {
			System.err.println("Invalid URI: " + e.getMessage());
			System.exit(-1);
		}
		
		obs = new ResourceObserver(cont);
	}
	
	public void run() {
		//sem.semWait();
		client = new CoapClient(uri);
		client.observe(obs);
		
		while(true) {}
	}

}
