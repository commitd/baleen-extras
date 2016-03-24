package com.tenode.baleen.annotators.documentcoreference.sieves;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import uk.gov.dstl.baleen.types.semantic.ReferenceTarget;

public class ProperHeadMatchSieveTest extends AbstractCoreferenceSieveTest {

	public ProperHeadMatchSieveTest() {
		super(8);
	}

	@Test
	public void testProperPassSameNumbers()
			throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "The 200 people visited and then the people left.";
		jCas.setDocumentText(text);

		processJCas();

		List<ReferenceTarget> targets = new ArrayList<>(JCasUtil.select(jCas, ReferenceTarget.class));
		assertEquals(1, targets.size());
	}

	@Test
	public void testProperPassDifferentNumbers()
			throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "The 200 people visited and 100 people left.";
		jCas.setDocumentText(text);

		processJCas();

		List<ReferenceTarget> targets = new ArrayList<>(JCasUtil.select(jCas, ReferenceTarget.class));
		assertEquals(0, targets.size());
	}

	@Test
	public void testProperPassSameLocation()
			throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "We visited the south of Amercia and travelled to the deep south of America.";
		jCas.setDocumentText(text);

		processJCas();

		List<ReferenceTarget> targets = new ArrayList<>(JCasUtil.select(jCas, ReferenceTarget.class));
		assertEquals(1, targets.size());
	}

	@Test
	public void testProperPassDifferentLocations()
			throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "We visited the south of Amercia and went to the north of America.";
		jCas.setDocumentText(text);

		processJCas();

		List<ReferenceTarget> targets = new ArrayList<>(JCasUtil.select(jCas, ReferenceTarget.class));
		assertEquals(0, targets.size());
	}

}
