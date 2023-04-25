package com.example.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/rs/monitor")
public class UnsecuredAPI {
	
	@GetMapping
    public ResponseEntity<String> health() {
		StringBuilder sb = new StringBuilder();
		sb.append("Application is UP").append(System.lineSeparator());
		return new ResponseEntity<>(sb.toString(), HttpStatus.OK);
	}


}
