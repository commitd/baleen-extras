package com.tenode.baleen.extras.common.print;

import uk.gov.dstl.baleen.types.language.Pattern;

/**
 * Print out all patterns.
 */
public class Patterns extends AbstractPrintAnnotator<Pattern> {

	/**
	 * Instantiates a new pattern annotator.
	 */
	public Patterns() {
		super(Pattern.class);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.tenode.baleen.extras.common.print.AbstractPrintAnnotator#print(uk.gov.dstl.baleen.types.
	 * Base)
	 */
	@Override
	protected String print(Pattern t) {
		final StringBuilder sb = new StringBuilder();

		writeLine(sb, "text", t.getCoveredText());
		writeLine(sb, "words", t.getWords());

		return sb.toString();
	}

}
