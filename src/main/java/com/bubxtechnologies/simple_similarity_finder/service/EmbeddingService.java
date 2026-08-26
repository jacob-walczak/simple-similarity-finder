package com.bubxtechnologies.simple_similarity_finder.service;

import java.util.ArrayList;
import java.util.List;

import com.bubxtechnologies.simple_similarity_finder.model.TestCase;
import com.bubxtechnologies.simple_similarity_finder.util.ApplicationException;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingService {

	private static final int PROGRESS_STEP = 100;

	private final EmbeddingModel embeddingModel;
	private final String modelName;

	public EmbeddingService(EmbeddingModel embeddingModel,
			@Value("${spring.ai.ollama.embedding.model:nomic-embed-text}") String modelName) {
		this.embeddingModel = embeddingModel;
		this.modelName = modelName;
	}

	public List<TestCase> embedTestCases(List<TestCase> testCases) {
		System.out.println("Generating embeddings using " + modelName + "...");
		List<TestCase> embeddedTestCases = new ArrayList<>(testCases.size());

		for (int i = 0; i < testCases.size(); i++) {
			TestCase testCase = testCases.get(i);
			embeddedTestCases.add(testCase.withEmbedding(embed(testCase.name())));

			int embeddedCount = i + 1;
			if (embeddedCount % PROGRESS_STEP == 0 || embeddedCount == testCases.size()) {
				System.out.println("Embedded " + embeddedCount + "/" + testCases.size());
			}
		}

		return List.copyOf(embeddedTestCases);
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
