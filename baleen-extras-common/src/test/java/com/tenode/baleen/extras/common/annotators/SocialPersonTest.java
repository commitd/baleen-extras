package com.tenode.baleen.extras.common.annotators;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import uk.gov.dstl.baleen.annotators.testing.AbstractAnnotatorTest;
import uk.gov.dstl.baleen.types.common.Person;

public class SocialPersonTest extends AbstractAnnotatorTest {

	public SocialPersonTest() {
		super(SocialPerson.class);
	}

	@Test
	public void test() throws AnalysisEngineProcessException, ResourceInitializationException {
		jCas.setDocumentText("Contact me on @baleen");

		processJCas();

		final Collection<Person> select = JCasUtil.select(jCas, Person.class);
		final Person next = select.iterator().next();
		assertEquals("@baleen", next.getValue());
	}

}
