package com.tenode.baleen.extras.clearnlp.resources;

import java.util.HashMap;

import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.impl.CustomResourceSpecifier_impl;
import org.junit.Test;

import com.tenode.baleen.extras.clearnlp.resources.ClearNlpLexica;

public class ClearNlpEntityDictionaryTest {

	@Test
	public void test() throws ResourceInitializationException {
		final ClearNlpLexica lexica = new ClearNlpLexica();
		lexica.initialize(new CustomResourceSpecifier_impl(), new HashMap<String, Object>());
		lexica.destroy();
	}

}
