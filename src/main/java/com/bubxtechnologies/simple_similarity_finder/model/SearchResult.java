package com.bubxtechnologies.simple_similarity_finder.model;

public record SearchResult(
		TestCase testCase,
		double finalScore,
		double semanticScore,
		double lexicalScore) {
}
