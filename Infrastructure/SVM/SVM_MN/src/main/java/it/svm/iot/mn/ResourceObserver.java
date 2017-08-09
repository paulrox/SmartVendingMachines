package it.svm.iot.mn;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import it.svm.iot.core.*;
import static it.svm.iot.core.Constants.DEBUG;

/**
 * This class implements the CoapHandler interface in order to handle
 * asynchronous requests from the CoAP servers.
 * @author Paolo Sassi
 * @author Matteo Rotundo
 *
 */
public class ResourceObserver implements CoapHandler {
	
	private SimpleSem cin_ready;
	private String content;
	
	/**
	 * ResouceObserver public constructor.
	 * @param cont String containing the parent container on the MN
	 */
	public ResourceObserver(SimpleSem cin_ready) {
		this.cin_ready = cin_ready;
		this.content = "";
	}
	
	/**
	 * Get the content received last.
	 * @return String object containing the content
	 */
	public String getContent() {
		return content;
	}
	
	@Override
    public void onLoad(CoapResponse response) {
		content = response.getResponseText();
		if (DEBUG)
			System.out.printf("Received new content %s\n", content);
		/* Signals to the ResourceMonitor the arrival of a new content */
		cin_ready.semSignal();
    }

    @Override
    public void onError() {
        System.err.printf("Unable to receive new content\n",
        				Thread.currentThread().getId());
    }

}
