package com.tenode.baleen.annotators.coreference.sieves;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Ignore;
import org.junit.Test;

import uk.gov.dstl.baleen.types.common.Organisation;
import uk.gov.dstl.baleen.types.common.Person;
import uk.gov.dstl.baleen.types.semantic.ReferenceTarget;

public class PreciseConstructsSieveTest extends AbstractCoreferenceSieveTest {

	public PreciseConstructsSieveTest() {
		super(4);
	}

	@Test
	public void testPreciseConstructApositive() throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "The prime minister, David Cameron explained on Tuesday.";
		// david camera - prime minister
		jCas.setDocumentText(text);

		Person p = new Person(jCas);
		p.setBegin(text.indexOf("David Cameron"));
		p.setEnd(p.getBegin() + "David Cameron".length());
		p.addToIndexes();

		processJCas();

		List<ReferenceTarget> targets = new ArrayList<>(JCasUtil.select(jCas, ReferenceTarget.class));
		assertEquals(1, targets.size());
	}

	@Test
	public void testPreciseConstructPredicate() throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "David Cameron is the prime minister.";
		// david camera - prime minister
		jCas.setDocumentText(text);

		Person p = new Person(jCas);
		p.setBegin(text.indexOf("David Cameron"));
		p.setEnd(p.getBegin() + "David Cameron".length());
		p.addToIndexes();

		processJCas();

		List<ReferenceTarget> targets = new ArrayList<>(JCasUtil.select(jCas, ReferenceTarget.class));
		assertEquals(1, targets.size());
	}

	// NOT IMPLEMENTED
	@Test
	@Ignore
	public void testPreciseConstructRole() throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "President Obama visited today.";
		// president - obama
		jCas.setDocumentText(text);

		processJCas();

		List<ReferenceTarget> targets = new ArrayList<>(JCasUtil.select(jCas, ReferenceTarget.class));
		assertEquals(1, targets.size());
	}

	@Test
	public void testPreciseConstructRelativePronoun()
			throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "The police want to catch a man who ran away.";
		// man - who
		jCas.setDocumentText(text);

		processJCas();

		List<ReferenceTarget> targets = new ArrayList<>(JCasUtil.select(jCas, ReferenceTarget.class));
		assertEquals(1, targets.size());
	}

	@Test
	public void testPreciseConstructAcronym()
			throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "The British Broadcasting Corporation or the BBC if you prefer shows television programmes.";
		// British Broadcasting Corporation - BBC
		jCas.setDocumentText(text);

		// We need these in otherwise we just get one long setence from the mention detector

		Organisation beeb = new Organisation(jCas);
		beeb.setBegin(text.indexOf("British Broadcasting Corporation"));
		beeb.setEnd(beeb.getBegin() + "British Broadcasting Corporation".length());
		beeb.addToIndexes();

		Organisation bbc = new Organisation(jCas);
		bbc.setBegin(text.indexOf("BBC"));
		bbc.setEnd(bbc.getBegin() + "BBC".length());
		bbc.addToIndexes();

		processJCas();

		List<ReferenceTarget> targets = new ArrayList<>(JCasUtil.select(jCas, ReferenceTarget.class));
		assertEquals(1, targets.size());
	}
}
