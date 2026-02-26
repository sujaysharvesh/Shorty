package com.example.Shorty;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class ShortyApplication {

	public static void main(String[] args) {
		log.info(">>> MAIN METHOD STARTED <<<");
		SpringApplication.run(ShortyApplication.class, args);
	}

}
