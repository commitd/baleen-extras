package com.tenode.baleen.abta.clearnlp;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ExternalResourceDescription;
import org.junit.Ignore;
import org.junit.Test;

import com.tenode.baleen.annotators.clearnlp.ClearNlpParser;
import com.tenode.baleen.annotators.clearnlp.ClearNlpTokeniser;
import com.tenode.baleen.resources.clearnlp.ClearNlpLexica;

import uk.gov.dstl.baleen.annotators.testing.AnnotatorTestBase;
import uk.gov.dstl.baleen.types.language.Dependency;
import uk.gov.dstl.baleen.types.language.Sentence;

public class ClearNlpParserTest extends AnnotatorTestBase {

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.gov.dstl.uima.tests.TestBase#setupFromDescriptor(java.lang.String)
	 */
	private AnalysisEngine ae;

	private AnalysisEngine tokeniserAe;

	@Override
	public void beforeTest() throws UIMAException {
		super.beforeTest();

		ExternalResourceDescription tokensDesc = ExternalResourceFactory.createExternalResourceDescription("lexica",
				ClearNlpLexica.class);

		AnalysisEngineDescription tokeniserDesc = AnalysisEngineFactory.createEngineDescription(ClearNlpTokeniser.class,
				"lexica",
				tokensDesc);

		tokeniserAe = AnalysisEngineFactory.createEngine(tokeniserDesc);

		AnalysisEngineDescription parserDesc = AnalysisEngineFactory.createEngineDescription(ClearNlpParser.class,
				"lexica",
				tokensDesc);

		ae = AnalysisEngineFactory.createEngine(parserDesc);
	}

	@Test
	// Ignored due to memory requirements for CI (excess of 4g)
	@Ignore
	public void test() throws Exception {
		String text = "The fox jumps over the dog.";
		jCas.setDocumentText(text);

		SimplePipeline.runPipeline(jCas, tokeniserAe, ae);

		Collection<Sentence> select = JCasUtil.select(jCas, Sentence.class);
		Sentence s1 = select.iterator().next();

		List<Dependency> dependencies = JCasUtil.selectCovered(jCas, Dependency.class, s1);

		for (Dependency d : dependencies) {
			System.out.println("------");
			System.out.println(d.getDependencyType());
			System.out.println(d.getDependent().getCoveredText());
			System.out.println(d.getGovernor().getCoveredText());
		}

		/*
		 * Output: ------ ROOT The The ------ nsubj fox jumps ------ ROOT jumps jumps ------ prep
		 * over jumps ------ det the dog ------ pobj dog over ------ punct . jumps
		 */

		// TODO: MOre tests after further use

		assertEquals(7, dependencies.size()); // 5 tokens in the first sentence

	}
}
