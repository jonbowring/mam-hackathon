package com.informatica.mam;

import java.util.Objects;

import org.springframework.data.annotation.Id;

/*
 * ------------------------------------------------------------------
 * Name: Media
 * Description: Model class for managing Media properties and methods
 * Author: Jonathon Bowring
 * ------------------------------------------------------------------
 */

class Media {
	
	// Declare Media related attributes
	@Id
	private String id;
	private String fileName;
	private String fileExtension;
	private String mimeType;
	private Long fileSize;
	private String fileEncoding;
	
	/*
	 * ------------------
	 * Constructors
	 * ------------------
	 */
	
	
	
	Media() {}
	
	Media(String fileName, String fileExtension,String mimeType,Long fileSize,String fileEncoding) {
		this.fileName = fileName;
		this.fileExtension=fileExtension;
		this.mimeType=mimeType;
		this.fileSize=fileSize;
		this.fileEncoding=fileEncoding;
	}
	
	/*
	 * ------------------
	 * Getter Methods
	 * ------------------
	 */
	
	public String getFileExtension() {
		return fileExtension;
	}



	public String getMimeType() {
		return mimeType;
	}

	
	public Long getFileSize() {
		return fileSize;
	}


	
	public String getId() {
		return this.id;
	}
	
	public String getFileName() {
		return this.fileName;
	}
	
	public String getFileEncoding() {
		return fileEncoding;
	}

	
	/*
	 * ------------------
	 * Setter Methods
	 * ------------------
	 */
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}
	
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}
	
	public void setFileEncoding(String fileEncoding) {
		this.fileEncoding = fileEncoding;
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
				Objects.equals(this.fileName, media.fileName) && Objects.equals(this.fileSize, media.fileSize) && Objects.equals(this.fileExtension, media.fileExtension)
				&& Objects.equals(this.mimeType, media.mimeType)&& Objects.equals(this.fileEncoding, media.fileEncoding);
	}
	
	// This function generates a hash code for the current instance
	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.fileName,this.fileExtension,this.fileSize,this.mimeType,this.fileEncoding);
	}
	
	// This function returns a string representation of the current instance
	@Override
	public String toString() {
		return "Media {" + "id=" + this.id + ", fileName='" + this.fileName + "'" + ", fileSize=" + this.fileSize + ", fileExtension='"+ this.fileExtension+"'" +", fileEncoding='"+ this.fileEncoding+"'}";
	}
	
	
}
