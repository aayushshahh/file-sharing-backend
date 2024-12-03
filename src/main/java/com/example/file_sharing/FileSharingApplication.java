package com.example.file_sharing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FileSharingApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileSharingApplication.class, args);
	}

}
