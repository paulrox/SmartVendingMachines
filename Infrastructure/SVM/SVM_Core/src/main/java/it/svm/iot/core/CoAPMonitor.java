package it.svm.iot.core;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;
import org.json.JSONObject;

public class CoAPMonitor extends CoapServer
{
  private static final int COAP_PORT = 5685;
  private static Mca IN_Mca;
  public String rn;
  
  void addEndpoints()
  {
    for (InetAddress addr : EndpointManager.getEndpointManager().getNetworkInterfaces()) {
      if (((addr instanceof Inet4Address)) || (addr.isLoopbackAddress()))
      {
        InetSocketAddress bindToAddress = new InetSocketAddress(addr, COAP_PORT);
        addEndpoint(new CoapEndpoint(bindToAddress));
      }
    }
  }
  
  public CoAPMonitor(String name, Mca mca) throws SocketException
  {
	rn = name;
	IN_Mca = mca;
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
		uri_res = "";
		/* Retrieving the URI path for the resource */
		for (String sub_string: tmp) {
			if (i != (tmp.length - 1) && i > 2) {
				uri_res += "/";
				uri_res += sub_string;
			}
			i++;
		}
		IN_Mca.createContentInstance(Constants.IN_CSE_URI + uri_res,
				reply);
		
		System.out.println("Created new content instance:\n"
				+ "res: " + uri_res);
		System.out.println("con: " + reply);
        }
        catch (Exception e) {
			// Doing nothing. The first notification message is ignored.
        }
    }
  }
}
