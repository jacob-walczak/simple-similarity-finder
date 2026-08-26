package com.bubxtechnologies.simple_similarity_finder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SimpleSimilarityFinderApplication {

	public static void main(String[] args) {
		SpringApplication.run(SimpleSimilarityFinderApplication.class, args);
	}

}
