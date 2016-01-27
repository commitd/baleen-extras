package com.tenode.baleen.abta.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import uk.gov.dstl.baleen.types.language.Dependency;
import uk.gov.dstl.baleen.types.language.WordToken;

public class DependencyGraph {

	private final SetMultimap<WordToken, WordToken> edges = HashMultimap.create();

	private DependencyGraph() {

	}

	private void addEdge(WordToken governor, WordToken dependent) {
		edges.put(governor, dependent);
		edges.put(dependent, governor);
	}

	public Set<WordToken> extractWords(int distance, Collection<Dependency> start) {
		Set<WordToken> words = new HashSet<>();

		for (Dependency d : start) {
			extractWords(words, distance, d.getGovernor());
			extractWords(words, distance, d.getDependent());
		}

		return words;

	}

	private void extractWords(Set<WordToken> collector, int distance, WordToken token) {

		// TODO: Depth first, We potentially revisit the same node repeatedly, so this could be
		// more efficient.

		Set<WordToken> set = edges.get(token);

		if (set != null) {
			collector.addAll(set);

			int newDistance = distance - 1;
			if (newDistance > 0) {
				set.forEach(a -> {
					extractWords(collector, newDistance, a);
				});

			}
		}

	}

	public static DependencyGraph build(JCas jCas) {
		DependencyGraph graph = new DependencyGraph();

		JCasUtil.select(jCas, Dependency.class).stream().forEach(d -> {
			if (!d.getDependencyType().equals("ROOT") && d.getGovernor() != null && d.getDependent() != null) {
				graph.addEdge(d.getGovernor(), d.getDependent());
			}
		});

		return graph;

	}

	public void print() {
		edges.asMap().entrySet().stream().forEach(e -> {
			System.out.println(e.getKey().getCoveredText());
			e.getValue().stream().forEach(w -> System.out.println("\t" + w.getCoveredText()));
		});
	}
}