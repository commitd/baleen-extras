package com.tenode.baleen.extras.common.annotators;

import java.util.regex.Matcher;

import org.apache.uima.jcas.JCas;

import uk.gov.dstl.baleen.annotators.regex.helpers.AbstractRegexAnnotator;
import uk.gov.dstl.baleen.types.semantic.Entity;

/**
 * Annotate acronyms, found in a basic fashion.
 *
 * @baleen.javadoc
 */
public class Acronyms extends AbstractRegexAnnotator<Entity> {

	private static final double CONFIDENCE = 0.5;
	private static final String PATTERN = "\\b[A-Z]{2,}\\b";

	/**
	 * Instantiates a new annotator.
	 */
	public Acronyms() {
		// Note lower confidence so that it can be overridden on merge
		super(PATTERN, true, CONFIDENCE);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * uk.gov.dstl.baleen.annotators.regex.helpers.AbstractRegexAnnotator#create(org.apache.uima.
	 * jcas.JCas, java.util.regex.Matcher)
	 */
	@Override
	protected Entity create(JCas jCas, Matcher matcher) {
		final Entity e = new Entity(jCas);
		e.setValue(matcher.group());
		return e;
	}

}
