package com.tenode.baleen.resources.wordnet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;

import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.impl.CustomResourceSpecifier_impl;
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
	public void testGetDictionary() {
		assertNotNull(wnr.getDictionary());
	}

	@Test
	public void testGetWord() throws JWNLException {
		IndexWord word = wnr.getWord(POS.VERB, "employs");
		assertNotNull(word);
		assertEquals("employ", word.getLemma());

		// Check that we can go from verb to noun
		assert word.getSenses().stream().filter(p -> p.getPOS() != POS.NOUN).count() > 0;
	}

}
