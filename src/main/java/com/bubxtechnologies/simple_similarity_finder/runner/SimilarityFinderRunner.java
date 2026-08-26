package com.bubxtechnologies.simple_similarity_finder.runner;

import java.util.List;

import com.bubxtechnologies.simple_similarity_finder.config.SimilarityFinderProperties;
import com.bubxtechnologies.simple_similarity_finder.model.SearchResult;
import com.bubxtechnologies.simple_similarity_finder.service.SimilarityService;
import com.bubxtechnologies.simple_similarity_finder.util.ApplicationException;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SimilarityFinderRunner implements CommandLineRunner {

	private final SimilarityFinderProperties properties;
	private final SimilarityService similarityService;

	public SimilarityFinderRunner(SimilarityFinderProperties properties, SimilarityService similarityService) {
		this.properties = properties;
		this.similarityService = similarityService;
	}

	@Override
	public void run(String @NonNull ... args) {
		String query = String.join(" ", args).trim();
		if (query.isBlank()) {
			printUsage();
			return;
		}

		try {
			List<SearchResult> results = similarityService.search(query);
			printResults(query, results);
		}
		catch (ApplicationException e) {
			System.err.println();
			System.err.println("Error: " + e.getMessage());
		}
	}

	private static void printUsage() {
		System.out.println("No query provided.");
		System.out.println();
		System.out.println("Usage:");
		System.out.println("./mvnw spring-boot:run -Dspring-boot.run.arguments=\"sprzedaż gotówką\"");
		System.out.println("java -jar target/simple-similarity-finder.jar \"sprzedaż gotówką\"");
	}

	private void printResults(String query, List<SearchResult> results) {
		System.out.println();
		System.out.println("Query: " + query);
		System.out.println();

		if (results.isEmpty()) {
			System.out.printf("No Test Cases matched minimum score %.3f.%n", properties.minimumScore());
			return;
		}

		System.out.println("Found " + results.size() + " similar Test Cases:");
		System.out.println();

		for (int i = 0; i < results.size(); i++) {
			SearchResult result = results.get(i);
			if (properties.showScoreDetails()) {
				printDetailedResult(i + 1, result);
			}
			else {
				System.out.printf("%d. %s | %s | score: %.3f%n",
						i + 1,
						result.testCase().id(),
						result.testCase().name(),
						result.finalScore());
			}
		}
	}

	private static void printDetailedResult(int position, SearchResult result) {
		System.out.printf("%d. %s | %s%n",
				position,
				result.testCase().id(),
				result.testCase().name());
		System.out.printf("   final:    %.3f%n", result.finalScore());
		System.out.printf("   semantic: %.3f%n", result.semanticScore());
		System.out.printf("   lexical:  %.3f%n", result.lexicalScore());
		System.out.println();
	}
}
