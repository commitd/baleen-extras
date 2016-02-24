package com.tenode.baleen.extras.common.grammar.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.gov.dstl.baleen.types.language.WordToken;

public class WordDistance implements Comparable<WordDistance> {

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