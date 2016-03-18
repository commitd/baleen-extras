package com.tenode.baleen.extras.common.print;

import uk.gov.dstl.baleen.types.semantic.Relation;

/**
 * Print out all relations.
 */
public class Relations extends AbstractPrintAnnotator<Relation> {

	/**
	 * Instantiates a new annotator.
	 */
	public Relations() {
		super(Relation.class);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.tenode.baleen.extras.common.print.AbstractPrintAnnotator#print(uk.gov.dstl.baleen.types.
	 * Base)
	 */
	@Override
	protected String print(Relation t) {
		final StringBuilder sb = new StringBuilder();
		writeLine(sb, "type", t.getRelationshipType() + ": " + t.getRelationSubType());
		writeLine(sb, "source", t.getSource());
		writeLine(sb, "target", t.getTarget());
		return sb.toString();
	}
}
