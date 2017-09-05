package it.svm.iot.core;
import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;
import org.json.JSONObject;

public class CoAPMonitor extends CoapServer
{
	private int coap_port;
	public String rn;
	public ArrayList<String> mote_addr;
	public ArrayList<String> vm_id;

	void addEndpoints()
	{
		for (InetAddress addr : EndpointManager.getEndpointManager().getNetworkInterfaces()) {
			if (((addr instanceof Inet4Address)) || (addr.isLoopbackAddress()))
			{
				InetSocketAddress bindToAddress = new InetSocketAddress(addr, coap_port);
				addEndpoint(new CoapEndpoint(bindToAddress));
			}
		}
	}

	public CoAPMonitor(String name, int port, ArrayList<String> mote_addr, 
			ArrayList<String> vm_id)
			throws SocketException
	{
		rn = name;
		this.mote_addr = mote_addr;
		this.vm_id = vm_id;
		coap_port = port;
		add(new Resource[] { new Monitor() });
	}

	class Monitor extends CoapResource
	{
		public Monitor()
		{
			super(rn);

			getAttributes().setTitle(rn);
		}
		/**
		 * 
		 * @param A string containing the uri resource
		 * @return The uri resource
		 */
		private String get_uri_res(String resource) {
			String uri = new String();
			
			if (resource.equals("tempdes")) {
				uri = "temp/des";
			} else if(resource.equals("tempsens")) {
				uri = "temp/sens";
			} else if(resource.equals("ProductAqty")) {
				uri = "ProductA/qty";
			}  else if(resource.equals("ProductAprice")) {
				uri = "ProductA/price";
			}  else if(resource.equals("ProductBqty")) {
				uri = "ProductB/qty";
			}  else if(resource.equals("ProductBprice")) {
				uri = "ProductB/price";
			} else
				uri = resource;	
			return uri;
		}
		
		/**
		 * 
		 * @param con String containing the JSON message from the controller
		 * @param res resource to be updated
		 * @return The message to be forwarded to the node
		 */
		private String get_message(String con, String res) {
			String message = new String();
			JSONObject root = new JSONObject(con);
			message = "value=";
			
			if (res.equals("alarm")) {
				String name_alarm = root.getString("alarm");
				message = message + (int)name_alarm.charAt(0);		
			} else if (res.equals("status")) {
				message  = message + root.getInt("status");
			} else if (res.equals("tempdes")) {
				message  = message + root.getDouble("tempdes");
			} else if (res.equals("tempsens")) {
				message  = message + root.getDouble("tempsens");
			} else if (res.equals("ProductAqty") || 
					res.equals("ProductBqty")) {
				message  = message + root.getInt("qty");
			} else if (res.equals("ProductAprice") || 
					res.equals("ProductBprice")) {
				message  = message + root.getDouble("price");
			} 
			
			return message;
		}
		
		/**
		 * It executes a PUT operation to the mote
		 * @param name_vm Vending machine name
		 * @param uri_res uri of the resource
		 * @param res name of the resource 
		 * @param reply String containing the JSON message from the controller
		 */
		
		private void put_to_mote(String name_vm, String uri_res, String res, String reply) {
			URI uri = null;
			CoapClient client;
			int i = 0;
			uri_res = get_uri_res(res);
			
			for (i = 0; i < vm_id.size(); i++) 
				if (name_vm.equals(vm_id.get(i)))
					break;
			try {
				uri = new URI("coap://[" + mote_addr.get(i)+"]:5683/" + uri_res);
			} catch (URISyntaxException e) {
				System.err.println("Invalid URI: " + e.getMessage());
				System.exit(-1);
			}
			
			client = new CoapClient(uri);
			
			if (!res.equals("loc")) {
				String message = get_message(reply, res);		
				System.out.println("New mote value: " + message);		
				CoapResponse response = client.put(message, 
					MediaTypeRegistry.TEXT_PLAIN);
				System.out.println(response.getResponseText());
			} else {
				JSONObject root = new JSONObject(reply);
				String message_lat = "lat=" + root.getDouble("lat");
				client.put(message_lat, 
						MediaTypeRegistry.TEXT_PLAIN);
				System.out.println("New mote value: " + message_lat);
				String message_lng = "lng=" + root.getDouble("lng");
				client.put(message_lng, 
						MediaTypeRegistry.TEXT_PLAIN);
				System.out.println("New mote value: " + message_lng);
			}
		}
		
		public void handlePOST(CoapExchange exchange)
		{	
			int i = 0;

			exchange.respond(ResponseCode.CREATED);
			byte[] content = exchange.getRequestPayload();
			String contentStr = new String(content);

			try {
				JSONObject root = new JSONObject(contentStr);
				JSONObject m2msgn = root.getJSONObject("m2m:sgn");
				JSONObject nev = m2msgn.getJSONObject("nev");
				JSONObject rep = nev.getJSONObject("rep");
				String reply = rep.getString("con");

				String uri_res = m2msgn.getString("sur");
				String []tmp = uri_res.split("/");
				String name_vm = null;
				uri_res = "";
				
				/* Retrieving the URI path for the resource */
				for (String sub_string: tmp) {
					if (i != (tmp.length - 1) && i > 2) {
						uri_res += "/";
						uri_res += sub_string;
					}
					i++;
					if (sub_string.contains("SVM_F") || 
							sub_string.contains("SVM_C"))
						name_vm = sub_string;
				}

				System.out.println("New content: " + reply);
				put_to_mote(name_vm, uri_res, tmp[tmp.length - 2], reply);

			}
			catch (Exception e) {
				// Doing nothing. The first notification message is ignored.
			}
		}
	}
}
