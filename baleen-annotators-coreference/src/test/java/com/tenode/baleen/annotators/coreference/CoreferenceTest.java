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
import com.tenode.baleen.wordnet.annotators.WordNetLemmatizer;
import com.tenode.baleen.wordnet.resources.WordNetResource;

import uk.gov.dstl.baleen.annotators.language.OpenNLP;
import uk.gov.dstl.baleen.annotators.testing.AbstractMultiAnnotatorTest;
import uk.gov.dstl.baleen.resources.SharedOpenNLPModel;
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

		return asArray(
				createAnalysisEngine(OpenNLP.class, "tokens",
						tokensDesc, "sentences", sentencesDesc, "posTags", posDesc, "phraseChunks", chunksDesc),
				createAnalysisEngine(WordNetLemmatizer.class, "wordnet", wordnetDesc),
				createAnalysisEngine(OpenNLPParser.class, "parserChunking",
						parserChunkingDesc),
				createAnalysisEngine(MaltParser.class),
				createAnalysisEngine(Coreference.class));
	}

	@Test
	@Ignore
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
	@Ignore
	public void testExistingRefTargets() throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "Chris went to London and he saw Big Ben there.";
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
	@Ignore
	public void testExactStringMatch() throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "Chris went to London and in London he saw Big Ben.";
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
	@Ignore
	public void testRelaxStringMatch() throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "The University of Warwick is near Coventry and that was the University at which Chris studied.";
		jCas.setDocumentText(text);

		Person chris = new Person(jCas);
		chris.setBegin(text.indexOf("Chris"));
		chris.setEnd(chris.getBegin() + "Chris".length());
		chris.addToIndexes();

		processJCas();

		List<ReferenceTarget> targets = new ArrayList<>(JCasUtil.select(jCas, ReferenceTarget.class));
		List<Location> location = new ArrayList<>(JCasUtil.select(jCas, Location.class));
		assertEquals(1, targets.size());
		assertSame(targets.get(0), location.get(0).getReferent());
		assertSame(targets.get(0), location.get(1).getReferent());
	}

	@Test
	@Ignore
	public void testPreciseConstructApositive() throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "The prime minister, David Cameron explained on Tuesday.";
		jCas.setDocumentText(text);

		processJCas();

		List<ReferenceTarget> targets = new ArrayList<>(JCasUtil.select(jCas, ReferenceTarget.class));
		assertEquals(1, targets.size());
	}

	@Test
	public void testPreciseConstructPredicate() throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "The David Cameron is the prime minister.";
		jCas.setDocumentText(text);

		processJCas();

		List<ReferenceTarget> targets = new ArrayList<>(JCasUtil.select(jCas, ReferenceTarget.class));
		assertEquals(1, targets.size());
	}
}
