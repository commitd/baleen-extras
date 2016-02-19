package com.tenode.baleen.annotators.coreference;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.ExternalResourceFactory;
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
import uk.gov.dstl.baleen.types.common.Person;
import uk.gov.dstl.baleen.types.semantic.Location;

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

}
