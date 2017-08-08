package it.svm.iot.mn;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;

import it.svm.iot.core.*;

/**
 * This class implements the CoapHandler interface in order to handle
 * asynchronous requests from the CoAP servers.
 * @author Paolo Sassi
 * @author Matteo Rotundo
 *
 */
public class ResourceObserver implements CoapHandler {
	
	private static Mca MN_Mca = Mca.getInstance();
	private String cont;
	/**
	 * ResouceObserver public constructor.
	 * @param cont String containing the parent container on the MN
	 */
	public ResourceObserver(String cont) {
		this.cont = cont;
	}
	
	@Override
    public void onLoad(CoapResponse response) {
		System.out.println(cont);
		MN_Mca.createContentInstance(cont, response.getResponseText());
        System.out.println(response.getResponseText());
    }

    @Override
    public void onError() {
        System.err.println("Unable to create content instance on: " + cont);
    }

}
