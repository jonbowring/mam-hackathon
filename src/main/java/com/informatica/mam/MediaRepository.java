package com.informatica.mam;

import org.springframework.data.jpa.repository.JpaRepository;

/*
 * ------------------------------------------------------------------
 * Name: MediaRepository
 * Description: Interface that will be used to map the Media class to the API
 * Author: Jonathon Bowring
 * ------------------------------------------------------------------
 */

interface MediaRepository extends JpaRepository<Media, Long> {
	
}
