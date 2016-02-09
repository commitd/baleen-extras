package com.tenode.baleen.extras.jobs.interactions.data;

import java.util.ArrayList;
import java.util.List;

public class ClusteredPatterns {

	private final List<PatternReference> patterns = new ArrayList<>();

	public ClusteredPatterns() {
		// Do nothing
	}

	public ClusteredPatterns(PatternReference pattern) {
		patterns.add(pattern);
	}

	public double calculateSimilarity(PatternReference pattern) {

		if (patterns.isEmpty()) {
			return 0;
		} else {
			double sum = patterns.stream().map(p -> p.calculateSimilarity(pattern)).reduce(0.0, (a, b) -> a + b);
			return sum / size();
		}
	}

	public void add(PatternReference pr) {
		patterns.add(pr);
	}

	public List<PatternReference> getPatterns() {
		return patterns;
	}

	public int size() {
		return patterns.size();
	}

}
