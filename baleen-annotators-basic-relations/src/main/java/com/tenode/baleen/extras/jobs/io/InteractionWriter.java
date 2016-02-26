package com.tenode.baleen.extras.jobs.io;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import com.tenode.baleen.extras.jobs.interactions.data.InteractionRelation;

@FunctionalInterface
public interface InteractionWriter extends AutoCloseable {
	default void initialise() throws IOException {

	}

	void write(InteractionRelation interaction, Collection<String> alternatives) throws IOException;

	default void write(InteractionRelation interaction) throws IOException {
		write(interaction, Collections.singletonList(interaction.getWord().getLemma()));
	}

	default void destroy() {

	}

	@Override
	default void close() {
		destroy();
	}
}
