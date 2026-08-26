package com.bubxtechnologies.simple_similarity_finder.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.bubxtechnologies.simple_similarity_finder.config.SimilarityFinderProperties;
import com.bubxtechnologies.simple_similarity_finder.model.EmbeddingCache;
import com.bubxtechnologies.simple_similarity_finder.model.EmbeddingCacheEntry;
import com.bubxtechnologies.simple_similarity_finder.model.TestCase;
import com.bubxtechnologies.simple_similarity_finder.util.ApplicationException;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class EmbeddingCacheService {

	private final SimilarityFinderProperties properties;
	private final ObjectMapper objectMapper;

	public EmbeddingCacheService(SimilarityFinderProperties properties, ObjectMapper objectMapper) {
		this.properties = properties;
		this.objectMapper = objectMapper;
	}

	public List<TestCase> loadOrGenerateEmbeddings(
			List<TestCase> testCases,
			String modelName,
			Function<TestCase, float[]> embeddingGenerator) {

		Path cachePath = Paths.get(properties.embeddingCachePath());
		EmbeddingCache cache = readCache(cachePath, modelName);
		Map<String, EmbeddingCacheEntry> cachedEntries = entriesById(cache);

		List<TestCase> result = new ArrayList<>(testCases.size());
		List<EmbeddingCacheEntry> updatedEntries = new ArrayList<>(testCases.size());
		int reused = 0;
		int generated = 0;
		boolean generationLogged = false;

		for (TestCase testCase : testCases) {
			String textHash = textHash(testCase);
			EmbeddingCacheEntry cachedEntry = cachedEntries.get(testCase.id());
			if (isReusable(cachedEntry, textHash)) {
				result.add(testCase.withEmbedding(cachedEntry.embedding()));
				updatedEntries.add(cachedEntry);
				reused++;
			}
			else {
				if (!generationLogged) {
					System.out.println("Generating embeddings using " + modelName + "...");
					generationLogged = true;
				}
				float[] embedding = embeddingGenerator.apply(testCase);
				result.add(testCase.withEmbedding(embedding));
				updatedEntries.add(new EmbeddingCacheEntry(testCase.id(), testCase.name(), textHash, embedding));
				generated++;
				printProgress(generated, testCases.size());
			}
		}

		if (cache != null) {
			System.out.println("Reused " + reused + " embeddings.");
			System.out.println("Generated " + generated + " embeddings.");
		}

		if (cache == null || generated > 0 || updatedEntries.size() != cachedEntries.size()) {
			writeCache(cachePath, new EmbeddingCache(modelName, updatedEntries));
		}

		return List.copyOf(result);
	}

	public String textHash(TestCase testCase) {
		return sha256(testCase.id() + "|" + testCase.name());
	}

	public boolean canReuse(EmbeddingCacheEntry entry, TestCase testCase) {
		return isReusable(entry, textHash(testCase));
	}

	public boolean isCacheForModel(EmbeddingCache cache, String modelName) {
		return cache != null && modelName.equals(cache.model());
	}

	public EmbeddingCache readFrom(Path cachePath) throws IOException {
		return objectMapper.readValue(cachePath.toFile(), EmbeddingCache.class);
	}

	public void writeTo(Path cachePath, EmbeddingCache cache) throws IOException {
		Path parent = cachePath.getParent();
		if (parent != null) {
			Files.createDirectories(parent);
		}
		objectMapper.writerWithDefaultPrettyPrinter().writeValue(cachePath.toFile(), cache);
	}

	private EmbeddingCache readCache(Path cachePath, String modelName) {
		if (!Files.exists(cachePath)) {
			System.out.println("Embedding cache not found.");
			return null;
		}

		try {
			if (Files.size(cachePath) == 0) {
				System.out.println("Embedding cache is empty. Rebuilding cache.");
				return null;
			}

			System.out.println("Loading embedding cache...");
			EmbeddingCache cache = readFrom(cachePath);
			if (!isCacheForModel(cache, modelName)) {
				System.out.println("Embedding cache model differs from current model. Rebuilding cache.");
				return null;
			}

			int cachedCount = cache.entries() == null ? 0 : cache.entries().size();
			System.out.println("Loaded " + cachedCount + " cached embeddings.");
			return cache;
		}
		catch (IOException | RuntimeException e) {
			System.out.println("Embedding cache is invalid. Rebuilding cache.");
			return null;
		}
	}

	private void writeCache(Path cachePath, EmbeddingCache cache) {
		try {
			writeTo(cachePath, cache);
			System.out.println("Saved embedding cache: " + properties.embeddingCachePath());
		}
		catch (IOException e) {
			throw new ApplicationException("Cannot save embedding cache: " + properties.embeddingCachePath(), e);
		}
	}

	private static Map<String, EmbeddingCacheEntry> entriesById(EmbeddingCache cache) {
		Map<String, EmbeddingCacheEntry> entries = new HashMap<>();
		if (cache == null || cache.entries() == null) {
			return entries;
		}

		for (EmbeddingCacheEntry entry : cache.entries()) {
			if (entry != null && entry.id() != null) {
				entries.put(entry.id(), entry);
			}
		}
		return entries;
	}

	private static boolean isReusable(EmbeddingCacheEntry entry, String textHash) {
		return entry != null
			&& entry.embedding() != null
			&& entry.embedding().length > 0
			&& textHash.equals(entry.textHash());
	}

	private static String sha256(String text) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(text.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 is not available", e);
		}
	}

	private static void printProgress(int generatedCount, int totalCount) {
		if (generatedCount % 100 == 0 || generatedCount == totalCount) {
			System.out.println("Embedded " + generatedCount + "/" + totalCount);
		}
	}
}
