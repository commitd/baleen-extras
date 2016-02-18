package com.tenode.baleen.annotators.coreference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Ignore;
import org.junit.Test;

import com.tenode.baleen.annotators.maltparser.MaltParser;
import com.tenode.baleen.annotators.opennlp.OpenNLPParser;
import com.tenode.baleen.resources.coreference.GenderMultiplicityResource;
import com.tenode.baleen.wordnet.annotators.WordNetLemmatizer;
import com.tenode.baleen.wordnet.resources.WordNetResource;

import uk.gov.dstl.baleen.annotators.language.OpenNLP;
import uk.gov.dstl.baleen.annotators.testing.AbstractMultiAnnotatorTest;
import uk.gov.dstl.baleen.resources.SharedOpenNLPModel;
import uk.gov.dstl.baleen.types.common.Organisation;
import uk.gov.dstl.baleen.types.common.Person;
import uk.gov.dstl.baleen.types.semantic.Location;
import uk.gov.dstl.baleen.types.semantic.ReferenceTarget;

public class CoreferenceTest extends AbstractMultiAnnotatorTest {

	@Override
	protected AnalysisEngine[] createAnalysisEngines() throws ResourceInitializationException {
		ExternalResourceDescription parserChunkingDesc = ExternalResourceFactory
				.createExternalResourceDescription("parserChunking", SharedOpenNLPModel.class);

		ExternalResourceDescription wordnetDesc = ExternalResourceFactory.createExternalResourceDescription("wordnet",
				WordNetResource.class);

		ExternalResourceDescription tokensDesc = ExternalResourceFactory.createExternalResourceDescription("tokens",
				SharedOpenNLPModel.class);
		ExternalResourceDescription sentencesDesc = ExternalResourceFactory
				.createExternalResourceDescription("sentences", SharedOpenNLPModel.class);
		ExternalResourceDescription posDesc = ExternalResourceFactory.createExternalResourceDescription("posTags",
				SharedOpenNLPModel.class);
		ExternalResourceDescription chunksDesc = ExternalResourceFactory
				.createExternalResourceDescription("phraseChunks", SharedOpenNLPModel.class);

		ExternalResourceDescription gMDesc = ExternalResourceFactory
				.createExternalResourceDescription(Coreference.PARAM_GENDER_MULTIPLICITY,
						GenderMultiplicityResource.class);

		return asArray(
				createAnalysisEngine(OpenNLP.class, "tokens",
						tokensDesc, "sentences", sentencesDesc, "posTags", posDesc, "phraseChunks", chunksDesc),
				createAnalysisEngine(WordNetLemmatizer.class, "wordnet", wordnetDesc),
				createAnalysisEngine(OpenNLPParser.class, "parserChunking",
						parserChunkingDesc),
				createAnalysisEngine(MaltParser.class),
				createAnalysisEngine(Coreference.class, Coreference.PARAM_GENDER_MULTIPLICITY, gMDesc));
	}

	@Test
	public void test() throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "Chris went to London and he saw Big Ben there.";
		jCas.setDocumentText(text);

		Person chris = new Person(jCas);
		chris.setBegin(text.indexOf("Chris"));
		chris.setEnd(chris.getBegin() + "Chris".length());
		chris.addToIndexes();

		Location london = new Location(jCas);
		london.setBegin(text.indexOf("London"));
		london.setEnd(london.getBegin() + "London".length());
		london.addToIndexes();

		processJCas();
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

	@Test
	public void testExactStringMatch() throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "Chris went to London and in London he saw Big Ben.";
		// london - london
		jCas.setDocumentText(text);

		Person chris = new Person(jCas);
		chris.setBegin(text.indexOf("Chris"));
		chris.setEnd(chris.getBegin() + "Chris".length());
		chris.addToIndexes();

		Location london = new Location(jCas);
		london.setBegin(text.indexOf("London"));
		london.setEnd(london.getBegin() + "London".length());
		london.addToIndexes();

		Location london2 = new Location(jCas);
		london2.setBegin(text.indexOf("London", london.getEnd()));
		london2.setEnd(london2.getBegin() + "London".length());
		london2.addToIndexes();

		processJCas();

		List<ReferenceTarget> targets = new ArrayList<>(JCasUtil.select(jCas, ReferenceTarget.class));
		List<Location> location = new ArrayList<>(JCasUtil.select(jCas, Location.class));
		assertEquals(1, targets.size());
		assertSame(targets.get(0), location.get(0).getReferent());
		assertSame(targets.get(0), location.get(1).getReferent());
	}

	@Test
	public void testRelaxStringMatch() throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "The University of Warwick is near Coventry and that was the University at which Chris studied.";
		// university of warwick - university
		jCas.setDocumentText(text);

		Person chris = new Person(jCas);
		chris.setBegin(text.indexOf("Chris"));
		chris.setEnd(chris.getBegin() + "Chris".length());
		chris.addToIndexes();

		Organisation uow = new Organisation(jCas);
		uow.setBegin(text.indexOf("University of Warwick"));
		uow.setEnd(uow.getBegin() + "University of Warwick".length());
		uow.addToIndexes();

		Organisation u = new Organisation(jCas);
		u.setBegin(text.indexOf("University", uow.getEnd()));
		u.setEnd(u.getBegin() + "University".length());
		u.addToIndexes();

		processJCas();

		List<ReferenceTarget> targets = new ArrayList<>(JCasUtil.select(jCas, ReferenceTarget.class));
		List<Organisation> location = new ArrayList<>(JCasUtil.select(jCas, Organisation.class));
		assertEquals(1, targets.size());
		assertSame(targets.get(0), location.get(0).getReferent());
		assertSame(targets.get(0), location.get(1).getReferent());
	}

	@Test
	public void testPreciseConstructApositive() throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "The prime minister, David Cameron explained on Tuesday.";
		// david camera - prime minister
		jCas.setDocumentText(text);

		processJCas();

		List<ReferenceTarget> targets = new ArrayList<>(JCasUtil.select(jCas, ReferenceTarget.class));
		assertEquals(1, targets.size());
	}

	@Test
	public void testPreciseConstructPredicate() throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "The David Cameron is the prime minister.";
		// david camera - prime minister
		jCas.setDocumentText(text);

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

	@Test
	public void testStrictHeadPass()
			throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "The Florida Supreme Court sat today, and the Florida Court made a decision.";
		jCas.setDocumentText(text);

		// TODO: This is just one of the three phases!

		// We need these in otherwise we just get one long setence from the mention detector

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
