package com.bubxtechnologies.simple_similarity_finder.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TextNormalizerTests {

	@Test
	void shouldNormalizePolishTextAndWhitespace() {
		assertThat(TextNormalizer.normalize("  Sprzedaż   GOTÓWKĄ! "))
			.isEqualTo("sprzedaz gotowka");
	}

	@Test
	void shouldRemoveUnnecessaryPunctuation() {
		assertThat(TextNormalizer.normalize("POS - sprzedaż: kartą płatniczą"))
			.isEqualTo("pos sprzedaz karta platnicza");
	}
}
