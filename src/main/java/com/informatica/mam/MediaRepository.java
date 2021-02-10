package com.informatica.mam;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

/*
 * ------------------------------------------------------------------
 * Name: MediaRepository
 * Description: Interface that will be used to map the Media class to the API
 * Author: Jonathon Bowring
 * ------------------------------------------------------------------
 */

interface MediaRepository extends MongoRepository<Media, String> {
	
	public Optional<Media> findByFileName(String fileName);
	
}
