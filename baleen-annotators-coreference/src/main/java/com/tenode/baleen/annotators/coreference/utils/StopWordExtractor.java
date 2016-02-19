package com.tenode.baleen.annotators.coreference.utils;

import java.util.List;

import com.google.common.base.Splitter;
import com.tenode.baleen.annotators.coreference.data.Mention;
import com.tenode.baleen.extras.common.language.StopWordRemover;

public final class StopWordExtractor {
	private static final StopWordRemover stopWordRemover = new StopWordRemover();

	private static final Splitter WHITESPACE_SPLITTER = Splitter.on(" ").omitEmptyStrings().trimResults();

	private StopWordExtractor() {
		// Do nothing - singleton
	}

	// TODO: This should at a cluster level
	public static boolean hasSubsetOfNonStopWords(Mention a, Mention b) {
		List<String> aNonStop = getNonStopWords(a);
		List<String> bNonStop = getNonStopWords(b);

		// TODO: This should not include the head word? See the paper for clarification.

		// NOTE: This is ordered, a is earlier than b and it is unusal to introduce more information
		// to an entity later in the document
		return aNonStop.containsAll(bNonStop);
	}

	public static List<String> getNonStopWords(Mention a) {
		return WHITESPACE_SPLITTER.splitToList(stopWordRemover.clean(a.getText().toLowerCase()));
	}

}
