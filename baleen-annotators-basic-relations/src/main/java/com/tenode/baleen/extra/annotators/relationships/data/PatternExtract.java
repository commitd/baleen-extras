package com.tenode.baleen.extra.annotators.relationships.data;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import uk.gov.dstl.baleen.types.language.WordToken;
import uk.gov.dstl.baleen.types.semantic.Entity;

public final class PatternExtract {
	private final int start;
	private final int end;

	private final Entity from;
	private final Entity to;

	private List<WordToken> words;

	public PatternExtract(final Entity from, final Entity to, final int start, final int end) {
		this.from = from;
		this.to = to;
		this.start = start;
		this.end = end;
	}

	public Entity getFrom() {
		return from;
	}

	public Entity getTo() {
		return to;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public void setWordTokens(final List<WordToken> words) {
		this.words = words;
	}

	public List<WordToken> getWordTokens() {
		return words;
	}

	public boolean contains(final String documentText, final String... needles) {
		final String text = documentText.substring(start, end);
		return Arrays.stream(needles).anyMatch(text::contains);
	}

	public String getText() {
		if (words == null) {
			return "";
		}

		return words.stream()
				.map(w -> w.getCoveredText()).collect(Collectors.joining(" "));
	}

	public boolean isEmpty() {
		return words == null || words.isEmpty();
	}

}