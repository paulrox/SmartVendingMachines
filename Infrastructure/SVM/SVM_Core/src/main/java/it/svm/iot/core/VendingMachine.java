package it.svm.iot.core;

import java.util.ArrayList;
import org.json.*;

/**
 * Class representing a Smart Vending Machine
 * @author Paolo Sassi
 * @author Matteo Rotundo
 *
 */

public class VendingMachine {
	public int id;
	public String type;
	public float temp;
	public float temp_des;
	public float lat;
	public float lng;
	public String alarm;
	public int statusOn;	/* true if the machine is on */
	public ArrayList<Product> products;
	public SimpleSem mutex;
	
	/**
	 * Constructor for class VendingMachine
	 *  
	 */
	public VendingMachine() {		
		this.id = 0;
		this.type = "";
		this.temp = 0;
		this.temp_des = 0;
		this.lat = 0;
		this.lng = 0;
		this.alarm = "N";
		this.statusOn = 0;
		this.products = new ArrayList<Product>();
		this.products.add(new Product("ProductA"));
		this.products.add(new Product("ProductB"));
		mutex = new SimpleSem(true);
	}
	
	/* Getter methods */
	
	/**
	 * Get the machine ID.
	 * @return Machine identifier
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Get the machine type.
	 * @return Machine type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Get the actual machine temperature.
	 * @return Temperature value in °C
	 */
	public float getTemp() {
		return temp;
	}
	
	/**
	 * Get the desired machine temperature.
	 * @return Desired temperature in °C
	 */
	public float getTempAct() {
		return temp_des;
	}
	
	/**
	 * Get the machine geographical position.
	 * @return JSONObject containing the latitude and longitude
	 */
	public JSONObject getPosition() {
		JSONObject ret = new JSONObject();
		ret.put("lat", lat);
		ret.put("lng", lng);
		
		return ret;
	}
	
	/**
	 * Get the alarm status.
	 * @return	the type of the alarm:
	 * 		 	alarm = 'N'; No Alarm
	 *			alarm = 'I'; Intrusion
	 * 			alarm = 'F'; Fault
	 */
	public String getAlarm() {
		return alarm;
	}
	
	
	/**
	 * Get the machine status.
	 * @return Machine status. 1 if the machine is on, 0  otherwise 
	 */
	public int isStatusOn() {
		return statusOn;
	}
	
	/**
	 * Get the products registered in the machine.
	 * @return Reference to the ArrayList object containing the products
	 */
	public ArrayList<Product> getProducts() {
		return products;
	}
	/**
	 * It looks for a product in the vm
	 * @param name product name
	 * @return the index or -1 if missing
	 */
	public int getProductIndex(String name) {
		
		int cnt = 0, index = -1;
		
		for (Product prod: products) {
			if (prod.getName().equals(name))
				index = cnt;
			cnt++;
		}
		return index;
	}
	/* Setter methods */
	
	/**
	 * Set the actual machine temperature.
	 * @param temp Temperature value in °C
	 */
	public void setTemp(float temp) {
		this.temp = temp;
	}
	
	/**
	 * Set the desired machine temperature.
	 * @param temp_des Desired temperature in °C
	 */
	public void setTempAct(float temp_des) {
		this.temp_des = temp_des;
	}
	
	/**
	 * Set the machine geographical position.
	 * @param pos JSONObject containing the machine latitude and longitude
	 */
	public void setPosition(JSONObject pos) {
		float latitude = (float) pos.getDouble("lat");
		if (lat >= Constants.MIN_LAT && lat <= Constants.MAX_LAT) {
			this.lat = latitude;
		} else {
			System.err.printf("VM %i: Invalid latitude value (%f).\n", id, lat);
			System.exit(1);
		}
		float longitude = (float) pos.getDouble("long");
		if (lng >= Constants.MIN_LNG && lat <= Constants.MAX_LNG) {
			this.lng = longitude;
		} else {
			System.err.printf("VM %i: Invalid longitude value (%f).\n", id, lng);
			System.exit(1);
		}
	}
	
	/**
	 * Set the alarm status.
	 * @param new_alarm New status alarm
	 */
	public void setAlarm(String new_alarm) {
		this.alarm = new_alarm;
	}
	

	/**
	 * Set the machine status.
	 * @param statusOn Machine status. True if the machine is on, False otherwise 
	 */
	public void setStatusOn(int statusOn) {
		this.statusOn = statusOn;
	}
	
	/**
	 *  Sets an id for the vm
	 * @param new_id the new id to set
	 */
	public void setId(int new_id) {
		this.id = new_id;
	}
	
	/**
	 *  Sets the type for the vending machine (Food or Coffee)
	 * @param new_type VM type value to be set
	 */
	public void setType(String new_type) {

		this.type = new_type;
	}
	
	
	public void print() {
		
		int indexA, indexB;
		
		indexA = getProductIndex("ProductA");
		indexB = getProductIndex("ProductB");
		
		System.out.println("");
		System.out.println("/**************************/");
		System.out.println("Vending machine ID: " + id);
		System.out.println("Type: " + type);
		System.out.println("Status: " + statusOn);
		System.out.println("Sensed temperature: " + temp);
		System.out.println("Desired temperature: " + temp_des);
		System.out.println("Latitude: " + lat);
		System.out.println("Longitude: " + lng);
		System.out.println("Alarm: " + alarm);
		System.out.println("ProductA qty: " + products.get(indexA).getQty());
		System.out.println("ProductA price: " + products.get(indexA).getPrice());
		System.out.println("ProductB qty: " + products.get(indexB).getQty());
		System.out.println("ProductB price: " + products.get(indexB).getPrice());
		System.out.println("/**************************/");
		System.out.println("");
	}
	public String get_vm_content() {
		String content = new String("{'id':'SVM_" + type + id + "',");
		int indexA, indexB;
		
		indexA = getProductIndex("ProductA");
		indexB = getProductIndex("ProductB");
		
		content = content + "'status':'" + statusOn+"',";
		content = content + "'tempsens':'" + temp +"',";
		content = content + "'tempdes':'" + temp_des +"',";
		content = content + "'lat':'" + lat +"',";
		content = content + "'long':'" + lng +"',";
		content = content + "'alarm':'" + alarm +"',";
		content = content + "'products':[{'id':'ProductA','qty':'"+ products.get(indexA).getQty() + 
				"','price':'" + products.get(indexA).getPrice() +"'},";
		content = content + "{'id':'ProductB,'qty':'"+ products.get(indexB).getQty() + 
			"','price':'" + products.get(indexB).getPrice() +"'}]";
		
		return content;
	}
}
