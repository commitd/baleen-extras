package com.tenode.baleen.extras.common.annotators;

import java.util.regex.Matcher;

import org.apache.uima.jcas.JCas;

import uk.gov.dstl.baleen.annotators.regex.helpers.AbstractRegexAnnotator;
import uk.gov.dstl.baleen.types.common.Person;

/**
 * Annotates Person using social methods, for example @username.
 *
 * @baleen.javadoc
 */
public class SocialPerson extends AbstractRegexAnnotator<Person> {

	// We need the \b in so we don't have emails
	private static final String PATTERN = "\\B@[A-Za-z0-9-_]+\\b";

	/**
	 * Instantiates a new annotator.
	 */
	public SocialPerson() {
		super(PATTERN, false, 1.0);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * uk.gov.dstl.baleen.annotators.regex.helpers.AbstractRegexAnnotator#create(org.apache.uima.
	 * jcas.JCas, java.util.regex.Matcher)
	 */
	@Override
	protected Person create(JCas jCas, Matcher matcher) {
		final Person p = new Person(jCas);
		p.setValue(matcher.group());
		return p;
	}

}
