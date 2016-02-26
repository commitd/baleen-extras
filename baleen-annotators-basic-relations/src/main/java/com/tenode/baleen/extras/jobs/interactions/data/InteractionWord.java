package com.tenode.baleen.extras.jobs.interactions.data;

import java.util.Set;
import java.util.stream.Stream;

public class InteractionWord {

	private final Word word;
	private final Set<RelationPair> pairs;

	public InteractionWord(Word word, Set<RelationPair> relationPairs) {
		this.word = word;
		this.pairs = relationPairs;
	}

	public Word getWord() {
		return word;
	}

	public Set<RelationPair> getPairs() {
		return pairs;
	}

	public Stream<InteractionRelation> toRelations(String type, String subType) {
		return pairs.stream().map(p -> new InteractionRelation(type, subType, word, p.getSource(), p.getTarget()));
	}

}
