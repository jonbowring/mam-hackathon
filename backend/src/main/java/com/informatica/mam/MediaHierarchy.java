package com.informatica.mam;

import java.util.Objects;

import org.springframework.data.annotation.Id;
import java.util.List;
import java.util.ArrayList;

public class MediaHierarchy {

	// Declare Media related attributes
	@Id
	private String id;
	private String hierarchyCode;
	private String hierarchyName;
	private List<MediaHierarchy> children;
	

	 MediaHierarchy() {
		// TODO Auto-generated constructor stub
	}
	
	public MediaHierarchy(String hierarchyCode) {
	
		this.hierarchyCode = hierarchyCode;
		this.children = new ArrayList<MediaHierarchy>();
	}
	
	public MediaHierarchy(String hierarchyCode, List<MediaHierarchy> children) {
		
		this.hierarchyCode = hierarchyCode;
		this.children = children;
	}
	/*
	 * ------------------
	 * Getter Methods
	 * ------------------
	 */

	public String getHierarchyCode() {
		return hierarchyCode;
	}

	public String getId() {
		return id;
	}

	
	public List<MediaHierarchy> getChildren() {
		return children;
	}
	
	public String getHierarchyName() {
		return hierarchyName;
	}
	
	/* Setter Methods*/

	public void setChildren(List<MediaHierarchy> children) {
		this.children = children;
	}

	public void setHierarchyCode(String hierarchyCode) {
		this.hierarchyCode = hierarchyCode;
	}
	
	

	public void setHierarchyName(String hierarchyName) {
		this.hierarchyName = hierarchyName;
	}
	public void setId(String id) {
		this.id = id;
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
		if(!(o instanceof MediaHierarchy)) {
			return false;
		}
		
		// Cast the passed object to a media type
		MediaHierarchy mediaHeirarchy = (MediaHierarchy) o;
		
		// Check if the object properties match and then return the result
		return Objects.equals(this.id,mediaHeirarchy.id) &&
				Objects.equals(this.hierarchyCode,mediaHeirarchy.hierarchyCode) &&
				Objects.equals(this.children,mediaHeirarchy.children) &&
				Objects.equals(this.hierarchyName,mediaHeirarchy.hierarchyName);
	}
	
	// This function generates a hash code for the current instance
	@Override
	public int hashCode() {
		return Objects.hash(this.id,this.hierarchyCode,this.children,this.hierarchyName);
	}
	
	
	
	// This function returns a string representation of the current instance
	@Override
	public String toString() {
		return "Media {" +"id="+this.id+ "hierarchyCode=" + this.hierarchyCode +"children="+this.children+"HierarchyName="+this.hierarchyName+"}";
	}

	
	
}
