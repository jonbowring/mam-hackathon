package com.informatica.mam;

import java.util.HashMap;
import java.util.Objects;

import org.springframework.data.annotation.Id;

public class Config {
	
	// Declare Config related attributes
	@Id
	private String id;
	private HashMap<String, String> properties;
	
	/*
	 * ------------------
	 * Constructors
	 * ------------------
	 */
	
	Config() {}
	
	Config(String id) {
		this.id = id;
		this.properties = new HashMap<String, String>();
	}
	
	Config(String id, HashMap<String, String> properties) {
		this.id = id;
		this.properties = properties;
	}
	
	/*
	 * ------------------
	 * Getter Methods
	 * ------------------
	 */
	
	public String getId() {
		return this.id;
	}
	
	public HashMap<String, String> getProperties() {
		return this.properties;
	}
	
	public String getProperty(String key) {
		return this.properties.get(key);
	}
	
	/*
	 * ------------------
	 * Setter Methods
	 * ------------------
	 */
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void setProperties(HashMap<String, String> properties) {
		this.properties = properties;
	}
	
	public void setProperty(String key, String value) {
		this.properties.put(key, value);
	}
	
	/*
	 * ------------------
	 * Other Methods
	 * ------------------
	 */
	
	// This function is used to check if the object passed in is the same as the current instance
	@Override
	public boolean equals(Object o) {
		
		// If the object matches then return true
		if(this == o) {
			return true;
		}
		
		// If the passed object is not an instance of Config then return false
		if(!(o instanceof Config)) {
			return false;
		}
		
		// Cast the passed object to a config type
		Config config = (Config) o;
		
		// Check if the object properties match and then return the result
		return Objects.equals(this.id, config.id) &&
				Objects.equals(this.properties, config.properties)
				;
	}
	
	// This function generates a hash code for the current instance
	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.properties);
	}
	
	// This function returns a string representation of the current instance
	@Override
	public String toString() {
		return "Config {" + "id=" + this.id + ", properties='" + this.properties.toString() + "'}";
	}
	
}
