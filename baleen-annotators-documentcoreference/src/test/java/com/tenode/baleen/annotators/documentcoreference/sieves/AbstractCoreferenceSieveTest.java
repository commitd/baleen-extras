package com.tenode.baleen.annotators.coreference.sieves;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;

import com.tenode.baleen.annotators.documentcoreference.Coreference;
import com.tenode.baleen.annotators.maltparser.MaltParser;
import com.tenode.baleen.annotators.opennlp.OpenNLPParser;
import com.tenode.baleen.resources.documentcoreference.GenderMultiplicityResource;
import com.tenode.baleen.wordnet.annotators.WordNetLemmatizer;
import com.tenode.baleen.wordnet.resources.WordNetResource;

import uk.gov.dstl.baleen.annotators.language.OpenNLP;
import uk.gov.dstl.baleen.annotators.testing.AbstractMultiAnnotatorTest;
import uk.gov.dstl.baleen.resources.SharedOpenNLPModel;

public class AbstractCoreferenceSieveTest extends AbstractMultiAnnotatorTest {

	private final int singlePass;

	public AbstractCoreferenceSieveTest(int singlePass) {
		this.singlePass = singlePass;
	}

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
				createAnalysisEngine(Coreference.class, Coreference.PARAM_GENDER_MULTIPLICITY, gMDesc, "pass",
						singlePass, "pronomial", true));
	}
}
