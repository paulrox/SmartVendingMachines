package it.svm.iot.in;

import java.io.IOException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.JSONObject;

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
        try {
            session.getRemote().sendString("Hello Webbrowser");
        } catch (IOException e) {
            e.printStackTrace();
            
        }
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        JSONObject root = new JSONObject(message);
        String cont;
        
        if (root.getString("type").equals("R") && is_first_time) {
        	is_first_time = false;
        	/* Initial Read request */
        	cont = "{'type':'OK', 'content' :[";
        	for (int i = 0; i < ADN.vms.size(); i++) {
        		cont = cont + ADN.vms.get(i).get_vm_content();
        		if (i < (ADN.vms.size() - 1))
        			cont = cont + ",";
        	}
        	cont = cont + "]}";
        	
        	try {
        		current_session.getRemote().sendString(cont);
        	} catch (IOException e) {
        		e.printStackTrace();
        	}


        } else if (root.getString("type").equals("R") && !is_first_time) {
        	/* Read request */
        } else if (root.getString("type").equals("W")) {
        	/* Write request */
        } else {
        	System.out.println("Unknown request!");
        }
    }
}