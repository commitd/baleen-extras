package com.tenode.baleen.extras.common.jcas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Assert;
import org.junit.Test;

import uk.gov.dstl.baleen.types.common.Person;
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
		assertSame(e.getClass(), span.getClazz());

		final Span span2 = new Span(e, 0, 5);
		final Span span3 = new Span(e, 0, 6);
		final Span span4 = new Span(e, 1, 5);
		final Span span5 = new Span(new Person(jCas), 1, 5);

		assertEquals(span, span2);
		assertEquals(span.hashCode(), span2.hashCode());
		Assert.assertNotEquals(span, span3);
		Assert.assertNotEquals(span.hashCode(), span3.hashCode());
		Assert.assertNotEquals(span, span5);
		Assert.assertNotEquals(span.hashCode(), span5.hashCode());
		Assert.assertNotEquals(span, span4);
		Assert.assertNotEquals(span.hashCode(), span4.hashCode());

		// Check doesn't error
		span.toString();
	}

}
