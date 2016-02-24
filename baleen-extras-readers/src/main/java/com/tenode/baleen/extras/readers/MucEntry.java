package com.tenode.baleen.extras.readers;

public class MucEntry {
	private final String id;

	private String text;

	public MucEntry(String id, String text) {
		this.id = id;
		this.text = text;
	}

	public String getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}