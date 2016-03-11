package com.tenode.baleen.extras.jobs.interactions.data;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import net.sf.extjwnl.data.POS;

public class WordTest {

	@Test
	public void test() {
		final Word w = new Word("lemma", POS.NOUN);
		assertEquals("lemma", w.getLemma());
		assertEquals(POS.NOUN, w.getPos());
	}

	@Test
	public void testEuqalsAndHashcode() {
		final Word w1n = new Word("lemma1", POS.NOUN);
		final Word w1v = new Word("lemma1", POS.VERB);
		final Word w2n = new Word("lemma2", POS.NOUN);
		final Word w2v = new Word("lemma2", POS.VERB);

		Assert.assertNotEquals(w1n, w1v);
		Assert.assertNotEquals(w1n, w2v);
		Assert.assertNotEquals(w1n, w2n);

	}
}
