package com.reporadar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class RepoRadarApplication {

	public static void main(String[] args) {
		var ctx = SpringApplication.run(RepoRadarApplication.class, args);
	}

}
