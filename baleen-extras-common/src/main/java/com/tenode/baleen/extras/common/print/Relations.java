package com.tenode.baleen.extras.common.print;

import uk.gov.dstl.baleen.types.semantic.Relation;

public class Relations extends AbstractPrintAnnotator<Relation> {

	public Relations() {
		super(Relation.class);
	}

	@Override
	protected String print(Relation t) {
		StringBuilder sb = new StringBuilder();
		writeLine(sb, t.getRelationshipType() + ": " + t.getRelationSubType());
		writeLine(sb, t.getSource());
		writeLine(sb, t.getTarget());
		return sb.toString();
	}
}
