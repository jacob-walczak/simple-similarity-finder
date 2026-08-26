package com.bubxtechnologies.simple_similarity_finder.model;

public record EmbeddingCacheEntry(String id, String name, String textHash, float[] embedding) {
}
