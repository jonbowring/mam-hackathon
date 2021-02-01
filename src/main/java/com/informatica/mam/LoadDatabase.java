package com.informatica.mam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * ------------------------------------------------------------------
 * Name: LoadDatabase
 * Description: A class for initialising the database when the app is started.
 * Author: Jonathon Bowring
 * ------------------------------------------------------------------
 */

@Configuration
public class LoadDatabase {
	
	private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);
	
	@Bean
	CommandLineRunner initDatabase(MediaRepository repository) {
		
		return args -> {
			log.info("Preloading " + repository.save(new Media("HelloWorld.txt")));
		};
		
	}
}
