package com.bubxtechnologies.simple_similarity_finder.model;

public record TestCase(String id, String name, String normalizedName, float[] embedding) {

	public TestCase withEmbedding(float[] embedding) {
		return new TestCase(id, name, normalizedName, embedding);
	}
}
