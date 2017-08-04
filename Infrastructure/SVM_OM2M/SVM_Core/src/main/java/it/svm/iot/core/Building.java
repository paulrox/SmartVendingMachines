package it.svm.iot.core;

import org.json.JSONObject;

/**
 * Class representing a building (set of nearby vending machines).
 * @author Paolo Sassi
 * @author Matteo Rotundo
 *
 */
public class Building {
	private int id;
	private String name;
	private float lat;
	private float lng;
	private String last_visit;
	
	/**
	 * Constructor for class building.
	 * @param id ID of the building
	 * @param name Name of the building
	 * @param lat Latitude of the building
	 * @param lng Longitude of the building
	 */
	public Building(int id, String name, float lat, float lng) {
		if (lat >= Constants.MIN_LAT && lat <= Constants.MAX_LAT &&
				lng >= Constants.MIN_LNG && lng <= Constants.MAX_LNG) {
			this.id = id;
			this.name = name;
			this.lat = lat;
			this.lng = lng;
			this.last_visit = "";
		} else {
			System.err.printf("Invalid building position.\n");
			System.exit(1);
		}
	}
	
	/* Getter methods */
	
	/**
	 * Get the building id.
	 * @return Building id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Get the building name.
	 * @return Building name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Get the building geographical position.
	 * @return JSONObject containing the latitude and longitude
	 */
	public JSONObject getPosition() {
		JSONObject pos = new JSONObject();
		pos.put("lat", lat);
		pos.put("lng", lng);
		
		return pos;
	}
	
	/**
	 * Get the last time the building has been visited by a technician.
	 * @return String containing the date and time of last visit
	 */
	public String getLastVisit() {
		return last_visit;
	}
	
	/* Setter methods */
	
	/**
	 * Set the last time the building has been visited by a technician.
	 * @param last_visit Date and time of the last visit
	 */
	public void setLastVisit(String last_visit) {
		this.last_visit = last_visit;
	}
}
