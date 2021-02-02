package com.informatica.mam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;
//import software.amazon.awssdk.core.sync.RequestBody; // This will need to be used explicitly to avoid collision
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateBucketConfiguration;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.waiters.S3Waiter;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;


/*
 * ------------------------------------------------------------------
 * Name: MediaController
 * Description: This class defines the Media REST API endpoints.
 * Author: Jonathon Bowring
 * ------------------------------------------------------------------
 */

@RestController
public class MediaController {
	
	// Declare the controller attributes
	private final MediaRepository repository;
	private final MediaAssembler assembler;
	private final S3Client s3;
	private Region s3Region;
	@Value("${cloud.aws.region.name}")
	private String s3RegionName;
	@Value("${cloud.aws.bucket.name}")
	private String s3Bucket;
	@Value("${cloud.aws.credentials.access-key}")
	private String s3Key;
	
	/*
	 * ------------------
	 * Constructors
	 * ------------------
	 */
	
	MediaController(MediaRepository repository, MediaAssembler assembler) {
		this.repository = repository;
		this.assembler = assembler;
		
		// Configure the AWS S3 client
		this.s3Region = Region.AP_SOUTHEAST_2;
		//this.s3Region = Region.of(s3RegionName);
		this.s3 = S3Client.builder().region(this.s3Region).build();
	}
	
	/*
	 * ------------------
	 * GET end points
	 * ------------------
	 */
	
	// GET's metadata for all media
	@GetMapping("/media")
	CollectionModel<EntityModel<Media>> all() {
		
		List<EntityModel<Media>> medias = repository.findAll().stream()
				.map(assembler::toModel)
				.collect(Collectors.toList());
		
		return CollectionModel.of(medias, //
				linkTo(methodOn(MediaController.class).all()).withSelfRel());
		
	}
	
	// GET's metadata media by id
	@GetMapping("/media/{id}")
	EntityModel<Media> one(@PathVariable Long id) {
		Media media = repository.findById(id)
				.orElseThrow(() -> new MediaNotFoundException(id));
		
		return assembler.toModel(media);
	}
	
	// GET's a file by id
	@GetMapping("/media/{id}/file")
	@ResponseBody byte[] oneFile(@PathVariable Long id) throws NoSuchKeyException, S3Exception, AwsServiceException, SdkClientException, IOException {
		
		// Build a request to download the file from S3
		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
		        .bucket(s3Bucket)
		        .key("test.png")
		        .build();
		
		// Download the file
		return IOUtils.toByteArray(s3.getObject(getObjectRequest));

	}
	
	/*
	 * ------------------
	 * POST end points
	 * ------------------
	 */
	
	// POSTs a new file to the application. Creates the metadata doc and uploads the file.
	@PostMapping("/media")
	CollectionModel<EntityModel<Media>> newMedia(@RequestParam("files") MultipartFile[] multiFiles, RedirectAttributes redirectAttributes) throws S3Exception, AwsServiceException, SdkClientException, IOException {
		
		// Instantiate the medias list
		List<EntityModel<Media>> medias = new ArrayList<>();
		
		// Loop through all of the posted files
		for(MultipartFile multiFile : multiFiles) {
			
			// Instantiate a new Media object
			EntityModel<Media> entityModel = assembler.toModel(repository.save(new Media(multiFile.getOriginalFilename())));
			
			// Generate a temp file ID
			UUID uuid = UUID.randomUUID();
			
			// Convert the multipart file to a file
			File newFile = new File(uuid.toString() + ".tmp");
			try (OutputStream os = new FileOutputStream(newFile)) {
				os.write(multiFile.getBytes());
			}
			
			// Upload the file to S3
			PutObjectRequest objectRequest = PutObjectRequest.builder()
					.bucket(s3Bucket)
					.key(multiFile.getOriginalFilename())
					.build();
			PutObjectResponse s3Response = s3.putObject(objectRequest, software.amazon.awssdk.core.sync.RequestBody.fromFile(newFile));
			
			// Delete the temp file
			newFile.delete();
			
			// Add the new file to the list
			medias.add(entityModel);
			
		}
		
		return CollectionModel.of(medias, //
				linkTo(methodOn(MediaController.class).all()).withSelfRel());
		
	}
	
	
	
	/*
	 * ------------------
	 * PUT end points
	 * ------------------
	 */
	
	// PUT's an updated metadata doc for an id
	@PutMapping("/media/{id}")
	ResponseEntity<?> replaceMedia(@RequestBody Media newMedia, @PathVariable Long id) {
		
		Media updatedMedia = repository.findById(id) //
				.map(media -> {
					media.setFileName(newMedia.getFileName());
					return repository.save(media);
				})
				.orElseGet(() -> {
					newMedia.setId(id);
					return repository.save(newMedia);
				});
		
		EntityModel<Media> entityModel = assembler.toModel(updatedMedia);
		
		return ResponseEntity
				.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
				.body(entityModel);
		
	}
	
	/*
	 * ------------------
	 * DELETE end points
	 * ------------------
	 */
	
	// DELETE's a file and it's associated metadata
	@DeleteMapping("/media/{id}")
	ResponseEntity<?> deleteMedia(@PathVariable Long id) {

	  repository.deleteById(id);

	  return ResponseEntity.noContent().build();
	}
	
	/*
	 * ------------------
	 * Helper methods
	 * ------------------
	 */

	
	
}
