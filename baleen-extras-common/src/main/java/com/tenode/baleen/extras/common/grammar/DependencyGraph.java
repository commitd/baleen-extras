package com.tenode.baleen.extras.common.grammar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.uima.cas.text.AnnotationFS;
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

	private final SetMultimap<WordToken, Edge> edges;

	private final SetMultimap<WordToken, Dependency> dependents;
	private final SetMultimap<WordToken, Dependency> governors;

	private DependencyGraph() {
		edges = HashMultimap.create();
		dependents = HashMultimap.create();
		governors = HashMultimap.create();

	}

	private DependencyGraph(SetMultimap<WordToken, Edge> edges, SetMultimap<WordToken, Dependency> dependentMap,
			SetMultimap<WordToken, Dependency> governorMap) {
		this.edges = edges;
		this.dependents = dependentMap;
		this.governors = governorMap;
	}

	public Set<Dependency> getDependents(WordToken word) {
		return dependents.get(word);
	}

	public Set<Dependency> getGovernors(WordToken word) {
		return governors.get(word);
	}

	private void addEdge(final Dependency dependency) {
		WordToken governor = dependency.getGovernor();
		WordToken dependent = dependency.getDependent();
		Edge edge = new Edge(dependent, dependency, governor);
		edges.put(governor, edge);
		edges.put(dependent, edge);
		dependents.put(dependent, dependency);
		governors.put(governor, dependency);
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
		return extractWords(distance, d -> true, start);
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
	public Set<WordToken> extractWords(final int distance, Predicate<Dependency> predicate, final Dependency... start) {
		return extractWords(distance, predicate, Arrays.asList(start));
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
	public Set<WordToken> extractWords(final int distance, Predicate<Dependency> predicate,
			final Collection<Dependency> start) {
		final Set<WordToken> words = new HashSet<>();

		if (distance <= 0) {
			return words;
		}

		final int governorDistance = distance - 1;
		for (final Dependency d : start) {
			if (governorDistance > 0) {
				extractWords(words, governorDistance, predicate, d.getGovernor());
			}
			extractWords(words, distance, predicate, d.getDependent());
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
		return nearestWords(distance, d -> true, Arrays.asList(start));
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
	public Set<WordToken> nearestWords(final int distance, Predicate<Dependency> predicate, final WordToken... start) {
		return nearestWords(distance, predicate, Arrays.asList(start));
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
	public Set<WordToken> nearestWords(final int distance, Predicate<Dependency> predicate,
			final Collection<WordToken> start) {
		final Set<WordToken> words = new HashSet<>();

		if (distance <= 0) {
			return words;
		}

		for (final WordToken d : start) {
			extractWords(words, distance, predicate, d);
		}

		return words;

	}

	private void extractWords(final Set<WordToken> collector, final int distance, Predicate<Dependency> predicate,
			final WordToken token) {
		// The word itself
		collector.add(token);

		// TODO: Depth first, We potentially revisit the same node repeatedly,
		// so this could be more efficient.

		final List<WordToken> set = edges.get(token).stream()
				.filter(e -> predicate.test(e.getDependency()))
				.map(e -> e.getOther(token))
				.collect(Collectors.toList());

		if (set != null) {
			collector.addAll(set);

			final int newDistance = distance - 1;
			if (newDistance > 0) {
				set.forEach(a -> {
					extractWords(collector, newDistance, predicate, a);
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
			sb.append("\t");
			sb.append(e.getKey().getCoveredText());
			sb.append(": ");
			e.getValue().stream()
					.map(x -> x.getOther(e.getKey()))
					.forEach(w -> sb.append(" " + w.getCoveredText()));
			sb.append("\n");
		});

		final StringBuilder governorSb = new StringBuilder();
		governors.asMap().entrySet().stream().forEach(e -> {
			governorSb.append("\t");
			governorSb.append(e.getKey().getCoveredText());
			governorSb.append(": ");
			e.getValue().stream()
					.forEach(w -> governorSb.append(" " + w.getCoveredText() + "[" + w.getDependencyType() + "]"));
			governorSb.append("\n");
		});

		final StringBuilder dependentSb = new StringBuilder();
		dependents.asMap().entrySet().stream().forEach(e -> {
			dependentSb.append("\t");
			dependentSb.append(e.getKey().getCoveredText());
			dependentSb.append(": ");
			e.getValue().stream()

					.forEach(w -> dependentSb.append(" " + w.getCoveredText() + "[" + w.getDependencyType() + "]"));
			dependentSb.append("\n");
		});

		DependencyGraph.LOGGER.info("Dependency graph:  Edges:\n{}\n  Governors\n{}\n  Dependent\n{}", sb.toString(),
				governorSb,
				dependentSb);
	}

	/**
	 * Create a new (sub) graph where words are only those matched by the filter.
	 *
	 * @param predicate
	 *            the predicate
	 * @return the new filtered dependency graph
	 */
	public DependencyGraph filter(Predicate<WordToken> predicate) {
		SetMultimap<WordToken, Edge> filteredEdges = HashMultimap.create();
		SetMultimap<WordToken, Dependency> filteredDependent = HashMultimap.create();
		SetMultimap<WordToken, Dependency> filteredGovernor = HashMultimap.create();

		edges.asMap().entrySet().stream()
				.filter(w -> {
					System.out.println(w.getKey().getPartOfSpeech() + " " + w.getKey().getCoveredText());
					return predicate.test(w.getKey());
				}).forEach(e -> {
					WordToken key = e.getKey();
					e.getValue().stream()
							.filter(edge -> predicate.test(edge.getOther(key)))
							.forEach(v -> filteredEdges.put(key, v));
				});

		governors.asMap().keySet().stream()
				.filter(predicate)
				.forEach(k -> filteredGovernor.putAll(k, governors.get(k)));

		dependents.asMap().keySet().stream()
				.filter(predicate)
				.forEach(k -> filteredDependent.putAll(k, dependents.get(k)));

		return new DependencyGraph(filteredEdges, filteredDependent, filteredGovernor);
	}

	private void addDependency(Dependency dependency) {
		if ((dependency.getDependencyType() == null || !dependency.getDependencyType().equals("ROOT"))
				&& dependency.getGovernor() != null
				&& dependency.getDependent() != null) {
			addEdge(dependency);

		}
	}

	public Set<WordToken> getWords() {
		return Collections.unmodifiableSet(edges.keySet());
	}

	public List<WordToken> shortestPath(Collection<WordToken> from, Collection<WordToken> to, int maxDistance) {
		if (from.isEmpty() || to.isEmpty() || maxDistance <= -1) {
			return Collections.emptyList();
		}

		Set<WordToken> visited = new HashSet<>();
		PriorityQueue<WordDistance> queue = new PriorityQueue<>();
		from.stream().forEach(t -> {
			queue.add(new WordDistance(t));
			visited.add(t);
		});

		while (!queue.isEmpty()) {
			WordDistance wd = queue.poll();
			LOGGER.debug("{}", wd);

			if (to.contains(wd.getWord())) {
				return wd.getWords();
			}

			if (wd.getDistance() < maxDistance) {
				Set<WordToken> nextWords = edges.get(wd.getWord()).stream()
						.map(w -> w.getOther(wd.getWord()))
						.collect(Collectors.toSet());
				nextWords.removeAll(visited);
				nextWords.stream().forEach(t -> {
					queue.add(new WordDistance(t, wd));
					visited.add(t);
				});
			}

		}

		return Collections.emptyList();

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

		JCasUtil.select(jCas, Dependency.class).stream().forEach(graph::addDependency);

		return graph;
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
	public static DependencyGraph build(final JCas jCas, AnnotationFS annnotation) {
		final DependencyGraph graph = new DependencyGraph();

		JCasUtil.selectCovered(jCas, Dependency.class, annnotation).stream().forEach(graph::addDependency);

		return graph;
	}

	private static class Edge {
		private final WordToken from;
		private final Dependency dependency;
		private final WordToken to;

		public Edge(WordToken from, Dependency dependency, WordToken to) {
			this.from = from;
			this.dependency = dependency;
			this.to = to;
		}

		public WordToken getFrom() {
			return from;
		}

		public Dependency getDependency() {
			return dependency;
		}

		public WordToken getTo() {
			return to;
		}

		public WordToken getOther(WordToken token) {
			return token.equals(to) ? from : to;
		}

		public boolean isTo(WordToken token) {
			return token.equals(to);
		}

		public boolean isFrom(WordToken token) {
			return token.equals(from);
		}
	}

	private static class WordDistance implements Comparable<WordDistance> {

		private final WordToken word;

		private final WordDistance wordDistance;

		private final int distance;

		public WordDistance(WordToken word) {
			this.word = word;
			this.wordDistance = null;
			this.distance = 0;
		}

		public WordDistance(WordToken word, WordDistance wordDistance) {
			this.word = word;
			this.wordDistance = wordDistance;
			this.distance = wordDistance.getDistance() + 1;
		}

		public WordToken getWord() {
			return word;
		}

		public int getDistance() {
			return distance;
		}

		public WordDistance getWordDistance() {
			return wordDistance;
		}

		public List<WordToken> getWords() {
			if (wordDistance == null) {
				return Collections.singletonList(word);
			} else {
				return collate(new ArrayList<>(distance));
			}
		}

		protected List<WordToken> collate(List<WordToken> list) {
			if (wordDistance != null) {
				list = wordDistance.collate(list);
			}
			list.add(word);
			return list;
		}

		@Override
		public int compareTo(WordDistance o) {
			return Integer.compare(getDistance(), o.getDistance());
		}

		@Override
		public String toString() {
			return word.getCoveredText() + " " + distance;
		}

	}

}