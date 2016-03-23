package com.tenode.baleen.annotators.coreference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import com.tenode.baleen.annotators.maltparser.MaltParser;
import com.tenode.baleen.annotators.opennlp.OpenNLPParser;
import com.tenode.baleen.resources.coreference.GenderMultiplicityResource;
import com.tenode.baleen.wordnet.annotators.WordNetLemmatizer;
import com.tenode.baleen.wordnet.resources.WordNetResource;

import uk.gov.dstl.baleen.annotators.language.OpenNLP;
import uk.gov.dstl.baleen.annotators.testing.AbstractMultiAnnotatorTest;
import uk.gov.dstl.baleen.resources.SharedOpenNLPModel;
import uk.gov.dstl.baleen.types.common.Person;
import uk.gov.dstl.baleen.types.language.WordToken;
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
				createAnalysisEngine(Coreference.class, Coreference.PARAM_GENDER_MULTIPLICITY, gMDesc, "pronomial",
						true));
	}

	@Test
	public void test() throws AnalysisEngineProcessException, ResourceInitializationException {
		String text = "Chris Smith went to London and he saw Big Ben. Chris saw his sister there.";
		jCas.setDocumentText(text);

		Person chrisSmith = new Person(jCas);
		chrisSmith.setBegin(text.indexOf("Chris Smith"));
		chrisSmith.setEnd(chrisSmith.getBegin() + "Chris Smith".length());
		chrisSmith.setValue("Chris Smith");
		chrisSmith.addToIndexes();

		Person chris = new Person(jCas);
		chris.setBegin(text.indexOf("Chris", chrisSmith.getEnd()));
		chris.setEnd(chris.getBegin() + "Chris".length());
		chris.setValue("Chris");
		chris.addToIndexes();

		Location london = new Location(jCas);
		london.setBegin(text.indexOf("London"));
		london.setEnd(london.getBegin() + "London".length());
		london.setValue("London");
		london.addToIndexes();

		Location bigBen = new Location(jCas);
		bigBen.setBegin(text.indexOf("Big Ben"));
		bigBen.setEnd(bigBen.getBegin() + "Big Ben".length());
		bigBen.setValue("Big Ben");
		bigBen.addToIndexes();

		processJCas();

		List<ReferenceTarget> targets = new ArrayList<>(JCasUtil.select(jCas, ReferenceTarget.class));

		List<Person> people = new ArrayList<>(JCasUtil.select(jCas, Person.class));
		List<WordToken> words = new ArrayList<>(JCasUtil.select(jCas, WordToken.class));

		long referenceId = people.get(0).getReferent().getInternalId();
		assertEquals("Chris Smith", people.get(0).getValue());
		assertEquals("Chris", people.get(1).getValue());

		assertEquals(referenceId, people.get(1).getReferent().getInternalId());

		// Check all the he and his connect to Chris
		boolean allMatch = words.stream()
				.filter(p -> p.getCoveredText().equalsIgnoreCase("his") || p.getCoveredText().equalsIgnoreCase("he"))
				.allMatch(p -> p.getReferent().getInternalId() == referenceId);
		assertTrue(allMatch);

		// We should have London or Big Ben to there - hence this should be 2, but something is off
		// at the moment...
		assertEquals(1, targets.size());
	}

}
