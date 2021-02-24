package com.informatica.mam;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.hateoas.EntityModel;

/*
 * ------------------------------------------------------------------
 * Name: MediaRepository
 * Description: Interface that will be used to map the Media class to the API
 * Author: Baswashree Masudi 
 * ------------------------------------------------------------------
 */

interface MediaHierarchyRepository extends MongoRepository<MediaHierarchy, String> {
	

	public List findByHierarchyCode(String hierarchyCode);
	
}
