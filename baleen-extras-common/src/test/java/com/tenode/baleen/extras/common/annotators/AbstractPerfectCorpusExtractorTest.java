package com.tenode.baleen.extras.common.annotators;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import uk.gov.dstl.baleen.annotators.testing.AbstractAnnotatorTest;
import uk.gov.dstl.baleen.types.common.Buzzword;

public class AbstractPerfectCorpusExtractorTest extends AbstractAnnotatorTest {

	public static class FakePerfectCorpusExtractor extends AbstractPerfectCorpusExtractor {

		@Override
		protected String[] getBuzzwords() {
			return new String[] { "test" };
		}
	}

	public AbstractPerfectCorpusExtractorTest() {
		super(FakePerfectCorpusExtractor.class);
	}

	@Test
	public void test() throws AnalysisEngineProcessException, ResourceInitializationException {
		jCas.setDocumentText("This is a test");

		processJCas();

		final List<Buzzword> list = new ArrayList<>(JCasUtil.select(jCas, Buzzword.class));
		assertEquals(1, list.size());
		assertEquals("test", list.get(0).getValue());
		assertEquals("test", list.get(0).getCoveredText());
	}

}
