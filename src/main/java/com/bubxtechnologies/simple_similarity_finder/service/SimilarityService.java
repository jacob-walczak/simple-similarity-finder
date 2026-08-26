package com.bubxtechnologies.simple_similarity_finder.service;

import java.util.Comparator;
import java.util.List;

import com.bubxtechnologies.simple_similarity_finder.config.SimilarityFinderProperties;
import com.bubxtechnologies.simple_similarity_finder.loader.TestCaseCsvLoader;
import com.bubxtechnologies.simple_similarity_finder.model.SearchResult;
import com.bubxtechnologies.simple_similarity_finder.model.TestCase;
import com.bubxtechnologies.simple_similarity_finder.util.CosineSimilarity;
import com.bubxtechnologies.simple_similarity_finder.util.TextNormalizer;
import org.springframework.stereotype.Service;

@Service
public class SimilarityService {

	private final SimilarityFinderProperties properties;
	private final TestCaseCsvLoader csvLoader;
	private final EmbeddingService embeddingService;
	private final LexicalSimilarityService lexicalSimilarityService;

	private List<TestCase> indexedTestCases;

	public SimilarityService(
			SimilarityFinderProperties properties,
			TestCaseCsvLoader csvLoader,
			EmbeddingService embeddingService,
			LexicalSimilarityService lexicalSimilarityService) {
		this.properties = properties;
		this.csvLoader = csvLoader;
		this.embeddingService = embeddingService;
		this.lexicalSimilarityService = lexicalSimilarityService;
	}

	public List<SearchResult> search(String query) {
		ensureIndexReady();

		String normalizedQuery = TextNormalizer.normalize(query);
		float[] queryEmbedding = embeddingService.embedQuery(query);

		return indexedTestCases.stream()
			.map(testCase -> toSearchResult(testCase, normalizedQuery, queryEmbedding))
			.filter(result -> result.finalScore() >= properties.minimumScore())
			.sorted(Comparator.comparingDouble(SearchResult::finalScore).reversed())
			.limit(properties.resultLimit())
			.toList();
	}

	private void ensureIndexReady() {
		if (indexedTestCases != null) {
			return;
		}

		System.out.println("Loading Test Cases from CSV...");
		List<TestCase> testCases = csvLoader.load();
		System.out.println("Loaded " + testCases.size() + " Test Cases.");
		System.out.println();

		indexedTestCases = embeddingService.embedTestCases(testCases);
		System.out.println();
		System.out.println("Index ready.");
	}

	private SearchResult toSearchResult(TestCase testCase, String normalizedQuery, float[] queryEmbedding) {
		double semanticScore = CosineSimilarity.calculate(queryEmbedding, testCase.embedding());
		double lexicalScore = lexicalSimilarityService.scoreNormalized(normalizedQuery, testCase.normalizedName());
		double finalScore = semanticScore * properties.semanticWeight() + lexicalScore * properties.lexicalWeight();

		return new SearchResult(testCase, finalScore, semanticScore, lexicalScore);
	}
}
