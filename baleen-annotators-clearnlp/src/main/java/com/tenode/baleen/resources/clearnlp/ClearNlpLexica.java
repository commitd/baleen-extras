package com.tenode.baleen.resources.clearnlp;

import java.util.Arrays;
import java.util.Map;

import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import edu.emory.clir.clearnlp.component.utils.GlobalLexica;
import uk.gov.dstl.baleen.uima.BaleenResource;

public class ClearNlpLexica extends BaleenResource {

	@Override
	protected boolean doInitialize(ResourceSpecifier specifier, Map<String, Object> additionalParams)
			throws ResourceInitializationException {

		GlobalLexica.initDistributionalSemanticsWords(
				Arrays.asList("brown-rcv1.clean.tokenized-CoNLL03.txt-c1000-freq1.txt.xz"));

		return true;
	}
}
