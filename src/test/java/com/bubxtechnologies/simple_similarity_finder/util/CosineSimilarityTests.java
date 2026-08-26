package com.bubxtechnologies.simple_similarity_finder.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CosineSimilarityTests {

	@Test
	void shouldReturnOneForIdenticalVectors() {
		assertThat(CosineSimilarity.calculate(new float[] { 1, 2, 3 }, new float[] { 1, 2, 3 }))
			.isEqualTo(1.0);
	}

	@Test
	void shouldReturnZeroForOrthogonalVectors() {
		assertThat(CosineSimilarity.calculate(new float[] { 1, 0 }, new float[] { 0, 1 }))
			.isEqualTo(0.0);
	}

	@Test
	void shouldClampOppositeVectorsToZero() {
		assertThat(CosineSimilarity.calculate(new float[] { 1, 0 }, new float[] { -1, 0 }))
			.isEqualTo(0.0);
	}
}
