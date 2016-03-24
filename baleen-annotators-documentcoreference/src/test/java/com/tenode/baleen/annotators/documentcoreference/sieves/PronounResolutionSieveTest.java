package com.tenode.baleen.annotators.documentcoreference.sieves;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import uk.gov.dstl.baleen.types.common.Person;
import uk.gov.dstl.baleen.types.semantic.ReferenceTarget;

public class PronounResolutionSieveTest extends AbstractCoreferenceSieveTest {

	public PronounResolutionSieveTest() {
		super(10);
	}

	@Test
	public void testPronomialSingleSentence()
			throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "John went to see Lucy and he ate with her.";
		jCas.setDocumentText(text);

		Person chris = new Person(jCas);
		chris.setBegin(text.indexOf("John"));
		chris.setEnd(chris.getBegin() + "John".length());
		chris.addToIndexes();

		Person lucy = new Person(jCas);
		lucy.setBegin(text.indexOf("Lucy"));
		lucy.setEnd(lucy.getBegin() + "Lucy".length());
		lucy.addToIndexes();

		processJCas();

		List<ReferenceTarget> targets = new ArrayList<>(JCasUtil.select(jCas, ReferenceTarget.class));
		assertEquals(2, targets.size());

		// TODO: Need to test what that its he which is matched
	}

	@Test
	public void testPronomialTwoSentence()
			throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "John went to see Lucy at the weekend. That was the first time that he saw her there.";
		jCas.setDocumentText(text);

		Person chris = new Person(jCas);
		chris.setBegin(text.indexOf("John"));
		chris.setEnd(chris.getBegin() + "John".length());
		chris.addToIndexes();

		Person lucy = new Person(jCas);
		lucy.setBegin(text.indexOf("Lucy"));
		lucy.setEnd(lucy.getBegin() + "Lucy".length());
		lucy.addToIndexes();

		processJCas();

		List<ReferenceTarget> targets = new ArrayList<>(JCasUtil.select(jCas, ReferenceTarget.class));
		assertEquals(2, targets.size());

		// TODO: Need to test what that its he which is matched
	}

}
