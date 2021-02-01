package com.informatica.mam;

/*
 * ------------------------------------------------------------------
 * Name: MediaNotFoundException
 * Description: Exception class for when media is not found
 * Author: Jonathon Bowring
 * ------------------------------------------------------------------
 */

class MediaNotFoundException extends RuntimeException {
	
	MediaNotFoundException(Long id) {
		super("Media not found: " + id);
	}
	
}
