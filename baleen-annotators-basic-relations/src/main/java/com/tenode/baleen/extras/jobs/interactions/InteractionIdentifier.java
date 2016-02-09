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
import com.tenode.baleen.extras.jobs.interactions.data.PatternReference;
import com.tenode.baleen.extras.jobs.interactions.data.Word;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;

public class InteractionIdentifier {

	private final int minPatternsInCluster;
	private final double threshold;
	private final Dictionary dictionary;

	public InteractionIdentifier(int minPatternsInCluster, double threshold, Dictionary dictionary) {
		this.minPatternsInCluster = minPatternsInCluster;
		this.threshold = threshold;
		this.dictionary = dictionary;
	}

	public Stream<String> process(List<PatternReference> patterns) {

		Set<Word> terms = gatherTerms(patterns);

		calculateTermFrequencies(patterns, terms);

		// Sort by number of times seen
		sort(patterns);

		// Cluster
		List<ClusteredPatterns> clusters = cluster(patterns);

		// Remove small clusters
		filterClusters(clusters);

		clusters.forEach(c -> {
			System.out.println("-----------------");
			c.getPatterns().forEach(p -> System.out.println(p));
		});

		// Find interaction words
		return extractInteractionWords(clusters);

	}

	private Stream<String> extractInteractionWords(List<ClusteredPatterns> clusters) {
		Stream<Word> distinctWords = clusters.stream().flatMap(cluster -> {
			// TODO: Should we use token or terms here?
			Map<Word, Long> wordCount = cluster.getPatterns().stream()
					.flatMap(p -> p.getTokens().stream())
					.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

			return wordCount.entrySet().stream()
					.filter(e -> e.getValue() >= 2)
					.map(e -> e.getKey());

		}).filter(w -> w.getPos() == POS.NOUN || w.getPos() == POS.VERB).distinct();

		// We need to map verbs and nouns to lemmas (which might have already been done)
		// Then map verbs to nouns and vice versa.

		return distinctWords.flatMap(w -> {
			IndexWord word = null;
			try {
				word = dictionary.lookupIndexWord(w.getPos(), w.getLemma());
			} catch (JWNLException e) {
				// Ignore
			}

			if (word == null) {
				return Stream.of(w.getLemma());
			}

			POS desiredPos = w.getPos() == POS.VERB ? POS.NOUN : POS.VERB;

			Stream<String> otherWords = word.getSenses().stream()
					.filter(s -> s.getPOS() == desiredPos)
					.flatMap(s -> s.getWords().stream())
					.map(x -> x.getLemma());

			return Stream.concat(Stream.of(word.getLemma()), otherWords);
		}).distinct();

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

			System.out.println(maxScore);

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
