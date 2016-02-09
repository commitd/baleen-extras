package com.tenode.baleen.wordnet;

import net.sf.extjwnl.data.POS;

public final class WordNetUtils {
	public WordNetUtils() {
		// Do nothing
	}

	public static POS toPos(String pos) {
		String lc = pos.toLowerCase();

		if (lc.startsWith("n")) {
			return POS.NOUN;
		} else if (lc.startsWith("v")) {
			return POS.VERB;
		} else if (lc.startsWith("r")) {
			return POS.ADVERB;
		} else if (lc.startsWith("j")) {
			return POS.ADJECTIVE;
		} else {
			return null;
		}
	}

}
