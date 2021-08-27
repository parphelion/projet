package com.example.demo.tasks;

import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.rest.repository.PostRepository;
import com.example.demo.rest.repository.UserRepository;
import com.example.demo.rest.services.FileStorageService;

@Service
@Transactional
public class FileRemovalTask {

	private static final Logger log = LoggerFactory.getLogger(FileRemovalTask.class);

	@Autowired
	PostRepository postRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	FileStorageService storageService;

	@Scheduled(cron = "${purge.photos.cron.expression}")
	public void purgeUnusedPhotos() throws IOException {
		log.info("started purging unused photo files");
		Set<String> photos = storageService.listUploadedPhotos();
		photos.stream().forEach(photo -> {
			if (!photo.endsWith(".tmp") && !postRepository.existsByFilepath(photo)) {
				try {
					storageService.deleteUploadedPhoto(photo);
				} catch (IOException ex) {
					log.error("File could not be deleted: {}", ex.getMessage());
				}
			}
		});
	}

	@Scheduled(cron = "${purge.avatars.cron.expression}")
	public void purgeUnusedAvatars() throws IOException {
		log.info("started purging unused avatar files");
		Set<String> avatars = storageService.listUploadedAvatars();
		avatars.stream().forEach(avatar -> {
			if (!avatar.equals("default.jpg") && !userRepository.existsByAvatar(avatar)) {
				try {
					storageService.deleteUploadedAvatar(avatar);
				} catch (IOException ex) {
					log.error("File could not be deleted: {}", ex.getMessage());
				}
			}
		});
	}
}