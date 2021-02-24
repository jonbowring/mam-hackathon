package com.informatica.mam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
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

import com.sun.xml.messaging.saaj.util.ByteInputStream;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
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
 * Author: Baswashree Masudi
 * ------------------------------------------------------------------
 */

@RestController
public class MediaHierarchyController {
	
	// Declare the controller attributes
	private final MediaHierarchyRepository repository;
//	private final MediaHierarchyAssembler assembler;
	private final S3Client s3;
	private Region s3Region;
	@Value("${cloud.aws.region.name}")
	private String s3RegionName;

	//@Value("${cloud.aws.bucket.name}")
	private String s3Bucket="jbowring-mam-hackathon";
	//@Value("${cloud.aws.access-key}")
	//private String s3AccessKey;
	//@Value("${cloud.aws.secret-key}")
	//private String s3SecretKey;
	
	

	/*
	 * ------------------
	 * Constructors
	 * ------------------
	 */
	
	MediaHierarchyController(MediaHierarchyRepository repository, MediaHierarchyAssembler assembler) {
		this.repository = repository;
		//this.assembler = assembler;
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
	
	// GET's metadata for all mediaHierarchy
	@GetMapping("/mediaHierarchy")
	
	CollectionModel<MediaHierarchy> all() {
		
		List<MediaHierarchy> medias =  repository.findAll();
		return CollectionModel.of(medias, //
				linkTo(methodOn(MediaHierarchyController.class).all()).withSelfRel());
		
	}
	
	
	// GET's media hierarchy by id
		@GetMapping("/mediaHierarchy/{id}")

		
		MediaHierarchy one(@PathVariable String id) {
			MediaHierarchy media = repository.findById(id)
					.orElseThrow(() -> new MediaNotFoundException(id));
			
			return media;
		}
	
	/*
	 * ------------------
	 * POST end points
	 * ------------------
	 */
	
	// POSTs a new media Hierarchy.
	@PostMapping("/mediaHierarchy/create")
	MediaHierarchy newMediaHierchy(@RequestBody MediaHierarchy mediaHierarchy) throws S3Exception, AwsServiceException, SdkClientException, IOException {
		
		
		repository.save(mediaHierarchy);
		return mediaHierarchy;
		
	}
	
	
	/*
	 * ------------------
	 * PUT end points
	 * ------------------
	 */
	// PUT's an updated metadata doc for an id
		@PutMapping("/mediaHierarchy/{id}")
		MediaHierarchy replaceMedia(@RequestBody MediaHierarchy newMedia, @PathVariable String id) {
			
			
			MediaHierarchy updatedMedia = repository.findById(id)
					.map(mediaHierarchy -> {
						if(newMedia.getHierarchyName()!=null) {
						mediaHierarchy.setHierarchyName(newMedia.getHierarchyName());
						}
						if(newMedia.getHierarchyCode()!=null) {
							mediaHierarchy.setHierarchyCode(newMedia.getHierarchyCode());
							}
						if(newMedia.getChildren()!=null) {
							mediaHierarchy.setChildren(newMedia.getChildren());
							}
						
						return repository.save(mediaHierarchy);
					})
					.orElseGet(() -> {
						newMedia.setId(id);
						return repository.save(newMedia);
					});
			return updatedMedia;
					
			
			
			
		
			
		}
		
	
	// PUT's an updated metadata doc for an id
	
	// DELETE's a media hierarchy and it's associated metadata
		@DeleteMapping("/mediaHierarchy/{id}")
		ResponseEntity<?> deleteMedia(@PathVariable String id) {

		  repository.deleteById(id);

		  return ResponseEntity.noContent().build();
		}
	
}
