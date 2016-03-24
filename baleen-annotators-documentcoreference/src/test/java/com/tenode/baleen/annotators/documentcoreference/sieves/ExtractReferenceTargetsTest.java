package com.tenode.baleen.annotators.documentcoreference.sieves;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import uk.gov.dstl.baleen.types.common.Person;
import uk.gov.dstl.baleen.types.semantic.Location;
import uk.gov.dstl.baleen.types.semantic.ReferenceTarget;

public class ExtractReferenceTargetsTest extends AbstractCoreferenceSieveTest {

	public ExtractReferenceTargetsTest() {
		super(0);
	}

	@Test
	public void testExistingRefTargets() throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "Chris went to London and he saw Big Ben there.";
		// there - london
		jCas.setDocumentText(text);

		Person chris = new Person(jCas);
		chris.setBegin(text.indexOf("Chris"));
		chris.setEnd(chris.getBegin() + "Chris".length());
		chris.addToIndexes();

		ReferenceTarget target = new ReferenceTarget(jCas);
		target.addToIndexes();

		Location london = new Location(jCas);
		london.setBegin(text.indexOf("London"));
		london.setEnd(london.getBegin() + "London".length());
		london.setReferent(target);
		london.addToIndexes();

		Location there = new Location(jCas);
		there.setBegin(text.indexOf("there"));
		there.setEnd(there.getBegin() + "there".length());
		there.setReferent(target);
		there.addToIndexes();

		processJCas();

		// We should have a reference target and it should be different to the previous, as its been
		// recreated.
		Collection<ReferenceTarget> targets = JCasUtil.select(jCas, ReferenceTarget.class);
		assertEquals(1, targets.size());
		assertTrue(targets.iterator().next().getInternalId() != target.getInternalId());
	}
}
