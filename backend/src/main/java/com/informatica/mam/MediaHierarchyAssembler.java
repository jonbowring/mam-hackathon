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
 * Author: Baswashree Masudi
 * ------------------------------------------------------------------
 */

@Component
class MediaHierarchyAssembler implements RepresentationModelAssembler<MediaHierarchy, EntityModel<MediaHierarchy>> {
	
	@Override
	public EntityModel<MediaHierarchy> toModel(MediaHierarchy mediaHierarchy) {
		return EntityModel.of(mediaHierarchy //
				);
	}
	
}
