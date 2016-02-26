package com.tenode.baleen.extras.jobs.interactions.data;

public class InteractionRelation {
	private final String type;

	private final String subType;

	private final Word word;

	private final String source;

	private final String target;

	public InteractionRelation(String type, String subType, Word word, String source, String target) {
		this.type = type;
		this.subType = subType;
		this.word = word;
		this.source = source;
		this.target = target;
	}

	public String getType() {
		return type;
	}

	public String getSubType() {
		return subType;
	}

	public String getSource() {
		return source;
	}

	public String getTarget() {
		return target;
	}

	public Word getWord() {
		return word;
	}

}
