package com.tenode.baleen.extras.common.annotators;

import java.util.regex.Matcher;

import org.apache.uima.jcas.JCas;

import uk.gov.dstl.baleen.annotators.regex.helpers.AbstractRegexAnnotator;
import uk.gov.dstl.baleen.types.common.Person;

public class SocialPerson extends AbstractRegexAnnotator<Person> {

	// We need the \b in so we don't have emails
	private static final String PATTERN = "\\B@[A-Za-z0-9-_]+\\b";

	public SocialPerson() {
		super(PATTERN, false, 1.0);
	}

	@Override
	protected Person create(JCas jCas, Matcher matcher) {
		Person p = new Person(jCas);
		p.setValue(matcher.group());
		return p;
	}

}
