package com.tenode.baleen.extras.documentcoreference.sieves;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import uk.gov.dstl.baleen.types.common.Person;
import uk.gov.dstl.baleen.types.semantic.ReferenceTarget;

public class RelaxedHeadMatchSieveTest extends AbstractCoreferenceSieveTest {

	public RelaxedHeadMatchSieveTest() {
		super(9);
	}

	@Test
	public void testRelaxedHead()
			throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "Circuit Judge N. Sanders has been seen talking to James when the Judge said ok.";
		jCas.setDocumentText(text);

		Person fsc = new Person(jCas);
		fsc.setBegin(text.indexOf("Circuit Judge N. Sanders"));
		fsc.setEnd(fsc.getBegin() + "Circuit Judge N. Sanders".length());
		fsc.addToIndexes();

		Person fc = new Person(jCas);
		fc.setBegin(text.indexOf("Judge", fsc.getEnd()));
		fc.setEnd(fc.getBegin() + "Judge".length());
		fc.addToIndexes();

		Person j = new Person(jCas);
		j.setBegin(text.indexOf("James"));
		j.setEnd(j.getBegin() + "James".length());
		j.addToIndexes();

		processJCas();

		List<ReferenceTarget> targets = new ArrayList<>(JCasUtil.select(jCas, ReferenceTarget.class));
		assertEquals(1, targets.size());
	}
}
