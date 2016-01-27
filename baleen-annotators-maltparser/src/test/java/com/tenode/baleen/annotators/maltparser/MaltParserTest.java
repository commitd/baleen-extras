package com.tenode.baleen.annotators.maltparser;

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
import uk.gov.dstl.baleen.types.language.Dependency;
import uk.gov.dstl.baleen.types.language.Sentence;

public class MaltParserTest extends AnnotatorTestBase {

	private AnalysisEngine ae;
	private AnalysisEngine openNlpAe;

	@Override
	public void beforeTest() throws UIMAException {
		super.beforeTest();

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

		AnalysisEngineDescription desc = AnalysisEngineFactory.createEngineDescription(MaltParser.class);

		ae = AnalysisEngineFactory.createEngine(desc);
	}

	@Test
	public void testProcess() throws AnalysisEngineProcessException {
		String text = "The fox jumps over the dog.";
		jCas.setDocumentText(text);

		SimplePipeline.runPipeline(jCas, openNlpAe, ae);

		Collection<Sentence> select = JCasUtil.select(jCas, Sentence.class);
		Sentence s1 = select.iterator().next();

		List<Dependency> dependencies = JCasUtil.selectCovered(jCas, Dependency.class, s1);

		for (Dependency d : dependencies) {
			System.out.println("------");
			System.out.println(d.getDependencyType());
			System.out.println(d.getDependent().getCoveredText());
			System.out.println(d.getGovernor().getCoveredText());
		}

		assertEquals(7, dependencies.size()); // 5 tokens in the first sentence }

	}
}
