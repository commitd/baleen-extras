//Dstl (c) Crown Copyright 2015
package com.tenode.baleen.abta.clearnlp;

import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ExternalResourceDescription;
import org.junit.Assert;
import org.junit.Test;

import com.tenode.baleen.annotators.clearnlp.ClearNlpTokeniser;
import com.tenode.baleen.resources.clearnlp.ClearNlpLexica;

import uk.gov.dstl.baleen.annotators.testing.AnnotatorTestBase;
import uk.gov.dstl.baleen.types.language.Sentence;
import uk.gov.dstl.baleen.types.language.WordToken;

public class ClearNLPTokeniserTest extends AnnotatorTestBase {

	private AnalysisEngine ae;

	@Override
	public void beforeTest() throws UIMAException {
		super.beforeTest();

		final ExternalResourceDescription tokensDesc = ExternalResourceFactory.createExternalResourceDescription(
				"lexica",
				ClearNlpLexica.class);

		final AnalysisEngineDescription desc = AnalysisEngineFactory.createEngineDescription(ClearNlpTokeniser.class,
				"lexica",
				tokensDesc);

		ae = AnalysisEngineFactory.createEngine(desc);
	}

	// NOTE: This test is based from the LanguageOpenNLPTest (but it doesn't have the parsechunks)
	@Test
	public void test() throws Exception {

		final String text = "This is some text. It has three sentences. The first sentence has four words.";

		jCas.setDocumentText(text);
		SimplePipeline.runPipeline(jCas, ae);

		Assert.assertEquals(3, JCasUtil.select(jCas, Sentence.class).size()); // 3 sentences

		final Sentence s1 = JCasUtil.selectByIndex(jCas, Sentence.class, 0);
		final List<WordToken> tokens = JCasUtil.selectCovered(jCas, WordToken.class, s1);

		Assert.assertEquals(5, tokens.size()); // 5 tokens in the first sentence
		Assert.assertEquals("NN", tokens.get(3).getPartOfSpeech()); // 4th token of first sentence
																	// is a

	}
}
