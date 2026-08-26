package com.bubxtechnologies.simple_similarity_finder.service;

import java.util.List;

import com.bubxtechnologies.simple_similarity_finder.model.TestCase;
import com.bubxtechnologies.simple_similarity_finder.util.ApplicationException;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingService {

	private final EmbeddingModel embeddingModel;
	private final EmbeddingCacheService embeddingCacheService;
	private final String modelName;

	public EmbeddingService(EmbeddingModel embeddingModel, EmbeddingCacheService embeddingCacheService,
			@Value("${spring.ai.ollama.embedding.model:nomic-embed-text}") String modelName) {
		this.embeddingModel = embeddingModel;
		this.embeddingCacheService = embeddingCacheService;
		this.modelName = modelName;
	}

	public List<TestCase> embedTestCases(List<TestCase> testCases) {
		return embeddingCacheService.loadOrGenerateEmbeddings(testCases, modelName, testCase -> embed(testCase.name()));
	}

	public float[] embedQuery(String query) {
		return embed(query);
	}

	private float[] embed(String text) {
		try {
			return embeddingModel.embed(text);
		}
		catch (RuntimeException e) {
			throw new ApplicationException("""
					Cannot generate embeddings using Ollama.
					Check that local Ollama is running and that model '%s' is available.
					Prepare it with: ollama pull %s""".formatted(modelName, modelName), e);
		}
	}
}
