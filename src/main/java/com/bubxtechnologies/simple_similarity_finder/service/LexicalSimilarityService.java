package com.bubxtechnologies.simple_similarity_finder.service;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.bubxtechnologies.simple_similarity_finder.util.TextNormalizer;
import org.springframework.stereotype.Service;

@Service
public class LexicalSimilarityService {

	public double score(String query, String testCaseName) {
		String normalizedQuery = TextNormalizer.normalize(query);
		String normalizedName = TextNormalizer.normalize(testCaseName);
		return scoreNormalized(normalizedQuery, normalizedName);
	}

	public double scoreNormalized(String normalizedQuery, String normalizedName) {
		if (normalizedQuery.isBlank() || normalizedName.isBlank()) {
			return 0.0;
		}
		if (normalizedQuery.equals(normalizedName)) {
			return 1.0;
		}

		Set<String> queryTokens = tokens(normalizedQuery);
		Set<String> nameTokens = tokens(normalizedName);
		if (queryTokens.isEmpty() || nameTokens.isEmpty()) {
			return 0.0;
		}

		double score = 0.0;

		if (normalizedName.contains(normalizedQuery)) {
			score = Math.max(score, 0.90);
		}

		long exactTokenMatches = queryTokens.stream()
			.filter(nameTokens::contains)
			.count();
		double exactTokenOverlap = (double) exactTokenMatches / queryTokens.size();
		score = Math.max(score, exactTokenOverlap * 0.85);

		if (queryTokens.size() == 1 && exactTokenMatches == 1) {
			score = Math.max(score, 0.95);
		}

		long similarTokenMatches = queryTokens.stream()
			.filter(queryToken -> nameTokens.stream().anyMatch(nameToken -> areSimilarTokens(queryToken, nameToken)))
			.count();
		double similarTokenOverlap = (double) similarTokenMatches / queryTokens.size();
		score = Math.max(score, similarTokenOverlap * 0.70);

		return Math.max(0.0, Math.min(1.0, score));
	}

	private static Set<String> tokens(String normalizedText) {
		return Arrays.stream(normalizedText.split(" "))
			.filter(token -> !token.isBlank())
			.collect(Collectors.toSet());
	}

	private static boolean areSimilarTokens(String queryToken, String nameToken) {
		return queryToken.equals(nameToken)
			|| queryToken.startsWith(nameToken)
			|| nameToken.startsWith(queryToken)
			|| commonPrefixLength(queryToken, nameToken) >= 5;
	}

	private static int commonPrefixLength(String left, String right) {
		int max = Math.min(left.length(), right.length());
		for (int i = 0; i < max; i++) {
			if (left.charAt(i) != right.charAt(i)) {
				return i;
			}
		}
		return max;
	}
}
