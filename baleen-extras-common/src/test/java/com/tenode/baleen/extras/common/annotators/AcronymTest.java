package com.tenode.baleen.extras.common.annotators;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import uk.gov.dstl.baleen.annotators.testing.AbstractAnnotatorTest;
import uk.gov.dstl.baleen.types.semantic.Entity;

public class AcronymTest extends AbstractAnnotatorTest {

	public AcronymTest() {
		super(Acronyms.class);
	}

	@Test
	public void test() throws AnalysisEngineProcessException, ResourceInitializationException {
		jCas.setDocumentText("I'll see you at the BUG.");

		processJCas();

		final Collection<Entity> select = JCasUtil.select(jCas, Entity.class);
		final Entity next = select.iterator().next();
		assertEquals("BUG", next.getValue());
	}

}
