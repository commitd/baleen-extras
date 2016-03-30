package com.tenode.baleen.extras.clearnlp.resources;

import java.util.Arrays;
import java.util.Map;

import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import edu.emory.clir.clearnlp.component.utils.GlobalLexica;
import uk.gov.dstl.baleen.uima.BaleenResource;

/**
 * A fake shared model which sets up the ClearNlp entity dictionary.
 * <p>
 * It is fake in the sense the this class provides no public additional functions.
 */
public class ClearNlpEntityDictionary extends BaleenResource {

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.gov.dstl.baleen.uima.BaleenResource#doInitialize(org.apache.uima.resource.
	 * ResourceSpecifier, java.util.Map)
	 */
	@Override
	protected boolean doInitialize(final ResourceSpecifier specifier, final Map<String, Object> additionalParams)
			throws ResourceInitializationException {

		GlobalLexica.initDistributionalSemanticsWords(
				Arrays.asList("brown-rcv1.clean.tokenized-CoNLL03.txt-c1000-freq1.txt.xz"));

		return true;
	}
}