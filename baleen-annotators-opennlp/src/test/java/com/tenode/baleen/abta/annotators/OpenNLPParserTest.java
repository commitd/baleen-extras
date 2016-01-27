package com.tenode.baleen.abta.annotators;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ExternalResourceDescription;
import org.junit.Test;

import uk.gov.dstl.baleen.annotators.language.OpenNLP;
import uk.gov.dstl.baleen.annotators.testing.AnnotatorTestBase;
import uk.gov.dstl.baleen.resources.SharedOpenNLPModel;
import uk.gov.dstl.baleen.types.language.PhraseChunk;
import uk.gov.dstl.baleen.types.language.Sentence;

public class OpenNLPParserTest extends AnnotatorTestBase {

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.gov.dstl.uima.tests.TestBase#setupFromDescriptor(java.lang.String)
	 */
	AnalysisEngine ae;
	private AnalysisEngine openNlpAe;

	@Override
	public void beforeTest() throws UIMAException {
		super.beforeTest();

		ExternalResourceDescription parserChunkingDesc = ExternalResourceFactory
				.createExternalResourceDescription("parserChunking", SharedOpenNLPModel.class);

		AnalysisEngineDescription desc = AnalysisEngineFactory.createEngineDescription(OpenNLPParser.class,
				"parserChunking",
				parserChunkingDesc);

		ae = AnalysisEngineFactory.createEngine(desc);

		// Add in the OpenNLP implementation too, as its a prerequisite
		// (in theory we should test OpenNLPParser in isolation, but in practise it as this as a
		// dependency
		// so better test they work together)

		ExternalResourceDescription tokensDesc = ExternalResourceFactory.createExternalResourceDescription("tokens",
				SharedOpenNLPModel.class);
		ExternalResourceDescription sentencesDesc = ExternalResourceFactory
				.createExternalResourceDescription("sentences", SharedOpenNLPModel.class);
		ExternalResourceDescription posDesc = ExternalResourceFactory.createExternalResourceDescription("posTags",
				SharedOpenNLPModel.class);
		ExternalResourceDescription chunksDesc = ExternalResourceFactory
				.createExternalResourceDescription("phraseChunks", SharedOpenNLPModel.class);

		AnalysisEngineDescription openNlpDesc = AnalysisEngineFactory.createEngineDescription(OpenNLP.class, "tokens",
				tokensDesc, "sentences", sentencesDesc, "posTags", posDesc, "phraseChunks", chunksDesc);

		openNlpAe = AnalysisEngineFactory.createEngine(openNlpDesc);
	}

	@Test
	public void test() throws AnalysisEngineProcessException {

		String text = "The fox jumps over the dog.";
		jCas.setDocumentText(text);

		SimplePipeline.runPipeline(jCas, openNlpAe, ae);

		Collection<Sentence> select = JCasUtil.select(jCas, Sentence.class);
		Sentence s1 = select.iterator().next();

		List<PhraseChunk> phrases = JCasUtil.selectCovered(jCas, PhraseChunk.class, s1);
		assertEquals(4, phrases.size());
		assertEquals("The fox", phrases.get(0).getCoveredText());
		assertEquals("jumps over the dog", phrases.get(1).getCoveredText());
		assertEquals("over the dog", phrases.get(2).getCoveredText());
		assertEquals("the dog", phrases.get(3).getCoveredText());
	}

}
