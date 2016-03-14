package com.tenode.baleen.wordnet.resources;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.impl.CustomResourceSpecifier_impl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;

public class WordNetResourceTest {

	private WordNetResource wnr;

	@Before
	public void before() throws ResourceInitializationException {
		wnr = new WordNetResource();
		wnr.initialize(new CustomResourceSpecifier_impl(), Collections.emptyMap());
	}

	@Test
	public void testDestory() {
		wnr.destroy();
	}

	@Test
	public void testGetDictionary() {
		Assert.assertNotNull(wnr.getDictionary());
	}

	@Test
	public void testGetWord() throws JWNLException {
		final Optional<IndexWord> missing = wnr.getWord(POS.VERB, "employs");
		Assert.assertFalse(missing.isPresent());

		final IndexWord employ = wnr.getWord(POS.VERB, "employ").get();
		Assert.assertNotNull(employ);
		Assert.assertEquals("employ", employ.getLemma());
	}

	@Test
	public void testLookupWord() throws JWNLException {
		final IndexWord word = wnr.lookupWord(POS.VERB, "employing").get();
		Assert.assertEquals("employ", word.getLemma());
	}

	@Test
	public void testSuperSense() throws JWNLException {
		final List<String> word = wnr.getSuperSenses(POS.VERB, "employs").collect(Collectors.toList());

		Assert.assertEquals("verb.consumption", word.get(0));
		Assert.assertEquals("verb.social", word.get(1));
	}

	@Test
	public void testBestSuperSense() throws JWNLException {
		final Optional<String> word = wnr.getBestSuperSense(POS.VERB, "know");

		Assert.assertEquals("verb.cognition", word.get());
	}

	@Test
	public void testMissingLookupWord() throws JWNLException {
		final Optional<IndexWord> word1 = wnr.lookupWord(POS.VERB, "ascasdcscz");
		Assert.assertFalse(word1.isPresent());

		final Optional<IndexWord> word2 = wnr.getWord(POS.VERB, "ascasdcscz");
		Assert.assertFalse(word2.isPresent());

		final long count = wnr.getSuperSenses(POS.VERB, "ascasdcscz").count();
		Assert.assertEquals(0, count);
	}

}
