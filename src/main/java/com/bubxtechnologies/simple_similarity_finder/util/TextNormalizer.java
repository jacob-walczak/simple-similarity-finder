package com.bubxtechnologies.simple_similarity_finder.util;

import java.text.Normalizer;
import java.util.Locale;

public final class TextNormalizer {

	private TextNormalizer() {
	}

	public static String normalize(String text) {
		if (text == null) {
			return "";
		}

		String lowerCase = text.toLowerCase(Locale.ROOT).trim();
		String withoutDiacritics = Normalizer.normalize(lowerCase, Normalizer.Form.NFD)
			.replaceAll("\\p{M}", "")
			.replace('ł', 'l');

		return withoutDiacritics
			.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}\\s]", " ")
			.replaceAll("\\s+", " ")
			.trim();
	}
}
