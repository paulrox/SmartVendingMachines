package it.svm.iot;

import java.util.ArrayList;
import org.json.*;

/**
 * Class representing a Smart Vending Machine
 * @author Paolo Sassi
 * @author Matteo Rotundo
 *
 */

public class VendingMachine {
	private int id;
	private String type;
	private float temp;
	private float temp_des;
	private float lat;
	private float lng;
	private Boolean intr_alarm;
	private Boolean fault_alarm;
	private Boolean statusOn;	/* true if the machine is on */
	private ArrayList<Product> products;
	
	/**
	 * Constructor for class VendingMachine
	 * @param i Id of the vending machine
	 * @param t Type of the vending machine ("Food" or "Coffee") 
	 */
	public VendingMachine(int id, String type) {
		if (id <= 0) {
			System.err.printf("Invalid vending machine id: %d.\n", id);
			System.exit(1);
		}
		if (type != "F" && type != "C") {
			System.err.printf("Invalid vending machine type: %s.\n", type);
			System.exit(1);
		}
		this.id = id;
		this.type = type;
		
		/* Use default values for the other parameters */
		if (type == "F") {
			/* Food machine */
			this.temp = (float) 15.0;
			this.temp_des = (float) 15.0;
		} else {
			/* Coffee machine */
			this.temp = (float) 60.0;
			this.temp_des = (float) 60.0;
		}
		this.lat = (float) 0.0;
		this.lng = (float) 0.0;
		this.intr_alarm = false;
		this.fault_alarm = false;
		this.statusOn = true;
		this.products = new ArrayList<Product>();
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
	 * Get the intrusion alarm status.
	 * @return Intrusion alarm. True if active, False otherwise
	 */
	public Boolean getIntrAlarm() {
		return intr_alarm;
	}
	
	/**
	 * Get the fault alarm status.
	 * @return Fault alarm. True if active, False otherwise
	 */
	public Boolean getFaultAlarm() {
		return fault_alarm;
	}
	
	/**
	 * Get the machine status.
	 * @return Machine status. True if the machine is on, False otherwise 
	 */
	public Boolean isStatusOn() {
		return statusOn;
	}
	
	/**
	 * Get the products registered in the machine.
	 * @return Reference to the ArrayList object containing the products
	 */
	public ArrayList<Product> getProducts() {
		return products;
	}
	
	/* Setter methods */
	
	/**
	 * Set the actual machine temperature.
	 * @param temp Temperature value in °C
	 */
	public void setTemp(float temp) {
		if (type == "Food") {
			if (temp >= Constants.MIN_FOOD_TEMP &&
			temp <= Constants.MAX_FOOD_TEMP) {
				this.temp = temp;
				return;
			}
		} else if (temp >= Constants.MIN_COFFEE_TEMP &&
				temp <= Constants.MAX_COFFEE_TEMP) {
			this.temp = temp;
			return;
		}
		
		System.err.printf("VM %i: Invalid actual temperature (%f).\n", id, temp);
		System.exit(1);
	}
	
	/**
	 * Set the desired machine temperature.
	 * @param temp_des Desired temperature in °C
	 */
	public void setTempAct(float temp_des) {
		if (type == "Food") {
			if (temp_des >= Constants.MIN_FOOD_TEMP &&
					temp_des <= Constants.MAX_FOOD_TEMP) {
				this.temp_des = temp_des;
				return;
			}
		} else if (temp_des >= Constants.MIN_COFFEE_TEMP &&
				temp_des <= Constants.MAX_COFFEE_TEMP) {
			this.temp_des = temp_des;
			return;
		}
		
		System.err.printf("VM %i: Invalid desired temperature (%f).\n", id, temp);
		System.exit(1);
	}
	
	/**
	 * Set the machine geographical position.
	 * @param pos JSONObject containing the machine latitude and longitude
	 */
	public void setPosition(JSONObject pos) {
		float lat = (float) pos.getDouble("lat");
		if (lat >= Constants.MIN_LAT && lat <= Constants.MAX_LAT) {
			this.lat = lat;
		} else {
			System.err.printf("VM %i: Invalid latitude value (%f).\n", id, lat);
			System.exit(1);
		}
		float lng = (float) pos.getDouble("lng");
		if (lng >= Constants.MIN_LNG && lat <= Constants.MAX_LNG) {
			this.lat = lng;
		} else {
			System.err.printf("VM %i: Invalid longitude value (%f).\n", id, lng);
			System.exit(1);
		}
	}
	
	/**
	 * Set the intrusion alarm status.
	 * @param intr_alarm Intrusion alarm. True if active, False otherwise
	 */
	public void setIntrAlarm(Boolean intr_alarm) {
		this.intr_alarm = intr_alarm;
	}
	
	/**
	 * Set the fault alarm status.
	 * @param fault_alarm Fault alarm. True if active, False otherwise
	 */
	public void setFaultAlarm(Boolean fault_alarm) {
		this.fault_alarm = fault_alarm;
	}
	
	/**
	 * Set the machine status.
	 * @param statusOn Machine status. True if the machine is on, False otherwise 
	 */
	public void setStatusOn(Boolean statusOn) {
		this.statusOn = statusOn;
	}
	

}
