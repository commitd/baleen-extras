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

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments [POS word1 ...]
	 * @throws JWNLException
	 *             the JWNL exception
	 */
	public static void main(String args[]) throws JWNLException {
		final Dictionary dictionary = Dictionary.getDefaultResourceInstance();

		final POS pos = POS.getPOSForLabel(args[0]);
		for (int i = 1; i < args.length; i++) {
			final String word = args[i];
			final IndexWord indexWord = dictionary.lookupIndexWord(pos, word);

			if (indexWord == null) {
				System.err.println(word + " not found");
				return;
			}

			final List<Synset> senses = indexWord.getSenses();
			if (senses.isEmpty()) {
				System.out.println(word + " has no senses");
				return;
			} else {
				System.out.println(word + " supersense is " + senses.get(0).getLexFileName());
			}
		}
	}
}
