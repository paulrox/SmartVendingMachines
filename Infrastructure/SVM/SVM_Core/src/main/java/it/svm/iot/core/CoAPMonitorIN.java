package it.svm.iot.core;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;
import org.json.JSONObject;

import it.svm.iot.core.Mca;

public class CoAPMonitorIN extends CoapServer
{
	private int coap_port;
	public String rn;
	public ArrayList<VendingMachine> vms;
	
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

	public CoAPMonitorIN(String name, Mca mca, int port, String cse, ArrayList<VendingMachine> vms)
			throws SocketException
	{
		rn = name;
		this.vms = vms;
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
				
				/* Retrieving the URI path for the resource */
				for (String sub_string: tmp) {
					if (sub_string.contains("SVM_F") || 
							sub_string.contains("SVM_C"))
						name_vm = sub_string;
				}

				System.out.println("New content: " + reply);
				/* Updating vm class */
				for (i = 0; i < vms.size(); i++) {
					if (vms.get(i).name.equals(name_vm)) {
						vms.get(i).mutex.semWait();
						vms.get(i).set_vm_res(reply, 
								tmp[tmp.length - 1], true, false);
						vms.get(i).mutex.semSignal();
					}
				}
				
			}
			catch (Exception e) {
				// Doing nothing. The first notification message is ignored.
			}
		}
	}
}
