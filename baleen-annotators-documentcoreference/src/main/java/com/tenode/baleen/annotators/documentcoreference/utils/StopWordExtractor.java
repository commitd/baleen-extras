package com.tenode.baleen.annotators.documentcoreference.utils;

import java.util.List;

import com.google.common.base.Splitter;
import com.tenode.baleen.annotators.documentcoreference.data.Mention;
import com.tenode.baleen.extras.common.language.StopWordRemover;

/**
 * Remove stop words from mentions.
 */
public final class StopWordExtractor {
	private static final StopWordRemover stopWordRemover = new StopWordRemover();

	private static final Splitter WHITESPACE_SPLITTER = Splitter.on(" ").omitEmptyStrings().trimResults();

	private StopWordExtractor() {
		// Do nothing - singleton
	}

	// TODO: This should at a cluster level
	public static boolean hasSubsetOfNonStopWords(Mention a, Mention b) {
		final List<String> aNonStop = getNonStopWords(a);
		final List<String> bNonStop = getNonStopWords(b);

		// TODO: This should not include the head word? See the paper for clarification.

		// NOTE: This is ordered, a is earlier than b and it is unusal to introduce more information
		// to an entity later in the document

		// NOTE: We enforce that the set isn't empty otherwise we aren't really testing anything
		return !aNonStop.isEmpty() && !bNonStop.isEmpty() && aNonStop.containsAll(bNonStop);
	}

	public static List<String> getNonStopWords(Mention a) {
		return WHITESPACE_SPLITTER.splitToList(stopWordRemover.clean(a.getText().toLowerCase()));
	}

}
