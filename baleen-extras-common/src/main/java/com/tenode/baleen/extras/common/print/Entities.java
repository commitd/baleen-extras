package com.tenode.baleen.extras.common.print;

import uk.gov.dstl.baleen.types.semantic.Entity;

/**
 * Print out all entities.
 */
public class Entities extends AbstractPrintAnnotator<Entity> {

	/**
	 * Instantiates a new annotator.
	 */
	public Entities() {
		super(Entity.class);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.tenode.baleen.extras.common.print.AbstractPrintAnnotator#print(uk.gov.dstl.baleen.types.
	 * Base)
	 */
	@Override
	protected String print(Entity t) {
		final StringBuilder sb = new StringBuilder();

		writeLine(sb, t.getValue());
		writeLine(sb, t.getTypeName());
		writeLine(sb, String.format("%d %d", t.getBegin(), t.getEnd()));

		return sb.toString();
	}

}
