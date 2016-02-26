package com.tenode.baleen.extras.jobs.writers;

import java.util.List;

import com.tenode.baleen.extras.jobs.interactions.data.InteractionWord;

public interface InteractionWordWriter {
	default void initialise() {

	}

	void write(InteractionWord word, String relationshipType, String lemma, List<String> alternatives);

	default void destroy() {

	}
}
