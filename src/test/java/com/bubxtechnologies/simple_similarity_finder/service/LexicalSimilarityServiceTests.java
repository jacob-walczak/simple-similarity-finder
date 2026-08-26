package com.bubxtechnologies.simple_similarity_finder.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LexicalSimilarityServiceTests {

	private final LexicalSimilarityService service = new LexicalSimilarityService();

	@Test
	void shouldReturnMaximumScoreForExactNormalizedMatch() {
		double score = service.score("sprzedaż gotówką", "sprzedaż gotówką");

		assertThat(score).isEqualTo(1.0);
	}

	@Test
	void shouldStronglyRewardExactSingleTokenMatch() {
		double score = service.score("POS", "POS - sprzedaż gotówką");

		assertThat(score).isGreaterThanOrEqualTo(0.95);
	}

	@Test
	void shouldReturnLowScoreForUnrelatedName() {
		double score = service.score("sprzedaż gotówką", "CRM - aktualizacja danych klienta");

		assertThat(score).isLessThan(0.20);
	}
}
