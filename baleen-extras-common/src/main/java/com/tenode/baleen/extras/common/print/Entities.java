package com.tenode.baleen.extras.common.print;

import uk.gov.dstl.baleen.types.semantic.Entity;

public class Entities extends AbstractPrintAnnotator<Entity> {

	public Entities() {
		super(Entity.class);
	}

	@Override
	protected String print(Entity t) {
		StringBuilder sb = new StringBuilder();

		writeLine(sb, t.getValue());
		writeLine(sb, t.getTypeName());

		return sb.toString();
	}

}
