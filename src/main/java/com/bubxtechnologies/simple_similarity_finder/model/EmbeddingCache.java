package com.bubxtechnologies.simple_similarity_finder.model;

import java.util.List;

public record EmbeddingCache(String model, List<EmbeddingCacheEntry> entries) {
}
