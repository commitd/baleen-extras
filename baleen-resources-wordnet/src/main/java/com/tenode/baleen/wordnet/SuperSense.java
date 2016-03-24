package com.tenode.baleen.wordnet;

import java.util.List;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.dictionary.Dictionary;

/**
 * A command line application convert words into supersenses.
 */
public final class SuperSense {

	private SuperSense() {
		// Singleton
	}

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments [POS word1 ...]
	 * @throws JWNLException
	 *             the JWNL exception
	 */
	public static void main(String[] args) {
		Dictionary dictionary;
		try {
			dictionary = Dictionary.getDefaultResourceInstance();
		} catch (JWNLException e) {
			System.err.println("Unable to open dictionary file:" + e.getMessage());
			return;
		}
		if (args.length < 2) {
			System.err.println("Usage: pos words...");
			return;
		}

		final POS pos = POS.getPOSForLabel(args[0]);
		if (pos == null) {
			System.err.println(args[0] + " is not a valid POS");
			return;
		}

		for (int i = 1; i < args.length; i++) {
			final String word = args[i];
			try {
				final IndexWord indexWord = dictionary.lookupIndexWord(pos, word);

				if (indexWord == null) {
					System.err.println(word + " not found");
					return;
				}

				final List<Synset> senses = indexWord.getSenses();
				if (senses.isEmpty()) {
					System.out.println(word + " has no senses");
				} else {
					System.out.println(word + " supersense is " + senses.get(0).getLexFileName());
				}
			} catch (JWNLException e) {
				System.err
						.println("Error occured processing " + pos.getLabel() + " " + word + ": " + e.getMessage());
			}
		}
	}
}
