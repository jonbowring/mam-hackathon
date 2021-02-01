package com.informatica.mam;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/*
 * ------------------------------------------------------------------
 * Name: Media
 * Description: Model class for managing Media properties and methods
 * Author: Jonathon Bowring
 * ------------------------------------------------------------------
 */

@Entity
class Media {
	
	// Declare Media related attributes
	private @Id @GeneratedValue Long id;
	private String fileName;
	
	/*
	 * ------------------
	 * Constructors
	 * ------------------
	 */
	
	Media() {}
	
	Media(String fileName) {
		this.fileName = fileName;
	}
	
	/*
	 * ------------------
	 * Getter Methods
	 * ------------------
	 */
	
	public Long getId() {
		return this.id;
	}
	
	public String getFileName() {
		return this.fileName;
	}
	
	/*
	 * ------------------
	 * Setter Methods
	 * ------------------
	 */
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
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
		
		// If the passed object is not an instance of Media then return false
		if(!(o instanceof Media)) {
			return false;
		}
		
		// Cast the passed object to a media type
		Media media = (Media) o;
		
		// Check if the object properties match and then return the result
		return Objects.equals(this.id, media.id) &&
				Objects.equals(this.fileName, media.fileName);
	}
	
	// This function generates a hash code for the current instance
	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.fileName);
	}
	
	// This function returns a string representation of the current instance
	@Override
	public String toString() {
		return "Media {" + "id=" + this.id + ", fileName='" + this.fileName + "' }";
	}
	
	
}
