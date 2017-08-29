package it.svm.iot.core;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.Request;
import org.json.JSONException;
import org.json.JSONObject;
import static it.svm.iot.core.Constants.DEBUG;

/**
 * API for handling the interactions between an AE and a CSE 
 * (Mca reference point) implemented as singleton.
 * @author Paolo Sassi
 * @author Matteo Rotundo
 *
 */
public final class Mca {
	private static Mca instance = null;
	
	/**
	 * Private constructor for the singleton pattern.
	 */
	private Mca() {}
	
	/**
	 * Get the only Mca instance.
	 * @return The Mca instance
	 */
	public static Mca getInstance() {
		if(instance == null) {
			instance = new Mca();
		}
		return instance;
	}
	
	/**
	 * Creates a new AE on the specified CSE. The function is private in
	 * order to allow just one.
	 * @param cse URI of the CSE
	 * @param rn Name of the AE to be created
	 * @return AE object containing the information of the created AE
	 */
	public AE createAE(String cse, String rn){
		AE ae = AE.getInstance();
		URI uri = null;
		try {
			uri = new URI(cse);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CoapClient client = new CoapClient(uri);
		Request req = Request.newPost();
		req.getOptions().addOption(new Option(267, 2));
		req.getOptions().addOption(new Option(256, "admin:admin"));
		req.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		JSONObject obj = new JSONObject();
		obj.put("api",rn.concat("-ID"));
		obj.put("rr","true");
		obj.put("rn", rn);
		JSONObject root = new JSONObject();
		root.put("m2m:ae", obj);
		String body = root.toString();
		if (DEBUG) 
			System.out.println(body);
		req.setPayload(body);
		CoapResponse responseBody = client.advanced(req);
		if (responseBody == null) {
			System.err.printf("MCA: Error in createAE(), no "
					+ "response from %s\n", cse);
			System.exit(-1);
		}
		String response = new String(responseBody.getPayload());
		if (DEBUG)
			System.out.println(response);
		JSONObject resp = new JSONObject(response);
		JSONObject container = (JSONObject) resp.get("m2m:ae");
		ae.setRn((String) container.get("rn"));
		ae.setTy((Integer) container.get("ty"));
		ae.setRi((String) container.get("ri"));
		ae.setPi((String) container.get("pi"));
		ae.setCt((String) container.get("ct"));
		ae.setLt((String) container.get("lt"));

		return ae;
	}

	/**
	 * Creates a new Container on the specified CSE.
	 * @param cse URI of the CSE
	 * @param rn Name of the Container to be created
	 * @return Container object containing the information of the 
	 * 		   created Container
	 */
	public Container createContainer(String cse, String rn){
		Container container = new Container();

		URI uri = null;
		try {
			uri = new URI(cse);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CoapClient client = new CoapClient(uri);
		Request req = Request.newPost();
		req.getOptions().addOption(new Option(267, 3));
		req.getOptions().addOption(new Option(256, "admin:admin"));
		req.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		JSONObject obj = new JSONObject();
		obj.put("rn", rn);
		JSONObject root = new JSONObject();
		root.put("m2m:cnt", obj);
		String body = root.toString();
		if (DEBUG)
			System.out.println(body);
		req.setPayload(body);
		CoapResponse responseBody = client.advanced(req);
		if (responseBody == null) {
			System.err.printf("MCA: Error in createContainer(), no "
					+ "response from %s\n", cse);
			System.exit(-1);
		}
		String response = new String(responseBody.getPayload());
		if (DEBUG)
			System.out.println(response);
		JSONObject resp = new JSONObject(response);
		JSONObject cont = (JSONObject) resp.get("m2m:cnt");
		container.setRn((String) cont.get("rn"));
		container.setTy((Integer) cont.get("ty"));
		container.setRi((String) cont.get("ri"));
		container.setPi((String) cont.get("pi"));
		container.setCt((String) cont.get("ct"));
		container.setLt((String) cont.get("lt"));
		container.setSt((Integer) cont.get("st"));
		container.setOl((String) cont.get("ol"));
		container.setLa((String) cont.get("la"));

		return container;
	}
	
	/**
	 * Creates a new contentInstance.
	 * @param cse URI of the CSE
	 * @param val Value of the contentInstance
	 */
	public void createContentInstance(String cse, String val){
		URI uri = null;
		try {
			uri = new URI(cse);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CoapClient client = new CoapClient(uri);
		Request req = Request.newPost();
		req.getOptions().addOption(new Option(267, 4));
		req.getOptions().addOption(new Option(256, "admin:admin"));
		req.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		JSONObject content = new JSONObject();
		content.put("cnf","SenML data");
		content.put("con",val);
		JSONObject root = new JSONObject();
		root.put("m2m:cin", content);
		String body = root.toString();
		if (DEBUG)
			System.out.println(body);
		req.setPayload(body);
		CoapResponse responseBody = client.advanced(req);
		if (responseBody == null) {
			System.err.printf("MCA: Error in createContentInstance(), no "
					+ "response from %s\n", cse);
			System.exit(-1);
		}
		String response = new String(responseBody.getPayload());
		if (DEBUG)
			System.out.println(response);
	}
	
	/**
	 * Discover resources on a remote CSE.
	 * @param mn_cse URI of the remote CSE
	 * @return String containing the list of the discovered resources
	 */
	public String discoverResources(String cse, String query){
		/* Append the query string */
		String path = null;
		String uri = cse + query;
		CoapClient client = new CoapClient(uri);
		Request req = Request.newGet();
		req.getOptions().addOption(new Option(256, "admin:admin"));
		req.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		CoapResponse responseBody = client.advanced(req);
		if (responseBody == null) {
			System.err.printf("MCA: Error in discoverResources(), no "
					+ "response from %s\n", uri);
			System.exit(-1);
		}
		String response = new String(responseBody.getPayload());
		try {
			JSONObject content = new JSONObject(response);
			path = content.getString("m2m:uril");
		} catch (JSONException e) { 
			/* The remote CSE doesn't contain any resource which satisfies
			 * the query.
			 */
			if (DEBUG)
				System.out.println("No resources on remote CSE");
			return null;
		}
		if (DEBUG)
			System.out.printf("Discovered resources: %s\n", path);
		return path;
	}
	
	/**
	 * Creates a subscription for retrieving updated content instances from a
	 * container.
	 * @param cse URI of the container
	 * @param notificationUrl URI of the CoAP server
	 */
	public void createSubscription(String cse, String notificationUrl, String resource_name){
		CoapClient client = new CoapClient(cse);
		Request req = Request.newPost();
		req.getOptions().addOption(new Option(267, 23));
		req.getOptions().addOption(new Option(256, "admin:admin"));
		req.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		req.getOptions().setAccept(MediaTypeRegistry.APPLICATION_JSON);
		JSONObject content = new JSONObject();
		content.put("rn", resource_name);
		content.put("nu", notificationUrl);
		content.put("nct", 2);
		JSONObject root = new JSONObject();
		root.put("m2m:sub", content);
		String body = root.toString();
		req.setPayload(body);
		CoapResponse responseBody = client.advanced(req);
		if (responseBody == null) {
			System.err.printf("MCA: Error in createSubscription(), no "
					+ "response from %s\n", cse);
			System.exit(-1);
		}
		String response = new String(responseBody.getPayload());
		if (DEBUG)
			System.out.println(response);
	}

	
}
