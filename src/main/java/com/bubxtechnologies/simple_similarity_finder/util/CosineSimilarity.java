package com.bubxtechnologies.simple_similarity_finder.util;

public final class CosineSimilarity {

	private CosineSimilarity() {
	}

	public static double calculate(float[] left, float[] right) {
		if (left == null || right == null || left.length == 0 || left.length != right.length) {
			return 0.0;
		}

		double dotProduct = 0.0;
		double leftNorm = 0.0;
		double rightNorm = 0.0;

		for (int i = 0; i < left.length; i++) {
			dotProduct += left[i] * right[i];
			leftNorm += left[i] * left[i];
			rightNorm += right[i] * right[i];
		}

		if (leftNorm == 0.0 || rightNorm == 0.0) {
			return 0.0;
		}

		double cosine = dotProduct / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
		return Math.max(0.0, Math.min(1.0, cosine));
	}
}
