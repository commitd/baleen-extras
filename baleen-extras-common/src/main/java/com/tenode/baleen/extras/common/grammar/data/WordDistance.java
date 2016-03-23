package com.tenode.baleen.extras.common.grammar.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.gov.dstl.baleen.types.language.WordToken;

/**
 * A word and its distance in depedency graph space.
 */
public class WordDistance implements Comparable<WordDistance> {

	/** The word. */
	private final WordToken word;

	/** The word distance. */
	private final WordDistance wordDistance;

	/** The distance. */
	private final int distance;

	/**
	 * Instantiates a new word distance.
	 *
	 * @param word
	 *            the word
	 */
	public WordDistance(WordToken word) {
		this.word = word;
		this.wordDistance = null;
		this.distance = 0;
	}

	/**
	 * Instantiates a new word distance.
	 *
	 * @param word
	 *            the word
	 * @param wordDistance
	 *            the word distance
	 */
	public WordDistance(WordToken word, WordDistance wordDistance) {
		this.word = word;
		this.wordDistance = wordDistance;
		this.distance = wordDistance.getDistance() + 1;
	}

	/**
	 * Gets the word.
	 *
	 * @return the word
	 */
	public WordToken getWord() {
		return word;
	}

	/**
	 * Gets the distance.
	 *
	 * @return the distance
	 */
	public int getDistance() {
		return distance;
	}

	/**
	 * Gets the word distance.
	 *
	 * @return the word distance
	 */
	public WordDistance getWordDistance() {
		return wordDistance;
	}

	/**
	 * Gets the words.
	 *
	 * @return the words
	 */
	public List<WordToken> getWords() {
		if (wordDistance == null) {
			return Collections.singletonList(word);
		} else {
			return collate(new ArrayList<>(distance));
		}
	}

	/**
	 * Collate all the words in the stack into the list.
	 *
	 * @param list
	 *            the list
	 * @return the list
	 */
	protected List<WordToken> collate(List<WordToken> list) {
		if (wordDistance != null) {
			list = wordDistance.collate(list);
		}
		list.add(word);
		return list;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(WordDistance o) {
		return Integer.compare(getDistance(), o.getDistance());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + distance;
		result = prime * result + (word == null ? 0 : word.hashCode());
		result = prime * result + (wordDistance == null ? 0 : wordDistance.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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
		WordDistance other = (WordDistance) obj;
		if (distance != other.distance) {
			return false;
		}
		if (word == null) {
			if (other.word != null) {
				return false;
			}
		} else if (!word.equals(other.word)) {
			return false;
		}
		if (wordDistance == null) {
			if (other.wordDistance != null) {
				return false;
			}
		} else if (!wordDistance.equals(other.wordDistance)) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return word.getCoveredText() + " " + distance;
	}

}