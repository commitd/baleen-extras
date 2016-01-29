package com.tenode.baleen.resources.clearnlp;

import java.util.HashMap;

import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.impl.CustomResourceSpecifier_impl;
import org.junit.Test;

public class ClearNlpLexicaTest {

	@Test
	public void test() throws ResourceInitializationException {
		final ClearNlpEntityDictionary dictionary = new ClearNlpEntityDictionary();
		dictionary.initialize(new CustomResourceSpecifier_impl(), new HashMap<String, Object>());
		dictionary.destroy();
	}

}
