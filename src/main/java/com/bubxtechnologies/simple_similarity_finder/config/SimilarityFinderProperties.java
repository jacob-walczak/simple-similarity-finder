package com.bubxtechnologies.simple_similarity_finder.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "similarity-finder")
public record SimilarityFinderProperties(
		String csvPath,
		String embeddingCachePath,
		int resultLimit,
		double semanticWeight,
		double lexicalWeight,
		double minimumScore,
		boolean showScoreDetails) {

	private static final double WEIGHT_SUM_TOLERANCE = 0.0001;

	public SimilarityFinderProperties {
		if (csvPath == null || csvPath.isBlank()) {
			throw new IllegalArgumentException("similarity-finder.csv-path must not be blank");
		}
		if (embeddingCachePath == null || embeddingCachePath.isBlank()) {
			throw new IllegalArgumentException("similarity-finder.embedding-cache-path must not be blank");
		}
		if (resultLimit <= 0) {
			throw new IllegalArgumentException("similarity-finder.result-limit must be greater than 0");
		}
		if (semanticWeight < 0.0 || lexicalWeight < 0.0) {
			throw new IllegalArgumentException("similarity-finder weights must not be negative");
		}
		double weightSum = semanticWeight + lexicalWeight;
		if (Math.abs(weightSum - 1.0) > WEIGHT_SUM_TOLERANCE) {
			throw new IllegalArgumentException("similarity-finder semantic-weight and lexical-weight must sum to 1.0");
		}
		if (minimumScore < 0.0 || minimumScore > 1.0) {
			throw new IllegalArgumentException("similarity-finder.minimum-score must be between 0.0 and 1.0");
		}
	}
}
