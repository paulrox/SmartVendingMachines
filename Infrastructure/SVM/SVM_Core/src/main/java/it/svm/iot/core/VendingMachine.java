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
	public String name;
	public String type;
	public float temp_sens; 
	public float temp_des;
	public float lat;
	public float lng;
	public String alarm;
	public int statusOn;	/* true if the machine is on */
	public ArrayList<Product> products;
	public SimpleSem mutex;
	public Boolean first_time;
	/* Monitoring only the observable resources. 
	 * These var suggest if there is an update from the last read. */
	public Boolean is_new_temp_sens;
	public Boolean is_new_alarm;
	public Boolean is_new_statusOn;
	/* The var is set if there is at least a resource update */
	public Boolean is_new;
	
	/**
	 * Constructor for class VendingMachine
	 *  
	 */
	
	public VendingMachine() {		
		this.id = 0;
		this.type = null;
		this.temp_sens = 0;
		this.temp_des = 0;
		this.lat = 0;
		this.lng = 0;
		this.alarm = "N";
		this.statusOn = 0;
		this.is_new_temp_sens = false;
		this.is_new_alarm = false;
		this.is_new_statusOn = false;
		this.is_new = false;
		this.products = new ArrayList<Product>();
		this.products.add(new Product("ProductA", this));
		this.products.add(new Product("ProductB", this));
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
		return temp_sens;
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
		this.temp_sens = temp;
		this.is_new_temp_sens = true;
		this.is_new = true;
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
		float longitude = (float) pos.getDouble("lng");
		if (lng >= Constants.MIN_LNG && lat <= Constants.MAX_LNG) {
			this.lng = longitude;
		} else {
			System.err.printf("VM %i: Invalid longitude value (%f).\n", id, lng);
			System.exit(1);
		}
	}
	
	/**
	 * Set the alarm status. If update is true it was a set that
	 * must notify the client web app.
	 * @param new_alarm New status alarm
	 */
	
	public void setAlarm(String new_alarm, Boolean update) {
		this.alarm = new_alarm;
		if (update) {
			this.is_new_alarm = true;
			this.is_new = true;
		}
	}
	

	/**
	 * Set the machine status. If update is true it was a set that
	 * must notify the client web app.
	 * @param statusOn Machine status. True if the machine is on, False otherwise 
	 */
	
	public void setStatusOn(int statusOn, Boolean update) {
		this.statusOn = statusOn;
		if (update) {
			this.is_new_statusOn = true;
			this.is_new = true;
		}
	}
	
	/**
	 *  Sets an id for the vm and if the type is already set 
	 *  also the name
	 * @param new_id the new id to set
	 */
	
	public void setId(int new_id) {
		this.id = new_id;
		if (type != null)
			name = "SVM_" + type + id;
	}
	
	/**
	 *  Sets the type for the vending machine (Food or Coffee)
	 *  and if the id is already set also the name
	 * @param new_type VM type value to be set
	 */
	public void setType(String new_type) {
		this.type = new_type;
		if (id > 0)
			name = "SVM_" + type + id;
	}
	
	/**
	 * 
	 * @return JsonObjcet containing all the values of the vm
	 */
	
	public JSONObject get_json_vm_content() {
		int indexA, indexB;
		JSONObject obj = new JSONObject();
		JSONObject productA = new JSONObject();
		JSONObject productB = new JSONObject();
		JSONArray array = new JSONArray();
		
		indexA = getProductIndex("ProductA");
		indexB = getProductIndex("ProductB");
		
		name = "SVM_" + type + id;
		obj.put("id", name);
		obj.put("status", statusOn);
		obj.put("tempsens",temp_sens);
		obj.put("tempdes", temp_des);
		obj.put("lat", lat);
		obj.put("lng", lng);
		obj.put("alarm", alarm);
		productA.put("id", "ProductA");
		productA.put("qty",  products.get(indexA).getQty());
		productA.put("price",  products.get(indexA).getPrice());
		productB.put("id", "ProductB");
		productB.put("qty",  products.get(indexB).getQty());
		productB.put("price",  products.get(indexB).getPrice());
		array.put(productA);
		array.put(productB);
		obj.put("products", array);
		
		is_new_alarm = false;
		is_new = false;
		is_new_statusOn = false;
		is_new_temp_sens = false;
		products.get(indexA).is_new_qty = false;
		products.get(indexA).is_new_price = false;
		products.get(indexB).is_new_qty = false;
		products.get(indexB).is_new_price = false;
		
		return obj;
	}
	
	/**
	 * 
	 * @return The JSONobject of the updated values
	 */

	public JSONObject get_json_update_content() {
		int indexA, indexB;
		JSONObject obj = new JSONObject();
		JSONObject productA = new JSONObject();
		JSONObject productB = new JSONObject();
		JSONArray array = new JSONArray();
		
		indexA = getProductIndex("ProductA");
		indexB = getProductIndex("ProductB");
	
		obj.put("id", "SVM_" + type + id);
		if (is_new_temp_sens) {
			obj.put("tempsens", temp_sens);
			is_new_temp_sens = false;
		}
		if (is_new_alarm) {	
			obj.put("alarm", alarm);
			is_new_alarm = false;
		}
		if (is_new_statusOn) {
			obj.put("status", statusOn);
			is_new_statusOn = false;
			
		}
		if (products.get(indexA).is_new_qty || 
				products.get(indexA).is_new_price ) {
			productA.put("id", "ProductA");
			if (products.get(indexA).is_new_qty) {
				productA.put("qty",  products.get(indexA).getQty());
				products.get(indexA).is_new_qty = false;
			}
			if (products.get(indexA).is_new_price) {
				productA.put("price",  products.get(indexA).getPrice());
				products.get(indexA).is_new_price = false;
			}
		}
		if (products.get(indexB).is_new_qty || 
				products.get(indexB).is_new_price ) {
			productB.put("id", "ProductB");
			if (products.get(indexB).is_new_qty) {
				productB.put("qty",  products.get(indexB).getQty());
				products.get(indexB).is_new_qty = false;
			}
			if (products.get(indexB).is_new_price) {
				productB.put("price",  products.get(indexB).getPrice());
				products.get(indexB).is_new_price = false;
			}
		}
		is_new = false;
		
		array.put(productA);
		array.put(productB);
		
		obj.put("products", array);
		
		return obj;
	}
	
	/**
	 * Updates the resource res of the vending machine with the value in content.
	 * If update is true it was a set that must notify the client web app.
	 * @param content Content of the instance
	 * @param res resource
	 */
	
	public void set_vm_res(String content, String res, Boolean update, Boolean first_time) {

		int index;
		JSONObject root = new JSONObject(content);
		
		if (res.equals("alarm")) {
			setAlarm(root.getString("alarm"), update);
		} else if (res.equals("status")) {
			setStatusOn(root.getInt("status"), update);
			if (first_time)
				setType(root.getString("type"));
		} else if (res.equals("loc")) {
			setPosition(root);
		} else if (res.equals("tempdes")) {
			setTempAct((float)root.getDouble("tempdes"));
		} else if (res.equals("tempsens")) {
			setTemp((float)root.getDouble("tempsens"));
		} else if (res.equals("ProductAqty")) {
			index = getProductIndex("ProductA");
			if (index >= 0) {
				products.get(index).setQty((root.getInt("qty")), 
						update);
			}
		} else if (res.equals("ProductBqty")) {
			index = getProductIndex("ProductB");
			if (index >= 0) {
				products.get(index).setQty((root.getInt("qty")), 
						update);
			}
		} else if (res.equals("ProductAprice")) {
			index = getProductIndex("ProductA");
			if (index >= 0) {
				products.get(index).setPrice((root.getDouble("price")), 
						update);
			}
		} else if (res.equals("ProductBprice")) {
			index = getProductIndex("ProductB");
			if (index > 0) {
				products.get(index).setPrice((root.getDouble("price")),
						update);
			}
		}
	}
}
