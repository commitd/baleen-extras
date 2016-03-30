package com.tenode.baleen.extras.clearnlp.annotators;

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
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.tenode.baleen.extras.clearnlp.annotators.ClearNlpParser;
import com.tenode.baleen.extras.clearnlp.annotators.ClearNlpTokeniser;
import com.tenode.baleen.extras.clearnlp.resources.ClearNlpLexica;

import uk.gov.dstl.baleen.annotators.testing.AnnotatorTestBase;
import uk.gov.dstl.baleen.types.language.Dependency;
import uk.gov.dstl.baleen.types.language.Sentence;

// NOTE: This is ignored since it requires 4gb + of memory (and doesn't seem to run on Jenkins, even with that settings)
@Ignore
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

		final ExternalResourceDescription tokensDesc = ExternalResourceFactory.createExternalResourceDescription(
				"lexica",
				ClearNlpLexica.class);

		final AnalysisEngineDescription tokeniserDesc = AnalysisEngineFactory.createEngineDescription(
				ClearNlpTokeniser.class,
				"lexica",
				tokensDesc);

		tokeniserAe = AnalysisEngineFactory.createEngine(tokeniserDesc);

		final AnalysisEngineDescription parserDesc = AnalysisEngineFactory.createEngineDescription(ClearNlpParser.class,
				"lexica",
				tokensDesc);

		ae = AnalysisEngineFactory.createEngine(parserDesc);
	}

	@Test
	public void test() throws Exception {
		final String text = "The fox jumps over the dog.";
		jCas.setDocumentText(text);

		SimplePipeline.runPipeline(jCas, tokeniserAe, ae);

		final Collection<Sentence> select = JCasUtil.select(jCas, Sentence.class);
		final Sentence s1 = select.iterator().next();

		final List<Dependency> dependencies = JCasUtil.selectCovered(jCas, Dependency.class, s1);

		for (final Dependency d : dependencies) {
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

		Assert.assertEquals(7, dependencies.size()); // 5 tokens in the first sentence

	}
}
