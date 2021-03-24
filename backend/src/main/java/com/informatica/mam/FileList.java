package com.informatica.mam;

/*
 * ------------------------------------------------------------------
 * Name: FileList
 * Description: A class for mapping requests that comprise of a file list
 * Author: Jonathon Bowring
 * ------------------------------------------------------------------
 */

public class FileList {
	
	// Declare the attributes
	private String[] files;
	
	/*
	 * ------------------
	 * Constructors
	 * ------------------
	 */
	
	public FileList() {}
	
	public FileList(String[] files) {
		this.files = files;
	}
	
	/*
	 * ------------------
	 * Getter Methods
	 * ------------------
	 */
	
	public String[] getFiles() {
		return this.files;
	}
	
	/*
	 * ------------------
	 * Setter methods
	 * ------------------
	 */
	
	public void setFiles(String[] files) {
		this.files = files;
	}
	
}
