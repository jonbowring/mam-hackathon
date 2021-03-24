package com.informatica.mam;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.io.OutputStream;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

/*
 * ------------------------------------------------------------------
 * Name: MediaController
 * Description: This class is used to make it easier to assemble 
 *     best practice RESTful API's.
 * Author: Jonathon Bowring
 * ------------------------------------------------------------------
 */

@Component
class MediaAssembler implements RepresentationModelAssembler<Media, EntityModel<Media>> {
	
	@Override
	public EntityModel<Media> toModel(Media media) {
		return EntityModel.of(media, //
				linkTo(methodOn(MediaController.class).one(media.getId())).withSelfRel(),
				linkTo(methodOn(MediaController.class).all()).withRel("medias"));
	}
	
}
