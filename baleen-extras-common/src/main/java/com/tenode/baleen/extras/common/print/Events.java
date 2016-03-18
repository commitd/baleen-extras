package com.tenode.baleen.extras.common.print;

import uk.gov.dstl.baleen.types.semantic.ComplexEvent;

/**
 * Print out all events.
 */
public class Events extends AbstractPrintAnnotator<ComplexEvent> {

	/**
	 * Instantiates a new annotator.
	 */
	public Events() {
		super(ComplexEvent.class);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.tenode.baleen.extras.common.print.AbstractPrintAnnotator#print(uk.gov.dstl.baleen.types.
	 * Base)
	 */
	@Override
	protected String print(ComplexEvent t) {
		final StringBuilder sb = new StringBuilder();

		writeLine(sb, t.getValue());
		// writeLine(sb, "Tokens", t.getTokens());
		writeLine(sb, "Type", t.getEventType());
		writeLine(sb, "Entities", t.getEntities());
		writeLine(sb, "Arguments", t.getArguments());

		return sb.toString();
	}

}
