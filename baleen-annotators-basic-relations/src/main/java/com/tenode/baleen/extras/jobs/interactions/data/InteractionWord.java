package com.tenode.baleen.extras.jobs.interactions.data;

import java.util.Set;
import java.util.stream.Stream;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;

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

	public Stream<String> getAlternativeWords(Dictionary dictionary) {
		IndexWord indexWord = null;
		try {
			indexWord = dictionary.lookupIndexWord(word.getPos(), word.getLemma());
		} catch (JWNLException e) {
			// Ignore
		}

		if (indexWord == null) {
			return Stream.of(word.getLemma());
		}

		POS desiredPos = word.getPos() == POS.VERB ? POS.NOUN : POS.VERB;

		Stream<String> otherWords = indexWord.getSenses().stream()
				.filter(s -> s.getPOS() == desiredPos)
				.flatMap(s -> s.getWords().stream())
				.map(x -> x.getLemma());

		return Stream.concat(Stream.of(word.getLemma()), otherWords);
	}

}
