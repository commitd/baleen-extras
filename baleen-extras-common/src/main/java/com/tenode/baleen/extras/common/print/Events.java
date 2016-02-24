package com.tenode.baleen.extras.common.print;

import uk.gov.dstl.baleen.types.semantic.ComplexEvent;

public class Events extends AbstractPrintAnnotator<ComplexEvent> {

	public Events() {
		super(ComplexEvent.class);
	}

	@Override
	protected String print(ComplexEvent t) {
		StringBuilder sb = new StringBuilder();

		writeLine(sb, t.getValue());
		writeLine(sb, t.getEventType());
		writeLine(sb, t.getEntities());
		writeLine(sb, t.getArguments());

		return sb.toString();
	}

}
