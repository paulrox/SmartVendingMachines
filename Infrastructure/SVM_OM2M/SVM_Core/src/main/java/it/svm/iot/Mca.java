package it.svm.iot;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.Request;
import org.json.JSONObject;

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
		System.out.println(body);
		req.setPayload(body);
		CoapResponse responseBody = client.advanced(req);
		String response = new String(responseBody.getPayload());
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
		System.out.println(body);
		req.setPayload(body);
		CoapResponse responseBody = client.advanced(req);

		String response = new String(responseBody.getPayload());
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
		content.put("cnf","message");
		content.put("con",val);
		JSONObject root = new JSONObject();
		root.put("m2m:cin", content);
		String body = root.toString();
		System.out.println(body);
		req.setPayload(body);
		CoapResponse responseBody = client.advanced(req);

		String response = new String(responseBody.getPayload());
		System.out.println(response);
	}
}
