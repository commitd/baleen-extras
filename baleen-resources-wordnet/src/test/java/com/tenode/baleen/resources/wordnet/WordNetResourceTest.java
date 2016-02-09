package com.tenode.baleen.resources.wordnet;

import java.util.Collections;
import java.util.Optional;

import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.impl.CustomResourceSpecifier_impl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.tenode.baleen.wordnet.resources.WordNetResource;

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
		final IndexWord word = wnr.lookupWord(POS.VERB, "employs").get();
		Assert.assertEquals("employ", word.getLemma());

		// Check that we can go from verb to noun
		assert word.getSenses().stream().filter(p -> p.getPOS() != POS.NOUN).count() > 0;
	}

}
