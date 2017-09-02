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
	private Mca mca;
	private String cse;
	public String rn;
	public ArrayList<String> mote_addr;

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

	public CoAPMonitor(String name, Mca mca, int port, String cse, ArrayList<String> mote_addr)
			throws SocketException
	{
		rn = name;
		this.mote_addr = mote_addr;
		this.mca = mca;
		coap_port = port;
		this.cse = cse;
		add(new Resource[] { new Monitor() });
	}

	class Monitor extends CoapResource
	{
		public Monitor()
		{
			super(rn);

			getAttributes().setTitle(rn);
		}
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
		
		private String get_message(String con, String res) {
			String message = new String();
			JSONObject root = new JSONObject(con);
			message = "value=";
			
			if (res.equals("alarm")) {
				message = message + root.getString("alarm");
			} else if (res.equals("status")) {
				message  = message + root.getInt("status");
			} else if (res.equals("loc")) {
				
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
		
		public void handlePOST(CoapExchange exchange)
		{	
			int i = 0;
			int id;
			URI uri = null;
			CoapClient client;
			
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
				String name_id; 
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
				name_id = new String(name_vm.substring((name_vm.length() - 1)));
				id = Integer.parseInt(name_id);
				
				mca.createContentInstance(cse + uri_res,
						reply);
				System.out.println("Created new content instance:\n"
						+ "res: " + uri_res);
				System.out.println("con: " + reply);
				String message = get_message(reply, tmp[tmp.length - 2]);
				uri_res = get_uri_res(tmp[tmp.length - 2]);
				try {
					uri = new URI("coap://[" + mote_addr.get(id - 2)+"]:5683/" + uri_res);
				} catch (URISyntaxException e) {
					System.err.println("Invalid URI: " + e.getMessage());
					System.exit(-1);
				}
				System.out.println("uri of the modified mote " + 
				"coap://[" + mote_addr.get(id - 2)+"]:5683/" + uri_res);
				System.out.println("message to be put in the mote: " + message);
				client = new CoapClient(uri);
				CoapResponse response = client.put(message, 
						MediaTypeRegistry.TEXT_PLAIN);
				System.out.println(response.getResponseText());
		
			}
			catch (Exception e) {
				// Doing nothing. The first notification message is ignored.
			}
		}
	}
}
