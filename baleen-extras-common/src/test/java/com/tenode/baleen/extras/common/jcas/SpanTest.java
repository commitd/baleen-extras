package com.tenode.baleen.extras.common.jcas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import uk.gov.dstl.baleen.types.semantic.Entity;

public class SpanTest {

	@Test
	public void test() throws UIMAException {
		final JCas jCas = JCasFactory.createJCas();
		final Entity e = new Entity(jCas);

		final Span span = new Span(e, 0, 5);

		assertEquals(0, span.getBegin());
		assertEquals(5, span.getEnd());
		assertSame(e, span.getEntity());
	}

}
