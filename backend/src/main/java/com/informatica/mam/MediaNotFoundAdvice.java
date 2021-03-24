package com.informatica.mam;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
class MediaNotFoundAdvice {
	
	@ResponseBody
	@ExceptionHandler(MediaNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	String MediaNotFoundHandler(MediaNotFoundException ex) {
		return ex.getMessage();
	}
	
}
