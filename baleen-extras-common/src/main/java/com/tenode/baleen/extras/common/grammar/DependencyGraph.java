package com.tenode.baleen.extras.common.grammar;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import uk.gov.dstl.baleen.types.language.Dependency;
import uk.gov.dstl.baleen.types.language.WordToken;

/**
 * A graph of grammar dependencies within an annotated jCas.
 *
 * Whilst UIMA annotations can store the output of a dependency grammar it is difficult to work with
 * and slow to query. This class builds a cache which means finding nearest neighbours (based on
 * dependency distance) faster and easier.
 *
 * The JCAS must have been annotated by a dependency grammer (MaltParser, ClearNlp) before passing
 * to build().
 *
 */
public class DependencyGraph {

	private static final Logger LOGGER = LoggerFactory.getLogger(DependencyGraph.class);

	private final SetMultimap<WordToken, WordToken> edges = HashMultimap.create();

	private DependencyGraph() {

	}

	private void addEdge(final WordToken governor, final WordToken dependent) {
		edges.put(governor, dependent);
		edges.put(dependent, governor);
	}

	/**
	 * Find the nearest neighbours within dependency distance links of the provided start
	 * dependencies.
	 *
	 * @param distance
	 *            the dependency distance
	 * @param start
	 *            array / of words to start from
	 * @return the (set of) words within range
	 */
	public Set<WordToken> extractWords(final int distance, final Dependency... start) {
		return extractWords(distance, Arrays.asList(start));
	}

	/**
	 * Find the nearest neighbours within dependency distance links of the provide start
	 * dependencies.
	 *
	 * @param distance
	 *            the dependency distance
	 * @param start
	 *            the start words (as list)
	 * @return the (set of) words within range
	 */
	public Set<WordToken> extractWords(final int distance, final Collection<Dependency> start) {
		final Set<WordToken> words = new HashSet<>();

		if (distance <= 0) {
			return words;
		}

		final int governorDistance = distance - 1;
		for (final Dependency d : start) {
			if (governorDistance > 0) {
				extractWords(words, governorDistance, d.getGovernor());
			}
			extractWords(words, distance, d.getDependent());
		}

		return words;
	}

	/**
	 * Find the nearest neighbours within dependency distance links of the provided start
	 * dependencies.
	 *
	 * @param distance
	 *            the dependency distance
	 * @param start
	 *            array / of words to start from
	 * @return the (set of) words within range
	 */
	public Set<WordToken> nearestWords(final int distance, final WordToken... start) {
		return nearestWords(distance, Arrays.asList(start));
	}

	/**
	 * Find the nearest neighbours within dependency distance links of the provide start
	 * dependencies.
	 *
	 * @param distance
	 *            the dependency distance
	 * @param start
	 *            the start words (as list)
	 * @return the (set of) words within range
	 */
	public Set<WordToken> nearestWords(final int distance, final Collection<WordToken> start) {
		final Set<WordToken> words = new HashSet<>();

		if (distance <= 0) {
			return words;
		}

		for (final WordToken d : start) {
			extractWords(words, distance, d);
		}

		return words;

	}

	private void extractWords(final Set<WordToken> collector, final int distance, final WordToken token) {
		// The word itself
		collector.add(token);

		// TODO: Depth first, We potentially revisit the same node repeatedly,
		// so this could be more efficient.

		final Set<WordToken> set = edges.get(token);

		if (set != null) {
			collector.addAll(set);

			final int newDistance = distance - 1;
			if (newDistance > 0) {
				set.forEach(a -> {
					extractWords(collector, newDistance, a);
				});

			}
		}

	}

	/**
	 * Log the dependency graph to the logger for debugging.
	 */
	public void log() {
		final StringBuilder sb = new StringBuilder();
		edges.asMap().entrySet().stream().forEach(e -> {
			sb.append(e.getKey().getCoveredText());
			sb.append(": ");
			e.getValue().stream().forEach(w -> sb.append(" " + w.getCoveredText()));
			sb.append("\n");
		});

		DependencyGraph.LOGGER.info("Dependeny graph: {}", sb.toString());
	}

	/**
	 * Build a dependency graph from a JCAS which has already been processed through a dependency
	 * grammar.
	 *
	 * Thus the JCAS as Dependency annotations.
	 *
	 * @param jCas
	 *            the jCAS to process.
	 * @return the dependency graph (non-null)
	 */
	public static DependencyGraph build(final JCas jCas) {
		final DependencyGraph graph = new DependencyGraph();

		JCasUtil.select(jCas, Dependency.class).stream().forEach(d -> {
			if ((d.getDependencyType() == null || !d.getDependencyType().equals("ROOT")) && d.getGovernor() != null
					&& d.getDependent() != null) {
				graph.addEdge(d.getGovernor(), d.getDependent());
			}
		});

		return graph;
	}
}