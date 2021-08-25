package com.example.demo.rest.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.example.demo.validators.ValidEmail;

public class LoginDto {
	
	@ValidEmail
	private String email;

    @NotNull
    @Size(min=8, max=32)
	private String password;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
