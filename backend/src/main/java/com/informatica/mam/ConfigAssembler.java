package com.informatica.mam;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.io.OutputStream;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class ConfigAssembler implements RepresentationModelAssembler<Config, EntityModel<Config>> {
	
	@Override
	public EntityModel<Config> toModel(Config config) {
		return EntityModel.of(config);
	}
	
}
