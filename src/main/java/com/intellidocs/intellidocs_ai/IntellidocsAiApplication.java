package com.intellidocs.intellidocs_ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync // enables @Async - needed for document ingestion pipeline
@EnableScheduling // enables @Scheduled - needed for token cleanup jobs
public class IntellidocsAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(IntellidocsAiApplication.class, args);
	}

}
