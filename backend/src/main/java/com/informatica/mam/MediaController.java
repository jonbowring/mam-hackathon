package com.informatica.mam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.sun.xml.messaging.saaj.util.ByteInputStream;

import net.minidev.json.JSONObject;

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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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
	
	@Autowired
	private ConfigRepository configRepo;

	

	@Value("${cloud.aws.bucket.name}")
	//private String s3Bucket;
	private String s3Bucket="jbowring-mam-hackathon";
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
	@ResponseBody byte[] oneFile(@PathVariable Long id) throws NoSuchKeyException, S3Exception, AwsServiceException, SdkClientException, IOException {
		
		// Build a request to download the file from S3
		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
		        .bucket(s3Bucket)
		        .key("test.png")
		        .build();
		
		// Download the file
		return IOUtils.toByteArray(s3.getObject(getObjectRequest));

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
		
		// Read the derivative config if existing
		Optional<Config> config = configRepo.findById("derivatives");
		
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
			BufferedImage bImg=ImageIO.read(multiFile.getInputStream());
			String contentType = multiFile.getContentType();
			int width = contentType.substring(0,5).equals("image") ? bImg.getWidth() : -1;
			int height = contentType.substring(0,5).equals("image") ? bImg.getHeight() : -1;
			String hierarchyCode="";
			
			// Instantiate a new Media object
			EntityModel<Media> entityModel = assembler.toModel(repository.save(new Media(multiFile.getOriginalFilename(),fileExtension,multiFile.getContentType(), multiFile.getSize(),fileEncoding,width,height,hierarchyCode)));
					
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
			
			EntityModel<Media> entityModel1 =  assembler.toModel(new Media(multiFile.getOriginalFilename(),fileExtension,multiFile.getContentType(), multiFile.getSize(),fileEncoding,width,height,url,entityModel.getContent().getId(),hierarchyCode));
			// Delete the temp file
			newFile.delete();
			
			// Add the new file to the list
			medias.add(entityModel1);
			
			// If derivatives are configured and the file type is an image then generate the derivative files and save them
			if(config != null && contentType.substring(0,5).equals("image")) {
				
				HashMap<String, String> properties = config.get().getProperties();
				
				// Loop through each of the derivatives
				properties.forEach((key, value) -> {
					
					// Split out the derivative settings
					String[] values = value.split(":");
					String postfix = values[0];
					int imgWidth = Integer.parseInt(values[1]);
					int imgHeight = Integer.parseInt(values[2]);
					
					// Initialise the generate derivative request and headers
					RestTemplate rest = new RestTemplate();
					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_JSON);
					headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
					
					// Initialise the JSON body
					JSONObject jsonRequest = new JSONObject();
					jsonRequest.put("url", url);
					
					// If width is provided then add it to the request
					if(imgWidth > 0) {
						jsonRequest.put("width", imgWidth);
					}
					
					// If height is provided then add it to the request
					if(imgHeight > 0) {
						jsonRequest.put("height", imgHeight);
					}
					
					// Send the request
					HttpEntity<String> request = new HttpEntity<String>(jsonRequest.toString(), headers);
					ResponseEntity<byte[]> response = rest.exchange(
							"http://localhost:3374/image/resize",
				            HttpMethod.POST, request, byte[].class, "1");

				    if (response.getStatusCode() == HttpStatus.OK) {
				        /*
				    	try {
							Files.write(Paths.get(uuid.toString() + "." + postfix + ".tmp"), response.getBody());
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						*/
				    	// Create a file from the base64 bytes
						File newDerivativeFile = new File(uuid.toString() + "." + postfix + ".tmp");
						try (OutputStream os = new FileOutputStream(newDerivativeFile)) {
							System.out.println("Writing to file: " + uuid.toString() + "." + postfix + ".tmp");
							os.write(response.getBody());
							
							InputStream fs = new FileInputStream(newDerivativeFile);
							InputStreamReader isr = new InputStreamReader(fs);
							fileEncoding=isr.getEncoding();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						
						String s3DerivativeName = s3FileName + "." + postfix;
						// Upload the file to S3
						PutObjectRequest derivativeRequest = PutObjectRequest.builder()
								.bucket(s3Bucket)
								.key(s3DerivativeName)
								.build();
						PutObjectResponse s3DerivativeResponse = s3.putObject(derivativeRequest, software.amazon.awssdk.core.sync.RequestBody.fromFile(newDerivativeFile));
						
						// Get the public URL and save it to the database
						String derivativeUrl = s3.utilities().getUrl(builder -> builder.bucket(s3Bucket).key(s3DerivativeName)).toExternalForm();
						System.out.println(derivativeUrl);
						
				    }
					/*
					String base64Img = rest.postForObject("http://localhost:3374/image/resize", request, String.class);
					System.out.println("\n\n\n\n\n");
					System.out.println(base64Img);
					byte[] base64Bytes = Base64.getDecoder().decode(base64Img.getBytes(StandardCharsets.UTF_8));
					*/
					//byte[] base64Bytes = null;
					/*
					try {
						base64Bytes = IOUtils.toByteArray(rest.postForObject("http://localhost:3374/image/resize", request, String.class));
					} catch (RestClientException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					*/
					
					
					
				});
				
			}
			
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
	
	@PutMapping("/media/hierarchy/{id}")
	Media replaceConfig(@RequestBody Media newMediaHierarchyUpdate, @PathVariable String id) {
		
		Media updatedConfig = repository.findById(id)
				.map(media -> {
					if(newMediaHierarchyUpdate.getHierarchyCode()!=null) {
						media.setHierarchyCode(newMediaHierarchyUpdate.getHierarchyCode());
					}
					
					return repository.save(media);
				})
				.orElseGet(() -> {
					newMediaHierarchyUpdate.setId(id);
					return repository.save(newMediaHierarchyUpdate);
				});
		return updatedConfig;
		
	}
	
	
	/*
	 * ------------------
	 * DELETE end points
	 * ------------------
	 */
	
	// DELETE's a file and it's associated metadata
	@DeleteMapping("/media/{id}")
	ResponseEntity<?> deleteMedia(@PathVariable String id) {

		// Get the requested media metadata
		Media media = repository.findById(id)
				.orElseThrow(() -> new MediaNotFoundException(id));
		
		// Delete the file from the S3 bucket
		DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
		        .bucket(s3Bucket)
		        .key(id + "/" + media.getFileName())
		        .build();
		s3.deleteObject(deleteObjectRequest);
		
		// Delete the file from the database
		repository.deleteById(id);
		
		// If successful then return an empty response
		return ResponseEntity.noContent().build();
	}
	
	/*
	 * ------------------
	 * Helper methods
	 * ------------------
	 */

	
	
}
