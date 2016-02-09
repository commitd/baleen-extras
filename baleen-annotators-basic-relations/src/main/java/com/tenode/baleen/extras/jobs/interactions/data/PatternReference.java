package com.tenode.baleen.extras.jobs.interactions.data;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class PatternReference {

	private final String id;

	private final List<Word> tokens;

	private int[] termFrequency;

	private int termMagnitude;

	public PatternReference(String id, List<Word> tokens) {
		this.id = id;
		this.tokens = tokens;
	}

	public PatternReference(String id, Word... tokens) {
		this.id = id;
		this.tokens = Arrays.asList(tokens);
	}

	public String getId() {
		return id;
	}

	public List<Word> getTokens() {
		return tokens;
	}

	public int getTFMagnitude() {
		return termMagnitude;
	}

	public int[] getTermFrequency() {
		return termFrequency;
	}

	public void calculateTermFrequency(Set<Word> terms) {
		termFrequency = new int[terms.size()];
		termMagnitude = 0;

		// Naive implementation, but perhaps correct way given that the tokens should be very small
		// in general
		int i = 0;
		for (Word term : terms) {
			for (Word token : tokens) {
				// Note we ignore the POS here
				if (term.getLemma().equals(token.getLemma())) {
					termFrequency[i]++;
					termMagnitude++;
				}
			}
			i++;
		}

	}

	public double calculateSimilarity(PatternReference pattern) {
		int[] otherTF = pattern.getTermFrequency();

		double score = 0;
		for (int i = 0; i < termFrequency.length; i++) {
			score += termFrequency[i] * otherTF[i];
		}

		// NOTE: Departure from the paper (they don't do the division to normalize the result)
		// TODO: Should this have the c + d in it (ie be (k(p1,p2) not the dot product)
		return score / (pattern.getTFMagnitude() * getTFMagnitude());
	}

	@Override
	public String toString() {
		return id + ":" + tokens.stream().map(Word::getLemma).collect(Collectors.joining(";"));
	}
}