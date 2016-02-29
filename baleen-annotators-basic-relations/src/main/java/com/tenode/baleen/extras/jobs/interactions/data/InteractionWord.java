package com.tenode.baleen.extras.jobs.interactions.data;

import java.util.Set;
import java.util.stream.Stream;

/**
 * A word which represents an interaction trigger (ie relation of interest) between two types of
 * entities.
 */
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

	public Stream<InteractionDefinition> toRelations(String type, String subType) {
		return pairs.stream().map(p -> new InteractionDefinition(type, subType, word, p.getSource(), p.getTarget()));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (pairs == null ? 0 : pairs.hashCode());
		result = prime * result + (word == null ? 0 : word.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		InteractionWord other = (InteractionWord) obj;
		if (pairs == null) {
			if (other.pairs != null) {
				return false;
			}
		} else if (!pairs.equals(other.pairs)) {
			return false;
		}
		if (word == null) {
			if (other.word != null) {
				return false;
			}
		} else if (!word.equals(other.word)) {
			return false;
		}
		return true;
	}

}
