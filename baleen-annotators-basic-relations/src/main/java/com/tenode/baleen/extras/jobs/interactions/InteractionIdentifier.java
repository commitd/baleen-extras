package com.tenode.baleen.extras.jobs.interactions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.tenode.baleen.extras.jobs.interactions.data.ClusteredPatterns;
import com.tenode.baleen.extras.jobs.interactions.data.InteractionWord;
import com.tenode.baleen.extras.jobs.interactions.data.PatternReference;
import com.tenode.baleen.extras.jobs.interactions.data.RelationPair;
import com.tenode.baleen.extras.jobs.interactions.data.Word;

import net.sf.extjwnl.data.POS;
import uk.gov.dstl.baleen.uima.UimaMonitor;

public class InteractionIdentifier {

	private final int minPatternsInCluster;
	private final double threshold;
	private final UimaMonitor monitor;

	public InteractionIdentifier(UimaMonitor monitor, int minPatternsInCluster, double threshold) {
		this.monitor = monitor;
		this.minPatternsInCluster = minPatternsInCluster;
		this.threshold = threshold;
	}

	public Stream<InteractionWord> process(List<PatternReference> patterns) {

		Set<Word> terms = gatherTerms(patterns);

		monitor.info("Gathered {} terms", terms.size());

		calculateTermFrequencies(patterns, terms);

		monitor.info("Calculated frequencies");

		// Sort by number of times seen
		sort(patterns);

		monitor.info("Sorted patterns by frequency");

		// Cluster
		List<ClusteredPatterns> clusters = cluster(patterns);

		monitor.info("Patterns clustered into {} clusters", clusters.size());

		// Remove small clusters
		filterClusters(clusters);

		monitor.info("Patterns filtered to {} clusters", clusters.size());

		// For debugging:
		// clusters.forEach(c -> {
		// System.out.println("-----------------");
		// c.getPatterns().forEach(p -> System.out.println(p));
		// });

		monitor.info("Finding interaction words");

		// Find interaction words
		return extractInteractionWords(clusters);

	}

	private Stream<InteractionWord> extractInteractionWords(List<ClusteredPatterns> clusters) {
		Stream<InteractionWord> distinctWords = clusters.stream().flatMap(cluster -> {
			// TODO: Should we use token or terms here?
			Map<Word, Long> wordCount = cluster.getPatterns().stream()
					.flatMap(p -> p.getTokens().stream())
					.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

			Set<RelationPair> relationPairs = cluster.getPairs();

			return wordCount.entrySet().stream()
					.filter(e -> e.getValue() >= 2)
					.map(e -> new InteractionWord(e.getKey(), relationPairs));

		}).filter(w -> w.getWord().getPos() == POS.NOUN || w.getWord().getPos() == POS.VERB).distinct();

		// We need to map verbs and nouns to lemmas (which might have already been done)
		// Then map verbs to nouns and vice versa.

		return distinctWords;

	}

	private Set<Word> gatherTerms(List<PatternReference> patterns) {
		return patterns.stream()
				.flatMap(p -> p.getTokens().stream())
				.collect(Collectors.toSet());
	}

	private void calculateTermFrequencies(List<PatternReference> patterns, Set<Word> terms) {
		patterns.forEach(p -> p.calculateTermFrequency(terms));
	}

	private void sort(List<PatternReference> patterns) {
		Collections.sort(patterns, (a, b) -> b.getTFMagnitude() - a.getTFMagnitude());
	}

	private List<ClusteredPatterns> cluster(List<PatternReference> patterns) {
		final List<ClusteredPatterns> clusters = new ArrayList<>();

		final double threshold = calculateThreshold(patterns);

		for (final PatternReference pr : patterns) {
			double maxScore = Double.NEGATIVE_INFINITY;
			ClusteredPatterns bestCluster = null;

			for (final ClusteredPatterns cp : clusters) {
				double score = cp.calculateSimilarity(pr);

				if (score > maxScore) {
					maxScore = score;
					bestCluster = cp;
				}
			}

			if (maxScore > threshold && bestCluster != null) {
				// use the existing cluster
				bestCluster.add(pr);
			} else {
				// Create a new cluster
				clusters.add(new ClusteredPatterns(pr));
			}
		}

		return clusters;
	}

	private double calculateThreshold(List<PatternReference> patterns) {
		// TODO:
		// Paper uses an algorithm which O(number of patterns * 2)
		// if we do that, it would be nice to cache the result in order to avoid recalcuating the
		// similarities again
		// At any rate as is defines the number of clusters which is important to the user it should
		// be specified
		// Since we normalize we know this will be in the range (0,1) which helps determine clusters
		return threshold;
	}

	private void filterClusters(List<ClusteredPatterns> clusters) {
		if (minPatternsInCluster != 0) {
			Iterator<ClusteredPatterns> iterator = clusters.iterator();
			while (iterator.hasNext()) {
				ClusteredPatterns patterns = iterator.next();

				if (patterns.size() < minPatternsInCluster) {
					iterator.remove();
				}
			}
		}
	}

}
