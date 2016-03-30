package com.tenode.baleen.extras.documentcoreference.sieves;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import uk.gov.dstl.baleen.types.common.Organisation;
import uk.gov.dstl.baleen.types.semantic.ReferenceTarget;

// TODO: This is just one of the three phases! (need to think of examples for the others!)
public class StrictHeadATest extends AbstractCoreferenceSieveTest {

	public StrictHeadATest() {
		super(5);
	}

	@Test
	public void testStrictHeadPass()
			throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "The Florida Supreme Court sat today, and the Florida Court made a decision.";
		jCas.setDocumentText(text);

		Organisation fsc = new Organisation(jCas);
		fsc.setBegin(text.indexOf("Florida Supreme Court"));
		fsc.setEnd(fsc.getBegin() + "Florida Supreme Court".length());
		fsc.addToIndexes();

		Organisation fc = new Organisation(jCas);
		fc.setBegin(text.indexOf("Florida Court"));
		fc.setEnd(fc.getBegin() + "Florida Court".length());
		fc.addToIndexes();

		processJCas();

		List<ReferenceTarget> targets = new ArrayList<>(JCasUtil.select(jCas, ReferenceTarget.class));
		assertEquals(1, targets.size());
	}

}
