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
import java.util.function.Consumer;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import software.amazon.awssdk.services.s3.model.GetUrlRequest.Builder;
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
	//private String s3Bucket="jbowring-mam-hackathon";
	//@Value("${cloud.aws.access-key}")
	//private String s3AccessKey;
	//@Value("${cloud.aws.secret-key}")
	//private String s3SecretKey;
	private	String fileEncoding;
	

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
	EntityModel<Media> one(@PathVariable String id) {
		Media media = repository.findById(id)
				.orElseThrow(() -> new MediaNotFoundException(id));
		
		return assembler.toModel(media);
	}
	
	// GET's a file by id
	@GetMapping("/media/{id}/file")
	@ResponseBody ResponseEntity<byte[]> oneFile(@PathVariable String id) throws NoSuchKeyException, S3Exception, AwsServiceException, SdkClientException, IOException {
		
		// Get the requested media metadata
		Media media = repository.findById(id)
				.orElseThrow(() -> new MediaNotFoundException(id));
		
		// Build a request to download the file from S3
		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
		        .bucket(s3Bucket)
		        .key(id + "/" + media.getFileName())
		        .build();
		
		// Download the file from S3
		byte[] fileBytes = IOUtils.toByteArray(s3.getObject(getObjectRequest));
		
		// Set the response headers
		HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + media.getFileName());
        
        // Return the file
		return ResponseEntity.ok()
	            .headers(headers)
	            .contentLength(fileBytes.length)
	            .contentType(MediaType.APPLICATION_OCTET_STREAM)
	            .body(fileBytes);

	}
	
	// GET's a zip containing one or more files
	@GetMapping("/media/file")
	@ResponseBody void getFiles(@RequestBody FileList req, OutputStream os) throws NoSuchKeyException, S3Exception, AwsServiceException, SdkClientException, IOException {
		
		// Initialise the zip output stream and link it to the response output stream
		ZipOutputStream zos = new ZipOutputStream(os);
		
		// Loop through each of the requested files
		for(String file : req.getFiles()) {
			
			// Find matching files in the MongoDB
			List<Media> medias = repository.findByFileName(file);
			
			// Sort the medias by the file name
			medias.sort(Comparator.comparing(Media::getFileName));
			
			// Loop through the media found in the DB
			String prevFile = "";
			int fileCounter = 0;
			for(Media tmpMedia : medias) {
				
				// Build a request to download the file from S3
				GetObjectRequest getObjectRequest = GetObjectRequest.builder()
				        .bucket(s3Bucket)
				        .key(tmpMedia.getId() + "/" + tmpMedia.getFileName())
				        .build();
				
				// Download the file and initialise the zip entry
				byte[] s3Bytes = IOUtils.toByteArray(s3.getObject(getObjectRequest));
				ByteArrayInputStream fis = new ByteArrayInputStream(s3Bytes);
				
				// Initialise the variables for post fixing the file name
				String currFile = tmpMedia.getFileName();
				String zipFile = "";
				String tokens[] = currFile.split("\\.(?=[^\\.]+$)");
				
				// If the curr media has the same file name as the previous file then postfix it
				// Also handle files that do and don't have a file extension for the post fixing
				if(currFile.equals(prevFile)) {
					fileCounter++;
					if(tokens.length == 1) {
						zipFile = tokens[0] + " (" + fileCounter + ")";
					}
					else if(tokens.length == 2) {
						zipFile = tokens[0] + " (" + fileCounter + ")." + tokens[1];
					}
					else {
						zipFile = currFile;
					}
					
				}
				// Else reset the counter
				else {
					fileCounter = 0;
					zipFile = currFile;
				}
				
				// Add the file to the zip
				ZipEntry zipEntry = new ZipEntry(zipFile);
				zos.putNextEntry(zipEntry);
				int length;
				while((length = fis.read(s3Bytes)) >= 0) {
					zos.write(s3Bytes, 0, length);
				}
				
				// Close the file input stream
				fis.close();
				
				// Update the prevFile to check for post fixing
				prevFile = tmpMedia.getFileName();
				
			}
			
		}
		
		// Close the zip output stream
		zos.close();
		
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
			String fileExtension = FilenameUtils.getExtension(multiFile.getOriginalFilename());
			
			
			// Generate a temp file ID
			UUID uuid = UUID.randomUUID();
			
			// Convert the multipart file to a file
			File newFile = new File(uuid.toString() + ".tmp");
			try (OutputStream os = new FileOutputStream(newFile)) {
				os.write(multiFile.getBytes());
				
				InputStream fs = new FileInputStream(newFile);
				InputStreamReader   isr = new InputStreamReader(fs);
				 fileEncoding=isr.getEncoding();
			}
			
			// Instantiate a new Media object
			EntityModel<Media> entityModel = assembler.toModel(repository.save(new Media(multiFile.getOriginalFilename(),fileExtension,multiFile.getContentType(), multiFile.getSize(),fileEncoding)));
					
			String s3FileName=	entityModel.getContent().getId() +"/"+multiFile.getOriginalFilename();
			// Upload the file to S3
			PutObjectRequest objectRequest = PutObjectRequest.builder()
					.bucket(s3Bucket)
					.key(s3FileName)
					.build();
			PutObjectResponse s3Response = s3.putObject(objectRequest, software.amazon.awssdk.core.sync.RequestBody.fromFile(newFile));
			
			// Get the public URL and save it to the database
			String url = s3.utilities().getUrl(builder -> builder.bucket(s3Bucket).key(s3FileName)).toExternalForm();
			//entityModel.getContent().setUrl(url);
			//Optional<Media> urlMedia = repository.findById(entityModel.getContent().getId());
			Media urlMedia = repository.findById(entityModel.getContent().getId())
					.orElseThrow(() -> new MediaNotFoundException(entityModel.getContent().getId()));
			urlMedia.setUrl(url);
			repository.save(urlMedia);
			
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
	ResponseEntity<?> replaceMedia(@RequestBody Media newMedia, @PathVariable String id) {
		
		Media updatedMedia = repository.findById(id) //
				.map(media -> {
					if(null!=newMedia.getFileExtension())
					media.setFileExtension(newMedia.getFileExtension());
					if(null!=newMedia.getFileName())
					media.setFileName(newMedia.getFileName());
					if(null!=newMedia.getFileSize())
					media.setFileSize(newMedia.getFileSize());
					if(null!=newMedia.getFileEncoding())
					media.setFileEncoding(newMedia.getFileEncoding());
					if(null!=newMedia.getMimeType())
					media.setMimeType(newMedia.getMimeType());
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
	ResponseEntity<?> deleteMedia(@PathVariable String id) {

	  repository.deleteById(id);

	  return ResponseEntity.noContent().build();
	}
	
	/*
	 * ------------------
	 * Helper methods
	 * ------------------
	 */

	
	
}
