package com.tenode.baleen.extras.common.print;

import uk.gov.dstl.baleen.types.language.Pattern;

public class Patterns extends AbstractPrintAnnotator<Pattern> {

	public Patterns() {
		super(Pattern.class);
	}

	@Override
	protected String print(Pattern t) {
		StringBuilder sb = new StringBuilder();

		writeLine(sb, t.getCoveredText());

		return sb.toString();
	}

}
