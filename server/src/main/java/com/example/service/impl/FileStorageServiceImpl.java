package com.example.service.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.rest.dao.PublicationRepository;
import com.example.rest.dao.UserRepository;
import com.example.service.FileStorageService;
import com.example.service.PublicationService;
import com.example.service.UserService;
import com.example.web.dto.response.AvatarResponse;
import com.example.web.exception.IncorrectFileExtensionException;

@Service
@Transactional
public class FileStorageServiceImpl implements FileStorageService {

	private static final Logger logger = LoggerFactory.getLogger(FileStorageServiceImpl.class);
	private static final List<String> contentTypes = Arrays.asList("image/png", "image/jpeg", "image/webp", "image/bmp", "image/jpg");
	private static final Path uploads = Paths.get("uploads");
	private static final Path photos = uploads.resolve("photos");
	private static final Path avatars = uploads.resolve("avatars");
	
	@Autowired
	private PublicationService postService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private PublicationRepository publicationRepository;
	
	@Autowired
	private UserRepository userRepository;

	@Override
	public void init() throws IOException {

		if (!Files.isDirectory(photos)) {
			Files.createDirectories(photos);
		}
		
		if (!Files.isDirectory(avatars)) {
			Files.createDirectories(avatars);
			var newFilePath = avatars.resolve("default.jpeg");
			var picture = "/9j/4AAQSkZJRgABAQEBLAEsAAD/4QBWRXhpZgAATU0AKgAAAAgABAEaAAUAAAABAAAAPgEbAAUAAAABAAAARgEoAAMAAAABAAIAAAITAAMAAAABAAEAAAAAAAAAAAEsAAAAAQAAASwAAAAB/+0ALFBob3Rvc2hvcCAzLjAAOEJJTQQEAAAAAAAPHAFaAAMbJUccAQAAAgAEAP/hDIFodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvADw/eHBhY2tldCBiZWdpbj0n77u/JyBpZD0nVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkJz8+Cjx4OnhtcG1ldGEgeG1sbnM6eD0nYWRvYmU6bnM6bWV0YS8nIHg6eG1wdGs9J0ltYWdlOjpFeGlmVG9vbCAxMC4xMCc+CjxyZGY6UkRGIHhtbG5zOnJkZj0naHR0cDovL3d3dy53My5vcmcvMTk5OS8wMi8yMi1yZGYtc3ludGF4LW5zIyc+CgogPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9JycKICB4bWxuczp0aWZmPSdodHRwOi8vbnMuYWRvYmUuY29tL3RpZmYvMS4wLyc+CiAgPHRpZmY6UmVzb2x1dGlvblVuaXQ+MjwvdGlmZjpSZXNvbHV0aW9uVW5pdD4KICA8dGlmZjpYUmVzb2x1dGlvbj4zMDAvMTwvdGlmZjpYUmVzb2x1dGlvbj4KICA8dGlmZjpZUmVzb2x1dGlvbj4zMDAvMTwvdGlmZjpZUmVzb2x1dGlvbj4KIDwvcmRmOkRlc2NyaXB0aW9uPgoKIDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PScnCiAgeG1sbnM6eG1wTU09J2h0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8nPgogIDx4bXBNTTpEb2N1bWVudElEPmFkb2JlOmRvY2lkOnN0b2NrOjFlMjI3NGQ0LTk1MGEtNGY3ZC04NTQzLWRhMGRlYzE3Mjk2NDwveG1wTU06RG9jdW1lbnRJRD4KICA8eG1wTU06SW5zdGFuY2VJRD54bXAuaWlkOjFjZTYxODdjLTRkMDgtNGMzYy04MDU1LTY2OWVlODJiZjRiMDwveG1wTU06SW5zdGFuY2VJRD4KIDwvcmRmOkRlc2NyaXB0aW9uPgo8L3JkZjpSREY+CjwveDp4bXBtZXRhPgogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAKICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIAo8P3hwYWNrZXQgZW5kPSd3Jz8+/9sAQwAFAwQEBAMFBAQEBQUFBgcMCAcHBwcPCwsJDBEPEhIRDxERExYcFxMUGhURERghGBodHR8fHxMXIiQiHiQcHh8e/9sAQwEFBQUHBgcOCAgOHhQRFB4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4e/8AAEQgBaAFoAwEiAAIRAQMRAf/EABwAAQADAQEBAQEAAAAAAAAAAAAFBgcEAwECCP/EAEIQAQABAwICAwwHBwQCAwAAAAABAgMEBREGIRIxQQcTIlFhcYGRobHB0RQWMkJVYnIjM0NS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";
			var bytes = Base64.getDecoder().decode(picture);
			Files.write(newFilePath, bytes, StandardOpenOption.CREATE);
		}
	}

	@Override
	public void save(MultipartFile file, String description) throws IncorrectFileExtensionException, IOException {

		String fileContentType = file.getContentType();

		if (contentTypes.contains(fileContentType)) {
			String generatedName = UUID.randomUUID().toString() + '.'
					+ FilenameUtils.getExtension(file.getOriginalFilename());

			Files.copy(file.getInputStream(), photos.resolve(generatedName));
			postService.createPost(generatedName, description);

		} else {
			throw new IncorrectFileExtensionException("Invalid file extension. Only PNG/JPEG files are allowed");
		}

	}
	
	@Override
	@Async
	public CompletableFuture<AvatarResponse> saveAvatar(MultipartFile file) throws IncorrectFileExtensionException, IOException {

		String fileContentType = file.getContentType();

		if (contentTypes.contains(fileContentType)) {
			String generatedName = UUID.randomUUID().toString() + '.'
					+ FilenameUtils.getExtension(file.getOriginalFilename());

			Files.copy(file.getInputStream(), avatars.resolve(generatedName));
			return userService.setProfilePicture(generatedName);

		} else {
			throw new IncorrectFileExtensionException("Invalid file extension. Only png/jpeg/webp/bmp files are allowed");
		}

	}

	@Override
	public Resource loadPhoto(String filename) throws MalformedURLException, FileNotFoundException {
		return getResource(photos.resolve(filename).toUri(), filename);
	}
	
	@Override
	public Resource loadAvatar(String filename) throws MalformedURLException, FileNotFoundException {
		return getResource(avatars.resolve(filename).toUri(), filename);	
	}
	
	private Resource getResource(URI uri, String filename) throws FileNotFoundException, MalformedURLException {
		
		Resource resource = new UrlResource(uri);

		if (resource.exists() || resource.isReadable()) {
			return resource;
		} else {
			throw new FileNotFoundException("Optional file " + filename + " was not found.");
		}
		
	}
	
	@Override
	public void deleteUnusedPhotos() throws IOException {
		try (Stream<Path> walk = Files.walk(uploads)){
			walk.filter(Files::isRegularFile)
	    		 .filter(f -> !f.getFileName().toString().endsWith(".tmp"))
	    		 .filter(f -> !publicationRepository.existsByFilepath(f.getFileName().toString()))
	    		 .forEach(t -> {
					try {
						Files.delete(t);
					} catch (IOException e) {
						logger.error("Could not delete the file: {}", e.getMessage());
					}
				});
		}
	}
	
	@Override
	public void deleteUnusedAvatars() throws IOException {
		try (Stream<Path> walk = Files.walk(uploads)){
			walk.filter(Files::isRegularFile)
	    		 .filter(f -> !f.getFileName().toString().equals("default.jpg"))
	    		 .filter(f -> !userRepository.existsByAvatar(f.getFileName().toString()))
	    		 .forEach(t -> {
					try {
						Files.delete(t);
					} catch (IOException e) {
						logger.error("Could not delete the file: {}", e.getMessage());
					}
				});
		}
	}
}

