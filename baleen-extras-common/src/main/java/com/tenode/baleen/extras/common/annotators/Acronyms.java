package com.tenode.baleen.extras.common.annotators;

import java.util.regex.Matcher;

import org.apache.uima.jcas.JCas;

import uk.gov.dstl.baleen.annotators.regex.helpers.AbstractRegexAnnotator;
import uk.gov.dstl.baleen.types.semantic.Entity;

public class Acronyms extends AbstractRegexAnnotator<Entity> {

	private static final String PATTERN = "\\b[A-Z]{2,}\\b";

	public Acronyms() {
		// Note lower confidence so that it can be overridden on merge
		super(PATTERN, true, 0.5);
	}

	@Override
	protected Entity create(JCas jCas, Matcher matcher) {
		Entity e = new Entity(jCas);
		e.setValue(matcher.group());
		return e;
	}

}
