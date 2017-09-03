package it.svm.iot.in;

import java.io.IOException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.JSONArray;
import org.json.JSONObject;

import it.svm.iot.core.Constants;

@WebSocket
public class MyWebSocketHandler {
	private Session current_session;
	private Boolean is_first_time = true;
	
    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.println("Close: statusCode=" + statusCode + ", reason=" + reason);
    }

    @OnWebSocketError
    public void onError(Throwable t) {
        System.out.println("Error: " + t.getMessage());
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("Connect: " + session.getRemoteAddress().getAddress());
        current_session = session;
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        JSONObject root = new JSONObject(message);

        if (root.getString("type").equals("R") && is_first_time) {
        	is_first_time = false;
        	/* Initial Read request */
    		JSONObject response = new JSONObject();
    		JSONArray content = new JSONArray();
    		response.put("type", "OK");
    		
        	for (int i = 0; i < ADN.vms.size(); i++) 
        		content.put(ADN.vms.get(i).get_json_vm_content());
        	response.put("content", content);
        	
        	try {
        		System.out.println(response.toString());
        		current_session.getRemote().sendString(response.toString());
        	} catch (IOException e) {
        		e.printStackTrace();
        	}

        } else if (root.getString("type").equals("R") && !is_first_time) {
        	/* Read request */
        	JSONObject response = new JSONObject();
    		JSONArray content = new JSONArray();
    		/* True if there is something new in at least one vm */
    		Boolean is_update = false;
    		
        	for (int i = 0; i < ADN.vms.size(); i++) { 
        		ADN.vms.get(i).mutex.semWait();
        		if (ADN.vms.get(i).is_new) {
        			/* Only if there is an update */
        			content.put(ADN.vms.get(i).get_json_update_content());
        			is_update = true;
        		}
        		ADN.vms.get(i).mutex.semSignal();;
        	}	
        	if (is_update)
        		response.put("type", "OK");
        	else
        		response.put("type", "NO");
        	
        	response.put("content", content);
        	if (is_update)
        		System.out.println(response.toString());
        	
        	try {
        		current_session.getRemote().sendString(response.toString());
        	} catch (IOException e) {
        		e.printStackTrace();
        	}
        	
        } else if (root.getString("type").equals("W")) {
        	/* Write request */
        	int i;
        	
        	System.out.println(message);
        	for (i = 0; i < ADN.vms.size(); i++) 
        		if (ADN.vms.get(i).name.equals(root.getString("id")))
        				break;
        	if (i < ADN.vms.size()) {
        		/* Vending machine found */
        		System.out.println("VM found");
        		ADN.vms.get(i).set_vm_res(root.getJSONObject("content").toString(), 
        				root.getString("resource"), false, false);
        		String parent_cont = Constants.IN_CSE_URI + "/" + 
						ADN.IN_AE_Controller.getRn() + "/" + root.getString("id");
				ADN.IN_Mca.createContentInstance(parent_cont + "/" +
						root.getString("resource"), 
						root.getJSONObject("content").toString());
				System.out.println("Content Instance created");
        	}
        } else {
        	System.out.println("Unknown request!");
        }
    }
}
