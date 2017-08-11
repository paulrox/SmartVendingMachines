package it.svm.iot.mn;

import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapObserveRelation;
import it.svm.iot.core.Mca;
import it.svm.iot.core.SimpleSem;


/**
 * Class which implements the threads used to observe the resources on
 * the VMs.
 * @author Paolo Sassi
 * @author Matteo Rotundo
 *
 */
public class ResourceMonitor extends Thread {
	
	private ResourceObserver obs;
	private CoapClient client;
	private CoapObserveRelation rel;
	private URI uri;
	private SimpleSem cin_ready;
	private Mca MN_Mca;
	private String container;
	private Boolean exit;
	
	/**
	 * Constructor for the ResourceMonitor class.
	 * @param mote_uri URI of the monitored mote
	 * @param container Container for the content instances
	 */
	public ResourceMonitor(String mote_uri, String container) {
		this.container = container;
		exit = false;
		uri = null;
		cin_ready = new SimpleSem(false);
		MN_Mca  = Mca.getInstance();
		try {
			uri = new URI(mote_uri);
		} catch (URISyntaxException e) {
			System.err.println("Invalid URI: " + e.getMessage());
			System.exit(-1);
		}
		obs = new ResourceObserver(cin_ready);
	}
	
	/**
	 * Get the observe relation object.
	 * @return The observe relation object
	 */
	public CoapObserveRelation getRelation() {
		return rel;
	}
	
	public void stopMonitor() {
		/* Cancel the resource observation */
		rel.proactiveCancel();
		exit = true;
	}
	
	/**
	 * Thread body for the ResourceMonitor
	 */
	public void run() {
		String content;
		client = new CoapClient(uri);
		rel = client.observe(obs);
		
		while(!exit) {
			/* Wait for a new content to publish on the MN-CSE */
			cin_ready.semWait();
			content = obs.getContent();
			MN_Mca.createContentInstance(container, content);
		}
	}
}
