package com.bubxtechnologies.simple_similarity_finder.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.bubxtechnologies.simple_similarity_finder.config.SimilarityFinderProperties;
import com.bubxtechnologies.simple_similarity_finder.model.TestCase;
import com.bubxtechnologies.simple_similarity_finder.util.ApplicationException;
import com.bubxtechnologies.simple_similarity_finder.util.TextNormalizer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class TestCaseCsvLoader {

	private final SimilarityFinderProperties properties;
	private final ResourceLoader resourceLoader;

	public TestCaseCsvLoader(SimilarityFinderProperties properties, ResourceLoader resourceLoader) {
		this.properties = properties;
		this.resourceLoader = resourceLoader;
	}

	public List<TestCase> load() {
		Resource resource = resourceLoader.getResource(properties.csvPath());
		if (!resource.exists()) {
			throw new ApplicationException("CSV file not found: " + properties.csvPath());
		}

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
			String header = reader.readLine();
			if (header == null) {
				throw new ApplicationException("CSV file is empty: " + properties.csvPath());
			}
			if (!"id,name".equals(header.trim())) {
				throw new ApplicationException("Invalid CSV header. Expected: id,name");
			}

			List<TestCase> testCases = new ArrayList<>();
			String line;
			int lineNumber = 1;
			while ((line = reader.readLine()) != null) {
				lineNumber++;
				if (line.isBlank()) {
					continue;
				}

				List<String> columns = parseCsvLine(line);
				if (columns.size() != 2 || columns.get(0).isBlank() || columns.get(1).isBlank()) {
					throw new ApplicationException("Invalid CSV record at line " + lineNumber + ": expected id,name");
				}

				String id = columns.get(0).trim();
				String name = columns.get(1).trim();
				testCases.add(new TestCase(id, name, TextNormalizer.normalize(name), null));
			}

			if (testCases.isEmpty()) {
				throw new ApplicationException("CSV file does not contain any Test Cases: " + properties.csvPath());
			}

			return List.copyOf(testCases);
		}
		catch (IOException e) {
			throw new ApplicationException("Cannot read CSV file: " + properties.csvPath(), e);
		}
	}

	private static List<String> parseCsvLine(String line) {
		List<String> columns = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean quoted = false;

		for (int i = 0; i < line.length(); i++) {
			char character = line.charAt(i);
			if (character == '"') {
				if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
					current.append('"');
					i++;
				}
				else {
					quoted = !quoted;
				}
			}
			else if (character == ',' && !quoted) {
				columns.add(current.toString());
				current.setLength(0);
			}
			else {
				current.append(character);
			}
		}

		if (quoted) {
			throw new ApplicationException("Invalid CSV record: unmatched quote");
		}

		columns.add(current.toString());
		return columns;
	}
}
