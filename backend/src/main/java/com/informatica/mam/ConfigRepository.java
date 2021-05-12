package com.informatica.mam;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.hateoas.EntityModel;

interface ConfigRepository extends MongoRepository<Config, String> {
	
}
