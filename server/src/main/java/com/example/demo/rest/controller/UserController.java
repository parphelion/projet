package com.example.demo.rest.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.rest.models.Like;
import com.example.demo.rest.models.Post;
import com.example.demo.rest.models.User;
import com.example.demo.rest.services.PostService;
import com.example.demo.rest.services.UserService;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {

	@Autowired
	private UserService userService;
	
	@Autowired
	private PostService postService;

	@GetMapping(value= "/getuser/{username}")
    public User getUser(@PathVariable("username") String username) {
		return userService.getUserData(username);	
    }
	
	@PostMapping(value= "/follow")
    public ResponseEntity<HttpStatus> follow(@RequestBody @Valid final String username) {
		userService.follow(username);
		return new ResponseEntity<>(HttpStatus.OK);
		
    }
	
	@PostMapping(value= "/unfollow")
    public ResponseEntity<HttpStatus> unfollow(@RequestBody @Valid final String username) {
		userService.unfollow(username);
		return new ResponseEntity<>(HttpStatus.OK);
    }
	
	@GetMapping(value = "/favorites")
	public List<Like> getFavorites(	@RequestParam Integer page, 
						            @RequestParam Integer size,
						            @RequestParam String sort) {
		return postService.getFavorites(page, size, sort);
	}
	
	@GetMapping(value = "/posts/{username}")
	public List<Post> getPosts( @PathVariable("username") String username,
												@RequestParam Integer page, 
									            @RequestParam Integer size,
									            @RequestParam String sort) {
		return postService.getUserPosts(username, page, size, sort);
	}
}