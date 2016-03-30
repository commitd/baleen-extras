package com.tenode.baleen.extras.documentcoreference.sieves;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import uk.gov.dstl.baleen.types.semantic.ReferenceTarget;

public class InSentencePronounTest extends AbstractCoreferenceSieveTest {

	public InSentencePronounTest() {
		super(3);
	}

	@Test
	public void testPronomialSingleSentenceNoEntities()
			throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "He said he has not been in touch with her.";
		jCas.setDocumentText(text);

		processJCas();

		List<ReferenceTarget> targets = new ArrayList<>(JCasUtil.select(jCas, ReferenceTarget.class));
		assertEquals(1, targets.size());
	}
}
