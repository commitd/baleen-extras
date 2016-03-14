package com.tenode.baleen.extras.common.grammar.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.gov.dstl.baleen.types.language.WordToken;

/**
 * A word and its distance in depedency graph space.
 */
public class WordDistance implements Comparable<WordDistance> {

	private final WordToken word;

	private final WordDistance wordDistance;

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
	public String toString() {
		return word.getCoveredText() + " " + distance;
	}

}