package com.informatica.mam;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@RestController
public class ConfigController {

	// Declare the controller attributes
	private final ConfigRepository repository;
	private final ConfigAssembler assembler;
	
	/*
	 * ------------------
	 * Constructors
	 * ------------------
	 */
	
	ConfigController(ConfigRepository repository, ConfigAssembler assembler) {
		this.repository = repository;
		this.assembler = assembler;
	}
	
	/*
	 * ------------------
	 * GET end points
	 * ------------------
	 */
	
	// GET's metadata for all config
	@GetMapping("/config")
	CollectionModel<EntityModel<Config>> all() {
		
		List<EntityModel<Config>> configs = repository.findAll().stream()
				.map(assembler::toModel)
				.collect(Collectors.toList());
		
		return CollectionModel.of(configs, //
				linkTo(methodOn(ConfigController.class).all()).withSelfRel());
		
	}
	
	// GET's metadata config by id
	@GetMapping("/config/{id}")
	Optional<Config> one(@PathVariable String id) {
		
		return repository.findById(id);
	}
	
	/*
	 * ------------------
	 * POST end points
	 * ------------------
	 */
	
	// POSTs a new media Hierarchy.
	@PostMapping("/config")
	Config newConfig(@RequestBody Config config) {
		
		repository.save(config);
		return config;
		
	}
	
	/*
	 * ------------------
	 * PUT end points
	 * ------------------
	 */
	
	// PUT's an updated config doc for an id
	@PutMapping("/config/{id}")
	Config replaceConfig(@RequestBody Config newConfig, @PathVariable String id) {
		
		Config updatedConfig = repository.findById(id)
				.map(config -> {
					if(newConfig.getProperties()!=null) {
					config.setProperties(newConfig.getProperties());
					}
					
					return repository.save(config);
				})
				.orElseGet(() -> {
					newConfig.setId(id);
					return repository.save(newConfig);
				});
		return updatedConfig;
		
	}
	
	/*
	 * ------------------
	 * DELETE end points
	 * ------------------
	 */
	
	// DELETE's a config and it's associated metadata
	@DeleteMapping("/config/{id}")
	ResponseEntity<?> deleteConfig(@PathVariable String id) {

		// Get the requested config
		Optional<Config> config = repository.findById(id);
		
		// Delete the file from the database
		repository.deleteById(id);
		
		// If successful then return an empty response
		return ResponseEntity.noContent().build();
	}
	
}
