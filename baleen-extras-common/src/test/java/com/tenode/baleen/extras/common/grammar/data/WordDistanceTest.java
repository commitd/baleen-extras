package com.tenode.baleen.extras.common.grammar.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import uk.gov.dstl.baleen.types.language.WordToken;

public class WordDistanceTest {

	@Test
	public void testNoDistance() throws UIMAException {
		final JCas jCas = JCasFactory.createJCas();

		final WordToken word = new WordToken(jCas);

		final WordDistance wd = new WordDistance(word);

		assertEquals(0, wd.getDistance());
		assertSame(word, wd.getWord());
		assertEquals(1, wd.getWords().size());
		assertEquals(word, wd.getWords().get(0));
	}

	@Test
	public void testSomeDistance() throws UIMAException {
		final JCas jCas = JCasFactory.createJCas();

		final WordToken w1 = new WordToken(jCas);
		final WordToken w2 = new WordToken(jCas);
		final WordDistance a = new WordDistance(w1);
		final WordDistance b = new WordDistance(w2, a);

		assertEquals(1, b.getDistance());
		assertSame(w2, b.getWord());
		assertEquals(2, b.getWords().size());
		assertSame(w1, b.getWords().get(0));
		assertSame(w2, b.getWords().get(1));

	}
}
