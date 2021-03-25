package com.informatica.mam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.mongodb.client.MongoClients;

@SpringBootApplication
public class MamApiApplication implements CommandLineRunner {

	
	public static void main(String[] args) {
		
		SpringApplication.run(MamApiApplication.class, args);
	}
	
	@Override
	public void run(String[] args) {
		
		//MongoOperations mongoOps = new MongoTemplate(new SimpleMongoClientDatabaseFactory(MongoClients.create(), "database"));
		//mongoOps.insert(new Media("HelloFactory.txt"));
		
		//repository.deleteAll();
		
		// Save a few media docs
		//repository.save(new Media("HelloCluster.txt"));
		
		// Test print of saved docs
		/*
		for (Media media : repository.findAll()) {
			System.out.println(media.getFileName());
		}
		*/
	}
	
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**").allowedOrigins("*").allowedMethods("GET", "POST", "PUT", "DELETE");
			}
		};
	}

}
