package com.bubxtechnologies.simple_similarity_finder.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.bubxtechnologies.simple_similarity_finder.config.SimilarityFinderProperties;
import com.bubxtechnologies.simple_similarity_finder.model.EmbeddingCache;
import com.bubxtechnologies.simple_similarity_finder.model.EmbeddingCacheEntry;
import com.bubxtechnologies.simple_similarity_finder.model.TestCase;
import com.bubxtechnologies.simple_similarity_finder.util.TextNormalizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;

class EmbeddingCacheServiceTests {

	@TempDir
	Path tempDir;

	@Test
	void shouldWriteAndReadCache() throws IOException {
		EmbeddingCacheService service = service(tempDir.resolve("cache.json"));
		EmbeddingCache cache = new EmbeddingCache("nomic-embed-text",
				List.of(new EmbeddingCacheEntry("TC-0001", "POS - sprzedaż gotówką", "hash", new float[] { 1.0f, 2.0f })));

		Path cachePath = tempDir.resolve("cache.json");
		service.writeTo(cachePath, cache);
		EmbeddingCache loaded = service.readFrom(cachePath);

		assertThat(loaded.model()).isEqualTo("nomic-embed-text");
		assertThat(loaded.entries()).hasSize(1);
		assertThat(loaded.entries().getFirst().embedding()).containsExactly(1.0f, 2.0f);
	}

	@Test
	void shouldReuseEmbeddingForSameHash() throws IOException {
		Path cachePath = tempDir.resolve("cache.json");
		EmbeddingCacheService service = service(cachePath);
		TestCase testCase = testCase("TC-0001", "POS - sprzedaż gotówką");
		service.writeTo(cachePath, new EmbeddingCache("nomic-embed-text",
				List.of(new EmbeddingCacheEntry(testCase.id(), testCase.name(), service.textHash(testCase), new float[] { 0.5f }))));
		AtomicInteger generated = new AtomicInteger();

		List<TestCase> result = service.loadOrGenerateEmbeddings(List.of(testCase), "nomic-embed-text", ignored -> {
			generated.incrementAndGet();
			return new float[] { 9.0f };
		});

		assertThat(generated).hasValue(0);
		assertThat(result.getFirst().embedding()).containsExactly(0.5f);
	}

	@Test
	void shouldGenerateEmbeddingWhenNameChanges() throws IOException {
		Path cachePath = tempDir.resolve("cache.json");
		EmbeddingCacheService service = service(cachePath);
		TestCase oldTestCase = testCase("TC-0001", "POS - sprzedaż gotówką");
		TestCase changedTestCase = testCase("TC-0001", "POS - sprzedaż gotówką z rabatem");
		service.writeTo(cachePath, new EmbeddingCache("nomic-embed-text",
				List.of(new EmbeddingCacheEntry(oldTestCase.id(), oldTestCase.name(), service.textHash(oldTestCase), new float[] { 0.5f }))));
		AtomicInteger generated = new AtomicInteger();

		List<TestCase> result = service.loadOrGenerateEmbeddings(List.of(changedTestCase), "nomic-embed-text", ignored -> {
			generated.incrementAndGet();
			return new float[] { 9.0f };
		});

		assertThat(generated).hasValue(1);
		assertThat(result.getFirst().embedding()).containsExactly(9.0f);
	}

	@Test
	void shouldTreatDifferentModelAsStaleCache() throws IOException {
		Path cachePath = tempDir.resolve("cache.json");
		EmbeddingCacheService service = service(cachePath);
		TestCase testCase = testCase("TC-0001", "POS - sprzedaż gotówką");
		service.writeTo(cachePath, new EmbeddingCache("old-model",
				List.of(new EmbeddingCacheEntry(testCase.id(), testCase.name(), service.textHash(testCase), new float[] { 0.5f }))));
		AtomicInteger generated = new AtomicInteger();

		List<TestCase> result = service.loadOrGenerateEmbeddings(List.of(testCase), "nomic-embed-text", ignored -> {
			generated.incrementAndGet();
			return new float[] { 9.0f };
		});

		assertThat(generated).hasValue(1);
		assertThat(result.getFirst().embedding()).containsExactly(9.0f);
	}

	private static TestCase testCase(String id, String name) {
		return new TestCase(id, name, TextNormalizer.normalize(name), null);
	}

	private static EmbeddingCacheService service(Path cachePath) {
		SimilarityFinderProperties properties = new SimilarityFinderProperties(
				"classpath:test-cases.csv",
				cachePath.toString(),
				10,
				0.65,
				0.35,
				0.50,
				true);
		return new EmbeddingCacheService(properties, new ObjectMapper());
	}
}
